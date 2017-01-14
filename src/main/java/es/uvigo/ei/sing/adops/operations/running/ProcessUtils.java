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
package es.uvigo.ei.sing.adops.operations.running;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class ProcessUtils {
	public static interface Printer {
		public void println(String line);
	}

	public static Printer wrap(final Logger log) {
		return new Printer() {
			@Override
			public void println(String line) {
				log.info(line);
			}
		};
	}

	public static Printer wrap(final PrintStream ps) {
		return new Printer() {
			@Override
			public void println(String line) {
				ps.println(line);
			}
		};
	}

	public static Printer wrap(final PrintWriter pw) {
		return new Printer() {
			@Override
			public void println(String line) {
				pw.println(line);
			}
		};
	}

	public static Printer[] wrap(PrintStream... streams) {
		final List<Printer> printers = new LinkedList<>();

		for (PrintStream ps : streams) {
			if (ps != null)
				printers.add(wrap(ps));
		}

		return printers.toArray(new Printer[printers.size()]);
	}

	public static Printer[] wrap(PrintWriter... writers) {
		final List<Printer> printers = new LinkedList<>();

		for (PrintWriter pw : writers) {
			if (pw != null)
				printers.add(wrap(pw));
		}

		return printers.toArray(new Printer[printers.size()]);
	}

	public static int runCommand(String command, Printer... outs) throws IOException, InterruptedException {
		return ProcessUtils.runCommand(command, null, null, outs);
	}

	public static int runCommand(String command, String[] envp, File dir, Printer... outs)
	throws IOException, InterruptedException {
		final Process process;

		if (envp == null && dir == null) {
			process = Runtime.getRuntime().exec(command);
		} else {
			process = Runtime.getRuntime().exec(command, envp, dir);
		}

		final Thread shutdownHook = new Thread() {
			@Override
			public void run() {
				process.destroy();
			};
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		try {
			if (outs.length > 0) {
				try (BufferedReader processIn = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

					String line;
					while ((line = processIn.readLine()) != null) {
						for (Printer out : outs) {
							out.println(line);
						}
					}
				}
			}

			return process.waitFor();
		} finally {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
	}
}
