package org.org.repository.crawler.services;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.org.model.IModelItem;
import org.org.model.IModelItemListener;
import org.org.model.ModelItemEvent;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.HttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.RepositoryCrawlerService;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.protocolplugins.HttpRepositoryBrowserPlugin;

/**
 * @author zepag
 */
public class HttpCrawledRepositoryServiceIntegrationTest extends TestCase {

	@Override
	protected void setUp() throws Exception {

	}

	public void testMaven2Repo() {
		RepositoryCrawlerService<IHttpCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IHttpCrawledRepositorySetup>(new HttpRepositoryBrowserPlugin());
		HttpCrawledRepositorySetup repositorySetup = new HttpCrawledRepositorySetup("http://repo1.maven.org/maven2");
		Set<String> groupFilters = new HashSet<String>();
		groupFilters.add("org.org-libs");
		repositorySetup.setGroupFilters(groupFilters);
		final AtomicInteger atomicInteger = new AtomicInteger(0);
		CrawledRepository crawledRepository = mavenRepositoryService.retrieveRepository(repositorySetup, "CrawledRepository Maven 2", new IModelItemListener() {
			public void changeOccured(ModelItemEvent modelItemEvent) {
				if (modelItemEvent.getEventType() == ModelItemEvent.EventType.POST_ADD_CHILD) {
					atomicInteger.incrementAndGet();
				}
			}
		});
		assertNotNull(crawledRepository);
		assertTrue(crawledRepository.hasChildren());
	}

	public void testMaven2Group() {
		RepositoryCrawlerService<IHttpCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IHttpCrawledRepositorySetup>(new HttpRepositoryBrowserPlugin());
		HttpCrawledRepositorySetup repositorySetup = new HttpCrawledRepositorySetup("http://repo1.maven.org/maven2");
		Group group = mavenRepositoryService.retrieveGroup(repositorySetup, "org.springframework.ws");
		assertNotNull(group);
		assertEquals("org.springframework.ws", group.getName());
		assertTrue(group.hasChildren());
		for (Artifact modelItem : group.getChildren()) {
			assertTrue(modelItem.hasChildren());
		}

	}

	@SuppressWarnings("rawtypes")
	public void testMaven2Artifact() {
		RepositoryCrawlerService<IHttpCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IHttpCrawledRepositorySetup>(new HttpRepositoryBrowserPlugin());
		HttpCrawledRepositorySetup repositorySetup = new HttpCrawledRepositorySetup("http://repo1.maven.org/maven2");
		Artifact artifact = mavenRepositoryService.retrieveArtifact(repositorySetup, "org.springframework", "spring");
		assertNotNull(artifact);
		assertEquals("spring", artifact.getId());
		assertTrue(artifact.hasChildren());
		for (IModelItem modelItem : artifact.getChildren()) {
			assertTrue(modelItem instanceof ArtifactVersion);
		}
	}

	public void testMaven2ArtifactVersion() {
		RepositoryCrawlerService<IHttpCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IHttpCrawledRepositorySetup>(new HttpRepositoryBrowserPlugin());
		HttpCrawledRepositorySetup repositorySetup = new HttpCrawledRepositorySetup("http://repo1.maven.org/maven2");
		Set<ArtifactVersion> artifactVersions = mavenRepositoryService.retrieveArtifactVersions(repositorySetup, "org.springframework", "spring", "2.0.7");
		assertNotNull(artifactVersions);
		assertTrue(artifactVersions.size() > 0);
	}

	public void testJITR() {
		RepositoryCrawlerService<IHttpCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IHttpCrawledRepositorySetup>(new HttpRepositoryBrowserPlugin());
		HttpCrawledRepositorySetup repositorySetup = new HttpCrawledRepositorySetup("http://www.jitr.org/repositories/release/");
		repositorySetup.setPatternSet(HttpRepositoryBrowserPlugin.APACHE2_PATTERNSET);
		final AtomicInteger atomicInteger = new AtomicInteger(0);
		CrawledRepository crawledRepository = mavenRepositoryService.retrieveRepository(repositorySetup, "CrawledRepository Maven 2", new IModelItemListener() {
			public void changeOccured(ModelItemEvent modelItemEvent) {
				if (modelItemEvent.getEventType() == ModelItemEvent.EventType.POST_ADD_CHILD) {
					atomicInteger.incrementAndGet();
				}
			}
		});
		assertNotNull(crawledRepository);
		assertTrue(crawledRepository.hasChildren());
	}

	public void testS3Browse() {
		RepositoryCrawlerService<IHttpCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IHttpCrawledRepositorySetup>(new HttpRepositoryBrowserPlugin());
		HttpCrawledRepositorySetup repositorySetup = new HttpCrawledRepositorySetup("http://s3browse.com/explore/repository.springsource.com/maven/bundles/external/");
		repositorySetup.setPatternSet(HttpRepositoryBrowserPlugin.S3BROWSE_PATTERNSET);
		Set<String> groupFilters = new HashSet<String>();
		groupFilters.add("org.apache.commons");
		repositorySetup.setGroupFilters(groupFilters);
		final AtomicInteger atomicInteger = new AtomicInteger(0);
		CrawledRepository crawledRepository = mavenRepositoryService.retrieveRepository(repositorySetup, "CrawledRepository Maven 2", new IModelItemListener() {
			public void changeOccured(ModelItemEvent modelItemEvent) {
				if (modelItemEvent.getEventType() == ModelItemEvent.EventType.POST_ADD_CHILD) {
					atomicInteger.incrementAndGet();
				}
			}
		});
		assertNotNull(crawledRepository);
		assertTrue(crawledRepository.hasChildren());
	}
}