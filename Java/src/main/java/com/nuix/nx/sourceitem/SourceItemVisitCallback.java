/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.sourceitem;

import nuix.SourceItem;

/***
 * Callback used by {@link SourceItemVisitor}.
 * @author JasonWells
 *
 */
public interface SourceItemVisitCallback {
	/***
	 * Called once for each allowed item.
	 * @param sourceItem The source item being visited.
	 * @return True if children should be visited as well, false if children should not be visited.
	 */
	public boolean visitSourceItem(SourceItem sourceItem);
}
