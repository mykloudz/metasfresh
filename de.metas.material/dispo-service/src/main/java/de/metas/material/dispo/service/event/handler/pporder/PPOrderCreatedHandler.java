package de.metas.material.dispo.service.event.handler.pporder;

import java.util.Collection;

import javax.annotation.Nullable;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import de.metas.Profiles;
import de.metas.material.dispo.commons.candidate.CandidateBusinessCase;
import de.metas.material.dispo.commons.candidate.CandidateType;
import de.metas.material.dispo.commons.candidate.businesscase.Flag;
import de.metas.material.dispo.commons.repository.CandidateRepositoryRetrieval;
import de.metas.material.dispo.commons.repository.query.CandidatesQuery;
import de.metas.material.dispo.service.candidatechange.CandidateChangeService;
import de.metas.material.event.commons.ProductDescriptor;
import de.metas.material.event.commons.SupplyRequiredDescriptor;
import de.metas.material.event.pporder.AbstractPPOrderEvent;
import de.metas.material.event.pporder.MaterialDispoGroupId;
import de.metas.material.event.pporder.PPOrder;
import de.metas.material.event.pporder.PPOrderCreatedEvent;
import de.metas.util.Loggables;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-material-dispo
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

@Service
@Profile(Profiles.PROFILE_MaterialDispo)
public final class PPOrderCreatedHandler
		extends PPOrderAdvisedOrCreatedHandler<PPOrderCreatedEvent>
{
	/**
	 *
	 * @param candidateChangeHandler
	 * @param candidateService needed in case we directly request a {@link PpOrderSuggestedEvent}'s proposed PP_Order to be created.
	 */
	public PPOrderCreatedHandler(
			@NonNull final CandidateChangeService candidateChangeHandler,
			@NonNull final CandidateRepositoryRetrieval candidateRepositoryRetrieval)
	{
		super(candidateChangeHandler, candidateRepositoryRetrieval);
	}

	@Override
	public Collection<Class<? extends PPOrderCreatedEvent>> getHandeledEventType()
	{
		return ImmutableList.of(PPOrderCreatedEvent.class);
	}

	@Override
	public void validateEvent(@NonNull final PPOrderCreatedEvent event)
	{
		event.validate();
	}

	@Override
	public void handleEvent(@NonNull final PPOrderCreatedEvent event)
	{
		handleAbstractPPOrderEvent(event);
	}

	@Override
	protected CandidatesQuery createPreExistingCandidatesQuery(
			@NonNull final PPOrder ppOrder,
			@Nullable final SupplyRequiredDescriptor supplyRequiredDescriptor_NOTUSED)
	{
		return createPreExistingCandidatesQuery(ppOrder);
	}

	private static CandidatesQuery createPreExistingCandidatesQuery(@NonNull final PPOrder ppOrder)
	{
		final MaterialDispoGroupId groupId = ppOrder.getMaterialDispoGroupId();
		if (groupId == null)
		{
			Loggables.addLog("The given ppOrderCreatedEvent has no groupId, so it was created manually by a user and not via material-dispo. Going to create new candidate records.");
			return CandidatesQuery.FALSE;
		}

		final ProductDescriptor productDescriptor = ppOrder.getProductDescriptor();

		final CandidatesQuery query = CandidatesQuery.builder()
				.type(CandidateType.SUPPLY)
				.businessCase(CandidateBusinessCase.PRODUCTION)
				.groupId(groupId)

				// there might also be supply candidates for co-products, so the groupId alone is not sufficient
				.materialDescriptorQuery(PPOrderHandlerUtils.createMaterialDescriptorQuery(productDescriptor))
				.build();

		return query;
	}

	@Override
	protected Flag extractIsAdviseEvent(@NonNull final AbstractPPOrderEvent ppOrderEvent)
	{
		return Flag.FALSE_DONT_UPDATE;
	}

	@Override
	protected Flag extractIsDirectlyPickSupply(@NonNull final AbstractPPOrderEvent ppOrderEvent)
	{
		return Flag.FALSE_DONT_UPDATE;
	}
}
