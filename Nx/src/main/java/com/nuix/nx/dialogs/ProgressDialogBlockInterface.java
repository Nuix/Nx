/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.dialogs;

/**
 * Interface to be implemented to provide code to be ran by the {@link ProgressDialog}.
 * @author Jason Wells
 *
 */
public interface ProgressDialogBlockInterface {
	public void DoWork(ProgressDialog dialog);
}
