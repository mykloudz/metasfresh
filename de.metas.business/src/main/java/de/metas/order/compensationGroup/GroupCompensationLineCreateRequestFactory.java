package de.metas.order.compensationGroup;

import java.math.BigDecimal;

import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_Product;
import org.compiere.model.X_C_OrderLine;
import org.springframework.stereotype.Service;

import de.metas.pricing.IEditablePricingContext;
import de.metas.pricing.IPricingResult;
import de.metas.pricing.rules.Discount;
import de.metas.pricing.service.IPricingBL;
import de.metas.product.IProductBL;
import de.metas.product.IProductDAO;
import de.metas.product.ProductId;
import de.metas.uom.UomId;
import de.metas.util.Services;
import de.metas.util.lang.CoalesceUtil;
import de.metas.util.lang.Percent;
import lombok.NonNull;

/*
 * #%L
 * de.metas.business
 * %%
 * Copyright (C) 2018 metas GmbH
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
public class GroupCompensationLineCreateRequestFactory
{

	public GroupCompensationLineCreateRequest createGroupCompensationLineCreateRequest(@NonNull final GroupTemplateLine templateLine, @NonNull final Group group)
	{
		final IProductBL productBL = Services.get(IProductBL.class);
		final IProductDAO productsRepo = Services.get(IProductDAO.class);

		final ProductId productId = templateLine.getProductId();
		final I_M_Product product = productsRepo.getById(productId);
		final I_C_UOM uom = productBL.getStockUOM(product);

		final GroupCompensationType type = extractGroupCompensationType(product);
		final GroupCompensationAmtType amtType = extractGroupCompensationAmtType(product);

		Percent percentage = Percent.ZERO;
		if (GroupCompensationType.Discount.equals(type) && GroupCompensationAmtType.Percent.equals(amtType))
		{
			percentage = calculateDefaultDiscountPercentage(templateLine, group);
		}

		return GroupCompensationLineCreateRequest.builder()
				.productId(productId)
				.uomId(UomId.ofRepoId(uom.getC_UOM_ID()))
				.type(type)
				.amtType(amtType)
				.percentage(percentage)
				.qtyEntered(BigDecimal.ZERO)
				.price(BigDecimal.ZERO)
				.groupTemplateLineId(templateLine.getId())
				.build();
	}

	private static final GroupCompensationType extractGroupCompensationType(final I_M_Product product)
	{
		return GroupCompensationType.ofAD_Ref_List_Value(CoalesceUtil.coalesce(product.getGroupCompensationType(), X_C_OrderLine.GROUPCOMPENSATIONTYPE_Discount));
	}

	private static final GroupCompensationAmtType extractGroupCompensationAmtType(final I_M_Product product)
	{
		return GroupCompensationAmtType.ofAD_Ref_List_Value(CoalesceUtil.coalesce(product.getGroupCompensationAmtType(), X_C_OrderLine.GROUPCOMPENSATIONAMTTYPE_Percent));
	}

	private Percent calculateDefaultDiscountPercentage(final GroupTemplateLine templateLine, final Group group)
	{
		if (templateLine.getPercentage() != null)
		{
			return templateLine.getPercentage();
		}

		return retrieveDiscountPercentageFromPricing(templateLine, group);
	}

	private final Percent retrieveDiscountPercentageFromPricing(final GroupTemplateLine templateLine, final Group group)
	{
		final IPricingBL pricingBL = Services.get(IPricingBL.class);

		final IEditablePricingContext pricingCtx = pricingBL.createPricingContext();
		pricingCtx.setProductId(templateLine.getProductId());
		pricingCtx.setBPartnerId(group.getBpartnerId());
		pricingCtx.setSOTrx(group.getSoTrx());
		pricingCtx.setDisallowDiscount(false);// just to be sure
		pricingCtx.setQty(BigDecimal.ONE);

		final IPricingResult pricingResult = pricingBL.createInitialResult(pricingCtx);
		pricingResult.setCalculated(true); // important, else the Discount rule does not react
		pricingResult.setPriceStd(group.getTotalNetAmt());

		final Discount discountRule = new Discount();
		discountRule.calculate(pricingCtx, pricingResult);

		return pricingResult.getDiscount();
	}

}
