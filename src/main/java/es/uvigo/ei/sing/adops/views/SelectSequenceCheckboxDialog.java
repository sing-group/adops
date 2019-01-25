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
package es.uvigo.ei.sing.adops.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.sing.adops.datatypes.ProjectExperiment;

public class SelectSequenceCheckboxDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final SortedSet<Integer> selectedIndexes;
	private final int countSequences;

	public SelectSequenceCheckboxDialog(final ProjectExperiment experiment) {
		super(Workbench.getInstance().getMainFrame(), "Select Sequences", true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setMinimumSize(new Dimension(360, 240));

		final List<String> names = experiment.listSequenceNames();
		final List<String> selectedNames = experiment.listSelectedSequenceName();

		this.selectedIndexes = new TreeSet<>();
		this.countSequences = names.size();

		final JPanel mainPanel = new JPanel(new BorderLayout());

		final JPanel sequencesPanel = new JPanel(new GridLayout(names.size(), 1));
		sequencesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		int i = 1;
		for (String name : names) {
			final int index = i++;
			final JCheckBox cbSequence = new JCheckBox(
				new AbstractAction(name) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						if (((JCheckBox) e.getSource()).isSelected()) {
							SelectSequenceCheckboxDialog.this.selectedIndexes.add(index);
						} else {
							SelectSequenceCheckboxDialog.this.selectedIndexes.remove(index);
						}
					}
				}
			);

			if (selectedNames.contains(name)) {
				cbSequence.setSelected(true);
				this.selectedIndexes.add(index);
			} else {
				cbSequence.setSelected(false);
			}

			sequencesPanel.add(cbSequence);
		}

		final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JButton btnAccept = new JButton(
			new AbstractAction("Accept") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					SelectSequenceCheckboxDialog.this.setVisible(false);
				}
			}
		);
		buttonsPanel.setBackground(Color.WHITE);

		buttonsPanel.setBorder(BorderFactory.createEtchedBorder());

		buttonsPanel.add(btnAccept);

		if (names.size() > 10) {
			mainPanel.add(new JScrollPane(sequencesPanel), BorderLayout.CENTER);
		} else {
			mainPanel.add(sequencesPanel, BorderLayout.CENTER);
		}
		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

		this.setContentPane(mainPanel);

		this.pack();
		this.setLocationRelativeTo(this.getParent());
	}

	public SortedSet<Integer> getSelectedIndexes() {
		return this.selectedIndexes;
	}

	public String getSelectedIndexesAsString() {
		if (this.selectedIndexes.size() == this.countSequences) {
			return "";
		} else {
			final StringBuilder sb = new StringBuilder();

			for (Integer index : this.selectedIndexes) {
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(index);
			}

			return sb.toString();
		}
	}
}
