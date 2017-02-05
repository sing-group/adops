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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import es.uvigo.ei.sing.adops.operations.running.Command;
import es.uvigo.ei.sing.adops.operations.running.OperationException;

public class ADOPSErrorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JButton btnOk;

	public ADOPSErrorPanel(Throwable exception, String message) {
		super(new BorderLayout());
		
		final JPanel panelMessage = new JPanel(new BorderLayout());

		final JTextArea taMessage = createFixedTextArea(message);
		taMessage.setPreferredSize(new Dimension(300, 100));
		
		final JLabel lblIcon = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
		lblIcon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		lblIcon.setBackground(Color.WHITE);
		lblIcon.setOpaque(true);
		
		final JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		this.btnOk = new JButton("OK");

		panelMessage.add(taMessage, BorderLayout.CENTER);
		panelButtons.add(this.btnOk);
		
		this.add(panelMessage, BorderLayout.CENTER);
		this.add(panelButtons, BorderLayout.SOUTH);
		this.add(lblIcon, BorderLayout.WEST);
		if (exception instanceof OperationException) {
			final OperationException oe = (OperationException) exception;
			
			final Command command = oe.getCommand();
			if (command != null) {
				final String commandLine = command.getCommand();
				final File directory = command.getDirectory();
				
				String commandMessage;
				if (directory == null ){
					commandMessage = commandLine;
				} else {
					commandMessage = String.format("Base directory: %s\n%s", directory.getAbsolutePath(), commandLine);
				}
				
				final JTextArea taCommand = createFixedTextArea(commandMessage, "Monospaced");
				taCommand.setColumns(80);
				taCommand.setBorder(
					BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Command"),
						taCommand.getBorder()
					)
				);
				
				final JButton btnCopyCommand = new JButton("Copy command");
				
				panelMessage.add(taCommand, BorderLayout.SOUTH);
				panelButtons.add(btnCopyCommand);
				
				btnCopyCommand.addActionListener(e -> {
					final Toolkit toolkit = Toolkit.getDefaultToolkit();
					final Clipboard clipboard = toolkit.getSystemClipboard();
					
					clipboard.setContents(new StringSelection(commandLine), null);
				});
			}
		}
	}
	
	private static JTextArea createFixedTextArea(String text) {
		return createFixedTextArea(text, null);
	}
	
	private static JTextArea createFixedTextArea(String text, String font) {
		final JTextArea textArea = new JTextArea(text);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(false);
		textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		if (font != null) {
			final Font currentFont = textArea.getFont();
			textArea.setFont(new Font(font, currentFont.getStyle(), currentFont.getSize()));
		}
		
		return textArea;
	}
	
	public JButton getBtnOk() {
		return btnOk;
	}
	
}
