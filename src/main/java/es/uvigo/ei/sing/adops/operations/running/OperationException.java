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
package es.uvigo.ei.sing.adops.operations.running;

public class OperationException extends Exception {
	private static final long serialVersionUID = 1L;

	private final Command command;

	public OperationException(Command command) {
		this.command = command;
	}

	public OperationException(Command command, String message) {
		super(message);
		this.command = command;
	}

	public OperationException(Command command, Throwable cause) {
		super(cause);
		this.command = command;
	}

	public OperationException(Command command, String message, Throwable cause) {
		super(message, cause);
		this.command = command;
	}

	public OperationException(String message) {
		super(message);
		this.command = null;
	}

	public OperationException(String message, Throwable cause) {
		super(message, cause);
		this.command = null;
	}

	public OperationException() {
		this.command = null;
	}

	public OperationException(Throwable cause) {
		super(cause);
		this.command = null;
	}

	public Command getCommand() {
		return command;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage();
	}
}
