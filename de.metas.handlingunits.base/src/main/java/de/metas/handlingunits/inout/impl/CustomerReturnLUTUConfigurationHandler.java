package de.metas.handlingunits.inout.impl;

import static org.adempiere.model.InterfaceWrapperHelper.loadOutOfTrx;

import java.math.BigDecimal;

import org.adempiere.warehouse.LocatorId;
import org.adempiere.warehouse.WarehouseId;
import org.adempiere.warehouse.api.IWarehouseBL;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_InOut;

import de.metas.handlingunits.allocation.ILUTUConfigurationFactory;
import de.metas.handlingunits.impl.AbstractDocumentLUTUConfigurationHandler;
import de.metas.handlingunits.model.I_M_HU_LUTU_Configuration;
import de.metas.handlingunits.model.I_M_HU_PI_Item_Product;
import de.metas.handlingunits.model.I_M_InOutLine;
import de.metas.handlingunits.model.X_M_HU;
import de.metas.product.ProductId;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * de.metas.handlingunits.base
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

public class CustomerReturnLUTUConfigurationHandler
		extends AbstractDocumentLUTUConfigurationHandler<I_M_InOutLine>
{

	public static final transient CustomerReturnLUTUConfigurationHandler instance = new CustomerReturnLUTUConfigurationHandler();

	private CustomerReturnLUTUConfigurationHandler()
	{
		super();
	}

	@Override
	public I_M_HU_LUTU_Configuration getCurrentLUTUConfigurationOrNull(final I_M_InOutLine documentLine)
	{
		Check.assumeNotNull(documentLine, "documentLine not null");

		final I_M_HU_LUTU_Configuration lutuConfiguration = documentLine.getM_HU_LUTU_Configuration();
		if (lutuConfiguration == null || lutuConfiguration.getM_HU_LUTU_Configuration_ID() <= 0)
		{
			return null;
		}

		return lutuConfiguration;
	}

	@Override
	public void setCurrentLUTUConfiguration(final I_M_InOutLine documentLine, final I_M_HU_LUTU_Configuration lutuConfiguration)
	{
		documentLine.setM_HU_LUTU_Configuration(lutuConfiguration);
	}

	@Override
	public I_M_HU_LUTU_Configuration createNewLUTUConfiguration(final I_M_InOutLine documentLine)
	{

		final ILUTUConfigurationFactory lutuFactory = Services.get(ILUTUConfigurationFactory.class);

		final I_M_HU_PI_Item_Product tuPIItemProduct = getM_HU_PI_Item_Product(documentLine);
		final ProductId cuProductId = ProductId.ofRepoIdOrNull(documentLine.getM_Product_ID());
		final I_C_UOM cuUOM = loadOutOfTrx(documentLine.getC_UOM_ID(), I_C_UOM.class);

		final I_C_BPartner bpartner = documentLine.getM_InOut().getC_BPartner();
		final I_M_HU_LUTU_Configuration lutuConfiguration = lutuFactory.createLUTUConfiguration(
				tuPIItemProduct,
				cuProductId,
				cuUOM,
				bpartner,
				false); // noLUForVirtualTU == false => allow placing the CU (e.g. a packing material product) directly on the LU

		//
		// Update LU/TU configuration
		updateLUTUConfigurationFromPPOrder(lutuConfiguration, documentLine);

		// NOTE: don't save it

		return lutuConfiguration;
	}

	@Override
	public void updateLUTUConfigurationFromPPOrder(@NonNull final I_M_HU_LUTU_Configuration lutuConfiguration, @NonNull final I_M_InOutLine documentLine)
	{
		final I_M_InOut customerReturn = documentLine.getM_InOut();
		//
		// Set BPartner / Location to be used
		final I_C_BPartner bpartner = customerReturn.getC_BPartner();
		final int bpartnerLocationId = customerReturn.getC_BPartner_Location_ID();
		lutuConfiguration.setC_BPartner_ID(bpartner != null ? bpartner.getC_BPartner_ID() : -1);
		lutuConfiguration.setC_BPartner_Location_ID(bpartnerLocationId);

		//
		// Set Locator
		final WarehouseId warehouseId = WarehouseId.ofRepoId(customerReturn.getM_Warehouse_ID());
		final LocatorId locatorId = Services.get(IWarehouseBL.class).getDefaultLocatorId(warehouseId);
		lutuConfiguration.setM_Locator_ID(locatorId.getRepoId());

		//
		// Set HUStatus=Planning because receipt schedules are always about planning
		lutuConfiguration.setHUStatus(X_M_HU.HUSTATUS_Planning);

		lutuConfiguration.setQtyTU(documentLine.getQtyEnteredTU());
		lutuConfiguration.setQtyLU(BigDecimal.ONE);
		lutuConfiguration.setIsInfiniteQtyLU(false);
	}

	@Override
	public I_M_HU_PI_Item_Product getM_HU_PI_Item_Product(final I_M_InOutLine inOutLine)
	{
		return inOutLine.getM_HU_PI_Item_Product();
	}
}
