/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
/**
 * Helper utility to change the Java look and feel.
 * @author Jason Wells
 */
public class LookAndFeelHelper {
	/**
	 * Changes the current Java look and feel to "Windows" if it is currently "Metal".  You will usually want to call
	 * this early on to keep your script from having the default Java look.
	 */
	public static void setWindowsIfMetal(){
		if(UIManager.getLookAndFeel().getName().equals("Metal")){
			try {
			    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			        if ("Windows".equals(info.getName())) {
			            UIManager.setLookAndFeel(info.getClassName());
			            break;
			        }
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
