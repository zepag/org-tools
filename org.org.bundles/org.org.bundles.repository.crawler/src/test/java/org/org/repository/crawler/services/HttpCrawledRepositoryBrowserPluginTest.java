package org.org.repository.crawler.services;

import java.util.Queue;

import junit.framework.TestCase;

import org.org.repository.crawler.items.mutable.HttpCrawledRepositorySetup;
import org.org.repository.crawler.mapping.Entry;
import org.org.repository.crawler.maven2.model.protocolplugins.HttpRepositoryBrowserPlugin;

public class HttpCrawledRepositoryBrowserPluginTest extends TestCase {

	public void testPlugin() throws Exception {
		HttpCrawledRepositorySetup repositorySetup = new HttpCrawledRepositorySetup("http://repo1.maven.org/maven2");
		HttpRepositoryBrowserPlugin browserPlugin = new HttpRepositoryBrowserPlugin();
		browserPlugin.init(repositorySetup);
		Queue<Entry> entries = browserPlugin.getEntryList("http://repo1.maven.org/maven2");
		assertFalse(entries.isEmpty());
	}

	public void testPlugin2() throws Exception {
		HttpCrawledRepositorySetup repositorySetup = new HttpCrawledRepositorySetup("http://s3browse.com/explore/repository.springsource.com/maven/bundles/external/org/apache/commons/com.springsource.org.apache.commons.attributes/");
		repositorySetup.setPatternSet(HttpRepositoryBrowserPlugin.S3BROWSE_PATTERNSET);
		HttpRepositoryBrowserPlugin browserPlugin = new HttpRepositoryBrowserPlugin();
		browserPlugin.init(repositorySetup);
		Queue<Entry> entries = browserPlugin.getEntryList("http://s3browse.com/explore/repository.springsource.com/maven/bundles/external/org/apache/commons/com.springsource.org.apache.commons.attributes/");
		assertFalse(entries.isEmpty());
	}
}
