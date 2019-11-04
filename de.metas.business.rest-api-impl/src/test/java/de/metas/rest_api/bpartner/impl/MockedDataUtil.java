package de.metas.rest_api.bpartner.impl;

import static org.assertj.core.api.Assertions.fail;

import java.util.UUID;

import de.metas.rest_api.JsonExternalId;
import de.metas.rest_api.MetasfreshId;
import de.metas.rest_api.bpartner.request.JsonRequestBPartner;
import de.metas.rest_api.bpartner.request.JsonRequestBPartner.JsonRequestBPartnerBuilder;
import de.metas.rest_api.bpartner.request.JsonRequestComposite;
import de.metas.rest_api.bpartner.request.JsonRequestComposite.JsonRequestCompositeBuilder;
import de.metas.rest_api.bpartner.request.JsonRequestContact;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsert.JsonRequestContactUpsertBuilder;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsertItem;
import de.metas.rest_api.bpartner.request.JsonRequestLocation;
import de.metas.rest_api.bpartner.request.JsonRequestLocationUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestLocationUpsert.JsonRequestLocationUpsertBuilder;
import de.metas.rest_api.bpartner.request.JsonRequestLocationUpsertItem;
import de.metas.rest_api.utils.IdentifierString;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

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

@UtilityClass
public class MockedDataUtil
{
	public static final String MOCKED_NEXT = UUID.randomUUID().toString();

	private static int metasfreshIdCounter = 1;

	public MetasfreshId nextMetasFreshId()
	{
		return MetasfreshId.of(metasfreshIdCounter++);
	}

	public JsonRequestComposite createMockBPartner(@NonNull final String bpartnerIdentifierStr)
	{
		final JsonRequestCompositeBuilder result = JsonRequestComposite.builder();

		final JsonRequestBPartnerBuilder bPartner = JsonRequestBPartner
				.builder()
				.companyName("bPartner.companyName")
				.name("bPartner.name")
				.group("bPartner.group")
				.language("bPartner.de_CH")
				.parentId(MetasfreshId.of(1))
				.phone("bPartner.phone")
				.url("bPartner.url");

		final IdentifierString bpartnerIdentifier = IdentifierString.of(bpartnerIdentifierStr);

		switch (bpartnerIdentifier.getType())
		{
			case EXTERNAL_ID:
				bPartner.code("code");
				bPartner.externalId(bpartnerIdentifier.asJsonExternalId());
				break;
			case VALUE:
				bPartner.code(bpartnerIdentifier.asValue());
				bPartner.externalId(JsonExternalId.of("externalId"));
				break;
			default:
				fail("bpartnerIdentifier is not supported by this mockup method; bpartnerIdentifier={}", bpartnerIdentifier);
				break;
		}

		result.bpartner(bPartner.build());

		final JsonRequestLocationUpsertBuilder locationUpsert = JsonRequestLocationUpsert.builder()
				.requestItem(createMockLocation("l1", "CH"))
				.requestItem(createMockLocation("l2", "DE"));

		result.locations(locationUpsert.build());

		final JsonRequestContactUpsertBuilder contactUpsert = JsonRequestContactUpsert.builder()
				.requestItem(createMockContact("c1"))
				.requestItem(createMockContact("c2"));

		result.contacts(contactUpsert.build());

		return result.build();
	}

	public JsonRequestLocationUpsertItem createMockLocation(
			@NonNull final String prefix,
			@NonNull final String countryCode)
	{
		final String externalId = prefix + "_externalId";
		return JsonRequestLocationUpsertItem.builder()
				.locationIdentifier("ext-" + externalId)
				.location(JsonRequestLocation.builder()
						.address1(prefix + "_address1")
						.address2(prefix + "_address2")
						.poBox(prefix + "_poBox")
						.district(prefix + "_district")
						.region(prefix + "_region")
						.city(prefix + "_city")
						.countryCode(countryCode)
						.externalId(JsonExternalId.of(externalId))
						.gln(prefix + "_gln")
						.postal(prefix + "_postal")
						.build())
				.build();
	}

	public JsonRequestContactUpsertItem createMockContact(@NonNull final String prefix)
	{
		final String externalId = prefix + "_externalId";
		return JsonRequestContactUpsertItem.builder()
				.contactIdentifier("ext-" + externalId)
				.contact(JsonRequestContact.builder()
						.email(prefix + "_email@email.net")
						.externalId(JsonExternalId.of(externalId))
						.name(prefix + "_name")
						.phone(prefix + "_phone")
						.build())
				.build();
	}
}
