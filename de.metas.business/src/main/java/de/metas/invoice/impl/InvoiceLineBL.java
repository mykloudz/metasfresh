package de.metas.invoice.impl;

/*
 * #%L
 * de.metas.swat.base
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;

import org.adempiere.exceptions.TaxCategoryNotFoundException;
import org.adempiere.invoice.service.IInvoiceBL;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.warehouse.WarehouseId;
import org.adempiere.warehouse.api.IWarehouseBL;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_Tax;
import org.compiere.model.I_M_InOut;
import org.compiere.model.I_M_PriceList;
import org.compiere.model.I_M_PriceList_Version;
import org.compiere.model.I_M_ProductPrice;
import org.compiere.model.MTax;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.slf4j.Logger;

import de.metas.adempiere.model.I_C_InvoiceLine;
import de.metas.currency.CurrencyPrecision;
import de.metas.invoice.IInvoiceLineBL;
import de.metas.location.CountryId;
import de.metas.logging.LogManager;
import de.metas.organization.OrgId;
import de.metas.pricing.IEditablePricingContext;
import de.metas.pricing.IPricingResult;
import de.metas.pricing.PriceListId;
import de.metas.pricing.PricingSystemId;
import de.metas.pricing.conditions.service.PricingConditionsResult;
import de.metas.pricing.exceptions.ProductNotOnPriceListException;
import de.metas.pricing.service.IPriceListBL;
import de.metas.pricing.service.IPriceListDAO;
import de.metas.pricing.service.IPricingBL;
import de.metas.pricing.service.ProductPrices;
import de.metas.product.ProductId;
import de.metas.tax.api.ITaxBL;
import de.metas.tax.api.TaxCategoryId;
import de.metas.tax.api.TaxNotFoundException;
import de.metas.uom.IUOMConversionBL;
import de.metas.uom.UOMConversionContext;
import de.metas.uom.UomId;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

public class InvoiceLineBL implements IInvoiceLineBL
{

	private static final Logger logger = LogManager.getLogger(InvoiceLineBL.class);

	@Override
	public void setTaxAmtInfo(final Properties ctx, final I_C_InvoiceLine il, final String getTrxName)
	{
		final IInvoiceBL invoiceBL = Services.get(IInvoiceBL.class);
		final ITaxBL taxBL = Services.get(ITaxBL.class);

		final int taxId = il.getC_Tax_ID();

		final boolean taxIncluded = invoiceBL.isTaxIncluded(il);
		final BigDecimal lineNetAmt = il.getLineNetAmt();
		final CurrencyPrecision taxPrecision = invoiceBL.getTaxPrecision(il);

		final I_C_Tax tax = MTax.get(ctx, taxId);
		final BigDecimal taxAmtInfo = taxBL.calculateTax(tax, lineNetAmt, taxIncluded, taxPrecision.toInt());

		il.setTaxAmtInfo(taxAmtInfo);
	}

	@Override
	public boolean setTax(final Properties ctx, final org.compiere.model.I_C_InvoiceLine il, final String trxName)
	{
		TaxCategoryId taxCategoryId = TaxCategoryId.ofRepoIdOrNull(il.getC_TaxCategory_ID());
		if (taxCategoryId == null && il.getM_Product_ID() > 0)
		{
			// NOTE: we can retrieve the tax category only if we have a product
			taxCategoryId = getTaxCategoryId(il);
			il.setC_TaxCategory_ID(TaxCategoryId.toRepoId(taxCategoryId));
		}

		if (il.getM_InOutLine_ID() <= 0)
		{
			logger.debug(il + "has M_InOutLine_ID=" + il.getM_InOutLine_ID() + ": returning");
			return false;
		}

		if (il.getM_Product_ID() <= 0)
		{
			// this might be the case if a descriptional il refers to an iol.
			logger.debug(il + "has M_Product_ID=" + il.getM_Product_ID() + ": returning");
			return false;
		}

		final I_M_InOut io = il.getM_InOutLine().getM_InOut();

		final WarehouseId warehouseId = WarehouseId.ofRepoId(io.getM_Warehouse_ID());
		final CountryId countryFromId = Services.get(IWarehouseBL.class).getCountryId(warehouseId);

		final I_C_BPartner_Location locationTo = InterfaceWrapperHelper.create(io.getC_BPartner_Location(), I_C_BPartner_Location.class);

		final Timestamp shipDate = io.getMovementDate();
		final int taxId = Services.get(ITaxBL.class).retrieveTaxIdForCategory(ctx,
				countryFromId,
				OrgId.ofRepoId(io.getAD_Org_ID()),
				locationTo,
				shipDate,
				taxCategoryId,
				il.getC_Invoice().isSOTrx(),
				false);

		if (taxId <= 0)
		{
			final I_C_Invoice invoice = il.getC_Invoice();
			throw TaxNotFoundException.builder()
					.taxCategoryId(taxCategoryId)
					.isSOTrx(io.isSOTrx())
					.shipDate(shipDate)
					.shipFromCountryId(countryFromId)
					.shipToC_Location_ID(locationTo.getC_Location_ID())
					.billDate(invoice.getDateInvoiced())
					.billFromCountryId(countryFromId)
					.billToC_Location_ID(invoice.getC_BPartner_Location().getC_Location_ID())
					.build();
		}

		final boolean taxChange = il.getC_Tax_ID() != taxId;
		if (taxChange)
		{
			logger.info("Changing C_Tax_ID to " + taxId + " for " + il);
			il.setC_Tax_ID(taxId);

			final I_C_Tax tax = il.getC_Tax();
			il.setC_TaxCategory_ID(tax.getC_TaxCategory_ID());
		}
		return taxChange;
	}

	@Override
	public boolean isPriceLocked(final I_C_InvoiceLine invoiceLine)
	{
		// // Introduced by US1184, because having the same price on Order and Invoice
		// no - invoice does not generally have to have the same prive not generally
		// // is enforced by German Law
		// if (invoiceLine.getC_OrderLine_ID() > 0)
		// return true;
		//
		// return false;
		return false;
	}

	@Override
	public TaxCategoryId getTaxCategoryId(final org.compiere.model.I_C_InvoiceLine invoiceLine)
	{
		// FIXME: we need to retrieve the C_TaxCategory_ID by using Pricing Engine

		if (invoiceLine.getC_Charge_ID() > 0)
		{
			return TaxCategoryId.ofRepoId(invoiceLine.getC_Charge().getC_TaxCategory_ID());
		}

		final I_C_Invoice invoice = invoiceLine.getC_Invoice();
		if (invoice.getM_PriceList_ID() != IPriceListDAO.M_PriceList_ID_None)
		{
			return getTaxCategoryFromProductPrice(invoiceLine, invoice);
		}

		// Fallback: try getting from Order Line
		if (invoiceLine.getC_OrderLine_ID() > 0)
		{
			return TaxCategoryId.ofRepoIdOrNull(invoiceLine.getC_OrderLine().getC_TaxCategory_ID());
		}

		// Fallback: try getting from Invoice -> Order
		if (invoiceLine.getC_Invoice().getC_Order_ID() > 0)
		{
			return getTaxCategoryFromOrder(invoiceLine, invoice);
		}

		throw new TaxCategoryNotFoundException(invoiceLine);
	}

	private TaxCategoryId getTaxCategoryFromProductPrice(
			final org.compiere.model.I_C_InvoiceLine invoiceLine,
			final I_C_Invoice invoice)
	{
		final IPriceListDAO priceListDAO = Services.get(IPriceListDAO.class);
		final Boolean processedPLVFiltering = null; // task 09533: the user doesn't know about PLV's processed flag, so we can't filter by it

		final I_M_PriceList priceList = invoice.getM_PriceList();

		final I_M_PriceList_Version priceListVersion = priceListDAO.retrievePriceListVersionOrNull(
				priceList,
				TimeUtil.asLocalDate(invoice.getDateInvoiced()),
				processedPLVFiltering);
		Check.errorIf(priceListVersion == null, "Missing PLV for M_PriceList and DateInvoiced of {}", invoice);

		final ProductId productId = ProductId.ofRepoIdOrNull(invoiceLine.getM_Product_ID());
		Check.assumeNotNull(productId, "M_Product_ID > 0 for {}", invoiceLine);

		final I_M_ProductPrice productPrice = Optional
				.ofNullable(ProductPrices.retrieveMainProductPriceOrNull(priceListVersion, productId))
				.orElseThrow(() -> new TaxCategoryNotFoundException(invoiceLine));

		return TaxCategoryId.ofRepoId(productPrice.getC_TaxCategory_ID());
	}

	private TaxCategoryId getTaxCategoryFromOrder(
			final org.compiere.model.I_C_InvoiceLine invoiceLine,
			final I_C_Invoice invoice)
	{
		final IPriceListDAO priceListDAO = Services.get(IPriceListDAO.class);
		final Boolean processedPLVFiltering = null; // task 09533: the user doesn't know about PLV's processed flag, so we can't filter by it

		final Properties ctx = InterfaceWrapperHelper.getCtx(invoiceLine);
		final String trxName = InterfaceWrapperHelper.getTrxName(invoiceLine);

		final I_C_Order order = InterfaceWrapperHelper.create(ctx, invoiceLine.getC_Invoice().getC_Order_ID(), I_C_Order.class, trxName);

		final I_M_PriceList priceList = Services.get(IPriceListDAO.class).getById(order.getM_PriceList_ID());

		final I_M_PriceList_Version priceListVersion = priceListDAO.retrievePriceListVersionOrNull(
				priceList,
				TimeUtil.asLocalDate(invoice.getDateInvoiced()),
				processedPLVFiltering);
		Check.errorIf(priceListVersion == null, "Missing PLV for M_PriceList and DateInvoiced of {}", invoice);

		final ProductId productId = ProductId.ofRepoIdOrNull(invoiceLine.getM_Product_ID());
		Check.assumeNotNull(productId, "M_Product_ID > 0 for {}", invoiceLine);

		final I_M_ProductPrice productPrice = Optional
				.ofNullable(ProductPrices.retrieveMainProductPriceOrNull(priceListVersion, productId))
				.orElseThrow(() -> new TaxCategoryNotFoundException(invoiceLine));
		return TaxCategoryId.ofRepoId(productPrice.getC_TaxCategory_ID());
	}

	@Override
	public void setQtyInvoicedInPriceUOM(final I_C_InvoiceLine invoiceLine)
	{
		final BigDecimal qtyInvoicedInPriceUOM = calculateQtyInvoicedInPriceUOM(invoiceLine);
		invoiceLine.setQtyInvoicedInPriceUOM(qtyInvoicedInPriceUOM);
	}

	private BigDecimal calculateQtyInvoicedInPriceUOM(@NonNull final I_C_InvoiceLine ilRecord)
	{
		final BigDecimal qtyEntered = ilRecord.getQtyEntered();
		Check.assumeNotNull(qtyEntered, "qtyEntered not null; ilRecord={}", ilRecord);

		final UomId priceUomId = UomId.ofRepoIdOrNull(ilRecord.getPrice_UOM_ID());
		if (priceUomId == null)
		{
			return qtyEntered;
		}

		final ProductId productId = ProductId.ofRepoIdOrNull(ilRecord.getM_Product_ID());

		final IUOMConversionBL uomConversionBL = Services.get(IUOMConversionBL.class);
		final BigDecimal qtyInPriceUOM = uomConversionBL.convertQty(
				UOMConversionContext.of(productId),
				qtyEntered,
				UomId.ofRepoId(ilRecord.getC_UOM_ID()),
				priceUomId);

		return qtyInPriceUOM;
	}

	@Override
	public IEditablePricingContext createPricingContext(final I_C_InvoiceLine invoiceLine)
	{
		final I_C_Invoice invoice = invoiceLine.getC_Invoice();
		final PriceListId priceListId = PriceListId.ofRepoIdOrNull(invoice.getM_PriceList_ID());

		final BigDecimal qtyInvoicedInPriceUOM = calculateQtyInvoicedInPriceUOM(invoiceLine);

		return createPricingContext(invoiceLine, priceListId, qtyInvoicedInPriceUOM);
	}

	public IEditablePricingContext createPricingContext(final I_C_InvoiceLine invoiceLine,
			final PriceListId priceListId,
			final BigDecimal priceQty)
	{
		final I_C_Invoice invoice = invoiceLine.getC_Invoice();

		final boolean isSOTrx = invoice.isSOTrx();

		final int productId = invoiceLine.getM_Product_ID();

		final int bPartnerId = invoice.getC_BPartner_ID();

		final LocalDate date = TimeUtil.asLocalDate(invoice.getDateInvoiced());

		final IEditablePricingContext pricingCtx = Services.get(IPricingBL.class).createInitialContext(
				productId,
				bPartnerId,
				invoiceLine.getPrice_UOM_ID(),
				priceQty,
				isSOTrx);
		pricingCtx.setPriceDate(date);

		// 03152: setting the 'ol' to allow the subscription system to compute the right price
		pricingCtx.setReferencedObject(invoiceLine);

		pricingCtx.setPriceListId(priceListId);
		// PLV is only accurate if PL selected in header
		// metas: relay on M_PriceList_ID only, don't use M_PriceList_Version_ID
		// pricingCtx.setM_PriceList_Version_ID(orderLine.getM_PriceList_Version_ID());

		final CountryId countryId = getCountryIdOrNull(invoiceLine);
		pricingCtx.setCountryId(countryId);

		return pricingCtx;
	}

	private CountryId getCountryIdOrNull(@NonNull final org.compiere.model.I_C_InvoiceLine invoiceLine)
	{
		final I_C_Invoice invoice = invoiceLine.getC_Invoice();

		if (invoice.getC_BPartner_Location_ID() <= 0)
		{
			return null;
		}

		final I_C_BPartner_Location bPartnerLocation = invoice.getC_BPartner_Location();
		if (bPartnerLocation.getC_Location_ID() <= 0)
		{
			return null;
		}

		return CountryId.ofRepoId(bPartnerLocation.getC_Location().getC_Country_ID());
	}

	@Override
	public void updateLineNetAmt(final I_C_InvoiceLine line, final BigDecimal qtyEntered)
	{
		if (qtyEntered != null)
		{
			final I_C_Invoice invoice = line.getC_Invoice();
			final PriceListId priceListId = PriceListId.ofRepoId(invoice.getM_PriceList_ID());

			//
			// We need to get the quantity in the pricing's UOM (if different)
			final BigDecimal convertedQty = calculateQtyInvoicedInPriceUOM(line);

			// this code has been borrowed from
			// org.compiere.model.CalloutOrder.amt
			final CurrencyPrecision netPrecision = Services.get(IPriceListBL.class).getAmountPrecision(priceListId);

			BigDecimal lineNetAmt = netPrecision.roundIfNeeded(convertedQty.multiply(line.getPriceActual()));
			logger.debug("LineNetAmt={}", lineNetAmt);
			line.setLineNetAmt(lineNetAmt);
		}
	}

	@Override
	public void updatePrices(final I_C_InvoiceLine invoiceLine)
	{
		// Product was not set yet. There is no point to calculate the prices
		if (invoiceLine.getM_Product_ID() <= 0)
		{
			return;
		}

		//
		// Calculate Pricing Result
		final IEditablePricingContext pricingCtx = createPricingContext(invoiceLine);
		final boolean usePriceUOM = InterfaceWrapperHelper.isNew(invoiceLine);
		pricingCtx.setConvertPriceToContextUOM(!usePriceUOM);

		pricingCtx.setManualPriceEnabled(invoiceLine.isManualPrice());

		if (pricingCtx.getManualPriceEnabled().isTrue())
		{
			// Task 08908: do not calculate the prices in case the price is manually set
			return;
		}

		final IPricingResult pricingResult = Services.get(IPricingBL.class).calculatePrice(pricingCtx);
		if (!pricingResult.isCalculated())
		{
			throw new ProductNotOnPriceListException(pricingCtx, invoiceLine.getLine());
		}

		//
		// PriceList
		final BigDecimal priceList = pricingResult.getPriceList();
		invoiceLine.setPriceList(priceList);

		invoiceLine.setPriceLimit(pricingResult.getPriceLimit());
		invoiceLine.setPrice_UOM_ID(UomId.toRepoId(pricingResult.getPriceUomId()));

		invoiceLine.setPriceEntered(pricingResult.getPriceStd());
		invoiceLine.setPriceActual(pricingResult.getPriceStd()); // will be updated in a few lines, if there is a discount

		// Issue https://github.com/metasfresh/metasfresh/issues/2400:
		// If the line has a discout, we assome it was manually added and stick with it
		// When invoices are created by the system, there is no need to change an already-set discound (and this code is executed only once anyways)
		if (invoiceLine.getDiscount().signum() == 0)
		{
			invoiceLine.setDiscount(pricingResult.getDiscount().toBigDecimal());
		}

		final PricingConditionsResult pricingConditions = pricingResult.getPricingConditions();
		invoiceLine.setBase_PricingSystem_ID(pricingConditions != null ? PricingSystemId.toRepoId(pricingConditions.getBasePricingSystemId()) : -1);

		//
		// Calculate PriceActual from PriceEntered and Discount
		calculatePriceActual(invoiceLine, pricingResult.getPrecision());

		invoiceLine.setPrice_UOM_ID(UomId.toRepoId(pricingResult.getPriceUomId())); //

	}

	private static void calculatePriceActual(final I_C_InvoiceLine invoiceLine, final CurrencyPrecision precision)
	{
		final BigDecimal discount = invoiceLine.getDiscount();
		final BigDecimal priceEntered = invoiceLine.getPriceEntered();

		BigDecimal priceActual;
		if (priceEntered.signum() == 0)
		{
			priceActual = priceEntered;
		}
		else
		{
			final CurrencyPrecision pricePrecision;
			if (precision != null)
			{
				pricePrecision = precision;
			}
			else
			{
				final I_C_Invoice invoice = invoiceLine.getC_Invoice();
				pricePrecision = Services.get(IPriceListBL.class).getPricePrecision(PriceListId.ofRepoId(invoice.getM_PriceList_ID()));
			}

			priceActual = subtractDiscount(priceEntered, discount, pricePrecision);
		}

		invoiceLine.setPriceActual(priceActual);
	}

	private static BigDecimal subtractDiscount(final BigDecimal baseAmount, final BigDecimal discount, final CurrencyPrecision precision)
	{
		BigDecimal multiplier = Env.ONEHUNDRED.subtract(discount);
		multiplier = multiplier.divide(Env.ONEHUNDRED, precision.toInt() * 3, RoundingMode.HALF_UP);

		final BigDecimal result = baseAmount.multiply(multiplier).setScale(precision.toInt(), RoundingMode.HALF_UP);
		return result;
	}
}
