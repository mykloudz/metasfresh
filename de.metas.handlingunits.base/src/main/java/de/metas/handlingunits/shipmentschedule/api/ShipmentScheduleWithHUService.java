package de.metas.handlingunits.shipmentschedule.api;

import static org.adempiere.model.InterfaceWrapperHelper.load;
import static org.adempiere.model.InterfaceWrapperHelper.loadOutOfTrx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nullable;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.service.ISysConfigBL;
import org.adempiere.util.lang.IContextAware;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_InOutLine;
import org.compiere.model.I_M_Product;
import org.compiere.util.Env;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import ch.qos.logback.classic.Level;
import de.metas.handlingunits.HUConstants;
import de.metas.handlingunits.IHUContext;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.IMutableHUContext;
import de.metas.handlingunits.allocation.IAllocationRequest;
import de.metas.handlingunits.allocation.IAllocationResult;
import de.metas.handlingunits.allocation.IAllocationSource;
import de.metas.handlingunits.allocation.ILUTUConfigurationFactory;
import de.metas.handlingunits.allocation.ILUTUProducerAllocationDestination;
import de.metas.handlingunits.allocation.impl.AllocationUtils;
import de.metas.handlingunits.allocation.impl.GenericAllocationSourceDestination;
import de.metas.handlingunits.allocation.impl.HULoader;
import de.metas.handlingunits.allocation.impl.LULoader;
import de.metas.handlingunits.allocation.transfer.HUTransformService;
import de.metas.handlingunits.allocation.transfer.HUTransformService.HUsToNewCUsRequest;
import de.metas.handlingunits.exceptions.HUException;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.model.I_M_HU_LUTU_Configuration;
import de.metas.handlingunits.model.I_M_ShipmentSchedule_QtyPicked;
import de.metas.handlingunits.model.X_M_HU;
import de.metas.handlingunits.shipmentschedule.api.impl.ShipmentScheduleQtyPickedProductStorage;
import de.metas.i18n.IMsgBL;
import de.metas.i18n.ITranslatableString;
import de.metas.inoutcandidate.api.IShipmentScheduleAllocDAO;
import de.metas.inoutcandidate.api.IShipmentScheduleBL;
import de.metas.inoutcandidate.api.ShipmentScheduleId;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.logging.LogManager;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.quantity.StockQtyAndUOMQty;
import de.metas.quantity.StockQtyAndUOMQtys;
import de.metas.storage.IStorageQuery;
import de.metas.storage.spi.hu.impl.HUStorageQuery;
import de.metas.util.Check;
import de.metas.util.ILoggable;
import de.metas.util.Loggables;
import de.metas.util.Services;
import de.metas.util.time.SystemTime;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.handlingunits.base
 * %%
 * Copyright (C) 2019 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@Service
public class ShipmentScheduleWithHUService
{

	private static final Logger logger = LogManager.getLogger(ShipmentScheduleWithHUService.class);

	private static final String SYSCFG_PICK_AVAILABLE_HUS_ON_THE_FLY = "de.metas.handlingunits.shipmentschedule.api.ShipmentScheduleWithHUService.PickAvailableHUsOnTheFly";

	private static final String MSG_NoQtyPicked = "MSG_NoQtyPicked";

	@Value
	@Builder
	public static class CreateCandidatesRequest
	{
		@NonNull
		IHUContext huContext;

		@NonNull
		ShipmentScheduleId shipmentScheduleId;

		@NonNull
		M_ShipmentSchedule_QuantityTypeToUse quantityType;
	}

	/**
	 * Create {@link IShipmentScheduleWithHU} (i.e. candidates) for given <code>schedule</code>.
	 *
	 * NOTE: this method will create missing LUs before.
	 *
	 * @return one single candidate if there are no {@link I_M_ShipmentSchedule_QtyPicked} for the given schedule. One candidate per {@link I_M_ShipmentSchedule_QtyPicked} otherwise.
	 */
	public ImmutableList<ShipmentScheduleWithHU> createShipmentSchedulesWithHU(@NonNull final CreateCandidatesRequest request)
	{
		final ImmutableList.Builder<ShipmentScheduleWithHU> candidates = ImmutableList.builder();

		final M_ShipmentSchedule_QuantityTypeToUse quantityType = request.getQuantityType();
		final IHUContext huContext = request.getHuContext();

		final I_M_ShipmentSchedule scheduleRecord = load(request.getShipmentScheduleId(), I_M_ShipmentSchedule.class);

		switch (quantityType)
		{
			case TYPE_QTY_TO_DELIVER:
				candidates.addAll(createShipmentSchedulesWithHUForQtyToDeliver(scheduleRecord, quantityType, huContext));
				break;
			case TYPE_PICKED_QTY:
				final Collection<? extends ShipmentScheduleWithHU> candidatesForPick = createAndValidateCandidatesForPick(huContext, scheduleRecord, quantityType);
				candidates.addAll(candidatesForPick);
				break;
			case TYPE_BOTH:
				candidates.addAll(createShipmentScheduleWithHUForPick(scheduleRecord, huContext, quantityType));
				candidates.addAll(createShipmentSchedulesWithHUForQtyToDeliver(scheduleRecord, quantityType, huContext));
				break;
			default:
				throw new AdempiereException("Unexpected QuantityType=" + quantityType + "; CreateCandidatesRequest=" + request);
		}

		return candidates.build();
	}

	private List<ShipmentScheduleWithHU> createShipmentSchedulesWithHUForQtyToDeliver(
			@NonNull final I_M_ShipmentSchedule scheduleRecord,
			@NonNull final M_ShipmentSchedule_QuantityTypeToUse quantityTypeToUse,
			@NonNull final IHUContext huContext)
	{
		final IShipmentScheduleBL shipmentScheduleBL = Services.get(IShipmentScheduleBL.class);

		final ArrayList<ShipmentScheduleWithHU> result = new ArrayList<>();

		final Quantity qtyToDeliver = shipmentScheduleBL.getQtyToDeliver(scheduleRecord);
		final boolean pickAvailableHUsOnTheFly = retrievePickAvailableHUsOntheFly(huContext);
		if (pickAvailableHUsOnTheFly)
		{
			result.addAll(pickHUsOnTheFly(scheduleRecord, qtyToDeliver, huContext));
		}

		// find out if and what what the pickHUsOnTheFly() method did for us
		final Quantity allocatedQty = result
				.stream()
				.map(ShipmentScheduleWithHU::getQtyPicked)
				.reduce(qtyToDeliver.toZero(), Quantity::add);
		Loggables.addLog("QtyToDeliver={}; Qty picked on-the-fly from available HUs: {}", qtyToDeliver, allocatedQty);

		final Quantity remainingQtyToAllocate = qtyToDeliver.subtract(allocatedQty);
		if (remainingQtyToAllocate.signum() > 0)
		{
			final boolean hasNoPickedHUs = result.isEmpty();

			final Quantity catchQtyOverride = hasNoPickedHUs
					? shipmentScheduleBL.getCatchQtyOverride(scheduleRecord).orElse(null)
					: null /* if at least one HU was picked, the catchOverride qty was added there */;

			final ProductId productId = ProductId.ofRepoId(scheduleRecord.getM_Product_ID());
			final StockQtyAndUOMQty stockQtyAndCatchQty = StockQtyAndUOMQty.builder()
					.productId(productId)
					.stockQty(remainingQtyToAllocate)
					.uomQty(catchQtyOverride)
					.build();

			result.add(ShipmentScheduleWithHU.ofShipmentScheduleWithoutHu(
					huContext, //
					scheduleRecord,
					stockQtyAndCatchQty,
					quantityTypeToUse));
		}
		return ImmutableList.copyOf(result);
	}

	private boolean retrievePickAvailableHUsOntheFly(@NonNull final IHUContext huContext)
	{
		final Properties ctx = huContext.getCtx();
		final int adClientId = Env.getAD_Client_ID(ctx);
		final int adOrgId = Env.getAD_Org_ID(ctx);

		final boolean pickAvailableHUsOntheFly = Services.get(ISysConfigBL.class)
				.getBooleanValue(SYSCFG_PICK_AVAILABLE_HUS_ON_THE_FLY,
						true,
						adClientId,
						adOrgId);

		Loggables.addLog("SysConfig {}={} for AD_Client_ID={} and AD_Org_ID={}",
				SYSCFG_PICK_AVAILABLE_HUS_ON_THE_FLY, pickAvailableHUsOntheFly, adClientId, adOrgId);

		return pickAvailableHUsOntheFly;
	}

	/**
	 * If there are any existing HUs that match the given {@code scheduleRecord},<br>
	 * then pick them now although they were not explicitly picked by users.
	 * <p>
	 * Goal: help keeping the metasfresh stock quantity near the real quantity and avoid some inventory effort for users that don't want to use metasfresh's picking.
	 * <p>
	 * Note that we don't use the picked HUs' catch weights since we don't know which HUs were actually picked in the real world.
	 */
	private ImmutableList<ShipmentScheduleWithHU> pickHUsOnTheFly(
			@NonNull final I_M_ShipmentSchedule scheduleRecord,
			@NonNull final Quantity qtyToDeliver,
			@NonNull final IHUContext huContext)
	{
		final IShipmentScheduleBL shipmentScheduleBL = Services.get(IShipmentScheduleBL.class);
		final IStorageQuery storageQuery = shipmentScheduleBL.createStorageQuery(scheduleRecord, true/* considerAttributes */);

		final boolean isHuStorageQuery = storageQuery instanceof HUStorageQuery;
		if (!isHuStorageQuery)
		{
			return ImmutableList.of();
		}

		final IHUShipmentScheduleBL huShipmentScheduleBL = Services.get(IHUShipmentScheduleBL.class);

		final ImmutableList.Builder<ShipmentScheduleWithHU> result = ImmutableList.builder();

		Quantity remainingQtyToAllocate = qtyToDeliver;

		boolean firstHU = true;

		// if we have HUs on stock, get them now
		final Iterator<I_M_HU> iterator = HUStorageQuery
				.cast(storageQuery)
				.createHUQueryBuilder()
				.createQuery()
				.iterate(I_M_HU.class);
		while (iterator.hasNext())
		{
			final I_M_HU sourceHURecord = iterator.next();

			final ProductId productId = ProductId.ofRepoId(scheduleRecord.getM_Product_ID());

			final I_C_UOM uomRecord = remainingQtyToAllocate.getUOM();
			final Quantity qtyOfSourceHU = extractQtyOfHU(sourceHURecord, productId, uomRecord);
			if (qtyOfSourceHU.signum() <= 0)
			{
				continue; // expected not to happen, but shall not be our problem if it does
			}

			final Quantity quantityToSplit = qtyOfSourceHU.min(remainingQtyToAllocate);

			final ILoggable loggable = Loggables.get();
			loggable.addLog("QtyToDeliver={}; split Qty={} from available M_HU_ID={} with Qty={}", qtyToDeliver, quantityToSplit, sourceHURecord.getM_HU_ID(), qtyOfSourceHU);

			// split a part out of the current HU
			final HUsToNewCUsRequest request = HUsToNewCUsRequest
					.builder()
					.keepNewCUsUnderSameParent(false)
					.onlyFromUnreservedHUs(false) // the HUs returned by the query doesn't contain HUs which are reserved to someone else
					.productId(productId)
					.qtyCU(quantityToSplit)
					.sourceHU(sourceHURecord)
					.build();
			final List<I_M_HU> newHURecords = HUTransformService
					.newInstance(huContext)
					.husToNewCUs(request);

			for (final I_M_HU newHURecord : newHURecords)
			{
				final Quantity qtyOfNewHU = extractQtyOfHU(newHURecord, productId, uomRecord);
				loggable.addLog("QtyToDeliver={}; assign split M_HU_ID={} with Qty={}", qtyToDeliver, newHURecord.getM_HU_ID(), qtyOfNewHU);

				Quantity catchQtyOverride = null;
				if (firstHU)
				{
					catchQtyOverride = shipmentScheduleBL.getCatchQtyOverride(scheduleRecord).orElse(null);
					firstHU = false;
				}
				// We don't extract the HU's catch weight; see method's javadoc comment.
				// but if this is the first HU, then we add the ship ment schedule's override quantity (if any)
				final StockQtyAndUOMQty qtys = StockQtyAndUOMQtys.createConvert(
						qtyOfNewHU/* qtyInAnyUom */,
						productId,
						catchQtyOverride);

				result.add(huShipmentScheduleBL.addQtyPicked(
						scheduleRecord,
						qtys,
						newHURecord,
						huContext));
				remainingQtyToAllocate = remainingQtyToAllocate.subtract(qtyOfNewHU);

			}

			if (remainingQtyToAllocate.signum() <= 0)
			{
				break; // we are done here
			}
		}

		return result.build();
	}

	private Quantity extractQtyOfHU(
			final I_M_HU sourceHURecord,
			final ProductId productId,
			final I_C_UOM uomRecord)
	{
		final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);

		return handlingUnitsBL
				.getStorageFactory()
				.getStorage(sourceHURecord)
				.getQuantity(productId, uomRecord);
	}

	private Collection<? extends ShipmentScheduleWithHU> createAndValidateCandidatesForPick(
			final IHUContext huContext,
			final I_M_ShipmentSchedule schedule,
			final M_ShipmentSchedule_QuantityTypeToUse quantityTypeToUse)
	{
		final Collection<? extends ShipmentScheduleWithHU> candidatesForPick = createShipmentScheduleWithHUForPick(schedule, huContext, quantityTypeToUse);

		if (Check.isEmpty(candidatesForPick))
		{
			final IShipmentScheduleAllocDAO shipmentScheduleAllocDAO = Services.get(IShipmentScheduleAllocDAO.class);

			// the parameter insists that we use qtyPicked records, but there aren't any
			// => nothing to do, basically

			// If we got no qty picked records just because they were already delivered,
			// don't fail this workpackage but just log the issue (task 09048)
			final boolean wereDelivered = shipmentScheduleAllocDAO.retrieveOnShipmentLineRecordsQuery(schedule).create().match();
			if (wereDelivered)
			{
				Loggables.withLogger(logger, Level.INFO).addLog("Skipped shipment schedule because it was already delivered: {}", schedule);
				return Collections.emptyList();
			}
			Loggables.withLogger(logger, Level.WARN).addLog("Shipment schedule has no I_M_ShipmentSchedule_QtyPicked records (or these records have inactive HUs); M_ShipmentSchedule={}", schedule);
			final ITranslatableString errorMsg = Services.get(IMsgBL.class).getTranslatableMsgText(MSG_NoQtyPicked);
			throw new AdempiereException(errorMsg);
		}
		return candidatesForPick;
	}

	private Collection<? extends ShipmentScheduleWithHU> createShipmentScheduleWithHUForPick(
			@NonNull final I_M_ShipmentSchedule schedule,
			@NonNull final IHUContext huContext,
			@Nullable final M_ShipmentSchedule_QuantityTypeToUse quantityType)
	{
		List<I_M_ShipmentSchedule_QtyPicked> qtyPickedRecords = retrieveQtyPickedRecords(schedule);
		if (qtyPickedRecords.isEmpty())
		{
			return Collections.emptyList();

		}

		final List<ShipmentScheduleWithHU> candidatesForPick = new ArrayList<>();

		//
		// Create necessary LUs (if any)
		createLUs(schedule, quantityType);

		// retrieve the qty picked entries again, some new ones might have been created on LU creation
		qtyPickedRecords = retrieveQtyPickedRecords(schedule);

		//
		// Iterate all QtyPicked records and create candidates from them

		for (final de.metas.inoutcandidate.model.I_M_ShipmentSchedule_QtyPicked qtyPickedRecord : qtyPickedRecords)
		{
			final I_M_ShipmentSchedule_QtyPicked qtyPickedRecordHU = InterfaceWrapperHelper.create(qtyPickedRecord, I_M_ShipmentSchedule_QtyPicked.class);

			// guard: Skip inactive records.
			if (!qtyPickedRecordHU.isActive())
			{
				Loggables.withLogger(logger, Level.INFO).addLog("Skipped inactive qtyPickedRecordHU={}", qtyPickedRecordHU);
				continue;
			}

			//
			// Create ShipmentSchedule+HU candidate and add it to our list
			final ShipmentScheduleWithHU candidate = //
					ShipmentScheduleWithHU.ofShipmentScheduleQtyPickedWithHuContext(qtyPickedRecordHU, huContext, quantityType);
			candidatesForPick.add(candidate);
		}

		return candidatesForPick;
	}

	/**
	 * @return records that do not have an {@link I_M_InOutLine} assigned to them and that also have
	 *         <ul>
	 *         <li>either no HU assigned to them, or</li>
	 *         <li>HUs which are already picked or shipped assigned to them</li>
	 *         </ul>
	 *
	 *         Hint: also take a look at {@link #isPickedOrShippedOrNoHU(I_M_ShipmentSchedule_QtyPicked)}.
	 *
	 * @task https://github.com/metasfresh/metasfresh/issues/759
	 * @task https://github.com/metasfresh/metasfresh/issues/1174
	 */
	private List<I_M_ShipmentSchedule_QtyPicked> retrieveQtyPickedRecords(final I_M_ShipmentSchedule schedule)
	{
		final IShipmentScheduleAllocDAO shipmentScheduleAllocDAO = Services.get(IShipmentScheduleAllocDAO.class);

		final List<I_M_ShipmentSchedule_QtyPicked> unshippedHUs = shipmentScheduleAllocDAO.retrieveNotOnShipmentLineRecords(schedule, I_M_ShipmentSchedule_QtyPicked.class)
				.stream()
				.filter(r -> isPickedOrShippedOrNoHU(r))
				.collect(ImmutableList.toImmutableList());

		// if we have an "undone" picking, i.e. positive and negative values sum up to zero, then return an empty list
		// this supports the case that something went wrong with picking, and the user needs to get out the shipment asap
		final BigDecimal qtySumOfUnshippedHUs = unshippedHUs.stream()
				.map(I_M_ShipmentSchedule_QtyPicked::getQtyPicked)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (qtySumOfUnshippedHUs.signum() <= 0)
		{
			return ImmutableList.of();
		}

		return unshippedHUs;
	}

	/**
	 * Create LUs for given shipment schedule.
	 *
	 * After calling this method, all our TUs from QtyPicked records shall have an LU.
	 */
	private void createLUs(
			@NonNull final I_M_ShipmentSchedule schedule,
			@NonNull final M_ShipmentSchedule_QuantityTypeToUse quantityType)
	{
		// Don't generate any HUs if we are in QuickShipment mode,
		// because in that mode we are creating shipments without and HUs

		// in case of using the isUseQtyPicked, create the LUs
		final boolean onlyUseQtyToDeliver = quantityType.isOnlyUseToDeliver();

		if (HUConstants.isQuickShipment() && onlyUseQtyToDeliver)
		{
			return;
		}

		final IShipmentScheduleAllocDAO shipmentScheduleAllocDAO = Services.get(IShipmentScheduleAllocDAO.class);
		final List<I_M_ShipmentSchedule_QtyPicked> qtyPickedRecords = shipmentScheduleAllocDAO.retrieveNotOnShipmentLineRecords(schedule, I_M_ShipmentSchedule_QtyPicked.class);

		//
		// Case: this shipment schedule line was not picked at all
		// => generate LUs for the whole Qty
		if (qtyPickedRecords.isEmpty())
		{
			createLUsForQtyToDeliver(schedule);
		}
		//
		// Case: this shipment schedule line was at least partial picked
		// => take all TUs which does not have an LU and add them to LUs
		else
		{
			createLUsForTUs(schedule, qtyPickedRecords);
		}
	}

	/**
	 * Returns {@code true} if there is either no HU assigned to the given {@code schedQtyPicked} or if that HU is either picked or shipped.
	 * If you don't see why it could possibly be already shipped, please take a look at issue <a href="https://github.com/metasfresh/metasfresh/issues/1174">#1174</a>.
	 *
	 * @param schedQtyPicked
	 *
	 * @task https://github.com/metasfresh/metasfresh/issues/1174
	 */
	private boolean isPickedOrShippedOrNoHU(final I_M_ShipmentSchedule_QtyPicked schedQtyPicked)
	{
		final I_M_HU huToVerify;
		if (schedQtyPicked.getVHU_ID() >= 0)
		{
			huToVerify = schedQtyPicked.getVHU();
		}
		else if (schedQtyPicked.getM_TU_HU_ID() >= 0)
		{
			huToVerify = schedQtyPicked.getM_TU_HU();
		}
		else if (schedQtyPicked.getM_LU_HU_ID() >= 0)
		{
			huToVerify = schedQtyPicked.getM_LU_HU();
		}
		else
		{
			return true;
		}

		if (huToVerify == null)
		{
			return true; // this *might* happen with our "minidumps" there we don't have the HU data in our DB
		}

		final String huStatus = huToVerify.getHUStatus();
		return X_M_HU.HUSTATUS_Picked.equals(huStatus) || X_M_HU.HUSTATUS_Shipped.equals(huStatus);
	}

	/**
	 * Take all TUs from <code>qtyPickedRecords</code> which does not have an LU and create/add them to LUs
	 */
	private void createLUsForTUs(final I_M_ShipmentSchedule schedule, final List<I_M_ShipmentSchedule_QtyPicked> qtyPickedRecords)
	{
		final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);

		//
		// Create HUContext from "schedule" because we want to get the Ctx and TrxName from there
		final IContextAware contextProvider = InterfaceWrapperHelper.getContextAware(schedule);
		final IHUContext huContext = handlingUnitsBL.createMutableHUContext(contextProvider);

		//
		// Create our LU Loader. This will help us to aggregate TUs on corresponding LUs
		final LULoader luLoader = new LULoader(huContext);

		//
		// Iterate QtyPicked records
		for (final I_M_ShipmentSchedule_QtyPicked qtyPickedRecord : qtyPickedRecords)
		{
			// refresh because it might be that a previous LU creation to update this record too
			InterfaceWrapperHelper.refresh(qtyPickedRecord);

			// Skip inactive lines
			if (!qtyPickedRecord.isActive())
			{
				continue;
			}

			// Skip lines without TUs
			if (qtyPickedRecord.getM_TU_HU_ID() <= 0)
			{
				continue;
			}

			// Skip lines with ZERO Qty
			if (qtyPickedRecord.getQtyPicked().signum() == 0)
			{
				continue;
			}

			// Skip lines which already have LUs created
			if (qtyPickedRecord.getM_LU_HU_ID() > 0)
			{
				continue;
			}

			final I_M_HU tuHU = qtyPickedRecord.getM_TU_HU();

			// 4507
			// make sure the TU from qtyPicked is a real TU
			if (!handlingUnitsBL.isTransportUnit(tuHU))
			{
				continue;
			}

			luLoader.addTU(tuHU);

			// NOTE: after TU was added to an LU we expect this qtyPickedRecord to be updated and M_LU_HU_ID to be set
			// Also, if there more then one QtyPickedRecords for tuHU, all those shall be updated
			// see de.metas.handlingunits.shipmentschedule.api.impl.ShipmentScheduleHUTrxListener.huParentChanged(I_M_HU, I_M_HU_Item)
		}
	}

	/**
	 * Create LUs for the whole QtyToDeliver from shipment schedule.
	 *
	 * Note: this method is not checking current QtyPicked records (because we assume there are none).
	 *
	 * @param schedule
	 */
	private void createLUsForQtyToDeliver(final I_M_ShipmentSchedule schedule)
	{
		final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);
		final IShipmentScheduleBL shipmentScheduleBL = Services.get(IShipmentScheduleBL.class);

		final IContextAware contextProvider = InterfaceWrapperHelper.getContextAware(schedule);
		final IMutableHUContext huContext = handlingUnitsBL.createMutableHUContext(contextProvider);

		//
		// Create Allocation Request: whole Qty to Deliver
		final Quantity qtyToDeliver = shipmentScheduleBL.getQtyToDeliver(schedule);
		final IAllocationRequest request = AllocationUtils.createQtyRequest(
				huContext,
				loadOutOfTrx(schedule.getM_Product_ID(), I_M_Product.class),
				qtyToDeliver,
				SystemTime.asDate(),
				schedule,      // reference model
				false // forceQtyAllocation
		);

		//
		// Create Allocation Source & Destination
		final IAllocationSource allocationSource = createAllocationSource(schedule);
		final ILUTUProducerAllocationDestination allocationDestination = createLUTUProducerDestination(schedule);
		if (allocationDestination == null)
		{
			return;
		}

		//
		// Execute transfer
		final IAllocationResult result = HULoader.of(allocationSource, allocationDestination)
				.setAllowPartialLoads(false)
				.setAllowPartialUnloads(false)
				.load(request);
		Check.assume(result.isCompleted(), "Result shall be completed: {}", result);

		// NOTE: at this point we shall have QtyPicked records with M_LU_HU_ID set
	}

	private IAllocationSource createAllocationSource(final I_M_ShipmentSchedule schedule)
	{
		final ShipmentScheduleQtyPickedProductStorage shipmentScheduleQtyPickedStorage = new ShipmentScheduleQtyPickedProductStorage(schedule);
		final GenericAllocationSourceDestination source = new GenericAllocationSourceDestination(shipmentScheduleQtyPickedStorage, schedule);

		return source;
	}

	private ILUTUProducerAllocationDestination createLUTUProducerDestination(@NonNull final I_M_ShipmentSchedule schedule)
	{
		final IHUShipmentScheduleBL huShipmentScheduleBL = Services.get(IHUShipmentScheduleBL.class);

		final I_M_HU_LUTU_Configuration lutuConfiguration = huShipmentScheduleBL.deriveM_HU_LUTU_Configuration(schedule);
		final ILUTUConfigurationFactory lutuConfigurationFactory = Services.get(ILUTUConfigurationFactory.class);
		lutuConfigurationFactory.save(lutuConfiguration);

		final ILUTUProducerAllocationDestination luProducerDestination = lutuConfigurationFactory.createLUTUProducerAllocationDestination(lutuConfiguration);
		//
		// Make sure we have our LU PI configured
		if (luProducerDestination.isNoLU())
		{
			throw new HUException("No Loading Unit found for TU: " + luProducerDestination.getTUPI()
					+ "\n@M_ShipmentSchedule_ID@: " + schedule
					+ "\n@Destination@: " + luProducerDestination);
		}

		return luProducerDestination;
	}
}
