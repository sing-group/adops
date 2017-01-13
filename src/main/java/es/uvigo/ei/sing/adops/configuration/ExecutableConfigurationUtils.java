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
package es.uvigo.ei.sing.adops.configuration;

import java.io.File;

public final class ExecutableConfigurationUtils {
	private ExecutableConfigurationUtils() {}
	
	public static String createExecutableCommand(ExecutableConfiguration configuration) {
		return ExecutableConfigurationUtils.createExecutableCommand(configuration, null, null);
	}
	
	public static String createExecutableCommand(ExecutableConfiguration configuration, String prefix, String suffix) {
		final StringBuilder sb = prefix == null? 
			new StringBuilder():
			new StringBuilder(prefix);
		
		final String binDir = configuration.getDirectory();
		final String bin = configuration.getBinary();
		
		if (binDir != null && !binDir.trim().isEmpty()) {
			sb.append(binDir.trim());
			if (!binDir.endsWith(File.separator))
				sb.append(File.separator);
		}
		if (bin != null && !bin.trim().isEmpty()) 
			sb.append(bin.trim());
		
		if (suffix != null)
			sb.append(suffix);
		
		return sb.toString();
	}
}
