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
 * This interface describes what a listener for the model's events has to implement.
 * @author pagregoire
 */
public interface IModelItemListener {
    
    /**
     * This method will be called is this listener is registered to the RootModelItem, when an Event is triggered on any model item.
     * @param modelItemEvent the event that has been propagated.
     */
    public void changeOccured(ModelItemEvent modelItemEvent);
}
