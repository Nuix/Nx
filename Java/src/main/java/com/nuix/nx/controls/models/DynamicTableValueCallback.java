/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls.models;
/***
 * Callback used by {@link com.nuix.nx.controls.DynamicTableControl} to allow calling code (likely a Ruby script)
 * to save changes a user has made to a record in the table.
 * @author Jason Wells
 *
 */
public interface DynamicTableValueCallback {
	public Object interact(Object record, int i, boolean setValue, Object aValue);
}
