package de.metas.contracts.inoutcandidate;

import static java.math.BigDecimal.ZERO;
import static org.adempiere.model.InterfaceWrapperHelper.create;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.save;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.impl.CompareQueryFilter.Operator;
import org.adempiere.mm.attributes.api.IAttributeSetInstanceBL;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.service.ISysConfigBL;
import org.adempiere.util.lang.IContextAware;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.adempiere.warehouse.LocatorId;
import org.adempiere.warehouse.WarehouseId;
import org.adempiere.warehouse.api.IWarehouseDAO;
import org.compiere.SpringContextHolder;
import org.compiere.model.IQuery;
import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_Product;
import org.compiere.model.X_M_Product;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.metas.contracts.IFlatrateBL;
import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.model.I_C_SubscriptionProgress;
import de.metas.contracts.model.X_C_SubscriptionProgress;
import de.metas.document.IDocumentLocationBL;
import de.metas.document.model.IDocumentLocation;
import de.metas.inoutcandidate.api.IDeliverRequest;
import de.metas.inoutcandidate.api.IShipmentScheduleEffectiveBL;
import de.metas.inoutcandidate.api.IShipmentScheduleInvalidateRepository;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.inoutcandidate.model.X_M_ShipmentSchedule;
import de.metas.inoutcandidate.spi.ShipmentScheduleHandler;
import de.metas.inoutcandidate.spi.ShipmentScheduleReferencedLine;
import de.metas.product.IProductBL;
import de.metas.storage.impl.ImmutableStorageSegment;
import de.metas.util.Check;
import de.metas.util.Loggables;
import de.metas.util.Services;
import de.metas.util.time.SystemTime;
import lombok.NonNull;

public class SubscriptionShipmentScheduleHandler extends ShipmentScheduleHandler
{
	@VisibleForTesting
	static final String SYSCONFIG_CREATE_SHIPMENT_SCHEDULES_IN_ADVANCE_DAYS = "C_SubscriptionProgress.Create_ShipmentSchedulesInAdvanceDays";

	@Override
	public List<I_M_ShipmentSchedule> createCandidatesFor(@NonNull final Object model)
	{
		final IDocumentLocationBL documentLocationBL = Services.get(IDocumentLocationBL.class);
		final I_C_SubscriptionProgress subscriptionLine = create(model, I_C_SubscriptionProgress.class);

		if (subscriptionLine.getQty().signum() <= 0)
		{
			Loggables.addLog(
					"Skip C_SubscriptionProgress_ID={} with Qty={}",
					subscriptionLine.getC_SubscriptionProgress_ID(), subscriptionLine.getQty());
			return ImmutableList.of();
		}
		final I_M_ShipmentSchedule newSched = newInstance(I_M_ShipmentSchedule.class, model);

		final int tableId = InterfaceWrapperHelper.getTableId(I_C_SubscriptionProgress.class);
		newSched.setAD_Table_ID(tableId);
		newSched.setRecord_ID(subscriptionLine.getC_SubscriptionProgress_ID());

		final I_C_Flatrate_Term term = subscriptionLine.getC_Flatrate_Term();

		Check.assume(term.getM_Product_ID() > 0, term + " has M_Product_ID>0");

		newSched.setM_Product_ID(term.getM_Product_ID());
		Services.get(IAttributeSetInstanceBL.class).cloneASI(term, newSched);

		newSched.setProductDescription(null);

		updateNewSchedWithValuesFromReferencedLine(newSched);

		final I_C_DocType doctypeForTerm = Services.get(IFlatrateBL.class).getDocTypeFor(term);
		newSched.setC_DocType_ID(doctypeForTerm.getC_DocType_ID());
		newSched.setDocSubType(doctypeForTerm.getDocSubType());

		newSched.setPriorityRule(X_M_ShipmentSchedule.PRIORITYRULE_High);

		newSched.setC_BPartner_Location_ID(subscriptionLine.getDropShip_Location_ID());
		newSched.setC_BPartner_ID(subscriptionLine.getDropShip_BPartner_ID());
		newSched.setAD_User_ID(subscriptionLine.getDropShip_User_ID());
		newSched.setBill_BPartner_ID(newSched.getC_BPartner_ID());

		final IDocumentLocation documentLocation = InterfaceWrapperHelper.create(newSched, IDocumentLocation.class);
		documentLocationBL.setBPartnerAddress(documentLocation);

		newSched.setDeliveryRule(term.getDeliveryRule());
		newSched.setDeliveryViaRule(term.getDeliveryViaRule());

		newSched.setQtyOrdered(subscriptionLine.getQty());
		newSched.setQtyOrdered_Calculated(subscriptionLine.getQty());
		newSched.setQtyReserved(subscriptionLine.getQty());

		newSched.setLineNetAmt(newSched.getQtyReserved().multiply(term.getPriceActual()));

		newSched.setDateOrdered(subscriptionLine.getEventDate());

		newSched.setAD_Org_ID(subscriptionLine.getAD_Org_ID());

		Check.assume(newSched.getAD_Client_ID() == subscriptionLine.getAD_Client_ID(),
				"The new M_ShipmentSchedule has the same AD_Client_ID as " + subscriptionLine + ", i.e." + newSched.getAD_Client_ID() + " == " + subscriptionLine.getAD_Client_ID());

		// only display item products
		// note: at least for C_Subscription_Progress records, we won't even create records for non-items
		final boolean display = Services.get(IProductBL.class).isItem(term.getM_Product_ID());
		newSched.setIsDisplayed(display);

		save(newSched);

		subscriptionLine.setStatus(X_C_SubscriptionProgress.STATUS_Open);
		subscriptionLine.setM_ShipmentSchedule_ID(newSched.getM_ShipmentSchedule_ID());
		save(subscriptionLine);

		invalidateCandidatesFor(subscriptionLine);

		// Note: AllowConsolidateInOut is set on the first update of this schedule
		return ImmutableList.of(newSched);
	}

	private void updateNewSchedWithValuesFromReferencedLine(@NonNull final I_M_ShipmentSchedule newSched)
	{
		final ShipmentScheduleReferencedLine subscriptionFromgressInfos = SpringContextHolder.instance
				.getBean(ShipmentScheduleSubscriptionReferenceProvider.class)
				.provideFor(newSched);

		newSched.setM_Warehouse_ID(subscriptionFromgressInfos.getWarehouseId().getRepoId());
		newSched.setPreparationDate(TimeUtil.asTimestamp(subscriptionFromgressInfos.getPreparationDate()));
		newSched.setDeliveryDate(TimeUtil.asTimestamp(subscriptionFromgressInfos.getDeliveryDate()));
	}

	@Override
	public void invalidateCandidatesFor(@NonNull final Object model)
	{
		final I_C_SubscriptionProgress subscriptionLine = InterfaceWrapperHelper.create(model, I_C_SubscriptionProgress.class);
		if (subscriptionLine.getM_ShipmentSchedule_ID() >= 0)
		{
			return;
		}

		final ImmutableStorageSegment segment = createStorageSegmentFor(subscriptionLine);

		final IShipmentScheduleInvalidateRepository invalidSchedulesRepo = Services.get(IShipmentScheduleInvalidateRepository.class);
		invalidSchedulesRepo.invalidateStorageSegments(ImmutableSet.of(segment));
	}

	private ImmutableStorageSegment createStorageSegmentFor(@NonNull final I_C_SubscriptionProgress subscriptionLine)
	{
		final IShipmentScheduleEffectiveBL shipmentScheduleEffectiveBL = Services.get(IShipmentScheduleEffectiveBL.class);
		final IWarehouseDAO warehouseDAO = Services.get(IWarehouseDAO.class);

		final WarehouseId warehouseId = shipmentScheduleEffectiveBL.getWarehouseId(subscriptionLine.getM_ShipmentSchedule());
		final List<LocatorId> locatorIds = warehouseDAO.getLocatorIds(warehouseId);

		final ImmutableStorageSegment segment = ImmutableStorageSegment.builder()
				.M_Product_ID(subscriptionLine.getC_Flatrate_Term().getM_Product_ID())
				.C_BPartner_ID(subscriptionLine.getDropShip_BPartner_ID())
				.M_Locator_IDs(LocatorId.toRepoIds(locatorIds))
				.build();
		return segment;
	}

	@Override
	public String getSourceTable()
	{
		return I_C_SubscriptionProgress.Table_Name;
	}

	@Override
	public Iterator<? extends Object> retrieveModelsWithMissingCandidates(
			final Properties ctx,
			final String trxName)
	{
		final int daysInAdvance = Services.get(ISysConfigBL.class).getIntValue(SYSCONFIG_CREATE_SHIPMENT_SCHEDULES_IN_ADVANCE_DAYS, 0, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
		final Timestamp eventDateMaximum = TimeUtil.addDays(SystemTime.asTimestamp(), daysInAdvance);

		final IQueryBL queryBL = Services.get(IQueryBL.class);

		final IQuery<I_C_Flatrate_Term> itemProductQuery = queryBL.createQueryBuilder(I_M_Product.class)
				.addEqualsFilter(I_M_Product.COLUMN_ProductType, X_M_Product.PRODUCTTYPE_Item)
				.andCollectChildren(I_C_Flatrate_Term.COLUMN_M_Product_ID)
				.addOnlyActiveRecordsFilter()
				.create();

		// Note: we used to also check if there is an active I_M_IolCandHandler_Log record referencing the C_SubscriptionProgress, but I don't see why.
		final Iterator<I_C_SubscriptionProgress> subscriptionLines = queryBL
				.createQueryBuilder(I_C_SubscriptionProgress.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_SubscriptionProgress.COLUMN_Status, X_C_SubscriptionProgress.STATUS_Planned)
				.addEqualsFilter(I_C_SubscriptionProgress.COLUMN_EventType, X_C_SubscriptionProgress.EVENTTYPE_Delivery)
				.addCompareFilter(I_C_SubscriptionProgress.COLUMN_EventDate, Operator.LESS_OR_EQUAL, eventDateMaximum)
				.addCompareFilter(I_C_SubscriptionProgress.COLUMN_Qty, Operator.GREATER, ZERO)
				.addEqualsFilter(I_C_SubscriptionProgress.COLUMN_M_ShipmentSchedule_ID, null) // we didn't do this in the very old code which i found
				.addInSubQueryFilter(
						I_C_SubscriptionProgress.COLUMN_C_Flatrate_Term_ID,
						I_C_Flatrate_Term.COLUMN_C_Flatrate_Term_ID,
						itemProductQuery)
				.addOnlyContextClient(ctx)
				.orderBy().addColumn(I_C_SubscriptionProgress.COLUMN_C_SubscriptionProgress_ID).endOrderBy()
				.create()
				.setOption(IQuery.OPTION_GuaranteedIteratorRequired, true)
				.setOption(IQuery.OPTION_IteratorBufferSize, 500)
				.iterate(I_C_SubscriptionProgress.class);

		return subscriptionLines;
	}

	@Override
	public IDeliverRequest createDeliverRequest(final I_M_ShipmentSchedule sched, final I_C_OrderLine salesOrderLine)
	{
		final I_C_SubscriptionProgress subscriptionLine = getSubscriptionProgress(sched);
		return subscriptionLine::getQty;
	}

	private I_C_SubscriptionProgress getSubscriptionProgress(final I_M_ShipmentSchedule sched)
	{
		final IContextAware contextAware = InterfaceWrapperHelper.getContextAware(sched);
		final TableRecordReference ref = TableRecordReference.of(sched.getAD_Table_ID(), sched.getRecord_ID());
		return ref.getModel(contextAware, I_C_SubscriptionProgress.class);
	}
}
