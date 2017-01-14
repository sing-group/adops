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
package es.uvigo.ei.sing.adops.operations.running.codeml;

import java.io.File;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.adops.configuration.CodeMLConfiguration;
import es.uvigo.ei.sing.adops.datatypes.CodeMLOutput;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.operations.running.OperationException;
import es.uvigo.ei.sing.adops.operations.running.ProcessOperation;

@Operation(name = "CodeML")
public class CodeMLOperation extends ProcessOperation<CodeMLProcessManager, CodeMLConfiguration, CodeMLOutput> {
	private final static Logger LOGGER = Logger.getLogger(CodeMLOperation.class);

	private File consFile;

	public CodeMLOperation() {
		super(CodeMLConfiguration.class);
	}

	@Override
	protected Logger getLogger() {
		return CodeMLOperation.LOGGER;
	}

	@Port(name = "FASTA File", order = 1, direction = Direction.INPUT, allowNull = false)
	public void setInputFile(File fastaFile) {
		super.setInputFile(fastaFile);
	}

	@Port(name = "con File", order = 2, direction = Direction.INPUT, allowNull = false)
	public void setConsFile(File f) {
		this.consFile = f;
	}

	@Port(name = "Output Folder", order = 3, direction = Direction.INPUT, allowNull = false)
	public void setOutputFolder(File outputFolder) {
		super.setOutputFolder(outputFolder);
	}

	@Port(name = "Use Std. Output", order = 4, direction = Direction.INPUT, defaultValue = "true")
	public void setUseStdOutput(boolean useStdOutput) {
		if (useStdOutput)
			this.addPrintStream(System.out);
		else
			this.removePrintStream(System.out);
	}

	@Port(direction = Direction.OUTPUT, order = 1000)
	@Override
	public CodeMLOutput call() throws OperationException, InterruptedException {
		final CodeMLOutput output = new CodeMLOutput(this.getInputFile(), this.getOutputFolder());
		this.process = CodeMLProcessManager.createManager(this.configuration);

		for (PrintStream ps : this.getPrintStreams()) {
			this.process.addPrinter(ps);
		}

		try {

			this.checkInterrupted();

			// Step 1 - Get paml-nexus file from FASTA input
			this.process.createNexusFile(this.getInputFile(), output.getNexusFile());

			// Step 2 - Build tree from .cons file
			this.process.createTreeFile(output.getTreeFile(), this.consFile);

			this.checkInterrupted();
			// Step 3 - Launch CodeML
			this.process.createCodeMLFile(output.getNexusFile(), output.getTreeFile(), output.getCodeMLCtlFile(), output.getOutputFile());

			this.checkInterrupted();

			this.process.executeCodeMLFile(output.getCodeMLCtlFile(), output.getLogFile());

			this.checkInterrupted();

			this.println("CodeML output code: " + output.getState());

			this.checkInterrupted();

			this.process.buildSummary(output.getOutputFile(), output.getSummaryFile());

			this.checkInterrupted();

			return output;
		} catch (OperationException oe) {
			if (oe.getCause() instanceof InterruptedException) {
				throw (InterruptedException) oe.getCause();
			} else {
				throw new OperationException(oe.getCommand(), "Error while running CodeML: " + oe.getMessage(), oe);
			}
		}
	}

	@Override
	protected void configureExperiment(Experiment experiment) {
		this.setInputFile(experiment.getResult().getTCoffeeOutput().getAlignmentFile());
		this.setOutputFolder(experiment.getFilesFolder());
	}
}
