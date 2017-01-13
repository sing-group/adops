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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import es.uvigo.ei.sing.adops.datatypes.AlignmentConfidences;
import es.uvigo.ei.sing.adops.datatypes.AlignmentConfidences.Confidence;

public class AlignmentTextViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final int DEFAULT_BLOCKS_PER_LINE = 9;
	private static final int DEFAULT_BLOCK_LENGTH = 10;
	private static final int DEFAULT_LABEL_TAB = 3;
	private static final int DEFAULT_LABEL_LENGTH = 10;
	private static final int DEFAULT_FONT_SIZE = 10;
	
	private final Configuration configuration = new Configuration();
	private final AlignmentConfidences confidences;
	private final int[] scores;
	private final HTMLDocument document;
	private final JEditorPane teDocument;
	private final JComboBox<String> cmbModels;
	
	public AlignmentTextViewer(final AlignmentConfidences confidences) {
		this(confidences, null);
	}
	
	public AlignmentTextViewer(final AlignmentConfidences confidences, final int[] scores) {
		super(new BorderLayout());
		
		this.confidences = confidences;
		this.scores = scores;
		
		// CONTROL PANEL
		final JPanel controlPanel = new JPanel(new BorderLayout());
		
		final JComponent selectionComponent;
		if (confidences.hasModels()) {
			this.cmbModels = new JComboBox<>(new Vector<String>(confidences.getModels()));
			this.cmbModels.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					AlignmentTextViewer.this.updateHtml();
				}
			});
			this.cmbModels.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AlignmentTextViewer.this.updateHtml();
				}
			});
			
			selectionComponent = this.cmbModels;
		} else {
			this.cmbModels = null;
			selectionComponent = new JLabel("No Positively Selected Sites");
		}
		
		final JButton btnDisplayConfiguration = new JButton(new AbstractAction("Display Configuration") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				AlignmentTextViewer.this.displayConfigurer.setLocationRelativeTo(AlignmentTextViewer.this);
				AlignmentTextViewer.this.displayConfigurer.pack();
				AlignmentTextViewer.this.displayConfigurer.setVisible(true);
			}
		});
		
//		final JButton btnExportToPDF = new JButton(new AbstractAction("Export to PDF") {
//			
//			@Override
//			public void actionPerformed(ActionEvent event) {
//				try {
//					final JFileChooser fileChooser = new JFileChooser(new File("."));
//					
//					if (fileChooser.showSaveDialog(AlignmentTextViewer.this) == JFileChooser.APPROVE_OPTION) {
//						
//						com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
//						PdfWriter.getInstance(document, new FileOutputStream(fileChooser.getSelectedFile()));
//						document.open();
//						document.addAuthor("Author of the Doc");
//						document.addCreator("Creator of the Doc");
//						document.addSubject("Subject of the Doc");
//						document.addCreationDate();
//						document.addTitle("This is the title");
//						
//						//SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
//						//SAXmyHtmlHandler shh = new SAXmyHtmlHandler(document);
//						
//						HTMLWorker htmlWorker = new HTMLWorker(document);
////						String str = "<html><head><title>titlu</title></head><body><table><tr><td><p style=’font-size: 10pt; font-family: Times’>" +
////								"Cher Monsieur,</p><br><p align=’justify’ style=’text-indent: 2em; font-size: 10pt; font-family: Times’>" +
////								"asdasdasdsadas<br></p><p align=’justify’ style=’text-indent: 2em; font-size: 10pt; font-family: Times’>" +
////								"En vous remerciant &agrave; nouveau de la confiance que vous nous t&eacute;moignez,</p>" +
////								"<br><p style=’font-size: 10pt; font-family: Times’>Bien Cordialement,<br>" +
////								"<br>ADMINISTRATEUR ADMINISTRATEUR<br>Ligne directe : 04 42 91 52 10<br>Acadomia&reg; – " +
////								"37 BD Aristide Briand  – 13100 Aix en Provence  </p></td></tr></table></body></html>";
//						htmlWorker.parse(new StringReader(AlignmentTextViewer.this.modelToHtml(true)));
//						
//						document.close();
//					}
//					
//
//				} catch(DocumentException e) {
//				e.printStackTrace();
//				} catch (FileNotFoundException e) {
//				e.printStackTrace();
//				} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//				} catch (IOException e) {
//				e.printStackTrace();
//				}
//			}
//		});
		
		this.editor = new HTMLEditorKit();
		this.editor.getStyleSheet().addRule(this.configuration.getRules());
		this.document = (HTMLDocument) editor.createDefaultDocument();
		
		this.teDocument = new JEditorPane();
		this.teDocument.setContentType("text/html");
		this.teDocument.setEditable(false);
		this.teDocument.setEditorKit(editor);
		this.teDocument.setDocument(this.document);
		this.teDocument.setText(this.modelToHtml(true));
		
		
		// COMPONENT AGGREGATION
		controlPanel.add(selectionComponent, BorderLayout.WEST);
		controlPanel.add(btnDisplayConfiguration, BorderLayout.EAST);
//		controlPanel.add(btnExportToPDF, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.NORTH);
		this.add(new JScrollPane(teDocument), BorderLayout.CENTER);
	}
	
	protected void printChar(StringBuilder sb, Confidence confidence, char c, boolean inlineStyle) {
		if (confidence != null) {
			if (inlineStyle) {
				Color background = null, foreground = null;
				
				if (confidence.getNeb() > 0.95d && confidence.getBeb() > 0.95d) {
					background = this.configuration.getNeb95beb95Background();
					foreground = this.configuration.getNeb95beb95Foreground();
				} else if (confidence.getNeb() > 0.95d && confidence.getBeb() > 0.90d) {
					background = this.configuration.getNeb95beb9095Background();
					foreground = this.configuration.getNeb95beb9095Foreground();
				} else if (confidence.getNeb() > 0.90d && confidence.getBeb() > 0.95d) {
					background = this.configuration.getNeb9095beb95Background();
					foreground = this.configuration.getNeb9095beb95Foreground();
				} else if (confidence.getNeb() > 0.90d && confidence.getBeb() > 0.90d) {
					background = this.configuration.getNeb9095beb9095Background();
					foreground = this.configuration.getNeb9095beb9095Foreground();
				}
				
				if (background == null || foreground == null) {
					sb.append(c);
				} else {
					sb.append("<span style=\"color: ")
						.append(Configuration.toHtmlColor(foreground))
						.append("; background-color: ")
						.append(Configuration.toHtmlColor(background))
						.append("\">")
						.append(c)
					.append("</span>");
				}
			} else {
				String className = null;
				
				if (confidence.getNeb() > 0.95d && confidence.getBeb() > 0.95d) {
					className = "neb95_beb95";
				} else if (confidence.getNeb() > 0.95d && confidence.getBeb() > 0.90d) {
					className = "neb95_beb9095";
				} else if (confidence.getNeb() > 0.90d && confidence.getBeb() > 0.95d) {
					className = "neb9095_beb95";
				} else if (confidence.getNeb() > 0.90d && confidence.getBeb() > 0.90d) {
					className = "neb9095_beb9095";
				} 
				
				if (className == null) {
					sb.append(c);
				} else {
					sb.append("<span class=\"").append(className).append("\">").append(c).append("</span>");
				}
			}
		} else {
			sb.append(c);
		}
	}
	
	private final static String strip(String text, int length) {
		if (text.length() < length) {
			for (int i = text.length(); i < length; i++) {
				text += ' ';
			}
		} else if (text.length() > length) {
			text = text.substring(0, length);
		}
		
		return text;
	}
	
	private final static void tab(StringBuilder sb, int tabs) {
		for (int i = 0; i < tabs; i++) {
			sb.append(' ');
		}
	}
	
	protected synchronized void updateHtml() {
		this.teDocument.setText(this.modelToHtml(true));
		this.editor.getStyleSheet().addRule(this.configuration.getRules());
	}
	
	protected synchronized void resetToDefault() {
		this.configuration.reset();
		this.updateHtml();
	}
	
	protected String modelToHtml(boolean inlineStyle) {
		final StringBuilder sb = new StringBuilder();
		final Map<Integer, Confidence> model = 
			(this.confidences.hasModels())?
					this.confidences.getModel((String) this.cmbModels.getSelectedItem()):
					Collections.<Integer, Confidence>emptyMap();
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new InputStreamReader(AlignmentTextViewer.class.getResourceAsStream("alignment.template.html")));
			
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals("[HEADER]")) {
					if (!inlineStyle) {
						sb.append("<style type=\"text/css\">\n");
						sb.append(this.configuration.getRules());
						sb.append("</style>");
					}
				} else if (line.trim().equals("[SEQUENCES]")) {
					final int seqLength = this.confidences.sequenceLength();
					final int blockLength = this.configuration.getBlockLength();
					final int blocksPerLine = this.configuration.getBlocksPerLine();
					final int labelTab = this.configuration.getLabelTab();
					final int labelLength = this.configuration.getLabelLength();
					
					int offset = 0;
					while (offset < seqLength) {
						if (this.configuration.isShowIndexes() && blockLength > 5) {
							sb.append("<span class=\"indexes\">");
							sb.append(strip("Indexes", labelLength + labelTab));
							
							int localOffset = offset;
							int indexOffset = offset;
							for (int blockIndex = 0; blockIndex < blocksPerLine; blockIndex++) {
								final int limit = Math.min(localOffset + blockLength, seqLength);
								
								int written = 0;
								for (int i = indexOffset; i < limit; i++) {
									final Integer movedIndex = this.confidences.getMovedIndex(i);
									
									if (movedIndex == null || (movedIndex.intValue() != 1 && movedIndex%5 != 0)) {
										sb.append(' ');
										written++;
									} else {
										final String toWrite = movedIndex.toString();
										sb.append(toWrite);
										i += toWrite.length()-1;
										written += toWrite.length();
									}
								}
								
								sb.append(' ');
								localOffset += blockLength;
								indexOffset += written;
							}
							
							sb.append("</span>\n");
						}
						
						for (Map.Entry<String, String> sequence : this.confidences.getSequences().entrySet()) {
							sb.append(strip(sequence.getKey(), labelLength));
							tab(sb, labelTab);
							
							final String seqString = sequence.getValue();
							
							int localOffset = offset;
							for (int blockIndex = 0; blockIndex < blocksPerLine; blockIndex++) {
								final int limit = Math.min(localOffset + blockLength, seqLength);
								
								for (int i = localOffset; i < limit; i++) {
									printChar(sb, model.get(i+1), seqString.charAt(i), inlineStyle);
								}
								
								if (limit == seqLength) break;
								
								localOffset += blockLength;
								
//								if (localOffset + blockLength >= seqLength) {
//									for (int i = localOffset; i < seqLength; i++) {
//										printChar(sb, model.get(i+1), seqString.charAt(i), inlineStyle);
//									}
//									break;
//								} else {
//									for (int i = localOffset; i < localOffset + blockLength; i++) {
//										printChar(sb, model.get(i+1), seqString.charAt(i), inlineStyle);
//									}
//									localOffset += blockLength;
//								}
								sb.append(' ');
							}
							
							sb.append('\n');
						}
						
						if (this.configuration.isShowScores()) {
							sb.append("<span class=\"scores\">");
							sb.append(strip("Scores", labelLength + labelTab));
							
							int localOffset = offset;
							for (int blockIndex = 0; blockIndex < blocksPerLine; blockIndex++) {
								final int limit = Math.min(localOffset + blockLength, seqLength);
								
								for (int i = localOffset; i < limit; i++) {
									final char scoreChar = this.scores[i] < 0 ? 
										'-' : Integer.toString(this.scores[i]).charAt(0);
									printChar(sb, model.get(i+1), scoreChar, inlineStyle);
								}
								
								if (limit == seqLength) break;
								
								localOffset += blockLength;
								sb.append(' ');
							}
							
							sb.append('\n');
							sb.append("</span>\n");
						}
						
						sb.append('\n');
						offset += blockLength * blocksPerLine;
					}
				} else {
					sb.append(line).append('\n');
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (reader != null) 
				try { reader.close(); }
				catch(IOException ioe) {}
		}
		
		
		return sb.toString();
	}
	
	private JFrame displayConfigurer = new JFrame("Display Configuration") {
		private static final long serialVersionUID = 1L;
		final JLabel lblNeb95Beb95 = new JLabel("NEB 95% - BEB 95%");
		final JLabel lblNeb95Beb9095 = new JLabel("NEB 95% - BEB 90-95%");
		final JLabel lblNeb9095Beb95 = new JLabel("NEB 90-95% - BEB 95%");
		final JLabel lblNeb9095Beb9095 = new JLabel("NEB 90-95% - BEB 90-95%");
		
		final class ChangeColorAction extends AbstractAction {
			private static final long serialVersionUID = 1L;

			private final String title;
			private final Method getter, setter;
			
			public ChangeColorAction(String label, String attribute, String title) throws SecurityException, NoSuchMethodException {
				super(label);
				
				this.title = title;
				
				attribute = Character.toUpperCase(attribute.charAt(0)) + attribute.substring(1);
				
				this.getter = Configuration.class.getMethod("get" + attribute);
				this.setter = Configuration.class.getMethod("set" + attribute, Color.class);
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					final Color newColor = JColorChooser.showDialog(
						AlignmentTextViewer.this.displayConfigurer, 
						this.title, 
						(Color) this.getter.invoke(AlignmentTextViewer.this.configuration)
					);
					
					if (newColor != null)
						this.setter.invoke(AlignmentTextViewer.this.configuration, newColor);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		{
			try {
				this.setResizable(false);
				this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				
				final JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
				final JPanel configurationPanel = new JPanel(new GridLayout(4, 3, 10, 5));
				configurationPanel.setBorder(BorderFactory.createTitledBorder("Color Options"));
				
				this.lblNeb95Beb95.setOpaque(true);
				this.lblNeb95Beb95.setBackground(AlignmentTextViewer.this.configuration.getNeb95beb95Background());
				this.lblNeb95Beb95.setForeground(AlignmentTextViewer.this.configuration.getNeb95beb95Foreground());
				final JButton btnNeb95Beb95Background = new JButton(
					new ChangeColorAction("Background", "neb95beb95Background", "NEB 95% - BEB 95% Background")
				);
				final JButton btnNeb95Beb95Foreground = new JButton(
					new ChangeColorAction("Foreground", "neb95beb95Foreground", "NEB 95% - BEB 95% Foreground")
				);
				
				this.lblNeb95Beb9095.setOpaque(true);
				this.lblNeb95Beb9095.setBackground(AlignmentTextViewer.this.configuration.getNeb95beb9095Background());
				this.lblNeb95Beb9095.setForeground(AlignmentTextViewer.this.configuration.getNeb95beb9095Foreground());
				final JButton btnNeb95Beb9095Background = new JButton(
					new ChangeColorAction("Background", "neb95beb9095Background", "NEB 95% - BEB 90-95% Background")
				);
				final JButton btnNeb95Beb9095Foreground = new JButton(
					new ChangeColorAction("Foreground", "neb95beb9095Foreground", "NEB 95% - BEB 90-95% Foreground")
				);
				
				this.lblNeb9095Beb95.setOpaque(true);
				this.lblNeb9095Beb95.setBackground(AlignmentTextViewer.this.configuration.getNeb9095beb95Background());
				this.lblNeb9095Beb95.setForeground(AlignmentTextViewer.this.configuration.getNeb9095beb95Foreground());
				final JButton btnNeb9095Beb95Background = new JButton(
						new ChangeColorAction("Background", "neb9095beb95Background", "NEB 90-95% - BEB 95% Background")
				);
				final JButton btnNeb9095Beb95Foreground = new JButton(
					new ChangeColorAction("Foreground", "neb9095beb95Foreground", "NEB 90-95% - BEB 95% Foreground")
				);
				
				this.lblNeb9095Beb9095.setOpaque(true);
				this.lblNeb9095Beb9095.setBackground(AlignmentTextViewer.this.configuration.getNeb9095beb9095Background());
				this.lblNeb9095Beb9095.setForeground(AlignmentTextViewer.this.configuration.getNeb9095beb9095Foreground());
				final JButton btnNeb9095Beb9095Background = new JButton(
					new ChangeColorAction("Background", "neb9095beb9095Background", "NEB 90-95% - BEB 90-95% Background")
				);
				final JButton btnNeb9095Beb9095Foreground = new JButton(
					new ChangeColorAction("Foreground", "neb9095beb9095Foreground", "NEB 90-95% - BEB 90-95% Foreground")
				);
				
				configurationPanel.add(this.lblNeb95Beb95);
				configurationPanel.add(btnNeb95Beb95Background);
				configurationPanel.add(btnNeb95Beb95Foreground);
				configurationPanel.add(this.lblNeb95Beb9095);
				configurationPanel.add(btnNeb95Beb9095Background);
				configurationPanel.add(btnNeb95Beb9095Foreground);
				configurationPanel.add(this.lblNeb9095Beb95);
				configurationPanel.add(btnNeb9095Beb95Background);
				configurationPanel.add(btnNeb9095Beb95Foreground);
				configurationPanel.add(this.lblNeb9095Beb9095);
				configurationPanel.add(btnNeb9095Beb9095Background);
				configurationPanel.add(btnNeb9095Beb9095Foreground);

				final JPanel controlPanelWrapper = new JPanel(new BorderLayout());
				controlPanelWrapper.setBorder(BorderFactory.createTitledBorder("Text Options"));
				
				final JPanel controlPanel = new JPanel(new GridLayout(5, 3, 5, 5));
				final JLabel lblLabelLength = new JLabel("Label Length");
				final JSpinner spnLabelLength = new JSpinner(new SpinnerNumberModel(DEFAULT_LABEL_LENGTH, 1, Integer.MAX_VALUE, 1));
				lblLabelLength.setLabelFor(spnLabelLength);
				
				final JLabel lblLabelTab = new JLabel("Label Tab");
				final JSpinner spnLabelTab = new JSpinner(new SpinnerNumberModel(DEFAULT_LABEL_TAB, 1, Integer.MAX_VALUE, 1));
				lblLabelTab.setLabelFor(spnLabelTab);
				
				final JLabel lblBlockLength = new JLabel("Block Length");
				final JSpinner spnBlockLength = new JSpinner(new SpinnerNumberModel(DEFAULT_BLOCK_LENGTH, 1, Integer.MAX_VALUE, 1));
				lblBlockLength.setLabelFor(spnBlockLength);
				
				final JLabel lblBlocksPerLine= new JLabel("Blocks Per Line");
				final JSpinner spnBlockPerLine = new JSpinner(new SpinnerNumberModel(DEFAULT_BLOCKS_PER_LINE, 1, Integer.MAX_VALUE, 1));
				lblBlocksPerLine.setLabelFor(spnBlockPerLine);
				
				final JLabel lblFontSize= new JLabel("Font Size");
				final JLabel lblFontPts= new JLabel("pt");
				final JSpinner spnFontSize = new JSpinner(new SpinnerNumberModel(DEFAULT_FONT_SIZE, 1, Integer.MAX_VALUE, 1));
				lblFontSize.setLabelFor(spnFontSize);
				
				final JCheckBox cbShowIndexes = new JCheckBox("Show indexes (block length must be 5 or greater)");
				final JCheckBox cbShowScores = new JCheckBox("Show scores");
//				cbShowScores.setEnabled(AlignmentTextViewer.this.scores != null);

				controlPanel.add(lblFontSize);
				controlPanel.add(spnFontSize);
				controlPanel.add(lblFontPts);
				controlPanel.add(lblLabelLength);
				controlPanel.add(spnLabelLength);
				controlPanel.add(new JLabel());
				controlPanel.add(lblLabelTab);
				controlPanel.add(spnLabelTab);
				controlPanel.add(new JLabel());
				controlPanel.add(lblBlockLength);
				controlPanel.add(spnBlockLength);
				controlPanel.add(new JLabel());
				controlPanel.add(lblBlocksPerLine);
				controlPanel.add(spnBlockPerLine);
				controlPanel.add(new JLabel());
				
				final JPanel cbPanel = new JPanel(new GridLayout(2, 1));
				cbPanel.add(cbShowIndexes);
				cbPanel.add(cbShowScores);
				
				controlPanelWrapper.add(controlPanel, BorderLayout.CENTER);
				controlPanelWrapper.add(cbPanel, BorderLayout.SOUTH);
				

				final JPanel buttonsPanel = new JPanel();
				final JCheckBox cbLiveUpdate = new JCheckBox("Live Update", true);
				final JButton btnUpdate = new JButton(new AbstractAction("Update") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						AlignmentTextViewer.this.updateHtml();						
					}
				});
				final JButton btnReset = new JButton(new AbstractAction("Reset to Default") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						AlignmentTextViewer.this.resetToDefault();
					}
				});
				final JButton btnClose = new JButton(new AbstractAction("Close") {
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					};
				});
				cbLiveUpdate.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						btnUpdate.setEnabled(!cbLiveUpdate.isSelected());
					}
				});
				btnUpdate.setEnabled(false);
				
				buttonsPanel.add(cbLiveUpdate);
				buttonsPanel.add(btnUpdate);
				buttonsPanel.add(btnReset);
				buttonsPanel.add(btnClose);
				
				mainPanel.add(controlPanelWrapper, BorderLayout.NORTH);
				mainPanel.add(configurationPanel, BorderLayout.CENTER);
				mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
				
				this.setContentPane(mainPanel);
				

				
				spnLabelLength.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						AlignmentTextViewer.this.configuration.setLabelLength((Integer) spnLabelLength.getValue());
					}
				});
				spnLabelTab.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						AlignmentTextViewer.this.configuration.setLabelTab((Integer) spnLabelTab.getValue());
					}
				});
				spnBlockLength.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						AlignmentTextViewer.this.configuration.setBlockLength((Integer) spnBlockLength.getValue());
					}
				});
				spnBlockPerLine.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						AlignmentTextViewer.this.configuration.setBlocksPerLine((Integer) spnBlockPerLine.getValue());
					}
				});
				spnFontSize.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						AlignmentTextViewer.this.configuration.setFontSize((Integer) spnFontSize.getValue());
					}
				});
				cbShowIndexes.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AlignmentTextViewer.this.configuration.setShowIndexes(cbShowIndexes.isSelected());
					};
				});
				cbShowScores.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AlignmentTextViewer.this.configuration.setShowScores(cbShowScores.isSelected());
					};
				});
				
				AlignmentTextViewer.this.configuration.addObserver(new Observer() {
					public void update(Observable o, Object arg) {
						if (cbLiveUpdate.isSelected()) {
							AlignmentTextViewer.this.updateHtml();
						}
						
						spnLabelLength.setValue(AlignmentTextViewer.this.configuration.getLabelLength());
						spnLabelTab.setValue(AlignmentTextViewer.this.configuration.getLabelTab());
						spnBlockLength.setValue(AlignmentTextViewer.this.configuration.getBlockLength());
						spnBlockPerLine.setValue(AlignmentTextViewer.this.configuration.getBlocksPerLine());
						spnFontSize.setValue(AlignmentTextViewer.this.configuration.getFontSize());
						
						lblNeb95Beb95.setBackground(AlignmentTextViewer.this.configuration.getNeb95beb95Background());
						lblNeb95Beb95.setForeground(AlignmentTextViewer.this.configuration.getNeb95beb95Foreground());
						
						lblNeb95Beb9095.setBackground(AlignmentTextViewer.this.configuration.getNeb95beb9095Background());
						lblNeb95Beb9095.setForeground(AlignmentTextViewer.this.configuration.getNeb95beb9095Foreground());
						
						lblNeb9095Beb95.setBackground(AlignmentTextViewer.this.configuration.getNeb9095beb95Background());
						lblNeb9095Beb95.setForeground(AlignmentTextViewer.this.configuration.getNeb9095beb95Foreground());
						
						lblNeb9095Beb9095.setBackground(AlignmentTextViewer.this.configuration.getNeb9095beb9095Background());
						lblNeb9095Beb9095.setForeground(AlignmentTextViewer.this.configuration.getNeb9095beb9095Foreground());
						
						cbShowIndexes.setSelected(AlignmentTextViewer.this.configuration.isShowIndexes());
						cbShowScores.setSelected(AlignmentTextViewer.this.configuration.isShowScores());
					};
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private HTMLEditorKit editor;
	
	@SuppressWarnings("unused")
	private final static class Configuration extends Observable {
		private int labelLength = DEFAULT_LABEL_LENGTH;
		private int labelTab = DEFAULT_LABEL_TAB;
		private int blockLength = DEFAULT_BLOCK_LENGTH;
		private int blocksPerLine = DEFAULT_BLOCKS_PER_LINE;
		private int fontSize = DEFAULT_FONT_SIZE;
		private Color neb95beb95Background = Color.YELLOW;
		private Color neb95beb9095Background = Color.RED;
		private Color neb9095beb95Background = Color.BLUE;
		private Color neb9095beb9095Background = Color.GREEN;
		private Color neb95beb95Foreground = Color.BLACK;
		private Color neb95beb9095Foreground = Color.WHITE;
		private Color neb9095beb95Foreground = Color.WHITE;
		private Color neb9095beb9095Foreground = Color.BLACK;
		private boolean showIndexes = false;
		private boolean showScores = false;
		
		public void reset() {
			this.labelLength = DEFAULT_LABEL_LENGTH;
			this.labelTab = DEFAULT_LABEL_TAB;
			this.blockLength = DEFAULT_BLOCK_LENGTH;
			this.blocksPerLine = DEFAULT_BLOCKS_PER_LINE;
			this.fontSize = DEFAULT_FONT_SIZE;
			this.neb95beb95Background = Color.YELLOW;
			this.neb95beb9095Background = Color.RED;
			this.neb9095beb95Background = Color.BLUE;
			this.neb9095beb9095Background = Color.GREEN;
			this.neb95beb95Foreground = Color.BLACK;
			this.neb95beb9095Foreground = Color.WHITE;
			this.neb9095beb95Foreground = Color.WHITE;
			this.neb9095beb9095Foreground = Color.BLACK;
			this.showIndexes = false;
			this.showScores = false;
			
			this.setChanged();
			this.notifyObservers();
		}
		
		public int getLabelLength() {
			return labelLength;
		}
		public void setLabelLength(int labelLength) {
			if (labelLength != this.labelLength) {
				this.labelLength = (labelLength <= 0)?DEFAULT_LABEL_LENGTH:labelLength;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public int getLabelTab() {
			return labelTab;
		}
		public void setLabelTab(int labelTab) {
			if (this.labelTab != labelTab) {
				this.labelTab = (labelTab <= 0)?DEFAULT_LABEL_TAB:labelTab;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public int getBlockLength() {
			return blockLength;
		}
		public void setBlockLength(int blockLength) {
			if (this.blockLength != blockLength) {
				this.blockLength = (blockLength <= 0)?DEFAULT_BLOCK_LENGTH:blockLength;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public int getBlocksPerLine() {
			return blocksPerLine;
		}
		public void setBlocksPerLine(int blocksPerLine) {
			if (this.blocksPerLine != blocksPerLine) {
				this.blocksPerLine = (blocksPerLine <= 0)?DEFAULT_BLOCKS_PER_LINE:blocksPerLine;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public int getFontSize() {
			return fontSize;
		}
		public void setFontSize(int fontSize) {
			if (this.fontSize != fontSize) {
				this.fontSize = (fontSize <= 0)?DEFAULT_FONT_SIZE:fontSize;
				this.setChanged();
				this.notifyObservers();
			}
		}
		
		public final static String toHtmlColor(Color color) {
			return Integer.toHexString(color.getRGB()).replaceFirst("ff", "#");
		}
		
		public Color getNeb95beb95Background() {
			return neb95beb95Background;
		}
		public void setNeb95beb95Background(Color neb95beb95) {
			if (this.neb95beb95Background != neb95beb95) {
				this.neb95beb95Background = neb95beb95;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public Color getNeb95beb9095Background() {
			return neb95beb9095Background;
		}
		public void setNeb95beb9095Background(Color neb95beb9095) {
			if (this.neb95beb9095Background != neb95beb9095) {
				this.neb95beb9095Background = neb95beb9095;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public Color getNeb9095beb95Background() {
			return neb9095beb95Background;
		}
		public void setNeb9095beb95Background(Color neb9095beb95) {
			if (this.neb9095beb95Background != neb9095beb95) {
				this.neb9095beb95Background = neb9095beb95;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public Color getNeb9095beb9095Background() {
			return neb9095beb9095Background;
		}
		public void setNeb9095beb9095Background(Color neb9095beb9095) {
			if (this.neb9095beb9095Background != neb9095beb9095) {
				this.neb9095beb9095Background = neb9095beb9095;
				this.setChanged();
				this.notifyObservers();
			}
		}

		public Color getNeb95beb95Foreground() {
			return neb95beb95Foreground;
		}
		public void setNeb95beb95Foreground(Color neb95beb95) {
			if (this.neb95beb95Foreground != neb95beb95) {
				this.neb95beb95Foreground = neb95beb95;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public Color getNeb95beb9095Foreground() {
			return neb95beb9095Foreground;
		}
		public void setNeb95beb9095Foreground(Color neb95beb9095) {
			if (this.neb95beb9095Foreground != neb95beb9095) {
				this.neb95beb9095Foreground = neb95beb9095;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public Color getNeb9095beb95Foreground() {
			return neb9095beb95Foreground;
		}
		public void setNeb9095beb95Foreground(Color neb9095beb95) {
			if (this.neb9095beb95Foreground != neb9095beb95) {
				this.neb9095beb95Foreground = neb9095beb95;
				this.setChanged();
				this.notifyObservers();
			}
		}
		public Color getNeb9095beb9095Foreground() {
			return neb9095beb9095Foreground;
		}
		public void setNeb9095beb9095Foreground(Color neb9095beb9095) {
			if (this.neb9095beb9095Foreground != neb9095beb9095) {
				this.neb9095beb9095Foreground = neb9095beb9095;
				this.setChanged();
				this.notifyObservers();
			}
		}
		
		public boolean isShowIndexes() {
			return showIndexes;
		}

		public void setShowIndexes(boolean showIndexes) {
			if (this.showIndexes != showIndexes) {
				this.showIndexes = showIndexes;
				this.setChanged();
				this.notifyObservers();
			}
		}
		
		public boolean isShowScores() {
			return showScores;
		}

		public void setShowScores(boolean showScores) {
			if (this.showScores != showScores) {
				this.showScores = showScores;
				this.setChanged();
				this.notifyObservers();
			}
		}

		public String getRules() {
			final StringBuilder sb = new StringBuilder();
			
			sb.append(".indexes { color: #707070; }\n");
			sb.append(".scores { color: #707070; }\n");
			
			sb.append("pre {\n")
				//.append("\tfont-family: Courier New, Courier, monospace;\n")
				.append("\tfont-family: monospace;\n")
				.append("\tfont-size: ").append(this.getFontSize()).append("pt;\n")
			.append("}\n\n");
			
			sb.append(".neb95_beb95 {\n")
				.append("\tbackground-color: ").append(toHtmlColor(this.getNeb95beb95Background())).append(";\n")
				.append("\tcolor: ").append(toHtmlColor(this.getNeb95beb95Foreground())).append(";\n")
			.append("}\n\n");
			
			sb.append(".neb95_beb9095 {\n")
				.append("\tbackground-color: ").append(toHtmlColor(this.getNeb95beb9095Background())).append(";\n")
				.append("\tcolor: ").append(toHtmlColor(this.getNeb95beb9095Foreground())).append(";\n")
			.append("}\n\n");
			
			sb.append(".neb9095_beb95 {\n")
				.append("\tbackground-color: ").append(toHtmlColor(this.getNeb9095beb95Background())).append(";\n")
				.append("\tcolor: ").append(toHtmlColor(this.getNeb9095beb95Foreground())).append(";\n")
			.append("}\n\n");
			
			sb.append(".neb9095_beb9095 {\n")
				.append("\tbackground-color: ").append(toHtmlColor(this.getNeb9095beb9095Background())).append(";\n")
				.append("\tcolor: ").append(toHtmlColor(this.getNeb9095beb9095Foreground())).append(";\n")
			.append("}\n\n");
			
			return sb.toString();
		}
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final Map<String, String> sequences = new TreeMap<String, String>();
				sequences.put("SEQUENCE1", "AGSTTREGGKMMNTTACAGSTTRERGKLMN--TTACCAGSTTREGGRLMNTTAAGSTTREGGKMMNTTACAGSTTREGGKLMNTTACAGSTTREGGRLMNTTA");
				sequences.put("SEQUENCE2", "AGSTTREGGKMMNTTACAGSTTRERGKLMN--TTACCAGSTTREGGRLMNTTAAGSTTREGGKMMNTTACAGSTTREGGKLMNTTACAGSTTREGGRLMNTTA");
				sequences.put("SEQUENCE3", "AGSTTREGGKMMNTTACAGSTTRERGKLMN--TTACCAGSTTREGGRLMNTTAAGSTTREGGKMMNTTACAGSTTREGGKLMNTTACAGSTTREGGRLMNTTA");
				sequences.put("SEQUENCE4", "AGSTTREGGKMMNTTACAGSTTRERGKLMN--TTACCAGSTTREGGRLMNTTAAGSTTREGGKMMNTTACAGSTTREGGKLMNTTACAGSTTREGGRLMNTTA");
				sequences.put("SEQUENCE5", "AGSTTREGGKMMNTTACAGSTTRERGKLMN--TTACCAGSTTREGGRLMNTTAAGSTTREGGKMMNTTACAGSTTREGGKLMNTTACAGSTTREGGRLMNTTA");
				sequences.put("SEQUENCE6", "AGSTTREGGKMMNTTACAGSTTRERGKLMNCCTTA-CAGSTTREGGRLMNTTAAGSTTREGGKMMNTTACAGSTTREGGKLMNTTACAGSTTREGGRLMNTTA");
				
				final Map<Integer, Confidence> model = new TreeMap<Integer, AlignmentConfidences.Confidence>();
				model.put(4, new Confidence(0.99d, 0.99d));
				model.put(14, new Confidence(0.99d, 0.99d));
				model.put(8, new Confidence(0.99d, 0.92d));
				model.put(12, new Confidence(0.92d, 0.99d));
				model.put(16, new Confidence(0.92d, 0.92d));
				
				final Map<Integer, Confidence> model2 = new TreeMap<Integer, AlignmentConfidences.Confidence>();
				model2.put(7, new Confidence(0.99d, 0.99d));
				model2.put(9, new Confidence(0.99d, 0.99d));
				model2.put(22, new Confidence(0.99d, 0.92d));
				model2.put(12, new Confidence(0.92d, 0.99d));
				model2.put(43, new Confidence(0.92d, 0.92d));
				
				final AlignmentConfidences alignmentConfidences = new AlignmentConfidences(sequences);
				alignmentConfidences.addModel("Model 2", model);
				alignmentConfidences.addModel("Model 8", model2);
				
				final JFrame frame = new JFrame("Align Viewer");
				frame.setContentPane(new AlignmentTextViewer(alignmentConfidences/*, "Model 2"*/));
				
				frame.setSize(800, 600);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
	}
}
