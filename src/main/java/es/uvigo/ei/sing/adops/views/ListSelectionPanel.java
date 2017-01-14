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

import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import es.uvigo.ei.sing.adops.views.utils.AllActionListener;
import es.uvigo.ei.sing.adops.views.utils.InvertActionListener;
import es.uvigo.ei.sing.adops.views.utils.NoneActionListener;

public abstract class ListSelectionPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static ImageIcon ICON_RIGHT_TO_LEFT = new ImageIcon(ListSelectionPanel.class.getResource("images/arrow-left.png"));
	private static ImageIcon ICON_RIGHT_TO_LEFT_ALL = new ImageIcon(ListSelectionPanel.class.getResource("images/arrow-left-double.png"));
	private static ImageIcon ICON_LEFT_TO_RIGHT = new ImageIcon(ListSelectionPanel.class.getResource("images/arrow-right.png"));
	private static ImageIcon ICON_LEFT_TO_RIGHT_ALL = new ImageIcon(ListSelectionPanel.class.getResource("images/arrow-right-double.png"));

	public ListSelectionPanel() {
		super();
	}

	public ListSelectionPanel(LayoutManager layout) {
		super(layout);
	}

	public ListSelectionPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public ListSelectionPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public void addChangeListener(ChangeListener listener) {
		this.listenerList.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		this.listenerList.remove(ChangeListener.class, listener);
	}

	public void fireChangeEvent(ChangeEvent event) {
		final ChangeListener[] listeners = listenerList.getListeners(ChangeListener.class);

		for (ChangeListener listener : listeners) {
			listener.stateChanged(event);
		}
	}

	protected static class ListModelChangedEventListener implements ListDataListener {
		private final ListSelectionPanel listSelectionPanel;

		public ListModelChangedEventListener(
			ListSelectionPanel listSelectionPanel
		) {
			this.listSelectionPanel = listSelectionPanel;
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			fireChangeEvent(e);
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			fireChangeEvent(e);
		}

		@Override
		public void contentsChanged(ListDataEvent e) {
			fireChangeEvent(e);
		}

		protected void fireChangeEvent(ListDataEvent e) {
			this.listSelectionPanel.fireChangeEvent(new ChangeEvent(e.getSource()));
		}
	}

	protected static JPanel createSelectionPanel(JList<?> list) {
		final JPanel panel = new JPanel(new GridLayout(1, 3));
		panel.setOpaque(false);

		final JButton btnAll = new JButton("All");
		final JButton btnNone = new JButton("None");
		final JButton btnInvert = new JButton("Invert");

		panel.add(btnAll);
		panel.add(btnNone);
		panel.add(btnInvert);

		btnAll.addActionListener(new AllActionListener(list));
		btnNone.addActionListener(new NoneActionListener(list));
		btnInvert.addActionListener(new InvertActionListener(list));

		return panel;
	}

	protected static <T> JPanel createButtonsPanel(
		final JList<T> leftList,
		final CustomListModel<T> leftModel,
		final JList<T> rightList,
		final CustomListModel<T> rightModel,
		boolean invertedOrder
	) {
		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		final JButton btnRightToLeft = new JButton(ListSelectionPanel.ICON_RIGHT_TO_LEFT);
		final JButton btnLeftToRight = new JButton(ListSelectionPanel.ICON_LEFT_TO_RIGHT);
		final JButton btnRightToLeftAll = new JButton(ListSelectionPanel.ICON_RIGHT_TO_LEFT_ALL);
		final JButton btnLeftToRightAll = new JButton(ListSelectionPanel.ICON_LEFT_TO_RIGHT_ALL);

		btnRightToLeft.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		btnLeftToRight.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		btnRightToLeftAll.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		btnLeftToRightAll.setAlignmentX(JComponent.CENTER_ALIGNMENT);

		if (invertedOrder) {
			panel.add(btnLeftToRight);
			panel.add(btnRightToLeft);
			panel.add(btnLeftToRightAll);
			panel.add(btnRightToLeftAll);
		} else {
			panel.add(btnRightToLeft);
			panel.add(btnLeftToRight);
			panel.add(btnRightToLeftAll);
			panel.add(btnLeftToRightAll);
		}

		btnRightToLeft.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final List<T> selectedValues = rightList.getSelectedValuesList();

					for (T selected : selectedValues) {
						rightModel.remove(selected);
						leftModel.add(selected);
					}
				}
			}
		);
		btnLeftToRight.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final List<T> unselectedValues = leftList.getSelectedValuesList();

					for (T unselected : unselectedValues) {
						leftModel.remove(unselected);
						rightModel.add(unselected);
					}
				}
			}
		);
		btnRightToLeftAll.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					leftModel.addAll(rightModel.getValues());
					rightModel.clear();
				}
			}
		);
		btnLeftToRightAll.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					rightModel.addAll(leftModel.getValues());
					leftModel.clear();
				}
			}
		);

		return panel;
	}

	protected static class CustomListModel<T> extends AbstractListModel<T> {
		private static final long serialVersionUID = 1L;
		private final List<T> values;

		public CustomListModel(List<T> values) {
			this.values = values;
		}

		public boolean remove(T value) {
			final int index = this.values.indexOf(value);
			if (index >= 0 && this.values.remove(value)) {
				this.fireIntervalRemoved(this, index, index);

				return true;
			} else {
				return false;
			}
		}

		public boolean add(T value) {
			if (this.values.add(value)) {
				final int index = this.values.indexOf(value);

				this.fireIntervalAdded(this, index, index);

				return true;
			} else {
				return false;
			}
		}

		public boolean addAll(Collection<T> valueToAdd) {
			final int size = this.values.size();
			if (this.values.addAll(valueToAdd)) {
				this.fireIntervalAdded(this, size, this.values.size() - 1);

				return true;
			} else {
				return false;
			}
		}

		public void clear() {
			if (!this.values.isEmpty()) {
				final int size = this.values.size();
				this.values.clear();

				this.fireIntervalRemoved(this, 0, size - 1);
			}
		}

		public List<T> getValues() {
			return Collections.unmodifiableList(this.values);
		}

		@Override
		public int getSize() {
			return this.values.size();
		}

		@Override
		public T getElementAt(int index) {
			return this.values.get(index);
		}
	}
}
