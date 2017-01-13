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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

public class DoubleListSelectionPanel<T> extends ListSelectionPanel {
	private static final long serialVersionUID = 1L;;
	private final CustomListModel<T> lmPositive;
	private final CustomListModel<T> lmValues;
	private final CustomListModel<T> lmNegative;
	private final JList<T> listNegative;
	private final JList<T> listValues;
	private final JList<T> listPositive;

	public DoubleListSelectionPanel(
		final List<T> values,
		String positiveTitle,
		String valuesTitle,
		String negativeTitle
	) {
		super(new GridBagLayout());
		final GridBagConstraints gbcPositive = new GridBagConstraints();
		final GridBagConstraints gbcPosButtons = new GridBagConstraints();
		final GridBagConstraints gbcValues = new GridBagConstraints();
		final GridBagConstraints gbcNegButtons = new GridBagConstraints();
		final GridBagConstraints gbcNegative = new GridBagConstraints();
		gbcPositive.fill = GridBagConstraints.BOTH;
		gbcPositive.gridx = 0;
		gbcPositive.weightx = 0.33d;
		gbcPositive.weighty = 1d;
		
		gbcPosButtons.gridx = 1;
		gbcPosButtons.weightx = 0d;
		gbcPosButtons.ipadx = 10;
		
		gbcValues.fill = GridBagConstraints.BOTH;
		gbcValues.gridx = 2;
		gbcValues.weightx = 0.33d;
		gbcValues.weighty = 1d;
		
		gbcNegButtons.gridx = 3;
		gbcNegButtons.weightx = 0d;
		gbcNegButtons.ipadx = 10;
		
		gbcNegative.fill = GridBagConstraints.BOTH;
		gbcNegative.gridx = 4;
		gbcNegative.weightx = 0.33d;
		gbcNegative.weighty = 1d;
		
		this.lmPositive = new CustomListModel<>(new ArrayList<T>());
		this.lmValues = new CustomListModel<>(values);
		this.lmNegative = new CustomListModel<>(new ArrayList<T>());
		
		this.listPositive = new JList<>(this.lmPositive);
		this.listValues = new JList<>(this.lmValues);
		this.listNegative = new JList<>(this.lmNegative);
		
		final JLabel lblPositive = new JLabel(positiveTitle);
		final JLabel lblValues = new JLabel(valuesTitle);
		final JLabel lblNegative = new JLabel(negativeTitle);
		lblPositive.setFont(lblPositive.getFont().deriveFont(Font.BOLD));
		lblValues.setFont(lblValues.getFont().deriveFont(Font.BOLD));
		lblNegative.setFont(lblNegative.getFont().deriveFont(Font.BOLD));
		lblPositive.setHorizontalAlignment(JLabel.LEFT);
		lblValues.setHorizontalAlignment(JLabel.CENTER);
		lblNegative.setHorizontalAlignment(JLabel.RIGHT);
		
		final Border border = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		lblNegative.setBorder(border);
		lblValues.setBorder(border);
		lblPositive.setBorder(border);
		
		final JPanel panelPositive = new JPanel(new BorderLayout());
		final JPanel panelPositiveSelectors = createSelectionPanel(this.listPositive);
		final JPanel panelValues = new JPanel(new BorderLayout());
		final JPanel panelValuesSelectors = createSelectionPanel(this.listValues);
		final JPanel panelNegative = new JPanel(new BorderLayout());
		final JPanel panelNegativeSelectors = createSelectionPanel(this.listNegative);
		panelPositive.setOpaque(false);
		panelValues.setOpaque(false);
		panelNegative.setOpaque(false);
		
		panelPositiveSelectors.setOpaque(false);
		panelValuesSelectors.setOpaque(false);
		panelNegativeSelectors.setOpaque(false);
		
		panelPositive.add(lblPositive, BorderLayout.NORTH);
		panelPositive.add(new JScrollPane(listPositive), BorderLayout.CENTER);
		panelPositive.add(panelPositiveSelectors, BorderLayout.SOUTH);
		
		panelValues.add(lblValues, BorderLayout.NORTH);
		panelValues.add(new JScrollPane(listValues), BorderLayout.CENTER);
		panelValues.add(panelValuesSelectors, BorderLayout.SOUTH);
		
		panelNegative.add(lblNegative, BorderLayout.NORTH);
		panelNegative.add(new JScrollPane(listNegative), BorderLayout.CENTER);
		panelNegative.add(panelNegativeSelectors, BorderLayout.SOUTH);
		
		final JPanel panelButtonsPositive = 
			createButtonsPanel(listPositive, lmPositive, listValues, lmValues, false);
		panelButtonsPositive.setOpaque(false);
		
		final JPanel panelButtonsNegative = 
			createButtonsPanel(listValues, lmValues, listNegative, lmNegative, true);
		panelButtonsNegative.setOpaque(false);
		
		this.add(panelPositive, gbcPositive);
		this.add(panelButtonsPositive, gbcPosButtons);
		this.add(panelValues, gbcValues);
		this.add(panelButtonsNegative, gbcNegButtons);
		this.add(panelNegative, gbcNegative);
		
		final ListModelChangedEventListener listModelChangedEventListener = 
			new ListModelChangedEventListener(this);
		this.lmNegative.addListDataListener(listModelChangedEventListener);
		this.lmValues.addListDataListener(listModelChangedEventListener);
		this.lmPositive.addListDataListener(listModelChangedEventListener);
	}
	
	public List<T> getNegativeValues() {
		return this.lmNegative.getValues();
	}
	
	public List<T> getUnselectedValues() {
		return this.lmValues.getValues();
	}
	
	public List<T> getPositiveValues() {
		return this.lmPositive.getValues();
	}
}
