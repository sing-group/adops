/*-
 * #%L
 * ADOPS
 * %%
 * Copyright (C) 2012 - 2017 David Reboiro-Jato, Miguel Reboiro-Jato, Jorge Vieira, Florentino Fdez-Riverola, Cristina P. Vieira, Nuno A. Fonseca
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package es.uvigo.ei.sing.adops.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.workbench.Workbench;
import say.swing.JFontChooser;

public class TextFileViewer extends JPanel {
	private final static Logger LOG = Logger.getLogger(TextFileViewer.class);
	
	private static final long serialVersionUID = 1L;

	private final JTextArea textArea;
	private final JTextField txtSearch;
	private final JCheckBox chkRegularExpression;
	private final Highlighter.HighlightPainter highlightPatiner;
	private final JFontChooser fontChooser;

  private final File file;
  private boolean wasModified = false;

  public TextFileViewer(final File file) {
    this(file, false, false);
  }

  public TextFileViewer(final File file, boolean isFasta, boolean checkSequencesAreMultipleOfThree) {
    super(new BorderLayout());

		this.file = file;

		// TEXT AREA
		this.textArea = new JTextArea(TextFileViewer.loadFile(file));
		this.textArea.setFont(
			new Font(
				Font.MONOSPACED,
				Font.PLAIN,
				this.textArea.getFont().getSize()
			)
		);
		this.textArea.setLineWrap(true);
		this.textArea.setWrapStyleWord(true);
		this.textArea.setEditable(false);

		this.highlightPatiner = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

		// OPTIONS PANEL
		final JPanel panelOptions = new JPanel(new BorderLayout());
		final JPanel panelOptionsEast = new JPanel(new FlowLayout());
		final JPanel panelOptionsWest = new JPanel(new FlowLayout());
		final JCheckBox chkLineWrap = new JCheckBox("Line wrap", true);
		final JButton btnChangeFont = new JButton("Change Font");

		final JLabel lblSearch = new JLabel("Search");
		this.txtSearch = new JTextField();
		this.chkRegularExpression = new JCheckBox("Reg. exp.", true);
		final JButton btnSearch = new JButton("Search");
		final JButton btnClear = new JButton("Clear");
		this.txtSearch.setColumns(12);
		// this.txtSearch.setOpaque(true);

		panelOptionsEast.add(btnChangeFont);
		panelOptionsEast.add(chkLineWrap);
		panelOptionsWest.add(lblSearch);
		panelOptionsWest.add(this.txtSearch);
		panelOptionsWest.add(this.chkRegularExpression);
		panelOptionsWest.add(btnSearch);
		panelOptionsWest.add(btnClear);

		if (isFasta) {
			panelOptionsWest.add(new JSeparator());

			final JButton btnExport = new JButton("Export...");

			panelOptionsWest.add(btnExport);

			btnExport.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							new ExportDialog(file, checkSequencesAreMultipleOfThree).setVisible(true);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(
								Workbench.getInstance().getMainFrame(),
								"Error reading fasta file: " + e1.getMessage(),
								"Export Error",
								JOptionPane.ERROR_MESSAGE
							);
						}
					}
				}
			);
		}

		panelOptions.add(panelOptionsWest, BorderLayout.WEST);
		panelOptions.add(panelOptionsEast, BorderLayout.EAST);

		this.fontChooser = new JFontChooser();

		this.add(new JScrollPane(this.textArea), BorderLayout.CENTER);
		this.add(panelOptions, BorderLayout.NORTH);

		chkLineWrap.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					textArea.setLineWrap(chkLineWrap.isSelected());
				}
			}
		);

		btnChangeFont.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeFont();
				}
			}
		);

		this.textArea.getDocument().addDocumentListener(
			new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					TextFileViewer.this.wasModified = true;
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					TextFileViewer.this.wasModified = true;
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					TextFileViewer.this.wasModified = true;
				}
			}
		);

		this.textArea.addFocusListener(
			new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if (TextFileViewer.this.wasModified) {
						try {
							FileUtils.write(TextFileViewer.this.file, TextFileViewer.this.textArea.getText());
							TextFileViewer.this.wasModified = false;
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		);

		final ActionListener alSearch = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSearch();
			}
		};
		txtSearch.addActionListener(alSearch);
		btnSearch.addActionListener(alSearch);

		btnClear.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					clearSearch();
				}
			}
		);
	}

	public void setEditable(boolean editable) {
		this.textArea.setEditable(editable);
	}

	public boolean isEditable() {
		return this.textArea.isEditable();
	}

	public String getText() {
		return textArea.getText();
	}

	private void updateSearch() {
		textArea.getHighlighter().removeAllHighlights();

		final String textToFind = txtSearch.getText();

		if (!textToFind.isEmpty()) {
			final String text = textArea.getText();

			if (this.chkRegularExpression.isSelected()) {
				try {
					final Pattern pattern = Pattern.compile(textToFind);
					this.txtSearch.setBackground(Color.WHITE);

					final Matcher matcher = pattern.matcher(text);

					while (matcher.find()) {
						try {
							textArea.getHighlighter().addHighlight(
								matcher.start(), matcher.end(), highlightPatiner
							);
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}
				} catch (PatternSyntaxException pse) {
					this.txtSearch.setBackground(Color.RED);
				}
			} else {
				final int textToFindLength = textToFind.length();

				int index = 0;
				while ((index = text.indexOf(textToFind, index)) != -1) {
					try {
						textArea.getHighlighter().addHighlight(
							index, index + textToFindLength, highlightPatiner
						);
						index += textToFindLength + 1;
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	private void changeFont() {
		fontChooser.setSelectedFont(textArea.getFont());
		if (fontChooser.showDialog(TextFileViewer.this) == JFontChooser.OK_OPTION) {
			textArea.setFont(fontChooser.getSelectedFont());
		}
	}

	private void clearSearch() {
		txtSearch.setText("");
		textArea.getHighlighter().removeAllHighlights();
	}

	private final static String loadFile(File file) {
		if (!file.isFile()) {
			throw new IllegalArgumentException("file must be a text file: " + file.getAbsolutePath());
		}
		
		try (FileReader fr = new FileReader(file)) {
			final StringBuilder sb = new StringBuilder();
			
			final char[] buffer = new char[1024];
			int c;
			while ((c = fr.read(buffer)) != -1) {
				sb.append(buffer, 0, c);
			}

			return sb.toString();
		} catch (IOException e) {
			LOG.error("Error loading file: " + file, e);
			
			throw new RuntimeException("Error loading file: " + file, e);
		}
	}
}
