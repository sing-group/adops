/*-
 * #%L
 * ADOPS
 * %%
 * Copyright (C) 2012 - 2019 David Reboiro-Jato, Miguel Reboiro-Jato, Jorge Vieira, Florentino Fdez-Riverola, Cristina P. Vieira, Nuno A. Fonseca
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import es.uvigo.ei.sing.adops.configuration.ExecutableConfigurationUtils;
import es.uvigo.ei.sing.adops.configuration.MrBayesConfiguration;
import es.uvigo.ei.sing.adops.datatypes.MrBayesOutput;
import es.uvigo.ei.sing.adops.operations.GetVersions;
import es.uvigo.ei.sing.adops.operations.running.Command;
import es.uvigo.ei.sing.adops.operations.running.OperationException;
import es.uvigo.ei.sing.adops.operations.running.ProcessManager;

public abstract class MrBayesProcessManager extends ProcessManager {
	private final static Logger LOGGER = Logger.getLogger(MrBayesProcessManager.class);

	private final static Map<String, Class<? extends MrBayesProcessManager>> VERSION_MANAGERS;
	private final static Class<? extends MrBayesProcessManager> DEFAUL_MANAGER_CLASS = MrBayesDefaultProcessManager.class;

	static {
		VERSION_MANAGERS = new TreeMap<String, Class<? extends MrBayesProcessManager>>();

		VERSION_MANAGERS.put("3.1.2", MrBayesDefaultProcessManager.class);
		VERSION_MANAGERS.put("3.2.1", MrBayes3_2ProcessManager.class);
		VERSION_MANAGERS.put("3.2.2", MrBayes3_2ProcessManager.class);
		VERSION_MANAGERS.put("3.2.1 x64", MrBayes3_2ProcessManager.class);
		VERSION_MANAGERS.put("3.2.2 x64", MrBayes3_2ProcessManager.class);
	}

	@Override
	protected Logger getLogger() {
		return MrBayesProcessManager.LOGGER;
	}

	public static List<String> getSupportedVersions() {
		return new ArrayList<>(VERSION_MANAGERS.keySet());
	}

	public static boolean isSupported(String version) {
		return VERSION_MANAGERS.containsKey(version);
	}

	public static String getVersion(MrBayesConfiguration configuration) throws IOException {
		return GetVersions.getMrBayesVersion(MrBayesProcessManager.createExecutableCommand(configuration, false));
	}

	public static MrBayesProcessManager createManager(MrBayesConfiguration configuration) throws OperationException {
		Class<? extends MrBayesProcessManager> pmClass;
		try {
			final String version = MrBayesProcessManager.getVersion(configuration);

			if (VERSION_MANAGERS.containsKey(version)) {
				pmClass = VERSION_MANAGERS.get(version);
			} else {
				pmClass = DEFAUL_MANAGER_CLASS;
			}
		} catch (IOException e) {
			pmClass = DEFAUL_MANAGER_CLASS;
		}

		try {
			return pmClass.getConstructor(MrBayesConfiguration.class).newInstance(configuration);
		} catch (Exception e) {
			throw new OperationException("Unexpected error while creating MrBayesProcessManager", e);
		}
	}

	protected static String createExecutableCommand(MrBayesConfiguration configuration, boolean tryToAddMpich) {
		String mpich = null;
		if (tryToAddMpich) {
			mpich = configuration.getMpich();

			if (mpich != null && !mpich.trim().isEmpty()) {
				mpich = mpich.trim() + ' ';
			} else {
				mpich = null;
			}
		}

		return ExecutableConfigurationUtils.createExecutableCommand(configuration, mpich, null);
	}

	protected final MrBayesConfiguration configuration;
	protected final String mrBayesCommand;

	public MrBayesProcessManager(MrBayesConfiguration configuration) throws OperationException {
		super();

		try {
			final String version = MrBayesProcessManager.getVersion(configuration);

			if (!this.isCompatibleWith(version))
				throw new IllegalArgumentException("Incompatible MrBayes version: " + version);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error retrieving MrBayes version");
		}

		this.configuration = configuration;
		this.mrBayesCommand = MrBayesProcessManager.createExecutableCommand(this.configuration, true);
	}

	protected int runMrBayes(String params) throws OperationException {
		final Command command = new Command(this.mrBayesCommand + " " + params);
		try {
			return this.runCommand(command);
		} catch (InterruptedException e) {
			throw new OperationException(command, "Error running MrBayes", e);
		}
	}

	protected int runMrBayes(String params, String[] envp, File directory) throws OperationException {
		final Command command = new Command(this.mrBayesCommand + " " + params, envp, directory);
		try {
			return this.runCommand(command);
		} catch (InterruptedException e) {
			throw new OperationException(command, "Error runing MrBayes", e);
		}
	}

	public abstract boolean isCompatibleWith(String version);

	public abstract void createMrBayesFile(MrBayesOutput output) throws OperationException;

	public abstract int alignSequences(MrBayesOutput output) throws OperationException;

	public abstract void buildSummary(MrBayesOutput output) throws OperationException;
}
