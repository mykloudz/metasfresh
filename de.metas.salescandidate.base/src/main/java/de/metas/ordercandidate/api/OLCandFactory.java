package de.metas.ordercandidate.api;

import de.metas.order.DeliveryRule;
import de.metas.order.DeliveryViaRule;
import de.metas.ordercandidate.model.I_C_OLCand;
import de.metas.pricing.PricingSystemId;
import de.metas.util.Services;
import lombok.NonNull;
import lombok.ToString;

/*
 * #%L
 * de.metas.salescandidate.base
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

@ToString
final class OLCandFactory
{
	private final IOLCandEffectiveValuesBL olCandEffectiveValuesBL = Services.get(IOLCandEffectiveValuesBL.class);

	public OLCand toOLCand(@NonNull final I_C_OLCand record)
	{
		return OLCand.builder()
				.olCandEffectiveValuesBL(olCandEffectiveValuesBL)
				.olCandRecord(record)
				.pricingSystemId(PricingSystemId.ofRepoIdOrNull(record.getM_PricingSystem_ID()))
				.deliveryRule(DeliveryRule.ofNullableCode(record.getDeliveryRule()))
				.deliveryViaRule(DeliveryViaRule.ofNullableCode(record.getDeliveryViaRule()))
				.shipperId(null)
				.build();
	}
}
