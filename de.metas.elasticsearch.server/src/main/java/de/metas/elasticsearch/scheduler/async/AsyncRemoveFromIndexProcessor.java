package de.metas.elasticsearch.scheduler.async;

import java.util.Set;

import org.adempiere.exceptions.AdempiereException;

import de.metas.async.exceptions.WorkpackageSkipRequestException;
import de.metas.async.model.I_C_Queue_WorkPackage;
import de.metas.async.spi.WorkpackageProcessorAdapter;
import de.metas.elasticsearch.IESSystem;
import de.metas.elasticsearch.config.ESModelIndexerId;
import de.metas.elasticsearch.indexer.IESIndexerResult;
import de.metas.elasticsearch.indexer.IESModelIndexer;
import de.metas.elasticsearch.indexer.IESModelIndexersRegistry;
import de.metas.elasticsearch.scheduler.impl.ESModelIndexingScheduler;
import de.metas.util.GuavaCollectors;
import de.metas.util.Loggables;
import de.metas.util.Services;

/*
 * #%L
 * de.metas.elasticsearch
 * %%
 * Copyright (C) 2016 metas GmbH
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

public class AsyncRemoveFromIndexProcessor extends WorkpackageProcessorAdapter
{
	private static final String PARAMETERNAME_ModelIndexerId = ESModelIndexingScheduler.PARAMETERNAME_ModelIndexerId;

	@Override
	public Result processWorkPackage(final I_C_Queue_WorkPackage workpackage, final String localTrxName)
	{
		final IESSystem esSystem = Services.get(IESSystem.class);
		if (!esSystem.isEnabled())
		{
			throw new AdempiereException("Skip processing because Elasticsearch feature is disabled.");
		}

		final ESModelIndexerId modelIndexerId = ESModelIndexerId.fromJson(getParameters().getParameterAsString(PARAMETERNAME_ModelIndexerId));

		// NOTE: we assume all queue elements are about the same table
		final boolean skipAlreadyScheduledItems = true;
		final Set<String> idsToRemove = retrieveQueueElements(skipAlreadyScheduledItems)
				.stream()
				.map(qe -> String.valueOf(qe.getRecord_ID()))
				.collect(GuavaCollectors.toImmutableSet());
		if (idsToRemove.isEmpty())
		{
			throw new AdempiereException("No source models found");
		}

		try
		{
			final IESModelIndexersRegistry esModelIndexersRegistry = Services.get(IESModelIndexersRegistry.class);
			final IESModelIndexer modelIndexer = esModelIndexersRegistry.getModelIndexerById(modelIndexerId);

			final IESIndexerResult result = modelIndexer.removeFromIndexByIds(idsToRemove);
			Loggables.addLog(result.getSummary());
			result.throwExceptionIfAnyFailure();

			return Result.SUCCESS;
		}
		catch (final Exception e)
		{
			final int skipTimeoutMillis = 60 * 1000; // 1min
			throw WorkpackageSkipRequestException.createWithTimeoutAndThrowable(e.getLocalizedMessage(), skipTimeoutMillis, e);
		}
	}
}
