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

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.uvigo.ei.sing.adops.configuration.Configuration;
import es.uvigo.ei.sing.adops.configuration.SubConfiguration;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.datatypes.OperationOutput;

public abstract class ProcessOperation<P extends ProcessManager, C extends SubConfiguration, V extends OperationOutput> implements Callable<V> {
	private final List<PrintStream> outputStreams;
	
	private File inputFile;
	private File outputFolder;

	private final AtomicBoolean interrupted;

	protected final Class<C> configurationClass;
	protected C configuration;

	protected P process;

	protected ProcessOperation(Class<C> configurationClass) {
		this.interrupted = new AtomicBoolean(false);

		this.outputStreams = new LinkedList<PrintStream>();

		this.configuration = Configuration.getSystemConfiguration().getSubConfiguration(configurationClass);
		this.configurationClass = configurationClass;
	}

	public void cancel() {
		synchronized (this.interrupted) {
			this.interrupted.set(true);
			if (this.process != null)
				this.process.interrupt();
		}
	}

	public void clear() {
		this.interrupted.set(false);
	}

	protected void checkInterrupted() throws InterruptedException {
		synchronized (this.interrupted) {
			if (this.interrupted.get()) {
				throw new InterruptedException();
			}
		}
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public File getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	protected Logger getLogger() {
		return null;
	}

	protected void println(String line, Level level) {
		if (this.getLogger() != null)
			this.getLogger().log(level, line);

		for (PrintStream ps : this.outputStreams) {
			ps.println(line);
		}
	}

	protected void println(String line) {
		this.println(line, Level.INFO);
	}

	public boolean setPrintStream(PrintStream ps) {
		this.clearPrintStreams();
		return this.addPrintStream(ps);
	}

	public boolean addPrintStream(PrintStream ps) {
		if (ps == null || this.outputStreams.contains(ps)) {
			return false;
		} else {
			return this.outputStreams.add(ps);
		}
	}

	public List<PrintStream> getPrintStreams() {
		return Collections.unmodifiableList(this.outputStreams);
	}

	public boolean removePrintStream(PrintStream ps) {
		return this.outputStreams.remove(ps);
	}

	public void clearPrintStreams() {
		this.outputStreams.clear();
	}

	public void resetConfiguration() {
		this.configuration = Configuration.getSystemConfiguration().getSubConfiguration(this.configurationClass);
	}

	public void configure(Experiment experiment) {
		this.configureExperiment(experiment);

		this.configure(experiment.getConfiguration());
	}

	public void configure(Configuration configuration) {
		this.configureConfiguration(configuration);

		this.configure(configuration.getSubConfiguration(this.configurationClass));
	}

	public void configure(C subConfiguration) {
		this.configuration = subConfiguration;

		this.configureSubConfiguration(this.configuration);
	}

	protected void configureExperiment(Experiment experiment) {}

	protected void configureConfiguration(Configuration configuration) {}

	protected void configureSubConfiguration(C subConfiguration) {}
}
