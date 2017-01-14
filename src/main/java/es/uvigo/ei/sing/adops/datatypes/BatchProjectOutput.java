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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class BatchProjectOutput extends Observable implements Observer {
	private final static Logger LOG = Logger.getLogger(BatchProjectOutput.class);
	
	private static final String STATUS_FILE = "status.txt";

	private final BatchProject project;

	private final Map<File, Project> projects;
	private final Map<Project, ExperimentOutput> projectOutputs;
	private final Map<Project, Throwable> projectErrors;

	public BatchProjectOutput(BatchProject project) {
		super();
		this.project = project;

		this.projects = new LinkedHashMap<>();
		this.projectOutputs = new HashMap<>();
		this.projectErrors = new HashMap<>();

		try {
			this.load();
		} catch (IOException e) {
			LOG.error("Error loading batch project output", e);
			
			throw new RuntimeException("Error loading batch project output", e);
		}
	}

	private int getProjectIndex(Project project) {
		final File[] projectDirectories = this.project.getProjectDirectories();

		for (int i = 0; i < projectDirectories.length; i++) {
			if (projectDirectories[i].equals(project.getFolder()))
				return i;
		}

		return -1;
	}

	private void load() throws IOException {
		final File statusFile = new File(this.project.getFolder(), BatchProjectOutput.STATUS_FILE);
		if (!statusFile.exists())
			return;

		synchronized (this.projects) {
			for (String line : FileUtils.readLines(statusFile)) {
				final String projectName = line.substring(0, line.indexOf('='));
				final File projectDir = new File(this.project.getFolder(), projectName);
				final String status = line.substring(line.indexOf('=') + 1);

				if (projectDir.exists()) {
					final Project project = new Project(projectDir);

					this.projects.put(this.project.getFastaFiles()[this.getProjectIndex(project)], project);

					if (status.startsWith("Executed")) {
						this.projectOutputs.put(project, project.getExperiments().get(0).getResult());
					} else if (status.startsWith("Error")) {
						this.projectErrors.put(project, new Exception(status.substring(6, status.length() - 1)));
					}
				}
			}
		}
	}

	private void store() throws IOException {
		synchronized (this.projects) {
			final File summary = new File(this.project.getFolder(), BatchProjectOutput.STATUS_FILE);
			final List<String> lines = new LinkedList<>();

			final File[] fastaFiles = this.project.getFastaFiles();
			for (int i = 0; i < fastaFiles.length; i++) {
				final File projectDir = this.project.getProjectDirectories()[i];
				final Project project = this.projects.get(fastaFiles[i]);

				if (project == null) {
					lines.add(projectDir.getName() + "=Unexecuted");
				} else if (this.projectOutputs.containsKey(project)) {
					lines.add(projectDir.getName() + "=Executed");
				} else if (this.projectErrors.containsKey(project)) {
					lines.add(projectDir.getName() + "=Error[" + this.projectErrors.get(project).getMessage() + "]");
				} else {
					lines.add(projectDir.getName() + "=Unexecuted");
				}
			}

			FileUtils.writeLines(summary, lines);
		}
	}

	public int numProjects() {
		return this.project.getFastaFiles().length;
	}

	public Project getProject(int projectIndex) throws IllegalArgumentException, IOException {
		if (projectIndex < 0 || projectIndex > this.numProjects())
			throw new IndexOutOfBoundsException("Illegal project index: " + projectIndex);

		synchronized (this.projects) {
			final File fastaFile = this.project.getFastaFiles()[projectIndex];

			if (!this.projects.containsKey(fastaFile)) {
				final File projectDir = this.project.getProjectDirectories()[projectIndex];

				try {
					final Project project = new Project(this.project.getConfiguration(), projectDir, fastaFile, true);
					this.projects.put(fastaFile, project);
					project.addObserver(this);
				} catch (Exception e) {
					final Project project = new Project(this.project.getConfiguration(), projectDir, fastaFile, true, false);
					this.projects.put(fastaFile, project);
					this.projectErrors.put(project, e);
				}

				this.store();
			}

			return this.projects.get(fastaFile);
		}
	}

	public boolean isFinished() {
		synchronized (this.projects) {
			final Set<Project> projects = new HashSet<>(this.projects.values());
			
			projects.removeAll(this.projectErrors.keySet());
			projects.removeAll(this.projectOutputs.keySet());

			return projects.isEmpty();
		}
	}

	public boolean hasResults() {
		return !(this.projectOutputs.isEmpty() && this.projectErrors.isEmpty());
	}

	public boolean hasResult(Project project) {
		return this.projectOutputs.containsKey(project) || this.projectErrors.containsKey(project);
	}

	public boolean isReady(Project project) {
		return !this.hasResult(project);
	}

	public boolean isRunning(Project project) {
		return !project.getExperiments().isEmpty() &&
			project.getExperiments().get(0).isRunning();
	}

	public boolean isFinished(Project project) {
		return this.projectOutputs.containsKey(project);
	}

	public boolean isError(Project project) {
		return this.projectErrors.containsKey(project);
	}

	public void addResult(Project project, ExperimentOutput result) {
		this.projectOutputs.put(project, result);

		try {
			this.store();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.setChanged();
		this.notifyObservers(project);

	}

	public void addResult(Project project, Throwable exception) {
		this.projectErrors.put(project, exception);

		try {
			this.store();
		} catch (IOException e) {
			LOG.error("Error storing result in batch project", e);
			
			throw new RuntimeException("Error storing result in batch project", e);
		}

		this.setChanged();
		this.notifyObservers(project);
	}

	public ExperimentOutput getProjectOutput(Project project) {
		return this.projectOutputs.get(project);
	}

	public Throwable getProjectError(Project project) {
		return this.projectErrors.get(project);
	}

	public void clear() {
		synchronized (this.projects) {
			for (File file : this.projects.keySet()) {
				final Project project = this.projects.remove(file);
				try {
					project.delete();
					project.deleteObserver(this);
				} catch (IOException e) {
					LOG.error("Error deleting project in batch project", e);
				}
			}

			this.projects.clear();
			this.projectOutputs.clear();
			this.projectErrors.clear();

			try {
				this.store();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		this.setChanged();
		this.notifyObservers(arg);
	}

	public Collection<Project> getCreatedProjects() {
		return this.projects.values();
	}
}
