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

import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.COMPLEX)
public class MrBayesOutput extends AbstractOperationOutput {
	public static final String OUTPUT_FOLDER_NAME = "mrbayes";

	private final File inputFile;
	private final File outputFolder;

	public MrBayesOutput(File inputFile, File outputFolder) {
		this(inputFile, outputFolder, -1);
	}

	public MrBayesOutput(File inputFile, File outputFolder, int state) {
		super(state);

		this.inputFile = inputFile;
		this.outputFolder = new File(outputFolder, MrBayesOutput.OUTPUT_FOLDER_NAME);
		this.outputFolder.mkdirs();
	}

	public File getInputFile() {
		return this.inputFile;
	}

	public File getNexusFile() {
		return new File(this.outputFolder, this.inputFile.getName() + ".mnxs");
	}

	public File getMrBayesFile() {
		return new File(this.outputFolder, this.inputFile.getName() + ".mrb");
	}

	@Clipboard(name = ".con File", order = 1)
	public File getConFile() {
		return new File(this.getMrBayesFile().getAbsolutePath() + ".con");
	}

	@Clipboard(name = ".mcmc File", order = 2)
	public File getMcmcFile() {
		return new File(this.getMrBayesFile().getAbsolutePath() + ".mcmc");
	}

	@Clipboard(name = ".parts File", order = 3)
	public File getPartsFile() {
		return new File(this.getMrBayesFile().getAbsolutePath() + ".parts");
	}

	@Clipboard(name = ".run1.p File", order = 4)
	public File getRun1pFile() {
		return new File(this.getMrBayesFile().getAbsolutePath() + ".run1.p");
	}

	@Clipboard(name = ".run1.t File", order = 5)
	public File getRun1tFile() {
		return new File(this.getMrBayesFile().getAbsolutePath() + ".run1.t");
	}

	@Clipboard(name = ".run2.p File", order = 6)
	public File getRun2pFile() {
		return new File(this.getMrBayesFile().getAbsolutePath() + ".run2.p");
	}

	@Clipboard(name = ".run2.t File", order = 7)
	public File getRun2tFile() {
		return new File(this.getMrBayesFile().getAbsolutePath() + ".run2.t");
	}

	@Clipboard(name = ".trprobs File", order = 8)
	public File getTrprobsFile() {
		return new File(this.getMrBayesFile().getAbsolutePath() + ".trprobs");
	}

	@Clipboard(name = "PSRF File", order = 9)
	public File getPsrfFile() {
		return new File(this.getMrBayesFile().getParent(), "mrbayes.log.psrf");
	}

	@Clipboard(name = "Log File", order = 10)
	public File getLogFile() {
		return new File(this.getMrBayesFile().getParent(), "mrbayes.log");
	}

}
