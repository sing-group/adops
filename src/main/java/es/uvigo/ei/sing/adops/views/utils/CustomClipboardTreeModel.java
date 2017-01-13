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

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.tree.AIBenchJTreeManager;
import es.uvigo.ei.aibench.workbench.tree.AIBenchJTreeManager.ClassTreeNode;

public class CustomClipboardTreeModel extends DefaultTreeModel {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private final boolean defaultIgnore;
	private final boolean includeSubclasses;
	private final Class<?>[] order;
	private final Map<Class<?>, String> namingMap;
	private final Map<Class<?>, String[]> leafOrder;
	private final Set<Class<?>> autoShow;
	private final Set<Class<?>> dontExpandOnNew;
	
	private final Set<TreeNode> dontExpandNewNodes;
	
	public CustomClipboardTreeModel(
			Class<?>[] order, 
			Map<Class<?>, String> namingMap
	) {
		this(order, new HashMap<Class<?>, String[]>(), namingMap, null, null, false, false);
	}
	
	public CustomClipboardTreeModel(
			Class<?>[] order, 
			Map<Class<?>, String> namingMap, 
			boolean defaultIgnore, 
			boolean includeSubclasses
	) {
		this(order, new HashMap<Class<?>, String[]>(), namingMap, null, null, defaultIgnore, includeSubclasses);
	}
	
	public CustomClipboardTreeModel(
			Class<?>[] order, 
			Map<Class<?>, String> namingMap, 
			Collection<Class<?>> autoShow, 
			Collection<Class<?>> dontExpandOnNew
	) {
		this(order, new HashMap<Class<?>, String[]>(), namingMap, autoShow, dontExpandOnNew, false, false);
	}
	
	public CustomClipboardTreeModel(
			Class<?>[] order, 
			Map<Class<?>, String> namingMap, 
			Collection<Class<?>> autoShow, 
			Collection<Class<?>> dontExpandOnNew, 
			boolean defaultIgnore, 
			boolean includeSubclasses
	) {
		this(order, new HashMap<Class<?>, String[]>(), namingMap, autoShow, dontExpandOnNew, defaultIgnore, includeSubclasses);
	}
	
	public CustomClipboardTreeModel(
		Class<?>[] order, 
		Map<Class<?>, String[]> leafOrder,
		Map<Class<?>, String> namingMap
	) {
		this(order, leafOrder, namingMap, null, null, false, false);
	}
	
	public CustomClipboardTreeModel(
		Class<?>[] order, 
		Map<Class<?>, String[]> leafOrder,
		Map<Class<?>, String> namingMap, 
		boolean defaultIgnore, 
		boolean includeSubclasses
	) {
		this(order, leafOrder, namingMap, null, null, defaultIgnore, includeSubclasses);
	}
	
	public CustomClipboardTreeModel(
		Class<?>[] order, 
		Map<Class<?>, String[]> leafOrder,
		Map<Class<?>, String> namingMap, 
		Collection<Class<?>> autoShow, 
		Collection<Class<?>> dontExpandOnNew
	) {
		this(order, leafOrder, namingMap, autoShow, dontExpandOnNew, false, false);
	}
	
	public CustomClipboardTreeModel(
		Class<?>[] order, 
		Map<Class<?>, String[]> leafOrder,
		Map<Class<?>, String> namingMap, 
		Collection<Class<?>> autoShow, 
		Collection<Class<?>> dontExpandOnNew, 
		boolean defaultIgnore, 
		boolean includeSubclasses
	) {
		super(new DefaultMutableTreeNode("Clipboard"));
		this.includeSubclasses = includeSubclasses;
		this.defaultIgnore = defaultIgnore;
		this.order = order;
		this.leafOrder = leafOrder;
		this.namingMap = namingMap;
		this.autoShow = (autoShow == null)?new HashSet<Class<?>>():new HashSet<Class<?>>(autoShow);
		this.dontExpandOnNew = (dontExpandOnNew == null)?new HashSet<Class<?>>():new HashSet<Class<?>>(dontExpandOnNew);
		
		this.dontExpandNewNodes = new HashSet<TreeNode>();
		
		AIBenchJTreeManager.getInstance().getAIBenchClipboardTree().addTreeExpansionListener(new TreeExpansionListener() {
			public void treeCollapsed(TreeExpansionEvent event) {}
			public void treeExpanded(TreeExpansionEvent event) {
				if (dontExpandNewNodes.contains(event.getPath().getLastPathComponent())) {
					CustomClipboardTreeModel.this.dontExpandNewNodes.remove(event.getPath().getLastPathComponent());
					AIBenchJTreeManager.getInstance().getAIBenchClipboardTree().collapsePath(event.getPath());					
				}
			}
		});
	}
	
	public boolean addDontExpandOnNew(Class<?> clazz) {
		return this.dontExpandOnNew.add(clazz);
	}
	
	public boolean removeDontExpandOnNew(Class<?> clazz) {
		return this.dontExpandOnNew.remove(clazz);
	}
	
	public Collection<Class<?>> getDontExpandOnNew() {
		return Collections.unmodifiableCollection(this.dontExpandOnNew);
	}
	
	private boolean isDontExpandOnNew(Class<?> clazz) {
		if (this.includeSubclasses) {
			for (Class<?> deonClazz:this.dontExpandOnNew) {
				if (deonClazz.isAssignableFrom(clazz)) return true;
			}
			return false;
		} else {
			return this.dontExpandOnNew.contains(clazz);			
		}
	}
	
	public boolean addAutoShow(Class<?> clazz) {
		return this.autoShow.add(clazz);
	}
	
	public boolean removeAutoShow(Class<?> clazz) {
		return this.autoShow.remove(clazz);
	}
	
	public Collection<Class<?>> getAutoShow() {
		return Collections.unmodifiableCollection(this.autoShow);
	}
	
	private boolean isAutoShow(Class<?> clazz) {
		if (this.includeSubclasses) {
			for (Class<?> asClazz:this.autoShow) {
				if (asClazz.isAssignableFrom(clazz)) return true;
			}
			return false;
		} else {
			return this.autoShow.contains(clazz);			
		}
	}

	private int indexOf(Class<?> clazz) {
		int index = -1;

		for (int i = 0; i < this.order.length; i++) {
			if ((!this.includeSubclasses && this.order[i].equals(clazz)) ||
				(this.includeSubclasses && this.order[i].isAssignableFrom(clazz))) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	@SuppressWarnings("unchecked")
	private void sortChildren(Class<?> clazz, DefaultMutableTreeNode node) {
		if (this.leafOrder.containsKey(clazz)) {
			Vector<DefaultMutableTreeNode> children = new Vector<DefaultMutableTreeNode>();
			
			Enumeration<TreeNode> childrenEnum = node.children();
			while (childrenEnum.hasMoreElements()) {
				children.add((DefaultMutableTreeNode) childrenEnum.nextElement());
			}
			node.removeAllChildren();
			
			for (String itemName:this.leafOrder.get(clazz)) {
				for (DefaultMutableTreeNode mtn:children) {
					if (mtn.getUserObject().toString().matches(itemName)) {
						node.add(mtn);
					}
				}
			}
		}
	}
	
//	private int indexOfLeaf(Class<?> clazz, String text) {
//		int index = -1;
//		
//		String[] items = this.leafOrder.get(clazz);
//		for (int i = 0; i < items.length; i++) {
//			if (text.matches(items[i])) {
//				index = i;
//				break;
//			}
//		}
//		
//		return index;
//	}
	
	private String getName(Class<?> clazz) {
		if (this.includeSubclasses) {
			if (this.namingMap.containsKey(clazz)) {
				return this.namingMap.get(clazz);
			} else {
				for (Class<?> c:this.namingMap.keySet()) {
					if (c.isAssignableFrom(clazz)) {
						return this.namingMap.get(c);
					}
				}
				return null;
			}
		} else {
			return this.namingMap.get(clazz);
		}
	}
	
	@Override
	public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {
		if (newChild instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) newChild).getUserObject() instanceof ClipboardItem) {
			ClipboardItem item = (ClipboardItem) ((DefaultMutableTreeNode) newChild).getUserObject();
			if (this.isDontExpandOnNew(item.getRegisteredUserClass())) {
				this.dontExpandNewNodes.add(newChild);
			}
		}
		
		if (parent == this.getRoot() && newChild instanceof ClassTreeNode) {
			final String newChildName = this.getName(((ClassTreeNode) newChild).clazz);//this.namingMap.get(((ClassTreeNode) newChild).clazz);
			final int newChildIndex = this.indexOf(((ClassTreeNode) newChild).clazz);
			
			if (newChildName != null)
				newChild.setUserObject(newChildName);
			
			if (newChildIndex != -1) {
				int newIndex = 0;
				int siblingIndex;
				TreeNode sibling;
				for (int i=0; i<parent.getChildCount(); i++) {
					sibling = parent.getChildAt(i);
					if (sibling instanceof ClassTreeNode) {
						ClassTreeNode classNode = (ClassTreeNode) sibling;
						siblingIndex = this.indexOf(classNode.clazz);
						if (newChildIndex <= siblingIndex) {
							break;
						} else {
							newIndex++;
						}
					}
				}
				super.insertNodeInto(newChild, parent, newIndex);
			} else {
				if (!this.defaultIgnore)
					super.insertNodeInto(newChild, parent, index);
			}
//		} else if (parent.getParent() instanceof ClassTreeNode) {
//			final Class<?> itemClass = ((ClassTreeNode) parent.getParent()).clazz;
//			final int newChildIndex = this.indexOfLeaf(itemClass, ((DefaultMutableTreeNode) newChild).getUserObject().toString());
//			
//			if (newChildIndex != -1) {
//				int newIndex = 0;
//				int siblingIndex;
//				TreeNode sibling;
//				for (int i=0; i<parent.getChildCount(); i++) {
//					sibling = parent.getChildAt(i);
//					if (sibling instanceof DefaultMutableTreeNode) {
//						DefaultMutableTreeNode siblingNode = (DefaultMutableTreeNode) sibling;
//						siblingIndex = this.indexOfLeaf(itemClass, siblingNode.getUserObject().toString());
//						if (newChildIndex <= siblingIndex) {
//							break;
//						} else {
//							newIndex++;
//						}
//					}
//				}
//				super.insertNodeInto(newChild, parent, newIndex);
//			} else {
//				if (!this.defaultIgnore)
//					super.insertNodeInto(newChild, parent, index);
//			}
		} else {
			if (parent instanceof ClassTreeNode && newChild instanceof DefaultMutableTreeNode) {
				this.sortChildren(((ClassTreeNode) parent).clazz, (DefaultMutableTreeNode) newChild);
			}
			
			super.insertNodeInto(newChild, parent, index);
			
			if (parent instanceof ClassTreeNode && this.isAutoShow(((ClassTreeNode) parent).clazz) && newChild instanceof DefaultMutableTreeNode) {
				Object userObject = ((DefaultMutableTreeNode) newChild).getUserObject();
				if (userObject instanceof ClipboardItem) {
					Workbench.getInstance().showData((ClipboardItem) userObject);
				}
			}
		}
	}

}
