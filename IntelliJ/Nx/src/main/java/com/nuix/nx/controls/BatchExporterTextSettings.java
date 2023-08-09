/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

/***
 * A control which encapsulates some of the common text export settings
 * @author Jason Wells
 */
@SuppressWarnings("serial")
public class BatchExporterTextSettings extends JPanel {
	private JTextField txtPath;
	private JTextField txtSuffix;
	private ComboItemBox comboNaming;
	private JLabel lblWrapLines;
	private JCheckBox chckbxWrapLines;
	private JLabel lblWrapLength;
	private JSpinner spinnerWrapLength;
	private JLabel lblPerPage;
	private JCheckBox chckbxPerPage;
	private JLabel lblLineSeparator;
	private JPanel panel;
	private ComboItemBox comboLineSeparator;
	private JLabel lblEncoding;
	private ComboItemBox comboEncoding;
	public BatchExporterTextSettings() {
		setBorder(new TitledBorder(null, "Text Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 200, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
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
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		lblWrapLines = new JLabel("Wrap Lines");
		GridBagConstraints gbc_lblWrapLines = new GridBagConstraints();
		gbc_lblWrapLines.insets = new Insets(0, 0, 0, 5);
		gbc_lblWrapLines.gridx = 0;
		gbc_lblWrapLines.gridy = 0;
		panel.add(lblWrapLines, gbc_lblWrapLines);
		
		chckbxWrapLines = new JCheckBox("");
		GridBagConstraints gbc_chckbxWrapLines = new GridBagConstraints();
		gbc_chckbxWrapLines.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxWrapLines.gridx = 1;
		gbc_chckbxWrapLines.gridy = 0;
		panel.add(chckbxWrapLines, gbc_chckbxWrapLines);
		
		lblWrapLength = new JLabel("Wrap Length");
		GridBagConstraints gbc_lblWrapLength = new GridBagConstraints();
		gbc_lblWrapLength.insets = new Insets(0, 0, 0, 5);
		gbc_lblWrapLength.gridx = 3;
		gbc_lblWrapLength.gridy = 0;
		panel.add(lblWrapLength, gbc_lblWrapLength);
		
		spinnerWrapLength = new JSpinner();
		GridBagConstraints gbc_spinnerWrapLength = new GridBagConstraints();
		gbc_spinnerWrapLength.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerWrapLength.gridx = 4;
		gbc_spinnerWrapLength.gridy = 0;
		panel.add(spinnerWrapLength, gbc_spinnerWrapLength);
		spinnerWrapLength.setMinimumSize(new Dimension(100, 22));
		spinnerWrapLength.setModel(new SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
		
		lblPerPage = new JLabel("Per Page");
		GridBagConstraints gbc_lblPerPage = new GridBagConstraints();
		gbc_lblPerPage.anchor = GridBagConstraints.EAST;
		gbc_lblPerPage.insets = new Insets(0, 0, 5, 5);
		gbc_lblPerPage.gridx = 0;
		gbc_lblPerPage.gridy = 5;
		add(lblPerPage, gbc_lblPerPage);
		
		chckbxPerPage = new JCheckBox("");
		GridBagConstraints gbc_chckbxPerPage = new GridBagConstraints();
		gbc_chckbxPerPage.anchor = GridBagConstraints.WEST;
		gbc_chckbxPerPage.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxPerPage.gridx = 1;
		gbc_chckbxPerPage.gridy = 5;
		add(chckbxPerPage, gbc_chckbxPerPage);
		
		lblLineSeparator = new JLabel("Line Separator");
		GridBagConstraints gbc_lblLineSeparator = new GridBagConstraints();
		gbc_lblLineSeparator.anchor = GridBagConstraints.EAST;
		gbc_lblLineSeparator.insets = new Insets(0, 0, 5, 5);
		gbc_lblLineSeparator.gridx = 0;
		gbc_lblLineSeparator.gridy = 6;
		add(lblLineSeparator, gbc_lblLineSeparator);
		
		comboLineSeparator = new ComboItemBox();
		GridBagConstraints gbc_comboLineSeparator = new GridBagConstraints();
		gbc_comboLineSeparator.insets = new Insets(0, 0, 5, 5);
		gbc_comboLineSeparator.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboLineSeparator.gridx = 1;
		gbc_comboLineSeparator.gridy = 6;
		add(comboLineSeparator, gbc_comboLineSeparator);
		
		lblEncoding = new JLabel("Encoding");
		GridBagConstraints gbc_lblEncoding = new GridBagConstraints();
		gbc_lblEncoding.anchor = GridBagConstraints.EAST;
		gbc_lblEncoding.insets = new Insets(0, 0, 5, 5);
		gbc_lblEncoding.gridx = 0;
		gbc_lblEncoding.gridy = 7;
		add(lblEncoding, gbc_lblEncoding);
		
		comboEncoding = new ComboItemBox();
		GridBagConstraints gbc_comboEncoding = new GridBagConstraints();
		gbc_comboEncoding.insets = new Insets(0, 0, 5, 5);
		gbc_comboEncoding.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboEncoding.gridx = 1;
		gbc_comboEncoding.gridy = 7;
		add(comboEncoding, gbc_comboEncoding);
		
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
			
			comboLineSeparator.addValue("\\n","\n");
			comboLineSeparator.addValue("\\r\\n","\r\n");
			comboLineSeparator.setSelectedValue(System.lineSeparator());
			
			comboEncoding.addValue("UTF-8","UTF-8");
			comboEncoding.addValue("UTF-16","UTF-16");
			comboEncoding.addValue("CP-1252 / WINDOWS-1252","CP-1252");
			comboEncoding.addValue("ASCII","ASCII");
			comboEncoding.addValue("ISO-8859-1","ISO-8859-1");
			comboEncoding.addValue("UTF-16LE","UTF-16LE");
			comboEncoding.addValue("UTF-16BE","UTF-16BE");
			comboEncoding.setSelectedIndex(0);
		}
	}
	
	@Override
	public void setEnabled(boolean value){
		for(Component c : getComponents()){
			c.setEnabled(value);
		}
	}
	
	public JCheckBox getChckbxWrapLines() {
		return chckbxWrapLines;
	}
	
	public JSpinner getSpinnerWrapLength() {
		return spinnerWrapLength;
	}
	
	public JCheckBox getChckbxPerPage() {
		return chckbxPerPage;
	}
	
	public ComboItemBox getComboLineSeparator() {
		return comboLineSeparator;
	}
	
	public ComboItemBox getComboEncoding() {
		return comboEncoding;
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
}