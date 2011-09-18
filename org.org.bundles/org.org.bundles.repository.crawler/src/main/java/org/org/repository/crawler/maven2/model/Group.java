/*
 org.org.lib.repository.crawler is a java library/OSGI Bundle
 Providing Crawling capabilities for Maven 2 HTTP exposed repositories
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
package org.org.repository.crawler.maven2.model;

import org.org.model.AbstractModelItem;

/**
 * @author pagregoire
 */
public class Group extends AbstractModelItem<GroupsHolder, Artifact> {
	public Group(String name) {
		this.name = name;
	}

	public String getUID() {
		return name;
	}

	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(name);
	}

	private String name;

	public static final String SEPARATOR = "/";

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
}