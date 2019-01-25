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
package es.uvigo.ei.sing.adops.operations.running.codeml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.sing.adops.configuration.CodeMLConfiguration;
import es.uvigo.ei.sing.adops.datatypes.CodeMLOutput;
import es.uvigo.ei.sing.adops.operations.running.OperationException;
import es.uvigo.ei.sing.alter.converter.DefaultFactory;
import es.uvigo.ei.sing.alter.parser.ParseException;
import es.uvigo.ei.sing.alter.reader.Reader;
import es.uvigo.ei.sing.alter.writer.Writer;

public class CodeMLDefaultProcessManager extends CodeMLProcessManager {
	public CodeMLDefaultProcessManager(CodeMLConfiguration configuration) throws OperationException {
		super(configuration);
	}

	@Override
	public boolean isCompatibleWith(String version) {
		return true;
	}

	@Override
	public void createNexusFile(File fastaFile, File nexusFile) throws OperationException {
		DefaultFactory factory = new DefaultFactory();
		Reader fastaReader = factory.getReader("linux", "clustal", "fasta", false, "logger");
		Writer nexusWriter = factory.getWriter("linux", "paml", "nexus", false, false, true, false, "logger");

		String fastaString;
		try {
			fastaString = FileUtils.readFileToString(fastaFile);
		} catch (IOException e) {
			throw new OperationException(null, "Error reading input fasta file", e);
		}
		try {
			FileUtils.writeStringToFile(nexusFile, nexusWriter.write(fastaReader.read(fastaString)));
		} catch (IOException e) {
			throw new OperationException(null, "Error creating nexus file", e);
		} catch (ParseException e) {
			throw new OperationException(null, "Error parsing fasta file", e);
		}
	}

	@Override
	public void createTreeFile(File treeFile, File consFile) throws OperationException {
		try (BufferedReader br = new BufferedReader(new FileReader(consFile))) {
			String line = null;
			
			while ((line = br.readLine()) != null && !line.contains("con_50_majrule"));
			while ((line = br.readLine()) != null && !line.contains("con_50_majrule"));

			line = line.substring(line.indexOf('('));
			line = line.replaceAll(":[01]\\.[0-9]+", "");
			
			final int seqCount = line.split(",").length;

			FileUtils.writeLines(treeFile, Arrays.asList(seqCount + " 1\n", line));
		} catch (IOException e) {
			throw new OperationException(null, "Error creating final tree file", e);
		}
	}

	@Override
	public void createCodeMLFile(
		File nexusFile, File treeFile, File ctlFile,
		File outputFile
	) throws OperationException {
		try {
			FileUtils.writeStringToFile(ctlFile, this.createCodeMLCtl(nexusFile, treeFile, outputFile));
		} catch (IOException e) {
			throw new OperationException(null, "Error creating ctl file", e);
		}
	}

	@Override
	public int executeCodeMLFile(File ctlFile, File logFile) throws OperationException {
		try {
			return this.runCodeML(ctlFile.getAbsolutePath(), logFile, true);
		} catch (InterruptedException e) {
			throw new OperationException(null, "CodeML operation interrupted", e);
		}
	}

	@Override
	public void buildSummary(File outFile, File summaryFile) throws OperationException {
		final Map<String, Double> logLikelihood = new HashMap<>();
		final Map<String, String> modelNames = new HashMap<>();
		String model2Data = "", model8Data = "";
		String currentModel = null;

		try (BufferedReader br = new BufferedReader(new FileReader(outFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("Model")) {
					int end = line.indexOf(" (");
					if (end == -1)
						end = line.length();

					currentModel = line.substring(0, line.indexOf(':'));
					modelNames.put(currentModel, line.substring(0, end));
				} else if (line.startsWith("lnL")) {
					int index = line.indexOf('-');
					logLikelihood.put(currentModel, Double.parseDouble(line.substring(index, line.indexOf(' ', index))));
				} else if (line.startsWith("Naive") || line.startsWith("Bayes")) {
					String modelData = line + "\n";
					int whiteLines = 0;
					
					while ((line = br.readLine()) != null && whiteLines < 3 && !line.startsWith("Time used")) {
						if (line.isEmpty())
							++whiteLines;
						modelData += line + "\n";
					}

					if (currentModel.startsWith("Model 2"))
						model2Data += modelData;
					else if (currentModel.startsWith("Model 8"))
						model8Data += modelData;
				}
			}
		} catch (IOException e) {
			throw new OperationException(null, "Error reading CodeML output file", e);
		}

		try (FileWriter sumFW = new FileWriter(summaryFile)) {
			for (String model : logLikelihood.keySet()) {
				sumFW.append(modelNames.get(model) + "\t" + logLikelihood.get(model) + "\n");
			}

			sumFW.append("\n");

			if (logLikelihood.containsKey("Model 0") && logLikelihood.containsKey("Model 1")) {
				final double diff01 = Math.abs(logLikelihood.get("Model 1") - logLikelihood.get("Model 0")) * 2;
				
				sumFW.append("\nModel 0 vs 1\t" + diff01 + "\n");
			}

			if (logLikelihood.containsKey("Model 2") && logLikelihood.containsKey("Model 1")) {
				final double diff21 = Math.abs(logLikelihood.get("Model 2") - logLikelihood.get("Model 1")) * 2;
				
				sumFW.append("\nModel 2 vs 1\t" + diff21 + "\n");
				if (diff21 > 5.9914 && !model2Data.isEmpty()) {
					sumFW.append("\nAdditional information for M1 vs M2:\n");
					sumFW.append(model2Data);
				}
			}

			if (logLikelihood.containsKey("Model 8") && logLikelihood.containsKey("Model 7")) {
				final double diff87 = Math.abs(logLikelihood.get("Model 8") - logLikelihood.get("Model 7")) * 2;
				
				sumFW.append("\nModel 8 vs 7\t" + diff87 + "\n");
				if (diff87 > 5.9914 && !model8Data.isEmpty()) {
					sumFW.append("\nAdditional information for M7 vs M8:\n");
					sumFW.append(model8Data);
				}
			}
		} catch (IOException e) {
			throw new OperationException(null, "Error creating CodeML summary file", e);
		}
	}

	@Override
	public void moveOutputFiles(CodeMLOutput output) throws OperationException {
		try {
			FileUtils.moveFile(new File("rub"), output.getRubFile());
			FileUtils.moveFile(new File("2NG.dN"), output.get2ngdnFile());
			FileUtils.moveFile(new File("2NG.dS"), output.get2ngdsFile());
			FileUtils.moveFile(new File("2NG.t"), output.get2ngtFile());
			FileUtils.moveFile(new File("lnf"), output.getLnfFile());
			FileUtils.moveFile(new File("rst"), output.getRstFile());
			FileUtils.moveFile(new File("rst1"), output.getRst1File());
		} catch (IOException e) {
			throw new OperationException(null, "Error moving CodeML output files", e);
		}
	}

	@Override
	protected String createCodeMLCtl(File nexusFile, File treeFile, File outputFile) {
		return new StringBuilder()
			.append("seqfile = ").append(nexusFile.getAbsolutePath()).append("   * sequence data file name\n")
			.append("treefile = ").append(treeFile.getAbsolutePath()).append("   * tree structure file name\n")

			.append("outfile = ").append(outputFile.getAbsolutePath()).append("      * main result file name\n")
			.append("noisy = 3   * 0,1,2,3,9: how much rubbish on the screen\n")
			.append("verbose = 0   * 1: detailed output, 0: concise output\n")
			.append("runmode = 0   * 0: user tree;  1: semi-automatic;  2: automatic\n")
			.append("              * 3: StepwiseAddition; (4,5):PerturbationNNI; -2: pairwise\n\n")

			.append("seqtype = 1   * 1:codons; 2:AAs; 3:codons-->AAs\n")
			.append("CodonFreq = 2   * 0:1/61 each, 1:F1X4, 2:F3X4, 3:codon table\n")
			.append("clock = 0   * 0: no clock, unrooted tree, 1: clock, rooted tree\n")
			.append("aaDist = 0   * 0:equal, +:geometric; -:linear, {1-5:G1974,Miyata,c,p,v}\n")
			.append("model = 0\n\n")

			.append("NSsites = ").append(this.configuration.getModels()).append("\n")
			.append("             * 0:one w; 1:NearlyNeutral; 2:PositiveSelection; 3:discrete;\n")
			.append("             * 4:freqs; 5:gamma;6:2gamma;7:beta;8:beta&w;9:beta&gamma;10:3normal\n")
			.append("icode = 0   * 0:standard genetic code; 1:mammalian mt; 2-10:see below\n")
			.append("Mgene = 0   * 0:rates, 1:separate; 2:pi, 3:kappa, 4:all\n\n")

			.append("fix_kappa = 0   * 1: kappa fixed, 0: kappa to be estimated\n")
			.append("kappa = .3   * initial or fixed kappa\n")
			.append("fix_omega = 0   * 1: omega or omega_1 fixed, 0: estimate\n")
			.append("omega = 1.3  * initial or fixed omega, for codons or codon-based AAs\n")
			.append("ncatG = 10   * # of categories in the dG or AdG models of rates\n\n")

			.append("getSE = 0   * 0: don't want them, 1: want S.E.s of estimates\n")
			.append("RateAncestor = 0   * (0,1,2): rates (alpha>0) or ancestral states (1 or 2)\n\n")

			.append("Small_Diff = .45e-6\n")
			.append("cleandata = 1  * remove sites with ambiguity data (1:yes, 0:no)?\n")
			.append("fix_blength = 0  * 0: ignore, -1: random, 1: initial, 2: fixed\n")
			.toString();
	}
}
