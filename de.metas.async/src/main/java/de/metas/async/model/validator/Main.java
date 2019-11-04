package de.metas.async.model.validator;

import java.util.List;

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

import org.adempiere.ad.migration.logger.IMigrationLogger;
import org.adempiere.ad.modelvalidator.AbstractModuleInterceptor;
import org.adempiere.ad.modelvalidator.IModelValidationEngine;
import org.adempiere.ad.session.MFSession;
import org.adempiere.service.ISysConfigBL;
import org.compiere.SpringContextHolder;
import org.compiere.model.I_AD_Client;
import org.compiere.util.Ini;

import com.google.common.collect.ImmutableList;

import de.metas.Profiles;
import de.metas.async.Async_Constants;
import de.metas.async.api.IAsyncBatchListeners;
import de.metas.async.api.impl.AsyncBatchDAO;
import de.metas.async.model.I_C_Queue_WorkPackage;
import de.metas.async.model.I_C_Queue_WorkPackage_Log;
import de.metas.async.model.I_C_Queue_WorkPackage_Param;
import de.metas.async.processor.IQueueProcessorExecutorService;
import de.metas.async.spi.impl.DefaultAsyncBatchListener;
import de.metas.event.Topic;
import de.metas.impexp.async.AsyncImportProcessBuilderFactory;
import de.metas.impexp.async.AsyncImportWorkpackageProcessor;
import de.metas.impexp.processing.IImportProcessFactory;
import de.metas.util.Check;
import de.metas.util.Services;

/**
 * ASync module main validator. This is the entry point for all other stuff.
 *
 * NOTE: to prevent data corruption, this validator shall be started as last one because it will also start the queue processors (if running on server).
 * Also to make sure this case does not happen we are using a inital delay (see {@link #getInitDelayMillis()}).
 *
 * @author tsa
 *
 */
public class Main extends AbstractModuleInterceptor
{

	private static final String SYSCONFIG_ASYNC_INIT_DELAY_MILLIS = "de.metas.async.Async_InitDelayMillis";

	private static final int THREE_MINUTES = 3 * 60 * 1000;

	@Override
	public void onInit(final IModelValidationEngine engine, final I_AD_Client client)
	{
		Check.assumeNull(client, "Registering async module on client level ({}) is not allowed", client);

		super.onInit(engine, client);

		// task 04585: start queue processors only if we are running on the backend server.
		// =>why not always run them?
		// if we have two metasfresh wars/ears (one backend, one webUI), JMX names will collide
		// if we start it on clients without having a central monitoring-gathering point we never know what's going on
		// => it can all be solved, but as of now isn't
		if (SpringContextHolder.instance.isSpringProfileActive(Profiles.PROFILE_App))
		{
			final int initDelayMillis = getInitDelayMillis();
			Services.get(IQueueProcessorExecutorService.class).init(initDelayMillis);
		}

		final IMigrationLogger migrationLogger = Services.get(IMigrationLogger.class);
		migrationLogger.addTableToIgnoreList(I_C_Queue_WorkPackage.Table_Name);
		migrationLogger.addTableToIgnoreList(I_C_Queue_WorkPackage_Log.Table_Name);
		migrationLogger.addTableToIgnoreList(I_C_Queue_WorkPackage_Param.Table_Name);

		// Data import (async support)
		Services.get(IImportProcessFactory.class).setAsyncImportProcessBuilderFactory(AsyncImportProcessBuilderFactory.instance);
		Services.get(IAsyncBatchListeners.class).registerAsyncBatchNoticeListener(new DefaultAsyncBatchListener(), AsyncBatchDAO.ASYNC_BATCH_TYPE_DEFAULT); // task 08917
	}

	/**
	 * Gets how many milliseconds to wait until to actually initialize the {@link IQueueProcessorExecutorService}.
	 *
	 * Mainly we use this delay to make sure everything else is started before the queue processors will start to process.
	 *
	 * @return how many milliseconds to wait until to actually initialize the {@link IQueueProcessorExecutorService}.
	 */
	private final int getInitDelayMillis()
	{
		// I will leave the default value of 3 minutes, which was the common time until #2894
		final int delayTimeInMillis = Services.get(ISysConfigBL.class).getIntValue(SYSCONFIG_ASYNC_INIT_DELAY_MILLIS, THREE_MINUTES);

		return delayTimeInMillis;

	}

	@Override
	protected void registerInterceptors(IModelValidationEngine engine, I_AD_Client client)
	{
		engine.addModelValidator(new C_Queue_PackageProcessor(), client);
		engine.addModelValidator(new C_Queue_Processor(), client);
		engine.addModelValidator(new de.metas.lock.model.validator.Main(), client);
		engine.addModelValidator(new C_Async_Batch(), client);
		engine.addModelValidator(C_Queue_WorkPackage.INSTANCE, client);
	}

	/**
	 * Init the async queue processor service on user login.
	 */
	@Override
	public void onUserLogin(final int AD_Org_ID, final int AD_Role_ID, final int AD_User_ID)
	{
		if (!Ini.isSwingClient())
		{
			return;
		}

		final int delayMillis = getInitDelayMillis();
		Services.get(IQueueProcessorExecutorService.class).init(delayMillis);
		return;
	}

	/**
	 * Destroy all queueud processors on user logout.
	 */
	@Override
	public void beforeLogout(final MFSession session)
	{
		if (!Ini.isSwingClient())
		{
			return;
		}

		Services.get(IQueueProcessorExecutorService.class).removeAllQueueProcessors();
	}

	@Override
	protected List<Topic> getAvailableUserNotificationsTopics()
	{
		return ImmutableList.of(
				Async_Constants.WORKPACKAGE_ERROR_USER_NOTIFICATIONS_TOPIC,
				AsyncImportWorkpackageProcessor.USER_NOTIFICATIONS_TOPIC);
	}
}
