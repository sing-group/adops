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
package es.uvigo.ei.sing.adops.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import es.uvigo.ei.sing.adops.datatypes.AlignmentConfidences.Confidence;
import es.uvigo.ei.sing.adops.datatypes.fasta.Fasta;

public class OmegaMapOutput {

	public static Map<Integer, Confidence> getConfidences(Fasta sequences, File summaryFile) throws IOException {
		Map<Integer, Confidence> omegaMapModel = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(summaryFile))) {
			String line = null;

			while ((line = br.readLine()) != null) {
				String[] lineSplit = line.split("\t");
				if (lineSplit.length > 0 && !lineSplit[0].isEmpty() && !lineSplit[0].equals("Site")) {
					if (lineSplit.length > 3 && !lineSplit[4].isEmpty()) {
						Integer site = Integer.valueOf(lineSplit[0]) + 1;
						Double pss = Double.valueOf(lineSplit[4]);
						omegaMapModel.put(site, new Confidence(pss, pss));
					}
				}
			}
		}

		return omegaMapModel;
	}

	public static int getPositionsCount(File sourceFile) throws IOException {
		return Files.readAllLines(sourceFile.toPath()).size() - 3;
	}
}
