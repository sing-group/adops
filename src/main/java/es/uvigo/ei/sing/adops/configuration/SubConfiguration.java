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
package es.uvigo.ei.sing.adops.configuration;

import java.util.Observable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class SubConfiguration extends Observable implements IConfiguration {
	private final Configuration properties;
	private final String prefix;

	public SubConfiguration(Configuration properties, String prefix) {
		this.properties = properties;
		this.prefix = prefix;
	}

	@Override
	public String getProperty(String key) {
		return this.properties.getProperty(this.prefix + '.' + key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		return this.properties.getProperty(this.prefix + '.' + key, defaultValue);
	}

	@Override
	public void setProperty(String key, String value) {
		this.properties.setProperty(this.prefix + '.' + key, value);

		this.setChanged();
		this.notifyObservers(this.prefix + '.' + key);
	}

	@Override
	public Set<String> listProperties() {
		final SortedSet<String> propNames = new TreeSet<>();

		for (String propName : this.properties.listProperties()) {
			if (propName.startsWith(this.prefix + '.')) {
				propNames.add(propName.substring(0, propName.indexOf('.')));
			}
		}

		return propNames;
	}

	@Override
	public void clear() {
		this.properties.clear();

		this.setChanged();
		this.notifyObservers();
	}
}
