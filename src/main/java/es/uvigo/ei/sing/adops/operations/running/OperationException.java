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

public class OperationException extends Exception {
	private static final long serialVersionUID = 1L;

	private final String command;

	public OperationException(String command) {
		this.command = command;
	}

	public OperationException(String command, String message) {
		super(message);
		this.command = command;
	}

	public OperationException(String command, Throwable cause) {
		super(cause);
		this.command = command;
	}

	public OperationException(String command, String message, Throwable cause) {
		super(message, cause);
		this.command = command;
	}

	public OperationException() {
		super();
		this.command = "";
	}

	public OperationException(Throwable cause) {
		super(cause);
		this.command = "";
	}

	public String getCommand() {
		return command;
	}
}
