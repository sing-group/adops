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
package es.uvigo.ei.sing.adops.operations.running;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.sing.adops.datatypes.CodeMLOutput;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.datatypes.ExperimentOutput;
import es.uvigo.ei.sing.adops.datatypes.MrBayesOutput;
import es.uvigo.ei.sing.adops.datatypes.OperationOutput;
import es.uvigo.ei.sing.adops.datatypes.TCoffeeOutput;
import es.uvigo.ei.sing.adops.operations.running.codeml.CodeMLOperation;
import es.uvigo.ei.sing.adops.operations.running.mrbayes.MrBayesOperation;
import es.uvigo.ei.sing.adops.operations.running.tcoffee.TCoffeeOperation;
import es.uvigo.ei.sing.adops.util.Utils;
import es.uvigo.ei.sing.adops.util.Utils.LinesFilter;

@Operation(name = "Run Experiment")
public class ExecuteExperimentBySteps extends CallableOperation<ExperimentOutput> {
	private final static Logger LOG = Logger.getLogger(ExecuteExperimentBySteps.class);
	
	private Experiment experiment;
	private ExperimentOutput experimentOutput;

	private TCoffeeOperation tCoffeeOperation;
	private MrBayesOperation mrBayesOperation;
	private CodeMLOperation codeMLOperation;

	private PrintStream logPS;
	private File logFile;

	private StatusProgress progress = new StatusProgress();

	@Progress
	public StatusProgress getProgress() {
		return progress;
	}

	public static class StatusProgress {
		private String status = "";

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}

	@Port(name = "Experiment", direction = Direction.INPUT, order = 1)
	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;

		this.setOutputFolder(experiment.getFolder());
	}

	@Port(name = "Use Std. Output", order = 2, direction = Direction.INPUT, defaultValue = "true")
	public void setUseStdOutput(boolean useStdOutput) {
		if (useStdOutput)
			this.addPrintStream(System.out);
		else
			this.removePrintStream(System.out);
	}

	@Port(name = "Print Stream", direction = Direction.INPUT, order = 3, allowNull = true)
	public void setOutputStream(PrintStream out) {
		this.addPrintStream(out);
	}

	@Port(direction = Direction.OUTPUT, order = 1000)
	public ExperimentOutput createExperimentOutput() throws OperationException {
		try {
			this.progress.setStatus("Starting");

			this.experiment.setRunning(true);

			this.checkInterrupted();

			this.progress.setStatus("Creating output.log file");
			this.logFile = this.newOutputFile("output.log");
			this.addPrintStream(new PrintStream(logFile));

			this.checkInterrupted();

			this.progress.setStatus("Generating input files");
			this.experiment.generateInputFiles();

			this.checkInterrupted();

			return this.experimentOutput = new ExperimentOutput(this.experiment);
		} catch (IOException ioe) {
			this.clearWhenException();
			throw new OperationException("Error initializing execution", ioe);
		} catch (InterruptedException ie) {
			return null;
		} catch (Exception e) {
			this.clearWhenException();
			throw new OperationException("Unexpected error initializing execution", e);
		}
	}

	@Port(direction = Direction.OUTPUT, order = 2000)
	public TCoffeeOutput runTCoffee() throws OperationException {
		try {
			this.progress.setStatus("Running T-Coffee");

			this.checkInterrupted();

			this.tCoffeeOperation = new TCoffeeOperation();
			this.tCoffeeOperation.configure(this.experiment);
			this.configureOutputStreams(this.tCoffeeOperation);

			this.checkInterrupted();

			final TCoffeeOutput output = this.tCoffeeOperation.call();

			this.checkInterrupted();

			this.replaceSequenceNames(output.getAlignmentFile(), this.experimentOutput.getRenamedAlignedFastaFile());
			this.replaceSequenceNames(output.getProteinAlignmentFile(), this.experimentOutput.getRenamedAlignedProteinFastaFile());
			this.replaceSequenceNames(output.getFinalScoreFile(), this.experimentOutput.getRenamedScoreAsciiFile());
			this.replaceSequenceNamesAln(output.getFinalUsedAlnFile(), this.experimentOutput.getRenamedAlignedProteinAlnFile());
			this.replaceOGaps(this.experimentOutput.getRenamedAlignedFastaFile());
			this.replaceOGaps(this.experimentOutput.getRenamedAlignedProteinFastaFile());
			this.replaceOGaps(
				this.experimentOutput.getRenamedScoreAsciiFile(), new Utils.LinesFilter() {
					@Override
					public boolean accept(int index, String line) {
						return line.matches("^C[0-9]+.*");
					}
				}
			);

			this.checkInterrupted();

			this.experimentOutput.setTCoffeeOutput(output);

			return output;
		} catch (OperationException oe) {
			this.clearWhenException();
			throw oe;
		} catch (IOException ioe) {
			this.clearWhenException();
			throw new OperationException("I/O error while running T-Coffee", ioe);
		} catch (InterruptedException ie) {
			return null;
		} catch (Exception e) {
			this.clearWhenException();
			throw new OperationException("Unexpected error running T-Coffee", e);
		}
	}

	@Port(direction = Direction.OUTPUT, order = 3000)
	public OperationOutput runMrBayes() throws OperationException {
		try {
			this.progress.setStatus("Running MrBayes");

			this.checkInterrupted();

			// MrBayes Configuration
			this.mrBayesOperation = new MrBayesOperation();
			this.mrBayesOperation.configure(this.experiment);
			this.configureOutputStreams(this.mrBayesOperation);

			this.checkInterrupted();

			final MrBayesOutput output = this.mrBayesOperation.call();

			this.checkInterrupted();

			this.replaceSequenceNames(output.getConFile(), this.experimentOutput.getRenamedTreeFile(), true);
			this.replaceSequenceNames(output.getPsrfFile(), this.experimentOutput.getPsrfFile());

			this.checkInterrupted();

			this.experimentOutput.setMrBayesOutput(output);

			return output;
		} catch (OperationException oe) {
			this.clearWhenException();
			throw oe;
		} catch (IOException ioe) {
			this.clearWhenException();
			throw new OperationException("I/O error while running MrBayes", ioe);
		} catch (InterruptedException ie) {
			return null;
		} catch (Exception e) {
			this.clearWhenException();
			throw new OperationException("Unexpected error running MrBayes", e);
		}
	}

	@Port(direction = Direction.OUTPUT, order = 4000)
	public CodeMLOutput runCodeML() throws OperationException {
		try {
			this.progress.setStatus("Running CodeML");

			this.checkInterrupted();

			this.codeMLOperation = new CodeMLOperation();
			this.codeMLOperation.configure(this.experiment);

			this.codeMLOperation.setConsFile(this.experimentOutput.getMrBayesOutput().getConFile());
			this.configureOutputStreams(this.codeMLOperation);

			this.checkInterrupted();

			final CodeMLOutput output = this.codeMLOperation.call();

			this.checkInterrupted();

			this.replaceSequenceNames(output.getOutputFile(), this.experimentOutput.getCodeMLOutputFile());
			this.replaceSequenceNames(output.getSummaryFile(), this.experimentOutput.getCodeMLSummaryFile());

			this.checkInterrupted();

			this.experimentOutput.setCodeMLOutput(output);

			return output;
		} catch (OperationException oe) {
			this.clearWhenException();
			throw oe;
		} catch (IOException ioe) {
			this.clearWhenException();
			throw new OperationException("I/O error while running CodeML", ioe);
		} catch (InterruptedException ie) {
			return null;
		} catch (Exception e) {
			this.clearWhenException();
			throw new OperationException("Unexpected error running CodeML", e);
		}
	}

	@Port(direction = Direction.OUTPUT, order = 5000)
	public ExperimentOutput getResult() throws OperationException {
		try {
			this.progress.setStatus("Generating summary file");

			this.checkInterrupted();

			FileUtils.writeLines(
				this.experimentOutput.getSummaryFile(), Arrays.asList(
					"--- EXPERIMENT NOTES",
					FileUtils.readFileToString(this.experiment.getNotesFile()),
					"\n--- WARNINGS",
					this.experiment.getWarnings(),
					"\n--- EXPERIMENT PROPERTIES",
					this.experiment.getConfiguration().toString(),
					"\n--- PSRF SUMMARY",
					FileUtils.readFileToString(this.experimentOutput.getPsrfFile()),
					"\n--- CODEML SUMMARY",
					FileUtils.readFileToString(this.experimentOutput.getCodeMLSummaryFile())
				)
			);

			if (this.logPS != null)
				this.logPS.close();

			this.experimentOutput.setFinished(true);
			this.experiment.setRunning(false);

			return this.experimentOutput;
		} catch (IOException ioe) {
			this.clearWhenException();
			throw new OperationException("Error generating result", ioe);
		} catch (InterruptedException ie) {
			this.clearWhenInterrupted();

			return null;
		} catch (Exception e) {
			this.clearWhenException();
			throw new OperationException("Unexpected error summarizing results", e);
		}
	}

	@Override
	protected ExperimentOutput internalCall() throws OperationException {
		this.createExperimentOutput();
		this.runTCoffee();
		this.runMrBayes();
		this.runCodeML();

		return this.getResult();
	}

	private void replaceOGaps(File inputFile) throws IOException {
		this.replaceOGaps(
			inputFile, new Utils.LinesFilter() {
				@Override
				public boolean accept(int index, String line) {
					return !line.startsWith(">");
				}
			}
		);
	}

	private void replaceOGaps(File inputFile, LinesFilter filter) throws IOException {
		FileUtils.writeLines(
			inputFile,
			Utils.replaceNames(
				Collections.singletonMap("o", "-"),
				FileUtils.readLines(inputFile),
				filter
			)
		);
	}

	private void replaceSequenceNames(File inputFile, File outputFile) throws IOException {
		this.replaceSequenceNames(inputFile, outputFile, false);
	}

	private void replaceSequenceNames(File inputFile, File outputFile, boolean isTree) throws IOException {
		final Map<String, String> names = this.experiment.getNames();

		if (isTree) {
			for (Map.Entry<String, String> name : names.entrySet()) {
				name.setValue(name.getValue().replaceAll("[():,]", "_"));
			}
		}

		final List<String> lines = FileUtils.readLines(inputFile);

		FileUtils.writeLines(outputFile, Utils.replaceNames(names, lines));
	}

	private void replaceSequenceNamesAln(File inputFile, File outputFile) throws IOException {
		final Map<String, String> names = this.experiment.getNames();
		final List<String> lines = FileUtils.readLines(inputFile);

		int maxLength = Integer.MIN_VALUE;
		for (Map.Entry<String, String> name : names.entrySet()) {
			maxLength = Math.max(maxLength, name.getValue().length());
		}

		for (Map.Entry<String, String> name : names.entrySet()) {
			String newName = name.getValue();

			while (newName.length() < maxLength) {
				newName += ' ';
			}

			name.setValue(newName);
		}

		final ListIterator<String> itLines = lines.listIterator();
		if (itLines.hasNext())
			itLines.next();

		// TODO Review parsing
		while (itLines.hasNext()) {
			itLines.next(); // White line

			String line = null;
			// Sequences
			while (itLines.hasNext()) {
				line = itLines.next();
				if (line.isEmpty())
					break;
				final String[] lineSplit = line.split("\\s+");
				final String name = lineSplit.length == 0 ? "" : lineSplit[0];

				if (names.get(name) == null)
					break;

				itLines.set(names.get(name) + "   " + lineSplit[1]);
			}

			// Marks line
			if (line != null && !line.isEmpty() && line.startsWith(" ")) {
				String newLine = line;

				if (maxLength < 13)
					newLine = newLine.substring(13 - maxLength);
				else
					for (int i = 16; i < maxLength + 3; i++) {
						newLine = ' ' + newLine;
					}

				itLines.set(newLine);
			}
		}

		FileUtils.writeLines(outputFile, lines);
	}

	@Override
	@Cancel
	public void cancel() {
		super.cancel();

		if (this.tCoffeeOperation != null)
			this.tCoffeeOperation.cancel();
		if (this.mrBayesOperation != null)
			this.mrBayesOperation.cancel();
		if (this.codeMLOperation != null)
			this.codeMLOperation.cancel();
	}

	@Override
	public void clear() {
		this.experiment.setRunning(false);
		if (this.logPS != null) {
			this.removePrintStream(this.logPS);
			this.logPS.close();
		}

		if (this.tCoffeeOperation != null)
			this.tCoffeeOperation.clear();
		if (this.mrBayesOperation != null)
			this.mrBayesOperation.clear();
		if (this.codeMLOperation != null)
			this.codeMLOperation.clear();

		super.clear();
	}

	@Override
	protected void clearWhenException() {
		if (this.logPS != null) {
			this.removePrintStream(this.logPS);
			this.logPS.close();
		}

		this.experiment.setRunning(false);
	}

	@Override
	protected void clearWhenInterrupted() {
		super.clearWhenInterrupted();

		if (this.logPS != null) {
			this.removePrintStream(this.logPS);
			this.logPS.close();
		}

		this.experiment.setRunning(false);
	}

	private void configureOutputStreams(ProcessOperation<?, ?, ?> operation) {
		operation.clearPrintStreams();

		for (PrintStream ps : this.getPrintStreams()) {
			operation.addPrintStream(ps);
		}
	}

	public final File generateTreeFile(File conFile, File namesFile, File treeFile) {
		// Read names file
		final Map<String, String> names = this.experiment.getNames();

		// Create new tree file
		try (BufferedReader br = new BufferedReader(new FileReader(conFile));
			FileWriter treeFW = new FileWriter(treeFile);
		) {

			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.indexOf("con_50_majrule") != -1)
					for (String s : names.keySet())
						line = line.replaceAll(s, names.get(s));

				treeFW.append(line + "\n");
			}
		} catch (IOException e) {
			LOG.error("Error generating tree file", e);
			
			throw new RuntimeException("Error generating tree file", e);
		}

		return treeFile;
	}
}
