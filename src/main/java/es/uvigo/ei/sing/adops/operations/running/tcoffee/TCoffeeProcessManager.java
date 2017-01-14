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
package es.uvigo.ei.sing.adops.operations.running.tcoffee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import es.uvigo.ei.sing.adops.configuration.ExecutableConfigurationUtils;
import es.uvigo.ei.sing.adops.configuration.TCoffeeConfiguration;
import es.uvigo.ei.sing.adops.operations.GetVersions;
import es.uvigo.ei.sing.adops.operations.running.OperationException;
import es.uvigo.ei.sing.adops.operations.running.ProcessManager;

public abstract class TCoffeeProcessManager extends ProcessManager {
	private final static Logger LOGGER = Logger.getLogger(TCoffeeProcessManager.class);

	private final static Map<String, Class<? extends TCoffeeProcessManager>> VERSION_MANAGERS;
	private final static Class<? extends TCoffeeProcessManager> DEFAUL_MANAGER_CLASS = TCoffeeDefaultProcessManager.class;

	static {
		VERSION_MANAGERS = new TreeMap<>();

		VERSION_MANAGERS.put("10.00.r1613", TCoffeeDefaultProcessManager.class);
		VERSION_MANAGERS.put("9.02.r1228", TCoffeeDefaultProcessManager.class);
	}

	@Override
	protected Logger getLogger() {
		return TCoffeeProcessManager.LOGGER;
	}

	public static List<String> getSupportedVersions() {
		return new ArrayList<String>(VERSION_MANAGERS.keySet());
	}

	public static boolean isSupported(String version) {
		return VERSION_MANAGERS.containsKey(version);
	}

	public static String getVersion(TCoffeeConfiguration configuration) throws IOException {
		return GetVersions.getTCoffeeVersion(ExecutableConfigurationUtils.createExecutableCommand(configuration));
	}

	public static TCoffeeProcessManager createManager(TCoffeeConfiguration configuration) throws OperationException {
		Class<? extends TCoffeeProcessManager> pmClass;
		try {
			final String version = TCoffeeProcessManager.getVersion(configuration);

			if (VERSION_MANAGERS.containsKey(version)) {
				pmClass = VERSION_MANAGERS.get(version);
			} else {
				pmClass = DEFAUL_MANAGER_CLASS;
			}
		} catch (IOException e) {
			pmClass = DEFAUL_MANAGER_CLASS;
		}

		try {
			return pmClass.getConstructor(TCoffeeConfiguration.class).newInstance(configuration);
		} catch (Exception e) {
			throw new OperationException("", "Unexpected error while creating TCoffeeProcessManager", e);
		}
	}

	protected final TCoffeeConfiguration configuration;
	protected final String tCoffeeCommand;

	public TCoffeeProcessManager(TCoffeeConfiguration configuration) throws OperationException {
		try {
			if (!this.isCompatibleWith(TCoffeeProcessManager.getVersion(configuration)))
				throw new IllegalArgumentException("Incompatible T-Coffee version");
		} catch (IOException ioe) {
			throw new IllegalArgumentException("Incompatible T-Coffee version");
		}

		this.configuration = configuration;
		this.tCoffeeCommand = ExecutableConfigurationUtils.createExecutableCommand(this.configuration);
	}

	protected int runTCoffee(String params, File outputFile, boolean append) throws OperationException {
		if (outputFile == null)
			throw new IllegalArgumentException("outputFile can not be null");

		return this.runTCoffee(params, outputFile, append, null, outputFile.getParentFile());
	}

	protected int runTCoffee(String params, File outputFile, boolean append, String[] envp, File directory) throws OperationException {
		final String command = this.tCoffeeCommand + " " + params;
		int result = -1;

		PrintStream ps = null;
		try {
			synchronized (this) {
				if (outputFile != null) {
					ps = new PrintStream(new FileOutputStream(outputFile, append));
					this.addPrinter(ps);
				}

				result = this.runCommand(command, envp, directory);

				if (ps != null) {
					this.removePrinter(ps);
				}
			}
		} catch (IOException ioe) {
			throw new OperationException(command, "I/O error while running T-Coffee: " + command, ioe);
		} catch (InterruptedException ie) {
			throw new OperationException(command, "T-Coffee interrupted", ie);
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

		return result;
	}

	public abstract boolean isCompatibleWith(String version);

	public abstract int convertDNAIntoAmino(File dnaFile, File outputFile) throws OperationException;

	public abstract int runAlignment(File fastaFile, AlignMethod alignMethod, File outputFile) throws OperationException;

	public abstract int evaluateAlignment(File alignmentFile, File output) throws OperationException;

	public abstract InformativePositions computeInformativePositions(File alignmentFile, File scoreFile, File consoleOutFile, int minScore)
		throws OperationException;

	public abstract int generateDivFile(File alnFile, File divFile) throws OperationException;

	public abstract SequenceDiversity calculateMinDiversity(Set<SequenceDiversity> usedSequences, File file) throws OperationException;

	public abstract int removeSequence(String sequenceId, File fastaFile, File newFastaFile) throws OperationException;

	public abstract int runAlingment(File fastaFile, AlignMethod alignMethod, File logFile) throws OperationException;

	public abstract int calculateAlignmentScore(File alnFile, File logFile) throws OperationException;

	public abstract int extractSequences(Set<String> sequences, File proteinFile, File resultFile) throws OperationException;

	public abstract int profile(File fastaFile, File profile, AlignMethod alignMethod, String resultsPrefix, File logFile) throws OperationException;

	public abstract int convertAminoIntoDNA(File fasta, File alnFile, File outputFile) throws OperationException;

	public abstract int toFastaAln(File clustal, File alnFile) throws OperationException;

	protected abstract int calculateI(File iInputFile, File iOutputFile, File outputFile) throws OperationException;

	protected abstract int calculateS(File scoreFile, File outputFile) throws OperationException;

	protected abstract int calculateBS(File alignmentFile, File scoreFile, File ipiFile, File outputFile, int minScore) throws OperationException;

	public static class InformativePositions {
		public int I, S, BS;
	}

	public static class SequenceDiversity implements Comparable<SequenceDiversity> {
		private final String id;
		private final int number;
		private final double average;

		public SequenceDiversity(String seqLine) {
			final String[] split = seqLine.split("\t");
			this.id = split[2].trim();
			this.number = Integer.parseInt(split[1].trim());

			split[4] = split[4].trim();
			if (split[4].equalsIgnoreCase("-nan") || split[4].equalsIgnoreCase("nan"))
				this.average = Double.NaN;
			else
				this.average = Double.parseDouble(split[4]);
		}

		public String getId() {
			return this.id;
		}

		public int getNumber() {
			return this.number;
		}

		public double getAverage() {
			return average;
		}

		@Override
		public int compareTo(SequenceDiversity sequence) {
			return Double.compare(this.getAverage(), sequence.getAverage());
		}
	}
}
