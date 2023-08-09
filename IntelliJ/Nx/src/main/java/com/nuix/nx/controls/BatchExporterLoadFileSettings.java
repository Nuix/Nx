/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.border.TitledBorder;

import com.nuix.nx.NuixConnection;

import nuix.MetadataProfile;

import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;

/***
 * A control which encapsulates some of the common load file export settings.
 * @author Jason Wells
 */
@SuppressWarnings("serial")
public class BatchExporterLoadFileSettings extends JPanel {
	private ComboItemBox comboLoadFileType;
	private ComboItemBox comboProfile;
	private ComboItemBox comboEncoding;
	private ComboItemBox comboLineSeparator;
	public BatchExporterLoadFileSettings() {
		setBorder(new TitledBorder(null, "Load File Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 200, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblType = new JLabel("Type");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.anchor = GridBagConstraints.EAST;
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 0;
		add(lblType, gbc_lblType);
		
		comboLoadFileType = new ComboItemBox();
		GridBagConstraints gbc_comboLoadFileType = new GridBagConstraints();
		gbc_comboLoadFileType.insets = new Insets(0, 0, 5, 5);
		gbc_comboLoadFileType.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboLoadFileType.gridx = 1;
		gbc_comboLoadFileType.gridy = 0;
		add(comboLoadFileType, gbc_comboLoadFileType);
		
		JLabel lblMetadataProfile = new JLabel("Metadata Profile");
		GridBagConstraints gbc_lblMetadataProfile = new GridBagConstraints();
		gbc_lblMetadataProfile.anchor = GridBagConstraints.EAST;
		gbc_lblMetadataProfile.insets = new Insets(0, 0, 5, 5);
		gbc_lblMetadataProfile.gridx = 0;
		gbc_lblMetadataProfile.gridy = 1;
		add(lblMetadataProfile, gbc_lblMetadataProfile);
		
		comboProfile = new ComboItemBox();
		GridBagConstraints gbc_comboProfile = new GridBagConstraints();
		gbc_comboProfile.insets = new Insets(0, 0, 5, 5);
		gbc_comboProfile.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboProfile.gridx = 1;
		gbc_comboProfile.gridy = 1;
		add(comboProfile, gbc_comboProfile);
		
		{
			JLabel lblEncoding = new JLabel("Encoding");
			GridBagConstraints gbc_lblEncoding = new GridBagConstraints();
			gbc_lblEncoding.anchor = GridBagConstraints.EAST;
			gbc_lblEncoding.insets = new Insets(0, 0, 5, 5);
			gbc_lblEncoding.gridx = 0;
			gbc_lblEncoding.gridy = 2;
			add(lblEncoding, gbc_lblEncoding);
			
			comboEncoding = new ComboItemBox();
			GridBagConstraints gbc_comboEncoding = new GridBagConstraints();
			gbc_comboEncoding.insets = new Insets(0, 0, 5, 5);
			gbc_comboEncoding.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboEncoding.gridx = 1;
			gbc_comboEncoding.gridy = 2;
			add(comboEncoding, gbc_comboEncoding);
			
			JLabel lblLineSeparator = new JLabel("Line Separator");
			GridBagConstraints gbc_lblLineSeparator = new GridBagConstraints();
			gbc_lblLineSeparator.anchor = GridBagConstraints.EAST;
			gbc_lblLineSeparator.insets = new Insets(0, 0, 5, 5);
			gbc_lblLineSeparator.gridx = 0;
			gbc_lblLineSeparator.gridy = 3;
			add(lblLineSeparator, gbc_lblLineSeparator);
			
			comboLineSeparator = new ComboItemBox();
			GridBagConstraints gbc_comboLineSeparator = new GridBagConstraints();
			gbc_comboLineSeparator.insets = new Insets(0, 0, 5, 5);
			gbc_comboLineSeparator.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboLineSeparator.gridx = 1;
			gbc_comboLineSeparator.gridy = 3;
			add(comboLineSeparator, gbc_comboLineSeparator);
			
			comboLoadFileType.addValue("Concordance","concordance");
			comboLoadFileType.addValue("Summation","summation");
			comboLoadFileType.addValue("Discovery Radar","discovery_radar");
			comboLoadFileType.addValue("Documatrix","documatrix");
			comboLoadFileType.addValue("EDRM XML","edrm_xml");
			comboLoadFileType.addValue("EDRM XML ZIP","edrm_xml_zip");
			comboLoadFileType.addValue("IPRO","ipro");
			comboLoadFileType.addValue("Ring Tail","ringtail");
			comboLoadFileType.addValue("XHTML Summary Report","xhtml_summary_report");
			comboLoadFileType.addValue("CSV Summary Report","csv_summary_report ");
			comboLoadFileType.setSelectedIndex(0);
			
			List<MetadataProfile> profiles = NuixConnection.getUtilities().getMetadataProfileStore().getMetadataProfiles();
			for(MetadataProfile profile : profiles){
				String label = profile.getName() + " - " + profile.getMetadata().size() + " fields";
				comboProfile.addValue(label,profile.getName());
			}
			comboProfile.setSelectedIndex(0);
			
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

	public ComboItemBox getComboLoadFileType() {
		return comboLoadFileType;
	}
	
	public ComboItemBox getComboProfile() {
		return comboProfile;
	}
	
	public ComboItemBox getComboEncoding() {
		return comboEncoding;
	}
	
	public ComboItemBox getComboLineSeparator() {
		return comboLineSeparator;
	}
}