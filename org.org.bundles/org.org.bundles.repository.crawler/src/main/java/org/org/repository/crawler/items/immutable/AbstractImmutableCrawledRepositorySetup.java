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
package org.org.repository.crawler.items.immutable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.org.repository.crawler.items.ICrawledRepositorySetup;

public abstract class AbstractImmutableCrawledRepositorySetup implements ICrawledRepositorySetup {
	private final String id;

	private final Set<String> groupFilters;

	public AbstractImmutableCrawledRepositorySetup(final String id, final Set<String> groupFilters) {
		super();
		this.id = id;
		this.groupFilters = Collections.<String> unmodifiableSet((groupFilters == null) ? new LinkedHashSet<String>() : groupFilters);
	}

	public AbstractImmutableCrawledRepositorySetup(final ICrawledRepositorySetup crawledRepositorySetup) {
		super();
		this.id = crawledRepositorySetup.getId();
		this.groupFilters = Collections.unmodifiableSet(crawledRepositorySetup.getGroupFilters());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.IRepositorySetup#getGroupFilters()
	 */
	public Set<String> getGroupFilters() {
		return groupFilters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.IRepositorySetup#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.IRepositorySetup#isFiltered()
	 */
	public boolean hasGroupFilters() {
		return groupFilters != null && !groupFilters.isEmpty();
	}

}
