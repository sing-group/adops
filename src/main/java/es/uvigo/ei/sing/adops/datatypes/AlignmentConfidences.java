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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AlignmentConfidences {
	private final Map<String, String> sequences;
	private final Map<String, Map<Integer, Confidence>> models;
	private final Map<Integer, Integer> movedIndexes;

	public AlignmentConfidences(Map<String, String> sequences) {
		this.sequences = sequences;
		this.models = new TreeMap<>();
		this.movedIndexes = new HashMap<>();

		int maxSeqLength = Integer.MIN_VALUE;

		for (String sequence : sequences.values()) {
			maxSeqLength = Math.max(maxSeqLength, sequence.length());
		}

		int offset = 1;
		for (int i = 0; i < maxSeqLength; i++) {
			boolean hasSpace = false;

			for (String sequence : sequences.values()) {
				if (sequence.length() <= i || sequence.charAt(i) == '-') {
					hasSpace = true;
					break;
				}
			}

			if (!hasSpace) {
				this.movedIndexes.put(i, offset++);
			}
		}
	}

	public Integer getMovedIndex(int index) {
		return this.movedIndexes.get(index);
	}

	public int sequenceLength() {
		return this.sequences.values().iterator().next().length();
	}

	public Map<String, String> getSequences() {
		return Collections.unmodifiableMap(sequences);
	}

	public void addModel(String id, Map<Integer, Confidence> model) {
		this.models.put(id, model);
	}

	public List<String> getModels() {
		return new ArrayList<>(this.models.keySet());
	}

	public boolean hasModels() {
		return !this.models.isEmpty();
	}

	public Map<Integer, Confidence> getModel(String id) {
		return Collections.unmodifiableMap(this.models.get(id));
	}

	public final static class Confidence {
		private final double beb, neb;

		public Confidence(double beb, double neb) {
			super();
			this.beb = beb;
			this.neb = neb;
		}

		public double getBeb() {
			return beb;
		}

		public double getNeb() {
			return neb;
		}
	}
}
