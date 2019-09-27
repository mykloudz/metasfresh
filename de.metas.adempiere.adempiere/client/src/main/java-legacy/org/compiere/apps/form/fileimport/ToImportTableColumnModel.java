package org.compiere.apps.form.fileimport;

import org.compiere.util.DisplayType;

import de.metas.impexp.ImpDataLine;

class ToImportTableColumnModel extends TableColumnModel
{

	public ToImportTableColumnModel(int columnIndex)
	{
		super(columnIndex);
	}

	@Override
	public String getColumnDisplayName()
	{
		return "";
	}

	@Override
	public Class<?> getColumnClass()
	{
		return Boolean.class;
	}
	
	@Override
	public int getWidth()
	{
		return 25;
	}
	
	@Override
	public boolean isCellEditable(ImpDataLine dataLine)
	{
		return dataLine.isImportable();
	}

	@Override
	public Object getValue(ImpDataLine dataLine)
	{
		return dataLine.isToImport();
	}

	@Override
	public void setValue(ImpDataLine dataLine, Object value)
	{
		final boolean valueBool = DisplayType.toBoolean(value);
		dataLine.setToImport(valueBool);
	}
}
