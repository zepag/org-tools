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

import java.util.Set;

import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.immutable.AbstractImmutableCrawledRepositorySetup;
import org.org.repository.crawler.items.immutable.ImmutableFileSystemCrawledRepositorySetup;

/**
 * @author pagregoire
 */
public class FileSystemCrawledRepositorySetup extends AbstractCrawledRepositorySetup implements IFileSystemCrawledRepositorySetup {

	private String basePath;

	public FileSystemCrawledRepositorySetup(String basePath) {
		super(basePath);
		this.basePath = basePath;
	}

	public FileSystemCrawledRepositorySetup(String basePath, Set<String> groupFilters) {
		super(basePath, groupFilters);
		this.basePath = basePath;
	}

	public FileSystemCrawledRepositorySetup(IFileSystemCrawledRepositorySetup fileSystemCrawledRepositorySetup) {
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

	/**
	 * @param basePath
	 *            The basePath to set.
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public AbstractCrawledRepositorySetup getMutable() {
		return this;
	}

	public AbstractImmutableCrawledRepositorySetup getImmutable() {
		return new ImmutableFileSystemCrawledRepositorySetup(this);
	}
}