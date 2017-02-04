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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import es.uvigo.ei.sing.adops.configuration.Configuration;
import es.uvigo.ei.sing.adops.configuration.TCoffeeConfiguration;
import es.uvigo.ei.sing.adops.datatypes.HasConfiguration;
import es.uvigo.ei.sing.adops.operations.running.tcoffee.AlignMethod;
import es.uvigo.ei.sing.adops.util.Utils;

public class EditConfigurationView extends JPanel {
	private static final String PROPERTY_VALUE = "PROPERTY_VALUE";
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(EditConfigurationView.class);

	private final Configuration configuration;
	private final File propertiesFile;
	private final Map<String, JComponent> editorsMap;

	private JButton btnSave, btnReset, btnExport;
	private Properties backupProperties;

	private final JPanel buttonsPanel;

	public EditConfigurationView(HasConfiguration hasProperties) {
		this(hasProperties, true);
	}

	public EditConfigurationView(HasConfiguration hasProperties, boolean modifiable) {
		this(hasProperties.getConfiguration(), hasProperties.getPropertiesFile(), modifiable);
	}

	public EditConfigurationView(Configuration configuration, File propertiesFile) {
		this(configuration, propertiesFile, true);
	}

	private Set<String> addCustomPropertyEditors(JPanel panel, GridBagConstraints gbcLabels, GridBagConstraints gbcText, boolean modifiable) {
		final Set<String> customized = new HashSet<String>();
		if (modifiable) {
			// ALIGN METHOD
			final String alignMethodProp = TCoffeeConfiguration.PROPERTIES_PREFIX + '.' + TCoffeeConfiguration.PROPERTY_ALIGN_METHOD;
			final JLabel lblAlignMethod = new JLabel(alignMethodProp);
			final JComboBox<AlignMethod> cmbAlignMethod = new JComboBox<>(AlignMethod.values());
			cmbAlignMethod.putClientProperty(PROPERTY_VALUE, this.configuration.getTCoffeeConfiguration().getAlignMethod().name());
			cmbAlignMethod.setSelectedItem(this.configuration.getTCoffeeConfiguration().getAlignMethod());
			cmbAlignMethod.addItemListener(
				new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						final String alignMethodName = ((AlignMethod) cmbAlignMethod.getSelectedItem()).name();

						cmbAlignMethod.putClientProperty(EditConfigurationView.PROPERTY_VALUE, alignMethodName);
						EditConfigurationView.this.changePropertyValue(alignMethodProp, alignMethodName);
					}
				}
			);
			cmbAlignMethod.addPropertyChangeListener(
				PROPERTY_VALUE, new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						cmbAlignMethod.setSelectedItem(AlignMethod.valueOf((String) evt.getNewValue()));
					}
				}
			);

			panel.add(lblAlignMethod, gbcLabels);
			panel.add(cmbAlignMethod, gbcText);

			this.editorsMap.put(alignMethodProp, cmbAlignMethod);

			customized.add(alignMethodProp);

			// MAX. SEQS
			final String maxSeqsProp = TCoffeeConfiguration.PROPERTIES_PREFIX + '.' + TCoffeeConfiguration.PROPERTY_MAX_SEQS;
			final JLabel lblMaxSeqs = new JLabel(maxSeqsProp);
			final JPanel panelMaxSeqs = new JPanel();
			final int maxSeqs = this.configuration.getTCoffeeConfiguration().getMaxSeqs();
			final JCheckBox chkMaxSeqs = new JCheckBox("Use optimization", maxSeqs > 0);
			final JSpinner spnMaxSeqs = new JSpinner(new SpinnerNumberModel(maxSeqs > 0 ? maxSeqs : 3, 1, Integer.MAX_VALUE, 1));
			spnMaxSeqs.putClientProperty(PROPERTY_VALUE, Integer.toString(maxSeqs));
			spnMaxSeqs.setEnabled(chkMaxSeqs.isSelected());

			final ChangeListener changeListener = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					spnMaxSeqs.setEnabled(chkMaxSeqs.isSelected());

					final String value = chkMaxSeqs.isSelected() ? spnMaxSeqs.getValue().toString() : "0";

					if (!value.equals(spnMaxSeqs.getClientProperty(PROPERTY_VALUE))) {
						spnMaxSeqs.putClientProperty(PROPERTY_VALUE, value);
						EditConfigurationView.this.changePropertyValue(maxSeqsProp, value);
					}
				}
			};
			final PropertyChangeListener propertyListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					final Integer value = Integer.parseInt(evt.getNewValue().toString());

					if (value > 0) {
						spnMaxSeqs.setEnabled(true);
						spnMaxSeqs.setValue(value);
						chkMaxSeqs.setSelected(true);
					} else {
						chkMaxSeqs.setSelected(false);
						spnMaxSeqs.setEnabled(false);
					}
				}
			};
			spnMaxSeqs.addPropertyChangeListener(PROPERTY_VALUE, propertyListener);
			chkMaxSeqs.addChangeListener(changeListener);
			spnMaxSeqs.addChangeListener(changeListener);

			panelMaxSeqs.add(chkMaxSeqs);
			panelMaxSeqs.add(spnMaxSeqs);

			panel.add(lblMaxSeqs, gbcLabels);
			panel.add(panelMaxSeqs, gbcText);

			this.editorsMap.put(maxSeqsProp, spnMaxSeqs);

			customized.add(maxSeqsProp);
		}

		return customized;
	}

	public EditConfigurationView(Configuration configuration, File propertiesFile, boolean modifiable) {
		super(new BorderLayout());

		this.configuration = configuration;
		this.propertiesFile = propertiesFile;
		this.backupProperties = this.configuration.toProperties(true);

		this.editorsMap = new HashMap<>();

		final JPanel panel = new JPanel(new GridBagLayout());

		final GridBagConstraints gbcLabels = new GridBagConstraints();
		gbcLabels.fill = GridBagConstraints.BOTH;
		gbcLabels.gridx = 0;
		gbcLabels.weightx = 25;
		gbcLabels.anchor = GridBagConstraints.NORTH;

		final GridBagConstraints gbcText = new GridBagConstraints();
		gbcText.fill = GridBagConstraints.BOTH;
		gbcText.gridx = 1;
		gbcText.weightx = 75;
		gbcText.anchor = GridBagConstraints.NORTH;

		final Set<String> customized = this.addCustomPropertyEditors(panel, gbcLabels, gbcText, modifiable);

		for (final String key : this.configuration.listProperties()) {
			final String value = this.configuration.getProperty(key);

			if (customized.contains(key))
				continue;

			final JLabel label = new JLabel(key);
			final JTextField txtValue = new JTextField(value);
			txtValue.setEditable(modifiable);

			label.setToolTipText(key);

			panel.add(label, gbcLabels);
			panel.add(txtValue, gbcText);

			this.editorsMap.put(key, txtValue);

			if (modifiable) {
				txtValue.addKeyListener(
					new KeyAdapter() {
						@Override
						public void keyReleased(KeyEvent e) {
							final String text = txtValue.getText();
							EditConfigurationView.this.changePropertyValue(key, text);
						}
					}
				);
			}
		}

		this.buttonsPanel = new JPanel();

		if (modifiable) {
			this.btnSave = new JButton(
				new AbstractAction("Save") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						EditConfigurationView.this.configuration.storeProperties(
							EditConfigurationView.this.propertiesFile
						);

						EditConfigurationView.this.backupProperties = EditConfigurationView.this.configuration.toProperties(true);

						EditConfigurationView.this.btnSave.setEnabled(false);
						EditConfigurationView.this.btnReset.setEnabled(false);
						EditConfigurationView.this.btnExport.setEnabled(true);
					}
				}
			);
			this.btnReset = new JButton(
				new AbstractAction("Reset") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						EditConfigurationView.this.resetValues();

						EditConfigurationView.this.btnSave.setEnabled(false);
						EditConfigurationView.this.btnReset.setEnabled(false);
						EditConfigurationView.this.btnExport.setEnabled(true);
					}
				}
			);

			this.btnSave.setEnabled(false);
			this.btnReset.setEnabled(false);
			this.buttonsPanel.add(this.btnSave);
			this.buttonsPanel.add(this.btnReset);
		}

		this.btnExport = new JButton(
			new AbstractAction("Export...") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					final JFileChooser fc = new JFileChooser(new File("."));

					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fc.setDialogType(JFileChooser.SAVE_DIALOG);

					if (fc.showSaveDialog(EditConfigurationView.this) == JFileChooser.APPROVE_OPTION) {
						final File file = fc.getSelectedFile();

						final Properties props = EditConfigurationView.this.configuration.toProperties(true);

						Utils.storeProperties(props, file, EditConfigurationView.LOG);
					}
				}
			}
		);
		this.buttonsPanel.add(this.btnExport);

		this.add(panel, BorderLayout.NORTH);
		this.add(this.buttonsPanel, BorderLayout.SOUTH);
	}

	private void changePropertyValue(final String key, final String text) {
		this.configuration.setProperty(key, text);

		final boolean changed = this.propertiesHaveChanged();
		this.btnSave.setEnabled(changed);
		this.btnReset.setEnabled(changed);
		this.btnExport.setEnabled(!changed);
	}

	private void resetValues() {
		for (String key : this.backupProperties.stringPropertyNames()) {
			final String value = this.backupProperties.getProperty(key);

			this.configuration.setProperty(key, value);

			final JComponent editor = this.editorsMap.get(key);
			if (editor instanceof JTextField) {
				final JTextField txtField = (JTextField) editor;
				txtField.setText(value);
			} else {
				editor.putClientProperty(PROPERTY_VALUE, value);
			}
		}
	}

	private boolean propertiesHaveChanged() {
		for (Map.Entry<String, JComponent> entry : this.editorsMap.entrySet()) {
			final String key = entry.getKey();
			final String value;

			if (entry.getValue() instanceof JTextField) {
				final JTextField txtField = (JTextField) entry.getValue();
				value = txtField.getText();
			} else {
				value = (String) entry.getValue().getClientProperty(PROPERTY_VALUE);
			}

			if (!value.equals(this.backupProperties.get(key))) {
				return true;
			}
		}

		return false;
	}

	public JPanel getButtonsPanel() {
		return buttonsPanel;
	}

	public JButton getBtnSave() {
		return btnSave;
	}

	public JButton getBtnExport() {
		return btnExport;
	}

	public JButton getBtnReset() {
		return btnReset;
	}
}
