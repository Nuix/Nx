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
import java.io.File;

import com.nuix.nx.NuixConnection;
import com.nuix.nx.controls.PathSelectionControl.ChooserType;

import nuix.Utilities;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/***
 * A control for providing settings relevant to local Nuix workers
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class LocalWorkerSettings extends JPanel {
	private JSpinner workerCount;
	private JSpinner memoryPerWorker;
	private PathSelectionControl workerTempDirectory;

	public LocalWorkerSettings() {
		setBorder(new TitledBorder(null, "Worker Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 100, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNumberOfWorkers = new JLabel("Number of Workers");
		GridBagConstraints gbc_lblNumberOfWorkers = new GridBagConstraints();
		gbc_lblNumberOfWorkers.anchor = GridBagConstraints.EAST;
		gbc_lblNumberOfWorkers.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumberOfWorkers.gridx = 0;
		gbc_lblNumberOfWorkers.gridy = 0;
		add(lblNumberOfWorkers, gbc_lblNumberOfWorkers);
		
		workerCount = new JSpinner();
		int initialWorkers = 2;
		int minWorkers = 1;
		int maxWorkers = 9999;
		Utilities util = NuixConnection.getUtilities();
		if(util != null && NuixConnection.getCurrentNuixVersion().isAtLeast("6.2.0")){
			initialWorkers = maxWorkers = util.getLicence().getWorkers();
		}
		workerCount.setModel(new SpinnerNumberModel(initialWorkers, minWorkers, maxWorkers, 1));
		GridBagConstraints gbc_workerCount = new GridBagConstraints();
		gbc_workerCount.fill = GridBagConstraints.HORIZONTAL;
		gbc_workerCount.insets = new Insets(0, 0, 5, 5);
		gbc_workerCount.gridx = 1;
		gbc_workerCount.gridy = 0;
		add(workerCount, gbc_workerCount);
		
		JLabel lblMemoryPerworkermb = new JLabel("Memory per-worker (MB)");
		GridBagConstraints gbc_lblMemoryPerworkermb = new GridBagConstraints();
		gbc_lblMemoryPerworkermb.anchor = GridBagConstraints.EAST;
		gbc_lblMemoryPerworkermb.insets = new Insets(0, 0, 5, 5);
		gbc_lblMemoryPerworkermb.gridx = 0;
		gbc_lblMemoryPerworkermb.gridy = 1;
		add(lblMemoryPerworkermb, gbc_lblMemoryPerworkermb);
		
		memoryPerWorker = new JSpinner();
		memoryPerWorker.setModel(new SpinnerNumberModel(new Integer(2048), new Integer(768), null, new Integer(1)));
		GridBagConstraints gbc_memoryPerWorker = new GridBagConstraints();
		gbc_memoryPerWorker.fill = GridBagConstraints.HORIZONTAL;
		gbc_memoryPerWorker.insets = new Insets(0, 0, 5, 5);
		gbc_memoryPerWorker.gridx = 1;
		gbc_memoryPerWorker.gridy = 1;
		add(memoryPerWorker, gbc_memoryPerWorker);
		
		JLabel lblWorkerTempDirectory = new JLabel("Worker temp directory");
		GridBagConstraints gbc_lblWorkerTempDirectory = new GridBagConstraints();
		gbc_lblWorkerTempDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblWorkerTempDirectory.insets = new Insets(0, 0, 0, 5);
		gbc_lblWorkerTempDirectory.gridx = 0;
		gbc_lblWorkerTempDirectory.gridy = 2;
		add(lblWorkerTempDirectory, gbc_lblWorkerTempDirectory);
		
		workerTempDirectory = new PathSelectionControl(ChooserType.DIRECTORY, (String) null, (String) null, "Choose worker temp directory");
		GridBagConstraints gbc_workerTempDirectory = new GridBagConstraints();
		gbc_workerTempDirectory.gridwidth = 2;
		gbc_workerTempDirectory.fill = GridBagConstraints.BOTH;
		gbc_workerTempDirectory.gridx = 1;
		gbc_workerTempDirectory.gridy = 2;
		add(workerTempDirectory, gbc_workerTempDirectory);
		workerTempDirectory.setPath("C:\\WorkerTemp");
	}

	@Override
	public void setEnabled(boolean value){
		for(Component c : getComponents()){
			c.setEnabled(value);
		}
	}
	
	public int getWorkerCount() {
		return (Integer) workerCount.getValue();
	}
	
	public void setWorkerCount(int value) {
		workerCount.setValue(value);
	}
	
	public int getMemoryPerWorker() {
		return (Integer) memoryPerWorker.getValue();
	}
	
	public void setMemoryPerWorker(int value) {
		memoryPerWorker.setValue(value);
	}
	
	public void setWorkerTempDirectory(String value){
		workerTempDirectory.setPath(value);
	}
	
	public String getWorkerTempDirectory() {
		return workerTempDirectory.getPath();
	}
	
	public File getWorkerTempDirectoryFile() {
		return workerTempDirectory.getPathFile();
	}
}
