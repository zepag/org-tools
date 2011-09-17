package org.org.repository.crawler.services;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.org.repository.crawler.mapping.Entry;
import org.org.repository.crawler.mapping.Entry.MavenType;
import org.org.repository.crawler.mapping.Entry.RawType;
import org.org.repository.crawler.maven2.RepositoryCrawlerHelper;
import org.org.repository.crawler.maven2.RepositoryCrawlerService.ArchivesSpecification;
import org.org.repository.crawler.maven2.RepositoryCrawlerService.LibrariesSpecification;
import org.org.repository.crawler.maven2.RepositoryCrawlerService.PomSpecification;

public class CrawledRepositoryBrowserHelperTest extends TestCase {
	private enum Expected {
		FAILURE, SUCCESS
	}

	public void testPopNameSegment() {
		doTestPopNameSegment("1.2.3", "1.2", Expected.SUCCESS);
		doTestPopNameSegment("1.2.3.", "1.2.3", Expected.SUCCESS);
		doTestPopNameSegment("1", "", Expected.SUCCESS);
		doTestPopNameSegment(null, "", Expected.FAILURE);
	}

	public void doTestPopNameSegment(final String name, final String expectedResult, Expected expected) {
		try {
			final String result = RepositoryCrawlerHelper.popNameSegment(name);
			if (expected == Expected.FAILURE) {
				fail("This should have failed");
			}
			assertEquals(expectedResult, result);
		} catch (Exception exception) {
			if (expected == Expected.SUCCESS) {
				exception.printStackTrace(System.err);
				fail("This should not have failed with Exception" + exception.getMessage());
			}
		}
	}

	public void testIsJavadoc() {
		doTestIsJavadoc(new Entry(null, "1-1-1-javadoc.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), true, Expected.SUCCESS);
		doTestIsJavadoc(new Entry(null, "1-javadoc.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), true, Expected.SUCCESS);
		doTestIsJavadoc(new Entry(null, "javadoc.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), false, Expected.SUCCESS);
		doTestIsJavadoc(new Entry(null, "1-1-1.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), false, Expected.SUCCESS);
		doTestIsJavadoc(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED), false, Expected.FAILURE);
		doTestIsJavadoc(null, false, Expected.FAILURE);
	}

	private void doTestIsJavadoc(final Entry entry, Boolean expectedResult, Expected expected) {
		try {
			final Boolean result = RepositoryCrawlerHelper.isJavadoc(entry);
			if (expected == Expected.FAILURE) {
				fail("This should have failed");
			}
			assertEquals(expectedResult, result);
		} catch (Exception exception) {
			if (expected == Expected.SUCCESS) {
				exception.printStackTrace(System.err);
				fail("This should not have failed with Exception" + exception.getMessage());
			}
		}
	}

	public void testIsSource() {
		doTestIsSource(new Entry(null, "1-1-1-sources.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), true, Expected.SUCCESS);
		doTestIsSource(new Entry(null, "1-sources.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), true, Expected.SUCCESS);
		doTestIsSource(new Entry(null, "sources.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), false, Expected.SUCCESS);
		doTestIsSource(new Entry(null, "1-1-1.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), false, Expected.SUCCESS);
		doTestIsSource(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED), false, Expected.FAILURE);
		doTestIsSource(null, false, Expected.FAILURE);
	}

	private void doTestIsSource(final Entry entry, Boolean expectedResult, Expected expected) {
		try {
			final Boolean result = RepositoryCrawlerHelper.isSource(entry);
			if (expected == Expected.FAILURE) {
				fail("This should have failed");
			}
			assertEquals(expectedResult, result);
		} catch (Exception exception) {
			if (expected == Expected.SUCCESS) {
				exception.printStackTrace(System.err);
				fail("This should not have failed with Exception" + exception.getMessage());
			}
		}
	}

	public void testIsPom() {
		PomSpecification pomSpecification = new PomSpecification();
		pomSpecification.addExtension("pom");
		doTestIsPom(new Entry(null, "1-1-1.pom", MavenType.UNDETERMINED, RawType.UNDETERMINED), pomSpecification, true, Expected.SUCCESS);
		doTestIsPom(new Entry(null, "1.1.pom", MavenType.UNDETERMINED, RawType.UNDETERMINED), pomSpecification, true, Expected.SUCCESS);
		doTestIsPom(new Entry(null, ".pom", MavenType.UNDETERMINED, RawType.UNDETERMINED), pomSpecification, true, Expected.SUCCESS);
		doTestIsPom(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED), pomSpecification, false, Expected.FAILURE);
		doTestIsPom(null, pomSpecification, false, Expected.FAILURE);
		doTestIsPom(new Entry(null, "1.pom", MavenType.UNDETERMINED, RawType.UNDETERMINED), null, false, Expected.FAILURE);
	}

	private void doTestIsPom(Entry entry, PomSpecification pomSpecification, Boolean expectedResult, Expected expected) {
		try {
			final Boolean result = RepositoryCrawlerHelper.isPom(entry, pomSpecification);
			if (expected == Expected.FAILURE) {
				fail("This should have failed");
			}
			assertEquals(expectedResult, result);
		} catch (Exception exception) {
			if (expected == Expected.SUCCESS) {
				exception.printStackTrace(System.err);
				fail("This should not have failed with Exception" + exception.getMessage());
			}
		}
	}

	public void testIsArchive() {
		ArchivesSpecification archivesSpecification = new ArchivesSpecification();
		archivesSpecification.addExtension("war");
		archivesSpecification.addExtension("ear");
		doTestIsArchive(new Entry(null, "1-1-1.war", MavenType.UNDETERMINED, RawType.UNDETERMINED), archivesSpecification, true, Expected.SUCCESS);
		doTestIsArchive(new Entry(null, "1-1-1.ear", MavenType.UNDETERMINED, RawType.UNDETERMINED), archivesSpecification, true, Expected.SUCCESS);
		doTestIsArchive(new Entry(null, "1-1-1.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), archivesSpecification, false, Expected.SUCCESS);
		doTestIsArchive(new Entry(null, "1-1-1", MavenType.UNDETERMINED, RawType.UNDETERMINED), archivesSpecification, false, Expected.SUCCESS);
		doTestIsArchive(new Entry(null, "1.1.war", MavenType.UNDETERMINED, RawType.UNDETERMINED), archivesSpecification, true, Expected.SUCCESS);
		doTestIsArchive(new Entry(null, "1.1.ear", MavenType.UNDETERMINED, RawType.UNDETERMINED), archivesSpecification, true, Expected.SUCCESS);
		doTestIsArchive(new Entry(null, ".war", MavenType.UNDETERMINED, RawType.UNDETERMINED), archivesSpecification, true, Expected.SUCCESS);
		doTestIsArchive(new Entry(null, ".ear", MavenType.UNDETERMINED, RawType.UNDETERMINED), archivesSpecification, true, Expected.SUCCESS);
		doTestIsArchive(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED), archivesSpecification, false, Expected.FAILURE);
		doTestIsArchive(null, archivesSpecification, false, Expected.FAILURE);
		doTestIsArchive(new Entry(null, "1.ear", MavenType.UNDETERMINED, RawType.UNDETERMINED), null, false, Expected.FAILURE);
	}

	private void doTestIsArchive(Entry entry, ArchivesSpecification archivesSpecification, Boolean expectedResult, Expected expected) {
		try {
			final Boolean result = RepositoryCrawlerHelper.isArchive(entry, archivesSpecification);
			if (expected == Expected.FAILURE) {
				fail("This should have failed");
			}
			assertEquals(expectedResult, result);
		} catch (Exception exception) {
			if (expected == Expected.SUCCESS) {
				exception.printStackTrace(System.err);
				fail("This should not have failed with Exception" + exception.getMessage());
			}
		}
	}

	public void testIsLibrary() {
		LibrariesSpecification librariesSpecification = new LibrariesSpecification();
		librariesSpecification.addExtension("jar");
		doTestIsLibrary(new Entry(null, "1-1-1.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), librariesSpecification, true, Expected.SUCCESS);
		doTestIsLibrary(new Entry(null, "1-1.war", MavenType.UNDETERMINED, RawType.UNDETERMINED), librariesSpecification, false, Expected.SUCCESS);
		doTestIsLibrary(new Entry(null, "1-1", MavenType.UNDETERMINED, RawType.UNDETERMINED), librariesSpecification, false, Expected.SUCCESS);
		doTestIsLibrary(new Entry(null, ".jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), librariesSpecification, true, Expected.SUCCESS);
		doTestIsLibrary(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED), librariesSpecification, false, Expected.FAILURE);
		doTestIsLibrary(null, librariesSpecification, false, Expected.FAILURE);
		doTestIsLibrary(new Entry(null, "1.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), null, false, Expected.FAILURE);

	}

	private void doTestIsLibrary(Entry entry, LibrariesSpecification librariesSpecification, Boolean expectedResult, Expected expected) {
		try {
			final Boolean result = RepositoryCrawlerHelper.isLibrary(entry, librariesSpecification);
			if (expected == Expected.FAILURE) {
				fail("This should have failed");
			}
			assertEquals(expectedResult, result);
		} catch (Exception exception) {
			if (expected == Expected.SUCCESS) {
				exception.printStackTrace(System.err);
				fail("This should not have failed with Exception" + exception.getMessage());
			}
		}
	}

	public void testGetVersionsFolders() {
		final Queue<Entry> versionFolderentries = new LinkedBlockingQueue<Entry>();
		versionFolderentries.add(new Entry(null, null, MavenType.ARTIFACT_VERSION_FOLDER, RawType.DIRECTORY));
		versionFolderentries.add(new Entry(null, null, MavenType.ARTIFACT_VERSION_FOLDER, RawType.DIRECTORY));
		final Queue<Entry> otherFolderEntries = new LinkedBlockingQueue<Entry>();
		otherFolderEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.DIRECTORY));
		otherFolderEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.DIRECTORY));
		otherFolderEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.DIRECTORY));
		final Queue<Entry> otherEntries = new LinkedBlockingQueue<Entry>();
		otherEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		otherEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		otherEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		otherEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		final Queue<Entry> entries = new LinkedBlockingQueue<Entry>();
		entries.addAll(versionFolderentries);
		entries.addAll(otherFolderEntries);
		entries.addAll(otherEntries);
		final Queue<Entry> result = RepositoryCrawlerHelper.getVersionsFolders(entries);
		assertEquals(versionFolderentries.size(), result.size());
		try {
			RepositoryCrawlerHelper.getVersionsFolders(null);
			fail("This should fail with IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			// trap expected exception
		}
	}

	public void testKeepGroupPathOnly() {
		final Queue<Entry> versionFolderentries = new LinkedBlockingQueue<Entry>();
		versionFolderentries.add(new Entry(null, null, MavenType.ARTIFACT_VERSION_FOLDER, RawType.DIRECTORY));
		versionFolderentries.add(new Entry(null, null, MavenType.ARTIFACT_VERSION_FOLDER, RawType.DIRECTORY));
		final Queue<Entry> otherFolderEntries = new LinkedBlockingQueue<Entry>();
		otherFolderEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.DIRECTORY));
		otherFolderEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.DIRECTORY));
		otherFolderEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.DIRECTORY));
		final Queue<Entry> otherEntries = new LinkedBlockingQueue<Entry>();
		otherEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		otherEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		otherEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		otherEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		final Queue<Entry> entries = new LinkedBlockingQueue<Entry>();
		entries.addAll(versionFolderentries);
		entries.addAll(otherFolderEntries);
		entries.addAll(otherEntries);
		final Queue<Entry> result = RepositoryCrawlerHelper.keepGroupPathOnly(entries);
		assertEquals(otherFolderEntries.size(), result.size());
		try {
			RepositoryCrawlerHelper.keepGroupPathOnly(null);
			fail("This should fail with IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			// trap expected exception
		}
	}

	public void testIsCurrentFolderAnArtifactFolder() {
		final Queue<Entry> folderEntries = new LinkedBlockingQueue<Entry>();
		folderEntries.add(new Entry(null, null, MavenType.ARTIFACT_VERSION_FOLDER, RawType.DIRECTORY));
		folderEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		folderEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		folderEntries.add(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED));
		final Boolean result = RepositoryCrawlerHelper.isCurrentFolderAnArtifactFolder(folderEntries);
		assertEquals(new Boolean(true), result);
		try {
			RepositoryCrawlerHelper.isCurrentFolderAnArtifactFolder(null);
			fail("This should fail with IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			// trap expected exception
		}
	}

	public void testGetLastGroupNameSegment() {
		doTestGetLastGroupNameSegment("1.2.3", "3", Expected.SUCCESS);
		doTestGetLastGroupNameSegment("1.2.3.", "3", Expected.SUCCESS);
		doTestGetLastGroupNameSegment("1", "1", Expected.SUCCESS);
		doTestGetLastGroupNameSegment(null, "", Expected.FAILURE);
	}

	private void doTestGetLastGroupNameSegment(final String name, final String expectedResult, Expected expected) {
		try {
			final String result = RepositoryCrawlerHelper.getLastGroupNameSegment(name);
			if (expected == Expected.FAILURE) {
				fail("This should have failed");
			}
			assertEquals(expectedResult, result);
		} catch (Exception exception) {
			if (expected == Expected.SUCCESS) {
				exception.printStackTrace(System.err);
				fail("This should not have failed with Exception" + exception.getMessage());
			}
		}
	}

	public void testIsRootFolder() {
		assertTrue(RepositoryCrawlerHelper.isRootFolder(""));
		assertFalse(RepositoryCrawlerHelper.isRootFolder("whatever"));
		assertFalse(RepositoryCrawlerHelper.isRootFolder("."));
		try {
			RepositoryCrawlerHelper.isRootFolder(null);
			fail("This should fail with IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			// trap expected exception
		}
	}

	public void testGetClassifier() {
		assertEquals("blabla", RepositoryCrawlerHelper.getClassifier(new Entry(null, "1-2-3-2.0.7-blabla.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), "2.0.7"));
		assertNull(RepositoryCrawlerHelper.getClassifier(new Entry(null, "1-2-3-2.0.7.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), "2.0.7"));
		assertEquals("2.0.7",RepositoryCrawlerHelper.getClassifier(new Entry(null, "1-2-3-2.0.7-2.0.7.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), "2.0.7"));
		assertEquals("kirkiki-b312345678655421", RepositoryCrawlerHelper.getClassifier(new Entry(null, "grubgrub-2.0.7-kirkiki-b312345678655421.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), "2.0.7"));
		try {
			RepositoryCrawlerHelper.getClassifier(null, "2.0.7");
			fail("This should fail with IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			// trap expected exception
		}
		try {
			RepositoryCrawlerHelper.getClassifier(new Entry(null, "1-2-3-2.0.7-blabla.jar", MavenType.UNDETERMINED, RawType.UNDETERMINED), null);
			fail("This should fail with IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			// trap expected exception
		}
		try {
			RepositoryCrawlerHelper.getClassifier(new Entry(null, null, MavenType.UNDETERMINED, RawType.UNDETERMINED), "2.0.7");
			fail("This should fail with IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			// trap expected exception
		}
	}
}
