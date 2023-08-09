/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

/*** 
 * Callback used for some of the path selection controls on {@link com.nuix.nx.dialogs.CustomTabPanel}
 * allowing a script to react to a user selects a path.
 * @author Jason Wells
 *
 */
public interface PathSelectedCallback {
	public void pathSelected(String selectedPath);
}
