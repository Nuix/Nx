/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.models;

import java.awt.Component;

/***
 * Callback used to allow code to define a custom serialization for a particular control to JSON.
 * @author Jason Wells
 *
 */
public interface ControlSerializationHandler {
	public Object serializeControlData(Component sourceControl);
}
