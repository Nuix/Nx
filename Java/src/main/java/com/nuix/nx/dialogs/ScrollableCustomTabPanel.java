package com.nuix.nx.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class ScrollableCustomTabPanel extends CustomTabPanel {

	private JPanel wrappedPanel;

	public ScrollableCustomTabPanel(String label, TabbedCustomDialog owner) {
		this.label = label;
		this.owner = owner;
		buttonGroups = new HashMap<String,ButtonGroup>();
		controls = new HashMap<String,Component>();
		
		setBorder(new EmptyBorder(5,5,5,5));
		
		BorderLayout outerLayout = new BorderLayout();
		setLayout(outerLayout);
		JScrollPane outerScroller = new JScrollPane();
		outerScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		super.add(outerScroller);
		
		wrappedPanel = new JPanel();
		outerScroller.setViewportView(wrappedPanel);
		
		rootLayout = new GridBagLayout();
		rootLayout.columnWidths = new int[]{LABEL_COLUMN_WIDTH,CONTROL_COLUMN_WIDTH};
		wrappedPanel.setLayout(rootLayout);
	}

	@Override
	public Component add(Component comp) {
		return wrappedPanel.add(comp);
	}
}
