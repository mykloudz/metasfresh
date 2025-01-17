package de.metas.handlingunits.model.validator;

import org.adempiere.ad.callout.annotations.Callout;
import org.adempiere.ad.callout.annotations.CalloutMethod;
import org.adempiere.ad.callout.api.ICalloutField;
import org.adempiere.ad.callout.spi.IProgramaticCalloutProvider;
import org.adempiere.ad.modelvalidator.annotations.Init;
import org.adempiere.ad.modelvalidator.annotations.ModelChange;
import org.adempiere.ad.modelvalidator.annotations.Validator;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.ModelValidator;

import de.metas.handlingunits.IHUCapacityBL;
import de.metas.handlingunits.IHUPIItemProductBL;
import de.metas.handlingunits.model.I_M_HU_PI_Item_Product;
import de.metas.i18n.IMsgBL;
import de.metas.product.IProductBL;
import de.metas.product.ProductId;
import de.metas.uom.UomId;
import de.metas.util.Services;

@Callout(I_M_HU_PI_Item_Product.class)
@Validator(I_M_HU_PI_Item_Product.class)
public class M_HU_PI_Item_Product
{

	private static final String MSG_QUANTITY_INVALID = "de.metas.handlingunits.InvalidQuantity";

	@Init
	public void setupCallouts()
	{
		final IProgramaticCalloutProvider calloutProvider = Services.get(IProgramaticCalloutProvider.class);
		calloutProvider.registerAnnotatedCallout(this);
	}

	@ModelChange(timings = {
			ModelValidator.TYPE_BEFORE_NEW,
			ModelValidator.TYPE_BEFORE_CHANGE
	})
	public void beforeSave_M_HU_PI_Item_Product(final I_M_HU_PI_Item_Product itemProduct)
	{
		updateItemProduct(itemProduct);

		setUOMItemProduct(itemProduct);

		Services.get(IHUPIItemProductBL.class).setNameAndDescription(itemProduct);

		// Validate the item product only if is saved.
		validateItemProduct(itemProduct);

	}

	@CalloutMethod(columnNames = {
			I_M_HU_PI_Item_Product.COLUMNNAME_M_HU_PI_Item_ID,
			I_M_HU_PI_Item_Product.COLUMNNAME_Qty,
			I_M_HU_PI_Item_Product.COLUMNNAME_IsInfiniteCapacity
	})
	public void onManualChange_M_HU_PI_Item_Product(final I_M_HU_PI_Item_Product itemProduct, final ICalloutField field)
	{
		updateItemProduct(itemProduct);

		setUOMItemProduct(itemProduct);

		Services.get(IHUPIItemProductBL.class).setNameAndDescription(itemProduct);
	}

	private void updateItemProduct(final I_M_HU_PI_Item_Product itemProduct)
	{
		if (itemProduct.isAllowAnyProduct())
		{
			itemProduct.setM_Product_ID(-1);
			itemProduct.setIsInfiniteCapacity(true);
		}
	}

	private void setUOMItemProduct(final I_M_HU_PI_Item_Product huPiItemProduct)
	{
		final ProductId productId = ProductId.ofRepoIdOrNull(huPiItemProduct.getM_Product_ID());
		if (productId == null)
		{
			// nothing to do
			return;
		}

		final UomId stockingUOMId = Services.get(IProductBL.class).getStockingUOMId(productId);
		huPiItemProduct.setC_UOM_ID(stockingUOMId.getRepoId());
	}

	private void validateItemProduct(final I_M_HU_PI_Item_Product itemProduct)
	{
		if (Services.get(IHUCapacityBL.class).isValidItemProduct(itemProduct))
		{
			return;
		}

		final String errorMsg = Services.get(IMsgBL.class).getMsg(InterfaceWrapperHelper.getCtx(itemProduct), M_HU_PI_Item_Product.MSG_QUANTITY_INVALID);
		throw new AdempiereException(errorMsg);
	}

}
