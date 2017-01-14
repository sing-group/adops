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

import java.io.IOException;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.datatypes.Project;
import es.uvigo.ei.sing.adops.datatypes.ProjectExperiment;

@Operation(name = "Create Experiment", description = "An experiment allows you to perform an analysis of a FASTA file.")
public class CreateExperiment {
	private String name;
	private String sequences;
	private Project project;

	@Port(name = "Project", order = 1, direction = Direction.INPUT, allowNull = false, description = "Project to which the experiment belongs")
	public void setProject(Project project) {
		this.project = project;
	}

	@Port(name = "Name", order = 2, direction = Direction.INPUT, allowNull = false, description = "Name of the experiment (must be unique for the project)")
	public void setName(String name) {
		this.name = name;
	}

	@Port(name = "Sequences", order = 3, direction = Direction.INPUT, allowNull = false, description = "Sequences to use (separated by white spaces)")
	public void setSequences(String sequences) {
		this.sequences = sequences;
	}

	@Port(direction = Direction.OUTPUT, order = 1000)
	public Experiment create() throws IOException, IllegalArgumentException {
		final ProjectExperiment experiment = new ProjectExperiment(this.project, this.name);
		
		experiment.getConfiguration().setInputSequences(this.sequences);
		experiment.storeAllProperties();
		
		return experiment;
	}

}
