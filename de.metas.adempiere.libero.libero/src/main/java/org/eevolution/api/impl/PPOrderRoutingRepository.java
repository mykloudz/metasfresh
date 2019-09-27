package org.eevolution.api.impl;

import static org.adempiere.model.InterfaceWrapperHelper.load;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.HashMap;

/*
 * #%L
 * de.metas.adempiere.libero.libero
 * %%
 * Copyright (C) 2015 metas GmbH
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

import java.util.List;
import java.util.Map;

import org.adempiere.ad.dao.ICompositeQueryFilter;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryFilter;
import org.adempiere.ad.dao.impl.CompareQueryFilter.Operator;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.lang.ImmutablePair;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_S_Resource;
import org.compiere.util.TimeUtil;
import org.eevolution.api.IPPOrderRoutingRepository;
import org.eevolution.api.PPOrderActivityScheduleChangeRequest;
import org.eevolution.api.PPOrderRouting;
import org.eevolution.api.PPOrderRoutingActivity;
import org.eevolution.api.PPOrderRoutingActivityCode;
import org.eevolution.api.PPOrderRoutingActivityId;
import org.eevolution.api.PPOrderRoutingActivitySchedule;
import org.eevolution.api.PPOrderRoutingActivityStatus;
import org.eevolution.model.I_PP_Order_Node;
import org.eevolution.model.I_PP_Order_NodeNext;
import org.eevolution.model.I_PP_Order_Workflow;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;

import de.metas.bpartner.BPartnerId;
import de.metas.material.planning.DurationUnitCodeUtils;
import de.metas.material.planning.IResourceDAO;
import de.metas.material.planning.ResourceType;
import de.metas.material.planning.pporder.LiberoException;
import de.metas.material.planning.pporder.PPOrderId;
import de.metas.material.planning.pporder.PPRoutingActivityId;
import de.metas.material.planning.pporder.PPRoutingId;
import de.metas.product.ResourceId;
import de.metas.quantity.Quantity;
import de.metas.uom.IUOMDAO;
import de.metas.util.Check;
import de.metas.util.GuavaCollectors;
import de.metas.util.Services;
import de.metas.util.time.DurationUtils;
import lombok.NonNull;

public class PPOrderRoutingRepository implements IPPOrderRoutingRepository
{
	@Override
	public PPOrderRouting getByOrderId(@NonNull final PPOrderId orderId)
	{
		//
		// Order Routing header
		final I_PP_Order_Workflow orderRoutingRecord = retrieveOrderWorkflowOrNull(orderId);
		Check.assumeNotNull(orderRoutingRecord, "Parameter orderWorkflow is not null");
		final TemporalUnit durationUnit = DurationUnitCodeUtils.toTemporalUnit(orderRoutingRecord.getDurationUnit());
		final int unitsPerCycle = orderRoutingRecord.getUnitsCycles().intValue();

		//
		// Order Activities
		final ImmutableList<PPOrderRoutingActivity> orderActivities = retrieveOrderNodes(orderId)
				.stream()
				.map(orderActivityRecord -> toPPOrderRoutingActivity(orderActivityRecord, durationUnit, unitsPerCycle))
				.collect(ImmutableList.toImmutableList());

		final ImmutableMap<Integer, PPOrderRoutingActivityCode> activityCodesByRepoId = orderActivities
				.stream()
				.collect(ImmutableMap.toImmutableMap(
						orderActivity -> orderActivity.getId().getRepoId(),
						orderActivity -> orderActivity.getCode()));

		//
		// First Activity Code
		final PPOrderRoutingActivityCode firstActivityCode = activityCodesByRepoId.get(orderRoutingRecord.getPP_Order_Node_ID());

		//
		// Order Activities Transitions
		final ImmutableSetMultimap<PPOrderRoutingActivityCode, PPOrderRoutingActivityCode> codeToNextCodeMap = retrieveOrderNodeNexts(orderId)
				.stream()
				.collect(ImmutableSetMultimap.toImmutableSetMultimap(
						nodeNextRecord -> activityCodesByRepoId.get(nodeNextRecord.getPP_Order_Node_ID()),
						nodeNextRecord -> activityCodesByRepoId.get(nodeNextRecord.getPP_Order_Next_ID())));

		return PPOrderRouting.builder()
				.ppOrderId(orderId)
				.routingId(PPRoutingId.ofRepoId(orderRoutingRecord.getAD_Workflow_ID()))
				.durationUnit(durationUnit)
				.qtyPerBatch(orderRoutingRecord.getQtyBatchSize())
				//
				.firstActivityCode(firstActivityCode)
				.activities(orderActivities)
				.codeToNextCodeMap(codeToNextCodeMap)
				//
				.build();
	}

	@Override
	public PPOrderRoutingActivity getOrderRoutingActivity(@NonNull final PPOrderRoutingActivityId orderRoutingActivityId)
	{
		//
		// Order Routing header
		final PPOrderId orderId = orderRoutingActivityId.getOrderId();
		final I_PP_Order_Workflow orderRoutingRecord = retrieveOrderWorkflowOrNull(orderId);
		Check.assumeNotNull(orderRoutingRecord, "Parameter orderWorkflow is not null");
		final TemporalUnit durationUnit = DurationUnitCodeUtils.toTemporalUnit(orderRoutingRecord.getDurationUnit());
		final int unitsPerCycle = orderRoutingRecord.getUnitsCycles().intValue();

		final I_PP_Order_Node orderActivityRecord = load(orderRoutingActivityId, I_PP_Order_Node.class);
		return toPPOrderRoutingActivity(orderActivityRecord, durationUnit, unitsPerCycle);
	}

	@Override
	public void deleteByOrderId(@NonNull final PPOrderId orderId)
	{
		final ITrxManager trxManager = Services.get(ITrxManager.class);
		trxManager.runInThreadInheritedTrx(() -> deleteByOrderIdInTrx(orderId));
	}

	public void deleteByOrderIdInTrx(@NonNull final PPOrderId orderId)
	{
		//
		// Set PP_Order_Workflow.PP_Order_Node_ID to null
		// ... to be able to delete nodes first
		final I_PP_Order_Workflow orderWorkflow = retrieveOrderWorkflowOrNull(orderId);
		if (orderWorkflow != null)
		{
			orderWorkflow.setPP_Order_Node_ID(-1);
			saveRecord(orderWorkflow);
		}

		//
		// Delete PP_Order_NodeNext
		for (final I_PP_Order_NodeNext orderNodeNext : retrieveOrderNodeNexts(orderId))
		{
			InterfaceWrapperHelper.delete(orderNodeNext);
		}

		//
		// Delete PP_Order_Node
		for (final I_PP_Order_Node orderNode : retrieveOrderNodes(orderId))
		{
			InterfaceWrapperHelper.delete(orderNode);
		}

		//
		// Delete PP_Order_Workflow
		// (after everything else which depends on this was deleted)
		if (orderWorkflow != null)
		{
			InterfaceWrapperHelper.delete(orderWorkflow);
		}
	}

	private I_PP_Order_Workflow retrieveOrderWorkflowOrNull(@NonNull final PPOrderId orderId)
	{
		final IQueryBL queryBL = Services.get(IQueryBL.class);
		return queryBL.createQueryBuilder(I_PP_Order_Workflow.class)
				.addEqualsFilter(I_PP_Order_Workflow.COLUMNNAME_PP_Order_ID, orderId)
				.create()
				.firstOnly(I_PP_Order_Workflow.class);
	}

	private List<I_PP_Order_Node> retrieveOrderNodes(final PPOrderId orderId)
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_PP_Order_Node.class)
				.addEqualsFilter(I_PP_Order_Node.COLUMNNAME_PP_Order_ID, orderId)
				.create()
				.list();
	}

	private List<I_PP_Order_NodeNext> retrieveOrderNodeNexts(@NonNull final PPOrderId orderId)
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_PP_Order_NodeNext.class)
				.addEqualsFilter(I_PP_Order_NodeNext.COLUMNNAME_PP_Order_ID, orderId)
				.create()
				.list();
	}

	@Override
	public String retrieveResourceNameForFirstNode(@NonNull final PPOrderId orderId)
	{
		final I_PP_Order_Workflow orderWorkflow = retrieveOrderWorkflowOrNull(orderId);
		final I_PP_Order_Node startNode = orderWorkflow.getPP_Order_Node();
		Check.assumeNotNull(startNode, LiberoException.class, "Start node shall exist for {}", orderId);

		final ResourceId resourceId = ResourceId.ofRepoId(startNode.getS_Resource_ID());
		final I_S_Resource resource = Services.get(IResourceDAO.class).getById(resourceId);
		return resource.getName();
	}

	private PPOrderRoutingActivity toPPOrderRoutingActivity(
			@NonNull final I_PP_Order_Node record,
			@NonNull final TemporalUnit durationUnit,
			final int unitsPerCycle)
	{
		final PPOrderId orderId = PPOrderId.ofRepoId(record.getPP_Order_ID());

		final PPRoutingId routingId = PPRoutingId.ofRepoId(record.getAD_Workflow_ID());

		final ResourceId resourceId = ResourceId.ofRepoId(record.getS_Resource_ID());
		final I_C_UOM uom = Services.get(IUOMDAO.class).getById(record.getC_UOM_ID());

		return PPOrderRoutingActivity.builder()
				.id(PPOrderRoutingActivityId.ofRepoId(orderId, record.getPP_Order_Node_ID()))
				.code(PPOrderRoutingActivityCode.ofString(record.getValue()))
				.routingActivityId(PPRoutingActivityId.ofAD_WF_Node_ID(routingId, record.getAD_WF_Node_ID()))
				//
				.subcontracting(record.isSubcontracting())
				.subcontractingVendorId(BPartnerId.ofRepoIdOrNull(record.getC_BPartner_ID()))
				//
				.milestone(record.isMilestone())
				//
				.resourceId(resourceId)
				//
				.status(PPOrderRoutingActivityStatus.ofDocStatus(record.getDocStatus()))
				//
				// Standard values
				.durationUnit(durationUnit)
				.queuingTime(Duration.of(record.getQueuingTime(), durationUnit))
				.setupTime(Duration.of(record.getSetupTime(), durationUnit))
				.waitingTime(Duration.of(record.getWaitingTime(), durationUnit))
				.movingTime(Duration.of(record.getMovingTime(), durationUnit))
				.durationPerOneUnit(Duration.of(record.getDuration(), durationUnit))
				.unitsPerCycle(unitsPerCycle)
				//
				// Planned values
				.setupTimeRequired(Duration.of(record.getSetupTimeRequiered(), durationUnit))
				.durationRequired(Duration.of(record.getDurationRequiered(), durationUnit))
				.qtyRequired(Quantity.of(record.getQtyRequiered(), uom))
				//
				// Reported values
				.setupTimeReal(Duration.of(record.getSetupTimeReal(), durationUnit))
				.durationReal(Duration.of(record.getDurationReal(), durationUnit))
				.qtyDelivered(Quantity.of(record.getQtyDelivered(), uom))
				.qtyScrapped(Quantity.of(record.getQtyScrap(), uom))
				.qtyRejected(Quantity.of(record.getQtyReject(), uom))
				.dateStart(TimeUtil.asLocalDateTime(record.getDateStart()))
				.dateFinish(TimeUtil.asLocalDateTime(record.getDateFinish()))
				//
				.build();
	}

	@Override
	public void changeActivitiesScheduling(@NonNull final PPOrderId orderId, @NonNull final List<PPOrderActivityScheduleChangeRequest> changeRequests)
	{
		final ITrxManager trxManager = Services.get(ITrxManager.class);
		trxManager.runInThreadInheritedTrx(() -> changeActivitiesSchedulingInTrx(orderId, changeRequests));
	}

	public void changeActivitiesSchedulingInTrx(@NonNull final PPOrderId orderId, @NonNull final List<PPOrderActivityScheduleChangeRequest> changeRequests)
	{
		final Map<PPOrderRoutingActivityId, PPOrderActivityScheduleChangeRequest> changeRequestsByActivityId = Maps.uniqueIndex(changeRequests, PPOrderActivityScheduleChangeRequest::getOrderRoutingActivityId);
		for (final I_PP_Order_Node orderActivity : retrieveOrderNodes(orderId))
		{
			final PPOrderRoutingActivityId orderRoutingActivityId = PPOrderRoutingActivityId.ofRepoId(orderId, orderActivity.getPP_Order_Node_ID());
			final PPOrderActivityScheduleChangeRequest activityChangeRequest = changeRequestsByActivityId.get(orderRoutingActivityId);
			if (activityChangeRequest == null)
			{
				continue;
			}

			applyActivityChanges(orderActivity, activityChangeRequest);
		}
	}

	private void applyActivityChanges(@NonNull final I_PP_Order_Node orderActivity, @NonNull final PPOrderActivityScheduleChangeRequest activityChangeRequest)
	{
		orderActivity.setDateStartSchedule(TimeUtil.asTimestamp(activityChangeRequest.getScheduledStartDate()));
		orderActivity.setDateFinishSchedule(TimeUtil.asTimestamp(activityChangeRequest.getScheduledEndDate()));
		saveRecord(orderActivity);
	}

	@Override
	public List<PPOrderRoutingActivitySchedule> getActivitySchedulesByDateAndResource(final LocalDateTime date, final ResourceId resourceId)
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_PP_Order_Node.class)
				.addOnlyActiveRecordsFilter()
				.filter(createActivityScheduleIntersectsWithDayTimeSlotFilter(date, resourceId))
				.orderBy(I_PP_Order_Node.COLUMN_DateStartSchedule)
				.create()
				.stream()
				.map(this::toPPOrderRoutingActivitySchedule)
				.collect(ImmutableList.toImmutableList());
	}

	private IQueryFilter<I_PP_Order_Node> createActivityScheduleIntersectsWithDayTimeSlotFilter(final LocalDateTime dateTime, final ResourceId resourceId)
	{
		final ResourceType resourceType = Services.get(IResourceDAO.class).getResourceTypeByResourceId(resourceId);
		final LocalDateTime dayStart = resourceType.getDayStart(dateTime);
		final LocalDateTime dayEnd = resourceType.getDayEnd(dateTime);

		final ICompositeQueryFilter<I_PP_Order_Node> filters = Services.get(IQueryBL.class).createCompositeQueryFilter(I_PP_Order_Node.class)
				.setJoinOr();

		//
		// Case 1: The time dependent process has already begun and ends at this day.
		filters.addCompositeQueryFilter()
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateStartSchedule, Operator.LESS_OR_EQUAL, dayStart)
				//
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateFinishSchedule, Operator.GREATER_OR_EQUAL, dayStart)
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateFinishSchedule, Operator.LESS_OR_EQUAL, dayEnd);

		//
		// Case 2: The time dependent process begins and ends at this day.
		filters.addCompositeQueryFilter()
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateStartSchedule, Operator.GREATER_OR_EQUAL, dayStart)
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateStartSchedule, Operator.LESS_OR_EQUAL, dayEnd)
				//
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateFinishSchedule, Operator.GREATER_OR_EQUAL, dayStart)
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateFinishSchedule, Operator.LESS_OR_EQUAL, dayEnd);

		//
		// Case 3: The time dependent process begins at this day and ends few days later.
		filters.addCompositeQueryFilter()
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateStartSchedule, Operator.GREATER_OR_EQUAL, dayStart)
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateStartSchedule, Operator.LESS_OR_EQUAL, dayEnd)
				//
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateFinishSchedule, Operator.GREATER_OR_EQUAL, dayEnd);

		//
		// Case 4: The time dependent process has already begun and ends few days later.
		filters.addCompositeQueryFilter()
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateStartSchedule, Operator.LESS_OR_EQUAL, dayStart)
				//
				.addCompareFilter(I_PP_Order_Node.COLUMN_DateFinishSchedule, Operator.GREATER_OR_EQUAL, dayEnd);

		return filters;
	}

	private PPOrderRoutingActivitySchedule toPPOrderRoutingActivitySchedule(final I_PP_Order_Node activity)
	{
		final PPOrderId orderId = PPOrderId.ofRepoId(activity.getPP_Order_ID());
		return PPOrderRoutingActivitySchedule.builder()
				.orderRoutingActivityId(PPOrderRoutingActivityId.ofRepoId(orderId, activity.getPP_Order_Node_ID()))
				.scheduledStartDate(TimeUtil.asLocalDateTime(activity.getDateStartSchedule()))
				.scheduledEndDate(TimeUtil.asLocalDateTime(activity.getDateFinishSchedule()))
				.build();
	}

	@Override
	public void save(@NonNull final PPOrderRouting orderRouting)
	{
		final ITrxManager trxManager = Services.get(ITrxManager.class);
		trxManager.runInThreadInheritedTrx(() -> saveInTrx(orderRouting));
	}

	private void saveInTrx(@NonNull final PPOrderRouting orderRouting)
	{
		//
		// Order Routing header
		final PPOrderId orderId = orderRouting.getPpOrderId();
		I_PP_Order_Workflow routingRecord = retrieveOrderWorkflowOrNull(orderId);
		if (routingRecord == null)
		{
			routingRecord = toNewOrderWorkflowRecord(orderRouting);
		}
		else
		{
			updateOrderWorkflowRecord(routingRecord, orderRouting);
		}
		// NOTE: first activity will be set later, see below...
		saveRecord(routingRecord);
		final int ppOrderWorkflowId = routingRecord.getPP_Order_Workflow_ID();

		//
		// Order Activities
		final Collection<I_PP_Order_Node> activityRecordsToDelete;
		{
			final HashMap<PPOrderRoutingActivityId, I_PP_Order_Node> existingActivityRecords = retrieveOrderNodes(orderId)
					.stream()
					.collect(GuavaCollectors.toHashMapByKey(this::extractPPOrderRoutingActivityId));

			// Create/Update
			for (final PPOrderRoutingActivity activity : orderRouting.getActivities())
			{
				I_PP_Order_Node activityRecord = existingActivityRecords.remove(activity.getId());
				if (activityRecord == null)
				{
					activityRecord = toNewOrderNodeRecord(activity, orderId, ppOrderWorkflowId);
				}
				else
				{
					updateOrderNodeRecord(activityRecord, activity);
				}

				saveRecord(activityRecord);
				activity.setId(extractPPOrderRoutingActivityId(activityRecord));
			}

			//
			activityRecordsToDelete = existingActivityRecords.values();
		}

		//
		// Set First Activity
		routingRecord.setPP_Order_Node_ID(orderRouting.getFirstActivity().getId().getRepoId());
		saveRecord(routingRecord);

		//
		// Transitions
		{
			final ArrayListMultimap<ImmutablePair<PPOrderRoutingActivityId, PPOrderRoutingActivityId>, I_PP_Order_NodeNext> allExistingNodeNexts = retrieveOrderNodeNexts(orderId)
					.stream()
					.collect(GuavaCollectors.toArrayListMultimapByKey(this::extractCurrentAndNextActivityIdPair));

			for (final PPOrderRoutingActivity activity : orderRouting.getActivities())
			{
				final PPOrderRoutingActivityId activityId = activity.getId();

				for (final PPOrderRoutingActivity nextActivity : orderRouting.getNextActivities(activity))
				{
					final PPOrderRoutingActivityId nextActivityId = nextActivity.getId();

					final List<I_PP_Order_NodeNext> existingNodeNexts = allExistingNodeNexts.removeAll(ImmutablePair.of(activityId, nextActivityId));
					if (existingNodeNexts.isEmpty())
					{
						final I_PP_Order_NodeNext nodeNextRecord = toNewOrderNodeNextRecord(activity, nextActivity);
						saveRecord(nodeNextRecord);
					}
					else
					{
						final I_PP_Order_NodeNext nodeNextRecord = existingNodeNexts.remove(0);
						updateOrderNodeNextRecord(nodeNextRecord, activity, nextActivity);
						saveRecord(nodeNextRecord);

						InterfaceWrapperHelper.deleteAll(existingNodeNexts);
					}
				}
			}
		}

		//
		// Delete remaining nodes if any
		InterfaceWrapperHelper.deleteAll(activityRecordsToDelete);
	}

	private ImmutablePair<PPOrderRoutingActivityId, PPOrderRoutingActivityId> extractCurrentAndNextActivityIdPair(final I_PP_Order_NodeNext record)
	{
		final PPOrderId orderId = PPOrderId.ofRepoId(record.getPP_Order_ID());
		return ImmutablePair.of(
				PPOrderRoutingActivityId.ofRepoId(orderId, record.getPP_Order_Node_ID()),
				PPOrderRoutingActivityId.ofRepoId(orderId, record.getPP_Order_Next_ID()));
	}

	private PPOrderRoutingActivityId extractPPOrderRoutingActivityId(final I_PP_Order_Node record)
	{
		final PPOrderId orderId = PPOrderId.ofRepoId(record.getPP_Order_ID());
		return PPOrderRoutingActivityId.ofRepoId(orderId, record.getPP_Order_Node_ID());
	}

	private I_PP_Order_Workflow toNewOrderWorkflowRecord(final PPOrderRouting from)
	{
		final I_PP_Order_Workflow record = InterfaceWrapperHelper.newInstance(I_PP_Order_Workflow.class);
		record.setPP_Order_ID(from.getPpOrderId().getRepoId());

		record.setWaitingTime(0);
		record.setWorkingTime(0);
		record.setDurationLimit(0);
		record.setQueuingTime(0);
		record.setSetupTime(0);
		record.setMovingTime(0);
		record.setDuration(0);
		
		updateOrderWorkflowRecord(record, from);

		return record;
	}

	private void updateOrderWorkflowRecord(final I_PP_Order_Workflow record, final PPOrderRouting from)
	{
		record.setIsActive(true);
		record.setAD_Workflow_ID(from.getRoutingId().getRepoId());
		record.setDurationUnit(DurationUnitCodeUtils.toDurationUnitCode(from.getDurationUnit()));
		record.setQtyBatchSize(from.getQtyPerBatch());
	}

	private I_PP_Order_Node toNewOrderNodeRecord(
			final PPOrderRoutingActivity activity,
			final PPOrderId ppOrderId,
			final int ppOrderWorkflowId)
	{
		final I_PP_Order_Node record = InterfaceWrapperHelper.newInstance(I_PP_Order_Node.class);
		record.setPP_Order_ID(ppOrderId.getRepoId());
		record.setPP_Order_Workflow_ID(ppOrderWorkflowId);

		updateOrderNodeRecord(record, activity);

		return record;
	}

	private void updateOrderNodeRecord(final I_PP_Order_Node record, final PPOrderRoutingActivity from)
	{
		final TemporalUnit durationUnit = from.getDurationUnit();

		record.setIsActive(true);

		record.setValue(from.getCode().getAsString());

		record.setAD_Workflow_ID(from.getRoutingActivityId().getRoutingId().getRepoId());
		record.setAD_WF_Node_ID(from.getRoutingActivityId().getRepoId());

		record.setIsSubcontracting(from.isSubcontracting());
		record.setC_BPartner_ID(BPartnerId.toRepoId(from.getSubcontractingVendorId()));

		record.setIsMilestone(from.isMilestone());

		record.setS_Resource_ID(from.getResourceId().getRepoId());

		record.setDocStatus(from.getStatus().getDocStatus());

		//
		// Standard values
		record.setSetupTime(DurationUtils.toInt(from.getSetupTime(), durationUnit));
		record.setSetupTimeRequiered(DurationUtils.toInt(from.getSetupTime(), durationUnit));
		record.setMovingTime(DurationUtils.toInt(from.getMovingTime(), durationUnit));
		record.setWaitingTime(DurationUtils.toInt(from.getWaitingTime(), durationUnit));
		record.setQueuingTime(DurationUtils.toInt(from.getQueuingTime(), durationUnit));
		record.setDuration(DurationUtils.toInt(from.getDurationPerOneUnit(), durationUnit));
		record.setDurationRequiered(DurationUtils.toInt(from.getDurationRequired(), durationUnit));

		//
		// Planned values
		record.setSetupTimeRequiered(DurationUtils.toInt(from.getSetupTimeRequired(), durationUnit));
		record.setDurationRequiered(DurationUtils.toInt(from.getDurationRequired(), durationUnit));
		record.setQtyRequiered(from.getQtyRequired().toBigDecimal());
		record.setC_UOM_ID(from.getQtyRequired().getUOMId());

		//
		// Reported values
		record.setSetupTimeReal(DurationUtils.toInt(from.getSetupTimeReal(), durationUnit));
		record.setDurationReal(DurationUtils.toInt(from.getDurationReal(), durationUnit));
		record.setQtyDelivered(from.getQtyDelivered().toBigDecimal());
		record.setQtyScrap(from.getQtyScrapped().toBigDecimal());
		record.setQtyReject(from.getQtyRejected().toBigDecimal());
		record.setDateStart(TimeUtil.asTimestamp(from.getDateStart()));
		record.setDateFinish(TimeUtil.asTimestamp(from.getDateFinish()));
	}

	private I_PP_Order_NodeNext toNewOrderNodeNextRecord(final PPOrderRoutingActivity activity, final PPOrderRoutingActivity nextActivity)
	{
		final I_PP_Order_NodeNext record = InterfaceWrapperHelper.newInstance(I_PP_Order_NodeNext.class);
		updateOrderNodeNextRecord(record, activity, nextActivity);
		return record;
	}

	private void updateOrderNodeNextRecord(final I_PP_Order_NodeNext record, final PPOrderRoutingActivity activity, final PPOrderRoutingActivity nextActivity)
	{
		final PPOrderId orderId = activity.getOrderId();

		// record.setAD_Org_ID(orderNode.getAD_Org_ID());
		record.setPP_Order_ID(orderId.getRepoId());

		record.setPP_Order_Node_ID(activity.getId().getRepoId());
		record.setAD_WF_Node_ID(activity.getRoutingActivityId().getRepoId());

		record.setPP_Order_Next_ID(nextActivity.getId().getRepoId());
		record.setAD_WF_Next_ID(nextActivity.getRoutingActivityId().getRepoId());

		record.setEntityType("U");
		// record.setIsStdUserWorkflow(wfNodeNext.isStdUserWorkflow());
		record.setSeqNo(10);
		// record.setTransitionCode();
	}

}
