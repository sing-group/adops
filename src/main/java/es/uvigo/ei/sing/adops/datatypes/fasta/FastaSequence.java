package es.uvigo.ei.sing.adops.datatypes.fasta;

public class FastaSequence {
	private final String id;
	private final String description;
	private final String sequence;
	
	public FastaSequence(String name, String sequence) {
		if (name.matches(".+\\s+.+")) {
			final String[] parts = name.split("\\s+", 2);
			this.id = parts[0];
			this.description = parts[1];
		} else {
			this.id = name;
			this.description = null;
		}
		this.sequence = sequence;
	}

	public FastaSequence(String id, String description, String sequence) {
		this.id = id;
		this.description = description;
		this.sequence = sequence;
	}
	
	public String getIdAndDescription() {
		if (this.hasDescription()) {
			return this.getId() + " " + this.getDescription();
		} else {
			return this.getId();
		}
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean hasDescription() {
		return this.description != null;
	}

	public String getSequence() {
		return sequence;
	}

}
