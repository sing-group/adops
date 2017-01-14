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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class TextFileDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final File file;

	public TextFileDialog(File file) {
		super();
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Dialog owner, boolean modal) {
		super(owner, modal);
		this.file = file;
		this.init();
	}

	public TextFileDialog(
		File file, Dialog owner, String title, boolean modal,
		GraphicsConfiguration gc
	) {
		super(owner, title, modal, gc);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Dialog owner, String title) {
		super(owner, title);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Dialog owner) {
		super(owner);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Frame owner, boolean modal) {
		super(owner, modal);
		this.file = file;
		this.init();
	}

	public TextFileDialog(
		File file, Frame owner, String title, boolean modal, GraphicsConfiguration gc
	) {
		super(owner, title, modal, gc);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Frame owner, String title) {
		super(owner, title);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Frame owner) {
		super(owner);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Window owner, ModalityType modalityType) {
		super(owner, modalityType);
		this.file = file;
		this.init();
	}

	public TextFileDialog(
		File file, Window owner, String title,
		ModalityType modalityType, GraphicsConfiguration gc
	) {
		super(owner, title, modalityType, gc);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Window owner, String title) {
		super(owner, title);
		this.file = file;
		this.init();
	}

	public TextFileDialog(File file, Window owner) {
		super(owner);
		this.file = file;
		this.init();
	}

	private void init() {
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		final JPanel content = new JPanel(new BorderLayout());
		final JPanel buttonsPanel = new JPanel();

		buttonsPanel.add(
			new JButton(
				new AbstractAction("Close") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent arg0) {
						TextFileDialog.this.setVisible(false);
					}
				}
			)
		);

		content.add(new TextFileViewer(this.file), BorderLayout.CENTER);
		content.add(buttonsPanel, BorderLayout.SOUTH);

		this.setContentPane(content);

		this.setMinimumSize(new Dimension(600, 400));
		this.pack();

		this.setLocationRelativeTo(this.getOwner());
	}
}
