package com.nuix.nx.controls.filters;

import java.util.Map;

public class DynamicTableCheckedRecordsFilter extends DynamicTableFilterProvider {

	/***
	 * {@inheritDoc}<br>
	 * This implementation returns true if provided expression is ":checked:" (case-insensitive).
	 */
	@Override
	public boolean handlesExpression(String filterExpression) {
		return filterExpression.equalsIgnoreCase(":checked:");
	}

	/***
	 * {@inheritDoc}<br>
	 * This implementation returns true if <code>isChecked</code> is true.
	 */
	@Override
	public boolean keepRecord(int sourceIndex, boolean isChecked, String filterExpression,
			Object record, Map<String,Object> rowValues) {
		return isChecked == true;
	}

}
