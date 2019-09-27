package de.metas.handlingunits.client.terminal.empties.model;

import static org.adempiere.model.InterfaceWrapperHelper.loadOutOfTrx;

/*
 * #%L
 * de.metas.handlingunits.client
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

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.adempiere.ad.dao.ConstantQueryFilter;
import org.adempiere.ad.dao.IQueryFilter;
import org.adempiere.ad.dao.impl.EqualsQueryFilter;
import org.adempiere.ad.service.IADReferenceDAO;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.exceptions.FillMandatoryException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.apps.AEnv;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_M_InOut;
import org.compiere.model.I_M_Warehouse;
import org.compiere.model.X_M_Transaction;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

import de.metas.adempiere.form.terminal.IKeyLayout;
import de.metas.adempiere.form.terminal.IKeyLayoutSelectionModel;
import de.metas.adempiere.form.terminal.ITerminalKey;
import de.metas.adempiere.form.terminal.ITerminalLookup;
import de.metas.adempiere.form.terminal.context.ITerminalContext;
import de.metas.adempiere.form.terminal.lookup.SimpleTableLookup;
import de.metas.bpartner.service.IBPartnerDAO;
import de.metas.handlingunits.client.terminal.mmovement.exception.MaterialMovementException;
import de.metas.handlingunits.client.terminal.mmovement.model.impl.AbstractLTCUModel;
import de.metas.handlingunits.client.terminal.select.model.BPartnerLocationKey;
import de.metas.handlingunits.client.terminal.select.model.BPartnerLocationKeyLayout;
import de.metas.handlingunits.empties.IHUEmptiesService;
import de.metas.handlingunits.inout.IReturnsInOutProducer;
import de.metas.handlingunits.model.I_M_HU_PI;
import de.metas.handlingunits.model.I_M_HU_PackingMaterial;
import de.metas.handlingunits.model.I_M_ReceiptSchedule;
import de.metas.handlingunits.model.X_M_HU_PI_Version;
import de.metas.util.Check;
import de.metas.util.Services;

/**
 *
 * @author tsa
 *
 * @task http://dewiki908/mediawiki/index.php/07193_R%C3%BCcknahme_Gebinde_%28104585385527%29
 */
public class EmptiesShipReceiveModel extends AbstractLTCUModel
{
	// services
	private final transient IBPartnerDAO bpartnerDAO = Services.get(IBPartnerDAO.class);
	private final transient IHUEmptiesService huEmptiesService = Services.get(IHUEmptiesService.class);

	public enum BPartnerReturnType
	{
		ReturnToVendor(X_M_Transaction.MOVEMENTTYPE_VendorReturns, Color.CYAN), ReturnFromCustomer(X_M_Transaction.MOVEMENTTYPE_CustomerReturns, Color.ORANGE);

		private final String movementType;
		private final Color color;

		private BPartnerReturnType(final String movementType, final Color color)
		{
			Check.assumeNotEmpty(movementType, "movementType not empty");
			this.movementType = movementType;
			this.color = color;
		}

		public String getMovementType()
		{
			return movementType;
		}

		public Color getColor()
		{
			return color;
		}

		public final String getDisplayName(final Properties ctx)
		{
			final IADReferenceDAO adReferenceDAO = Services.get(IADReferenceDAO.class);
			return adReferenceDAO.retrieveListNameTrl(ctx, X_M_Transaction.MOVEMENTTYPE_AD_Reference_ID, movementType);
		}
	}

	private static final String PROPERTY_BPartnerReturnType = "BPartnerReturnType";
	private static final String PROPERTY_BPartner = "BPartner";

	/**
	 * M_InOut SO
	 */
	private static final int WINDOW_CUSTOMER_RETURN = 53097; // FIXME: HARDCODED

	/**
	 * M_InOut PO
	 */
	private static final int WINDOW_RETURN_TO_VENDOR = 53098; // FIXME: HARDCODED

	private final I_M_Warehouse _warehouse;
	private BPartnerReturnType _bpartnerReturnType = null;
	private KeyNamePair _bpartnerKNP = null;

	/**
	 * BPartner for which the empties inout will be created
	 */
	private I_C_BPartner _bpartner;

	/**
	 * The BPartner Location for which the empties inout will be created
	 */
	private org.compiere.model.I_C_BPartner_Location _bpLocation = null;
	private final SimpleTableLookup<I_C_BPartner> bpartnerLookup = new SimpleTableLookup<>(I_C_BPartner.class, I_C_BPartner.COLUMNNAME_C_BPartner_ID, I_C_BPartner.COLUMNNAME_Name);
	private Date _date;
	private final BPartnerLocationKeyLayout _bpLocationKeyLayout;

	// #643: Order
	private I_C_Order _order;

	public EmptiesShipReceiveModel(
			final ITerminalContext terminalContext, final int warehouseId, final I_M_ReceiptSchedule receiptSchedule)
	{
		super(terminalContext);

		Check.assumeNotNull(warehouseId > 0, "warehouse > 0");
		_warehouse = InterfaceWrapperHelper.create(terminalContext.getCtx(), warehouseId, I_M_Warehouse.class, ITrx.TRXNAME_None);
		Check.assumeNotNull(_warehouse, "warehouse not null");

		_date = Env.getDate(terminalContext.getCtx()); // use Login date (08306)

		if (receiptSchedule != null)
		{
			_bpartner = loadOutOfTrx(receiptSchedule.getC_BPartner_ID(), I_C_BPartner.class);
			_bpLocation = loadOutOfTrx(receiptSchedule.getC_BPartner_Location_ID(), I_C_BPartner_Location.class);
			_order = receiptSchedule.getC_Order();
		}

		// load bpartner if selected. This will be the suggested bpartner. It is free for the user to change it if needed
		if (_bpartner != null)
		{

			_bpartnerKNP = new KeyNamePair(_bpartner.getC_BPartner_ID(), _bpartner.getName());

			// In case the bpartner was selected or taken from order/ receipt schedule, the suggested return type will be ReturnToVendor
			_bpartnerReturnType = BPartnerReturnType.ReturnToVendor;

		}

		{
			_bpLocationKeyLayout = new BPartnerLocationKeyLayout(terminalContext);
			_bpLocationKeyLayout.setColumns(7);
			_bpLocationKeyLayout.setRows(2);
			final IKeyLayoutSelectionModel bpLocationSelectionModel = _bpLocationKeyLayout.getKeyLayoutSelectionModel();
			bpLocationSelectionModel.setAllowKeySelection(true);
			bpLocationSelectionModel.setAutoSelectIfOnlyOne(true);
		}

		// setBpartnerReturnType(BPartnerReturnType.ReturnToVendor); // nothing shall be selected by default
		loadEmptiesKey();
		loadBPartnerLocations();
	}

	private final void loadEmptiesKey()
	{
		final List<EmptiesKey> luEmpties = new ArrayList<>();
		final List<EmptiesKey> tuEmpties = new ArrayList<>();
		final int adOrgID = Env.getAD_Org_ID(getTerminalContext().getCtx());
		final List<I_M_HU_PI> huPIs = handlingUnitsDAO.retrieveAvailablePIsForOrg(getTerminalContext().getCtx(), adOrgID);
		for (final I_M_HU_PI huPI : huPIs)
		{
			// Skip PIs which does not have an UnitType set
			final String huUnitType = handlingUnitsBL.getHU_UnitType(huPI);
			if (Check.isEmpty(huUnitType, true))
			{
				continue;
			}

			final EmptiesKey huPIKey = new EmptiesKey(getTerminalContext(), huPI, huUnitType);
			if (X_M_HU_PI_Version.HU_UNITTYPE_LoadLogistiqueUnit.equals(huUnitType))
			{
				luEmpties.add(huPIKey);
			}
			else if (X_M_HU_PI_Version.HU_UNITTYPE_TransportUnit.equals(huUnitType))
			{
				tuEmpties.add(huPIKey);
			}
			else
			{
				// we don't care about other types
			}
		}

		getTUKeyLayout().setKeys(tuEmpties);
		getLUKeyLayout().setKeys(luEmpties);
	}

	@Override
	public void execute() throws MaterialMovementException
	{
		try
		{
			final I_M_InOut emptiesInOut = createEmptiesInOut();

			//
			// Open window with shipment document for the user if it was created successfully
			if (emptiesInOut != null)
			{
				AEnv.zoom(I_M_InOut.Table_Name, emptiesInOut.getM_InOut_ID(), WINDOW_CUSTOMER_RETURN, WINDOW_RETURN_TO_VENDOR);
			}
		}
		catch (Exception ex)
		{
			throw new MaterialMovementException(ex.getLocalizedMessage(), ex);
		}
	}

	private final I_M_InOut createEmptiesInOut()
	{

		final IReturnsInOutProducer producer = huEmptiesService.newReturnsInOutProducer(getCtx());

		producer.setC_BPartner(getC_BPartner());
		producer.setC_BPartner_Location(getC_BPartner_Location());

		final BPartnerReturnType bpartnerReturnType = getBPartnerReturnType();
		Check.assumeNotNull(bpartnerReturnType, "bpartnerReturnType not null");
		producer.setMovementType(bpartnerReturnType.getMovementType());
		producer.setM_Warehouse(getM_Warehouse());

		producer.setMovementDate(getDate());

		// task #643: Set the order to the producer
		producer.setC_Order(getOrder());

		addPackingMaterialsFromKeyLayout(producer, getLUKeyLayout());
		addPackingMaterialsFromKeyLayout(producer, getTUKeyLayout());

		if (producer.isEmpty())
		{
			throw new MaterialMovementException("@NoSelection@");
		}

		//
		// Create Shipment document and return it
		final I_M_InOut inOut = producer.create();
		return inOut;
	}

	private void addPackingMaterialsFromKeyLayout(final IReturnsInOutProducer producer, final IKeyLayout keyLayout)
	{
		Check.assumeNotNull(keyLayout, "keyLayout not null");

		for (final EmptiesKey key : keyLayout.getKeys(EmptiesKey.class))
		{
			final int qty = key.getQty();
			if (qty <= 0)
			{
				continue;
			}

			final List<I_M_HU_PackingMaterial> packingMaterials = key.getM_HU_PackingMaterials(getC_BPartner());
			if (packingMaterials.isEmpty())
			{
				throw new AdempiereException("@NotFound@ @M_HU_PackingMaterial_ID@"
						+ "\n @M_HU_PI_ID@: " + key.getPIName());
			}

			for (final I_M_HU_PackingMaterial packingMaterial : packingMaterials)
			{
				producer.addPackingMaterial(packingMaterial, qty);
			}
		}
	}

	@Override
	protected void onCUPressed(final ITerminalKey key)
	{
		throw new IllegalStateException("Shall not be called");
	}

	@Override
	protected void onTUPressed(final ITerminalKey key)
	{
		final EmptiesKey emptiesKey = (EmptiesKey)key;
		setQtyTU(BigDecimal.valueOf(emptiesKey.getQty()));

		updateUIStatus();
	}

	@Override
	protected void onQtyTUChanged(final BigDecimal qtyTU, final BigDecimal qtyTUOld)
	{
		setSelectedEmptiesKeyQty(getTUKeyLayout(), qtyTU);
	}

	@Override
	protected void onLUPressed(final ITerminalKey key)
	{
		final EmptiesKey emptiesKey = (EmptiesKey)key;
		setQtyLU(BigDecimal.valueOf(emptiesKey.getQty()));

		updateUIStatus();
	}

	@Override
	protected void onQtyLUChanged(final BigDecimal qtyLU, final BigDecimal qtyLUOld)
	{
		setSelectedEmptiesKeyQty(getLUKeyLayout(), qtyLU);
	}

	private void setSelectedEmptiesKeyQty(final IKeyLayout keyLayout, final BigDecimal qty)
	{
		final EmptiesKey emptiesKey = keyLayout.getKeyLayoutSelectionModel().getSelectedKeyOrNull(EmptiesKey.class);
		if (emptiesKey == null)
		{
			// shall not happen, but skip it
			return;
		}

		emptiesKey.setQty(qty);
	}

	private final void updateUIStatus()
	{
		final boolean qtyTUFieldReadonly = getTUKeyLayout().getKeyLayoutSelectionModel().isEmpty();
		setQtyTUReadonly(qtyTUFieldReadonly);

		final boolean qtyLUFieldReadonly = getLUKeyLayout().getKeyLayoutSelectionModel().isEmpty();
		setQtyLUReadonly(qtyLUFieldReadonly);
	}

	public final BPartnerReturnType getBPartnerReturnType()
	{
		return _bpartnerReturnType;
	}

	public final void setBpartnerReturnType(final BPartnerReturnType bpartnerReturnType)
	{
		if (_bpartnerReturnType == bpartnerReturnType)
		{
			return;
		}

		// Check.assumeNotNull(bpartnerReturnType, "bpartnerReturnType not null"); // we allow null
		final BPartnerReturnType bpartnerReturnTypeOld = _bpartnerReturnType;
		_bpartnerReturnType = bpartnerReturnType;

		//
		// Update BPartner Filter
		setBPartnerLookupFilter();

		firePropertyChanged(PROPERTY_BPartnerReturnType, bpartnerReturnTypeOld, _bpartnerReturnType);
	}

	private final void setBPartnerLookupFilter()
	{
		final BPartnerReturnType bpartnerReturnType = getBPartnerReturnType();

		final IQueryFilter<I_C_BPartner> bpartnerFilter;
		if (bpartnerReturnType == null)
		{
			bpartnerFilter = ConstantQueryFilter.of(false);
		}
		else if (bpartnerReturnType == BPartnerReturnType.ReturnToVendor)
		{
			bpartnerFilter = new EqualsQueryFilter<>(I_C_BPartner.COLUMNNAME_IsVendor, true);
		}
		else if (bpartnerReturnType == BPartnerReturnType.ReturnFromCustomer)
		{
			bpartnerFilter = new EqualsQueryFilter<>(I_C_BPartner.COLUMNNAME_IsCustomer, true);
		}
		else
		{
			throw new AdempiereException("@NotSupported@ BPartner return type: " + bpartnerReturnType);
		}
		bpartnerLookup.setFilters(bpartnerFilter);

		// request BPartner Re-validation
		setBPartner(null);
	}

	public final KeyNamePair getBPartner()
	{
		return _bpartnerKNP;
	}

	public final int getC_BPartner_ID()
	{
		final KeyNamePair bpartner = getBPartner();
		if (bpartner == null || bpartner.getKey() <= 0)
		{
			throw new FillMandatoryException("C_BPartner_ID");
		}
		return bpartner.getKey();
	}

	public final I_C_BPartner getC_BPartner()
	{
		if (_bpartner == null)
		{
			final int bpartnerId = getC_BPartner_ID();
			_bpartner = InterfaceWrapperHelper.create(getCtx(), bpartnerId, I_C_BPartner.class, ITrx.TRXNAME_None);
		}

		Check.assumeNotNull(_bpartner, "bpartner not null");
		return _bpartner;
	}

	public final void setBPartner(final KeyNamePair bpartner)
	{
		if (_bpartnerKNP == bpartner)
		{
			return;
		}

		final KeyNamePair bpartnerOld = _bpartnerKNP;
		_bpartnerKNP = bpartner;
		_bpartner = null; // needs to be re-fetched
		_bpLocation = null;
		firePropertyChanged(PROPERTY_BPartner, bpartnerOld, _bpartnerKNP);

		//
		// Load Ship locations
		loadBPartnerLocations();
	}

	public final I_C_BPartner_Location getC_BPartner_Location()
	{
		return getBPartnerLocationKeyLayout()
				.getSelectedKey()
				.getC_BPartner_Location(I_C_BPartner_Location.class);
	}

	private final void loadBPartnerLocations()
	{
		if (getBPartner() == null)
		{
			_bpLocationKeyLayout.createAndSetKeysFromBPartnerLocations(null);
			return;
		}

		final I_C_BPartner bpartner = getC_BPartner();
		final List<I_C_BPartner_Location> shipToLocations = bpartnerDAO.retrieveBPartnerShipToLocations(bpartner);
		_bpLocationKeyLayout.createAndSetKeysFromBPartnerLocations(shipToLocations);

		// in case the location was already taken from the order / receipt schedule
		// It is safe to consider this location as a shipTo location because all C_BPartner_Locations in order line must be ShipTo
		if (_bpLocation != null)
		{
			final ITerminalContext terminalContext = getTerminalContext();

			// recreate the key the same way as for the key layout
			final BPartnerLocationKey key = new BPartnerLocationKey(terminalContext, _bpLocation);

			// mark this key as selected.
			_bpLocationKeyLayout.setSelectedKey(key);
		}
		else
		{
			// if no location was selected, just select the first one, as before
			_bpLocationKeyLayout.selectFirstKeyIfAny();
		}
	}

	public I_M_Warehouse getM_Warehouse()
	{
		return _warehouse;
	}

	public final ITerminalLookup getBPartnerLookup()
	{
		return bpartnerLookup;
	}

	public BPartnerLocationKeyLayout getBPartnerLocationKeyLayout()
	{
		return _bpLocationKeyLayout;
	}

	public Date getDate()
	{
		return _date;
	}

	public void setDate(final Date date)
	{
		_date = date;
	}

	public void setOrder(I_C_Order order)
	{
		_order = order;
	}

	public I_C_Order getOrder()
	{
		return _order;
	}
}
