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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.sing.adops.operations.running.ProcessUtils.Printer;

public abstract class CallableOperation<V> implements Callable<V> {
	private final Set<File> generatedFiles;
	private final List<PrintStream> outputStreams;
	private File outputFolder;
	
	private boolean isInterrupted = false;

	public CallableOperation() {
		super();

		this.generatedFiles = new HashSet<File>();
		this.outputStreams = new LinkedList<PrintStream>();
	}

	protected Logger getLogger() {
		return null;
	}

	public File getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	protected Printer[] getPrinters() {
		final Printer[] psPrinters = ProcessUtils.wrap(
			this.outputStreams.toArray(new PrintStream[this.outputStreams.size()])
		);

		if (this.getLogger() == null) {
			return psPrinters;
		} else {
			final Printer[] printers = new Printer[psPrinters.length + 1];

			printers[0] = ProcessUtils.wrap(this.getLogger());
			System.arraycopy(psPrinters, 0, printers, 1, psPrinters.length);

			return printers;
		}
	}

	protected void println(String line) {
		if (this.getLogger() != null)
			this.getLogger().info(line);

		for (PrintStream ps : this.outputStreams) {
			ps.println(line);
		}
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

	protected File newOutputFile(String name) {
		final File file = new File(this.getOutputFolder(), name);
		this.addGeneratedFile(file);

		return file;
	}

	protected void addGeneratedFile(File file) {
		this.generatedFiles.add(file);
	}

	protected void addGeneratedFiles(File... files) {
		this.generatedFiles.addAll(Arrays.asList(files));
	}

	protected void clearGeneratedFile() {
		this.generatedFiles.clear();
	}

	public void clear() {
		for (File file : new ArrayList<File>(this.generatedFiles)) {
			file.delete();
			this.generatedFiles.remove(file);
		}
	}
	
	protected void clearWhenInterrupted() {
		this.isInterrupted = false;
	}
	
	protected void clearWhenException() {
		
	}

	protected void checkInterrupted() throws InterruptedException {
		if (this.isInterrupted) {
			throw new InterruptedException();
		}
	}
	
	@Override
	public V call() throws OperationException {
		if (!this.getOutputFolder().isDirectory() && !this.getOutputFolder().mkdirs()) {
			throw new OperationException("", "Output folder does not exist and can not be created");
		}
		
		try {
			return this.internalCall();
		} catch (InterruptedException ie) {
			this.clearWhenInterrupted();
			
			return null;
		} catch (OperationException oe) {
			this.clearWhenException();
			
			throw oe;
		}
	}
	
	protected abstract V internalCall() throws InterruptedException, OperationException;

	@Cancel
	public void cancel() {
//		if (this.currentThread != null)
//			this.currentThread.interrupt();
		this.isInterrupted = true;
	}

}
