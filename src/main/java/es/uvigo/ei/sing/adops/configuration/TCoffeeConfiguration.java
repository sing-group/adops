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

import es.uvigo.ei.sing.adops.operations.running.tcoffee.AlignMethod;


public class TCoffeeConfiguration extends SubConfiguration implements ExecutableConfiguration {
	public static final String PROPERTIES_PREFIX = "tcoffee";
	
	public static final String VALUE_MAX_SEQS = "0";
	public static final String VALUE_ALIGN_METHOD = "CLUSTALW2";
	public static final String VALUE_MIN_SCORE = "3";
	
	public static final String PROPERTY_PARAMETERS = "params";
	public static final String PROPERTY_MAX_SEQS = "maxSeqs";
	public static final String PROPERTY_ALIGN_METHOD = "alignMethod";
	public static final String PROPERTY_MIN_SCORE = "minScore";

	public TCoffeeConfiguration(Configuration configuration) {
		super(configuration, TCoffeeConfiguration.PROPERTIES_PREFIX);
	}
	
	@Override
	public String getDirectory() {
		return this.getProperty(ExecutableConfiguration.PROPERTY_DIRECTORY);
	}
	
	@Override
	public void setDirectory(String directory) {
		this.setProperty(ExecutableConfiguration.PROPERTY_DIRECTORY, directory);
	}
	
	@Override
	public String getBinary() {
		return this.getProperty(ExecutableConfiguration.PROPERTY_BINARY);
	}

	@Override
	public void setBinary(String binary) {
		this.setProperty(ExecutableConfiguration.PROPERTY_BINARY, binary);
	}
	
	public String getParameters() {
		return this.getProperty(TCoffeeConfiguration.PROPERTY_PARAMETERS);
	}
	
	public void setParameters(String parameters) {
		this.setProperty(TCoffeeConfiguration.PROPERTY_PARAMETERS, parameters);
	}

	public int getMaxSeqs() {
		return Integer.parseInt(this.getProperty(TCoffeeConfiguration.PROPERTY_MAX_SEQS, TCoffeeConfiguration.VALUE_MAX_SEQS));
	}
	
	public void setMaxSeqs(int maxseqs) {
		this.setProperty(TCoffeeConfiguration.PROPERTY_MAX_SEQS, Integer.toString(maxseqs));
	}

	public AlignMethod getAlignMethod() {
		return AlignMethod.valueOf(this.getProperty(TCoffeeConfiguration.PROPERTY_ALIGN_METHOD, TCoffeeConfiguration.VALUE_ALIGN_METHOD));
	}
	
	public void setAlignMethod(AlignMethod alignMethod) {
		this.setProperty(TCoffeeConfiguration.PROPERTY_ALIGN_METHOD, alignMethod.name());
	}

	public int getMinScore() {
		return Integer.parseInt(this.getProperty(TCoffeeConfiguration.PROPERTY_MIN_SCORE, TCoffeeConfiguration.VALUE_MIN_SCORE));
	}
	
	public void setMinScore(int minScore) {
		this.setProperty(TCoffeeConfiguration.PROPERTY_MIN_SCORE, Integer.toString(minScore));
	}
}
