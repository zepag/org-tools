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
package org.org.repository.crawler.items;

import java.util.Set;

import org.org.repository.crawler.items.immutable.AbstractImmutableCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.AbstractCrawledRepositorySetup;

public interface ICrawledRepositorySetup {
	
	public abstract Set<String> getGroupFilters();
	
	public abstract String getId();

	public abstract boolean hasGroupFilters();

	public abstract AbstractImmutableCrawledRepositorySetup getImmutable();

	public abstract AbstractCrawledRepositorySetup getMutable();
}