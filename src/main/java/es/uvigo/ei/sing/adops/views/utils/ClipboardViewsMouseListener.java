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
package es.uvigo.ei.sing.adops.views.utils;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.workbench.MainWindow;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.tree.AIBenchTreeMouseListener;

public class ClipboardViewsMouseListener extends AIBenchTreeMouseListener {
	public final static ClipboardViewsMouseListener setAsDefaultClipboardTreeListener() {
		final JTree tree = Workbench.getInstance().getTreeManager().getAIBenchClipboardTree();
		
		final MouseListener[] listeners = tree.getMouseListeners();  
		for (MouseListener listener:listeners) {
			if (listener instanceof AIBenchTreeMouseListener) {
				tree.removeMouseListener(listener);
			}
		}
		
		final ClipboardViewsMouseListener listener = new ClipboardViewsMouseListener();
		tree.addMouseListener(listener);
		
		return listener;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 1 && !e.isPopupTrigger()) {
			if (e.getComponent() instanceof JTree) {
				final JTree tree = (JTree) e.getComponent();
				
				final TreePath selectionPath = tree.getPathForLocation(e.getX(), e.getY());
				
				if (selectionPath != null) {
					final int pathCount = selectionPath.getPathCount();
					
					// First ClassTreeNode child.
					if (pathCount <= 2) {
						super.mouseClicked(e);
					} else if (pathCount > 2) {
						if (selectionPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
							final DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
							if (targetNode.getUserObject() != null) {
								final Object targetItem = (targetNode.getUserObject() instanceof ClipboardItem)?
									((ClipboardItem) targetNode.getUserObject()).getUserData():
									targetNode.getUserObject();
								
								
								final Object[] path = selectionPath.getPath();
								final MainWindow mainWindow = (MainWindow) Workbench.getInstance().getMainFrame();
								
								boolean showed = false;
								for (int i = 2; i < (pathCount - 1) && !showed; i++) {
									if (path[i] instanceof DefaultMutableTreeNode) {
										final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[i];
										
										if (node.getUserObject() instanceof ClipboardItem) {
											final ClipboardItem item = (ClipboardItem) node.getUserObject();
											
											for (Component view : mainWindow.getDataViews((ClipboardItem) item)) {
												if (view instanceof ClipboardItemView) {
													final ClipboardItemView ciView = (ClipboardItemView) view;
													if (ciView.showClipboardItem(targetItem)) {
														Workbench.getInstance().showData(item);
														
														showed = true;
														break;
													}
												}
											}
										}
									}
								}
								
								if (!showed) {
									super.mouseClicked(e);
								}
							}
						}
					}
				}
			}
		} else {
			super.mouseClicked(e);
		}
	}
}
