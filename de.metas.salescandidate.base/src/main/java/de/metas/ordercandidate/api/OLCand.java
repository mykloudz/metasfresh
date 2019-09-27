package de.metas.ordercandidate.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import javax.annotation.Nullable;

import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.adempiere.warehouse.WarehouseId;
import org.compiere.util.TimeUtil;

import com.google.common.base.MoreObjects;

import de.metas.bpartner.service.BPartnerInfo;
import de.metas.ordercandidate.model.I_C_OLCand;
import de.metas.pricing.InvoicableQtyBasedOn;
import de.metas.pricing.PricingSystemId;
import de.metas.pricing.attributebased.IProductPriceAware;
import de.metas.product.ProductId;
import lombok.Builder;
import lombok.Getter;
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

public final class OLCand implements IProductPriceAware
{
	private final IOLCandEffectiveValuesBL olCandEffectiveValuesBL;

	private final I_C_OLCand candidate;

	private LocalDate dateDoc;

	private final BPartnerInfo bpartnerInfo;
	private final BPartnerInfo billBPartnerInfo;
	private final BPartnerInfo dropShipBPartnerInfo;
	private final BPartnerInfo handOverBPartnerInfo;
	private final PricingSystemId pricingSystemId;

	@Getter
	private final String externalLineId;

	@Getter
	private final String externalHeaderId;

	@Builder
	private OLCand(
			@NonNull final IOLCandEffectiveValuesBL olCandEffectiveValuesBL,
			//
			@NonNull final I_C_OLCand candidate,
			@Nullable final PricingSystemId pricingSystemId)
	{
		this.olCandEffectiveValuesBL = olCandEffectiveValuesBL;

		this.candidate = candidate;

		this.dateDoc = TimeUtil.asLocalDate(candidate.getDateOrdered());

		this.bpartnerInfo = BPartnerInfo.builder()
				.bpartnerId(this.olCandEffectiveValuesBL.getBPartnerEffectiveId(candidate))
				.bpartnerLocationId(this.olCandEffectiveValuesBL.getLocationEffectiveId(candidate))
				.contactId(this.olCandEffectiveValuesBL.getContactEffectiveId(candidate))
				.build();
		this.billBPartnerInfo = BPartnerInfo.builder()
				.bpartnerId(this.olCandEffectiveValuesBL.getBillBPartnerEffectiveId(candidate))
				.bpartnerLocationId(this.olCandEffectiveValuesBL.getBillLocationEffectiveId(candidate))
				.contactId(this.olCandEffectiveValuesBL.getBillContactEffectiveId(candidate))
				.build();
		this.dropShipBPartnerInfo = BPartnerInfo.builder()
				.bpartnerId(this.olCandEffectiveValuesBL.getDropShipBPartnerEffectiveId(candidate))
				.bpartnerLocationId(this.olCandEffectiveValuesBL.getDropShipLocationEffectiveId(candidate))
				.contactId(this.olCandEffectiveValuesBL.getDropShipContactEffectiveId(candidate))
				.build();
		this.handOverBPartnerInfo = BPartnerInfo.builder()
				.bpartnerId(this.olCandEffectiveValuesBL.getHandOverPartnerEffectiveId(candidate))
				.bpartnerLocationId(this.olCandEffectiveValuesBL.getHandOverLocationEffectiveId(candidate))
				// .contactId(this.xolCandEffectiveValuesBL.getHandOver_User_Effective_ID(candidate))
				.build();

		this.pricingSystemId = pricingSystemId;

		this.externalLineId = candidate.getExternalLineId();
		this.externalHeaderId = candidate.getExternalHeaderId();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).addValue(candidate).toString();
	}

	public I_C_OLCand unbox()
	{
		return candidate;
	}

	public int getId()
	{
		return candidate.getC_OLCand_ID();
	}

	public TableRecordReference toTableRecordReference()
	{
		return TableRecordReference.of(I_C_OLCand.Table_Name, getId());
	}

	public int getAD_Client_ID()
	{
		return candidate.getAD_Client_ID();
	}

	public int getAD_Org_ID()
	{
		return candidate.getAD_Org_ID();
	}

	public BPartnerInfo getBPartnerInfo()
	{
		return bpartnerInfo;
	}

	public BPartnerInfo getBillBPartnerInfo()
	{
		return billBPartnerInfo;
	}

	public BPartnerInfo getDropShipBPartnerInfo()
	{
		return dropShipBPartnerInfo;
	}

	public BPartnerInfo getHandOverBPartnerInfo()
	{
		return handOverBPartnerInfo;
	}

	public PricingSystemId getPricingSystemId()
	{
		return pricingSystemId;
	}

	public int getC_Charge_ID()
	{
		return candidate.getC_Charge_ID();
	}

	public int getM_Product_ID()
	{
		return ProductId.toRepoId(olCandEffectiveValuesBL.getM_Product_Effective_ID(candidate));
	}

	public int getC_UOM_ID()
	{
		return olCandEffectiveValuesBL.getC_UOM_Effective_ID(candidate);
	}

	public int getM_AttributeSet_ID()
	{
		return candidate.getM_AttributeSet_ID();
	}

	public int getM_AttributeSetInstance_ID()
	{
		return candidate.getM_AttributeSetInstance_ID();
	}

	public WarehouseId getWarehouseDestId()
	{
		return WarehouseId.ofRepoIdOrNull(candidate.getM_Warehouse_Dest_ID());
	}

	public boolean isManualPrice()
	{
		return candidate.isManualPrice();
	}

	public BigDecimal getPriceActual()
	{
		return candidate.getPriceActual();
	}

	public boolean isManualDiscount()
	{
		return candidate.isManualDiscount();
	}

	public BigDecimal getDiscount()
	{
		return candidate.getDiscount();
	}

	public int getC_Currency_ID()
	{
		return candidate.getC_Currency_ID();
	}

	public String getProductDescription()
	{
		return candidate.getProductDescription();
	}

	public int getLine()
	{
		return candidate.getLine();
	}

	public boolean isProcessed()
	{
		return candidate.isProcessed();
	}

	public void setProcessed(final boolean processed)
	{
		candidate.setProcessed(true);
	}

	public boolean isError()
	{
		return candidate.isError();
	}

	public void setError(final String errorMsg, final int adNoteId)
	{
		candidate.setIsError(true);
		candidate.setErrorMsg(errorMsg);
		candidate.setAD_Note_ID(adNoteId);
	}

	public BigDecimal getQty()
	{
		return candidate.getQty();
	}

	public String getPOReference()
	{
		return candidate.getPOReference();
	}

	public LocalDate getDateDoc()
	{
		return dateDoc;
	}

	public void setDateDoc(@NonNull final LocalDate dateDoc)
	{
		this.dateDoc = dateDoc;
	}

	public ZonedDateTime getDatePromised()
	{
		return olCandEffectiveValuesBL.getDatePromised_Effective(candidate);
	}

	public int getAD_InputDataSource_ID()
	{
		return candidate.getAD_InputDataSource_ID();
	}

	public int getAD_DataDestination_ID()
	{
		return candidate.getAD_DataDestination_ID();
	}

	public boolean isImportedWithIssues()
	{
		final org.adempiere.process.rpl.model.I_C_OLCand rplCandidate = InterfaceWrapperHelper.create(candidate, org.adempiere.process.rpl.model.I_C_OLCand.class);
		return rplCandidate.isImportedWithIssues();
	}

	// FIXME hardcoded (08691)
	public Object getValueByColumn(final OLCandAggregationColumn column)
	{
		final String olCandColumnName = column.getColumnName();

		if (olCandColumnName.equals(I_C_OLCand.COLUMNNAME_Bill_BPartner_ID))
		{
			return getBillBPartnerInfo().getBpartnerId();
		}
		else if (olCandColumnName.equals(I_C_OLCand.COLUMNNAME_Bill_Location_ID))
		{
			return getBillBPartnerInfo().getBpartnerLocationId();
		}
		else if (olCandColumnName.equals(I_C_OLCand.COLUMNNAME_Bill_User_ID))
		{
			return getBillBPartnerInfo().getContactId();
		}
		else if (olCandColumnName.equals(I_C_OLCand.COLUMNNAME_DropShip_BPartner_ID))
		{
			return getDropShipBPartnerInfo().getBpartnerId();
		}
		else if (olCandColumnName.equals(I_C_OLCand.COLUMNNAME_DropShip_Location_ID))
		{
			return getDropShipBPartnerInfo().getBpartnerLocationId();
		}
		else if (olCandColumnName.equals(I_C_OLCand.COLUMNNAME_M_PricingSystem_ID))
		{
			return getPricingSystemId();
		}
		else if (olCandColumnName.equals(I_C_OLCand.COLUMNNAME_DateOrdered))
		{
			return getDateDoc();
		}
		else if (olCandColumnName.equals(I_C_OLCand.COLUMNNAME_DatePromised_Effective))
		{
			return getDatePromised();
		}
		else
		{
			return InterfaceWrapperHelper.getValueByColumnId(candidate, column.getAdColumnId());
		}
	}

	@Override
	public int getM_ProductPrice_ID()
	{
		return candidate.getM_ProductPrice_ID();
	}

	@Override
	public boolean isExplicitProductPriceAttribute()
	{
		return candidate.isExplicitProductPriceAttribute();
	}

	public int getFlatrateConditionsId()
	{
		return candidate.getC_Flatrate_Conditions_ID();
	}

	public int getHUPIProductItemId()
	{
		if (candidate.getM_HU_PI_Item_Product_Override_ID() > 0)
		{
			return candidate.getM_HU_PI_Item_Product_Override_ID();
		}
		return candidate.getM_HU_PI_Item_Product_ID();
	}

	public InvoicableQtyBasedOn getInvoicableQtyBasedOn()
	{
		return InvoicableQtyBasedOn.fromRecordString(candidate.getInvoicableQtyBasedOn());
	}

	public LocalDate getPresetDateInvoiced()
	{
		return TimeUtil.asLocalDate(candidate.getPresetDateInvoiced());
	}

	public LocalDate getPresetDateShipped()
	{
		return TimeUtil.asLocalDate(candidate.getPresetDateShipped());
	}
}
