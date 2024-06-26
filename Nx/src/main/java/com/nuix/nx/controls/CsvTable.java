/******************************************
 Copyright 2018 Nuix
 http://www.apache.org/licenses/LICENSE-2.0
 *******************************************/

package com.nuix.nx.controls;

import com.nuix.nx.controls.models.CsvTableModel;
import com.nuix.nx.dialogs.CommonDialogs;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

/***
 * A table control designed with the idea of accepting data imported from a CSV.
 * @author Jason Wells
 *
 */
@SuppressWarnings("serial")
public class CsvTable extends JPanel {
    private JTable recordsTable;
    private CsvTableModel recordsTableModel = null;
    private JButton btnRemovedSelectedRows;
    private JButton btnAddRow;
    private JButton btnImportCsv;
    private String defaultImportDirectory = "C:\\";

    public CsvTable(List<String> headers) {
        recordsTableModel = new CsvTableModel(headers);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        btnImportCsv = new JButton("Import CSV");
        btnImportCsv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                File csvFile = CommonDialogs.openFileDialog(defaultImportDirectory, "Comma Separated Values", "csv", "Import CSV");
                if (csvFile != null) {
                    importCsv(csvFile);
                }
            }


        });
        GridBagConstraints gbc_btnImportCsv = new GridBagConstraints();
        gbc_btnImportCsv.insets = new Insets(0, 0, 5, 5);
        gbc_btnImportCsv.gridx = 0;
        gbc_btnImportCsv.gridy = 0;
        add(btnImportCsv, gbc_btnImportCsv);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        GridBagConstraints gbc_toolBar = new GridBagConstraints();
        gbc_toolBar.insets = new Insets(0, 0, 5, 0);
        gbc_toolBar.fill = GridBagConstraints.BOTH;
        gbc_toolBar.gridx = 1;
        gbc_toolBar.gridy = 0;
        add(toolBar, gbc_toolBar);

        btnAddRow = new JButton("");
        btnAddRow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Map<String, String> record = new HashMap<String, String>();
                for (int i = 0; i < headers.size(); i++) {
                    record.put(headers.get(i), "");
                }
                recordsTableModel.addRecord(record);
            }
        });
        btnAddRow.setToolTipText("Add Row");
        btnAddRow.setIcon(new ImageIcon(CsvTable.class.getResource("/icons/add.png")));
        toolBar.add(btnAddRow);

        btnRemovedSelectedRows = new JButton("");
        btnRemovedSelectedRows.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int[] selectedIndices = recordsTable.getSelectedRows();
                Arrays.sort(selectedIndices);
                for (int i = selectedIndices.length - 1; i >= 0; i--) {
                    recordsTableModel.removeRecordAt(selectedIndices[i]);
                }
            }
        });
        btnRemovedSelectedRows.setToolTipText("Removed Selected Rows");
        btnRemovedSelectedRows.setIcon(new ImageIcon(CsvTable.class.getResource("/icons/delete.png")));
        toolBar.add(btnRemovedSelectedRows);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 2;
        gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 1;
        add(scrollPane, gbc_scrollPane);

        recordsTable = new JTable(recordsTableModel);
        recordsTable.setFont(new Font("Arial MS Unicode", Font.PLAIN, 14));
        recordsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        recordsTable.setFillsViewportHeight(true);
        scrollPane.setViewportView(recordsTable);

        recordsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        recordsTable.getColumnModel().getColumn(0).setMinWidth(40);
        recordsTable.getColumnModel().getColumn(0).setMaxWidth(40);
        recordsTable.getColumnModel().getColumn(0).setWidth(40);
    }

    private void importCsv(File csvFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.EXCEL.builder()
                    .setSkipHeaderRecord(false)
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(br);

            String[] headers = null;
            Set<String> csvHeadersLookup = new HashSet<String>();
            Set<String> expectedHeaders = new HashSet<String>(recordsTableModel.getHeaders());
            List<String> missingHeaders = new ArrayList<String>();

            for (CSVRecord csvRecord : records) {
                String[] values = csvRecord.values();
                if (headers == null) {
                    headers = values;

                    System.out.println("Headers:");
                    for (int i = 0; i < headers.length; i++) {
                        //CSV reader leaks in newlines for some reason, gotta chop em off
                        headers[i] = headers[i].trim();
                        System.out.println(i + ": " + headers[i]);
                        csvHeadersLookup.add(headers[i]);
                    }

                    for (String expectedHeader : expectedHeaders) {
                        if (!csvHeadersLookup.contains(expectedHeader)) {
                            missingHeaders.add(expectedHeader);
                        }
                    }
                } else {
                    if (values == null) {
                        break;
                    } else {
                        Map<String, String> record = new HashMap<String, String>();
                        // Populate expected fields with defaults
                        for (String expectedHeader : expectedHeaders) {
                            record.put(expectedHeader, "");
                        }

                        for (int i = 0; i < headers.length; i++) {
                            String header = headers[i];
                            if (expectedHeaders.contains(header)) {
                                if (i > values.length - 1) {
                                    // Handle empty column value which will be surfaced
                                    // as a shorter value array if the empty value is at
                                    // the end
                                    record.put(header, "");
                                } else {
                                    record.put(header, values[i].trim());
                                }
                            }
                        }
                        recordsTableModel.addRecord(record);
                    }
                }
            }

            if (!missingHeaders.isEmpty()) {
                StringBuilder message = new StringBuilder();
                message.append("The following expected headers were not found in CSV:\n\n");
                message.append(String.join("\n", missingHeaders));
                message.append("\n\nImported data from columns which were present (if any).");
                CommonDialogs.showInformation(message.toString(), "Some Columns Missing");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            CommonDialogs.showError("Error importing CSV: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            CommonDialogs.showError("Error importing CSV: " + e.getMessage());
        }
    }

    public List<String> getHeaders() {
        return recordsTableModel.getHeaders();
    }

    public List<Map<String, String>> getRecords() {
        return recordsTableModel.getRecords();
    }

    public void addRecord(Map<String, String> record) {
        recordsTableModel.addRecord(record);
    }

    public JTable getTable() {
        return recordsTable;
    }

    @Override
    public void setEnabled(boolean value) {
        recordsTable.setEnabled(value);
        btnImportCsv.setEnabled(value);
        btnAddRow.setEnabled(value);
        btnRemovedSelectedRows.setEnabled(value);
        super.setEnabled(value);
    }

    public String getDefaultImportDirectory() {
        return defaultImportDirectory;
    }

    public void setDefaultImportDirectory(String defaultImportDirectory) {
        this.defaultImportDirectory = defaultImportDirectory;
    }
}
