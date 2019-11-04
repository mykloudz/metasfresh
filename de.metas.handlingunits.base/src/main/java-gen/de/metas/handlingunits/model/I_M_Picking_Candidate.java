package de.metas.handlingunits.model;


/** Generated Interface for M_Picking_Candidate
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public interface I_M_Picking_Candidate 
{

    /** TableName=M_Picking_Candidate */
    public static final String Table_Name = "M_Picking_Candidate";

    /** AD_Table_ID=540831 */
//    public static final int Table_ID = org.compiere.model.MTable.getTable_ID(Table_Name);

//    org.compiere.util.KeyNamePair Model = new org.compiere.util.KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org
     */
//    java.math.BigDecimal accessLevel = java.math.BigDecimal.valueOf(3);

    /** Load Meta Data */

	/**
	 * Get Mandant.
	 * Mandant für diese Installation.
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getAD_Client_ID();

    /** Column definition for AD_Client_ID */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_AD_Client> COLUMN_AD_Client_ID = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_AD_Client>(I_M_Picking_Candidate.class, "AD_Client_ID", org.compiere.model.I_AD_Client.class);
    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/**
	 * Set Sektion.
	 * Organisatorische Einheit des Mandanten
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setAD_Org_ID (int AD_Org_ID);

	/**
	 * Get Sektion.
	 * Organisatorische Einheit des Mandanten
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getAD_Org_ID();

    /** Column definition for AD_Org_ID */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_AD_Org> COLUMN_AD_Org_ID = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_AD_Org>(I_M_Picking_Candidate.class, "AD_Org_ID", org.compiere.model.I_AD_Org.class);
    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/**
	 * Set Status.
	 *
	 * <br>Type: List
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setApprovalStatus (java.lang.String ApprovalStatus);

	/**
	 * Get Status.
	 *
	 * <br>Type: List
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.lang.String getApprovalStatus();

    /** Column definition for ApprovalStatus */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_ApprovalStatus = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "ApprovalStatus", null);
    /** Column name ApprovalStatus */
    public static final String COLUMNNAME_ApprovalStatus = "ApprovalStatus";

	/**
	 * Get Erstellt.
	 * Datum, an dem dieser Eintrag erstellt wurde
	 *
	 * <br>Type: DateTime
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.sql.Timestamp getCreated();

    /** Column definition for Created */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_Created = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "Created", null);
    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/**
	 * Get Erstellt durch.
	 * Nutzer, der diesen Eintrag erstellt hat
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getCreatedBy();

    /** Column definition for CreatedBy */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_AD_User> COLUMN_CreatedBy = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_AD_User>(I_M_Picking_Candidate.class, "CreatedBy", org.compiere.model.I_AD_User.class);
    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/**
	 * Set Maßeinheit.
	 * Maßeinheit
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setC_UOM_ID (int C_UOM_ID);

	/**
	 * Get Maßeinheit.
	 * Maßeinheit
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getC_UOM_ID();

    /** Column definition for C_UOM_ID */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_C_UOM> COLUMN_C_UOM_ID = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_C_UOM>(I_M_Picking_Candidate.class, "C_UOM_ID", org.compiere.model.I_C_UOM.class);
    /** Column name C_UOM_ID */
    public static final String COLUMNNAME_C_UOM_ID = "C_UOM_ID";

	/**
	 * Set Aktiv.
	 * Der Eintrag ist im System aktiv
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsActive (boolean IsActive);

	/**
	 * Get Aktiv.
	 * Der Eintrag ist im System aktiv
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isActive();

    /** Column definition for IsActive */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_IsActive = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "IsActive", null);
    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/**
	 * Set Handling Unit.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setM_HU_ID (int M_HU_ID);

	/**
	 * Get Handling Unit.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getM_HU_ID();

	public de.metas.handlingunits.model.I_M_HU getM_HU();

	public void setM_HU(de.metas.handlingunits.model.I_M_HU M_HU);

    /** Column definition for M_HU_ID */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, de.metas.handlingunits.model.I_M_HU> COLUMN_M_HU_ID = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, de.metas.handlingunits.model.I_M_HU>(I_M_Picking_Candidate.class, "M_HU_ID", de.metas.handlingunits.model.I_M_HU.class);
    /** Column name M_HU_ID */
    public static final String COLUMNNAME_M_HU_ID = "M_HU_ID";

	/**
	 * Set Picking candidate.
	 *
	 * <br>Type: ID
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setM_Picking_Candidate_ID (int M_Picking_Candidate_ID);

	/**
	 * Get Picking candidate.
	 *
	 * <br>Type: ID
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getM_Picking_Candidate_ID();

    /** Column definition for M_Picking_Candidate_ID */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_M_Picking_Candidate_ID = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "M_Picking_Candidate_ID", null);
    /** Column name M_Picking_Candidate_ID */
    public static final String COLUMNNAME_M_Picking_Candidate_ID = "M_Picking_Candidate_ID";

	/**
	 * Set Picking Slot.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setM_PickingSlot_ID (int M_PickingSlot_ID);

	/**
	 * Get Picking Slot.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getM_PickingSlot_ID();

    /** Column definition for M_PickingSlot_ID */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_M_PickingSlot_ID = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "M_PickingSlot_ID", null);
    /** Column name M_PickingSlot_ID */
    public static final String COLUMNNAME_M_PickingSlot_ID = "M_PickingSlot_ID";

	/**
	 * Set Lieferdisposition.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setM_ShipmentSchedule_ID (int M_ShipmentSchedule_ID);

	/**
	 * Get Lieferdisposition.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getM_ShipmentSchedule_ID();

    /** Column definition for M_ShipmentSchedule_ID */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_M_ShipmentSchedule_ID = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "M_ShipmentSchedule_ID", null);
    /** Column name M_ShipmentSchedule_ID */
    public static final String COLUMNNAME_M_ShipmentSchedule_ID = "M_ShipmentSchedule_ID";

	/**
	 * Set Pack To.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setPackTo_HU_PI_ID (int PackTo_HU_PI_ID);

	/**
	 * Get Pack To.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getPackTo_HU_PI_ID();

	public de.metas.handlingunits.model.I_M_HU_PI getPackTo_HU_PI();

	public void setPackTo_HU_PI(de.metas.handlingunits.model.I_M_HU_PI PackTo_HU_PI);

    /** Column definition for PackTo_HU_PI_ID */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, de.metas.handlingunits.model.I_M_HU_PI> COLUMN_PackTo_HU_PI_ID = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, de.metas.handlingunits.model.I_M_HU_PI>(I_M_Picking_Candidate.class, "PackTo_HU_PI_ID", de.metas.handlingunits.model.I_M_HU_PI.class);
    /** Column name PackTo_HU_PI_ID */
    public static final String COLUMNNAME_PackTo_HU_PI_ID = "PackTo_HU_PI_ID";

	/**
	 * Set Pick From HU.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setPickFrom_HU_ID (int PickFrom_HU_ID);

	/**
	 * Get Pick From HU.
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getPickFrom_HU_ID();

	public de.metas.handlingunits.model.I_M_HU getPickFrom_HU();

	public void setPickFrom_HU(de.metas.handlingunits.model.I_M_HU PickFrom_HU);

    /** Column definition for PickFrom_HU_ID */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, de.metas.handlingunits.model.I_M_HU> COLUMN_PickFrom_HU_ID = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, de.metas.handlingunits.model.I_M_HU>(I_M_Picking_Candidate.class, "PickFrom_HU_ID", de.metas.handlingunits.model.I_M_HU.class);
    /** Column name PickFrom_HU_ID */
    public static final String COLUMNNAME_PickFrom_HU_ID = "PickFrom_HU_ID";

	/**
	 * Set Komm. Status.
	 *
	 * <br>Type: List
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setPickStatus (java.lang.String PickStatus);

	/**
	 * Get Komm. Status.
	 *
	 * <br>Type: List
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.lang.String getPickStatus();

    /** Column definition for PickStatus */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_PickStatus = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "PickStatus", null);
    /** Column name PickStatus */
    public static final String COLUMNNAME_PickStatus = "PickStatus";

	/**
	 * Set Menge (Lagereinheit).
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setQtyPicked (java.math.BigDecimal QtyPicked);

	/**
	 * Get Menge (Lagereinheit).
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getQtyPicked();

    /** Column definition for QtyPicked */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_QtyPicked = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "QtyPicked", null);
    /** Column name QtyPicked */
    public static final String COLUMNNAME_QtyPicked = "QtyPicked";

	/**
	 * Set Qty Review.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setQtyReview (java.math.BigDecimal QtyReview);

	/**
	 * Get Qty Review.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getQtyReview();

    /** Column definition for QtyReview */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_QtyReview = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "QtyReview", null);
    /** Column name QtyReview */
    public static final String COLUMNNAME_QtyReview = "QtyReview";

	/**
	 * Set Status.
	 *
	 * <br>Type: List
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setStatus (java.lang.String Status);

	/**
	 * Get Status.
	 *
	 * <br>Type: List
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.lang.String getStatus();

    /** Column definition for Status */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_Status = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "Status", null);
    /** Column name Status */
    public static final String COLUMNNAME_Status = "Status";

	/**
	 * Get Aktualisiert.
	 * Datum, an dem dieser Eintrag aktualisiert wurde
	 *
	 * <br>Type: DateTime
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.sql.Timestamp getUpdated();

    /** Column definition for Updated */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object> COLUMN_Updated = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, Object>(I_M_Picking_Candidate.class, "Updated", null);
    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/**
	 * Get Aktualisiert durch.
	 * Nutzer, der diesen Eintrag aktualisiert hat
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getUpdatedBy();

    /** Column definition for UpdatedBy */
    public static final org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_AD_User> COLUMN_UpdatedBy = new org.adempiere.model.ModelColumn<I_M_Picking_Candidate, org.compiere.model.I_AD_User>(I_M_Picking_Candidate.class, "UpdatedBy", org.compiere.model.I_AD_User.class);
    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";
}
