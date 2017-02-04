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
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.sing.adops.configuration.Configuration;
import es.uvigo.ei.sing.adops.util.Utils;

@Datatype(structure = Structure.COMPLEX, namingMethod = "getName")
public class ProjectExperiment extends Observable implements Experiment, Observer {
	private static final Logger LOG = Logger.getLogger(ProjectExperiment.class);

	private static final String DIRECTORY_ALLFILES = "allfiles";
	private static final String FILE_EXPERIMENT_CONF = "experiment.conf";
	private static final String FILE_EXPERIMENT_NOTES = "notes.txt";

	private final String name;
	private boolean deleted = false;
	private boolean running = false;

	private final Project project;
	private final File folder, filesFolder;
	private final File fastaFile, namesFile;

	private final Configuration configuration;
	private final File propertiesFile;
	private String notes;

	private String warnings;

	private ExperimentOutput result;

	public ProjectExperiment(Project project, File experimentDir) throws IOException {
		super();

		this.project = project;
		this.name = experimentDir.getName();
		this.warnings = "";

		this.folder = experimentDir;
		this.filesFolder = new File(this.folder, ProjectExperiment.DIRECTORY_ALLFILES);

		if (!this.filesFolder.exists()) {
			throw new IllegalArgumentException(
				"Experiment folder (" + this.folder.getAbsolutePath() + ") does not exist"
			);
		}

		this.propertiesFile = new File(this.folder, ProjectExperiment.FILE_EXPERIMENT_CONF);
		this.configuration = new Configuration(
			project.getConfiguration(),
			this.propertiesFile
		);

		this.fastaFile = new File(folder, this.project.getFastaFile().getName());
		this.namesFile = new File(folder, this.project.getNamesFile().getName());

		if (this.getNotesFile().exists()) {
			this.notes = FileUtils.readFileToString(this.getNotesFile());
		} else {
			this.getNotesFile().createNewFile();
			this.notes = "";
		}

		this.loadResult();

		project.addExperiment(this);
	}

	public ProjectExperiment(Project project, String name) throws IOException, IllegalArgumentException {
		super();
		this.folder = new File(project.getFolder(), name);

		if (folder.getAbsolutePath().length() >= 264 || name.length() >= 128)
			throw new IllegalArgumentException("Experiment path too long. T-Coffee won't work.");
		if (folder.getAbsolutePath().contains(" "))
			throw new IllegalArgumentException("Experiment path can't contain white spaces. T-Coffee won't work.");

		if (this.folder.exists() && this.folder.listFiles().length != 0)
			throw new IllegalArgumentException("Experiment folder is not empty (" + this.folder + ")");

		this.project = project;
		this.warnings = "";

		name = name.trim();
		if (name.isEmpty())
			throw new IllegalArgumentException("name");
		this.name = name;

		this.filesFolder = new File(this.folder, ProjectExperiment.DIRECTORY_ALLFILES);

		if (!this.filesFolder.exists() && !this.filesFolder.mkdirs()) {
			throw new IllegalArgumentException(
				"Experiment folder (" + this.folder.getAbsolutePath() + ") could not be created"
			);
		}

		this.propertiesFile = new File(this.folder, ProjectExperiment.FILE_EXPERIMENT_CONF);
		this.configuration = new Configuration(
			project.getConfiguration(),
			this.propertiesFile
		);

		this.fastaFile = new File(folder, this.project.getFastaFile().getName());
		this.namesFile = new File(folder, this.project.getNamesFile().getName());

		if (this.getNotesFile().exists()) {
			this.notes = FileUtils.readFileToString(this.getNotesFile());
		} else {
			this.getNotesFile().createNewFile();
			this.notes = "";
		}

		this.storeAllProperties();

		project.addExperiment(this);
	}

	private void loadResult() {
		if (!this.isClean())
			new ExperimentOutput(this);
	}

	@Override
	public void deleteResult() {
		if (this.hasResult())
			this.getResult().delete(); // Will be deleted on the update method.

		try {
			if (this.filesFolder.isDirectory())
				FileUtils.cleanDirectory(this.filesFolder);
		} catch (IOException e) {}

		this.result = null;

		this.setChanged();
		this.notifyObservers();
	}

	public String getName() {
		return this.name;
	}

	private String checkFastaFile() throws IllegalArgumentException {
		final Set<Character> aminos = new HashSet<Character>(Arrays.asList('a', 'c', 't', 'g', 'A', 'C', 'T', 'G', '-'));
		BufferedReader br = null;

		try {
			final StringBuilder sb = new StringBuilder();
			final LinkedHashMap<String, StringBuilder> replacements = new LinkedHashMap<String, StringBuilder>();

			br = new BufferedReader(new FileReader(this.fastaFile));

			String line = null;
			while ((line = br.readLine()) != null && !line.startsWith(">")) {
				sb.append(line).append('\n');
			}

			String seqId = null;
			String seq = null;
			while (line != null) {
				seqId = line;
				seq = "";
				while ((line = br.readLine()) != null && !line.startsWith(">")) {
					seq += line;
				}

				// Non ACTG characters replacement
				char[] charSequence = seq.toCharArray();
				String data = "";
				for (int i = 0; i < charSequence.length; i++) {
					if (aminos.contains(charSequence[i])) {
						data += charSequence[i];
					} else {
						if (replacements.containsKey(seqId)) {
							replacements.get(seqId).append(String.format(", [%d,%c]", i + 1, charSequence[i]));
						} else {
							replacements.put(seqId, new StringBuilder(String.format("[%d,%c]", i + 1, charSequence[i])));
						}

						data += '-';
					}
				}

				// Incomplete codons replacement
				charSequence = data.toCharArray();
				data = "";
				String codon = "";
				for (int i = 0; i < charSequence.length; i++) {
					codon += Character.toString(charSequence[i]);
					if ((i + 1) % 3 == 0) {
						if (codon.contains("-")) {
							data += "---";
							if (replacements.containsKey(seqId)) {
								replacements.get(seqId).append(String.format(", [%s,---]", codon));
							} else {
								replacements.put(seqId, new StringBuilder(String.format("[%s,---]", codon)));
							}
						} else {
							data += codon;

						}

						codon = "";
					}
				}

				sb.append(seqId).append('\n');
				sb.append(data).append('\n');
			}

			FileUtils.write(this.fastaFile, sb.toString());

			if (replacements.isEmpty()) {
				return "";
			} else {
				final StringBuilder summary = new StringBuilder("Replacements done on input file\n");
				for (Map.Entry<String, StringBuilder> replacement : replacements.entrySet()) {
					summary.append(replacement.getKey()).append('\n');
					summary.append(replacement.getValue()).append('\n');
				}

				summary.append("\n-----\n");

				return summary.toString();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Input file is not a valida Fasta file");
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {}
			}
		}
	}

	@Clipboard(name = "Experiment folder", order = 1)
	public ConstantDatatype getFolderData() {
		return new ConstantDatatype("Experiment folder", this.getFolder().getName());
	}

	@Override
	public File getFolder() {
		return this.folder;
	}

	@Override
	public File getFilesFolder() {
		return this.filesFolder;
	}

	@Override
	@Clipboard(name = "Result", order = 2)
	public ExperimentOutput getResult() {
		return this.result;
	}

	@Override
	public String getWarnings() {
		return this.warnings;
	}

	@Override
	public boolean hasResult() {
		return this.result != null;
	}

	public void setResult(ExperimentOutput result) {
		if (this.result != null)
			this.result.deleteObserver(this);

		this.result = result;
		this.result.addObserver(this);

		this.setChanged();
		this.notifyObservers(result);
	}

	@Override
	public boolean isClean() {
		if (this.hasResult()) {
			return false;
		} else {
			final FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (
						pathname.equals(ProjectExperiment.this.getNotesFile()) ||
							pathname.equals(ProjectExperiment.this.getPropertiesFile()) ||
							pathname.equals(ProjectExperiment.this.getFastaFile()) ||
							pathname.equals(ProjectExperiment.this.getNamesFile())
					) {
						return false;
					} else if (pathname.equals(ProjectExperiment.this.getFilesFolder())) {
						final List<File> files = Arrays.asList(pathname.listFiles());

						int countOutputFolders = 0;
						if (files.contains(new File(pathname, TCoffeeOutput.OUTPUT_FOLDER_NAME)))
							countOutputFolders++;
						if (files.contains(new File(pathname, MrBayesOutput.OUTPUT_FOLDER_NAME)))
							countOutputFolders++;
						if (files.contains(new File(pathname, CodeMLOutput.OUTPUT_FOLDER_NAME)))
							countOutputFolders++;

						return files.size() != countOutputFolders;
					}

					return true;
				}
			};

			return this.getFolder().listFiles(filter).length == 0;
		}
	}

	@Override
	public void clear() {
		final FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (
					pathname.equals(ProjectExperiment.this.getNotesFile()) ||
						pathname.equals(ProjectExperiment.this.getPropertiesFile()) ||
						pathname.equals(ProjectExperiment.this.getFastaFile()) ||
						pathname.equals(ProjectExperiment.this.getNamesFile()) ||
						pathname.equals(ProjectExperiment.this.getFilesFolder())
				) {
					return false;
				}

				return true;
			}
		};

		for (File file : this.getFolder().listFiles(filter)) {
			if (file.isFile())
				file.delete();
			else if (file.isDirectory())
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {}
		}

		try {
			FileUtils.cleanDirectory(this.getFilesFolder());
		} catch (IOException e) {}

		new File(this.getFilesFolder(), TCoffeeOutput.OUTPUT_FOLDER_NAME).mkdirs();
		new File(this.getFilesFolder(), MrBayesOutput.OUTPUT_FOLDER_NAME).mkdirs();
		new File(this.getFilesFolder(), CodeMLOutput.OUTPUT_FOLDER_NAME).mkdirs();

		this.result = null;

		this.setChanged();
		this.notifyObservers();
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void setRunning(boolean running) {
		this.running = running;

		this.setChanged();
		this.notifyObservers();
	}

	@Override
	public Configuration getConfiguration() {
		return this.configuration;
	}

	@Override
	public File getFastaFile() {
		return this.fastaFile;
	}

	@Override
	public File getNamesFile() {
		return this.namesFile;
	}

	@Override
	public Map<String, String> getNames() {
		final Map<String, String> names = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(this.getNamesFile()))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				final String[] splits = line.split(" - ");
				names.put(splits[1].trim(), splits[0].trim());
			}
		} catch (IOException e) {
			LOG.error("Error retrieving experiment names", e);
		}

		return names;
	}

	@Override
	public File getPropertiesFile() {
		return this.propertiesFile;
	}

	public void storeAllProperties() {
		Utils.storeProperties(this.configuration.toProperties(true), this.getPropertiesFile(), ProjectExperiment.LOG);
	}

	@Override
	public String getNotes() {
		return this.notes;
	}

	public Project getProject() {
		return this.project;
	}

	@Override
	public void setNotes(String notes) {
		this.notes = notes;

		try {
			FileUtils.writeStringToFile(this.getNotesFile(), notes);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't not store notes", e);
		}
	}

	@Override
	public File getNotesFile() {
		return new File(this.folder, ProjectExperiment.FILE_EXPERIMENT_NOTES);
	}

	@Override
	public void delete() {
		try {
			FileUtils.deleteDirectory(this.folder);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't not delete project's directory", e);
		}
		
		this.deleted = true;
		this.project.removeExperiment(this);
		this.setChanged();
		this.notifyObservers();
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	@Override
	public File getSourceFastaFile() {
		return project.getFastaFile();
	}

	@Override
	public File getSourceNamesFile() {
		return project.getNamesFile();
	}

	public List<String> listSequenceName() {
		final List<String> names = new ArrayList<String>();

		try {
			final List<String> lines = FileUtils.readLines(this.getSourceNamesFile());

			for (String line : lines) {
				final String[] split = line.split(" - ");

				if (split.length == 2) {
					names.add(split[0].trim());
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return names;
	}

	public List<String> listSelectedSequenceName() {
		final String sequences = this.configuration.getInputSequences();

		if (sequences.trim().isEmpty()) {
			return this.listSequenceName();
		} else {
			final Set<Integer> indexes = new HashSet<Integer>();
			for (String index : sequences.split("\\s")) {
				indexes.add(Integer.parseInt(index));
			}

			final List<String> names = new ArrayList<String>();
			try {
				final List<String> lines = FileUtils.readLines(this.getSourceNamesFile());

				int index = 1;
				for (String line : lines) {
					if (indexes.contains(index++)) {
						final String[] split = line.split(" - ");

						if (split.length == 2) {
							names.add(split[0].trim());
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return names;
		}
	}

	@Override
	public void generateInputFiles() {
		final String sequences = this.configuration.getProperty(Configuration.PROPERTY_INPUT_SEQUENCES);

		if (sequences.isEmpty()) {
			try {
				FileUtils.copyFile(this.project.getFastaFile(), this.fastaFile);
				FileUtils.copyFile(this.project.getNamesFile(), this.namesFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			final Set<Integer> seqNumbers = new TreeSet<>();
			String newFasta = "", newNames = "";

			for (String i : sequences.split(" "))
				seqNumbers.add(Integer.parseInt(i));

			try (
				BufferedReader fastaBR = new BufferedReader(new FileReader(this.project.getFastaFile()));
				BufferedReader namesBR = new BufferedReader(new FileReader(this.project.getNamesFile()))
			) {

				int j = 0;
				Iterator<Integer> iter = seqNumbers.iterator();
				int i = iter.next();

				String sequence = null;
				String line;
				do {
					line = fastaBR.readLine();
					if (line == null || line.startsWith(">")) {
						if (sequence != null) {
							++j;
							String seqName = namesBR.readLine();
							if (j == i) {
								newFasta += sequence + '\n';
								newNames += seqName + '\n';
								if (!iter.hasNext())
									break;
								i = iter.next();
							}
						}
						sequence = line;
					} else {
						sequence += '\n' + line;
					}
				} while (line != null);
				
				FileUtils.write(this.fastaFile, newFasta);
				FileUtils.write(this.namesFile, newNames);
			} catch (IOException e) {
				throw new RuntimeException("Error generating input files", e);
			}
		}

		final String summary = this.checkFastaFile();
		if (!summary.isEmpty()) {
			if (this.warnings.isEmpty()) {
				this.warnings = summary;
			} else {
				this.warnings += "\n\n" + summary;
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof ExperimentOutput) {
			final ExperimentOutput output = (ExperimentOutput) o;

			if (output.isDeleted()) {
				output.deleteObserver(this);
				this.result = null;
			} else if (output.isFinished() && output.isComplete()) {
				this.storeAllProperties();
			}

			this.setChanged();
			this.notifyObservers(output);
		}
	}
}
