/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JTextField;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JButton;

import com.nuix.nx.dialogs.CommonDialogs;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

/***
 * A control which provides a user a way to select paths
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class PathSelectionControl extends JPanel {
	public enum ChooserType {
		DIRECTORY,
		OPEN_FILE,
		SAVE_FILE
	}
	private JTextField txtFilePath;
	private String dialogTitle = "Choose";
	private JButton btnChoose;
	private PathSelectedCallback pathSelectedCallback;
	private String initialDirectory = null;
	
	public PathSelectionControl(ChooserType type, String fileTypeName, String fileExtension, String openFileDialogTitle) {
		if(openFileDialogTitle != null)
			dialogTitle = openFileDialogTitle;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		txtFilePath = new JTextField();
		GridBagConstraints gbc_txtFilePath = new GridBagConstraints();
		gbc_txtFilePath.insets = new Insets(0, 0, 0, 5);
		gbc_txtFilePath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFilePath.gridx = 0;
		gbc_txtFilePath.gridy = 0;
		add(txtFilePath, gbc_txtFilePath);
		txtFilePath.setColumns(10);
		
		btnChoose = new JButton("Choose");
		btnChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File selectedFile = null;
				String existingValue = txtFilePath.getText();
				switch(type){
				case DIRECTORY:
					if(initialDirectory != null){
						selectedFile = CommonDialogs.getDirectory(initialDirectory, dialogTitle);
					} else {
						selectedFile = CommonDialogs.getDirectory(existingValue, dialogTitle);	
					}
					break;
				case OPEN_FILE:
					if(initialDirectory != null){
						selectedFile = CommonDialogs.openFileDialog(initialDirectory, fileTypeName, fileExtension, dialogTitle);
					} else {
						selectedFile = CommonDialogs.openFileDialog(existingValue, fileTypeName, fileExtension, dialogTitle);	
					}
					break;
				case SAVE_FILE:
					if(initialDirectory != null){
						selectedFile = CommonDialogs.saveFileDialog(initialDirectory, fileTypeName, fileExtension, dialogTitle);
					} else {
						selectedFile = CommonDialogs.saveFileDialog(existingValue, fileTypeName, fileExtension, dialogTitle);	
					}
					break;
				default:
					break;
				}
				
				if(selectedFile != null)
					txtFilePath.setText(selectedFile.toString());
				firePathSelected();
			}
		});
		GridBagConstraints gbc_btnChoose = new GridBagConstraints();
		gbc_btnChoose.gridx = 1;
		gbc_btnChoose.gridy = 0;
		add(btnChoose, gbc_btnChoose);

	}
	
	protected void firePathSelected(){
		if(pathSelectedCallback != null){
			pathSelectedCallback.pathSelected(getPath());
		}
	}
	
	public void whenPathSelected(PathSelectedCallback callback){
		pathSelectedCallback = callback;
	}

	public void setPath(String path){
		txtFilePath.setText(path);
	}
	
	public String getPath(){
		return txtFilePath.getText();
	}
	
	public File getPathFile(){
		return new File(getPath());
	}
	
	public void setEnabled(boolean value){
		txtFilePath.setEnabled(value);
		btnChoose.setEnabled(value);
	}
	
	public void setPathFieldEditable(boolean value){
		txtFilePath.setEditable(value);
	}

	public String getInitialDirectory() {
		return initialDirectory;
	}

	public void setInitialDirectory(String initialDirectory) {
		this.initialDirectory = initialDirectory;
	}
}
