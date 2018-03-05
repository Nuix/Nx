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
 * A control which encapsulates some of the common native export settings.
 * @author Jason Wells
 */
@SuppressWarnings("serial")
public class BatchExporterNativeSettings extends JPanel {
	private JTextField txtPath;
	private JTextField txtsuffix;
	private ComboItemBox comboNaming;
	private JLabel lblMailFormat;
	private ComboItemBox comboMailFormat;
	private JLabel lblIncludeAttachments;
	private JCheckBox chckbxIncludeAttachments;
	private JLabel lblRegenerateStored;
	private JCheckBox chckbxRegenerateStored;
	public BatchExporterNativeSettings() {
		setBorder(new TitledBorder(null, "Native Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 200, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 10, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
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
		txtPath.setText("NATIVE");
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
		
		txtsuffix = new JTextField();
		GridBagConstraints gbc_txtsuffix = new GridBagConstraints();
		gbc_txtsuffix.insets = new Insets(0, 0, 5, 5);
		gbc_txtsuffix.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtsuffix.gridx = 1;
		gbc_txtsuffix.gridy = 2;
		add(txtsuffix, gbc_txtsuffix);
		txtsuffix.setColumns(10);
		
		lblMailFormat = new JLabel("Mail Format");
		GridBagConstraints gbc_lblMailFormat = new GridBagConstraints();
		gbc_lblMailFormat.anchor = GridBagConstraints.EAST;
		gbc_lblMailFormat.insets = new Insets(0, 0, 5, 5);
		gbc_lblMailFormat.gridx = 0;
		gbc_lblMailFormat.gridy = 4;
		add(lblMailFormat, gbc_lblMailFormat);
		
		comboMailFormat = new ComboItemBox();
		GridBagConstraints gbc_comboMailFormat = new GridBagConstraints();
		gbc_comboMailFormat.insets = new Insets(0, 0, 5, 5);
		gbc_comboMailFormat.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboMailFormat.gridx = 1;
		gbc_comboMailFormat.gridy = 4;
		add(comboMailFormat, gbc_comboMailFormat);
		
		lblIncludeAttachments = new JLabel("Include Attachments");
		GridBagConstraints gbc_lblIncludeAttachments = new GridBagConstraints();
		gbc_lblIncludeAttachments.anchor = GridBagConstraints.EAST;
		gbc_lblIncludeAttachments.insets = new Insets(0, 0, 5, 5);
		gbc_lblIncludeAttachments.gridx = 0;
		gbc_lblIncludeAttachments.gridy = 5;
		add(lblIncludeAttachments, gbc_lblIncludeAttachments);
		
		chckbxIncludeAttachments = new JCheckBox("");
		chckbxIncludeAttachments.setSelected(true);
		GridBagConstraints gbc_chckbxIncludeAttachments = new GridBagConstraints();
		gbc_chckbxIncludeAttachments.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIncludeAttachments.anchor = GridBagConstraints.WEST;
		gbc_chckbxIncludeAttachments.gridx = 1;
		gbc_chckbxIncludeAttachments.gridy = 5;
		add(chckbxIncludeAttachments, gbc_chckbxIncludeAttachments);
		
		lblRegenerateStored = new JLabel("Regenerate Stored");
		GridBagConstraints gbc_lblRegenerateStored = new GridBagConstraints();
		gbc_lblRegenerateStored.anchor = GridBagConstraints.EAST;
		gbc_lblRegenerateStored.insets = new Insets(0, 0, 5, 5);
		gbc_lblRegenerateStored.gridx = 0;
		gbc_lblRegenerateStored.gridy = 6;
		add(lblRegenerateStored, gbc_lblRegenerateStored);
		
		chckbxRegenerateStored = new JCheckBox("");
		GridBagConstraints gbc_chckbxRegenerateStored = new GridBagConstraints();
		gbc_chckbxRegenerateStored.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRegenerateStored.anchor = GridBagConstraints.WEST;
		gbc_chckbxRegenerateStored.gridx = 1;
		gbc_chckbxRegenerateStored.gridy = 6;
		add(chckbxRegenerateStored, gbc_chckbxRegenerateStored);
		
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
		
		comboMailFormat.addValue("Native","native");
		comboMailFormat.addValue("EML","eml");
		comboMailFormat.addValue("HTML","html");
		comboMailFormat.addValue("Mime HTML","mime_html");
		comboMailFormat.addValue("MSG","msg");
		comboMailFormat.addValue("DXL","dxl");
		comboMailFormat.addValue("MBOX","mbox");
		comboMailFormat.addValue("PST","pst");
		comboMailFormat.addValue("NSF","nsf");
		comboMailFormat.setSelectedIndex(0);
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
		return txtsuffix;
	}
	
	public ComboItemBox getComboMailFormat() {
		return comboMailFormat;
	}
	
	public JCheckBox getChckbxIncludeAttachments() {
		return chckbxIncludeAttachments;
	}
	
	public JCheckBox getChckbxRegenerateStored() {
		return chckbxRegenerateStored;
	}
}