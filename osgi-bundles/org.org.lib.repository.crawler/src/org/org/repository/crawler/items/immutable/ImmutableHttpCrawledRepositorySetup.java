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

import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.IPatternSet;
import org.org.repository.crawler.items.mutable.AbstractCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.HttpCrawledRepositorySetup;

public class ImmutableHttpCrawledRepositorySetup extends AbstractImmutableCrawledRepositorySetup implements IHttpCrawledRepositorySetup {
	private final IPatternSet patternSet;

	private final String baseUrl;

	private final String proxyHost;

	private final Integer proxyPort;

	public ImmutableHttpCrawledRepositorySetup(String id, Set<String> groupFilters, final IPatternSet patternSet, final String baseUrl, final String proxyHost, final Integer proxyPort) {
		super(id, groupFilters);
		this.patternSet = patternSet;
		this.baseUrl = baseUrl;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}

	public ImmutableHttpCrawledRepositorySetup(IHttpCrawledRepositorySetup httpCrawledRepositorySetup) {
		super(httpCrawledRepositorySetup.getId(), httpCrawledRepositorySetup.getGroupFilters());
		this.patternSet = httpCrawledRepositorySetup.getPatternSet().getImmutable();
		this.baseUrl = httpCrawledRepositorySetup.getBaseUrl();
		this.proxyHost = httpCrawledRepositorySetup.getProxyHost();
		this.proxyPort = httpCrawledRepositorySetup.getProxyPort();
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public IPatternSet getPatternSet() {
		return patternSet;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public AbstractImmutableCrawledRepositorySetup getImmutable() {
		return this;
	}

	public AbstractCrawledRepositorySetup getMutable() {
		return new HttpCrawledRepositorySetup(this);
	}
}
