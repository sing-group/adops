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
package es.uvigo.ei.sing.adops.views.error;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import es.uvigo.ei.aibench.workbench.MainWindow;
import es.uvigo.ei.aibench.workbench.error.ErrorNotifier;

public class ADOPSErrorNotifier implements ErrorNotifier {

	@Override
	public void showError(MainWindow mainWindow, Throwable exception) {
		this.showError(mainWindow, exception, exception.getMessage());
	}

	@Override
	public void showError(MainWindow mainWindow, Throwable exception, String message) {
		SwingUtilities.invokeLater(() -> {
			System.out.println("ERROR: " + message + exception);
			final ADOPSErrorPanel panel = new ADOPSErrorPanel(exception, message);
			
			final JDialog dialog = new JDialog(mainWindow, "Error", true);
			dialog.getContentPane().add(panel);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.pack();
			
			dialog.setLocationRelativeTo(mainWindow);
			
			panel.getBtnOk().addActionListener(e -> {
				dialog.setVisible(false);
			});
			
			dialog.setVisible(true);
		});
	}

}
