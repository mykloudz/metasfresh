package de.metas.handlingunits.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.service.ISysConfigBL;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;

import ch.qos.logback.classic.Level;
import de.metas.handlingunits.model.X_M_HU_PI_Version;
import de.metas.handlingunits.process.api.HUProcessDescriptor;
import de.metas.handlingunits.process.api.IMHUProcessDAO;
import de.metas.logging.LogManager;
import de.metas.process.AdProcessId;
import de.metas.util.ILoggable;
import de.metas.util.Loggables;
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

/**
 * This service facade offers useful methods around to handling unit related jasper report processed.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public class HUReportService
{
	public static HUReportService get()
	{
		return INSTANCE;
	}

	private static final transient Logger logger = LogManager.getLogger(HUReportService.class);

	public static final String SYSCONFIG_RECEIPT_LABEL_PROCESS_ID = "de.metas.handlingunits.MaterialReceiptLabel.AD_Process_ID";
	public static final String SYSCONFIG_RECEIPT_LABEL_AUTO_PRINT_ENABLED = "de.metas.handlingunits.MaterialReceiptLabel.AutoPrint.Enabled";
	public static final String SYSCONFIG_RECEIPT_LABEL_AUTO_PRINT_ENABLED_C_BPARTNER_ID = SYSCONFIG_RECEIPT_LABEL_AUTO_PRINT_ENABLED + ".C_BPartner_ID_";
	public static final String SYSCONFIG_RECEIPT_LABEL_AUTO_PRINT_COPIES = "de.metas.handlingunits.MaterialReceiptLabel.AutoPrint.Copies";

	// 895 webui
	public static final String SYSCONFIG_PICKING_LABEL_AUTO_PRINT_ENABLED = "de.metas.ui.web.picking.PickingLabel.AutoPrint.Enabled";
	public static final String SYSCONFIG_PICKING_LABEL_PROCESS_ID = "de.metas.ui.web.picking.PickingLabel.AD_Process_ID";
	public static final String SYSCONFIG_PICKING_LABEL_AUTO_PRINT_COPIES = "de.metas.ui.web.picking.PickingLabel.AutoPrint.Copies";

	private static final HUReportService INSTANCE = new HUReportService();

	private HUReportService()
	{
	}

	/**
	 * Return the AD_Process_ID that was configured via {@code SysConfig} {@value #SYSCONFIG_RECEIPT_LABEL_AUTO_PRINT_PROCESS_ID}, if any.
	 *
	 * @return AD_Process_ID or <code>-1</code>
	 */
	public AdProcessId retrievePrintReceiptLabelProcessIdOrNull()
	{
		return retrieveProcessIdBySysConfig(SYSCONFIG_RECEIPT_LABEL_PROCESS_ID);
	}

	private AdProcessId retrievePickingLabelProcessIdOrNull()
	{
		return retrieveProcessIdBySysConfig(SYSCONFIG_PICKING_LABEL_PROCESS_ID);
	}

	private AdProcessId retrieveProcessIdBySysConfig(final String sysConfigName)
	{
		final ISysConfigBL sysConfigBL = Services.get(ISysConfigBL.class);
		final Properties ctx = Env.getCtx();
		final int reportProcessId = sysConfigBL.getIntValue(sysConfigName, -1, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
		return AdProcessId.ofRepoIdOrNull(reportProcessId);
	}

	/**
	 * @return those HUs from the given {@code huToReport} what match the given {@code adProcessId}'s {@link HUProcessDescriptor}.
	 * If there is no {@link HUProcessDescriptor}, then it returns an empty list.
	 */
	public List<HUToReport> getHUsToProcess(
			@NonNull final HUToReport huToReport,
			@NonNull final AdProcessId adProcessId)
	{
		final IMHUProcessDAO huProcessDAO = Services.get(IMHUProcessDAO.class);
		final HUProcessDescriptor huProcessDescriptor = huProcessDAO.getByProcessIdOrNull(adProcessId);
		if (huProcessDescriptor == null)
		{
			return ImmutableList.of();
		}

		return huToReport.streamRecursively()
				.filter(currentHU -> currentHU.isTopLevel() || !huProcessDescriptor.isAcceptOnlyTopLevelHUs()) // if acceptOnlyTopLevelHUs then only accept topLevel-HUs
				.filter(currentHU -> huProcessDescriptor.appliesToHUUnitType(currentHU.getHUUnitType()))
				.collect(ImmutableList.toImmutableList());
	}

	public int getReceiptLabelAutoPrintCopyCount()
	{
		return getAutoPrintCopyCountForSysConfig(SYSCONFIG_RECEIPT_LABEL_AUTO_PRINT_COPIES);
	}

	public int getPickingLabelAutoPrintCopyCount()
	{
		return getAutoPrintCopyCountForSysConfig(SYSCONFIG_PICKING_LABEL_AUTO_PRINT_COPIES);
	}

	public int getAutoPrintCopyCountForSysConfig(final String sysConfigName)
	{
		final ISysConfigBL sysConfigBL = Services.get(ISysConfigBL.class);
		final Properties ctx = Env.getCtx();
		final int copies = sysConfigBL.getIntValue(sysConfigName, 1, Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
		return copies;
	}

	/**
	 * Checks the sysconfig and returns true or if receipt label auto printing is enabled in general or for the given HU's C_BPartner_ID.
	 *
	 * @param vendorBPartnerId the original vendor. By now, might not be the same as M_HU.C_BPartner_ID anymore
	 * @return
	 */
	public boolean isReceiptLabelAutoPrintEnabled(final int vendorBPartnerId)
	{
		final ISysConfigBL sysConfigBL = Services.get(ISysConfigBL.class);
		final Properties ctx = Env.getCtx();

		final String vendorSysconfigName = SYSCONFIG_RECEIPT_LABEL_AUTO_PRINT_ENABLED_C_BPARTNER_ID + vendorBPartnerId;
		final String valueForBPartner = sysConfigBL.getValue(vendorSysconfigName, "NOT_SET", Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
		logger.info("SysConfig {}={};", vendorSysconfigName, valueForBPartner);

		if (!"NOT_SET".equals(valueForBPartner))
		{
			return DisplayType.toBoolean(valueForBPartner, false);
		}

		final String genericValue = sysConfigBL.getValue(SYSCONFIG_RECEIPT_LABEL_AUTO_PRINT_ENABLED, "N", Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
		logger.info("SysConfig {}={};", SYSCONFIG_RECEIPT_LABEL_AUTO_PRINT_ENABLED, genericValue);

		return DisplayType.toBoolean(genericValue, false);
	}

	public boolean isPickingLabelAutoPrintEnabled()
	{
		final Properties ctx = Env.getCtx();

		final ISysConfigBL sysConfigBL = Services.get(ISysConfigBL.class);

		final String genericValue = sysConfigBL.getValue(SYSCONFIG_PICKING_LABEL_AUTO_PRINT_ENABLED, "N", Env.getAD_Client_ID(ctx), Env.getAD_Org_ID(ctx));
		logger.info("SysConfig {}={};", SYSCONFIG_PICKING_LABEL_AUTO_PRINT_ENABLED, genericValue);

		return DisplayType.toBoolean(genericValue, false);
	}

	public List<HUToReport> getHUsToProcess(@NonNull final Set<HUToReport> husToCheck)
	{
		if (husToCheck.isEmpty())
		{
			return ImmutableList.of();
		}

		final List<HUToReport> tuHUs = new ArrayList<>();
		final List<HUToReport> luHUs = new ArrayList<>();
		for (final HUToReport hu : husToCheck)
		{
			final String huUnitType = hu.getHUUnitType();

			// BL NOT IMPLEMENTED YET FOR VIRTUAL PI REPORTS, because we don't have any
			if (X_M_HU_PI_Version.HU_UNITTYPE_VirtualPI.equals(huUnitType))
			{
				continue;
			}

			if (X_M_HU_PI_Version.HU_UNITTYPE_LoadLogistiqueUnit.equals(huUnitType))
			{
				luHUs.add(hu);
			}
			else if (X_M_HU_PI_Version.HU_UNITTYPE_TransportUnit.equals(huUnitType))
			{
				tuHUs.add(hu);
			}
			else
			{
				throw new AdempiereException("Invalid unit type: " + huUnitType);
			}
		}

		final String huUnitTypeToReport;
		if (!tuHUs.isEmpty())
		{
			huUnitTypeToReport = X_M_HU_PI_Version.HU_UNITTYPE_TransportUnit;
		}
		else if (!luHUs.isEmpty())
		{
			huUnitTypeToReport = X_M_HU_PI_Version.HU_UNITTYPE_LoadLogistiqueUnit;
		}
		else
		{
			huUnitTypeToReport = X_M_HU_PI_Version.HU_UNITTYPE_VirtualPI;
		}

		if (!luHUs.isEmpty() || !tuHUs.isEmpty())
		{
			return extractHUsToProcess(huUnitTypeToReport, luHUs, tuHUs);
		}
		else
		{
			return ImmutableList.of();
		}
	}

	/**
	 * In case at least one TU was selected, we will deliver the processes for TUs.
	 *
	 * This will happen even though we have, for instance, just 1 TU and some LUs selected.
	 *
	 * The HUs to have the processes applied will be the 1 TU and the included TUs of the selected LUs
	 *
	 * @param huUnitType
	 * @param luHUs
	 * @param tuHUs
	 */
	private static List<HUToReport> extractHUsToProcess(final String huUnitType, final List<HUToReport> luHUs, final List<HUToReport> tuHUs)
	{
		// In case the unit type is Virtual PI we don't have to return anything, since we don't have processes for virtual PIs
		if (X_M_HU_PI_Version.HU_UNITTYPE_VirtualPI.equals(huUnitType))
		{
			return ImmutableList.of();
		}

		// In case we the unit type is LU we just have to process the LUs
		else if (X_M_HU_PI_Version.HU_UNITTYPE_LoadLogistiqueUnit.equals(huUnitType))
		{
			return luHUs;
		}

		// In case the unit type is TU we have 2 possibilities:
		else
		{
			// In case the are no selected LUs, simply return the TUs
			if (luHUs.isEmpty())
			{
				return tuHUs;
			}
			// if this point is reached, it means we have both TUs and LUs selected
			else
			{
				final ImmutableList.Builder<HUToReport> husToProcess = ImmutableList.builder();

				// first, add all the selected TUs
				husToProcess.addAll(tuHUs);

				for (final HUToReport lu : luHUs)
				{
					final List<HUToReport> includedHUs = lu.getIncludedHUs();
					husToProcess.addAll(includedHUs);
				}

				return husToProcess.build();
			}
		}
	}

	/**
	 * @param onlyIfAutoPrintIsEnabled if {@code false}, then do the printing even if {@link #isPickingLabelAutoPrintEnabled()} is {@code false}.
	 */
	public void printPickingLabel(@Nullable final HUToReportWrapper huToReport, final boolean onlyIfAutoPrintIsEnabled)
	{
		final ILoggable loggable = Loggables.get().withLogger(logger, Level.INFO);

		if (huToReport == null)
		{
			loggable.addLog("Param 'huToReport'==null; nothing to do");
			return;
		}

		if (onlyIfAutoPrintIsEnabled && !isPickingLabelAutoPrintEnabled())
		{
			loggable.addLog("Auto printing receipt labels is not enabled via SysConfig; nothing to do");
			return;
		}

		if (!huToReport.isTopLevel())
		{
			loggable.addLog("We only print top level HUs; nothing to do; huToReport={}", huToReport);
			return;
		}

		final AdProcessId adProcessId = retrievePickingLabelProcessIdOrNull();
		if (adProcessId == null)
		{
			loggable.addLog("No process configured via SysConfig {}; nothing to do", SYSCONFIG_PICKING_LABEL_PROCESS_ID);
			return;
		}

		final List<HUToReport> husToProcess = getHUsToProcess(huToReport, adProcessId);
		if (husToProcess.isEmpty())
		{
			loggable.addLog("The selected hu does not match AD_Process_ID={}; nothing to do; huToReport={}", adProcessId, huToReport);
			return;
		}

		final int copies = getPickingLabelAutoPrintCopyCount();

		loggable.addLog("Going to invoke HUReportExecutor to run AD_Process_ID={} with copies={} on husToProcess={}", adProcessId, copies, husToProcess);

		final Properties ctx = Env.getCtx();
		HUReportExecutor.newInstance(ctx)
				.numberOfCopies(copies)
				.executeHUReportAfterCommit(adProcessId, husToProcess);
	}

}
