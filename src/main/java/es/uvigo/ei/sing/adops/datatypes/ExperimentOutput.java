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
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.sing.adops.datatypes.AlignmentConfidences.Confidence;
import es.uvigo.ei.sing.adops.datatypes.fasta.Fasta;
import es.uvigo.ei.sing.adops.util.FastaUtils;

@Datatype(structure = Structure.COMPLEX, namingMethod = "getName")
public class ExperimentOutput extends AbstractOperationOutput {
	private static final String FILE_OUTPUT_ALIGNMENT = "aligned.fasta";
	private static final String FILE_OUTPUT_PROTEIN_ALIGNMENT_FASTA = "aligned.prot.fasta";
	private static final String FILE_OUTPUT_PROTEIN_ALIGNMENT_ALN = "aligned.prot.aln";
	private static final String FILE_OUTPUT_SCORE_ASCII = "aligned.score_ascii";
	private static final String FILE_OUTPUT_TREE = "tree.con";
	private static final String FILE_OUTPUT_LOG = "output.log";
	private static final String FILE_OUTPUT_SUMMARY = "output.sum";
	private static final String FILE_OUTPUT_PSRF = "mrbayes.log.psrf";
	private static final String FILE_OUTPUT_CODEML = "codeml.out";
	private static final String FILE_OUTPUT_CODEML_SUMMARY = "codeml.sum";
	public static final String FILE_OMEGAMAP_CODEML_SUMMARY = "omegamap.sum";

	private TCoffeeOutput tCoffeeOutput;
	private MrBayesOutput mrBayesOutput;
	private CodeMLOutput codeMLOutput;

	private boolean finished;

	private Experiment experiment;

	public ExperimentOutput(Experiment experiment) {
		super(0);

		this.experiment = experiment;

		this.finished = false;
		this.deleted = false;

		this.tCoffeeOutput = new TCoffeeOutput(
			experiment.getFastaFile(),
			experiment.getFilesFolder(),
			experiment.getConfiguration().getTCoffeeConfiguration().getAlignMethod()
		);
		this.mrBayesOutput = new MrBayesOutput(this.tCoffeeOutput.getAlignmentFile(), experiment.getFilesFolder());
		this.codeMLOutput = new CodeMLOutput(
			this.tCoffeeOutput.getAlignmentFile(), experiment.getFilesFolder(), 0
		);

		experiment.setResult(this);
	}

	public String getName() {
		return "Experiment Result";
	}

	public void setTCoffeeOutput(TCoffeeOutput tCoffeeOutput) {
		this.tCoffeeOutput = tCoffeeOutput;

		this.setChanged();
		this.notifyObservers();
	}

	public void setMrBayesOutput(MrBayesOutput mrBayesOutput) {
		this.mrBayesOutput = mrBayesOutput;

		this.setChanged();
		this.notifyObservers();
	}

	public void setCodeMLOutput(CodeMLOutput codeMLOutput) {
		this.codeMLOutput = codeMLOutput;

		this.setChanged();
		this.notifyObservers();
	}

	public void setFinished(boolean finished) {
		this.finished = finished;

		this.setChanged();
		this.notifyObservers();
	}

	public boolean isFinished() {
		return finished;
	}

	@Clipboard(name = "Log File", order = 1)
	public ConstantDatatype getLogFileData() {
		if (this.getLogFile().exists()) {
			return new ConstantDatatype("Log File", this.getLogFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "Alignment File", order = 2)
	public ConstantDatatype getRenamedAlignedFastaFileData() {
		if (this.getRenamedAlignedFastaFile().exists()) {
			return new ConstantDatatype("Alignment File", this.getRenamedAlignedFastaFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "Protein Alignment Fasta File", order = 3)
	public ConstantDatatype getRenamedAlignedProteinFastaFileData() {
		if (this.getRenamedAlignedProteinFastaFile().exists()) {
			return new ConstantDatatype("Protein Alignment Fasta File", this.getRenamedAlignedProteinFastaFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "Protein Alignment Aln File", order = 4)
	public ConstantDatatype getRenamedAlignedProteinAlnFileData() {
		if (this.getRenamedAlignedProteinAlnFile().exists()) {
			return new ConstantDatatype("Protein Alignment Aln File", this.getRenamedAlignedProteinAlnFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "PSRF File", order = 5)
	public ConstantDatatype getPsrfFileData() {
		if (this.getPsrfFile().exists()) {
			return new ConstantDatatype("PSRF File", this.getPsrfFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "MrBayes Consensus Tree", order = 6)
	public ConstantDatatype getRenamedTreeFileData() {
		if (this.getRenamedTreeFile().exists()) {
			return new ConstantDatatype("MrBayes Consensus Tree", this.getRenamedTreeFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "CodeML Summary", order = 7)
	public ConstantDatatype getCodeMLSummaryFileData() {
		if (this.getCodeMLSummaryFile().exists()) {
			return new ConstantDatatype("CodeML Summary", this.getCodeMLSummaryFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "CodeML Output File", order = 8)
	public ConstantDatatype getCodeMLOutputFileData() {
		if (this.getCodeMLOutputFile().exists()) {
			return new ConstantDatatype("CodeML Output File", this.getCodeMLOutputFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "omegaMap Summary", order = 9)
	public ConstantDatatype getOmegaMapSummaryFileData() {
		if (this.getOmegaMapSummaryFile().exists()) {
			return new ConstantDatatype("omegaMap Summary", this.getOmegaMapSummaryFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "Summary File", order = 10)
	public ConstantDatatype getSummaryFileData() {
		if (this.getSummaryFile().exists()) {
			return new ConstantDatatype("Summary File", this.getSummaryFile().getName());
		} else {
			return null;
		}
	}

	@Clipboard(name = "T-Coffee Output", order = 11)
	public ConstantDatatype getTCoffeeOutputData() {
		if (this.getTCoffeeOutput().isComplete()) {
			return new ConstantDatatype("T-Coffee Output", "OK");
		} else {
			return null;
		}
	}

	@Clipboard(name = "MrBayes Output", order = 12)
	public ConstantDatatype getMrBayesOutputData() {
		if (this.getMrBayesOutput().isComplete()) {
			return new ConstantDatatype("MrBayes Output", "OK");
		} else {
			return null;
		}
	}

	@Clipboard(name = "CodeML Output", order = 13)
	public ConstantDatatype getCodeMLOutputData() {
		if (this.getCodeMLOutput().isComplete()) {
			return new ConstantDatatype("CodeML Output", "OK");
		} else {
			return null;
		}
	}

	public File getLogFile() {
		// return this.logFile;
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_LOG);
	}

	public File getSummaryFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_SUMMARY);
	}

	public File getPsrfFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_PSRF);
	}

	public File getCodeMLSummaryFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_CODEML_SUMMARY);
	}

	public File getOmegaMapSummaryFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OMEGAMAP_CODEML_SUMMARY);
	}

	public File getCodeMLOutputFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_CODEML);
	}

	public TCoffeeOutput getTCoffeeOutput() {
		return this.tCoffeeOutput;
	}

	public MrBayesOutput getMrBayesOutput() {
		return this.mrBayesOutput;
	}

	public CodeMLOutput getCodeMLOutput() {
		return this.codeMLOutput;
	}

	public File getRenamedAlignedFastaFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_ALIGNMENT);
	}

	public File getRenamedAlignedProteinFastaFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_PROTEIN_ALIGNMENT_FASTA);
	}

	public File getRenamedAlignedProteinAlnFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_PROTEIN_ALIGNMENT_ALN);
	}

	public File getRenamedScoreAsciiFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_SCORE_ASCII);
	}

	public File getRenamedTreeFile() {
		return new File(this.experiment.getFolder(), ExperimentOutput.FILE_OUTPUT_TREE);
	}

	public AlignmentConfidences loadConfidences() throws IOException {
		if (
			this.getRenamedAlignedFastaFile().exists()
		) {
			final Fasta sequences = FastaUtils.loadSequences(
				this.getRenamedAlignedProteinFastaFile(), false
			);

			AlignmentConfidences confidences = new AlignmentConfidences(sequences);

			if (this.getCodeMLSummaryFile().exists()) {
				AlignmentConfidences codeMlConfidences = this.getCodeMLOutput().getConfidences(sequences, this.getCodeMLSummaryFile());
				for (String model : codeMlConfidences.getModels()) {
					confidences.addModel(model, codeMlConfidences.getModel(model));
				}
			}

			if (this.getOmegaMapSummaryFile().exists()) {
				Map<Integer, Confidence> omegaMapConfidences = OmegaMapOutput.getConfidences(sequences, this.getOmegaMapSummaryFile());
				if (!omegaMapConfidences.isEmpty()) {
					confidences.addModel("omegaMap", omegaMapConfidences);
				}
			}

			if (confidences.getModels() == null || confidences.getModels().isEmpty()) {
				return null;
			} else {
				return confidences;
			}
		} else {
			return null;
		}
	}

	public int[] loadScores() throws IOException {
		if (this.getRenamedScoreAsciiFile().exists()) {
			final List<String> lines = FileUtils.readLines(this.getRenamedScoreAsciiFile());

			String score = "";
			for (String line : lines) {
				if (line.matches("c[o-]n?\\s+([0-9]|-)+")) {
					score += line.split("\\s+")[1];
				}
			}

			final int scores[] = new int[score.length()];
			for (int i = 0; i < score.length(); i++) {
				try {
					scores[i] = Integer.valueOf(Character.toString(score.charAt(i)));
				} catch (NumberFormatException nfe) {
					scores[i] = -1;
				}
			}

			return scores;
		} else {
			return null;
		}
	}
}
