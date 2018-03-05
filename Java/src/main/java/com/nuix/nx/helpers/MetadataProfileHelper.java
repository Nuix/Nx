/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.helpers;

import nuix.MetadataProfile;

public class MetadataProfileHelper {
	/***
	 * Convenience method for determining if a given meta data profile contains a field with the given name.
	 * @param name The name to test for
	 * @param profile The profile to inspect
	 * @return True if a field with the given name (case ignored) is found in the given profile.
	 */
	public static boolean profileContainsField(String name, MetadataProfile profile){
		return profile.getMetadata().stream().anyMatch(f -> f.getName().equalsIgnoreCase(name));
	}
}
