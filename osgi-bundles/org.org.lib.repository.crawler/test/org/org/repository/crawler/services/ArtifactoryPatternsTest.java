package org.org.repository.crawler.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.org.repository.crawler.maven2.model.protocolplugins.HttpRepositoryBrowserPlugin;

/**
 * @author zepag
 */
public class ArtifactoryPatternsTest extends TestCase {
	private Pattern directoryEntryPattern;

	private Pattern fileEntryPattern;

	private Pattern parentPattern;

	private Pattern entryPattern;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// directoryEntryPattern = Pattern.compile(".+<a href=\"[^#]+/\">(.+)</a>",Pattern.CASE_INSENSITIVE);
		// fileEntryPattern = Pattern.compile(".+<a href=\"[^#]+\">(.+)</a>", Pattern.CASE_INSENSITIVE);
		// parentPattern = Pattern.compile(".+<a href=\"[^#]+\">..</a>", Pattern.CASE_INSENSITIVE);
		// entryPattern = Pattern.compile(".+<a href=\"[^#]+\">(.+)</a>", Pattern.CASE_INSENSITIVE);
		directoryEntryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.ARTIFACTORY_PATTERNSET.getDirectoryEntryPattern(), Pattern.CASE_INSENSITIVE);
		fileEntryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.ARTIFACTORY_PATTERNSET.getFileEntryPattern(), Pattern.CASE_INSENSITIVE);
		parentPattern = Pattern.compile(HttpRepositoryBrowserPlugin.ARTIFACTORY_PATTERNSET.getParentDirectoryPattern(), Pattern.CASE_INSENSITIVE);
		entryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.ARTIFACTORY_PATTERNSET.getEntryPattern(), Pattern.CASE_INSENSITIVE);
	}

	public void testPatterns() {
		final String[] parentEntriesToTest = new String[] { "                <a href=\"http://www.telesdv.ep.parl.union.eu:80/dynamic-maven-repository/all/org/apache/commons/\">..</a>" };
		final String[] fileEntriesToTest = new String[] { "                <a href=\"http://www.telesdv.ep.parl.union.eu:80/dynamic-maven-repository/all/org/apache/commons/commons-chain/maven-metadata.xml\">maven-metadata.xml</a>" };
		final String[] directoryEntriesToTest = new String[] { "                <a href=\"http://www.telesdv.ep.parl.union.eu:80/dynamic-maven-repository/all/org/apache/commons/commons-chain/1.2/\">1.2</a>" };

		for (String fileEntry : fileEntriesToTest) {
			// CHECK IT IS AN ENTRY
			assertTrue(entryPattern.matcher(fileEntry).matches());
			Matcher matcher = fileEntryPattern.matcher(fileEntry);
			// CHECK IT IS MORE PRECISELY A FILE ENTRY
			assertTrue(matcher.matches());
			// CHECK IT IS NOT A DIRECTORY ENTRY
			assertFalse(directoryEntryPattern.matcher(fileEntry).matches());
			// CHECK IT IS NOT A PARENT ENTRY
			assertFalse(parentPattern.matcher(fileEntry).matches());
			// CHECK THE RETRIEVED VALUE
			assertEquals("maven-metadata.xml", matcher.group(HttpRepositoryBrowserPlugin.DIRECTORY_PATTERN_GROUP_INDEX));
		}
		for (String directoryEntry : directoryEntriesToTest) {
			// CHECK IT IS AN ENTRY
			assertTrue(entryPattern.matcher(directoryEntry).matches());
			Matcher matcher = directoryEntryPattern.matcher(directoryEntry);
			// CHECK IT IS MORE PRECISELY A DIRECTORY ENTRY
			assertTrue(matcher.matches());
			// CHECK IT IS NOT A PARENT ENTRY
			assertFalse(parentPattern.matcher(directoryEntry).matches());
			// CHECK THE RETRIEVED VALUE
			assertEquals("1.2", matcher.group(HttpRepositoryBrowserPlugin.DIRECTORY_PATTERN_GROUP_INDEX));
		}
		for (String parentEntry : parentEntriesToTest) {
			// CHECK IT IS AN ENTRY
			assertTrue(entryPattern.matcher(parentEntry).matches());
			// CHECK IT IS A PARENT ENTRY
			assertTrue(parentPattern.matcher(parentEntry).matches());
		}

	}
}