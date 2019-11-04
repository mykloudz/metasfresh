/** Generated Model - DO NOT CHANGE */
package de.metas.handlingunits.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for M_HU_PI_Item_Product
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public class X_M_HU_PI_Item_Product extends org.compiere.model.PO implements I_M_HU_PI_Item_Product, org.compiere.model.I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 2094292850L;

    /** Standard Constructor */
    public X_M_HU_PI_Item_Product (Properties ctx, int M_HU_PI_Item_Product_ID, String trxName)
    {
      super (ctx, M_HU_PI_Item_Product_ID, trxName);
      /** if (M_HU_PI_Item_Product_ID == 0)
        {
			setIsAllowAnyProduct (false); // N
			setIsInfiniteCapacity (false); // N
			setM_HU_PI_Item_ID (0);
			setM_HU_PI_Item_Product_ID (0);
			setQty (BigDecimal.ZERO);
			setValidFrom (new Timestamp( System.currentTimeMillis() ));
        } */
    }

    /** Load Constructor */
    public X_M_HU_PI_Item_Product (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }


    /** Load Meta Data */
    @Override
    protected org.compiere.model.POInfo initPO (Properties ctx)
    {
      org.compiere.model.POInfo poi = org.compiere.model.POInfo.getPOInfo (ctx, Table_Name, get_TrxName());
      return poi;
    }

	/** Set Geschäftspartner.
		@param C_BPartner_ID 
		Bezeichnet einen Geschäftspartner
	  */
	@Override
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1) 
			set_Value (COLUMNNAME_C_BPartner_ID, null);
		else 
			set_Value (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}

	/** Get Geschäftspartner.
		@return Bezeichnet einen Geschäftspartner
	  */
	@Override
	public int getC_BPartner_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Maßeinheit.
		@param C_UOM_ID 
		Maßeinheit
	  */
	@Override
	public void setC_UOM_ID (int C_UOM_ID)
	{
		if (C_UOM_ID < 1) 
			set_Value (COLUMNNAME_C_UOM_ID, null);
		else 
			set_Value (COLUMNNAME_C_UOM_ID, Integer.valueOf(C_UOM_ID));
	}

	/** Get Maßeinheit.
		@return Maßeinheit
	  */
	@Override
	public int getC_UOM_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_UOM_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Beschreibung.
		@param Description Beschreibung	  */
	@Override
	public void setDescription (java.lang.String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Beschreibung.
		@return Beschreibung	  */
	@Override
	public java.lang.String getDescription () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Description);
	}

	/** Set TU-EAN.
		@param EAN_TU TU-EAN	  */
	@Override
	public void setEAN_TU (java.lang.String EAN_TU)
	{
		set_Value (COLUMNNAME_EAN_TU, EAN_TU);
	}

	/** Get TU-EAN.
		@return TU-EAN	  */
	@Override
	public java.lang.String getEAN_TU () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_EAN_TU);
	}

	/** Set GTIN.
		@param GTIN GTIN	  */
	@Override
	public void setGTIN (java.lang.String GTIN)
	{
		set_Value (COLUMNNAME_GTIN, GTIN);
	}

	/** Get GTIN.
		@return GTIN	  */
	@Override
	public java.lang.String getGTIN () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_GTIN);
	}

	/** Set Jedes Produkt erlauben.
		@param IsAllowAnyProduct Jedes Produkt erlauben	  */
	@Override
	public void setIsAllowAnyProduct (boolean IsAllowAnyProduct)
	{
		set_Value (COLUMNNAME_IsAllowAnyProduct, Boolean.valueOf(IsAllowAnyProduct));
	}

	/** Get Jedes Produkt erlauben.
		@return Jedes Produkt erlauben	  */
	@Override
	public boolean isAllowAnyProduct () 
	{
		Object oo = get_Value(COLUMNNAME_IsAllowAnyProduct);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Unbestimmte Kapazität.
		@param IsInfiniteCapacity Unbestimmte Kapazität	  */
	@Override
	public void setIsInfiniteCapacity (boolean IsInfiniteCapacity)
	{
		set_Value (COLUMNNAME_IsInfiniteCapacity, Boolean.valueOf(IsInfiniteCapacity));
	}

	/** Get Unbestimmte Kapazität.
		@return Unbestimmte Kapazität	  */
	@Override
	public boolean isInfiniteCapacity () 
	{
		Object oo = get_Value(COLUMNNAME_IsInfiniteCapacity);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	@Override
	public de.metas.handlingunits.model.I_M_HU_PackagingCode getM_HU_PackagingCode_LU_Fallback()
	{
		return get_ValueAsPO(COLUMNNAME_M_HU_PackagingCode_LU_Fallback_ID, de.metas.handlingunits.model.I_M_HU_PackagingCode.class);
	}

	@Override
	public void setM_HU_PackagingCode_LU_Fallback(de.metas.handlingunits.model.I_M_HU_PackagingCode M_HU_PackagingCode_LU_Fallback)
	{
		set_ValueFromPO(COLUMNNAME_M_HU_PackagingCode_LU_Fallback_ID, de.metas.handlingunits.model.I_M_HU_PackagingCode.class, M_HU_PackagingCode_LU_Fallback);
	}

	/** Set LU Fallback-Verpackungscode.
		@param M_HU_PackagingCode_LU_Fallback_ID 
		Wird benutzt wenn die Ausgabe eines LU Verpackungscodes erforderlich ist, aber in metasfresh keine HU erfasst wurde.
	  */
	@Override
	public void setM_HU_PackagingCode_LU_Fallback_ID (int M_HU_PackagingCode_LU_Fallback_ID)
	{
		if (M_HU_PackagingCode_LU_Fallback_ID < 1) 
			set_Value (COLUMNNAME_M_HU_PackagingCode_LU_Fallback_ID, null);
		else 
			set_Value (COLUMNNAME_M_HU_PackagingCode_LU_Fallback_ID, Integer.valueOf(M_HU_PackagingCode_LU_Fallback_ID));
	}

	/** Get LU Fallback-Verpackungscode.
		@return Wird benutzt wenn die Ausgabe eines LU Verpackungscodes erforderlich ist, aber in metasfresh keine HU erfasst wurde.
	  */
	@Override
	public int getM_HU_PackagingCode_LU_Fallback_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_HU_PackagingCode_LU_Fallback_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public de.metas.handlingunits.model.I_M_HU_PI_Item getM_HU_PI_Item()
	{
		return get_ValueAsPO(COLUMNNAME_M_HU_PI_Item_ID, de.metas.handlingunits.model.I_M_HU_PI_Item.class);
	}

	@Override
	public void setM_HU_PI_Item(de.metas.handlingunits.model.I_M_HU_PI_Item M_HU_PI_Item)
	{
		set_ValueFromPO(COLUMNNAME_M_HU_PI_Item_ID, de.metas.handlingunits.model.I_M_HU_PI_Item.class, M_HU_PI_Item);
	}

	/** Set Packvorschrift Position.
		@param M_HU_PI_Item_ID Packvorschrift Position	  */
	@Override
	public void setM_HU_PI_Item_ID (int M_HU_PI_Item_ID)
	{
		if (M_HU_PI_Item_ID < 1) 
			set_Value (COLUMNNAME_M_HU_PI_Item_ID, null);
		else 
			set_Value (COLUMNNAME_M_HU_PI_Item_ID, Integer.valueOf(M_HU_PI_Item_ID));
	}

	/** Get Packvorschrift Position.
		@return Packvorschrift Position	  */
	@Override
	public int getM_HU_PI_Item_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_HU_PI_Item_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Packvorschrift.
		@param M_HU_PI_Item_Product_ID Packvorschrift	  */
	@Override
	public void setM_HU_PI_Item_Product_ID (int M_HU_PI_Item_Product_ID)
	{
		if (M_HU_PI_Item_Product_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_HU_PI_Item_Product_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_HU_PI_Item_Product_ID, Integer.valueOf(M_HU_PI_Item_Product_ID));
	}

	/** Get Packvorschrift.
		@return Packvorschrift	  */
	@Override
	public int getM_HU_PI_Item_Product_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_HU_PI_Item_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Produkt.
		@param M_Product_ID 
		Produkt, Leistung, Artikel
	  */
	@Override
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_Product_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}

	/** Get Produkt.
		@return Produkt, Leistung, Artikel
	  */
	@Override
	public int getM_Product_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Name.
		@param Name Name	  */
	@Override
	public void setName (java.lang.String Name)
	{
		set_ValueNoCheck (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Name	  */
	@Override
	public java.lang.String getName () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Name);
	}

	/** Set Menge.
		@param Qty 
		Menge
	  */
	@Override
	public void setQty (java.math.BigDecimal Qty)
	{
		set_Value (COLUMNNAME_Qty, Qty);
	}

	/** Get Menge.
		@return Menge
	  */
	@Override
	public java.math.BigDecimal getQty () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Qty);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set UPC.
		@param UPC 
		Produktidentifikation (Barcode) durch Universal Product Code oder European Article Number)
	  */
	@Override
	public void setUPC (java.lang.String UPC)
	{
		set_Value (COLUMNNAME_UPC, UPC);
	}

	/** Get UPC.
		@return Produktidentifikation (Barcode) durch Universal Product Code oder European Article Number)
	  */
	@Override
	public java.lang.String getUPC () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_UPC);
	}

	/** Set Gültig ab.
		@param ValidFrom 
		Gültig ab inklusiv (erster Tag)
	  */
	@Override
	public void setValidFrom (java.sql.Timestamp ValidFrom)
	{
		set_Value (COLUMNNAME_ValidFrom, ValidFrom);
	}

	/** Get Gültig ab.
		@return Gültig ab inklusiv (erster Tag)
	  */
	@Override
	public java.sql.Timestamp getValidFrom () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_ValidFrom);
	}

	/** Set Gültig bis.
		@param ValidTo 
		Gültig bis inklusiv (letzter Tag)
	  */
	@Override
	public void setValidTo (java.sql.Timestamp ValidTo)
	{
		set_Value (COLUMNNAME_ValidTo, ValidTo);
	}

	/** Get Gültig bis.
		@return Gültig bis inklusiv (letzter Tag)
	  */
	@Override
	public java.sql.Timestamp getValidTo () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_ValidTo);
	}
}