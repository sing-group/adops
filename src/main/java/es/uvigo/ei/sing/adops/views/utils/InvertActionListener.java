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
package es.uvigo.ei.sing.adops.views.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JList;

public class InvertActionListener implements ActionListener {
	private final JList<?> list;

	public InvertActionListener(JList<?> list) {
		super();
		this.list = list;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final int[] selectedIndices = this.list.getSelectedIndices();
		final int[] newSelectedIndices = new int[this.list.getModel().getSize() - selectedIndices.length];

		int index = 0;
		for (int i = 0; i < this.list.getModel().getSize(); i++) {
			if (Arrays.binarySearch(selectedIndices, i) < 0) {
				newSelectedIndices[index++] = i;
			}
		}

		this.list.setSelectedIndices(newSelectedIndices);
	}
}
