/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.dialogs;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/***
 * Provides convenience methods for displaying common dialogs.
 * @author Jason Wells
 *
 */
public class CommonDialogs {
	/***
	 * Shows a plain message dialog.
	 * @param message The message
	 * @param title The dialog title
	 */
	public static void showMessage(String message, String title){
		JOptionPane.showMessageDialog(null,message,title,JOptionPane.PLAIN_MESSAGE);
	}
	
	/***
	 * Shows a plain message dialog.
	 * @param message The message
	 */
	public static void showMessage(String message){
		showMessage(message,"Message");
	}
	
	/***
	 * Shows an error message dialog.
	 * @param message The message
	 * @param title The dialog title
	 */
	public static void showError(String message, String title){
		JOptionPane.showMessageDialog(null,message,title,JOptionPane.ERROR_MESSAGE);
	}
	
	/***
	 * Shows an error message dialog.
	 * @param message The message
	 */
	public static void showError(String message){
		showMessage(message,"Error");
	}
	
	/***
	 * Shows an information message dialog.
	 * @param message The message
	 * @param title The title
	 */
	public static void showInformation(String message, String title){
		JOptionPane.showMessageDialog(null,message,title,JOptionPane.INFORMATION_MESSAGE);
	}
	
	/***
	 * Shows an information message dialog.
	 * @param message The message
	 */
	public static void showInformation(String message){
		showInformation(message,"Information");
	}
	
	/***
	 * Shows a warning message dialog.
	 * @param message The message
	 * @param title The title
	 */
	public static void showWarning(String message, String title){
		JOptionPane.showMessageDialog(null,message,title,JOptionPane.WARNING_MESSAGE);
	}
	
	/***
	 * Shows a warning message dialog.
	 * @param message The message
	 */
	public static void showWarning(String message){
		showWarning(message,"Warning");
	}
	
	/***
	 * Shows a confirmation dialog where the users selects "Yes" or "No".
	 * @param parentComponent The parent component that this dialog is modal to.
	 * @param message The message
	 * @param title The dialog title
	 * @return True if the user selects "Yes", False if the user selects "No" or closes the dialog.
	 */
	public static boolean getConfirmation(Component parentComponent, String message, String title){
		int result = JOptionPane.showConfirmDialog(parentComponent, message, title, JOptionPane.YES_NO_OPTION);
		return result == JOptionPane.YES_OPTION;
	}
	
	/***
	 * Shows a confirmation dialog where the users selects "Yes" or "No".
	 * @param message The message
	 * @param title The dialog title
	 * @return True if the user selects "Yes", False if the user selects "No" or closes the dialog.
	 */
	public static boolean getConfirmation(String message, String title){
		int result = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
		return result == JOptionPane.YES_OPTION;
	}
	
	/***
	 * Shows a confirmation dialog where the users selects "Yes" or "No".
	 * @param message The message
	 * @return True if the user selects "Yes", False if the user selects "No" or closes the dialog.
	 */
	public static boolean getConfirmation(String message){
		return getConfirmation(message,"Confirm?");
	}
	
	/***
	 * Shows a dialog allowing the user to input some text.
	 * @param message The message
	 * @param defaultValue The default value of the text field when the dialog is displayed.
	 * @return The text the user provided.
	 */
	public static String getInput(String message, String defaultValue){
		return JOptionPane.showInputDialog(message, defaultValue);
	}
	
	/***
	 * Shows a dialog allowing the user to input some text.
	 * @param message The message
	 * @return The text the user provided.
	 */
	public static String getInput(String message){
		return getInput(message,"");
	}
	
	/***
	 * Shows a dialog with a drop down, allowing the user to select one of several values.
	 * @param message The message
	 * @param choices The choices to present to the user.
	 * @param defaultValue The default choice.
	 * @param title The dialog title
	 * @return The choice the user selected.
	 */
	public static String getSelection(String message, List<String> choices, String defaultValue, String title){
		if(defaultValue == null){
			defaultValue = choices.get(0);
		}
		return (String) JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE, null, choices.toArray(), defaultValue);
	}
	
	/***
	 * Shows a dialog with a drop down, allowing the user to select one of several values.
	 * @param message The message
	 * @param choices The choices to present to the user.
	 * @param defaultValue The default choice.
	 * @return The choice the user selected.
	 */
	public static String getSelection(String message, List<String> choices, String defaultValue){
		return getSelection(message,choices,defaultValue,"Select a Choice");
	}
	
	/***
	 * Shows a dialog with a drop down, allowing the user to select one of several values.
	 * @param message The message
	 * @param choices The choices to present to the user.
	 * @return The choice the user selected.
	 */
	public static String getSelection(String message, List<String> choices){
		return getSelection(message,choices,null);
	}
	
	/***
	 * Shows a dialog allowing the user to select a directory.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\".
	 * @param title The dialog title
	 * @return A java.io.File object representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File getDirectory(File initialDirectory, String title){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		if(initialDirectory == null)
			initialDirectory = new File("C:\\");
		chooser.setCurrentDirectory(initialDirectory);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showOpenDialog(null);
		if(result == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		else
			return null;
	}
	
	/***
	 * Shows a dialog allowing the user to select a directory.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\".
	 * @param title The dialog title
	 * @return A java.io.File object representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File getDirectory(String initialDirectory, String title){
		return getDirectory(new File(initialDirectory), title);
	}
	
	/***
	 * Shows a dialog allowing the user to select a file to open.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\". 
	 * @param fileTypeName The name of the file type to show in the filter such as "Comma Separated Values (*.csv)"
	 * @param fileExtension The extension of the file type to show in the filter such as "csv".
	 * @param title The dialog title
	 * @return A java.io.File object representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File openFileDialog(File initialDirectory,String fileTypeName, String fileExtension, String title){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		if(initialDirectory == null)
			initialDirectory = new File("C:\\");
		chooser.setCurrentDirectory(initialDirectory);
		FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(fileTypeName,fileExtension);
		chooser.addChoosableFileFilter(extensionFilter);
		chooser.setFileFilter(extensionFilter);
		int result = chooser.showOpenDialog(null);
		if(result == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		else
			return null;
	}
	
	/***
	 * Shows a dialog allowing the user to select one or more files to open.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\". 
	 * @param fileTypeName The name of the file type to show in the filter such as "Comma Separated Values (*.csv)"
	 * @param fileExtension The extension of the file type to show in the filter such as "csv".  If null will default to "All Files".
	 * @param title The dialog title
	 * @return An array of java.io.File objects representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File[] openMultipleFilesDialog(File initialDirectory,String fileTypeName, String fileExtension, String title){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		if(initialDirectory == null)
			initialDirectory = new File("C:\\");
		chooser.setCurrentDirectory(initialDirectory);
		chooser.setMultiSelectionEnabled(true);
		
		if(fileExtension != null){
			FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(fileTypeName,fileExtension);
			chooser.addChoosableFileFilter(extensionFilter);
			chooser.setFileFilter(extensionFilter);
		}
		
		int result = chooser.showOpenDialog(null);
		if(result == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFiles();
		else
			return null;
	}
	
	/***
	 * Shows a dialog allowing the user to select one or more files to open.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\". 
	 * @param fileTypeName The name of the file type to show in the filter such as "Comma Separated Values (*.csv)"
	 * @param fileExtension The extension of the file type to show in the filter such as "csv".  If null will default to "All Files".
	 * @param title The dialog title
	 * @return An array of java.io.File objects representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File[] openMultipleFilesDialog(String initialDirectory,String fileTypeName, String fileExtension, String title){
		return openMultipleFilesDialog(new File(initialDirectory),fileTypeName,fileExtension,title);
	}
	
	/***
	 * Shows a dialog allowing the user to select a file to open.  Applies no extension filtering.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\". 
	 * @param title The dialog title
	 * @return A java.io.File object representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File openFileDialog(File initialDirectory, String title){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		if(initialDirectory == null)
			initialDirectory = new File("C:\\");
		chooser.setCurrentDirectory(initialDirectory);
		chooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File arg0) {
				return true;
			}

			@Override
			public String getDescription() {
				return "All Files";
			}
			
		});
		int result = chooser.showOpenDialog(null);
		if(result == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		else
			return null;
	}
	
	public static File[] selectFilesDialog(File initialDirectory, String title){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		if(initialDirectory == null)
			initialDirectory = new File("C:\\");
		chooser.setMultiSelectionEnabled(true);
		chooser.setCurrentDirectory(initialDirectory);
		chooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File arg0) {
				return true;
			}

			@Override
			public String getDescription() {
				return "All Files";
			}
			
		});
		int result = chooser.showOpenDialog(null);
		if(result == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFiles();
		else
			return null;
	}
	
	public static File[] selectFilesDialog(String initialDirectory, String title){
		return selectFilesDialog(new File(initialDirectory),title);
	}
	
	public static File[] selectDirectories(File initialDirectory, String title){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		if(initialDirectory == null)
			initialDirectory = new File("C:\\");
		chooser.setCurrentDirectory(initialDirectory);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showOpenDialog(null);
		if(result == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFiles();
		else
			return null;
	}
	
	public static File[] selectDirectories(String initialDirectory, String title){
		return selectDirectories(new File(initialDirectory),title);
	}
	
	/***
	 * Shows a dialog allowing the user to select a file to open.  Applies no extension filtering.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\". 
	 * @param title The dialog title
	 * @return A java.io.File object representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File openFileDialog(String initialDirectory, String title){
		return openFileDialog(new File(initialDirectory),title);
	}
	
	/***
	 * Shows a dialog allowing the user to select a file to open.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\". 
	 * @param fileTypeName The name of the file type to show in the filter such as "Comma Separated Values (*.csv)"
	 * @param fileExtension The extension of the file type to show in the filter such as "csv".
	 * @param title The dialog title
	 * @return A java.io.File object representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File openFileDialog(String initialDirectory,String fileTypeName, String fileExtension, String title){
		return openFileDialog(new File(initialDirectory),fileTypeName,fileExtension,title);
	}
	
	/***
	 * Shows a dialog allowing the user to select a file to save.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\". 
	 * @param fileTypeName The name of the file type to show in the filter such as "Comma Separated Values (*.csv)"
	 * @param fileExtension The extension of the file type to show in the filter such as "csv".
	 * @param title The dialog title
	 * @return A java.io.File object representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File saveFileDialog(File initialDirectory,String fileTypeName, String fileExtension, String title){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		if(initialDirectory == null)
			initialDirectory = new File("C:\\");
		chooser.setCurrentDirectory(initialDirectory);
		FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(fileTypeName,fileExtension);
		chooser.addChoosableFileFilter(extensionFilter);
		chooser.setFileFilter(extensionFilter);
		int result = chooser.showSaveDialog(null);
		if(result == JFileChooser.APPROVE_OPTION){
			File selectedFile = chooser.getSelectedFile();
			//Ensure there is an extension
			if(!selectedFile.getPath().toLowerCase().endsWith("."+fileExtension.toLowerCase())){
				selectedFile = new File(selectedFile.getPath()+"."+fileExtension);
			}
			return selectedFile;
		}
		else
			return null;
	}
	
	/***
	 * Shows a dialog allowing the user to select a file to save.
	 * @param initialDirectory The initial directory to start in.  If null, will default to "C:\". 
	 * @param fileTypeName The name of the file type to show in the filter such as "Comma Separated Values (*.csv)"
	 * @param fileExtension The extension of the file type to show in the filter such as "csv".
	 * @param title The dialog title
	 * @return A java.io.File object representing the users selection, or null if the user selected "Cancel" or closed the dialog.
	 */
	public static File saveFileDialog(String initialDirectory,String fileTypeName, String fileExtension, String title){
		return saveFileDialog(new File(initialDirectory),fileTypeName,fileExtension,title);
	}
}
