package de.metas.order;

import java.util.Arrays;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.X_C_Order;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import de.metas.util.lang.ReferenceListAwareEnum;
import lombok.Getter;
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
 * but WITHOUT ANY WARRANTY), without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public enum DeliveryRule implements ReferenceListAwareEnum
{
	AFTER_RECEIPT(X_C_Order.DELIVERYRULE_AfterReceipt), //
	AVAILABILITY(X_C_Order.DELIVERYRULE_Availability), //
	COMPLETE_LINE(X_C_Order.DELIVERYRULE_CompleteLine), //
	COMPLETE_ORDER(X_C_Order.DELIVERYRULE_CompleteOrder), //
	FORCE(X_C_Order.DELIVERYRULE_Force), //
	MANUAL(X_C_Order.DELIVERYRULE_Manual), //
	WITH_NEXT_SUBSCRIPTION_DELIVERY("S") // see de.metas.inoutcandidate.model.X_M_ShipmentSchedule.DELIVERYRULE_MitNaechsterAbolieferung
	;

	@Getter
	private final String code;

	DeliveryRule(final String code)
	{
		this.code = code;
	}

	public static DeliveryRule ofNullableCode(final String code)
	{
		return code != null ? ofCode(code) : null;
	}

	public static DeliveryRule ofCode(@NonNull final String code)
	{
		DeliveryRule type = typesByCode.get(code);
		if (type == null)
		{
			throw new AdempiereException("No " + DeliveryRule.class + " found for code: " + code);
		}
		return type;
	}

	private static final ImmutableMap<String, DeliveryRule> typesByCode = Maps.uniqueIndex(Arrays.asList(values()), DeliveryRule::getCode);

	public static String toCodeOrNull(final DeliveryRule type)
	{
		return type != null ? type.getCode() : null;
	}
}
