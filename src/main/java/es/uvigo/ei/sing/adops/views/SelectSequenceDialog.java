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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.sing.adops.datatypes.ProjectExperiment;
import es.uvigo.ei.sing.adops.views.utils.AllActionListener;
import es.uvigo.ei.sing.adops.views.utils.InvertActionListener;
import es.uvigo.ei.sing.adops.views.utils.NoneActionListener;

public class SelectSequenceDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static ImageIcon ICON_ADD = new ImageIcon(SelectSequenceDialog.class.getResource("images/arrow-right.png"));
	private static ImageIcon ICON_ADD_ALL = new ImageIcon(SelectSequenceDialog.class.getResource("images/arrow-right-double.png"));
	private static ImageIcon ICON_REMOVE = new ImageIcon(SelectSequenceDialog.class.getResource("images/arrow-left.png"));
	private static ImageIcon ICON_REMOVE_ALL = new ImageIcon(SelectSequenceDialog.class.getResource("images/arrow-left-double.png"));
	
	private final ProjectExperiment experiment;
	private final Vector<String> selectedNames;
	private final Vector<String> unselectedNames;
 
	private final CustomListModel lmUnselected;
	private final CustomListModel lmSelected;
	private final JList<String> listUnselected;
	private final JList<String> listSelected;
	
	private final static class CustomListModel extends AbstractListModel<String> {
		private static final long serialVersionUID = 1L;
		private final List<String> values;
		
		public CustomListModel(List<String> values) {
			this.values = values;
		}
		
		public void update() {
			this.fireContentsChanged(this, 0, this.values.size());
		}

		@Override
		public int getSize() {
			return this.values.size();
		}

		@Override
		public String getElementAt(int index) {
			return this.values.get(index);
		}
	}
	
	public SelectSequenceDialog(final ProjectExperiment experiment) {
		super(Workbench.getInstance().getMainFrame(), "Select Sequences", true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		this.experiment = experiment;
		
		final List<String> names = experiment.listSequenceName();
		this.selectedNames = new Vector<String>(experiment.listSelectedSequenceName());
		this.unselectedNames = new Vector<String>(names);
		this.unselectedNames.removeAll(selectedNames);
		
		
		this.lmSelected = new CustomListModel(this.selectedNames);
		this.lmUnselected = new CustomListModel(this.unselectedNames);
		
		listUnselected = new JList<>(this.lmUnselected);
		listSelected = new JList<>(this.lmSelected);
		
		final JLabel lblUnselected = new JLabel("Available Sequences");
		final JLabel lblSelected = new JLabel("Selected Sequences");
		lblUnselected.setFont(lblUnselected.getFont().deriveFont(Font.BOLD));
		lblSelected.setFont(lblSelected.getFont().deriveFont(Font.BOLD));
		lblSelected.setHorizontalAlignment(JLabel.RIGHT);
		final Border border = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		lblUnselected.setBorder(border);
		lblSelected.setBorder(border);
		
		final JButton btnAdd = new JButton(SelectSequenceDialog.ICON_ADD);
		final JButton btnRemove = new JButton(SelectSequenceDialog.ICON_REMOVE);
		final JButton btnAddAll = new JButton(SelectSequenceDialog.ICON_ADD_ALL);
		final JButton btnRemoveAll = new JButton(SelectSequenceDialog.ICON_REMOVE_ALL);
		btnAdd.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		btnRemove.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		btnAddAll.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		btnRemoveAll.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		
		final JButton btnUAll = new JButton("All");
		final JButton btnUNone = new JButton("None");
		final JButton btnUInvert = new JButton("Invert");
		
		final JButton btnSAll = new JButton("All");
		final JButton btnSNone = new JButton("None");
		final JButton btnSInvert = new JButton("Invert");
		
		final JPanel panelLeft = new JPanel(new BorderLayout());
		final JPanel panelLeftButtons = new JPanel(new GridLayout(1, 3));
		final JPanel panelRight = new JPanel(new BorderLayout());
		final JPanel panelRightButtons = new JPanel(new GridLayout(1, 3));
		panelLeft.setOpaque(false);
		panelRight.setOpaque(false);
		panelLeftButtons.setOpaque(false);
		panelRightButtons.setOpaque(false);
		
		panelLeftButtons.add(btnUAll);
		panelLeftButtons.add(btnUNone);
		panelLeftButtons.add(btnUInvert);
		
		panelRightButtons.add(btnSAll);
		panelRightButtons.add(btnSNone);
		panelRightButtons.add(btnSInvert);
		
		panelLeft.add(lblUnselected, BorderLayout.NORTH);
		panelLeft.add(new JScrollPane(listUnselected), BorderLayout.CENTER);
		panelLeft.add(panelLeftButtons, BorderLayout.SOUTH);
		
		panelRight.add(lblSelected, BorderLayout.NORTH);
		panelRight.add(new JScrollPane(listSelected), BorderLayout.CENTER);
		panelRight.add(panelRightButtons, BorderLayout.SOUTH);
		
		final JPanel panelCenter = new JPanel();
		panelCenter.setOpaque(false);
		panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.Y_AXIS));
		panelCenter.add(btnAdd);
		panelCenter.add(btnRemove);
		panelCenter.add(btnAddAll);
		panelCenter.add(btnRemoveAll);
		
		final GridBagLayout layout = new GridBagLayout();
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
		
		
		final JPanel sequencesPanel = new JPanel(layout);
		sequencesPanel.setBackground(Color.WHITE);
		sequencesPanel.setBorder(BorderFactory.createEtchedBorder());
		sequencesPanel.add(panelLeft, gbcLeft);
		sequencesPanel.add(panelCenter, gbcCenter);
		sequencesPanel.add(panelRight, gbcRight);
		
		
		final JPanel mainPanel = new JPanel(new BorderLayout());
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> selectedValues = listUnselected.getSelectedValuesList();
				
				for (String selected : selectedValues) {
					unselectedNames.remove(selected);
					selectedNames.add(selected);
				}

				repaintLists();
			}
		});		
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> unselectedValues = listSelected.getSelectedValuesList();
				
				for (String unselected : unselectedValues) {
					selectedNames.remove(unselected);
					unselectedNames.add(unselected);
				}

				repaintLists();
			}
		});		
		btnAddAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedNames.addAll(unselectedNames);
				unselectedNames.clear();

				repaintLists();
			}
		});		
		btnRemoveAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				unselectedNames.addAll(selectedNames);
				selectedNames.clear();

				repaintLists();
			}
		});
		
		final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JButton btnAccept = new JButton(new AbstractAction("Accept") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				SelectSequenceDialog.this.setVisible(false);
			}
		});
		buttonsPanel.add(btnAccept);
		final JButton btnReset = new JButton(new AbstractAction("Reset") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedNames.clear();
				unselectedNames.clear();
				
				selectedNames.addAll(experiment.listSelectedSequenceName());
				unselectedNames.addAll(experiment.listSequenceName());
				unselectedNames.removeAll(selectedNames);
				
				repaintLists();
			}
		});
		buttonsPanel.add(btnReset);
		
		btnUAll.addActionListener(new AllActionListener(this.listUnselected));
		btnSAll.addActionListener(new AllActionListener(this.listSelected));
		btnUNone.addActionListener(new NoneActionListener(this.listUnselected));
		btnSNone.addActionListener(new NoneActionListener(this.listSelected));
		btnUInvert.addActionListener(new InvertActionListener(this.listUnselected));
		btnSInvert.addActionListener(new InvertActionListener(this.listSelected));
		
		mainPanel.add(sequencesPanel, BorderLayout.CENTER);
		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		
		this.setMinimumSize(new Dimension(640, 480));
		this.setSize(new Dimension(640, 480));
		this.setLocationRelativeTo(this.getParent());
	}
	
	private synchronized void repaintLists() {
		this.listSelected.clearSelection();
		this.listUnselected.clearSelection();
		
		this.lmSelected.update();
		this.lmUnselected.update();
	}
	
	public SortedSet<Integer> getSelectedIndexes() {
//		return this.selectedIndexes;
		final List<String> names = this.experiment.listSequenceName();
		final SortedSet<Integer> selectedIndex = new TreeSet<Integer>();
		
		for (String selected : this.selectedNames) {
			selectedIndex.add(names.indexOf(selected) + 1);
		}
		
		return selectedIndex;
	}
	
	public String getSelectedIndexesAsString() {
		final List<String> names = this.experiment.listSequenceName();
		if (this.selectedNames.size() == names.size()) {
			return "";
		} else {
			final StringBuilder sb = new StringBuilder();
			
			for (Integer index : this.getSelectedIndexes()) {
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(index);
			}
			
			return sb.toString();
		}
	}
}
