package de.metas.invoicecandidate.api.impl;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.refreshAll;
import static org.adempiere.model.InterfaceWrapperHelper.save;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import javax.annotation.Nullable;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.test.AdempiereTestHelper;
import org.compiere.model.IQuery;
import org.compiere.model.I_C_PaymentTerm;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.metas.invoicecandidate.InvoiceCandidateId;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.payment.paymentterm.PaymentTermId;
import de.metas.process.PInstanceId;
import de.metas.util.Services;
import de.metas.util.lang.CoalesceUtil;
import de.metas.util.rest.ExternalHeaderAndLineId;
import de.metas.util.rest.ExternalId;
import lombok.NonNull;

/*
 * #%L
 * de.metas.swat.base
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

public class InvoiceCandDAOTest
{
	private static final ExternalId EXTERNAL_HEADER_ID1 = ExternalId.of("HEADER_1");
	private static final ExternalId EXTERNAL_LINE_ID1 = ExternalId.of("LINE_1");

	private static final ExternalId EXTERNAL_HEADER_ID2 = ExternalId.of("HEADER_2");
	private static final ExternalId EXTERNAL_LINE_ID2 = ExternalId.of("LINE_2");

	private static final ExternalId EXTERNAL_HEADER_ID3 = ExternalId.of("HEADER_3");
	private static final ExternalId EXTERNAL_LINE_ID3 = ExternalId.of("LINE_3");

	private static final int P_INSTANCE_ID = 1002265;

	private InvoiceCandDAO invoiceCandDAO;

	@Before
	public void init()
	{
		AdempiereTestHelper.get().init();

		invoiceCandDAO = new InvoiceCandDAO();
	}

	@Test
	public void updateMissingPaymentTermIds()
	{
		// create two ICs without a payment term; those shall be updated by the method under test
		final I_C_Invoice_Candidate invoiceCandidateWithoutPaymentTerm1 = newInstance(I_C_Invoice_Candidate.class);
		save(invoiceCandidateWithoutPaymentTerm1);

		final I_C_Invoice_Candidate invoiceCandidateWithoutPaymentTerm2 = newInstance(I_C_Invoice_Candidate.class);
		save(invoiceCandidateWithoutPaymentTerm2);

		// Create two ICs with different payment terms.
		// By virtue of it's lower C_Invoice_Candidate_ID, the first IC's payment term ID shall be set in the first two ICs we creates
		final PaymentTermId paymentTermId1 = createPaymentTerm();
		final I_C_Invoice_Candidate invoiceCandidateWithPaymentTerm1 = newInstance(I_C_Invoice_Candidate.class);
		invoiceCandidateWithPaymentTerm1.setC_PaymentTerm_ID(paymentTermId1.getRepoId());
		save(invoiceCandidateWithPaymentTerm1);

		final PaymentTermId paymentTermId2 = createPaymentTerm();
		final I_C_Invoice_Candidate invoiceCandidateWithPaymentTerm2 = newInstance(I_C_Invoice_Candidate.class);
		invoiceCandidateWithPaymentTerm2.setC_PaymentTerm_ID(paymentTermId2.getRepoId());
		save(invoiceCandidateWithPaymentTerm2);

		final PInstanceId selectionId = Services.get(IQueryBL.class).createQueryBuilder(I_C_Invoice_Candidate.class)
				.create()
				.createSelection();

		// create an unrelated IC that also has no payment term; it shall be left untouched
		final I_C_Invoice_Candidate unrelatedInvoiceCandidateWithoutPaymentTerm = newInstance(I_C_Invoice_Candidate.class);
		save(unrelatedInvoiceCandidateWithoutPaymentTerm);

		// invoke the method under test and refresh all ICs
		invoiceCandDAO.updateMissingPaymentTermIds(selectionId);

		refreshAll(ImmutableList.of(
				invoiceCandidateWithoutPaymentTerm1,
				invoiceCandidateWithoutPaymentTerm2,
				invoiceCandidateWithPaymentTerm1,
				invoiceCandidateWithPaymentTerm2,
				unrelatedInvoiceCandidateWithoutPaymentTerm));

		// verify
		assertThat(getPaymentTermId(invoiceCandidateWithoutPaymentTerm1)).isEqualTo(paymentTermId1);
		assertThat(getPaymentTermId(invoiceCandidateWithoutPaymentTerm2)).isEqualTo(paymentTermId1);
		assertThat(getPaymentTermId(invoiceCandidateWithPaymentTerm1)).isEqualTo(paymentTermId1);

		assertThat(getPaymentTermId(invoiceCandidateWithPaymentTerm2))
				.as("invoiceCandidateWithPaymentTerm2 shall be left unchanged because it already has a C_PaymentTerm_ID")
				.isEqualTo(paymentTermId2);

		assertThat(getPaymentTermId(unrelatedInvoiceCandidateWithoutPaymentTerm))
				.as("unrelatedInvoiceCandidateWithoutPaymentTerm shall be left unchanged because it's not part of the selection")
				.isNull();
	}

	private PaymentTermId createPaymentTerm()
	{
		final I_C_PaymentTerm record = newInstance(I_C_PaymentTerm.class);
		saveRecord(record);
		return PaymentTermId.ofRepoId(record.getC_PaymentTerm_ID());
	}

	private PaymentTermId getPaymentTermId(@NonNull final I_C_Invoice_Candidate ic)
	{
		return CoalesceUtil.coalesceSuppliers(
				() -> PaymentTermId.ofRepoIdOrNull(ic.getC_PaymentTerm_Override_ID()),
				() -> PaymentTermId.ofRepoIdOrNull(ic.getC_PaymentTerm_ID()));
	}

	@Test
	public void createQueryByHeaderAndLineId_testInvoiceCandidates()
	{
		final InvoiceCandidateId candidateId1_1 = createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID1);
		final InvoiceCandidateId candidateId2_2 = createInvoiceCandidate(EXTERNAL_HEADER_ID2, EXTERNAL_LINE_ID2);
		createInvoiceCandidate(EXTERNAL_HEADER_ID3, EXTERNAL_LINE_ID3);

		final List<ExternalHeaderAndLineId> headerAndLineIds = ImmutableList.of(
				ExternalHeaderAndLineId.builder().externalHeaderId(EXTERNAL_HEADER_ID1).externalLineId(EXTERNAL_LINE_ID1).build(),
				ExternalHeaderAndLineId.builder().externalHeaderId(EXTERNAL_HEADER_ID2).externalLineId(EXTERNAL_LINE_ID2).build());

		// invoke the method under test
		final IQuery<I_C_Invoice_Candidate> createQueryByHeaderAndLineId = invoiceCandDAO.createQueryByHeaderAndLineId(headerAndLineIds);

		final int size = createQueryByHeaderAndLineId.list().size();
		assertThat(size).isEqualTo(2);

		final List<I_C_Invoice_Candidate> records = createQueryByHeaderAndLineId.list();
		assertThat(records)
				.extracting("C_Invoice_Candidate_ID", "ExternalHeaderId", "ExternalLineId")
				.containsExactlyInAnyOrder(
						tuple(candidateId1_1.getRepoId(), EXTERNAL_HEADER_ID1.getValue(), EXTERNAL_LINE_ID1.getValue()),
						tuple(candidateId2_2.getRepoId(), EXTERNAL_HEADER_ID2.getValue(), EXTERNAL_LINE_ID2.getValue()));
	}

	@Test
	public void createQueryByHeaderAndLineId_checkInvoiceCandidatesNotSelected()
	{
		createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID1);

		final ImmutableList<ExternalHeaderAndLineId> headerAndLineIds = ImmutableList.of(
				ExternalHeaderAndLineId.builder()
						.externalHeaderId(EXTERNAL_HEADER_ID2)
						.externalLineId(EXTERNAL_LINE_ID2)
						.build());

		final IQuery<I_C_Invoice_Candidate> createQueryByHeaderAndLineId = invoiceCandDAO.createQueryByHeaderAndLineId(headerAndLineIds);

		final int selection = createQueryByHeaderAndLineId.createSelection(PInstanceId.ofRepoId(P_INSTANCE_ID));
		assertThat(selection).isEqualTo(0);
	}

	@Test
	public void createQueryByHeaderAndLineId_checkEmptyListOfExternalLineIds()
	{
		final List<ExternalHeaderAndLineId> headerAndLineIds = ImmutableList.of(
				ExternalHeaderAndLineId.builder().externalHeaderId(EXTERNAL_HEADER_ID3).externalLineId(EXTERNAL_LINE_ID3).build());

		// invoke the method under test
		final IQuery<I_C_Invoice_Candidate> createQueryByHeaderAndLineId = invoiceCandDAO.createQueryByHeaderAndLineId(headerAndLineIds);

		final int selection = createQueryByHeaderAndLineId.createSelection(PInstanceId.ofRepoId(P_INSTANCE_ID));
		assertThat(selection).isEqualTo(0);
	}

	@Test
	public void createQueryByHeaderAndLineId_select_lineId_subset()
	{
		final InvoiceCandidateId candidateId1_1 = createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID1);
		final InvoiceCandidateId candidateId1_2 = createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID2);
		createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID3);
		createInvoiceCandidate(EXTERNAL_HEADER_ID2, null);

		final List<ExternalHeaderAndLineId> headerAndLineIds = ImmutableList.of(
				ExternalHeaderAndLineId.builder()
						.externalHeaderId(EXTERNAL_HEADER_ID1)
						.externalLineId(EXTERNAL_LINE_ID1)
						.externalLineId(EXTERNAL_LINE_ID2)
						.build());

		// invoke the method under test
		final IQuery<I_C_Invoice_Candidate> createQueryByHeaderAndLineId = invoiceCandDAO.createQueryByHeaderAndLineId(headerAndLineIds);

		final int selection = createQueryByHeaderAndLineId.createSelection(PInstanceId.ofRepoId(P_INSTANCE_ID));
		assertThat(selection).isEqualTo(2);

		final List<I_C_Invoice_Candidate> records = createQueryByHeaderAndLineId.list();
		assertThat(records)
				.extracting("C_Invoice_Candidate_ID", "ExternalHeaderId", "ExternalLineId")
				.containsExactlyInAnyOrder(
						tuple(candidateId1_1.getRepoId(), EXTERNAL_HEADER_ID1.getValue(), EXTERNAL_LINE_ID1.getValue()),
						tuple(candidateId1_2.getRepoId(), EXTERNAL_HEADER_ID1.getValue(), EXTERNAL_LINE_ID2.getValue()));
	}

	@Test
	public void createQueryByHeaderAndLineId_select_only_by_headerId()
	{
		final InvoiceCandidateId candidateId1_1 = createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID1);
		final InvoiceCandidateId candidateId1_2 = createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID2);
		final InvoiceCandidateId candidateId1_3 = createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID3);
		createInvoiceCandidate(EXTERNAL_HEADER_ID2, null);

		final List<ExternalHeaderAndLineId> headerAndLineIds = ImmutableList.of(
				ExternalHeaderAndLineId.builder().externalHeaderId(EXTERNAL_HEADER_ID1).build());

		// invoke the method under test
		final IQuery<I_C_Invoice_Candidate> createQueryByHeaderAndLineId = invoiceCandDAO.createQueryByHeaderAndLineId(headerAndLineIds);

		final int selection = createQueryByHeaderAndLineId.createSelection(PInstanceId.ofRepoId(P_INSTANCE_ID));
		assertThat(selection).isEqualTo(3);

		final List<I_C_Invoice_Candidate> records = createQueryByHeaderAndLineId.list();
		assertThat(records)
				.extracting("C_Invoice_Candidate_ID", "ExternalHeaderId", "ExternalLineId")
				.containsExactlyInAnyOrder(
						tuple(candidateId1_1.getRepoId(), EXTERNAL_HEADER_ID1.getValue(), EXTERNAL_LINE_ID1.getValue()),
						tuple(candidateId1_2.getRepoId(), EXTERNAL_HEADER_ID1.getValue(), EXTERNAL_LINE_ID2.getValue()),
						tuple(candidateId1_3.getRepoId(), EXTERNAL_HEADER_ID1.getValue(), EXTERNAL_LINE_ID3.getValue()));
	}

	@Test
	public void createQueryByHeaderAndLineId_select_mixed()
	{
		final InvoiceCandidateId candidateId1_1 = createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID1);
		final InvoiceCandidateId candidateId1_2 = createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID2);
		final InvoiceCandidateId candidateId1_3 = createInvoiceCandidate(EXTERNAL_HEADER_ID1, EXTERNAL_LINE_ID3);
		createInvoiceCandidate(EXTERNAL_HEADER_ID2, EXTERNAL_LINE_ID1); // this one shall not be part of the result further down
		final InvoiceCandidateId candidateId2_2 = createInvoiceCandidate(EXTERNAL_HEADER_ID2, EXTERNAL_LINE_ID2);
		final InvoiceCandidateId candidateId2_3 = createInvoiceCandidate(EXTERNAL_HEADER_ID2, EXTERNAL_LINE_ID3);

		final List<ExternalHeaderAndLineId> headerAndLineIds = ImmutableList.of(
				ExternalHeaderAndLineId.builder().externalHeaderId(EXTERNAL_HEADER_ID1).build(), // all with headerId1
				ExternalHeaderAndLineId.builder().externalHeaderId(EXTERNAL_HEADER_ID2).externalLineId(EXTERNAL_LINE_ID2).externalLineId(EXTERNAL_LINE_ID3).build());

		// invoke the method under test
		final IQuery<I_C_Invoice_Candidate> createQueryByHeaderAndLineId = invoiceCandDAO.createQueryByHeaderAndLineId(headerAndLineIds);

		final int selection = createQueryByHeaderAndLineId.createSelection(PInstanceId.ofRepoId(P_INSTANCE_ID));
		assertThat(selection).isEqualTo(5);

		final List<I_C_Invoice_Candidate> records = createQueryByHeaderAndLineId.list();
		assertThat(records)
				.extracting("C_Invoice_Candidate_ID", "ExternalHeaderId", "ExternalLineId")
				.containsExactlyInAnyOrder(
						tuple(candidateId1_1.getRepoId(), EXTERNAL_HEADER_ID1.getValue(), EXTERNAL_LINE_ID1.getValue()),
						tuple(candidateId1_2.getRepoId(), EXTERNAL_HEADER_ID1.getValue(), EXTERNAL_LINE_ID2.getValue()),
						tuple(candidateId1_3.getRepoId(), EXTERNAL_HEADER_ID1.getValue(), EXTERNAL_LINE_ID3.getValue()),
						tuple(candidateId2_2.getRepoId(), EXTERNAL_HEADER_ID2.getValue(), EXTERNAL_LINE_ID2.getValue()),
						tuple(candidateId2_3.getRepoId(), EXTERNAL_HEADER_ID2.getValue(), EXTERNAL_LINE_ID3.getValue()));
	}

	private InvoiceCandidateId createInvoiceCandidate(
			@Nullable final ExternalId externalHeaderId,
			@Nullable final ExternalId externalLineId)
	{
		final I_C_Invoice_Candidate invoiceCandidate = newInstance(I_C_Invoice_Candidate.class);
		invoiceCandidate.setExternalHeaderId(ExternalId.toValue(externalHeaderId));
		invoiceCandidate.setExternalLineId(ExternalId.toValue(externalLineId));
		saveRecord(invoiceCandidate);

		return InvoiceCandidateId.ofRepoId(invoiceCandidate.getC_Invoice_Candidate_ID());
	}
}
