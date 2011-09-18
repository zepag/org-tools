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

import java.util.Set;

import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.AbstractCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.FileSystemCrawledRepositorySetup;

/**
 * @author pagregoire
 */
public class ImmutableFileSystemCrawledRepositorySetup extends AbstractImmutableCrawledRepositorySetup implements IFileSystemCrawledRepositorySetup {

	private final String basePath;

	public ImmutableFileSystemCrawledRepositorySetup(String id, Set<String> groupFilters,final String basePath) {
		super(id, groupFilters);
		this.basePath = basePath;
	}

	public ImmutableFileSystemCrawledRepositorySetup(IFileSystemCrawledRepositorySetup fileSystemCrawledRepositorySetup) {
		super(fileSystemCrawledRepositorySetup.getId(), fileSystemCrawledRepositorySetup.getGroupFilters());
		this.basePath = fileSystemCrawledRepositorySetup.getBasePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.mutable.IFileSystemRepositorySetup#getBasePath()
	 */
	public String getBasePath() {
		return this.basePath;
	}

	public AbstractImmutableCrawledRepositorySetup getImmutable() {
		return this;
	}

	public AbstractCrawledRepositorySetup getMutable() {
		return new FileSystemCrawledRepositorySetup(this);
	}
}
