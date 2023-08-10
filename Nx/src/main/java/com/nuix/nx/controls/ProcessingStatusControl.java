/******************************************
Copyright 2018 Nuix
http://www.apache.org/licenses/LICENSE-2.0
*******************************************/

package com.nuix.nx.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.nuix.nx.controls.models.ItemStatisticsTableModel;
import com.nuix.nx.dialogs.CommonDialogs;
import com.nuix.nx.helpers.FormatHelpers;

import nuix.LoadProcessingJob;
import nuix.Processor;
import nuix.ProcessorCleaningUpCallback;

/***
 * Control for displaying processing status.
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class ProcessingStatusControl extends JPanel {
	private static Logger logger = Logger.getLogger(ProcessingStatusControl.class);
	
	private JTable itemStatsTable;
	private ItemStatisticsTableModel itemStatsTableModel = new ItemStatisticsTableModel();
	private JTextArea txtrProcessingLog;
	
	private int totalProcessed = 0;
	private int totalCorrupted = 0;
	private int totalEncrypted = 0;
	private int totalDeleted = 0;
	private LoadProcessingJob job = null;
	
	private JLabel lblTotalProcessedCount;
	private JLabel lblTotalCorruptedCount;
	private JLabel lblTotalEncryptedCount;
	private JLabel lblTotalDeletedCount;
	private JLabel lblElapsedTimeValue;
	private JButton btnPauseProcessing;
	private JButton btnResumeProcessing;
	private JButton btnStopProcessing;
	private JButton btnAbortProcessing;
	
	private List<ProcessingFinishedListener> processingFinishedListeners = new ArrayList<ProcessingFinishedListener>();
	private JTabbedPane tabbedPane;
	private JPanel processingStatusTab;
	private JPanel processingSettingsTab;
	private DataProcessingSettingsControl dataProcessingSettingsControl;
	private JButton btnOpenLogDirectory;
	
	public ProcessingStatusControl() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		add(tabbedPane, gbc_tabbedPane);
		
		processingStatusTab = new JPanel();
		tabbedPane.addTab("Processing Status", null, processingStatusTab, null);
		GridBagLayout gbl_processingStatusTab = new GridBagLayout();
		gbl_processingStatusTab.columnWidths = new int[]{0, 0};
		gbl_processingStatusTab.rowHeights = new int[]{150, 0, 0};
		gbl_processingStatusTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_processingStatusTab.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		processingStatusTab.setLayout(gbl_processingStatusTab);
		
		JPanel processingLogPanel = new JPanel();
		GridBagConstraints gbc_processingLogPanel = new GridBagConstraints();
		gbc_processingLogPanel.insets = new Insets(0, 0, 5, 0);
		gbc_processingLogPanel.fill = GridBagConstraints.BOTH;
		gbc_processingLogPanel.gridx = 0;
		gbc_processingLogPanel.gridy = 0;
		processingStatusTab.add(processingLogPanel, gbc_processingLogPanel);
		processingLogPanel.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Processing Log", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		processingLogPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		processingLogPanel.add(scrollPane, BorderLayout.CENTER);
		
		txtrProcessingLog = new JTextArea();
		txtrProcessingLog.setFont(new Font("Consolas", Font.PLAIN, 11));
		txtrProcessingLog.setEditable(false);
		scrollPane.setViewportView(txtrProcessingLog);
		
		JPanel itemStatsPanel = new JPanel();
		GridBagConstraints gbc_itemStatsPanel = new GridBagConstraints();
		gbc_itemStatsPanel.fill = GridBagConstraints.BOTH;
		gbc_itemStatsPanel.gridx = 0;
		gbc_itemStatsPanel.gridy = 1;
		processingStatusTab.add(itemStatsPanel, gbc_itemStatsPanel);
		itemStatsPanel.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Item Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagLayout gbl_itemStatsPanel = new GridBagLayout();
		gbl_itemStatsPanel.columnWidths = new int[]{0, 0, 75, 0, 75, 0, 75, 0, 75, 0, 75, 0, 0};
		gbl_itemStatsPanel.rowHeights = new int[]{0, 0, 0};
		gbl_itemStatsPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_itemStatsPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		itemStatsPanel.setLayout(gbl_itemStatsPanel);
		
		JLabel lblElapsed = new JLabel("Elapsed:");
		lblElapsed.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblElapsed = new GridBagConstraints();
		gbc_lblElapsed.insets = new Insets(0, 0, 5, 5);
		gbc_lblElapsed.gridx = 1;
		gbc_lblElapsed.gridy = 0;
		itemStatsPanel.add(lblElapsed, gbc_lblElapsed);
		
		lblElapsedTimeValue = new JLabel("00:00:00");
		GridBagConstraints gbc_lblElapsedTimeValue = new GridBagConstraints();
		gbc_lblElapsedTimeValue.anchor = GridBagConstraints.WEST;
		gbc_lblElapsedTimeValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblElapsedTimeValue.gridx = 2;
		gbc_lblElapsedTimeValue.gridy = 0;
		itemStatsPanel.add(lblElapsedTimeValue, gbc_lblElapsedTimeValue);
		
		JLabel lblTotalProcessed = new JLabel("Total Processed:");
		lblTotalProcessed.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblTotalProcessed = new GridBagConstraints();
		gbc_lblTotalProcessed.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalProcessed.gridx = 3;
		gbc_lblTotalProcessed.gridy = 0;
		itemStatsPanel.add(lblTotalProcessed, gbc_lblTotalProcessed);
		
		lblTotalProcessedCount = new JLabel("0");
		GridBagConstraints gbc_lblTotalProcessedCount = new GridBagConstraints();
		gbc_lblTotalProcessedCount.anchor = GridBagConstraints.WEST;
		gbc_lblTotalProcessedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalProcessedCount.gridx = 4;
		gbc_lblTotalProcessedCount.gridy = 0;
		itemStatsPanel.add(lblTotalProcessedCount, gbc_lblTotalProcessedCount);
		
		JLabel lblTotalCorrupted = new JLabel("Total Corrupted:");
		lblTotalCorrupted.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblTotalCorrupted = new GridBagConstraints();
		gbc_lblTotalCorrupted.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalCorrupted.gridx = 5;
		gbc_lblTotalCorrupted.gridy = 0;
		itemStatsPanel.add(lblTotalCorrupted, gbc_lblTotalCorrupted);
		
		lblTotalCorruptedCount = new JLabel("0");
		GridBagConstraints gbc_lblTotalCorruptedCount = new GridBagConstraints();
		gbc_lblTotalCorruptedCount.anchor = GridBagConstraints.WEST;
		gbc_lblTotalCorruptedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalCorruptedCount.gridx = 6;
		gbc_lblTotalCorruptedCount.gridy = 0;
		itemStatsPanel.add(lblTotalCorruptedCount, gbc_lblTotalCorruptedCount);
		
		JLabel lblTotalEncrypted = new JLabel("Total Encrypted:");
		lblTotalEncrypted.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblTotalEncrypted = new GridBagConstraints();
		gbc_lblTotalEncrypted.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalEncrypted.gridx = 7;
		gbc_lblTotalEncrypted.gridy = 0;
		itemStatsPanel.add(lblTotalEncrypted, gbc_lblTotalEncrypted);
		
		lblTotalEncryptedCount = new JLabel("0");
		GridBagConstraints gbc_lblTotalEncryptedCount = new GridBagConstraints();
		gbc_lblTotalEncryptedCount.anchor = GridBagConstraints.WEST;
		gbc_lblTotalEncryptedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalEncryptedCount.gridx = 8;
		gbc_lblTotalEncryptedCount.gridy = 0;
		itemStatsPanel.add(lblTotalEncryptedCount, gbc_lblTotalEncryptedCount);
		
		JLabel lblTotalDeleted = new JLabel("Total Deleted:");
		lblTotalDeleted.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblTotalDeleted = new GridBagConstraints();
		gbc_lblTotalDeleted.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalDeleted.gridx = 9;
		gbc_lblTotalDeleted.gridy = 0;
		itemStatsPanel.add(lblTotalDeleted, gbc_lblTotalDeleted);
		
		lblTotalDeletedCount = new JLabel("0");
		GridBagConstraints gbc_lblTotalDeletedCount = new GridBagConstraints();
		gbc_lblTotalDeletedCount.anchor = GridBagConstraints.WEST;
		gbc_lblTotalDeletedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalDeletedCount.gridx = 10;
		gbc_lblTotalDeletedCount.gridy = 0;
		itemStatsPanel.add(lblTotalDeletedCount, gbc_lblTotalDeletedCount);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_1.gridwidth = 12;
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		itemStatsPanel.add(scrollPane_1, gbc_scrollPane_1);
		
		itemStatsTable = new JTable(itemStatsTableModel);
		itemStatsTable.setFillsViewportHeight(true);
		scrollPane_1.setViewportView(itemStatsTable);
		
		processingSettingsTab = new JPanel();
		tabbedPane.addTab("Processing Settings Used", null, processingSettingsTab, null);
		GridBagLayout gbl_processingSettingsTab = new GridBagLayout();
		gbl_processingSettingsTab.columnWidths = new int[]{0, 0, 0};
		gbl_processingSettingsTab.rowHeights = new int[]{0, 0};
		gbl_processingSettingsTab.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_processingSettingsTab.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		processingSettingsTab.setLayout(gbl_processingSettingsTab);
		
		dataProcessingSettingsControl = new DataProcessingSettingsControl();
		dataProcessingSettingsControl.hideSaveLoadResetButtons();
		DisablingGlassPaneWrapper processingSettingsDisabler = new DisablingGlassPaneWrapper(dataProcessingSettingsControl);
		processingSettingsDisabler.activateGlassPane(true);
		GridBagConstraints gbc_dataProcessingSettingsControl = new GridBagConstraints();
		gbc_dataProcessingSettingsControl.insets = new Insets(0, 0, 0, 5);
		gbc_dataProcessingSettingsControl.fill = GridBagConstraints.BOTH;
		gbc_dataProcessingSettingsControl.gridx = 0;
		gbc_dataProcessingSettingsControl.gridy = 0;
		processingSettingsTab.add(processingSettingsDisabler, gbc_dataProcessingSettingsControl);
		
		TableColumn kindCol = itemStatsTable.getColumnModel().getColumn(0);
		
		TableColumn typeCol = itemStatsTable.getColumnModel().getColumn(1);
		
		TableColumn mimetypeCol = itemStatsTable.getColumnModel().getColumn(2);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 50, 0, 10, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		btnPauseProcessing = new JButton("Pause Processing");
		btnPauseProcessing.setEnabled(false);
		btnPauseProcessing.setIcon(new ImageIcon(ProcessingStatusControl.class.getResource("/icons/control_pause.png")));
		btnPauseProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pauseProcessing();
			}
		});
		GridBagConstraints gbc_btnPauseProcessing = new GridBagConstraints();
		gbc_btnPauseProcessing.insets = new Insets(0, 0, 0, 5);
		gbc_btnPauseProcessing.gridx = 0;
		gbc_btnPauseProcessing.gridy = 0;
		panel.add(btnPauseProcessing, gbc_btnPauseProcessing);
		
		btnResumeProcessing = new JButton("Resume Processing");
		btnResumeProcessing.setEnabled(false);
		btnResumeProcessing.setIcon(new ImageIcon(ProcessingStatusControl.class.getResource("/icons/control_play.png")));
		btnResumeProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resumeProcessing();
			}
		});
		GridBagConstraints gbc_btnResumeProcessing = new GridBagConstraints();
		gbc_btnResumeProcessing.insets = new Insets(0, 0, 0, 5);
		gbc_btnResumeProcessing.gridx = 1;
		gbc_btnResumeProcessing.gridy = 0;
		panel.add(btnResumeProcessing, gbc_btnResumeProcessing);
		
		btnStopProcessing = new JButton("Stop Processing");
		btnStopProcessing.setEnabled(false);
		btnStopProcessing.setIcon(new ImageIcon(ProcessingStatusControl.class.getResource("/icons/control_stop.png")));
		btnStopProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopProcessing();
			}
		});
		GridBagConstraints gbc_btnStopProcessing = new GridBagConstraints();
		gbc_btnStopProcessing.insets = new Insets(0, 0, 0, 5);
		gbc_btnStopProcessing.gridx = 3;
		gbc_btnStopProcessing.gridy = 0;
		panel.add(btnStopProcessing, gbc_btnStopProcessing);
		
		btnAbortProcessing = new JButton("Abort Processing");
		btnAbortProcessing.setEnabled(false);
		btnAbortProcessing.setIcon(new ImageIcon(ProcessingStatusControl.class.getResource("/icons/cancel.png")));
		btnAbortProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abortProcessing();
			}
		});
		GridBagConstraints gbc_btnAbortProcessing = new GridBagConstraints();
		gbc_btnAbortProcessing.insets = new Insets(0, 0, 0, 5);
		gbc_btnAbortProcessing.gridx = 5;
		gbc_btnAbortProcessing.gridy = 0;
		panel.add(btnAbortProcessing, gbc_btnAbortProcessing);
		
		btnOpenLogDirectory = new JButton("Open Log Directory");
		btnOpenLogDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Desktop.getDesktop().open(new File(System.getProperty("nuix.logdir")));
				} catch (Exception e) {
					logger.error(e);
				}
			}
		});
		btnOpenLogDirectory.setIcon(new ImageIcon(ProcessingStatusControl.class.getResource("/icons/folder_table.png")));
		GridBagConstraints gbc_btnOpenLogDirectory = new GridBagConstraints();
		gbc_btnOpenLogDirectory.gridx = 7;
		gbc_btnOpenLogDirectory.gridy = 0;
		panel.add(btnOpenLogDirectory, gbc_btnOpenLogDirectory);
		kindCol.setPreferredWidth(100);
		kindCol.setWidth(100);
		kindCol.setMinWidth(100);
		kindCol.setMaxWidth(100);
		typeCol.setPreferredWidth(250);
		typeCol.setWidth(250);
		typeCol.setMinWidth(250);
		typeCol.setMaxWidth(250);
		mimetypeCol.setPreferredWidth(250);
		mimetypeCol.setWidth(250);
		mimetypeCol.setMinWidth(250);
		mimetypeCol.setMaxWidth(250);
	}
	
	private void pauseProcessing(){
		String message = "Pausing places the processing job in an idle state from which it can be resumed from later.  "+
				"Are you sure you want to pause processing?";
		
		if(job != null && !job.hasFinished()){
			if(CommonDialogs.getConfirmation(this,message, "Pause processing?")){
				log("Requesting processor pause, please wait...");
				job.pause();
				btnResumeProcessing.setEnabled(true);
				btnPauseProcessing.setEnabled(false);
			}
		}
	}
	
	private void resumeProcessing(){
		if(job != null && job.hasPaused()){
			log("Requesting processor resume...");
			job.resume();
			btnResumeProcessing.setEnabled(false);
			btnPauseProcessing.setEnabled(true);
		}
	}
	
	private void stopProcessing(){
		String message = "Stopping processing requests that the processor gracefully stop processing as soon as possible.  "+
				"Are you sure you want to stop processing?";
		if(job != null && !job.hasFinished()){
			if(CommonDialogs.getConfirmation(this,message, "Stop processing?")){
				log("Requesting processor stop, please wait...");
				btnPauseProcessing.setEnabled(false);
				btnResumeProcessing.setEnabled(false);
				btnStopProcessing.setEnabled(false);
				btnAbortProcessing.setEnabled(false);
				job.stop();
			}
		}
	}
	
	private void abortProcessing(){
		String message = "Aborting processing requests that the processor forcefully stop processing as soon as possible.  "+
				"Are you sure you want to abort processing?";
		if(job != null && !job.hasFinished()){
			if(CommonDialogs.getConfirmation(this,message, "Abort processing?")){
				log("Requesting processor abort, please wait...");
				btnPauseProcessing.setEnabled(false);
				btnResumeProcessing.setEnabled(false);
				btnStopProcessing.setEnabled(false);
				btnAbortProcessing.setEnabled(false);
				job.abort();
			}
		}
	}
	
	public void log(String message){
		txtrProcessingLog.append(DateTime.now().toString("YYYY-MM-dd hh:mm:ss") + ": " + message+"\n");
		txtrProcessingLog.setCaretPosition(txtrProcessingLog.getDocument().getLength());
	}

	public void beginProcessing(Processor processor){
//		BroadcastingLogAppender loggingListener = new BroadcastingLogAppender();
//		loggingListener.setLayout(new PatternLayout("%-5p [%t]: %m"));
//		loggingListener.setLevel(Level.ALL);
//		Queue<String> logFifo = new CircularFifoQueue<String>(1000);
//		loggingListener.addListener(new LogEventListener() {
//			
//			@Override
//			public void eventLogged(Appender source, LoggingEvent event) {
//				logFifo.add(source.getLayout().format(event));
//				SwingUtilities.invokeLater(() -> {
//					txtrLogEvents.setText(String.join("\n", logFifo));
//					txtrLogEvents.setCaretPosition(txtrLogEvents.getDocument().getLength());	
//				});
//			}
//		});
//		loggingListener.hookLogging();
		
		dataProcessingSettingsControl.loadSettings(processor.getProcessingSettings());
		
		long startTime = System.currentTimeMillis();
		Timer elapsedTimer = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long elapsedSeconds = (System.currentTimeMillis() - startTime)/1000;
				lblElapsedTimeValue.setText(FormatHelpers.secondsToElapsedString(elapsedSeconds));
			}
		});
		elapsedTimer.start();
		
		Object syncObject = new Object();
		processor.whenItemProcessed((i)->{
			synchronized(syncObject){
				totalProcessed++;
				if (i.isCorrupted()){ totalCorrupted++; }
				if (i.isDeleted()){ totalDeleted++; }
				if (i.isEncrypted()){ totalEncrypted++; }
			}
			
			SwingUtilities.invokeLater(()->{
				lblTotalProcessedCount.setText(FormatHelpers.formatNumber(totalProcessed));
				lblTotalCorruptedCount.setText(FormatHelpers.formatNumber(totalCorrupted));
				lblTotalDeletedCount.setText(FormatHelpers.formatNumber(totalDeleted));
				lblTotalEncryptedCount.setText(FormatHelpers.formatNumber(totalEncrypted));
				itemStatsTableModel.record(i);
			});
		});
		
		processor.whenCleaningUp(new ProcessorCleaningUpCallback() {
			@Override
			public void cleaningUp() {
				SwingUtilities.invokeLater(()->{
					log("Processor cleaning up...");
				});
			}
		});
		
		try {
			log("Worker Count: "+processor.getParallelProcessingSettings().get("workerCount"));
			if (System.getProperties().containsKey("nuix.processing.worker.timeout")){
				log("Worker Timeout (seconds): "+System.getProperty("nuix.processing.worker.timeout"));
			}
			log("Beginning processing...");
			
			job = processor.processAsync();
			
			//Enabled buttons
			btnPauseProcessing.setEnabled(true);
			btnStopProcessing.setEnabled(true);
			btnAbortProcessing.setEnabled(true);
			
			job.waitUntilFinished();
			log("Processing Finished");
		} catch (Exception e) {
			String message = "Error while processing: "+e.getMessage();
			log(message);
			logger.error(message,e);
		}
		
		//Disable buttons
		btnPauseProcessing.setEnabled(false);
		btnResumeProcessing.setEnabled(false);
		btnStopProcessing.setEnabled(false);
		btnAbortProcessing.setEnabled(false);
		
		elapsedTimer.stop();
		itemStatsTableModel.refresh();
		
		notifyProcessingFinishedListeners();
	}
	
	public void addProcessingFinishedListener(ProcessingFinishedListener listener){
		processingFinishedListeners.add(listener);
	}
	
	public void removeProcessingFinishedListener(ProcessingFinishedListener listener){
		processingFinishedListeners.remove(listener);
	}
	
	private void notifyProcessingFinishedListeners(){
		for(ProcessingFinishedListener listener : processingFinishedListeners){
			listener.processingFinished();
		}
	}
}
