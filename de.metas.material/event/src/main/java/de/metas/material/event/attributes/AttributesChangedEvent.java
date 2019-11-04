package de.metas.material.event.attributes;

import java.math.BigDecimal;
import java.time.Instant;

import org.adempiere.warehouse.WarehouseId;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import de.metas.material.event.MaterialEvent;
import de.metas.material.event.commons.EventDescriptor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * metasfresh-material-event
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

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@Value
@Builder
public class AttributesChangedEvent implements MaterialEvent
{
	public static final String TYPE = "AttributesChangedEvent";

	private final EventDescriptor eventDescriptor;

	@NonNull
	WarehouseId warehouseId;

	@NonNull
	Instant date;

	int productId;

	@NonNull
	BigDecimal qty;

	@NonNull
	AttributesKeyWithASI oldStorageAttributes;
	@NonNull
	AttributesKeyWithASI newStorageAttributes;

	int huId;
}
