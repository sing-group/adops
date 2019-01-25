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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ProgressHandler;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.sing.adops.datatypes.Experiment;

public class DefaultProcessLauncher extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JTextArea taDocument;
	private final JScrollPane spDocument;

	public DefaultProcessLauncher(Experiment experiment) {
		super(new BorderLayout());

		this.taDocument = new JTextArea();
		this.spDocument = new JScrollPane(this.taDocument);

		final DocumentOutputStream dos = new DocumentOutputStream();
		final JButton btnLaunch = new JButton(
			new AbstractAction("Launch") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					Core.getInstance().executeOperation(
						"es.uvigo.ei.sing.adops.operations.testprocess",
						new ProgressHandler() {
							@Override
							public void validationError(Throwable t) {}

							@Override
							public void operationStart(Object progressBean, Object operationID) {}

							@Override
							public void operationFinished(List<Object> results, List<ClipboardItem> clipboardItems) {}

							@Override
							public void operationError(Throwable t) {}
						},
						Collections.singletonList(dos)
					);
				}
			}
		);

		this.add(btnLaunch, BorderLayout.NORTH);
		this.add(spDocument, BorderLayout.CENTER);

		this.taDocument.append("Hello World!!");
	}

	private class DocumentOutputStream extends OutputStream {
		private byte[] buffer;
		private int index;

		public DocumentOutputStream() {
			super();
			this.buffer = new byte[1024];
			this.index = 0;
		}

		@Override
		public void write(int b) throws IOException {
			this.buffer[this.index++] = (byte) b;

			if (this.index == this.buffer.length) {
				this.index = 0;

				final String text = new String(this.buffer, Charset.forName("UTF-8"));

				DefaultProcessLauncher.this.taDocument.append(text);
				final JScrollBar scrollbar = DefaultProcessLauncher.this.spDocument.getVerticalScrollBar();
				scrollbar.setValue(scrollbar.getMaximum());
			}
		}

		@Override
		public void flush() throws IOException {
			super.flush();
		}
	}
}
