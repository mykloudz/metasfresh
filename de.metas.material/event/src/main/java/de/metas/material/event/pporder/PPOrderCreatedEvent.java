package de.metas.material.event.pporder;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.metas.material.event.commons.EventDescriptor;
import de.metas.material.event.commons.SupplyRequiredDescriptor;
import de.metas.util.Check;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/*
 * #%L
 * metasfresh-material-event
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

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class PPOrderCreatedEvent extends AbstractPPOrderEvent
{
	public static PPOrderCreatedEvent cast(@Nullable final AbstractPPOrderEvent ppOrderEvent)
	{
		return (PPOrderCreatedEvent)ppOrderEvent;
	}

	public static final String TYPE = "PPOrderCreatedEvent";

	@JsonCreator
	@Builder
	public PPOrderCreatedEvent(
			@JsonProperty("eventDescriptor") @NonNull final EventDescriptor eventDescriptor,
			@JsonProperty("ppOrder") final @NonNull PPOrder ppOrder,
			@JsonProperty("supplyRequiredDescriptor") @Nullable final SupplyRequiredDescriptor supplyRequiredDescriptor)
	{
		super(eventDescriptor, ppOrder, supplyRequiredDescriptor);
	}

	public void validate()
	{
		final PPOrder ppOrder = getPpOrder();
		final int ppOrderId = ppOrder.getPpOrderId();
		Check.errorIf(ppOrderId <= 0, "The given ppOrderCreatedEvent event has a ppOrder with ppOrderId={}", ppOrderId);

		ppOrder.getLines().forEach(this::validateLine);
	}

	private void validateLine(final PPOrderLine ppOrderLine)
	{
		final int ppOrderLineId = ppOrderLine.getPpOrderLineId();
		Check.errorIf(ppOrderLineId <= 0,
				"The given ppOrderCreatedEvent event has a ppOrderLine with ppOrderLineId={}; ppOrderLine={}",
				ppOrderLineId, ppOrderLine);
	}
}
