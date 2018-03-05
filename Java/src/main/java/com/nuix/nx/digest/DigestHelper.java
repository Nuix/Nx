/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.digest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import com.google.common.primitives.UnsignedBytes;

import nuix.Item;

/***
 * This class assists in the manipulation of Nuix digest lists.
 * @author JasonWells
 * <pre>
 * {@code
 * # Ruby example
 * java_import com.nuix.nx.digest
 * 
 * digest_name = Time.now.strftime("%Y%m%d_%H-%M-%S")
 * puts "Digest Name: #{digest_name}"
 * 
 * items = $current_case.search(query)
 * puts "Input Items Count: #{items.size}"
 * 
 * helper = DigestHelper.new
 * helper.addAllItems(items)
 * 
 * save_location = DigestHelper::getDigestListLocation(digest_name)
 * puts "Save Location: #{save_location}"
 * saved = helper.saveDigestList(save_location)
 * puts "Digests Saved: #{saved}"
 * 
 * digest_hits = $current_case.search("digest-list:\"#{digest_name}\"")
 * puts "Digest Hits: #{digest_hits.size}"
 * puts "Count Matches: #{digest_hits.size == items.size}"
 * 
 * helper2 = DigestHelper.new
 * loaded = helper2.loadDigestList(save_location)
 * puts "Loaded from File Count: #{loaded}"
 * puts "Matches Saved: #{saved == loaded}"
 * }
 * </pre>
 */
public class DigestHelper {
	/***
	 * Returns the assumed directory containing digest lists.
	 * @return File representing the digest lists directory.
	 */
	public static File getDigestListDirectory(){
		String appData = java.lang.System.getenv("APPDATA");
		return new File(appData+"\\Nuix\\Digest Lists");
	}
	
	/***
	 * Returns the assumed location of a particular named digest list.
	 * @param name The named of the digest list.
	 * @return File represent the digest list location.
	 */
	public static File getDigestListLocation(String name){
		return new File(getDigestListDirectory()+"\\"+name+".hash");
	}
	
	/***
	 * Convenience method for converting hex string to byte array.
	 * @param hex String of hexadecimal.
	 * @return Byte array equivalent.
	 */
	public static byte[] hexToBytes(String hex){
		return DatatypeConverter.parseHexBinary(hex);
	}
	
	/***
	 * Convenience method for converting a byte array to a hex string.
	 * @param bytes The bytes to convert.
	 * @return A string representation of the byte array as hexadecimal.
	 */
	public static String bytesToHex(byte[] bytes){
		return DatatypeConverter.printHexBinary(bytes);
	}
	
	/***
	 * Creates an instance of DigestHelper containing the digests from all specified digest lists.
	 * @param digestFiles Collection of digest files to load into the resulting instance.
	 * @return A DigestHelper instance containing all the digests from the provided existing digest lists.
	 * @throws IOException Thrown if there is an issue with the file stream.
	 */
	public static DigestHelper createFromExistingDigestLists(Collection<File> digestFiles) throws IOException{
		DigestHelper result = new DigestHelper();
		for(File digestFile : digestFiles){
			result.loadDigestList(digestFile);
		}
		return result;
	}
	
	/***
	 * Creates an instance of DigestHelper containing the digests from all specified digest lists.
	 * Assumes digest lists are stored in "%appdata%\Nuix\Digest Lists"
	 * @param existingDigestNames A collection of digest list names.
	 * @return A DigestHelper instance containing all the digests from the provided existing digest lists.
	 * @throws IOException Thrown if there is an issue with the file stream.
	 */
	public static DigestHelper createFromExistingDigestListsByName(Collection<String> existingDigestNames) throws IOException{
		return createFromExistingDigestLists(existingDigestNames.stream().map(name -> getDigestListLocation(name)).collect(Collectors.toList()));
	}
	
	private Set<byte[]> digestSet;
	
	public DigestHelper(){
		 digestSet = new HashSet<byte[]>();
	}
	
	/***
	 * Add a collection of MD5 values where each MD5 is represented as its equivalent byte array.
	 * @param md5DigestByteArrays A collection of MD5 byte arrays.
	 */
	public void addAllMd5ByteArrays(Collection<byte[]> md5DigestByteArrays){
		digestSet.addAll(md5DigestByteArrays);
	}
	
	/***
	 * Add a collection of MD5 values where MD5 is represented as a hexadecimal string.
	 * @param md5DigestStrings A collection of MD5 hexadecimal strings.  Null or empty values are ignored.
	 */
	public void addAllMd5Strings(Collection<String> md5DigestStrings){
		for(String digest : md5DigestStrings){
			digestSet.add(hexToBytes(digest));
		}
		addAllMd5ByteArrays(md5DigestStrings.stream()
				.filter(v -> v != null && !v.isEmpty())
				.map(md5 -> hexToBytes(md5))
				.collect(Collectors.toSet()));
	}
	
	/***
	 * Add a collection of items, using each item's MD5 value.
	 * @param items Collection of items to add to this instance.  Items which return a null or empty MD5 string will be ignored.
	 */
	public void addAllItems(Collection<Item> items){
		addAllMd5Strings(items.stream()
				.map(i -> i.getDigests().getMd5())
				.filter(v -> v != null && !v.isEmpty())
				.collect(Collectors.toSet()));
	}
	
	/***
	 * Saves a Nuix digest list based on data which has been added so far using methods {@link #addAllItems(Collection)} or
	 * {@link #addAllMd5Strings(Collection)} or {@link #addAllMd5ByteArrays(Collection)}.
	 * @param location The location in which to save the Nuix formatted digest list file.
	 * @return The number of digests saved.  Should match the count returned by {@link #getDistinctDigestCount()} before this call was made.
	 * @throws IOException If there is an issue with the output stream.
	 */
	public int saveDigestList(File location) throws IOException{
		int countSaved = 0;
		FileOutputStream outputStream = null;
		try{
			outputStream = new FileOutputStream(location);
			outputStream.write("F2DL".getBytes());
			outputStream.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(1).array());
			outputStream.write(ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short) 3).array());
			outputStream.write("MD5".getBytes());
			for(byte[] digestBytes : digestSet.stream().sorted(UnsignedBytes.lexicographicalComparator()).collect(Collectors.toList())){
				outputStream.write(digestBytes);
				countSaved++;
			}
		}finally{
			if(outputStream != null)
				outputStream.close();
		}
		return countSaved;
	}
	
	/***
	 * Saves a digest list to the appropriate location with provided name.
	 * Assumes digest lists are stored in "%appdata%\Nuix\Digest Lists"
	 * @param name The name of the digest list to save.
	 * @return The number of digests saved.  Should match the count returned by {@link #getDistinctDigestCount()} before this call was made.
	 * @throws IOException If there is an issue with the output stream.
	 */
	public int saveDigestListByName(String name) throws IOException{
		return saveDigestList(getDigestListLocation(name));
	}
	
	/***
	 * Saves a Nuix digest list based on data which has been added so far using methods {@link #addAllItems(Collection)} or
	 * {@link #addAllMd5Strings(Collection)} or {@link #addAllMd5ByteArrays(Collection)}.
	 * @param location The location in which to save the Nuix formatted digest list file.
	 * @return The number of digests saved.  Should match the count returned by {@link #getDistinctDigestCount()} before this call was made.
	 * @throws IOException Caused by an issue with the output stream.
	 */
	public int saveDigestList(String location) throws IOException{
		return saveDigestList(new File(location));
	}
	
	/***
	 * Includes all entries from an existing Nuix digest list file. 
	 * @param location The location of the existing digest list file.
	 * @return The count of digests loaded from the file.
	 * @throws IOException Caused by an issue with the input stream.
	 */
	public int loadDigestList(File location) throws IOException{
		FileInputStream inputStream = null;
		int countRead = 0;
		try{
			inputStream = new FileInputStream(location);
			inputStream.skip(13);
			byte[] buffer = new byte[16];
			while(inputStream.available() != 0){
				inputStream.read(buffer);
				digestSet.add(buffer);
				countRead++;
			}
		}finally{
			if(inputStream != null)
				inputStream.close();
		}
		return countRead;
	}
	
	/***
	 * Includes all entries from an the appropriate existing Nuix digest list file based on the provided name.
	 * Assumes digest lists are stored in "%appdata%\Nuix\Digest Lists"
	 * @param name The name of the existing Nuix digest list to load.
	 * @return The count of digests loaded from the file.
	 * @throws IOException Caused by an issue with the input stream.
	 */
	public int loadDigestListByName(String name) throws IOException{
		return loadDigestList(getDigestListLocation(name));
	}
	
	/***
	 * Includes all entries from an existing Nuix digest list file. 
	 * @param location The location of the existing digest list file.
	 * @return The count of digests loaded from the file.
	 * @throws IOException Caused by an issue with the input stream.
	 */
	public int loadDigestList(String location) throws IOException{
		return loadDigestList(new File(location));
	}
	
	/***
	 * Returns the counts of distinct digests currently stored in this instance.
	 * @return Count of distinct digests stored.
	 */
	public int getDistinctDigestCount(){
		return digestSet.size();
	}
	
	/***
	 * Clears out all digests currently being tracked by this instance.
	 */
	public void clear(){
		digestSet.clear();
	}
	
	/***
	 * Whether this instance currently contains the specific digest.
	 * @param md5 MD5 as byte array.
	 * @return True if this instance contains this digest.
	 */
	public boolean currentlyContains(byte[] md5){
		return digestSet.contains(md5);
	}
	
	/***
	 * Whether this instance currently contains the specific digest.
	 * @param md5 MD5 as hexadecimal string.
	 * @return True if this instance contains this digest.
	 */
	public boolean currentlyContains(String md5){
		return digestSet.contains(hexToBytes(md5));
	}
	
	/***
	 * Whether this instance currently contains the specific digest for the provided item.
	 * @param item The item which will have it's MD5 checked for.
	 * @return True if this instance contains the digest for this item.
	 */
	public boolean currentlyContains(Item item){
		return currentlyContains(item.getDigests().getMd5());
	}
}
