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
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXTable;

import com.nuix.nx.controls.models.ChoiceTableModelChangeListener;
import com.nuix.nx.controls.models.DynamicTableModel;
import com.nuix.nx.controls.models.DynamicTableValueCallback;

/***
 * Control for displaying a dynamically defined table of data.  Meant to mostly make this a little easier
 * from a Ruby script.
 * @author JWells01
 *
 */
@SuppressWarnings("serial")
public class DynamicTableControl extends JPanel {

	private JXTable dataTable;
	private DynamicTableModel tableModel;
	private JTextField txtFilter;
	private JButton btnClearFilter;
	private JToolBar toolBar;
	private JButton btnCheckDisplayed;
	private JButton btnUncheckDisplayed;
	private JScrollPane scrollPane;
	private JLabel lblLblcounts;
	private JButton btnSortup;
	private JButton btnSortdown;
	//private JButton btnSortcheckedtotop;
	private JButton btnShowChecked;
	private JButton btnShowunchecked;
	private JSeparator separator;
	private JSeparator separator_1;
	private JLabel lblFilter;
	private Timer filterUpdateTimer;
	private JSeparator separator_2;
	private JButton btnAddRecord;
	private Supplier<Object> addRecordCallback = null;
	private JButton btnRemoveSelected;
	
	public DynamicTableControl(List<String> headers, List<Object> records, DynamicTableValueCallback valueCallback) {
		filterUpdateTimer = new Timer(250, new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					// Possible for this to fire before were properly ready so we check for nulls
					if(tableModel != null && txtFilter != null) {
						tableModel.setFilter(txtFilter.getText());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		filterUpdateTimer.setRepeats(false);
		filterUpdateTimer.start();
		
		lblLblcounts = new JLabel("Checked: 0 Visible: 0 Total: 0");
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 450, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		{
			{
				tableModel = new DynamicTableModel(headers,records,valueCallback,false);
				tableModel.setChangeListener(new ChoiceTableModelChangeListener() {
					@Override
					public void dataChanged() {
						lblLblcounts.setText("Checked: " + tableModel.getCheckedValueCount() + 
								" Visible: " + tableModel.getVisibleValueCount() +
								" Total: " + tableModel.getTotalValueCount());
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
				btnClearFilter.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/zoom_out.png")));
				btnShowChecked.setToolTipText("Show all currently checked choices");
				btnShowChecked.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/tick.png")));
				toolBar.add(btnShowChecked);
				
				btnShowunchecked = new JButton("");
				btnShowunchecked.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						txtFilter.setText(":unchecked:");
					}
				});
				btnShowunchecked.setToolTipText("Show all currently un-checked choices");
				btnShowunchecked.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/cross.png")));
				toolBar.add(btnShowunchecked);
				
				btnCheckDisplayed = new JButton();
				btnCheckDisplayed.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tableModel.checkDisplayedRecords();
					}
				});
				
				separator = new JSeparator();
				separator.setOrientation(SwingConstants.VERTICAL);
				toolBar.add(separator);
				btnCheckDisplayed.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/accept.png")));
				btnCheckDisplayed.setToolTipText("Check all visible");
				toolBar.add(btnCheckDisplayed);
				
				btnUncheckDisplayed = new JButton("");
				btnUncheckDisplayed.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tableModel.uncheckDisplayedRecords();
					}
				});
				btnUncheckDisplayed.setToolTipText("Uncheck all visible");
				btnUncheckDisplayed.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/unaccept.png")));
				toolBar.add(btnUncheckDisplayed);
				
				btnSortup = new JButton("");
				btnSortup.setToolTipText("Move selected row up.  Only available when no filtering is currently applied to the table.");
				btnSortup.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/arrow_up.png")));
				btnSortup.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						int[] rows = dataTable.getSelectedRows();
						rows = tableModel.shiftRowsUp(rows);
						dataTable.setRowSelectionInterval(rows[0], rows[rows.length-1]);
					}
				});
				
				separator_1 = new JSeparator();
				separator_1.setOrientation(SwingConstants.VERTICAL);
				toolBar.add(separator_1);
				toolBar.add(btnSortup);
				
				btnSortdown = new JButton("");
				btnSortdown.setToolTipText("Move selected row down.  Only available when no filtering is currently applied to the table.");
				btnSortdown.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/arrow_down.png")));
				btnSortdown.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						int[] rows = dataTable.getSelectedRows();
						rows = tableModel.shiftRowsDown(rows);
						dataTable.setRowSelectionInterval(rows[0], rows[rows.length-1]);
					}
				});
				toolBar.add(btnSortdown);
				
				separator_2 = new JSeparator();
				separator_2.setOrientation(SwingConstants.VERTICAL);
				toolBar.add(separator_2);
				
				btnAddRecord = new JButton("");
				btnAddRecord.setEnabled(false);
				btnAddRecord.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(addRecordCallback != null){
							Object newRecord = addRecordCallback.get();
							getModel().addRecord(newRecord);
						}
					}
				});
				btnAddRecord.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/add.png")));
				toolBar.add(btnAddRecord);
				
				btnRemoveSelected = new JButton("");
				btnRemoveSelected.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int[] selectedIndices = dataTable.getSelectedRows();
						Arrays.sort(selectedIndices);
						ArrayUtils.reverse(selectedIndices);
						for(int index : selectedIndices){
							getModel().remove(index);
						}
					}
				});
				btnRemoveSelected.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/delete.png")));
				toolBar.add(btnRemoveSelected);
				
//				btnSortcheckedtotop = new JButton("");
//				btnSortcheckedtotop.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						tableModel.sortCheckedToTop();
//					}
//				});
//				btnSortcheckedtotop.setIcon(new ImageIcon(DynamicTableControl.class.getResource("/com/nuix/nx/controls/bullet_arrow_top.png")));
//				btnSortcheckedtotop.setToolTipText("Sort checked items to top.  Only available when no filtering is currently applied to the table.");
//				toolBar.add(btnSortcheckedtotop);
				
				
				scrollPane = new JScrollPane();
				GridBagConstraints gbc_scrollPane = new GridBagConstraints();
				gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
				gbc_scrollPane.gridwidth = 3;
				gbc_scrollPane.fill = GridBagConstraints.BOTH;
				gbc_scrollPane.gridx = 0;
				gbc_scrollPane.gridy = 1;
				add(scrollPane, gbc_scrollPane);
				dataTable = new JXTable(tableModel){

		            //Implement table cell tool tips.           
		            public String getToolTipText(MouseEvent e) {
		                String tip = null;
		                java.awt.Point p = e.getPoint();
		                int rowIndex = rowAtPoint(p);
		                //int colIndex = columnAtPoint(p);
		                try {
		                    //comment row, exclude heading
		                    if(rowIndex != 0){
		                      tip = "";//tableModel.getChoice(rowIndex).getToolTip();
		                    }
		                } catch (RuntimeException e1) {
		                    //catch null pointer exception if mouse is over an empty line
		                }

		                return tip;
		            }
		        };
				dataTable.setSortsOnUpdates(false);
				dataTable.setSortable(false);
				dataTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
				dataTable.setFillsViewportHeight(true);
				scrollPane.setViewportView(dataTable);
				
				GridBagConstraints gbc_lblLblcounts = new GridBagConstraints();
				gbc_lblLblcounts.gridwidth = 2;
				gbc_lblLblcounts.anchor = GridBagConstraints.WEST;
				gbc_lblLblcounts.insets = new Insets(0, 0, 0, 5);
				gbc_lblLblcounts.gridx = 0;
				gbc_lblLblcounts.gridy = 2;
				add(lblLblcounts, gbc_lblLblcounts);
				TableColumn checkColumn = dataTable.getColumnModel().getColumn(0);
				checkColumn.setMinWidth(25);
				checkColumn.setMaxWidth(25);
			}
		}
		
		txtFilter.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
				  String filterText = txtFilter.getText();
				  boolean sortEnabled = filterText.length() == 0;
				  btnSortup.setEnabled(sortEnabled);
				  btnSortdown.setEnabled(sortEnabled);
				  //btnSortcheckedtotop.setEnabled(sortEnabled);
				  filterUpdateTimer.stop();
				  filterUpdateTimer.start();
			  }
			  
			  public void removeUpdate(DocumentEvent e) {
				  String filterText = txtFilter.getText();
				  boolean sortEnabled = filterText.length() == 0;
				  btnSortup.setEnabled(sortEnabled);
				  btnSortdown.setEnabled(sortEnabled);
				  //btnSortcheckedtotop.setEnabled(sortEnabled);
				  filterUpdateTimer.stop();
				  filterUpdateTimer.start();
			  }

			  public void insertUpdate(DocumentEvent e) {
				  String filterText = txtFilter.getText();
				  boolean sortEnabled = filterText.length() == 0;
				  btnSortup.setEnabled(sortEnabled);
				  btnSortdown.setEnabled(sortEnabled);
				  //btnSortcheckedtotop.setEnabled(sortEnabled);
				  filterUpdateTimer.stop();
				  filterUpdateTimer.start();
			  }
		});
	}

	public DynamicTableModel getTableModel() {
		return tableModel;
	}

	public void setTableModel(DynamicTableModel model) {
		tableModel = model;
	}

	public void setEnabled(boolean value){
		dataTable.setEnabled(value);
		dataTable.setVisible(value);
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
		//btnSortcheckedtotop.setEnabled(sortEnabled && value);
	}
	
	public void setFilter(String filterString){
		txtFilter.setText(filterString);
		tableModel.setFilter(filterString);
	}
	
	public void setCheckedAtIndex(int index, boolean value){
		tableModel.setCheckedAtIndex(index, value);
	}
	
	public List<Object> getCheckedRecords(){
		return tableModel.getCheckedRecords();
	}
	
	public List<Object> getRecords(){
		return tableModel.getRecords();
	}
	
	public void setRecords(List<Object> records){
		tableModel.setRecords(records);
	}
	
	public DynamicTableModel getModel(){
		return tableModel;
	}
	
	public JXTable getTable(){
		return dataTable;
	}
	
	public void setUserCanAddRecords(boolean value,Supplier<Object> callback){
		if(value == true){
			addRecordCallback = callback;
		} else {
			addRecordCallback = null;
		}
		btnAddRecord.setEnabled(value);
		btnRemoveSelected.setEnabled(value);
	}
	
	public void setDefaultCheckState(boolean defaultCheckState) {
		tableModel.setDefaultCheckState(defaultCheckState);
	}

	public JButton getBtnAddRecord() {
		return btnAddRecord;
	}
}
