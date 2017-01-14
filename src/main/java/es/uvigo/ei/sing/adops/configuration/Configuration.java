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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import es.uvigo.ei.sing.adops.Utils;

public class Configuration extends Observable implements IConfiguration, Observer {
	private final static Logger LOG = Logger.getLogger(Configuration.class);

	public static final String PROPERTY_INPUT_NAMES = "input.names";
	public static final String PROPERTY_INPUT_ORIGINAL_FASTA = "input.original_fasta";
	public static final String PROPERTY_INPUT_FASTA = "input.fasta";
	public static final String PROPERTY_INPUT_SEQUENCES = "input.sequences";

	public static final String PROPERTY_EXPORT_DEFAULT_PATH = "export.default.path";

	private static final String SYSTEM_CONF_FILE = "system.conf";
	private final static Configuration SYSTEM_CONFIGURATION = new Configuration(new File(Configuration.SYSTEM_CONF_FILE));

	private final Configuration parent;
	private final Properties properties;

	private final MrBayesConfiguration mrBayesConfiguration;
	private final CodeMLConfiguration codeMLConfiguration;
	private final TCoffeeConfiguration tcoffeeConfiguration;

	private final Map<Class<? extends SubConfiguration>, SubConfiguration> subConfigurationsMap;

	public Configuration(File propertiesFile) {
		this(null, propertiesFile);
	}

	public Configuration(Configuration parent, File propertiesFile) {
		this(parent, Utils.loadOrCreateProperties(propertiesFile, Configuration.LOG));
	}

	public Configuration(Configuration parent) {
		this(parent, new Properties());
	}

	public Configuration(Properties properties) {
		this(null, properties);
	}

	public Configuration(Configuration parent, Properties properties) {
		super();
		this.parent = parent;
		this.properties = properties;

		this.tcoffeeConfiguration = new TCoffeeConfiguration(this);
		this.mrBayesConfiguration = new MrBayesConfiguration(this);
		this.codeMLConfiguration = new CodeMLConfiguration(this);

		this.tcoffeeConfiguration.addObserver(this);
		this.mrBayesConfiguration.addObserver(this);
		this.codeMLConfiguration.addObserver(this);

		this.subConfigurationsMap = new HashMap<>();
		this.subConfigurationsMap.put(TCoffeeConfiguration.class, this.tcoffeeConfiguration);
		this.subConfigurationsMap.put(MrBayesConfiguration.class, this.mrBayesConfiguration);
		this.subConfigurationsMap.put(CodeMLConfiguration.class, this.codeMLConfiguration);
	}

	@SuppressWarnings("unchecked")
	public <C extends SubConfiguration> C getSubConfiguration(Class<C> configClass) {
		return (C) this.subConfigurationsMap.get(configClass);
	}

	public MrBayesConfiguration getMrBayesConfiguration() {
		return this.mrBayesConfiguration;
	}

	public CodeMLConfiguration getCodeMLConfiguration() {
		return this.codeMLConfiguration;
	}

	public TCoffeeConfiguration getTCoffeeConfiguration() {
		return this.tcoffeeConfiguration;
	}

	public static Configuration getSystemConfiguration() {
		return Configuration.SYSTEM_CONFIGURATION;
	}

	public String getFastaFile() {
		return this.getProperty(Configuration.PROPERTY_INPUT_FASTA);
	}

	public void setFastaFile(String inputFasta) {
		this.setProperty(Configuration.PROPERTY_INPUT_FASTA, inputFasta);
	}

	public String getOriginalFastaFile() {
		return this.getProperty(Configuration.PROPERTY_INPUT_ORIGINAL_FASTA);
	}

	public void setOriginalFastaFile(String inputOriginalFasta) {
		this.setProperty(Configuration.PROPERTY_INPUT_ORIGINAL_FASTA, inputOriginalFasta);
	}

	public String getNamesFile() {
		return this.getProperty(Configuration.PROPERTY_INPUT_NAMES);
	}

	public void setNamesFile(String namesFile) {
		this.setProperty(Configuration.PROPERTY_INPUT_NAMES, namesFile);
	}

	public String getInputSequences() {
		return this.getProperty(Configuration.PROPERTY_INPUT_SEQUENCES);
	}

	public void setInputSequences(String sequences) {
		this.setProperty(Configuration.PROPERTY_INPUT_SEQUENCES, sequences);
	}

	public String getExportDefaultPath() {
		final String path = this.getProperty(Configuration.PROPERTY_EXPORT_DEFAULT_PATH);
		if (path == null || path.isEmpty()) {
			return System.getProperty("user.home");
		} else {
			return path;
		}
	}

	public void setExportDefaultPath(String path) {
		this.setProperty(Configuration.PROPERTY_EXPORT_DEFAULT_PATH, path);
	}

	@Override
	public String getProperty(String key) {
		if (this.properties.containsKey(key)) {
			return this.properties.getProperty(key);
		} else {
			if (this.parent != null)
				return this.parent.getProperty(key);
			else
				return null;
		}
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		if (this.properties.containsKey(key)) {
			return this.properties.getProperty(key);
		} else {
			if (this.parent != null)
				return this.parent.getProperty(key, defaultValue);
			else
				return defaultValue;
		}
	}

	@Override
	public void setProperty(String key, String value) {
		if (
			key.equals(TCoffeeConfiguration.PROPERTY_MAX_SEQS) ||
				key.equals(TCoffeeConfiguration.PROPERTY_ALIGN_METHOD)
		)
			Thread.dumpStack();

		if (this.parent != null) {
			if (value.equals(this.parent.getProperty(key))) {
				this.properties.remove(key);
			} else {
				this.properties.setProperty(key, value);
			}
		} else {
			this.properties.setProperty(key, value);
		}

		this.setChanged();
		this.notifyObservers(key);
	}

	@Override
	public Set<String> listProperties() {
		final SortedSet<String> properties = new TreeSet<>(this.properties.stringPropertyNames());
		if (this.parent != null) {
			properties.addAll(this.parent.listProperties());
		}

		return properties;
	}

	public Set<String> listChangedProperties() {
		return this.properties.stringPropertyNames();
	}

	@Override
	public void clear() {
		this.properties.clear();

		this.setChanged();
		this.notifyObservers();
	}

	public void loadProperties(File propertiesFile) {
		try {
			Utils.loadProperties(propertiesFile, this.properties, Configuration.LOG);
		} catch (IOException e) {
			Configuration.LOG.warn("Properties could not be loaded from " + propertiesFile.getAbsolutePath(), e);
		}
	}

	public Properties toProperties(boolean full) {
		final Properties props = new Properties();

		for (String key : full ? this.listProperties() : this.listChangedProperties()) {
			props.put(key, this.getProperty(key));
		}

		return props;
	}

	public boolean storeProperties(File propertiesFile) {
		return Utils.storeProperties(this.properties, propertiesFile, Configuration.LOG);
	}

	public void addProperties(Configuration configuration) {
		this.properties.putAll(configuration.properties);
	}

	@Override
	public void update(Observable o, Object arg) {
		this.setChanged();
		this.notifyObservers(arg);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		for (String key : this.listProperties()) {
			sb.append(key).append('=').append(this.getProperty(key)).append('\n');
		}

		return sb.toString();
	}
}
