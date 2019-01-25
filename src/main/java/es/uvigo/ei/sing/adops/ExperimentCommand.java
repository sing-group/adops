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
package es.uvigo.ei.sing.adops;

import java.util.Arrays;
import java.util.List;

import es.uvigo.ei.sing.yacli.Command;
import es.uvigo.ei.sing.yacli.Option;
import es.uvigo.ei.sing.yacli.Parameters;

public class ExperimentCommand implements Command {
	private static final Option OPTION_CONFIGURATION_FILE =
		new Option("ConfigurationFile", "cf", "Experiment configuration file", true, true);

	private static final List<Option> OPTIONS = Arrays.asList(
		ExperimentCommand.OPTION_CONFIGURATION_FILE
	);

	@Override
	public String getName() {
		return "run";
	}

	@Override
	public String getDescription() {
		return "Runs an experiment";
	}

	@Override
	public List<Option> getOptions() {
		return ExperimentCommand.OPTIONS;
	}

	@Override
	public void execute(Parameters parameters) throws Exception {
		if (parameters.hasOption(ExperimentCommand.OPTION_CONFIGURATION_FILE)) {

		}
	}
}
