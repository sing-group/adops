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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.JTabbedPane;

public class DraggableJTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;

	private boolean dragging = false;
	private Image tabImage = null;
	private Point currentMouseLocation = null;
	private int draggedTabIndex = 0;

	public DraggableJTabbedPane() {
		this.addMouseMotionListener(
			new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent e) {
					if (!dragging) {
						// Gets the tab index based on the mouse position
						int tabNumber = getUI().tabForCoordinate(
							DraggableJTabbedPane.this, e.getX(), e.getY()
						);

						if (tabNumber >= 0) {
							draggedTabIndex = tabNumber;
							Rectangle bounds = getUI().getTabBounds(
								DraggableJTabbedPane.this, tabNumber
							);

							// Paint the tabbed pane to a buffer
							Image totalImage = new BufferedImage(
								getWidth(),
								getHeight(), BufferedImage.TYPE_INT_ARGB
							);
							Graphics totalGraphics = totalImage.getGraphics();
							totalGraphics.setClip(bounds);
							// Don't be double buffered when painting to a
							// static
							// image.
							setDoubleBuffered(false);
							paintComponent(totalGraphics);

							// Paint just the dragged tab to the buffer
							tabImage = new BufferedImage(
								bounds.width,
								bounds.height, BufferedImage.TYPE_INT_ARGB
							);
							Graphics graphics = tabImage.getGraphics();
							graphics.drawImage(
								totalImage, 0, 0, bounds.width,
								bounds.height, bounds.x, bounds.y,
								bounds.x + bounds.width,
								bounds.y + bounds.height,
								DraggableJTabbedPane.this
							);

							dragging = true;
							repaint();
						}
					} else {
						currentMouseLocation = e.getPoint();

						// Need to repaint
						repaint();
					}

					super.mouseDragged(e);
				}
			}
		);

		addMouseListener(
			new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {

					if (dragging) {
						int tabNumber = getUI().tabForCoordinate(
							DraggableJTabbedPane.this, e.getX(), 10
						);

						if (tabNumber >= 0) {
							Component comp = getComponentAt(draggedTabIndex);
							Component tabComponent = getTabComponentAt(draggedTabIndex);
							// String title = getTitleAt(draggedTabIndex);
							removeTabAt(draggedTabIndex);
							// insertTab(title, null, comp, null, tabNumber);
							insertTab(null, null, comp, null, tabNumber);
							setTabComponentAt(tabNumber, tabComponent);
						}
					}

					dragging = false;
					tabImage = null;

					DraggableJTabbedPane.this.repaint();
				}
			}
		);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Are we dragging?
		if (dragging && currentMouseLocation != null && tabImage != null) {
			// Draw the dragged tab
			g.drawImage(tabImage, currentMouseLocation.x, currentMouseLocation.y, this);
		}
	}

	// public static void main(String[] args) throws ClassNotFoundException,
	// InstantiationException, IllegalAccessException,
	// UnsupportedLookAndFeelException {
	// UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	//
	// JFrame test = new JFrame("Tab test");
	// test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// test.setSize(400, 400);
	//
	// DraggableJTabbedPane tabs = new DraggableJTabbedPane();
	// tabs.addTab("One", new JButton("One"));
	// tabs.addTab("Two", new JButton("Two"));
	// tabs.addTab("Three", new JButton("Three"));
	// tabs.addTab("Four", new JButton("Four"));
	//
	// test.add(tabs);
	// test.setVisible(true);
	// }
}
