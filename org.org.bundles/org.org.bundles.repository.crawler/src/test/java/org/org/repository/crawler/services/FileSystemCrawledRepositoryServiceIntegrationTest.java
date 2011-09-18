package org.org.repository.crawler.services;

import java.io.File;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.org.model.IModelItem;
import org.org.model.IModelItemListener;
import org.org.model.IModelItemVisitor;
import org.org.model.ModelItemEvent;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.FileSystemCrawledRepositorySetup;
import org.org.repository.crawler.maven2.RepositoryCrawlerService;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.protocolplugins.FileSystemRepositoryBrowserPlugin;

/**
 * @author zepag
 */
public class FileSystemCrawledRepositoryServiceIntegrationTest extends TestCase {
	public final static String REPOSITORY_PATH;

	public final static Integer TOTAL_ARTIFACT_VERSIONS = 6;

	static {
		String tmpPath = null;
		try {
			tmpPath = new File(Thread.currentThread().getContextClassLoader().getResource("test-repo").toURI()).getAbsolutePath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		REPOSITORY_PATH = tmpPath;
	}

	public static class CounterModelItemListener implements IModelItemListener {
		private final AtomicInteger atomicInteger = new AtomicInteger(0);

		private final Class<?> modelItemType;

		public CounterModelItemListener(final Class<?> modelItemType) {
			this.modelItemType = modelItemType;
		}

		public void changeOccured(ModelItemEvent modelItemEvent) {
			if (modelItemEvent.getEventType() == ModelItemEvent.EventType.POST_ADD_CHILD && modelItemType.isAssignableFrom(modelItemEvent.getTargetItem().getClass())) {
				atomicInteger.incrementAndGet();
			}
		}

		public void clearCounter() {
			atomicInteger.set(0);
		}

		public Integer getCount() {
			return atomicInteger.get();
		}
	}

	public static class CounterModelVisitor implements IModelItemVisitor {

		private final AtomicInteger atomicInteger = new AtomicInteger(0);

		private final Class<?> modelItemType;

		public CounterModelVisitor(final Class<?> modelItemType) {
			this.modelItemType = modelItemType;
		}

		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			if (modelItemType.isAssignableFrom(modelItem.getClass())) {
				atomicInteger.incrementAndGet();
			}
			return true;
		}

		public void clearCounter() {
			atomicInteger.set(0);
		}

		public Integer getCount() {
			return atomicInteger.get();
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		File repositoryPath = new File(REPOSITORY_PATH);
		assertTrue("Test repository " + REPOSITORY_PATH + " is not available.", repositoryPath.exists());
	}

	/**
	 * This tests the retrieval of the whole specified repository
	 */
	public void testRetrieveRepo() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH);
		CounterModelItemListener artifactVersionCounter = new CounterModelItemListener(ArtifactVersion.class);
		CrawledRepository crawledRepository = mavenRepositoryService.retrieveRepository(repositorySetup, "CrawledRepository Maven local", artifactVersionCounter);
		assertNotNull(crawledRepository);
		assertTrue("CrawledRepository has children", crawledRepository.hasChildren());
		assertEquals(TOTAL_ARTIFACT_VERSIONS, artifactVersionCounter.getCount());
	}

	/**
	 * This tests the retrieval of the specified repository with a filter on the "org.org-libs" group.<br>
	 * This should retrieve all the artifacts with a group name starting with "org.org-libs".
	 */
	public void testRetrieveRepoWithFilter() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		Set<String> groupFilters = new LinkedHashSet<String>();
		groupFilters.add("org.org-libs.*");
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH, groupFilters);
		CounterModelItemListener artifactVersionCounter = new CounterModelItemListener(ArtifactVersion.class);
		CrawledRepository crawledRepository = mavenRepositoryService.retrieveRepository(repositorySetup, "CrawledRepository Maven local", artifactVersionCounter);
		assertTrue("CrawledRepository has children", crawledRepository.hasChildren());
		assertEquals(new Integer(TOTAL_ARTIFACT_VERSIONS - 1), artifactVersionCounter.getCount());
	}

	/**
	 * This tests the retrieval of the specified repository with a precise group specified.<br>
	 * This should retrieve all the repository.
	 */
	public void testRetrieveRepoWithPreciseGroupFilter() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		Set<String> groupFilters = new LinkedHashSet<String>();
		groupFilters.add("org.org-libs");
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH, groupFilters);
		CounterModelItemListener artifactVersionCounter = new CounterModelItemListener(ArtifactVersion.class);
		CrawledRepository crawledRepository = mavenRepositoryService.retrieveRepository(repositorySetup, "CrawledRepository Maven local", artifactVersionCounter);
		assertTrue("CrawledRepository has children", crawledRepository.hasChildren());
		assertEquals(new Integer(TOTAL_ARTIFACT_VERSIONS - 2), artifactVersionCounter.getCount());
	}

	/**
	 * 
	 */
	public void testRetrieveGroup() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH);
		CounterModelItemListener artifactVersionCounter = new CounterModelItemListener(ArtifactVersion.class);
		Group group = mavenRepositoryService.retrieveGroup(repositorySetup, "org.org-libs", artifactVersionCounter);
		assertNotNull(group);
		assertEquals("org.org-libs", group.getName());
		assertTrue("Group has children", group.hasChildren());
		for (Artifact modelItem : group.getChildren()) {
			assertTrue(modelItem.hasChildren());
		}
		assertEquals(new Integer(TOTAL_ARTIFACT_VERSIONS - 2), artifactVersionCounter.getCount());
	}

	/**
	 * 
	 */
	public void testRetrieveGroup2() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH);
		CounterModelItemListener artifactVersionCounter = new CounterModelItemListener(ArtifactVersion.class);
		Group group = mavenRepositoryService.retrieveGroup(repositorySetup, "org.org-libs.org-libs-usurper", artifactVersionCounter);
		assertNotNull(group);
		assertEquals("org.org-libs.org-libs-usurper", group.getName());
		assertTrue("Group has children", group.hasChildren());
		for (Artifact modelItem : group.getChildren()) {
			assertTrue(modelItem.hasChildren());
		}
		assertEquals(new Integer(1), artifactVersionCounter.getCount());
	}

	/**
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public void testRetrieveArtifact() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH);
		CounterModelItemListener artifactVersionCounter = new CounterModelItemListener(ArtifactVersion.class);
		Artifact artifact = mavenRepositoryService.retrieveArtifact(repositorySetup, "org.org-libs", "org-libs-usurper", artifactVersionCounter);
		assertNotNull(artifact);
		assertEquals("org-libs-usurper", artifact.getId());
		assertTrue(artifact.hasChildren());
		for (IModelItem modelItem : artifact.getChildren()) {
			assertTrue(modelItem instanceof ArtifactVersion);
		}
		assertEquals(new Integer(2), artifactVersionCounter.getCount());
	}

	/**
	 * 
	 */
	public void testRetrieveArtifactVersion() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH);
		Set<ArtifactVersion> artifactVersions = mavenRepositoryService.retrieveArtifactVersions(repositorySetup, "org.org-libs", "org-libs-usurper", "1.0.0");
		assertTrue(artifactVersions.size() > 0);
		assertEquals(artifactVersions.size(), 2);
	}

	/**
	 * 
	 */
	public void testRefreshRepoWithPreciseGroup() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH);
		CounterModelItemListener artifactVersionCounter = new CounterModelItemListener(ArtifactVersion.class);
		CrawledRepository crawledRepository = mavenRepositoryService.retrieveRepository(repositorySetup, "CrawledRepository Maven local", artifactVersionCounter);
		assertNotNull(crawledRepository);
		assertTrue(crawledRepository.hasChildren());
		assertEquals(TOTAL_ARTIFACT_VERSIONS, artifactVersionCounter.getCount());

		artifactVersionCounter.clearCounter();
		crawledRepository.addGroupFilter("org.org-libs");
		crawledRepository.clearChildren();
		crawledRepository = mavenRepositoryService.refreshRepository(crawledRepository, crawledRepository.getLabel(), artifactVersionCounter);
		assertNotNull(crawledRepository);
		assertTrue("CrawledRepository has children", crawledRepository.hasChildren());
		assertEquals(new Integer(TOTAL_ARTIFACT_VERSIONS - 2), artifactVersionCounter.getCount());
	}

	/**
	 * 
	 */
	public void testRefreshRepoWithPreciseGroupAndFormerGroupsKept() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH);
		CounterModelItemListener artifactVersionCounter = new CounterModelItemListener(ArtifactVersion.class);
		CrawledRepository crawledRepository = mavenRepositoryService.retrieveRepository(repositorySetup, "CrawledRepository Maven local", artifactVersionCounter);
		assertNotNull(crawledRepository);
		assertTrue(crawledRepository.hasChildren());
		assertEquals(TOTAL_ARTIFACT_VERSIONS, artifactVersionCounter.getCount());

		artifactVersionCounter.clearCounter();
		crawledRepository.addGroupFilter("org.org-libs");
		crawledRepository = mavenRepositoryService.refreshRepository(crawledRepository, crawledRepository.getLabel(), artifactVersionCounter);
		assertNotNull(crawledRepository);
		assertTrue(crawledRepository.hasChildren());
		assertTrue(crawledRepository.getRepositorySetup().getGroupFilters().contains("org"));
		assertTrue(crawledRepository.getRepositorySetup().getGroupFilters().contains("org.org-libs"));
		assertTrue(crawledRepository.getRepositorySetup().getGroupFilters().contains("org.org-libs.org-libs-usurper"));
		assertEquals(3, crawledRepository.getChildren().size());
		CounterModelVisitor artifactVersionCountVisitor = new CounterModelVisitor(ArtifactVersion.class);
		crawledRepository.accept(artifactVersionCountVisitor);
		assertEquals(TOTAL_ARTIFACT_VERSIONS, artifactVersionCountVisitor.getCount());
		assertEquals(new Integer(0), artifactVersionCounter.getCount());
	}

	/**
	 * 
	 */
	public void testRefreshRepoWithFilter() {
		RepositoryCrawlerService<IFileSystemCrawledRepositorySetup> mavenRepositoryService = new RepositoryCrawlerService<IFileSystemCrawledRepositorySetup>(new FileSystemRepositoryBrowserPlugin());
		IFileSystemCrawledRepositorySetup repositorySetup = new FileSystemCrawledRepositorySetup(REPOSITORY_PATH);
		CounterModelItemListener artifactVersionCounter = new CounterModelItemListener(ArtifactVersion.class);
		CrawledRepository crawledRepository = mavenRepositoryService.retrieveRepository(repositorySetup, "CrawledRepository Maven local", artifactVersionCounter);
		assertNotNull(crawledRepository);
		assertTrue("CrawledRepository has children", crawledRepository.hasChildren());
		assertEquals(TOTAL_ARTIFACT_VERSIONS, artifactVersionCounter.getCount());

		artifactVersionCounter.clearCounter();
		crawledRepository.addGroupFilter("org.org-libs");
		crawledRepository.clearChildren();
		crawledRepository = mavenRepositoryService.refreshRepository(crawledRepository, crawledRepository.getLabel(), artifactVersionCounter);
		assertNotNull(crawledRepository);
		assertTrue("CrawledRepository has children", crawledRepository.hasChildren());
		assertEquals(new Integer(TOTAL_ARTIFACT_VERSIONS - 2), artifactVersionCounter.getCount());

		artifactVersionCounter.clearCounter();
		crawledRepository.addGroupFilter(".*");
		crawledRepository.clearChildren();
		crawledRepository = mavenRepositoryService.refreshRepository(crawledRepository, crawledRepository.getLabel(), artifactVersionCounter);
		assertNotNull(crawledRepository);
		assertTrue("CrawledRepository has children", crawledRepository.hasChildren());
		assertEquals(new Integer(TOTAL_ARTIFACT_VERSIONS), artifactVersionCounter.getCount());

		artifactVersionCounter.clearCounter();
		crawledRepository.clearGroupFilters();
		crawledRepository.addGroupFilter("org.org-libs.org-libs-usurper.*");
		crawledRepository.clearChildren();
		crawledRepository = mavenRepositoryService.refreshRepository(crawledRepository, crawledRepository.getLabel(), artifactVersionCounter);
		assertNotNull(crawledRepository);
		assertTrue("CrawledRepository has children", crawledRepository.hasChildren());
		assertEquals(new Integer(1), artifactVersionCounter.getCount());
	}
}