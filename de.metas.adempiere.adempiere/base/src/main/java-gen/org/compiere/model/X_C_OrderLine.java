/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for C_OrderLine
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public class X_C_OrderLine extends org.compiere.model.PO implements I_C_OrderLine, org.compiere.model.I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 408081812L;

    /** Standard Constructor */
    public X_C_OrderLine (Properties ctx, int C_OrderLine_ID, String trxName)
    {
      super (ctx, C_OrderLine_ID, trxName);
      /** if (C_OrderLine_ID == 0)
        {
			setC_BPartner_ID (0); // @SQL=SELECT (CASE WHEN IsSOTrx='Y' AND IsDropShip='Y' THEN DropShip_BPartner_ID ELSE C_BPartner_ID END) AS DefaultValue FROM C_Order WHERE C_Order_ID=@C_Order_ID@
			setC_BPartner_Location_ID (0); // @SQL=SELECT (CASE WHEN IsSOTrx='Y' AND IsDropShip='Y' THEN DropShip_Location_ID ELSE C_BPartner_Location_ID END) AS DefaultValue FROM C_Order WHERE C_Order_ID=@C_Order_ID@


			setC_Currency_ID (0); // @C_Currency_ID@
			setC_Order_ID (0);
			setC_OrderLine_ID (0);
			setC_Tax_ID (0);
			setC_UOM_ID (0); // @#C_UOM_ID@
			setDateOrdered (new Timestamp( System.currentTimeMillis() )); // @DateOrdered@
			setEnforcePriceLimit (false); // N
			setFreightAmt (BigDecimal.ZERO);
			setFrequencyType (null); // M
			setInvoicableQtyBasedOn (null); // Nominal
			setIsCampaignPrice (false); // N
			setIsDescription (false); // N
			setIsDiscountEditable (true); // Y
			setIsGroupCompensationLine (false); // N
			setIsManualDiscount (false); // N
			setIsManualPaymentTerm (false); // N
			setIsManualPrice (false); // N
			setIsPriceEditable (true); // Y
			setIsSubscription (false); // N
			setIsTempPricingConditions (false); // N
			setLine (0); // @SQL=SELECT COALESCE(MAX(Line),0)+10 AS DefaultValue FROM C_OrderLine WHERE C_Order_ID=@C_Order_ID@
			setLineNetAmt (BigDecimal.ZERO);
			setM_Product_ID (0);
			setPriceActual (BigDecimal.ZERO);
			setPriceEntered (BigDecimal.ZERO);
			setPriceLimit (BigDecimal.ZERO);
			setPriceList (BigDecimal.ZERO);
			setProcessed (false); // N
			setQtyDelivered (BigDecimal.ZERO);
			setQtyEntered (BigDecimal.ZERO); // 1
			setQtyInvoiced (BigDecimal.ZERO);
			setQtyLostSales (BigDecimal.ZERO);
			setQtyOrdered (BigDecimal.ZERO); // 1
			setQtyReserved (BigDecimal.ZERO);
			setRunsMax (0); // 12
        } */
    }

    /** Load Constructor */
    public X_C_OrderLine (Properties ctx, ResultSet rs, String trxName)
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

	/** Set Buchende Organisation.
		@param AD_OrgTrx_ID 
		Performing or initiating organization
	  */
	@Override
	public void setAD_OrgTrx_ID (int AD_OrgTrx_ID)
	{
		if (AD_OrgTrx_ID < 1) 
			set_Value (COLUMNNAME_AD_OrgTrx_ID, null);
		else 
			set_Value (COLUMNNAME_AD_OrgTrx_ID, Integer.valueOf(AD_OrgTrx_ID));
	}

	/** Get Buchende Organisation.
		@return Performing or initiating organization
	  */
	@Override
	public int getAD_OrgTrx_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_OrgTrx_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Ansprechpartner.
		@param AD_User_ID 
		User within the system - Internal or Business Partner Contact
	  */
	@Override
	public void setAD_User_ID (int AD_User_ID)
	{
		if (AD_User_ID < 0) 
			set_Value (COLUMNNAME_AD_User_ID, null);
		else 
			set_Value (COLUMNNAME_AD_User_ID, Integer.valueOf(AD_User_ID));
	}

	/** Get Ansprechpartner.
		@return User within the system - Internal or Business Partner Contact
	  */
	@Override
	public int getAD_User_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_User_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Preissystem.
		@param Base_PricingSystem_ID Preissystem	  */
	@Override
	public void setBase_PricingSystem_ID (int Base_PricingSystem_ID)
	{
		if (Base_PricingSystem_ID < 1) 
			set_Value (COLUMNNAME_Base_PricingSystem_ID, null);
		else 
			set_Value (COLUMNNAME_Base_PricingSystem_ID, Integer.valueOf(Base_PricingSystem_ID));
	}

	/** Get Preissystem.
		@return Preissystem	  */
	@Override
	public int getBase_PricingSystem_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Base_PricingSystem_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Anschrift-Text.
		@param BPartnerAddress Anschrift-Text	  */
	@Override
	public void setBPartnerAddress (java.lang.String BPartnerAddress)
	{
		set_Value (COLUMNNAME_BPartnerAddress, BPartnerAddress);
	}

	/** Get Anschrift-Text.
		@return Anschrift-Text	  */
	@Override
	public java.lang.String getBPartnerAddress () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_BPartnerAddress);
	}

	/** Set Kostenstelle.
		@param C_Activity_ID 
		Kostenstelle
	  */
	@Override
	public void setC_Activity_ID (int C_Activity_ID)
	{
		if (C_Activity_ID < 1) 
			set_Value (COLUMNNAME_C_Activity_ID, null);
		else 
			set_Value (COLUMNNAME_C_Activity_ID, Integer.valueOf(C_Activity_ID));
	}

	/** Get Kostenstelle.
		@return Kostenstelle
	  */
	@Override
	public int getC_Activity_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Activity_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Geschäftspartner.
		@param C_BPartner_ID 
		Identifies a Business Partner
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
		@return Identifies a Business Partner
	  */
	@Override
	public int getC_BPartner_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Standort.
		@param C_BPartner_Location_ID 
		Identifies the (ship to) address for this Business Partner
	  */
	@Override
	public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
	{
		if (C_BPartner_Location_ID < 1) 
			set_Value (COLUMNNAME_C_BPartner_Location_ID, null);
		else 
			set_Value (COLUMNNAME_C_BPartner_Location_ID, Integer.valueOf(C_BPartner_Location_ID));
	}

	/** Get Standort.
		@return Identifies the (ship to) address for this Business Partner
	  */
	@Override
	public int getC_BPartner_Location_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_Location_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_C_Campaign getC_Campaign()
	{
		return get_ValueAsPO(COLUMNNAME_C_Campaign_ID, org.compiere.model.I_C_Campaign.class);
	}

	@Override
	public void setC_Campaign(org.compiere.model.I_C_Campaign C_Campaign)
	{
		set_ValueFromPO(COLUMNNAME_C_Campaign_ID, org.compiere.model.I_C_Campaign.class, C_Campaign);
	}

	/** Set Werbemassnahme.
		@param C_Campaign_ID 
		Marketing Campaign
	  */
	@Override
	public void setC_Campaign_ID (int C_Campaign_ID)
	{
		if (C_Campaign_ID < 1) 
			set_Value (COLUMNNAME_C_Campaign_ID, null);
		else 
			set_Value (COLUMNNAME_C_Campaign_ID, Integer.valueOf(C_Campaign_ID));
	}

	/** Get Werbemassnahme.
		@return Marketing Campaign
	  */
	@Override
	public int getC_Campaign_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Campaign_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Kosten.
		@param C_Charge_ID 
		Additional document charges
	  */
	@Override
	public void setC_Charge_ID (int C_Charge_ID)
	{
		if (C_Charge_ID < 1) 
			set_Value (COLUMNNAME_C_Charge_ID, null);
		else 
			set_Value (COLUMNNAME_C_Charge_ID, Integer.valueOf(C_Charge_ID));
	}

	/** Get Kosten.
		@return Additional document charges
	  */
	@Override
	public int getC_Charge_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Charge_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public de.metas.order.model.I_C_CompensationGroup_SchemaLine getC_CompensationGroup_SchemaLine()
	{
		return get_ValueAsPO(COLUMNNAME_C_CompensationGroup_SchemaLine_ID, de.metas.order.model.I_C_CompensationGroup_SchemaLine.class);
	}

	@Override
	public void setC_CompensationGroup_SchemaLine(de.metas.order.model.I_C_CompensationGroup_SchemaLine C_CompensationGroup_SchemaLine)
	{
		set_ValueFromPO(COLUMNNAME_C_CompensationGroup_SchemaLine_ID, de.metas.order.model.I_C_CompensationGroup_SchemaLine.class, C_CompensationGroup_SchemaLine);
	}

	/** Set Compensation Group Schema Line.
		@param C_CompensationGroup_SchemaLine_ID Compensation Group Schema Line	  */
	@Override
	public void setC_CompensationGroup_SchemaLine_ID (int C_CompensationGroup_SchemaLine_ID)
	{
		if (C_CompensationGroup_SchemaLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_CompensationGroup_SchemaLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_CompensationGroup_SchemaLine_ID, Integer.valueOf(C_CompensationGroup_SchemaLine_ID));
	}

	/** Get Compensation Group Schema Line.
		@return Compensation Group Schema Line	  */
	@Override
	public int getC_CompensationGroup_SchemaLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_CompensationGroup_SchemaLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Währung.
		@param C_Currency_ID 
		The Currency for this record
	  */
	@Override
	public void setC_Currency_ID (int C_Currency_ID)
	{
		if (C_Currency_ID < 1) 
			set_Value (COLUMNNAME_C_Currency_ID, null);
		else 
			set_Value (COLUMNNAME_C_Currency_ID, Integer.valueOf(C_Currency_ID));
	}

	/** Get Währung.
		@return The Currency for this record
	  */
	@Override
	public int getC_Currency_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Currency_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_C_Order_CompensationGroup getC_Order_CompensationGroup()
	{
		return get_ValueAsPO(COLUMNNAME_C_Order_CompensationGroup_ID, org.compiere.model.I_C_Order_CompensationGroup.class);
	}

	@Override
	public void setC_Order_CompensationGroup(org.compiere.model.I_C_Order_CompensationGroup C_Order_CompensationGroup)
	{
		set_ValueFromPO(COLUMNNAME_C_Order_CompensationGroup_ID, org.compiere.model.I_C_Order_CompensationGroup.class, C_Order_CompensationGroup);
	}

	/** Set Order Compensation Group.
		@param C_Order_CompensationGroup_ID Order Compensation Group	  */
	@Override
	public void setC_Order_CompensationGroup_ID (int C_Order_CompensationGroup_ID)
	{
		if (C_Order_CompensationGroup_ID < 1) 
			set_Value (COLUMNNAME_C_Order_CompensationGroup_ID, null);
		else 
			set_Value (COLUMNNAME_C_Order_CompensationGroup_ID, Integer.valueOf(C_Order_CompensationGroup_ID));
	}

	/** Get Order Compensation Group.
		@return Order Compensation Group	  */
	@Override
	public int getC_Order_CompensationGroup_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Order_CompensationGroup_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_C_Order getC_Order()
	{
		return get_ValueAsPO(COLUMNNAME_C_Order_ID, org.compiere.model.I_C_Order.class);
	}

	@Override
	public void setC_Order(org.compiere.model.I_C_Order C_Order)
	{
		set_ValueFromPO(COLUMNNAME_C_Order_ID, org.compiere.model.I_C_Order.class, C_Order);
	}

	/** Set Auftrag.
		@param C_Order_ID 
		Order
	  */
	@Override
	public void setC_Order_ID (int C_Order_ID)
	{
		if (C_Order_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_Order_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_Order_ID, Integer.valueOf(C_Order_ID));
	}

	/** Get Auftrag.
		@return Order
	  */
	@Override
	public int getC_Order_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Order_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Auftragsposition.
		@param C_OrderLine_ID 
		Sales Order Line
	  */
	@Override
	public void setC_OrderLine_ID (int C_OrderLine_ID)
	{
		if (C_OrderLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_OrderLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_OrderLine_ID, Integer.valueOf(C_OrderLine_ID));
	}

	/** Get Auftragsposition.
		@return Sales Order Line
	  */
	@Override
	public int getC_OrderLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_OrderLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Zahlungsbedingung abw..
		@param C_PaymentTerm_Override_ID 
		Die Bedingungen für die Bezahlung dieses Vorgangs
	  */
	@Override
	public void setC_PaymentTerm_Override_ID (int C_PaymentTerm_Override_ID)
	{
		if (C_PaymentTerm_Override_ID < 1) 
			set_Value (COLUMNNAME_C_PaymentTerm_Override_ID, null);
		else 
			set_Value (COLUMNNAME_C_PaymentTerm_Override_ID, Integer.valueOf(C_PaymentTerm_Override_ID));
	}

	/** Get Zahlungsbedingung abw..
		@return Die Bedingungen für die Bezahlung dieses Vorgangs
	  */
	@Override
	public int getC_PaymentTerm_Override_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_PaymentTerm_Override_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Projekt.
		@param C_Project_ID 
		Financial Project
	  */
	@Override
	public void setC_Project_ID (int C_Project_ID)
	{
		if (C_Project_ID < 1) 
			set_Value (COLUMNNAME_C_Project_ID, null);
		else 
			set_Value (COLUMNNAME_C_Project_ID, Integer.valueOf(C_Project_ID));
	}

	/** Get Projekt.
		@return Financial Project
	  */
	@Override
	public int getC_Project_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Project_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_C_ProjectPhase getC_ProjectPhase()
	{
		return get_ValueAsPO(COLUMNNAME_C_ProjectPhase_ID, org.compiere.model.I_C_ProjectPhase.class);
	}

	@Override
	public void setC_ProjectPhase(org.compiere.model.I_C_ProjectPhase C_ProjectPhase)
	{
		set_ValueFromPO(COLUMNNAME_C_ProjectPhase_ID, org.compiere.model.I_C_ProjectPhase.class, C_ProjectPhase);
	}

	/** Set Projekt-Phase.
		@param C_ProjectPhase_ID 
		Phase of a Project
	  */
	@Override
	public void setC_ProjectPhase_ID (int C_ProjectPhase_ID)
	{
		if (C_ProjectPhase_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_ProjectPhase_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_ProjectPhase_ID, Integer.valueOf(C_ProjectPhase_ID));
	}

	/** Get Projekt-Phase.
		@return Phase of a Project
	  */
	@Override
	public int getC_ProjectPhase_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_ProjectPhase_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_C_ProjectTask getC_ProjectTask()
	{
		return get_ValueAsPO(COLUMNNAME_C_ProjectTask_ID, org.compiere.model.I_C_ProjectTask.class);
	}

	@Override
	public void setC_ProjectTask(org.compiere.model.I_C_ProjectTask C_ProjectTask)
	{
		set_ValueFromPO(COLUMNNAME_C_ProjectTask_ID, org.compiere.model.I_C_ProjectTask.class, C_ProjectTask);
	}

	/** Set Projekt-Aufgabe.
		@param C_ProjectTask_ID 
		Actual Project Task in a Phase
	  */
	@Override
	public void setC_ProjectTask_ID (int C_ProjectTask_ID)
	{
		if (C_ProjectTask_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_ProjectTask_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_ProjectTask_ID, Integer.valueOf(C_ProjectTask_ID));
	}

	/** Get Projekt-Aufgabe.
		@return Actual Project Task in a Phase
	  */
	@Override
	public int getC_ProjectTask_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_ProjectTask_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Steuerkategorie.
		@param C_TaxCategory_ID 
		Steuerkategorie
	  */
	@Override
	public void setC_TaxCategory_ID (int C_TaxCategory_ID)
	{
		if (C_TaxCategory_ID < 1) 
			set_Value (COLUMNNAME_C_TaxCategory_ID, null);
		else 
			set_Value (COLUMNNAME_C_TaxCategory_ID, Integer.valueOf(C_TaxCategory_ID));
	}

	/** Get Steuerkategorie.
		@return Steuerkategorie
	  */
	@Override
	public int getC_TaxCategory_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_TaxCategory_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Steuer.
		@param C_Tax_ID 
		Tax identifier
	  */
	@Override
	public void setC_Tax_ID (int C_Tax_ID)
	{
		if (C_Tax_ID < 1) 
			set_Value (COLUMNNAME_C_Tax_ID, null);
		else 
			set_Value (COLUMNNAME_C_Tax_ID, Integer.valueOf(C_Tax_ID));
	}

	/** Get Steuer.
		@return Tax identifier
	  */
	@Override
	public int getC_Tax_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Tax_ID);
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

	/** Set Lieferdatum.
		@param DateDelivered 
		Date when the product was delivered
	  */
	@Override
	public void setDateDelivered (java.sql.Timestamp DateDelivered)
	{
		set_ValueNoCheck (COLUMNNAME_DateDelivered, DateDelivered);
	}

	/** Get Lieferdatum.
		@return Date when the product was delivered
	  */
	@Override
	public java.sql.Timestamp getDateDelivered () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_DateDelivered);
	}

	/** Set Rechnungsdatum.
		@param DateInvoiced 
		Date printed on Invoice
	  */
	@Override
	public void setDateInvoiced (java.sql.Timestamp DateInvoiced)
	{
		set_ValueNoCheck (COLUMNNAME_DateInvoiced, DateInvoiced);
	}

	/** Get Rechnungsdatum.
		@return Date printed on Invoice
	  */
	@Override
	public java.sql.Timestamp getDateInvoiced () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_DateInvoiced);
	}

	/** Set Auftragsdatum.
		@param DateOrdered 
		Date of Order
	  */
	@Override
	public void setDateOrdered (java.sql.Timestamp DateOrdered)
	{
		set_Value (COLUMNNAME_DateOrdered, DateOrdered);
	}

	/** Get Auftragsdatum.
		@return Date of Order
	  */
	@Override
	public java.sql.Timestamp getDateOrdered () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_DateOrdered);
	}

	/** Set Zugesagter Termin.
		@param DatePromised 
		Date Order was promised
	  */
	@Override
	public void setDatePromised (java.sql.Timestamp DatePromised)
	{
		set_Value (COLUMNNAME_DatePromised, DatePromised);
	}

	/** Get Zugesagter Termin.
		@return Date Order was promised
	  */
	@Override
	public java.sql.Timestamp getDatePromised () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_DatePromised);
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

	/** Set Rabatt %.
		@param Discount 
		Discount in percent
	  */
	@Override
	public void setDiscount (java.math.BigDecimal Discount)
	{
		set_Value (COLUMNNAME_Discount, Discount);
	}

	/** Get Rabatt %.
		@return Discount in percent
	  */
	@Override
	public java.math.BigDecimal getDiscount () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Discount);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Preislimit erzwingen.
		@param EnforcePriceLimit 
		Do not allow prices below the limit price
	  */
	@Override
	public void setEnforcePriceLimit (boolean EnforcePriceLimit)
	{
		set_Value (COLUMNNAME_EnforcePriceLimit, Boolean.valueOf(EnforcePriceLimit));
	}

	/** Get Preislimit erzwingen.
		@return Do not allow prices below the limit price
	  */
	@Override
	public boolean isEnforcePriceLimit () 
	{
		Object oo = get_Value(COLUMNNAME_EnforcePriceLimit);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Frachtbetrag.
		@param FreightAmt 
		Freight Amount 
	  */
	@Override
	public void setFreightAmt (java.math.BigDecimal FreightAmt)
	{
		set_Value (COLUMNNAME_FreightAmt, FreightAmt);
	}

	/** Get Frachtbetrag.
		@return Freight Amount 
	  */
	@Override
	public java.math.BigDecimal getFreightAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_FreightAmt);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** 
	 * FrequencyType AD_Reference_ID=283
	 * Reference name: C_Recurring Frequency
	 */
	public static final int FREQUENCYTYPE_AD_Reference_ID=283;
	/** Täglich = D */
	public static final String FREQUENCYTYPE_Taeglich = "D";
	/** Wöchentlich = W */
	public static final String FREQUENCYTYPE_Woechentlich = "W";
	/** Monatlich = M */
	public static final String FREQUENCYTYPE_Monatlich = "M";
	/** Quartalsweise = Q */
	public static final String FREQUENCYTYPE_Quartalsweise = "Q";
	/** Set Häufigkeitsart.
		@param FrequencyType 
		Häufigkeitsart für Ereignisse
	  */
	@Override
	public void setFrequencyType (java.lang.String FrequencyType)
	{

		set_Value (COLUMNNAME_FrequencyType, FrequencyType);
	}

	/** Get Häufigkeitsart.
		@return Häufigkeitsart für Ereignisse
	  */
	@Override
	public java.lang.String getFrequencyType () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_FrequencyType);
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

	/** Set Compensation base amount.
		@param GroupCompensationBaseAmt 
		Base amount for calculating percentage group compensation
	  */
	@Override
	public void setGroupCompensationBaseAmt (java.math.BigDecimal GroupCompensationBaseAmt)
	{
		set_Value (COLUMNNAME_GroupCompensationBaseAmt, GroupCompensationBaseAmt);
	}

	/** Get Compensation base amount.
		@return Base amount for calculating percentage group compensation
	  */
	@Override
	public java.math.BigDecimal getGroupCompensationBaseAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_GroupCompensationBaseAmt);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Compensation percentage.
		@param GroupCompensationPercentage Compensation percentage	  */
	@Override
	public void setGroupCompensationPercentage (java.math.BigDecimal GroupCompensationPercentage)
	{
		set_Value (COLUMNNAME_GroupCompensationPercentage, GroupCompensationPercentage);
	}

	/** Get Compensation percentage.
		@return Compensation percentage	  */
	@Override
	public java.math.BigDecimal getGroupCompensationPercentage () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_GroupCompensationPercentage);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
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

	/** 
	 * InvoicableQtyBasedOn AD_Reference_ID=541023
	 * Reference name: InvoicableQtyBasedOn
	 */
	public static final int INVOICABLEQTYBASEDON_AD_Reference_ID=541023;
	/** Nominal = Nominal */
	public static final String INVOICABLEQTYBASEDON_Nominal = "Nominal";
	/** CatchWeight = CatchWeight */
	public static final String INVOICABLEQTYBASEDON_CatchWeight = "CatchWeight";
	/** Set Abr. Menge basiert auf.
		@param InvoicableQtyBasedOn 
		Legt fest wie die abrechenbare Menge ermittelt wird, wenn die tatsächlich gelieferte Menge von der mominal gelieferten Menge abweicht.
	  */
	@Override
	public void setInvoicableQtyBasedOn (java.lang.String InvoicableQtyBasedOn)
	{

		set_Value (COLUMNNAME_InvoicableQtyBasedOn, InvoicableQtyBasedOn);
	}

	/** Get Abr. Menge basiert auf.
		@return Legt fest wie die abrechenbare Menge ermittelt wird, wenn die tatsächlich gelieferte Menge von der mominal gelieferten Menge abweicht.
	  */
	@Override
	public java.lang.String getInvoicableQtyBasedOn () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_InvoicableQtyBasedOn);
	}

	/** Set AP.
		@param IsCampaignPrice 
		Aktionspreise
	  */
	@Override
	public void setIsCampaignPrice (boolean IsCampaignPrice)
	{
		set_Value (COLUMNNAME_IsCampaignPrice, Boolean.valueOf(IsCampaignPrice));
	}

	/** Get AP.
		@return Aktionspreise
	  */
	@Override
	public boolean isCampaignPrice () 
	{
		Object oo = get_Value(COLUMNNAME_IsCampaignPrice);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Description Only.
		@param IsDescription 
		if true, the line is just description and no transaction
	  */
	@Override
	public void setIsDescription (boolean IsDescription)
	{
		set_Value (COLUMNNAME_IsDescription, Boolean.valueOf(IsDescription));
	}

	/** Get Description Only.
		@return if true, the line is just description and no transaction
	  */
	@Override
	public boolean isDescription () 
	{
		Object oo = get_Value(COLUMNNAME_IsDescription);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Discount Editable.
		@param IsDiscountEditable 
		Allow user to change the discount
	  */
	@Override
	public void setIsDiscountEditable (boolean IsDiscountEditable)
	{
		set_Value (COLUMNNAME_IsDiscountEditable, Boolean.valueOf(IsDiscountEditable));
	}

	/** Get Discount Editable.
		@return Allow user to change the discount
	  */
	@Override
	public boolean isDiscountEditable () 
	{
		Object oo = get_Value(COLUMNNAME_IsDiscountEditable);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Group Compensation Line.
		@param IsGroupCompensationLine Group Compensation Line	  */
	@Override
	public void setIsGroupCompensationLine (boolean IsGroupCompensationLine)
	{
		set_Value (COLUMNNAME_IsGroupCompensationLine, Boolean.valueOf(IsGroupCompensationLine));
	}

	/** Get Group Compensation Line.
		@return Group Compensation Line	  */
	@Override
	public boolean isGroupCompensationLine () 
	{
		Object oo = get_Value(COLUMNNAME_IsGroupCompensationLine);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Prod.-Beschr. ändern.
		@param IsIndividualDescription Prod.-Beschr. ändern	  */
	@Override
	public void setIsIndividualDescription (boolean IsIndividualDescription)
	{
		set_Value (COLUMNNAME_IsIndividualDescription, Boolean.valueOf(IsIndividualDescription));
	}

	/** Get Prod.-Beschr. ändern.
		@return Prod.-Beschr. ändern	  */
	@Override
	public boolean isIndividualDescription () 
	{
		Object oo = get_Value(COLUMNNAME_IsIndividualDescription);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Manueller Rabatt.
		@param IsManualDiscount 
		Ein Rabatt, der von Hand eingetragen wurde, wird vom Provisionssystem nicht überschrieben
	  */
	@Override
	public void setIsManualDiscount (boolean IsManualDiscount)
	{
		set_Value (COLUMNNAME_IsManualDiscount, Boolean.valueOf(IsManualDiscount));
	}

	/** Get Manueller Rabatt.
		@return Ein Rabatt, der von Hand eingetragen wurde, wird vom Provisionssystem nicht überschrieben
	  */
	@Override
	public boolean isManualDiscount () 
	{
		Object oo = get_Value(COLUMNNAME_IsManualDiscount);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Manuelle Zahlungsbedingung.
		@param IsManualPaymentTerm 
		Die Zahlungsbedingung wurde vom Nutzer ausgewählt und soll nicht durch das System überschrieben werden
	  */
	@Override
	public void setIsManualPaymentTerm (boolean IsManualPaymentTerm)
	{
		set_Value (COLUMNNAME_IsManualPaymentTerm, Boolean.valueOf(IsManualPaymentTerm));
	}

	/** Get Manuelle Zahlungsbedingung.
		@return Die Zahlungsbedingung wurde vom Nutzer ausgewählt und soll nicht durch das System überschrieben werden
	  */
	@Override
	public boolean isManualPaymentTerm () 
	{
		Object oo = get_Value(COLUMNNAME_IsManualPaymentTerm);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Manueller Preis.
		@param IsManualPrice Manueller Preis	  */
	@Override
	public void setIsManualPrice (boolean IsManualPrice)
	{
		set_Value (COLUMNNAME_IsManualPrice, Boolean.valueOf(IsManualPrice));
	}

	/** Get Manueller Preis.
		@return Manueller Preis	  */
	@Override
	public boolean isManualPrice () 
	{
		Object oo = get_Value(COLUMNNAME_IsManualPrice);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Verpackungsmaterial.
		@param IsPackagingMaterial Verpackungsmaterial	  */
	@Override
	public void setIsPackagingMaterial (boolean IsPackagingMaterial)
	{
		set_Value (COLUMNNAME_IsPackagingMaterial, Boolean.valueOf(IsPackagingMaterial));
	}

	/** Get Verpackungsmaterial.
		@return Verpackungsmaterial	  */
	@Override
	public boolean isPackagingMaterial () 
	{
		Object oo = get_Value(COLUMNNAME_IsPackagingMaterial);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Price Editable.
		@param IsPriceEditable 
		Allow user to change the price
	  */
	@Override
	public void setIsPriceEditable (boolean IsPriceEditable)
	{
		set_Value (COLUMNNAME_IsPriceEditable, Boolean.valueOf(IsPriceEditable));
	}

	/** Get Price Editable.
		@return Allow user to change the price
	  */
	@Override
	public boolean isPriceEditable () 
	{
		Object oo = get_Value(COLUMNNAME_IsPriceEditable);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Abo.
		@param IsSubscription Abo	  */
	@Override
	public void setIsSubscription (boolean IsSubscription)
	{
		set_Value (COLUMNNAME_IsSubscription, Boolean.valueOf(IsSubscription));
	}

	/** Get Abo.
		@return Abo	  */
	@Override
	public boolean isSubscription () 
	{
		Object oo = get_Value(COLUMNNAME_IsSubscription);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Temporary pricing conditions.
		@param IsTempPricingConditions Temporary pricing conditions	  */
	@Override
	public void setIsTempPricingConditions (boolean IsTempPricingConditions)
	{
		set_Value (COLUMNNAME_IsTempPricingConditions, Boolean.valueOf(IsTempPricingConditions));
	}

	/** Get Temporary pricing conditions.
		@return Temporary pricing conditions	  */
	@Override
	public boolean isTempPricingConditions () 
	{
		Object oo = get_Value(COLUMNNAME_IsTempPricingConditions);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Benutze abw. Adresse.
		@param IsUseBPartnerAddress Benutze abw. Adresse	  */
	@Override
	public void setIsUseBPartnerAddress (boolean IsUseBPartnerAddress)
	{
		set_Value (COLUMNNAME_IsUseBPartnerAddress, Boolean.valueOf(IsUseBPartnerAddress));
	}

	/** Get Benutze abw. Adresse.
		@return Benutze abw. Adresse	  */
	@Override
	public boolean isUseBPartnerAddress () 
	{
		Object oo = get_Value(COLUMNNAME_IsUseBPartnerAddress);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Zeile Nr..
		@param Line 
		Unique line for this document
	  */
	@Override
	public void setLine (int Line)
	{
		set_Value (COLUMNNAME_Line, Integer.valueOf(Line));
	}

	/** Get Zeile Nr..
		@return Unique line for this document
	  */
	@Override
	public int getLine () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Line);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Zeilennetto.
		@param LineNetAmt 
		Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	  */
	@Override
	public void setLineNetAmt (java.math.BigDecimal LineNetAmt)
	{
		set_Value (COLUMNNAME_LineNetAmt, LineNetAmt);
	}

	/** Get Zeilennetto.
		@return Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	  */
	@Override
	public java.math.BigDecimal getLineNetAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_LineNetAmt);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	@Override
	public org.compiere.model.I_C_OrderLine getLink_OrderLine()
	{
		return get_ValueAsPO(COLUMNNAME_Link_OrderLine_ID, org.compiere.model.I_C_OrderLine.class);
	}

	@Override
	public void setLink_OrderLine(org.compiere.model.I_C_OrderLine Link_OrderLine)
	{
		set_ValueFromPO(COLUMNNAME_Link_OrderLine_ID, org.compiere.model.I_C_OrderLine.class, Link_OrderLine);
	}

	/** Set Zugehörige Bestellposition.
		@param Link_OrderLine_ID 
		Mit diesem Feld kann eine Auftragsposition die ihr zugehörige Bestellposition referenzieren.
	  */
	@Override
	public void setLink_OrderLine_ID (int Link_OrderLine_ID)
	{
		if (Link_OrderLine_ID < 1) 
			set_Value (COLUMNNAME_Link_OrderLine_ID, null);
		else 
			set_Value (COLUMNNAME_Link_OrderLine_ID, Integer.valueOf(Link_OrderLine_ID));
	}

	/** Get Zugehörige Bestellposition.
		@return Mit diesem Feld kann eine Auftragsposition die ihr zugehörige Bestellposition referenzieren.
	  */
	@Override
	public int getLink_OrderLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Link_OrderLine_ID);
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
	public org.compiere.model.I_M_DiscountSchemaBreak getM_DiscountSchemaBreak()
	{
		return get_ValueAsPO(COLUMNNAME_M_DiscountSchemaBreak_ID, org.compiere.model.I_M_DiscountSchemaBreak.class);
	}

	@Override
	public void setM_DiscountSchemaBreak(org.compiere.model.I_M_DiscountSchemaBreak M_DiscountSchemaBreak)
	{
		set_ValueFromPO(COLUMNNAME_M_DiscountSchemaBreak_ID, org.compiere.model.I_M_DiscountSchemaBreak.class, M_DiscountSchemaBreak);
	}

	/** Set Discount Schema Break.
		@param M_DiscountSchemaBreak_ID 
		Trade Discount Break
	  */
	@Override
	public void setM_DiscountSchemaBreak_ID (int M_DiscountSchemaBreak_ID)
	{
		if (M_DiscountSchemaBreak_ID < 1) 
			set_Value (COLUMNNAME_M_DiscountSchemaBreak_ID, null);
		else 
			set_Value (COLUMNNAME_M_DiscountSchemaBreak_ID, Integer.valueOf(M_DiscountSchemaBreak_ID));
	}

	/** Get Discount Schema Break.
		@return Trade Discount Break
	  */
	@Override
	public int getM_DiscountSchemaBreak_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_DiscountSchemaBreak_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_M_DiscountSchema getM_DiscountSchema()
	{
		return get_ValueAsPO(COLUMNNAME_M_DiscountSchema_ID, org.compiere.model.I_M_DiscountSchema.class);
	}

	@Override
	public void setM_DiscountSchema(org.compiere.model.I_M_DiscountSchema M_DiscountSchema)
	{
		set_ValueFromPO(COLUMNNAME_M_DiscountSchema_ID, org.compiere.model.I_M_DiscountSchema.class, M_DiscountSchema);
	}

	/** Set Rabatt Schema.
		@param M_DiscountSchema_ID 
		Schema um den prozentualen Rabatt zu berechnen
	  */
	@Override
	public void setM_DiscountSchema_ID (int M_DiscountSchema_ID)
	{
		if (M_DiscountSchema_ID < 1) 
			set_Value (COLUMNNAME_M_DiscountSchema_ID, null);
		else 
			set_Value (COLUMNNAME_M_DiscountSchema_ID, Integer.valueOf(M_DiscountSchema_ID));
	}

	/** Get Rabatt Schema.
		@return Schema um den prozentualen Rabatt zu berechnen
	  */
	@Override
	public int getM_DiscountSchema_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_DiscountSchema_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Version Preisliste.
		@param M_PriceList_Version_ID 
		Bezeichnet eine einzelne Version der Preisliste
	  */
	@Override
	public void setM_PriceList_Version_ID (int M_PriceList_Version_ID)
	{
		if (M_PriceList_Version_ID < 1) 
			set_Value (COLUMNNAME_M_PriceList_Version_ID, null);
		else 
			set_Value (COLUMNNAME_M_PriceList_Version_ID, Integer.valueOf(M_PriceList_Version_ID));
	}

	/** Get Version Preisliste.
		@return Bezeichnet eine einzelne Version der Preisliste
	  */
	@Override
	public int getM_PriceList_Version_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_PriceList_Version_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Produktnotiz.
		@param M_Product_DocumentNote Produktnotiz	  */
	@Override
	public void setM_Product_DocumentNote (java.lang.String M_Product_DocumentNote)
	{
		set_ValueNoCheck (COLUMNNAME_M_Product_DocumentNote, M_Product_DocumentNote);
	}

	/** Get Produktnotiz.
		@return Produktnotiz	  */
	@Override
	public java.lang.String getM_Product_DocumentNote () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_M_Product_DocumentNote);
	}

	/** Set Produkt.
		@param M_Product_ID 
		Produkt, Leistung, Artikel
	  */
	@Override
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1) 
			set_Value (COLUMNNAME_M_Product_ID, null);
		else 
			set_Value (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
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

	@Override
	public org.compiere.model.I_M_Promotion getM_Promotion()
	{
		return get_ValueAsPO(COLUMNNAME_M_Promotion_ID, org.compiere.model.I_M_Promotion.class);
	}

	@Override
	public void setM_Promotion(org.compiere.model.I_M_Promotion M_Promotion)
	{
		set_ValueFromPO(COLUMNNAME_M_Promotion_ID, org.compiere.model.I_M_Promotion.class, M_Promotion);
	}

	/** Set Promotion.
		@param M_Promotion_ID Promotion	  */
	@Override
	public void setM_Promotion_ID (int M_Promotion_ID)
	{
		if (M_Promotion_ID < 1) 
			set_Value (COLUMNNAME_M_Promotion_ID, null);
		else 
			set_Value (COLUMNNAME_M_Promotion_ID, Integer.valueOf(M_Promotion_ID));
	}

	/** Get Promotion.
		@return Promotion	  */
	@Override
	public int getM_Promotion_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Promotion_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_M_Shipper getM_Shipper()
	{
		return get_ValueAsPO(COLUMNNAME_M_Shipper_ID, org.compiere.model.I_M_Shipper.class);
	}

	@Override
	public void setM_Shipper(org.compiere.model.I_M_Shipper M_Shipper)
	{
		set_ValueFromPO(COLUMNNAME_M_Shipper_ID, org.compiere.model.I_M_Shipper.class, M_Shipper);
	}

	/** Set Lieferweg.
		@param M_Shipper_ID 
		Method or manner of product delivery
	  */
	@Override
	public void setM_Shipper_ID (int M_Shipper_ID)
	{
		if (M_Shipper_ID < 1) 
			set_Value (COLUMNNAME_M_Shipper_ID, null);
		else 
			set_Value (COLUMNNAME_M_Shipper_ID, Integer.valueOf(M_Shipper_ID));
	}

	/** Get Lieferweg.
		@return Method or manner of product delivery
	  */
	@Override
	public int getM_Shipper_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Shipper_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Ziel-Lager.
		@param M_Warehouse_Dest_ID Ziel-Lager	  */
	@Override
	public void setM_Warehouse_Dest_ID (int M_Warehouse_Dest_ID)
	{
		if (M_Warehouse_Dest_ID < 1) 
			set_Value (COLUMNNAME_M_Warehouse_Dest_ID, null);
		else 
			set_Value (COLUMNNAME_M_Warehouse_Dest_ID, Integer.valueOf(M_Warehouse_Dest_ID));
	}

	/** Get Ziel-Lager.
		@return Ziel-Lager	  */
	@Override
	public int getM_Warehouse_Dest_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Warehouse_Dest_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Lager.
		@param M_Warehouse_ID 
		Storage Warehouse and Service Point
	  */
	@Override
	public void setM_Warehouse_ID (int M_Warehouse_ID)
	{
		if (M_Warehouse_ID < 1) 
			set_Value (COLUMNNAME_M_Warehouse_ID, null);
		else 
			set_Value (COLUMNNAME_M_Warehouse_ID, Integer.valueOf(M_Warehouse_ID));
	}

	/** Get Lager.
		@return Storage Warehouse and Service Point
	  */
	@Override
	public int getM_Warehouse_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Warehouse_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set No Price Conditions Indicator.
		@param NoPriceConditionsColor_ID 
		Rot bedeutet, dass erforderliche Preis-Konditionen fehlen; gelb bedeutet, dass temporäre Preis-Konditionen nur für die jeweilige Position erstellt wurden.
	  */
	@Override
	public void setNoPriceConditionsColor_ID (int NoPriceConditionsColor_ID)
	{
		if (NoPriceConditionsColor_ID < 1) 
			set_Value (COLUMNNAME_NoPriceConditionsColor_ID, null);
		else 
			set_Value (COLUMNNAME_NoPriceConditionsColor_ID, Integer.valueOf(NoPriceConditionsColor_ID));
	}

	/** Get No Price Conditions Indicator.
		@return Rot bedeutet, dass erforderliche Preis-Konditionen fehlen; gelb bedeutet, dass temporäre Preis-Konditionen nur für die jeweilige Position erstellt wurden.
	  */
	@Override
	public int getNoPriceConditionsColor_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_NoPriceConditionsColor_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Gesamtauftragsrabbat.
		@param OrderDiscount Gesamtauftragsrabbat	  */
	@Override
	public void setOrderDiscount (java.math.BigDecimal OrderDiscount)
	{
		set_Value (COLUMNNAME_OrderDiscount, OrderDiscount);
	}

	/** Get Gesamtauftragsrabbat.
		@return Gesamtauftragsrabbat	  */
	@Override
	public java.math.BigDecimal getOrderDiscount () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_OrderDiscount);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Skonto %.
		@param PaymentDiscount Skonto %	  */
	@Override
	public void setPaymentDiscount (java.math.BigDecimal PaymentDiscount)
	{
		set_Value (COLUMNNAME_PaymentDiscount, PaymentDiscount);
	}

	/** Get Skonto %.
		@return Skonto %	  */
	@Override
	public java.math.BigDecimal getPaymentDiscount () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PaymentDiscount);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Abrufauftragsdatum.
		@param POCallOrderDate Abrufauftragsdatum	  */
	@Override
	public void setPOCallOrderDate (java.sql.Timestamp POCallOrderDate)
	{
		set_Value (COLUMNNAME_POCallOrderDate, POCallOrderDate);
	}

	/** Get Abrufauftragsdatum.
		@return Abrufauftragsdatum	  */
	@Override
	public java.sql.Timestamp getPOCallOrderDate () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_POCallOrderDate);
	}

	@Override
	public org.eevolution.model.I_PP_Cost_Collector getPP_Cost_Collector()
	{
		return get_ValueAsPO(COLUMNNAME_PP_Cost_Collector_ID, org.eevolution.model.I_PP_Cost_Collector.class);
	}

	@Override
	public void setPP_Cost_Collector(org.eevolution.model.I_PP_Cost_Collector PP_Cost_Collector)
	{
		set_ValueFromPO(COLUMNNAME_PP_Cost_Collector_ID, org.eevolution.model.I_PP_Cost_Collector.class, PP_Cost_Collector);
	}

	/** Set Manufacturing Cost Collector.
		@param PP_Cost_Collector_ID Manufacturing Cost Collector	  */
	@Override
	public void setPP_Cost_Collector_ID (int PP_Cost_Collector_ID)
	{
		if (PP_Cost_Collector_ID < 1) 
			set_Value (COLUMNNAME_PP_Cost_Collector_ID, null);
		else 
			set_Value (COLUMNNAME_PP_Cost_Collector_ID, Integer.valueOf(PP_Cost_Collector_ID));
	}

	/** Get Manufacturing Cost Collector.
		@return Manufacturing Cost Collector	  */
	@Override
	public int getPP_Cost_Collector_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_PP_Cost_Collector_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Vorbelegtes Rechnungsdatum.
		@param PresetDateInvoiced Vorbelegtes Rechnungsdatum	  */
	@Override
	public void setPresetDateInvoiced (java.sql.Timestamp PresetDateInvoiced)
	{
		set_Value (COLUMNNAME_PresetDateInvoiced, PresetDateInvoiced);
	}

	/** Get Vorbelegtes Rechnungsdatum.
		@return Vorbelegtes Rechnungsdatum	  */
	@Override
	public java.sql.Timestamp getPresetDateInvoiced () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_PresetDateInvoiced);
	}

	/** Set Vorbelegtes Lieferdatum.
		@param PresetDateShipped Vorbelegtes Lieferdatum	  */
	@Override
	public void setPresetDateShipped (java.sql.Timestamp PresetDateShipped)
	{
		set_Value (COLUMNNAME_PresetDateShipped, PresetDateShipped);
	}

	/** Get Vorbelegtes Lieferdatum.
		@return Vorbelegtes Lieferdatum	  */
	@Override
	public java.sql.Timestamp getPresetDateShipped () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_PresetDateShipped);
	}

	/** Set Einzelpreis.
		@param PriceActual 
		Actual Price 
	  */
	@Override
	public void setPriceActual (java.math.BigDecimal PriceActual)
	{
		set_Value (COLUMNNAME_PriceActual, PriceActual);
	}

	/** Get Einzelpreis.
		@return Actual Price 
	  */
	@Override
	public java.math.BigDecimal getPriceActual () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceActual);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Cost Price.
		@param PriceCost 
		Price per Unit of Measure including all indirect costs (Freight, etc.)
	  */
	@Override
	public void setPriceCost (java.math.BigDecimal PriceCost)
	{
		set_Value (COLUMNNAME_PriceCost, PriceCost);
	}

	/** Get Cost Price.
		@return Price per Unit of Measure including all indirect costs (Freight, etc.)
	  */
	@Override
	public java.math.BigDecimal getPriceCost () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceCost);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Preis.
		@param PriceEntered 
		Price Entered - the price based on the selected/base UoM
	  */
	@Override
	public void setPriceEntered (java.math.BigDecimal PriceEntered)
	{
		set_Value (COLUMNNAME_PriceEntered, PriceEntered);
	}

	/** Get Preis.
		@return Price Entered - the price based on the selected/base UoM
	  */
	@Override
	public java.math.BigDecimal getPriceEntered () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceEntered);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Mindestpreis.
		@param PriceLimit 
		Lowest price for a product
	  */
	@Override
	public void setPriceLimit (java.math.BigDecimal PriceLimit)
	{
		set_Value (COLUMNNAME_PriceLimit, PriceLimit);
	}

	/** Get Mindestpreis.
		@return Lowest price for a product
	  */
	@Override
	public java.math.BigDecimal getPriceLimit () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceLimit);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Mindestpreis Notiz.
		@param PriceLimitNote Mindestpreis Notiz	  */
	@Override
	public void setPriceLimitNote (java.lang.String PriceLimitNote)
	{
		set_Value (COLUMNNAME_PriceLimitNote, PriceLimitNote);
	}

	/** Get Mindestpreis Notiz.
		@return Mindestpreis Notiz	  */
	@Override
	public java.lang.String getPriceLimitNote () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_PriceLimitNote);
	}

	/** Set Auszeichnungspreis.
		@param PriceList 
		Auszeichnungspreis
	  */
	@Override
	public void setPriceList (java.math.BigDecimal PriceList)
	{
		set_Value (COLUMNNAME_PriceList, PriceList);
	}

	/** Get Auszeichnungspreis.
		@return Auszeichnungspreis
	  */
	@Override
	public java.math.BigDecimal getPriceList () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceList);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Auszeichnungspreis (standard).
		@param PriceList_Std 
		Auszeichnungspreis (standard)
	  */
	@Override
	public void setPriceList_Std (java.math.BigDecimal PriceList_Std)
	{
		set_Value (COLUMNNAME_PriceList_Std, PriceList_Std);
	}

	/** Get Auszeichnungspreis (standard).
		@return Auszeichnungspreis (standard)
	  */
	@Override
	public java.math.BigDecimal getPriceList_Std () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceList_Std);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Standardpreis.
		@param PriceStd Standardpreis	  */
	@Override
	public void setPriceStd (java.math.BigDecimal PriceStd)
	{
		set_Value (COLUMNNAME_PriceStd, PriceStd);
	}

	/** Get Standardpreis.
		@return Standardpreis	  */
	@Override
	public java.math.BigDecimal getPriceStd () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceStd);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Preiseinheit.
		@param Price_UOM_ID Preiseinheit	  */
	@Override
	public void setPrice_UOM_ID (int Price_UOM_ID)
	{
		if (Price_UOM_ID < 1) 
			set_Value (COLUMNNAME_Price_UOM_ID, null);
		else 
			set_Value (COLUMNNAME_Price_UOM_ID, Integer.valueOf(Price_UOM_ID));
	}

	/** Get Preiseinheit.
		@return Preiseinheit	  */
	@Override
	public int getPrice_UOM_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Price_UOM_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Verarbeitet.
		@param Processed 
		Checkbox sagt aus, ob der Beleg verarbeitet wurde. 
	  */
	@Override
	public void setProcessed (boolean Processed)
	{
		set_Value (COLUMNNAME_Processed, Boolean.valueOf(Processed));
	}

	/** Get Verarbeitet.
		@return Checkbox sagt aus, ob der Beleg verarbeitet wurde. 
	  */
	@Override
	public boolean isProcessed () 
	{
		Object oo = get_Value(COLUMNNAME_Processed);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Produktbeschreibung.
		@param ProductDescription 
		Produktbeschreibung
	  */
	@Override
	public void setProductDescription (java.lang.String ProductDescription)
	{
		set_Value (COLUMNNAME_ProductDescription, ProductDescription);
	}

	/** Get Produktbeschreibung.
		@return Produktbeschreibung
	  */
	@Override
	public java.lang.String getProductDescription () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_ProductDescription);
	}

	/** Set Ertrag netto.
		@param ProfitPriceActual 
		Effektiver Preis pro Einheit, minus erwartetem Skonto und vertraglicher Rückerstattung.
	  */
	@Override
	public void setProfitPriceActual (java.math.BigDecimal ProfitPriceActual)
	{
		set_Value (COLUMNNAME_ProfitPriceActual, ProfitPriceActual);
	}

	/** Get Ertrag netto.
		@return Effektiver Preis pro Einheit, minus erwartetem Skonto und vertraglicher Rückerstattung.
	  */
	@Override
	public java.math.BigDecimal getProfitPriceActual () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ProfitPriceActual);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Gelieferte Menge.
		@param QtyDelivered 
		Delivered Quantity
	  */
	@Override
	public void setQtyDelivered (java.math.BigDecimal QtyDelivered)
	{
		set_ValueNoCheck (COLUMNNAME_QtyDelivered, QtyDelivered);
	}

	/** Get Gelieferte Menge.
		@return Delivered Quantity
	  */
	@Override
	public java.math.BigDecimal getQtyDelivered () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyDelivered);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Menge.
		@param QtyEntered 
		The Quantity Entered is based on the selected UoM
	  */
	@Override
	public void setQtyEntered (java.math.BigDecimal QtyEntered)
	{
		set_Value (COLUMNNAME_QtyEntered, QtyEntered);
	}

	/** Get Menge.
		@return The Quantity Entered is based on the selected UoM
	  */
	@Override
	public java.math.BigDecimal getQtyEntered () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyEntered);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Bestellte Menge in Preiseinheit.
		@param QtyEnteredInPriceUOM 
		Bestellte Menge in Preiseinheit
	  */
	@Override
	public void setQtyEnteredInPriceUOM (java.math.BigDecimal QtyEnteredInPriceUOM)
	{
		set_ValueNoCheck (COLUMNNAME_QtyEnteredInPriceUOM, QtyEnteredInPriceUOM);
	}

	/** Get Bestellte Menge in Preiseinheit.
		@return Bestellte Menge in Preiseinheit
	  */
	@Override
	public java.math.BigDecimal getQtyEnteredInPriceUOM () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyEnteredInPriceUOM);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Berechn. Menge.
		@param QtyInvoiced 
		Menge in Produkt-Maßeinheit, die bereits in Rechnung gestellt wurde.
	  */
	@Override
	public void setQtyInvoiced (java.math.BigDecimal QtyInvoiced)
	{
		set_ValueNoCheck (COLUMNNAME_QtyInvoiced, QtyInvoiced);
	}

	/** Get Berechn. Menge.
		@return Menge in Produkt-Maßeinheit, die bereits in Rechnung gestellt wurde.
	  */
	@Override
	public java.math.BigDecimal getQtyInvoiced () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyInvoiced);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Lost Sales Qty.
		@param QtyLostSales 
		Quantity of potential sales
	  */
	@Override
	public void setQtyLostSales (java.math.BigDecimal QtyLostSales)
	{
		set_Value (COLUMNNAME_QtyLostSales, QtyLostSales);
	}

	/** Get Lost Sales Qty.
		@return Quantity of potential sales
	  */
	@Override
	public java.math.BigDecimal getQtyLostSales () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyLostSales);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Bestellt/ Beauftragt.
		@param QtyOrdered 
		Bestellt/ Beauftragt
	  */
	@Override
	public void setQtyOrdered (java.math.BigDecimal QtyOrdered)
	{
		set_Value (COLUMNNAME_QtyOrdered, QtyOrdered);
	}

	/** Get Bestellt/ Beauftragt.
		@return Bestellt/ Beauftragt
	  */
	@Override
	public java.math.BigDecimal getQtyOrdered () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyOrdered);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set QtyOrderedOverUnder.
		@param QtyOrderedOverUnder QtyOrderedOverUnder	  */
	@Override
	public void setQtyOrderedOverUnder (java.math.BigDecimal QtyOrderedOverUnder)
	{
		set_Value (COLUMNNAME_QtyOrderedOverUnder, QtyOrderedOverUnder);
	}

	/** Get QtyOrderedOverUnder.
		@return QtyOrderedOverUnder	  */
	@Override
	public java.math.BigDecimal getQtyOrderedOverUnder () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyOrderedOverUnder);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Offen.
		@param QtyReserved 
		Offene Menge
	  */
	@Override
	public void setQtyReserved (java.math.BigDecimal QtyReserved)
	{
		set_ValueNoCheck (COLUMNNAME_QtyReserved, QtyReserved);
	}

	/** Get Offen.
		@return Offene Menge
	  */
	@Override
	public java.math.BigDecimal getQtyReserved () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyReserved);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	@Override
	public org.compiere.model.I_C_OrderLine getRef_OrderLine()
	{
		return get_ValueAsPO(COLUMNNAME_Ref_OrderLine_ID, org.compiere.model.I_C_OrderLine.class);
	}

	@Override
	public void setRef_OrderLine(org.compiere.model.I_C_OrderLine Ref_OrderLine)
	{
		set_ValueFromPO(COLUMNNAME_Ref_OrderLine_ID, org.compiere.model.I_C_OrderLine.class, Ref_OrderLine);
	}

	/** Set Gegenbelegzeile-Fremdorganisation.
		@param Ref_OrderLine_ID 
		Mit diesem Feld kann eine Auftragsposition die ihr zugehörige Gegenbeleg-Position aus einer anderen Organisation referenzieren
	  */
	@Override
	public void setRef_OrderLine_ID (int Ref_OrderLine_ID)
	{
		if (Ref_OrderLine_ID < 1) 
			set_Value (COLUMNNAME_Ref_OrderLine_ID, null);
		else 
			set_Value (COLUMNNAME_Ref_OrderLine_ID, Integer.valueOf(Ref_OrderLine_ID));
	}

	/** Get Gegenbelegzeile-Fremdorganisation.
		@return Mit diesem Feld kann eine Auftragsposition die ihr zugehörige Gegenbeleg-Position aus einer anderen Organisation referenzieren
	  */
	@Override
	public int getRef_OrderLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Ref_OrderLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Revenue Recognition Amt.
		@param RRAmt 
		Revenue Recognition Amount
	  */
	@Override
	public void setRRAmt (java.math.BigDecimal RRAmt)
	{
		set_Value (COLUMNNAME_RRAmt, RRAmt);
	}

	/** Get Revenue Recognition Amt.
		@return Revenue Recognition Amount
	  */
	@Override
	public java.math.BigDecimal getRRAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_RRAmt);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Revenue Recognition Start.
		@param RRStartDate 
		Revenue Recognition Start Date
	  */
	@Override
	public void setRRStartDate (java.sql.Timestamp RRStartDate)
	{
		set_Value (COLUMNNAME_RRStartDate, RRStartDate);
	}

	/** Get Revenue Recognition Start.
		@return Revenue Recognition Start Date
	  */
	@Override
	public java.sql.Timestamp getRRStartDate () 
	{
		return (java.sql.Timestamp)get_Value(COLUMNNAME_RRStartDate);
	}

	/** Set Laufzeit.
		@param RunsMax 
		Number of recurring runs
	  */
	@Override
	public void setRunsMax (int RunsMax)
	{
		set_Value (COLUMNNAME_RunsMax, Integer.valueOf(RunsMax));
	}

	/** Get Laufzeit.
		@return Number of recurring runs
	  */
	@Override
	public int getRunsMax () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_RunsMax);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Ressourcenzuordnung.
		@param S_ResourceAssignment_ID 
		Resource Assignment
	  */
	@Override
	public void setS_ResourceAssignment_ID (int S_ResourceAssignment_ID)
	{
		if (S_ResourceAssignment_ID < 1) 
			set_Value (COLUMNNAME_S_ResourceAssignment_ID, null);
		else 
			set_Value (COLUMNNAME_S_ResourceAssignment_ID, Integer.valueOf(S_ResourceAssignment_ID));
	}

	/** Get Ressourcenzuordnung.
		@return Resource Assignment
	  */
	@Override
	public int getS_ResourceAssignment_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_S_ResourceAssignment_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** 
	 * ShipmentAllocation_BestBefore_Policy AD_Reference_ID=541043
	 * Reference name: ShipmentAllocation_BestBefore_Policy
	 */
	public static final int SHIPMENTALLOCATION_BESTBEFORE_POLICY_AD_Reference_ID=541043;
	/** Newest_First = N */
	public static final String SHIPMENTALLOCATION_BESTBEFORE_POLICY_Newest_First = "N";
	/** Expiring_First = E */
	public static final String SHIPMENTALLOCATION_BESTBEFORE_POLICY_Expiring_First = "E";
	/** Set Zuordnung Mindesthaltbarkeit.
		@param ShipmentAllocation_BestBefore_Policy Zuordnung Mindesthaltbarkeit	  */
	@Override
	public void setShipmentAllocation_BestBefore_Policy (java.lang.String ShipmentAllocation_BestBefore_Policy)
	{

		set_Value (COLUMNNAME_ShipmentAllocation_BestBefore_Policy, ShipmentAllocation_BestBefore_Policy);
	}

	/** Get Zuordnung Mindesthaltbarkeit.
		@return Zuordnung Mindesthaltbarkeit	  */
	@Override
	public java.lang.String getShipmentAllocation_BestBefore_Policy () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_ShipmentAllocation_BestBefore_Policy);
	}

	/** Set Positions-Steuer.
		@param TaxAmtInfo 
		Betrag der enthaltenen oder zuzgl. Steuer in einer Rechungs- oder Auftragsposition
	  */
	@Override
	public void setTaxAmtInfo (java.math.BigDecimal TaxAmtInfo)
	{
		set_Value (COLUMNNAME_TaxAmtInfo, TaxAmtInfo);
	}

	/** Get Positions-Steuer.
		@return Betrag der enthaltenen oder zuzgl. Steuer in einer Rechungs- oder Auftragsposition
	  */
	@Override
	public java.math.BigDecimal getTaxAmtInfo () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_TaxAmtInfo);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	@Override
	public org.compiere.model.I_C_ElementValue getUser1()
	{
		return get_ValueAsPO(COLUMNNAME_User1_ID, org.compiere.model.I_C_ElementValue.class);
	}

	@Override
	public void setUser1(org.compiere.model.I_C_ElementValue User1)
	{
		set_ValueFromPO(COLUMNNAME_User1_ID, org.compiere.model.I_C_ElementValue.class, User1);
	}

	/** Set Nutzer 1.
		@param User1_ID 
		User defined list element #1
	  */
	@Override
	public void setUser1_ID (int User1_ID)
	{
		if (User1_ID < 1) 
			set_Value (COLUMNNAME_User1_ID, null);
		else 
			set_Value (COLUMNNAME_User1_ID, Integer.valueOf(User1_ID));
	}

	/** Get Nutzer 1.
		@return User defined list element #1
	  */
	@Override
	public int getUser1_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_User1_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_C_ElementValue getUser2()
	{
		return get_ValueAsPO(COLUMNNAME_User2_ID, org.compiere.model.I_C_ElementValue.class);
	}

	@Override
	public void setUser2(org.compiere.model.I_C_ElementValue User2)
	{
		set_ValueFromPO(COLUMNNAME_User2_ID, org.compiere.model.I_C_ElementValue.class, User2);
	}

	/** Set Nutzer 2.
		@param User2_ID 
		User defined list element #2
	  */
	@Override
	public void setUser2_ID (int User2_ID)
	{
		if (User2_ID < 1) 
			set_Value (COLUMNNAME_User2_ID, null);
		else 
			set_Value (COLUMNNAME_User2_ID, Integer.valueOf(User2_ID));
	}

	/** Get Nutzer 2.
		@return User defined list element #2
	  */
	@Override
	public int getUser2_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_User2_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}