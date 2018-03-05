/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls.models;

import java.awt.Component;

/***
 * Callback used to allow code to define a custom deserialization for a particular control from JSON.
 * @author JWells01
 *
 */
public interface ControlDeserializationHandler {
	public void deserializeControlData(Object data, Component destinationControl);
}
