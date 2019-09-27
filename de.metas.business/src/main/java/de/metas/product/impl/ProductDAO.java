package de.metas.product.impl;

import static org.adempiere.model.InterfaceWrapperHelper.loadByIdsOutOfTrx;
import static org.adempiere.model.InterfaceWrapperHelper.loadByRepoIdAwares;
import static org.adempiere.model.InterfaceWrapperHelper.loadByRepoIdAwaresOutOfTrx;
import static org.adempiere.model.InterfaceWrapperHelper.loadOutOfTrx;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2015 metas GmbH
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.dao.IQueryOrderBy.Direction;
import org.adempiere.ad.dao.IQueryOrderBy.Nulls;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.proxy.Cached;
import org.compiere.model.IQuery;
import org.compiere.model.I_M_Product;
import org.compiere.model.I_M_Product_Category;
import org.compiere.util.Env;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import de.metas.cache.annotation.CacheCtx;
import de.metas.organization.OrgId;
import de.metas.product.CreateProductRequest;
import de.metas.product.IProductDAO;
import de.metas.product.IProductMappingAware;
import de.metas.product.ProductAndCategoryAndManufacturerId;
import de.metas.product.ProductAndCategoryId;
import de.metas.product.ProductCategoryId;
import de.metas.product.ProductId;
import de.metas.product.ResourceId;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;

public class ProductDAO implements IProductDAO
{
	@Override
	public I_M_Product getById(@NonNull final ProductId productId)
	{
		return getById(productId, I_M_Product.class);
	}

	@Override
	public <T extends I_M_Product> T getById(@NonNull final ProductId productId, @NonNull final Class<T> productClass)
	{
		final T product = loadOutOfTrx(productId, productClass); // assume caching is configured on table level
		if (product == null)
		{
			throw new AdempiereException("@NotFound@ @M_Product_ID@: " + productId);
		}
		return product;
	}

	@Override
	public I_M_Product getById(final int productId)
	{
		return getById(ProductId.ofRepoId(productId), I_M_Product.class);
	}

	@Override
	public List<I_M_Product> getByIds(@NonNull final Set<ProductId> productIds)
	{
		return loadByRepoIdAwaresOutOfTrx(productIds, I_M_Product.class);
	}

	@Override
	public I_M_Product retrieveProductByValue(@NonNull final String value)
	{
		final ProductId productId = retrieveProductIdByValue(value);
		return productId != null ? getById(productId) : null;
	}

	@Override
	public ProductId retrieveProductIdByValue(@NonNull final String value)
	{
		return retrieveProductIdByValueOrNull(Env.getCtx(), value);
	}

	@Cached(cacheName = I_M_Product.Table_Name + "#ID#by#" + I_M_Product.COLUMNNAME_Value)
	public ProductId retrieveProductIdByValueOrNull(@CacheCtx final Properties ctx, @NonNull final String value)
	{
		final int productRepoId = Services.get(IQueryBL.class).createQueryBuilder(I_M_Product.class, ctx, ITrx.TRXNAME_None)
				.addEqualsFilter(I_M_Product.COLUMNNAME_Value, value)
				.addOnlyActiveRecordsFilter()
				.addOnlyContextClient(ctx)
				.create()
				.firstIdOnly();
		return ProductId.ofRepoIdOrNull(productRepoId);
	}

	@Override
	public ProductId retrieveProductIdBy(@NonNull final ProductQuery query)
	{
		final IQueryBuilder<I_M_Product> queryBuilder;
		if (query.isOutOfTrx())
		{
			queryBuilder = Services
					.get(IQueryBL.class)
					.createQueryBuilderOutOfTrx(I_M_Product.class)
					.setOption(IQuery.OPTION_ReturnReadOnlyRecords, true);
		}
		else
		{
			queryBuilder = Services
					.get(IQueryBL.class)
					.createQueryBuilder(I_M_Product.class);
		}

		if (query.isIncludeAnyOrg())
		{
			queryBuilder
					.addInArrayFilter(I_M_Product.COLUMN_AD_Org_ID, query.getOrgId(), OrgId.ANY)
					.orderByDescending(I_M_Product.COLUMN_AD_Org_ID);
		}
		else
		{
			queryBuilder.addEqualsFilter(I_M_Product.COLUMN_AD_Org_ID, query.getOrgId());
		}

		final int productRepoId = queryBuilder
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_M_Product.COLUMNNAME_Value, query.getValue())
				.create()
				.firstId();

		return ProductId.ofRepoIdOrNull(productRepoId);
	}

	@Override
	public Stream<I_M_Product> streamAllProducts()
	{
		return Services.get(IQueryBL.class).createQueryBuilderOutOfTrx(I_M_Product.class)
				.addOnlyActiveRecordsFilter()
				.orderBy(I_M_Product.COLUMNNAME_M_Product_ID)
				.create()
				.iterateAndStream();
	}

	@Override
	@Cached(cacheName = I_M_Product_Category.Table_Name + "#Default")
	public I_M_Product_Category retrieveDefaultProductCategory(@CacheCtx final Properties ctx)
	{
		final I_M_Product_Category pc = Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_Product_Category.class, ctx, ITrx.TRXNAME_None)
				.addOnlyActiveRecordsFilter()
				.orderBy()
				.addColumn(I_M_Product_Category.COLUMNNAME_IsDefault, Direction.Descending, Nulls.Last)
				.addColumn(I_M_Product_Category.COLUMNNAME_M_Product_Category_ID)
				.endOrderBy()
				.create()
				.first(I_M_Product_Category.class);
		Check.assumeNotNull(pc, "default product category shall exist");
		return pc;
	}

	@Override
	public ProductId retrieveMappedProductIdOrNull(final ProductId productId, final OrgId orgId)
	{
		final I_M_Product product = getById(productId);
		final IProductMappingAware productMappingAware = InterfaceWrapperHelper.asColumnReferenceAwareOrNull(product, IProductMappingAware.class);
		if (productMappingAware.getM_Product_Mapping_ID() <= 0)
		{
			return null;
		}
		if (!productMappingAware.getM_Product_Mapping().isActive())
		{
			return null;
		}

		return Services.get(IQueryBL.class).createQueryBuilderOutOfTrx(I_M_Product.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(IProductMappingAware.COLUMNNAME_M_Product_Mapping_ID, productMappingAware.getM_Product_Mapping_ID())
				.addEqualsFilter(I_M_Product.COLUMN_AD_Org_ID, orgId)
				.create()
				.firstIdOnly(ProductId::ofRepoIdOrNull);
	}

	@Override
	public List<de.metas.product.model.I_M_Product> retrieveAllMappedProducts(final I_M_Product product)
	{
		final IProductMappingAware productMappingAware = InterfaceWrapperHelper.asColumnReferenceAwareOrNull(product, IProductMappingAware.class);
		if (productMappingAware.getM_Product_Mapping_ID() <= 0)
		{
			return Collections.emptyList();
		}
		if (!productMappingAware.getM_Product_Mapping().isActive())
		{
			return Collections.emptyList();
		}

		return Services.get(IQueryBL.class).createQueryBuilder(de.metas.product.model.I_M_Product.class, product)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(IProductMappingAware.COLUMNNAME_M_Product_Mapping_ID, productMappingAware.getM_Product_Mapping_ID())
				.addNotEqualsFilter(I_M_Product.COLUMNNAME_M_Product_ID, product.getM_Product_ID())
				.create()
				.list(de.metas.product.model.I_M_Product.class);
	}

	@Override
	public ProductCategoryId retrieveProductCategoryByProductId(final ProductId productId)
	{
		if (productId == null)
		{
			return null;
		}

		final I_M_Product product = getById(productId);
		return product != null && product.isActive() ? ProductCategoryId.ofRepoId(product.getM_Product_Category_ID()) : null;
	}

	@Override
	public ProductAndCategoryId retrieveProductAndCategoryIdByProductId(@NonNull final ProductId productId)
	{
		final ProductCategoryId productCategoryId = retrieveProductCategoryByProductId(productId);
		return productCategoryId != null ? ProductAndCategoryId.of(productId, productCategoryId) : null;
	}

	@Override
	public ProductAndCategoryAndManufacturerId retrieveProductAndCategoryAndManufacturerByProductId(@NonNull final ProductId productId)
	{
		final I_M_Product product = getById(productId);
		if (!product.isActive())
		{
			throw new AdempiereException("Cannot retrieve product category and manufacturer because product is not active: " + product);
		}

		return createProductAndCategoryAndManufacturerId(product);
	}

	@Override
	public String retrieveProductValueByProductId(@NonNull final ProductId productId)
	{
		final I_M_Product product = getById(productId);
		return product.getValue();
	}

	@Override
	public Set<ProductAndCategoryAndManufacturerId> retrieveProductAndCategoryAndManufacturersByProductIds(final Set<ProductId> productIds)
	{
		return loadByIdsOutOfTrx(ProductId.toRepoIds(productIds), I_M_Product.class)
				.stream()
				.map(this::createProductAndCategoryAndManufacturerId)
				.collect(ImmutableSet.toImmutableSet());
	}

	private ProductAndCategoryAndManufacturerId createProductAndCategoryAndManufacturerId(final I_M_Product product)
	{
		return ProductAndCategoryAndManufacturerId.of(product.getM_Product_ID(), product.getM_Product_Category_ID(), product.getManufacturer_ID());
	}

	@Override
	public I_M_Product_Category getProductCategoryById(@NonNull final ProductCategoryId id)
	{
		return getProductCategoryById(id, I_M_Product_Category.class);
	}

	@Override
	public <T extends I_M_Product_Category> T getProductCategoryById(@NonNull final ProductCategoryId id, final Class<T> modelClass)
	{
		return loadOutOfTrx(id, modelClass);
	}

	@Override
	public String getProductCategoryNameById(@NonNull final ProductCategoryId id)
	{
		return getProductCategoryById(id).getName();
	}

	@Override
	public Stream<I_M_Product_Category> streamAllProductCategories()
	{
		return Services.get(IQueryBL.class).createQueryBuilderOutOfTrx(I_M_Product_Category.class)
				.addOnlyActiveRecordsFilter()
				.orderBy(I_M_Product_Category.COLUMN_M_Product_Category_ID)
				.create()
				.iterateAndStream();
	}

	@Cached(cacheName = I_M_Product.Table_Name + "#by#" + I_M_Product.COLUMNNAME_S_Resource_ID)
	@Override
	public ProductId getProductIdByResourceId(@NonNull final ResourceId resourceId)
	{
		final ProductId productId = Services.get(IQueryBL.class)
				.createQueryBuilderOutOfTrx(I_M_Product.class)
				.addEqualsFilter(I_M_Product.COLUMN_S_Resource_ID, resourceId)
				.addOnlyActiveRecordsFilter()
				.create()
				.firstIdOnly(ProductId::ofRepoIdOrNull);
		if (productId == null)
		{
			throw new AdempiereException("No product found for " + resourceId);
		}
		return productId;
	}

	@Override
	public void updateProductsByResourceIds(@NonNull final Set<ResourceId> resourceIds, @NonNull final Consumer<I_M_Product> productUpdater)
	{
		updateProductsByResourceIds(resourceIds, (resourceId, product) -> {
			if (product != null)
			{
				productUpdater.accept(product);
			}
		});
	}

	@Override
	public void updateProductsByResourceIds(@NonNull final Set<ResourceId> resourceIds, @NonNull final BiConsumer<ResourceId, I_M_Product> productUpdater)
	{
		Check.assumeNotEmpty(resourceIds, "resourceIds is not empty");

		final Set<ProductId> productIds = Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_Product.class) // in trx!
				.addInArrayFilter(I_M_Product.COLUMN_S_Resource_ID, resourceIds)
				.create()
				.listIds(ProductId::ofRepoId);
		if (productIds.isEmpty())
		{
			return;
		}

		final Map<ResourceId, I_M_Product> productsByResourceId = Maps.uniqueIndex(
				loadByRepoIdAwares(productIds, I_M_Product.class),
				product -> ResourceId.ofRepoId(product.getS_Resource_ID()));

		resourceIds.forEach(resourceId -> {
			final I_M_Product product = productsByResourceId.get(resourceId); // might be null
			productUpdater.accept(resourceId, product);
			saveRecord(product);
		});
	}

	@Override
	public void deleteProductByResourceId(@NonNull final ResourceId resourceId)
	{
		Services.get(IQueryBL.class)
				.createQueryBuilder(I_M_Product.class) // in trx
				.addEqualsFilter(I_M_Product.COLUMN_S_Resource_ID, resourceId)
				.addOnlyActiveRecordsFilter()
				.addOnlyContextClient()
				.create()
				.delete();
	}

	@Override
	public I_M_Product createProduct(@NonNull final CreateProductRequest request)
	{
		final I_M_Product product = newInstance(I_M_Product.class);
		if (request.getProductValue() != null)
		{
			product.setValue(request.getProductValue());
		}
		product.setName(request.getProductName());
		product.setM_Product_Category_ID(request.getProductCategoryId().getRepoId());
		product.setProductType(request.getProductType());
		product.setC_UOM_ID(request.getUomId().getRepoId());
		product.setIsPurchased(request.isPurchased());
		product.setIsSold(request.isSold());

		if (request.getBomVerified() != null)
		{
			product.setIsVerified(request.getBomVerified());
		}

		if (request.getPlanningSchemaSelector() != null)
		{
			product.setM_ProductPlanningSchema_Selector(request.getPlanningSchemaSelector().getCode());
		}

		saveRecord(product);

		return product;
	}
}
