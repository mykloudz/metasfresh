package de.metas.rest_api;

import static de.metas.util.Check.assumeNotEmpty;
import static de.metas.util.Check.isEmpty;

import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import de.metas.util.rest.ExternalId;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.business.rest-api
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

@Value
public class JsonExternalId
{
	String value;

	@JsonCreator
	public static JsonExternalId of(@NonNull final String value)
	{
		return new JsonExternalId(value);
	}

	public static JsonExternalId ofOrNull(@Nullable final String value)
	{
		if (isEmpty(value, true))
		{
			return null;
		}
		return new JsonExternalId(value);
	}

	public static boolean isEqualTo(
			@Nullable final JsonExternalId jsonExternalId,
			@Nullable final ExternalId externalId)
	{
		if (jsonExternalId == null && externalId == null)
		{
			return true;
		}
		if (jsonExternalId == null ^ externalId == null)
		{
			return false; // one is null, the other one isn't
		}

		return Objects.equals(jsonExternalId.getValue(), externalId.getValue());
	}

	private JsonExternalId(@NonNull final String value)
	{
		this.value = assumeNotEmpty(value, "Param value={} may not be empty", value);
	}

	@JsonValue
	public String getValue()
	{
		return value;
	}

	public static boolean equals(final JsonExternalId id1, final JsonExternalId id2)
	{
		return Objects.equals(id1, id2);
	}

}
