/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

// Callback used for some of the path selection controls on CustomTabPanel
// allowing a script to react to a user selecting a path
public interface PathSelectedCallback {
	public void pathSelected(String selectedPath);
}
