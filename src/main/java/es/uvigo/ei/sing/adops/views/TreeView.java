/*-
 * #%L
 * ADOPS
 * %%
 * Copyright (C) 2012 - 2019 David Reboiro-Jato, Miguel Reboiro-Jato, Jorge Vieira, Florentino Fdez-Riverola, Cristina P. Vieira, Nuno A. Fonseca
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.forester.archaeopteryx.Configuration;
import org.forester.archaeopteryx.MainPanel;
import org.forester.archaeopteryx.PdfExporter;
import org.forester.archaeopteryx.TreePanel;
import org.forester.archaeopteryx.Util;
import org.forester.archaeopteryx.Util.GraphicsExportType;
import org.forester.io.parsers.PhylogenyParser;
import org.forester.io.parsers.nexus.NexusPhylogeniesParser;
import org.forester.io.parsers.util.PhylogenyParserException;
import org.forester.phylogeny.Phylogeny;
import org.forester.util.ForesterUtil;

public class TreeView extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final String CONFIGURATION_FILE = TreeView.class.getResource("_aptx_configuration_file.asc").toString();
	private final MainPanel mainPanel;
	private final JFileChooser fileChooser;

	public TreeView(File mrbayesFile) throws PhylogenyParserException, IOException {
		super(new BorderLayout());

		final PhylogenyParser parser = new NexusPhylogeniesParser();
		parser.setSource(mrbayesFile);
		final Phylogeny[] phys = parser.parse();

		this.fileChooser = new JFileChooser(mrbayesFile.getParent());
		this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		this.fileChooser.setDialogTitle("Export phylogeny");

		final Configuration configuration = new Configuration(TreeView.CONFIGURATION_FILE, true, false);

		this.mainPanel = new MainPanel(configuration, null);

		for (Phylogeny phy : phys) {
			this.mainPanel.addPhylogenyInNewTab(phy, configuration, phy.getName(), null);
		}

		final JPanel panelExport = new JPanel(new FlowLayout(FlowLayout.LEFT));

		final JLabel lblExport = new JLabel("Export phylogeny as ");
		final JComboBox<GraphicsExportType> cmbFormat = new JComboBox<>(GraphicsExportType.values());
		cmbFormat.setSelectedItem(GraphicsExportType.PNG);
		final JButton btnExport = new JButton(
			new AbstractAction("Go") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					Phylogeny phylogeny = mainPanel.getCurrentTreePanel().getPhylogeny();

					exportPhylogenyAsImage(phylogeny, (GraphicsExportType) cmbFormat.getSelectedItem());
				}
			}
		);

		panelExport.add(lblExport);
		panelExport.add(cmbFormat);
		panelExport.add(btnExport);

		this.add(this.mainPanel, BorderLayout.CENTER);
		this.add(panelExport, BorderLayout.NORTH);
	}

	protected MainPanel getMainPanel() {
		return this.mainPanel;
	}

	private void exportPhylogenyAsPdf(final String file_name) {
		String pdfWritenTo = "";

		try {
			final TreePanel treePanel = this.getMainPanel().getCurrentTreePanel();
			pdfWritenTo = PdfExporter.writePhylogenyToPdf(
				file_name, treePanel,
				treePanel.getWidth(), treePanel.getHeight()
			);

			if (!ForesterUtil.isEmpty(pdfWritenTo)) {
				JOptionPane.showMessageDialog(
					this,
					"Wrote PDF to: " + pdfWritenTo,
					"Information",
					JOptionPane.INFORMATION_MESSAGE
				);
			} else {
				JOptionPane.showMessageDialog(
					this,
					"There was an unknown problem when attempting to write to PDF file: \"" + file_name + "\"",
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			}
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void exportPhylogenyAsImage(Phylogeny p, GraphicsExportType type) {
		if (p == null || p.isEmpty() || type == null)
			return;

		if (this.fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = this.fileChooser.getSelectedFile();

			if (!file.toString().toLowerCase().endsWith(type.toString())) {
				file = new File(file.toString() + "." + type);
			}
			if (file.exists()) {
				final int selection = JOptionPane.showConfirmDialog(
					this,
					file + " already exists. Overwrite?",
					"Warning",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE
				);

				if (selection != JOptionPane.OK_OPTION) {
					return;
				} else {
					try {
						file.delete();
					} catch (final Exception e) {
						JOptionPane.showMessageDialog(
							this,
							"Failed to delete: " + file,
							"Error",
							JOptionPane.WARNING_MESSAGE
						);
					}
				}
			}

			if (type.equals(GraphicsExportType.PDF)) {
				this.exportPhylogenyAsPdf(file.toString());
			} else {
				this.writePhylogenyToGraphicsFile(file.toString(), type);
			}
		}
	}

	private void writePhylogenyToGraphicsFile(final String fileName, final GraphicsExportType type) {
		final TreePanel treePanel = this.getMainPanel().getCurrentTreePanel();

		treePanel.setParametersForPainting(
			treePanel.getWidth(),
			treePanel.getHeight(),
			true
		);

		try {
			final String fileWrittenTo = Util.writePhylogenyToGraphicsFile(
				fileName,
				treePanel.getWidth(),
				treePanel.getHeight(),
				treePanel,
				this.getMainPanel().getControlPanel(),
				type,
				this.getMainPanel().getOptions()
			);

			if (fileWrittenTo != null && fileWrittenTo.length() > 0) {
				JOptionPane.showMessageDialog(
					this,
					"Wrote image to: " + fileWrittenTo,
					"Graphics Export",
					JOptionPane.INFORMATION_MESSAGE
				);
			} else {
				JOptionPane.showMessageDialog(
					this,
					"There was an unknown problem when attempting to write to an image file: \"" + fileName + "\"",
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			}
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			this.getMainPanel().repaint();
		}
	}
}
