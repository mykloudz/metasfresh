package de.metas.material.dispo.service.candidatechange.handler;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import de.metas.Profiles;
import de.metas.material.dispo.commons.candidate.Candidate;
import de.metas.material.dispo.commons.candidate.CandidateId;
import de.metas.material.dispo.commons.candidate.CandidateType;
import de.metas.material.dispo.commons.repository.CandidateRepositoryRetrieval;
import de.metas.material.dispo.commons.repository.CandidateRepositoryWriteService;
import de.metas.material.dispo.commons.repository.CandidateRepositoryWriteService.DeleteResult;
import de.metas.material.dispo.commons.repository.CandidateRepositoryWriteService.SaveResult;
import de.metas.material.dispo.commons.repository.DateAndSeqNo;
import de.metas.material.dispo.commons.repository.atp.AvailableToPromiseMultiQuery;
import de.metas.material.dispo.commons.repository.atp.AvailableToPromiseRepository;
import de.metas.material.dispo.service.candidatechange.StockCandidateService;
import de.metas.material.event.PostMaterialEventService;
import de.metas.material.event.supplyrequired.SupplyRequiredEvent;
import de.metas.util.Loggables;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-material-dispo-service
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
public class DemandCandiateHandler implements CandidateHandler
{
	private final CandidateRepositoryRetrieval candidateRepository;
	private final AvailableToPromiseRepository availableToPromiseRepository;
	private final PostMaterialEventService materialEventService;
	private final StockCandidateService stockCandidateService;
	private final CandidateRepositoryWriteService candidateRepositoryWriteService;

	public DemandCandiateHandler(
			@NonNull final CandidateRepositoryRetrieval candidateRepository,
			@NonNull final CandidateRepositoryWriteService candidateRepositoryCommands,
			@NonNull final PostMaterialEventService materialEventService,
			@NonNull final AvailableToPromiseRepository availableToPromiseRepository,
			@NonNull final StockCandidateService stockCandidateService)
	{
		this.candidateRepository = candidateRepository;
		this.candidateRepositoryWriteService = candidateRepositoryCommands;
		this.materialEventService = materialEventService;
		this.availableToPromiseRepository = availableToPromiseRepository;
		this.stockCandidateService = stockCandidateService;
	}

	@Override
	public Collection<CandidateType> getHandeledTypes()
	{
		return ImmutableList.of(
				CandidateType.DEMAND,
				CandidateType.UNRELATED_DECREASE,
				CandidateType.INVENTORY_DOWN,
				CandidateType.ATTRIBUTES_CHANGED_FROM);
	}

	/**
	 * Persists (updates or creates) the given demand candidate and also its <b>child</b> stock candidate.
	 */
	@Override
	public Candidate onCandidateNewOrChange(@NonNull final Candidate candidate)
	{
		assertCorrectCandidateType(candidate);

		final SaveResult candidateSaveResult = candidateRepositoryWriteService.addOrUpdateOverwriteStoredSeqNo(candidate);

		if (!candidateSaveResult.isDateChanged() && !candidateSaveResult.isQtyChanged())
		{
			return candidateSaveResult.toCandidateWithQtyDelta(); // nothing to do
		}

		final Candidate savedCandidate = candidateSaveResult.getCandidate();

		final Optional<Candidate> preExistingChildStockCandidate = candidateRepository.retrieveSingleChild(savedCandidate.getId());
		final CandidateId preExistingChildStockId = preExistingChildStockCandidate.isPresent() ? preExistingChildStockCandidate.get().getId() : null;

		final SaveResult stockCandidate = stockCandidateService
				.createStockCandidate(savedCandidate.withNegatedQuantity())
				.withCandidateId(preExistingChildStockId);

		final Candidate savedStockCandidate = candidateRepositoryWriteService
				.addOrUpdateOverwriteStoredSeqNo(stockCandidate.getCandidate().withParentId(savedCandidate.getId()))
				.getCandidate();

		final SaveResult deltaToApplyToLaterStockCandiates = candidateSaveResult.withNegatedQuantity();

		stockCandidateService.applyDeltaToMatchingLaterStockCandidates(deltaToApplyToLaterStockCandiates);

		final Candidate candidateToReturn = candidateSaveResult
				.toCandidateWithQtyDelta()
				.withParentId(savedStockCandidate.getId());

		if (savedCandidate.getType() == CandidateType.DEMAND)
		{
			fireSupplyRequiredEventIfQtyBelowZero(candidateToReturn);
		}
		return candidateToReturn;
	}

	@Override
	public void onCandidateDelete(@NonNull final Candidate candidate)
	{
		assertCorrectCandidateType(candidate);

		candidateRepositoryWriteService.deleteCandidatebyId(candidate.getId());

		final Optional<Candidate> childStockCandidate = candidateRepository.retrieveSingleChild(candidate.getId());
		if (!childStockCandidate.isPresent())
		{
			return; // nothing to do
		}
		final DeleteResult stockDeleteResult = candidateRepositoryWriteService.deleteCandidatebyId(childStockCandidate.get().getId());

		final DateAndSeqNo timeOfDeletedStock = stockDeleteResult.getPreviousTime();
		final SaveResult applyDeltaRequest = SaveResult.builder()
				.candidate(candidate
						.withQuantity(ZERO)
						.withDate(timeOfDeletedStock.getDate())
						.withSeqNo(timeOfDeletedStock.getSeqNo()))
				.previousQty(stockDeleteResult.getPreviousQty())
				.build();
		stockCandidateService.applyDeltaToMatchingLaterStockCandidates(applyDeltaRequest);
	}

	private void assertCorrectCandidateType(@NonNull final Candidate demandCandidate)
	{
		final CandidateType type = demandCandidate.getType();

		Preconditions.checkArgument(
				getHandeledTypes().contains(demandCandidate.getType()),
				"Given parameter 'demandCandidate' has type=%s; demandCandidate=%s",
				type, demandCandidate);
	}

	private void fireSupplyRequiredEventIfQtyBelowZero(@NonNull final Candidate demandCandidateWithId)
	{
		final AvailableToPromiseMultiQuery query = AvailableToPromiseMultiQuery
				.forDescriptorAndAllPossibleBPartnerIds(demandCandidateWithId.getMaterialDescriptor());

		final BigDecimal availableQuantityAfterDemandWasApplied = availableToPromiseRepository.retrieveAvailableStockQtySum(query);
		Loggables.addLog("Quantity after demand applied: {}", availableQuantityAfterDemandWasApplied);
		if (availableQuantityAfterDemandWasApplied.signum() < 0)
		{
			final BigDecimal requiredQty = availableQuantityAfterDemandWasApplied.negate();

			final SupplyRequiredEvent supplyRequiredEvent = SupplyRequiredEventCreator //
					.createSupplyRequiredEvent(demandCandidateWithId, requiredQty);
			materialEventService.postEventAfterNextCommit(supplyRequiredEvent);
			Loggables.addLog("Fire supplyRequiredEvent after next commit; event={}", supplyRequiredEvent);
		}
	}
}
