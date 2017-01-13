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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import org.forester.io.parsers.util.PhylogenyParserException;

import es.uvigo.ei.sing.adops.Utils;
import es.uvigo.ei.sing.adops.datatypes.AlignmentConfidences;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.datatypes.ExperimentOutput;
import es.uvigo.ei.sing.adops.datatypes.ProjectExperiment;
import es.uvigo.ei.sing.alter.parser.ParseException;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.workbench.Workbench;

public class ExperimentView extends JPanel implements Observer {
	private static final String PSS_TAB = "PSS";
	private static final String SUMMARY_TAB = "Summary";
	private static final String CODEML_SUMMARY_TAB = "Codeml Summary";
	private static final String CODEML_OUTPUT_TAB = "Codeml Output";
	private static final String PSRF_TAB = "PSRF";
	private static final String TREE_VIEW_TAB = "Tree View";
	private static final String TREE_TAB = "Tree";
	private static final String ALN_FILE_TAB = "ALN File";
	private static final String EXECUTION_LOG_TAB = "Execution Log";
	private static final String ALIGNED_AMIN_TAB = "Aligned Amin.";
	private static final String ALIGNED_NUCL_TAB = "Aligned Nucl.";

	private static final long serialVersionUID = 1L;
	
	private static final String OPERATION_RUN_EXPERIMENT = "es.uvigo.ei.sing.adops.operations.runexperimentbysteps";
	
	private final JTabbedPane tabResults;
	private final ProjectExperiment experiment;
	
//	private boolean liveLog;

	private final TextFileViewer notesViewer;
	private JPanel logPanel;

	private TextFileViewer alignedNuclFileView;
	private TextFileViewer alignedAminoFileView;
	private TextFileViewer alignedProtAlnFileView;
	private TextFileViewer psrfView;
	private TextFileViewer treeView;
	private TreeView mbTreeView;
	private TextFileViewer codeMLOutputView;
	private TextFileViewer codeMLSummaryView;
	private TextFileViewer outputView;

	private AlignmentTextViewer alignmentTextViewer;

//	private GenomicViewer genomicViewer;
	
	public ExperimentView(final ProjectExperiment experiment) {
		super(new BorderLayout());
		
		this.experiment = experiment;
		this.tabResults = new JTabbedPane(JTabbedPane.BOTTOM);
		this.add(this.tabResults, BorderLayout.CENTER);
		
		
		this.notesViewer = new TextFileViewer(this.experiment.getNotesFile());

		this.notesViewer.setEditable(true);
		this.tabResults.addTab("Notes", this.notesViewer);

//		if (experiment.hasResult() && experiment.getResult().isComplete()) {
//			this.showResult(experiment.getResult());
//		}
		if (experiment.hasResult()) {
			this.showResult(experiment.getResult());
		}
		
		experiment.addObserver(this);
	}
	
	private synchronized void clearTabs() {
		if (this.logPanel != null) {
			this.tabResults.remove(this.logPanel);
			this.logPanel = null;
		}
		if (this.alignedNuclFileView != null) {
			this.tabResults.remove(this.alignedNuclFileView);
			this.alignedNuclFileView = null;
		}
		if (this.alignedAminoFileView != null) {
			this.tabResults.remove(this.alignedAminoFileView);
			this.alignedAminoFileView = null;
		}
		if (this.alignedProtAlnFileView != null) {
			this.tabResults.remove(this.alignedProtAlnFileView);
			this.alignedProtAlnFileView = null;
		}
		if (this.psrfView != null) {
			this.tabResults.remove(this.psrfView);
			this.psrfView = null;
		}
		if (this.treeView != null) {
			this.tabResults.remove(this.treeView);
			this.treeView = null;
		}
		if (this.mbTreeView != null) {
			this.tabResults.remove(this.mbTreeView);
			this.mbTreeView = null;
		}
		if (this.codeMLOutputView != null) {
			this.tabResults.remove(this.codeMLOutputView);
			this.codeMLOutputView = null;
		}
		if (this.codeMLSummaryView != null) {
			this.tabResults.remove(this.codeMLSummaryView);
			this.codeMLSummaryView = null;
		}
		if (this.outputView != null) {
			this.tabResults.remove(this.outputView);
			this.outputView = null;
		}
		if (this.alignmentTextViewer != null) {
			this.tabResults.remove(this.alignmentTextViewer);
			this.alignmentTextViewer = null;
		}
	}
	
	public void launchExecution(boolean showLog) throws IllegalStateException {
		if (this.experiment.hasResult()) {
			throw new IllegalStateException("Experiment already have results");
		} else {
			this.experiment.clear(); // Tabs will be cleared in the Observer.update();
//			this.clearTabs();
			
			final PrintStream outputStream;
			
			if (showLog) {
				this.logPanel = new LogPanel(this.experiment);
				this.tabResults.addTab(ExperimentView.EXECUTION_LOG_TAB, this.logPanel);
				this.tabResults.setSelectedComponent(this.logPanel);
				
//				this.liveLog = true;
				
				outputStream = new PrintStream(((LogPanel) this.logPanel).getOutputStream());
			} else {  // NO_OPTION
//				this.liveLog = false;
				
				outputStream = null;
			}
			
			Core.getInstance().getClipboard().putItem(outputStream, "Output Stream");
			Workbench.getInstance().executeOperation(
				ExperimentView.OPERATION_RUN_EXPERIMENT, 
				null, 
				Arrays.asList(experiment, false, outputStream)
			);
		}
	}

	@Override
	public void update(Observable observable, Object param) {
		if (param instanceof ExperimentOutput) {
			final ExperimentOutput output = (ExperimentOutput) param;
			
			if (output.isDeleted()) {
				Utils.safeGUIRun(new Runnable() {
					@Override
					public void run() {
						// All tabs except "Notes" are removed
//						while (ExperimentView.this.tabResults.getTabCount() > 1) {
//							ExperimentView.this.tabResults.removeTabAt(1);
//						}
						ExperimentView.this.clearTabs();
					}
				});
			} else {
				if (output.isFinished()) {
					this.experiment.storeAllProperties();
					output.deleteObserver(this);
					
					Utils.safeGUIRun(new Runnable() {
						@Override
						public void run() {
							ExperimentView.this.showResult(output);
						}
					});
				} else {
					Utils.safeGUIRun(new Runnable() {
						@Override
						public void run() {
							ExperimentView.this.showResult(output);
						}
					});
				}
			}
		} else if (observable instanceof Experiment) {
			final Experiment exp = (Experiment) observable;
			if (exp.isClean() && !exp.isRunning()) {
				this.clearTabs();
//				this.tabResults.removeAll();
//				this.mbTreeView = null;
//				this.logPanel = null;
//				this.alignedNuclFileView = null;
//				this.alignedAminoFileView = null;
//				this.alignedProtAlnFileView = null;
////				this.genomicViewer = null;
//				
//				this.tabResults.addTab("Notes", this.notesViewer);
			}
		}
	}
	
	private synchronized void showResult(ExperimentOutput output) {
		final File logFile = output.getLogFile();
		final File alignedNucleotides = output.getRenamedAlignedFastaFile();
		final File alignedAminoacids = output.getRenamedAlignedProteinFastaFile();
		final File alignedProtAlnFile = output.getRenamedAlignedProteinAlnFile();
		final File treeFile = output.getRenamedTreeFile();
		final File psrfFile = output.getPsrfFile();
		final File codeMLOutputFile = output.getCodeMLOutputFile();
		final File codeMLSummaryFile = output.getCodeMLSummaryFile();
		final File outputFile = output.getSummaryFile();
		
		if (this.logPanel == null && logFile.exists() && 
			!this.experiment.isRunning() //&&	this.experiment.hasResult() && this.experiment.getResult().isFinished()
		) {
//			this.logPanel = new LogPanel(this.experiment);
			this.logPanel = new TextFileViewer(logFile);
			this.tabResults.addTab(ExperimentView.EXECUTION_LOG_TAB, this.logPanel);
		}
		
		if (this.alignedProtAlnFileView == null && alignedProtAlnFile.exists()) {
			this.alignedProtAlnFileView = new TextFileViewer(alignedProtAlnFile);
			this.tabResults.addTab(ExperimentView.ALN_FILE_TAB, this.alignedProtAlnFileView);
		}
		
		if (this.alignedNuclFileView == null && alignedNucleotides.exists()) {
				this.alignedNuclFileView = new TextFileViewer(alignedNucleotides);
				this.tabResults.addTab(ExperimentView.ALIGNED_NUCL_TAB, alignedNuclFileView);
//				this.tabResults.setSelectedComponent(this.alignedNuclFileView);
				
		}
		
		if (this.alignedAminoFileView == null && alignedAminoacids.exists()) {
			this.alignedAminoFileView = new TextFileViewer(alignedAminoacids);
			this.tabResults.addTab(ExperimentView.ALIGNED_AMIN_TAB, alignedAminoFileView);
//			this.tabResults.setSelectedComponent(this.alignedAminoFileView);
		}
//			if (this.genomicViewer == null) {
//				try {
//					this.genomicViewer = new GenomicViewer(new Genome(new FastaGenomeIndex(alignedNucleotides), "ADOPS"));
//					this.genomicViewer.addTrackFile(this.createGPFile(alignedNucleotides));
////					genomicViewer.repaintGenomicViewer();
//					this.tabResults.addTab("Aligned Viewer", this.genomicViewer);
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
		if (this.treeView == null && treeFile.exists()) {
			this.treeView = new TextFileViewer(treeFile);
			this.tabResults.addTab(ExperimentView.TREE_TAB, treeView);
			try {
				this.mbTreeView = new TreeView(treeFile);
				this.tabResults.addTab(ExperimentView.TREE_VIEW_TAB, this.mbTreeView);
//				this.tabResults.setSelectedComponent(this.mbTreeView);
			} catch (PhylogenyParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (this.psrfView == null && psrfFile.exists()) {
			this.psrfView = new TextFileViewer(psrfFile);
			this.tabResults.addTab(ExperimentView.PSRF_TAB, this.psrfView);
		}
		
		if (this.codeMLOutputView == null && codeMLOutputFile.exists()) {
			this.codeMLOutputView = new TextFileViewer(codeMLOutputFile);
			this.tabResults.addTab(ExperimentView.CODEML_OUTPUT_TAB, this.codeMLOutputView);
		}
		
		if (this.codeMLSummaryView == null && codeMLSummaryFile.exists()) {
			this.codeMLSummaryView = new TextFileViewer(codeMLSummaryFile);
			this.tabResults.addTab(ExperimentView.CODEML_SUMMARY_TAB, this.codeMLSummaryView);
		}
		
		if (this.outputView == null && outputFile.exists()) {
			this.outputView = new TextFileViewer(outputFile);
			this.tabResults.addTab(ExperimentView.SUMMARY_TAB, this.outputView);
		}
		
		try {
			if (this.alignmentTextViewer == null) {
				final AlignmentConfidences confidences = output.loadConfidences();
				if (confidences != null && this.alignmentTextViewer == null /*&& !confidences.getModels().isEmpty()*/) {
					this.alignmentTextViewer = new AlignmentTextViewer(confidences, output.loadScores());
					this.tabResults.addTab(ExperimentView.PSS_TAB, this.alignmentTextViewer);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.tabResults.setSelectedIndex(this.tabResults.getTabCount()-1);
//		
//		
//		if (output.isComplete()) {
//			if (!this.liveLog) {
//				this.tabResults.addTab("Execution Log", new TextFileViewer(logFile));
//			}
//			
//			this.tabResults.addTab("PSRF", new TextFileViewer(psrfFile));
//			this.tabResults.addTab("Tree", new TextFileViewer(treeFile));
//			this.tabResults.addTab("Codeml Output", new TextFileViewer(codeMLOutputFile));
//			this.tabResults.addTab("Codeml Summary", new TextFileViewer(codeMLSummaryFile));
//			this.tabResults.addTab("Summary", new TextFileViewer(outputFile));
//			try {
//				final AlignmentConfidences confidences = output.loadConfidences();
//				this.tabResults.addTab("PSS", new AlignmentTextViewer(confidences));
//			} catch (ParseException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}
	
	private final static class TextAreaOutputStream extends OutputStream {
		private final JTextArea textArea;
		private final BufferedOutputStream bos;
		
		private StringBuffer buffer;
		
		public TextAreaOutputStream(JTextArea textArea, OutputStream os) {
			this.textArea = textArea;
			this.buffer = new StringBuffer();
			
			if (os != null)
				this.bos = new BufferedOutputStream(os);
			else this.bos = null;
		}

		@Override
		public void write(final int b) throws IOException {
			final char c = (char) b;
			
			if (this.bos != null)
				this.bos.write(b);
			
			this.buffer.append(c);
			if (c == '\n') {
				final String text = this.buffer.toString();
				this.buffer = new StringBuffer();
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						TextAreaOutputStream.this.textArea.append(text);
					}
				});
			}
			
		}

		@Override
		public void close() throws IOException {
			this.bos.close();
		}
	}
	
	private final static class LogPanel extends JPanel implements Observer {
		private static final long serialVersionUID = 1L;
		
		private final TextAreaOutputStream out;
		private final JPanel topPanel;
		
		public LogPanel(ProjectExperiment experiment) {
			this(experiment, null);
		}
		
		public LogPanel(ProjectExperiment experiment, OutputStream out) {
			super(new BorderLayout());
			
			this.topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			final JTextArea textArea = new JTextArea();
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(false);
			textArea.setEditable(false);
			textArea.setFont(new Font(
				Font.MONOSPACED,
				Font.PLAIN,
				textArea.getFont().getSize()
			));
			
			final DefaultCaret caret = (DefaultCaret) textArea.getCaret();
			caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
			
			final JCheckBox chkAutoScroll = new JCheckBox("Auto scroll", true);
			chkAutoScroll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					if (chkAutoScroll.isSelected()) {
						textArea.setCaretPosition(textArea.getDocument().getLength());
						caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
					} else {
						caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
					}
				}
			});
			
			this.topPanel.add(chkAutoScroll);
			
			this.add(this.topPanel, BorderLayout.NORTH);
			this.add(new JScrollPane(textArea), BorderLayout.CENTER);
			
			this.out = new TextAreaOutputStream(textArea, out);
			experiment.addObserver(this);
		}
		
		public TextAreaOutputStream getOutputStream() {
			return this.out;
		}

		@Override
		public void update(Observable o, Object arg) {
			if (o instanceof ProjectExperiment) {
				final ProjectExperiment experiment = (ProjectExperiment) o;
				
				if (experiment.hasResult() && experiment.getResult().isFinished()) {
					Utils.safeGUIRun(new Runnable() {
						@Override
						public void run() {
							LogPanel.this.remove(LogPanel.this.topPanel);
							experiment.deleteObserver(LogPanel.this);
						}
					});
				}
			}
		}
	}
	
//	private File createGPFile(File alignmentFile) throws ParseException, IOException {
//		final DefaultFactory factory = new DefaultFactory();
////		final Reader reader = factory.getReader("linux", "clustal", "fasta", false, "logger");
//		final Reader reader = factory.getReader(null, null, null, true, "logger");
//		
//		final MSA msa = reader.read(FileUtils.readFileToString(alignmentFile));
//		final Sequence[] sequences = new Sequence[msa.getSeqs().size()];
//		int i = 0;
//		for (Object seq : msa.getSeqs()) {
//			sequences[i++] = (Sequence) seq;
//		}
//		
//		final File gpFile = File.createTempFile("example_", ".gp");
//		gpFile.deleteOnExit();
//		PrintWriter pw = null;
//		
//		try {
//			pw = new PrintWriter(gpFile);
//			
////			for (int j = 0; j < sequences.length; j++) {
////				final Sequence currentSeq = sequences[j];
////				
//			Random random = new Random(this.experiment.hashCode());
//			for (int k = 0; k < sequences.length; k++) {
//				final Sequence currentSeq = sequences[k];
//				
//				for (int j = 0; j < currentSeq.getData().length(); j++) {
//					if (random.nextDouble() > 0.9d) {
//						pw.print(currentSeq.getId());
//						pw.print('\t');
//						pw.print(j);
//						pw.print('\t');
//						pw.println(currentSeq.getData().charAt(j));
//					}
//				}
//			}
////			}
//		} finally {
//			if (pw != null) 
//				pw.close();
//		}
//		
//		
//		return gpFile;
//	}
}
