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
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;

import es.uvigo.ei.sing.adops.datatypes.HasConfiguration;

public class CheckProgramsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final HasConfiguration hasConfiguration;

	private CheckProgramsView checkProgramsView;

	public CheckProgramsDialog(HasConfiguration hasConfiguration) {
		super();

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Dialog owner, boolean modal) {
		super(owner, modal);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(
		HasConfiguration hasConfiguration, Dialog owner, String title, boolean modal,
		GraphicsConfiguration gc
	) {
		super(owner, title, modal, gc);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Dialog owner, String title, boolean modal) {
		super(owner, title, modal);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Dialog owner, String title) {
		super(owner, title);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Dialog owner) {
		super(owner);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Frame owner, boolean modal) {
		super(owner, modal);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(
		HasConfiguration hasConfiguration, Frame owner, String title, boolean modal,
        GraphicsConfiguration gc
	) {
		super(owner, title, modal, gc);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Frame owner, String title, boolean modal) {
		super(owner, title, modal);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Frame owner, String title) {
		super(owner, title);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Frame owner) {
		super(owner);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Window owner, ModalityType modalityType) {
		super(owner, modalityType);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(
		HasConfiguration hasConfiguration, Window owner, String title,
		ModalityType modalityType, GraphicsConfiguration gc
	) {
		super(owner, title, modalityType, gc);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(
		HasConfiguration hasConfiguration, Window owner, String title,
		ModalityType modalityType
	) {
		super(owner, title, modalityType);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Window owner, String title) {
		super(owner, title);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	public CheckProgramsDialog(HasConfiguration hasConfiguration, Window owner) {
		super(owner);

		this.hasConfiguration = hasConfiguration;
		this.init();
	}

	private void init() {
		this.checkProgramsView = new CheckProgramsView(this.hasConfiguration.getConfiguration());
		final JButton btnClose = new JButton(
			new AbstractAction("Close") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent event) {
					CheckProgramsDialog.this.setVisible(false);
				}
			}
		);
		this.checkProgramsView.getButtonsPanel().add(btnClose);

		this.setContentPane(this.checkProgramsView);
		this.pack();
		this.setLocationRelativeTo(this.getOwner());
	}
}
