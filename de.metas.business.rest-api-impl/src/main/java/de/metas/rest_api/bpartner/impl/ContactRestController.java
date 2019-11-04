package de.metas.rest_api.bpartner.impl;

import static de.metas.rest_api.bpartner.SwaggerDocConstants.CONTACT_IDENTIFIER_DOC;
import static de.metas.rest_api.bpartner.SwaggerDocConstants.NEXT_DOC;
import static de.metas.rest_api.bpartner.SwaggerDocConstants.SINCE_DOC;

import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.metas.Profiles;
import de.metas.bpartner.composite.BPartnerContact;
import de.metas.rest_api.MetasfreshId;
import de.metas.rest_api.SyncAdvise;
import de.metas.rest_api.SyncAdvise.IfExists;
import de.metas.rest_api.SyncAdvise.IfNotExists;
import de.metas.rest_api.bpartner.ContactRestEndpoint;
import de.metas.rest_api.bpartner.impl.bpartnercomposite.JsonServiceFactory;
import de.metas.rest_api.bpartner.impl.bpartnercomposite.jsonpersister.JsonPersisterService;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsertItem;
import de.metas.rest_api.bpartner.request.JsonResponseUpsert;
import de.metas.rest_api.bpartner.request.JsonResponseUpsert.JsonResponseUpsertBuilder;
import de.metas.rest_api.bpartner.request.JsonResponseUpsertItem;
import de.metas.rest_api.bpartner.response.JsonResponseContact;
import de.metas.rest_api.bpartner.response.JsonResponseContactList;
import de.metas.rest_api.utils.IdentifierString;
import de.metas.util.rest.MetasfreshRestAPIConstants;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;

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

@RequestMapping(MetasfreshRestAPIConstants.ENDPOINT_API + "/contact")
@RestController
@Profile(Profiles.PROFILE_App)
public class ContactRestController implements ContactRestEndpoint
{

	private final BPartnerEndpointService bpartnerEndpointService;
	private final JsonServiceFactory jsonServiceFactory;

	public ContactRestController(
			@NonNull final BPartnerEndpointService bpIbPartnerEndpointservice,
			@NonNull final JsonServiceFactory jsonServiceFactory)
	{
		this.bpartnerEndpointService = bpIbPartnerEndpointservice;
		this.jsonServiceFactory = jsonServiceFactory;
	}

	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved contact"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})
	@GetMapping("{contactIdentifier}")
	@Override
	public ResponseEntity<JsonResponseContact> retrieveContact(
			@ApiParam(required = true, value = CONTACT_IDENTIFIER_DOC) //
			@PathVariable("contactIdentifier") //
			@NonNull final String contactIdentifierStr)
	{
		final IdentifierString contactIdentifier = IdentifierString.of(contactIdentifierStr);

		final Optional<JsonResponseContact> contact = bpartnerEndpointService.retrieveContact(contactIdentifier);
		return toResponseEntity(contact);
	}

	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved contact(s)"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "There is no page for the given 'next' value")
	})
	@GetMapping
	@Override
	public ResponseEntity<JsonResponseContactList> retrieveContactsSince(

			@ApiParam(value = SINCE_DOC, allowEmptyValue = true) //
			@RequestParam(name = "since", required = false) //
			@Nullable final Long epochTimestampMillis,

			@ApiParam(value = NEXT_DOC, allowEmptyValue = true) //
			@RequestParam(name = "next", required = false) //
			@Nullable final String next)
	{
		final Optional<JsonResponseContactList> list = bpartnerEndpointService.retrieveContactsSince(epochTimestampMillis, next);
		return toResponseEntity(list);
	}

	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully created or updated contact"),
			@ApiResponse(code = 401, message = "You are not authorized to create or update the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
	})
	@ApiOperation("Create of update a contact for a particular bpartner. If the contact exists, then the properties that are *not* specified are left untouched.")
	@PutMapping
	@Override
	public ResponseEntity<JsonResponseUpsert> createOrUpdateContact(
			@RequestBody @NonNull final JsonRequestContactUpsert contacts)
	{
		final JsonResponseUpsertBuilder response = JsonResponseUpsert.builder();
		final SyncAdvise syncAdvise = SyncAdvise.builder().ifExists(IfExists.UPDATE_MERGE).ifNotExists(IfNotExists.CREATE).build();

		final JsonPersisterService persister = jsonServiceFactory.createPersister();

		for (final JsonRequestContactUpsertItem requestItem : contacts.getRequestItems())
		{
			final BPartnerContact bpartnerContact = persister.persist(
					IdentifierString.of(requestItem.getContactIdentifier()),
					requestItem.getContact(),
					syncAdvise);

			final MetasfreshId metasfreshId = MetasfreshId.of(bpartnerContact.getId());

			final JsonResponseUpsertItem responseItem = JsonResponseUpsertItem
					.builder()
					.identifier(requestItem.getContactIdentifier())
					.metasfreshId(metasfreshId)
					.build();
			response.responseItem(responseItem);
		}
		return new ResponseEntity<>(response.build(), HttpStatus.CREATED);
	}

	private static <T> ResponseEntity<T> toResponseEntity(@NonNull final Optional<T> optionalResult)
	{
		return optionalResult
				.map(result -> ResponseEntity.ok(result))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
