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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import es.uvigo.ei.sing.adops.operations.running.ProcessUtils.Printer;

public abstract class ProcessManager {
	protected Process activeProcess = null;

	private final Map<Object, Printer> printers;

	private final AtomicBoolean isInterrupted = new AtomicBoolean(false);

	public ProcessManager() {
		super();

		this.printers = new LinkedHashMap<>();
	}

	protected Logger getLogger() {
		return null;
	}

	public boolean addPrinter(PrintStream ps) {
		if (ps == null || this.printers.containsKey(ps)) {
			return false;
		} else {
			this.printers.put(ps, ProcessUtils.wrap(ps));
			return true;
		}
	}

	public boolean addPrinter(PrintWriter ps) {
		if (ps == null || this.printers.containsKey(ps)) {
			return false;
		} else {
			this.printers.put(ps, ProcessUtils.wrap(ps));
			return true;
		}
	}

	public boolean removePrinter(PrintStream ps) {
		return this.printers.remove(ps) != null;
	}

	public boolean removePrinter(PrintWriter pw) {
		return this.printers.remove(pw) != null;
	}

	protected Printer[] getPrinters() {
		final Printer[] psPrinters = this.printers.values().toArray(new Printer[this.printers.size()]);

		if (this.getLogger() == null) {
			return psPrinters;
		} else {
			final Printer[] printers = new Printer[psPrinters.length + 1];

			printers[0] = ProcessUtils.wrap(this.getLogger());
			System.arraycopy(psPrinters, 0, printers, 1, psPrinters.length);

			return printers;
		}
	}

	public boolean hasPrinter(PrintStream ps) {
		return this.printers.containsKey(ps);
	}

	public boolean hasPrinter(PrintWriter pw) {
		return this.printers.containsKey(pw);
	}

	public void clearPrinters() {
		this.printers.clear();
	}

	protected void println(String line) {
		if (this.getLogger() != null)
			this.getLogger().info(line);

		for (Printer ps : this.printers.values()) {
			ps.println(line);
		}
	}

	protected synchronized int runCommand(String command) throws OperationException,
		InterruptedException {
		return this.runCommand(command, this.getPrinters());
	}

	protected synchronized int runCommand(String command, String[] envp, File dir)
		throws OperationException, InterruptedException {
		return this.runCommand(command, envp, dir, this.getPrinters());
	}

	protected synchronized int runCommand(String command, Printer... outs)
		throws OperationException, InterruptedException {
		return this.runCommand(command, null, null, outs);
	}

	protected synchronized int runCommand(String command, String[] envp, File dir, Printer... outs)
	throws OperationException, InterruptedException {
		this.activeProcess = null;

		if (this.getLogger() != null)
			this.getLogger().info("Command: " + command);

		final Thread hook = new Thread() {
			@Override
			public void run() {
				if (ProcessManager.this.activeProcess != null)
					ProcessManager.this.activeProcess.destroy();
			}
		};

		try {
			Runtime.getRuntime().addShutdownHook(hook);

			if (envp == null && dir == null) {
				this.activeProcess = Runtime.getRuntime().exec(command);
			} else {
				this.activeProcess = Runtime.getRuntime().exec(command, envp, dir);
			}

			this.checkInterrupted();

			if (outs.length > 0) {
				BufferedReader processIn = null;
				try {
					processIn = new BufferedReader(
						new InputStreamReader(
							this.activeProcess.getInputStream()
						)
					);

					String line;
					while ((line = processIn.readLine()) != null) {
						this.checkInterrupted();

						for (Printer out : outs) {
							out.println(line);
						}
					}
				} finally {
					if (processIn != null)
						try {
							processIn.close();
						} catch (IOException ioe) {}
				}
			}

			this.checkInterrupted();

			return this.activeProcess.waitFor();
		} catch (InterruptedException ie) {
			this.destroyActiveProcess();

			throw ie;
		} catch (IOException ioe) {
			this.destroyActiveProcess();

			this.checkInterrupted();
			throw new OperationException(command, ioe);
		} finally {
			Runtime.getRuntime().removeShutdownHook(hook);
		}
	}

	public void interrupt() {
		synchronized (this.isInterrupted) {
			this.isInterrupted.set(true);
			this.destroyActiveProcess();
		}
	}

	protected void checkInterrupted() throws InterruptedException {
		synchronized (this.isInterrupted) {
			if (this.isInterrupted.get()) {
				throw new InterruptedException();
			}
		}
	}

	protected void destroyActiveProcess() {
		synchronized (this.isInterrupted) {
			if (this.activeProcess != null) {
				this.activeProcess.destroy();
				this.activeProcess = null;
			}
		}
	}
}
