package org.adempiere.model;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.element.api.AdWindowId;
import org.adempiere.ad.window.api.IADWindowDAO;
import org.adempiere.model.ZoomInfoFactory.IZoomSource;
import org.adempiere.model.ZoomInfoFactory.ZoomInfo;
import org.compiere.model.I_Fact_Acct;
import org.compiere.model.MQuery;
import org.compiere.model.MQuery.Operator;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;

import de.metas.i18n.ITranslatableString;
import de.metas.util.Loggables;
import de.metas.util.Services;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
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

public class FactAcctZoomProvider implements IZoomProvider
{
	public static final transient FactAcctZoomProvider instance = new FactAcctZoomProvider();
	private static final String COLUMNNAME_Posted = "Posted";

	private FactAcctZoomProvider()
	{
	}

	@Override
	public List<ZoomInfo> retrieveZoomInfos(final IZoomSource source, final AdWindowId targetAD_Window_ID, final boolean checkRecordsCount)
	{
		//
		// Get the Fact_Acct AD_Window_ID
		final AdWindowId factAcctWindowId = RecordZoomWindowFinder.findAdWindowId(I_Fact_Acct.Table_Name).orElse(null);
		if (factAcctWindowId == null)
		{
			return ImmutableList.of();
		}

		// If not our target window ID, return nothing
		if (targetAD_Window_ID != null && !AdWindowId.equals(targetAD_Window_ID, factAcctWindowId))
		{
			return ImmutableList.of();
		}

		// Return nothing if source is not Posted
		if (source.hasField(COLUMNNAME_Posted))
		{
			final boolean posted = source.getFieldValueAsBoolean(COLUMNNAME_Posted);
			if (!posted)
			{
				return ImmutableList.of();
			}
		}

		//
		// Build query and check count if needed
		final MQuery query = new MQuery(I_Fact_Acct.Table_Name);
		query.addRestriction(I_Fact_Acct.COLUMNNAME_AD_Table_ID, Operator.EQUAL, source.getAD_Table_ID());
		query.addRestriction(I_Fact_Acct.COLUMNNAME_Record_ID, Operator.EQUAL, source.getRecord_ID());

		if (checkRecordsCount)
		{
			final Stopwatch stopwatch = Stopwatch.createStarted();

			final int count = Services.get(IQueryBL.class).createQueryBuilder(I_Fact_Acct.class)
					.addEqualsFilter(I_Fact_Acct.COLUMN_AD_Table_ID, source.getAD_Table_ID())
					.addEqualsFilter(I_Fact_Acct.COLUMN_Record_ID, source.getRecord_ID())
					.create()
					.count();

			final Duration countDuration = Duration.ofNanos(stopwatch.stop().elapsed(TimeUnit.NANOSECONDS));
			query.setRecordCount(count, countDuration);

			Loggables.addLog("FactAcctZoomProvider {} took {}", this, countDuration);
		}

		//
		final ITranslatableString destinationDisplay = Services.get(IADWindowDAO.class).retrieveWindowName(factAcctWindowId);
		return ImmutableList.of(ZoomInfo.of(
				I_Fact_Acct.Table_Name/* id */,
				I_Fact_Acct.Table_Name/* internalName */,
				factAcctWindowId,
				query,
				destinationDisplay));
	}

}
