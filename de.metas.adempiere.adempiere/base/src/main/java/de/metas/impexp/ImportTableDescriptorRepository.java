package de.metas.impexp;

import org.adempiere.ad.table.api.AdTableId;
import org.adempiere.ad.table.api.IADTableDAO;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_Issue;
import org.compiere.model.I_AD_Table;
import org.compiere.model.I_C_DataImport;
import org.compiere.model.I_I_ElementValue;
import org.compiere.model.I_I_Product;
import org.compiere.model.I_I_ReportLine;
import org.compiere.model.POInfo;
import org.springframework.stereotype.Repository;

import de.metas.cache.CCache;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2019 metas GmbH
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

@Repository
public class ImportTableDescriptorRepository
{
	private final CCache<AdTableId, ImportTableDescriptor> //
	importTableDescriptors = CCache.<AdTableId, ImportTableDescriptor> builder()
			.additionalTableNameToResetFor(I_AD_Table.Table_Name)
			.additionalTableNameToResetFor(I_AD_Column.Table_Name)
			.build();

	public ImportTableDescriptor getByTableId(@NonNull final AdTableId adTableId)
	{
		return importTableDescriptors.getOrLoad(adTableId, this::retrieveByTableId);
	}

	public ImportTableDescriptor getByTableName(@NonNull final String tableName)
	{
		final IADTableDAO adTablesRepo = Services.get(IADTableDAO.class);
		final AdTableId adTableId = AdTableId.ofRepoId(adTablesRepo.retrieveTableId(tableName));

		return getByTableId(adTableId);
	}

	private ImportTableDescriptor retrieveByTableId(@NonNull final AdTableId adTableId)
	{
		final POInfo poInfo = POInfo.getPOInfo(adTableId);
		Check.assumeNotNull(poInfo, "poInfo is not null for AD_Table_ID={}", adTableId);

		final String tableName = poInfo.getTableName();

		final String keyColumnName = poInfo.getKeyColumnName();
		if (keyColumnName == null)
		{
			throw new AdempiereException("Table " + tableName + " has not primary key");
		}

		final String dataImportConfigIdColumnName = poInfo.hasColumnName(I_C_DataImport.COLUMNNAME_C_DataImport_ID)
				? I_C_DataImport.COLUMNNAME_C_DataImport_ID
				: null;

		final String adIssueIdColumnName = poInfo.hasColumnName(I_AD_Issue.COLUMNNAME_AD_Issue_ID)
				? I_AD_Issue.COLUMNNAME_AD_Issue_ID
				: null;

		// Set Additional Table Info
		String tableUnique1 = "";
		String tableUnique2 = "";
		String tableUniqueParent = "";
		String tableUniqueChild = "";

		if (I_I_Product.Table_Name.equals(tableName))
		{
			tableUnique1 = I_I_Product.COLUMNNAME_UPC; // UPC = unique
			tableUnique2 = I_I_Product.COLUMNNAME_Value;
			tableUniqueParent = I_I_Product.COLUMNNAME_BPartner_Value; // Makes it unique
			tableUniqueChild = I_I_Product.COLUMNNAME_VendorProductNo; // Vendor No may not be unique !
		}
		else if (I_I_ElementValue.Table_Name.equals(tableName))
		{
			tableUniqueParent = I_I_ElementValue.COLUMNNAME_ElementName; // the parent key
			tableUniqueChild = I_I_ElementValue.COLUMNNAME_Value; // the key
		}
		else if (I_I_ReportLine.Table_Name.equals(tableName))
		{
			tableUniqueParent = I_I_ReportLine.COLUMNNAME_ReportLineSetName; // the parent key
			tableUniqueChild = I_I_ReportLine.COLUMNNAME_Name; // the key
		}

		return ImportTableDescriptor.builder()
				.tableName(tableName)
				.keyColumnName(keyColumnName)
				//
				.tableUnique1(tableUnique1)
				.tableUnique2(tableUnique2)
				.tableUniqueParent(tableUniqueParent)
				.tableUniqueChild(tableUniqueChild)
				//
				.dataImportConfigIdColumnName(dataImportConfigIdColumnName)
				.adIssueIdColumnName(adIssueIdColumnName)
				//
				.build();
	}

}
