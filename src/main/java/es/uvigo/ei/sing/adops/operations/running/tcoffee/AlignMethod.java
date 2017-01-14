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
package es.uvigo.ei.sing.adops.operations.running.tcoffee;

public enum AlignMethod {
	CLUSTALW("clustalw", "-method=clustalw_msa"),
	CLUSTALW2("clustalw2", "-method=clustalw2_msa"),
	MUSCLE("muscle", "-method=muscle_msa"),
	EXPRESSO("expresso", "-mode=expresso"),
	T_COFFEE("t_coffee", ""),
	MULTIPLE("multiple", "-method=clustalw2_msa,kalign_msa,t_coffee_msa,mafft_msa,muscle_msa,pcma_msa"),
	ACCURATE("accurate", "-mode=accurate");

	private String name, tcoffeeString;

	private AlignMethod(String name, String tcoffeeString) {
		this.name = name;
		this.tcoffeeString = tcoffeeString;
	}

	public String getName() {
		return name;
	}

	public String getTCoffeeString() {
		return this.tcoffeeString;
	}
}
