/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.nuix.nx.NuixConnection;
import com.nuix.nx.controls.models.Choice;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/***
 * A control for providing some of the common OCR settings
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class OcrSettings extends JPanel {
	private JCheckBox chckbxRegeneratePdfs;
	private JCheckBox chckbxUpdatePdfText;
	private JCheckBox chckbxUpdateItemText;
	private ComboItemBox comboTextModification;
	private ComboItemBox comboQuality;
	private ComboItemBox comboRotation;
	private JCheckBox chckbxDeskew;
	private ChoiceTableControl<String> languageChoices;
	private JLabel lblOutputDirectory;
	private PathSelectionControl outputDirectory;
	private JPanel panel;
	private JPanel panel_1;
	private JLabel lblUpdateDuplicates;
	private JCheckBox chckbxUpdateDuplicates;
	private JLabel lblTimeoutminutes;
	private JSpinner timeoutMinutes;
	public OcrSettings() {
		setBorder(new TitledBorder(null, "OCR Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 100, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		lblOutputDirectory = new JLabel("Output Directory");
		GridBagConstraints gbc_lblOutputDirectory = new GridBagConstraints();
		gbc_lblOutputDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblOutputDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputDirectory.gridx = 0;
		gbc_lblOutputDirectory.gridy = 0;
		add(lblOutputDirectory, gbc_lblOutputDirectory);
		
		outputDirectory = new PathSelectionControl(PathSelectionControl.ChooserType.DIRECTORY, (String) null, (String) null, "Output Directory");
		GridBagConstraints gbc_outputDirectory = new GridBagConstraints();
		gbc_outputDirectory.gridwidth = 2;
		gbc_outputDirectory.insets = new Insets(0, 0, 5, 0);
		gbc_outputDirectory.fill = GridBagConstraints.BOTH;
		gbc_outputDirectory.gridx = 1;
		gbc_outputDirectory.gridy = 0;
		add(outputDirectory, gbc_outputDirectory);
		
		panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 3;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 25, 0, 0, 25, 0, 0, 25, 0, 0, 25, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblUpdatePdfText = new JLabel("Update PDF Text");
		GridBagConstraints gbc_lblUpdatePdfText = new GridBagConstraints();
		gbc_lblUpdatePdfText.anchor = GridBagConstraints.EAST;
		gbc_lblUpdatePdfText.insets = new Insets(0, 0, 0, 5);
		gbc_lblUpdatePdfText.gridx = 0;
		gbc_lblUpdatePdfText.gridy = 0;
		panel_1.add(lblUpdatePdfText, gbc_lblUpdatePdfText);
		
		chckbxUpdatePdfText = new JCheckBox("");
		GridBagConstraints gbc_chckbxUpdatePdfText = new GridBagConstraints();
		gbc_chckbxUpdatePdfText.anchor = GridBagConstraints.WEST;
		gbc_chckbxUpdatePdfText.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxUpdatePdfText.gridx = 1;
		gbc_chckbxUpdatePdfText.gridy = 0;
		panel_1.add(chckbxUpdatePdfText, gbc_chckbxUpdatePdfText);
		chckbxUpdatePdfText.setSelected(true);
		
		JLabel lblUpdateItemText = new JLabel("Update Item Text");
		GridBagConstraints gbc_lblUpdateItemText = new GridBagConstraints();
		gbc_lblUpdateItemText.insets = new Insets(0, 0, 0, 5);
		gbc_lblUpdateItemText.anchor = GridBagConstraints.EAST;
		gbc_lblUpdateItemText.gridx = 3;
		gbc_lblUpdateItemText.gridy = 0;
		panel_1.add(lblUpdateItemText, gbc_lblUpdateItemText);
		
		chckbxUpdateItemText = new JCheckBox("");
		GridBagConstraints gbc_chckbxUpdateItemText = new GridBagConstraints();
		gbc_chckbxUpdateItemText.anchor = GridBagConstraints.WEST;
		gbc_chckbxUpdateItemText.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxUpdateItemText.gridx = 4;
		gbc_chckbxUpdateItemText.gridy = 0;
		panel_1.add(chckbxUpdateItemText, gbc_chckbxUpdateItemText);
		chckbxUpdateItemText.setSelected(true);
		
		JLabel lblRegeneratePdfs = new JLabel("Regenerate PDFs");
		GridBagConstraints gbc_lblRegeneratePdfs = new GridBagConstraints();
		gbc_lblRegeneratePdfs.anchor = GridBagConstraints.EAST;
		gbc_lblRegeneratePdfs.insets = new Insets(0, 0, 0, 5);
		gbc_lblRegeneratePdfs.gridx = 6;
		gbc_lblRegeneratePdfs.gridy = 0;
		panel_1.add(lblRegeneratePdfs, gbc_lblRegeneratePdfs);
		
		chckbxRegeneratePdfs = new JCheckBox("");
		GridBagConstraints gbc_chckbxRegeneratePdfs = new GridBagConstraints();
		gbc_chckbxRegeneratePdfs.anchor = GridBagConstraints.WEST;
		gbc_chckbxRegeneratePdfs.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxRegeneratePdfs.gridx = 7;
		gbc_chckbxRegeneratePdfs.gridy = 0;
		panel_1.add(chckbxRegeneratePdfs, gbc_chckbxRegeneratePdfs);
		
		JLabel lblDeskew = new JLabel("Deskew");
		GridBagConstraints gbc_lblDeskew = new GridBagConstraints();
		gbc_lblDeskew.insets = new Insets(0, 0, 0, 5);
		gbc_lblDeskew.anchor = GridBagConstraints.EAST;
		gbc_lblDeskew.gridx = 9;
		gbc_lblDeskew.gridy = 0;
		panel_1.add(lblDeskew, gbc_lblDeskew);
		
		chckbxDeskew = new JCheckBox("");
		GridBagConstraints gbc_chckbxDeskew = new GridBagConstraints();
		gbc_chckbxDeskew.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxDeskew.anchor = GridBagConstraints.WEST;
		gbc_chckbxDeskew.gridx = 10;
		gbc_chckbxDeskew.gridy = 0;
		panel_1.add(chckbxDeskew, gbc_chckbxDeskew);
		chckbxDeskew.setSelected(true);
		
		lblUpdateDuplicates = new JLabel("Update Duplicates");
		GridBagConstraints gbc_lblUpdateDuplicates = new GridBagConstraints();
		gbc_lblUpdateDuplicates.insets = new Insets(0, 0, 0, 5);
		gbc_lblUpdateDuplicates.anchor = GridBagConstraints.EAST;
		gbc_lblUpdateDuplicates.gridx = 12;
		gbc_lblUpdateDuplicates.gridy = 0;
		panel_1.add(lblUpdateDuplicates, gbc_lblUpdateDuplicates);
		
		chckbxUpdateDuplicates = new JCheckBox("");
		chckbxUpdateDuplicates.setSelected(true);
		GridBagConstraints gbc_chckbxUpdateDuplicates = new GridBagConstraints();
		gbc_chckbxUpdateDuplicates.gridx = 13;
		gbc_chckbxUpdateDuplicates.gridy = 0;
		panel_1.add(chckbxUpdateDuplicates, gbc_chckbxUpdateDuplicates);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 25, 0, 0, 25, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 2.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblTextUpdateMethod = new JLabel("Text Update Method");
		GridBagConstraints gbc_lblTextUpdateMethod = new GridBagConstraints();
		gbc_lblTextUpdateMethod.anchor = GridBagConstraints.EAST;
		gbc_lblTextUpdateMethod.insets = new Insets(0, 0, 0, 5);
		gbc_lblTextUpdateMethod.gridx = 0;
		gbc_lblTextUpdateMethod.gridy = 0;
		panel.add(lblTextUpdateMethod, gbc_lblTextUpdateMethod);
		
		comboTextModification = new ComboItemBox();
		GridBagConstraints gbc_comboTextModification = new GridBagConstraints();
		gbc_comboTextModification.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboTextModification.insets = new Insets(0, 0, 0, 5);
		gbc_comboTextModification.gridx = 1;
		gbc_comboTextModification.gridy = 0;
		panel.add(comboTextModification, gbc_comboTextModification);
		
		comboTextModification.addValue("Append", "append");
		comboTextModification.addValue("Overwrite", "overwrite");
		comboTextModification.setSelectedIndex(0);
		
		JLabel lblQuality = new JLabel("Quality");
		GridBagConstraints gbc_lblQuality = new GridBagConstraints();
		gbc_lblQuality.insets = new Insets(0, 0, 0, 5);
		gbc_lblQuality.anchor = GridBagConstraints.EAST;
		gbc_lblQuality.gridx = 3;
		gbc_lblQuality.gridy = 0;
		panel.add(lblQuality, gbc_lblQuality);
		
		comboQuality = new ComboItemBox();
		GridBagConstraints gbc_comboQuality = new GridBagConstraints();
		gbc_comboQuality.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboQuality.insets = new Insets(0, 0, 0, 5);
		gbc_comboQuality.gridx = 4;
		gbc_comboQuality.gridy = 0;
		panel.add(comboQuality, gbc_comboQuality);
		
		if(NuixConnection.getCurrentNuixVersion().isLessThan("7.4")){
			comboQuality.addValue("High Quality","high_quality");
			comboQuality.addValue("Medium Quality","mid_range");
			comboQuality.addValue("Fast","fast");	
		} else {
			// Nuix 7.4 OCR quality settings are quite a bit different
			comboQuality.addValue("Default","default");
			comboQuality.addValue("Document Archiving (accuracy)","document_archiving_accuracy");
			comboQuality.addValue("Document Archiving (speed)","document_archiving_speed");
			comboQuality.addValue("Book Archiving (accuracy)","book_archiving_accuracy");
			comboQuality.addValue("Book Archiving (speed)","book_archiving_speed");
			comboQuality.addValue("Document Conversion (accuracy)","document_conversion_accuracy");
			comboQuality.addValue("Document Conversion (speed)","document_conversion_speed");
			comboQuality.addValue("Text Extraction (accuracy)","text_extraction_accuracy");
			comboQuality.addValue("Text Extraction (speed)","text_extraction_speed");
			comboQuality.addValue("Field Level Recognition","field_level_recognition");
		}
		
		comboQuality.setSelectedIndex(0);
		
		JLabel lblRotation = new JLabel("Rotation");
		GridBagConstraints gbc_lblRotation = new GridBagConstraints();
		gbc_lblRotation.insets = new Insets(0, 0, 0, 5);
		gbc_lblRotation.anchor = GridBagConstraints.EAST;
		gbc_lblRotation.gridx = 6;
		gbc_lblRotation.gridy = 0;
		panel.add(lblRotation, gbc_lblRotation);
		
		comboRotation = new ComboItemBox();
		GridBagConstraints gbc_comboRotation = new GridBagConstraints();
		gbc_comboRotation.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboRotation.gridx = 7;
		gbc_comboRotation.gridy = 0;
		panel.add(comboRotation, gbc_comboRotation);
		
		comboRotation.addValue("Auto","auto");
		comboRotation.addValue("No Rotation","no_rotation");
		comboRotation.addValue("Left","left");
		comboRotation.addValue("Right","right");
		comboRotation.addValue("Upside Down","upside_down");
		comboRotation.setSelectedIndex(0);
		
		lblTimeoutminutes = new JLabel("Item Timeout (minutes):");
		GridBagConstraints gbc_lblTimeoutminutes = new GridBagConstraints();
		gbc_lblTimeoutminutes.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeoutminutes.gridx = 0;
		gbc_lblTimeoutminutes.gridy = 3;
		add(lblTimeoutminutes, gbc_lblTimeoutminutes);
		
		timeoutMinutes = new JSpinner();
		timeoutMinutes.setModel(new SpinnerNumberModel(90, 1, 1440, 1));
		GridBagConstraints gbc_timeoutMinutes = new GridBagConstraints();
		gbc_timeoutMinutes.fill = GridBagConstraints.HORIZONTAL;
		gbc_timeoutMinutes.insets = new Insets(0, 0, 5, 5);
		gbc_timeoutMinutes.gridx = 1;
		gbc_timeoutMinutes.gridy = 3;
		add(timeoutMinutes, gbc_timeoutMinutes);
		
		JLabel lblLanguages = new JLabel("Languages");
		lblLanguages.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblLanguages = new GridBagConstraints();
		gbc_lblLanguages.insets = new Insets(0, 0, 5, 5);
		gbc_lblLanguages.gridx = 0;
		gbc_lblLanguages.gridy = 4;
		add(lblLanguages, gbc_lblLanguages);
		
		languageChoices = new ChoiceTableControl<String>();
		GridBagConstraints gbc_languageChoices = new GridBagConstraints();
		gbc_languageChoices.gridwidth = 3;
		gbc_languageChoices.fill = GridBagConstraints.BOTH;
		gbc_languageChoices.gridx = 0;
		gbc_languageChoices.gridy = 5;

		languageChoices.getTableModel().setChoiceTypeName("Language");
		List<Choice<String>> languages = new ArrayList<Choice<String>>();
		languages.add(new Choice<String>("English","English","",true));
		languages.add(new Choice<String>("Abkhaz"));
		languages.add(new Choice<String>("Adyghe"));
		languages.add(new Choice<String>("Afrikaans"));
		languages.add(new Choice<String>("Agul"));
		languages.add(new Choice<String>("Albanian"));
		languages.add(new Choice<String>("Altaic"));
		languages.add(new Choice<String>("Arabic"));
		languages.add(new Choice<String>("ArmenianEastern"));
		languages.add(new Choice<String>("ArmenianGrabar"));
		languages.add(new Choice<String>("ArmenianWestern"));
		languages.add(new Choice<String>("Awar"));
		languages.add(new Choice<String>("Aymara"));
		languages.add(new Choice<String>("AzeriCyrillic"));
		languages.add(new Choice<String>("AzeriLatin"));
		languages.add(new Choice<String>("Bashkir"));
		languages.add(new Choice<String>("Basque"));
		languages.add(new Choice<String>("Belarusian"));
		languages.add(new Choice<String>("Bemba"));
		languages.add(new Choice<String>("Blackfoot"));
		languages.add(new Choice<String>("Breton"));
		languages.add(new Choice<String>("Bugotu"));
		languages.add(new Choice<String>("Bulgarian"));
		languages.add(new Choice<String>("Buryat"));
		languages.add(new Choice<String>("Catalan"));
		languages.add(new Choice<String>("Chamorro"));
		languages.add(new Choice<String>("Chechen"));
		languages.add(new Choice<String>("ChinesePRC"));
		languages.add(new Choice<String>("ChineseTaiwan"));
		languages.add(new Choice<String>("Chukcha"));
		languages.add(new Choice<String>("Chuvash"));
		languages.add(new Choice<String>("Corsican"));
		languages.add(new Choice<String>("CrimeanTatar"));
		languages.add(new Choice<String>("Croatian"));
		languages.add(new Choice<String>("Crow"));
		languages.add(new Choice<String>("Czech"));
		languages.add(new Choice<String>("Danish"));
		languages.add(new Choice<String>("Dargwa"));
		languages.add(new Choice<String>("Dungan"));
		languages.add(new Choice<String>("Dutch"));
		languages.add(new Choice<String>("DutchBelgian"));
		languages.add(new Choice<String>("EskimoCyrillic"));
		languages.add(new Choice<String>("EskimoLatin"));
		languages.add(new Choice<String>("Esperanto"));
		languages.add(new Choice<String>("Estonian"));
		languages.add(new Choice<String>("Even"));
		languages.add(new Choice<String>("Evenki"));
		languages.add(new Choice<String>("Faeroese"));
		languages.add(new Choice<String>("Fijian"));
		languages.add(new Choice<String>("Finnish"));
		languages.add(new Choice<String>("French"));
		languages.add(new Choice<String>("Frisian"));
		languages.add(new Choice<String>("Friulian"));
		languages.add(new Choice<String>("GaelicScottish"));
		languages.add(new Choice<String>("Gagauz"));
		languages.add(new Choice<String>("Galician"));
		languages.add(new Choice<String>("Ganda"));
		languages.add(new Choice<String>("German"));
		languages.add(new Choice<String>("GermanLuxembourg"));
		languages.add(new Choice<String>("GermanNewSpelling"));
		languages.add(new Choice<String>("Greek"));
		languages.add(new Choice<String>("Guarani"));
		languages.add(new Choice<String>("Hani"));
		languages.add(new Choice<String>("Hausa"));
		languages.add(new Choice<String>("Hawaiian"));
		languages.add(new Choice<String>("Hebrew"));
		languages.add(new Choice<String>("Hungarian"));
		languages.add(new Choice<String>("Icelandic"));
		languages.add(new Choice<String>("Ido"));
		languages.add(new Choice<String>("Indonesian"));
		languages.add(new Choice<String>("Ingush"));
		languages.add(new Choice<String>("Interlingua"));
		languages.add(new Choice<String>("Irish"));
		languages.add(new Choice<String>("Italian"));
		languages.add(new Choice<String>("Japanese"));
		languages.add(new Choice<String>("Kabardian"));
		languages.add(new Choice<String>("Kalmyk"));
		languages.add(new Choice<String>("KarachayBalkar"));
		languages.add(new Choice<String>("Karakalpak"));
		languages.add(new Choice<String>("Kasub"));
		languages.add(new Choice<String>("Kawa"));
		languages.add(new Choice<String>("Kazakh"));
		languages.add(new Choice<String>("Khakas"));
		languages.add(new Choice<String>("Khanty"));
		languages.add(new Choice<String>("Kikuyu"));
		languages.add(new Choice<String>("Kirgiz"));
		languages.add(new Choice<String>("Kongo"));
		languages.add(new Choice<String>("Korean"));
		languages.add(new Choice<String>("Koryak"));
		languages.add(new Choice<String>("Kpelle"));
		languages.add(new Choice<String>("Kumyk"));
		languages.add(new Choice<String>("Kurdish"));
		languages.add(new Choice<String>("Lak"));
		languages.add(new Choice<String>("Lappish"));
		languages.add(new Choice<String>("Latin"));
		languages.add(new Choice<String>("Latvian"));
		languages.add(new Choice<String>("LatvianGothic"));
		languages.add(new Choice<String>("Lezgin"));
		languages.add(new Choice<String>("Lithuanian"));
		languages.add(new Choice<String>("Luba"));
		languages.add(new Choice<String>("Macedonian"));
		languages.add(new Choice<String>("Malagasy"));
		languages.add(new Choice<String>("Malay"));
		languages.add(new Choice<String>("Malinke"));
		languages.add(new Choice<String>("Maltese"));
		languages.add(new Choice<String>("Mansi"));
		languages.add(new Choice<String>("Maori"));
		languages.add(new Choice<String>("Mari"));
		languages.add(new Choice<String>("Maya"));
		languages.add(new Choice<String>("Miao"));
		languages.add(new Choice<String>("Minankabaw"));
		languages.add(new Choice<String>("Mohawk"));
		languages.add(new Choice<String>("Mongol"));
		languages.add(new Choice<String>("Mordvin"));
		languages.add(new Choice<String>("Nahuatl"));
		languages.add(new Choice<String>("Nenets"));
		languages.add(new Choice<String>("Nivkh"));
		languages.add(new Choice<String>("Nogay"));
		languages.add(new Choice<String>("Norwegian"));
		languages.add(new Choice<String>("NorwegianBokmal"));
		languages.add(new Choice<String>("NorwegianNynorsk"));
		languages.add(new Choice<String>("Nyanja"));
		languages.add(new Choice<String>("Occidental"));
		languages.add(new Choice<String>("Ojibway"));
		languages.add(new Choice<String>("Ossetic"));
		languages.add(new Choice<String>("Papiamento"));
		languages.add(new Choice<String>("PidginEnglish"));
		languages.add(new Choice<String>("Polish"));
		languages.add(new Choice<String>("PortugueseBrazilian"));
		languages.add(new Choice<String>("PortugueseStandard"));
		languages.add(new Choice<String>("Provencal"));
		languages.add(new Choice<String>("Quechua"));
		languages.add(new Choice<String>("RhaetoRomanic"));
		languages.add(new Choice<String>("Romanian"));
		languages.add(new Choice<String>("RomanianMoldavia"));
		languages.add(new Choice<String>("Romany"));
		languages.add(new Choice<String>("Ruanda"));
		languages.add(new Choice<String>("Rundi"));
		languages.add(new Choice<String>("Russian"));
		languages.add(new Choice<String>("RussianOldSpelling"));
		languages.add(new Choice<String>("RussianWithAccent"));
		languages.add(new Choice<String>("Samoan"));
		languages.add(new Choice<String>("Selkup"));
		languages.add(new Choice<String>("SerbianCyrillic"));
		languages.add(new Choice<String>("SerbianLatin"));
		languages.add(new Choice<String>("Shona"));
		languages.add(new Choice<String>("Sioux"));
		languages.add(new Choice<String>("Slovak"));
		languages.add(new Choice<String>("Slovenian"));
		languages.add(new Choice<String>("Somali"));
		languages.add(new Choice<String>("Sorbian"));
		languages.add(new Choice<String>("Sotho"));
		languages.add(new Choice<String>("Spanish"));
		languages.add(new Choice<String>("Sunda"));
		languages.add(new Choice<String>("Swahili"));
		languages.add(new Choice<String>("Swazi"));
		languages.add(new Choice<String>("Swedish"));
		languages.add(new Choice<String>("Tabassaran"));
		languages.add(new Choice<String>("Tagalog"));
		languages.add(new Choice<String>("Tahitian"));
		languages.add(new Choice<String>("Tajik"));
		languages.add(new Choice<String>("Tatar"));
		languages.add(new Choice<String>("Thai"));
		languages.add(new Choice<String>("Tinpo"));
		languages.add(new Choice<String>("Tongan"));
		languages.add(new Choice<String>("Tswana"));
		languages.add(new Choice<String>("Tun"));
		languages.add(new Choice<String>("Turkish"));
		languages.add(new Choice<String>("Turkmen"));
		languages.add(new Choice<String>("TurkmenLatin"));
		languages.add(new Choice<String>("Tuvin"));
		languages.add(new Choice<String>("Udmurt"));
		languages.add(new Choice<String>("UighurCyrillic"));
		languages.add(new Choice<String>("UighurLatin"));
		languages.add(new Choice<String>("Ukrainian"));
		languages.add(new Choice<String>("UzbekCyrillic"));
		languages.add(new Choice<String>("UzbekLatin"));
		languages.add(new Choice<String>("Vietnamese"));
		languages.add(new Choice<String>("Visayan"));
		languages.add(new Choice<String>("Welsh"));
		languages.add(new Choice<String>("Wolof"));
		languages.add(new Choice<String>("Xhosa"));
		languages.add(new Choice<String>("Yakut"));
		languages.add(new Choice<String>("Yiddish"));
		languages.add(new Choice<String>("Zapotec"));
		languages.add(new Choice<String>("Zulu"));
		languageChoices.setChoices(languages);
		
		add(languageChoices, gbc_languageChoices);
				
		// Update duplicates was added in Nuix 7.2.0
		if(NuixConnection.getCurrentNuixVersion().isLessThan("7.2.0")){
			chckbxUpdateDuplicates.setVisible(false);
			lblTimeoutminutes.setVisible(false);
			timeoutMinutes.setVisible(false);
		}
		
		languageChoices.constrainFirstColumn();
	}

	public JCheckBox getChckbxRegeneratePdfs() {
		return chckbxRegeneratePdfs;
	}
	public JCheckBox getChckbxUpdatePdfText() {
		return chckbxUpdatePdfText;
	}
	public JCheckBox getChckbxUpdateItemText() {
		return chckbxUpdateItemText;
	}
	public ComboItemBox getComboTextModification() {
		return comboTextModification;
	}
	public ComboItemBox getComboQuality() {
		return comboQuality;
	}
	public ComboItemBox getComboRotation() {
		return comboRotation;
	}
	public JCheckBox getChckbxDeskew() {
		return chckbxDeskew;
	}
	public boolean getUpdateDuplicates(){
		return chckbxUpdateDuplicates.isSelected();
	}
	public void setUpdateDuplicates(boolean value){
		chckbxUpdateDuplicates.setSelected(value);
	}
	public ChoiceTableControl<String> getLanguageChoices() {
		return languageChoices;
	}
	public PathSelectionControl getOutputDirectory() {
		return outputDirectory;
	}
	public int getTimeoutMinutes(){
		return (int)timeoutMinutes.getValue();
	}
	public void setTimeoutMinutes(int value){
		timeoutMinutes.setValue(value);
	}
}
