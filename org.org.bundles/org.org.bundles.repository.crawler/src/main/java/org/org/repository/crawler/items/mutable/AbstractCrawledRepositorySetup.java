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
package org.org.repository.crawler.items.mutable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.org.repository.crawler.items.ICrawledRepositorySetup;

public abstract class AbstractCrawledRepositorySetup implements ICrawledRepositorySetup {

	public static final String KEEP_ALL_PATTERN = ".*";

	private String id;

	private Set<String> groupFilters;

	public AbstractCrawledRepositorySetup(String id) {
		this(id, Collections.<String> emptySet());
	}

	public AbstractCrawledRepositorySetup(String id, Set<String> groupFilters) {
		this.id = id;
		this.groupFilters = groupFilters == null ? null : new LinkedHashSet<String>(groupFilters);
		if (groupFilters.size() == 0) {
			this.groupFilters.add(KEEP_ALL_PATTERN);
		}
	}

	public AbstractCrawledRepositorySetup(ICrawledRepositorySetup crawledRepositorySetup) {
		this.id = crawledRepositorySetup.getId();
		this.groupFilters = new LinkedHashSet<String>(crawledRepositorySetup.getGroupFilters());
		if (groupFilters.size() == 0) {
			this.groupFilters.add(KEEP_ALL_PATTERN);
		}
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

	public void setGroupFilters(Set<String> groupFilters) {
		this.groupFilters = (groupFilters == null) ? Collections.<String> emptySet() : groupFilters;
	}

	public void setId(String id) {
		this.id = id;
	}
}
