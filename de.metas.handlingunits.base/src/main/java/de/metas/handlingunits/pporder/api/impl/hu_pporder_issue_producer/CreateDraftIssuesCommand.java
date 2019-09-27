package de.metas.handlingunits.pporder.api.impl.hu_pporder_issue_producer;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.compiere.util.TimeUtil;
import org.eevolution.model.I_PP_Order_BOMLine;
import org.eevolution.model.X_PP_Order_BOMLine;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;

import de.metas.handlingunits.IHUContext;
import de.metas.handlingunits.IHUStatusBL;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.IHandlingUnitsDAO;
import de.metas.handlingunits.attribute.IPPOrderProductAttributeBL;
import de.metas.handlingunits.exceptions.HUException;
import de.metas.handlingunits.hutransaction.IHUTrxBL;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.model.I_PP_Order_Qty;
import de.metas.handlingunits.model.X_M_HU;
import de.metas.handlingunits.pporder.api.IHUPPOrderQtyDAO;
import de.metas.handlingunits.storage.IHUProductStorage;
import de.metas.logging.LogManager;
import de.metas.material.planning.pporder.IPPOrderBOMBL;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.time.SystemTime;
import lombok.Builder;
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
 * Creates Draft Issue Candidates
 */
public class CreateDraftIssuesCommand
{
	private static final String MSG_IssuingAggregatedTUsNotAllowed = "de.metas.handlingunits.pporder.api.impl.HUPPOrderIssueProducer.IssuingAggregatedTUsNotAllowed";
	private static final String MSG_IssuingVHUsNotAllowed = "de.metas.handlingunits.pporder.api.impl.HUPPOrderIssueProducer.IssuingVHUsNotAllowed";
	private static final String MSG_IssuingHUWithMultipleProductsNotAllowed = "de.metas.handlingunits.pporder.api.impl.HUPPOrderIssueProducer.IssuingHUsWithMultipleProductsNotAllowed";

	//
	// Services
	private static final Logger logger = LogManager.getLogger(CreateDraftIssuesCommand.class);
	private final transient IPPOrderBOMBL ppOrderBOMBL = Services.get(IPPOrderBOMBL.class);
	private final transient IHUTrxBL huTrxBL = Services.get(IHUTrxBL.class);
	private final transient IPPOrderProductAttributeBL ppOrderProductAttributeBL = Services.get(IPPOrderProductAttributeBL.class);
	private final transient IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);
	private final transient IHandlingUnitsDAO handlingUnitsDAO = Services.get(IHandlingUnitsDAO.class);
	private final transient IHUStatusBL huStatusBL = Services.get(IHUStatusBL.class);
	private final transient IHUPPOrderQtyDAO huPPOrderQtyDAO = Services.get(IHUPPOrderQtyDAO.class);

	private final ImmutableList<I_PP_Order_BOMLine> targetOrderBOMLines;
	private final LocalDate movementDate;
	private final boolean considerIssueMethodForQtyToIssueCalculation;
	private final ImmutableList<I_M_HU> hus;

	@Builder
	private CreateDraftIssuesCommand(
			final @NonNull List<I_PP_Order_BOMLine> targetOrderBOMLines,
			final @Nullable LocalDate movementDate,
			final boolean considerIssueMethodForQtyToIssueCalculation,
			@NonNull final Collection<I_M_HU> hus)
	{
		Check.assumeNotEmpty(targetOrderBOMLines, "Parameter targetOrderBOMLines is not empty");

		this.targetOrderBOMLines = ImmutableList.copyOf(targetOrderBOMLines);
		this.movementDate = movementDate != null ? movementDate : SystemTime.asLocalDate();
		this.considerIssueMethodForQtyToIssueCalculation = considerIssueMethodForQtyToIssueCalculation;
		this.hus = ImmutableList.copyOf(hus);
	}

	public List<I_PP_Order_Qty> execute()
	{
		if (hus.isEmpty())
		{
			return ImmutableList.of();
		}

		// NOTE: we would prefer to always run this out of transactions,
		// but in some cases like issuing from Swing POS we cannot enforce it because in that case
		// the candidates are created and processed in one uber-transaction
		// trxManager.assertThreadInheritedTrxNotExists();

		final List<I_PP_Order_Qty> candidates = huTrxBL.process(huContext -> {
			return hus.stream()
					.map(hu -> createCreateDraftIssue_InTrx(huContext, hu))
					.filter(issueCandidate -> issueCandidate != null)
					.collect(ImmutableList.toImmutableList());
		});
		return candidates;
	}

	private I_PP_Order_Qty createCreateDraftIssue_InTrx(
			@NonNull final IHUContext huContext,
			@NonNull final I_M_HU hu)
	{
		if (!X_M_HU.HUSTATUS_Active.equals(hu.getHUStatus()))
		{
			throw new HUException("Parameter 'hu' needs to have the status \"active\", but has HUStatus=" + hu.getHUStatus())
					.setParameter("hu", hu);
		}

		removeHuFromParentIfAny(huContext, hu);

		final IHUProductStorage productStorage = retrieveProductStorage(huContext, hu);
		if (productStorage == null)
		{
			return null;
		}

		// Actually create and save the candidate
		final I_PP_Order_Qty candidate = createIssueCandidate(hu, productStorage);

		// update the HU's status so that it's not moved somewhere else etc
		huStatusBL.setHUStatus(huContext, hu, X_M_HU.HUSTATUS_Issued);
		handlingUnitsDAO.saveHU(hu);

		return candidate;
	}

	/**
	 * If not a top level HU, take it out first
	 *
	 * @param huContext
	 * @param hu
	 */
	private void removeHuFromParentIfAny(
			@NonNull final IHUContext huContext,
			@NonNull final I_M_HU hu)
	{
		if (handlingUnitsBL.isTopLevel(hu))
		{
			return; // nothing to do
		}

		if (handlingUnitsBL.isAggregateHU(hu))
		{
			throw HUException.ofAD_Message(MSG_IssuingAggregatedTUsNotAllowed);
		}
		if (handlingUnitsBL.isVirtual(hu))
		{
			throw HUException.ofAD_Message(MSG_IssuingVHUsNotAllowed);
		}
		else
		{
			huTrxBL.setParentHU(huContext //
					, null // parentHUItem
					, hu //
					, true // destroyOldParentIfEmptyStorage
			);
		}
	}

	private IHUProductStorage retrieveProductStorage(
			@NonNull final IHUContext huContext,
			@NonNull final I_M_HU hu)
	{
		final List<IHUProductStorage> productStorages = huContext.getHUStorageFactory()
				.getStorage(hu)
				.getProductStorages();

		// Empty HU
		if (productStorages.isEmpty())
		{
			logger.warn("{}: Skip {} from issuing because its storage is empty", this, hu);
			return null; // no candidate
		}

		if (productStorages.size() != 1)
		{
			throw HUException.ofAD_Message(MSG_IssuingHUWithMultipleProductsNotAllowed)
					.setParameter("HU", hu)
					.setParameter("ProductStorages", productStorages);
		}
		final IHUProductStorage productStorage = productStorages.get(0);
		return productStorage;
	}

	private I_PP_Order_Qty createIssueCandidate(
			@NonNull final I_M_HU hu,
			@NonNull final IHUProductStorage productStorage)
	{
		final ProductId productId = productStorage.getProductId();
		final I_PP_Order_BOMLine targetBOMLine = getTargetOrderBOMLine(productId);

		final I_PP_Order_Qty candidate = newInstance(I_PP_Order_Qty.class);

		candidate.setPP_Order_ID(targetBOMLine.getPP_Order_ID());
		candidate.setPP_Order_BOMLine(targetBOMLine);

		candidate.setM_Locator_ID(hu.getM_Locator_ID());
		candidate.setM_HU_ID(hu.getM_HU_ID());
		candidate.setM_Product_ID(productId.getRepoId());

		final Quantity qtyToIssue = calculateQtyToIssue(targetBOMLine, productStorage)
				.switchToSourceIfMorePrecise();
		candidate.setQty(qtyToIssue.toBigDecimal());
		candidate.setC_UOM_ID(qtyToIssue.getUOMId());

		candidate.setMovementDate(TimeUtil.asTimestamp(movementDate));
		candidate.setProcessed(false);
		huPPOrderQtyDAO.save(candidate);

		ppOrderProductAttributeBL.addPPOrderProductAttributesFromIssueCandidate(candidate);
		return candidate;
	}

	private I_PP_Order_BOMLine getTargetOrderBOMLine(@NonNull final ProductId productId)
	{
		final List<I_PP_Order_BOMLine> targetBOMLines = targetOrderBOMLines;
		return targetBOMLines
				.stream()
				.filter(bomLine -> bomLine.getM_Product_ID() == productId.getRepoId())
				.findFirst()
				.orElseThrow(() -> new HUException("No BOM line found for productId=" + productId + " in " + targetBOMLines));
	}

	/** @return how much quantity to take "from" and issue it to given BOM line */
	private Quantity calculateQtyToIssue(final I_PP_Order_BOMLine targetBOMLine, final IHUProductStorage from)
	{
		if (considerIssueMethodForQtyToIssueCalculation)
		{
			//
			// Case: if this is an Issue BOM Line, IssueMethod is Backflush and we did not over-issue on it yet
			// => enforce the capacity to Projected Qty Required (i.e. standard Qty that needs to be issued on this line).
			// initial concept: http://dewiki908/mediawiki/index.php/07433_Folie_Zuteilung_Produktion_Fertigstellung_POS_%28102170996938%29
			// additional (use of projected qty required): http://dewiki908/mediawiki/index.php/07601_Calculation_of_Folie_in_Action_Receipt_%28102017845369%29
			final String issueMethod = targetBOMLine.getIssueMethod();
			if (X_PP_Order_BOMLine.ISSUEMETHOD_IssueOnlyForReceived.equals(issueMethod))
			{
				return ppOrderBOMBL.calculateQtyToIssueBasedOnFinishedGoodReceipt(targetBOMLine, from.getC_UOM());
			}
		}

		return from.getQty();
	}
}
