package es.uvigo.ei.sing.adops.datatypes.fasta;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

public class Fasta {
	private final List<FastaSequence> sequences;

	public Fasta(List<FastaSequence> sequences) {
		this.sequences = new ArrayList<>(sequences);
	}
	
	public List<String> getIds() {
		return this.sequences.stream()
			.map(FastaSequence::getId)
		.collect(toList());
	}
	
	public List<String> getSequencesChain() {
		return this.sequences.stream()
			.map(FastaSequence::getSequence)
		.collect(toList());
	}
	
	public FastaSequence getSequence(final String id) {
		return this.sequences.stream()
			.filter(sequence -> sequence.getId().equals(id))
		.findFirst()
		.orElseThrow(() -> new IllegalArgumentException("Invalid sequence id: " + id));
	}
	
	public List<FastaSequence> getSequences() {
		return unmodifiableList(sequences);
	}
	
	public int size() {
		return this.sequences.size();
	}
}
