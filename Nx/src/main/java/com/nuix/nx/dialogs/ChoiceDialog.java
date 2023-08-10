/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import nuix.Case;
import nuix.Item;
import nuix.ItemKind;
import nuix.ItemSet;
import nuix.ProductionSet;

import com.nuix.nx.NuixConnection;
import com.nuix.nx.controls.models.Choice;
import com.nuix.nx.controls.models.ChoiceTableModel;
import java.awt.Toolkit;

/***
 * Displays a dialog allowing a user to select one or more choices.
 * @author Jason Wells
 *
 * @param <T> The data type of the choice values.  Allows any value to be supported as a choice value.
 */
@SuppressWarnings("serial")
public class ChoiceDialog<T> extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private boolean dialogResult = false;
	private JTable choiceTable;
	private ChoiceTableModel<T> tableModel;

	/***
	 * Create a new instance
	 * @param valueTypeName Determines what the label of the value column will be.
	 */
	public ChoiceDialog(String valueTypeName) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(ChoiceDialog.class.getResource("/icons/nuix_icon.png")));
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(400,300);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		tableModel = new ChoiceTableModel<T>();
		tableModel.setChoiceTypeName(valueTypeName);
		choiceTable = new JTable(tableModel){

		            //Implement table cell tool tips.           
		            public String getToolTipText(MouseEvent e) {
		                String tip = null;
		                java.awt.Point p = e.getPoint();
		                int rowIndex = rowAtPoint(p);
		                try {
		                    //comment row, exclude heading
		                    if(rowIndex != 0){
		                      tip = tableModel.getDisplayedChoice(rowIndex).getToolTip();
		                    }
		                } catch (RuntimeException e1) {
		                    //catch null pointer exception if mouse is over an empty line
		                }

		                return tip;
		            }
		        };
		contentPanel.add(choiceTable, BorderLayout.NORTH);
		
		TableColumn checkColumn = choiceTable.getColumnModel().getColumn(0);
		checkColumn.setMinWidth(30);
		checkColumn.setMaxWidth(30);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ChoiceDialog.this.dialogResult = true;
						ChoiceDialog.this.dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ChoiceDialog.this.dialogResult = false;
						ChoiceDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	/***
	 * Provides a value signifying whether the dialog was cancelled/closed or user hit ok.
	 * @return True if user selected ok button, cancel otherwise.
	 */
	public boolean getDialogResult() {
		return dialogResult;
	}

	public ChoiceTableModel<T> getTableModel() {
		return tableModel;
	}

	public void setTableModel(ChoiceTableModel<T> tableModel) {
		this.tableModel = tableModel;
	}
	
	/***
	 * Shows a choice dialog for a provided collection of values.
	 * @param choiceValues A collection of values to show the user.
	 * @param typeName Determines the label placed on the second column.
	 * @param title Determines the title of the dialog.
	 * @return A list of selected values, or null if the user cancels or closes the dialog.
	 * @param <T> The data type of the choice values.  Allows any value to be supported as a choice value.
	 */
	public static <T> List<T> forValues(Collection<T> choiceValues, String typeName, String title){
		ChoiceDialog<T> dialog = new ChoiceDialog<T>(typeName);
		dialog.setTitle(title);
		List<Choice<T>> choices = new ArrayList<Choice<T>>();
		for(T value : choiceValues){
			choices.add(new Choice<T>(value));
		}
		return forChoices(choices,typeName,title);
	}
	
	/***
	 * Shows a choice dialog for a provided list of choices.
	 * @param choices A collection of choices to show the user.
	 * @param typeName Determines the label placed on the second column.
	 * @param title Determines the title of the dialog.
	 * @param singleSelectMode Whether we wish to restrict user to a single selection
	 * @return A list of selected values, or null if the user cancels or closes the dialog.
	 * @param <T> The data type of the choice values.  Allows any value to be supported as a choice value.
	 */
	public static <T> List<T> forChoices(List<Choice<T>> choices, String typeName, String title, boolean singleSelectMode){
		ChoiceDialog<T> dialog = new ChoiceDialog<T>(typeName);
		dialog.setTitle(title);
		dialog.getTableModel().setChoices(choices);
		dialog.getTableModel().setSingleSelectMode(singleSelectMode);
		// With single select mode lets check first choice by default
		if(singleSelectMode && choices.size() >= 1) {
			dialog.getTableModel().setValueAt(true, 0, 0);	
		}
		dialog.setVisible(true);
		if(dialog.getDialogResult() == true)
			return dialog.tableModel.getCheckedValues();
		else
			return null;
	}
	
	/***
	 * Shows a choice dialog for a provided list of choices.
	 * @param choices A collection of choices to show the user.
	 * @param typeName Determines the label placed on the second column.
	 * @param title Determines the title of the dialog.
	 * @return A list of selected values, or null if the user cancels or closes the dialog.
	 * @param <T> The data type of the choice values.  Allows any value to be supported as a choice value.
	 */
	public static <T> List<T> forChoices(List<Choice<T>> choices, String typeName, String title){
		return forChoices(choices,typeName,title,false);
	}
	
	/***
	 * Presents the user with a dialog where they can select tag names.
	 * @param nuixCase The case to obtain the list of possible tags from.  If null is provided will attempt to use result of {@link NuixConnection#getCurrentCase()}
	 * @return A list of selected tag names, or null if the user cancelled or closed the dialog.
	 * @throws IOException May throw exception caused by call into Nuix API
	 */
	public static List<String> forTags(Case nuixCase) throws IOException{
		if(nuixCase == null)
			nuixCase = NuixConnection.getCurrentCase();
		return forValues(nuixCase.getAllTags(),"Tag","Select Tags");
	}
	
	/***
	 * Presents the user with a dialog where they can select custodian names.
	 * @param nuixCase The case to obtain the list of possible custodian names from.  If null is provided will attempt to use result of {@link NuixConnection#getCurrentCase()}
	 * @return A list of selected custodian names, or null if the user cancelled or closed the dialog.
	 * @throws IOException May throw exception caused by call into Nuix API
	 */
	public static List<String> forCustodians(Case nuixCase) throws IOException{
		if(nuixCase == null)
			nuixCase = NuixConnection.getCurrentCase();
		return forValues(nuixCase.getAllCustodians(),"Custodian","Select Custodians");
	}
	
	/***
	 * Presents the user with a dialog where they can select productions sets.
	 * @param nuixCase The case to obtain the list of possible production sets from.  If null is provided will attempt to use result of {@link NuixConnection#getCurrentCase()}
	 * @return A list of selected production sets, or null if the user cancelled or closed the dialog.
	 * @throws IOException May throw exception caused by call into Nuix API
	 */
	public static List<ProductionSet> forProductionSets(Case nuixCase) throws IOException{
		if(nuixCase == null)
			nuixCase = NuixConnection.getCurrentCase();
		return forValues(nuixCase.getProductionSets(),"Production Set","Select Production Sets");
	}
	
	/***
	 * Presents the user with a dialog where they can select item sets.
	 * @param nuixCase The case to obtain the list of possible item sets from.  If null is provided will attempt to use result of {@link NuixConnection#getCurrentCase()}
	 * @return A list of selected item sets, or null if the user cancelled or closed the dialog.
	 * @throws IOException May throw exception caused by call into Nuix API
	 */
	public static List<ItemSet> forItemSets(Case nuixCase) throws IOException{
		List<Choice<ItemSet>> choices = new ArrayList<Choice<ItemSet>>();
		if(nuixCase == null)
			nuixCase = NuixConnection.getCurrentCase();
		for(ItemSet itemSet : nuixCase.getAllItemSets()){
			choices.add(new Choice<ItemSet>(itemSet,itemSet.getName()));
		}
		return forChoices(choices,"Item Set","Selected Item Sets");
	}
	
	/***
	 * Presents the user with a dialog where they can select evidence items.
	 * @param nuixCase The case to obtain the list of possible evidence items from.  If null is provided will attempt to use result of {@link NuixConnection#getCurrentCase()}
	 * @return A list of selected evidence items, or null if the user cancelled or closed the dialog.
	 * @throws IOException May throw exception caused by call into Nuix API
	 */
	public static List<Item> forEvidenceItems(Case nuixCase) throws IOException{
		List<Choice<Item>> choices = new ArrayList<Choice<Item>>();
		if(nuixCase == null)
			nuixCase = NuixConnection.getCurrentCase();
		for(Item item : nuixCase.getRootItems()){
			choices.add(new Choice<Item>(item,item.getName()));
		}
		return forChoices(choices,"Evidence","Selected Evidence");
	}
	
	/***
	 * Presents the user with a dialog where they can select item kinds.
	 * @return A list of selected item kinds, or null if the user cancelled or closed the dialog.
	 */
	public static List<ItemKind> forKinds(){
		return forValues(NuixConnection.getUtilities().getItemTypeUtility().getAllKinds(),"Item Kind","Select Item Kinds");
	}
}
