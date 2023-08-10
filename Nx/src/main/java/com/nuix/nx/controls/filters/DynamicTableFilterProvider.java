package com.nuix.nx.controls.filters;

import java.util.List;
import java.util.Map;

import com.nuix.nx.controls.models.DynamicTableModel;
import com.nuix.nx.controls.models.DynamicTableValueCallback;

public abstract class DynamicTableFilterProvider {
	/***
	 * Whether this filter provider handles the given filter expression.  If true is returned, {@link #keepRecord(int, boolean, String, Object, Map)}
	 * will be called for each record to determine whether the given record is filtered out or not.  If false is returned then
	 * the dynamic table model will continue looking for a filter handler.  Filter expression should never be null or an all whitespace or empty
	 * string since {@link DynamicTableModel} has built in logic to handle that before asking filters.
	 * @param filterExpression The expression the user has provided.
	 * @return True if this filter should handle this expression.  False otherwise.
	 */
	public abstract boolean handlesExpression(String filterExpression);
	
	/***
	 * If {@link #handlesExpression(String)} returns true, this method will be invoked for each record.  It should return
	 * true for records that should make it into the final collection and false for records that should be filtered out.
	 * @param sourceIndex The index of the record in the full un-filtered source collection.
	 * @param isChecked Whether the given record is currently checked
	 * @param filterExpression The filter expression provided by the user.  This value should never be null, only whitespace or empty.
	 * @param record The record to inspect and make a decision about.
	 * @param rowValues A map of the values actual represented as columns in the table.  Relies on table model's {@link DynamicTableValueCallback}.
	 * @return True to keep the record, false to filter it out.
	 */
	public abstract boolean keepRecord(int sourceIndex, boolean isChecked, String filterExpression,
			Object record, Map<String,Object> rowValues);
	
	/***
	 * Invoked once before filtering begins, allowing for up front work to be performed that might
	 * then be leveraged in subsequent calls to {@link #keepRecord(int, boolean, String, Object, Map)}.
	 * Override in derived implementation, default implementation does nothing.
	 * @param filterExpression The filter expression that was provided
	 * @param allRecords All the of records pre-filtering
	 */
	public void beforeFiltering(String filterExpression, List<Object> allRecords) {}
	
	/***
	 * Called once after filtering.  Can be used to clean up any resources that may have been created by calls to {@link #beforeFiltering(String, List)}
	 * and/or {@link #keepRecord(int, boolean, String, Object, Map)}.
	 */
	public void afterFiltering() {}
}
