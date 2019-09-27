package de.metas.invoicecandidate.internalbusinesslogic;

import static de.metas.util.lang.CoalesceUtil.coalesce;

import java.util.List;

import javax.annotation.Nullable;

import org.adempiere.exceptions.AdempiereException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.metas.pricing.InvoicableQtyBasedOn;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.quantity.StockQtyAndUOMQty;
import de.metas.quantity.StockQtyAndUOMQtys;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

/*
 * #%L
 * de.metas.swat.base
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

@Value
public class ShipmentData
{
	ProductId productId;

	@JsonIgnore
	boolean empty;

	Quantity qtyInStockUom;

	Quantity qtyNominal;

	Quantity qtyCatch;

	List<ShippedQtyItem> shippedQtyItems;

	@Builder
	@JsonCreator
	private ShipmentData(
			@JsonProperty("productId") @NonNull ProductId productId,
			@JsonProperty("qtyInStockUom") @NonNull Quantity qtyInStockUom,
			@JsonProperty("qtyNominal") @NonNull Quantity qtyNominal,
			@JsonProperty("qtyCatch") @Nullable Quantity qtyCatch,
			@JsonProperty("shippedQtyItems") @Singular List<ShippedQtyItem> shippedQtyItems)
	{
		this.productId = productId;
		this.empty = shippedQtyItems.isEmpty();
		this.qtyInStockUom = qtyInStockUom;
		this.qtyNominal = qtyNominal;
		this.qtyCatch = qtyCatch;
		this.shippedQtyItems = shippedQtyItems;
	}

	public StockQtyAndUOMQty computeInvoicableQtyDelivered(@NonNull final InvoicableQtyBasedOn invoicableQtyBasedOn)
	{
		if(empty)
		{
			return StockQtyAndUOMQtys.createZero(productId, getQtyNominal().getUomId());
		}
		Quantity deliveredInUom;
		switch (invoicableQtyBasedOn)
		{
			case CatchWeight:
				deliveredInUom = coalesce(getQtyCatch(), getQtyNominal());
				break;
			case NominalWeight:
				deliveredInUom = getQtyNominal();
				break;
			default:
				throw new AdempiereException("Unexpected InvoicableQtyBasedOn=" + invoicableQtyBasedOn);
		}

		return StockQtyAndUOMQty.builder()
				.productId(productId)
				.stockQty(qtyInStockUom)
				.uomQty(deliveredInUom).build();
	}
}
