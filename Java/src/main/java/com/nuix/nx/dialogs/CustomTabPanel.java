/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXDatePicker;
import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nuix.nx.NuixConnection;
import com.nuix.nx.controls.BatchExporterLoadFileSettings;
import com.nuix.nx.controls.BatchExporterNativeSettings;
import com.nuix.nx.controls.BatchExporterPdfSettings;
import com.nuix.nx.controls.BatchExporterTextSettings;
import com.nuix.nx.controls.BatchExporterTraversalSettings;
import com.nuix.nx.controls.ChoiceTableControl;
import com.nuix.nx.controls.ComboItem;
import com.nuix.nx.controls.ComboItemBox;
import com.nuix.nx.controls.CsvTable;
import com.nuix.nx.controls.DynamicTableControl;
import com.nuix.nx.controls.LocalWorkerSettings;
import com.nuix.nx.controls.OcrSettings;
import com.nuix.nx.controls.PathList;
import com.nuix.nx.controls.PathSelectedCallback;
import com.nuix.nx.controls.PathSelectionControl;
import com.nuix.nx.controls.PathSelectionControl.ChooserType;
import com.nuix.nx.controls.StringList;
import com.nuix.nx.controls.models.Choice;
import com.nuix.nx.controls.models.ControlDeserializationHandler;
import com.nuix.nx.controls.models.ControlSerializationHandler;
import com.nuix.nx.controls.models.DynamicTableValueCallback;

/***
 * This class represents a tab in the {@link TabbedCustomDialog} class.  This tab component hosts all the
 * various methods for adding controls to a tab.
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class CustomTabPanel extends JPanel{
	private String label = "New Tab";
	private TabbedCustomDialog owner;
	Map<String,Component> controls;
	private Map<String,ButtonGroup> buttonGroups;
	private static final Insets genericInsets = new Insets(2,2,2,2);
	GridBagLayout rootLayout;
	private int LABEL_COLUMN_WIDTH = 50;
	private int CONTROL_COLUMN_WIDTH = 500;
	private int headersCount = 0;
	private int choiceTableHeight = 200;
	private boolean hasVerticalFiller = false;
	Map<String,Boolean> enabledStates = null;
	private Set<String> skipSerializing = new HashSet<String>();
	
	public CustomTabPanel(String label,TabbedCustomDialog owner) {
		this.label = label;
		this.owner = owner;
		buttonGroups = new HashMap<String,ButtonGroup>();
		controls = new HashMap<String,Component>();
		
		setBorder(new EmptyBorder(5,5,5,5));
		rootLayout = new GridBagLayout();
		rootLayout.columnWidths = new int[]{LABEL_COLUMN_WIDTH,CONTROL_COLUMN_WIDTH};
		setLayout(rootLayout);
	}
	
	protected void addBasicLabelledComponent(String label, Component component){
		addBasicLabelledComponent(label,component,false,true);
	}
	
	protected void addBasicLabelledComponent(String label, Component component, boolean fillVertical) {
		addBasicLabelledComponent(label,component,fillVertical,true);
	}
	
	protected void addBasicLabelledComponent(String label, Component component, boolean fillVertical, boolean fillHorizontal){
		if(fillVertical)
			hasVerticalFiller = true;
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = controls.size()+headersCount;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = genericInsets;
		c.anchor = GridBagConstraints.EAST;
		JLabel labelComponent = new JLabel(label);
		labelComponent.setHorizontalAlignment(SwingConstants.RIGHT);
		labelComponent.setVerticalAlignment(SwingConstants.CENTER);
		Font font = labelComponent.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
		labelComponent.setFont(boldFont);
		addComponent(labelComponent,c);
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = controls.size()+headersCount;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		
		if(fillVertical){c.weighty = 1.0;}
		else{c.weighty = 0;}
		
		if(fillVertical && fillHorizontal){
			c.fill = GridBagConstraints.BOTH;
		} else if(fillVertical){
			c.fill = GridBagConstraints.VERTICAL;
		} else if(fillHorizontal){
			c.fill = GridBagConstraints.HORIZONTAL;
		}
		else {
			c.fill = GridBagConstraints.NONE;
		}
		c.insets = genericInsets;
		c.anchor = GridBagConstraints.NORTHWEST;
		addComponent(component,c);
	}
	
	/***
	 * Adds a panel to fill remaining vertical space if none of the controls which have
	 * been already added are vertical space filling.  Helps ensure that controls are packed
	 * nicely in the GridBagLayout.
	 */
	void addVerticalFillerAsNeeded(){
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = controls.size()+headersCount;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		if(hasVerticalFiller){c.weighty = 0;}
		c.fill = GridBagConstraints.BOTH;
		c.insets = genericInsets;
		c.anchor = GridBagConstraints.NORTHWEST;
		JPanel filler = new JPanel();
		//filler.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
		addComponent(filler,c);
	}
	
	protected void addComponent(Component component, int col){
		addComponent(component,controls.size()+headersCount,col,1,1,false);
	}
	
	protected void addComponents(Component leftComponent, Component rightComponent){
		int row = controls.size()+headersCount;
		addComponent(leftComponent,row,0,1,1,false);
		addComponent(rightComponent,row,1,1,1,false);
	}
	
	protected void addComponent(Component component, int row, int col, int width, int height){
		addComponent(component,row,col,width,height,false);
	}
	
	protected void addComponent(Component component, int row, int col, int width, int height, boolean fillVertical){
		if(fillVertical)
			hasVerticalFiller = true;
		double weighty = 0;
		if(fillVertical) weighty = 1.0;
		addComponent(component,row,col,width,height,0,weighty);
	}
	
	protected void addComponent(Component component, int row, int col, int width, int height, double weightx, double weighty){
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = col;
		c.gridy = row;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = weightx;
		c.weighty = weighty;
		c.fill = GridBagConstraints.BOTH;
		c.insets = genericInsets;
		c.anchor = GridBagConstraints.NORTHEAST;
		addComponent(component,c);
	}
	
	protected void addComponent(Component component, GridBagConstraints c){
		rootLayout.setConstraints(component,c);
		add(component);
	}
	
	/***
	 * Registers control to be tracked which is important to ensuring that control values are able to be
	 * passed back to script and eligible for being saved to or loaded from JSON. 
	 * @param identifier Unique identifier associated with this control
	 * @param component The actual control
	 * @throws Exception Thrown is something goes wrong
	 */
	protected void trackComponent(String identifier, Component component) throws Exception{
		if(controls.containsKey(identifier) || owner.controls.containsKey(identifier))
			throw new Exception("Cannot append component, component with the identifier already exists: "+identifier);
		else {
			controls.put(identifier, component);
			owner.controls.put(identifier, component);
		}
	}
	
	/***
	 * Appends a check box control to the tab.
	 * Calls to {@link #getControl(String)} should cast result to JCheckBox.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param isChecked Whether this is checked initially.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendCheckBox(String identifier, String controlLabel, boolean isChecked) throws Exception{
		JCheckBox component = new JCheckBox(controlLabel);
		component.setSelected(isChecked);
		addComponent(component,1);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends 2 check boxes, in a single row, to the tab.
	 * @param identifierA The unique identifier for the first check box.
	 * @param controlLabelA The label for the first checkbox.
	 * @param isCheckedA Whether the first check box is checked by default.
	 * @param identifierB The unique identifier of the second check box.
	 * @param controlLabelB The label for the second checkbox.
	 * @param isCheckedB Whether the second check box is checked by default.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if a provided identifier has already been used.
	 */
	public CustomTabPanel appendCheckBoxes(String identifierA, String controlLabelA, boolean isCheckedA,
			String identifierB, String controlLabelB, boolean isCheckedB) throws Exception{
		
		JCheckBox componentA = new JCheckBox(controlLabelA);
		componentA.setSelected(isCheckedA);
		
		JCheckBox componentB = new JCheckBox(controlLabelB);
		componentB.setSelected(isCheckedB);
		
		JPanel gridPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(1,2);
		gridPanel.setLayout(gridLayout);
		gridPanel.add(componentA);
		gridPanel.add(componentB);
		
		addComponent(gridPanel,1);
		
		trackComponent(identifierA, componentA);
		trackComponent(identifierB, componentB);
				
		return this;
	}
	
	/***
	 * Appends a radio button control to the dialog.
	 * Calls to {@link #getControl(String)} should cast result to JRadioButton.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param radioButtonGroupName The name of the radio button group to assign this to.  The group determines what other radio buttons are
	 * unchecked when a given radio button is checked.
	 * @param isChecked Whether this is checked initially.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendRadioButton(String identifier, String controlLabel, String radioButtonGroupName, boolean isChecked) throws Exception{
		JRadioButton component = new JRadioButton(controlLabel);
		buttonGroups.putIfAbsent(radioButtonGroupName, new ButtonGroup());
		buttonGroups.get(radioButtonGroupName).add(component);
		component.setSelected(isChecked);
		addComponent(component,1);
		trackComponent(identifier, component);
		return this;
	}
	
	public CustomTabPanel appendRadioButtonLeft(String identifier, String controlLabel, String radioButtonGroupName, boolean isChecked) throws Exception{
		JRadioButton component = new JRadioButton(controlLabel);
		buttonGroups.putIfAbsent(radioButtonGroupName, new ButtonGroup());
		buttonGroups.get(radioButtonGroupName).add(component);
		component.setSelected(isChecked);
		addComponent(component,0);
		trackComponent(identifier, component);
		return this;
	}
	
	public CustomTabPanel appendRadioButtonGroup(String groupLabel, String radioButtonGroupName, Map<String,String> radioButtonChoices) throws Exception{
		JPanel radioButtonContainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		radioButtonContainerPanel.getInsets().set(1, 1, 1, 1);
		
		addBasicLabelledComponent(groupLabel, radioButtonContainerPanel);
		buttonGroups.putIfAbsent(radioButtonGroupName, new ButtonGroup());
		ButtonGroup group = buttonGroups.get(radioButtonGroupName);
		int index = 0;
		for(Map.Entry<String, String> entry : radioButtonChoices.entrySet()){
			JRadioButton rb = new JRadioButton(entry.getKey());
			group.add(rb);
			radioButtonContainerPanel.add(rb);
			trackComponent(entry.getValue(), rb);
			if(index == 0){ rb.setSelected(true); }
			index++;
		}
		
		return this;
	}
	
	/***
	 * Appends a header label that spans 2 columns.
	 * @param text The text of the label.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 */
	public CustomTabPanel appendHeader(String text){
		JLabel component = new JLabel(text);
		Font f = new Font("serif", Font.BOLD, 16);
		component.setFont(f);
		addComponent(component,controls.size()+headersCount,0,2,1);
		headersCount++;
		return this;
	}
	
	/***
	 * Appends a label that spans 2 columns.
	 * @param identifier The unique identifier for this control.
	 * @param text The text of the label.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if this identifier has already been used
	 */
	public CustomTabPanel appendLabel(String identifier, String text) throws Exception{
		JLabel component = new JLabel(text);
		addComponent(component,controls.size()+headersCount,0,2,1);
		trackComponent(identifier, component);
		headersCount++;
		return this;
	}

	/***
	 * Appends an image to the tab
	 * @param imageFile File object representing the image file on disk
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 */
	public CustomTabPanel appendImage(File imageFile){
		BufferedImage picture = null;
		try {
			picture = ImageIO.read(imageFile);
			JLabel component = new JLabel(new ImageIcon(picture));
			component.setAlignmentX(LEFT_ALIGNMENT);
			Font f = new Font("serif", Font.BOLD, 16);
			component.setFont(f);
			addComponent(component,controls.size()+headersCount,0,2,1);
			headersCount++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/***
	 * Appends an image to the tab
	 * @param imageFile String representing path to the image file on disk
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 */
	public CustomTabPanel appendImage(String imageFile){
		return appendImage(new File(imageFile));
	}
	
	/***
	 * Appends a separator that spans 2 columns.
	 * @param label Label text that will appear in the center of the separator.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 */
	public CustomTabPanel appendSeparator(String label){
		MatteBorder mb = new MatteBorder(1, 0, 0, 0, Color.BLACK);
		TitledBorder tb = new TitledBorder(mb, label, TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
		JPanel component = new JPanel();
		component.setBorder(tb);
		addComponent(component,controls.size()+headersCount,0,2,1);
		headersCount++;
		return this;
	}
	
	/***
	 * Appends a text field control to the dialog.
	 * Calls to {@link #getControl(String)} should cast result to JTextField.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param text The initial text this text field should contain.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendTextField(String identifier, String controlLabel, String text) throws Exception{
		JTextField component = new JTextField();
		component.setText(text);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a text field with an associated check box.  Text field is enabled/disabled based on whether
	 * check box is checked.
	 * @param checkBoxId The unique identifier of the check box.
	 * @param isChecked Whether the check box is checked by default.
	 * @param textFieldId The unique identifier of the text field.
	 * @param textFieldDefault The default text of the text field.
	 * @param controlLabel The label of the control.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if a provided identifier has already been used.
	 */
	public CustomTabPanel appendCheckableTextField(String checkBoxId, boolean isChecked,
			String textFieldId, String textFieldDefault, String controlLabel) throws Exception{
		
		JCheckBox checkComponent = new JCheckBox(controlLabel);
		checkComponent.setSelected(isChecked);
		
		JTextField textComponent = new JTextField();
		textComponent.setText(textFieldDefault);
		
		addComponents(checkComponent, textComponent);
		
		trackComponent(checkBoxId, checkComponent);
		trackComponent(textFieldId, textComponent);
		
		enabledOnlyWhenChecked(textFieldId, checkBoxId);
		
		return this;
	}
	
	/***
	 * Appends a JButton control with the specified label and attaches the provided action listener to the
	 * button.  From Ruby pass a block to add a handler to the button.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label text of the button
	 * @param actionListener The action listener to attach to the button.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendButton(String identifier, String controlLabel, ActionListener actionListener) throws Exception{
		JButton component = new JButton(controlLabel);
		component.addActionListener(actionListener);
		Double preferredHeight = component.getPreferredSize().getHeight();
		component.setPreferredSize(new Dimension(150,preferredHeight.intValue()));
		addBasicLabelledComponent("", component, false, false);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Creates a up/down number picker control (known in Java as a Spinner).
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param initialValue Initial value for the control.
	 * @param min The minimum value for the control.
	 * @param max The maximum value for the control.
	 * @param step Determines the "step" value, which determines how much up/down buttons increment.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendSpinner(String identifier, String controlLabel, int initialValue, int min, int max, int step) throws Exception {
		JSpinner component = new JSpinner();
		component.setValue(initialValue);
		Double preferredHeight = component.getPreferredSize().getHeight();
		component.setPreferredSize(new Dimension(150,preferredHeight.intValue()));
		component.setModel(new SpinnerNumberModel(initialValue, min, max, step));
		addBasicLabelledComponent(controlLabel, component, false, false);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Creates a up/down number picker control (known in Java as a Spinner).  Uses a default step of 1.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param initialValue Initial value for the control.
	 * @param min The minimum value for the control.
	 * @param max The maximum value for the control.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendSpinner(String identifier, String controlLabel, int initialValue, int min, int max) throws Exception {
		return appendSpinner(identifier,controlLabel,initialValue,min,max,1);
	}
	
	/***
	 * Creates a up/down number picker control (known in Java as a Spinner).  Uses a default step of 1, a minimum of 0
	 * and a maximum of Integer.MAX_VALUE.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param initialValue Initial value for the control.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendSpinner(String identifier, String controlLabel, int initialValue) throws Exception {
		return appendSpinner(identifier,controlLabel,initialValue,0,Integer.MAX_VALUE,1);
	}
	
	/***
	 * Creates a up/down number picker control (known in Java as a Spinner).  Uses a default step of 1, a minimum of 0
	 * , a maximum of Integer.MAX_VALUE and an initial value of 0.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendSpinner(String identifier, String controlLabel) throws Exception {
		return appendSpinner(identifier,controlLabel,0,0,Integer.MAX_VALUE,1);
	}
	
	/***
	 * Appends a date picker field control to the dialog
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param defaultDate The default date to display.  Can be null for no default, a java.util.Date object or a String in the format "yyyymmdd".
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendDatePicker(String identifier, String controlLabel, Object defaultDate) throws Exception{
		JXDatePicker component = new JXDatePicker();
		component.setFormats(new String[] {"yyyyMMdd"});
		
		if(defaultDate != null){
			if(defaultDate instanceof Date){
				component.setDate((Date) defaultDate);
			}
			else if (defaultDate instanceof DateTime){
				component.setDate(((DateTime)defaultDate).toDate());
			}
			else if (defaultDate instanceof String){
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				Date date = dateFormat.parse((String) defaultDate);
				component.setDate((Date) date);
			}
		}
		
		Double preferredHeight = component.getPreferredSize().getHeight();
		component.setPreferredSize(new Dimension(150,preferredHeight.intValue()));
		addBasicLabelledComponent(controlLabel, component,false,false);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a date picker field control to the dialog with the default date being today's date.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendDatePicker(String identifier, String controlLabel) throws Exception{
		return appendDatePicker(identifier, controlLabel, null);
	}
	
	/***
	 * Appends a text area control to the dialog.
	 * Calls to {@link #getControl(String)} should cast result to JTextArea.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param text The initial text this text area should contain.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendTextArea(String identifier, String controlLabel, String text) throws Exception{
		JPanel panel = new JPanel();
		//panel.setPreferredSize(new Dimension(CONTROL_COLUMN_WIDTH,150));
		panel.setLayout(new BorderLayout());
		JTextArea textArea = new JTextArea();
		JScrollPane scroll = new JScrollPane(textArea);
		panel.add(scroll,BorderLayout.CENTER);
		textArea.setText(text);
		if(controlLabel != null && controlLabel.length() > 0){
			addBasicLabelledComponent(controlLabel, panel, true);	
		} else {
			addComponent(panel,controls.size()+headersCount,0,2,1,true);
			headersCount++;
		}
		
		trackComponent(identifier, textArea);
		return this;
	}
	
	/***
	 * Appends a read only text area control for you to place some user information.
	 * Calls to {@link #getControl(String)} should cast result to JTextArea.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param text The initial text this text area should contain.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendInformation(String identifier, String controlLabel, String text) throws Exception{
		JPanel panel = new JPanel();
		//panel.setPreferredSize(new Dimension(CONTROL_COLUMN_WIDTH,100));
		panel.setLayout(new BorderLayout());
		JTextArea textArea = new JTextArea();
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scroll,BorderLayout.CENTER);
		textArea.setText(text);
		textArea.setEditable(false);
		addComponent(panel,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier, textArea);
		return this;
	}
	
	/***
	 * Appends a read only text area control for you to place some user information similar to {@link #appendInformation(String, String, String)}.
	 * This method differs from {@link #appendInformation(String, String, String)} in that the created text area will be assigned a monospaced
	 * font to preserve formatting of the provided text.
	 * Calls to {@link #getControl(String)} should cast result to JTextArea.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param text The initial text this text area should contain.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendFormattedInformation(String identifier, String controlLabel, String text) throws Exception{
		JPanel panel = new JPanel();
		//panel.setPreferredSize(new Dimension(CONTROL_COLUMN_WIDTH,100));
		panel.setLayout(new BorderLayout());
		JTextArea textArea = new JTextArea();
		Font f = new Font("Consolas", Font.PLAIN, 11);
		textArea.setFont(f);
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scroll,BorderLayout.CENTER);
		textArea.setText(text);
		textArea.setEditable(false);
		addComponent(panel,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier, textArea);
		return this;
	}
	
	/***
	 * Appends a control with controls used to provide worker settings, as passed to {@link nuix.ParallelProcessingConfigurable#setParallelProcessingSettings(Map)}.
	 * @param identifier The unique identifier of this control.  Note values of nested controls will be returns as a Map under this identifier.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendLocalWorkerSettings(String identifier) throws Exception{
		LocalWorkerSettings component = new LocalWorkerSettings();
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier,component);
		return this;
	}
	
	/***
	 * Appends a control with controls used to provide traversal settings, as passed to {@link nuix.BatchExporter#setTraversalOptions(Map)}.
	 * @param identifier The unique identifier of this control.  Note values of nested controls will be returns as a Map under this identifier.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendBatchExporterTraversalSettings(String identifier) throws Exception {
		BatchExporterTraversalSettings component = new BatchExporterTraversalSettings();
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier,component);
		return this;
	}
	
	/***
	 * Appends a control with controls used to provide native export settings, as passed to {@link nuix.BatchExporter#addProduct(String, Map)} for the "native" product.
	 * @param identifier The unique identifier of this control.  Note values of nested controls will be returns as a Map under this identifier.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendBatchExporterNativeSettings(String identifier) throws Exception {
		BatchExporterNativeSettings component = new BatchExporterNativeSettings();
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier,component);
		return this;
	}
	
	/***
	 * Appends a control with controls used to provide text export settings, as passed to {@link nuix.BatchExporter#addProduct(String, Map)} for the "text" product.
	 * @param identifier The unique identifier of this control.  Note values of nested controls will be returns as a Map under this identifier.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendBatchExporterTextSettings(String identifier) throws Exception {
		BatchExporterTextSettings component = new BatchExporterTextSettings();
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier,component);
		return this;
	}
	
	/***
	 * Appends a control with controls used to provide PDF export settings, as passed to {@link nuix.BatchExporter#addProduct(String, Map)} for the "pdf" product.
	 * @param identifier The unique identifier of this control.  Note values of nested controls will be returns as a Map under this identifier.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendBatchExporterPdfSettings(String identifier) throws Exception {
		BatchExporterPdfSettings component = new BatchExporterPdfSettings();
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier,component);
		return this;
	}
	
	/***
	 * Appends a control with controls used to provide loadfile export settings, as passed to {@link nuix.BatchExporter#addLoadFile(String, Map)}.
	 * @param identifier The unique identifier of this control.  Note values of nested controls will be returns as a Map under this identifier.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendBatchExporterLoadFileSettings(String identifier) throws Exception {
		BatchExporterLoadFileSettings component = new BatchExporterLoadFileSettings();
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier,component);
		return this;
	}
	
	/***
	 * Appends a control with controls used to provide OCR settings, as passed to {@link nuix.OcrProcessor}.
	 * @param identifier The unique identifier of this control.  Note values of nested controls will be returns as a Map under this identifier.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendOcrSettings(String identifier) throws Exception {
		OcrSettings component = new OcrSettings();
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier,component);
		return this;
	}
	
	/***
	 * Appends a password text field control to the dialog.
	 * Calls to {@link #getControl(String)} should cast result to JPasswordField.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param text The initial text this field should contain.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendPasswordField(String identifier, String controlLabel, String text) throws Exception{
		JPasswordField component = new JPasswordField();
		component.setText(text);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows the user to select multiple choices.
	 * See {@link #appendStringChoiceTable(String, String, Collection)} for a simpler usage.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param choices The list of {@link Choice} objects to display.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 * @param <T> The data type of the choice values.  Allows any value to be supported as a choice value.
	 */
	public <T> CustomTabPanel appendChoiceTable(String identifier, String controlLabel, List<Choice<T>> choices) throws Exception{
		ChoiceTableControl<T> component = new ChoiceTableControl<T>();
		component.setChoices(choices);
		component.setPreferredSize(new Dimension(CONTROL_COLUMN_WIDTH,choiceTableHeight));
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a table control with the specified headers, which is capable of importing a CSV with the same headers.
	 * @param identifier The unique identifier for this control.
	 * @param headers List of headers for the table.  Also determines import CSV columns.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendCsvTable(String identifier, List<String> headers) throws Exception{
		CsvTable component = new CsvTable(headers);
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a fairly flexible table control to the tab.  Table control relies on provided callback to get column values and optionally write
	 * column value changes back to underlying row objects.
	 * <pre>
 	 * {@code
	 * # Define what the headers will be
	 * headers = [
	 * 	"First",
	 * 	"Last",
	 * 	"Location",
	 * 	"Occupation",
	 * ]
	 * 
	 * # Define the records which will be displayed, this can essentially look
	 * # like whatever you want as later the callback we define will be responsible
	 * # for getting/setting values for individual records
	 * records = [
	 * 	{first: "Luke", last: "Skywalker", location: "Tatooine", occupation: "Moisture Farmer"},
	 * 	{first: "Beru", last: "Lars", location: "Tatooine", occupation: "Moisture Farmer"},
	 * 	{first: "Owen", last: "Lars", location: "Tatooine", occupation: "Moisture Farmer"},
	 * 	{first: "Obi-wan", last: "Kenobi", location: "Tatooine", occupation: "Hermit"},
	 * ]
	 * 
	 * # Now we add the dynamic table, configuring headers, records and callback which will get/set cell values
	 * # Method signature
	 * # public CustomTabPanel appendDynamicTable(String identifier, String controlLabel, List<String> headers,
	 * # 	List<Object> records, DynamicTableValueCallback callback)
	 * #
	 * # Callback signature
	 * # interact(Object record, int i, boolean setValue, Object aValue)
	 * #
	 * main_tab.appendDynamicTable("characters_table","Characters",headers,records) do |record, column_index, setting_value, value|
	 * 	# record: The current record the table wants to interact with from the records array
	 * 	# column_index: The column index the table wants to interact with
	 * 	# setting_value: True if the table wishes to set a new value for this record/column index, false if reading the current value
	 * 	# value: If setting_value is true, the value the table wishes to store back on the item
	 * 
	 * 	# Debugging messages
	 * 	show_debug = false
	 * 	if show_debug
	 * 		if setting_value
	 * 			puts "Setting column #{column_index} with value '#{value}' in object:\n#{record.inspect}"
	 * 		else
	 * 			puts "Getting column #{column_index} in object:\n#{record.inspect}"
	 * 		end
	 * 	end
	 * 
	 * 	if setting_value
	 * 		# Logic for setting values
	 * 		case column_index
	 * 		when 0
	 * 			# Example of modifying value before storing it
	 * 			record[:first] = value.capitalize
	 * 		when 1
	 * 			record[:last] = value.capitalize
	 * 		when 2
	 * 			record[:location] = value
	 * 		when 3
	 * 			record[:occupation] = value
	 * 		end
	 * 	else
	 * 		# Logic for getting values
	 * 		case column_index
	 * 		when 0
	 * 			next record[:first]
	 * 		when 1
	 * 			next record[:last]
	 * 		when 2
	 * 			next record[:location]
	 * 		when 3
	 * 			next record[:occupation]
	 * 		end
	 * 	end
	 * end
	 * }
	 * </pre>
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param headers List of headers for this control.
	 * @param records A List of objects representing each row in the table.  Can be any object provided callback is able to get values from.
	 * @param callback A callback which is responsible for reading and potentially writing values associated to each column in the table.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendDynamicTable(String identifier, String controlLabel,
			List<String> headers, List<Object> records, DynamicTableValueCallback callback) throws Exception{
		DynamicTableControl component = new DynamicTableControl(headers,records,callback);
		component.setPreferredSize(new Dimension(CONTROL_COLUMN_WIDTH,choiceTableHeight));
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows the user to select multiple choices.
	 * Calls to {@link #getControl(String)} should cast result to ChoiceTableControl&lt;String&gt;.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param choices The collection of String choices to display.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendStringChoiceTable(String identifier, String controlLabel, Collection<String> choices) throws Exception{
		List<Choice<String>> actualChoices = new ArrayList<Choice<String>>();
		for(String choice : choices){
			actualChoices.add(new Choice<String>(choice));
		}
		return appendChoiceTable(identifier, controlLabel, actualChoices);
	}
	
	/***
	 * Appends a combo box control allowing a user to select one of many choices.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param choices A collection of strings which will be the available choices
	 * @param callback Optional (may be null) callback which will be invoked when the combo box value changes
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendComboBox(String identifier, String controlLabel, Collection<String> choices, Runnable callback) throws Exception{
		JComboBox<String> component = new JComboBox<String>();
		for(String choice : choices){
			component.addItem(choice);
		}
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		
		if(callback != null){
			component.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					callback.run();
				}
			});
		}
		
		return this;
	}
	
	/***
	 * Appends a combo box control allowing a user to select one of many choices.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param choices A collection of strings which will be the available choices
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendComboBox(String identifier, String controlLabel, Collection<String> choices) throws Exception {
		return appendComboBox(identifier,controlLabel,choices,null);
	}
	
	public CustomTabPanel appendComboItemBox(String identifier, String controlLabel, List<ComboItem> choices, Runnable callback) throws Exception{
		ComboItemBox component = new ComboItemBox();
		component.setValues(choices);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		
		if(callback != null){
			component.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					callback.run();
				}
			});
		}
		
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a directory.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendDirectoryChooser(String identifier, String controlLabel) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.DIRECTORY,null,null,"Choose Directory");
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a directory.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param initialDirectory A string containing a directory path which will be the initially selected directory when selection dialog is shown.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendDirectoryChooser(String identifier, String controlLabel, String initialDirectory) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.DIRECTORY,null,null,"Choose Directory");
		component.setInitialDirectory(initialDirectory);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a directory.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param callback Callback which will be invoked when user selects a file using the choose button
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendDirectoryChooser(String identifier, String controlLabel, PathSelectedCallback callback) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.DIRECTORY,null,null,"Choose Directory");
		component.whenPathSelected(callback);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a directory.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param initialDirectory A string containing a directory path which will be the initially selected directory when selection dialog is shown.
	 * @param callback Callback which will be invoked when user selects a file using the choose button
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendDirectoryChooser(String identifier, String controlLabel, String initialDirectory,
			PathSelectedCallback callback) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.DIRECTORY,null,null,"Choose Directory");
		component.setInitialDirectory(initialDirectory);
		component.whenPathSelected(callback);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a file to open.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param fileTypeName The name portion of the file type filter.
	 * @param fileExtension The extension (without period) to filter the visible files on.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendOpenFileChooser(String identifier, String controlLabel, String fileTypeName, String fileExtension) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.OPEN_FILE,fileTypeName,fileExtension,"Choose Existing File");
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a file to open.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param fileTypeName The name portion of the file type filter.
	 * @param fileExtension The extension (without period) to filter the visible files on.
	 * @param initialDirectory A string containing a directory path which will be the initially selected directory when selection dialog is shown.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendOpenFileChooser(String identifier, String controlLabel, String fileTypeName, String fileExtension,
			String initialDirectory) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.OPEN_FILE,fileTypeName,fileExtension,"Choose Existing File");
		component.setInitialDirectory(initialDirectory);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a file to open.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param fileTypeName The name portion of the file type filter.
	 * @param fileExtension The extension (without period) to filter the visible files on.
	 * @param callback Callback which will be invoked when user selects a file using the choose button
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendOpenFileChooser(String identifier, String controlLabel, String fileTypeName, String fileExtension, PathSelectedCallback callback) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.OPEN_FILE,fileTypeName,fileExtension,"Choose Existing File");
		component.whenPathSelected(callback);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a file to open.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param fileTypeName The name portion of the file type filter.
	 * @param fileExtension The extension (without period) to filter the visible files on.
	 * @param initialDirectory A string containing a directory path which will be the initially selected directory when selection dialog is shown.
	 * @param callback Callback which will be invoked when user selects a file using the choose button
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendOpenFileChooser(String identifier, String controlLabel, String fileTypeName, String fileExtension, String initialDirectory,
			PathSelectedCallback callback) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.OPEN_FILE,fileTypeName,fileExtension,"Choose Existing File");
		component.setInitialDirectory(initialDirectory);
		component.whenPathSelected(callback);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a file to save.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param fileTypeName The name portion of the file type filter.
	 * @param fileExtension The extension (without period) to filter the visible files on.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendSaveFileChooser(String identifier, String controlLabel, String fileTypeName, String fileExtension) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.SAVE_FILE,fileTypeName,fileExtension,"Choose File");
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a control which allows a user to select a file to save.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label for this control.
	 * @param fileTypeName The name portion of the file type filter.
	 * @param fileExtension The extension (without period) to filter the visible files on.
	 * @param initialDirectory A string containing a directory path which will be the initially selected directory when selection dialog is shown.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendSaveFileChooser(String identifier, String controlLabel, String fileTypeName, String fileExtension,
			String initialDirectory) throws Exception{
		PathSelectionControl component = new PathSelectionControl(ChooserType.SAVE_FILE,fileTypeName,fileExtension,"Choose File");
		component.setInitialDirectory(initialDirectory);
		addBasicLabelledComponent(controlLabel, component);
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a list box allowing the user to specify multiple file and directory paths.
	 * @param identifier The unique identifier for this control.
	 * @param initialPaths Initial values to populate the list with, can be null.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendPathList(String identifier, List<String> initialPaths) throws Exception{
		PathList component = new PathList();
		if(initialPaths != null)
			component.setPaths(initialPaths);
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a list box allowing the user to specify multiple file and directory paths.
	 * @param identifier The unique identifier for this control.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendPathList(String identifier) throws Exception{
		return appendPathList(identifier,null);
	}
	
	/***
	 * Appends a list box allowing the user to specify string values.
	 * @param identifier The unique identifier for this control.
	 * @param initialValues Initial values to populate the list with, can be null.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendStringList(String identifier, List<String> initialValues) throws Exception{
		StringList component = new StringList();
		if(initialValues != null)
			component.setValues(initialValues);
		addComponent(component,controls.size()+headersCount,0,2,1,true);
		headersCount++;
		trackComponent(identifier, component);
		return this;
	}
	
	/***
	 * Appends a list box allowing the user to specify string values.
	 * @param identifier The unique identifier for this control.
	 * @return Returns this CustomTabPanel instance to allow for method chaining.
	 * @throws Exception Exception May throw an exception if the provided identifier has already been used.
	 */
	public CustomTabPanel appendStringList(String identifier) throws Exception{
		return appendStringList(identifier,null);
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
		else if(component instanceof ComboItemBox){
			return ((ComboItemBox)component).getSelectedValue();
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
	 * Sets the date contained in a date picker control previously added through a call to {@link #appendDatePicker(String, String)} or 
	 * {@link #appendDatePicker(String, String, Object)}.
	 * @param identifier The unique identifier of the previously added date picker control.
	 * @param value The value to set the date picker to.  Accepts values of {@link java.util.Date}, {@link org.joda.time.DateTime} or a date
	 * String formatted "yyyyMMdd".
	 * @throws Exception May be thrown if identifier does not point to a valid/existing control or date format string is invalid.
	 */
	public void setDate(String identifier, Object value) throws Exception{
		Component component = controls.get(identifier);
		if(component instanceof JXDatePicker){
			JXDatePicker datePickerComponent = (JXDatePicker)component;
			if(value != null){
				if(value instanceof Date){
					datePickerComponent.setDate((Date) value);
				}
				else if (value instanceof DateTime){
					datePickerComponent.setDate(((DateTime)value).toDate());
				}
				else if (value instanceof String){
					DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
					Date date = dateFormat.parse((String) value);
					datePickerComponent.setDate((Date) date);
				}
			}
		}
		else
			throw new Exception("Control for identifier '"+identifier+"' is not a supported control type: JXDatePicker");
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
		for(String identifier : controls.keySet()){
			// Only skip serializing fields marked to be skipped and
			// if we are serializing control values to build JSON
			if(forJsonCreation && skipSerializing.contains(identifier)){
				continue;
			}
			Component component = controls.get(identifier);
			Object value = null;
			ControlSerializationHandler handler = owner.getSerializer(identifier);
			if(handler != null && forJsonCreation){
				value = handler.serializeControlData(component);
			}
			else if(component instanceof JCheckBox){
				value = ((JCheckBox)component).isSelected();	
			}
			else if(component instanceof JRadioButton){
				value = ((JRadioButton)component).isSelected();
			}
			else if(component instanceof JPasswordField){
				value = new String(((JPasswordField)component).getPassword());
			}
			else if (component instanceof JTextField){
				value = ((JTextField)component).getText();
			}
			else if (component instanceof ChoiceTableControl<?>){
				if(forJsonCreation)
					value = ((ChoiceTableControl<?>)component).getTableModel().getCheckedLabels();
				else
					value = ((ChoiceTableControl<?>)component).getTableModel().getCheckedValues();
			}
			else if(component instanceof PathSelectionControl){
				value = ((PathSelectionControl)component).getPath();
			}
			else if(component instanceof ComboItemBox){
				value = ((ComboItemBox)component).getSelectedComboItem().getValue();
			}
			else if(component instanceof JComboBox<?>){
				value = ((JComboBox<?>)component).getSelectedItem();
			}
			else if(component instanceof JTextArea){
				value = ((JTextArea)component).getText();
			}
			else if(component instanceof JXDatePicker){
				value = ((JXDatePicker)component).getDate();
			}
			else if(component instanceof PathList){
				value = ((PathList)component).getPaths();
			}
			else if(component instanceof StringList){
				value = ((StringList)component).getValues();
			}
			else if(component instanceof JSpinner){
				value = ((JSpinner)component).getValue();
			}
			else if(component instanceof DynamicTableControl){
				if(forJsonCreation){
					value = ((DynamicTableControl)component).getTableModel().getCheckedRecordHashes();
				}else{
					value = ((DynamicTableControl)component).getTableModel().getCheckedRecords();	
				}
			}
			else if(component instanceof LocalWorkerSettings){
				Map<String,Object> workerSettings = new HashMap<String,Object>();
				LocalWorkerSettings lws = ((LocalWorkerSettings)component);
				workerSettings.put("workerCount",lws.getWorkerCount());
				workerSettings.put("workerMemory",lws.getMemoryPerWorker());
				workerSettings.put("workerTemp",lws.getWorkerTempDirectory());
				value = workerSettings;
			}
			else if(component instanceof BatchExporterTraversalSettings){
				Map<String,Object> traversalSettings = new HashMap<String,Object>();
				BatchExporterTraversalSettings control = ((BatchExporterTraversalSettings)component);
				traversalSettings.put("strategy",control.getComboTraversal().getSelectedValue());
				traversalSettings.put("deduplication",control.getComboDedupe().getSelectedValue());
				traversalSettings.put("sortOrder",control.getComboSortOrder().getSelectedValue());
				value = traversalSettings;
			}
			else if(component instanceof BatchExporterNativeSettings){
				Map<String,Object> nativeSettings = new HashMap<String,Object>();
				BatchExporterNativeSettings control = ((BatchExporterNativeSettings)component);
				nativeSettings.put("naming",control.getComboNaming().getSelectedValue());
				nativeSettings.put("path",control.getTxtPath().getText());
				nativeSettings.put("suffix",control.getTxtSuffix().getText());
				
				nativeSettings.put("mailFormat",control.getComboMailFormat().getSelectedValue());
				nativeSettings.put("includeAttachments",control.getChckbxIncludeAttachments().isSelected());
				nativeSettings.put("regenerateStored",control.getChckbxRegenerateStored().isSelected());
				
				value = nativeSettings;
			}
			else if(component instanceof BatchExporterTextSettings){
				Map<String,Object> textSettings = new HashMap<String,Object>();
				BatchExporterTextSettings control = ((BatchExporterTextSettings)component);
				textSettings.put("naming",control.getComboNaming().getSelectedValue());
				textSettings.put("path",control.getTxtPath().getText());
				textSettings.put("suffix",control.getTxtSuffix().getText());
				
				if(forJsonCreation){
					textSettings.put("wrapLinesChecked",control.getChckbxWrapLines().isSelected());
					textSettings.put("wrapLines",(Integer)control.getSpinnerWrapLength().getValue());
				}
				else {
					if(control.getChckbxWrapLines().isSelected()){
						textSettings.put("wrapLines",(Integer)control.getSpinnerWrapLength().getValue());
					}
				}
				textSettings.put("perPage",control.getChckbxPerPage().isSelected());
				textSettings.put("lineSeparator",control.getComboLineSeparator().getSelectedValue());
				textSettings.put("encoding",control.getComboEncoding().getSelectedValue());
				
				value = textSettings;
			}
			else if(component instanceof BatchExporterPdfSettings){
				Map<String,Object> pdfSettings = new HashMap<String,Object>();
				BatchExporterPdfSettings control = ((BatchExporterPdfSettings)component);
				pdfSettings.put("naming",control.getComboNaming().getSelectedValue());
				pdfSettings.put("path",control.getTxtPath().getText());
				pdfSettings.put("suffix",control.getTxtSuffix().getText());
				
				pdfSettings.put("regenerateStored",control.getChckbxRegenerateStored().isSelected());
				
				value = pdfSettings;
			}
			else if(component instanceof BatchExporterLoadFileSettings){
				Map<String,Object> lfSettings = new HashMap<String,Object>();
				BatchExporterLoadFileSettings control = ((BatchExporterLoadFileSettings)component);
				lfSettings.put("type",control.getComboLoadFileType().getSelectedValue());
				lfSettings.put("metadataProfile",control.getComboProfile().getSelectedValue());
				lfSettings.put("encoding",control.getComboEncoding().getSelectedValue());
				lfSettings.put("lineSeparator",control.getComboLineSeparator().getSelectedValue());
				
				value = lfSettings;
			}
			else if(component instanceof OcrSettings){
				Map<String,Object> ocrSettings = new HashMap<String,Object>();
				OcrSettings control = ((OcrSettings)component);
				ocrSettings.put("regeneratePdfs",control.getChckbxRegeneratePdfs().isSelected());
				ocrSettings.put("updatePdf",control.getChckbxUpdatePdfText().isSelected());
				ocrSettings.put("updateText",control.getChckbxUpdateItemText().isSelected());
				ocrSettings.put("textModification",control.getComboTextModification().getSelectedValue());
				ocrSettings.put("quality",control.getComboQuality().getSelectedValue());
				ocrSettings.put("rotation",control.getComboRotation().getSelectedValue());
				ocrSettings.put("deskew",control.getChckbxDeskew().isSelected());
				ocrSettings.put("outputDirectory",control.getOutputDirectory().getPath());
				ocrSettings.put("languages",control.getLanguageChoices().getTableModel().getCheckedValues());
				if(NuixConnection.getCurrentNuixVersion().isAtLeast("7.2.0")){
					ocrSettings.put("updateDuplicates", control.getUpdateDuplicates());
					ocrSettings.put("timeout", control.getTimeoutMinutes());
				}
				value = ocrSettings;
			} else if (component instanceof CsvTable){
				CsvTable table = (CsvTable)component;
				value = table.getRecords();
			}
			
			result.put(identifier, value);
		}
		return result;
	}
	
	/***
	 * Gets a JSON String equivalent of the dialog's values.  This is a convenience method for calling
	 * {@link #toMap} and then converting that Map to a JSON string.
	 * @return A JSON string representation of the dialogs values (based on the Map returned by {@link toMap}).
	 */
	public String toJson(){
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.toJson(toMap());
	}
	
	/**
	 * @return The height of choice table controls
	 */
	public int getChoiceTableHeight() {
		return choiceTableHeight;
	}

	/**
	 * @param choiceTableHeight The choiceTableHeight to set
	 */
	public void setChoiceTableHeight(int choiceTableHeight) {
		this.choiceTableHeight = choiceTableHeight;
	}

	/***
	 * Gets the label of this tab.
	 * @return The label of this tab.
	 */
	public String getLabel() {
		return label;
	}

	/***
	 * Sets the label of this tab.
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/***
	 * Allows you to specify code which customizes how a particular control's value is serialized.
	 * @param identifier The unique identifier of the control to which you are providing custom serialization logic for.
	 * @param handler Provides logic regarding how to serialize the control's value.
	 */
	public void whenSerializing(String identifier, ControlSerializationHandler handler){
		owner.whenSerializing(identifier,handler);
	}
	
	/***
	 * Allows you to specify code which customizes how a particular control's value is deserialized.
	 * @param identifier The unique identifier of the control to which you are providing custom deserialization logic for.
	 * @param handler Provides logic regarding how to deserialize the control's value.
	 */
	public void whenDeserializing(String identifier, ControlDeserializationHandler handler){
		owner.whenDeserializing(identifier,handler);
	}

	@Override
	public void setEnabled(boolean value) {
		if(value == true){
			if(enabledStates != null){
				for(Map.Entry<String,Boolean> state : enabledStates.entrySet()){
					getControl(state.getKey()).setEnabled(state.getValue());
				}
				enabledStates = null;
			}
		} else {
			enabledStates = new HashMap<String,Boolean>();
			for(Map.Entry<String,Component> controlEntry : controls.entrySet()){
				enabledStates.put(controlEntry.getKey(),controlEntry.getValue().isEnabled());
			}
			for(Component c : getComponents()){
				c.setEnabled(false);
			}
		}
	}

	/***
	 * Allows you to specify that a particular control's value should not be serialized in calls to {@link #toMap(boolean)} with a value of true (meaning it is generating map of setting for generating JSON).
	 * @param identifier The unique identified of the control to be skipped.
	 */
	public void doNotSerialize(String identifier){
		skipSerializing.add(identifier);
	}
	
	/***
	 * Registers a callback which is notified when a particular text control's value is modified.
	 * @param identifier The unique of identifier of the text control to monitor.
	 * @param callback Callback invoked when the text control's value is modified.
	 * @throws Exception May be thrown if identifier does not refer to a supported/existing control.
	 */
	public void whenTextChanged(String identifier, Consumer<String> callback) throws Exception{
		Component component = controls.get(identifier);
		if(component instanceof JTextComponent){
			JTextComponent textComponent = ((JTextComponent)component); 
			textComponent.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					updated();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					updated();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					updated();
				}
				
				private void updated(){
					callback.accept(textComponent.getText());
				}
			});
		}
		else
			throw new Exception("Control for identifier '"+identifier+"' is not a supported control type: JPasswordField, JTextField, PathSelectionControl, JTextArea");
	}
}
