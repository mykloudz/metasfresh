package org.adempiere.mm.attributes.api;

import org.adempiere.mm.attributes.AttributeSetInstanceId;

import lombok.experimental.UtilityClass;

/*
 * #%L
 * de.metas.business
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

@UtilityClass
public final class AttributeConstants
{
	/**
	 * No ASI (record which actually exists in M_AttributeSetInstance table)
	 */
	public static final int M_AttributeSetInstance_ID_None = AttributeSetInstanceId.NONE.getRepoId();

	/**
	 * No Attribute Set (record which actually exists in M_AttributeSet table)
	 */
	public static final int M_AttributeSet_ID_None = 0;


	public static final String ATTR_TE = "HU_TE";
	public static final String ATTR_DateReceived = "HU_DateReceived";
	public static final String ATTR_SecurPharmScannedStatus = "HU_Scanned";
	
	public static final String ATTR_BestBeforeDate = "HU_BestBeforeDate";
	public static final String ATTR_MonthsUntilExpiry = "MonthsUntilExpiry";

	//
	public static final String ATTR_SubProducerBPartner_Value = "SubProducerBPartner";

	public static final String ATTR_SerialNo = "SerialNo";
	public static final String ATTR_LotNr = "Lot-Nummer";

}
