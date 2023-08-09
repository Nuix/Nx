/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.callbacks;

/***
 * Used as a way to provide simple feedback regarding progress which can be quantified
 * as amount completed over total amount.
 * @author Jason Wells
 *
 */
public interface SimpleProgressCallback {
	public void progressUpdated(long current, long total);
}
