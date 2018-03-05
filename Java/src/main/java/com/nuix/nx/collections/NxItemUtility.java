/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.nuix.nx.NuixConnection;

import nuix.Case;
import nuix.Item;

/***
 * Convenience methods for some common operations that are not offered currently
 * in the Nuix API
 * @author Jason Wells
 *
 */
public class NxItemUtility {
	// Regular expression used to strip hyphens from GUIDs
	private static Pattern hyphenRemover = Pattern.compile("\\-");
	
	/***
	 * Fetches the items associated with the provided list of GUID strings by searching for them in
	 * batches, which helps mitigate performance issues which can arise from searching for especially
	 * large GUID queries.
	 * @param nuixCase The case object that will be searched against to resolve the GUIDs to their items.
	 * @param guids The list of GUIDs to resolve into a list of items.
	 * @param batchSize How many GUIDs will be searched for at once.
	 * @return All the items which matched the GUIDs provided.
	 * @throws IOException Likely thrown if there is an error while searching.
	 */
	public static List<Item> itemsFromGuids(Case nuixCase, List<String> guids, int batchSize) throws IOException{
		List<Item> result = new ArrayList<Item>();
		int lastIndex = guids.size() - 1;
		int batches = (int)Math.ceil(((float)guids.size()) / ((float)batchSize));
		for (int i = 0; i < batches; i++) {
			int start = batchSize * i;
			int end = start + batchSize;
			if(end > lastIndex){
				end = lastIndex;
			}
			List<String> guidsSlice = guids.subList(start,end);
			String sliceQuery = generateGuidQuery(guidsSlice);
			result.addAll(nuixCase.search(sliceQuery));
		}
		return result;
	}
	
	/***
	 * Fetches the items associated with the provided list of GUID strings by searching for them in
	 * batches, which helps mitigate performance issues which can arise from searching for especially
	 * large GUID queries.  Uses a default batch size of 2000 GUIDs per search.
	 * @param nuixCase The case object that will be searched against to resolve the GUIDs to their items.
	 * @param guids The list of GUIDs to resolve into a list of items.
	 * @return All the items which matched the GUIDs provided.
	 * @throws IOException Likely thrown if there is an error while searching.
	 */
	public static List<Item> itemsFromGuids(Case nuixCase, List<String> guids) throws IOException{
		return itemsFromGuids(nuixCase,guids,2000);
	}
	
	/***
	 * Generates a GUID query from the provided list of GUIDs.  For example if the provided list contained
	 * A, B and C then the resulting query would be guid:("A" OR "B" or "C")
	 * @param guids The collection of GUIDs to include in the query
	 * @return A Nuix query suitable for searching for multiple GUIDs
	 */
	public static String generateGuidQuery(Collection<String> guids){
		List<String> quotedGuids = guids.stream().map(g -> hyphenRemover.matcher(g).replaceAll("")).collect(Collectors.toList());
		String query = "guid:(" + String.join(" OR ", quotedGuids) + ")";
		return query;
	}
	
	/***
	 * Generates a GUID query from the provided list of Items.  For example if the provided list contained
	 * A, B and C then the resulting query would be guid:("A" OR "B" or "C")
	 * @param items The list of items to include in the query
	 * @return A Nuix query suitable for searching for multiple GUIDs
	 */
	public static String generateGuidQueryFromItems(Collection<Item> items){
		return generateGuidQuery(items.stream().map(i -> i.getGuid()).collect(Collectors.toList()));
	}
	
	/***
	 * Generates a GUID query to find the families of a collection of provided items.
	 * @param items The items for which you wish to build the family query for.
	 * @return The query String.
	 */
	public static String generateFamiliesQuery(Collection<Item> items){
		StringJoiner guids = new StringJoiner(" OR ");
		for(Item item : NuixConnection.getUtilities().getItemUtility().findTopLevelItems(items)){
			guids.add(item.getGuid());
		}
		String guidsJoined = guids.toString();
		return "has-exclusion:0 AND (guid:("+guidsJoined+") OR path-guid:("+guidsJoined+"))";
	}
	
	/***
	 * This is a convenience method for obtaining a list of items by removing all excluded items from the
	 * provided collection.
	 * @param items The collection of items from which you want all non-excluded items only.
	 * @return A list of items, based on the provided collection of items, with excluded items removed.
	 */
	public static List<Item> removeExcluded(Collection<Item> items){
		return items.stream().filter(i -> !i.isExcluded()).collect(Collectors.toList());
	}
	
	/***
	 * This is a convenience method for obtaining a list of items by removing all immaterial items
	 * from the provided collection.
	 * @param items The collection of items from which you want all audited items only.
	 * @return A list of items, based on the provided collection of items, with excluded items removed.
	 */
	public static List<Item> removeImmaterial(Collection<Item> items){
		return items.stream().filter(i -> i.isAudited()).collect(Collectors.toList());
	}
	
	/***
	 * Resolves an item to its physical file ancestor if it has one or null if one could not be found.
	 * @param item The item you wish to resolve the physical file ancestor for
	 * @return The physical file ancestor item if there is one, or null if one could not be found
	 */
	public static Item findPhysicalFileAncestor(Item item){
		// Search path in reverse for first physical file ancestor
		List<Item> pathItems = item.getPath();
		Item ancestor = null;
		for (int i = pathItems.size() - 1; i >= 0; i--) {
		    Item currentPathItem = pathItems.get(i);
		    // If this is it, record and break from loop
		    if(currentPathItem.isPhysicalFile()){
		    	ancestor = currentPathItem;
		    	break;
		    }
		}
		return ancestor;
	}
	
	/***
	 * Resolves provided items to their physical file ancestor items.  Items which do not have a
	 * physical file ancestor will yield nothing in the result.
	 * @param items Items to resolve to physical file ancestors
	 * @return Any physical file ancestors located for the provided items
	 */
	public static Set<Item> findPhysicalFileAncestors(Collection<Item> items){
		Set<Item> result = new HashSet<Item>();
		for(Item item : items){
			// Check if we already captured the "family" for this item
			if(result.contains(item)){
				continue;
			} else {
				Item ancestor = findPhysicalFileAncestor(item);
				//Record ancestor item if we found one
				if(ancestor != null){
					result.add(ancestor);
				}
			}
		}
		return result;
	}
	
	/***
	 * Similar to ItemUtility.findFamilies, but rather than finding the descendants of top level items
	 * this examines the ancestors of provided items looking for an ancestor which is a physical file
	 * (Item.isPhysicalFile returns true).  If a given item is not found to have a physical file ancestor
	 * then it will yield no "family" and not be included in the result.
	 * @param items The items to resolve to physical file "families"
	 * @return Physical file items and their descendants based on provided items.  It is possible a provided
	 * item is not present in the result if it does not have a physical file ancestor.
	 */
	public static Set<Item> findPhysicalFileFamilies(Collection<Item> items){
		Set<Item> result = new HashSet<Item>();
		for(Item item : items){
			// Check if we already captured the "family" for this item
			if(result.contains(item)){
				continue;
			} else {
				Set<Item> ancestors = findPhysicalFileAncestors(new ArrayList<Item>(items));
				Set<Item> descendants = NuixConnection.getUtilities().getItemUtility().findDescendants(ancestors);
				result.addAll(descendants);
			}
		}
		return result;
	}
	
	/***
	 * Resolves an item to its kind:container ancestor if it has one or null if one could not be found.
	 * @param item The item you wish to resolve the kind container ancestor for
	 * @return The kind container item if there is one, or null if one could not be found
	 */
	public static Item findContainerAncestor(Item item){
		// Search path in reverse for first kind container ancestor
			List<Item> pathItems = item.getPath();
			Item ancestor = null;
			for (int i = pathItems.size() - 1; i >= 0; i--) {
			    Item currentPathItem = pathItems.get(i);
			    // If this is it, record and break from loop
			    if(currentPathItem.isKind("container")){
			    	ancestor = currentPathItem;
			    	break;
			    }
			}
			return ancestor;
	}
	
	/***
	 * Resolves provided items to kind container ancestor items.  Items which do not have a
	 * physical file ancestor will yield nothing in the result.
	 * @param items Items to resolve to kind container ancestors
	 * @return Any kind container ancestors located for the provided items
	 */
	public static Set<Item> findContainerAncestors(Collection<Item> items){
		Set<Item> result = new HashSet<Item>();
		for(Item item : items){
			// Check if we already captured the "family" for this item
			if(result.contains(item)){
				continue;
			} else {
				Item ancestor = findContainerAncestor(item);
				//Record ancestor item if we found one
				if(ancestor != null){
					result.add(ancestor);
				}
			}
		}
		return result;
	}
	
	/***
	 * Similar to ItemUtility.findFamilies, but rather than finding the descendants of top level items
	 * this examines the ancestors of provided items looking for an ancestor which is kind container
	 * (Item.isKind("container") returns true).  If a given item is not found to have a kind container ancestor
	 * then it will yield no "family" and not be included in the result.
	 * @param items The items to resolve to container "families"
	 * @return Kind container items and their descendants based on provided items.  It is possible a provided
	 * item is not present in the result if it does not have a kind container ancestor.
	 */
	public static Set<Item> findContainerFamilies(Collection<Item> items){
		Set<Item> result = new HashSet<Item>();
		for(Item item : items){
			// Check if we already captured the "family" for this item
			if(result.contains(item)){
				continue;
			} else {
				Set<Item> ancestors = findContainerAncestors(new ArrayList<Item>(items));
				Set<Item> descendants = NuixConnection.getUtilities().getItemUtility().findDescendants(ancestors);
				result.addAll(descendants);
			}
		}
		return result;
	}
	
	/***
	 * Convenience method to calculate to the total audited size (in bytes) of a provided collection of items.
	 * @param items The items which you wish to sum the total audited size of.
	 * @return The total audited size of the items in bytes.
	 */
	public static long calculateTotalAuditedSize(Collection<Item> items){
		return new ArrayList<Item>(items).stream().mapToLong(item -> item.getDigests().getInputSize()).sum();
	}
}
