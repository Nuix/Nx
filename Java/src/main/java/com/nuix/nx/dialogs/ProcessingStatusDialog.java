/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import com.nuix.nx.controls.ProcessingFinishedListener;
import com.nuix.nx.controls.ProcessingStatusControl;

import nuix.Processor;

/***
 * A dialog which starts and monitors a processing job.  Also has buttons to pause, resume, stop and abort the job.
 * @author JWells01
 *
 */
@SuppressWarnings("serial")
public class ProcessingStatusDialog extends JDialog {
	private static Logger logger = Logger.getLogger(ProcessingStatusDialog.class);
	
	private final JPanel contentPanel = new JPanel();
	private ProcessingStatusControl processingStatusControl;
	private boolean closeWindowAllowed = false;
	private JButton btnClose;
	private Timer autoCloseTimer;

	public ProcessingStatusDialog() {
		//We will own closing this
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setModal(true);
		setTitle("Processing Status");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProcessingStatusDialog.class.getResource("/com/nuix/nx/dialogs/nuix_icon.png")));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//setSize(screenSize.width - 200,screenSize.height - 200);
		setSize(1024,screenSize.height - 200);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			processingStatusControl = new ProcessingStatusControl();
			contentPanel.add(processingStatusControl, BorderLayout.CENTER);
			
			btnClose = new JButton("Close");
			btnClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if(closeWindowAllowed){
	            		dispose();
	            	}
				}
			});
			btnClose.setEnabled(false);
			GridBagConstraints gbc_btnClose = new GridBagConstraints();
			gbc_btnClose.anchor = GridBagConstraints.EAST;
			gbc_btnClose.gridx = 0;
			gbc_btnClose.gridy = 2;
			processingStatusControl.add(btnClose, gbc_btnClose);
		}
		
		
		this.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
            	System.out.println("closeWindowAllowed: "+closeWindowAllowed);
            	if(closeWindowAllowed){
            		autoCloseTimer.stop();
            		dispose();
            	}
            }
        });
	}

	/***
	 * Logs a message to the log text area and the Nuix logs
	 * @param message The message to log
	 */
	public void log(String message){
		processingStatusControl.log(message);
		logger.info(message);
	}

	/***
	 * Displays the dialog and begins processing, showing status while processing is occurring.
	 * @param processor The processor to start and monitor
	 */
	public void displayAndBeginProcessing(Processor processor){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				setVisible(true);				
			}
		});
		t.start();
		
		closeWindowAllowed = false;
		btnClose.setEnabled(false);
		try {
			processingStatusControl.beginProcessing(processor);
		} catch (Exception e) {
			String message = "Unexecpected Error:\n\n"+e.getMessage();
			CommonDialogs.showError(message);
			logger.error(message,e);
		}
		closeWindowAllowed = true;
		btnClose.setEnabled(true);
		// Auto close this dialog in 60 seconds
		autoCloseTimer = new Timer(60 * 1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				autoCloseTimer.stop();
				dispose();
			}
		});
		autoCloseTimer.setRepeats(false);
		autoCloseTimer.start();
		processingStatusControl.log("Window will automatically close in 60 seconds...");
	}
	
	/***
	 * Add a callback which will be invoked when processing ends
	 * @param listener The callback to add
	 */
	public void addProcessingFinishedListener(ProcessingFinishedListener listener){
		processingStatusControl.addProcessingFinishedListener(listener);
	}
	
	/***
	 * Removes a previously added callback
	 * @param listener The callback to remove
	 */
	public void removeProcessingFinishedListener(ProcessingFinishedListener listener){
		processingStatusControl.removeProcessingFinishedListener(listener);
	}
}
