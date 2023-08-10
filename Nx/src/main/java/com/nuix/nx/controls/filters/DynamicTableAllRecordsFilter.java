package com.nuix.nx.controls.filters;

import java.util.Map;

public class DynamicTableAllRecordsFilter extends DynamicTableFilterProvider {

	@Override
	public boolean handlesExpression(String filterExpression) {
		return true;
	}

	@Override
	public boolean keepRecord(int sourceIndex, boolean isChecked, String filterExpression,
			Object record, Map<String,Object> rowValues) {
		return true;
	}

}
