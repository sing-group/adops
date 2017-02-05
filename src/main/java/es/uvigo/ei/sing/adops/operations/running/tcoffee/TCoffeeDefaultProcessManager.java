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
package es.uvigo.ei.sing.adops.operations.running.tcoffee;

import static java.lang.Math.max;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.sing.adops.configuration.TCoffeeConfiguration;
import es.uvigo.ei.sing.adops.operations.running.FileFormatException;
import es.uvigo.ei.sing.adops.operations.running.OperationException;
import es.uvigo.ei.sing.adops.util.IOUtils;
import es.uvigo.ei.sing.adops.util.Utils;

public class TCoffeeDefaultProcessManager extends TCoffeeProcessManager {
	public TCoffeeDefaultProcessManager(TCoffeeConfiguration configuration)
		throws OperationException {
		super(configuration);
	}

	@Override
	public boolean isCompatibleWith(String version) {
		return true;
	}

	@Override
	public int runAlignment(File fastaFile, AlignMethod alignMethod, File outputFile)
		throws OperationException {
		final String tcoffeeParams = String.format(
			"%s %s -run_name %s. -cache=no",
			shortenPath(fastaFile.getAbsolutePath(), outputFile.getParentFile()),
			alignMethod.getTCoffeeString(),
			shortenPath(fastaFile.getAbsolutePath(), outputFile.getParentFile())
		);

		return this.runTCoffee(tcoffeeParams, outputFile, true);
	}

	public int evaluateAlignment(File alignmentFile, File outputFile) throws OperationException {
		final String tCoffeeParams = String.format(
			"-infile=%s -output=score_ascii -special_mode=evaluate -evaluate_mode=t_coffee_fast",
			shortenPath(alignmentFile.getAbsolutePath(), outputFile.getParentFile())
		);

		return this.runTCoffee(tCoffeeParams, outputFile, true);
	}

	@Override
	public InformativePositions computeInformativePositions(File alignmentFile, File scoreFile, File ipiIFile, File bsAlignmentFile, File ipiBSFile, File outputFile, int minScore) throws OperationException {
		// Compute I
		final int i = this.calculateI(alignmentFile, ipiIFile, outputFile);
		try {
			IOUtils.checkIfFileIsEmpty(ipiIFile);
		} catch (FileFormatException e) {
			throw new OperationException(this.getLastCommand(), e.getMessage(), e);
		}
		
		// Compute S
		final int s = this.calculateS(scoreFile, outputFile);

		// Compute BS
		final int bs = this.calculateBS(alignmentFile, bsAlignmentFile, scoreFile, ipiBSFile, outputFile, minScore);
		try {
			IOUtils.checkIfFileIsEmpty(ipiBSFile);
		} catch (FileFormatException e) {
			throw new OperationException(this.getLastCommand(), e.getMessage(), e);
		}

		this.println(alignmentFile.getName() + " I:" + i + " S:" + s + " BS:" + bs);

		return new InformativePositions(i, s, bs);

	}

	@Override
	protected int calculateS(File scoreFile, File outputFile) throws OperationException {
		try {
			final String currentScoreFileData = FileUtils.readFileToString(scoreFile);
			final int scoreIni = currentScoreFileData.indexOf("SCORE=") + "SCORE=".length();
			final int scoreEnd = currentScoreFileData.indexOf("\n", scoreIni);

			return Integer.parseInt(currentScoreFileData.substring(scoreIni, scoreEnd));
		} catch (IOException e) {
			throw new OperationException(null, "Error reading score file", e);
		}
	}

	@Override
	protected int calculateBS(File alignmentFile, File bsAlignmentFile, File scoreFile, File ipiFile, File outputFile, int minScore) throws OperationException {
		final String tcoffeeParams = String.format(
			"-other_pg seq_reformat -in %s -struc_in %s -struc_in_f number_aln -action +use_cons +keep [&d-9] +rm_gap 1",
			shortenPath(alignmentFile.getAbsolutePath(), bsAlignmentFile.getParentFile()),
			shortenPath(scoreFile.getAbsolutePath(), bsAlignmentFile.getParentFile()),
			minScore
		);

		this.runTCoffee(tcoffeeParams, bsAlignmentFile, false);

		return this.calculateI(bsAlignmentFile, ipiFile, outputFile);
	}

	private void replaceOWithGaps(File inputFile) throws OperationException {
		if (this.getLogger() != null)
			this.getLogger().info("Command: Replacing o with gaps in: " + inputFile.getAbsolutePath());
		try {
			FileUtils.writeLines(
				inputFile,
				Utils.replaceNames(
					Collections.singletonMap("o", "-"),
					FileUtils.readLines(inputFile)
				)
			);
		} catch (IOException ioe) {
			throw new OperationException(ioe);
		}
	}

	protected int calculateI(File iInputFile, File iOutputFile, File outputFile) throws OperationException {
		if (!iOutputFile.getName().endsWith(".fasta")) {
			throw new IllegalArgumentException("iOutputFile must have the '.fasta' suffix");
		}
		String iOutputPath = iOutputFile.getAbsolutePath();
		iOutputPath = iOutputPath.substring(0, iOutputPath.length() - 6);
		
		// Command automatically adds the .fasta suffix to the output file.
		String tcoffeeParams = String.format(
			"-other_pg seq_reformat -in=%s -output=clustalw_aln -action +convert xX- -out %s",
			shortenPath(iInputFile.getAbsolutePath(), outputFile.getParentFile()),
			iOutputPath
		);
		
		this.runTCoffee(tcoffeeParams, outputFile, true);
		tcoffeeParams = String.format(
			"-other_pg seq_reformat -in=%s -output fasta",
			shortenPath(iOutputFile.getAbsolutePath(), iOutputFile.getParentFile())
		);
		this.runTCoffee(tcoffeeParams, iOutputFile, false);

		return maxSequenceLength(iOutputFile);
	}

	protected static int maxSequenceLength(File fastaFile) throws OperationException {
		String flatSequence = "";
		int maxLength = 0;
		try {
			for (String seq : FileUtils.readLines(fastaFile)) {
				if (seq.startsWith(">")) {
					maxLength = max(maxLength, flatSequence.length());
					
					flatSequence = "";
				} else {
					flatSequence += seq;
				}
			}
		} catch (IOException e) {
			throw new OperationException(null, "Error reading intermediate fasta file", e);
		}

		maxLength = max(maxLength, flatSequence.length());
		
		return maxLength;
	}

	@Override
	public int generateDivFile(File alnFile, File divFile) throws OperationException {
		final String tcoffeeParams = String.format("-other_pg seq_reformat -in %s -output sim", alnFile.getAbsolutePath());
		return this.runTCoffee(tcoffeeParams, divFile, false);
	}

	public SequenceDiversity calculateMinDiversity(Set<SequenceDiversity> usedSequences, File file) throws OperationException {
		final SortedSet<SequenceDiversity> sequences = new TreeSet<>();

		try {
			for (String line : FileUtils.readLines(file)) {
				if (line.contains("AVG")) {
					sequences.add(new SequenceDiversity(line));
				}
			}
		} catch (IOException e) {
			throw new OperationException(null, "Error reading diversity file", e);
		}

		for (SequenceDiversity sequence : sequences) {
			if (!usedSequences.contains(sequence)) {
				return sequence;
			}
		}

		return null;
	}

	@Override
	public int removeSequence(String sequenceId, File fastaFile, File newFastaFile) throws OperationException {
		final String tcoffeeParams = String.format(
			"-other_pg seq_reformat -in %s -action +keep_name +remove_seq %s",
			shortenPath(fastaFile.getAbsolutePath(), newFastaFile.getParentFile()),
			sequenceId
		);
		return this.runTCoffee(tcoffeeParams, newFastaFile, false);
	}

	@Override
	public int runAlingment(File fastaFile, AlignMethod alignMethod, File logFile) throws OperationException {
		String tcoffeeParams = String.format(
			"%s %s -run_name %s. -cache=no",
			shortenPath(fastaFile.getAbsolutePath(), logFile.getParentFile()),
			alignMethod.getTCoffeeString(),
			shortenPath(fastaFile.getAbsolutePath(), logFile.getParentFile())
		);

		return this.runTCoffee(tcoffeeParams, logFile, true);
	}

	@Override
	public int calculateAlignmentScore(File alnFile, File logFile) throws OperationException {
		final String tcoffeeParams = String.format(
			"-infile %s -output=score_ascii -special_mode=evaluate -evaluate_mode=t_coffee_fast",
			shortenPath(alnFile.getAbsolutePath(), logFile.getParentFile())
		);

		return this.runTCoffee(tcoffeeParams, logFile, true);
	}

	@Override
	public int extractSequences(Set<String> sequences, File proteinFile, File resultFile) throws OperationException {
		String remSeqsString = "";
		for (String s : sequences)
			remSeqsString += s + ' ';

		final String tcoffeeParams = String.format(
			"-other_pg seq_reformat -in %s -action +keep_name +extract_seq_list %s",
			shortenPath(proteinFile.getAbsolutePath(), resultFile.getParentFile()),
			remSeqsString
		);

		return this.runTCoffee(tcoffeeParams, resultFile, false);
	}

	@Override
	public int profile(
		File fastaFile, File profile, AlignMethod alignMethod,
		String resultsPrefix, File logFile
	) throws OperationException {
		final String tcoffeeParams = String.format(
			"%s -profile %s %s -cache=no -run_name=%s_rsa_1.",
			shortenPath(fastaFile.getAbsolutePath(), logFile.getParentFile()),
			shortenPath(profile.getAbsolutePath(), logFile.getParentFile()),
			alignMethod.getTCoffeeString(),
			resultsPrefix
		);

		return this.runTCoffee(tcoffeeParams, logFile, true);
	}

	@Override
	public int convertDNAIntoAmino(File dnaFile, File outputFile) throws OperationException {
		final String tCoffeeParams = "-other_pg seq_reformat -action +translate -output fasta_seq -in " + shortenPath(dnaFile.getAbsolutePath(), outputFile.getParentFile());

		return this.runTCoffee(tCoffeeParams, outputFile, true);
	}

	@Override
	public int convertAminoIntoDNA(File fasta, File alnFile, File outputFile)
		throws OperationException {
		replaceOWithGaps(alnFile);
		final String tcoffeeParams = String.format(
			"-other_pg seq_reformat -action +thread_dna_on_prot_aln -output clustalw -in %s -in2 %s",
			shortenPath(fasta.getAbsolutePath(), outputFile.getParentFile()),
			shortenPath(alnFile.getAbsolutePath(), outputFile.getParentFile())
		);

		return this.runTCoffee(tcoffeeParams, outputFile, false);
	}

	@Override
	public int toFastaAln(File clustal, File alnFile) throws OperationException {
		final String tcoffeeParams = "-other_pg seq_reformat -output fasta_aln -in " + shortenPath(clustal.getAbsolutePath(), alnFile.getParentFile());

		return this.runTCoffee(tcoffeeParams, alnFile, false);
	}
	
	private static String shortenPath(String pathString, File basedir) {
		final Path path = Paths.get(pathString);
		final Path pathBasedir = basedir.toPath();
		
		final Path relativePath = pathBasedir.relativize(path);
		final String relativePathString = relativePath.toString();
		
		return relativePathString.length() < pathString.length() ? relativePathString : pathString;
	}
}
