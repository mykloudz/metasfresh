package de.metas.purchasecandidate.grossprofit;

import de.metas.money.Money;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.purchasecandidate.base
 * %%
 * Copyright (C) 2018 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation,
 * either version 2 of the
 * License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not,
 * see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@Value
@Builder
public class PurchaseProfitInfo
{
	/** sales priceActual minus cash discount minus refund/bonus (if any) */
	Money customerPriceGrossProfit;
	
	Money purchasePriceActual;

	/** purchase priceActual minus cash discount minus refund/bonus (if any); should better be less than {@link #customerPriceGrossProfit}.. */
	Money priceGrossProfit;

	private PurchaseProfitInfo(
			@NonNull final Money purchasePriceActual,
			@NonNull final Money customerPriceGrossProfit,
			@NonNull final Money priceGrossProfit)
	{
		// Money.getCommonCurrencyOfAll(purchasePriceActual, customerPriceGrossProfit, priceGrossProfit);

		this.purchasePriceActual = purchasePriceActual;
		this.customerPriceGrossProfit = customerPriceGrossProfit;
		this.priceGrossProfit = priceGrossProfit;

	}

}