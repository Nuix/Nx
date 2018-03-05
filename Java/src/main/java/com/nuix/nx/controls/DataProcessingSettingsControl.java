/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nuix.nx.dialogs.CommonDialogs;

/***
 * A control which allows the user to specify settings found in the GUI under "Data Processing Settings" during ingestion
 * setup.
 * @author JWells01
 *
 */
@SuppressWarnings("serial")
public class DataProcessingSettingsControl extends JPanel {
	
	private static Logger logger = Logger.getLogger(DataProcessingSettingsControl.class);
	
	class ComboChoice {
		public String displayName;
		public String settingName;
		
		public ComboChoice(String displayName, String settingName){
			this.displayName = displayName;
			this.settingName = settingName;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
	}
	
	private JTextField txtCarvingBlockSize;
	private JCheckBox chckbxPerformItemIdentification;
	private JCheckBox chckbxCalculateProcessingSize;
	private JSpinner spinnerMaxDigestSize;
	private JCheckBox chckbxDigestIncludeBcc;
	private JCheckBox chckbxDigestIncludeItemDate;
	private JComboBox<ComboChoice> comboTraversal;
	private JCheckBox chckbxReuseEvidenceStores;
	private JCheckBox chckbxCalculateAuditedSize;
	private JCheckBox chckbxStoreBinary;
	private JSpinner spinnerMaxBinarySize;
	private JCheckBox chckbxRecoverDeletedFiles;
	private JCheckBox chckbxExtractEndOfFileSlackSpace;
	private JCheckBox chckbxSmartProcessRegistry;
	private JCheckBox chckbxExtractFromSlackSpace;
	private JCheckBox chckbxIndexUnallocatedSpace;
	private JCheckBox chckbxCarveFileSystem;
	private JCheckBox chckbxCreateFamilySearchFields;
	private JCheckBox chckbxHideImmaterialItems;
	private JComboBox<ComboChoice> comboAnalysisLanguage;
	private JCheckBox chckbxUseStopWords;
	private JCheckBox chckbxUseStemming;
	private JCheckBox chckbxEnableExactQueries;
	private JCheckBox chckbxProcessText;
	private JCheckBox chckbxEnableNearDuplicates;
	private JCheckBox chckbxEnableTextSummarisation;
	private JCheckBox chckbxExtractNamedEntities;
	private JCheckBox chckbxExtractNamedEntitiesFromTextStripped;
	private JCheckBox chckbxExtractNamedEntitiesFromProperties;
	private JCheckBox chckbxGenerateThumbnails;
	private JCheckBox chckbxPerformSkinToneAnalysis;
	private JCheckBox chckbxDetectFaces;
	private JCheckBox chckbxMd5;
	private JCheckBox chckbxSha1;
	private JCheckBox chckbxSha256;
	private JCheckBox chckbxSsdeep;
	private JToolBar toolBar;
	private JButton btnSaveSettings;
	private JButton btnLoadSettings;
	private JButton btnResetSettings;
	
	private BiMap<String,JCheckBox> booleanSettingsMap = HashBiMap.create();
	private BiMap<String,JSpinner> intSettingsMap = HashBiMap.create();
	private List<ComboChoice> traversalChoices = new ArrayList<ComboChoice>();
	private List<ComboChoice> languageChoices = new ArrayList<ComboChoice>();
	private Map<String,Object> defaultSettings = null;
	
	public DataProcessingSettingsControl() {
		setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.anchor = GridBagConstraints.WEST;
		gbc_toolBar.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 0;
		add(toolBar, gbc_toolBar);
		
		btnSaveSettings = new JButton("Save Processing Settings");
		btnSaveSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File outputFile = CommonDialogs.saveFileDialog("C:\\", "Processing Settings JSON", "json", "Save Processing Settings");
				if(outputFile != null){
					try {
						saveJSONFile(outputFile);
					} catch (Exception e1) {
						String message = "There was an error while saving the file:\n\n"+e1.getMessage();
						CommonDialogs.showError(message);
						logger.error(message,e1);
					}
				}
			}
		});
		btnSaveSettings.setIcon(new ImageIcon(DataProcessingSettingsControl.class.getResource("/com/nuix/nx/controls/page_save.png")));
		toolBar.add(btnSaveSettings);
		
		btnLoadSettings = new JButton("Load Processing Settings");
		btnLoadSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File inputFile = CommonDialogs.openFileDialog("C:\\", "Processing Settings JSON", "json", "Load Processing Settings");
				if(inputFile != null){
					try {
						loadSettingsJSONFile(inputFile);
					} catch (Exception e1) {
						String message = "There was an error while loading the file:\n\n"+e1.getMessage();
						CommonDialogs.showError(message);
						logger.error(message,e1);
					}
				}
			}
		});
		btnLoadSettings.setIcon(new ImageIcon(DataProcessingSettingsControl.class.getResource("/com/nuix/nx/controls/folder_page.png")));
		toolBar.add(btnLoadSettings);
		
		btnResetSettings = new JButton("Reset Processing Settings");
		btnResetSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(CommonDialogs.getConfirmation("Are you sure you want to reset all settings?", "Reset Settings")){
					loadDefaultSettings();	
				}
			}
		});
		btnResetSettings.setIcon(new ImageIcon(DataProcessingSettingsControl.class.getResource("/com/nuix/nx/controls/cancel.png")));
		toolBar.add(btnResetSettings);
		
		JPanel topMostPanel = new JPanel();
		GridBagConstraints gbc_topMostPanel = new GridBagConstraints();
		gbc_topMostPanel.anchor = GridBagConstraints.WEST;
		gbc_topMostPanel.insets = new Insets(0, 0, 5, 0);
		gbc_topMostPanel.fill = GridBagConstraints.VERTICAL;
		gbc_topMostPanel.gridx = 0;
		gbc_topMostPanel.gridy = 1;
		add(topMostPanel, gbc_topMostPanel);
		
		chckbxPerformItemIdentification = new JCheckBox("Perform item identification");
		topMostPanel.add(chckbxPerformItemIdentification);
		
		chckbxCalculateProcessingSize = new JCheckBox("Calculate processing size up-front");
		topMostPanel.add(chckbxCalculateProcessingSize);
		
		JPanel traversalPanel = new JPanel();
		GridBagConstraints gbc_traversalPanel = new GridBagConstraints();
		gbc_traversalPanel.insets = new Insets(0, 0, 5, 0);
		gbc_traversalPanel.fill = GridBagConstraints.BOTH;
		gbc_traversalPanel.gridx = 0;
		gbc_traversalPanel.gridy = 2;
		add(traversalPanel, gbc_traversalPanel);
		GridBagLayout gbl_traversalPanel = new GridBagLayout();
		gbl_traversalPanel.columnWidths = new int[]{0, 400, 0, 0};
		gbl_traversalPanel.rowHeights = new int[]{0, 0};
		gbl_traversalPanel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_traversalPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		traversalPanel.setLayout(gbl_traversalPanel);
		
		JLabel lblTraversal = new JLabel("Traversal:");
		GridBagConstraints gbc_lblTraversal = new GridBagConstraints();
		gbc_lblTraversal.insets = new Insets(0, 0, 0, 5);
		gbc_lblTraversal.anchor = GridBagConstraints.EAST;
		gbc_lblTraversal.gridx = 0;
		gbc_lblTraversal.gridy = 0;
		traversalPanel.add(lblTraversal, gbc_lblTraversal);
		
		comboTraversal = new JComboBox<ComboChoice>();
		GridBagConstraints gbc_comboTraversal = new GridBagConstraints();
		gbc_comboTraversal.insets = new Insets(0, 0, 0, 5);
		gbc_comboTraversal.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboTraversal.gridx = 1;
		gbc_comboTraversal.gridy = 0;
		traversalPanel.add(comboTraversal, gbc_comboTraversal);
		
		JPanel mainSettingsPanel = new JPanel();
		GridBagConstraints gbc_mainSettingsPanel = new GridBagConstraints();
		gbc_mainSettingsPanel.fill = GridBagConstraints.BOTH;
		gbc_mainSettingsPanel.gridx = 0;
		gbc_mainSettingsPanel.gridy = 3;
		add(mainSettingsPanel, gbc_mainSettingsPanel);
		GridBagLayout gbl_mainSettingsPanel = new GridBagLayout();
		gbl_mainSettingsPanel.columnWidths = new int[]{0, 0, 0};
		gbl_mainSettingsPanel.rowHeights = new int[]{0, 0};
		gbl_mainSettingsPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_mainSettingsPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		mainSettingsPanel.setLayout(gbl_mainSettingsPanel);
		
		JPanel leftPanel = new JPanel();
		GridBagConstraints gbc_leftPanel = new GridBagConstraints();
		gbc_leftPanel.insets = new Insets(0, 0, 0, 5);
		gbc_leftPanel.fill = GridBagConstraints.BOTH;
		gbc_leftPanel.gridx = 0;
		gbc_leftPanel.gridy = 0;
		mainSettingsPanel.add(leftPanel, gbc_leftPanel);
		GridBagLayout gbl_leftPanel = new GridBagLayout();
		gbl_leftPanel.columnWidths = new int[]{0, 0};
		gbl_leftPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_leftPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_leftPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		leftPanel.setLayout(gbl_leftPanel);
		
		JPanel evidenceSettings = new JPanel();
		evidenceSettings.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Evidence Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		GridBagConstraints gbc_evidenceSettings = new GridBagConstraints();
		gbc_evidenceSettings.insets = new Insets(0, 0, 5, 0);
		gbc_evidenceSettings.fill = GridBagConstraints.BOTH;
		gbc_evidenceSettings.gridx = 0;
		gbc_evidenceSettings.gridy = 0;
		leftPanel.add(evidenceSettings, gbc_evidenceSettings);
		GridBagLayout gbl_evidenceSettings = new GridBagLayout();
		gbl_evidenceSettings.columnWidths = new int[]{0, 0, 0};
		gbl_evidenceSettings.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_evidenceSettings.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_evidenceSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		evidenceSettings.setLayout(gbl_evidenceSettings);
		
		chckbxReuseEvidenceStores = new JCheckBox("Reuse evidence stores");
		GridBagConstraints gbc_chckbxReuseEvidenceStores = new GridBagConstraints();
		gbc_chckbxReuseEvidenceStores.gridwidth = 2;
		gbc_chckbxReuseEvidenceStores.anchor = GridBagConstraints.WEST;
		gbc_chckbxReuseEvidenceStores.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxReuseEvidenceStores.gridx = 0;
		gbc_chckbxReuseEvidenceStores.gridy = 0;
		evidenceSettings.add(chckbxReuseEvidenceStores, gbc_chckbxReuseEvidenceStores);
		
		chckbxCalculateAuditedSize = new JCheckBox("Calculate audited size");
		GridBagConstraints gbc_chckbxCalculateAuditedSize = new GridBagConstraints();
		gbc_chckbxCalculateAuditedSize.gridwidth = 2;
		gbc_chckbxCalculateAuditedSize.anchor = GridBagConstraints.WEST;
		gbc_chckbxCalculateAuditedSize.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxCalculateAuditedSize.gridx = 0;
		gbc_chckbxCalculateAuditedSize.gridy = 1;
		evidenceSettings.add(chckbxCalculateAuditedSize, gbc_chckbxCalculateAuditedSize);
		
		chckbxStoreBinary = new JCheckBox("Store binary of data items");
		GridBagConstraints gbc_chckbxStoreBinary = new GridBagConstraints();
		gbc_chckbxStoreBinary.gridwidth = 2;
		gbc_chckbxStoreBinary.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxStoreBinary.anchor = GridBagConstraints.WEST;
		gbc_chckbxStoreBinary.gridx = 0;
		gbc_chckbxStoreBinary.gridy = 2;
		evidenceSettings.add(chckbxStoreBinary, gbc_chckbxStoreBinary);
		
		JLabel lblMaximumBinarySize = new JLabel("Maximum binary size (MB):");
		GridBagConstraints gbc_lblMaximumBinarySize = new GridBagConstraints();
		gbc_lblMaximumBinarySize.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaximumBinarySize.gridx = 0;
		gbc_lblMaximumBinarySize.gridy = 3;
		evidenceSettings.add(lblMaximumBinarySize, gbc_lblMaximumBinarySize);
		
		spinnerMaxBinarySize = new JSpinner();
		spinnerMaxBinarySize.setModel(new SpinnerNumberModel(1000, 0, 1000, 1));
		GridBagConstraints gbc_spinnerMaxBinarySize = new GridBagConstraints();
		gbc_spinnerMaxBinarySize.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerMaxBinarySize.gridx = 1;
		gbc_spinnerMaxBinarySize.gridy = 3;
		evidenceSettings.add(spinnerMaxBinarySize, gbc_spinnerMaxBinarySize);
		
		JPanel deletedRecoverySettings = new JPanel();
		deletedRecoverySettings.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Deleted File Recovery & Forensic Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		GridBagConstraints gbc_deletedRecoverySettings = new GridBagConstraints();
		gbc_deletedRecoverySettings.insets = new Insets(0, 0, 5, 0);
		gbc_deletedRecoverySettings.fill = GridBagConstraints.BOTH;
		gbc_deletedRecoverySettings.gridx = 0;
		gbc_deletedRecoverySettings.gridy = 1;
		leftPanel.add(deletedRecoverySettings, gbc_deletedRecoverySettings);
		GridBagLayout gbl_deletedRecoverySettings = new GridBagLayout();
		gbl_deletedRecoverySettings.columnWidths = new int[]{0, 0, 0};
		gbl_deletedRecoverySettings.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_deletedRecoverySettings.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_deletedRecoverySettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		deletedRecoverySettings.setLayout(gbl_deletedRecoverySettings);
		
		chckbxRecoverDeletedFiles = new JCheckBox("Recover deleted files from disk images");
		GridBagConstraints gbc_chckbxRecoverDeletedFiles = new GridBagConstraints();
		gbc_chckbxRecoverDeletedFiles.gridwidth = 2;
		gbc_chckbxRecoverDeletedFiles.anchor = GridBagConstraints.WEST;
		gbc_chckbxRecoverDeletedFiles.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRecoverDeletedFiles.gridx = 0;
		gbc_chckbxRecoverDeletedFiles.gridy = 0;
		deletedRecoverySettings.add(chckbxRecoverDeletedFiles, gbc_chckbxRecoverDeletedFiles);
		
		chckbxExtractEndOfFileSlackSpace = new JCheckBox("Extract end-of-slack space from disk images");
		GridBagConstraints gbc_chckbxExtractEndOfFileSlackSpace = new GridBagConstraints();
		gbc_chckbxExtractEndOfFileSlackSpace.gridwidth = 2;
		gbc_chckbxExtractEndOfFileSlackSpace.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxExtractEndOfFileSlackSpace.anchor = GridBagConstraints.WEST;
		gbc_chckbxExtractEndOfFileSlackSpace.gridx = 0;
		gbc_chckbxExtractEndOfFileSlackSpace.gridy = 1;
		deletedRecoverySettings.add(chckbxExtractEndOfFileSlackSpace, gbc_chckbxExtractEndOfFileSlackSpace);
		
		chckbxSmartProcessRegistry = new JCheckBox("Smart process Microsoft Registry files");
		GridBagConstraints gbc_chckbxSmartProcessRegistry = new GridBagConstraints();
		gbc_chckbxSmartProcessRegistry.gridwidth = 2;
		gbc_chckbxSmartProcessRegistry.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSmartProcessRegistry.anchor = GridBagConstraints.WEST;
		gbc_chckbxSmartProcessRegistry.gridx = 0;
		gbc_chckbxSmartProcessRegistry.gridy = 2;
		deletedRecoverySettings.add(chckbxSmartProcessRegistry, gbc_chckbxSmartProcessRegistry);
		
		chckbxExtractFromSlackSpace = new JCheckBox("Extract from mailbox slack space");
		GridBagConstraints gbc_chckbxExtractFromSlackSpace = new GridBagConstraints();
		gbc_chckbxExtractFromSlackSpace.gridwidth = 2;
		gbc_chckbxExtractFromSlackSpace.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxExtractFromSlackSpace.anchor = GridBagConstraints.WEST;
		gbc_chckbxExtractFromSlackSpace.gridx = 0;
		gbc_chckbxExtractFromSlackSpace.gridy = 3;
		deletedRecoverySettings.add(chckbxExtractFromSlackSpace, gbc_chckbxExtractFromSlackSpace);
		
		chckbxIndexUnallocatedSpace = new JCheckBox("Index unallocated space");
		GridBagConstraints gbc_chckbxIndexUnallocatedSpace = new GridBagConstraints();
		gbc_chckbxIndexUnallocatedSpace.gridwidth = 2;
		gbc_chckbxIndexUnallocatedSpace.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIndexUnallocatedSpace.anchor = GridBagConstraints.WEST;
		gbc_chckbxIndexUnallocatedSpace.gridx = 0;
		gbc_chckbxIndexUnallocatedSpace.gridy = 4;
		deletedRecoverySettings.add(chckbxIndexUnallocatedSpace, gbc_chckbxIndexUnallocatedSpace);
		
		chckbxCarveFileSystem = new JCheckBox("Carve file system unallocated space");
		GridBagConstraints gbc_chckbxCarveFileSystem = new GridBagConstraints();
		gbc_chckbxCarveFileSystem.gridwidth = 2;
		gbc_chckbxCarveFileSystem.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxCarveFileSystem.anchor = GridBagConstraints.WEST;
		gbc_chckbxCarveFileSystem.gridx = 0;
		gbc_chckbxCarveFileSystem.gridy = 5;
		deletedRecoverySettings.add(chckbxCarveFileSystem, gbc_chckbxCarveFileSystem);
		
		JLabel lblCarvingBlockSize = new JLabel("Carving block size (in bytes):");
		GridBagConstraints gbc_lblCarvingBlockSize = new GridBagConstraints();
		gbc_lblCarvingBlockSize.insets = new Insets(0, 0, 0, 5);
		gbc_lblCarvingBlockSize.anchor = GridBagConstraints.EAST;
		gbc_lblCarvingBlockSize.gridx = 0;
		gbc_lblCarvingBlockSize.gridy = 6;
		deletedRecoverySettings.add(lblCarvingBlockSize, gbc_lblCarvingBlockSize);
		
		txtCarvingBlockSize = new JTextField();
		GridBagConstraints gbc_txtCarvingBlockSize = new GridBagConstraints();
		gbc_txtCarvingBlockSize.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtCarvingBlockSize.gridx = 1;
		gbc_txtCarvingBlockSize.gridy = 6;
		deletedRecoverySettings.add(txtCarvingBlockSize, gbc_txtCarvingBlockSize);
		txtCarvingBlockSize.setColumns(10);
		
		JPanel familyTextSettings = new JPanel();
		familyTextSettings.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Family Text Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		GridBagConstraints gbc_familyTextSettings = new GridBagConstraints();
		gbc_familyTextSettings.insets = new Insets(0, 0, 5, 0);
		gbc_familyTextSettings.fill = GridBagConstraints.BOTH;
		gbc_familyTextSettings.gridx = 0;
		gbc_familyTextSettings.gridy = 2;
		leftPanel.add(familyTextSettings, gbc_familyTextSettings);
		GridBagLayout gbl_familyTextSettings = new GridBagLayout();
		gbl_familyTextSettings.columnWidths = new int[]{0, 0};
		gbl_familyTextSettings.rowHeights = new int[]{0, 0, 0};
		gbl_familyTextSettings.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_familyTextSettings.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		familyTextSettings.setLayout(gbl_familyTextSettings);
		
		chckbxCreateFamilySearchFields = new JCheckBox("Create family search fields for top level items");
		GridBagConstraints gbc_chckbxCreateFamilySearchFields = new GridBagConstraints();
		gbc_chckbxCreateFamilySearchFields.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxCreateFamilySearchFields.anchor = GridBagConstraints.WEST;
		gbc_chckbxCreateFamilySearchFields.gridx = 0;
		gbc_chckbxCreateFamilySearchFields.gridy = 0;
		familyTextSettings.add(chckbxCreateFamilySearchFields, gbc_chckbxCreateFamilySearchFields);
		
		chckbxHideImmaterialItems = new JCheckBox("Hide immaterial items (text rolled up to parent)");
		GridBagConstraints gbc_chckbxHideImmaterialItems = new GridBagConstraints();
		gbc_chckbxHideImmaterialItems.anchor = GridBagConstraints.WEST;
		gbc_chckbxHideImmaterialItems.gridx = 0;
		gbc_chckbxHideImmaterialItems.gridy = 1;
		familyTextSettings.add(chckbxHideImmaterialItems, gbc_chckbxHideImmaterialItems);
		
		JPanel textIndexingSettings = new JPanel();
		textIndexingSettings.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Text Indexing Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		GridBagConstraints gbc_textIndexingSettings = new GridBagConstraints();
		gbc_textIndexingSettings.insets = new Insets(0, 0, 5, 0);
		gbc_textIndexingSettings.fill = GridBagConstraints.BOTH;
		gbc_textIndexingSettings.gridx = 0;
		gbc_textIndexingSettings.gridy = 3;
		leftPanel.add(textIndexingSettings, gbc_textIndexingSettings);
		GridBagLayout gbl_textIndexingSettings = new GridBagLayout();
		gbl_textIndexingSettings.columnWidths = new int[]{0, 0, 0};
		gbl_textIndexingSettings.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_textIndexingSettings.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_textIndexingSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		textIndexingSettings.setLayout(gbl_textIndexingSettings);
		
		JLabel lblAnalysisLanguage = new JLabel("Analysis Language:");
		GridBagConstraints gbc_lblAnalysisLanguage = new GridBagConstraints();
		gbc_lblAnalysisLanguage.insets = new Insets(0, 0, 5, 5);
		gbc_lblAnalysisLanguage.anchor = GridBagConstraints.EAST;
		gbc_lblAnalysisLanguage.gridx = 0;
		gbc_lblAnalysisLanguage.gridy = 0;
		textIndexingSettings.add(lblAnalysisLanguage, gbc_lblAnalysisLanguage);
		
		comboAnalysisLanguage = new JComboBox<ComboChoice>();
		GridBagConstraints gbc_comboAnalysisLanguage = new GridBagConstraints();
		gbc_comboAnalysisLanguage.insets = new Insets(0, 0, 5, 0);
		gbc_comboAnalysisLanguage.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboAnalysisLanguage.gridx = 1;
		gbc_comboAnalysisLanguage.gridy = 0;
		textIndexingSettings.add(comboAnalysisLanguage, gbc_comboAnalysisLanguage);
		
		chckbxUseStopWords = new JCheckBox("Use stop words");
		GridBagConstraints gbc_chckbxUseStopWords = new GridBagConstraints();
		gbc_chckbxUseStopWords.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxUseStopWords.anchor = GridBagConstraints.WEST;
		gbc_chckbxUseStopWords.gridwidth = 2;
		gbc_chckbxUseStopWords.gridx = 0;
		gbc_chckbxUseStopWords.gridy = 1;
		textIndexingSettings.add(chckbxUseStopWords, gbc_chckbxUseStopWords);
		
		chckbxUseStemming = new JCheckBox("Use stemming");
		GridBagConstraints gbc_chckbxUseStemming = new GridBagConstraints();
		gbc_chckbxUseStemming.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxUseStemming.anchor = GridBagConstraints.WEST;
		gbc_chckbxUseStemming.gridwidth = 2;
		gbc_chckbxUseStemming.gridx = 0;
		gbc_chckbxUseStemming.gridy = 2;
		textIndexingSettings.add(chckbxUseStemming, gbc_chckbxUseStemming);
		
		chckbxEnableExactQueries = new JCheckBox("Enable exact queries");
		GridBagConstraints gbc_chckbxEnableExactQueries = new GridBagConstraints();
		gbc_chckbxEnableExactQueries.anchor = GridBagConstraints.WEST;
		gbc_chckbxEnableExactQueries.gridwidth = 2;
		gbc_chckbxEnableExactQueries.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxEnableExactQueries.gridx = 0;
		gbc_chckbxEnableExactQueries.gridy = 3;
		textIndexingSettings.add(chckbxEnableExactQueries, gbc_chckbxEnableExactQueries);
		
		JPanel rightPanel = new JPanel();
		GridBagConstraints gbc_rightPanel = new GridBagConstraints();
		gbc_rightPanel.fill = GridBagConstraints.BOTH;
		gbc_rightPanel.gridx = 1;
		gbc_rightPanel.gridy = 0;
		mainSettingsPanel.add(rightPanel, gbc_rightPanel);
		GridBagLayout gbl_rightPanel = new GridBagLayout();
		gbl_rightPanel.columnWidths = new int[]{0, 0};
		gbl_rightPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_rightPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_rightPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		rightPanel.setLayout(gbl_rightPanel);
		
		JPanel itemContentSettings = new JPanel();
		itemContentSettings.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Item Content Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		GridBagConstraints gbc_itemContentSettings = new GridBagConstraints();
		gbc_itemContentSettings.insets = new Insets(0, 0, 5, 0);
		gbc_itemContentSettings.fill = GridBagConstraints.BOTH;
		gbc_itemContentSettings.gridx = 0;
		gbc_itemContentSettings.gridy = 0;
		rightPanel.add(itemContentSettings, gbc_itemContentSettings);
		GridBagLayout gbl_itemContentSettings = new GridBagLayout();
		gbl_itemContentSettings.columnWidths = new int[]{30, 0, 0};
		gbl_itemContentSettings.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_itemContentSettings.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_itemContentSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		itemContentSettings.setLayout(gbl_itemContentSettings);
		
		chckbxProcessText = new JCheckBox("Process text");
		GridBagConstraints gbc_chckbxProcessText = new GridBagConstraints();
		gbc_chckbxProcessText.gridwidth = 2;
		gbc_chckbxProcessText.anchor = GridBagConstraints.WEST;
		gbc_chckbxProcessText.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxProcessText.gridx = 0;
		gbc_chckbxProcessText.gridy = 0;
		itemContentSettings.add(chckbxProcessText, gbc_chckbxProcessText);
		
		chckbxEnableNearDuplicates = new JCheckBox("Enable near-duplicates");
		GridBagConstraints gbc_chckbxEnableNearDuplicates = new GridBagConstraints();
		gbc_chckbxEnableNearDuplicates.gridwidth = 2;
		gbc_chckbxEnableNearDuplicates.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxEnableNearDuplicates.anchor = GridBagConstraints.WEST;
		gbc_chckbxEnableNearDuplicates.gridx = 0;
		gbc_chckbxEnableNearDuplicates.gridy = 1;
		itemContentSettings.add(chckbxEnableNearDuplicates, gbc_chckbxEnableNearDuplicates);
		
		chckbxEnableTextSummarisation = new JCheckBox("Enable text summarisation");
		GridBagConstraints gbc_chckbxEnableTextSummarisation = new GridBagConstraints();
		gbc_chckbxEnableTextSummarisation.gridwidth = 2;
		gbc_chckbxEnableTextSummarisation.anchor = GridBagConstraints.WEST;
		gbc_chckbxEnableTextSummarisation.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxEnableTextSummarisation.gridx = 0;
		gbc_chckbxEnableTextSummarisation.gridy = 2;
		itemContentSettings.add(chckbxEnableTextSummarisation, gbc_chckbxEnableTextSummarisation);
		
		chckbxExtractNamedEntities = new JCheckBox("Extract named entities from text");
		GridBagConstraints gbc_chckbxExtractNamedEntities = new GridBagConstraints();
		gbc_chckbxExtractNamedEntities.gridwidth = 2;
		gbc_chckbxExtractNamedEntities.anchor = GridBagConstraints.WEST;
		gbc_chckbxExtractNamedEntities.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxExtractNamedEntities.gridx = 0;
		gbc_chckbxExtractNamedEntities.gridy = 3;
		itemContentSettings.add(chckbxExtractNamedEntities, gbc_chckbxExtractNamedEntities);
		
		chckbxExtractNamedEntitiesFromTextStripped = new JCheckBox("Include text stripped items");
		chckbxExtractNamedEntitiesFromTextStripped.setEnabled(false);
		GridBagConstraints gbc_chckbxExtractNamedEntitiesFromTextStripped = new GridBagConstraints();
		gbc_chckbxExtractNamedEntitiesFromTextStripped.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxExtractNamedEntitiesFromTextStripped.anchor = GridBagConstraints.WEST;
		gbc_chckbxExtractNamedEntitiesFromTextStripped.gridx = 1;
		gbc_chckbxExtractNamedEntitiesFromTextStripped.gridy = 4;
		itemContentSettings.add(chckbxExtractNamedEntitiesFromTextStripped, gbc_chckbxExtractNamedEntitiesFromTextStripped);
		
		chckbxExtractNamedEntitiesFromProperties = new JCheckBox("Extract named entities from properties");
		GridBagConstraints gbc_chckbxExtractNamedEntitiesFromProperties = new GridBagConstraints();
		gbc_chckbxExtractNamedEntitiesFromProperties.gridwidth = 2;
		gbc_chckbxExtractNamedEntitiesFromProperties.anchor = GridBagConstraints.WEST;
		gbc_chckbxExtractNamedEntitiesFromProperties.gridx = 0;
		gbc_chckbxExtractNamedEntitiesFromProperties.gridy = 5;
		itemContentSettings.add(chckbxExtractNamedEntitiesFromProperties, gbc_chckbxExtractNamedEntitiesFromProperties);
		
		JPanel imageSettings = new JPanel();
		imageSettings.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Image Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		GridBagConstraints gbc_imageSettings = new GridBagConstraints();
		gbc_imageSettings.insets = new Insets(0, 0, 5, 0);
		gbc_imageSettings.fill = GridBagConstraints.BOTH;
		gbc_imageSettings.gridx = 0;
		gbc_imageSettings.gridy = 1;
		rightPanel.add(imageSettings, gbc_imageSettings);
		GridBagLayout gbl_imageSettings = new GridBagLayout();
		gbl_imageSettings.columnWidths = new int[]{0, 0};
		gbl_imageSettings.rowHeights = new int[]{0, 0, 0, 0};
		gbl_imageSettings.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_imageSettings.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		imageSettings.setLayout(gbl_imageSettings);
		
		chckbxGenerateThumbnails = new JCheckBox("Generate thumbnails for image data");
		GridBagConstraints gbc_chckbxGenerateThumbnails = new GridBagConstraints();
		gbc_chckbxGenerateThumbnails.anchor = GridBagConstraints.WEST;
		gbc_chckbxGenerateThumbnails.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxGenerateThumbnails.gridx = 0;
		gbc_chckbxGenerateThumbnails.gridy = 0;
		imageSettings.add(chckbxGenerateThumbnails, gbc_chckbxGenerateThumbnails);
		
		chckbxPerformSkinToneAnalysis = new JCheckBox("Perform image colour and skin-tone analysis");
		GridBagConstraints gbc_chckbxPerformSkinToneAnalysis = new GridBagConstraints();
		gbc_chckbxPerformSkinToneAnalysis.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxPerformSkinToneAnalysis.anchor = GridBagConstraints.WEST;
		gbc_chckbxPerformSkinToneAnalysis.gridx = 0;
		gbc_chckbxPerformSkinToneAnalysis.gridy = 1;
		imageSettings.add(chckbxPerformSkinToneAnalysis, gbc_chckbxPerformSkinToneAnalysis);
		
		chckbxDetectFaces = new JCheckBox("Detect faces");
		GridBagConstraints gbc_chckbxDetectFaces = new GridBagConstraints();
		gbc_chckbxDetectFaces.anchor = GridBagConstraints.WEST;
		gbc_chckbxDetectFaces.gridx = 0;
		gbc_chckbxDetectFaces.gridy = 2;
		imageSettings.add(chckbxDetectFaces, gbc_chckbxDetectFaces);
		
		JPanel digestSettings = new JPanel();
		digestSettings.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Digest Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		GridBagConstraints gbc_digestSettings = new GridBagConstraints();
		gbc_digestSettings.insets = new Insets(0, 0, 5, 0);
		gbc_digestSettings.fill = GridBagConstraints.BOTH;
		gbc_digestSettings.gridx = 0;
		gbc_digestSettings.gridy = 2;
		rightPanel.add(digestSettings, gbc_digestSettings);
		GridBagLayout gbl_digestSettings = new GridBagLayout();
		gbl_digestSettings.columnWidths = new int[]{20, 0, 0, 0};
		gbl_digestSettings.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_digestSettings.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_digestSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		digestSettings.setLayout(gbl_digestSettings);
		
		JLabel lblDigestsToCompute = new JLabel("Digests to compute:");
		GridBagConstraints gbc_lblDigestsToCompute = new GridBagConstraints();
		gbc_lblDigestsToCompute.insets = new Insets(0, 0, 5, 5);
		gbc_lblDigestsToCompute.anchor = GridBagConstraints.WEST;
		gbc_lblDigestsToCompute.gridwidth = 2;
		gbc_lblDigestsToCompute.gridx = 0;
		gbc_lblDigestsToCompute.gridy = 0;
		digestSettings.add(lblDigestsToCompute, gbc_lblDigestsToCompute);
		
		chckbxMd5 = new JCheckBox("MD5");
		chckbxMd5.setSelected(true);
		chckbxMd5.setEnabled(false);
		GridBagConstraints gbc_chckbxMd5 = new GridBagConstraints();
		gbc_chckbxMd5.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxMd5.anchor = GridBagConstraints.WEST;
		gbc_chckbxMd5.gridx = 1;
		gbc_chckbxMd5.gridy = 1;
		digestSettings.add(chckbxMd5, gbc_chckbxMd5);
		
		chckbxSha1 = new JCheckBox("SHA-1");
		GridBagConstraints gbc_chckbxSha1 = new GridBagConstraints();
		gbc_chckbxSha1.anchor = GridBagConstraints.WEST;
		gbc_chckbxSha1.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSha1.gridx = 1;
		gbc_chckbxSha1.gridy = 2;
		digestSettings.add(chckbxSha1, gbc_chckbxSha1);
		
		chckbxSha256 = new JCheckBox("SHA-256");
		GridBagConstraints gbc_chckbxSha256 = new GridBagConstraints();
		gbc_chckbxSha256.anchor = GridBagConstraints.WEST;
		gbc_chckbxSha256.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSha256.gridx = 1;
		gbc_chckbxSha256.gridy = 3;
		digestSettings.add(chckbxSha256, gbc_chckbxSha256);
		
		chckbxSsdeep = new JCheckBox("SSDeep");
		GridBagConstraints gbc_chckbxSsdeep = new GridBagConstraints();
		gbc_chckbxSsdeep.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSsdeep.anchor = GridBagConstraints.WEST;
		gbc_chckbxSsdeep.gridx = 1;
		gbc_chckbxSsdeep.gridy = 4;
		digestSettings.add(chckbxSsdeep, gbc_chckbxSsdeep);
		
		JLabel lblMaximumDigestSize = new JLabel("Maximum digest size (MB):");
		GridBagConstraints gbc_lblMaximumDigestSize = new GridBagConstraints();
		gbc_lblMaximumDigestSize.anchor = GridBagConstraints.WEST;
		gbc_lblMaximumDigestSize.gridwidth = 2;
		gbc_lblMaximumDigestSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaximumDigestSize.gridx = 0;
		gbc_lblMaximumDigestSize.gridy = 5;
		digestSettings.add(lblMaximumDigestSize, gbc_lblMaximumDigestSize);
		
		spinnerMaxDigestSize = new JSpinner();
		spinnerMaxDigestSize.setModel(new SpinnerNumberModel(new Integer(250), new Integer(0), null, new Integer(1)));
		GridBagConstraints gbc_spinnerMaxDigestSize = new GridBagConstraints();
		gbc_spinnerMaxDigestSize.insets = new Insets(0, 0, 5, 0);
		gbc_spinnerMaxDigestSize.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerMaxDigestSize.gridx = 2;
		gbc_spinnerMaxDigestSize.gridy = 5;
		digestSettings.add(spinnerMaxDigestSize, gbc_spinnerMaxDigestSize);
		
		JPanel emailDigestSettings = new JPanel();
		emailDigestSettings.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Email Digest Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5))));
		GridBagConstraints gbc_emailDigestSettings = new GridBagConstraints();
		gbc_emailDigestSettings.gridwidth = 3;
		gbc_emailDigestSettings.fill = GridBagConstraints.BOTH;
		gbc_emailDigestSettings.gridx = 0;
		gbc_emailDigestSettings.gridy = 6;
		digestSettings.add(emailDigestSettings, gbc_emailDigestSettings);
		GridBagLayout gbl_emailDigestSettings = new GridBagLayout();
		gbl_emailDigestSettings.columnWidths = new int[]{0, 0};
		gbl_emailDigestSettings.rowHeights = new int[]{0, 0, 0};
		gbl_emailDigestSettings.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_emailDigestSettings.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		emailDigestSettings.setLayout(gbl_emailDigestSettings);
		
		chckbxDigestIncludeBcc = new JCheckBox("Include Bcc");
		GridBagConstraints gbc_chckbxDigestIncludeBcc = new GridBagConstraints();
		gbc_chckbxDigestIncludeBcc.anchor = GridBagConstraints.WEST;
		gbc_chckbxDigestIncludeBcc.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxDigestIncludeBcc.gridx = 0;
		gbc_chckbxDigestIncludeBcc.gridy = 0;
		emailDigestSettings.add(chckbxDigestIncludeBcc, gbc_chckbxDigestIncludeBcc);
		
		chckbxDigestIncludeItemDate = new JCheckBox("Include Item Date");
		GridBagConstraints gbc_chckbxDigestIncludeItemDate = new GridBagConstraints();
		gbc_chckbxDigestIncludeItemDate.anchor = GridBagConstraints.WEST;
		gbc_chckbxDigestIncludeItemDate.gridx = 0;
		gbc_chckbxDigestIncludeItemDate.gridy = 1;
		emailDigestSettings.add(chckbxDigestIncludeItemDate, gbc_chckbxDigestIncludeItemDate);
		
		initialize();
		initDataBindings();
	}
	
	private void initialize(){
		defineTraversalChoices();
		buildSettingControlMaps();
	}
	
	private void defineTraversalChoices(){
		traversalChoices.add(new ComboChoice("Full traversal","full_traversal"));
		traversalChoices.add(new ComboChoice("Process loose files and forensic images but not their contents","loose_files_and_forensic_images"));
		traversalChoices.add(new ComboChoice("Process loose files but not their contents","loose_files"));
		
		for(ComboChoice choice : traversalChoices){
			comboTraversal.addItem(choice);			
		}
		
		languageChoices.add(new ComboChoice("English","en"));
		languageChoices.add(new ComboChoice("Japanese","ja"));
		
		for(ComboChoice choice : languageChoices){
			comboAnalysisLanguage.addItem(choice);
		}
	}
	
	private void buildSettingControlMaps(){
		//Map check boxes
		booleanSettingsMap.put("addBccToEmailDigests",chckbxDigestIncludeBcc);
		booleanSettingsMap.put("addCommunicationDateToEmailDigests",chckbxDigestIncludeItemDate);
		booleanSettingsMap.put("calculateAuditedSize",chckbxCalculateAuditedSize);
		booleanSettingsMap.put("calculateSSDeepFuzzyHash",chckbxSsdeep);
		booleanSettingsMap.put("carveFileSystemUnallocatedSpace",chckbxCarveFileSystem);
		booleanSettingsMap.put("createThumbnails",chckbxGenerateThumbnails);
		booleanSettingsMap.put("detectFaces",chckbxDetectFaces);
		booleanSettingsMap.put("enableExactQueries",chckbxEnableExactQueries);
		booleanSettingsMap.put("extractEndOfFileSlackSpace",chckbxExtractEndOfFileSlackSpace);
		booleanSettingsMap.put("extractFromSlackSpace",chckbxExtractFromSlackSpace);
		booleanSettingsMap.put("extractNamedEntitiesFromProperties",chckbxExtractNamedEntitiesFromProperties);
		booleanSettingsMap.put("extractNamedEntitiesFromText",chckbxExtractNamedEntities);
		booleanSettingsMap.put("extractNamedEntitiesFromTextStripped",chckbxExtractNamedEntitiesFromTextStripped);
		booleanSettingsMap.put("extractShingles",chckbxEnableNearDuplicates);
		booleanSettingsMap.put("hideEmbeddedImmaterialData",chckbxHideImmaterialItems);
		booleanSettingsMap.put("identifyPhysicalFiles",chckbxPerformItemIdentification);
		booleanSettingsMap.put("processFamilyFields",chckbxCreateFamilySearchFields);
		booleanSettingsMap.put("processText",chckbxProcessText);
		booleanSettingsMap.put("processTextSummaries",chckbxEnableTextSummarisation);
		booleanSettingsMap.put("recoverDeletedFiles",chckbxRecoverDeletedFiles);
		booleanSettingsMap.put("reuseEvidenceStores",chckbxReuseEvidenceStores);
		booleanSettingsMap.put("skinToneAnalysis",chckbxPerformSkinToneAnalysis);
		booleanSettingsMap.put("smartProcessRegistry",chckbxSmartProcessRegistry);
		booleanSettingsMap.put("stemming",chckbxUseStemming);
		booleanSettingsMap.put("stopWords",chckbxUseStopWords);
		booleanSettingsMap.put("storeBinary",chckbxStoreBinary);
		booleanSettingsMap.put("carveUnidentifiedData",chckbxIndexUnallocatedSpace);
		
		// Map spinners
		intSettingsMap.put("maxStoredBinarySize",spinnerMaxBinarySize);
		intSettingsMap.put("maxDigestSize",spinnerMaxDigestSize);
	}
	
	/***
	 * Gets the settings represented by this control as a Map which could be passed directly to Nuix
	 * via Processing.setProcessingSettings
	 * @return A Map of processing settings compatible with Processing.setProcessingSettings 
	 */
	public Map<String,Object> getSettings(){
		Map<String,Object> result = new TreeMap<String,Object>();
		
		// Bulk add bools
		for(Map.Entry<String,JCheckBox> boolEntry : booleanSettingsMap.entrySet()){
			result.put(boolEntry.getKey(),boolEntry.getValue().isSelected());
		}
		
		//reportProcessingStatus is special
		result.put("reportPrcessingStatus",chckbxCalculateProcessingSize.isSelected() ? "physical_files" : "none");
		
		//Bulk add ints
		for(Map.Entry<String,JSpinner> intEntry : intSettingsMap.entrySet()){
			result.put(intEntry.getKey(),(Integer)intEntry.getValue().getValue() * (1000 * 1000));
		}
		
		//Digests
		List<String> digestSettings = new ArrayList<String>();
		boolean md5 = chckbxMd5.isSelected();
		boolean sha1 = chckbxSha1.isSelected();
		boolean sha256 = chckbxSha256.isSelected();
		
		if (md5){digestSettings.add("MD5");}
		if (sha1){digestSettings.add("SHA-1");}
		if (sha256){digestSettings.add("SHA-256");}
		
		result.put("digests",digestSettings);
		
		//Traversal
		String traversalSetting = ((ComboChoice)comboTraversal.getSelectedItem()).settingName;
		result.put("traversalScope",traversalSetting);
		
		//Language
		String languageSetting = ((ComboChoice)comboAnalysisLanguage.getSelectedItem()).settingName;
		result.put("analysisLanguage",languageSetting);
		
		//Block size
		if(txtCarvingBlockSize.getText().length() > 0){
			int carvingBlockSize = Integer.parseInt(txtCarvingBlockSize.getText());
			result.put("carvingBlockSize",carvingBlockSize);
		}
		
		return result;
	}
	
	/***
	 * Loads settings Map into the control.  Map format should be compatible with Processor.setProcessingSettings
	 * @param settings A Map of processing settings compatible with Processor.setProcessingSettings
	 */
	@SuppressWarnings("unchecked")
	public void loadSettings(Map<String,Object> settings){
		clearSettings();
		for(Map.Entry<String,Object> entry : settings.entrySet()){
			if(booleanSettingsMap.containsKey(entry.getKey())){
				booleanSettingsMap.get(entry.getKey()).setSelected((Boolean)entry.getValue());
			} else if(entry.getKey().equals("reportProcessingStatus")){
				chckbxCalculateProcessingSize.setSelected(((String)entry.getValue()).equalsIgnoreCase("physical_files"));
			} else if(intSettingsMap.containsKey(entry.getKey())){
				Object mapValue = entry.getValue();
				Double value = null;
				if(mapValue instanceof Double){
					value = (Double)entry.getValue();
				} else if (mapValue instanceof Integer){
					value = new Double((Integer)entry.getValue());
				} else if (mapValue instanceof Long){
					value = new Double((Long)entry.getValue());
				}
				intSettingsMap.get(entry.getKey()).setValue(value.intValue() / (1000 * 1000));
			} else if (entry.getKey().equals("digests")){
				List<String> digests = (List<String>)entry.getValue();
				for(String digest : digests){
					if(digest.equals("MD5")){ chckbxMd5.setSelected(true); }
					else if (digest.equals("SHA-1")){ chckbxSha1.setSelected(true); }
					else if (digest.equals("SHA-256")){ chckbxSha256.setSelected(true); }
				}
			} else if (entry.getKey().equals("traversalScope")){
				String traversalSetting = (String)entry.getValue();
				for(ComboChoice possibleChoice : traversalChoices){
					if(traversalSetting.equals(possibleChoice.settingName)){
						comboTraversal.setSelectedItem(possibleChoice);
						break;
					}
				}
			} else if (entry.getKey().equals("analysisLanguage")){
				String languageSetting = (String)entry.getValue();
				for(ComboChoice possibleChoice : languageChoices){
					if(languageSetting.equals(possibleChoice.settingName)){
						comboAnalysisLanguage.setSelectedItem(possibleChoice);
						break;
					}
				}
			}else if (entry.getKey().equals("carvingBlockSize")){
				Double blockSize = (Double)entry.getValue();
				txtCarvingBlockSize.setText(blockSize.intValue()+"");
			}
		}
	}
	
	/***
	 * Gets the settings represented by this control as a JSON string
	 * @return The settings as a JSON string
	 */
	public String getSettingsJSON(){
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.toJson(getSettings());
	}
	
	/***
	 * Saves the settings represented by this control as a JSON file
	 * @param filePath The location to save the file to
	 * @throws Exception Thrown if something goes wrong
	 */
	public void saveJSONFile(File filePath) throws Exception {
		FileWriter fw = null;
		PrintWriter pw = null;
		try{
			fw = new FileWriter(filePath);
			pw = new PrintWriter(fw);
			pw.print(getSettingsJSON());
		}catch(Exception exc){
			throw exc;
		}
		finally{
			try {
				fw.close();
			} catch (IOException e) {}
			pw.close();
		}
	}
	
	/***
	 * Saves the settings represented by this control as a JSON file
	 * @param filePath The location to save the file to
	 * @throws Exception Thrown if something goes wrong
	 */
	public void saveJSONFile(String filePath) throws Exception {
		saveJSONFile(new File(filePath));
	}
	
	/***
	 * Loads settings into the control from a JSON string
	 * @param json The JSON string of settings to load
	 */
	public void loadSettingsJSON(String json){
		Gson gson = new Gson();
		Map<String,Object> settings = gson.fromJson(json,new TypeToken<Map<String,Object>>(){}.getType());
		loadSettings(settings);
	}
	
	/***
	 * Loads settings into the control from a JSON file
	 * @param filePath The location of the file to load
	 * @throws Exception Thrown if something goes wrong
	 */
	public void loadSettingsJSONFile(String filePath) throws Exception{
		List<String> lines = Files.readAllLines(Paths.get(filePath));
		String json = Joiner.on("\n").join(lines);
		loadSettingsJSON(json);
	}
	
	/***
	 * Loads settings into the control from a JSON file
	 * @param filePath The location of the file to load
	 * @throws Exception Thrown if something goes wrong
	 */
	public void loadSettingsJSONFile(File filePath) throws Exception{
		loadSettingsJSONFile(filePath.getPath());
	}
	
	/***
	 * Clears the settings of this control.  All check boxes are unchecked, max binary size
	 * is set to 1000MB, max digest size is set to 250MB, 
	 */
	public void clearSettings(){
		// Bulk add bools
		for(Map.Entry<String,JCheckBox> boolEntry : booleanSettingsMap.entrySet()){
			boolEntry.getValue().setSelected(false);
		}
		
		chckbxCalculateProcessingSize.setSelected(false);
		
		//Reset Ints
		spinnerMaxBinarySize.setValue(1000);
		spinnerMaxDigestSize.setValue(250);
//		for(Map.Entry<String,JSpinner> intEntry : intSettingsMap.entrySet()){
//			intEntry.getValue().setValue(0);
//		}
		
		//Digests
		chckbxMd5.setSelected(true);
		chckbxSha1.setSelected(false);
		chckbxSha256.setSelected(false);
		
		//Traversal
		comboTraversal.setSelectedIndex(0);
		
		//Language
		comboAnalysisLanguage.setSelectedIndex(0);
		
		//Block size
		txtCarvingBlockSize.setText("");
	}
	
	public void loadDefaultSettings(){
		clearSettings();
		if(defaultSettings != null){
			loadSettings(defaultSettings);
		}
	}
	
	public Map<String, Object> getDefaultSettings() {
		return defaultSettings;
	}

	public void setDefaultSettings(Map<String, Object> defaultSettings) {
		this.defaultSettings = defaultSettings;
		loadDefaultSettings();
	}
	
	public void setDefaultSettingsFromJSON(String json) {
		Gson gson = new Gson();
		Map<String,Object> settings = gson.fromJson(json,new TypeToken<Map<String,Object>>(){}.getType());
		setDefaultSettings(settings);
	}
	
	public void setDefaultSettingsFromJSONFile(String filePath) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(filePath));
		String json = Joiner.on("\n").join(lines);
		setDefaultSettingsFromJSON(json);
	}
	
	public void setDefaultSettingsFromJSONFile(File filePath) throws Exception {
		setDefaultSettingsFromJSONFile(filePath.getPath());
	}

	protected void initDataBindings() {
		BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
		BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("enabled");
		AutoBinding<JCheckBox, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, chckbxExtractNamedEntities, jCheckBoxBeanProperty, chckbxExtractNamedEntitiesFromTextStripped, jCheckBoxBeanProperty_1);
		autoBinding.bind();
	}
	
	public void hideSaveLoadResetButtons(){
		btnSaveSettings.setVisible(false);
		btnLoadSettings.setVisible(false);
		btnResetSettings.setVisible(false);
	}
}
