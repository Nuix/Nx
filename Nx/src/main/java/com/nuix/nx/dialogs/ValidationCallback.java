/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.dialogs;

import java.util.Map;

/***
 * Interface for a callback that allows the code being called to approve or disapprove of the current state of the settings in
 * a {@link TabbedCustomDialog}.  Used by {@link TabbedCustomDialog#validateBeforeClosing(ValidationCallback)}.
 * @author Jason Wells
 *
 */
public interface ValidationCallback {
	/***
	 * A method used to validate.
	 * @param currentValues A map of the current control values
	 * @return Return false if something is not acceptable, otherwise true.
	 */
	public boolean validate(Map<String,Object> currentValues);
}
