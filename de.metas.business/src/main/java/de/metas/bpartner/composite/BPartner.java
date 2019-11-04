package de.metas.bpartner.composite;

import static de.metas.util.Check.isEmpty;
import static de.metas.util.lang.CoalesceUtil.coalesce;

import javax.annotation.Nullable;

import org.adempiere.ad.table.RecordChangeLog;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import de.metas.bpartner.BPGroupId;
import de.metas.bpartner.BPartnerId;
import de.metas.i18n.ITranslatableString;
import de.metas.i18n.Language;
import de.metas.i18n.TranslatableStrings;
import de.metas.util.rest.ExternalId;
import lombok.Builder;
import lombok.Data;

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

@Data
@JsonPropertyOrder(alphabetic = true/* we want the serialized json to be less flaky in our snapshot files */)
public class BPartner
{
	public static final String ID = "id";
	public static final String EXTERNAL_ID = "externalId";
	public static final String ACTIVE = "active";
	public static final String NAME = "name";
	public static final String NAME_2 = "name2";
	public static final String NAME_3 = "name3";
	public static final String COMPANY_NAME = "companyName";
	public static final String PARENT_ID = "parentId";
	public static final String VALUE = "value";
	public static final String PHONE = "phone";
	public static final String LANGUAGE = "language";
	public static final String URL = "url";
	public static final String URL_2 = "url2";
	public static final String URL_3 = "url3";
	public static final String GROUP_ID = "groupId";
	public static final String VENDOR = "vendor";
	public static final String CUSTOMER = "customer";

	/** May be null if the bpartner was not yet saved. */
	private BPartnerId id;

	private ExternalId externalId;
	private boolean active;
	private String value;
	private String name;
	private String name2;
	private String name3;

	/** non-empty value implies that the bpartner is also a company */
	private String companyName;

	/** This translates to `C_BPartner.BPartner_Parent_ID`. It's a this bpartner's central/parent company. */
	private BPartnerId parentId;

	/** This translates to `C_BPartner.Phone2`. It's this bpartner's central phone number. */
	private String phone;

	private Language language;

	private String url;

	private String url2;

	private String url3;

	private BPGroupId groupId;
	
	private final boolean vendor;
	private final boolean customer;

	private final RecordChangeLog changeLog;

	/** They are all nullable because we can create a completely empty instance which we then fill. */
	@Builder(toBuilder = true)
	private BPartner(
			@Nullable final BPartnerId id,
			@Nullable final ExternalId externalId,
			@Nullable final Boolean active,
			@Nullable final String value,
			@Nullable final String name,
			@Nullable final String name2,
			@Nullable final String name3,
			@Nullable final String companyName,
			@Nullable final BPartnerId parentId,
			@Nullable final String phone,
			@Nullable final Language language,
			@Nullable final String url,
			@Nullable final String url2,
			@Nullable final String url3,
			@Nullable final BPGroupId groupId,
			@Nullable final Boolean vendor,
			@Nullable final Boolean customer,
			@Nullable final RecordChangeLog changeLog)
	{
		this.id = id;
		this.externalId = externalId;
		this.active = coalesce(active, true);
		this.value = value;
		this.name = name;
		this.name2 = name2;
		this.name3 = name3;
		this.companyName = companyName;
		this.parentId = parentId;
		this.phone = phone;
		this.language = language;
		this.url = url;
		this.url2 = url2;
		this.url3 = url3;
		this.groupId = groupId;
		this.vendor = coalesce(vendor, false);
		this.customer = coalesce(customer, false);

		this.changeLog = changeLog;
	}

	/** empty list means valid */
	public ImmutableList<ITranslatableString> validate()
	{
		final ImmutableList.Builder<ITranslatableString> result = ImmutableList.builder();
		if (isEmpty(value, true))
		{
			result.add(TranslatableStrings.constant("bpartner.value"));
		}
		if (isEmpty(name, true))
		{
			result.add(TranslatableStrings.constant("bpartner.name"));
		}
		if (groupId == null)
		{
			result.add(TranslatableStrings.constant("bpartner.groupId"));
		}
		return result.build();
	}
}
