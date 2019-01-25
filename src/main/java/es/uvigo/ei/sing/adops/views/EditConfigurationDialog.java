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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;

import es.uvigo.ei.sing.adops.datatypes.HasConfiguration;

public class EditConfigurationDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final HasConfiguration hasConfiguration;

	private EditConfigurationView editConfigurationView;

	private JButton btnClose;

	public EditConfigurationDialog(HasConfiguration hasConfiguration, boolean modifiable) {
		super();

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Dialog owner, boolean modal, boolean modifiable) {
		super(owner, modal);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(
		HasConfiguration hasConfiguration, Dialog owner, String title, boolean modal,
		GraphicsConfiguration gc, boolean modifiable
	) {
		super(owner, title, modal, gc);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Dialog owner, String title, boolean modal, boolean modifiable) {
		super(owner, title, modal);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Dialog owner, String title, boolean modifiable) {
		super(owner, title);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Dialog owner, boolean modifiable) {
		super(owner);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Frame owner, boolean modal, boolean modifiable) {
		super(owner, modal);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(
		HasConfiguration hasConfiguration, Frame arg0, String arg1, boolean arg2,
		GraphicsConfiguration arg3, boolean modifiable
	) {
		super(arg0, arg1, arg2, arg3);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Frame arg0, String arg1, boolean arg2, boolean modifiable) {
		super(arg0, arg1, arg2);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Frame owner, String title, boolean modifiable) {
		super(owner, title);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Frame owner, boolean modifiable) {
		super(owner);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Window owner, ModalityType modalityType, boolean modifiable) {
		super(owner, modalityType);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(
		HasConfiguration hasConfiguration, Window owner, String title,
		ModalityType modalityType, GraphicsConfiguration gc, boolean modifiable
	) {
		super(owner, title, modalityType, gc);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(
		HasConfiguration hasConfiguration, Window owner, String title,
		ModalityType modalityType, boolean modifiable
	) {
		super(owner, title, modalityType);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Window owner, String title, boolean modifiable) {
		super(owner, title);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	public EditConfigurationDialog(HasConfiguration hasConfiguration, Window owner, boolean modifiable) {
		super(owner);

		this.hasConfiguration = hasConfiguration;
		this.init(modifiable);
	}

	private void init(boolean modifiable) {
		this.editConfigurationView = new EditConfigurationView(this.hasConfiguration, modifiable);
		this.btnClose = new JButton(
			new AbstractAction("Close") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent event) {
					EditConfigurationDialog.this.setVisible(false);
				}
			}
		);
		editConfigurationView.getButtonsPanel().add(btnClose);

		this.setContentPane(editConfigurationView);
		this.setMinimumSize(new Dimension(600, 400));
		this.pack();
		this.setLocationRelativeTo(this.getOwner());
	}

	public JButton getBtnClose() {
		return btnClose;
	}

	public JButton getBtnSave() {
		return this.editConfigurationView.getBtnSave();
	}

	public JButton getBtnExport() {
		return this.editConfigurationView.getBtnExport();
	}

	public JButton getBtnReset() {
		return this.editConfigurationView.getBtnReset();
	}
}
