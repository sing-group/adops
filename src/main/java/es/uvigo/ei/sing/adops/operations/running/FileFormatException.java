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

public class FileFormatException extends Exception {
	private static final long serialVersionUID = 1L;

	private final File file;
	
	public FileFormatException(File file) {
		this.file = file;
	}

	public FileFormatException(File file, String message) {
		super(message);
		this.file = file;
	}

	public FileFormatException(File file, Throwable cause) {
		super(cause);
		this.file = file;
	}

	public FileFormatException(File file, String message, Throwable cause) {
		super(message, cause);
		this.file = file;
	}

	public FileFormatException(File file, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

}