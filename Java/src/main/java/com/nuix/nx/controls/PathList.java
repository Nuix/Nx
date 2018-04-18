/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.lang.ArrayUtils;

import com.nuix.nx.controls.models.ArrangeableListModel;
import com.nuix.nx.dialogs.CommonDialogs;

/***
 * A control which allows the user to supply a list of file and directory paths
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class PathList extends JPanel {
	private JButton btnAddFiles;
	private JButton btnAddDirectories;
	private JButton btnRemoveSelected;
	private JList<String> pathList;
	private ArrangeableListModel<String> listModel = new ArrangeableListModel<String>();
	private JButton btnImportTextFile;
	private JButton btnMoveUp;
	private JButton btnMoveDown;

	public PathList() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.EAST;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.VERTICAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		
		btnAddFiles = new JButton("Add Files");
		btnAddFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File[] selectedFiles = CommonDialogs.selectFilesDialog("C:\\","Add File Path");
				if(selectedFiles != null){
					for(File file : selectedFiles){
						listModel.addElement(file.getPath());
					}
				}
			}
		});
		btnAddFiles.setIcon(new ImageIcon(PathList.class.getResource("/com/nuix/nx/controls/add.png")));
		panel.add(btnAddFiles);
		
		btnAddDirectories = new JButton("Add Directories");
		btnAddDirectories.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File[] selectedDirectories = CommonDialogs.selectDirectories("C:\\", "Add Directory Path");
				if(selectedDirectories != null){
					for(File directory : selectedDirectories){
						listModel.addElement(directory.getPath());
					}
				}
			}
		});
		btnAddDirectories.setIcon(new ImageIcon(PathList.class.getResource("/com/nuix/nx/controls/add.png")));
		panel.add(btnAddDirectories);
		
		btnRemoveSelected = new JButton("Remove Selected");
		btnRemoveSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedIndices = pathList.getSelectedIndices();
				Arrays.sort(selectedIndices);
				ArrayUtils.reverse(selectedIndices);
				for(int index : selectedIndices){
					listModel.remove(index);
				}
			}
		});
		
		btnImportTextFile = new JButton("Import");
		btnImportTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File textFile = CommonDialogs.openFileDialog("C:\\", "Text File", "txt", "Choose Text File to Import");
				if(textFile != null){
					try(BufferedReader br = new BufferedReader(new FileReader(textFile))) {
					    for(String line; (line = br.readLine()) != null; ) {
					    	addPath(line.trim());
					    }
					} catch(Exception exc){
						CommonDialogs.showError("Error while importing text file: \n"+exc.getMessage());
					}
				}
			}
		});
		btnImportTextFile.setIcon(new ImageIcon(PathList.class.getResource("/com/nuix/nx/dialogs/page_white_get.png")));
		panel.add(btnImportTextFile);
		btnRemoveSelected.setIcon(new ImageIcon(PathList.class.getResource("/com/nuix/nx/controls/delete.png")));
		panel.add(btnRemoveSelected);
		
		btnMoveUp = new JButton("Move Up");
		btnMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int[] selectedIndices = pathList.getSelectedIndices();
				if(selectedIndices != null && selectedIndices.length > 0){
					if(listModel.shiftRowsUp(selectedIndices[0], selectedIndices[selectedIndices.length-1])){
						pathList.clearSelection();
						for (int i = 0; i < selectedIndices.length; i++) {
							selectedIndices[i] = selectedIndices[i] - 1; 
						}
						pathList.setSelectedIndices(selectedIndices);
					}
				}
			}
		});
		btnMoveUp.setIcon(new ImageIcon(PathList.class.getResource("/com/nuix/nx/controls/arrow_up.png")));
		panel.add(btnMoveUp);
		
		btnMoveDown = new JButton("Move Down");
		btnMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedIndices = pathList.getSelectedIndices();
				if(selectedIndices != null && selectedIndices.length > 0){
					if(listModel.shiftRowsDown(selectedIndices[0], selectedIndices[selectedIndices.length-1])){
						pathList.clearSelection();
						for (int i = 0; i < selectedIndices.length; i++) {
							selectedIndices[i] = selectedIndices[i] + 1; 
						}
						pathList.setSelectedIndices(selectedIndices);
					}
				}
			}
		});
		btnMoveDown.setIcon(new ImageIcon(PathList.class.getResource("/com/nuix/nx/controls/arrow_down.png")));
		panel.add(btnMoveDown);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);
		
		pathList = new JList<String>();
		pathList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		pathList.setModel(listModel);
		scrollPane.setViewportView(pathList);
	}
	
	public List<String> getPaths(){
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < listModel.size(); i++) {
			result.add(listModel.getElementAt(i));
		}
		return result;
	}
	
	public List<File> getPathFiles(){
		return getPaths().stream().map(p -> new File(p)).collect(Collectors.toList());
	}
	
	public void setPaths(List<String> paths){
		pathList.clearSelection();
		listModel.clear();
		for(String path : paths){
			listModel.addElement(path);
		}
	}
	
	public void addPath(String path){
		listModel.addElement(path);
	}
	
	public void setFilesButtonVisible(boolean value){
		btnAddFiles.setVisible(value);
	}
	
	public void setDirectoriesButtonVisible(boolean value){
		btnAddDirectories.setVisible(value);
	}
	public JButton getBtnImportTextFile() {
		return btnImportTextFile;
	}

	@Override
	public void setEnabled(boolean value) {
		btnAddDirectories.setEnabled(value);
		btnAddFiles.setEnabled(value);
		btnImportTextFile.setEnabled(value);
		btnRemoveSelected.setEnabled(value);
		super.setEnabled(value);
	}
	
}
