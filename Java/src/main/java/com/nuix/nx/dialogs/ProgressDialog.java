/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.dialogs;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.nuix.nx.controls.ReportDisplayPanel;
import com.nuix.nx.controls.models.ReportDataModel;
import org.joda.time.DateTime;

import java.net.URL;

/***
 * Provides a configurable progress dialog.  Note that you do not create an instance of it
 * directly. Instead use the static method {@link #forBlock(ProgressDialogBlockInterface)} to
 * get an instance which will exist for the duration of the provided callback.
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class ProgressDialog extends JDialog {
	
	private final JPanel contentPanel = new JPanel();
	private boolean abortWasRequested = false;
	private boolean closeAllowed = false;
	private boolean logAllStatusUpdates = false;
	private JLabel lblMainStatus;
	private JLabel lblSubStatus;
	private JProgressBar subProgress;
	private JProgressBar mainProgress;
	private JButton btnAbort;
	private Runnable abortCallback;
	private JTextArea txtrLog;
	private JScrollPane scrollPane;

	private ReportDisplayPanel reportDisplay;
	private ProgressDialogLoggingCallback loggingCallback;
	private JPanel buttonsPanel;
	private JButton btnClose;
	private boolean timestampLoggedMessages = false;

	private ProgressDialog() {
		super((JDialog)null);
		URL iconUri = ProgressDialog.class.getResource("/com/nuix/nx/dialogs/nuix_icon.png");
		setIconImage(Toolkit.getDefaultToolkit().getImage(iconUri));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("Progress Dialog");
		setSize(new Dimension(800,600));
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		//gbl_contentPanel.rowHeights = new int[]{0, 25, 25, 25, 300, 0, 0, 0}; // Sections control their own size
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.5, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		
		lblMainStatus = new JLabel("...");
		lblMainStatus.setFont(new Font("Tahoma", Font.PLAIN, 16));
		GridBagConstraints gbc_lblMainStatus = new GridBagConstraints();
		gbc_lblMainStatus.anchor = GridBagConstraints.WEST;
		gbc_lblMainStatus.insets = new Insets(0, 0, 5, 0);
		gbc_lblMainStatus.gridx = 0;
		gbc_lblMainStatus.gridy = 0;
		contentPanel.add(lblMainStatus, gbc_lblMainStatus);
		
		mainProgress = new JProgressBar();
		mainProgress.setStringPainted(true);
		GridBagConstraints gbc_mainProgress = new GridBagConstraints();
		gbc_mainProgress.insets = new Insets(0, 0, 5, 0);
		gbc_mainProgress.fill = GridBagConstraints.BOTH;
		gbc_mainProgress.gridx = 0;
		gbc_mainProgress.gridy = 1;
		contentPanel.add(mainProgress, gbc_mainProgress);
		
		lblSubStatus = new JLabel("...");
		GridBagConstraints gbc_lblSubStatus = new GridBagConstraints();
		gbc_lblSubStatus.anchor = GridBagConstraints.WEST;
		gbc_lblSubStatus.insets = new Insets(0, 0, 5, 0);
		gbc_lblSubStatus.gridx = 0;
		gbc_lblSubStatus.gridy = 2;
		contentPanel.add(lblSubStatus, gbc_lblSubStatus);
		
		subProgress = new JProgressBar();
		subProgress.setStringPainted(true);
		GridBagConstraints gbc_subProgress = new GridBagConstraints();
		gbc_subProgress.insets = new Insets(0, 0, 5, 0);
		gbc_subProgress.fill = GridBagConstraints.BOTH;
		gbc_subProgress.gridx = 0;
		gbc_subProgress.gridy = 3;
		contentPanel.add(subProgress, gbc_subProgress);
		
		scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(700, 300));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		contentPanel.add(scrollPane, gbc_scrollPane);
		
		txtrLog = new JTextArea();
		txtrLog.setBackground(Color.WHITE);
		txtrLog.setEditable(false);
		scrollPane.setViewportView(txtrLog);

		reportDisplay = new ReportDisplayPanel();
		GridBagConstraints reportConstraints = new GridBagConstraints();
		//reportConstraints.anchor = GridBagConstraints.EAST;
		reportConstraints.fill = GridBagConstraints.BOTH;
		reportConstraints.gridx = 0;
		reportConstraints.gridy = 5;
		contentPanel.add(reportDisplay, reportConstraints);


		buttonsPanel = new JPanel();
		GridBagConstraints gbc_buttonsPanel = new GridBagConstraints();
		gbc_buttonsPanel.anchor = GridBagConstraints.EAST;
		gbc_buttonsPanel.fill = GridBagConstraints.VERTICAL;
		gbc_buttonsPanel.gridx = 0;
		gbc_buttonsPanel.gridy = 6;
		contentPanel.add(buttonsPanel, gbc_buttonsPanel);
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnAbort = new JButton("Abort");
		buttonsPanel.add(btnAbort);
		btnAbort.setFont(new Font("Tahoma", Font.BOLD, 13));
		
		btnClose = new JButton("Close");
		btnClose.setEnabled(false);
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ProgressDialog.this.dispose();
			}
		});
		buttonsPanel.add(btnClose);
		btnAbort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ProgressDialog.this.confirmAbort();
			}
		});
		
		this.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
            	//If the abort button is hidden we wont allow user to close the window until
            	//callback has completed.  If abort button is visible we will allow the user
            	//the option to abort.  Script is responsible to notice this and shutdown
            	//gracefully.
            	if(ProgressDialog.this.getAbortButtonVisible() == true){
	                if(closeAllowed)
	                	ProgressDialog.this.dispose();
	                else
	                	ProgressDialog.this.confirmAbort();
            	}
            	else if(closeAllowed){
            		ProgressDialog.this.dispose();
            	}
            }
        });
	}
	
	protected void confirmAbort() {
		String message = "Are you sure you want to abort?";
		String title = "Abort?";
		if(JOptionPane.showConfirmDialog(ProgressDialog.this, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
			abortWasRequested = true;
			btnAbort.setText("Abort Requested...");
			btnAbort.setEnabled(false);
			if(abortCallback != null){
				try
				{
					abortCallback.run();
				} catch(Exception exc){
					logMessage("An exception occurred in the onAbort callback:");
					logMessage(exc.getMessage());
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					exc.printStackTrace(pw);
					logMessage(sw.toString());
				}
			}
		}
	}

	/***
	 * Used to determine if the user requested to abort.
	 * @return True if the user clicked abort and clicked yes on the confirmation dialog.
	 */
	public boolean abortWasRequested(){
		return abortWasRequested;
	}
	
	/***
	 * Allows you to supply a callback which will be called when the user
	 * aborts by clicking and confirming the abort button or closing the dialog.
	 * @param callback The callback to invoke on abort.
	 */
	public void onAbort(Runnable callback){
		abortCallback = callback;
	}
	
	/***
	 * Set the main status label text.
	 * @param status The value set it to.
	 */
	public void setMainStatus(String status){
		lblMainStatus.setText(status);
		if(logAllStatusUpdates)
			logMessage(status);
	}
	
	/***
	 * Set the main status label and writes it as a log message.
	 * @param status The value set it to.
	 */
	public void setMainStatusAndLogIt(String status){
		lblMainStatus.setText(status);
		logMessage(status);
	}
	
	/***
	 * Set the sub status label text.
	 * @param status The value to set it to.
	 */
	public void setSubStatus(String status){
		lblSubStatus.setText(status);
		if(logAllStatusUpdates)
			logMessage(status);
	}
	
	/***
	 * Set the sub status label and writes it as a log message.
	 * @param status The value to set it to.
	 */
	public void setSubStatusAndLogIt(String status){
		lblSubStatus.setText(status);
		logMessage(status);
	}
	
	/***
	 * Sets the value and maximum value for the main progress bar.
	 * @param value The current value to set it to.
	 * @param max The maximum value to set it to.
	 */
	public void setMainProgress(int value,int max){
		mainProgress.setMaximum(max);
		mainProgress.setValue(value);
	}
	
	/***
	 * Sets the current value for the main progress bar without changing the maximum value.
	 * @param value The current value to set it to.
	 */
	public void setMainProgress(int value){
		mainProgress.setValue(value);
	}
	
	/***
	 * Increases the current value of the main progress bar by 1.
	 */
	public void incrementMainProgress() {
		mainProgress.setValue(mainProgress.getValue()+1);
	}
	
	/***
	 * Sets whether the main progress bar is visible.
	 * @param value True for visible, false for hidden.
	 */
	public void setMainProgressVisible(boolean value){
		mainProgress.setVisible(value);
	}
	
	/***
	 * Sets the value and maximum value for the sub progress bar.
	 * @param value The current value to set it to.
	 * @param max The maximum value to set it to.
	 */
	public void setSubProgress(int value,int max){
		subProgress.setValue(value);
		subProgress.setMaximum(max);
	}
	
	/***
	 * Sets the current value for the sub progress bar without changing the maximum value.
	 * @param value The current value to set it to.
	 */
	public void setSubProgress(int value){
		subProgress.setValue(value);
	}
	
	/***
	 * Increases the current value of the sub progress bar by 1.
	 */
	public void incrememntSubProgress() {
		subProgress.setValue(subProgress.getValue()+1);
	}
	
	/***
	 * Sets whether the sub progress bar is visible.
	 * @param value True for visible, false for hidden.
	 */
	public void setSubProgressVisible(boolean value){
		subProgress.setVisible(value);
	}

	/***
	 * Sets whether the abort button should be visible.  Useful to hide the abort button if you
	 * do not plan on supporting responding to it.
	 * @param value True if you wish the button to be visible, false if you don't.
	 */
	public void setAbortButtonVisible(boolean value){
		btnAbort.setVisible(value);
	}
	
	/***
	 * Gets whether the abort button is currently visible.
	 * @return True if the button is currently visible.
	 */
	public boolean getAbortButtonVisible(){
		return btnAbort.isVisible();
	}
	
	/***
	 * Sets whether the log text area should be visible.  Useful if you will not be logging any messages
	 * while the progress dialog is up.
	 * @param value True to make the log text area visible, false to hide it.
	 */
	public void setLogVisible(boolean value){
		if(scrollPane.isVisible() && value == false){
			//Shrink the dialog a bit to make up for empty space
			Dimension current = getSize();
			Dimension resized = new Dimension(current.width,current.height-300);
			setSize(resized);
		}
		else if(!scrollPane.isVisible() && value == true){
			//Enlarge dialog to make room for soon to be visible log area
			Dimension current = getSize();
			Dimension resized = new Dimension(current.width,current.height+300);
			setSize(resized);
		}
		scrollPane.setVisible(value);
	}
	
	/***
	 * Logs a message to the log text area.
	 * @param message The message to write.  A newline is automatically appended.
	 */
	public void logMessage(String message){
		if(timestampLoggedMessages){
			txtrLog.append(DateTime.now().toString("YYYY-MM-dd hh:mm:ss") + ": " + message+"\n");
		} else {
			txtrLog.append(message+"\n");			
		}
		
		txtrLog.setCaretPosition(txtrLog.getDocument().getLength());
		
		if(loggingCallback != null){
			loggingCallback.messageLogged(message);
		} else {
			System.out.println(message);	
		}
	}
	
	/***
	 * Sets whether text in the message area show be line/word wrapped.
	 * @param wrapText True enables wrapping, false (default) disables it.
	 */
	public void setTextWrapping(boolean wrapText) {
		txtrLog.setLineWrap(wrapText);
		txtrLog.setWrapStyleWord(wrapText);
	}
	
	/***
	 * Clears the log text area of all message.
	 */
	public void clearLog(){
		txtrLog.setText("");
	}
	
	/***
	 * Gets the current text in the log text area.
	 * @return The log text area's current contents.
	 */
	public String getLogText(){
		return txtrLog.getText();
	}
	
	/***
	 * Gets a value indicating whether all status message updates will be logged.
	 * @return True if all status messages will be logged
	 */
	public boolean getLogAllStatusUpdates() {
		return logAllStatusUpdates;
	}

	/***
	 * Sets a value indicating whether all status message updates will be logged.
	 * @param logAllStatusUpdates True if all status messages should be logged
	 */
	public void setLogAllStatusUpdates(boolean logAllStatusUpdates) {
		this.logAllStatusUpdates = logAllStatusUpdates;
	}

	/**
	 * Displays a progress dialog for the duration of called method.
	 * @see ProgressDialogBlockInterface#DoWork
	 * @param block Creates and displays a progress dialog for the life span of the call to {@link ProgressDialogBlockInterface#DoWork}
	 */
	public static void forBlock(ProgressDialogBlockInterface block){
		ProgressDialog dialog = new ProgressDialog();
		dialog.setVisible(true);
		try
		{
			block.DoWork(dialog);
		}
		catch(Exception exc){
			dialog.logMessage("Error occured in provided block:");
			dialog.logMessage(exc.getMessage());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exc.printStackTrace(pw);
			dialog.logMessage(sw.toString());
			exc.printStackTrace();
		}
		dialog.setAbortButtonVisible(false);
		dialog.btnClose.setEnabled(true);
		dialog.closeAllowed = true;
	}
	
	/***
	 * Allows you to provide a callback which will be called each time a message is logged
	 * to this progress dialog.  Useful if you wish messages logged to the progress dialog
	 * to additionally be recorded elsewhere.
	 * @param callback Callback interface object which will receive logged messages.
	 */
	public void onMessageLogged(ProgressDialogLoggingCallback callback){
		loggingCallback = callback;
	}
	
	/***
	 * Enlarges the progress dialog to match the screen size less some margin on all sides.
	 * @param margin The amount in pixels of "margin" to consider when enlarging this progress dialog.
	 */
	public void embiggen(int margin){
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setSize(new Dimension(screenSize.width - margin, screenSize.height - margin));
		setLocationRelativeTo(null);
	}
	
	/***
	 * This is a convenience method for setting the progress dialog into a "script completed" state.
	 * Main status is set to "Completed" and this is logged.
	 * Sub status if cleared.
	 * Main progress is set to 100%.
	 * Sub progress is set to 100%.
	 */
	public void setCompleted(){
		setMainStatusAndLogIt("Completed");
		setSubStatus("");
		setMainProgress(1,1);
		setSubProgress(1,1);
	}

	/***
	 * Sets the value determining whether messages logged will lead with a time stamp.
	 * @return boolean True if you time stamps will be logged before each message.
	 */
	public boolean getTimestampLoggedMessages() {
		return timestampLoggedMessages;
	}

	/***
	 * Sets the value determining whether messages logged will lead with a time stamp.
	 * @param timestampLoggedMessages True if you want time stamps logged before each message.
	 */
	public void setTimestampLoggedMessages(boolean timestampLoggedMessages) {
		this.timestampLoggedMessages = timestampLoggedMessages;
	}

	/**
	 * Adds a section to the bottom of the dialog as a Report.  Uses the provided
	 * ReportDataModel to make a ReportDisplayPanel which gets inserted under the
	 * log area and above the buttons.
	 * @param reportDataModel The data model to display at the bottom of the dialog.  Must
	 *                        not be null.
	 */
	public void addReport(ReportDataModel reportDataModel) {
		SwingUtilities.invokeLater(() -> {
			reportDisplay.setReportDataModel(reportDataModel);
		});
	}

	/***
	 * Sets whether the report section is visible.
	 * @param value True for visible, false for hidden.
	 */
	public void setReportDisplayVisible(boolean value){
		reportDisplay.setVisible(value);
	}


}
