package com.nuix.nx.filters;

import com.nuix.nx.controls.filters.DynamicTableFilterProvider;

import java.util.Map;

public class DynamicTableContainsFilter extends DynamicTableFilterProvider {

	@Override
	public boolean handlesExpression(String filterExpression) {
		return true;
	}

	@Override
	public boolean keepRecord(int sourceIndex, boolean isChecked, String filterExpression, Object record,
			Map<String, Object> rowValues) {
		boolean result = false;
		for(Map.Entry<String, Object> entry : rowValues.entrySet()) {
			// Easy way to get string value of most common types
			String stringValue = String.format("%s", entry.getValue());
			// Does string value contain the filter expression?
			if(stringValue.contains(filterExpression)) {
				// Any match will be considered a success and keeps the record so we
				// do not need to keep looking
				result = true;
				break;
			}
		}
		return result;
	}

}
