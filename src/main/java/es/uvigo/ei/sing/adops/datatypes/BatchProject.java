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
package es.uvigo.ei.sing.adops.datatypes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.sing.adops.configuration.Configuration;
import es.uvigo.ei.sing.adops.util.Utils;

@Datatype(namingMethod = "getName", structure = Structure.LIST)
public class BatchProject extends Observable implements HasConfiguration, Observer {
	private static final String CONFIGURATION_FILE = "batch.conf";

	private static final Logger LOG = Logger.getLogger(BatchProject.class);

	private final Configuration configuration;
	private final File propertiesFile;
	private final File folder;
	private final File[] fastaFiles;

	private volatile BatchProjectOutput output;

	private boolean deleted;
	private boolean running;

	public BatchProject(File folder, File[] fastaFiles, boolean absoluteFasta) throws IOException, IllegalArgumentException {
		this(Configuration.getSystemConfiguration(), folder, fastaFiles, absoluteFasta);
	}

	public BatchProject(File folder, File propertiesFile, File[] fastaFiles, boolean absoluteFasta) throws IOException, IllegalArgumentException {
		this(new Configuration(propertiesFile), folder, fastaFiles, absoluteFasta);
	}

	public BatchProject(Configuration configuration, File folder, File[] fastaFiles, boolean absoluteFasta) throws IOException, IllegalArgumentException {
		this.fastaFiles = new File[fastaFiles.length];
		this.folder = folder;
		this.deleted = false;
		this.running = false;

		if (this.folder.exists()) {
			if (this.folder.listFiles().length > 0)
				throw new IllegalArgumentException("Batch project folder already exists and it is not empty (" + this.folder + ")");
			if (!this.folder.canRead())
				throw new IllegalArgumentException("Batch project folder is not readable (" + this.folder + ")");
			if (!this.folder.canWrite())
				throw new IllegalArgumentException("Batch project folder is not writeable (" + this.folder + ")");
		} else if (!this.folder.mkdirs()) {
			throw new IllegalArgumentException(
				"Batch project folder (" + folder.getAbsolutePath() + ") could not be created"
			);
		}

		this.propertiesFile = new File(this.folder, BatchProject.CONFIGURATION_FILE);
		this.configuration = new Configuration(configuration);
		Utils.storeAllProperties(this.configuration.toProperties(true), this.propertiesFile, LOG);

		if (absoluteFasta) {
			System.arraycopy(fastaFiles, 0, this.fastaFiles, 0, fastaFiles.length);
			this.configuration.setFastaFile(filesToFilenames(this.fastaFiles));
			Arrays.sort(this.fastaFiles);
		} else {
			for (int i = 0; i < fastaFiles.length; i++) {
				this.fastaFiles[i] = batchFastaFile(this.folder, fastaFiles[i]);
				FileUtils.copyFile(fastaFiles[i], this.fastaFiles[i]);
			}

			Arrays.sort(this.fastaFiles);
			this.createFastasFile();
		}
	}

	public BatchProject(File folder) throws IllegalStateException {
		this.folder = folder;
		this.deleted = false;
		this.running = false;
		this.propertiesFile = new File(this.folder, BatchProject.CONFIGURATION_FILE);
		this.configuration = new Configuration(Configuration.getSystemConfiguration(), this.propertiesFile);

		if (this.hasAbsoluteFastaPaths()) {
			this.fastaFiles = filenamesToFiles(this.configuration.getFastaFile());
		} else {
			final List<File> fastaList = new LinkedList<>();

			try {
				for (String fastaFilename : FileUtils.readLines(new File(this.folder, "fasta.names"))) {
					fastaList.add(new File(this.folder, fastaFilename));
				}

				this.fastaFiles = fastaList.toArray(new File[fastaList.size()]);
			} catch (IOException e) {
				throw new IllegalStateException("Missing 'fasta.names' file", e);
			}
		}

		Arrays.sort(this.fastaFiles);
		this.getOutput(); // Forces output loading
	}

	private boolean hasAbsoluteFastaPaths() {
		final String fastaFiles = this.configuration.getFastaFile();

		return fastaFiles != null && !fastaFiles.trim().isEmpty();
	}

	private final static String noExtensionFilename(String filename) {
		if (filename.contains(".")) {
			return filename.substring(0, filename.lastIndexOf('.'));
		} else {
			return filename;
		}
	}

	private final static String filenameExtension(String filename) {
		if (filename.contains(".") && filename.charAt(filename.length() - 1) != '.') {
			return filename.substring(filename.lastIndexOf('.') + 1);
		} else {
			return "";
		}
	}

	private final static String numberedFileName(String filename, int number) {
		return new StringBuilder(noExtensionFilename(filename))
			.append('#').append(number)
			.append(filenameExtension(filename))
		.toString();
	}

	private final static File batchFastaFile(File folder, File fasta) {
		File batchFastaFile = new File(folder, fasta.getName());

		int count = 1;
		while (batchFastaFile.exists()) {
			batchFastaFile = new File(folder, numberedFileName(fasta.getName(), count++));
		}

		return batchFastaFile;
	}

	private final static String filesToFilenames(File[] files) {
		final StringBuilder sb = new StringBuilder();

		for (File file : files) {
			if (sb.length() > 0)
				sb.append(',');
			sb.append(file.getAbsolutePath());
		}

		return sb.toString();
	}

	private final static File[] filenamesToFiles(String string) {
		final String[] filenames = string.split(",");
		final File[] files = new File[filenames.length];

		for (int i = 0; i < filenames.length; i++) {
			files[i] = new File(filenames[i]);
		}

		return files;
	}

	public String getName() {
		return this.folder.getName();
	}

	public BatchProjectOutput getOutput() {
		if (this.output == null) {
			synchronized (this) {
				if (this.output == null) {
					this.output = new BatchProjectOutput(this);
					this.output.addObserver(this);
				}
			}
		}

		return this.output;
	}

	@ListElements(modifiable = false)
	public List<Project> listProjects() {
		return new ArrayList<>(this.getOutput().getCreatedProjects());
	}

	@Override
	public Configuration getConfiguration() {
		return this.configuration;
	}

	public void updateProjectsConfiguration() {
		final File propertiesFile = this.getPropertiesFile();
		final BatchProjectOutput output = this.getOutput();

		for (Project project : this.listProjects()) {
			if (output.isReady(project)) {
				File overridedPropertiesFile = project.getPropertiesFile();

				try {
					FileUtils.copyFile(propertiesFile, overridedPropertiesFile);

					if (!project.getExperiments().isEmpty()) {
						overridedPropertiesFile = project.getExperiments().get(0).getPropertiesFile();
						FileUtils.copyFile(propertiesFile, overridedPropertiesFile);
					}
				} catch (IOException e) {
					LOG.error(
						new StringBuilder("Error copying properties file: ")
							.append(propertiesFile)
							.append(" => ")
							.append(overridedPropertiesFile)
						.toString(),
						e
					);
				}
			}
		}
	}

	public File[] getFastaFiles() {
		return fastaFiles;
	}

	private void createFastasFile() throws IOException {
		final List<String> fastasNames = new ArrayList<>(this.fastaFiles.length);

		for (File fastaFile : this.fastaFiles) {
			fastasNames.add(fastaFile.getName());
		}

		FileUtils.writeLines(new File(this.folder, "fasta.names"), fastasNames);
	}

	public File[] getProjectDirectories() throws IllegalStateException {
		final List<File> projects = new LinkedList<>();

		if (this.hasAbsoluteFastaPaths()) {
			for (int i = 0; i < fastaFiles.length; i++) {
				final File fastaFile = batchFastaFile(this.folder, fastaFiles[i]);
				projects.add(new File(this.folder, noExtensionFilename(fastaFile.getName())));
			}
		} else {
			try {
				for (String name : FileUtils.readLines(new File(this.folder, "fasta.names"))) {
					projects.add(new File(this.folder, noExtensionFilename(name)));
				}
			} catch (IOException e) {
				throw new IllegalStateException("Missing 'project.names' file.", e);
			}
		}

		return projects.toArray(new File[projects.size()]);
	}

	public File getFolder() {
		return this.folder;
	}

	@Override
	public File getPropertiesFile() {
		return this.propertiesFile;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public void delete() throws IOException {
		FileUtils.deleteDirectory(this.folder);
		this.deleted = true;

		this.setChanged();
		this.notifyObservers();
	}

	public boolean hasResults() {
		return this.output != null && this.output.hasResults();
	}

	public boolean isFinished() {
		return this.output != null && this.output.isFinished();
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public void update(Observable o, Object arg) {
		this.setChanged();
		this.notifyObservers();
	}

	public void clearUncompleteProjects() {
		final BatchProjectOutput output = this.getOutput();
		for (Project project : this.listProjects()) {
			if (!output.isReady(project) &&
				!(output.isFinished(project) || output.isError(project)) &&
				!project.getExperiments().isEmpty()
			) {
				project.getExperiments().get(0).delete();
			}
		}
	}
}
