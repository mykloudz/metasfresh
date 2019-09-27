package org.eevolution.api.impl;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

import org.compiere.model.I_AD_Workflow;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_C_UOM;
import org.compiere.model.X_C_DocType;
import org.compiere.util.TimeUtil;
import org.eevolution.api.ActivityControlCreateRequest;
import org.eevolution.api.IPPCostCollectorBL;
import org.eevolution.api.IPPOrderBL;
import org.eevolution.api.IPPOrderDAO;
import org.eevolution.api.IPPOrderRoutingRepository;
import org.eevolution.api.PPOrderRouting;
import org.eevolution.api.PPOrderRoutingActivity;
import org.eevolution.api.PPOrderRoutingActivityStatus;
import org.eevolution.api.PPOrderScheduleChangeRequest;
import org.eevolution.model.I_PP_Order;
import org.eevolution.model.I_PP_Order_BOMLine;
import org.eevolution.model.X_PP_Order;

import de.metas.document.DocTypeId;
import de.metas.document.DocTypeQuery;
import de.metas.document.IDocTypeDAO;
import de.metas.material.planning.WorkingTime;
import de.metas.material.planning.pporder.IPPOrderBOMBL;
import de.metas.material.planning.pporder.IPPOrderBOMDAO;
import de.metas.material.planning.pporder.LiberoException;
import de.metas.material.planning.pporder.PPOrderId;
import de.metas.material.planning.pporder.PPOrderUtil;
import de.metas.material.planning.pporder.PPRoutingId;
import de.metas.product.IProductBL;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.uom.IUOMDAO;
import de.metas.uom.UOMPrecision;
import de.metas.uom.UomId;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.time.SystemTime;
import lombok.NonNull;

public class PPOrderBL implements IPPOrderBL
{
	@Override
	public void setDefaults(final I_PP_Order ppOrder)
	{
		ppOrder.setLine(10);
		ppOrder.setPriorityRule(X_PP_Order.PRIORITYRULE_Medium);
		ppOrder.setDescription("");
		ppOrder.setQtyDelivered(BigDecimal.ZERO);
		ppOrder.setQtyReject(BigDecimal.ZERO);
		ppOrder.setQtyScrap(BigDecimal.ZERO);
		ppOrder.setIsSelected(false);
		ppOrder.setIsSOTrx(false);
		ppOrder.setIsApproved(false);
		ppOrder.setIsPrinted(false);
		ppOrder.setProcessed(false);
		ppOrder.setProcessing(false);
		ppOrder.setPosted(false);
		setDocType(ppOrder, X_C_DocType.DOCBASETYPE_ManufacturingOrder, /* docSubType */null);
		ppOrder.setDocStatus(X_PP_Order.DOCSTATUS_Drafted);
		ppOrder.setDocAction(X_PP_Order.DOCACTION_Complete);
	}

	/**
	 * Set Qty Entered - enforce entered UOM
	 *
	 * @param QtyEntered
	 */
	@Override
	public void setQtyEntered(final I_PP_Order order, final BigDecimal QtyEntered)
	{
		final BigDecimal qtyEnteredToUse;
		final UomId uomId = UomId.ofRepoIdOrNull(order.getC_UOM_ID());
		if (QtyEntered != null && uomId != null)
		{
			final UOMPrecision precision = Services.get(IUOMDAO.class).getStandardPrecision(uomId);
			qtyEnteredToUse = precision.round(QtyEntered);
		}
		else
		{
			qtyEnteredToUse = QtyEntered;
		}
		order.setQtyEntered(qtyEnteredToUse);
	}	// setQtyEntered

	/**
	 * Set Qty Ordered - enforce Product UOM
	 *
	 * @param qtyOrdered
	 */
	@Override
	public void setQtyOrdered(final I_PP_Order order, final BigDecimal qtyOrdered)
	{
		final BigDecimal qtyOrderedToUse;
		if (qtyOrdered != null)
		{
			final ProductId productId = ProductId.ofRepoId(order.getM_Product_ID());
			final UOMPrecision precision = Services.get(IProductBL.class).getUOMPrecision(productId);
			qtyOrderedToUse = precision.round(qtyOrdered);
		}
		else
		{
			qtyOrderedToUse = qtyOrdered;
		}
		order.setQtyOrdered(qtyOrderedToUse);
	}	// setQtyOrdered

	@Override
	public void addDescription(final I_PP_Order order, final String description)
	{
		final String desc = order.getDescription();
		if (desc == null || desc.isEmpty())
		{
			order.setDescription(description);
		}
		else
		{
			order.setDescription(desc + " | " + description);
		}
	}	// addDescription

	@Override
	public void updateQtyBatchs(I_PP_Order order, boolean override)
	{
		BigDecimal qtyBatchSize = order.getQtyBatchSize();
		if (qtyBatchSize.signum() == 0 || override)
		{
			final int AD_Workflow_ID = order.getAD_Workflow_ID();
			// No workflow entered, or is just a new record:
			if (AD_Workflow_ID <= 0)
			{
				return;
			}

			final I_AD_Workflow wf = order.getAD_Workflow();
			qtyBatchSize = wf.getQtyBatchSize().setScale(0, RoundingMode.UP);
			order.setQtyBatchSize(qtyBatchSize);
		}

		final BigDecimal qtyBatchs;
		if (qtyBatchSize.signum() == 0)
		{
			qtyBatchs = BigDecimal.ONE;
		}
		else
		{
			final BigDecimal qtyOrdered = order.getQtyOrdered();
			qtyBatchs = qtyOrdered.divide(qtyBatchSize, 0, BigDecimal.ROUND_UP);
		}
		order.setQtyBatchs(qtyBatchs);
	}

	@Override
	public boolean isSomethingProcessed(final I_PP_Order ppOrder)
	{
		final PPOrderId orderId = PPOrderId.ofRepoId(ppOrder.getPP_Order_ID());

		//
		// Main product
		if (ppOrder.getQtyDelivered().signum() != 0 || ppOrder.getQtyScrap().signum() != 0 || ppOrder.getQtyReject().signum() != 0)
		{
			return true;
		}

		//
		// BOM
		final IPPOrderBOMBL orderBOMService = Services.get(IPPOrderBOMBL.class);
		if (orderBOMService.isSomethingReportedOnBOMLines(orderId))
		{
			return true;
		}

		//
		// Routing
		final IPPOrderRoutingRepository orderRoutingRepo = Services.get(IPPOrderRoutingRepository.class);
		final PPOrderRouting orderRouting = orderRoutingRepo.getByOrderId(orderId);
		if (orderRouting.isSomethingProcessed())
		{
			return true;
		}

		//
		return false;
	}

	private I_C_UOM getMainProductStockingUOM(final I_PP_Order ppOrder)
	{
		final ProductId mainProductId = ProductId.ofRepoId(ppOrder.getM_Product_ID());
		return Services.get(IProductBL.class).getStockUOM(mainProductId);
	}

	@Override
	public Quantity getQtyOpen(final I_PP_Order ppOrder)
	{
		final I_C_UOM uom = getMainProductStockingUOM(ppOrder);
		final BigDecimal qtyOrdered = ppOrder.getQtyOrdered();
		final BigDecimal qtyReceived = ppOrder.getQtyDelivered();
		final BigDecimal qtyScrap = ppOrder.getQtyScrap();
		final BigDecimal qtyToReceive = qtyOrdered.subtract(qtyReceived).subtract(qtyScrap);
		return Quantity.of(qtyToReceive, uom);
	}

	@Override
	public Quantity getQtyReceived(final I_PP_Order ppOrder)
	{
		final I_C_UOM uom = getMainProductStockingUOM(ppOrder);
		final BigDecimal qtyReceived = ppOrder.getQtyDelivered();
		return Quantity.of(qtyReceived, uom);
	}

	@Override
	public Quantity getQtyReceived(@NonNull final PPOrderId ppOrderId)
	{
		final I_PP_Order ppOrder = Services.get(IPPOrderDAO.class).getById(ppOrderId);
		return getQtyReceived(ppOrder);
	}

	@Override
	public Quantity getQtyScrapped(final I_PP_Order ppOrder)
	{
		final I_C_UOM uom = getMainProductStockingUOM(ppOrder);
		final BigDecimal qtyScrap = ppOrder.getQtyScrap();
		return Quantity.of(qtyScrap, uom);
	}

	@Override
	public Quantity getQtyRejected(final I_PP_Order ppOrder)
	{
		final I_C_UOM uom = getMainProductStockingUOM(ppOrder);
		final BigDecimal qtyReject = ppOrder.getQtyReject();
		return Quantity.of(qtyReject, uom);
	}

	@Override
	public I_C_OrderLine getDirectOrderLine(final I_PP_Order ppOrder)
	{
		Check.assumeNotNull(ppOrder, LiberoException.class, "ppOrder not null");

		final I_C_OrderLine orderLine = ppOrder.getC_OrderLine();
		if (orderLine == null)
		{
			return null;
		}

		//
		// Check: if orderline's Product is not the same as MO's Product
		// ... it means this is a MO for an intermediare product which will be used in another manufacturing order
		// to produce the final product which will be shipped for this order line
		//
		// So we return null because this order line is not the "direct" order line for this product
		if (orderLine.getM_Product_ID() != ppOrder.getM_Product_ID())
		{
			return null;
		}

		return orderLine;
	}

	@Override
	public void updateBOMOrderLinesWarehouseAndLocator(final I_PP_Order ppOrder)
	{
		final IPPOrderBOMDAO ppOrderBOMsRepo = Services.get(IPPOrderBOMDAO.class);

		for (final I_PP_Order_BOMLine orderBOMLine : ppOrderBOMsRepo.retrieveOrderBOMLines(ppOrder))
		{
			PPOrderUtil.updateBOMLineWarehouseAndLocatorFromOrder(orderBOMLine, ppOrder);
			ppOrderBOMsRepo.save(orderBOMLine);
		}
	}

	@Override
	public void setDocType(@NonNull final I_PP_Order ppOrder, @NonNull final String docBaseType, final String docSubType)
	{
		final IDocTypeDAO docTypesRepo = Services.get(IDocTypeDAO.class);

		final DocTypeId docTypeId = docTypesRepo.getDocTypeId(DocTypeQuery.builder()
				.docBaseType(docBaseType)
				.docSubType(docSubType)
				.adClientId(ppOrder.getAD_Client_ID())
				.adOrgId(ppOrder.getAD_Org_ID())
				.build());

		ppOrder.setC_DocTypeTarget_ID(docTypeId.getRepoId());
		ppOrder.setC_DocType_ID(docTypeId.getRepoId());
	}

	@Override
	public void closeQtyOrdered(final I_PP_Order ppOrder)
	{
		final BigDecimal qtyOrderedOld = ppOrder.getQtyOrdered();
		final BigDecimal qtyDelivered = ppOrder.getQtyDelivered();

		ppOrder.setQtyBeforeClose(qtyOrderedOld);
		setQtyOrdered(ppOrder, qtyDelivered);

		final IPPOrderDAO ppOrdersRepo = Services.get(IPPOrderDAO.class);
		ppOrdersRepo.save(ppOrder);
	}

	@Override
	public void uncloseQtyOrdered(final I_PP_Order ppOrder)
	{
		final BigDecimal qtyOrderedBeforeClose = ppOrder.getQtyBeforeClose();

		ppOrder.setQtyOrdered(qtyOrderedBeforeClose);
		ppOrder.setQtyBeforeClose(BigDecimal.ZERO);

		final IPPOrderDAO ppOrdersRepo = Services.get(IPPOrderDAO.class);
		ppOrdersRepo.save(ppOrder);
	}

	@Override
	public void changeScheduling(@NonNull final PPOrderScheduleChangeRequest request)
	{
		Services.get(IPPOrderRoutingRepository.class).changeActivitiesScheduling(request.getOrderId(), request.getActivityChangeRequests());
		Services.get(IPPOrderDAO.class).changeOrderScheduling(request.getOrderId(), request.getScheduledStartDate(), request.getScheduledEndDate());
	}

	@Override
	public void createOrderRouting(@NonNull final I_PP_Order ppOrderRecord)
	{
		final PPOrderRouting orderRouting = CreateOrderRoutingCommand.builder()
				.routingId(PPRoutingId.ofRepoId(ppOrderRecord.getAD_Workflow_ID()))
				.ppOrderId(PPOrderId.ofRepoId(ppOrderRecord.getPP_Order_ID()))
				.dateStartSchedule(TimeUtil.asLocalDateTime(ppOrderRecord.getDateStartSchedule()))
				.qtyOrdered(getQtyOrdered(ppOrderRecord))
				.build()
				.execute();

		final IPPOrderRoutingRepository orderRoutingsRepo = Services.get(IPPOrderRoutingRepository.class);
		orderRoutingsRepo.save(orderRouting);
	}

	@Override
	public Quantity getQtyOrdered(final I_PP_Order ppOrderRecord)
	{
		final I_C_UOM mainProductUOM = getMainProductStockingUOM(ppOrderRecord);
		return Quantity.of(ppOrderRecord.getQtyOrdered(), mainProductUOM);
	}

	@Override
	public void closeAllActivities(@NonNull final PPOrderId orderId)
	{
		reportQtyToProcessOnNotStartedActivities(orderId);

		final IPPOrderRoutingRepository orderRoutingsRepo = Services.get(IPPOrderRoutingRepository.class);
		final PPOrderRouting orderRouting = orderRoutingsRepo.getByOrderId(orderId);

		for (final PPOrderRoutingActivity activity : orderRouting.getActivities())
		{
			final PPOrderRoutingActivityStatus activityStatus = activity.getStatus();
			if (activityStatus == PPOrderRoutingActivityStatus.IN_PROGRESS
					|| activityStatus == PPOrderRoutingActivityStatus.COMPLETED)
			{
				orderRouting.closeActivity(activity.getId());
			}
		}

		orderRoutingsRepo.save(orderRouting);
	}

	private void reportQtyToProcessOnNotStartedActivities(final PPOrderId orderId)
	{
		final IPPOrderDAO ordersRepo = Services.get(IPPOrderDAO.class);
		final IPPOrderRoutingRepository orderRoutingsRepo = Services.get(IPPOrderRoutingRepository.class);
		final IPPCostCollectorBL costCollectorsService = Services.get(IPPCostCollectorBL.class);

		final PPOrderRouting orderRouting = orderRoutingsRepo.getByOrderId(orderId);
		final I_PP_Order orderRecord = ordersRepo.getById(orderId);
		final LocalDateTime reportDate = SystemTime.asLocalDateTime();

		for (final PPOrderRoutingActivity activity : orderRouting.getActivities())
		{
			final PPOrderRoutingActivityStatus activityStatus = activity.getStatus();
			if (activityStatus == PPOrderRoutingActivityStatus.NOT_STARTED)
			{
				final Quantity qtyToProcess = activity.getQtyToDeliver();
				if (qtyToProcess.signum() <= 0)
				{
					// TODO: should we create a negate CC?
					continue;
				}

				final Duration setupTimeRemaining = activity.getSetupTimeRemaining();
				final WorkingTime durationRemaining = WorkingTime.builder()
						.durationPerOneUnit(activity.getDurationPerOneUnit())
						.unitsPerCycle(activity.getUnitsPerCycle())
						.qty(qtyToProcess.toBigDecimal())
						.activityTimeUnit(activity.getDurationUnit())
						.build();

				costCollectorsService.createActivityControl(ActivityControlCreateRequest.builder()
						.order(orderRecord)
						.orderActivity(activity)
						.movementDate(reportDate)
						.qtyMoved(qtyToProcess)
						.durationSetup(setupTimeRemaining)
						.duration(durationRemaining.getDuration())
						.build());
			}
		}
	}

	@Override
	public void voidOrderRouting(final PPOrderId orderId)
	{
		final IPPOrderRoutingRepository orderRoutingRepo = Services.get(IPPOrderRoutingRepository.class);
		final PPOrderRouting orderRouting = orderRoutingRepo.getByOrderId(orderId);
		orderRouting.voidIt();
		orderRoutingRepo.save(orderRouting);
	}

}
