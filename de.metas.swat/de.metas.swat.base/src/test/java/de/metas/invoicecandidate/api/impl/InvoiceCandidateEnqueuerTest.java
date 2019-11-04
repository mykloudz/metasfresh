package de.metas.invoicecandidate.api.impl;

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

import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.ad.wrapper.POJOLookupMap;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.lang.IMutable;
import org.adempiere.util.lang.Mutable;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.compiere.util.TrxRunnable;
import org.junit.Assert;
import org.junit.Test;

import de.metas.bpartner.BPartnerLocationId;
import de.metas.invoicecandidate.AbstractICTestSupport;
import de.metas.invoicecandidate.api.IInvoiceCandDAO;
import de.metas.invoicecandidate.api.IInvoiceCandidateEnqueueResult;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.process.PInstanceId;
import de.metas.util.Services;
import de.metas.util.time.FixedTimeSource;
import de.metas.util.time.SystemTime;

public class InvoiceCandidateEnqueuerTest extends AbstractICTestSupport
{
	/**
	 * Test case:
	 * <ul>
	 * <li>system date is 01.01.2015
	 * <li>we have 2 invoice candidates, approximately the same, but one has DateInvoiced set to 01.01.2015 (system date) and the second one has no DateInvoiced set
	 * <li>when enqueuing for invoicing both shall end in the same workpackage because basically they are on the same date.
	 * </ul>
	 *
	 * @task http://dewiki908/mediawiki/index.php/08492_rechnungsdatum_and_buchungsdatum_in_invoice_candidates_%28102690023594%29
	 */
	@Test
	public void test_2InvoiceCandidates_OneWithDateInvoicedSet_OneWithoutDateInvoicedButSameDay()
	{
		//
		// Setup
		// registerModelInterceptors(); // got a lot of errors if i am registering it, so i keep it simple
		final IInvoiceCandDAO invoiceCandDAO = Services.get(IInvoiceCandDAO.class);
		SystemTime.setTimeSource(new FixedTimeSource(2015, 1, 1, 0, 0, 0));

		//
		// Create invoice candidates
		final int priceEntered = 1;
		final int qty = 1;
		final boolean isManual = false;
		final boolean isSOTrx = true;

		final BPartnerLocationId billBPartnerAndLocationId = BPartnerLocationId.ofRepoId(1, 2);

		final I_C_Invoice_Candidate ic1 = createInvoiceCandidate()
				.setBillBPartnerAndLocationId(billBPartnerAndLocationId)
				.setPriceEntered(priceEntered)
				.setQtyOrdered(qty)
				.setSOTrx(isSOTrx)
				.setManual(isManual)
				.build();

		ic1.setQtyToInvoice_Override(BigDecimal.valueOf(qty)); // to make sure it's invoiced
		ic1.setDateInvoiced(TimeUtil.getDay(2015, 1, 1));
		InterfaceWrapperHelper.save(ic1);
		invoiceCandDAO.invalidateCand(ic1);


		final I_C_Invoice_Candidate ic2 = createInvoiceCandidate()
				.setBillBPartnerAndLocationId(billBPartnerAndLocationId)
				.setPriceEntered(priceEntered)
				.setQtyOrdered(qty)
				.setSOTrx(isSOTrx)
				.setManual(isManual)
				.build();
		ic2.setQtyToInvoice_Override(BigDecimal.valueOf(qty)); // to make sure it's invoiced
		// ic2.setDateInvoiced(TimeUtil.getDay(2015, 1, 1)); // DON'T set it
		InterfaceWrapperHelper.save(ic2);
		invoiceCandDAO.invalidateCand(ic2);

		//
		// Enqueue them to be invoiced
		final PInstanceId selectionId = POJOLookupMap.get().createSelectionFromModels(ic1, ic2);
		final ITrxManager trxManager = Services.get(ITrxManager.class);
		final IMutable<IInvoiceCandidateEnqueueResult> enqueueResultRef = new Mutable<>();
		trxManager.runInNewTrx(new TrxRunnable()
		{
			@Override
			public void run(final String localTrxName)
			{
				final IInvoiceCandidateEnqueueResult result = new InvoiceCandidateEnqueuer()
						.setContext(Env.getCtx())
						.setInvoicingParams(createDefaultInvoicingParams())
						.setFailOnChanges(false) // ... because we have some invalid candidates which we know that it will be updated here
						.enqueueSelection(selectionId);
				enqueueResultRef.setValue(result);
			}
		});

		//
		// Validate the invoice enqueuing result
		final IInvoiceCandidateEnqueueResult enqueueResult = enqueueResultRef.getValue();
		Assert.assertEquals("Invalid EnqueuedWorkpackageCount", 1, enqueueResult.getWorkpackageEnqueuedCount());
		Assert.assertEquals("Invalid InvoiceCandidateSelectionCount", 2, enqueueResult.getInvoiceCandidateEnqueuedCount());
	}
}
