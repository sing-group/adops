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

import java.io.File;
import java.util.Arrays;

public class Command {
	private final String command;
	private final String[] envp;
	private final File directory;
	
	public Command(String command) {
		this(command, null, null);
	}
	
	public Command(String command, String[] envp, File directory) {
		if (command == null || command.trim().isEmpty()) {
			throw new IllegalArgumentException("Command can't be null or empty");
		}
		
		this.command = command;
		this.envp = envp;
		this.directory = directory;
	}
	
	public String getCommand() {
		return command;
	}
	
	public File getDirectory() {
		return directory;
	}
	
	public String[] getEnvp() {
		return envp;
	}
	
	public boolean hasAdvancedParameters() {
		return this.envp != null && this.directory != null;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.command);
		
		if (this.hasAdvancedParameters()) {
			sb.append(" (envp = ")
				.append(Arrays.toString(this.envp))
				.append(", directory = ")
				.append(this.directory)
				.append(")");
		}
		
		return sb.toString();
	}
}
