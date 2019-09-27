package de.metas.handlingunits.expiry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.mm.attributes.AttributeId;
import org.adempiere.mm.attributes.api.AttributeConstants;
import org.adempiere.mm.attributes.api.IAttributeDAO;
import org.compiere.model.I_M_Attribute;
import org.compiere.util.Env;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSet;

import de.metas.handlingunits.HuId;
import de.metas.handlingunits.IHUContext;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.IHandlingUnitsDAO;
import de.metas.handlingunits.IMutableHUContext;
import de.metas.handlingunits.attribute.HUAttributeConstants;
import de.metas.handlingunits.attribute.storage.IAttributeStorage;
import de.metas.handlingunits.hutransaction.IHUTrxBL;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.logging.LogManager;
import de.metas.util.Loggables;
import de.metas.util.Services;
import de.metas.util.time.SystemTime;
import lombok.NonNull;

/*
 * #%L
 * de.metas.handlingunits.base
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

@Service
public class HUWithExpiryDatesService
{

	private static final Logger logger = LogManager.getLogger(HUWithExpiryDatesService.class);

	private final HUWithExpiryDatesRepository huWithExpiryDatesRepository;

	public HUWithExpiryDatesService(@NonNull final HUWithExpiryDatesRepository huWithExpiryDatesRepository)
	{
		this.huWithExpiryDatesRepository = huWithExpiryDatesRepository;
	}

	public void markExpiredWhereWarnDateExceeded(@NonNull final LocalDateTime expiredWarnDate)
	{
		huWithExpiryDatesRepository.getByWarnDateExceeded(expiredWarnDate)
				.map(HUWithExpiryDates::getHuId)
				.forEach(this::markExpiredInOwnTrx);
	}

	private void markExpiredInOwnTrx(@NonNull final HuId huId)
	{
		final IHUTrxBL huTrxBL = Services.get(IHUTrxBL.class);
		huTrxBL.process(huContext -> {
			try
			{
				markExpiredUsingHuContext(huId, huContext);
				Loggables.addLog("Successfully processed M_HU_ID={}", huId);
			}
			catch (final AdempiereException ex)
			{
				Loggables.addLog("!!! Failed processing M_HU_ID={}: {} !!!", huId, ex.getLocalizedMessage());
				logger.warn("Failed processing M_HU_ID={}. Skipped", huId, ex);
			}
		});
	}

	public void markExpiredIfWarnDateExceeded(@NonNull final HuId huId)
	{
		final LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
		final HUWithExpiryDates huWithExpiryDates = huWithExpiryDatesRepository.getByIdIfWarnDateExceededOrNull(huId, startOfToday);
		if (huWithExpiryDates == null)
		{
			return;
		}

		final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);
		final IMutableHUContext huContext = handlingUnitsBL.createMutableHUContext(Env.getCtx());
		markExpiredUsingHuContext(huId, huContext);
	}

	private void markExpiredUsingHuContext(
			@NonNull final HuId huId,
			@NonNull final IHUContext huContext)
	{
		final IHandlingUnitsDAO handlingUnitsDAO = Services.get(IHandlingUnitsDAO.class);

		final I_M_HU hu = handlingUnitsDAO.getById(huId);

		final IAttributeStorage huAttributes = huContext
				.getHUAttributeStorageFactory()
				.getAttributeStorage(hu);

		huAttributes.setSaveOnChange(true);

		final I_M_Attribute huExpiredAttribute = retrieveHU_Expired_Attribute();
		huAttributes.setValue(huExpiredAttribute, HUAttributeConstants.ATTR_Expired_Value_Expired);
	}

	private I_M_Attribute retrieveHU_Expired_Attribute()
	{
		final IAttributeDAO attributeDAO = Services.get(IAttributeDAO.class);
		return attributeDAO.retrieveAttributeByValue(HUAttributeConstants.ATTR_Expired); // this is cached
	}

	public void updateMonthsUntilExpiry()
	{
		final ImmutableSet<HuId> husWithExpiryDates = huWithExpiryDatesRepository.getAllWithBestBeforeDate();

		husWithExpiryDates.stream()
				.forEach(huId -> updateMonthsUntilExpiry(huId));
	}

	private void updateMonthsUntilExpiry(final HuId huId)
	{
		final LocalDate bestBeforeDate = huWithExpiryDatesRepository.getBestBeforeDate(huId);

		final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);
		final IMutableHUContext huContext = handlingUnitsBL.createMutableHUContext(Env.getCtx());
		updateMonthsUntilExpiryUsingHuContext(huId, bestBeforeDate, huContext);
	}

	private void updateMonthsUntilExpiryUsingHuContext(
			@NonNull final HuId huId,
			@NonNull LocalDate bestBeforeDate,
			@NonNull final IHUContext huContext)
	{
		final IHandlingUnitsDAO handlingUnitsDAO = Services.get(IHandlingUnitsDAO.class);
		final IAttributeDAO attributeDAO = Services.get(IAttributeDAO.class);

		final I_M_HU hu = handlingUnitsDAO.getById(huId);

		final IAttributeStorage huAttributes = huContext
				.getHUAttributeStorageFactory()
				.getAttributeStorage(hu);

		huAttributes.setSaveOnChange(true);

		final AttributeId monthsUntilExpiryAttributeId = retrieveHU_MonthsUntilExpiry_AttributeId();

		final LocalDate today = SystemTime.asLocalDate();
		final long months = ChronoUnit.MONTHS.between(today, bestBeforeDate);

		final I_M_Attribute monthsUntilExpiryAttribute = attributeDAO.getAttributeById(monthsUntilExpiryAttributeId);
		huAttributes.setValue(monthsUntilExpiryAttribute, new BigDecimal(months));
	}

	private AttributeId retrieveHU_MonthsUntilExpiry_AttributeId()
	{
		final IAttributeDAO attributeDAO = Services.get(IAttributeDAO.class);
		return attributeDAO.retrieveAttributeIdByValue(AttributeConstants.ATTR_MonthsUntilExpiry); // this is cached
	}
}
