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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.sing.adops.FastaUtils;
import es.uvigo.ei.sing.adops.configuration.Configuration;
import es.uvigo.ei.sing.alter.converter.DefaultFactory;
import es.uvigo.ei.sing.alter.converter.ProgramOptions;
import es.uvigo.ei.sing.alter.parser.ParseException;
import es.uvigo.ei.sing.alter.types.Fasta;
import es.uvigo.ei.sing.alter.types.FastaSequence;
import es.uvigo.ei.sing.alter.writer.Writer;

public class ExportDialog extends JDialog {
	private static final String TAB_LABEL_POSITIVE_NEGATIVE_EXPORT = "Positive/Negative";
	private static final String TAB_LABEL_SINGLE_EXPORT = "Single";
	private static final long serialVersionUID = 1L;
	private JComboBox<String> cmbOS;
	private JComboBox<String> cmbProgram;
	private JComboBox<String> cmbFormat;
	
	private JButton btnExport;

	private final File file;
	private JTextField txtExportDirectory;
	private JTextField txtExportName;
	private SingleListSelectionPanel<FastaSequenceWrapper> panelSingleSequence;
	private DoubleListSelectionPanel<FastaSequenceWrapper> panelPositiveNegative;
	private JTabbedPane panelSequenceSelection;
	
	public ExportDialog(File file) throws ParseException, IOException {
		super(Workbench.getInstance().getMainFrame(), "Export File", true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		if (!FastaUtils.isFasta(file, ExportDialog.class.getName())) {
			throw new IllegalArgumentException(file.getAbsolutePath() + " is not a valid Fasta file");
		}
		
		this.file = file;
		
		final JPanel panelMain = new JPanel(new BorderLayout(0, 10));

		panelMain.add(this.createExportPanel(), BorderLayout.NORTH);
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
		if (this.getExportDirectoryPath().isEmpty() || 
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
				JOptionPane.showConfirmDialog(
					this, 
					"Directory " + exportDirectory.getAbsolutePath() + " could not be created.",
					"Export Error",
					JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE
				);
			} else {
				final ALTEROutputConfiguration alterConfig = this.getALTERConfiguration();
				final Writer writer = new DefaultFactory().getWriter(
					alterConfig.getOs(), 
					alterConfig.getProgram(), 
					alterConfig.getFormat(), 
					false, false, false, false, 
					ExportDialog.class.getName()
				);
				String exportName = this.getExportName();
				
				try {
					final String suffix = "." + alterConfig.getFormat();
					
					if (this.isSingleExport()) {
						if (!exportName.toLowerCase().endsWith(suffix)) {
							exportName += suffix;
						}
						
						final File exportFile = new File(exportDirectory, exportName);
						final Fasta fasta = FastaUtils.extractSequences(
							FastaUtils.readFasta(this.file), 
							this.getSelectedValuesId()
						);
						
						FileUtils.writeStringToFile(exportFile, writer.write(fasta));
					} else {
						if (exportName.toLowerCase().endsWith(suffix)) {
							exportName = exportName.substring(0, exportName.length() - suffix.length());
						}
						
						final File positiveExportFile = new File(exportDirectory, exportName + ".pos" + suffix);
						final File negativeExportFile = new File(exportDirectory, exportName + ".neg" + suffix);
						
						final Fasta positiveFasta = FastaUtils.extractSequences(
							FastaUtils.readFasta(this.file), 
							this.getPositiveValuesId()
						);
						final Fasta negativeFasta = FastaUtils.extractSequences(
							FastaUtils.readFasta(this.file), 
							this.getNegativeValuesId()
						);
						
						FileUtils.writeStringToFile(positiveExportFile, writer.write(positiveFasta));
						FileUtils.writeStringToFile(negativeExportFile, writer.write(negativeFasta));
					}
					
					JOptionPane.showConfirmDialog(
						this, 
						"Files correctly exported",
						"Export Completed",
						JOptionPane.OK_OPTION,
						JOptionPane.INFORMATION_MESSAGE
					);
					this.setVisible(false);
				} catch (Exception e) {
					JOptionPane.showConfirmDialog(
						this, 
						"Error exporting file: " + e.getMessage(),
						"Export Error",
						JOptionPane.OK_OPTION,
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		} else {
			throw new IllegalStateException("Incomplete configuration");
		}
	}
	
	private boolean isSingleExport() {
		final Component selectedTabComponent = 
			this.panelSequenceSelection.getSelectedComponent();
		
		return selectedTabComponent.equals(this.panelSingleSequence);
	}
	
	private List<String> getSelectedValuesId() {
		final List<String> values = new ArrayList<String>();
		for (FastaSequenceWrapper wrapper : this.panelSingleSequence.getSelectedValues()) {
			values.add(wrapper.getWrapped().getId());
		}
		
		return values;
	}
	
	private List<String> getPositiveValuesId() {
		final List<String> values = new ArrayList<String>();
		for (FastaSequenceWrapper wrapper : this.panelPositiveNegative.getPositiveValues()) {
			values.add(wrapper.getWrapped().getId());
		}
		
		return values;
	}
	
	private List<String> getNegativeValuesId() {
		final List<String> values = new ArrayList<String>();
		for (FastaSequenceWrapper wrapper : this.panelPositiveNegative.getNegativeValues()) {
			values.add(wrapper.getWrapped().getId());
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
	
	private ALTEROutputConfiguration getALTERConfiguration() {
		return new ALTEROutputConfiguration(
			(String) this.cmbOS.getSelectedItem(), 
			(String) this.cmbProgram.getSelectedItem(), 
			(String) this.cmbFormat.getSelectedItem()
		);
	}
	
	private JPanel createExportPanel() {
		final JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Export options"));
		
		final GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		panel.setLayout(layout);
		
		final JLabel lblOS = new JLabel("O.S.");
		lblOS.setToolTipText("Operative system");
		
		final JLabel lblProgram = new JLabel("Program");
		lblProgram.setToolTipText("Output program");
		
		final JLabel lblFormat = new JLabel("Format");
		lblFormat.setToolTipText("Output format");
		
		this.cmbOS = new JComboBox<>(
			new Vector<>(ProgramOptions.getSO())
		);
		
		final Vector<String> outputPrograms = 
			new Vector<String>(ProgramOptions.getOutputPrograms());
		Collections.sort(outputPrograms);
		
		this.cmbProgram = new JComboBox<>(outputPrograms);
		this.cmbProgram.setSelectedIndex(0);
		
		final Vector<String> formats = new Vector<String>(
			ProgramOptions.getOutputProgramFormats((String) cmbProgram.getSelectedItem())
		);
		Collections.sort(formats);
		this.cmbFormat = new JComboBox<>(formats);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(lblOS)
				.addComponent(this.cmbOS)
			)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(lblProgram)
				.addComponent(this.cmbProgram)
			)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(lblFormat)
				.addComponent(this.cmbFormat)
			)
		);
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(lblOS)
				.addComponent(lblProgram)
				.addComponent(lblFormat)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(this.cmbOS)
				.addComponent(this.cmbProgram)
				.addComponent(this.cmbFormat)
			)
		);
		
		this.cmbProgram.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String currentProgram = (String) cmbProgram.getSelectedItem();
				final String currentFormat = (String) cmbFormat.getSelectedItem();
				
				final Vector<String> formats = new Vector<String>( 
					ProgramOptions.getOutputProgramFormats(currentProgram)
				);
				Collections.sort(formats);
				
				cmbFormat.setModel(new DefaultComboBoxModel<>(formats));
				
				if (formats.contains(currentFormat)) {
					cmbFormat.setSelectedIndex(formats.indexOf(currentFormat));
				}
			}
		});
		
		return panel;
	}
	
	private JTabbedPane createSequenceSelectionPanel() throws ParseException, IOException {
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
		
		this.panelSequenceSelection.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ExportDialog.this.updateButtonExport();
			}
		});
		
		this.panelSingleSequence.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ExportDialog.this.updateButtonExport();
			}
		});
		
		this.panelPositiveNegative.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ExportDialog.this.updateButtonExport();
			}
		});
		
		return this.panelSequenceSelection;
	}
	
	private SingleListSelectionPanel<FastaSequenceWrapper> createSinglePanel() throws ParseException, IOException {
		final List<FastaSequence> fastaSeqList = 
			FastaUtils.listFastaSequences(FastaUtils.readFasta(this.file));
		
		final List<FastaSequenceWrapper> fastaSeqWrappedList = 
			new ArrayList<FastaSequenceWrapper>(fastaSeqList.size());
		for (FastaSequence sequence : fastaSeqList) {
			fastaSeqWrappedList.add(new FastaSequenceWrapper(sequence));
		}
		
		return new SingleListSelectionPanel<FastaSequenceWrapper>(
			fastaSeqWrappedList, 
			"Selected Sequences",
			"Unselected Sequences"
		);
	}

	private DoubleListSelectionPanel<FastaSequenceWrapper> createPosNegPanel() throws ParseException, IOException {
		final List<FastaSequence> fastaSeqList = 
			FastaUtils.listFastaSequences(FastaUtils.readFasta(this.file));
		
		final List<FastaSequenceWrapper> fastaSeqWrappedList = 
			new ArrayList<FastaSequenceWrapper>(fastaSeqList.size());
		for (FastaSequence sequence : fastaSeqList) {
			fastaSeqWrappedList.add(new FastaSequenceWrapper(sequence));
		}
		
		return new DoubleListSelectionPanel<FastaSequenceWrapper>(
			fastaSeqWrappedList, 
			"Positive Sequences", 
			"Unselected Sequences",
			"Negative Sequences"
		);
	}

	private JPanel createPathPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		final String exportDefaultPath = 
			Configuration.getSystemConfiguration().getExportDefaultPath();
		
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
		this.txtExportName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				ExportDialog.this.updateButtonExport();
			}
		});
		
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
		
		final JButton btnCancel = new JButton(new AbstractAction("Cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				ExportDialog.this.setVisible(false);
			}
		});
		this.btnExport = new JButton(new AbstractAction("Export") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ExportDialog.this.doExport();
			}
		});
		this.btnExport.setEnabled(this.isConfigurationComplete());
		
		panel.add(this.btnExport);
		panel.add(btnCancel);
		
		return panel;
	}

	private static class FastaSequenceWrapper {
		private final FastaSequence wrapped;
		
		public FastaSequenceWrapper(FastaSequence sequence) {
			this.wrapped = sequence;
		}
		
		public FastaSequence getWrapped() {
			return wrapped;
		}
		
		@Override
		public String toString() {
			return wrapped.getId() + " " + wrapped.getDesc();
		}
	}
	
	private final static class ALTEROutputConfiguration {
		private final String os;
		private final String program;
		private final String format;
		
		public ALTEROutputConfiguration(String os, String program, String format) {
			this.os = os;
			this.program = program;
			this.format = format;
		}

		public String getOs() {
			return os;
		}

		public String getProgram() {
			return program;
		}

		public String getFormat() {
			return format;
		}
	}
}
