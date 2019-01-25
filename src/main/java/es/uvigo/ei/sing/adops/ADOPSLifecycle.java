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
package es.uvigo.ei.sing.adops;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.platonos.pluginengine.PluginLifecycle;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.sing.adops.datatypes.BatchProject;
import es.uvigo.ei.sing.adops.datatypes.Project;
import es.uvigo.ei.sing.adops.views.utils.ClipboardViewsMouseListener;
import es.uvigo.ei.sing.adops.views.utils.CustomClipboardTreeModel;

public class ADOPSLifecycle extends PluginLifecycle {
	private final static Logger log = Logger.getLogger(ADOPSLifecycle.class);

	private final static Class<?>[] CLIPBOARD_ORDER = new Class<?>[] {
		Project.class,
		BatchProject.class
	};
	private final static Map<Class<?>, String> CLIPBOARD_MAPPING = new HashMap<>(ADOPSLifecycle.CLIPBOARD_ORDER.length + 1, 1f);

	static {
		CLIPBOARD_MAPPING.put(CLIPBOARD_ORDER[0], "Projects");
		CLIPBOARD_MAPPING.put(CLIPBOARD_ORDER[1], "Batch Projects");
	}

	@Override
	protected void start() {
		try {
			IIORegistry.getDefaultInstance().registerServiceProvider(
				new TIFFImageWriterSpi(), ImageWriterSpi.class
			);
		} catch (Exception e) {
			log.warn("Tiff image writer could not be registered", e);
		}
		try {
			IIORegistry.getDefaultInstance().registerServiceProvider(
				new TIFFImageReaderSpi(), ImageReaderSpi.class
			);
		} catch (Exception e) {
			log.warn("Tiff image reader could not be registered", e);
		}

		if (System.getProperty("aibench.nogui") != null)
			return;

		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					while (Workbench.getInstance().getMainFrame() == null) {
						Thread.yield();
					}

					final JTree tree = Workbench.getInstance().getTreeManager().getAIBenchClipboardTree();

					final CustomClipboardTreeModel treeModel = new CustomClipboardTreeModel(
						CLIPBOARD_ORDER, CLIPBOARD_MAPPING, true, true
					);
					for (Class<?> clazz : CLIPBOARD_ORDER) {
						treeModel.addAutoShow(clazz);
					}
					tree.setModel(treeModel);

					ClipboardViewsMouseListener.setAsDefaultClipboardTreeListener();
					final JMenu menu = Workbench.getInstance().getMenuBar().getMenu(0);
					menu.addSeparator();
					menu.add(
						new JMenuItem(
							new AbstractAction("Exit") {
								private static final long serialVersionUID = 1L;

								@Override
								public void actionPerformed(ActionEvent e) {
									System.exit(0);
								}
							}
						)
					);
				}
			}
		);
	}
}
