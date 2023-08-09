/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.helpers;

import java.text.NumberFormat;
import java.util.Locale;

/***
 * Provides some methods for common formatting tasks.
 * @author Jason Wells
 *
 */
public class FormatHelpers {
	/***
	 * Converts a given number of seconds into and elapsed string which breaks down
	 * time into seconds, minutes and hours
	 * @param offsetSeconds Number of seconds elapsed
	 * @return Seconds formatting into an elapsed string
	 */
	public static String secondsToElapsedString(double offsetSeconds){
		Long millis = (long)(offsetSeconds*1000.0f);
		int seconds = (int) (millis / 1000) % 60 ;
		int minutes = (int) ((millis / (1000*60)) % 60);
		int hours   = (int) ((millis / (1000*60*60)) % 24);
		return String.format("%02d:%02d:%02d",hours,minutes,seconds);
	}
	
	/***
	 * Convenience method for formatting an int into a string using US locale
	 * @param number The number to format
	 * @return The number formatted as a string
	 */
	public static String formatNumber(int number){
		return NumberFormat.getNumberInstance(Locale.US).format(number);
	}
	
	/***
	 * Convenience method for formatting a long int into a string using US locale
	 * @param number The number to format
	 * @return The number formatted as a string
	 */
	public static String formatNumber(long number){
		return NumberFormat.getNumberInstance(Locale.US).format(number);
	}
	
	/***
	 * Convenience method for formatting a double into a string using US locale
	 * @param number The number to format
	 * @return The number formatted as a string
	 */
	public static String formatNumber(double number){
		return NumberFormat.getNumberInstance(Locale.US).format(number);
	}
}
