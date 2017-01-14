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
package es.uvigo.ei.sing.adops.operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetVersions {
	public static String getTCoffeeVersion(String binPath) throws IOException {
		try {
			Process process = Runtime.getRuntime().exec(binPath + " -version");

			BufferedReader procBR = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String versionLine = procBR.readLine();

			process.destroy();

			final int versionIndex = versionLine.indexOf("Version");
			if (versionIndex >= 0) {
				final String versionSubstring = versionLine.substring(versionIndex + 8);
				final int spaceIndex = versionSubstring.indexOf(' ');
				final int parenIndex = versionSubstring.indexOf(')');
				final int index = spaceIndex < 0 || parenIndex < 0 ? Math.max(spaceIndex, parenIndex) : Math.min(spaceIndex, parenIndex);

				if (index < 0)
					throw new IOException("Missing version");
				else
					return versionSubstring.substring(0, index);

			} else {
				throw new IOException("Missing program");
			}
		} catch (Exception e) {
			throw new IOException("Missing program");
		}
	}

	public static String getMrBayesVersion(String binPath) throws IOException {
		try {
			File tmpFile = File.createTempFile("ver", null);
			tmpFile.deleteOnExit();
			Process process = Runtime.getRuntime().exec(binPath + " " + tmpFile.getAbsolutePath());

			BufferedReader procBR = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String versionLine = null;
			do {
				versionLine = procBR.readLine();
			} while (versionLine != null && !versionLine.contains("MrBayes v"));

			process.destroy();

			if (versionLine != null) {
				return versionLine.substring(versionLine.indexOf(" v") + 2);
			} else {
				throw new IOException("Missing program");
			}
		} catch (Exception e) {
			throw new IOException("Missing program");
		}
	}

	public static String getCodeMLVersion(String binPath) throws IOException {
		try {
			File tmpFile = File.createTempFile("ver", null);
			tmpFile.deleteOnExit();
			Process process = Runtime.getRuntime().exec(binPath + " " + tmpFile.getAbsolutePath());

			BufferedReader procBR = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String versionLine = procBR.readLine();

			process.destroy();

			if (versionLine.contains("version ")) {
				return versionLine.substring(versionLine.indexOf("version ") + 8, versionLine.indexOf(","));
			} else {
				throw new IOException("Missing program");
			}
		} catch (Exception e) {
			throw new IOException("Missing program");
		}
	}

}
