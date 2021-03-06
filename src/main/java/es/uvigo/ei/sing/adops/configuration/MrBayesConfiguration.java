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

public class MrBayesConfiguration extends SubConfiguration implements ExecutableConfiguration {
	private static final String VALUE_NGEN = "500000";

	private static final String VALUE_PBURNIN = "1250";
	private static final String VALUE_TBURNIN = "1250";

	public static final String PROPERTIES_PREFIX = "mrbayes";

	public static final String PROPERTY_MPICH = "mpich";
	public static final String PROPERTY_PARAMETERS = "params";
	public static final String PROPERTY_NGEN = "ngen";
	public static final String PROPERTY_PBURNIN = "pburnin";
	public static final String PROPERTY_TBURNIN = "tburnin";

	public MrBayesConfiguration(Configuration configuration) {
		super(configuration, MrBayesConfiguration.PROPERTIES_PREFIX);
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

	public String getMpich() {
		return this.getProperty(MrBayesConfiguration.PROPERTY_MPICH);
	}

	public void setMpich(String mpich) {
		this.setProperty(MrBayesConfiguration.PROPERTY_MPICH, mpich);
	}

	public String getParameters() {
		return this.getProperty(MrBayesConfiguration.PROPERTY_PARAMETERS);
	}

	public void setParameters(String parameters) {
		this.setProperty(MrBayesConfiguration.PROPERTY_PARAMETERS, parameters);
	}

	public int getNumOfGenerations() {
		return Integer.parseInt(this.getProperty(MrBayesConfiguration.PROPERTY_NGEN, MrBayesConfiguration.VALUE_NGEN));
	}

	public void setNumOfGenerations(int ngen) {
		this.setProperty(MrBayesConfiguration.PROPERTY_NGEN, Integer.toString(ngen));
	}

	public int getPBurnin() {
		return Integer.parseInt(this.getProperty(MrBayesConfiguration.PROPERTY_PBURNIN, MrBayesConfiguration.VALUE_PBURNIN));
	}

	public void setPBurnin(int burnin) {
		this.setProperty(MrBayesConfiguration.PROPERTY_PBURNIN, Integer.toString(burnin));
	}

	public int getTBurnin() {
		return Integer.parseInt(this.getProperty(MrBayesConfiguration.PROPERTY_TBURNIN, MrBayesConfiguration.VALUE_TBURNIN));
	}

	public void setTBurnin(int burnin) {
		this.setProperty(MrBayesConfiguration.PROPERTY_TBURNIN, Integer.toString(burnin));
	}

}
