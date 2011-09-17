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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class defines the RootModelItem singleton registry.
 * 
 * @author pagregoire
 */
@SuppressWarnings("unchecked")
public class RootModelItem<C extends IModelItem> extends AbstractModelItem<IModelItem,C> implements IModelItem<IModelItem,C> {
	/**
	 * the default UID for the RootModelItem
	 */
	public String UID;

	/**
	 * the instances registry of RootModelItem singleton
	 */
	private static Map<String, RootModelItem> instances = new ConcurrentHashMap<String, RootModelItem>();

	/**
	 * The default private constructor.
	 */
	private RootModelItem(String uid) {
		this.UID = uid;
	}

	/**
	 * @see org.org.model.IModelItem#setParent(org.org.model.IModelItem)
	 */
	public void setParent(IModelItem modelItem) {
		throw new ModelException("Impossible to set a parent to a Root Item");
	}

	/**
	 * This method tests if the RootModelItem instance exists.
	 * 
	 * @return true if it exists, false otherwise.
	 */
	public static boolean isInstanciated(String rootModelItemUID) {
		return (instances.containsKey(rootModelItemUID));
	}

	/**
	 * This method returns the RootModelItem instance, creating it if it doesn't
	 * already exist.
	 * 
	 * @return the RootModelItem instance
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IModelItem> RootModelItem<T> getInstance(String rootModelItemUID) {
		if (!instances.containsKey(rootModelItemUID)) {
			instances.put(rootModelItemUID, new RootModelItem(rootModelItemUID));
		}
		return instances.get(rootModelItemUID);
	}
	
	/**
	 * @see org.org.model.IModelItem#getParent()
	 */
	public IModelItem getParent() {
		return null;
	}

	/**
	 * This method returns a description of this RootModelItem instance.
	 * 
	 * @return a StringBuffer containing the description
	 */
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(UID);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(toStringBuilderDescription());
		for (IModelItem key : getChildren()) {
			buffer.append(key.getUID());
		}
		return buffer.toString();
	}

	@Override
	public String getUID() {
		return UID;
	}
}
