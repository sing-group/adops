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
package es.uvigo.ei.sing.adops.operations.running.mrbayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.sing.adops.configuration.MrBayesConfiguration;
import es.uvigo.ei.sing.adops.datatypes.MrBayesOutput;
import es.uvigo.ei.sing.adops.operations.running.OperationException;

public class MrBayes3_2ProcessManager extends MrBayesDefaultProcessManager {
	private static final String[] SUPPORTED_VERSIONS = { "3.2.1", "3.2.2" };

	public MrBayes3_2ProcessManager(MrBayesConfiguration configuration) throws OperationException {
		super(configuration);
	}

	@Override
	public boolean isCompatibleWith(String version) {
		for (String supportedVersion : SUPPORTED_VERSIONS) {
			if (version.startsWith(supportedVersion))
				return true;
		}
		
		return false;
	}

	@Override
	public void buildSummary(MrBayesOutput output) throws OperationException {
		try {
			FileUtils.moveFile(
				new File(output.getConFile().getAbsolutePath() + ".tre"), 
				output.getConFile()
			);
			
			final List<String> lines = FileUtils.readLines(output.getConFile());
			final ListIterator<String> itLines = lines.listIterator();
			while (itLines.hasNext()) {
				final String line = itLines.next();
				
				if (line.contains("tree con_50_majrule")) {
					final String[] lineSplit = line.split("=");
					final String tree = lineSplit[1].trim();
					
					itLines.set(lineSplit[0] + "= " + Newick.parse(tree.trim()));
				}
			}
			
			FileUtils.writeLines(output.getConFile(), lines);
			
			super.buildSummary(output);
		} catch (Exception e) {
			throw new OperationException("Error while working with consensus tree", e);
		}
	}
	
	@Override
	public void createMrBayesFile(MrBayesOutput output)
			throws OperationException {
		BufferedReader br = null;
		PrintWriter pw = null;
		
		try {
			br = new BufferedReader(new FileReader(output.getNexusFile()));
			pw = new PrintWriter(output.getMrBayesFile());
			
			
			// Nexus data copying and nchar extraction
			pw.println(br.readLine());
			pw.println(br.readLine());
			
			final String ncharLine = br.readLine();
			pw.println(ncharLine);
			
			final int nchar = Integer.parseInt(ncharLine.split("nchar=")[1].split(";")[0]);
			
			String line;
			while ((line = br.readLine()) != null) {
				pw.println(line);
			}

			// MrBayes parameter creation
			pw.println("begin mrbayes;");
			pw.println("set autoclose=yes nowarn=yes;"); 
			pw.print("charset first_pos  = 1-");
			pw.print(nchar);
			pw.println("\\3;");
			pw.print("charset second_pos = 2-");
			pw.print(nchar);
			pw.println("\\3;");
			pw.print("charset third_pos  = 3-");
			pw.print(nchar);
			pw.println("\\3;");
			pw.println("partition by_codon = 3:first_pos,second_pos,third_pos;");
			pw.println("set partition=by_codon;");
			pw.println("lset nst=6 rates=invgamma;");
			pw.println("unlink shape=(3);");
			pw.print("mcmc ngen=");
			pw.print(this.configuration.getNumOfGenerations());
			pw.println(";");
			pw.print("sump burnin=");
			pw.print(this.configuration.getPBurnin());
			pw.println(";");
			pw.print("sumt conformat=simple burnin=");
			pw.print(this.configuration.getTBurnin());
			pw.println(";");
        	pw.println("end;");
        	pw.println();
        	pw.flush();
		} catch (IOException ioe) {
			throw new OperationException(null, "Error creating MrBayes input file: " + output.getMrBayesFile(), ioe);
		} finally {
			if (br != null)
				try { br.close(); }
				catch (IOException ioe) {}
			if (pw != null)
				pw.close();
		}
	}
}
