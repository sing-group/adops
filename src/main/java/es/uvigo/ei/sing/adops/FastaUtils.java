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
package es.uvigo.ei.sing.adops;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.sing.alter.parser.ParseException;
import es.uvigo.ei.sing.alter.reader.AutodetectionReader;
import es.uvigo.ei.sing.alter.reader.FastaReader;
import es.uvigo.ei.sing.alter.reader.Reader;
import es.uvigo.ei.sing.alter.types.Fasta;
import es.uvigo.ei.sing.alter.types.FastaSequence;

public class FastaUtils {
	public static boolean isFasta(File file) {
		return FastaUtils.isFasta(file, FastaUtils.class.getName());
	}

	public static boolean isFasta(File file, String logger) {
		if (file.getName().toLowerCase().endsWith("fasta")) {
			final Reader reader = new AutodetectionReader(logger);
			
			try {
				reader.read(FileUtils.readFileToString(file));
				
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}

	public static Fasta readFasta(File file) throws ParseException, IOException {
		return FastaUtils.readFasta(file, FastaUtils.class.getName());
	}

	public static Fasta readFasta(File file, String logger) throws ParseException, IOException {
		if (isFasta(file)) {
			FastaReader reader = new FastaReader(logger);
			
			return (Fasta) reader.read(FileUtils.readFileToString(file));
		} else {
			throw new IllegalArgumentException(file.getAbsolutePath() + " file is not a valid Fasta");
		}
	}

	public static Fasta extractSequences(Fasta fasta, List<String> sequenceIds) {
		final Vector<FastaSequence> selectedSequences = 
			new Vector<FastaSequence>(sequenceIds.size());
		
		for (FastaSequence fastaSeq : FastaUtils.listFastaSequences(fasta)) {
			if (sequenceIds.contains(fastaSeq.getId())) {
				selectedSequences.add(fastaSeq);
			}
		}
		
		return new Fasta(selectedSequences);
	}
	
	public static List<FastaSequence> listFastaSequences(File fastaFile)
	throws ParseException, IOException {
		return FastaUtils.listFastaSequences(fastaFile, FastaUtils.class.getName());
	}
	
	public static List<FastaSequence> listFastaSequences(File fastaFile, String logger)
	throws ParseException, IOException {
		final Fasta fasta = FastaUtils.readFasta(fastaFile, logger);
		final List<FastaSequence> sequences = 
			new ArrayList<FastaSequence>(fasta.getSeqs().size());
		
		for (Object seq : fasta.getSeqs()) {
			sequences.add((FastaSequence) seq);
		}
		
		return sequences;
	}

	public static List<FastaSequence> listFastaSequences(Fasta fasta) {
		final List<FastaSequence> sequences = 
			new ArrayList<FastaSequence>(fasta.getSeqs().size());
		
		for (Object seq : fasta.getSeqs()) {
			sequences.add((FastaSequence) seq);
		}
		
		return sequences;
	}
}
