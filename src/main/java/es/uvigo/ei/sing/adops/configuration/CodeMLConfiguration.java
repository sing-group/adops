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


public class CodeMLConfiguration extends SubConfiguration implements ExecutableConfiguration {
	public static final String PROPERTIES_PREFIX = "codeml";
	
	public static final String PROPERTY_PARAMETERS = "params";
	public static final String PROPERTY_MODELS = "models";

	public CodeMLConfiguration(Configuration configuration) {
		super(configuration, CodeMLConfiguration.PROPERTIES_PREFIX);
	}
	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.sing.adops.configuration.ExecutableConfiguration#getDirectory()
	 */
	@Override
	public String getDirectory() {
		return this.getProperty(ExecutableConfiguration.PROPERTY_DIRECTORY);
	}
	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.sing.adops.configuration.ExecutableConfiguration#setDirectory(java.lang.String)
	 */
	@Override
	public void setDirectory(String directory) {
		this.setProperty(ExecutableConfiguration.PROPERTY_DIRECTORY, directory);
	}
	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.sing.adops.configuration.ExecutableConfiguration#getBinary()
	 */
	@Override
	public String getBinary() {
		return this.getProperty(ExecutableConfiguration.PROPERTY_BINARY);
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.sing.adops.configuration.ExecutableConfiguration#setBinary(java.lang.String)
	 */
	@Override
	public void setBinary(String binary) {
		this.setProperty(ExecutableConfiguration.PROPERTY_BINARY, binary);
	}
	
	public String getParameters() {
		return this.getProperty(CodeMLConfiguration.PROPERTY_PARAMETERS);
	}
	
	public void setParameters(String parameters) {
		this.setProperty(CodeMLConfiguration.PROPERTY_PARAMETERS, parameters);
	}

	public String getModels() {
		return this.getProperty(CodeMLConfiguration.PROPERTY_MODELS);
	}
	
	public void setModels(String models) {
		this.setProperty(CodeMLConfiguration.PROPERTY_MODELS, models);
	}

}
