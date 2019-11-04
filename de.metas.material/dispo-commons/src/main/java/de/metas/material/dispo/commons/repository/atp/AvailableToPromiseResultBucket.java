package de.metas.material.dispo.commons.repository.atp;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

import de.metas.material.commons.attributes.AttributesKeyMatcher;
import de.metas.material.commons.attributes.AttributesKeyPatterns;
import de.metas.material.event.commons.AttributesKey;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/*
 * #%L
 * metasfresh-material-dispo-commons
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

@ToString
final class AvailableToPromiseResultBucket
{
	public static AvailableToPromiseResultBucket newAcceptingAny()
	{
		return builder().build();
	}

	@Getter(AccessLevel.PACKAGE)
	@VisibleForTesting
	private final WarehouseClassifier warehouse;

	@Getter(AccessLevel.PACKAGE)
	@VisibleForTesting
	private final ProductClassifier product;

	@Getter(AccessLevel.PACKAGE)
	@VisibleForTesting
	private final BPartnerClassifier bpartner;

	@Getter(AccessLevel.PACKAGE)
	@VisibleForTesting
	private final AttributesKeyMatcher storageAttributesKeyMatcher;

	private final ArrayList<AvailableToPromiseResultGroupBuilder> groups = new ArrayList<>();

	@Builder
	private AvailableToPromiseResultBucket(
			@Nullable final WarehouseClassifier warehouse,
			@Nullable final BPartnerClassifier bpartner,
			@Nullable final ProductClassifier product,
			@Nullable final AttributesKeyMatcher storageAttributesKeyMatcher)
	{
		this.warehouse = warehouse != null ? warehouse : WarehouseClassifier.any();
		this.bpartner = bpartner != null ? bpartner : BPartnerClassifier.any();
		this.product = product != null ? product : ProductClassifier.any();
		this.storageAttributesKeyMatcher = storageAttributesKeyMatcher != null
				? storageAttributesKeyMatcher
				: AttributesKeyPatterns.matchingAll();
	}

	Stream<AvailableToPromiseResultGroup> buildAndStreamGroups()
	{
		return groups.stream().map(AvailableToPromiseResultGroupBuilder::build);
	}

	/**
	 * @return true if the request was added now (or in the past) to one of the bucket groups
	 */
	public boolean addQtyToAllMatchingGroups(@NonNull final AddToResultGroupRequest request)
	{
		if (!isMatching(request))
		{
			return false;
		}

		boolean addedToAtLeastOneGroup = false;
		for (final AvailableToPromiseResultGroupBuilder group : groups)
		{
			if (!isGroupMatching(group, request))
			{
				continue;
			}

			if (group.isAlreadyIncluded(request))
			{
				// btw: don't break; a request might be added to >1 groups
				addedToAtLeastOneGroup = true;
			}
			else
			{
				group.addQty(request);
				addedToAtLeastOneGroup = true;
			}
		}

		if (!addedToAtLeastOneGroup)
		{
			final AttributesKey storageAttributesKey = storageAttributesKeyMatcher.toAttributeKeys(request.getStorageAttributesKey());
			final AvailableToPromiseResultGroupBuilder group = newGroup(request, storageAttributesKey);
			group.addQty(request);
			addedToAtLeastOneGroup = true;
		}

		return addedToAtLeastOneGroup;
	}

	public boolean addToNewGroupIfFeasible(@NonNull final AddToResultGroupRequest request)
	{
		if (!isMatching(request))
		{
			return false;
		}

		boolean alreadyIncludedInMatchingGroup = false;

		if (!request.getBpartner().isSpecificBPartner())
		{
			for (final AvailableToPromiseResultGroupBuilder group : groups)
			{
				if (!isGroupMatching(group, request))
				{
					continue;
				}

				if (group.isAlreadyIncluded(request))
				{
					alreadyIncludedInMatchingGroup = true;
				}
				else
				{
					group.addQty(request);
					alreadyIncludedInMatchingGroup = true;
				}
			}
		}

		if (!alreadyIncludedInMatchingGroup)
		{
			final AttributesKey storageAttributesKey = request.getStorageAttributesKey();
			final AvailableToPromiseResultGroupBuilder group = newGroup(request, storageAttributesKey);
			group.addQty(request);
			alreadyIncludedInMatchingGroup = true;
		}

		return alreadyIncludedInMatchingGroup;
	}

	@VisibleForTesting
	boolean isMatching(final AddToResultGroupRequest request)
	{
		if (!product.isMatching(request.getProductId()))
		{
			return false;
		}

		if (!warehouse.isMatching(request.getWarehouseId()))
		{
			return false;
		}

		if (!bpartner.isMatching(request.getBpartner()))
		{
			return false;
		}

		if (!storageAttributesKeyMatcher.matches(request.getStorageAttributesKey()))
		{
			return false;
		}

		return true;
	}

	private static boolean isGroupMatching(
			@NonNull final AvailableToPromiseResultGroupBuilder group,
			@NonNull final AddToResultGroupRequest request)
	{
		if (!Objects.equals(group.getProductId(), request.getProductId()))
		{
			return false;
		}

		if (!group.getWarehouse().isMatching(request.getWarehouseId()))
		{
			return false;
		}

		if (!group.getBpartner().isMatching(request.getBpartner()))
		{
			return false;
		}

		if (!isGroupAttributesKeyMatching(group, request.getStorageAttributesKey()))
		{
			return false;
		}

		return true;
	}

	private static boolean isGroupAttributesKeyMatching(
			@NonNull final AvailableToPromiseResultGroupBuilder group,
			@NonNull final AttributesKey requestStorageAttributesKey)
	{
		final AttributesKey groupAttributesKey = group.getStorageAttributesKey();
		if (groupAttributesKey.isAll())
		{
			return true;
		}
		else if (groupAttributesKey.isOther())
		{
			// accept it. We assume that the actual matching was done on Bucket level and not on Group level
			return true;
		}
		else if (groupAttributesKey.isNone())
		{
			// shall not happen
			return false;
		}
		else
		{
			return requestStorageAttributesKey.contains(groupAttributesKey);
		}
	}

	private AvailableToPromiseResultGroupBuilder newGroup(
			@NonNull final AddToResultGroupRequest request,
			@NonNull final AttributesKey storageAttributesKey)
	{
		final AvailableToPromiseResultGroupBuilder group = AvailableToPromiseResultGroupBuilder.builder()
				.bpartner(request.getBpartner())
				.warehouse(WarehouseClassifier.specific(request.getWarehouseId()))
				.productId(request.getProductId())
				.storageAttributesKey(storageAttributesKey)
				.build();

		groups.add(group);

		return group;
	}

	void addDefaultEmptyGroupIfPossible()
	{
		if (product.isAny())
		{
			return;
		}

		final AttributesKey defaultAttributesKey = this.storageAttributesKeyMatcher.toAttributeKeys().orElse(null);
		if (defaultAttributesKey == null)
		{
			return;
		}

		final AvailableToPromiseResultGroupBuilder group = AvailableToPromiseResultGroupBuilder.builder()
				.bpartner(bpartner)
				.warehouse(warehouse)
				.productId(product.getProductId())
				.storageAttributesKey(defaultAttributesKey)
				.build();

		groups.add(group);
	}

	@VisibleForTesting
	boolean isZeroQty()
	{
		if (groups.isEmpty())
		{
			return true;
		}

		for (AvailableToPromiseResultGroupBuilder group : groups)
		{
			if (!group.isZeroQty())
			{
				return false;
			}
		}

		return true;
	}
}
