package de.metas.rest_api.ordercandidates.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import de.metas.rest_api.ordercandidates.request.JsonOrganization;
import lombok.Builder;
import lombok.Value;

/*
 * #%L
 * de.metas.ordercandidate.rest-api
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

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@Value
@Builder
public class JsonOLCand
{
	private int id;
	private String externalLineId;
	private String externalHeaderId;

	private String poReference;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private JsonOrganization org;

	private JsonResponseBPartnerLocationAndContact bpartner;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private JsonResponseBPartnerLocationAndContact billBPartner;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private JsonResponseBPartnerLocationAndContact dropShipBPartner;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private JsonResponseBPartnerLocationAndContact handOverBPartner;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dateOrdered;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate datePromised;

	private int flatrateConditionsId;

	private int productId;

	private String productDescription;

	private BigDecimal qty;
	private int uomId;
	private int huPIItemProductId;

	private int pricingSystemId;
	private BigDecimal price;
	private BigDecimal discount;

	private int warehouseDestId;
}
