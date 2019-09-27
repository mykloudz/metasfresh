package de.metas.product;

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

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.compiere.model.I_M_Product;
import org.compiere.model.I_M_Product_Category;

import de.metas.organization.OrgId;
import de.metas.util.ISingletonService;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;

public interface IProductDAO extends ISingletonService
{
	<T extends I_M_Product> T getById(ProductId productId, Class<T> productClass);

	I_M_Product getById(ProductId productId);

	I_M_Product getById(final int productId);

	List<I_M_Product> getByIds(final Set<ProductId> productIds);

	/**
	 * @return default product category; never returns null
	 */
	I_M_Product_Category retrieveDefaultProductCategory(Properties ctx);

	/**
	 *
	 *
	 * @param productId
	 * @param orgId
	 * @return the product of the given <code>org</code> that is mapped to the given <code>product</code> or <code>null</code> if the given product references no mapping, or the mapping is not active
	 *         or if there is no pendant in the given <code>org</code>.
	 * @task http://dewiki908/mediawiki/index.php/09700_Counter_Documents_%28100691234288%29
	 */
	ProductId retrieveMappedProductIdOrNull(ProductId productId, OrgId orgId);

	/**
	 * Retrieve all the products from all the organizations that have the same mapping as the given product
	 *
	 * @param product
	 * @return list of the products if found, empty list otherwise
	 */
	List<de.metas.product.model.I_M_Product> retrieveAllMappedProducts(I_M_Product product);

	I_M_Product retrieveProductByValue(String value);

	ProductId retrieveProductIdByValue(String value);

	ProductId retrieveProductIdBy(ProductQuery query);

	@Value
	@Builder
	public static class ProductQuery
	{
		@NonNull
		String value;

		@NonNull
		OrgId orgId;

		@Default
		boolean includeAnyOrg = false;

		@Default
		boolean outOfTrx = false;
	}

	Stream<I_M_Product> streamAllProducts();

	/** @return product category or null */
	ProductCategoryId retrieveProductCategoryByProductId(ProductId productId);

	ProductAndCategoryId retrieveProductAndCategoryIdByProductId(ProductId productId);

	ProductAndCategoryAndManufacturerId retrieveProductAndCategoryAndManufacturerByProductId(ProductId productId);

	Set<ProductAndCategoryAndManufacturerId> retrieveProductAndCategoryAndManufacturersByProductIds(Set<ProductId> productIds);

	String retrieveProductValueByProductId(ProductId productId);

	I_M_Product_Category getProductCategoryById(ProductCategoryId id);

	<T extends I_M_Product_Category> T getProductCategoryById(ProductCategoryId id, Class<T> modelClass);

	Stream<I_M_Product_Category> streamAllProductCategories();
	
	String getProductCategoryNameById(ProductCategoryId id);

	ProductId getProductIdByResourceId(ResourceId resourceId);

	void updateProductsByResourceIds(Set<ResourceId> resourceIds, Consumer<I_M_Product> productUpdater);

	void updateProductsByResourceIds(Set<ResourceId> resourceIds, BiConsumer<ResourceId, I_M_Product> productUpdater);

	void deleteProductByResourceId(ResourceId resourceId);

	I_M_Product createProduct(CreateProductRequest request);
}
