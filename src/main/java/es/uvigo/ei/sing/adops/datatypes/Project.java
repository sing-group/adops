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
package es.uvigo.ei.sing.adops.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.sing.adops.configuration.Configuration;

@Datatype(
	structure = Structure.LIST,
	namingMethod = "getName",
	autoOpen = true
)
public class Project extends Observable implements HasConfiguration {
	private static final String CONFIGURATION_FILE = "project.conf";
	private static final String FILE_NAMES = "names.txt";
	private static final String FILE_FASTA = "input.fasta";

	private final File folder, originalFastaFile;
	private File fastaFile, namesFile;
	private final Configuration configuration;
	private final File propertiesFile;

	private final List<ProjectExperiment> experiments;

	private boolean deleted;

	public Project(File folder, File originalFastaFile) throws IOException, IllegalArgumentException {
		this(Configuration.getSystemConfiguration(), folder, originalFastaFile);
	}

	public Project(Configuration configuration, File folder, File originalFastaFile) throws IOException, IllegalArgumentException {
		this(configuration, folder, originalFastaFile, false);
	}

	public Project(File folder, File originalFastaFile, boolean absoluteFasta) throws IOException, IllegalArgumentException {
		this(Configuration.getSystemConfiguration(), folder, originalFastaFile, absoluteFasta);
	}

	public Project(Configuration configuration, File folder, File originalFastaFile, boolean absoluteFasta) throws IOException, IllegalArgumentException {
		this(configuration, folder, originalFastaFile, absoluteFasta, true);
	}

	public Project(Configuration configuration, File folder, File originalFastaFile, boolean absoluteFasta, boolean checkFasta)
		throws IOException, IllegalArgumentException {
		super();

		if (folder.getAbsolutePath().length() >= 200 || folder.getName().length() >= 128)
			throw new IllegalArgumentException("Project path too long. T-Coffee won't work.");
		if (folder.getAbsolutePath().contains(" "))
			throw new IllegalArgumentException("Project path can't contain white spaces. T-Coffee won't work.");

		if (checkFasta)
			Project.checkFastaFile(originalFastaFile);

		this.folder = folder;
		this.deleted = false;

		if (this.folder.exists() && this.folder.listFiles().length != 0) {
			throw new IllegalArgumentException("Project folder is not empty (" + this.folder + ")");
		} else if (!this.folder.exists() && !this.folder.mkdirs()) {
			throw new IllegalArgumentException(
				"Project folder (" + folder.getAbsolutePath() + ") could not be created"
			);
		}

		this.propertiesFile = new File(folder, Project.CONFIGURATION_FILE);
		this.configuration = new Configuration(
			configuration,
			this.propertiesFile
		);

		this.fastaFile = new File(folder, Project.FILE_FASTA);
		this.namesFile = new File(folder, Project.FILE_NAMES);

		if (absoluteFasta) {
			this.originalFastaFile = originalFastaFile;
			this.configuration.setFastaFile(fastaFile.getAbsolutePath());
		} else {
			this.originalFastaFile = new File(folder, Project.FILE_FASTA);
			FileUtils.copyFile(originalFastaFile, this.originalFastaFile);
		}

		this.createNamesFile();

		this.configuration.storeProperties(this.propertiesFile);

		this.experiments = new ArrayList<ProjectExperiment>();
	}

	public Project(File folder) throws IOException, IllegalArgumentException {
		this.folder = folder;

		if (!this.folder.exists() || !this.folder.canRead()) {
			throw new IllegalArgumentException(
				"Project folder (" + folder.getAbsolutePath() + ") does not exist or can not be read"
			);
		}

		this.propertiesFile = new File(folder, Project.CONFIGURATION_FILE);
		if (!this.propertiesFile.exists() || !this.propertiesFile.canRead()) {
			throw new IllegalArgumentException(
				"Properties file (" + this.propertiesFile.getAbsolutePath() + ") does not exist or can not be read"
			);
		}
		this.configuration = new Configuration(
			Configuration.getSystemConfiguration(),
			this.propertiesFile
		);

		this.originalFastaFile = this.loadOriginalFastaFile();
		if (!this.originalFastaFile.exists() || !this.originalFastaFile.canRead()) {
			throw new IllegalArgumentException(
				"Original FASTA file (" + originalFastaFile.getAbsolutePath() + ") does not exist or can not be read"
			);
		}

		this.fastaFile = this.loadFastaFile();
		if (!this.fastaFile.exists() || !this.fastaFile.canRead()) {
			throw new IllegalArgumentException(
				"FASTA file (" + fastaFile.getAbsolutePath() + ") does not exist or can not be read"
			);
		}

		this.fastaFile = new File(folder, Project.FILE_FASTA);
		this.namesFile = this.loadNamesFile();
		if (!this.namesFile.exists() || !this.namesFile.canRead()) {
			throw new IllegalArgumentException(
				"Names file (" + namesFile.getAbsolutePath() + ") does not exist or can not be read"
			);
		}

		this.experiments = new ArrayList<ProjectExperiment>();

		final File[] folderFiles = this.folder.listFiles();
		Arrays.sort(folderFiles);

		for (File expFolder : folderFiles) {
			if (expFolder.isDirectory()) {
				new ProjectExperiment(this, expFolder);
			}
		}
	}

	private static void checkFastaFile(File fastaFile) throws IllegalArgumentException {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(fastaFile));

			String line = null;
			while ((line = br.readLine()) != null && !line.startsWith(">"))
				;

			String sequenceName = line.substring(1);
			String sequence = "";
			while (true) {
				line = br.readLine();

				if (line == null || line.startsWith(">")) {
					if (sequence.isEmpty()) {
						throw new IllegalArgumentException("Empty sequence: " + sequenceName);
					} else if (sequence.length() % 3 != 0) {
						throw new IllegalArgumentException("Sequence length must be multiple of 3: " + sequenceName);
					}

					if (line == null) {
						break;
					} else {
						sequenceName = line.substring(1);
						sequence = "";
					}
				} else {
					sequence += line.replaceAll("\\s", "");
				}
			}

		} catch (IOException ioe) {
			throw new IllegalArgumentException("Error reading input Fasta file: " + fastaFile.getAbsolutePath(), ioe);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException ioe) {}
		}
	}

	private void createNamesFile() {
		try {
			List<String> originalLines = FileUtils.readLines(this.originalFastaFile);
			List<String> fastaLines = new ArrayList<String>(), namesLines = new ArrayList<String>();

			int i = 1;
			for (String l : originalLines) {
				if (l.startsWith(">")) {
					fastaLines.add(">C" + i);
					namesLines.add(l.substring(1) + " - C" + i);
					++i;
				} else {
					fastaLines.add(l);
				}
			}
			FileUtils.writeLines(this.namesFile, namesLines);
			FileUtils.writeLines(this.fastaFile, fastaLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private File loadOriginalFastaFile() {
		final String originalFastaFile = this.configuration.getOriginalFastaFile();

		if (originalFastaFile == null || originalFastaFile.trim().isEmpty()) {
			return new File(this.folder, Project.FILE_FASTA);
		} else {
			return new File(originalFastaFile);
		}
	}

	private File loadFastaFile() {
		final String fastaFile = this.configuration.getFastaFile();

		if (fastaFile == null || fastaFile.trim().isEmpty()) {
			return new File(this.folder, Project.FILE_FASTA);
		} else {
			return new File(fastaFile);
		}
	}

	private File loadNamesFile() {
		final String namesFile = this.configuration.getNamesFile();

		if (namesFile == null || namesFile.trim().isEmpty()) {
			return new File(this.folder, Project.FILE_NAMES);
		} else {
			return new File(namesFile);
		}
	}

	public String getName() {
		return this.folder.getName();
	}

	@Override
	public Configuration getConfiguration() {
		return this.configuration;
	}

	@Clipboard(name = "Project folder", order = 1)
	public File getFolder() {
		return folder;
	}

	@ListElements(modifiable = true)
	public List<ProjectExperiment> getExperiments() {
		return experiments;
	}

	void addExperiment(ProjectExperiment experiment) {
		this.experiments.add(experiment);

		this.setChanged();
		this.notifyObservers(experiment);
	}

	void removeExperiment(ProjectExperiment experiment) {
		this.experiments.remove(experiment);

		this.setChanged();
		this.notifyObservers(experiment);
	}

	public File getFastaFile() {
		return this.fastaFile;
	}

	public File getNamesFile() {
		return this.namesFile;
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
}
