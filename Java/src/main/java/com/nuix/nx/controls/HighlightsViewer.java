package com.nuix.nx.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.log4j.Logger;

import com.nuix.nx.NuixConnection;
import com.nuix.nx.controls.models.Choice;
import com.nuix.nx.controls.models.ChoiceTableModelChangeListener;
import com.nuix.nx.dialogs.CommonDialogs;

import nuix.Case;
import nuix.CaseStatistics;
import nuix.Highlight;
import nuix.Item;
import nuix.MetadataProfile;
import nuix.ReaderReadLogic;
import nuix.Text;
import nuix.TokenInfo;
import nuix.Utilities;
import nuix.WordList;
import nuix.WordListStore;

@SuppressWarnings("serial")
public class HighlightsViewer extends JPanel {
	private static Logger logger = Logger.getLogger(HighlightsViewer.class);
	
	public class ExtendedHighlight implements Comparable<ExtendedHighlight> {
		private Highlight base;
		private Color highlightColor = Color.YELLOW;
		private Set<String> sources = new HashSet<String>();
		
		public ExtendedHighlight(Highlight h) { base = h; }
		public ExtendedHighlight(Highlight h, Color c) { base = h; highlightColor = c; }
		public ExtendedHighlight(Highlight h, Color c, String source) { base = h; highlightColor = c; sources.add(source); }
		public int getEnd() { return base.getEnd(); }
		public int getStart() { return base.getStart(); }
		public String getText() { return base.getText(); }
		public Color getHighlightColor() { return highlightColor; }
		public void setHighlightColor(Color highlightColor) { this.highlightColor = highlightColor; }
		public void setHighlightColor(String hexColor) { setHighlightColor(Color.decode(hexColor)); }
		public String getSources() { return String.join("; ", sources); }
		public void addSource(String source) { this.sources.add(source); }
		
		@Override
		public int compareTo(ExtendedHighlight other) {
			if(this.getStart() != other.getStart()) {
				return Integer.compare(this.getStart(), other.getStart());
			} else {
				return Integer.compare(this.getEnd(), other.getEnd());
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + getEnd();
			result = prime * result + getStart();
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExtendedHighlight other = (ExtendedHighlight) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (getEnd() != other.getEnd())
				return false;
			if (getStart() != other.getStart())
				return false;
			return true;
		}
		
		private HighlightsViewer getEnclosingInstance() {
			return HighlightsViewer.this;
		}
	}
	
	private Item visualizedItem = null;
	private Case nuixCase = null;
	
	private int currentTextLength = 0;
	private int currentHighlightIndex = 0;
	private List<ExtendedHighlight> highlights = new ArrayList<ExtendedHighlight>();
	private String contentQuery = "";
	private String filteringQuery = "";
	private boolean termDataModified = false;
	
	private Color queryHighlightColor = Color.YELLOW;
	private Color wordListHighlightColor = Color.MAGENTA;
	private Color otherTermHighlightColor = Color.ORANGE;
	private Color expressionHighlightColor = Color.WHITE;
	
	private Map<Color,DefaultHighlighter.DefaultHighlightPainter> painters = new HashMap<Color,DefaultHighlighter.DefaultHighlightPainter>();
	
	private JTextArea textArea;
	private ItemTable itemTable;
	private List<Pattern> highlightExpressions = new ArrayList<Pattern>();
	private JTextField txtQuery;
	private JLabel lblHighlighIndexValue;
	private JTextField txtFilteringQuery;
	private ItemMetadataTable itemMetadataTable;
	private JSplitPane horizontalSplitPane;
	private JSplitPane verticalSplitPane;
	private boolean painted;
	private ChoiceTableControl<String> wordListChoices;
	private JTabbedPane tabbedPane;
	private JTextArea txtOtherTerms;
	private JLabel lblHighlightTextValue;
	private JLabel lblHighlightSourcesValue;

	public HighlightsViewer() {
		this(null);
	}
	
	public HighlightsViewer(Case nuixCase) {
		this.nuixCase = nuixCase;
		setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(tabbedPane);
		
		JPanel viewerPanel = new JPanel();
		tabbedPane.addTab("Review Highlights", null, viewerPanel, null);
		GridBagLayout gbl_viewerPanel = new GridBagLayout();
		gbl_viewerPanel.columnWidths = new int[]{400, 0};
		gbl_viewerPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_viewerPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_viewerPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		viewerPanel.setLayout(gbl_viewerPanel);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		viewerPanel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		txtQuery = new JTextField();
		txtQuery.setToolTipText("Nuix search syntax provided in the Content Query\" area is used both while searching for items AND when obtaining item highlighted terms.");
		txtQuery.setFont(new Font("Dialog", Font.PLAIN, 14));
		txtQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setContentQuery(txtQuery.getText());
			}
		});
		
		JLabel lblContentQuery = new JLabel("Content Query");
		lblContentQuery.setToolTipText("");
		GridBagConstraints gbc_lblContentQuery = new GridBagConstraints();
		gbc_lblContentQuery.insets = new Insets(0, 0, 5, 5);
		gbc_lblContentQuery.anchor = GridBagConstraints.EAST;
		gbc_lblContentQuery.gridx = 0;
		gbc_lblContentQuery.gridy = 0;
		panel_1.add(lblContentQuery, gbc_lblContentQuery);
		GridBagConstraints gbc_txtQuery = new GridBagConstraints();
		gbc_txtQuery.insets = new Insets(0, 0, 5, 0);
		gbc_txtQuery.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtQuery.gridx = 1;
		gbc_txtQuery.gridy = 0;
		panel_1.add(txtQuery, gbc_txtQuery);
		txtQuery.setColumns(10);
		
		JLabel lblFilteringQuery = new JLabel("Filtering Query");
		lblFilteringQuery.setToolTipText("");
		GridBagConstraints gbc_lblFilteringQuery = new GridBagConstraints();
		gbc_lblFilteringQuery.anchor = GridBagConstraints.EAST;
		gbc_lblFilteringQuery.insets = new Insets(0, 0, 0, 5);
		gbc_lblFilteringQuery.gridx = 0;
		gbc_lblFilteringQuery.gridy = 1;
		panel_1.add(lblFilteringQuery, gbc_lblFilteringQuery);
		
		txtFilteringQuery = new JTextField();
		txtFilteringQuery.setToolTipText("Nuix search syntax provided in the \"Filtering Query\" area is used only while searching for items, but will not be used when obtaining item highlighted terms.");
		txtFilteringQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFilteringQuery(txtFilteringQuery.getText());
			}
		});
		txtFilteringQuery.setFont(new Font("Dialog", Font.PLAIN, 14));
		GridBagConstraints gbc_txtFilteringQuery = new GridBagConstraints();
		gbc_txtFilteringQuery.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFilteringQuery.gridx = 1;
		gbc_txtFilteringQuery.gridy = 1;
		panel_1.add(txtFilteringQuery, gbc_txtFilteringQuery);
		txtFilteringQuery.setColumns(10);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.WEST;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.VERTICAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		viewerPanel.add(panel, gbc_panel);
		
		JButton btnNextItem = new JButton("Next Item");
		btnNextItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				itemTable.selectNextItem();
			}
		});
		
		JButton btnPreviousItem = new JButton("Previous Item");
		btnPreviousItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				itemTable.selectPreviousItem();
			}
		});
		btnPreviousItem.setIcon(new ImageIcon(HighlightsViewer.class.getResource("/com/nuix/nx/controls/resultset_previous.png")));
		panel.add(btnPreviousItem);
		btnNextItem.setIcon(new ImageIcon(HighlightsViewer.class.getResource("/com/nuix/nx/controls/resultset_next.png")));
		panel.add(btnNextItem);
		
		JButton btnPreviousHighlight = new JButton("Previous Highlight");
		panel.add(btnPreviousHighlight);
		btnPreviousHighlight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				previousHighlight();
			}
		});
		
		btnPreviousHighlight.setIcon(new ImageIcon(HighlightsViewer.class.getResource("/com/nuix/nx/controls/book_previous.png")));
		btnPreviousHighlight.setToolTipText("");
		
		JButton btnNextHighlight = new JButton("Next Highlight");
		panel.add(btnNextHighlight);
		btnNextHighlight.setToolTipText("");
		btnNextHighlight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextHighlight();
			}
		});
		
		btnNextHighlight.setIcon(new ImageIcon(HighlightsViewer.class.getResource("/com/nuix/nx/controls/book_next.png")));
		
		JLabel lblHighlightIndex = new JLabel("Highlight:");
		lblHighlightIndex.setFont(new Font("Dialog", Font.BOLD, 16));
		panel.add(lblHighlightIndex);
		
		lblHighlighIndexValue = new JLabel("0/0");
		panel.add(lblHighlighIndexValue);
		lblHighlighIndexValue.setFont(new Font("Dialog", Font.PLAIN, 16));
		
		JLabel lblHighlightText = new JLabel("Text:");
		lblHighlightText.setFont(new Font("Dialog", Font.BOLD, 16));
		panel.add(lblHighlightText);
		
		lblHighlightTextValue = new JLabel("    ");
		lblHighlightTextValue.setFont(new Font("Dialog", Font.PLAIN, 16));
		panel.add(lblHighlightTextValue);
		
		JLabel lblHighlightSources = new JLabel("Sources:");
		lblHighlightSources.setFont(new Font("Dialog", Font.BOLD, 16));
		panel.add(lblHighlightSources);
		
		lblHighlightSourcesValue = new JLabel("    ");
		lblHighlightSourcesValue.setFont(new Font("Dialog", Font.PLAIN, 16));
		panel.add(lblHighlightSourcesValue);
		
		horizontalSplitPane = new JSplitPane();
		horizontalSplitPane.setContinuousLayout(true);
		GridBagConstraints gbc_horizontalSplitPane = new GridBagConstraints();
		gbc_horizontalSplitPane.fill = GridBagConstraints.BOTH;
		gbc_horizontalSplitPane.gridx = 0;
		gbc_horizontalSplitPane.gridy = 2;
		viewerPanel.add(horizontalSplitPane, gbc_horizontalSplitPane);
		
		itemTable = new ItemTable();
		itemTable.setMinimumSize(new Dimension(200,200));
		horizontalSplitPane.setLeftComponent(itemTable);
		
		verticalSplitPane = new JSplitPane();
		verticalSplitPane.setContinuousLayout(true);
		verticalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		horizontalSplitPane.setRightComponent(verticalSplitPane);
		
		JScrollPane textScrollPane = new JScrollPane();
		verticalSplitPane.setRightComponent(textScrollPane);
		textScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("Dialog", Font.PLAIN, 14));
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textScrollPane.setViewportView(textArea);
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(textArea, popupMenu);
		
		JMenuItem menuItemAddSelectTerm = new JMenuItem("Add these to \"Other Terms\"");
		menuItemAddSelectTerm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedText = textArea.getSelectedText();
				if(visualizedItem != null && selectedText != null && selectedText.length() > 0) {
					for(TokenInfo token : visualizedItem.tokenise(selectedText)) {
						txtOtherTerms.append("\n"+token.getText());
					}
					rebuildHighlightsFromItem();
					rebuildHighlighters();
					scrollTo(0);
				}
			}
		});
		popupMenu.add(menuItemAddSelectTerm);
		
		itemMetadataTable = new ItemMetadataTable();
		verticalSplitPane.setLeftComponent(itemMetadataTable);
		
		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("Additional Terms", null, panel_2, null);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 25, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{25.0, 0.0, 75.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		JLabel lblNewLabel = new JLabel("Highlighted Word Lists");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_2.add(lblNewLabel, gbc_lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Other Terms");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel_2.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		wordListChoices = new ChoiceTableControl<String>();
		GridBagConstraints gbc_wordListChoices = new GridBagConstraints();
		gbc_wordListChoices.insets = new Insets(0, 0, 0, 5);
		gbc_wordListChoices.fill = GridBagConstraints.BOTH;
		gbc_wordListChoices.gridx = 0;
		gbc_wordListChoices.gridy = 1;
		panel_2.add(wordListChoices, gbc_wordListChoices);
		
		Utilities util = NuixConnection.getUtilities();
		WordListStore wordListStore = util.getWordListStore();
		List<Choice<String>> wlChoices = wordListStore.getWordListNames().stream()
				.map(wln -> new Choice<String>(wln))
				.collect(Collectors.toList()); 
		wordListChoices.setChoices(wlChoices);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 2;
		gbc_scrollPane.gridy = 1;
		panel_2.add(scrollPane, gbc_scrollPane);
		
		txtOtherTerms = new JTextArea();
		txtOtherTerms.setToolTipText("Additional terms to be higlighted.  Note: text here is provided to Item.tokenise and therefore may not be highlighted exactly as entered!");
		scrollPane.setViewportView(txtOtherTerms);
		
		// When user modifies other terms text area, we want to mark that highlights will need
		// rebuild once highlight view tab is selected again
		txtOtherTerms.getDocument().addDocumentListener(new DocumentListener() {
			private void docChanged() { termDataModified = true; }
			@Override
			public void removeUpdate(DocumentEvent e) { docChanged(); }
			@Override
			public void insertUpdate(DocumentEvent e) { docChanged(); }
			@Override
			public void changedUpdate(DocumentEvent e) { docChanged(); }
		});
		
		itemTable.whenSelectionChanges(new BiConsumer<Integer,Item>() {
			@Override
			public void accept(Integer rowIndex, Item item) {
				try {
					if(item != null) {
						visualizeItemText(item);	
					}
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					String exceptionAsString = sw.toString();
					CommonDialogs.showError("Error processing selection change: "+exceptionAsString);
					logger.error(e);
				}
			}
		});
		
		// We use this to basically mark a "dirty" flag regarding terms to higlight
		// so that when the highlight view tab becomes visible again we know to
		// rebuild and re-apply the highlights
		wordListChoices.onTableDataChanged(new ChoiceTableModelChangeListener() {
			@Override
			public void dataChanged() {
				termDataModified = true;
			}
		});
		
		// We detect if the user is changing back to the main tab with the highlight
		// viewer.  If they are and we see that they made changes on another tab such
		// that highlights need to be rebuilt, then we will rebuild them now.
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(termDataModified && tabbedPane.getSelectedIndex() == 0) {
					rebuildHighlightsFromItem();
					rebuildHighlighters();
					scrollTo(0);
					termDataModified = false;
				}
			}
		});
	}

	@Override
	public void paint(Graphics g) {
	    super.paint(g);

	    // Hacky solution to ensure split panes respond to these properly
	    // as these methods don't work right until component has had
	    // layout performed and has actual realized size
	    if (!painted) {
	        painted = true;
	        verticalSplitPane.setDividerLocation(0.25d);
	        verticalSplitPane.setResizeWeight(0.25d);
	        horizontalSplitPane.setDividerLocation(0.25d);
	        horizontalSplitPane.setResizeWeight(0.25d);
	    }
	}
	
	public void nextHighlight() {
		currentHighlightIndex++;
		if(currentHighlightIndex > highlights.size()-1) { currentHighlightIndex = 0; }
		try {
			scrollToHighlightIndex(currentHighlightIndex);
			rebuildHighlighters();
			updateCurrentHighlightLabels();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public void previousHighlight() {
		currentHighlightIndex--;
		if(currentHighlightIndex < 0) { currentHighlightIndex = highlights.size()-1; }
		try {
			scrollToHighlightIndex(currentHighlightIndex);
			rebuildHighlighters();
			updateCurrentHighlightLabels();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private void updateCurrentHighlightLabels() {
		if(highlights.size() == 0) {
			lblHighlighIndexValue.setText("0/0");
			lblHighlightTextValue.setText("    ");
			lblHighlightSourcesValue.setText("    ");
		} else {
			ExtendedHighlight eh = highlights.get(currentHighlightIndex);
			
			lblHighlighIndexValue.setText(String.format("%s/%s",currentHighlightIndex+1, highlights.size()));
			lblHighlightTextValue.setText(eh.getText());
			lblHighlightSourcesValue.setText(eh.getSources());
		}
	}
	
	public void setContentQuery(String query) {
		txtQuery.setText(query);
		realoadItems();
	}
	
	public void setFilteringQuery(String query) {
		txtFilteringQuery.setText(query);
		realoadItems();
	}
	
	private void realoadItems() {
		contentQuery = txtQuery.getText();
		filteringQuery = txtFilteringQuery.getText();
		
		// We accept 2 queries:
		// - Content Query: used for searching and highlight generation
		// - Filtering Query: used for searching, but not highlight generation
		// Either of these can be blank, but we need to AND them together appropriately
		// when they have values, so the following is to handle AND'ing them together
		// for searching, while still being able to cope with empty values.
		List<String> queryFragments = new ArrayList<String>();
		queryFragments.add(contentQuery);
		queryFragments.add(filteringQuery);
		queryFragments = queryFragments.stream()
			.map(q -> q.trim())
			.filter(q -> !q.isEmpty())
			.map(q -> "("+q+")")
			.collect(Collectors.toList());
		
		String fullQuery = String.join(" AND ", queryFragments);
		
		if(nuixCase != null) {
			try {
				Map<String,Object> searchOptions = new HashMap<String,Object>();
				List<String> defaultFields = new ArrayList<String>();
				defaultFields.add("content");
				searchOptions.put("defaultFields", defaultFields);
				List<Item> items = nuixCase.search(fullQuery,searchOptions);
				logger.info(String.format("%s items returned by query: %s", items.size(), fullQuery));
				itemTable.setItems(items);
			} catch (IOException e) {
				CommonDialogs.showError("Error while searching: "+e.getMessage());
				logger.error(e);
			}
		} else {
			CommonDialogs.showError("Component has not been provided Case object (did you provide a case in the constructor?)");
		}
	}
	
	public void setItemTableMetadataProfile(MetadataProfile profile) {
		itemTable.setProfile(profile);
	}
	
	public void setMetadataTableProfile(MetadataProfile profile) {
		itemMetadataTable.setProfile(profile);
	}

	public Collection<String> getOtherTerms() {
		List<String> result = new ArrayList<String>();
		for(String line : txtOtherTerms.getText().split("\\r?\\n")) {
			result.add(line);
		}
		return result;
	}

	public void setOtherTerms(Collection<String> otherTerms) throws Exception {
		txtOtherTerms.setText(String.join("\n", otherTerms));
		if(visualizedItem != null) {
			rebuildHighlightsFromItem();
			rebuildHighlighters();
			scrollTo(0);	
		}
	}
	
	public void addHighlightExpression(String expression) {
		highlightExpressions.add(Pattern.compile(expression,Pattern.CASE_INSENSITIVE));
	}
	
	private void visualizeItemText(Item item) throws Exception {
		visualizedItem = item;
		
		ReaderReadLogic<Boolean> textOperation = new ReaderReadLogic<Boolean>() {
			@Override
			public Boolean withReader(Reader reader) throws IOException {
				textArea.read(reader, null);
				return true;
			}
		};
		
		Text itemTextObject = item.getTextObject();
		itemTextObject.usingText(textOperation);
		currentTextLength = textArea.getText().length();
		
		SwingUtilities.invokeLater(()->{
			itemMetadataTable.setItem(visualizedItem);
			
			rebuildHighlightsFromItem();
			rebuildHighlighters();
			scrollTo(0);	
		});
	}
	
	private void rebuildHighlightsFromItem() {
		try {
			currentHighlightIndex = 0;
			highlights.clear();
			
			
			buildQueryHighlights();
			buildOtherTermsHighlights();
			buildExpressionHighlights();
			buildWordListHighlights();
			
			// We will now merge together duplicative highlights, partially because
			// highlight painters in text area act a little funny when they overlap
			Map<ExtendedHighlight,ExtendedHighlight> mergedHighlights = new HashMap<ExtendedHighlight,ExtendedHighlight>();
			for(ExtendedHighlight eh : highlights) {
				if(mergedHighlights.containsKey(eh)) {
					mergedHighlights.get(eh).addSource(eh.getSources());
				} else {
					mergedHighlights.put(eh, eh);
				}
			}
			
			highlights.clear();
			highlights.addAll(mergedHighlights.keySet());
			Collections.sort(highlights);
			
			updateCurrentHighlightLabels();
		} catch (Exception e) {
			CommonDialogs.showError("Error Rebuilding Highlights: "+e.getMessage());
			logger.error(e);
		}
	}
	
	private void buildWordListHighlights() throws Exception {
		Set<String> termSet = new HashSet<String>();
		
		// If we have word list names, then we need to fetch terms in those word lists
		Utilities util = NuixConnection.getUtilities();
		WordListStore wordListStore = util.getWordListStore();
		
		Set<String> existingWordListNames = wordListStore.getWordListNames()
				.stream()
				.map(wln -> wln.trim().toLowerCase())
				.collect(Collectors.toSet());
		
		for(String wordListName : getSelectedWordListNames()) {
			String normalizedWordListName = wordListName.trim().toLowerCase();
			
			if(!existingWordListNames.contains(normalizedWordListName)) {
				logger.info("Skipping inclusion of non-existent word list: "+wordListName);
			} else {
				termSet.clear();
				WordList wordList = wordListStore.getWordList(normalizedWordListName);
				for(String term : wordList) {
					// Item.getHighlights says this should be done
					List<TokenInfo> tokenInfos = visualizedItem.tokenise(term);
					for(TokenInfo tokenInfo : tokenInfos) {
						termSet.add(tokenInfo.getText());
					}
				}
				
				highlights.addAll(visualizedItem.getHighlights("", termSet)
						.stream()
						.filter(h -> h.getStart() <= currentTextLength) // Don't bother if beyond truncation
						.map(ex -> new ExtendedHighlight(ex,wordListHighlightColor,"WORD_LIST:"+wordListName))
						.collect(Collectors.toList()));
			}
		}
	}
	
	private void buildOtherTermsHighlights() throws Exception {
		Set<String> termSet = new HashSet<String>();
		
		// Also include directly provided other terms
		for(String term : getOtherTerms()) {
			// Item.getHighlights says this should be done
//			List<TokenInfo> tokenInfos = visualizedItem.tokenise(term);e
//			for(TokenInfo tokenInfo : tokenInfos) {
//				termSet.add(tokenInfo.getText());
//			}
			
			termSet.add(term);
		}
		
		highlights.addAll(visualizedItem.getHighlights("", termSet)
				.stream()
				.filter(h -> h.getStart() <= currentTextLength) // Don't bother if beyond truncation
				.map(ex -> new ExtendedHighlight(ex,otherTermHighlightColor,"OTHER_TERMS"))
				.collect(Collectors.toList()));
	}
	
	private void buildExpressionHighlights() throws Exception {
		Set<String> termSet = new HashSet<String>();
		
		if(highlightExpressions.size() > 0) {
			CaseStatistics caseStats = nuixCase.getStatistics();
			Map<String,Long> termStats = caseStats.getTermStatistics(String.format("guid:(%s)", visualizedItem.getGuid()));
			for(Map.Entry<String,Long> termStat : termStats.entrySet()) {
				for(Pattern expression : highlightExpressions) {
					if(expression.matcher(termStat.getKey()).find()) {
						List<TokenInfo> tokenInfos = visualizedItem.tokenise(termStat.getKey());
						for(TokenInfo tokenInfo : tokenInfos) {
							termSet.add(tokenInfo.getText());
						}
					}
				}
			}
		}
		
		highlights.addAll(visualizedItem.getHighlights("", termSet)
				.stream()
				.filter(h -> h.getStart() <= currentTextLength) // Don't bother if beyond truncation
				.map(ex -> new ExtendedHighlight(ex,expressionHighlightColor,"EXPRESSION"))
				.collect(Collectors.toList()));
	}
	
	private void buildQueryHighlights() throws Exception {
		highlights.addAll(visualizedItem.getHighlights(contentQuery, null)
				.stream()
				.filter(h -> h.getStart() <= currentTextLength) // Don't bother if beyond truncation
				.map(ex -> new ExtendedHighlight(ex,queryHighlightColor,"QUERY"))
				.collect(Collectors.toList()));
	}
	
	private void rebuildHighlighters() {
		try {
			removeAllHighlighters();
			if(highlights.size() > 0) {
				for (int i = 0; i < highlights.size(); i++) {
					ExtendedHighlight eh = highlights.get(i);
					if(currentHighlightIndex == i) {
						addHighlighter(eh.getStart(),eh.getEnd(),Color.GREEN);	
					} else {
						addHighlighter(eh.getStart(),eh.getEnd(),eh.getHighlightColor());
					}
					
				}
			}
		} catch (BadLocationException e) {
			CommonDialogs.showError("Error Rebuilding Highlighters: "+e.getMessage());
			logger.error(e);
		}
	}
	
	private void scrollTo(int position) {
		try {
			// < Sad Java 8 Sounds >
			// Rectangle2D viewRect = textArea.modelToView2D(position);
			// textArea.scrollRectToVisible(viewRect.getBounds());
			
			@SuppressWarnings("deprecation")
			Rectangle viewRect = textArea.modelToView(position);
			textArea.scrollRectToVisible(viewRect.getBounds());
			textArea.setCaretPosition(position);
		} catch (Exception e) {
			logger.info(String.format("Error Scrolling to Position %s", position), e);
		}
	}
	
	private void scrollToHighlight(ExtendedHighlight h) throws BadLocationException {
		scrollTo(h.getStart());
	}
	
	private void scrollToHighlightIndex(int highlightIndex) throws BadLocationException {
		if(highlights.size() == 0) { return; }
		if(highlightIndex < 0) { highlightIndex = 0; }
		if(highlightIndex > highlights.size()-1) { highlightIndex = highlights.size()-1; }
		scrollToHighlight(highlights.get(highlightIndex));
	}
	
	private void addHighlighterPainter(int startIndex, int endIndex, HighlightPainter painter) throws BadLocationException {
		if(startIndex < 0) { startIndex = 0; }
		if(endIndex > currentTextLength-1) { endIndex = currentTextLength-1; }
		if(endIndex < startIndex) { endIndex = startIndex; }
		
		textArea.getHighlighter().addHighlight(startIndex, endIndex, painter);
	}
	
	private void addHighlighter(int startIndex, int endIndex, Color color) throws BadLocationException {
		DefaultHighlightPainter painter = null;
		if(painters.containsKey(color)) {
			painter = painters.get(color);
		} else {
			painter = new DefaultHighlighter.DefaultHighlightPainter(color);
			painters.put(color, painter);
		}
		addHighlighterPainter(startIndex,endIndex,painter);
	}
	
	private void removeAllHighlighters() {
		textArea.getHighlighter().removeAllHighlights();
	}
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	public List<String> getSelectedWordListNames() {
		return wordListChoices.getCheckedValues();
	}

	public void setSelectedWordListNames(List<String> wordListNames) {
		wordListChoices.uncheckAllChoices();
		wordListChoices.setCheckedByLabels(wordListNames, true);
	}

	public Color getQueryHighlightColor() { return queryHighlightColor; }

	public void setQueryHighlightColor(Color queryHighlightColor) { this.queryHighlightColor = queryHighlightColor; }

	public Color getWordListHighlightColor() { return wordListHighlightColor; }

	public void setWordListHighlightColor(Color wordListHighlightColor) { this.wordListHighlightColor = wordListHighlightColor; }

	public Color getOtherTermHighlightColor() { return otherTermHighlightColor; }

	public void setOtherTermHighlightColor(Color otherTermHighlightColor) { this.otherTermHighlightColor = otherTermHighlightColor; }

	public Color getExpressionHighlightColor() { return expressionHighlightColor; }

	public void setExpressionHighlightColor(Color expressionHighlightColor) { this.expressionHighlightColor = expressionHighlightColor; }
	
}
