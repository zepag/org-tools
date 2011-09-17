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
 * This class defines a Model Item Event triggered by some action against the model.
 * 
 * @author pagregoire
 */
public class ModelItemEvent {
	/**
	 * the item that the event originated from.
	 */
	@SuppressWarnings("unchecked")
	private IModelItem sourceItem;

	/**
	 * an item directly concerned by this event.
	 */
	@SuppressWarnings("unchecked")
	private IModelItem targetItem;

	/**
	 * the eventType taken from the constants in this class.
	 */
	private EventType eventType;

	public enum EventType {
		ITEM_PROPERTY_CHANGED, PRE_ADD_CHILD, POST_ADD_CHILD, PRE_REMOVE_CHILD, POST_REMOVE_CHILD, PRE_UPDATE_CHILD, POST_UPDATE_CHILD
	}

	/**
	 * This Constructor creates an event specifying its source, target and type.
	 * 
	 * @param sourceItem
	 * @param targetItem
	 * @param eventType
	 */
	@SuppressWarnings("unchecked")
	public ModelItemEvent(IModelItem sourceItem, IModelItem targetItem, EventType eventType) {
		this.sourceItem = sourceItem;
		this.targetItem = targetItem;
		this.eventType = eventType;
	}

	/**
	 * This method returns the event type of this event.
	 * 
	 * @return an integer value taken from the constants of this class.
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * This method returns the model item that originated this event.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IModelItem getSourceItem() {
		return sourceItem;
	}

	/**
	 * This method another affected item.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IModelItem getTargetItem() {
		return targetItem;
	}

	public String toString() {
		return eventType.toString() + "{sourceItem=" + sourceItem + ",targetItem" + targetItem + "}";
	}
}
