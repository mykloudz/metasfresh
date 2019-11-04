package org.eevolution.callout;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;

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

import org.adempiere.ad.callout.annotations.Callout;
import org.adempiere.ad.callout.annotations.CalloutMethod;
import org.adempiere.mm.attributes.AttributeSetInstanceId;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.warehouse.LocatorId;
import org.adempiere.warehouse.WarehouseId;
import org.adempiere.warehouse.api.IWarehouseBL;
import org.compiere.model.CalloutEngine;
import org.compiere.model.I_C_DocType;
import org.eevolution.api.IPPOrderBL;
import org.eevolution.api.IProductBOMDAO;
import org.eevolution.api.ProductBOMId;
import org.eevolution.model.I_PP_Order;
import org.eevolution.model.I_PP_Product_BOM;
import org.eevolution.model.I_PP_Product_Planning;

import de.metas.document.DocTypeId;
import de.metas.document.IDocTypeDAO;
import de.metas.document.sequence.IDocumentNoBuilderFactory;
import de.metas.document.sequence.impl.IDocumentNoInfo;
import de.metas.material.planning.IProductPlanningDAO;
import de.metas.material.planning.IProductPlanningDAO.ProductPlanningQuery;
import de.metas.material.planning.pporder.IPPRoutingRepository;
import de.metas.material.planning.pporder.PPRoutingId;
import de.metas.organization.OrgId;
import de.metas.product.IProductBL;
import de.metas.product.ProductId;
import de.metas.product.ResourceId;
import de.metas.uom.IUOMConversionBL;
import de.metas.uom.UomId;
import de.metas.util.Services;
import lombok.NonNull;

/**
 * Manufacturing order callout
 *
 * @author metas-dev <dev@metasfresh.com>
 * @author based on initial version developed by Victor Perez, Teo Sarca under ADempiere project
 */
@Callout(I_PP_Order.class)
public class PP_Order extends CalloutEngine
{
	/**
	 * When document type (target) is changed, update the DocumentNo.
	 */
	@CalloutMethod(columnNames = I_PP_Order.COLUMNNAME_C_DocTypeTarget_ID)
	public void onC_DocTypeTarget_ID(I_PP_Order ppOrder)
	{
		final DocTypeId docTypeTargetId = DocTypeId.ofRepoIdOrNull(ppOrder.getC_DocTypeTarget_ID());
		final I_C_DocType docTypeTarget = docTypeTargetId != null
				? Services.get(IDocTypeDAO.class).getById(docTypeTargetId)
				: null;

		final IDocumentNoInfo documentNoInfo = Services.get(IDocumentNoBuilderFactory.class)
				.createPreliminaryDocumentNoBuilder()
				.setNewDocType(docTypeTarget)
				.setOldDocType_ID(ppOrder.getC_DocType_ID())
				.setOldDocumentNo(ppOrder.getDocumentNo())
				.setDocumentModel(ppOrder)
				.buildOrNull();
		if (documentNoInfo == null)
		{
			return;
		}

		// DocumentNo
		if (documentNoInfo.isDocNoControlled())
		{
			ppOrder.setDocumentNo(documentNoInfo.getDocumentNo());
		}

		return;
	}

	@CalloutMethod(columnNames = I_PP_Order.COLUMNNAME_M_Product_ID)
	public void onProductChanged(final I_PP_Order ppOrder)
	{
		final ProductId productId = ProductId.ofRepoIdOrNull(ppOrder.getM_Product_ID());
		if (productId == null)
		{
			return;
		}

		final UomId uomId = Services.get(IProductBL.class).getStockUOMId(productId);
		ppOrder.setC_UOM_ID(uomId.getRepoId());

		final I_PP_Product_Planning pp = findPP_Product_Planning(ppOrder);
		ppOrder.setAD_Workflow_ID(pp.getAD_Workflow_ID());
		
		final ProductBOMId productBOMId = ProductBOMId.ofRepoIdOrNull(pp.getPP_Product_BOM_ID());
		ppOrder.setPP_Product_BOM_ID(ProductBOMId.toRepoId(productBOMId));

		if (productBOMId != null)
		{
			final IProductBOMDAO productBOMsRepo = Services.get(IProductBOMDAO.class);
			final I_PP_Product_BOM bom = productBOMsRepo.getById(productBOMId);
			ppOrder.setC_UOM_ID(bom.getC_UOM_ID());
		}

		Services.get(IPPOrderBL.class).updateQtyBatchs(ppOrder, true); // override
	}

	@CalloutMethod(columnNames = I_PP_Order.COLUMNNAME_QtyEntered)
	public void onQtyEnteredChanged(final I_PP_Order ppOrder)
	{
		updateQtyOrdered(ppOrder);

		Services.get(IPPOrderBL.class).updateQtyBatchs(ppOrder, true); // override
	}

	@CalloutMethod(columnNames = I_PP_Order.COLUMNNAME_C_UOM_ID)
	public void onC_UOM_ID(final I_PP_Order ppOrder)
	{
		updateQtyOrdered(ppOrder);
	}

	@CalloutMethod(columnNames = I_PP_Order.COLUMNNAME_M_Warehouse_ID)
	public void onM_Warehouse_ID(final I_PP_Order ppOrder)
	{
		final WarehouseId warehouseId = WarehouseId.ofRepoIdOrNull(ppOrder.getM_Warehouse_ID());
		if (warehouseId == null)
		{
			return;
		}

		LocatorId locatorId = Services.get(IWarehouseBL.class).getDefaultLocatorId(warehouseId);
		ppOrder.setM_Locator_ID(locatorId.getRepoId());
	}

	/** Calculates and sets QtyOrdered from QtyEntered and UOM */
	private final void updateQtyOrdered(final I_PP_Order ppOrder)
	{
		final ProductId productId = ProductId.ofRepoIdOrNull(ppOrder.getM_Product_ID());
		final UomId uomToId = UomId.ofRepoIdOrNull(ppOrder.getC_UOM_ID());
		final BigDecimal qtyEntered = ppOrder.getQtyEntered();

		BigDecimal qtyOrdered;
		if (productId == null || uomToId == null)
		{
			qtyOrdered = qtyEntered;
		}
		else
		{
			qtyOrdered = Services.get(IUOMConversionBL.class)
					.convertToProductUOM(productId, qtyEntered, uomToId);
			if (qtyOrdered == null)
			{
				qtyOrdered = qtyEntered;
			}
		}

		ppOrder.setQtyOrdered(qtyOrdered);
	}

	/**
	 * Find Product Planning Data for given manufacturing order. If not planning found, a new one is created and filled with default values.
	 * <p>
	 * TODO: refactor with org.eevolution.process.MRP.getProductPlanning method
	 *
	 * @param ctx context
	 * @param ppOrder manufacturing order
	 * @return product planning data (never return null)
	 */
	protected static I_PP_Product_Planning findPP_Product_Planning(@NonNull final I_PP_Order ppOrder)
	{
		final ProductPlanningQuery query = ProductPlanningQuery.builder()
				.orgId(OrgId.ofRepoIdOrAny(ppOrder.getAD_Org_ID()))
				.warehouseId(WarehouseId.ofRepoIdOrNull(ppOrder.getM_Warehouse_ID()))
				.plantId(ResourceId.ofRepoIdOrNull(ppOrder.getS_Resource_ID()))
				.productId(ProductId.ofRepoId(ppOrder.getM_Product_ID()))
				.attributeSetInstanceId(AttributeSetInstanceId.ofRepoId(ppOrder.getM_AttributeSetInstance_ID()))
				.build();
		I_PP_Product_Planning pp = Services.get(IProductPlanningDAO.class).find(query);

		if (pp == null)
		{
			pp = newInstance(I_PP_Product_Planning.class);
			pp.setAD_Org_ID(ppOrder.getAD_Org_ID());
			pp.setM_Warehouse_ID(ppOrder.getM_Warehouse_ID());
			pp.setS_Resource_ID(ppOrder.getS_Resource_ID());
			pp.setM_Product_ID(ppOrder.getM_Product_ID());
		}
		InterfaceWrapperHelper.setSaveDeleteDisabled(pp, true);

		final ProductId productId = ProductId.ofRepoId(pp.getM_Product_ID());
		if (pp.getAD_Workflow_ID() <= 0)
		{
			final IPPRoutingRepository routingsRepo = Services.get(IPPRoutingRepository.class);
			final PPRoutingId routingId = routingsRepo.getRoutingIdByProductId(productId);
			pp.setAD_Workflow_ID(PPRoutingId.toRepoId(routingId));
		}
		if (pp.getPP_Product_BOM_ID() <= 0)
		{
			final IProductBOMDAO bomsRepo = Services.get(IProductBOMDAO.class);
			final ProductBOMId bomId = bomsRepo.getDefaultBOMIdByProductId(productId).orElse(null);
			if(bomId != null)
			{
				pp.setPP_Product_BOM_ID(bomId.getRepoId());
			}
		}

		return pp;
	}
}
