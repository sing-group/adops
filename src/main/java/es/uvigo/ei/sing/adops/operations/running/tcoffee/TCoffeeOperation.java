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
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.adops.configuration.TCoffeeConfiguration;
import es.uvigo.ei.sing.adops.datatypes.Experiment;
import es.uvigo.ei.sing.adops.datatypes.TCoffeeOutput;
import es.uvigo.ei.sing.adops.operations.running.OperationException;
import es.uvigo.ei.sing.adops.operations.running.ProcessOperation;
import es.uvigo.ei.sing.adops.operations.running.tcoffee.TCoffeeProcessManager.InformativePositions;
import es.uvigo.ei.sing.adops.operations.running.tcoffee.TCoffeeProcessManager.SequenceDiversity;

@Operation(name = "TCoffee")
public class TCoffeeOperation
extends ProcessOperation<TCoffeeProcessManager, TCoffeeConfiguration, TCoffeeOutput> {
	private static final int MIN_GAIN = 1;
	private final static Logger LOGGER = Logger.getLogger(TCoffeeOperation.class);
	
	public TCoffeeOperation() {
		super(TCoffeeConfiguration.class);
	}
	
	@Port(
		name = "FASTA File",
		order = 1,
		direction = Direction.INPUT,
		allowNull = false
	)
	public void setInputFile(File inputFile) {
		super.setInputFile(inputFile);
	}
	
	@Port(
		name = "Output Folder",
		order = 2,
		direction = Direction.INPUT,
		allowNull = false
	)
	public void setOutputFolder(File outputFolder) {
		super.setOutputFolder(outputFolder);
	}
	
	@Port(
		name = "Maximum number of sequences to be removed",
		order = 3,
		direction = Direction.INPUT,
		allowNull = false
	)
	public void setMaxSeqs(int maxSeqs) {
		this.configuration.setMaxSeqs(maxSeqs);
	}

	@Port(
		name = "Align method",
		order = 4,
		direction = Direction.INPUT,
		allowNull = false,
		defaultValue = TCoffeeConfiguration.VALUE_ALIGN_METHOD
	)
	public void setAlignMethod (AlignMethod method) {
		this.configuration.setAlignMethod(method);
	}


	@Port(
		name = "Min. Score",
		order = 4,
		direction = Direction.INPUT,
		allowNull = false,
		defaultValue = TCoffeeConfiguration.VALUE_MIN_SCORE
	)
	public void setMinScore(int minScore) {
		this.configuration.setMinScore(minScore);
	}

	@Port(
		name = "Use Std. Output",
		order = 5,
		direction = Direction.INPUT,
		defaultValue = "true"
	)
	public void setUseStdOutput(boolean useStdOutput) {
		if (useStdOutput) 
			this.addPrintStream(System.out);
		else 
			this.removePrintStream(System.out);
	}
	
	@Override
	protected Logger getLogger() {
		return TCoffeeOperation.LOGGER;
	}

	@Port(
	    direction = Direction.OUTPUT,
	    order = 1000
	)
	@Override
	public TCoffeeOutput call() throws OperationException, InterruptedException {
		final TCoffeeOutput output = new TCoffeeOutput(this.getInputFile(), this.getOutputFolder(), this.configuration.getAlignMethod());
		this.process = TCoffeeProcessManager.createManager(this.configuration);
		
		for (PrintStream ps : this.getPrintStreams()) {
			this.process.addPrinter(ps);
		}
		
		// TODO: check if number of sequences in fasta file > maxSeqs
		try {
			// Step 1 - DNA -> Protein conversion
			this.checkInterrupted();
			
			output.setState(this.process.convertDNAIntoAmino(output.getInputFile(), output.getProteinFile()));
			
			// Step 2 - Initial alignment
			// TODO: CACHE/BACKUP
			this.checkInterrupted();
			
			FileUtils.copyFile(output.getProteinFile(), output.getInitialFastaFile());
			
			this.checkInterrupted();
			
			output.setState(this.process.runAlignment(output.getInitialFastaFile(), this.configuration.getAlignMethod(), output.getOutputFile()));

			this.checkInterrupted();
			
			output.setState(this.process.evaluateAlignment(output.getInitialAlnFile(), output.getOutputFile()));
			
			this.checkInterrupted();
			
			final InformativePositions initialIP = this.process.computeInformativePositions(
				output.getInitialAlnFile(), output.getInitialScoreFile(), output.getOutputFile(), this.configuration.getMinScore()
			); 
			
			this.checkInterrupted();
			
			output.setState(this.process.generateDivFile(output.getInitialAlnFile(), output.getInitialDivFile()));
			
			File finalAlnFile = output.getInitialAlnFile(); // TODO is this the correct file?
			File finalScoreFile = output.getInitialScoreFile();
			
			// Step 3 - Optimization loop
			this.checkInterrupted();
			
			final int maxSeqs = this.configuration.getMaxSeqs();
			if (maxSeqs > 0) {
				InformativePositions bestIP = initialIP;
				File previousFastaFile = output.getInitialFastaFile();
				File previousDivFile = output.getInitialDivFile();
				
				File bestFastaFile = previousFastaFile;
				File bestDivFile = previousDivFile;
				File bestAlnFile = output.getInitialAlnFile();
				File bestAlnHtmlFile = output.getInitialHtmlFile();
				File bestScoreFile = output.getInitialScoreFile();
				
				output.increaseFirstLevelCounter();
				Set<String> remSeqs = new HashSet<String>();
				
				while (output.getFirstLevelCounter() <= maxSeqs) {
					Set<SequenceDiversity> testedSequences = new HashSet<SequenceDiversity>();
					InformativePositions prevBestIP = bestIP;
					String curSeqRm = null;
					
					while (output.getSecondLevelCounter() <= maxSeqs) {
						this.checkInterrupted();
						
						//final SequenceDiversity seq = process.calculateMinDiversity(output.getSecondLevelCounter(), output.getCurrentDivFile());
						final SequenceDiversity seq = this.process.calculateMinDiversity(testedSequences, previousDivFile);
						testedSequences.add(seq);
						
						this.println("[Optimization] Chosen sequence: " + seq.getId() + "/" + seq.getNumber() + "; AVG: " + seq.getAverage());
						
						this.checkInterrupted();
						
						// Next fasta creation. Removes a sequence from current fasta
						output.setState(this.process.removeSequence(seq.getId(), previousFastaFile, output.getCurrentFastaFile()));
						
						this.checkInterrupted();
						
						// Aligns fasta file
						output.setState(this.process.runAlignment(output.getCurrentFastaFile(), this.configuration.getAlignMethod(), output.getOutputFile()));
						
						// Calculates alignment score
						output.setState(this.process.calculateAlignmentScore(output.getCurrentAlnFile(), output.getOutputFile()));
						
						this.checkInterrupted();
						
						final InformativePositions currentIP = this.process.computeInformativePositions(
							output.getCurrentAlnFile(), output.getCurrentScoreFile(), output.getOutputFile(), this.configuration.getMinScore()
						);
						
						//TODO core & i heuristics
						// bs heuristic
						this.checkInterrupted();
						if (currentIP.BS > prevBestIP.BS + TCoffeeOperation.MIN_GAIN &&
							(currentIP.BS > bestIP.BS || currentIP.BS == bestIP.BS && currentIP.S > bestIP.S)
						) {
							bestIP = currentIP;
							bestAlnFile = output.getCurrentAlnFile();
							bestFastaFile = output.getCurrentFastaFile();
							bestDivFile = output.getCurrentDivFile();
							bestAlnHtmlFile = output.getCurrentHtmlFile();
							bestScoreFile = output.getCurrentScoreFile();
							curSeqRm = seq.getId();
						}
						
						// select_metric == Diversity
						output.setState(process.generateDivFile(output.getCurrentAlnFile(), output.getCurrentDivFile()));
						
						//previousFastaFile = output.getCurrentFastaFile();
						previousDivFile = output.getCurrentDivFile();
						
						output.increaseSecondLevelCounter();
					}
					
					if (prevBestIP.S == bestIP.S){
						//unable to improve. TODO check this
						break;
					}
					
					if (curSeqRm != null) remSeqs.add(curSeqRm);
					this.println("[Optimization] Removed Sequences: " + remSeqs);
					previousFastaFile = bestFastaFile;
					previousDivFile = bestDivFile;
					output.increaseFirstLevelCounter();
				}
	
				this.checkInterrupted();
				
				FileUtils.copyFile(bestAlnFile, output.getReducedSetAlnFile());
				FileUtils.copyFile(bestAlnHtmlFile, output.getReducedSetAlnHtmlFile());
				FileUtils.copyFile(bestFastaFile, output.getReducedSetFastaFile());
				FileUtils.copyFile(bestScoreFile, output.getReducedSetScoreFile());
				
				finalAlnFile = output.getReducedSetAlnFile();
				finalScoreFile = output.getReducedSetScoreFile();
				
				// Step 4 - Increase the number of sequences in the alignment
				if (!remSeqs.isEmpty()) {
					this.checkInterrupted();
					
					File expandingTmpFile = File.createTempFile("tmp", "fasta");
					expandingTmpFile.deleteOnExit();
					
					output.setState(this.process.extractSequences(remSeqs, output.getProteinFile(), expandingTmpFile));
						
					this.checkInterrupted();
					
					output.setState(this.process.profile(
						expandingTmpFile, 
						output.getReducedSetAlnFile(), 
						this.configuration.getAlignMethod(), 
						output.getResultsPrefix(), 
						output.getOutputFile()
					));
		
					this.checkInterrupted();
					
					FileUtils.copyFile(output.getFinalRealingmentAlnFile(), output.getRenamedFinalRealingmentAlnFile());
					if (output.getFinalRealingmentHtmlFile().exists()) {
						FileUtils.copyFile(output.getFinalRealingmentHtmlFile(), output.getRenamedFinalRealingmentHtmlFile());
					} else {
						this.println("[WARNING] Missing " + output.getFinalRealingmentHtmlFile() + " file", Level.WARN);
					}
					
					this.checkInterrupted();
					
					output.setState(this.process.evaluateAlignment(output.getRenamedFinalRealingmentAlnFile(), output.getOutputFile()));
					
					final InformativePositions renamedFinalRealignmentBestIP = this.process.computeInformativePositions(
						output.getRenamedFinalRealingmentAlnFile(), 
						output.getRenamedFinalRealingmentScoreFile(), 
						output.getOutputFile(),
						this.configuration.getMinScore()
					);
					this.println(
						String.format("Final alignment: Score=%d IP=%d BS=%d\n", renamedFinalRealignmentBestIP.S, renamedFinalRealignmentBestIP.I, renamedFinalRealignmentBestIP.BS)
					);
		
					//finalAlnFile = output.getRenamedFinalRealingmentAlnFile(); 
				}
			}
			
			FileUtils.copyFile(finalAlnFile, output.getFinalUsedAlnFile());
			FileUtils.copyFile(finalScoreFile, output.getFinalScoreFile());
	
			this.checkInterrupted();
			
			// Step 5  - Protein Aln -> DNA conversion -> Protein Fasta
			output.setState(this.process.convertAminoIntoDNA(output.getInputFile(), finalAlnFile, output.getFinalClustalFile()));

			this.checkInterrupted();
			
			output.setState(this.process.toFastaAln(output.getFinalClustalFile(), output.getAlignmentFile()));
			
			this.checkInterrupted();
			
			output.setState(this.process.convertDNAIntoAmino(output.getAlignmentFile(), output.getProteinAlignmentFile()));
	
			this.checkInterrupted();
			
			return output; 
		} catch (IOException ioe) {
			throw new OperationException("", "I/O error while running T-Coffee", ioe);
		} catch (OperationException oe) {
			if (oe.getCause() instanceof InterruptedException) {
				throw (InterruptedException) oe.getCause();
			} else {
				throw new OperationException(oe.getCommand(), "Error while running T-Coffee: " + oe.getMessage(), oe);
			}
		}
	}
	
	@Override
	protected void configureExperiment(Experiment experiment) {
		this.setInputFile(experiment.getFastaFile());
		this.setOutputFolder(experiment.getFilesFolder());
	}
	
	@Override
	protected void configureSubConfiguration(TCoffeeConfiguration tCoffeeConfiguration) {
		this.setMaxSeqs(tCoffeeConfiguration.getMaxSeqs());
		this.setAlignMethod(tCoffeeConfiguration.getAlignMethod());
	}
}
