package de.metas.rest_api.bpartner.impl.bpartnercomposite;

import static de.metas.util.Check.assumeNotEmpty;

import org.adempiere.exceptions.AdempiereException;

import de.metas.bpartner.GLN;
import de.metas.rest_api.JsonExternalId;
import de.metas.rest_api.MetasfreshId;
import de.metas.rest_api.utils.IdentifierString;
import de.metas.rest_api.utils.JsonExternalIds;
import de.metas.util.lang.RepoIdAware;
import de.metas.util.rest.ExternalId;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.business.rest-api-impl
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
public class BPartnerCompositeLookupKey
{
	public static BPartnerCompositeLookupKey ofMetasfreshId(@NonNull final MetasfreshId metasfreshId)
	{
		return new BPartnerCompositeLookupKey(metasfreshId, null, null, null);
	}

	public static <T extends RepoIdAware> BPartnerCompositeLookupKey ofMetasfreshId(@NonNull final T id)
	{
		return ofMetasfreshId(MetasfreshId.of(id));
	}

	public static BPartnerCompositeLookupKey ofJsonExternalId(@NonNull final JsonExternalId jsonExternalId)
	{
		return new BPartnerCompositeLookupKey(null, jsonExternalId, null, null);
	}

	public static BPartnerCompositeLookupKey ofExternalId(@NonNull final ExternalId externalId)
	{
		return ofJsonExternalId(JsonExternalIds.of(externalId));
	}

	public static BPartnerCompositeLookupKey ofCode(@NonNull final String code)
	{
		assumeNotEmpty(code, "Given parameter 'code' may not be empty");
		return new BPartnerCompositeLookupKey(null, null, code.trim(), null);
	}

	public static BPartnerCompositeLookupKey ofGln(@NonNull final GLN gln)
	{
		return new BPartnerCompositeLookupKey(null, null, null, gln);
	}

	public static BPartnerCompositeLookupKey ofIdentifierString(@NonNull final IdentifierString bpartnerIdentifier)
	{
		switch (bpartnerIdentifier.getType())
		{
			case EXTERNAL_ID:
				return BPartnerCompositeLookupKey.ofJsonExternalId(bpartnerIdentifier.asJsonExternalId());
			case VALUE:
				return BPartnerCompositeLookupKey.ofCode(bpartnerIdentifier.asValue());
			case GLN:
				return BPartnerCompositeLookupKey.ofGln(bpartnerIdentifier.asGLN());
			case METASFRESH_ID:
				return BPartnerCompositeLookupKey.ofMetasfreshId(bpartnerIdentifier.asMetasfreshId());
			default:
				throw new AdempiereException("Unexpected type=" + bpartnerIdentifier.getType());
		}
	}

	MetasfreshId metasfreshId;
	JsonExternalId jsonExternalId;
	String code;
	GLN gln;

	private BPartnerCompositeLookupKey(
			MetasfreshId metasfreshId,
			JsonExternalId jsonExternalId,
			String code,
			GLN gln)
	{
		this.metasfreshId = metasfreshId;
		this.jsonExternalId = jsonExternalId;
		this.code = code;
		this.gln = gln;
	}

}
