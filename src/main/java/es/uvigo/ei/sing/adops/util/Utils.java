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
package es.uvigo.ei.sing.adops.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public final class Utils {
	private final static Logger LOG = Logger.getLogger(Utils.class);

	private Utils() {}

	public static interface LinesFilter {
		public boolean accept(int index, String line);
	}

	public static List<String> replaceNames(Map<String, String> names, List<String> strings) {
		return replaceNames(names, strings, null);
	}

	public static List<String> replaceNames(Map<String, String> names, List<String> strings, LinesFilter filter) {
		final List<String> replaced = new ArrayList<>(strings.size());

		int index = 0;
		for (String string : strings) {
			if (filter == null || filter.accept(index++, string))
				replaced.add(replaceNames(names, string));
			else
				replaced.add(string);
		}

		return replaced;
	}

	public static String replaceNames(Map<String, String> names, String string) {
		final StringBuilder sb = new StringBuilder();

		String first;
		int index;
		do {
			first = null;
			index = -1;

			SortedSet<String> sortedNames = new TreeSet<>(
				new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						if (o2.length() == o1.length())
							return o1.compareTo(o2);
						else
							return o2.length() - o1.length();
					}
				}
			);

			sortedNames.addAll(names.keySet());

			for (String name : sortedNames) {
				final int indexOf = string.indexOf(name);

				if (indexOf != -1 && (index == -1 || indexOf < index)) {
					index = indexOf;
					first = name;
				}
			}

			if (index > -1) {
				if (index > 0)
					sb.append(string.substring(0, index));
				sb.append(names.get(first));
				string = string.substring(index + first.length());
			} else {
				sb.append(string);
			}
		} while (index >= 0 && !string.isEmpty());

		return sb.toString();
	}

	public static Properties loadProperties(File propertiesFile) throws IOException {
		return Utils.loadProperties(propertiesFile, new Properties(), Utils.LOG);
	}

	public static Properties loadProperties(File propertiesFile, Properties properties) throws IOException {
		return Utils.loadProperties(propertiesFile, properties, Utils.LOG);
	}

	public static Properties loadProperties(File propertiesFile, Logger log) throws IOException {
		return Utils.loadProperties(propertiesFile, new Properties(), log);
	}

	public static Properties loadOrCreateProperties(File propertiesFile) throws IOException {
		return Utils.loadOrCreateProperties(propertiesFile, new Properties(), Utils.LOG);
	}

	public static Properties loadOrCreateProperties(File propertiesFile, Logger log) {
		return Utils.loadOrCreateProperties(propertiesFile, new Properties(), log);
	}

	public static Properties loadOrCreateProperties(File propertiesFile, Properties properties) {
		return Utils.loadOrCreateProperties(propertiesFile, properties, Utils.LOG);
	}

	public static Properties loadProperties(File propertiesFile, Properties properties, Logger log) throws IOException {
		final String fileName = propertiesFile.getName();

		if (propertiesFile.exists() && propertiesFile.canRead()) {
			try (Reader reader = new FileReader(propertiesFile)) {
				properties.load(reader);

				log.debug(fileName + " properties loaded");
			} catch (Exception e) {
				log.error(fileName + " file could not be loaded", e);
				
				throw new IOException(fileName + " could not be loaded", e);
			}
		} else {
			log.error(fileName + " file could not be read");
			
			throw new RuntimeException(fileName + " could not be read");
		}

		return properties;
	}

	public static Properties loadOrCreateProperties(File propertiesFile, Properties properties, Logger log) {
		final String fileName = propertiesFile.getName();

		if (propertiesFile.exists()) {
			try (Reader reader = new FileReader(propertiesFile)) {
				properties.load(reader);

				log.debug(fileName + " properties loaded");
			} catch (Exception e) {
				log.error(fileName + " file could not be loaded", e);
				
				throw new RuntimeException(fileName + " could not be loaded", e);
			}
		} else {
			try {
				propertiesFile.createNewFile();

				log.debug(fileName + " file created");
			} catch (IOException e) {
				log.error(fileName + " file could not be created", e);
				
				throw new RuntimeException(fileName + " could not be created", e);
			}
		}

		return properties;
	}

	// TODO: Throw exception?
	public static boolean storeProperties(Properties properties, File propFile, Logger log) {
		try (FileWriter fw = new FileWriter(propFile)) {
			properties.store(fw, null);

			log.debug("Properties stored in file: " + propFile);

			return true;
		} catch (IOException e) {
			log.error("Error storing properties in file: " + propFile, e);

			return false;
		}
	}

	// TODO: Throw exception?
	public static boolean storeAllProperties(Properties properties, File propFile, Logger log) {
		final Properties props = new Properties();
		for (String key : properties.stringPropertyNames()) {
			props.put(key, properties.getProperty(key));
		}

		try (FileWriter fw = new FileWriter(propFile)) {
			properties.store(fw, null);

			log.debug("Properties stored in file: " + propFile);

			return true;
		} catch (IOException e) {
			log.error("Error storing properties in file: " + propFile, e);

			return false;
		}
	}
	
}
