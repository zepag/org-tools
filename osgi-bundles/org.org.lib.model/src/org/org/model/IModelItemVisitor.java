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

/**
 * This interface describes a visitor for the model.
 * 
 * @author pagregoire
 */
public interface IModelItemVisitor {
    /**
     * Visits the given item element.
     * 
     * @param modelItem
     *            the model element to visit
     * @return <code>true</code> if the elements's members should be visited; <code>false</code> if they should be skipped
     */
    @SuppressWarnings("unchecked")
	public boolean visit(IModelItem modelItem);
}