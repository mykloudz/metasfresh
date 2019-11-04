/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for M_Product
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public class X_M_Product extends org.compiere.model.PO implements I_M_Product, org.compiere.model.I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 651513871L;

    /** Standard Constructor */
    public X_M_Product (Properties ctx, int M_Product_ID, String trxName)
    {
      super (ctx, M_Product_ID, trxName);
      /** if (M_Product_ID == 0)
        {
			setC_UOM_ID (0);
			setIsBOM (false); // N
			setIsDropShip (false);
			setIsExcludeAutoDelivery (false); // N
			setIsInvoicePrintDetails (false);
			setIsPickListPrintDetails (false);
			setIsPurchased (true); // Y
			setIsQuotationGroupping (false); // N
			setIsSelfService (true); // Y
			setIsSold (true); // Y
			setIsStocked (true); // Y
			setIsSummary (false);
			setIsVerified (false); // N
			setIsWebStoreFeatured (false);
			setLowLevel (0); // 0
			setM_AttributeSetInstance_ID (0);
			setM_Product_Category_ID (0);
			setM_Product_ID (0);
			setName (null);
			setProductType (null); // I
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_M_Product (Properties ctx, ResultSet rs, String trxName)
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

	/** Set Obligatorische Zusatzangaben.
		@param Additional_produktinfos Obligatorische Zusatzangaben	  */
	@Override
	public void setAdditional_produktinfos (java.lang.String Additional_produktinfos)
	{
		set_Value (COLUMNNAME_Additional_produktinfos, Additional_produktinfos);
	}

	/** Get Obligatorische Zusatzangaben.
		@return Obligatorische Zusatzangaben	  */
	@Override
	public java.lang.String getAdditional_produktinfos () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Additional_produktinfos);
	}

	@Override
	public org.compiere.model.I_C_RevenueRecognition getC_RevenueRecognition()
	{
		return get_ValueAsPO(COLUMNNAME_C_RevenueRecognition_ID, org.compiere.model.I_C_RevenueRecognition.class);
	}

	@Override
	public void setC_RevenueRecognition(org.compiere.model.I_C_RevenueRecognition C_RevenueRecognition)
	{
		set_ValueFromPO(COLUMNNAME_C_RevenueRecognition_ID, org.compiere.model.I_C_RevenueRecognition.class, C_RevenueRecognition);
	}

	/** Set Umsatzrealisierung.
		@param C_RevenueRecognition_ID 
		Method for recording revenue
	  */
	@Override
	public void setC_RevenueRecognition_ID (int C_RevenueRecognition_ID)
	{
		if (C_RevenueRecognition_ID < 1) 
			set_Value (COLUMNNAME_C_RevenueRecognition_ID, null);
		else 
			set_Value (COLUMNNAME_C_RevenueRecognition_ID, Integer.valueOf(C_RevenueRecognition_ID));
	}

	/** Get Umsatzrealisierung.
		@return Method for recording revenue
	  */
	@Override
	public int getC_RevenueRecognition_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_RevenueRecognition_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Maßeinheit.
		@param C_UOM_ID 
		Unit of Measure
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
		@return Unit of Measure
	  */
	@Override
	public int getC_UOM_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_UOM_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Klassifizierung.
		@param Classification 
		Classification for grouping
	  */
	@Override
	public void setClassification (java.lang.String Classification)
	{
		set_Value (COLUMNNAME_Classification, Classification);
	}

	/** Get Klassifizierung.
		@return Classification for grouping
	  */
	@Override
	public java.lang.String getClassification () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Classification);
	}

	/** Set Auszeichnungsname.
		@param CustomerLabelName Auszeichnungsname	  */
	@Override
	public void setCustomerLabelName (java.lang.String CustomerLabelName)
	{
		set_Value (COLUMNNAME_CustomerLabelName, CustomerLabelName);
	}

	/** Get Auszeichnungsname.
		@return Auszeichnungsname	  */
	@Override
	public java.lang.String getCustomerLabelName () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_CustomerLabelName);
	}

	/** Set Zolltarifnummer.
		@param CustomsTariff Zolltarifnummer	  */
	@Override
	public void setCustomsTariff (java.lang.String CustomsTariff)
	{
		set_Value (COLUMNNAME_CustomsTariff, CustomsTariff);
	}

	/** Get Zolltarifnummer.
		@return Zolltarifnummer	  */
	@Override
	public java.lang.String getCustomsTariff () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_CustomsTariff);
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

	/** Set Beschreibungs-URL.
		@param DescriptionURL 
		URL for the description
	  */
	@Override
	public void setDescriptionURL (java.lang.String DescriptionURL)
	{
		set_Value (COLUMNNAME_DescriptionURL, DescriptionURL);
	}

	/** Get Beschreibungs-URL.
		@return URL for the description
	  */
	@Override
	public java.lang.String getDescriptionURL () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_DescriptionURL);
	}

	/** Set Eingestellt.
		@param Discontinued 
		This product is no longer available
	  */
	@Override
	public void setDiscontinued (boolean Discontinued)
	{
		set_Value (COLUMNNAME_Discontinued, Boolean.valueOf(Discontinued));
	}

	/** Get Eingestellt.
		@return This product is no longer available
	  */
	@Override
	public boolean isDiscontinued () 
	{
		Object oo = get_Value(COLUMNNAME_Discontinued);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Eingestellt durch.
		@param DiscontinuedBy 
		Discontinued By
	  */
	@Override
	public void setDiscontinuedBy (java.sql.Timestamp DiscontinuedBy)
	{
		set_Value (COLUMNNAME_DiscontinuedBy, DiscontinuedBy);
	}

	/** Get Eingestellt durch.
		@return Discontinued By
	  */
	@Override
	public java.sql.Timestamp getDiscontinuedBy () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_DiscontinuedBy);
	}

	/** Set Notiz / Zeilentext.
		@param DocumentNote 
		Additional information for a Document
	  */
	@Override
	public void setDocumentNote (java.lang.String DocumentNote)
	{
		set_Value (COLUMNNAME_DocumentNote, DocumentNote);
	}

	/** Get Notiz / Zeilentext.
		@return Additional information for a Document
	  */
	@Override
	public java.lang.String getDocumentNote () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_DocumentNote);
	}

	/** Set External ID.
		@param ExternalId External ID	  */
	@Override
	public void setExternalId (java.lang.String ExternalId)
	{
		set_Value (COLUMNNAME_ExternalId, ExternalId);
	}

	/** Get External ID.
		@return External ID	  */
	@Override
	public java.lang.String getExternalId () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_ExternalId);
	}

	/** Set Group1.
		@param Group1 Group1	  */
	@Override
	public void setGroup1 (java.lang.String Group1)
	{
		set_Value (COLUMNNAME_Group1, Group1);
	}

	/** Get Group1.
		@return Group1	  */
	@Override
	public java.lang.String getGroup1 () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Group1);
	}

	/** Set Group2.
		@param Group2 Group2	  */
	@Override
	public void setGroup2 (java.lang.String Group2)
	{
		set_Value (COLUMNNAME_Group2, Group2);
	}

	/** Get Group2.
		@return Group2	  */
	@Override
	public java.lang.String getGroup2 () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Group2);
	}

	/** 
	 * GroupCompensationAmtType AD_Reference_ID=540759
	 * Reference name: GroupCompensationAmtType
	 */
	public static final int GROUPCOMPENSATIONAMTTYPE_AD_Reference_ID=540759;
	/** Percent = P */
	public static final String GROUPCOMPENSATIONAMTTYPE_Percent = "P";
	/** PriceAndQty = Q */
	public static final String GROUPCOMPENSATIONAMTTYPE_PriceAndQty = "Q";
	/** Set Compensation Amount Type.
		@param GroupCompensationAmtType Compensation Amount Type	  */
	@Override
	public void setGroupCompensationAmtType (java.lang.String GroupCompensationAmtType)
	{

		set_Value (COLUMNNAME_GroupCompensationAmtType, GroupCompensationAmtType);
	}

	/** Get Compensation Amount Type.
		@return Compensation Amount Type	  */
	@Override
	public java.lang.String getGroupCompensationAmtType () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_GroupCompensationAmtType);
	}

	/** 
	 * GroupCompensationType AD_Reference_ID=540758
	 * Reference name: GroupCompensationType
	 */
	public static final int GROUPCOMPENSATIONTYPE_AD_Reference_ID=540758;
	/** Surcharge = S */
	public static final String GROUPCOMPENSATIONTYPE_Surcharge = "S";
	/** Discount = D */
	public static final String GROUPCOMPENSATIONTYPE_Discount = "D";
	/** Set Compensation Type.
		@param GroupCompensationType Compensation Type	  */
	@Override
	public void setGroupCompensationType (java.lang.String GroupCompensationType)
	{

		set_Value (COLUMNNAME_GroupCompensationType, GroupCompensationType);
	}

	/** Get Compensation Type.
		@return Compensation Type	  */
	@Override
	public java.lang.String getGroupCompensationType () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_GroupCompensationType);
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

	/** Set Min. Garantie-Tage.
		@param GuaranteeDaysMin 
		Minumum number of guarantee days
	  */
	@Override
	public void setGuaranteeDaysMin (int GuaranteeDaysMin)
	{
		set_Value (COLUMNNAME_GuaranteeDaysMin, Integer.valueOf(GuaranteeDaysMin));
	}

	/** Get Min. Garantie-Tage.
		@return Minumum number of guarantee days
	  */
	@Override
	public int getGuaranteeDaysMin () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GuaranteeDaysMin);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Kommentar/Hilfe.
		@param Help 
		Comment or Hint
	  */
	@Override
	public void setHelp (java.lang.String Help)
	{
		set_Value (COLUMNNAME_Help, Help);
	}

	/** Get Kommentar/Hilfe.
		@return Comment or Hint
	  */
	@Override
	public java.lang.String getHelp () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Help);
	}

	/** Set Bild-URL.
		@param ImageURL 
		URL of  image
	  */
	@Override
	public void setImageURL (java.lang.String ImageURL)
	{
		set_Value (COLUMNNAME_ImageURL, ImageURL);
	}

	/** Get Bild-URL.
		@return URL of  image
	  */
	@Override
	public java.lang.String getImageURL () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_ImageURL);
	}

	/** Set Zutaten.
		@param Ingredients Zutaten	  */
	@Override
	public void setIngredients (java.lang.String Ingredients)
	{
		set_Value (COLUMNNAME_Ingredients, Ingredients);
	}

	/** Get Zutaten.
		@return Zutaten	  */
	@Override
	public java.lang.String getIngredients () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Ingredients);
	}

	/** Set Stückliste.
		@param IsBOM 
		Bill of Materials
	  */
	@Override
	public void setIsBOM (boolean IsBOM)
	{
		set_Value (COLUMNNAME_IsBOM, Boolean.valueOf(IsBOM));
	}

	/** Get Stückliste.
		@return Bill of Materials
	  */
	@Override
	public boolean isBOM () 
	{
		Object oo = get_Value(COLUMNNAME_IsBOM);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Streckengeschäft.
		@param IsDropShip 
		Drop Shipments are sent from the Vendor directly to the Customer
	  */
	@Override
	public void setIsDropShip (boolean IsDropShip)
	{
		set_Value (COLUMNNAME_IsDropShip, Boolean.valueOf(IsDropShip));
	}

	/** Get Streckengeschäft.
		@return Drop Shipments are sent from the Vendor directly to the Customer
	  */
	@Override
	public boolean isDropShip () 
	{
		Object oo = get_Value(COLUMNNAME_IsDropShip);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Ausnehmen von Automatischer Lieferung.
		@param IsExcludeAutoDelivery 
		Exclude from automatic Delivery
	  */
	@Override
	public void setIsExcludeAutoDelivery (boolean IsExcludeAutoDelivery)
	{
		set_Value (COLUMNNAME_IsExcludeAutoDelivery, Boolean.valueOf(IsExcludeAutoDelivery));
	}

	/** Get Ausnehmen von Automatischer Lieferung.
		@return Exclude from automatic Delivery
	  */
	@Override
	public boolean isExcludeAutoDelivery () 
	{
		Object oo = get_Value(COLUMNNAME_IsExcludeAutoDelivery);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Print detail records on invoice .
		@param IsInvoicePrintDetails 
		Print detail BOM elements on the invoice
	  */
	@Override
	public void setIsInvoicePrintDetails (boolean IsInvoicePrintDetails)
	{
		set_Value (COLUMNNAME_IsInvoicePrintDetails, Boolean.valueOf(IsInvoicePrintDetails));
	}

	/** Get Print detail records on invoice .
		@return Print detail BOM elements on the invoice
	  */
	@Override
	public boolean isInvoicePrintDetails () 
	{
		Object oo = get_Value(COLUMNNAME_IsInvoicePrintDetails);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Wird produziert.
		@param IsManufactured Wird produziert	  */
	@Override
	public void setIsManufactured (boolean IsManufactured)
	{
		throw new IllegalArgumentException ("IsManufactured is virtual column");	}

	/** Get Wird produziert.
		@return Wird produziert	  */
	@Override
	public boolean isManufactured () 
	{
		Object oo = get_Value(COLUMNNAME_IsManufactured);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Detaileinträge auf Kommissionierschein drucken.
		@param IsPickListPrintDetails 
		Print detail BOM elements on the pick list
	  */
	@Override
	public void setIsPickListPrintDetails (boolean IsPickListPrintDetails)
	{
		set_Value (COLUMNNAME_IsPickListPrintDetails, Boolean.valueOf(IsPickListPrintDetails));
	}

	/** Get Detaileinträge auf Kommissionierschein drucken.
		@return Print detail BOM elements on the pick list
	  */
	@Override
	public boolean isPickListPrintDetails () 
	{
		Object oo = get_Value(COLUMNNAME_IsPickListPrintDetails);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Wird Eingekauft.
		@param IsPurchased Wird Eingekauft	  */
	@Override
	public void setIsPurchased (boolean IsPurchased)
	{
		set_Value (COLUMNNAME_IsPurchased, Boolean.valueOf(IsPurchased));
	}

	/** Get Wird Eingekauft.
		@return Wird Eingekauft	  */
	@Override
	public boolean isPurchased () 
	{
		Object oo = get_Value(COLUMNNAME_IsPurchased);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Ist Angebotsgruppe.
		@param IsQuotationGroupping Ist Angebotsgruppe	  */
	@Override
	public void setIsQuotationGroupping (boolean IsQuotationGroupping)
	{
		set_Value (COLUMNNAME_IsQuotationGroupping, Boolean.valueOf(IsQuotationGroupping));
	}

	/** Get Ist Angebotsgruppe.
		@return Ist Angebotsgruppe	  */
	@Override
	public boolean isQuotationGroupping () 
	{
		Object oo = get_Value(COLUMNNAME_IsQuotationGroupping);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Selbstbedienung.
		@param IsSelfService 
		This is a Self-Service entry or this entry can be changed via Self-Service
	  */
	@Override
	public void setIsSelfService (boolean IsSelfService)
	{
		set_Value (COLUMNNAME_IsSelfService, Boolean.valueOf(IsSelfService));
	}

	/** Get Selbstbedienung.
		@return This is a Self-Service entry or this entry can be changed via Self-Service
	  */
	@Override
	public boolean isSelfService () 
	{
		Object oo = get_Value(COLUMNNAME_IsSelfService);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Verkauft.
		@param IsSold 
		Organization sells this product
	  */
	@Override
	public void setIsSold (boolean IsSold)
	{
		set_Value (COLUMNNAME_IsSold, Boolean.valueOf(IsSold));
	}

	/** Get Verkauft.
		@return Organization sells this product
	  */
	@Override
	public boolean isSold () 
	{
		Object oo = get_Value(COLUMNNAME_IsSold);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Lagerhaltig.
		@param IsStocked 
		Organization stocks this product
	  */
	@Override
	public void setIsStocked (boolean IsStocked)
	{
		set_Value (COLUMNNAME_IsStocked, Boolean.valueOf(IsStocked));
	}

	/** Get Lagerhaltig.
		@return Organization stocks this product
	  */
	@Override
	public boolean isStocked () 
	{
		Object oo = get_Value(COLUMNNAME_IsStocked);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Zusammenfassungseintrag.
		@param IsSummary 
		This is a summary entity
	  */
	@Override
	public void setIsSummary (boolean IsSummary)
	{
		set_Value (COLUMNNAME_IsSummary, Boolean.valueOf(IsSummary));
	}

	/** Get Zusammenfassungseintrag.
		@return This is a summary entity
	  */
	@Override
	public boolean isSummary () 
	{
		Object oo = get_Value(COLUMNNAME_IsSummary);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Verified.
		@param IsVerified 
		The BOM configuration has been verified
	  */
	@Override
	public void setIsVerified (boolean IsVerified)
	{
		set_ValueNoCheck (COLUMNNAME_IsVerified, Boolean.valueOf(IsVerified));
	}

	/** Get Verified.
		@return The BOM configuration has been verified
	  */
	@Override
	public boolean isVerified () 
	{
		Object oo = get_Value(COLUMNNAME_IsVerified);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Beworben im Web-Shop.
		@param IsWebStoreFeatured 
		If selected, the product is displayed in the inital or any empy search
	  */
	@Override
	public void setIsWebStoreFeatured (boolean IsWebStoreFeatured)
	{
		set_Value (COLUMNNAME_IsWebStoreFeatured, Boolean.valueOf(IsWebStoreFeatured));
	}

	/** Get Beworben im Web-Shop.
		@return If selected, the product is displayed in the inital or any empy search
	  */
	@Override
	public boolean isWebStoreFeatured () 
	{
		Object oo = get_Value(COLUMNNAME_IsWebStoreFeatured);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Low Level.
		@param LowLevel Low Level	  */
	@Override
	public void setLowLevel (int LowLevel)
	{
		set_Value (COLUMNNAME_LowLevel, Integer.valueOf(LowLevel));
	}

	/** Get Low Level.
		@return Low Level	  */
	@Override
	public int getLowLevel () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LowLevel);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_M_AttributeSet getM_AttributeSet()
	{
		return get_ValueAsPO(COLUMNNAME_M_AttributeSet_ID, org.compiere.model.I_M_AttributeSet.class);
	}

	@Override
	public void setM_AttributeSet(org.compiere.model.I_M_AttributeSet M_AttributeSet)
	{
		set_ValueFromPO(COLUMNNAME_M_AttributeSet_ID, org.compiere.model.I_M_AttributeSet.class, M_AttributeSet);
	}

	/** Set Merkmals-Satz.
		@param M_AttributeSet_ID 
		Product Attribute Set
	  */
	@Override
	public void setM_AttributeSet_ID (int M_AttributeSet_ID)
	{
		if (M_AttributeSet_ID < 0) 
			set_Value (COLUMNNAME_M_AttributeSet_ID, null);
		else 
			set_Value (COLUMNNAME_M_AttributeSet_ID, Integer.valueOf(M_AttributeSet_ID));
	}

	/** Get Merkmals-Satz.
		@return Product Attribute Set
	  */
	@Override
	public int getM_AttributeSet_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_AttributeSet_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_M_AttributeSetInstance getM_AttributeSetInstance()
	{
		return get_ValueAsPO(COLUMNNAME_M_AttributeSetInstance_ID, org.compiere.model.I_M_AttributeSetInstance.class);
	}

	@Override
	public void setM_AttributeSetInstance(org.compiere.model.I_M_AttributeSetInstance M_AttributeSetInstance)
	{
		set_ValueFromPO(COLUMNNAME_M_AttributeSetInstance_ID, org.compiere.model.I_M_AttributeSetInstance.class, M_AttributeSetInstance);
	}

	/** Set Merkmale.
		@param M_AttributeSetInstance_ID 
		Merkmals Ausprägungen zum Produkt
	  */
	@Override
	public void setM_AttributeSetInstance_ID (int M_AttributeSetInstance_ID)
	{
		if (M_AttributeSetInstance_ID < 0) 
			set_Value (COLUMNNAME_M_AttributeSetInstance_ID, null);
		else 
			set_Value (COLUMNNAME_M_AttributeSetInstance_ID, Integer.valueOf(M_AttributeSetInstance_ID));
	}

	/** Get Merkmale.
		@return Merkmals Ausprägungen zum Produkt
	  */
	@Override
	public int getM_AttributeSetInstance_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_AttributeSetInstance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_M_CustomsTariff getM_CustomsTariff()
	{
		return get_ValueAsPO(COLUMNNAME_M_CustomsTariff_ID, org.compiere.model.I_M_CustomsTariff.class);
	}

	@Override
	public void setM_CustomsTariff(org.compiere.model.I_M_CustomsTariff M_CustomsTariff)
	{
		set_ValueFromPO(COLUMNNAME_M_CustomsTariff_ID, org.compiere.model.I_M_CustomsTariff.class, M_CustomsTariff);
	}

	/** Set Customs Tariff.
		@param M_CustomsTariff_ID Customs Tariff	  */
	@Override
	public void setM_CustomsTariff_ID (int M_CustomsTariff_ID)
	{
		if (M_CustomsTariff_ID < 1) 
			set_Value (COLUMNNAME_M_CustomsTariff_ID, null);
		else 
			set_Value (COLUMNNAME_M_CustomsTariff_ID, Integer.valueOf(M_CustomsTariff_ID));
	}

	/** Get Customs Tariff.
		@return Customs Tariff	  */
	@Override
	public int getM_CustomsTariff_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_CustomsTariff_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_M_FreightCategory getM_FreightCategory()
	{
		return get_ValueAsPO(COLUMNNAME_M_FreightCategory_ID, org.compiere.model.I_M_FreightCategory.class);
	}

	@Override
	public void setM_FreightCategory(org.compiere.model.I_M_FreightCategory M_FreightCategory)
	{
		set_ValueFromPO(COLUMNNAME_M_FreightCategory_ID, org.compiere.model.I_M_FreightCategory.class, M_FreightCategory);
	}

	/** Set Fracht-Kategorie.
		@param M_FreightCategory_ID 
		Category of the Freight
	  */
	@Override
	public void setM_FreightCategory_ID (int M_FreightCategory_ID)
	{
		if (M_FreightCategory_ID < 1) 
			set_Value (COLUMNNAME_M_FreightCategory_ID, null);
		else 
			set_Value (COLUMNNAME_M_FreightCategory_ID, Integer.valueOf(M_FreightCategory_ID));
	}

	/** Get Fracht-Kategorie.
		@return Category of the Freight
	  */
	@Override
	public int getM_FreightCategory_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_FreightCategory_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Lagerort.
		@param M_Locator_ID 
		Warehouse Locator
	  */
	@Override
	public void setM_Locator_ID (int M_Locator_ID)
	{
		if (M_Locator_ID < 1) 
			set_Value (COLUMNNAME_M_Locator_ID, null);
		else 
			set_Value (COLUMNNAME_M_Locator_ID, Integer.valueOf(M_Locator_ID));
	}

	/** Get Lagerort.
		@return Warehouse Locator
	  */
	@Override
	public int getM_Locator_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Locator_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Produkt Kategorie.
		@param M_Product_Category_ID 
		Kategorie eines Produktes
	  */
	@Override
	public void setM_Product_Category_ID (int M_Product_Category_ID)
	{
		if (M_Product_Category_ID < 1) 
			set_Value (COLUMNNAME_M_Product_Category_ID, null);
		else 
			set_Value (COLUMNNAME_M_Product_Category_ID, Integer.valueOf(M_Product_Category_ID));
	}

	/** Get Produkt Kategorie.
		@return Kategorie eines Produktes
	  */
	@Override
	public int getM_Product_Category_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_Category_ID);
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

	/** 
	 * M_ProductPlanningSchema_Selector AD_Reference_ID=540829
	 * Reference name: M_ProductPlanningSchema_Selector_List
	 */
	public static final int M_PRODUCTPLANNINGSCHEMA_SELECTOR_AD_Reference_ID=540829;
	/** Normal = N */
	public static final String M_PRODUCTPLANNINGSCHEMA_SELECTOR_Normal = "N";
	/** QuotationBOMProduct = Q */
	public static final String M_PRODUCTPLANNINGSCHEMA_SELECTOR_QuotationBOMProduct = "Q";
	/** Set M_ProductPlanningSchema_Selector.
		@param M_ProductPlanningSchema_Selector M_ProductPlanningSchema_Selector	  */
	@Override
	public void setM_ProductPlanningSchema_Selector (java.lang.String M_ProductPlanningSchema_Selector)
	{

		set_Value (COLUMNNAME_M_ProductPlanningSchema_Selector, M_ProductPlanningSchema_Selector);
	}

	/** Get M_ProductPlanningSchema_Selector.
		@return M_ProductPlanningSchema_Selector	  */
	@Override
	public java.lang.String getM_ProductPlanningSchema_Selector () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_M_ProductPlanningSchema_Selector);
	}

	/** Set Hersteller.
		@param Manufacturer_ID 
		Hersteller des Produktes
	  */
	@Override
	public void setManufacturer_ID (int Manufacturer_ID)
	{
		if (Manufacturer_ID < 1) 
			set_Value (COLUMNNAME_Manufacturer_ID, null);
		else 
			set_Value (COLUMNNAME_Manufacturer_ID, Integer.valueOf(Manufacturer_ID));
	}

	/** Get Hersteller.
		@return Hersteller des Produktes
	  */
	@Override
	public int getManufacturer_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Manufacturer_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** 
	 * MRP_Exclude AD_Reference_ID=319
	 * Reference name: _YesNo
	 */
	public static final int MRP_EXCLUDE_AD_Reference_ID=319;
	/** Yes = Y */
	public static final String MRP_EXCLUDE_Yes = "Y";
	/** No = N */
	public static final String MRP_EXCLUDE_No = "N";
	/** Set MRP ausschliessen.
		@param MRP_Exclude MRP ausschliessen	  */
	@Override
	public void setMRP_Exclude (java.lang.String MRP_Exclude)
	{

		set_Value (COLUMNNAME_MRP_Exclude, MRP_Exclude);
	}

	/** Get MRP ausschliessen.
		@return MRP ausschliessen	  */
	@Override
	public java.lang.String getMRP_Exclude () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_MRP_Exclude);
	}

	/** Set Name.
		@param Name Name	  */
	@Override
	public void setName (java.lang.String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Name	  */
	@Override
	public java.lang.String getName () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Name);
	}

	/** Set Verpackungseinheit.
		@param Package_UOM_ID Verpackungseinheit	  */
	@Override
	public void setPackage_UOM_ID (int Package_UOM_ID)
	{
		if (Package_UOM_ID < 1) 
			set_Value (COLUMNNAME_Package_UOM_ID, null);
		else 
			set_Value (COLUMNNAME_Package_UOM_ID, Integer.valueOf(Package_UOM_ID));
	}

	/** Get Verpackungseinheit.
		@return Verpackungseinheit	  */
	@Override
	public int getPackage_UOM_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Package_UOM_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Pck. Gr..
		@param PackageSize Pck. Gr.	  */
	@Override
	public void setPackageSize (java.lang.String PackageSize)
	{
		set_Value (COLUMNNAME_PackageSize, PackageSize);
	}

	/** Get Pck. Gr..
		@return Pck. Gr.	  */
	@Override
	public java.lang.String getPackageSize () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_PackageSize);
	}

	/** Set Verarbeiten.
		@param Processing Verarbeiten	  */
	@Override
	public void setProcessing (boolean Processing)
	{
		set_Value (COLUMNNAME_Processing, Boolean.valueOf(Processing));
	}

	/** Get Verarbeiten.
		@return Verarbeiten	  */
	@Override
	public boolean isProcessing () 
	{
		Object oo = get_Value(COLUMNNAME_Processing);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** 
	 * ProductType AD_Reference_ID=270
	 * Reference name: M_Product_ProductType
	 */
	public static final int PRODUCTTYPE_AD_Reference_ID=270;
	/** Item = I */
	public static final String PRODUCTTYPE_Item = "I";
	/** Service = S */
	public static final String PRODUCTTYPE_Service = "S";
	/** Resource = R */
	public static final String PRODUCTTYPE_Resource = "R";
	/** ExpenseType = E */
	public static final String PRODUCTTYPE_ExpenseType = "E";
	/** Online = O */
	public static final String PRODUCTTYPE_Online = "O";
	/** FreightCost = F */
	public static final String PRODUCTTYPE_FreightCost = "F";
	/** Set Produktart.
		@param ProductType 
		Type of product
	  */
	@Override
	public void setProductType (java.lang.String ProductType)
	{

		set_Value (COLUMNNAME_ProductType, ProductType);
	}

	/** Get Produktart.
		@return Type of product
	  */
	@Override
	public java.lang.String getProductType () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_ProductType);
	}

	@Override
	public org.compiere.model.I_R_MailText getR_MailText()
	{
		return get_ValueAsPO(COLUMNNAME_R_MailText_ID, org.compiere.model.I_R_MailText.class);
	}

	@Override
	public void setR_MailText(org.compiere.model.I_R_MailText R_MailText)
	{
		set_ValueFromPO(COLUMNNAME_R_MailText_ID, org.compiere.model.I_R_MailText.class, R_MailText);
	}

	/** Set EMail-Vorlage.
		@param R_MailText_ID 
		Text templates for mailings
	  */
	@Override
	public void setR_MailText_ID (int R_MailText_ID)
	{
		if (R_MailText_ID < 1) 
			set_Value (COLUMNNAME_R_MailText_ID, null);
		else 
			set_Value (COLUMNNAME_R_MailText_ID, Integer.valueOf(R_MailText_ID));
	}

	/** Get EMail-Vorlage.
		@return Text templates for mailings
	  */
	@Override
	public int getR_MailText_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_R_MailText_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_S_ExpenseType getS_ExpenseType()
	{
		return get_ValueAsPO(COLUMNNAME_S_ExpenseType_ID, org.compiere.model.I_S_ExpenseType.class);
	}

	@Override
	public void setS_ExpenseType(org.compiere.model.I_S_ExpenseType S_ExpenseType)
	{
		set_ValueFromPO(COLUMNNAME_S_ExpenseType_ID, org.compiere.model.I_S_ExpenseType.class, S_ExpenseType);
	}

	/** Set Aufwandsart.
		@param S_ExpenseType_ID 
		Expense report type
	  */
	@Override
	public void setS_ExpenseType_ID (int S_ExpenseType_ID)
	{
		if (S_ExpenseType_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_S_ExpenseType_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_S_ExpenseType_ID, Integer.valueOf(S_ExpenseType_ID));
	}

	/** Get Aufwandsart.
		@return Expense report type
	  */
	@Override
	public int getS_ExpenseType_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_S_ExpenseType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_S_Resource getS_Resource()
	{
		return get_ValueAsPO(COLUMNNAME_S_Resource_ID, org.compiere.model.I_S_Resource.class);
	}

	@Override
	public void setS_Resource(org.compiere.model.I_S_Resource S_Resource)
	{
		set_ValueFromPO(COLUMNNAME_S_Resource_ID, org.compiere.model.I_S_Resource.class, S_Resource);
	}

	/** Set Ressource.
		@param S_Resource_ID 
		Resource
	  */
	@Override
	public void setS_Resource_ID (int S_Resource_ID)
	{
		if (S_Resource_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_S_Resource_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_S_Resource_ID, Integer.valueOf(S_Resource_ID));
	}

	/** Get Ressource.
		@return Resource
	  */
	@Override
	public int getS_Resource_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_S_Resource_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Kundenbetreuer.
		@param SalesRep_ID Kundenbetreuer	  */
	@Override
	public void setSalesRep_ID (int SalesRep_ID)
	{
		if (SalesRep_ID < 1) 
			set_Value (COLUMNNAME_SalesRep_ID, null);
		else 
			set_Value (COLUMNNAME_SalesRep_ID, Integer.valueOf(SalesRep_ID));
	}

	/** Get Kundenbetreuer.
		@return Kundenbetreuer	  */
	@Override
	public int getSalesRep_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_SalesRep_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Regaltiefe.
		@param ShelfDepth 
		Shelf depth required
	  */
	@Override
	public void setShelfDepth (int ShelfDepth)
	{
		set_Value (COLUMNNAME_ShelfDepth, Integer.valueOf(ShelfDepth));
	}

	/** Get Regaltiefe.
		@return Shelf depth required
	  */
	@Override
	public int getShelfDepth () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ShelfDepth);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Regalhöhe.
		@param ShelfHeight 
		Shelf height required
	  */
	@Override
	public void setShelfHeight (java.math.BigDecimal ShelfHeight)
	{
		set_Value (COLUMNNAME_ShelfHeight, ShelfHeight);
	}

	/** Get Regalhöhe.
		@return Shelf height required
	  */
	@Override
	public java.math.BigDecimal getShelfHeight () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ShelfHeight);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Regalbreite.
		@param ShelfWidth 
		Shelf width required
	  */
	@Override
	public void setShelfWidth (int ShelfWidth)
	{
		set_Value (COLUMNNAME_ShelfWidth, Integer.valueOf(ShelfWidth));
	}

	/** Get Regalbreite.
		@return Shelf width required
	  */
	@Override
	public int getShelfWidth () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ShelfWidth);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set SKU.
		@param SKU 
		Stock Keeping Unit
	  */
	@Override
	public void setSKU (java.lang.String SKU)
	{
		set_Value (COLUMNNAME_SKU, SKU);
	}

	/** Get SKU.
		@return Stock Keeping Unit
	  */
	@Override
	public java.lang.String getSKU () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_SKU);
	}

	/** Set UnitsPerPack.
		@param UnitsPerPack 
		The Units Per Pack indicates the no of units of a product packed together.
	  */
	@Override
	public void setUnitsPerPack (int UnitsPerPack)
	{
		set_Value (COLUMNNAME_UnitsPerPack, Integer.valueOf(UnitsPerPack));
	}

	/** Get UnitsPerPack.
		@return The Units Per Pack indicates the no of units of a product packed together.
	  */
	@Override
	public int getUnitsPerPack () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_UnitsPerPack);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Einheiten pro Palette.
		@param UnitsPerPallet 
		Units Per Pallet
	  */
	@Override
	public void setUnitsPerPallet (java.math.BigDecimal UnitsPerPallet)
	{
		set_Value (COLUMNNAME_UnitsPerPallet, UnitsPerPallet);
	}

	/** Get Einheiten pro Palette.
		@return Units Per Pallet
	  */
	@Override
	public java.math.BigDecimal getUnitsPerPallet () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_UnitsPerPallet);
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

	/** Set Suchschlüssel.
		@param Value 
		Search key for the record in the format required - must be unique
	  */
	@Override
	public void setValue (java.lang.String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Suchschlüssel.
		@return Search key for the record in the format required - must be unique
	  */
	@Override
	public java.lang.String getValue () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Value);
	}

	/** Set Versions-Nr..
		@param VersionNo 
		Version Number
	  */
	@Override
	public void setVersionNo (java.lang.String VersionNo)
	{
		set_Value (COLUMNNAME_VersionNo, VersionNo);
	}

	/** Get Versions-Nr..
		@return Version Number
	  */
	@Override
	public java.lang.String getVersionNo () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_VersionNo);
	}

	/** Set Volumen.
		@param Volume 
		Volume of a product
	  */
	@Override
	public void setVolume (java.math.BigDecimal Volume)
	{
		set_Value (COLUMNNAME_Volume, Volume);
	}

	/** Get Volumen.
		@return Volume of a product
	  */
	@Override
	public java.math.BigDecimal getVolume () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Volume);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Lager- und Transporttemperatur.
		@param Warehouse_temperature Lager- und Transporttemperatur	  */
	@Override
	public void setWarehouse_temperature (java.lang.String Warehouse_temperature)
	{
		set_Value (COLUMNNAME_Warehouse_temperature, Warehouse_temperature);
	}

	/** Get Lager- und Transporttemperatur.
		@return Lager- und Transporttemperatur	  */
	@Override
	public java.lang.String getWarehouse_temperature () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Warehouse_temperature);
	}

	/** Set Gewicht.
		@param Weight 
		Weight of a product
	  */
	@Override
	public void setWeight (java.math.BigDecimal Weight)
	{
		set_Value (COLUMNNAME_Weight, Weight);
	}

	/** Get Gewicht.
		@return Weight of a product
	  */
	@Override
	public java.math.BigDecimal getWeight () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Weight);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}
}