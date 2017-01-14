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
package es.uvigo.ei.sing.adops.views;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

public class SingleListSelectionPanel<T> extends ListSelectionPanel {
	private static final long serialVersionUID = 1L;

	private final CustomListModel<T> lmUnselected;
	private final CustomListModel<T> lmSelected;
	private final JList<T> listUnselected;
	private final JList<T> listSelected;

	public SingleListSelectionPanel(final List<T> values, String selectedTitle, String unselectedTitle) {
		super(new GridBagLayout());
		final GridBagConstraints gbcRight = new GridBagConstraints();
		final GridBagConstraints gbcCenter = new GridBagConstraints();
		final GridBagConstraints gbcLeft = new GridBagConstraints();
		gbcLeft.fill = GridBagConstraints.BOTH;
		gbcLeft.gridx = 0;
		gbcLeft.weightx = 0.5d;
		gbcLeft.weighty = 1d;
		gbcCenter.gridx = 1;
		gbcCenter.weightx = 0d;
		gbcCenter.ipadx = 10;
		gbcRight.fill = GridBagConstraints.BOTH;
		gbcRight.gridx = 2;
		gbcRight.weightx = 0.5d;
		gbcRight.weighty = 1d;

		this.lmSelected = new CustomListModel<>(new Vector<>(values));
		this.lmUnselected = new CustomListModel<>(new Vector<T>());

		this.listSelected = new JList<>(this.lmSelected);
		this.listUnselected = new JList<>(this.lmUnselected);

		final JLabel lblSelected = new JLabel(selectedTitle);
		final JLabel lblUnselected = new JLabel(unselectedTitle);
		lblUnselected.setFont(lblUnselected.getFont().deriveFont(Font.BOLD));
		lblSelected.setFont(lblSelected.getFont().deriveFont(Font.BOLD));
		lblSelected.setHorizontalAlignment(JLabel.RIGHT);
		final Border border = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		lblUnselected.setBorder(border);
		lblSelected.setBorder(border);

		final JPanel panelUnselected = new JPanel(new BorderLayout());
		final JPanel panelUnselectedButtons = createSelectionPanel(listUnselected);
		final JPanel panelCenter = createButtonsPanel(
			listUnselected, lmUnselected, listSelected, lmSelected, false
		);
		final JPanel panelSelected = new JPanel(new BorderLayout());
		final JPanel panelSelectedButtons = createSelectionPanel(listSelected);
		panelUnselected.setOpaque(false);
		panelUnselectedButtons.setOpaque(false);
		panelCenter.setOpaque(false);
		panelSelected.setOpaque(false);
		panelSelectedButtons.setOpaque(false);

		panelUnselected.add(lblUnselected, BorderLayout.NORTH);
		panelUnselected.add(new JScrollPane(listUnselected), BorderLayout.CENTER);
		panelUnselected.add(panelUnselectedButtons, BorderLayout.SOUTH);

		panelSelected.add(lblSelected, BorderLayout.NORTH);
		panelSelected.add(new JScrollPane(listSelected), BorderLayout.CENTER);
		panelSelected.add(panelSelectedButtons, BorderLayout.SOUTH);

		this.add(panelUnselected, gbcLeft);
		this.add(panelCenter, gbcCenter);
		this.add(panelSelected, gbcRight);

		final ListModelChangedEventListener listModelChangedEventListener = new ListModelChangedEventListener(this);
		this.lmSelected.addListDataListener(listModelChangedEventListener);
		this.lmUnselected.addListDataListener(listModelChangedEventListener);
	}

	public List<T> getSelectedValues() {
		return this.lmSelected.getValues();
	}

	public List<T> getUnselectedValues() {
		return this.lmUnselected.getValues();
	}
}
