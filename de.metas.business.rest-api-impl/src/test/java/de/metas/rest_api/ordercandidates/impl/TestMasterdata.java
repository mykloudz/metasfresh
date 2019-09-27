package de.metas.rest_api.ordercandidates.impl;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;

import java.time.LocalDate;

import javax.annotation.Nullable;

import org.adempiere.pricing.model.I_C_PricingRule;
import org.adempiere.warehouse.WarehouseId;
import org.adempiere.warehouse.model.I_M_Warehouse;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Country;
import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_Location;
import org.compiere.model.I_C_TaxCategory;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_PriceList;
import org.compiere.model.I_M_PriceList_Version;
import org.compiere.model.I_M_PricingSystem;
import org.compiere.model.I_M_Product;
import org.compiere.util.TimeUtil;
import org.junit.Ignore;

import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.GLN;
import de.metas.document.DocBaseAndSubType;
import de.metas.impex.model.I_AD_InputDataSource;
import de.metas.location.CountryId;
import de.metas.location.LocationId;
import de.metas.money.CurrencyId;
import de.metas.pricing.PriceListId;
import de.metas.pricing.PriceListVersionId;
import de.metas.pricing.PricingSystemId;
import de.metas.pricing.rules.IPricingRule;
import de.metas.pricing.rules.PriceListVersion;
import de.metas.tax.api.TaxCategoryId;
import de.metas.uom.UomId;
import lombok.Builder;
import lombok.NonNull;

/*
 * #%L
 * de.metas.business.rest-api-impl
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

@Ignore
final class TestMasterdata
{
	public UomId createUOM(final String uomCode)
	{
		final I_C_UOM uomRecord = newInstance(I_C_UOM.class);
		uomRecord.setX12DE355(uomCode);
		saveRecord(uomRecord);
		return UomId.ofRepoId(uomRecord.getC_UOM_ID());
	}

	public CountryId createCountry(final String countryCode)
	{
		final I_C_Country record = newInstance(I_C_Country.class);
		record.setCountryCode(countryCode);
		saveRecord(record);
		return CountryId.ofRepoId(record.getC_Country_ID());
	}

	public void createDataSource(final String internalName)
	{
		final I_AD_InputDataSource dataSourceRecord = newInstance(I_AD_InputDataSource.class);
		dataSourceRecord.setInternalName(internalName);
		saveRecord(dataSourceRecord);
	}

	public void createDocType(final DocBaseAndSubType docBaseAndSubType)
	{
		final I_C_DocType docTypeRecord = newInstance(I_C_DocType.class);
		docTypeRecord.setDocBaseType(docBaseAndSubType.getDocBaseType());
		docTypeRecord.setDocSubType(docBaseAndSubType.getDocSubType());
		saveRecord(docTypeRecord);
	}

	@Builder(builderMethodName = "prepareBPartnerAndLocation", builderClassName = "_BPartnerAndLocationBuilder")
	private BPartnerLocationId createBPartnerAndLocation(
			@NonNull final String bpValue,
			@Nullable final PricingSystemId salesPricingSystemId,
			@NonNull final CountryId countryId,
			@Nullable final GLN gln)
	{
		final I_C_BPartner bpRecord = newInstance(I_C_BPartner.class);
		bpRecord.setValue(bpValue);
		bpRecord.setName(bpValue + "-name");
		bpRecord.setIsCustomer(true);
		bpRecord.setM_PricingSystem_ID(PricingSystemId.toRepoId(salesPricingSystemId));
		saveRecord(bpRecord);

		return prepareBPartnerLocation()
				.bpartnerId(BPartnerId.ofRepoId(bpRecord.getC_BPartner_ID()))
				.countryId(countryId)
				.gln(gln)
				.build();
	}

	@Builder(builderMethodName = "prepareBPartnerLocation", builderClassName = "_BPartnerLocationBuilder")
	private BPartnerLocationId createBPartnerLocation(
			@NonNull final BPartnerId bpartnerId,
			@NonNull final CountryId countryId,
			@Nullable final GLN gln)
	{
		final LocationId locationId = createLocation(countryId);

		final I_C_BPartner_Location bplRecord = newInstance(I_C_BPartner_Location.class);
		bplRecord.setC_BPartner_ID(bpartnerId.getRepoId());
		bplRecord.setC_Location_ID(locationId.getRepoId());
		bplRecord.setGLN(gln != null ? gln.getCode() : null);
		saveRecord(bplRecord);

		return BPartnerLocationId.ofRepoId(bplRecord.getC_BPartner_ID(), bplRecord.getC_BPartner_Location_ID());
	}

	private LocationId createLocation(final CountryId countryId)
	{
		final I_C_Location record = newInstance(I_C_Location.class);
		record.setC_Country_ID(countryId.getRepoId());
		saveRecord(record);
		return LocationId.ofRepoId(record.getC_Location_ID());
	}

	public void createProduct(final String value, final UomId uomId)
	{
		final I_M_Product record = newInstance(I_M_Product.class);
		record.setValue(value);
		record.setC_UOM_ID(uomId.getRepoId());
		saveRecord(record);
	}

	public PricingSystemId createPricingSystem()
	{
		final I_M_PricingSystem record = newInstance(I_M_PricingSystem.class);
		saveRecord(record);
		return PricingSystemId.ofRepoId(record.getM_PricingSystem_ID());
	}

	public PriceListId createSalesPriceList(
			@NonNull final PricingSystemId pricingSystemId,
			@NonNull final CountryId countryId,
			@NonNull final CurrencyId currencyId,
			@Nullable final TaxCategoryId defaultTaxCategoryId)
	{
		final I_M_PriceList record = newInstance(I_M_PriceList.class);
		record.setM_PricingSystem_ID(pricingSystemId.getRepoId());
		record.setC_Country_ID(countryId.getRepoId());
		record.setC_Currency_ID(currencyId.getRepoId());
		record.setIsSOPriceList(true);
		record.setPricePrecision(2);
		record.setDefault_TaxCategory_ID(TaxCategoryId.toRepoId(defaultTaxCategoryId));
		saveRecord(record);
		return PriceListId.ofRepoId(record.getM_PriceList_ID());
	}

	public PriceListVersionId createPriceListVersion(
			@NonNull final PriceListId priceListId,
			@NonNull final LocalDate validFrom)
	{
		I_M_PriceList_Version record = newInstance(I_M_PriceList_Version.class);
		record.setM_PriceList_ID(priceListId.getRepoId());
		record.setValidFrom(TimeUtil.asTimestamp(validFrom));
		saveRecord(record);
		return PriceListVersionId.ofRepoId(record.getM_PriceList_Version_ID());
	}

	public TaxCategoryId createTaxCategory()
	{
		final I_C_TaxCategory record = newInstance(I_C_TaxCategory.class);
		saveRecord(record);
		return TaxCategoryId.ofRepoId(record.getC_TaxCategory_ID());
	}

	public void createPricingRules()
	{
		createPricingRule(PriceListVersion.class, 10);
	}

	private void createPricingRule(
			@NonNull final Class<? extends IPricingRule> clazz,
			int seqNo)
	{
		final String classname = clazz.getName();

		final I_C_PricingRule pricingRule = newInstance(I_C_PricingRule.class);
		pricingRule.setName(classname);
		pricingRule.setClassname(classname);
		pricingRule.setIsActive(true);
		pricingRule.setSeqNo(seqNo);
		saveRecord(pricingRule);
	}

	public WarehouseId createWarehouse(final String value)
	{
		final I_M_Warehouse record = newInstance(I_M_Warehouse.class);
		record.setValue(value);
		saveRecord(record);
		return WarehouseId.ofRepoId(record.getM_Warehouse_ID());
	}

}
