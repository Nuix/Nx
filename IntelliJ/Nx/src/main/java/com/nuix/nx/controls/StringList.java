/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.lang.ArrayUtils;

import com.nuix.nx.controls.models.StringListTableModel;
import com.nuix.nx.dialogs.CommonDialogs;

/***
 * A control which allows a user to provide a list of string values (custodian names, tag names, etc)
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class StringList extends JPanel {
	private JButton btnAdd;
	private JButton btnRemoveSelected;
	private JTextField txtUservalue;
	private JButton btnImportFile;
	private JPanel buttonLayoutPanel;
	private JTable table;
	private StringListTableModel model = new StringListTableModel();

	public StringList() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		buttonLayoutPanel = new JPanel(){
			@Override
			public void setEnabled(boolean enabled) {
				for(Component c : getComponents()){
					c.setEnabled(enabled);
				}
				super.setEnabled(enabled);
			}
		};
		GridBagConstraints gbc_buttonLayoutPanel = new GridBagConstraints();
		gbc_buttonLayoutPanel.insets = new Insets(0, 0, 5, 0);
		gbc_buttonLayoutPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonLayoutPanel.gridx = 0;
		gbc_buttonLayoutPanel.gridy = 0;
		add(buttonLayoutPanel, gbc_buttonLayoutPanel);
		GridBagLayout gbl_buttonLayoutPanel = new GridBagLayout();
		gbl_buttonLayoutPanel.columnWidths = new int[]{75, 116, 0, 151, 0};
		gbl_buttonLayoutPanel.rowHeights = new int[]{25, 0};
		gbl_buttonLayoutPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_buttonLayoutPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		buttonLayoutPanel.setLayout(gbl_buttonLayoutPanel);
		
		btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String userValue = txtUservalue.getText();
				if(userValue != null && !userValue.trim().isEmpty()){
					addValue(userValue);
					txtUservalue.setText("");
				}
			}
		});
		btnAdd.setIcon(new ImageIcon(StringList.class.getResource("/icons/add.png")));
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnAdd.insets = new Insets(0, 0, 0, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		buttonLayoutPanel.add(btnAdd, gbc_btnAdd);
		
		txtUservalue = new JTextField();
		GridBagConstraints gbc_txtUservalue = new GridBagConstraints();
		gbc_txtUservalue.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUservalue.insets = new Insets(0, 0, 0, 5);
		gbc_txtUservalue.gridx = 1;
		gbc_txtUservalue.gridy = 0;
		buttonLayoutPanel.add(txtUservalue, gbc_txtUservalue);
		txtUservalue.setColumns(10);
		txtUservalue.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
					String userValue = txtUservalue.getText();
					if(userValue != null && !userValue.trim().isEmpty()){
						addValue(userValue);
						txtUservalue.setText("");
					}
					arg0.consume();
				}
			}
		});
		
		btnRemoveSelected = new JButton("Remove Selected");
		btnRemoveSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedIndices = table.getSelectedRows();
				Arrays.sort(selectedIndices);
				ArrayUtils.reverse(selectedIndices);
				for(int index : selectedIndices){
					model.removeValueAt(index);
				}
			}
		});
		
		btnImportFile = new JButton("Import File");
		btnImportFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File textFile = CommonDialogs.openFileDialog("C:\\", "Text File", "txt", "Choose Text File to Import");
				if(textFile != null){
					try(BufferedReader br = new BufferedReader(new FileReader(textFile))) {
					    for(String line; (line = br.readLine()) != null; ) {
					    	addValue(line.trim());
					    }
					} catch(Exception exc){
						CommonDialogs.showError("Error while importing text file: \n"+exc.getMessage());
					}
				}
			}
		});
		btnImportFile.setIcon(new ImageIcon(StringList.class.getResource("/icons/page_white_get.png")));
		GridBagConstraints gbc_btnImportFile = new GridBagConstraints();
		gbc_btnImportFile.insets = new Insets(0, 0, 0, 5);
		gbc_btnImportFile.gridx = 2;
		gbc_btnImportFile.gridy = 0;
		buttonLayoutPanel.add(btnImportFile, gbc_btnImportFile);
		btnRemoveSelected.setIcon(new ImageIcon(StringList.class.getResource("/icons/delete.png")));
		GridBagConstraints gbc_btnRemoveSelected = new GridBagConstraints();
		gbc_btnRemoveSelected.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnRemoveSelected.gridx = 3;
		gbc_btnRemoveSelected.gridy = 0;
		buttonLayoutPanel.add(btnRemoveSelected, gbc_btnRemoveSelected);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);
		
		table = new JTable(model);
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
	}
	
	/***
	 * Gets the current values of the list.
	 * @return The current values of the list.
	 */
	public List<String> getValues(){
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < model.getValueCount(); i++) {
			result.add(model.getValueAt(i));
		}
		return result;
	}
	
	/***
	 * Sets the values of the list.
	 * @param values The new values of the list.
	 */
	public void setValues(List<String> values){
		model.setValues(values);
	}
	
	/***
	 * Adds a value to the list.
	 * @param value The value to add.
	 */
	public void addValue(String value){
		model.addValue(value);
	}
	
	public JButton getBtnImportFile() {
		return btnImportFile;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		for(Component c : getComponents()){
			c.setEnabled(enabled);
		}
		super.setEnabled(enabled);
	}

	/***
	 * Sets whether the user is allowed to edit entries.
	 * @param editable True if the user should be allowed to edit entries, False if not.
	 */
	public void setEditable(boolean editable) {
		model.setEditable(editable);
	}
}
