/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx;

import nuix.Case;
import nuix.Utilities;

/***
 * This class provides a way to hand required Nuix objects over to the library
 * for the methods that need them.  In the least you will probably want to call
 * {@link #setUtilities(Utilities)} before using any of the classes in the
 * com.nuix.nx.* package.
 * @author Jason Wells
 *
 */
public class NuixConnection {
	private static Utilities utilities;
	private static Case currentCase;
	private static NuixVersion currentVersion;
	
	/***
	 * Gets the instance of Utilities provided by previous call to {@link #setUtilities(Utilities)}
	 * @return Utilities instance provided by code using library, or null if {@link #setUtilities(Utilities)} was not yet called
	 */
	public static Utilities getUtilities() {
		return utilities;
	}
	/***
	 * Sets the instance of Utilities for the current session.  It is important to note that without making this
	 * call aspects of this library may fail with exceptions as they need to have an instance of Utilities to function.
	 * It is recommended that any code using this library call this shortly after loading the JAR file.
	 * @param utilities The Nuix Utilities object associated with the current session.
	 */
	public static void setUtilities(Utilities utilities) {
		NuixConnection.utilities = utilities;
	}
	
	/***
	 * Get the Nuix case provided via {@link #setCurrentCase(Case)}
	 * @return The case previously provided or null if no case has been provided
	 */
	public static Case getCurrentCase() {
		return currentCase;
	}
	/***
	 * Set the Nuix case to be considered the "current" case
	 * @param currentCase The case to set as being the "current" case
	 */
	public static void setCurrentCase(Case currentCase) {
		NuixConnection.currentCase = currentCase;
	}
	
	/***
	 * Sets the current Nuix version.  This may be used by library to detect features which are not available
	 * in a given version of Nuix.
	 * @param version A String containing the current Nuix version.
	 */
	public static void setCurrentNuixVersion(String version){
		currentVersion = NuixVersion.parse(version);
	}
	/***
	 * Gets a {@link NuixVersion} object representing the current version of Nuix, assuming code using the library has
	 * previously made a call to {@link #setCurrentNuixVersion(String)} previously.
	 * @return A {@link NuixVersion} if previously set by call to {@link #setCurrentNuixVersion(String)}, else null.
	 */
	public static NuixVersion getCurrentNuixVersion(){
		return currentVersion;
	}
}
