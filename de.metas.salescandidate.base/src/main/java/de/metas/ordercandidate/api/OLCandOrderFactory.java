package de.metas.ordercandidate.api;

import static org.adempiere.model.InterfaceWrapperHelper.create;
import static org.adempiere.model.InterfaceWrapperHelper.delete;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.save;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.FillMandatoryException;
import org.adempiere.mm.attributes.api.AttributeConstants;
import org.adempiere.mm.attributes.api.IAttributeSetInstanceAware;
import org.adempiere.mm.attributes.api.IAttributeSetInstanceAwareFactoryService;
import org.adempiere.mm.attributes.api.IAttributeSetInstanceBL;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.LegacyAdapters;
import org.adempiere.warehouse.WarehouseId;
import org.compiere.model.I_AD_Note;
import org.compiere.model.MNote;
import org.compiere.model.MOrderLine;
import org.compiere.model.X_C_Order;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.slf4j.Logger;

import de.metas.adempiere.model.I_AD_User;
import de.metas.adempiere.model.I_C_Order;
import de.metas.bpartner.BPartnerContactId;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.service.BPartnerInfo;
import de.metas.currency.CurrencyPrecision;
import de.metas.currency.ICurrencyDAO;
import de.metas.document.DocTypeId;
import de.metas.document.engine.DocStatus;
import de.metas.document.engine.IDocumentBL;
import de.metas.freighcost.FreightCostRule;
import de.metas.i18n.IMsgBL;
import de.metas.interfaces.I_C_OrderLine;
import de.metas.logging.LogManager;
import de.metas.money.CurrencyId;
import de.metas.order.DeliveryRule;
import de.metas.order.DeliveryViaRule;
import de.metas.order.IOrderLineBL;
import de.metas.order.InvoiceRule;
import de.metas.ordercandidate.model.I_C_OLCand;
import de.metas.ordercandidate.model.I_C_Order_Line_Alloc;
import de.metas.ordercandidate.spi.IOLCandListener;
import de.metas.payment.PaymentRule;
import de.metas.payment.paymentterm.PaymentTermId;
import de.metas.pricing.PricingSystemId;
import de.metas.pricing.attributebased.IAttributePricingBL;
import de.metas.pricing.attributebased.IProductPriceAware;
import de.metas.shipping.ShipperId;
import de.metas.user.UserId;
import de.metas.user.api.IUserDAO;
import de.metas.util.Check;
import de.metas.util.ILoggable;
import de.metas.util.Loggables;
import de.metas.util.Services;
import lombok.Builder;
import lombok.NonNull;

/*
 * #%L
 * de.metas.swat.base
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

class OLCandOrderFactory
{
	private static final Logger logger = LogManager.getLogger(OLCandOrderFactory.class);
	private final IUserDAO userDAO = Services.get(IUserDAO.class);
	private final IMsgBL msgBL = Services.get(IMsgBL.class);
	private final IDocumentBL documentBL = Services.get(IDocumentBL.class);
	private final IAttributeSetInstanceBL attributeSetInstanceBL = Services.get(IAttributeSetInstanceBL.class);
	private final IAttributeSetInstanceAwareFactoryService attributeSetInstanceAwareFactoryService = Services.get(IAttributeSetInstanceAwareFactoryService.class);
	private final IAttributePricingBL attributePricingBL = Services.get(IAttributePricingBL.class);
	private final IOrderLineBL orderLineBL = Services.get(IOrderLineBL.class);
	private final ICurrencyDAO currencyDAO = Services.get(ICurrencyDAO.class);

	private static final String MSG_OL_CAND_PROCESSOR_PROCESSING_ERROR_DESC_1P = "OLCandProcessor.ProcessingError_Desc";
	private static final String MSG_OL_CAND_PROCESSOR_ORDER_COMPLETION_FAILED_2P = "OLCandProcessor.Order_Completion_Failed";

	//
	// Parameters
	private final OLCandOrderDefaults orderDefaults;
	private final Properties ctx;
	private final UserId userInChargeId;
	private final ILoggable loggable;
	private final int olCandProcessorId;
	private final IOLCandListener olCandListeners;

	//
	private I_C_Order order;
	private I_C_OrderLine currentOrderLine = null;
	private final Map<Integer, I_C_OrderLine> orderLines = new LinkedHashMap<>();
	private final List<OLCand> candidates = new ArrayList<>();

	@Builder
	private OLCandOrderFactory(
			@NonNull final OLCandOrderDefaults orderDefaults,
			final int olCandProcessorId,
			final UserId userInChargeId,
			final ILoggable loggable,
			final IOLCandListener olCandListeners)
	{
		this.orderDefaults = orderDefaults;
		ctx = Env.getCtx();
		this.userInChargeId = userInChargeId != null ? userInChargeId : UserId.SYSTEM;
		this.loggable = loggable != null ? loggable : Loggables.nop();

		Check.assume(olCandProcessorId > 0, "olCandProcessorId > 0");
		this.olCandProcessorId = olCandProcessorId;

		this.olCandListeners = olCandListeners;

	}

	private I_C_Order newOrder(final OLCand candidateOfGroup)
	{
		final I_C_Order order = newInstance(I_C_Order.class);
		order.setDocStatus(DocStatus.Drafted.getCode());
		order.setDocAction(X_C_Order.DOCACTION_Complete);
	
		//
		// use values from orderDefaults when the order candidate doesn't have such values
		order.setC_DocTypeTarget_ID(DocTypeId.toRepoId(orderDefaults.getDocTypeTargetId()));
		order.setM_Warehouse_ID(WarehouseId.toRepoId(orderDefaults.getWarehouseId()));
	
		// use the values from 'olCand'
		order.setAD_Org_ID(candidateOfGroup.getAD_Org_ID());
	
		final BPartnerInfo bpartner = candidateOfGroup.getBPartnerInfo();
		order.setC_BPartner_ID(BPartnerId.toRepoId(bpartner.getBpartnerId()));
		order.setC_BPartner_Location_ID(BPartnerLocationId.toRepoId(bpartner.getBpartnerLocationId()));
		order.setAD_User_ID(BPartnerContactId.toRepoId(bpartner.getContactId()));
	
		// if the olc has no value set, we are not falling back here!
		final BPartnerInfo billBPartner = candidateOfGroup.getBillBPartnerInfo();
		order.setBill_BPartner_ID(BPartnerId.toRepoId(billBPartner.getBpartnerId()));
		order.setBill_Location_ID(BPartnerLocationId.toRepoId(billBPartner.getBpartnerLocationId()));
		order.setBill_User_ID(BPartnerContactId.toRepoId(billBPartner.getContactId()));
	
		final Timestamp dateDoc = TimeUtil.asTimestamp(candidateOfGroup.getDateDoc());
		order.setDateOrdered(dateDoc);
		order.setDateAcct(dateDoc);
	
		// task 06269 (see KurzBeschreibung)
		// note that C_Order.DatePromised is propagated to C_OrderLine.DatePromised in MOrder.afterSave() and MOrderLine.setOrder()
		// also note that for now we set datepromised only in the header, so different DatePromised values result in differnt orders, and all ol have the same datepromised
		order.setDatePromised(TimeUtil.asTimestamp(candidateOfGroup.getDatePromised()));
	
		// if the olc has no value set, we are not falling back here!
		// 05617
		final BPartnerInfo dropShipBPartner = candidateOfGroup.getDropShipBPartnerInfo();
		order.setDropShip_BPartner_ID(BPartnerId.toRepoId(dropShipBPartner.getBpartnerId()));
		order.setDropShip_Location_ID(BPartnerLocationId.toRepoId(dropShipBPartner.getBpartnerLocationId()));
		final boolean isDropShip = dropShipBPartner != null || dropShipBPartner.getBpartnerLocationId() != null;
		order.setIsDropShip(isDropShip);
	
		final BPartnerInfo handOverBPartner = candidateOfGroup.getHandOverBPartnerInfo();
		order.setHandOver_Partner_ID(BPartnerId.toRepoId(handOverBPartner.getBpartnerId()));
		order.setHandOver_Location_ID(BPartnerLocationId.toRepoId(handOverBPartner.getBpartnerLocationId()));
		order.setIsUseHandOver_Location(handOverBPartner.getBpartnerLocationId() != null);
	
		if (candidateOfGroup.getC_Currency_ID() > 0)
		{
			order.setC_Currency_ID(candidateOfGroup.getC_Currency_ID());
		}
	
		order.setPOReference(candidateOfGroup.getPOReference());
	
		order.setDeliveryRule(DeliveryRule.toCodeOrNull(candidateOfGroup.getDeliveryRule()));
		order.setDeliveryViaRule(DeliveryViaRule.toCodeOrNull(candidateOfGroup.getDeliveryViaRule()));
		order.setFreightCostRule(FreightCostRule.toCodeOrNull(candidateOfGroup.getFreightCostRule()));
		order.setInvoiceRule(InvoiceRule.toCodeOrNull(candidateOfGroup.getInvoiceRule()));
		order.setPaymentRule(PaymentRule.toCodeOrNull(candidateOfGroup.getPaymentRule()));
		order.setC_PaymentTerm_ID(PaymentTermId.toRepoId(candidateOfGroup.getPaymentTermId()));
		order.setM_PricingSystem_ID(PricingSystemId.toRepoId(candidateOfGroup.getPricingSystemId()));
		order.setM_Shipper_ID(ShipperId.toRepoId(candidateOfGroup.getShipperId()));
	
		// task 08926: set the data source; this shall trigger IsEdiEnabled to be set to true, if the data source is "EDI"
		final de.metas.order.model.I_C_Order orderWithDataSource = InterfaceWrapperHelper.create(order, de.metas.order.model.I_C_Order.class);
		orderWithDataSource.setAD_InputDataSource_ID(candidateOfGroup.getAD_InputDataSource_ID());
	
		save(order);
		return order;
	}

	public void completeOrDelete()
	{
		final I_C_Order order = this.order;
		if (order == null)
		{
			return;
		}
		else if (orderLines.isEmpty())
		{
			delete(order);
		}
		else
		{
			try
			{
				documentBL.processEx(order, X_C_Order.DOCACTION_Complete, DocStatus.Completed.getCode());
				save(order);

				loggable.addLog("@Created@ @C_Order_ID@ " + order.getDocumentNo());
			}
			catch (final Exception ex)
			{
				final String errorMsg = msgBL.getMsg(ctx, MSG_OL_CAND_PROCESSOR_ORDER_COMPLETION_FAILED_2P, new Object[] { order.getDocumentNo(), ex.getLocalizedMessage() });
				loggable.addLog(errorMsg);
				logger.warn("Caught exception while completing {}", order, ex);

				final I_AD_Note note = createOrderCompleteErrorNote(errorMsg);
				for (final OLCand candidate : candidates)
				{
					candidate.setError(errorMsg, note.getAD_Note_ID());
					InterfaceWrapperHelper.save(candidate);
				}
			}
		}
	}

	public void closeCurrentOrderLine()
	{
		if (currentOrderLine == null)
		{
			return;
		}

		save(currentOrderLine);
		currentOrderLine = null;
	}

	public void addOLCand(@NonNull final OLCand candidate)
	{
		try
		{
			addOLCand0(candidate);
			markAsProcessed(candidate);
		}
		catch (final Exception ex)
		{
			markAsError(candidate, ex);
		}
	}

	private void addOLCand0(@NonNull final OLCand candidate) throws Exception
	{
		if (currentOrderLine == null)
		{
			currentOrderLine = newOrderLine(candidate);
		}

		currentOrderLine.setM_Warehouse_Dest_ID(WarehouseId.toRepoId(candidate.getWarehouseDestId()));
		currentOrderLine.setProductDescription(candidate.getProductDescription()); // 08626: Propagate ProductDescription to C_OrderLine
		currentOrderLine.setLine(candidate.getLine());

		//
		// Quantity
		{
			final BigDecimal newQty = currentOrderLine.getQtyOrdered().add(candidate.getQty());
			currentOrderLine.setQtyEntered(newQty);
			currentOrderLine.setQtyOrdered(newQty);
		}

		//
		// Prices
		{
			currentOrderLine.setInvoicableQtyBasedOn(candidate.getInvoicableQtyBasedOn().getRecordString());

			currentOrderLine.setIsManualPrice(candidate.isManualPrice());
			if (candidate.isManualPrice())
			{
				currentOrderLine.setPriceEntered(candidate.getPriceActual());
			}
			else
			{
				// leave it to the pricing engine
			}
			currentOrderLine.setIsManualDiscount(candidate.isManualDiscount());
			if (candidate.isManualDiscount())
			{
				currentOrderLine.setDiscount(candidate.getDiscount());
			}
			if (candidate.isManualPrice() || candidate.isManualDiscount())
			{
				// FIXME: use price list's precision
				final CurrencyId currencyId = CurrencyId.ofRepoIdOrNull(candidate.getC_Currency_ID());
				final CurrencyPrecision stdPrecision = currencyId != null
						? currencyDAO.getStdPrecision(currencyId)
						: ICurrencyDAO.DEFAULT_PRECISION;
				orderLineBL.updatePriceActual(currentOrderLine, stdPrecision);
			}
		}

		//
		// Attach the olCand's IProductPriceAttributeAware to the new order line, so that the rest of the system can know if it is supposed to guess an ASI and PIIP or not (task 08839)
		{
			final IAttributeSetInstanceAware orderLineASIAware = attributeSetInstanceAwareFactoryService.createOrNull(currentOrderLine);
			Check.assumeNotNull(orderLineASIAware, "We can allways obtain a not-null ASI aware for C_OrderLine {} ", currentOrderLine);

			final IProductPriceAware productPriceAware = candidate;
			attributePricingBL.setDynAttrProductPriceAttributeAware(orderLineASIAware, productPriceAware);
		}

		//
		// Fire listeners
		olCandListeners.onOrderLineCreated(candidate, currentOrderLine);

		//
		// Save the current order line
		InterfaceWrapperHelper.save(currentOrderLine);

		// Establishing a "real" link with FK-constraints between order candidate and order line (03472)
		createOla(candidate, currentOrderLine);

		//
		orderLines.put(currentOrderLine.getC_OrderLine_ID(), currentOrderLine);
		candidates.add(candidate);
	}

	private I_C_OrderLine newOrderLine(@NonNull final OLCand candToProcess)
	{
		if (order == null)
		{
			order = newOrder(candToProcess);
		}

		final I_C_OrderLine orderLine = create(new MOrderLine(LegacyAdapters.convertToPO(order)), I_C_OrderLine.class);

		if (candToProcess.getC_Charge_ID() > 0)
		{
			orderLine.setC_Charge_ID(candToProcess.getC_Charge_ID());
		}
		else
		{
			final int productId = candToProcess.getM_Product_ID();
			if (productId <= 0)
			{
				throw new FillMandatoryException(I_C_OLCand.COLUMNNAME_M_Product_ID);
			}
			orderLine.setM_Product_ID(productId);
			orderLine.setC_UOM_ID(candToProcess.getC_UOM_ID());
			orderLine.setM_AttributeSetInstance_ID(AttributeConstants.M_AttributeSetInstance_ID_None);
		}

		// make sure that both records have their independent ASI to avoid unwanted side effects if the order line's ASI is altered.
		attributeSetInstanceBL.cloneASI(orderLine, candToProcess);

		orderLine.setPresetDateInvoiced(TimeUtil.asTimestamp(candToProcess.getPresetDateInvoiced()));
		orderLine.setPresetDateShipped(TimeUtil.asTimestamp(candToProcess.getPresetDateShipped()));

		return orderLine;
	}

	// 03472
	// Between C_OL_Cand and C_OrderLine I think we have an explicit AD_Relation. It should be replaced with a table
	// similar to C_Invoice_Line_Alloc, so that it is more transparent and we can define decent FK constraints. (and in
	// future we can have m:n if we want to)
	private void createOla(final OLCand candidate, final I_C_OrderLine orderLine)
	{
		// Check.assume(Env.getAD_Client_ID(ctx) == orderCand.getAD_Client_ID(), "AD_Client_ID of " + orderCand + " and of its CTX are the same");
		final int orderLineId = orderLine.getC_OrderLine_ID();

		final I_C_Order_Line_Alloc newOla = InterfaceWrapperHelper.newInstance(I_C_Order_Line_Alloc.class);
		newOla.setAD_Org_ID(candidate.getAD_Org_ID());

		newOla.setC_OLCand_ID(candidate.getId());
		newOla.setC_OrderLine_ID(orderLineId);
		newOla.setQtyOrdered(candidate.getQty());
		newOla.setC_OLCandProcessor_ID(olCandProcessorId);

		InterfaceWrapperHelper.save(newOla);
	}

	private void markAsProcessed(final OLCand olCand)
	{
		olCand.setProcessed(true);
		saveCandidate(olCand);
	}

	private void markAsError(final OLCand olCand, final Exception ex)
	{
		Loggables.addLog("Caught exception while processing {}; message={}; exception={}", olCand, ex.getLocalizedMessage(), ex);
		logger.warn("Caught exception while processing {}", olCand, ex);

		final I_AD_Note note = createOLCandErrorNote(olCand, ex);

		olCand.setError(ex.getLocalizedMessage(), note.getAD_Note_ID());
		saveCandidate(olCand);
	}

	private void saveCandidate(final OLCand cand)
	{
		save(cand.unbox());
	}

	private I_AD_Note createOrderCompleteErrorNote(final String errorMsg)
	{
		final I_AD_User user = userDAO.getById(userInChargeId);

		final String candidateIdsAsString = candidates.stream()
				.map(OLCand::getId)
				.map(String::valueOf)
				.collect(Collectors.joining(", "));
		final String adLanguage = user.getC_BPartner().getAD_Language();

		final MNote note = new MNote(ctx, IOLCandBL.MSG_OL_CAND_PROCESSOR_PROCESSING_ERROR_0P, userInChargeId.getRepoId(), ITrx.TRXNAME_None);
		note.setClientOrg(user.getAD_Client_ID(), user.getAD_Org_ID());
		note.setReference(errorMsg);
		note.setTextMsg(msgBL.getMsg(adLanguage, MSG_OL_CAND_PROCESSOR_PROCESSING_ERROR_DESC_1P, new Object[] { candidateIdsAsString }));
		save(note);

		return note;
	}

	private I_AD_Note createOLCandErrorNote(final OLCand olCand, final Exception ex)
	{
		final I_AD_User user = userDAO.getById(userInChargeId);

		final MNote note = new MNote(ctx, IOLCandBL.MSG_OL_CAND_PROCESSOR_PROCESSING_ERROR_0P, userInChargeId.getRepoId(), ITrx.TRXNAME_None);
		note.setRecord(olCand.toTableRecordReference());
		note.setClientOrg(user.getAD_Client_ID(), user.getAD_Org_ID());
		note.setTextMsg(ex.getLocalizedMessage());
		save(note);

		return note;
	}
}
