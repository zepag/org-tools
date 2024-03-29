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

@SuppressWarnings("rawtypes")
public interface IDuplicateable<T extends AbstractModelItem> {
	/**
	 * Duplicates the given modelItem to a new modelItem with a different id.<br>
	 * New objects should be created for children also, parent should be removed,<br>
	 * and references to parent in children updated.
	 * 
	 * @param changedState
	 * @return
	 */
	public T duplicate(Object... changedData);
}