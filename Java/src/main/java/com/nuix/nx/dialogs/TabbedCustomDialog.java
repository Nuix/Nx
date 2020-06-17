/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.dialogs;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXDatePicker;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nuix.nx.NuixConnection;
import com.nuix.nx.controls.BatchExporterLoadFileSettings;
import com.nuix.nx.controls.BatchExporterNativeSettings;
import com.nuix.nx.controls.BatchExporterPdfSettings;
import com.nuix.nx.controls.BatchExporterTextSettings;
import com.nuix.nx.controls.BatchExporterTraversalSettings;
import com.nuix.nx.controls.ChoiceTableControl;
import com.nuix.nx.controls.ComboItemBox;
import com.nuix.nx.controls.CsvTable;
import com.nuix.nx.controls.DynamicTableControl;
import com.nuix.nx.controls.LocalWorkerSettings;
import com.nuix.nx.controls.MultipleChoiceComboBox;
import com.nuix.nx.controls.OcrSettings;
import com.nuix.nx.controls.PathList;
import com.nuix.nx.controls.PathSelectionControl;
import com.nuix.nx.controls.StringList;
import com.nuix.nx.controls.models.Choice;
import com.nuix.nx.controls.models.ControlDeserializationHandler;
import com.nuix.nx.controls.models.ControlSerializationHandler;

/***
 * Allows you to build a settings dialog with multiple tabs.  Each tab is a {@link com.nuix.nx.dialogs.CustomTabPanel} which
 * suports easily adding various controls such as check boxes, text field, radio buttons and so on.
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class TabbedCustomDialog extends JDialog {
	private Map<String,CustomTabPanel> tabs = new LinkedHashMap<String,CustomTabPanel>();
	Map<String,Component> controls;
	private boolean dialogResult = false;
	private ValidationCallback validationCallback;
	private boolean stickySettingsEnabled = false;
	private String stickySettingsFilePath = "";
	private File helpFile = null;
	private String helpUrl = null;
	private String dateSerializationFormat = "yyyy-MM-dd HH:mm:ss";
	private SimpleDateFormat sdf = new SimpleDateFormat(dateSerializationFormat);
	
	private Map<String,ControlSerializationHandler> serializationHandlers = new HashMap<String,ControlSerializationHandler>();
	private Map<String,ControlDeserializationHandler> deserializationHandlers = new HashMap<String,ControlDeserializationHandler>();
	
	private JTabbedPane tabbedPane;
	private JButton btnOk;
	private JButton btnCancel;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmSaveSettings;
	private JMenuItem mntmLoadSettings;
	private JMenu mnHelp;
	private JMenuItem mntmViewHelp;
	private Runnable jsonFileLoadedCallback;
	
	/***
	 * Create a new instance.
	 */
	public TabbedCustomDialog() {
		 this("Script"); 
	}
	
	/***
	 * Create a new instance with the specified title.
	 * @param title The inital title of the dialog.
	 */
	public TabbedCustomDialog(String title) {
		super((JDialog)null);
		setTitle(title);
		setIconImage(Toolkit.getDefaultToolkit().getImage(TabbedCustomDialog.class.getResource("/com/nuix/nx/dialogs/nuix_icon.png")));
		controls = new HashMap<String,Component>();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setModal(true);
		setSize(new Dimension(1024,768));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{432, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		JPanel buttonsPanel = new JPanel();
		GridBagConstraints gbc_buttonsPanel = new GridBagConstraints();
		gbc_buttonsPanel.anchor = GridBagConstraints.EAST;
		gbc_buttonsPanel.gridx = 0;
		gbc_buttonsPanel.gridy = 1;
		getContentPane().add(buttonsPanel, gbc_buttonsPanel);
		
		btnOk = new JButton("Ok");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(TabbedCustomDialog.this.validationCallback != null){
					try{
						if(validationCallback.validate(TabbedCustomDialog.this.toMap()) == false){
							return;
						}
					}
					catch(Exception exc){
						System.out.println("Validation callback on TabbedCustomDialog threw an exception!");
						exc.printStackTrace();
					}
				}
				TabbedCustomDialog.this.dialogResult = true;
				if(stickySettingsEnabled){
					try {
						saveJsonFile(stickySettingsFilePath);
					} catch (Exception e) {
						
					}
				}
				TabbedCustomDialog.this.dispose();
			}
		});
		buttonsPanel.add(btnOk);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogResult = false;
				TabbedCustomDialog.this.dispose();
			}
		});
		buttonsPanel.add(btnCancel);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		mnFile.setIcon(new ImageIcon(TabbedCustomDialog.class.getResource("/com/nuix/nx/dialogs/page_white_text.png")));
		menuBar.add(mnFile);
		
		mntmSaveSettings = new JMenuItem("Save Settings...");
		mntmSaveSettings.setIcon(new ImageIcon(TabbedCustomDialog.class.getResource("/com/nuix/nx/dialogs/page_white_put.png")));
		mntmSaveSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File saveLocation = CommonDialogs.saveFileDialog("", "Settings JSON File", "json", "Save Settings to File");
				if(saveLocation != null){
					try {
						saveJsonFile(saveLocation.getPath());
					} catch (Exception e) {
						CommonDialogs.showError("Error while saving settings: "+e.getMessage());
						e.printStackTrace();
					}
				}
			}
		});
		mnFile.add(mntmSaveSettings);
		
		mntmLoadSettings = new JMenuItem("Load Settings...");
		mntmLoadSettings.setIcon(new ImageIcon(TabbedCustomDialog.class.getResource("/com/nuix/nx/dialogs/page_white_get.png")));
		mntmLoadSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File loadLocation = CommonDialogs.openFileDialog("", "Settings JSON File", "json", "Load Settings to File");
				if(loadLocation != null){
					try {
						loadJsonFile(loadLocation.getPath());
					} catch (IOException e1) {
						CommonDialogs.showError("Error while loading settings: " + e1.getMessage());
					}
				}
			}
		});
		mnFile.add(mntmLoadSettings);
		
		mnHelp = new JMenu("Help");
		mnHelp.setIcon(new ImageIcon(TabbedCustomDialog.class.getResource("/com/nuix/nx/dialogs/help.png")));
		mnHelp.setVisible(false);
		menuBar.add(mnHelp);
		
		mntmViewHelp = new JMenuItem("View Help");
		mntmViewHelp.setVisible(false);
		mntmViewHelp.setIcon(new ImageIcon(TabbedCustomDialog.class.getResource("/com/nuix/nx/dialogs/help.png")));
		mntmViewHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(helpFile != null){
					if(helpFile.exists()){
						try {
							Desktop.getDesktop().open(helpFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						CommonDialogs.showWarning("Could not find help file at: "+helpFile.getPath());
					}
				}
			}
		});
		mnHelp.add(mntmViewHelp);
		
		mntmViewOnlineHelp = new JMenuItem("View Online Help");
		mntmViewOnlineHelp.setVisible(false);
		mntmViewOnlineHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(helpUrl));
				} catch (IOException e1) {
					CommonDialogs.showError("Unable to open help page at "+helpUrl+"\n\n"+e1.getMessage());
				}
			}
		});
		mntmViewOnlineHelp.setIcon(new ImageIcon(TabbedCustomDialog.class.getResource("/com/nuix/nx/dialogs/help.png")));
		mnHelp.add(mntmViewOnlineHelp);
	}
	
	/***
	 * Displays this custom dialog.  Dialog is modal so this call will block caller until dialog is closed.  To determine
	 * whether user clicked "Ok", "Cancel" or closed the dialog, call {@link #getDialogResult()} afterwards.
	 */
	public void display(){
		dialogResult = false;
		if(stickySettingsEnabled){
			try{
				loadJsonFile(stickySettingsFilePath);
			}catch(Exception exc){}
		}
		
		if(helpFile != null && helpFile.exists()) {
			mnHelp.setVisible(true);
			mntmViewHelp.setVisible(true);
		}
			
		if(helpUrl != null && !helpUrl.trim().isEmpty()) {
			mnHelp.setVisible(true);
			mntmViewOnlineHelp.setVisible(true);
		}
		
		for(CustomTabPanel tab : tabs.values()){
			tab.addVerticalFillerAsNeeded();
			tabbedPane.addTab(tab.getLabel(), tab);
		}
		//pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/***
	 * Similar to {@link #display()} except that the dialog will be displayed non-modal and will invoke the provided callback
	 * when the dialog is closed and the result of {@link #getDialogResult()} returns true.  Note that invoking this from a
	 * script essentially behaves as asynchronous call from the script's perspective!
	 * @param callback The callback to invoke when the dialog is closed.
	 */
	public void displayNonModal(Consumer<Boolean> callback){
		dialogResult = false;
		if(stickySettingsEnabled){
			try{
				loadJsonFile(stickySettingsFilePath);
			}catch(Exception exc){}
		}
		
		if(helpFile != null && helpFile.exists()) {
			mnHelp.setVisible(true);
			mntmViewHelp.setVisible(true);
		}
			
		if(helpUrl != null && !helpUrl.trim().isEmpty()) {
			mnHelp.setVisible(true);
			mntmViewOnlineHelp.setVisible(true);
		}
		
		for(CustomTabPanel tab : tabs.values()){
			tab.addVerticalFillerAsNeeded();
			tabbedPane.addTab(tab.getLabel(), tab);
		}

		//Call users callback on successful close
		this.addWindowListener(new WindowListener(){

			@Override
			public void windowOpened(WindowEvent e) { /*ignored for this use*/  }

			@Override
			public void windowClosing(WindowEvent e) { /*ignored for this use*/  }

			@Override
			public void windowClosed(WindowEvent e) {
				if(getDialogResult() == true){
					callback.accept(getDialogResult());
				}
			}

			@Override
			public void windowIconified(WindowEvent e) { /*ignored for this use*/  }

			@Override
			public void windowDeiconified(WindowEvent e) { /*ignored for this use*/  }

			@Override
			public void windowActivated(WindowEvent e) { /*ignored for this use*/  }

			@Override
			public void windowDeactivated(WindowEvent e) { /*ignored for this use*/ }
		});
		
		setModal(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/***
	 * Resizes the dialog to fill the screen, less the specified margin on all sides.
	 * @param margins The margin size on all sides
	 */
	public void fillScreen(int margins){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension fillSize = new Dimension((int) screenSize.getWidth() - (margins * 2),
				(int) screenSize.getHeight() - (margins * 2));
		setSize(fillSize);
	}
	
	/***
	 * Gets the tab with the specified ID
	 * @param id The ID specified when the tab was created
	 * @return The tab associated with the specified ID
	 */
	public CustomTabPanel getTab(String id) {
		return tabs.get(id);
	}
	
	/***
	 * Adds a new tab to the dialog.
	 * @param id The case sensitive unique identifier used to reference this tab
	 * @param label The label for the tab
	 * @return A newly created tab associated to this dialog
	 */
	public CustomTabPanel addTab(String id, String label) {
		if(tabs.containsKey(id))
			throw new RuntimeException("Dialog already contains a tab with the ID: "+id);
		
		CustomTabPanel newTab = new CustomTabPanel(label,this);
		tabs.put(id,newTab);
		controls.put(id, newTab);
		return newTab;
	}
	
	public ScrollableCustomTabPanel addScrollableTab(String id, String label) {
		if(tabs.containsKey(id))
			throw new RuntimeException("Dialog already contains a tab with the ID: "+id);
		
		ScrollableCustomTabPanel newTab = new ScrollableCustomTabPanel(label,this);
		tabs.put(id,newTab);
		controls.put(id, newTab);
		return newTab;
	}
	
	/***
	 * Updates the label for given tab to a new value
	 * @param tabId Id assigned to the tab when it was added
	 * @param label The new label value for the tab
	 */
	public void setTabLabel(String tabId, String label){
		tabbedPane.setTitleAt(tabbedPane.indexOfComponent(getTab(tabId)), label);
	}
	
	/***
	 * Allows you to change the orientation of the tabs in the dialog by providing one of the
	 * JTabbedPane alignment constants
	 * @param tabPlacement A JTabbedPane alignment constant, such as JTabbedPane.LEFT
	 */
	public void setTabPlacement(int tabPlacement){
		tabbedPane.setTabPlacement(tabPlacement);
	}
	
	/***
	 * Changes the orientation of the dialogs tabs to be along the left side of the dialog
	 */
	public void setTabPlacementLeft(){
		setTabPlacement(JTabbedPane.LEFT);
	}

	/***
	 * Gets the result of showing the dialog.
	 * @return True if the user clicked the 'Ok' button.  False if otherwise ('Cancel' button or closed the dialog).
	 */
	public boolean getDialogResult() {
		return dialogResult;
	}
	
	/***
	 * Returns a Map of the control values.
	 * @return Map where the assigned identifier is the key and the control's value is the value.
	 */
	public Map<String,Object> toMap(){
		return toMap(false);
	}
	
	/***
	 * Returns a Map of the control values.
	 * @param forJsonCreation Set to true if the output is intended to be serialized to JSON.  Some
	 * of the values in the map may be generated differently to better cooperate with what JSON
	 * is capable of storing.
	 * @return Map where the assigned identifier is the key and the control's value is the value.
	 */
	public Map<String,Object> toMap(boolean forJsonCreation){
		Map<String,Object> result = new HashMap<String,Object>();
		for(CustomTabPanel tab : tabs.values()){
			result.putAll(tab.toMap(forJsonCreation));
		}
		return result;
	}
	
	/***
	 * Registers an event handle such that a given control is only enabled when another checkable control is checked.
	 * @param dependentControlIdentifier The identifier of the already added control for which the enabled state depends on another checkable control.
	 * @param targetCheckableIdentifier The identifier of the already added checkable control which will determine the enabled state of the dependent control.
	 * @throws Exception This could be caused by various things such as invalid identifiers or the identifier provided in targetCheckableIdentifier
	 * does not point to a checkable control (CheckBox or RadioButton).
	 */
	public void enabledOnlyWhenChecked(String dependentControlIdentifier, String targetCheckableIdentifier) throws Exception{
		Component dependentComponent = controls.get(dependentControlIdentifier);
		Component targetComponent = controls.get(targetCheckableIdentifier);
		boolean currentCheckedState = isChecked(targetCheckableIdentifier);
		dependentComponent.setEnabled(currentCheckedState);
		((java.awt.ItemSelectable)targetComponent).addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				try {
					controls.get(dependentControlIdentifier).setEnabled(isChecked(targetCheckableIdentifier));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/***
	 * Registers an event handle such that a given control is only enabled when another checkable control is not checked.
	 * @param dependentControlIdentifier The identifier of the already added control for which the enabled state depends on another checkable control.
	 * @param targetCheckableIdentifier The identifier of the already added checkable control which will determine the enabled state of the dependent control.
	 * @throws Exception This could be caused by various things such as invalid identifiers or the identifier provided in targetCheckableIdentifier
	 * does not point to a checkable control (CheckBox or RadioButton).
	 */
	public void enabledOnlyWhenNotChecked(String dependentControlIdentifier, String targetCheckableIdentifier) throws Exception{
		Component dependentComponent = controls.get(dependentControlIdentifier);
		Component targetComponent = controls.get(targetCheckableIdentifier);
		boolean currentCheckedState = isChecked(targetCheckableIdentifier);
		dependentComponent.setEnabled(!currentCheckedState);
		((java.awt.ItemSelectable)targetComponent).addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				try {
					controls.get(dependentControlIdentifier).setEnabled(!isChecked(targetCheckableIdentifier));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/***
	 * Gets whether a particular Checkbox or RadioButton is checked.
	 * @param identifier The unique identifier assigned to the control when it was appended to this dialog.
	 * @return True if the control is checked, false otherwise.
	 * @throws Exception Thrown if identifier is invalid or identifier does not refer to a Checkbox or RadioButton.
	 */
	public boolean isChecked(String identifier) throws Exception{
		Component component = controls.get(identifier);
		if(component instanceof JCheckBox){
			return ((JCheckBox)controls.get(identifier)).isSelected();	
		}
		else if(component instanceof JRadioButton){
			return ((JRadioButton)controls.get(identifier)).isSelected();
		}
		else
			throw new Exception("Control for identifier '"+identifier+"' is not a JCheckbox or JRadioButton");
	}
	
	/***
	 * Sets whether a particular Checkbox or RadioButton is checked.
	 * @param identifier The unique identifier assigned to the control when it was appended to this dialog.
	 * @param isChecked True to check the control, false to uncheck the control.
	 * @throws Exception Thrown if identifier is invalid or identifier does not refer to a Checkbox or RadioButton.
	 */
	public void setChecked(String identifier, boolean isChecked) throws Exception{
		Component component = controls.get(identifier);
		if(component instanceof JCheckBox){
			((JCheckBox)controls.get(identifier)).setSelected(isChecked);
		}
		else if(component instanceof JRadioButton){
			((JRadioButton)controls.get(identifier)).setSelected(isChecked);
		}
		else
			throw new Exception("Control for identifier '"+identifier+"' is not a JCheckbox or JRadioButton");
	}
	
	/***
	 * Gets the text present in a TextField or PasswordField.
	 * @param identifier The unique identifier assigned to the control when it was appended to this dialog.
	 * @return The text present in the control.
	 * @throws Exception Thrown if identifier is invalid or identifier does not refer to a TextField or PasswordField.
	 */
	public String getText(String identifier) throws Exception{
		Component component = controls.get(identifier);
		if(component instanceof JPasswordField){
			return ((JPasswordField)component).getPassword().toString();
		}
		else if (component instanceof JTextField){
			return ((JTextField)component).getText();
		}
		else if(component instanceof PathSelectionControl){
			return ((PathSelectionControl)component).getPath();
		}
		else if(component instanceof JComboBox<?>){
			return (String)((JComboBox<?>)component).getSelectedItem();
		}
		else if(component instanceof JTextArea){
			return ((JTextArea)component).getText();
		}
		else
			throw new Exception("Control for identifier '"+identifier+"' is not a supported control type: JPasswordField, JTextField, PathSelectionControl, JTextArea");
	}
	
	/***
	 * Sets the text present in a TextField or PasswordField.
	 * @param identifier The unique identifier assigned to the control when it was appended to this dialog.
	 * @param text The text value to set.
	 * @throws Exception Thrown if identifier is invalid or identifier does not refer to a TextField or PasswordField.
	 */
	public void setText(String identifier, String text) throws Exception{
		Component component = controls.get(identifier);
		if(component instanceof JPasswordField){
			((JPasswordField)component).setText(text);
		}
		else if (component instanceof JTextField){
			((JTextField)component).setText(text);
		}
		else if(component instanceof PathSelectionControl){
			((PathSelectionControl)component).setPath(text);
		}
		else if(component instanceof ComboItemBox){
			((ComboItemBox)component).setSelectedValue(text);
		}
		else if(component instanceof JComboBox<?>){
			((JComboBox<?>)component).setSelectedItem(text);
		}
		else if (component instanceof JTextArea){
			((JTextArea)component).setText(text);
		}
		else
			throw new Exception("Control for identifier '"+identifier+"' is not a supported control type: JPasswordField, JTextField, PathSelectionControl, JTextArea");
	}
	
	/***
	 * Allows you to get the actual Java Swing control.  You will likely need to cast it to the appropriate type before use.
	 * @param identifier The unique identifier assigned to the control when it was appended to this dialog.
	 * @return The control as base class Component.  See documentation for various append methods for control types.
	 */
	public Component getControl(String identifier){
		return controls.get(identifier);
	}
	
	/***
	 * This enables "sticky settings" where the dialog will save a JSON file of settings when 'Okay' is clicked and will attempt to
	 * load previously saved settings when the dialog is displayed.  This currently only works with controls added to the dialog which
	 * support {@link setText} and {@link setChecked}.  See {@link #toJson} and {@link #loadJson}.
	 * @param filePath The full file path where you expect the settings JSON file to be located.  Likely you will generate a path relative to your script at runtime.
	 */
	public void enableStickySettings(String filePath){
		stickySettingsEnabled = true;
		stickySettingsFilePath = filePath;
	}
	
	/***
	 * Allows code to implement and provide a callback which can validate whether things are okay.
	 * @param callback Callback should return false if things are not satisfactory, true otherwise.
	 */
	public void validateBeforeClosing(ValidationCallback callback){
		validationCallback = callback;
	}
	
	/***
	 * Gets a JSON String equivalent of the dialog values.  This is a convenience method for calling
	 * {@link #toMap} and then converting that Map to a JSON string.
	 * @return A JSON string representation of the dialogs values (based on the Map returned by {@link toMap}).
	 */
	public String toJson(){
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.setDateFormat(dateSerializationFormat);
		Gson gson = builder.create();
		return gson.toJson(toMap(true));
	}
	
	/***
	 * Attempts to set control values based on entries in JSON file.  Entries which are unknown or cause errors are ignored.
	 * Loads JSON onto any controls in any tabs contained by this TabbedCustomDialog instance.
	 * @param json A JSON string to attempt to load.
	 */
	public void loadJson(String json){
		loadJson(json,controls);
	}
	
	/***
	 * Loads JSON but only to the controls contained within the specified tab.
	 * @param json A JSON string to attempt to load
	 * @param tabIdentifier Identifier of existing tab to which you would like to load the JSON into.
	 */
	public void loadJson(String json, String tabIdentifier){
		loadJson(json,tabs.get(tabIdentifier).controls);
	}
	
	/***
	 * Attempts to set control values based on entries in JSON file.  Entries which are unknown or cause errors are ignored.
	 * @param json A JSON string to attempt to load.
	 * @param controlMap A map of components to load the JSON onto.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void loadJson(String json, Map<String,Component> controlMap){
		Gson gson = new Gson();
		Map<String,Object> fieldValues = gson.fromJson(json,new TypeToken<Map<String,Object>>(){}.getType());
		for(Map.Entry<String,Object> entry : fieldValues.entrySet()){
			String controlIdentifier = entry.getKey();
			Component control = controlMap.get(controlIdentifier);
			try{
				if(deserializationHandlers.containsKey(controlIdentifier)){
					//Dynamic table needs to have filter cleared before being meddled with
					if (control instanceof DynamicTableControl){
						((DynamicTableControl)control).setFilter("");
					}
					
					deserializationHandlers.get(controlIdentifier).deserializeControlData(entry.getValue(), control);
				}
				else if (entry.getValue() instanceof Boolean){
					// Call general purpose set checked method
					setChecked(controlIdentifier,(Boolean)entry.getValue());
				}
				else if(control instanceof JSpinner){
					((JSpinner)control).setValue(entry.getValue());
				}
				// These require a bit more logic to deserialize
				else if(control instanceof ChoiceTableControl){
					ChoiceTableControl choiceTable = (ChoiceTableControl) control;
					choiceTable.getTableModel().uncheckAllChoices();
					List<Choice> loadedChoices = new ArrayList<Choice>();
					for(String value : (Iterable<String>)entry.getValue()){
						Choice choice = choiceTable.getTableModel().getFirstChoiceByLabel(value);
						if(choice != null){
							choiceTable.getTableModel().setChoiceSelection(choice, true);
							loadedChoices.add(choice);
						}
						else {
							System.out.println("Unable to resolve choice for "+entry.getKey()+" => "+value);
						}
					}
					choiceTable.getTableModel().sortChoicesToTop(loadedChoices);
				} else if(control instanceof PathList){
					PathList pathList = (PathList) control;
					List<String> values = new ArrayList<String>();
					for(String value : (Iterable<String>)entry.getValue()){
						values.add(value);
					}
					pathList.setPaths(values);
				}
				else if(control instanceof StringList){
					StringList stringList = (StringList) control;
					List<String> values = new ArrayList<String>();
					for(String value : (Iterable<String>)entry.getValue()){
						values.add(value);
					}
					stringList.setValues(values);
				}
				else if(control instanceof MultipleChoiceComboBox) {
					MultipleChoiceComboBox mccb = (MultipleChoiceComboBox) control;
					List<String> values = (ArrayList<String>) entry.getValue();
					mccb.setCheckedChoices(values);
				}
				else if(control instanceof LocalWorkerSettings){
					LocalWorkerSettings lws = (LocalWorkerSettings) control;
					Map<String,Object> settings = (Map<String,Object>)entry.getValue();
					//System.out.println(settings);
					lws.setWorkerCount(((Double) settings.get("workerCount")).intValue());
					lws.setMemoryPerWorker(((Double) settings.get("workerMemory")).intValue());
					lws.setWorkerTempDirectory((String) settings.get("workerTemp"));
				}
				else if(control instanceof BatchExporterTraversalSettings){
					BatchExporterTraversalSettings bets = (BatchExporterTraversalSettings) control;
					Map<String,Object> settings = (Map<String,Object>)entry.getValue();
					bets.getComboTraversal().setSelectedValue((String)settings.get("strategy"));
					bets.getComboDedupe().setSelectedValue((String)settings.get("deduplication"));
					bets.getComboSortOrder().setSelectedValue((String)settings.get("sortOrder"));
				}
				else if(control instanceof BatchExporterNativeSettings){
					BatchExporterNativeSettings bens = (BatchExporterNativeSettings) control;
					Map<String,Object> settings = (Map<String,Object>)entry.getValue();
					bens.getComboNaming().setSelectedValue((String)settings.get("naming"));
					bens.getTxtPath().setText((String)settings.get("path"));
					bens.getTxtSuffix().setText((String)settings.get("suffix"));
					bens.getComboMailFormat().setSelectedValue((String)settings.get("mailFormat"));
					bens.getChckbxIncludeAttachments().setSelected((Boolean)settings.get("includeAttachments"));
					bens.getChckbxRegenerateStored().setSelected((Boolean)settings.get("regenerateStored"));
				}
				else if(control instanceof BatchExporterTextSettings){
					BatchExporterTextSettings bets = (BatchExporterTextSettings) control;
					Map<String,Object> settings = (Map<String,Object>)entry.getValue();
					bets.getComboNaming().setSelectedValue((String)settings.get("naming"));
					bets.getTxtPath().setText((String)settings.get("path"));
					bets.getTxtSuffix().setText((String)settings.get("suffix"));
					bets.getChckbxWrapLines().setSelected((Boolean)settings.get("wrapLinesChecked"));
					bets.getSpinnerWrapLength().setValue(((Double)settings.get("wrapLines")).intValue());
					bets.getComboLineSeparator().setSelectedValue((String)settings.get("lineSeparator"));
					bets.getComboEncoding().setSelectedValue((String)settings.get("encoding"));
				}
				else if(control instanceof BatchExporterPdfSettings){
					BatchExporterPdfSettings beps = (BatchExporterPdfSettings) control;
					Map<String,Object> settings = (Map<String,Object>)entry.getValue();
					beps.getComboNaming().setSelectedValue((String)settings.get("naming"));
					beps.getTxtPath().setText((String)settings.get("path"));
					beps.getTxtSuffix().setText((String)settings.get("suffix"));
					beps.getChckbxRegenerateStored().setSelected((Boolean)settings.get("regenerateStored"));
				}
				else if(control instanceof BatchExporterLoadFileSettings){
					BatchExporterLoadFileSettings belfs = (BatchExporterLoadFileSettings) control;
					Map<String,Object> settings = (Map<String,Object>)entry.getValue();
					belfs.getComboLoadFileType().setSelectedValue((String)settings.get("type"));
					belfs.getComboProfile().setSelectedValue((String)settings.get("metadataProfile"));
					belfs.getComboEncoding().setSelectedValue((String)settings.get("encoding"));
					belfs.getComboLineSeparator().setSelectedValue((String)settings.get("lineSeparator"));
				}
				else if(control instanceof OcrSettings){
					OcrSettings os = (OcrSettings) control;
					Map<String,Object> settings = (Map<String,Object>)entry.getValue();
					os.getChckbxRegeneratePdfs().setSelected((Boolean)settings.get("regeneratePdfs"));
					os.getChckbxUpdatePdfText().setSelected((Boolean)settings.get("updatePdf"));
					os.getChckbxUpdateItemText().setSelected((Boolean)settings.get("updateText"));
					os.getComboTextModification().setSelectedValue((String)settings.get("textModification"));
					os.getComboQuality().setSelectedValue((String)settings.get("quality"));
					os.getComboRotation().setSelectedValue((String)settings.get("rotation"));
					os.getChckbxDeskew().setSelected((Boolean)settings.get("deskew"));
					os.getOutputDirectory().setPath((String)settings.get("outputDirectory"));
					
					if(NuixConnection.getCurrentNuixVersion().isAtLeast("7.2.0") && settings.containsKey("updateDuplicates")){
						os.setUpdateDuplicates((Boolean)settings.get("updateDuplicates"));
					}
					
					if(NuixConnection.getCurrentNuixVersion().isAtLeast("7.2.0") && settings.containsKey("timeout")){
						os.setTimeoutMinutes(((Double)settings.get("timeout")).intValue());
					}
					
					ChoiceTableControl choiceTable = os.getLanguageChoices();
					// Should clear check state before loading in new check state
					choiceTable.getTableModel().uncheckAllChoices();
					List<Choice> loadedChoices = new ArrayList<Choice>();
					for(String value : (Iterable<String>)settings.get("languages")){
						Choice choice = choiceTable.getTableModel().getFirstChoiceByLabel(value);
						if(choice != null){
							choiceTable.getTableModel().setChoiceSelection(choice, true);
							loadedChoices.add(choice);
						}
						else {
							System.out.println("Unable to resolve choice for "+entry.getKey()+" => "+value);
						}
					}
					choiceTable.getTableModel().sortChoicesToTop(loadedChoices);
				}
				else if (control instanceof DynamicTableControl){
					((DynamicTableControl)control).setFilter("");
					List<String> values = new ArrayList<String>();
					for(String value : (Iterable<String>)entry.getValue()){
						values.add(value);
					}
					((DynamicTableControl)control).getTableModel().setCheckedRecordsFromHashes(values);
				}
				else if (control instanceof CsvTable){
					CsvTable table = (CsvTable)control;
					List<Map<String,String>> records = (List<Map<String,String>>)entry.getValue();
					for (Map<String, String> record : records) {
						table.addRecord(record);
					}
				}
				else if(entry.getValue() instanceof String){
					if(control instanceof JXDatePicker){
						((JXDatePicker)control).setDate(sdf.parse((String)entry.getValue()));
					} else {
						// Call general purpose set text method
						setText(controlIdentifier,(String)entry.getValue());
					}
				}
			}catch(Exception exc){
				System.out.println("Error while deserializing JSON field '"+controlIdentifier+"':");
				System.out.println(exc.toString());
				exc.printStackTrace();
			}
		}
	}
	
	/***
	 * Saves the settings of this dialog to a JSON file
	 * @param filePath Path to the JSON file settings will be saved to
	 * @throws Exception Thrown if there are exceptions while saving the file
	 */
	public void saveJsonFile(String filePath) throws Exception{
		FileWriter fw = null;
		PrintWriter pw = null;
		try{
			fw = new FileWriter(filePath);
			pw = new PrintWriter(fw);
			pw.print(toJson());
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
	 * Loads the settings of this dialog from a JSON file
	 * @param filePath Path to the JSON file settings will be loaded from
	 * @throws IOException Thrown if there are exceptions while loading the file
	 */
	public void loadJsonFile(String filePath) throws IOException{
		List<String> lines = Files.readAllLines(Paths.get(filePath));
		loadJson(Joiner.on("\n").join(lines));
		if (jsonFileLoadedCallback != null){
			jsonFileLoadedCallback.run();
		}
	}
	
	/***
	 * The File currently associated to the "Help" menu "View Documentation" entry
	 * @return The currently associated help file
	 */
	public File getHelpFile() {
		return helpFile;
	}
	
	/***
	 * Sets the path to a help file which will be associated to the "Help" menu "View Documentation" entry
	 * @param helpFile Path to a help file (set null to hide help menu, default is null)
	 */
	public void setHelpFile(File helpFile) {
		this.helpFile = helpFile;
	}
	
	/***
	 * Sets the path to a help file which will be associated to the "Help" menu "View Help" entry
	 * @param helpFile Path to a help file (set null to hide help menu, default is null)
	 */
	public void setHelpFile(String helpFile) {
		setHelpFile(new File(helpFile));
	}

	/***
	 * Sets the URL which will be associated to the "Help" menu "View Online Help" entry.
	 * @param helpUrl URL to a web site
	 */
	public void setHelpUrl(String helpUrl) {
		this.helpUrl = helpUrl;
	}

	/***
	 * Hides the file menu from the user (effectively disabling save and load).  Mostly included for situations
	 * where settings cannot be reasonably saved to JSON so you wish to hide those choices from the user.
	 */
	public void hideFileMenu(){
		mnFile.setVisible(false);
	}
	
	/***
	 * Allows you to provide a callback to run when a JSON file is loaded.
	 * @param callback The callback to run
	 */
	public void whenJsonFileLoaded(Runnable callback){
		jsonFileLoadedCallback = callback;
	}
	
	/***
	 * Advanced!  Allows you to define a callback which will handle serialization of a particular control
	 * to JSON.
	 * @param identifier The identifier provided when the control was added to the dialog.
	 * @param handler The callback which will handle serializing the controls data.
	 */
	public void whenSerializing(String identifier, ControlSerializationHandler handler){
		serializationHandlers.put(identifier, handler);
	}
	
	/***
	 * Advanced!  Allows you to define a callback which will handle deserialization of a particular control
	 * from JSON.
	 * @param identifier The identifier provided when the control was added to the dialog.
	 * @param handler The callback which will handle deserializing the controls data.
	 */
	public void whenDeserializing(String identifier, ControlDeserializationHandler handler){
		deserializationHandlers.put(identifier, handler);
	}
	
	/***
	 * Gets the previously provided deserialization handler for a particular control
	 * @param identifier Control id to which the desired deserializer was assigned
	 * @return The deserialization handler if one was provided
	 */
	public ControlDeserializationHandler getDeserializer(String identifier){
		return deserializationHandlers.get(identifier);
	}
	
	/***
	 * Gets the previously provided serialization handler for a particular control
	 * @param identifier Control id to which the desired serializer was assigned
	 * @return The serialization handler if one was provided
	 */
	public ControlSerializationHandler getSerializer(String identifier){
		return serializationHandlers.get(identifier);
	}
	
	private Map<String,JMenu> parentMenus = new HashMap<String,JMenu>();
	private JMenuItem mntmViewOnlineHelp;
	
	/***
	 * Adds a menu entry to the menu bar and then a menu item to that menu.
	 * @param parentMenuLabel The label of the menu to be added to the menu bar.  If one already exists with this name (exact matching) then the created
	 * menu item will be added to that existing menu.  If a menu with this label does not already exist, it is created.
	 * @param menuItemLabel The label of the menu item to be added to the specified parent menu.
	 * @param action The action to perform when the given menu item is clicked.  Example:
	 * <pre>{@code
	 * dialog.addMenu("Add Queries","Add Custodian Queries") do
	 *	$current_case.getAllCustodians.each do |custodian_name|
	 *			record = {
	 *				:name => "Custodian: #{custodian_name}",
	 *				:query => "custodian:\"#{custodian_name}\"",
	 *			}
	 *			dynamic_table.getModel.addRecord(record)
	 *		end
	 *		dialog.setSelectedTabIndex(2)
	 * 	end
	 * }</pre>
	 */
	public void addMenu(String parentMenuLabel, String menuItemLabel, Runnable action) {
		JMenu parentMenu = null;
		if(!parentMenus.containsKey(parentMenuLabel)) {
			parentMenu = new JMenu(parentMenuLabel);
			menuBar.add(parentMenu);
			parentMenus.put(parentMenuLabel, parentMenu);
		} else {
			parentMenu = parentMenus.get(parentMenuLabel);
		}
		
		JMenuItem menuItem = new JMenuItem(menuItemLabel);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { action.run(); }
		});
		parentMenu.add(menuItem);
	}
	
	/***
	 * Sets which tab is currently selected.
	 * @param index The index of the tab to make selected (index starts at 0).
	 */
	public void setSelectedTabIndex(int index) {
		tabbedPane.setSelectedIndex(index);
	}
}
