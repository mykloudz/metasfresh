package de.metas.impexp.async;

/*
 * #%L
 * de.metas.async
 * %%
 * Copyright (C) 2015 metas GmbH
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

import java.util.Properties;

import org.adempiere.ad.table.api.IADTableDAO;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.util.Env;
import org.slf4j.Logger;

import de.metas.async.model.I_C_Queue_WorkPackage;
import de.metas.async.spi.WorkpackageProcessorAdapter;
import de.metas.event.Topic;
import de.metas.event.Type;
import de.metas.impexp.processing.IImportProcess;
import de.metas.impexp.processing.IImportProcessFactory;
import de.metas.impexp.processing.ImportProcessResult;
import de.metas.logging.LogManager;
import de.metas.notification.INotificationBL;
import de.metas.notification.UserNotificationRequest;
import de.metas.user.UserId;
import de.metas.util.Check;
import de.metas.util.Loggables;
import de.metas.util.Services;

/**
 * Workpackage processor used to import records enqueued by {@link AsyncImportProcessBuilder}.
 *
 * @author tsa
 *
 */
public class AsyncImportWorkpackageProcessor extends WorkpackageProcessorAdapter
{
	// services
	private static final transient Logger logger = LogManager.getLogger(AsyncImportWorkpackageProcessor.class);

	public static final String PARAM_completid = "ImportTableName";
	public static final String PARAM_ImportTableName = "ImportTableName";
	public static final String PARAM_Selection_ID = IImportProcess.PARAM_Selection_ID;

	private static final String MSG_Event_RecordsImported = "org.adempiere.impexp.async.Event_RecordsImported";
	public static final Topic USER_NOTIFICATIONS_TOPIC = Topic.of("org.adempiere.impexp.async.RecordsImported", Type.REMOTE);

	/** @return false. IMPORTANT: let the {@link IImportProcess} manage the transactions */
	@Override
	public boolean isRunInTransaction()
	{
		return false;
	}

	private String getImportTableName()
	{
		return getParameters().getParameterAsString(PARAM_ImportTableName);
	}

	@Override
	public Result processWorkPackage(final I_C_Queue_WorkPackage workpackage, final String localTrxName)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(workpackage);

		final String importTableName = getImportTableName();
		Check.assumeNotNull(importTableName, "importTableName not null");

		// Make sure we have a selection defined. It will be used by the import processor.
		final int selectionId = getParameters().getParameterAsInt(PARAM_Selection_ID, -1);
		Check.assume(selectionId > 0, "selectionId > 0");

		final IImportProcess<Object> importProcessor = Services.get(IImportProcessFactory.class).newImportProcessForTableName(importTableName);
		importProcessor.setCtx(ctx);
		importProcessor.setLoggable(Loggables.get());
		importProcessor.setParameters(getParameters()); // make all package parameters available to the import-BL!
		final ImportProcessResult result = importProcessor.run();

		final UserId recipientUserId = UserId.ofRepoId(workpackage.getCreatedBy());
		notifyImportDone(result, recipientUserId);

		return Result.SUCCESS;
	}

	private void notifyImportDone(final ImportProcessResult result, final UserId recipientUserId)
	{
		try
		{
			final String targetTableName = result.getTargetTableName();
			final String windowName = Services.get(IADTableDAO.class).retrieveWindowName(Env.getCtx(), targetTableName);

			Services.get(INotificationBL.class)
					.send(UserNotificationRequest.builder()
							.topic(USER_NOTIFICATIONS_TOPIC)
							.recipientUserId(recipientUserId)
							.contentADMessage(MSG_Event_RecordsImported)
							.contentADMessageParam(result.getCountInsertsIntoTargetTableString())
							.contentADMessageParam(result.getCountUpdatesIntoTargetTableString())
							.contentADMessageParam(windowName)
							.build());
		}
		catch (final Exception ex)
		{
			logger.warn("Failed notifying user '{}' about {}. Ignored.", recipientUserId, result, ex);
		}
	}

}
