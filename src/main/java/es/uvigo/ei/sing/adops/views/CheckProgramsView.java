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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import es.uvigo.ei.sing.adops.configuration.Configuration;
import es.uvigo.ei.sing.adops.operations.running.codeml.CodeMLProcessManager;
import es.uvigo.ei.sing.adops.operations.running.mrbayes.MrBayesProcessManager;
import es.uvigo.ei.sing.adops.operations.running.tcoffee.TCoffeeProcessManager;

public class CheckProgramsView extends JPanel {
	private static final long serialVersionUID = 1L;

	private final static ImageIcon WARNING_ICON = new ImageIcon(CheckProgramsDialog.class.getResource("images/warning8.png"));

	private final static MouseListener TOOLTIP_TRIGGER = new MouseAdapter() {
		@Override
		public void mouseEntered(MouseEvent e) {
			if (e.getComponent() instanceof JLabel) {
				final Action toolTipAction = ((JLabel) e.getComponent()).getActionMap().get("postTip");

				if (toolTipAction != null) {
					ActionEvent postTip = new ActionEvent(e.getComponent(), ActionEvent.ACTION_PERFORMED, "");
					toolTipAction.actionPerformed(postTip);
				}
			}
		}
	};

	private final Configuration configuration;
	private final JLabel lblTCoffee;
	private final JLabel lblMrBayes;
	private final JLabel lblCodeML;

	private JPanel buttonsPanel;

	public CheckProgramsView(final Configuration configuration) {
		super(new BorderLayout());

		this.configuration = configuration;

		final JPanel checkPanel = new JPanel(new GridLayout(3, 1, 10, 10));
		checkPanel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)
			)
		);
		checkPanel.setBackground(Color.WHITE);

		checkPanel.add(this.lblTCoffee = new JLabel("T-Coffee:"));
		checkPanel.add(this.lblMrBayes = new JLabel("MrBayes:"));
		checkPanel.add(this.lblCodeML = new JLabel("CodeML:"));

		this.buttonsPanel = new JPanel();
		this.buttonsPanel.add(
			new JButton(
				new AbstractAction("Check") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						CheckProgramsView.this.checkVersions();
					}
				}
			)
		);

		this.add(checkPanel, BorderLayout.CENTER);
		this.add(this.buttonsPanel, BorderLayout.SOUTH);

		this.checkVersions();
	}

	public JPanel getButtonsPanel() {
		return this.buttonsPanel;
	}

	private void checkVersions() {
		CheckProgramsView.removeTooltip(this.lblTCoffee);
		CheckProgramsView.removeTooltip(this.lblMrBayes);
		CheckProgramsView.removeTooltip(this.lblCodeML);
		try {
			final String version = TCoffeeProcessManager.getVersion(configuration.getTCoffeeConfiguration());

			if (TCoffeeProcessManager.isSupported(version)) {
				lblTCoffee.setText("T-Coffee: Ok [Version " + version + "]");
			} else {
				lblTCoffee.setText("T-Coffee: Warning [Unsupported version " + version + "]");
				addTooltip(lblTCoffee, TCoffeeProcessManager.getSupportedVersions());
			}
		} catch (IOException ioe) {
			lblTCoffee.setText("T-Coffee: Missing");
		}
		try {
			final String version = MrBayesProcessManager.getVersion(configuration.getMrBayesConfiguration());

			if (MrBayesProcessManager.isSupported(version)) {
				lblMrBayes.setText("MrBayes: Ok [Version " + version + "]");
			} else {
				lblMrBayes.setText("MrBayes: Warning [Unsupported version " + version + "]");
				addTooltip(lblMrBayes, MrBayesProcessManager.getSupportedVersions());
			}
		} catch (IOException ioe) {
			lblMrBayes.setText("MrBayes: Missing");
		}
		try {
			final String version = CodeMLProcessManager.getVersion(configuration.getCodeMLConfiguration());

			if (CodeMLProcessManager.isSupported(version)) {
				lblCodeML.setText("CodeML: Ok [Version " + version + "]");
			} else {
				lblCodeML.setText("CodeML: Warning [Unsupported version " + version + "]");
				addTooltip(lblCodeML, CodeMLProcessManager.getSupportedVersions());
			}
		} catch (IOException ioe) {
			lblCodeML.setText("CodeML: Missing");
		}
	}

	private final static void removeTooltip(JLabel label) {
		label.removeMouseListener(TOOLTIP_TRIGGER);
		label.setToolTipText(null);
		label.setIcon(null);
	}

	private final static void addTooltip(JLabel label, List<String> versions) {
		final StringBuilder sb = new StringBuilder("<html>Supported versions:<br>");

		for (String version : versions) {
			sb.append("&nbsp;&nbsp;&nbsp;").append(version).append("<br>");
		}

		sb.append("</html>");

		label.setToolTipText(sb.toString());
		label.setIcon(WARNING_ICON);

		label.addMouseListener(TOOLTIP_TRIGGER);
	}
}
