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
package es.uvigo.ei.sing.adops.operations.running.mrbayes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import es.uvigo.ei.sing.adops.configuration.MrBayesConfiguration;
import es.uvigo.ei.sing.adops.datatypes.MrBayesOutput;
import es.uvigo.ei.sing.adops.operations.running.OperationException;

public class MrBayesDefaultProcessManager extends MrBayesProcessManager {
	public MrBayesDefaultProcessManager(MrBayesConfiguration configuration) throws OperationException {
		super(configuration);
	}

	@Override
	public boolean isCompatibleWith(String version) {
		return true;
	}

	@Override
	public int alignSequences(MrBayesOutput output) throws OperationException {
		try (PrintWriter logFilePW = new PrintWriter(output.getLogFile())) {
			this.addPrinter(logFilePW);

			final int state = this.runMrBayes(output.getMrBayesFile().getAbsolutePath());

			this.removePrinter(logFilePW);

			return state;
		} catch (FileNotFoundException e) {
			throw new OperationException(null, "Can't find log file", e);
		}
	}

	@Override
	public void buildSummary(MrBayesOutput output) throws OperationException {
		try (
			BufferedReader br = new BufferedReader(new FileReader(output.getLogFile()));
			PrintWriter pw = new PrintWriter(output.getPsrfFile())
		) {
			String line = null;

			// Skip lines
			while (!(line = br.readLine()).contains("Estimated marginal likelihoods"));

			// Write important lines
			do {
				pw.println(line);
				line = br.readLine().trim();
			} while (!(line.startsWith("Setting") && line.endsWith(Integer.toString(this.configuration.getTBurnin()))));
		} catch (IOException ioe) {
			throw new OperationException(null, "Error building MrBayes summary", ioe);
		} catch (NullPointerException npe) {
			throw new OperationException(null, "Error building MrBayes summary", npe);
		}
	}

	@Override
	public void createMrBayesFile(MrBayesOutput output)
	throws OperationException {
		try (
			BufferedReader br = new BufferedReader(new FileReader(output.getNexusFile()));
			PrintWriter pw = new PrintWriter(output.getMrBayesFile())
		) {
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
			pw.print("sumt burnin=");
			pw.print(this.configuration.getTBurnin());
			pw.println(";");
			pw.println("end;");
			pw.println();
			pw.flush();
		} catch (IOException ioe) {
			throw new OperationException(null, "Error creating MrBayes input file: " + output.getMrBayesFile(), ioe);
		}
	}
}
