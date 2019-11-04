package de.metas.payment.api.impl;

import static org.adempiere.model.InterfaceWrapperHelper.load;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.adempiere.ad.dao.ICompositeQueryFilter;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.dao.IQueryFilter;
import org.adempiere.ad.dao.IQueryOrderBy;
import org.adempiere.ad.dao.impl.CompareQueryFilter.Operator;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.DBException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.IQuery;
import org.compiere.model.I_C_AllocationLine;
import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.I_C_PaySelection;
import org.compiere.model.I_C_Payment;
import org.compiere.model.I_Fact_Acct;
import org.compiere.model.X_C_DocType;
import org.compiere.util.DB;

import de.metas.adempiere.model.I_C_PaySelectionLine;
import de.metas.allocation.api.IAllocationDAO;
import de.metas.bpartner.BPartnerId;
import de.metas.document.engine.DocStatus;
import de.metas.payment.PaymentId;
import de.metas.payment.api.IPaymentDAO;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

public abstract class AbstractPaymentDAO implements IPaymentDAO
{
	@Override
	public I_C_Payment getById(@NonNull final PaymentId paymentId)
	{
		return load(paymentId, I_C_Payment.class);
	}

	@Override
	public BigDecimal getInvoiceOpenAmount(I_C_Payment payment, final boolean creditMemoAdjusted)
	{
		final I_C_Invoice invoice = payment.getC_Invoice();
		Check.assumeNotNull(invoice, "Invoice available for {}", payment);

		// NOTE: we are not using C_InvoicePaySchedule_ID. It shall be a column in C_Payment

		return Services.get(IAllocationDAO.class).retrieveOpenAmt(invoice, creditMemoAdjusted);
	}

	@Override
	public List<I_C_PaySelectionLine> getProcessedLines(final I_C_PaySelection paySelection)
	{
		Check.assumeNotNull(paySelection, "Pay selection not null");

		return Services.get(IQueryBL.class).createQueryBuilder(I_C_PaySelectionLine.class, paySelection)
				.addEqualsFilter(I_C_PaySelectionLine.COLUMNNAME_C_PaySelection_ID, paySelection.getC_PaySelection_ID())
				.addOnlyActiveRecordsFilter()
				.create()
				.list(I_C_PaySelectionLine.class);
	}

	@Override
	public List<I_C_Payment> retrievePostedWithoutFactAcct(final Properties ctx, final Timestamp startTime)
	{
		final IQueryBL queryBL = Services.get(IQueryBL.class);

		final String trxName = ITrx.TRXNAME_ThreadInherited;

		final IQueryBuilder<I_C_Payment> queryBuilder = queryBL.createQueryBuilder(I_C_Payment.class, ctx, trxName)
				.addOnlyActiveRecordsFilter();

		queryBuilder
				.addEqualsFilter(I_C_Payment.COLUMNNAME_Posted, true) // Posted
				.addEqualsFilter(I_C_Payment.COLUMNNAME_Processed, true) // Processed
				.addInArrayOrAllFilter(I_C_Payment.COLUMN_DocStatus, DocStatus.completedOrClosedStatuses());

		// Only the documents created after the given start time
		if (startTime != null)
		{
			queryBuilder.addCompareFilter(I_C_Payment.COLUMNNAME_Created, Operator.GREATER_OR_EQUAL, startTime);
		}

		// Check if there are fact accounts created for each document
		final IQueryBuilder<I_Fact_Acct> subQueryBuilder = queryBL.createQueryBuilder(I_Fact_Acct.class, ctx, trxName)
				.addEqualsFilter(I_Fact_Acct.COLUMN_AD_Table_ID, InterfaceWrapperHelper.getTableId(I_C_Payment.class));

		queryBuilder
				.addNotInSubQueryFilter(I_C_Payment.COLUMNNAME_C_Payment_ID, I_Fact_Acct.COLUMNNAME_Record_ID, subQueryBuilder.create()) // has no accounting
		;

		// Exclude the entries that don't have either PayAmt or OverUnderAmt. These entries will produce 0 in posting
		final ICompositeQueryFilter<I_C_Payment> nonZeroFilter = queryBL.createCompositeQueryFilter(I_C_Payment.class).setJoinOr()
				.addNotEqualsFilter(I_C_Payment.COLUMNNAME_PayAmt, BigDecimal.ZERO)
				.addNotEqualsFilter(I_C_Payment.COLUMNNAME_OverUnderAmt, BigDecimal.ZERO);

		queryBuilder.filter(nonZeroFilter);

		return queryBuilder
				.create()
				.list();

	}

	@Override
	public List<I_C_Payment> retrievePayments(de.metas.adempiere.model.I_C_Invoice invoice)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(invoice);
		final String trxName = InterfaceWrapperHelper.getTrxName(invoice);

		final IQueryBL queryBL = Services.get(IQueryBL.class);

		final IQueryBuilder<I_C_Payment> queryBuilder = queryBL.createQueryBuilder(I_C_Payment.class, ctx, trxName)
				.addOnlyActiveRecordsFilter();

		final boolean isReceipt = invoice.isSOTrx();

		queryBuilder
				.addEqualsFilter(I_C_Payment.COLUMNNAME_C_BPartner_ID, invoice.getC_BPartner_ID()) // C_BPartner_ID
				.addEqualsFilter(I_C_Payment.COLUMNNAME_Processed, true) // Processed
				.addInArrayOrAllFilter(I_C_Payment.COLUMN_DocStatus, DocStatus.completedOrClosedStatuses())
				.addEqualsFilter(I_C_Payment.COLUMNNAME_IsReceipt, isReceipt); // Matching DocType

		final IQuery<I_C_AllocationLine> allocationsQuery = queryBL.createQueryBuilder(I_C_AllocationLine.class, ctx, ITrx.TRXNAME_None)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_AllocationLine.COLUMNNAME_C_Invoice_ID, invoice.getC_Invoice_ID())
				.create();

		final IQueryFilter<I_C_Payment> allocationFilter = queryBL.createCompositeQueryFilter(I_C_Payment.class)
				.addInSubQueryFilter(I_C_Payment.COLUMNNAME_C_Payment_ID, I_C_AllocationLine.COLUMNNAME_C_Payment_ID, allocationsQuery);

		final ICompositeQueryFilter<I_C_Payment> linkedPayments = queryBL.createCompositeQueryFilter(I_C_Payment.class).setJoinOr()
				.addEqualsFilter(I_C_Payment.COLUMNNAME_C_Invoice_ID, invoice.getC_Invoice_ID())
				.addFilter(allocationFilter);

		queryBuilder.filter(linkedPayments);

		// ordering by DocumentNo
		final IQueryOrderBy orderBy = queryBuilder.orderBy().addColumn(I_C_Payment.COLUMNNAME_DocumentNo).createQueryOrderBy();

		return queryBuilder
				.create()
				.setOrderBy(orderBy)
				.list();
	}

	@Override
	public Stream<PaymentId> streamPaymentIdsByBPartnerId(@NonNull final BPartnerId bpartnerId)
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilder(I_C_Payment.class)
				.addEqualsFilter(I_C_Payment.COLUMN_C_BPartner_ID, bpartnerId)
				.create()
				.listIds(PaymentId::ofRepoId)
				.stream();
	}

	/*
	 * TODO please consider the following improvement
	 * - create an AD_Table like `C_InvoiceOpenAmounts` that has the required values (`C_BPartner_ID`, `C_Currency_ID`, `InvoiceOpen`...) as columns
	 * - put this select-stuff into a DB function. that function shall return the `C_InvoiceOpenAmounts` as result (i.e. the metasfresh `C_InvoiceOpenAmounts` table is not a physical table in the DB; it's just what the DB-function returns)
	 * - create a model class `I_C_InvoiceOpenAmounts` for your AD_Table.
	 * - have the `PaymentDAO` implementation invoke the DB-function to get it's `I_C_InvoiceOpenAmounts` (=> you can do this with IQueryBL)
	 * - have the `PlainPaymentDAO ` implementation return "plain" instances of `I_C_InvoiceOpenAmounts`
	 * - that way, one can write a unit test where they first create one or two plain `I_C_InvoiceOpenAmounts`s and then query them in their test
	 */
	public abstract void updateDiscountAndPayment(I_C_Payment payment, int c_Invoice_ID, I_C_DocType c_DocType);
}
