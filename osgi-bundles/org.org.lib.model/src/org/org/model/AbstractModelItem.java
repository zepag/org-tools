/*
 org.org.lib.model is a java library/OSGI Bundle
 Providing a tree model utility.
 Copyright (C) 2007  Pierre-Antoine Grégoire
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.org.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is an abstract definition of a model item. It should be implemented.
 * 
 * @author pagregoire
 */
@SuppressWarnings("unchecked")
public abstract class AbstractModelItem<P extends IModelItem, C extends IModelItem> implements IModelItem<P, C>, Comparable<IModelItem> {
	private volatile int hashcode = 0;

	/**
	 * The children of the model item.
	 */
	protected Map<String, C> children = new ConcurrentHashMap<String, C>();

	/**
	 * the listeners registered in this RootModelItem
	 */
	private List<IModelItemListener> listeners = new CopyOnWriteArrayList<IModelItemListener>();

	/**
	 * the flag toggling on and off the listeners.
	 */
	private boolean listenersToggled = true;

	/**
	 * This method registers a listener.
	 * 
	 * @param listener
	 *            an implementation of IModelListener
	 */
	public void addListener(IModelItemListener listener) {
		listeners.add(listener);
	}

	/**
	 * This method de-registers a listener.
	 * 
	 * @param listener
	 *            an implementation of IModelListener
	 */
	public void removeListener(IModelItemListener listener) {
		listeners.remove(listener);
	}

	/**
	 * This method toggle the listeners' triggering ON
	 */
	public void toggleListenersOn() {
		listenersToggled = true;
	}

	/**
	 * This method toggle the listeners' triggering OFF
	 */
	public void toggleListenersOff() {
		listenersToggled = false;
	}

	/**
	 * This method is used by AbstractModelItem implementors to trigger the listeners from this RootModelItem.
	 * 
	 * @param event
	 */
	protected void triggerListeners(ModelItemEvent event) {
		if (listenersToggled) {
			for (IModelItemListener listener : listeners) {
				listener.changeOccured(event);
			}
		}
	}

	/**
	 * The parent of the model item.
	 */
	protected P parent;

	/**
	 * This method propagates a given event to the parent model item or triggers the listeners if the parent element is a RootModelItem.
	 * 
	 * @param modelItemEvent
	 *            the event to propagate
	 * @see RootModelItem#triggerListeners(ModelItemEvent)
	 */
	@SuppressWarnings("unchecked")
	protected void propagateEvent(ModelItemEvent modelItemEvent) {
		if (this.parent != null) {
			if (this.parent instanceof AbstractModelItem) {
				((AbstractModelItem) parent).propagateEvent(modelItemEvent);
			}
		}
		if (this.listeners.size() != 0) {
			triggerListeners(modelItemEvent);
		}
	}

	/**
	 * @see org.org.model.IModelItem#addChild(org.org.model.IModelItem)
	 */
	@SuppressWarnings("unchecked")
	public void addChild(C child) {
		boolean isAnUpdate = children.containsKey(child.getUID());
		if (isAnUpdate) {
			propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.EventType.PRE_UPDATE_CHILD));
		} else {
			propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.EventType.PRE_ADD_CHILD));
		}
		children.put(child.getUID(), child);
		if (child instanceof AbstractModelItem) {
			if (child.getParent() == null) {
				child.setParent(this);
			}
		}
		if (isAnUpdate) {
			propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.EventType.POST_UPDATE_CHILD));
		} else {
			propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.EventType.POST_ADD_CHILD));
		}
	}

	/**
	 * @see org.org.model.IModelItem#setParent(org.org.model.IModelItem)
	 */
	@SuppressWarnings("unchecked")
	public void setParent(P parent) {
		if (parent instanceof AbstractModelItem && !parent.hasChild(this.getUID())) {
			parent.addChild(this);
		}
		this.parent = parent;
		propagateEvent(new ModelItemEvent(this, parent, ModelItemEvent.EventType.ITEM_PROPERTY_CHANGED));
	}

	/**
	 * @see org.org.model.IModelItem#clearChildren()
	 */
	public void clearChildren() {
		for (String key : children.keySet()) {
			removeChild(key);
		}
	}

	/**
	 * @see org.org.model.IModelItem#removeChild(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public void removeChild(String UID) {
		IModelItem child = (IModelItem) children.get(UID);
		propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.EventType.PRE_REMOVE_CHILD));
		children.remove(UID);
		propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.EventType.POST_REMOVE_CHILD));
	}

	/**
	 * @see org.org.model.IModelItem#getChildren()
	 */
	public Set<C> getChildren() {
		return new TreeSet<C>(children.values());
	}

	/**
	 * @see org.org.model.IModelItem#hasChildren()
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * @see org.org.model.IModelItem#hasChild(java.lang.String)
	 */
	public boolean hasChild(String UID) {
		return children.containsKey(UID);
	}

	/**
	 * @see org.org.model.IModelItem#getChild(java.lang.String)
	 */
	public C getChild(String UID) {
		return children.get(UID);
	}

	/**
	 * @see org.org.model.IModelItem#getParent()
	 */
	public P getParent() {
		return parent;
	}

	/**
	 * @see org.org.model.IModelItem#getUID()
	 */
	public abstract String getUID();

	/**
	 * @return
	 */
	public abstract StringBuilder toStringBuilderDescription();

	/**
	 * This is an utility method that allows a simple tabbed display of the toString() representation of the objects' tree.
	 * 
	 * @param level
	 *            an integer representing the number of tabulations to display before the current element.
	 * @return a StringBuffer containing a representation of this model item and of its children.
	 */
	@SuppressWarnings("unchecked")
	protected StringBuilder toStringBuilderChildren(int level) {
		StringBuilder result = new StringBuilder();
		result.append(toStringBuilderDescription());
		for (C next : children.values()) {
			result.append("\n");
			for (int i = 0; i < level; i++) {
				result.append("\t");
			}
			result.append(((AbstractModelItem) next).toStringBuilderChildren(level + 1));
		}
		return result;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(IModelItem o) {
		int result = 0;
		if (o instanceof AbstractModelItem) {
			result = getUID().compareTo(((AbstractModelItem) o).getUID());
		} else {
			result = 1;
		}
		return result;
	}

	public void accept(IModelItemVisitor visitor) {
		boolean shouldContinue = visitor.visit(this);
		if (shouldContinue) {
			for (Iterator<C> it = getChildren().iterator(); it.hasNext();) {
				it.next().accept(visitor);
			}
		}
		if (visitor instanceof IModelItemAdvancedVisitor) {
			((IModelItemAdvancedVisitor) visitor).aftervisit(this, shouldContinue);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[\n");
		result.append(toStringBuilderChildren(1));
		result.append("\n]");
		return result.toString();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (hashcode == 0) {
			hashcode = 37;
			hashcode = 17 * hashcode + getUID().hashCode();
		}
		return hashcode;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if ((obj != null) && (obj.getClass().equals(this.getClass()))) {
			result = ((IModelItem) obj).getUID().equals(this.getUID());
		}
		return result;
	}

}
