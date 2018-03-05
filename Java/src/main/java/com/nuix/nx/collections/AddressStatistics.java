/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.nuix.nx.callbacks.SimpleProgressCallback;

import nuix.Address;
import nuix.Communication;
import nuix.Item;

/***
 * Helper class which provides some convenience methods for working with Nuix Address collections.
 * @author Jason Wells
 */
public class AddressStatistics {
	// The regular expression used to strip domain from an email address
	private static Pattern domainRegex = Pattern.compile("^.*(@[^@]+)$");
	
	private Set<String> distinctFromAddresses;
	private Set<String> distinctToAddresses;
	private Set<String> distinctCcAddresses;
	private Set<String> distinctBccAddresses;
	private Set<String> distinctRecipientAddresses;
	
	/***
	 * Create a new instance
	 */
	private AddressStatistics(){
		distinctFromAddresses = new HashSet<String>();
		distinctToAddresses = new HashSet<String>();
		distinctCcAddresses = new HashSet<String>();
		distinctBccAddresses = new HashSet<String>();
		distinctRecipientAddresses = new HashSet<String>();
	}
	
	/***
	 * Get the domain name from a standard email address
	 * @param emailAddress The email address to parse
	 * @return The domain portion of the address is one was able to be parsed
	 */
	public static String getEmailDomain(String emailAddress){
		return domainRegex.matcher(emailAddress).replaceAll("\\1");
	}
	
	/***
	 * Normalizes a series of email addresses by trimming whitespace and converting to
	 * all lower case.  Useful for when you need to compare addresses which may have
	 * come from different email addresses and therefore may have slight differences
	 * which could make matching them up fail.   
	 * @param addresses The list of addresses to normalize
	 * @return A list of normalized addresses
	 */
	private static List<String> normalizeAddresses(List<Address> addresses){
		return addresses.stream().map(a -> a.getAddress().trim().toLowerCase()).collect(Collectors.toList());	
	}
	
	/***
	 * Constructs an instance representing the given item.
	 * @param item The item to build the address statistics for
	 * @return An instance representing the provided item
	 */
	public static AddressStatistics forItem(Item item){
		AddressStatistics result = new AddressStatistics();
		Communication comm = item.getCommunication();
		if(comm != null){
			result.distinctFromAddresses.addAll(normalizeAddresses(comm.getFrom()));
			
			result.distinctToAddresses.addAll(normalizeAddresses(comm.getTo()));
			result.distinctRecipientAddresses.addAll(normalizeAddresses(comm.getTo()));
			
			result.distinctCcAddresses.addAll(normalizeAddresses(comm.getCc()));
			result.distinctRecipientAddresses.addAll(normalizeAddresses(comm.getCc()));
			
			result.distinctBccAddresses.addAll(normalizeAddresses(comm.getBcc()));
			result.distinctRecipientAddresses.addAll(normalizeAddresses(comm.getBcc()));
		}
		return result;
	}
	
	/***
	 * Constructs an instance representing the given items.
	 * @param items The items to build the address statistics for
	 * @return An instance representing the provided items
	 */
	public static AddressStatistics forItems(Collection<Item> items){
		return forItems(items,null);
	}
	
	/***
	 * Constructs an instance representing the given items and allows you to provide a progress callback.
	 * @param items The items to build the address statistics for
	 * @param progressCallback The progress callback
	 * @return An instance representing the provided items
	 */
	public static AddressStatistics forItems(Collection<Item> items, SimpleProgressCallback progressCallback){
		AddressStatistics result = new AddressStatistics();
		long current = 0;
		long total = items.size();
		for(Item item : items){
			Communication comm = item.getCommunication();
			if(comm != null){
				result.distinctFromAddresses.addAll(normalizeAddresses(comm.getFrom()));
				result.distinctToAddresses.addAll(normalizeAddresses(comm.getTo()));
				result.distinctCcAddresses.addAll(normalizeAddresses(comm.getCc()));
				result.distinctBccAddresses.addAll(normalizeAddresses(comm.getBcc()));
			}
			
			if(progressCallback != null){
				current++;
				progressCallback.progressUpdated(current, total);
			}
		}
		result.distinctRecipientAddresses.addAll(result.distinctToAddresses);
		result.distinctRecipientAddresses.addAll(result.distinctCcAddresses);
		result.distinctRecipientAddresses.addAll(result.distinctBccAddresses);
		return result;
	}
	
	/***
	 * Returns a Set of distinct from addresses
	 * @return A set of distinct from address strings
	 */
	public Set<String> getDistinctFromAddresses() {
		return distinctFromAddresses;
	}
	
	/***
	 * Returns a Set of distinct To addresses
	 * @return A set of distinct To address strings
	 */
	public Set<String> getDistinctToAddresses() {
		return distinctToAddresses;
	}
	
	/***
	 * Returns a Set of distinct CC addresses
	 * @return A set of distinct CC address strings
	 */
	public Set<String> getDistinctCcAddresses() {
		return distinctCcAddresses;
	}
	
	/***
	 * Returns a Set of distinct BCC addresses
	 * @return A set of distinct BCC address strings
	 */
	public Set<String> getDistinctBccAddresses() {
		return distinctBccAddresses;
	}
	
	/***
	 * Returns a Set of distinct recipient addresses (To, CC, BCC)
	 * @return A set of distinct recipient address strings (To, CC, BCC)
	 */
	public Set<String> getDistinctRecipientAddresses() {
		return distinctRecipientAddresses;
	}
	
	/***
	 * Returns a Set of distinct email domains found in the From field
	 * @return A distinct Set of email domains found in the From field
	 */
	public Set<String> getDistinctFromDomains() {
		Set<String> result = new HashSet<String>();
		for(String address : getDistinctFromAddresses()){
			String domain = getEmailDomain(address);
			if(!domain.trim().isEmpty()){
				result.add(domain);
			}
		}
		return result;
	}
	
	/***
	 * Returns a Set of distinct email domains found in the To field
	 * @return A distinct Set of email domains found in the To field
	 */
	public Set<String> getDistinctToDomains() {
		Set<String> result = new HashSet<String>();
		for(String address : getDistinctToAddresses()){
			String domain = getEmailDomain(address);
			if(!domain.trim().isEmpty()){
				result.add(domain);
			}
		}
		return result;
	}
	
	/***
	 * Returns a Set of distinct email domains found in the CC field
	 * @return A distinct Set of email domains found in the CC field
	 */
	public Set<String> getDistinctCcDomains() {
		Set<String> result = new HashSet<String>();
		for(String address : getDistinctCcAddresses()){
			String domain = getEmailDomain(address);
			if(!domain.trim().isEmpty()){
				result.add(domain);
			}
		}
		return result;
	}
	
	/***
	 * Returns a Set of distinct email domains found in the BCC field
	 * @return A distinct Set of email domains found in the BCC field
	 */
	public Set<String> getDistinctBccDomains() {
		Set<String> result = new HashSet<String>();
		for(String address : getDistinctBccAddresses()){
			String domain = getEmailDomain(address);
			if(!domain.trim().isEmpty()){
				result.add(domain);
			}
		}
		return result;
	}
	
	/***
	 * Returns a Set of distinct email domains found in the recipient fields (To, CC, BCC)
	 * @return A distinct Set of email domains found in the recipient fields (To, CC, BCC)
	 */
	public Set<String> getDistinctRecipientDomains() {
		Set<String> result = new HashSet<String>();
		for(String address : getDistinctRecipientAddresses()){
			String domain = getEmailDomain(address);
			if(!domain.trim().isEmpty()){
				result.add(domain);
			}
		}
		return result;
	}
	
	/***
	 * Tests whether any of the provided addresses appears in the list of From fields tracked
	 * by this instance
	 * @param addresses The list of addresses to compare
	 * @return True if at least one address in the provided addresses is present in the tracked From fields
	 */
	public boolean fromAnyAddressIn(Collection<String> addresses){
		for(String address : addresses){
			if(distinctFromAddresses.contains(address))
				return true;
		}
		return false;
	}
	
	/***
	 * Tests whether any of the provided addresses are NOT in the list of From fields tracked
	 * by this instance
	 * @param addresses The list of addresses to compare
	 * @return True if at least one address in the provided addresses is NOT present in the tracked From fields
	 */
	public boolean fromAnyAddressOutside(Collection<String> addresses){
		Set<String> addressSet = new HashSet<String>(addresses);
		for(String address : distinctFromAddresses){
			if(!addressSet.contains(address))
				return true;
		}
		return false;
	}
	
	/***
	 * Tests whether any of the provided addresses was found to be a recipient (TO, CC, BCC)
	 * tracked by this instance
	 * @param addresses The list of addresses to compare
	 * @return True if at least one of the provided addresses is found to be a recipient (TO, CC, BCC)
	 */
	public boolean receivedByAnyAddressIn(Collection<String> addresses){
		for(String address : addresses){
			if(distinctRecipientAddresses.contains(address))
				return true;
		}
		return false;
	}
	
	/***
	 * Tests whether any of the provided addresses was NOT found to be a recipient (TO, CC, BCC)
	 * tracked by this instance
	 * @param addresses The list of addresses to compare
	 * @return True if at least one of the provided addresses is found to NOT be a recipient (TO, CC, BCC)
	 */
	public boolean receivedByAnyAddressOutside(Collection<String> addresses){
		Set<String> addressSet = new HashSet<String>(addresses);
		for(String address : distinctRecipientAddresses){
			if(!addressSet.contains(address))
				return true;
		}
		return false;
	}
}
