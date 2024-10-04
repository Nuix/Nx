/******************************************
 Copyright 2018 Nuix
 http://www.apache.org/licenses/LICENSE-2.0
 *******************************************/

package com.nuix.nx.dialogs;

import com.nuix.nx.controls.ProcessingFinishedListener;
import com.nuix.nx.controls.ProcessingStatusControl;
import lombok.Getter;
import lombok.Setter;
import nuix.Processor;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/***
 * A dialog which starts and monitors a processing job.  Also has buttons to pause, resume, stop and abort the job.
 * @author Jason Wells
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

    @Getter
    @Setter
    private int autoCloseDelaySeconds = 60;

    /***
     * Gets whether the job was aborted
     * @return True if the job was aborted thru the interface
     */
    public boolean getJobWasAborted() {
        return processingStatusControl.getJobWasAborted();
    }

    /***
     * Gets whether the job was stopped
     * @return True if the job was stopped through the interface
     */
    public boolean getJobWasStopped() {
        return processingStatusControl.getJobWasStopped();
    }

    public ProcessingStatusDialog() {
        super((JDialog) null);
        //We will own closing this
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setTitle("Processing Status");
        setIconImage(Toolkit.getDefaultToolkit().getImage(ProcessingStatusDialog.class.getResource("/icons/nuix_icon.png")));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //setSize(screenSize.width - 200,screenSize.height - 200);
        setSize(1024, screenSize.height - 200);
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
                    if (closeWindowAllowed) {
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


        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.out.println("closeWindowAllowed: " + closeWindowAllowed);
                if (closeWindowAllowed) {
                    if (autoCloseTimer != null) {
                        autoCloseTimer.stop();
                    }

                    dispose();
                }
            }
        });
    }

    /***
     * Logs a message to the log text area and the Nuix logs
     * @param message The message to log
     */
    public void log(String message) {
        processingStatusControl.log(message);
        logger.info(message);
    }

    /***
     * Displays the dialog and begins processing, showing status while processing is occurring.
     * @param processor The processor to start and monitor
     */
    public void displayAndBeginProcessing(Processor processor) {
        Thread t = new Thread(() -> {
            setVisible(true);
        });

        closeWindowAllowed = false;
        btnClose.setEnabled(false);
        try {
            t.start();
            processingStatusControl.beginProcessing(processor);
        } catch (Exception e) {
            String message = "Unexpected Error:\n\n" + e.getMessage();
            CommonDialogs.showError(message);
            logger.error(message, e);
        }
        closeWindowAllowed = true;
        btnClose.setEnabled(true);

        if (autoCloseDelaySeconds > 0) {
            // Auto close this dialog in 60 seconds
            autoCloseTimer = new Timer(autoCloseDelaySeconds * 1000, e -> {
                autoCloseTimer.stop();
                dispose();
            });
            autoCloseTimer.setRepeats(false);
            autoCloseTimer.start();
            processingStatusControl.log("Window will automatically close in " + autoCloseDelaySeconds + " seconds...");
        }

        try {
            t.join();
        } catch (InterruptedException ignored) {
        }
    }

    /***
     * Add a callback which will be invoked when processing ends
     * @param listener The callback to add
     */
    public void addProcessingFinishedListener(ProcessingFinishedListener listener) {
        processingStatusControl.addProcessingFinishedListener(listener);
    }

    /***
     * Removes a previously added callback
     * @param listener The callback to remove
     */
    public void removeProcessingFinishedListener(ProcessingFinishedListener listener) {
        processingStatusControl.removeProcessingFinishedListener(listener);
    }
}
