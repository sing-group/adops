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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.sing.adops.operations.running.tcoffee.AlignMethod;

@Datatype(structure=Structure.COMPLEX)
public class TCoffeeOutput extends AbstractOperationOutput {
	public static final String OUTPUT_FOLDER_NAME = "tcoffee";
	
	private final File inputFile;
	private final File outputFolder;
	private final AlignMethod alignMethod;
	
	private final Set<File> optimizationFiles;
	
	private int firstLevelCounter, secondLevelCounter;
	
	public TCoffeeOutput(File inputFile, File outputFolder, AlignMethod alignMethod) {
		this(inputFile, outputFolder, alignMethod, -1);
	}
	
	public TCoffeeOutput(File inputFile, File outputFolder, AlignMethod alignMethod, int state) {
		super(state);
		
		this.inputFile = inputFile;
		this.outputFolder = new File(outputFolder, TCoffeeOutput.OUTPUT_FOLDER_NAME);
		this.outputFolder.mkdirs();
		this.alignMethod = alignMethod;
		
		this.optimizationFiles = new HashSet<File>();
		this.resetOptimization(false);
	}
	
	public void resetOptimization(boolean deletePreviousFiles) {
		if (deletePreviousFiles) {
			for (File file : this.optimizationFiles) {
				file.delete();
			}
			this.optimizationFiles.clear();
		}
		
		this.firstLevelCounter = this.secondLevelCounter = 0;
	}
	
	public void initOptimization(boolean deletePreviousFiles) {
		this.resetOptimization(deletePreviousFiles);
		
		if (!this.getInitialAlnFile().isFile()) {
			throw new IllegalStateException("Missing inital alignment file: " + this.getInitialAlnFile());
		}
		
		if (!this.getInitialFastaFile().isFile()) {
			throw new IllegalStateException("Missing inital fasta file: " + this.getInitialFastaFile());
		}
	}
	
	@Override
	protected Set<File> getIgnoredFiles() {
		return new HashSet<File>(Arrays.asList(
			this.getCurrentAlnFile(),
			this.getCurrentDivFile(),
			this.getCurrentFastaFile(),
			this.getCurrentHtmlFile(),
			this.getCurrentScoreFile(),
			this.getCurrentTmpAlnFile()
		));
	}
	
	public File getCurrentFastaFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getCurrentFastaSuffix());
	}
	
	public File getCurrentHtmlFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getCurrentFastaSuffix() + ".html");
	}
	
	public File getCurrentScoreFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getCurrentFastaSuffix() + ".score_ascii");
	}
	
	public File getCurrentAlnFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getCurrentFastaSuffix() + ".aln");
	}
	
	public File getCurrentTmpAlnFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getCurrentFastaSuffix() + ".tmp.aln");
	}
	
	public File getCurrentDivFile() {
		return new File(this.getCurrentAlnFile().getAbsolutePath() + ".div");
	}
	
	public void increaseFirstLevelCounter() {
		this.firstLevelCounter++;
		this.secondLevelCounter = 0;
		
		this.increaseSecondLevelCounter(); // Forces file storage
	}
	
	public void increaseSecondLevelCounter() {
		this.secondLevelCounter++;
		
		this.optimizationFiles.add(this.getCurrentFastaFile());
		this.optimizationFiles.add(this.getCurrentHtmlFile());
		this.optimizationFiles.add(this.getCurrentScoreFile());
		this.optimizationFiles.add(this.getCurrentAlnFile());
		this.optimizationFiles.add(this.getCurrentTmpAlnFile());
		this.optimizationFiles.add(this.getCurrentDivFile());
	}
	
	public File getInputFile() {
		return inputFile;
	}
	
	public File getAlignmentFile() {
		return new File(this.outputFolder, this.inputFile.getName() + ".fasta");
	}
	
	public File getProteinAlignmentFile() {
		return new File(this.outputFolder, this.getAlignmentFile().getName() + this.getProteinSuffix());
	}
	
	protected String getProteinSuffix() {
		return ".prot.fasta";
	}
	
	protected String getInitialFastaSuffix() {
		return String.format("%s.%s_rs_0_0.fasta",
			this.getProteinSuffix(),
			this.alignMethod.getName()
		);
	}
	
	protected String getCurrentFastaSuffix() {
		return String.format("%s.%s_rs_%d_%d.fasta",
			this.getProteinSuffix(),
			this.alignMethod.getName(),
			this.secondLevelCounter,
			this.firstLevelCounter
		);
	}
	
	public String getResultsPrefix() {
		return this.getProteinFile().getName() + "." + this.alignMethod.getName();
	}
	
	public File getOutputFile() {
		return new File(this.outputFolder, "tcoffee.out.log");
	}
	
	public File getSummaryFile() {
		return this.getOutputFile();
	}
	
	public File getProteinFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getProteinSuffix());
	}
	
	public File getFinalClustalFile() {
		return new File(this.outputFolder, this.inputFile.getName() + ".clus");
	}
	
	public File getFinalOutputFile() {
		return new File(this.outputFolder, this.inputFile.getName() + ".fasta");
	}
	
	public File getInitialFastaFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getInitialFastaSuffix());
	}
	
	public File getInitialAlnFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getInitialFastaSuffix() + ".aln");
	}
	
	public File getInitialDivFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getInitialFastaSuffix() + ".aln.div");
	}
	
	public File getInitialScoreFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getInitialFastaSuffix() + ".score_ascii");
	}
	
	public File getInitialHtmlFile() {
		return new File(this.outputFolder, this.inputFile.getName() + this.getInitialFastaSuffix() + ".html");
	}
	
	public File getReducedSetAlnFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "_rsa.aln");
	}
	
	public File getReducedSetAlnHtmlFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "_rsa.html");
	}
	
	public File getReducedSetFastaFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "_rs.fasta");
	}
	
	public File getReducedSetScoreFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "_rs.score_ascii");
	}

	public File getFinalRealingmentAlnFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "_rsa_1.aln");
	}
	
	public File getFinalRealingmentHtmlFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "_rsa_1.html");
	}
	
	public File getRenamedFinalRealingmentAlnFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "_r.aln");
	}
	
	public File getRenamedFinalRealingmentHtmlFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "_r.html");
	}
	
	public File getRenamedFinalRealingmentScoreFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "_r.score_ascii");
	}
	
	public File getFinalUsedAlnFile () {
		return new File(this.outputFolder, this.getResultsPrefix() + "final.aln");
	}

	public File getFinalScoreFile() {
		return new File(this.outputFolder, this.getResultsPrefix() + "final.score_ascii");
	}
	
	public int getFirstLevelCounter() {
		return this.firstLevelCounter;
	}
	
	public int getSecondLevelCounter() {
		return this.secondLevelCounter;
	}
	
}
