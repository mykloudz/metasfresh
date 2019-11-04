package de.metas.ordercandidate.api;

import java.util.stream.Stream;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.model.RelationTypeZoomProvidersFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;

import com.google.common.base.MoreObjects;

import de.metas.freighcost.FreightCostRule;
import de.metas.order.BPartnerOrderParams;
import de.metas.order.DeliveryRule;
import de.metas.order.DeliveryViaRule;
import de.metas.order.InvoiceRule;
import de.metas.ordercandidate.model.I_C_OLCand;
import de.metas.ordercandidate.model.I_C_OLCandProcessor;
import de.metas.payment.PaymentRule;
import de.metas.payment.paymentterm.PaymentTermId;
import de.metas.pricing.PricingSystemId;
import de.metas.shipping.ShipperId;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.Builder;
import lombok.NonNull;

/*
 * #%L
 * de.metas.swat.base
 * %%
 * Copyright (C) 2017 metas GmbH
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

final class RelationTypeOLCandSource implements OLCandSource
{
	private final IOLCandBL olCandBL = Services.get(IOLCandBL.class);
	private final IOLCandEffectiveValuesBL olCandEffectiveValuesBL = Services.get(IOLCandEffectiveValuesBL.class);

	private final OLCandOrderDefaults orderDefaults;
	private final int olCandProcessorId;
	private final String relationTypeInternalName;

	@Builder
	private RelationTypeOLCandSource(
			@NonNull final OLCandOrderDefaults orderDefaults,
			final int olCandProcessorId)
	{
		Check.assume(olCandProcessorId > 0, "olCandProcessorId > 0");

		this.olCandProcessorId = olCandProcessorId;
		this.orderDefaults = orderDefaults;
		this.relationTypeInternalName = mkRelationTypeInternalNameForOLCandProcessorId(olCandProcessorId);
	}

	private static String mkRelationTypeInternalNameForOLCandProcessorId(final int olCandProcessorId)
	{
		return I_C_OLCandProcessor.Table_Name + "_" + olCandProcessorId + "<=>" + I_C_OLCand.Table_Name;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("relationTypeInternalName", relationTypeInternalName)
				.add("olCandProcessorId", olCandProcessorId)
				.toString();
	}

	@Override
	public Stream<OLCand> streamOLCands()
	{
		// FIXME: get rid of it
		final PO processorPO = InterfaceWrapperHelper.getPO(InterfaceWrapperHelper.loadOutOfTrx(olCandProcessorId, I_C_OLCandProcessor.class));

		return RelationTypeZoomProvidersFactory.instance
				.getZoomProviderBySourceTableNameAndInternalName(I_C_OLCand.Table_Name, relationTypeInternalName)
				.retrieveDestinations(Env.getCtx(), processorPO, I_C_OLCand.class, ITrx.TRXNAME_ThreadInherited)
				.stream()
				.map(record -> toOLCand(record));
	}

	private OLCand toOLCand(@NonNull final I_C_OLCand olCandRecord)
	{
		final BPartnerOrderParams params = olCandBL.getBPartnerOrderParams(olCandRecord);

		final DeliveryRule deliveryRule = olCandBL.getDeliveryRule(olCandRecord, params, orderDefaults);
		final DeliveryViaRule deliveryViaRule = olCandBL.getDeliveryViaRule(olCandRecord, params, orderDefaults);
		final FreightCostRule freightCostRule = olCandBL.getFreightCostRule(params, orderDefaults);
		final InvoiceRule invoiceRule = olCandBL.getInvoiceRule(params, orderDefaults);
		final PaymentRule paymentRule = olCandBL.getPaymentRule(params, orderDefaults);
		final PaymentTermId paymentTermId = olCandBL.getPaymentTermId(params, orderDefaults);
		final PricingSystemId pricingSystemId = olCandBL.getPricingSystemId(olCandRecord, params, orderDefaults);
		final ShipperId shipperId = olCandBL.getShipperId(params, orderDefaults);

		return OLCand.builder()
				.olCandEffectiveValuesBL(olCandEffectiveValuesBL)
				.olCandRecord(olCandRecord)

				.deliveryRule(deliveryRule)
				.deliveryViaRule(deliveryViaRule)
				.freightCostRule(freightCostRule)
				.invoiceRule(invoiceRule)
				.paymentRule(paymentRule)
				.paymentTermId(paymentTermId)
				.pricingSystemId(pricingSystemId)
				.shipperId(shipperId)
				.build();
	}
}
