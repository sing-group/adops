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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.sing.adops.Utils;
import es.uvigo.ei.sing.adops.configuration.TCoffeeConfiguration;
import es.uvigo.ei.sing.adops.operations.running.OperationException;

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
		final String tcoffeeParams = String.format("%s %s -run_name %s. -cache=no",
			fastaFile.getAbsolutePath(),
			alignMethod.getTCoffeeString(),
			fastaFile.getAbsolutePath()
		); 
		
		return this.runTCoffee(tcoffeeParams, outputFile, true);
	}
	
	public int evaluateAlignment(File alignmentFile, File outputFile) throws OperationException {
		final String tCoffeeParams = String.format("-infile=%s -output=score_ascii -special_mode=evaluate -evaluate_mode=t_coffee_fast",
			alignmentFile.getAbsolutePath()
		);
		
		return this.runTCoffee(tCoffeeParams, outputFile, true);
	}
	
	public InformativePositions computeInformativePositions(File alignmentFile, File scoreFile, File outputFile, int minScore) throws OperationException {
		final InformativePositions ip = new InformativePositions();
		
		File ipiFile;
		try {
			ipiFile = File.createTempFile("ipi", null);
			ipiFile.deleteOnExit();
			
			// Compute I
			ip.I = this.calculateI(alignmentFile, ipiFile, outputFile);
			
			// Compute S
			ip.S = this.calculateS(scoreFile, outputFile);
			
			// Compute BS
			ip.BS = this.calculateBS(alignmentFile, scoreFile, ipiFile, outputFile, minScore);
			
			this.println(alignmentFile.getName() + " I:" + ip.I + " S:" + ip.S + " BS:" + ip.BS);

		} catch (IOException e) {
			throw new OperationException (null, "Error creating tmp file", e);
		}
		
		return ip;

	}
	
	@Override
	protected int calculateS(File scoreFile, File outputFile)  throws OperationException {
		String currentScoreFileData;
		try {
			currentScoreFileData = FileUtils.readFileToString(scoreFile);
			final int scoreIni = currentScoreFileData.indexOf("SCORE=") + "SCORE=".length();
			final int scoreEnd = currentScoreFileData.indexOf("\n", scoreIni);
			
			return Integer.parseInt(currentScoreFileData.substring(scoreIni, scoreEnd));
		} catch (IOException e) {
			throw new OperationException (null, "Error reading score file", e);
		}
	}
	
	@Override
	protected int calculateBS(File alignmentFile, File scoreFile, File ipiFile, File outputFile, int minScore) throws OperationException {
		final String tcoffeeParams = String.format("-other_pg seq_reformat -in %s -struc_in %s -struc_in_f number_aln -action +use_cons +keep [&d-9] +rm_gap 1",
			alignmentFile.getAbsolutePath(),
			scoreFile.getAbsolutePath(),
			minScore
		);
//				"-other_pg seq_reformat -in " + alignmentFile.getAbsolutePath() + " -struc_in " + scoreFile.getAbsolutePath() + " -struc_in_f number_aln -action +use_cons +keep [3-9] +rm_gap 1";
		
		File tmpAlnFile;
		try {
			tmpAlnFile = File.createTempFile("tmp", "aln");
			tmpAlnFile.deleteOnExit();
			this.runTCoffee(tcoffeeParams, tmpAlnFile, false);

			return this.calculateI(tmpAlnFile, ipiFile, outputFile);
		} catch (IOException e) {
			throw new OperationException(null, "Error creating temporary file", e);
		}
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
//		try {
//			try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
//				PrintWriter pw = new PrintWriter(outputFile)
//			) {
//				
//				String line;
//				
//				while ((line = reader.readLine()) != null) {
//					if (!line.startsWith(">")) {
//						line = line.replaceAll("[-o]", "");
//					}
//					
//					pw.println(line);
//				}
//			}
//		} catch (IOException ioe) {
//			throw new OperationException(ioe);
//		}
	}
	
	protected int calculateI(File iInputFile, File iOutputFile, File outputFile) throws OperationException {
		// TODO: Keep fasta.fasta? Add this file to output?
		File fastaFileFinal = new File(iOutputFile.getAbsolutePath() + ".fasta");
		String tcoffeeParams = String.format("-other_pg seq_reformat -in=%s -output=clustalw_aln -action +convert xX- -out %s",
			iInputFile.getAbsolutePath(),
			iOutputFile.getAbsolutePath()
		);
		this.runTCoffee(tcoffeeParams, outputFile, true);
		tcoffeeParams = String.format("-other_pg seq_reformat -in=%s -output fasta",
			iOutputFile.getAbsolutePath()
		);
		this.runTCoffee(tcoffeeParams, fastaFileFinal, false);
		
		final List<String> flatSequences = new ArrayList<String>();
		String flatSequence = "";
		try {
			for (String seq : FileUtils.readLines(fastaFileFinal)) {
				if (seq.startsWith(">")) {
					if (!flatSequence.isEmpty())
						flatSequences.add(flatSequence);
					flatSequence = "";
				}
				else flatSequence += seq;
			}
		} catch (IOException e) {
			throw new OperationException(null, "Error reading intermediate fasta file", e);
		}
		if (!flatSequence.contentEquals("")) flatSequences.add(flatSequence);

		int maxLength = 0;
		for (String seq : flatSequences)
			if (seq.length() > maxLength)
				maxLength = seq.length();
		return maxLength;
	}
	
	@Override
	public int generateDivFile(File alnFile, File divFile) throws OperationException {
		final String tcoffeeParams = String.format("-other_pg seq_reformat -in %s -output sim", alnFile.getAbsolutePath());
		return this.runTCoffee(tcoffeeParams, divFile, false);
	}

	public SequenceDiversity calculateMinDiversity(Set<SequenceDiversity> usedSequences, File file) throws OperationException {
		final SortedSet<SequenceDiversity> sequences = new TreeSet<SequenceDiversity>();

		try {
			for (String line : FileUtils.readLines(file)) {
				if (line.contains("AVG")) {
					sequences.add(new SequenceDiversity(line));
				}
			}
		} catch (IOException e) {
			throw new OperationException (null, "Error reading diversity file", e);
		}
		
		for (SequenceDiversity sequence : sequences) {
			if (!usedSequences.contains(sequence)) {
				return sequence;
			}
		}

		return null;
//		int index = 0;
//		
//		
//		BufferedReader br = null;
//		
//		try {
//			br = new BufferedReader(new FileReader(file));
//			
//			
//			String line;
//			while ((line = br.readLine()) != null) {
//				if (line.contains("AVG")) {
//					sequences.add(new SequenceDiversity(line));
//				}
//			}
//			
//			index = 0;
//			for (SequenceDiversity sequence : sequences) {
//				if (++index == curseq) {
//					return sequence;
//				}
//			}
//			
//			return null;
//		} finally {
//			if (br != null) 
//				try { br.close(); }
//				catch (IOException ioe) {}
//		}
	}
	
	@Override
	public int removeSequence(String sequenceId, File fastaFile, File newFastaFile) throws OperationException {
		final String tcoffeeParams = String.format("-other_pg seq_reformat -in %s -action +keep_name +remove_seq %s",
			fastaFile.getAbsolutePath(), 
			sequenceId
		);
		return this.runTCoffee(tcoffeeParams, newFastaFile, false);
	}
	
	@Override
	public int runAlingment(File fastaFile, AlignMethod alignMethod, File logFile) throws OperationException {
		String tcoffeeParams = String.format("%s %s -run_name %s. -cache=no",
			fastaFile.getAbsolutePath(),
			alignMethod.getTCoffeeString(),
			fastaFile.getAbsolutePath()
		); 
		
		return this.runTCoffee(tcoffeeParams, logFile, true);
	}
	
	@Override
	public int calculateAlignmentScore(File alnFile, File logFile) throws OperationException {
		final String tcoffeeParams = String.format("-infile %s -output=score_ascii -special_mode=evaluate -evaluate_mode=t_coffee_fast", 
			alnFile.getAbsolutePath()
		); 
		
		return this.runTCoffee(tcoffeeParams, logFile, true);
	}
	
	@Override
	public int extractSequences(Set<String> sequences, File proteinFile, File resultFile) throws OperationException {
		String remSeqsString = "";
		for (String s : sequences) remSeqsString += s + ' ';
		
		final String tcoffeeParams = String.format("-other_pg seq_reformat -in %s -action +keep_name +extract_seq_list %s",
			proteinFile.getAbsolutePath(),
			remSeqsString
		);
				
		return this.runTCoffee(tcoffeeParams, resultFile, false);
	}
	
	@Override
	public int profile(File fastaFile, File profile, AlignMethod alignMethod,
			String resultsPrefix, File logFile) throws OperationException {
		final String tcoffeeParams = String.format("%s -profile %s %s -cache=no -run_name=%s_rsa_1.",
			fastaFile.getAbsolutePath(),
			profile.getAbsolutePath(),
			alignMethod.getTCoffeeString(),
			resultsPrefix
		);
		
		return this.runTCoffee(tcoffeeParams, logFile, true);
	}

	@Override
	public int convertDNAIntoAmino(File dnaFile, File outputFile) throws OperationException {
		final String tCoffeeParams = "-other_pg seq_reformat -action +translate -output fasta_seq -in " + dnaFile.getAbsolutePath();
		
		return this.runTCoffee(tCoffeeParams, outputFile, true);
	}
	
	@Override
	public int convertAminoIntoDNA(File fasta, File alnFile, File outputFile)
	throws OperationException {
		replaceOWithGaps(alnFile);
		final String tcoffeeParams = String.format("-other_pg seq_reformat -action +thread_dna_on_prot_aln -output clustalw -in %s -in2 %s",
			fasta.getAbsolutePath(),
			alnFile.getAbsolutePath()
		);
//		this.println(tcoffeeParams);

		return this.runTCoffee(tcoffeeParams, outputFile, false);
	}
	
	@Override
	public int toFastaAln(File clustal, File alnFile) throws OperationException {
		final String tcoffeeParams = "-other_pg seq_reformat -output fasta_aln -in " + clustal.getAbsolutePath();
//		this.println(tcoffeeParams);
		
		return this.runTCoffee(tcoffeeParams, alnFile, false);
	}
}
