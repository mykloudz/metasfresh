package de.metas.ordercandidate.api;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableList;

import de.metas.bpartner.BPartnerContactId;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.service.BPartnerInfo;
import de.metas.document.DocTypeId;
import de.metas.impex.api.IInputDataSourceDAO;
import de.metas.ordercandidate.model.I_C_OLCand;
import de.metas.organization.IOrgDAO;
import de.metas.organization.OrgId;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.time.SystemTime;
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

@Repository
public class OLCandRepository
{
	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);

	public OLCandSource getForProcessor(@NonNull final OLCandProcessorDescriptor processor)
	{
		return RelationTypeOLCandSource.builder()
				.orderDefaults(processor.getDefaults())
				.olCandProcessorId(processor.getId())
				.build();
	}

	public List<OLCand> create(@NonNull final List<OLCandCreateRequest> requests)
	{
		Check.assumeNotEmpty(requests, "requests is not empty");

		final OLCandFactory olCandFactory = new OLCandFactory();

		final ITrxManager trxManager = Services.get(ITrxManager.class);
		return trxManager.callInThreadInheritedTrx(() -> requests.stream()
				.map(this::createAndSaveOLCandRecord)
				.map(olCandFactory::toOLCand)
				.collect(ImmutableList.toImmutableList()));
	}

	private I_C_OLCand createAndSaveOLCandRecord(@NonNull final OLCandCreateRequest request)
	{
		final I_C_OLCand olCandPO = InterfaceWrapperHelper.newInstance(I_C_OLCand.class);

		if (request.getOrgId() != null)
		{
			olCandPO.setAD_Org_ID(request.getOrgId().getRepoId());
		}
		final OrgId orgId = OrgId.ofRepoIdOrAny(olCandPO.getAD_Org_ID());
		final ZoneId timeZone = orgDAO.getTimeZone(orgId);

		{
			final BPartnerInfo bpartner = request.getBpartner();
			final BPartnerLocationId bpartnerLocationId = bpartner.getBpartnerLocationId();
			if (bpartnerLocationId == null)
			{
				throw new AdempiereException("Given OLCandCreateRequest has no BpartnerLocationId")
						.appendParametersToMessage()
						.setParameter("OLCandCreateRequest", request);
			}

			olCandPO.setC_BPartner_ID(BPartnerId.toRepoId(bpartner.getBpartnerId()));
			olCandPO.setC_BPartner_Location_ID(BPartnerLocationId.toRepoId(bpartnerLocationId));
			olCandPO.setAD_User_ID(BPartnerContactId.toRepoId(bpartner.getContactId()));
		}

		if (request.getBillBPartner() != null)
		{
			final BPartnerInfo bpartner = request.getBillBPartner();
			olCandPO.setBill_BPartner_ID(BPartnerId.toRepoId(bpartner.getBpartnerId()));
			olCandPO.setBill_Location_ID(BPartnerLocationId.toRepoId(bpartner.getBpartnerLocationId()));
			olCandPO.setBill_User_ID(BPartnerContactId.toRepoId(bpartner.getContactId()));
		}

		if (request.getDropShipBPartner() != null)
		{
			final BPartnerInfo bpartner = request.getDropShipBPartner();
			olCandPO.setDropShip_BPartner_ID(BPartnerId.toRepoId(bpartner.getBpartnerId()));
			olCandPO.setDropShip_Location_ID(BPartnerLocationId.toRepoId(bpartner.getBpartnerLocationId()));
			// olCandPO.setDropShip_User_ID(bpartner.getContactId());
		}

		if (request.getHandOverBPartner() != null)
		{
			final BPartnerInfo bpartner = request.getHandOverBPartner();
			olCandPO.setHandOver_Partner_ID(BPartnerId.toRepoId(bpartner.getBpartnerId()));
			olCandPO.setHandOver_Location_ID(BPartnerLocationId.toRepoId(bpartner.getBpartnerLocationId()));
			// olCandPO.setHandOver_User_ID(bpartner.getContactId());
		}

		if (!Check.isEmpty(request.getPoReference(), true))
		{
			olCandPO.setPOReference(request.getPoReference());
		}

		olCandPO.setDateCandidate(SystemTime.asDayTimestamp());
		olCandPO.setDateOrdered(TimeUtil.asTimestamp(request.getDateOrdered()));
		olCandPO.setDatePromised(TimeUtil.asTimestamp(request.getDateRequired()
				.atTime(LocalTime.MAX)
				.atZone(timeZone)));

		olCandPO.setPresetDateInvoiced(TimeUtil.asTimestamp(request.getPresetDateInvoiced()));
		olCandPO.setC_DocTypeInvoice_ID(DocTypeId.toRepoId(request.getDocTypeInvoiceId()));

		olCandPO.setPresetDateShipped(TimeUtil.asTimestamp(request.getPresetDateShipped()));

		olCandPO.setC_Flatrate_Conditions_ID(request.getFlatrateConditionsId());

		olCandPO.setM_Product_ID(request.getProductId().getRepoId());
		olCandPO.setProductDescription(request.getProductDescription());
		olCandPO.setQty(request.getQty());
		olCandPO.setC_UOM_ID(request.getUomId().getRepoId());
		olCandPO.setM_HU_PI_Item_Product_ID(request.getHuPIItemProductId());

		if (request.getPricingSystemId() != null)
		{
			olCandPO.setM_PricingSystem_ID(request.getPricingSystemId().getRepoId());
		}

		if (request.getPrice() != null)
		{
			olCandPO.setIsManualPrice(true);
			olCandPO.setPriceEntered(request.getPrice());
			olCandPO.setC_Currency_ID(request.getCurrencyId().getRepoId());
		}

		if (request.getDiscount() != null)
		{
			olCandPO.setIsManualDiscount(true);
			olCandPO.setDiscount(request.getDiscount().toBigDecimal());
		}

		if (request.getWarehouseDestId() != null)
		{
			olCandPO.setM_Warehouse_Dest_ID(request.getWarehouseDestId().getRepoId());
		}

		olCandPO.setAD_User_EnteredBy_ID(Env.getAD_User_ID());

		final IInputDataSourceDAO inputDataSourceDAO = Services.get(IInputDataSourceDAO.class);
		if (request.getDataSourceInternalName() != null)
		{
			final int inputDataSourceId = inputDataSourceDAO.retrieveInputDataSourceId(request.getDataSourceInternalName());
			olCandPO.setAD_InputDataSource_ID(inputDataSourceId);
		}
		if (request.getDataDestInternalName() != null)
		{
			final int inputDataDestId = inputDataSourceDAO.retrieveInputDataSourceId(request.getDataDestInternalName());
			olCandPO.setAD_DataDestination_ID(inputDataDestId);
		}

		olCandPO.setExternalLineId(request.getExternalLineId());
		olCandPO.setExternalHeaderId(request.getExternalHeaderId());

		InterfaceWrapperHelper.saveRecord(olCandPO);

		return olCandPO;
	}

	public List<OLCand> getByQuery(@NonNull final OLCandQuery olCandQuery)
	{
		final OLCandFactory olCandFactory = new OLCandFactory();

		return toSqlQueryBuilder(olCandQuery)
				.create()
				.stream()
				.map(olCandFactory::toOLCand)
				.collect(ImmutableList.toImmutableList());
	}

	private IQueryBuilder<I_C_OLCand> toSqlQueryBuilder(@NonNull final OLCandQuery olCandQuery)
	{
		final IQueryBuilder<I_C_OLCand> queryBuilder = Services.get(IQueryBL.class)
				.createQueryBuilder(I_C_OLCand.class)
				.addOnlyActiveRecordsFilter();

		if (olCandQuery.getExternalHeaderId() != null)
		{
			queryBuilder.addEqualsFilter(I_C_OLCand.COLUMN_ExternalHeaderId, olCandQuery.getExternalHeaderId());
		}
		if (olCandQuery.getInputDataSourceName() != null)
		{
			final int inputDataSourceId = Services.get(IInputDataSourceDAO.class).retrieveInputDataSourceId(olCandQuery.getInputDataSourceName());
			queryBuilder.addEqualsFilter(I_C_OLCand.COLUMN_AD_InputDataSource_ID, inputDataSourceId);
		}

		return queryBuilder;
	}
}
