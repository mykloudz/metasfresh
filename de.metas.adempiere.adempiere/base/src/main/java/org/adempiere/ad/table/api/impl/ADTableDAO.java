package org.adempiere.ad.table.api.impl;

import static org.adempiere.model.InterfaceWrapperHelper.createOld;
import static org.adempiere.model.InterfaceWrapperHelper.getCtx;
import static org.adempiere.model.InterfaceWrapperHelper.loadOutOfTrx;
import static org.adempiere.model.InterfaceWrapperHelper.translate;

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

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.dao.impl.UpperCaseQueryFilterModifier;
import org.adempiere.ad.service.ISequenceDAO;
import org.adempiere.ad.table.api.AdTableId;
import org.adempiere.ad.table.api.IADTableDAO;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_Element;
import org.compiere.model.I_AD_Table;
import org.compiere.model.I_AD_Window;
import org.compiere.model.MTable;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.metas.document.DocumentConstants;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

public class ADTableDAO implements IADTableDAO
{
	private static final ImmutableSet<String> STANDARD_COLUMN_NAMES = ImmutableSet.of(
			"AD_Client_ID", "AD_Org_ID",
			"IsActive",
			"Created", "CreatedBy",
			"Updated", "UpdatedBy");

	@Override
	public I_AD_Column retrieveColumn(final String tableName, final String columnName)
	{
		final I_AD_Column column = retrieveColumnOrNull(tableName, columnName);
		if (column == null)
		{
			throw new AdempiereException("@NotFound@ @AD_Column_ID@ " + columnName + " (@AD_Table_ID@=" + tableName + ")");
		}
		return column;
	}

	@Override
	public I_AD_Column retrieveColumnOrNull(final String tableName, final String columnName)
	{
		final IQueryBuilder<I_AD_Column> queryBuilder = retrieveColumnQueryBuilder(tableName, columnName, ITrx.TRXNAME_ThreadInherited);
		return queryBuilder.create()
				.setOnlyActiveRecords(true)
				.firstOnly(I_AD_Column.class);
	}

	@Override
	public boolean hasColumnName(final String tableName, final String columnName)
	{
		final IQueryBuilder<I_AD_Column> queryBuilder = retrieveColumnQueryBuilder(tableName, columnName, ITrx.TRXNAME_None);
		return queryBuilder.create()
				.setOnlyActiveRecords(true)
				.match();
	}

	@Override
	public IQueryBuilder<I_AD_Column> retrieveColumnQueryBuilder(final String tableName,
			final String columnName,
			final String trxName)
	{
		final String trxNametoUse = trxName == null ? ITrx.TRXNAME_None : trxName;

		//
		// Create queryBuilder with default context (not needed for tables)
		final IQueryBuilder<I_AD_Column> queryBuilder = Services.get(IQueryBL.class)
				.createQueryBuilder(I_AD_Column.class, Env.getCtx(), trxNametoUse);

		//
		// Filter by tableName
		queryBuilder.addEqualsFilter(I_AD_Column.COLUMNNAME_AD_Table_ID, retrieveTableId(tableName));
		//
		// Filter by columnName
		queryBuilder.addEqualsFilter(I_AD_Column.COLUMNNAME_ColumnName, columnName, UpperCaseQueryFilterModifier.instance);

		return queryBuilder;
	}

	@Override
	public String retrieveColumnName(final int adColumnId)
	{
		return DB.getSQLValueStringEx(ITrx.TRXNAME_None, "SELECT ColumnName FROM AD_Column WHERE AD_Column_ID=?", adColumnId);
	}

	@Override
	public String retrieveTableName(@NonNull final AdTableId adTableId)
	{
		final Properties ctx = Env.getCtx();
		@SuppressWarnings("deprecation")
		final String tableName = MTable.getTableName(ctx, adTableId.getRepoId());
		return tableName;
	}

	@Override
	public int retrieveTableId(final String tableName)
	{
		// NOTE: make sure we are returning -1 in case tableName was not found (and NOT throw exception),
		// because there is business logic which depends on this

		@SuppressWarnings("deprecation")
		// TODO move getTable_ID out of MTable
		final int tableId = MTable.getTable_ID(tableName);

		return tableId;
	}

	@Override
	public boolean isExistingTable(final String tableName)
	{
		if (Check.isEmpty(tableName, true))
		{
			return false;
		}
		return retrieveTableId(tableName) > 0;
	}

	@Override
	public boolean isTableId(final String tableName, final int adTableId)
	{
		if (adTableId <= 0)
		{
			return false;
		}
		if (Check.isEmpty(tableName))
		{
			return false;
		}
		return adTableId == retrieveTableId(tableName);
	}

	@Override
	public List<I_AD_Table> retrieveAllTables(final Properties ctx, final String trxName)
	{
		final IQueryBuilder<I_AD_Table> queryBuilder = Services.get(IQueryBL.class)
				.createQueryBuilder(I_AD_Table.class, ctx, trxName);

		queryBuilder.orderBy()
				.addColumn(I_AD_Table.COLUMNNAME_TableName);

		return queryBuilder.create()
				.list();
	}

	@Override
	public boolean isVirtualColumn(final I_AD_Column column)
	{
		final String s = column.getColumnSQL();
		return !Check.isEmpty(s, true);
	}

	@Override
	public String retrieveWindowName(final Properties ctx, final String tableName)
	{
		// NOTE: atm we use MTable.get because that's the only place where we have the table cached.
		// In future we shall replace it with something which is database independent.
		final I_AD_Table adTable = MTable.get(ctx, tableName);
		if (adTable == null)
		{
			return "";
		}
		final I_AD_Window adWindow = adTable.getAD_Window();
		if (adWindow == null)
		{
			return "";
		}
		final I_AD_Window adWindowTrl = translate(adWindow, I_AD_Window.class);
		return adWindowTrl.getName();
	}

	@Override
	public void onTableNameRename(@NonNull final I_AD_Table table)
	{
		final I_AD_Table tableOld = createOld(table, I_AD_Table.class);
		final String tableNameOld = tableOld.getTableName();
		final String tableNameNew = table.getTableName();

		// Do nothing if the table name was not actually changed
		if (Objects.equals(tableNameOld, tableNameNew))
		{
			return;
		}

		final Properties ctx = getCtx(table);
		Services.get(ISequenceDAO.class).renameTableSequence(ctx, tableNameOld, tableNameNew);
	}

	@Override
	public I_AD_Element retrieveElement(final String columnName)
	{
		final IQueryBL queryBL = Services.get(IQueryBL.class);

		final I_AD_Element element = queryBL.createQueryBuilder(I_AD_Element.class, Env.getCtx(), ITrx.TRXNAME_None)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_AD_Element.COLUMNNAME_ColumnName, columnName)
				.create()
				.firstOnly(I_AD_Element.class);
		return element;
	}

	@Override
	public I_AD_Table retrieveTable(final String tableName)
	{
		@SuppressWarnings("deprecation")
		final int tableID = MTable.getTable_ID(tableName);
		return loadOutOfTrx(tableID, I_AD_Table.class); // load out of trx to benefit from caching
	}


	@Override
	public I_AD_Table retrieveTable(@NonNull final AdTableId tableId)
	{
		return loadOutOfTrx(tableId, I_AD_Table.class); // load out of trx to benefit from caching
	}

	@Override
	public I_AD_Table retrieveTableOrNull(@Nullable final String tableName)
	{
		@SuppressWarnings("deprecation")
		final int tableID = MTable.getTable_ID(tableName);
		if (tableID <= 0)
		{
			return null;
		}
		return loadOutOfTrx(tableID, I_AD_Table.class); // load out of trx to benefit from caching
	}

	@Override
	public List<I_AD_Column> retrieveColumnsForTable(final I_AD_Table table)
	{
		final IQueryBL queryBL = Services.get(IQueryBL.class);

		return queryBL.createQueryBuilder(I_AD_Column.class, table)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_AD_Column.COLUMNNAME_AD_Table_ID, table.getAD_Table_ID())
				.orderBy()
				.addColumnAscending(I_AD_Column.COLUMNNAME_AD_Column_ID)
				.endOrderBy()
				.create()
				.list();
	}

	@Override
	public I_AD_Table retrieveDocumentTableTemplate(final I_AD_Table targetTable)
	{
		return retrieveTable(DocumentConstants.AD_TABLE_Document_Template_TableName);
	}

	@Override
	public boolean isStandardColumn(final String columnName)
	{
		return STANDARD_COLUMN_NAMES.contains(columnName);
	}

	@Override
	public Set<String> getTableNamesWithRemoteCacheInvalidation()
	{
		final List<String> tableNames = Services.get(IQueryBL.class)
				.createQueryBuilder(I_AD_Table.Table_Name)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_AD_Table.COLUMNNAME_IsEnableRemoteCacheInvalidation, true)
				.orderBy(I_AD_Table.COLUMNNAME_TableName)
				.create()
				.listDistinct(I_AD_Table.COLUMNNAME_TableName, String.class);
		return ImmutableSet.copyOf(tableNames);
	}

	@Override
	public int getTypeaheadMinLength(@NonNull final String tableName)
	{
		final I_AD_Table table = retrieveTable(tableName);
		final int typeaheadMinLength = table.getACTriggerLength();
		return typeaheadMinLength > 0 ? typeaheadMinLength : 0;
	}

	@Override
	public List<I_AD_Table> retrieveAllImportTables()
	{
		return Services.get(IQueryBL.class)
				.createQueryBuilderOutOfTrx(I_AD_Table.class)
				.addOnlyActiveRecordsFilter()
				.addStringLikeFilter(I_AD_Table.COLUMNNAME_TableName, "I_%", /* ignore case */false)
				.orderBy(I_AD_Table.COLUMNNAME_TableName)
				.create()
				.stream()
				.filter(table -> table.getTableName().startsWith("I_")) // required because "I_%" could match "IMP_blabla" too
				.collect(ImmutableList.toImmutableList());
	}
}
