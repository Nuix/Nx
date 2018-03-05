/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.helpers;

import java.text.NumberFormat;
import java.util.Locale;

/***
 * Provides some methods for common formatting tasks.
 * @author JWells01
 *
 */
public class FormatHelpers {
	public static String secondsToElapsedString(double offsetSeconds){
		Long millis = (long)(offsetSeconds*1000.0f);
		int seconds = (int) (millis / 1000) % 60 ;
		int minutes = (int) ((millis / (1000*60)) % 60);
		int hours   = (int) ((millis / (1000*60*60)) % 24);
		return String.format("%02d:%02d:%02d",hours,minutes,seconds);
	}
	
	public static String formatNumber(int number){
		return NumberFormat.getNumberInstance(Locale.US).format(number);
	}
	
	public static String formatNumber(long number){
		return NumberFormat.getNumberInstance(Locale.US).format(number);
	}
	
	public static String formatNumber(double number){
		return NumberFormat.getNumberInstance(Locale.US).format(number);
	}
}
