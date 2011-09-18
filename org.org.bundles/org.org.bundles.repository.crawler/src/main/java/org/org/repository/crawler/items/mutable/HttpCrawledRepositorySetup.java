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

import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.IPatternSet;
import org.org.repository.crawler.items.immutable.AbstractImmutableCrawledRepositorySetup;
import org.org.repository.crawler.items.immutable.ImmutableHttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.protocolplugins.HttpRepositoryBrowserPlugin;

/**
 * @author pagregoire
 */
public class HttpCrawledRepositorySetup extends AbstractCrawledRepositorySetup implements IHttpCrawledRepositorySetup {

	private IPatternSet patternSet = HttpRepositoryBrowserPlugin.APACHE2_PATTERNSET;

	private String baseUrl;

	private String proxyHost;

	private Integer proxyPort;

	public HttpCrawledRepositorySetup(String baseUrl) {
		super(baseUrl);
		this.baseUrl = baseUrl;
	}

	public HttpCrawledRepositorySetup(String baseUrl, Set<String> groupFilters) {
		super(baseUrl, groupFilters);
		this.baseUrl = baseUrl;
	}

	public HttpCrawledRepositorySetup(IHttpCrawledRepositorySetup httpCrawledRepositorySetup) {
		super(httpCrawledRepositorySetup.getId(), httpCrawledRepositorySetup.getGroupFilters());
		this.baseUrl = httpCrawledRepositorySetup.getBaseUrl();
		this.patternSet = httpCrawledRepositorySetup.getPatternSet();
		this.proxyHost = httpCrawledRepositorySetup.getProxyHost();
		this.proxyPort = httpCrawledRepositorySetup.getProxyPort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.mutable.IHttpRepositorySetup#getBaseUrl()
	 */
	public String getBaseUrl() {
		return this.baseUrl;
	}

	/**
	 * @param baseUrl
	 *            The baseUrl to set.
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.mutable.IHttpRepositorySetup#getProxyHost()
	 */
	public String getProxyHost() {
		return this.proxyHost;
	}

	/**
	 * @param proxyHost
	 *            The proxyHost to set.
	 */
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.mutable.IHttpRepositorySetup#getProxyPort()
	 */
	public Integer getProxyPort() {
		return this.proxyPort;
	}

	/**
	 * @param proxyPort
	 *            The proxyPort to set.
	 */
	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public AbstractCrawledRepositorySetup getMutable() {
		return this;
	}

	public AbstractImmutableCrawledRepositorySetup getImmutable() {
		return new ImmutableHttpCrawledRepositorySetup(this);
	}

	public IPatternSet getPatternSet() {
		return patternSet.getMutable();
	}

	public void setPatternSet(IPatternSet patternSet) {
		this.patternSet = patternSet;
	}
}
