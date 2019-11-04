package org.adempiere.test;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.save;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
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
import java.util.function.Function;

import org.adempiere.ad.dao.impl.POJOQuery;
import org.adempiere.ad.persistence.cache.AbstractModelListCacheLocal;
import org.adempiere.ad.wrapper.POJOLookupMap;
import org.adempiere.ad.wrapper.POJOWrapper;
import org.adempiere.context.SwingContextProvider;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.model.PlainContextAware;
import org.adempiere.util.lang.IContextAware;
import org.adempiere.util.proxy.Cached;
import org.adempiere.util.proxy.impl.JavaAssistInterceptor;
import org.adempiere.util.reflect.TestingClassInstanceProvider;
import org.compiere.Adempiere;
import org.compiere.model.I_AD_Client;
import org.compiere.model.I_AD_Org;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.util.Env;
import org.compiere.util.Ini;
import org.compiere.util.Util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.qos.logback.classic.Level;
import de.metas.JsonObjectMapperHolder;
import de.metas.adempiere.form.IClientUI;
import de.metas.adempiere.model.I_AD_User;
import de.metas.cache.CacheMgt;
import de.metas.cache.interceptor.CacheInterceptor;
import de.metas.i18n.Language;
import de.metas.logging.LogManager;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.UnitTestServiceNamePolicy;
import de.metas.util.lang.UIDStringUtil;
import de.metas.util.time.SystemTime;
import io.github.jsonSnapshot.SnapshotConfig;
import io.github.jsonSnapshot.SnapshotMatcher;
import io.github.jsonSnapshot.SnapshotMatchingStrategy;
import io.github.jsonSnapshot.matchingstrategy.JSONAssertMatchingStrategy;

/**
 * Helper to be used in order to setup ANY test which depends on ADempiere.
 *
 * @author tsa
 *
 */
public class AdempiereTestHelper
{
	private static final AdempiereTestHelper instance = new AdempiereTestHelper();

	public static final String AD_LANGUAGE = "de_DE";

	/** This config makes sure that the snapshot files end up in {@code src/test/resource/} so they make it into the test jars */
	public static final SnapshotConfig SNAPSHOT_CONFIG = new SnapshotConfig()
	{
		@Override
		public String getFilePath()
		{
			return "src/test/resources/";
		}

		@Override
		public SnapshotMatchingStrategy getSnapshotMatchingStrategy()
		{
			return JSONAssertMatchingStrategy.INSTANCE_STRICT;
		}
	};

	public static AdempiereTestHelper get()
	{
		return instance;
	}

	private boolean staticInitialized = false;

	public void staticInit()
	{
		if (staticInitialized)
		{
			return;
		}

		Adempiere.enableUnitTestMode();

		Check.setDefaultExClass(AdempiereException.class);

		Util.setClassInstanceProvider(TestingClassInstanceProvider.instance);

		//
		// Configure services; note the this is not the place to register individual services, see init() for that.
		Services.setAutodetectServices(true);
		Services.setServiceNameAutoDetectPolicy(new UnitTestServiceNamePolicy()); // 04113

		//
		// Make sure cache is empty
		CacheMgt.get().reset();

		staticInitialized = true;
	}

	public void init()
	{
		// Make sure context is clear before starting a new test
		final Properties ctx = setupContext();

		// By default we are running in client mode
		Ini.setClient(true);

		// Make sure staticInit was called
		staticInit();

		POJOQuery.clear_UUID_TO_PAGE();

		// Make sure database is clean
		POJOLookupMap.resetAll();

		// we also don't want any model interceptors to interfere, unless we explicitly test them up to do so
		POJOLookupMap.get().clear();

		//
		// POJOWrapper defaults
		POJOWrapper.setAllowRefreshingChangedModels(false);
		POJOWrapper.setDefaultStrictValues(POJOWrapper.DEFAULT_StrictValues);

		// Setup services
		{
			// Make sure we don't have custom registered services
			// Each test shall init it's services if it wants
			Services.clear();

			//
			// Register our cache interceptor
			// NOTE: in normal run, it is registered from org.compiere.Adempiere.startup(RunMode)
			Services.getInterceptor().registerInterceptor(Cached.class, new CacheInterceptor()); // task 06952
			JavaAssistInterceptor.FAIL_ON_ERROR = true;

			Services.registerService(IClientUI.class, new TestClientUI());
		}

		// Base Language
		Language.setBaseLanguage(() -> AD_LANGUAGE);
		Env.setContext(ctx, Env.CTXNAME_AD_Language, AD_LANGUAGE);

		// Reset System Time and random UUID
		SystemTime.resetTimeSource();
		UIDStringUtil.reset();

		// Caching
		AbstractModelListCacheLocal.DEBUG = true;
		CacheMgt.get().reset();

		// Logging
		LogManager.setLevel(Level.WARN);

		// JSON
		JsonObjectMapperHolder.resetSharedJsonObjectMapper();

		createSystemRecords();
	}

	private static Properties setupContext()
	{
		Env.setContextProvider(new SwingContextProvider());

		final Properties ctx = Env.getCtx();
		ctx.clear();
		return ctx;
	}

	public void setupContext_AD_Client_IfNotSet()
	{
		final Properties ctx = Env.getCtx();

		// Do nothing if already set
		if (Env.getAD_Client_ID(ctx) > 0)
		{
			return;
		}

		final IContextAware contextProvider = PlainContextAware.newOutOfTrx(ctx);
		final I_AD_Client adClient = InterfaceWrapperHelper.newInstance(I_AD_Client.class, contextProvider);
		adClient.setValue("Test");
		adClient.setName("Test");
		adClient.setAD_Language(AD_LANGUAGE);
		InterfaceWrapperHelper.save(adClient);

		Env.setContext(ctx, Env.CTXNAME_AD_Client_ID, adClient.getAD_Client_ID());
	}

	private static void createSystemRecords()
	{
		final I_AD_Org allOrgs = newInstance(I_AD_Org.class);
		allOrgs.setAD_Org_ID(0);
		save(allOrgs);

		final I_AD_User systemUser = newInstance(I_AD_User.class);
		systemUser.setAD_User_ID(0);
		save(systemUser);

		final I_M_AttributeSetInstance noAsi = newInstance(I_M_AttributeSetInstance.class);
		noAsi.setM_AttributeSetInstance_ID(0);
		save(noAsi);
	}

	/**
	 * Create JSON serialization function to be used by {@link SnapshotMatcher#start(SnapshotConfig, Function)}.
	 * 
	 * The function is using our {@link JsonObjectMapperHolder#newJsonObjectMapper()} with a pretty printer.
	 */
	public static Function<Object, String> createSnapshotJsonFunction()
	{
		final ObjectMapper jsonObjectMapper = JsonObjectMapperHolder.newJsonObjectMapper();
		final ObjectWriter writerWithDefaultPrettyPrinter = jsonObjectMapper.writerWithDefaultPrettyPrinter();
		return object -> {
			try
			{
				return writerWithDefaultPrettyPrinter.writeValueAsString(object);
			}
			catch (JsonProcessingException e)
			{
				throw AdempiereException.wrapIfNeeded(e);
			}
		};
	}
}
