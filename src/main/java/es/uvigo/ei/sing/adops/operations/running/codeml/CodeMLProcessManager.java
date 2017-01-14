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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import es.uvigo.ei.sing.adops.configuration.CodeMLConfiguration;
import es.uvigo.ei.sing.adops.configuration.ExecutableConfigurationUtils;
import es.uvigo.ei.sing.adops.datatypes.CodeMLOutput;
import es.uvigo.ei.sing.adops.operations.GetVersions;
import es.uvigo.ei.sing.adops.operations.running.OperationException;
import es.uvigo.ei.sing.adops.operations.running.ProcessManager;

public abstract class CodeMLProcessManager extends ProcessManager {
	private final static Logger LOGGER = Logger.getLogger(CodeMLProcessManager.class);

	private final static Class<? extends CodeMLProcessManager> DEFAUL_MANAGER_CLASS = CodeMLDefaultProcessManager.class;
	private final static Map<String, Class<? extends CodeMLProcessManager>> VERSION_MANAGERS;

	static {
		VERSION_MANAGERS = new TreeMap<>();

		VERSION_MANAGERS.put("4.7", CodeMLDefaultProcessManager.class);
		VERSION_MANAGERS.put("4.5", CodeMLDefaultProcessManager.class);
		VERSION_MANAGERS.put("4.4d", CodeMLDefaultProcessManager.class);
	}

	@Override
	protected Logger getLogger() {
		return CodeMLProcessManager.LOGGER;
	}

	public static List<String> getSupportedVersions() {
		return new ArrayList<>(VERSION_MANAGERS.keySet());
	}

	public static boolean isSupported(String version) {
		return VERSION_MANAGERS.containsKey(version);
	}

	public static String getVersion(CodeMLConfiguration configuration) throws IOException {
		return GetVersions.getCodeMLVersion(ExecutableConfigurationUtils.createExecutableCommand(configuration));
	}

	public static CodeMLProcessManager createManager(CodeMLConfiguration configuration) throws OperationException {
		Class<? extends CodeMLProcessManager> pmClass;
		try {
			final String version = CodeMLProcessManager.getVersion(configuration);

			if (VERSION_MANAGERS.containsKey(version)) {
				pmClass = VERSION_MANAGERS.get(version);
			} else {
				pmClass = DEFAUL_MANAGER_CLASS;
			}
		} catch (IOException e) {
			pmClass = DEFAUL_MANAGER_CLASS;
		}

		try {
			return pmClass.getConstructor(CodeMLConfiguration.class).newInstance(configuration);
		} catch (Exception e) {
			throw new OperationException("", "Unexpected error while creating CodeMLProcessManager", e);
		}
	}

	protected final CodeMLConfiguration configuration;
	protected String codeMLCommand;

	public CodeMLProcessManager(CodeMLConfiguration configuration) throws OperationException {
		super();

		try {
			if (!this.isCompatibleWith(CodeMLProcessManager.getVersion(configuration))) {
				throw new IllegalArgumentException("Incompatible CodeML version");
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Incompatible CodeML version");
		}

		this.configuration = configuration;
		this.codeMLCommand = ExecutableConfigurationUtils.createExecutableCommand(configuration);
	}

	protected String getCodeMLCommand() {
		return codeMLCommand;
	}

	protected int runCodeML(String params, File outputFile, boolean append) throws OperationException, InterruptedException {
		if (outputFile == null)
			throw new IllegalArgumentException("outputFile can not be null");

		return this.runCodeML(params, outputFile, append, null, outputFile.getParentFile());
	}

	protected int runCodeML(String params, File outputFile, boolean append, String[] envp, File directory) throws OperationException, InterruptedException {
		final String command = this.getCodeMLCommand() + " " + params;
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
			throw new OperationException(command, "I/O error while running CodeML: " + command, ioe);
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

		return result;
	}

	public abstract boolean isCompatibleWith(String version);

	public abstract void createNexusFile(File fastaFile, File nexusFile) throws OperationException;

	public abstract void createTreeFile(File treeFile, File consFile) throws OperationException;

	public abstract void createCodeMLFile(File nexusFile, File treeFile, File ctlFile, File outputFile) throws OperationException;

	public abstract int executeCodeMLFile(File ctlFile, File logFile) throws OperationException;

	public abstract void buildSummary(File outFile, File summaryFile) throws OperationException;

	public abstract void moveOutputFiles(CodeMLOutput output) throws OperationException;

	protected abstract String createCodeMLCtl(File nexusFile, File treeFile, File outputFile);

}
