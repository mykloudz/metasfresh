package org.adempiere.mm.attributes.api;

import java.util.function.Predicate;

import org.adempiere.mm.attributes.AttributeId;
import org.adempiere.mm.attributes.AttributeSetInstanceId;
import org.compiere.model.I_M_Attribute;
import org.compiere.model.I_M_AttributeInstance;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.model.I_M_AttributeValue;

import com.google.common.base.Predicates;

import de.metas.product.ProductId;
import de.metas.util.ISingletonService;

/**
 * Service to create and update AttributeInstances and AttributeSetInstances.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public interface IAttributeSetInstanceBL extends ISingletonService
{
	/** Call {@link #buildDescription(I_M_AttributeSetInstance, boolean)} with verbose = false. */
	String buildDescription(I_M_AttributeSetInstance asi);

	/**
	 * Build ASI Description
	 *
	 * e.g. - Product Values - Instance Values - SerNo = #123 - Lot = \u00ab123\u00bb - GuaranteeDate = 10/25/2003
	 *
	 * @param asi may be {@code null}; in that case, an empty string is returned
	 */
	String buildDescription(I_M_AttributeSetInstance asi, boolean verboseDescription);

	/**
	 * Builds and set {@link I_M_AttributeSetInstance#COLUMNNAME_Description}.
	 *
	 * @param asi may be {@code null}; in that case, nothing is done;
	 */
	void setDescription(I_M_AttributeSetInstance asi);

	/**
	 * Creates and saves a new "empty" ASI based on the given product's attribute set.
	 *
	 * @param product
	 * @return newly created and saved ASI; never return null
	 */
	I_M_AttributeSetInstance createASI(ProductId productId);

	/**
	 * Get an existing Attribute Set Instance, create a new one if none exists yet.
	 *
	 * In case a new ASI is created, it will be saved and also set to ASI aware ({@link IAttributeSetInstanceAware#setM_AttributeSetInstance(I_M_AttributeSetInstance)}).
	 *
	 * @param asiAware
	 * @return existing ASI/newly created ASI
	 */
	I_M_AttributeSetInstance getCreateASI(IAttributeSetInstanceAware asiAware);

	/**
	 * Get/Create {@link I_M_AttributeInstance} for given <code>asi</code>. If a new ai is created, if is also saved.
	 *
	 * @param asi
	 * @param attributeId
	 * @return attribute instance; never return null
	 */
	I_M_AttributeInstance getCreateAttributeInstance(I_M_AttributeSetInstance asi, AttributeId attributeId);

	/**
	 * Convenient way to quickly create/update and save an {@link I_M_AttributeInstance} for {@link I_M_AttributeValue}.
	 *
	 * @param asi
	 * @param attributeValue attribute value to set; must be not null
	 * @return created/updated attribute instance
	 */
	I_M_AttributeInstance getCreateAttributeInstance(I_M_AttributeSetInstance asi, I_M_AttributeValue attributeValue);

	/**
	 * If both the given <code>to</code> and <code>from</code> can be converted to {@link IAttributeSetInstanceAware}s and if <code>from</code>'s ASI-aware has an M_AttributeSetInstance,
	 * then that ASI is copied/cloned to the given <code>to</code> and saved.
	 * <p>
	 * Note that <code>to</code> itself is not saved. Also note that any existing ASI which might already be referenced by <code>to</code> is discarded/ignored.
	 *
	 * @param to
	 * @param from
	 * @see IAttributeSetInstanceAwareFactoryService#createOrNull(Object)
	 */
	void cloneASI(Object to, Object from);

	default I_M_AttributeSetInstance createASIFromAttributeSet(IAttributeSet attributeSet)
	{
		return createASIFromAttributeSet(attributeSet, Predicates.alwaysTrue());
	}

	I_M_AttributeSetInstance createASIFromAttributeSet(IAttributeSet attributeSet, Predicate<I_M_Attribute> filter);

	I_M_AttributeSetInstance createASIWithASFromProductAndInsertAttributeSet(ProductId productId, IAttributeSet attributeSet);

	/**
	 * set in {@link I_M_AttributeInstance} the correct value for given <code>asi</code> and given <code>attribute</code>
	 * <br>
	 * the ai is also saved.
	 *
	 * @param asi
	 * @param attribute
	 * @param value
	 * @return
	 */
	void setAttributeInstanceValue(I_M_AttributeSetInstance asi, I_M_Attribute attribute, Object value);

	void setAttributeInstanceValue(I_M_AttributeSetInstance asi, AttributeId attributeId, Object value);

	String getASIDescriptionById(AttributeSetInstanceId asiId);

	void updateASIAttributeFromModel(String attributeCode, Object fromModel);

	boolean isStorageRelevant(I_M_AttributeInstance ai);
}
