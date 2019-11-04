package de.metas.organization;

import org.adempiere.service.ClientId;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
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
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ClientAndOrgId
{
	public static ClientAndOrgId ofClientAndOrg(final int adClientId, final int adOrgId)
	{
		return ofClientAndOrg(
				ClientId.ofRepoId(adClientId),
				OrgId.ofRepoId(adOrgId));
	}

	@JsonCreator
	public static ClientAndOrgId ofClientAndOrg(
			@JsonProperty("clientId") @NonNull final ClientId adClientId,
			@JsonProperty("orgId") @NonNull final OrgId adOrgId)
	{
		return new ClientAndOrgId(adClientId, adOrgId);
	}

	@JsonProperty("clientId")
	ClientId clientId;

	@JsonProperty("orgId")
	OrgId orgId;

	private ClientAndOrgId(
			@NonNull final ClientId clientId,
			@NonNull final OrgId orgId)
	{
		this.clientId = clientId;
		this.orgId = orgId;
	}
}
