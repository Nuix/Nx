/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.awt.Component;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/***
 * A control which encapsulates some of the common traversal settings
 * @author Jason Wells
 */
@SuppressWarnings("serial")
public class BatchExporterTraversalSettings extends JPanel {
	private ComboItemBox comboTraversal;
	private ComboItemBox comboDedupe;
	private ComboItemBox comboSortOrder;
	public BatchExporterTraversalSettings() {
		setBorder(new TitledBorder(null, "Traversal Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblTraversalStategy = new JLabel("Traversal Stategy");
		GridBagConstraints gbc_lblTraversalStategy = new GridBagConstraints();
		gbc_lblTraversalStategy.insets = new Insets(0, 0, 5, 5);
		gbc_lblTraversalStategy.anchor = GridBagConstraints.EAST;
		gbc_lblTraversalStategy.gridx = 0;
		gbc_lblTraversalStategy.gridy = 0;
		add(lblTraversalStategy, gbc_lblTraversalStategy);
		
		comboTraversal = new ComboItemBox();
		GridBagConstraints gbc_comboTraversal = new GridBagConstraints();
		gbc_comboTraversal.insets = new Insets(0, 0, 5, 0);
		gbc_comboTraversal.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboTraversal.gridx = 1;
		gbc_comboTraversal.gridy = 0;
		add(comboTraversal, gbc_comboTraversal);
		
		JLabel lblDeduplication = new JLabel("Deduplication");
		GridBagConstraints gbc_lblDeduplication = new GridBagConstraints();
		gbc_lblDeduplication.anchor = GridBagConstraints.EAST;
		gbc_lblDeduplication.insets = new Insets(0, 0, 5, 5);
		gbc_lblDeduplication.gridx = 0;
		gbc_lblDeduplication.gridy = 1;
		add(lblDeduplication, gbc_lblDeduplication);
		
		comboDedupe = new ComboItemBox();
		GridBagConstraints gbc_comboDedupe = new GridBagConstraints();
		gbc_comboDedupe.insets = new Insets(0, 0, 5, 0);
		gbc_comboDedupe.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboDedupe.gridx = 1;
		gbc_comboDedupe.gridy = 1;
		add(comboDedupe, gbc_comboDedupe);
		
		JLabel lblSortOrder = new JLabel("Sort Order");
		GridBagConstraints gbc_lblSortOrder = new GridBagConstraints();
		gbc_lblSortOrder.anchor = GridBagConstraints.EAST;
		gbc_lblSortOrder.insets = new Insets(0, 0, 5, 5);
		gbc_lblSortOrder.gridx = 0;
		gbc_lblSortOrder.gridy = 2;
		add(lblSortOrder, gbc_lblSortOrder);
		
		comboSortOrder = new ComboItemBox();
		GridBagConstraints gbc_comboSortOrder = new GridBagConstraints();
		gbc_comboSortOrder.insets = new Insets(0, 0, 5, 0);
		gbc_comboSortOrder.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboSortOrder.gridx = 1;
		gbc_comboSortOrder.gridy = 2;
		add(comboSortOrder, gbc_comboSortOrder);

		comboTraversal.addValue("Items", "items");
		comboTraversal.addValue("Items and Descendants", "items_and_descendants");
		comboTraversal.addValue("Top Level Items", "top_level_items");
		comboTraversal.setSelectedIndex(0);
		
		comboDedupe.addValue("None","none");
		comboDedupe.addValue("MD5","md5");
		comboDedupe.addValue("MD5 per Custodian","md5_per_custodian");
		comboDedupe.setSelectedIndex(0);
		
		comboSortOrder.addValue("Result Set Order","none");
		comboSortOrder.addValue("Item Position","position");
		comboSortOrder.addValue("Top Level Item Date Ascending","top_level_item_date");
		comboSortOrder.addValue("Top Level Item Date Descending","top_level_item_date_descending");
		comboSortOrder.addValue("Document ID","document_id");
		comboSortOrder.setSelectedIndex(0);
	}
	
	@Override
	public void setEnabled(boolean value){
		for(Component c : getComponents()){
			c.setEnabled(value);
		}
	}
	
	public ComboItemBox getComboTraversal() {
		return comboTraversal;
	}
	
	public ComboItemBox getComboDedupe() {
		return comboDedupe;
	}
	
	public ComboItemBox getComboSortOrder() {
		return comboSortOrder;
	}
}