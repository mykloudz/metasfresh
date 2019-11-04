package org.eevolution.model;

/*
 * #%L
 * de.metas.adempiere.libero.libero
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

import org.adempiere.ad.callout.spi.IProgramaticCalloutProvider;
import org.adempiere.ad.modelvalidator.AbstractModuleInterceptor;
import org.adempiere.ad.modelvalidator.IModelValidationEngine;
import org.adempiere.util.jmx.JMXRegistry;
import org.adempiere.util.jmx.JMXRegistry.OnJMXAlreadyExistsPolicy;
import org.compiere.SpringContextHolder;
import org.compiere.model.I_AD_Client;
import org.compiere.model.I_S_Resource;
import org.compiere.model.I_S_ResourceType;
import org.compiere.util.Env;
import org.eevolution.mrp.jmx.JMXMRPStatus;

import de.metas.cache.CacheMgt;
import de.metas.cache.model.IModelCacheService;
import de.metas.material.event.PostMaterialEventService;
import de.metas.material.planning.pporder.PPOrderPojoConverter;
import lombok.NonNull;

/**
 * Libero Validator
 *
 * @author Victor Perez
 * @author Trifon Trifonov
 *         <li>[ 2270421 ] Can not complete Shipment (Customer)</li>
 * @author Teo Sarca, www.arhipac.ro
 */
public final class LiberoValidator extends AbstractModuleInterceptor
{
	/** Context variable which says if libero manufacturing is enabled */
	public static final String CTX_IsLiberoEnabled = "#IsLiberoEnabled";

	private final PPOrderPojoConverter ppOrderConverter;
	private final PostMaterialEventService materialEventService;

	public LiberoValidator()
	{
		this(SpringContextHolder.instance.getBean(PPOrderPojoConverter.class),
				SpringContextHolder.instance.getBean(PostMaterialEventService.class));
	}

	public LiberoValidator(
			@NonNull final PPOrderPojoConverter ppOrderConverter,
			@NonNull final PostMaterialEventService materialEventService)
	{
		this.ppOrderConverter = ppOrderConverter;
		this.materialEventService = materialEventService;
	}

	@Override
	protected void onAfterInit()
	{
		setupJMX();
	}

	@Override
	protected void registerInterceptors(final IModelValidationEngine engine, final I_AD_Client client)
	{
		//
		// Master data
		engine.addModelValidator(new org.eevolution.model.validator.PP_Product_BOM(), client);
		engine.addModelValidator(new org.eevolution.model.validator.PP_Product_BOMLine(), client);
		engine.addModelValidator(org.eevolution.model.validator.PP_Product_Planning.INSTANCE, client);

		// PP_Order related
		engine.addModelValidator(new org.eevolution.model.validator.PP_Order(ppOrderConverter, materialEventService), client);
		engine.addModelValidator(new org.eevolution.model.validator.PP_Order_PostMaterialEvent(ppOrderConverter, materialEventService), client); // gh #523
		engine.addModelValidator(new org.eevolution.model.validator.PP_Order_BOM(), client);
		engine.addModelValidator(new org.eevolution.model.validator.PP_Order_BOMLine(), client);
		engine.addModelValidator(new org.eevolution.model.validator.PP_Order_Node_Product(), client);
		engine.addModelValidator(new org.eevolution.model.validator.PP_Cost_Collector(), client);

		//
		// DRP
		engine.addModelValidator(new org.eevolution.model.validator.DD_Order(), client);
		engine.addModelValidator(new org.eevolution.model.validator.DD_OrderFireMaterialEvent(), client); // gh #523
		engine.addModelValidator(new org.eevolution.model.validator.DD_OrderLine(), client);
		engine.addModelValidator(new org.eevolution.drp.model.validator.M_Movement(), client);

		//
		// Forecast
		engine.addModelValidator(new org.eevolution.model.validator.M_Forecast(), client);
	}

	@Override
	protected void registerCallouts(final IProgramaticCalloutProvider calloutsRegistry)
	{
		calloutsRegistry.registerAnnotatedCallout(org.eevolution.callout.DD_Order.instance);
		calloutsRegistry.registerAnnotatedCallout(org.eevolution.callout.DD_OrderLine.instance);
	}

	@Override
	protected void setupCaching(final IModelCacheService cachingService)
	{
		cachingService.addTableCacheConfigIfAbsent(I_S_Resource.class);
		cachingService.addTableCacheConfigIfAbsent(I_S_ResourceType.class);

		CacheMgt.get().enableRemoteCacheInvalidationForTableName(I_PP_Order.Table_Name);
	}

	private void setupJMX()
	{
		final JMXRegistry jmxRegistry = JMXRegistry.get();

		final JMXMRPStatus jmxMRPStatus = new JMXMRPStatus();
		jmxRegistry.registerJMX(jmxMRPStatus, OnJMXAlreadyExistsPolicy.Replace);
	}

	@Override
	public void onUserLogin(final int AD_Org_ID, final int AD_Role_ID, final int AD_User_ID)
	{
		Env.setContext(Env.getCtx(), CTX_IsLiberoEnabled, true);
	}
}	// LiberoValidator
