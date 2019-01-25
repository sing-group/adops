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
package es.uvigo.ei.sing.adops.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import es.uvigo.ei.sing.adops.operations.running.FileFormatException;

public final class IOUtils {
	
	private IOUtils() {}

	public static void checkIfFileIsEmpty(final File proteinFile) throws FileFormatException {
		try {
			final String content = new String(Files.readAllBytes(proteinFile.toPath()));
			
			if (content.trim().isEmpty())
				throw new FileFormatException(proteinFile, "File is empty");
		} catch (IOException ioe) {
			throw new FileFormatException(proteinFile, ioe);
		}
	}

}
