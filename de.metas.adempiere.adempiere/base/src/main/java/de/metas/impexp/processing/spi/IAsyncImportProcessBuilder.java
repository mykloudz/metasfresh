package de.metas.impexp.processing.spi;

import java.util.Collection;

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

import org.adempiere.util.lang.impl.TableRecordReference;

import de.metas.impexp.processing.IImportProcess;

/**
 * Builds an {@link IImportProcess} instance and executes it asynchronously.
 * 
 * @author tsa
 *
 */
public interface IAsyncImportProcessBuilder
{
	/**
	 * Builds the {@link IImportProcess} and starts it asynchronously.
	 * 
	 * This method will return directly and it will not wait for the actual import process to finish.
	 */
	void buildAndEnqueue();

	IAsyncImportProcessBuilder setCtx(Properties ctx);

	/**
	 * Sets the import table name (the source).
	 * 
	 * @param tableName import table name (e.g. I_BPartner).
	 */
	IAsyncImportProcessBuilder setImportTableName(String tableName);

	/**
	 * Enqueues an import table record that needs to be imported.
	 */
	IAsyncImportProcessBuilder addImportRecord(TableRecordReference importRecordRef);

	/**
	 * Enqueues import table records that needs to be imported.
	 */
	IAsyncImportProcessBuilder addImportRecords(Collection<TableRecordReference> importRecordRefs);
}
