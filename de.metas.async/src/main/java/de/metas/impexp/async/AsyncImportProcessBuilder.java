package de.metas.impexp.async;

import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;

import de.metas.async.processor.IWorkPackageQueueFactory;
import de.metas.impexp.processing.IImportProcess;
import de.metas.impexp.processing.spi.IAsyncImportProcessBuilder;
import de.metas.process.PInstanceId;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

/**
 * {@link IAsyncImportProcessBuilder} implementation which creates and enqueues an {@link AsyncImportWorkpackageProcessor} workpackage.
 *
 * @author tsa
 *
 */
final class AsyncImportProcessBuilder implements IAsyncImportProcessBuilder
{
	private final IWorkPackageQueueFactory workPackageQueueFactory = Services.get(IWorkPackageQueueFactory.class);

	private String _importTableName;
	private PInstanceId importRecordsSelectionId = null;

	private boolean validateOnly = false;
	private boolean insertOnly = false;
	private boolean deleteOldImported = false;
	private boolean completeDocuments = false;

	private Properties _ctx;

	AsyncImportProcessBuilder()
	{
	}

	@Override
	public void buildAndEnqueue()
	{
		final Properties ctx = getCtx();
		final String importTableName = getImportTableName();

		if (importRecordsSelectionId == null)
		{
			throw new AdempiereException("No import records provided");
		}

		workPackageQueueFactory
				.getQueueForEnqueuing(ctx, AsyncImportWorkpackageProcessor.class)
				.newBlock()
				.setContext(ctx)
				.newWorkpackage()
				.parameters()
				.setParameter(IImportProcess.PARAM_IsDocComplete, completeDocuments)
				.setParameter(AsyncImportWorkpackageProcessor.PARAM_ImportTableName, importTableName)
				.setParameter(IImportProcess.PARAM_DeleteOldImported, deleteOldImported)
				.setParameter(IImportProcess.PARAM_IsValidateOnly, validateOnly)
				.setParameter(IImportProcess.PARAM_IsInsertOnly, insertOnly)
				.setParameter(AsyncImportWorkpackageProcessor.PARAM_Selection_ID, importRecordsSelectionId)
				.end()
				//
				.build();

	}

	@Override
	public IAsyncImportProcessBuilder setCtx(@NonNull final Properties ctx)
	{
		this._ctx = ctx;
		return this;
	}

	private Properties getCtx()
	{
		Check.assumeNotNull(_ctx, "_ctx not null");
		return _ctx;
	}

	@Override
	public IAsyncImportProcessBuilder setImportTableName(@NonNull final String importTableName)
	{
		Check.assumeNull(this._importTableName, "importTableName not already configured");
		Check.assumeNotEmpty(importTableName, "importTableName not empty");
		this._importTableName = importTableName;
		return this;
	}

	private String getImportTableName()
	{
		Check.assumeNotEmpty(_importTableName, "importTableName not empty");
		return _importTableName;
	}

	@Override
	public IAsyncImportProcessBuilder setImportFromSelectionId(@NonNull final PInstanceId fromSelectionId)
	{
		this.importRecordsSelectionId = fromSelectionId;
		return this;
	}

	@Override
	public IAsyncImportProcessBuilder setCompleteDocuments(final boolean completeDocuments)
	{
		this.completeDocuments = completeDocuments;
		return this;
	}

	@Override
	public IAsyncImportProcessBuilder setValidateOnly(final boolean validateOnly)
	{
		this.validateOnly = validateOnly;
		return this;
	}

	@Override
	public IAsyncImportProcessBuilder setInsertOnly(final boolean insertOnly)
	{
		this.insertOnly = insertOnly;
		return this;
	}

	@Override
	public IAsyncImportProcessBuilder setDeleteOldImported(final boolean deleteOldImported)
	{
		this.deleteOldImported = deleteOldImported;
		return this;
	}
}
