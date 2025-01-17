package de.metas.uom;

import java.time.temporal.TemporalUnit;

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

import org.compiere.model.I_C_UOM;

import de.metas.util.ISingletonService;

public interface IUOMDAO extends ISingletonService
{
	String X12DE355_Each = "PCE";
	int C_UOM_ID_Each = 100;
	String X12DE355_Kilogram = "KGM";

	I_C_UOM getById(int uomId);

	I_C_UOM getByIdOrNull(int uomId);

	I_C_UOM getById(UomId uomId);

	UomId getUomIdByX12DE355(String x12de355);

	String getX12DE355ById(UomId uomId);

	/**
	 * @return uom; never return null
	 */
	I_C_UOM retrieveByX12DE355(Properties ctx, String x12de355);

	/**
	 * @param throwExIfNull if <code>false</code> and there is no UOM with the given <code>x12de355</code>, then we return <code>null</code>.
	 * @return uom; never return null
	 */
	I_C_UOM retrieveByX12DE355(Properties ctx, String x12de355, boolean throwExIfNull);

	/**
	 * Gets UOM for Each/Stuck.
	 *
	 * @param ctx
	 * @return
	 */
	I_C_UOM retrieveEachUOM(Properties ctx);

	TemporalUnit getTemporalUnitByUomId(final UomId uomId);

	UOMPrecision getStandardPrecision(final UomId uomId);

}
