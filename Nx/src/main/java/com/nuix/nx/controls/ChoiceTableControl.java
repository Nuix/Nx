/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import com.nuix.nx.controls.models.Choice;
import com.nuix.nx.controls.models.ChoiceTableModel;
import com.nuix.nx.controls.models.ChoiceTableModelChangeListener;

import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/***
 * A table control for allowing the user to check one or more choices which are defined
 * using {@link Choice} objects.
 * @author Jason Wells
 *
 * @param <T> The data type of the of the {@link Choice} instances which will be displayed by this table.
 */
@SuppressWarnings("serial")
public class ChoiceTableControl<T> extends JPanel {

	private JTable choiceTable;
	private ChoiceTableModel<T> tableModel;
	private JTextField txtFilter;
	private JButton btnClearFilter;
	private JToolBar toolBar;
	private JButton btnCheckDisplayed;
	private JButton btnUncheckDisplayed;
	private JScrollPane scrollPane;
	private JLabel lblLblcounts;
	private JButton btnSortup;
	private JButton btnSortdown;
	private JButton btnSortcheckedtotop;
	private JButton btnShowChecked;
	private JButton btnShowunchecked;
	private JSeparator separator;
	private JSeparator separator_1;
	private JLabel lblFilter;
	
	public ChoiceTableControl(){
		this("");
	}
	
	public ChoiceTableControl(String choiceTypeName) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 450, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		{
			{
				tableModel = new ChoiceTableModel<T>();
				tableModel.setChoiceTypeName(choiceTypeName);
				tableModel.setChangeListener(new ChoiceTableModelChangeListener() {
					@Override
					public void dataChanged() {
						lblLblcounts.setText("Checked: " + tableModel.getCheckedValueCount() + 
								" Visible: " + tableModel.getVisibleValueCount() +
								" Total: " + tableModel.getTotalValueCount());
					}

					@Override
					public void structureChanged() {
						TableColumn checkColumn = choiceTable.getColumnModel().getColumn(0);
						checkColumn.setMinWidth(25);
						checkColumn.setMaxWidth(25);
					}
				});
				
				lblFilter = new JLabel("Filter:");
				GridBagConstraints gbc_lblFilter = new GridBagConstraints();
				gbc_lblFilter.insets = new Insets(0, 0, 5, 5);
				gbc_lblFilter.anchor = GridBagConstraints.EAST;
				gbc_lblFilter.gridx = 0;
				gbc_lblFilter.gridy = 0;
				add(lblFilter, gbc_lblFilter);
				
				txtFilter = new JTextField();
				GridBagConstraints gbc_txtFilter = new GridBagConstraints();
				gbc_txtFilter.insets = new Insets(0, 0, 5, 5);
				gbc_txtFilter.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtFilter.gridx = 1;
				gbc_txtFilter.gridy = 0;
				add(txtFilter, gbc_txtFilter);
				txtFilter.setColumns(10);
				
				toolBar = new JToolBar();
				toolBar.setFloatable(false);
				GridBagConstraints gbc_toolBar = new GridBagConstraints();
				gbc_toolBar.insets = new Insets(0, 0, 5, 0);
				gbc_toolBar.gridx = 2;
				gbc_toolBar.gridy = 0;
				add(toolBar, gbc_toolBar);
				
				btnShowChecked = new JButton("");
				btnShowChecked.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						txtFilter.setText(":checked:");
					}
				});
				
				btnClearFilter = new JButton("");
				btnClearFilter.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						txtFilter.setText("");
					}
				});
				toolBar.add(btnClearFilter);
				btnClearFilter.setToolTipText("Clear current filter");
				btnClearFilter.setIcon(new ImageIcon(ChoiceTableControl.class.getResource("/icons/zoom_out.png")));
				btnShowChecked.setToolTipText("Show all currently checked choices");
				btnShowChecked.setIcon(new ImageIcon(ChoiceTableControl.class.getResource("/icons/tick.png")));
				toolBar.add(btnShowChecked);
				
				btnShowunchecked = new JButton("");
				btnShowunchecked.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						txtFilter.setText(":unchecked:");
					}
				});
				btnShowunchecked.setToolTipText("Show all currently un-checked choices");
				btnShowunchecked.setIcon(new ImageIcon(ChoiceTableControl.class.getResource("/icons/cross.png")));
				toolBar.add(btnShowunchecked);
				
				btnCheckDisplayed = new JButton();
				btnCheckDisplayed.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tableModel.checkDisplayedChoices();
					}
				});
				
				separator = new JSeparator();
				separator.setOrientation(SwingConstants.VERTICAL);
				toolBar.add(separator);
				btnCheckDisplayed.setIcon(new ImageIcon(ChoiceTableControl.class.getResource("/icons/accept.png")));
				btnCheckDisplayed.setToolTipText("Check all visible");
				toolBar.add(btnCheckDisplayed);
				
				btnUncheckDisplayed = new JButton("");
				btnUncheckDisplayed.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tableModel.uncheckDisplayedChoices();
					}
				});
				btnUncheckDisplayed.setToolTipText("Uncheck all visible");
				btnUncheckDisplayed.setIcon(new ImageIcon(ChoiceTableControl.class.getResource("/icons/unaccept.png")));
				toolBar.add(btnUncheckDisplayed);
				
				btnSortup = new JButton("");
				btnSortup.setToolTipText("Move selected row up.  Only available when no filtering is currently applied to the table.");
				btnSortup.setIcon(new ImageIcon(ChoiceTableControl.class.getResource("/icons/arrow_up.png")));
				btnSortup.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						int[] rows = new int[]{choiceTable.getSelectedRow()};
						rows = tableModel.shiftRowsUp(rows);
						choiceTable.setRowSelectionInterval(rows[0], rows[rows.length-1]);
					}
				});
				
				separator_1 = new JSeparator();
				separator_1.setOrientation(SwingConstants.VERTICAL);
				toolBar.add(separator_1);
				toolBar.add(btnSortup);
				
				btnSortdown = new JButton("");
				btnSortdown.setToolTipText("Move selected row down.  Only available when no filtering is currently applied to the table.");
				btnSortdown.setIcon(new ImageIcon(ChoiceTableControl.class.getResource("/icons/arrow_down.png")));
				btnSortdown.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						int[] rows = new int[]{choiceTable.getSelectedRow()};
						rows = tableModel.shiftRowsDown(rows);
						choiceTable.setRowSelectionInterval(rows[0], rows[rows.length-1]);
					}
				});
				toolBar.add(btnSortdown);
				
				btnSortcheckedtotop = new JButton("");
				btnSortcheckedtotop.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tableModel.sortCheckedToTop();
					}
				});
				btnSortcheckedtotop.setIcon(new ImageIcon(ChoiceTableControl.class.getResource("/icons/bullet_arrow_top.png")));
				btnSortcheckedtotop.setToolTipText("Sort checked items to top.  Only available when no filtering is currently applied to the table.");
				toolBar.add(btnSortcheckedtotop);
				
				
				scrollPane = new JScrollPane();
				GridBagConstraints gbc_scrollPane = new GridBagConstraints();
				gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
				gbc_scrollPane.gridwidth = 3;
				gbc_scrollPane.fill = GridBagConstraints.BOTH;
				gbc_scrollPane.gridx = 0;
				gbc_scrollPane.gridy = 1;
				add(scrollPane, gbc_scrollPane);
				choiceTable = new JTable(tableModel){

		            //Implement table cell tool tips.           
		            public String getToolTipText(MouseEvent e) {
		                String tip = null;
		                java.awt.Point p = e.getPoint();
		                int rowIndex = rowAtPoint(p);
		                //int colIndex = columnAtPoint(p);
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
				choiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				choiceTable.setFillsViewportHeight(true);
				scrollPane.setViewportView(choiceTable);
				
				lblLblcounts = new JLabel("Checked: 0 Visible: 0 Total: 0");
				GridBagConstraints gbc_lblLblcounts = new GridBagConstraints();
				gbc_lblLblcounts.gridwidth = 2;
				gbc_lblLblcounts.anchor = GridBagConstraints.WEST;
				gbc_lblLblcounts.insets = new Insets(0, 0, 0, 5);
				gbc_lblLblcounts.gridx = 0;
				gbc_lblLblcounts.gridy = 2;
				add(lblLblcounts, gbc_lblLblcounts);
				constrainFirstColumn();
				choiceTable.getModel().addTableModelListener(new TableModelListener() {
					@Override
					public void tableChanged(TableModelEvent e) {
						constrainFirstColumn();
					}
				});
			}
		}
		
		txtFilter.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
				  String filterText = txtFilter.getText();
				  boolean sortEnabled = filterText.length() == 0;
				  btnSortup.setEnabled(sortEnabled);
				  btnSortdown.setEnabled(sortEnabled);
				  btnSortcheckedtotop.setEnabled(sortEnabled);
				  tableModel.setFilter(filterText);
			  }
			  
			  public void removeUpdate(DocumentEvent e) {
				  String filterText = txtFilter.getText();
				  boolean sortEnabled = filterText.length() == 0;
				  btnSortup.setEnabled(sortEnabled);
				  btnSortdown.setEnabled(sortEnabled);
				  btnSortcheckedtotop.setEnabled(sortEnabled);
				  tableModel.setFilter(filterText);
			  }

			  public void insertUpdate(DocumentEvent e) {
				  String filterText = txtFilter.getText();
				  boolean sortEnabled = filterText.length() == 0;
				  btnSortup.setEnabled(sortEnabled);
				  btnSortdown.setEnabled(sortEnabled);
				  btnSortcheckedtotop.setEnabled(sortEnabled);
				  tableModel.setFilter(filterText);
			  }
		});
	}
	
	public void constrainFirstColumn(){
		TableColumn checkColumn = choiceTable.getColumnModel().getColumn(0);
		checkColumn.setMinWidth(25);
		checkColumn.setWidth(25);
		checkColumn.setPreferredWidth(25);
		checkColumn.setMaxWidth(25);
	}

	public ChoiceTableModel<T> getTableModel() {
		return tableModel;
	}

	public void setTableModel(ChoiceTableModel<T> model) {
		tableModel = model;
	}
	
	public void setChoices(List<Choice<T>> choices){
		tableModel.setChoices(choices);
	}
	
	public void setValues(List<T> values){
		List<Choice<T>> choices = new ArrayList<Choice<T>>();
		for(T value : values){
			choices.add(new Choice<T>(value));
		}
		setChoices(choices);
	}
	
	public void setEnabled(boolean value){
		choiceTable.setEnabled(value);
		choiceTable.setVisible(value);
		if(value)
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		else
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		txtFilter.setEnabled(value);
		btnClearFilter.setEnabled(value);
		toolBar.setEnabled(value);
		btnCheckDisplayed.setEnabled(value);
		btnUncheckDisplayed.setEnabled(value);
		String filterText = txtFilter.getText();
		boolean sortEnabled = filterText.length() == 0;
		btnSortup.setEnabled(sortEnabled && value);
		btnSortdown.setEnabled(sortEnabled && value);
		btnSortcheckedtotop.setEnabled(sortEnabled && value);
	}
	
	public void setFilter(String filterString){
		tableModel.setFilter(filterString);
	}
	
	public void fitColumns(){
		this.choiceTable.sizeColumnsToFit(1);
	}
}
