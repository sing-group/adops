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
package es.uvigo.ei.sing.adops.operations;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.datatypes.ProjectExperiment;

@Operation(name = "Copy Experiment", description = "Creates a new experiment from another one within the same project")
public class CopyExperiment {
	private ProjectExperiment experiment;
	private String name;

	@Port(name = "Experiment", order = 1, direction = Direction.INPUT, allowNull = false, description = "Source Experiment")
	public void setExperiment(ProjectExperiment experiment) {
		this.experiment = experiment;
	}

	@Port(name = "Name", order = 2, direction = Direction.INPUT, allowNull = false, description = "New Name")
	public void setName(String name) {
		this.name = name;
	}

	@Port(order = 3, direction = Direction.OUTPUT)
	public Experiment copyExperiment() throws IOException, IllegalArgumentException {
		final File newFolder = new File(this.experiment.getFolder().getParentFile(), this.name);

		if (newFolder.exists())
			throw new FileExistsException("New experiment folder already exists (" + newFolder + ")");

		if (!(new File(newFolder, this.experiment.getFilesFolder().getName()).mkdirs()))
			throw new IOException("Error writing in new folder (" + newFolder + ")");

		FileUtils.copyFile(
			this.experiment.getPropertiesFile(),
			new File(newFolder, this.experiment.getPropertiesFile().getName())
		);
		FileUtils.copyFile(
			this.experiment.getNotesFile(),
			new File(newFolder, this.experiment.getNotesFile().getName())
		);

		return new ProjectExperiment(this.experiment.getProject(), newFolder);
	}

}
