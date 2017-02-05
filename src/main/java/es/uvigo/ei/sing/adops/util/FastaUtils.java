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
package es.uvigo.ei.sing.adops.util;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import es.uvigo.ei.sing.adops.datatypes.fasta.Fasta;
import es.uvigo.ei.sing.adops.datatypes.fasta.FastaSequence;

public class FastaUtils {
	public static boolean isFasta(File file) {
		if (file.getName().toLowerCase().endsWith("fasta")) {
			try {
				loadAndCheckSequences(file);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	public static void writeSequences(File fastaFile, Fasta sequences)
	throws IOException {
		writeSequences(fastaFile, sequences, false);
	}

	public static void writeSequences(File fastaFile, Fasta sequences, boolean append)
	throws IOException {
		final List<String> lines = sequences.getSequences().stream()
			.flatMap(sequence -> Stream.of(">" + sequence.getIdAndDescription(), sequence.getSequence()))
		.collect(toList());
		
		if (append)
			Files.write(fastaFile.toPath(), lines, StandardOpenOption.APPEND);
		else
			Files.write(fastaFile.toPath(), lines);
	}
	
	public static Fasta loadAndCheckSequences(File fastaFile, List<String> selectedIds) {
		final Fasta fasta = loadAndCheckSequences(fastaFile);
		
		final List<FastaSequence> sequences = fasta.getSequences().stream()
			.filter(sequence -> selectedIds.contains(sequence.getId()))
		.collect(toList());
		
		return new Fasta(sequences);
	}
	
	public static Fasta loadAndCheckSequences(File fastaFile)
	throws IllegalArgumentException {
		return loadSequences(fastaFile, true);
	}
	
	public static Fasta loadSequences(File fastaFile, boolean check)
	throws IllegalArgumentException {
		final List<FastaSequence> sequences = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(fastaFile))) {
			String line = null;
			
			// Ignores lines until first sequence
			while ((line = br.readLine()) != null && !line.startsWith(">"));

			String sequenceName = line.substring(1);
			String sequence = "";
			while (true) {
				line = br.readLine();

				if (line == null || line.startsWith(">")) {
					if (sequence.isEmpty()) {
						throw new IllegalArgumentException("Empty sequence: " + sequenceName);
					} else if (check) {
						if (sequence.length() % 3 != 0) {
							throw new IllegalArgumentException("Sequence length must be multiple of 3: " + sequenceName);
						}
					}

					sequences.add(new FastaSequence(sequenceName, sequence));
					
					if (line == null) {
						break;
					} else {
						sequenceName = line.substring(1);
						sequence = "";
					}
				} else {
					sequence += line.replaceAll("\\s", "");
				}
			}

		} catch (IOException ioe) {
			throw new IllegalArgumentException("Error reading input Fasta file: " + fastaFile.getAbsolutePath(), ioe);
		}
		
		return new Fasta(sequences);
	}
}
