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

import static es.uvigo.ei.sing.adops.util.FastaUtils.loadAndCheckSequences;
import static es.uvigo.ei.sing.adops.util.FastaUtils.writeSequences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.sing.adops.configuration.Configuration;
import es.uvigo.ei.sing.adops.datatypes.fasta.Fasta;
import es.uvigo.ei.sing.adops.datatypes.fasta.FastaSequence;
import es.uvigo.ei.sing.adops.util.FastaUtils;

public class ExportDialog extends JDialog {
	private static final String TAB_LABEL_POSITIVE_NEGATIVE_EXPORT = "Positive/Negative";
	private static final String TAB_LABEL_SINGLE_EXPORT = "Single";
	private static final long serialVersionUID = 1L;
	
	private JButton btnExport;

	private final File file;
	private JTextField txtExportDirectory;
	private JTextField txtExportName;
	private SingleListSelectionPanel<FastaSequenceWrapper> panelSingleSequence;
	private DoubleListSelectionPanel<FastaSequenceWrapper> panelPositiveNegative;
	private JTabbedPane panelSequenceSelection;

	public ExportDialog(File file) throws IOException {
		super(Workbench.getInstance().getMainFrame(), "Export File", true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		if (!FastaUtils.isFasta(file)) {
			throw new IllegalArgumentException(file.getAbsolutePath() + " is not a valid Fasta file");
		}

		this.file = file;

		final JPanel panelMain = new JPanel(new BorderLayout(0, 10));

		panelMain.add(this.createSequenceSelectionPanel(), BorderLayout.CENTER);
		panelMain.add(this.createPathPanel(), BorderLayout.SOUTH);

		final JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(panelMain, BorderLayout.CENTER);
		contentPane.add(this.createButtonsPanel(), BorderLayout.SOUTH);

		this.setContentPane(contentPane);

		this.pack();
		this.setLocationRelativeTo(this.getOwner());
	}

	private boolean isConfigurationComplete() {
		if (
			this.getExportDirectoryPath().isEmpty() ||
				this.getExportName().isEmpty()
		) {
			return false;
		} else {
			if (this.isSingleExport()) {
				return !this.getSelectedValuesId().isEmpty();
			} else {
				return !this.getPositiveValuesId().isEmpty() &&
					!this.getNegativeValuesId().isEmpty();
			}
		}
	}

	private void updateButtonExport() {
		this.btnExport.setEnabled(this.isConfigurationComplete());
	}

	private void doExport() {
		if (this.isConfigurationComplete()) {
			final File exportDirectory = this.getExportDirectory();

			if (!exportDirectory.isDirectory() && !exportDirectory.mkdirs()) {
				JOptionPane.showMessageDialog(
					this,
					"Directory " + exportDirectory.getAbsolutePath() + " could not be created.",
					"Export Error",
					JOptionPane.ERROR_MESSAGE
				);
			} else {
				String exportName = this.getExportName();

				try {
					final String suffix = ".fasta";

					if (this.isSingleExport()) {
						if (!exportName.toLowerCase().endsWith(suffix)) {
							exportName += suffix;
						}

						final File exportFile = new File(exportDirectory, exportName);
						final Fasta fasta = loadAndCheckSequences(this.file, this.getSelectedValuesId());
						
						writeSequences(exportFile, fasta);
					} else {
						if (exportName.toLowerCase().endsWith(suffix)) {
							exportName = exportName.substring(0, exportName.length() - suffix.length());
						}

						final File positiveExportFile = new File(exportDirectory, exportName + ".pos" + suffix);
						final File negativeExportFile = new File(exportDirectory, exportName + ".neg" + suffix);

						final Fasta positiveFasta = loadAndCheckSequences(
							this.file,
							this.getPositiveValuesId()
						);
						final Fasta negativeFasta = loadAndCheckSequences(
							this.file,
							this.getNegativeValuesId()
						);

						writeSequences(positiveExportFile, positiveFasta);
						writeSequences(negativeExportFile, negativeFasta);
					}

					JOptionPane.showMessageDialog(
						this,
						"Files correctly exported",
						"Export Completed",
						JOptionPane.INFORMATION_MESSAGE
					);
					this.setVisible(false);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(
						this,
						"Error exporting file: " + e.getMessage(),
						"Export Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		} else {
			throw new IllegalStateException("Incomplete configuration");
		}
	}

	private boolean isSingleExport() {
		final Component selectedTabComponent = this.panelSequenceSelection.getSelectedComponent();

		return selectedTabComponent.equals(this.panelSingleSequence);
	}

	private List<String> getSelectedValuesId() {
		final List<String> values = new ArrayList<>();
		
		for (FastaSequenceWrapper wrapper : this.panelSingleSequence.getSelectedValues()) {
			values.add(wrapper.getId());
		}

		return values;
	}

	private List<String> getPositiveValuesId() {
		final List<String> values = new ArrayList<>();
		
		for (FastaSequenceWrapper wrapper : this.panelPositiveNegative.getPositiveValues()) {
			values.add(wrapper.getId());
		}

		return values;
	}

	private List<String> getNegativeValuesId() {
		final List<String> values = new ArrayList<>();
		
		for (FastaSequenceWrapper wrapper : this.panelPositiveNegative.getNegativeValues()) {
			values.add(wrapper.getId());
		}

		return values;
	}

	private File getExportDirectory() {
		return new File(this.getExportDirectoryPath());
	}

	private String getExportDirectoryPath() {
		return this.txtExportDirectory.getText();
	}

	private String getExportName() {
		return this.txtExportName.getText();
	}


	private JTabbedPane createSequenceSelectionPanel() throws IOException {
		this.panelSequenceSelection = new JTabbedPane();

		this.panelSingleSequence = this.createSinglePanel();
		this.panelPositiveNegative = this.createPosNegPanel();

		this.panelSequenceSelection.addTab(
			ExportDialog.TAB_LABEL_SINGLE_EXPORT,
			this.panelSingleSequence
		);
		this.panelSequenceSelection.addTab(
			ExportDialog.TAB_LABEL_POSITIVE_NEGATIVE_EXPORT,
			this.panelPositiveNegative
		);

		this.panelSequenceSelection.addChangeListener(
			new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ExportDialog.this.updateButtonExport();
				}
			}
		);

		this.panelSingleSequence.addChangeListener(
			new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ExportDialog.this.updateButtonExport();
				}
			}
		);

		this.panelPositiveNegative.addChangeListener(
			new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ExportDialog.this.updateButtonExport();
				}
			}
		);

		return this.panelSequenceSelection;
	}

	private SingleListSelectionPanel<FastaSequenceWrapper> createSinglePanel() {
		final List<FastaSequenceWrapper> fastaSeqWrappedList = loadAndWrapSequences();

		return new SingleListSelectionPanel<>(
			fastaSeqWrappedList,
			"Selected Sequences",
			"Unselected Sequences"
		);
	}

	private DoubleListSelectionPanel<FastaSequenceWrapper> createPosNegPanel() {
		final List<FastaSequenceWrapper> fastaSeqWrappedList = loadAndWrapSequences();

		return new DoubleListSelectionPanel<>(
			fastaSeqWrappedList,
			"Positive Sequences",
			"Unselected Sequences",
			"Negative Sequences"
		);
	}

	protected List<FastaSequenceWrapper> loadAndWrapSequences() {
		final Fasta fastaSeqList = loadAndCheckSequences(this.file);

		final List<FastaSequenceWrapper> fastaSeqWrappedList = new ArrayList<>(fastaSeqList.size());
		for (FastaSequence sequence : fastaSeqList.getSequences()) {
			fastaSeqWrappedList.add(new FastaSequenceWrapper(sequence));
		}
		return fastaSeqWrappedList;
	}

	private JPanel createPathPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		final String exportDefaultPath = Configuration.getSystemConfiguration().getExportDefaultPath();

		final JLabel lblPath = new JLabel("Export to ");
		this.txtExportDirectory = new JTextField(exportDefaultPath);
		this.txtExportDirectory.setToolTipText(exportDefaultPath);
		this.txtExportDirectory.setEditable(false);

		final AbstractAction selectExportDirectoryAction = new AbstractAction("Select...") {
			private static final long serialVersionUID = 1L;

			private File file = new File(exportDefaultPath);

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fileChooser = new JFileChooser(this.file);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setFileHidingEnabled(true);
				fileChooser.setMultiSelectionEnabled(false);

				final JFrame parentFrame = Workbench.getInstance().getMainFrame();
				if (fileChooser.showOpenDialog(parentFrame) != JFileChooser.APPROVE_OPTION) {
					this.file = fileChooser.getSelectedFile();

					txtExportDirectory.setText(this.file.getAbsolutePath());
					txtExportDirectory.setToolTipText(this.file.getAbsolutePath());

					ExportDialog.this.updateButtonExport();
				}
			}
		};

		final JButton btnPath = new JButton(selectExportDirectoryAction);
		this.txtExportDirectory.addActionListener(selectExportDirectoryAction);

		String name = this.file.getName();
		if (name.toLowerCase().endsWith(".fasta")) {
			name = name.substring(0, name.length() - 6);
		}

		final JLabel lblName = new JLabel(" with name ");
		this.txtExportName = new JTextField(name);
		this.txtExportName.addKeyListener(
			new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					ExportDialog.this.updateButtonExport();
				}
			}
		);

		panel.add(lblPath);
		panel.add(this.txtExportDirectory);
		panel.add(btnPath);
		panel.add(lblName);
		panel.add(this.txtExportName);

		return panel;
	}

	private JPanel createButtonsPanel() {
		final JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder());

		final JButton btnCancel = new JButton(
			new AbstractAction("Cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					ExportDialog.this.setVisible(false);
				}
			}
		);
		this.btnExport = new JButton(
			new AbstractAction("Export") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					ExportDialog.this.doExport();
				}
			}
		);
		this.btnExport.setEnabled(this.isConfigurationComplete());

		panel.add(this.btnExport);
		panel.add(btnCancel);

		return panel;
	}

	private static class FastaSequenceWrapper {
		private final FastaSequence sequence;

		public FastaSequenceWrapper(FastaSequence sequence) {
			this.sequence = sequence;
		}

		public String getId() {
			return sequence.getId();
		}
		
		@Override
		public String toString() {
			return sequence.getIdAndDescription();
		}
	}
}
