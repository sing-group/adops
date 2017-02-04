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
package es.uvigo.ei.sing.adops.operations.running.mrbayes;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.adops.configuration.MrBayesConfiguration;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.datatypes.MrBayesOutput;
import es.uvigo.ei.sing.adops.operations.running.OperationException;
import es.uvigo.ei.sing.adops.operations.running.ProcessOperation;
import es.uvigo.ei.sing.alter.converter.DefaultFactory;
import es.uvigo.ei.sing.alter.parser.ParseException;
import es.uvigo.ei.sing.alter.reader.Reader;
import es.uvigo.ei.sing.alter.writer.Writer;

@Operation(name = "MrBayes")
public class MrBayesOperation extends ProcessOperation<MrBayesProcessManager, MrBayesConfiguration, MrBayesOutput> {
	private final static Logger LOGGER = Logger.getLogger(MrBayesOperation.class);

	public MrBayesOperation() {
		super(MrBayesConfiguration.class);
	}

	@Port(name = "FASTA File", order = 1, direction = Direction.INPUT, allowNull = false)
	public void setInputFile(File fastaFile) {
		super.setInputFile(fastaFile);
	}

	@Port(name = "Output Folder", order = 2, direction = Direction.INPUT, allowNull = false)
	public void setOutputFolder(File outputFolder) {
		super.setOutputFolder(outputFolder);
	}

	@Port(name = "Generations", order = 3, direction = Direction.INPUT, defaultValue = "500000")
	public void setGenerations(int ngen) {
		this.configuration.setNumOfGenerations(ngen);
	}

	@Port(name = "SumP Burnin", order = 4, direction = Direction.INPUT, defaultValue = "1250")
	public void setPBurnin(int burnin) {
		this.configuration.setPBurnin(burnin);
	}

	@Port(name = "SumT Burnin", order = 5, direction = Direction.INPUT, defaultValue = "1250")
	public void setTBurnin(int burnin) {
		this.configuration.setTBurnin(burnin);
	}

	@Port(name = "Use Std. Output", order = 6, direction = Direction.INPUT, defaultValue = "true")
	public void setUseStdOutput(boolean useStdOutput) {
		if (useStdOutput)
			this.addPrintStream(System.out);
		else
			this.removePrintStream(System.out);
	}

	@Port(direction = Direction.OUTPUT, order = 1000)
	@Override
	public MrBayesOutput call() throws OperationException, InterruptedException {
		final MrBayesOutput output = new MrBayesOutput(this.getInputFile(), this.getOutputFolder());
		this.process = MrBayesProcessManager.createManager(this.configuration);

		for (PrintStream ps : this.getPrintStreams()) {
			this.process.addPrinter(ps);
		}

		try {
			this.checkInterrupted();

			final DefaultFactory factory = new DefaultFactory();
			final Reader fastaReader = factory.getReader("linux", "clustal", "fasta", false, "logger");
			final Writer nexusWriter = factory.getWriter("linux", "mrbayes", "nexus", false, false, false, false, "logger");

			this.checkInterrupted();

			final String fastaString = FileUtils.readFileToString(this.getInputFile());

			this.checkInterrupted();

			final String nexusString = nexusWriter.write(fastaReader.read(fastaString));
			FileUtils.writeStringToFile(output.getNexusFile(), nexusString);

			this.checkInterrupted();

			this.process.createMrBayesFile(output);

			this.checkInterrupted();

			output.setState(this.process.alignSequences(output));

			this.println("MrBayes output code: " + output.getState());
			if (output.getState() == 134) {
				throw new OperationException("Missing input. You are probably using a path too long.");
			} else if (output.getState() != 0) {
				throw new OperationException("Could not converge. Please, check output log.");
			}

			this.checkInterrupted();

			this.process.buildSummary(output);

			this.checkInterrupted();

			return output;
		} catch (OperationException oe) {
			if (oe.getCause() instanceof InterruptedException) {
				throw (InterruptedException) oe.getCause();
			} else {
				throw new OperationException(oe.getCommand(), "Error while running MrBayes: " + oe.getMessage() + "  (processing input file: " + this.getInputFile() + ")", oe);
			}
		} catch (ParseException pe) {
			throw new OperationException("Error while using ALTER (processing input file: " + this.getInputFile() + ")", pe);
		} catch (IOException ioe) {
			throw new OperationException(this.process.getLastCommand(), "I/O error while running MrBayes: " + ioe.getMessage() + " (processing input file: " + this.getInputFile() + ")", ioe);
		}
	}

	@Override
	protected void configureExperiment(Experiment experiment) {
		super.configureExperiment(experiment);

		this.setInputFile(experiment.getResult().getTCoffeeOutput().getAlignmentFile());
		this.setOutputFolder(experiment.getFilesFolder());
	}

	@Override
	public Logger getLogger() {
		return MrBayesOperation.LOGGER;
	}
}
