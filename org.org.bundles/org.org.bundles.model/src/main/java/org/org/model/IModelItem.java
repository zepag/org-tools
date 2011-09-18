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

import java.util.Set;

/**
 * This interface is for a tree Model Item. A Tree Model has parent and childs and methods to handle them. It also has a UID.
 * 
 * @author pagregoire
 */
@SuppressWarnings("rawtypes")
public interface IModelItem<P extends IModelItem, C extends IModelItem> {

	/**
	 * This method returns the parent for this model item or null if it doesn't have any parent.
	 * 
	 * @return another IModelItem implementation
	 */
	public P getParent();

	/**
	 * This method returns a list of children model items for this model item.
	 * 
	 * @return a list of IModelItem implementations.
	 */
	public Set<C> getChildren();

	/**
	 * This method returns a child model item for this model item.
	 * 
	 * @param UID
	 *            the unique ID of the child model item to retrieve
	 * @return an IModelItem implementation
	 */
	public C getChild(String UID);

	/**
	 * This method tests if this model item has any children model items.
	 * 
	 * @return <b>true</b> if it has children, <b>false </b> if it doesn't
	 */
	public boolean hasChildren();

	/**
	 * This method tests if this model item has any children model items.
	 * 
	 * @param UID
	 *            the unique ID of the child model item to retrieve
	 * @return <b>true</b> if it has children, <b>false </b> if it doesn't
	 */
	public boolean hasChild(String UID);

	/**
	 * This method returns the unique ID of this model item. Note that the unique ID generation is not handled by this interface. It is left to the implementor.
	 * 
	 * @return a String representing the unique ID
	 */
	public String getUID();

	/**
	 * This method clears all children references from this model item.
	 */
	public void clearChildren();

	/**
	 * This method removes a given children reference from this model item.
	 * 
	 * @param UID
	 *            the Unique id of the reference to remove.
	 */
	public void removeChild(String UID);

	/**
	 * This method adds a child model item to this model item
	 * 
	 * @param modelItem
	 *            an IModelItem implementation
	 */
	public void addChild(C modelItem);

	/**
	 * This method adds this child model item as a child model item to another model item. Calling this method on a root model item will throw a ModelException
	 * 
	 * @param modelItem
	 *            an IModelItem implementation
	 */
	public void setParent(P modelItem);

	public void accept(IModelItemVisitor visitor);
}
