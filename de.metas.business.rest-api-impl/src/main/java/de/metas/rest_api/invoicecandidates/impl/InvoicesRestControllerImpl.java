package de.metas.rest_api.invoicecandidates.impl;

import java.util.List;

import org.compiere.util.Env;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.metas.Profiles;
import de.metas.invoicecandidate.api.IInvoiceCandBL;
import de.metas.invoicecandidate.api.IInvoiceCandidateEnqueueResult;
import de.metas.logging.LogManager;
import de.metas.process.IADPInstanceDAO;
import de.metas.process.PInstanceId;
import de.metas.rest_api.invoicecandidates.IInvoicesRestEndpoint;
import de.metas.rest_api.invoicecandidates.request.JsonEnqueueForInvoicingRequest;
import de.metas.rest_api.invoicecandidates.response.JsonEnqueueForInvoicingResponse;
import de.metas.rest_api.utils.JsonErrors;
import de.metas.util.Services;
import de.metas.util.rest.ExternalHeaderAndLineId;
import io.swagger.annotations.ApiOperation;
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
 * Used for managing invoices and invoice candidates(create, query)
 */
@RestController
@RequestMapping(value = IInvoicesRestEndpoint.ENDPOINT, consumes = "application/json", produces = "application/json")
@Profile(Profiles.PROFILE_App)
class InvoicesRestControllerImpl implements IInvoicesRestEndpoint
{

	private static final Logger logger = LogManager.getLogger(InvoicesRestControllerImpl.class);

	private final IADPInstanceDAO adPInstanceDAO = Services.get(IADPInstanceDAO.class);
	private final IInvoiceCandBL invoiceCandBL = Services.get(IInvoiceCandBL.class);
	private final InvoiceJsonConverterService jsonConverter;

	public InvoicesRestControllerImpl(@NonNull final InvoiceJsonConverterService jsonConverter)
	{
		this.jsonConverter = jsonConverter;
	}

	@ApiOperation("Enqueues invoice candidates for invoicing")
	@PostMapping(path = "enqueueForInvoicing")
	@Override
	public ResponseEntity<JsonEnqueueForInvoicingResponse> enqueueForInvoicing(@RequestBody @NonNull final JsonEnqueueForInvoicingRequest request)
	{
		try
		{
			final PInstanceId pInstanceId = adPInstanceDAO.createSelectionId();
			final List<ExternalHeaderAndLineId> headerAndLineIds = jsonConverter.convertJICToExternalHeaderAndLineIds(request.getInvoiceCandidates());
			invoiceCandBL.createSelectionForInvoiceCandidates(headerAndLineIds, pInstanceId);

			final IInvoiceCandidateEnqueueResult enqueueResult = invoiceCandBL
					.enqueueForInvoicing()
					.setInvoicingParams(jsonConverter.createInvoicingParams(request))
					.setFailIfNothingEnqueued(true)
					.enqueueSelection(pInstanceId);

			final JsonEnqueueForInvoicingResponse response = jsonConverter.toJson(enqueueResult);
			return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
		}
		catch (final Exception ex)
		{
			logger.warn("Got exception while processing request={}", request, ex);

			final String adLanguage = Env.getADLanguageOrBaseLanguage();
			return ResponseEntity
					.badRequest()
					.body(JsonEnqueueForInvoicingResponse.error(JsonErrors.ofThrowable(ex, adLanguage)));
		}
	}
}
