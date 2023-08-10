/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/***
 * A control which encapsulates some of the common PDF export settings
 * @author Jason Wells
 */
@SuppressWarnings("serial")
public class BatchExporterPdfSettings extends JPanel {
	private JTextField txtPath;
	private JTextField txtSuffix;
	private ComboItemBox comboNaming;
	private JLabel lblRegenerateStored;
	private JCheckBox chckbxRegenerateStored;
	
	public BatchExporterPdfSettings() {
		setBorder(new TitledBorder(null, "Text Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 200, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNaming = new JLabel("Naming");
		GridBagConstraints gbc_lblNaming = new GridBagConstraints();
		gbc_lblNaming.insets = new Insets(0, 0, 5, 5);
		gbc_lblNaming.anchor = GridBagConstraints.EAST;
		gbc_lblNaming.gridx = 0;
		gbc_lblNaming.gridy = 0;
		add(lblNaming, gbc_lblNaming);
		
		comboNaming = new ComboItemBox();
		GridBagConstraints gbc_comboNaming = new GridBagConstraints();
		gbc_comboNaming.insets = new Insets(0, 0, 5, 5);
		gbc_comboNaming.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboNaming.gridx = 1;
		gbc_comboNaming.gridy = 0;
		add(comboNaming, gbc_comboNaming);
		
		JLabel lblSubDirectoryName = new JLabel("Sub Directory Name");
		GridBagConstraints gbc_lblSubDirectoryName = new GridBagConstraints();
		gbc_lblSubDirectoryName.anchor = GridBagConstraints.EAST;
		gbc_lblSubDirectoryName.insets = new Insets(0, 0, 5, 5);
		gbc_lblSubDirectoryName.gridx = 0;
		gbc_lblSubDirectoryName.gridy = 1;
		add(lblSubDirectoryName, gbc_lblSubDirectoryName);
		
		txtPath = new JTextField();
		txtPath.setText("TEXT");
		GridBagConstraints gbc_txtPath = new GridBagConstraints();
		gbc_txtPath.insets = new Insets(0, 0, 5, 5);
		gbc_txtPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPath.gridx = 1;
		gbc_txtPath.gridy = 1;
		add(txtPath, gbc_txtPath);
		txtPath.setColumns(10);
		
		JLabel lblSuffix = new JLabel("Suffix");
		GridBagConstraints gbc_lblSuffix = new GridBagConstraints();
		gbc_lblSuffix.anchor = GridBagConstraints.EAST;
		gbc_lblSuffix.insets = new Insets(0, 0, 5, 5);
		gbc_lblSuffix.gridx = 0;
		gbc_lblSuffix.gridy = 2;
		add(lblSuffix, gbc_lblSuffix);
		
		txtSuffix = new JTextField();
		GridBagConstraints gbc_txtSuffix = new GridBagConstraints();
		gbc_txtSuffix.insets = new Insets(0, 0, 5, 5);
		gbc_txtSuffix.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSuffix.gridx = 1;
		gbc_txtSuffix.gridy = 2;
		add(txtSuffix, gbc_txtSuffix);
		txtSuffix.setColumns(10);
		
		{
			comboNaming.addValue("Document ID","document_id");
			comboNaming.addValue("Document ID with Page","document_id_with_page");
			comboNaming.addValue("Page Only","page_only");
			comboNaming.addValue("Full","full");
			comboNaming.addValue("Full with Periods","full_with_periods");
			comboNaming.addValue("Item Name","item_name");
			comboNaming.addValue("Item Name with Path","item_name_with_path");
			comboNaming.addValue("GUID","guid");
			comboNaming.addValue("MD5","md5");
			comboNaming.setSelectedIndex(0);
		}
		
		lblRegenerateStored = new JLabel("Regenerate Stored");
		GridBagConstraints gbc_lblRegenerateStored = new GridBagConstraints();
		gbc_lblRegenerateStored.insets = new Insets(0, 0, 5, 5);
		gbc_lblRegenerateStored.gridx = 0;
		gbc_lblRegenerateStored.gridy = 4;
		add(lblRegenerateStored, gbc_lblRegenerateStored);
		
		chckbxRegenerateStored = new JCheckBox("");
		GridBagConstraints gbc_chckbxRegnerateStored = new GridBagConstraints();
		gbc_chckbxRegnerateStored.anchor = GridBagConstraints.WEST;
		gbc_chckbxRegnerateStored.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRegnerateStored.gridx = 1;
		gbc_chckbxRegnerateStored.gridy = 4;
		add(chckbxRegenerateStored, gbc_chckbxRegnerateStored);
	}
	
	@Override
	public void setEnabled(boolean value){
		for(Component c : getComponents()){
			c.setEnabled(value);
		}
	}
	
	public ComboItemBox getComboNaming() {
		return comboNaming;
	}
	
	public JTextField getTxtPath() {
		return txtPath;
	}
	
	public JTextField getTxtSuffix() {
		return txtSuffix;
	}
	
	public JCheckBox getChckbxRegenerateStored() {
		return chckbxRegenerateStored;
	}
}
