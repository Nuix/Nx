package com.nuix.nx.controls;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import com.jidesoft.combobox.CheckBoxListExComboBox;
import java.awt.GridBagConstraints;
import javax.swing.DefaultComboBoxModel;
import java.awt.Insets;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/***
 * A combo box (based on CheckBoxListExComboBox) which offers a drop down which allows the user to pick
 * multiple choices by checking them, rather than just the single choice of a traditional combo box.
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class MultipleChoiceComboBox extends JPanel {

	private CheckBoxListExComboBox comboBox;
	private String[] possibleChoices = new String[] {};

	public MultipleChoiceComboBox() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		comboBox = new CheckBoxListExComboBox();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		add(comboBox, gbc_comboBox);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_toolBar.gridx = 1;
		gbc_toolBar.gridy = 0;
		add(toolBar, gbc_toolBar);
		
		JButton btnCheckAll = new JButton("");
		btnCheckAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkAll();
			}
		});
		btnCheckAll.setIcon(new ImageIcon(MultipleChoiceComboBox.class.getResource("/icons/accept.png")));
		btnCheckAll.setToolTipText("Check all choices");
		toolBar.add(btnCheckAll);
		
		JButton btnUncheckAll = new JButton("");
		btnUncheckAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uncheckAll();
			}
		});
		btnUncheckAll.setIcon(new ImageIcon(MultipleChoiceComboBox.class.getResource("/icons/unaccept.png")));
		btnUncheckAll.setToolTipText("Uncheck all choices");
		toolBar.add(btnUncheckAll);

	}

	/***
	 * Sets the list of choices offered to the user.
	 * @param choices List of string choices.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setChoices(List<String> choices) {
		possibleChoices = choices.toArray(new String[] {});
		comboBox.setModel(new DefaultComboBoxModel(possibleChoices));
	}
	
	/***
	 * Sets which of the possible choices are currently checked.
	 * @param checkedChoices List of string choices to be checked, choices not in this list will be unchecked.
	 */
	public void setCheckedChoices(List<String> checkedChoices) {
		comboBox.setSelectedItem(checkedChoices.toArray(new String[] {}));
	}
	
	/***
	 * Gets a list of which choices are currently checked.
	 * @return List of choices currently checked.
	 */
	public List<String> getCheckedChoices() {
		List<String> result = new ArrayList<String>();
		Object[] selectedFields = comboBox.getSelectedObjects();
		for (int i = 0; i < selectedFields.length; i++) {
			result.add((String)selectedFields[i]);
		}
		return result;
	}
	
	/***
	 * Checks all available choices.
	 */
	public void checkAll() {
		comboBox.setSelectedItem(possibleChoices);
	}
	
	/***
	 * Unchecks all available choices.
	 */
	public void uncheckAll() {
		comboBox.setSelectedItem(new String[] {});
	}
}
