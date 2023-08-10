package com.nuix.nx.controls;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.nuix.nx.dialogs.CustomTabPanel;

@SuppressWarnings("serial")
public class ButtonRow extends JPanel {
	private CustomTabPanel owningTab;
	
	public ButtonRow() {}
	public ButtonRow(CustomTabPanel owningTab) {
		this.owningTab = owningTab;
	}
	
	/***
	 * Appends a JButton control with the specified label and attaches the provided action listener to the
	 * button.  From Ruby pass a block to add a handler to the button.
	 * @param identifier The unique identifier for this control.
	 * @param controlLabel The label text of the button
	 * @param actionListener The action listener to attach to the button.
	 * @return Returns this ButtonRow instance to allow for method chaining.
	 * @throws Exception May throw an exception if the provided identifier has already been used.
	 */
	public ButtonRow appendButton(String identifier, String controlLabel, ActionListener actionListener) throws Exception{
		JButton component = new JButton(controlLabel);
		component.addActionListener(actionListener);
		Double preferredHeight = component.getPreferredSize().getHeight();
		component.setPreferredSize(new Dimension(150,preferredHeight.intValue()));
		add(component);
		owningTab.trackComponent(identifier, component);
		return this;
	}
}
