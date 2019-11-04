package de.metas.report.jasper.client.process;

import javax.annotation.Nullable;

import org.springframework.stereotype.Component;

import de.metas.adempiere.report.jasper.OutputType;
import de.metas.process.ProcessInfo;
import de.metas.report.ExecuteReportStrategy;
import de.metas.report.jasper.client.JRClient;
import de.metas.util.Check;
import de.metas.util.lang.CoalesceUtil;
import lombok.NonNull;

/*
 * #%L
 * de.metas.swat.base
 * %%
 * Copyright (C) 2018 metas GmbH
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

/**
 * This is the default strategy that is always used, unless specified differently.
 * See {@link ExecuteReportStrategy} on how to invoke your on implementation.
 */
@Component
public class JasperExecuteReportStrategy implements ExecuteReportStrategy
{
	@Override
	public ExecuteReportResult executeReport(
			@NonNull final ProcessInfo processInfo,
			@Nullable final OutputType outputType)
	{
		final OutputType outputTypeEffective = Check.assumeNotNull(
				CoalesceUtil.coalesce(
						outputType,
						processInfo.getJRDesiredOutputType()),
				"From the given parameters, either outputType or processInfo.getJRDesiredOutputType() need to be non-null; processInfo={}",
				processInfo);

		final JRClient jrClient = JRClient.get();
		final byte[] reportData = jrClient.report(processInfo, outputTypeEffective);

		return new ExecuteReportResult(outputTypeEffective, reportData);
	}
}
