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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.sing.adops.datatypes.AlignmentConfidences.Confidence;
import es.uvigo.ei.sing.adops.datatypes.fasta.Fasta;

@Datatype(structure = Structure.COMPLEX)
public class CodeMLOutput extends AbstractOperationOutput {
	public static final String OUTPUT_FOLDER_NAME = "codeml";

	private final File inputFile;
	private final File outputFolder;

	public CodeMLOutput(File inputFile, File outputFolder) {
		this(inputFile, outputFolder, -1);
	}

	public CodeMLOutput(File inputFile, File outputFolder, int state) {
		super(state);

		this.inputFile = inputFile;
		this.outputFolder = new File(outputFolder, CodeMLOutput.OUTPUT_FOLDER_NAME);
		this.outputFolder.mkdirs();
	}

	public File getNexusFile() {
		return new File(this.outputFolder, this.inputFile.getName() + ".pnxs");
	}

	public File getTreeFile() {
		return new File(this.outputFolder, this.inputFile.getName() + ".tree");
	}

	public File getCodeMLCtlFile() {
		return new File(this.outputFolder, "codeml.ctl");
	}

	@Clipboard(name = "output File", order = 1)
	public File getOutputFile() {
		return new File(this.outputFolder, this.inputFile.getName() + ".out");
	}

	@Clipboard(name = "summary File", order = 2)
	public File getSummaryFile() {
		return new File(this.outputFolder, this.inputFile.getName() + ".out.sum");
	}

	@Clipboard(name = "lnf File", order = 3)
	public File getLnfFile() {
		return new File(this.outputFolder, "lnf");
	}

	@Clipboard(name = "rst File", order = 4)
	public File getRstFile() {
		return new File(this.outputFolder, "rst");
	}

	@Clipboard(name = "rst1 File", order = 5)
	public File getRst1File() {
		return new File(this.outputFolder, "rst1");
	}

	@Clipboard(name = "rub File", order = 6)
	public File getRubFile() {
		return new File(this.outputFolder, "rub");
	}

	@Clipboard(name = "2NG.t File", order = 7)
	public File get2ngtFile() {
		return new File(this.outputFolder, "2NG.t");
	}

	@Clipboard(name = "2NG.dN File", order = 8)
	public File get2ngdnFile() {
		return new File(this.outputFolder, "2NG.dN");
	}

	@Clipboard(name = "2NG.dS File", order = 9)
	public File get2ngdsFile() {
		return new File(this.outputFolder, "2NG.dS");
	}

	@Clipboard(name = "Log File", order = 10)
	public File getLogFile() {
		return new File(this.outputFolder, "codeml.log");
	}

	@Override
	public File[] getResultFiles() {
		return new File[] {
			this.getOutputFile(),
			this.getSummaryFile(),
			this.getLnfFile(),
			this.getRstFile(),
			this.getRst1File(),
			this.getRubFile(),
			this.get2ngtFile(),
			this.get2ngdnFile(),
			this.get2ngdsFile(),
			this.getLogFile()
		};
	}

	public AlignmentConfidences getConfidences(Fasta sequences) throws IOException {
		return this.getConfidences(sequences, this.getSummaryFile());
	}

	public AlignmentConfidences getConfidences(Fasta sequences, File summaryFile) throws IOException {
		final AlignmentConfidences confidences = new AlignmentConfidences(sequences);

		final Collection<String> seqs = sequences.getSequencesChain();
		final Map<Integer, Integer> movedIndexes = new HashMap<>();
		final int seqLength = seqs.iterator().next().length();

		for (int i = 0, dashIndex = 0; dashIndex < seqLength; i++, dashIndex++) {
			boolean increased;
			do {
				increased = false;

				for (String seq : seqs) {
					final char seqChar = seq.charAt(dashIndex);

					if (seqChar == '-' || seqChar == 'o') {
						dashIndex++;
						increased = true;
						break;
					}
				}

			} while (increased && dashIndex < seqLength);

			if (dashIndex < seqLength)
				movedIndexes.put(i + 1, dashIndex + 1);
		}

		try (BufferedReader br = new BufferedReader(new FileReader(summaryFile))) {
			String model = null;
			String line = null;

			Map<Integer, Float> bebs = null;
			Map<Integer, Float> nebs = null;
			
			while ((line = br.readLine()) != null) {
				if (line.matches("Model [028] vs [17]\t\\p{Digit}+\\.\\p{Digit}+")) {
					if (line.startsWith("Model 0")) {
						model = "Model 0: one-ratio";
					} else if (line.startsWith("Model 2")) {
						model = "Model 2: PositiveSelection";
					} else if (line.startsWith("Model 8")) {
						model = "Model 8: beta&w>1";
					} else {
						model = null;
					}
					bebs = nebs = null;
				} else if (model != null && line.startsWith("Naive Empirical Bayes")) {
					nebs = new HashMap<>();
					bebs = null;

					int skipLines = 0;
					while ((line = br.readLine()) != null && skipLines++ < 4);
					
					while ((line = br.readLine()) != null && !line.trim().isEmpty()) {
						String[] splits = line.trim().split("\\s+");
						nebs.put(Integer.valueOf(splits[0]), Float.valueOf(splits[2].replaceAll("\\*", "")));
					}
				} else if (model != null && line.startsWith("Bayes Empirical Bayes")) {
					bebs = new HashMap<>();

					int skipLines = 0;
					while ((line = br.readLine()) != null && skipLines++ < 4);
					
					while ((line = br.readLine()) != null && !line.trim().isEmpty()) {
						String[] splits = line.trim().split("\\s+");
						bebs.put(Integer.valueOf(splits[0]), Float.valueOf(splits[2].replaceAll("\\*", "")));
					}
				}
				
				if (model != null && nebs != null && bebs != null) {
					final Map<Integer, Confidence> modelConfidences = new HashMap<>();
					for (Integer i : nebs.keySet()) {
						if (bebs.containsKey(i)) {
							modelConfidences.put(movedIndexes.get(i), new Confidence(bebs.get(i), nebs.get(i)));
						}
					}
					if (!modelConfidences.isEmpty())
						confidences.addModel(model, modelConfidences);

					nebs = bebs = null;
					model = null;
				}
			}
		}

		return confidences;
	}
}
