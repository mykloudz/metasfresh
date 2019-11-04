package de.metas.inoutcandidate.api.impl;

import static org.adempiere.model.InterfaceWrapperHelper.getTableId;
import static org.adempiere.model.InterfaceWrapperHelper.load;
import static org.adempiere.model.InterfaceWrapperHelper.loadByRepoIdAwares;
import static org.adempiere.model.InterfaceWrapperHelper.loadByRepoIdAwaresOutOfTrx;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.dao.IQueryFilter;
import org.adempiere.ad.dao.impl.ModelColumnNameValue;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.model.PlainContextAware;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.compiere.model.IQuery;
import org.compiere.model.MOrderLine;
import org.compiere.util.DB;
import org.slf4j.Logger;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import de.metas.bpartner.BPartnerId;
import de.metas.inout.model.I_M_InOutLine;
import de.metas.inoutcandidate.api.IShipmentScheduleAllocDAO;
import de.metas.inoutcandidate.api.IShipmentScheduleInvalidateRepository;
import de.metas.inoutcandidate.api.IShipmentSchedulePA;
import de.metas.inoutcandidate.api.OlAndSched;
import de.metas.inoutcandidate.api.ShipmentScheduleId;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.interfaces.I_C_OrderLine;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.logging.LogManager;
import de.metas.order.IOrderDAO;
import de.metas.order.OrderAndLineId;
import de.metas.order.OrderLineId;
import de.metas.process.PInstanceId;
import de.metas.product.ProductId;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

public class ShipmentSchedulePA implements IShipmentSchedulePA
{
	private final static Logger logger = LogManager.getLogger(ShipmentSchedulePA.class);

	/**
	 * Order by clause used to fetch {@link I_M_ShipmentSchedule}s.
	 *
	 * NOTE: this ordering is VERY important because that's the order in which QtyOnHand will be allocated too.
	 */
	private static final String ORDER_CLAUSE = "\n ORDER BY " //
			//
			// Priority
			+ "\n   COALESCE(" + I_M_ShipmentSchedule.COLUMNNAME_PriorityRule_Override + ", " + I_M_ShipmentSchedule.COLUMNNAME_PriorityRule + ")," //
			//
			// QtyToDeliver_Override:
			// NOTE: (Mark) If we want to force deliverying something, that shall get higher priority,
			// so that's why QtyToDeliver_Override is much more important than PreparationDate, DeliveryDate etc
			+ "\n   COALESCE(" + I_M_ShipmentSchedule.COLUMNNAME_QtyToDeliver_Override + ", 0) DESC,"
			//
			// Preparation Date
			+ "\n   " + I_M_ShipmentSchedule.COLUMNNAME_PreparationDate + ","
			//
			// Delivery Date
			// NOTE: stuff that shall be deivered first shall have a higher prio
			+ "\n   COALESCE(" + I_M_ShipmentSchedule.COLUMNNAME_DeliveryDate_Override + ", " + I_M_ShipmentSchedule.COLUMNNAME_DeliveryDate + ")," // stuff that shall be deivered first shall have
			// a higher prio
			//
			// Date Ordered
			+ "\n   " + I_M_ShipmentSchedule.COLUMNNAME_DateOrdered + ", "
			//
			// Order Line
			+ "\n   " + I_M_ShipmentSchedule.COLUMNNAME_C_OrderLine_ID;

	@Override
	public I_M_ShipmentSchedule getById(@NonNull final ShipmentScheduleId id)
	{
		return getById(id, I_M_ShipmentSchedule.class);
	}

	@Override
	public <T extends I_M_ShipmentSchedule> T getById(@NonNull final ShipmentScheduleId id, @NonNull final Class<T> modelClass)
	{
		final T shipmentSchedule = load(id, modelClass);
		if (shipmentSchedule == null)
		{
			throw new AdempiereException("@NotFound@ @M_ShipmentSchedule_ID@: " + id);
		}
		return shipmentSchedule;
	}

	@Override
	public Map<ShipmentScheduleId, I_M_ShipmentSchedule> getByIdsOutOfTrx(@NonNull final Set<ShipmentScheduleId> ids)
	{
		return getByIdsOutOfTrx(ids, I_M_ShipmentSchedule.class);
	}

	@Override
	public <T extends I_M_ShipmentSchedule> Map<ShipmentScheduleId, T> getByIdsOutOfTrx(@NonNull final Set<ShipmentScheduleId> ids, final Class<T> modelClass)
	{
		final List<T> shipmentSchedules = loadByRepoIdAwaresOutOfTrx(ids, modelClass);
		return Maps.uniqueIndex(shipmentSchedules, ss -> ShipmentScheduleId.ofRepoId(ss.getM_ShipmentSchedule_ID()));
	}

	@Override
	public Map<ShipmentScheduleId, I_M_ShipmentSchedule> getByIds(@NonNull final Set<ShipmentScheduleId> ids)
	{
		final List<I_M_ShipmentSchedule> shipmentSchedules = loadByRepoIdAwares(ids, I_M_ShipmentSchedule.class);
		return Maps.uniqueIndex(shipmentSchedules, ss -> ShipmentScheduleId.ofRepoId(ss.getM_ShipmentSchedule_ID()));
	}

	@Override
	public I_M_ShipmentSchedule getByOrderLineId(@NonNull final OrderLineId orderLineId)
	{
		return getByOrderLineIdQuery(orderLineId)
				.create()
				.firstOnly(I_M_ShipmentSchedule.class);
	}

	@Override
	public ShipmentScheduleId getShipmentScheduleIdByOrderLineId(@NonNull final OrderLineId orderLineId)
	{
		return getByOrderLineIdQuery(orderLineId)
				.create()
				.firstIdOnly(ShipmentScheduleId::ofRepoIdOrNull);
	}

	private IQueryBuilder<I_M_ShipmentSchedule> getByOrderLineIdQuery(@NonNull final OrderLineId orderLineId)
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_ShipmentSchedule.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_AD_Table_ID, InterfaceWrapperHelper.getTableId(I_C_OrderLine.class))
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_Record_ID, orderLineId)
				.orderBy(I_M_ShipmentSchedule.COLUMNNAME_M_ShipmentSchedule_ID);
	}

	@Override
	public List<I_M_ShipmentSchedule> retrieveUnprocessedForRecord(@NonNull final TableRecordReference recordRef)
	{
		return Services.get(IQueryBL.class).createQueryBuilder(I_M_ShipmentSchedule.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMN_Processed, false)
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_AD_Table_ID, recordRef.getAD_Table_ID())
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMN_Record_ID, recordRef.getRecord_ID())
				.orderBy(I_M_ShipmentSchedule.COLUMN_M_ShipmentSchedule_ID)
				.create()
				.listImmutable(I_M_ShipmentSchedule.class);
	}

	/**
	 * Note: The {@link I_C_OrderLine}s contained in the {@link OlAndSched} instances are {@link MOrderLine}s.
	 */
	@Override
	public List<OlAndSched> retrieveInvalid(@NonNull final PInstanceId pinstanceId)
	{
		final IShipmentScheduleInvalidateRepository invalidSchedulesRepo = Services.get(IShipmentScheduleInvalidateRepository.class);
		// 1.
		// Mark the M_ShipmentSchedule_Recompute records that point to the scheds which we will work with
		// This allows us to distinguish them from records created later

		// task 08727: Tag the recompute records out-of-trx.
		// This is crucial because the invalidation-SQL checks if there exist un-tagged recompute records to avoid creating too many unneeded records.
		// So if the tagging was in-trx, then the invalidation-SQL would still see them as un-tagged and therefore the invalidation would fail.
		invalidSchedulesRepo.markAllToRecomputeOutOfTrx(pinstanceId);

		// 2.
		// Load the scheds the are pointed to by our marked M_ShipmentSchedule_Recompute records
		final IQueryBL queryBL = Services.get(IQueryBL.class);
		final List<I_M_ShipmentSchedule> shipmentSchedules = queryBL
				.createQueryBuilder(I_M_ShipmentSchedule.class)
				.addOnlyActiveRecordsFilter()
				.filter(invalidSchedulesRepo.createInvalidShipmentSchedulesQueryFilter(pinstanceId))
				.create()
				.setOrderBy(queryBL.createSqlQueryOrderBy(ORDER_CLAUSE))
				.list();
		if (shipmentSchedules.isEmpty())
		{
			return ImmutableList.of();
		}

		return createOlAndScheds(shipmentSchedules);
	}

	private static final OrderAndLineId extractOrderAndLineId(final I_M_ShipmentSchedule shipmentSchedule)
	{
		return OrderAndLineId.ofRepoIdsOrNull(shipmentSchedule.getC_Order_ID(), shipmentSchedule.getC_OrderLine_ID());
	}

	private List<OlAndSched> createOlAndScheds(final List<I_M_ShipmentSchedule> shipmentSchedules)
	{
		final Set<OrderAndLineId> orderLineIds = shipmentSchedules.stream()
				.map(shipmentSchedule -> extractOrderAndLineId(shipmentSchedule))
				.filter(Predicates.notNull())
				.collect(ImmutableSet.toImmutableSet());

		final Map<OrderAndLineId, I_C_OrderLine> orderLines = Services.get(IOrderDAO.class).getOrderLinesByIds(orderLineIds);

		final List<OlAndSched> result = new ArrayList<>();

		for (final I_M_ShipmentSchedule schedule : shipmentSchedules)
		{
			final OrderAndLineId orderLineId = extractOrderAndLineId(schedule);
			final I_C_OrderLine orderLine;
			if (orderLineId == null)
			{
				orderLine = null;
			}
			else
			{
				orderLine = orderLines.get(orderLineId);
			}

			final OlAndSched olAndSched = OlAndSched.builder()
					.shipmentSchedule(schedule)
					.orderLineOrNull(orderLine)
					.build();
			result.add(olAndSched);
		}
		return result;
	}

	@Override
	public void deleteSchedulesWithoutOrderLines()
	{
		final String sql = "DELETE FROM " + I_M_ShipmentSchedule.Table_Name + " s "
				+ "WHERE s.AD_Table_ID=" + getTableId(I_C_OrderLine.class) + " "
				+ "AND NOT EXISTS ("
				+ "   select 1 from " + org.compiere.model.I_C_OrderLine.Table_Name + " ol "
				+ "   where ol." + org.compiere.model.I_C_OrderLine.COLUMNNAME_C_OrderLine_ID + "=s." + I_M_ShipmentSchedule.COLUMNNAME_C_OrderLine_ID
				+ ")";

		final int delCnt = DB.executeUpdateEx(sql, ITrx.TRXNAME_ThreadInherited);

		logger.debug("Deleted {} shipment schedules whose C_OrderLine is already gone", delCnt);
	}

	@Override
	public void setIsDiplayedForProduct(@NonNull final ProductId productId, final boolean displayed)
	{
		Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_ShipmentSchedule.class)
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_M_Product_ID, productId)
				.create()
				.updateDirectly()
				.addSetColumnValue(I_M_ShipmentSchedule.COLUMNNAME_IsDisplayed, displayed)
				.execute();
	}

	@Override
	public Stream<I_M_ShipmentSchedule> streamUnprocessedByPartnerIdAndAllowConsolidateInOut(
			@NonNull final BPartnerId bpartnerId,
			final boolean allowConsolidateInOut)
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_ShipmentSchedule.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_Processed, false)
				.addCoalesceEqualsFilter(bpartnerId, I_M_ShipmentSchedule.COLUMNNAME_C_BPartner_Override_ID, I_M_ShipmentSchedule.COLUMNNAME_C_BPartner_ID)
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMN_AllowConsolidateInOut, allowConsolidateInOut)
				.orderBy(I_M_ShipmentSchedule.COLUMNNAME_M_ShipmentSchedule_ID)
				//
				.create()
				.setOption(IQuery.OPTION_GuaranteedIteratorRequired, true) // because the Processed flag might change while we iterate
				.setOption(IQuery.OPTION_IteratorBufferSize, 500)
				.iterateAndStream();
	}

	/**
	 * Mass-update a given shipment schedule column.
	 *
	 * If there were any changes and the invalidate parameter is on true, those shipment schedules will be invalidated.
	 *
	 * @param inoutCandidateColumnName {@link I_M_ShipmentSchedule}'s column to update
	 * @param value value to set (you can also use {@link ModelColumnNameValue})
	 * @param updateOnlyIfNull if true then it will update only if column value is null (not set)
	 * @param selectionId ShipmentSchedule selection (AD_PInstance_ID)
	 * @param trxName
	 */
	private final <ValueType> void updateColumnForSelection(
			final String inoutCandidateColumnName,
			final ValueType value,
			final boolean updateOnlyIfNull,
			final PInstanceId selectionId,
			final boolean invalidate)
	{
		final IQueryBL queryBL = Services.get(IQueryBL.class);

		//
		// Create the selection which we will need to update
		final IQueryBuilder<I_M_ShipmentSchedule> selectionQueryBuilder = queryBL
				.createQueryBuilder(I_M_ShipmentSchedule.class)
				.setOnlySelection(selectionId)
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_Processed, false) // do not touch the processed shipment schedules
		;

		if (updateOnlyIfNull)
		{
			selectionQueryBuilder.addEqualsFilter(inoutCandidateColumnName, null);
		}
		final PInstanceId selectionToUpdateId = selectionQueryBuilder.create().createSelection();
		if (selectionToUpdateId == null)
		{
			// nothing to update
			return;
		}

		//
		// Update our new selection
		queryBL.createQueryBuilder(I_M_ShipmentSchedule.class)
				.setOnlySelection(selectionToUpdateId)
				.create()
				.updateDirectly()
				.addSetColumnValue(inoutCandidateColumnName, value)
				.execute();

		//
		// Invalidate the inout candidates which we updated
		if (invalidate)
		{
			final IShipmentScheduleInvalidateRepository invalidSchedulesRepo = Services.get(IShipmentScheduleInvalidateRepository.class);
			invalidSchedulesRepo.invalidateSchedulesForSelection(selectionToUpdateId);
		}
	}

	@Override
	public void updateDeliveryDate_Override(final Timestamp deliveryDate, final PInstanceId pinstanceId)
	{
		// No need of invalidation after deliveryDate update because it is not used for anything else than preparation date calculation.
		// In case this calculation is needed, the invalidation will be done on preparation date updating
		// see de.metas.inoutcandidate.api.impl.ShipmentSchedulePA.updatePreparationDate_Override(Timestamp, int, String)

		final boolean invalidate = false;

		updateColumnForSelection(
				I_M_ShipmentSchedule.COLUMNNAME_DeliveryDate_Override,               // inoutCandidateColumnName
				deliveryDate,               // value
				false,               // updateOnlyIfNull
				pinstanceId,               // selectionId
				invalidate               // invalidate schedules = false
		);
	}

	@Override
	public void updatePreparationDate_Override(final Timestamp preparationDate, final PInstanceId pinstanceId)
	{
		// in case the preparation date is given, it will only be set. No Invalidation needed
		// in case it is not given (null) an invalidation is needed because it will be calculated based on the delivery date

		boolean invalidate = false;

		if (preparationDate == null)
		{
			invalidate = true;
		}
		updateColumnForSelection(
				I_M_ShipmentSchedule.COLUMNNAME_PreparationDate_Override,               // inoutCandidateColumnName
				preparationDate,               // value
				false,               // updateOnlyIfNull
				pinstanceId,               // selectionId
				invalidate               // invalidate schedules
		);
	}

	@Override
	public IQueryBuilder<I_M_ShipmentSchedule> createQueryForShipmentScheduleSelection(final Properties ctx, final IQueryFilter<I_M_ShipmentSchedule> userSelectionFilter)
	{
		final IQueryBuilder<I_M_ShipmentSchedule> queryBuilder = Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_ShipmentSchedule.class, ctx, ITrx.TRXNAME_None)
				.filter(userSelectionFilter)
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_Processed, false)
				.addOnlyActiveRecordsFilter()
				.addOnlyContextClient();

		return queryBuilder;
	}

	@Override
	public Set<I_M_ShipmentSchedule> retrieveForInvoiceCandidate(@NonNull final I_C_Invoice_Candidate candidate)
	{
		final Set<I_M_ShipmentSchedule> schedules;

		final int tableID = candidate.getAD_Table_ID();

		// invoice candidate references an orderline
		if (tableID == InterfaceWrapperHelper.getTableId(I_C_OrderLine.class))
		{
			Check.errorIf(candidate.getC_OrderLine_ID() <= 0, "C_Invoice_Candidate has AD_Table_ID=>C_OrderLine, but does not reference any C_OrderLine_ID; candidate={}", candidate);
			final OrderLineId orderLineId = OrderLineId.ofRepoId(candidate.getC_OrderLine_ID());
			final I_M_ShipmentSchedule shipmentSchedule = getByOrderLineId(orderLineId);
			schedules = shipmentSchedule != null ? ImmutableSet.of(shipmentSchedule) : ImmutableSet.of();
		}

		// invoice candidate references an inoutline
		else if (tableID == InterfaceWrapperHelper.getTableId(I_M_InOutLine.class))
		{
			final I_M_InOutLine inoutLine = TableRecordReference
					.ofReferenced(candidate)
					.getModel(PlainContextAware.newWithThreadInheritedTrx(), I_M_InOutLine.class);

			schedules = ImmutableSet.copyOf(retrieveForInOutLine(inoutLine));
		}
		else
		{
			schedules = ImmutableSet.of();
		}

		return schedules;
	}

	@Override
	public Set<I_M_ShipmentSchedule> retrieveForInOutLine(@NonNull final I_M_InOutLine inoutLine)
	{
		final Map<Integer, I_M_ShipmentSchedule> schedules = new LinkedHashMap<>();

		// add all the shipment schedules from the QtyPicked entries
		final Map<Integer, I_M_ShipmentSchedule> schedulesForInOutLine = Services.get(IShipmentScheduleAllocDAO.class).retrieveSchedulesForInOutLineQuery(inoutLine)
				.create()
				.mapById(I_M_ShipmentSchedule.class);
		schedules.putAll(schedulesForInOutLine);

		// fallback to the case when the inoutline has an orderline set but has no Qty Picked entries
		// this happens when we create manual Shipments
		final OrderLineId orderLineId = OrderLineId.ofRepoIdOrNull(inoutLine.getC_OrderLine_ID());
		if (orderLineId != null)
		{
			final I_M_ShipmentSchedule schedForOrderLine = getByOrderLineId(orderLineId);
			if (schedForOrderLine != null)
			{
				schedules.put(schedForOrderLine.getM_ShipmentSchedule_ID(), schedForOrderLine);
			}
		}

		return ImmutableSet.copyOf(schedules.values());
	}

	@Override
	public void deleteAllForReference(
			@Nullable final TableRecordReference referencedRecord)
	{
		if (referencedRecord == null)
		{
			logger.debug("given parameter referencedRecord is null; nothing to delete");
			return;
		}
		final int deletedCount = Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_ShipmentSchedule.class)
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_AD_Table_ID, referencedRecord.getAD_Table_ID())
				.addEqualsFilter(I_M_ShipmentSchedule.COLUMN_Record_ID, referencedRecord.getRecord_ID())
				.create()
				.delete();
		logger.debug("Deleted {} M_ShipmentSchedule records for referencedRecord={}", deletedCount, referencedRecord);
	}

	@Override
	public Set<ProductId> getProductIdsByShipmentScheduleIds(@NonNull final Collection<ShipmentScheduleId> shipmentScheduleIds)
	{
		if (shipmentScheduleIds.isEmpty())
		{
			return ImmutableSet.of();
		}

		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_ShipmentSchedule.class)
				.addInArrayFilter(I_M_ShipmentSchedule.COLUMN_M_ShipmentSchedule_ID, shipmentScheduleIds)
				.create()
				.listDistinct(I_M_ShipmentSchedule.COLUMNNAME_M_Product_ID, Integer.class)
				.stream()
				.map(ProductId::ofRepoId)
				.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public void save(@NonNull final I_M_ShipmentSchedule record)
	{
		InterfaceWrapperHelper.saveRecord(record);
	}
}
