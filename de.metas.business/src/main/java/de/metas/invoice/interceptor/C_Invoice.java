package de.metas.invoice.interceptor;

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
import java.time.LocalDate;
import java.util.List;

import org.adempiere.ad.modelvalidator.annotations.DocValidate;
import org.adempiere.ad.modelvalidator.annotations.Interceptor;
import org.adempiere.ad.modelvalidator.annotations.ModelChange;
import org.adempiere.invoice.service.IInvoiceBL;
import org.adempiere.invoice.service.IInvoiceDAO;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_Payment;
import org.compiere.model.I_M_PriceList_Version;
import org.compiere.model.ModelValidator;
import org.compiere.util.TimeUtil;
import org.springframework.stereotype.Component;

import de.metas.adempiere.model.I_C_Invoice;
import de.metas.adempiere.model.I_C_InvoiceLine;
import de.metas.allocation.api.IAllocationBL;
import de.metas.allocation.api.IAllocationDAO;
import de.metas.bpartner.BPartnerId;
import de.metas.document.DocTypeId;
import de.metas.document.IDocTypeBL;
import de.metas.document.IDocumentLocationBL;
import de.metas.document.engine.DocStatus;
import de.metas.invoice.InvoiceId;
import de.metas.invoice.export.async.C_Invoice_CreateExportData;
import de.metas.money.CurrencyId;
import de.metas.money.Money;
import de.metas.order.OrderId;
import de.metas.payment.reservation.PaymentReservationCaptureRequest;
import de.metas.payment.reservation.PaymentReservationService;
import de.metas.pricing.service.IPriceListDAO;
import de.metas.pricing.service.ProductPrices;
import de.metas.product.ProductId;
import de.metas.util.Services;
import de.metas.util.time.SystemTime;
import lombok.NonNull;

@Interceptor(I_C_Invoice.class)
@Component
public class C_Invoice // 03771
{
	private final PaymentReservationService paymentReservationService;

	public C_Invoice(@NonNull final PaymentReservationService paymentReservationService)
	{
		this.paymentReservationService = paymentReservationService;
	}

	@DocValidate(timings = { ModelValidator.TIMING_BEFORE_COMPLETE })
	public void onBeforeComplete(final I_C_Invoice invoice)
	{
		allocateInvoiceAgainstCreditMemo(invoice);
		linkInvoiceToPaymentIfNeeded(invoice);
	}

	@DocValidate(timings = { ModelValidator.TIMING_AFTER_COMPLETE })
	public void onAfterComplete(final I_C_Invoice invoice)
	{
		markAsPaid(invoice);
		allocateInvoiceAgainstPaymentIfNeeded(invoice);
		captureMoneyIfNeeded(invoice);

		C_Invoice_CreateExportData.scheduleOnTrxCommit(invoice);
	}

	@DocValidate(timings = { ModelValidator.TIMING_AFTER_REVERSEACCRUAL, ModelValidator.TIMING_AFTER_REVERSECORRECT })
	public void onAfterReversal(final I_C_Invoice invoice)
	{
		Services.get(IInvoiceBL.class).handleReversalForInvoice(invoice);
	}

	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_NEW, ModelValidator.TYPE_BEFORE_CHANGE }, ifColumnsChanged = { I_C_Invoice.COLUMNNAME_C_BPartner_ID, I_C_Invoice.COLUMNNAME_C_BPartner_Location_ID, I_C_Invoice.COLUMNNAME_AD_User_ID })
	public void updateBPartnerAddress(final I_C_Invoice doc)
	{
		Services.get(IDocumentLocationBL.class).setBPartnerAddress(doc);
	}

	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_CHANGE }
	// exclude columns which are not relevant if they change
			, ignoreColumnsChanged = {
					I_C_Invoice.COLUMNNAME_IsPaid
			})
	public void updateIsReadOnly(final I_C_Invoice invoice)
	{
		Services.get(IInvoiceBL.class).updateInvoiceLineIsReadOnlyFlags(invoice);
	}

	/**
	 * 07634: Remove lines of products which are not in the invoice's price list / version.
	 */
	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_CHANGE }, ifColumnsChanged = { I_C_Invoice.COLUMNNAME_M_PriceList_ID })
	public void removeMaterialLinesNotCorrespondingToPriceList(final I_C_Invoice invoice)
	{
		LocalDate invoiceDate = TimeUtil.asLocalDate(invoice.getDateInvoiced());
		if (invoiceDate == null)
		{
			invoiceDate = SystemTime.asLocalDate();
		}

		final IPriceListDAO priceListDAO = Services.get(IPriceListDAO.class);

		final Boolean processedPLVFiltering = null; // task 09533: the user doesn't know about PLV's processed flag, so we can't filter by it
		final I_M_PriceList_Version priceListVersion = priceListDAO
				.retrievePriceListVersionOrNull(invoice.getM_PriceList(), invoiceDate, processedPLVFiltering); // can be null

		final String trxName = InterfaceWrapperHelper.getTrxName(invoice);

		final List<I_C_InvoiceLine> invoiceLines = Services.get(IInvoiceDAO.class).retrieveLines(invoice, trxName);
		for (final I_C_InvoiceLine invoiceLine : invoiceLines)
		{
			final ProductId productId = ProductId.ofRepoIdOrNull(invoiceLine.getM_Product_ID());
			if (!ProductPrices.hasMainProductPrice(priceListVersion, productId))
			{
				InterfaceWrapperHelper.delete(invoiceLine);
			}
		}
	}

	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_NEW })
	public void setIsDiscountPrinted(final I_C_Invoice invoice)
	{
		// do nothing in case of PO invoice
		if (!invoice.isSOTrx())
		{
			return;
		}

		final boolean isDiscountPrinted;

		// in case the invoice is linked to an order, set the IsDiscountPrinted from there
		final I_C_Order order = invoice.getC_Order();

		if (order != null && order.getC_Order_ID() > 0)
		{
			isDiscountPrinted = order.isDiscountPrinted();
		}
		else
		{
			// in case the invoice is not linked to an order, take the value from the partner

			final I_C_BPartner partner = invoice.getC_BPartner();

			isDiscountPrinted = partner.isDiscountPrinted();
		}

		invoice.setIsDiscountPrinted(isDiscountPrinted);
	}

	/**
	 * Mark invoice as paid if the grand total/open amount is 0
	 *
	 * @task 09489
	 */
	private void markAsPaid(final I_C_Invoice invoice)
	{
		// services
		final IInvoiceBL invoiceBL = Services.get(IInvoiceBL.class);

		final boolean ignoreProcessed = true; // need to ignoreProcessed, because right now, PRocessed not yet set to true by the engine.
		invoiceBL.testAllocation(invoice, ignoreProcessed);
	}

	/**
	 * Allocate the credit memo against it's parent invoices.
	 *
	 * Note: ATM, there should only be one parent invoice for a credit memo, but it's possible to have more in the future.
	 */
	private void allocateInvoiceAgainstCreditMemo(final I_C_Invoice creditMemo)
	{
		// services
		final IInvoiceBL invoiceBL = Services.get(IInvoiceBL.class);
		// final IInvoiceReferenceDAO invoiceReferenceDAO = Services.get(IInvoiceReferenceDAO.class);
		final IAllocationDAO allocationDAO = Services.get(IAllocationDAO.class);

		final boolean isCreditMemo = invoiceBL.isCreditMemo(creditMemo);

		if (!isCreditMemo)
		{
			// nothing to do
			return;
		}

		// The amount from the credit memo to be allocated to parent invoices
		final BigDecimal creditMemoLeft = creditMemo.getGrandTotal();

		// the parent invoice might be null if the credit memo was created manually
		if (creditMemo.getRef_Invoice_ID() > 0)
		{
			final I_C_Invoice parentInvoice = InterfaceWrapperHelper.create(creditMemo.getRef_Invoice(), I_C_Invoice.class);
			final BigDecimal invoiceOpenAmt = allocationDAO.retrieveOpenAmt(parentInvoice,
					false); // creditMemoAdjusted = false

			final BigDecimal amtToAllocate = invoiceOpenAmt.min(creditMemoLeft);

			// Allocate the minimum between parent invoice open amt and what is left of the creditMemo's grand Total
			invoiceBL.allocateCreditMemo(parentInvoice, creditMemo, amtToAllocate);
		}
	}

	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_DELETE })
	public void onDeleteInvoice_DeleteLines(final I_C_Invoice invoice)
	{
		// services
		final IInvoiceBL invoiceBL = Services.get(IInvoiceBL.class);

		// ONLY delete lines for status Draft or In Progress
		final DocStatus docStatus = DocStatus.ofCode(invoice.getDocStatus());
		if (!docStatus.isDraftedOrInProgress())
		{
			return;
		}

		// task 09026. Do not touch other invoices because it was not yet required.
		// ONLY delete lines for credit memo or adjustment charge
		final boolean isAdjustmentCharge = invoiceBL.isAdjustmentCharge(invoice);
		if (isAdjustmentCharge)
		{
			deleteInvoiceLines(invoice);
			return;
		}

		final boolean isCreditMemo = invoiceBL.isCreditMemo(invoice);
		if (isCreditMemo)
		{
			deleteInvoiceLines(invoice);
			return;
		}

	}

	/**
	 * task 09026
	 * We need to delete the Invoice Lines before deleting the Invoice itself.
	 * This is not a common thing to be done, therefore I will leave this method only here, as private (not in the DAO class).
	 * Currently, it shall only happen in case of uncompleted invoices that are adjustment charges or credit memos.
	 */
	private void deleteInvoiceLines(final I_C_Invoice invoice)
	{
		final List<I_C_InvoiceLine> lines = Services.get(IInvoiceDAO.class).retrieveLines(invoice);
		for (final I_C_InvoiceLine line : lines)
		{
			InterfaceWrapperHelper.delete(line);
		}
	}

	private void linkInvoiceToPaymentIfNeeded(final I_C_Invoice invoice)
	{
		final I_C_Order order = invoice.getC_Order();
		if (order != null
				&& Services.get(IDocTypeBL.class).isPrepay(DocTypeId.ofRepoId(order.getC_DocType_ID()))
				&& order.getC_Payment_ID() > 0)
		{
			final I_C_Payment payment = order.getC_Payment();
			payment.setC_Invoice_ID(invoice.getC_Invoice_ID());
			InterfaceWrapperHelper.save(payment);

			Services.get(IAllocationBL.class).autoAllocateSpecificPayment(invoice, payment, true);
		}
	}

	private void allocateInvoiceAgainstPaymentIfNeeded(final I_C_Invoice invoice)
	{
		final I_C_Order order = invoice.getC_Order();
		if (order != null
				&& Services.get(IDocTypeBL.class).isPrepay(DocTypeId.ofRepoId(order.getC_DocType_ID()))
				&& order.getC_Payment_ID() > 0)
		{
			final I_C_Payment payment = order.getC_Payment();
			Services.get(IAllocationBL.class).autoAllocateSpecificPayment(invoice, payment, true);
		}
	}

	private void captureMoneyIfNeeded(final I_C_Invoice salesInvoice)
	{
		//
		// We capture money only for sales invoices
		if (!salesInvoice.isSOTrx())
		{
			return;
		}

		//
		// Avoid reversals
		if (Services.get(IInvoiceBL.class).isReversal(salesInvoice))
		{
			return;
		}

		//
		// We capture money only for regular invoices (not credit memos)
		// TODO: for credit memos we shall refund a part of already reserved money
		if (Services.get(IInvoiceBL.class).isCreditMemo(salesInvoice))
		{
			return;
		}

		//
		//
		// If there is no order, we cannot capture money because we don't know which is the payment reservation
		final OrderId salesOrderId = OrderId.ofRepoIdOrNull(salesInvoice.getC_Order_ID());
		if (salesOrderId == null)
		{
			return;
		}

		//
		// No payment reservation
		if (!paymentReservationService.hasPaymentReservation(salesOrderId))
		{
			return;
		}

		final LocalDate dateTrx = TimeUtil.asLocalDate(salesInvoice.getDateInvoiced());
		final Money grandTotal = extractGrandTotal(salesInvoice);

		paymentReservationService.captureAmount(PaymentReservationCaptureRequest.builder()
				.salesOrderId(salesOrderId)
				.salesInvoiceId(InvoiceId.ofRepoId(salesInvoice.getC_Invoice_ID()))
				.customerId(BPartnerId.ofRepoId(salesInvoice.getC_BPartner_ID()))
				.dateTrx(dateTrx)
				.amount(grandTotal)
				.build());
	}

	private static Money extractGrandTotal(@NonNull final I_C_Invoice salesInvoice)
	{
		return Money.of(salesInvoice.getGrandTotal(), CurrencyId.ofRepoId(salesInvoice.getC_Currency_ID()));
	}
}
