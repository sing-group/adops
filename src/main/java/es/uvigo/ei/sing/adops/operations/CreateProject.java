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
package es.uvigo.ei.sing.adops.operations;

import java.io.File;
import java.io.IOException;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.adops.datatypes.Project;

@Operation(name = "Create Project", description = "A project allows you to run several experiments on a FASTA file.")
public class CreateProject {
	private File fastaFile;
	private boolean copyFastaFile;
	private File folder;

	@Port(name = "Project Folder", order = 1, direction = Direction.INPUT, allowNull = false, description = "Folder that will contain the project files.")
	public void setFolder(File folder) {
		this.folder = folder;
	}

	@Port(name = "FASTA File", order = 2, direction = Direction.INPUT, allowNull = false, description = "Input FASTA file for the experiments")
	public void setFastaFile(File fastaFile) {
		this.fastaFile = fastaFile;
	}

	@Port(name = "Copy FASTA File", order = 3, direction = Direction.INPUT, defaultValue = "true", allowNull = false, description = "Whether to copy the FASTA file into the project folder")
	public void setCopyFastaFile(boolean copyFastaFile) {
		this.copyFastaFile = copyFastaFile;
	}

	@Port(direction = Direction.OUTPUT, order = 1000)
	public Project create() throws IllegalArgumentException, IOException {
		return new Project(this.folder, this.fastaFile, !this.copyFastaFile);
	}
	
}
