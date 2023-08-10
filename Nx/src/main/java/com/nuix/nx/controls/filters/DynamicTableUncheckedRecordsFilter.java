package com.nuix.nx.controls.filters;

import java.util.Map;

public class DynamicTableUncheckedRecordsFilter extends DynamicTableFilterProvider {

	/***
	 * {@inheritDoc}<br>
	 * This implementation returns true if provided expression is ":unchecked:" (case-insensitive).
	 */
	@Override
	public boolean handlesExpression(String filterExpression) {
		return filterExpression.equalsIgnoreCase(":unchecked:");
	}

	/***
	 * {@inheritDoc}<br>
	 * This implementation returns true if <code>isChecked</code> is false.
	 */
	@Override
	public boolean keepRecord(int sourceIndex, boolean isChecked, String filterExpression,
			Object record, Map<String,Object> rowValues) {
		return isChecked == false;
	}

}
