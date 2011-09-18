package org.org.repository.crawler.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.org.repository.crawler.maven2.model.protocolplugins.HttpRepositoryBrowserPlugin;

/**
 * @author zepag
 */
public class Tomcat6PatternsTest extends TestCase {
	private Pattern directoryEntryPattern;

	private Pattern fileEntryPattern;

	private Pattern parentPattern;

	private Pattern entryPattern;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		directoryEntryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.TOMCAT6_PATTERNSET.getDirectoryEntryPattern(), Pattern.CASE_INSENSITIVE);
		fileEntryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.TOMCAT6_PATTERNSET.getFileEntryPattern(), Pattern.CASE_INSENSITIVE);
		parentPattern = Pattern.compile(HttpRepositoryBrowserPlugin.TOMCAT6_PATTERNSET.getParentDirectoryPattern(), Pattern.CASE_INSENSITIVE);
		entryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.TOMCAT6_PATTERNSET.getEntryPattern(), Pattern.CASE_INSENSITIVE);
	}

	public void testPatterns() {
		final String[] parentEntriesToTest = new String[] { "<body><h1>Directory Listing For /maven/repository/ - <a href=\"/static/maven/\"><b>Up To /maven</b></a></h1><HR size=\"1\" noshade=\"noshade\"><table width=\"100%\" cellspacing=\"0\" cellpadding=\"5\" align=\"center\">" };
		final String[] fileEntriesToTest = new String[] { "<a href=\"/static/maven/repository/antlr/antlr/maven-metadata-local.xml\"><tt>maven-metadata-local.xml</tt></a></td>" };
		final String[] directoryEntriesToTest = new String[] { "<a href=\"/static/maven/repository/antlr/\"><tt>antlr/</tt></a></td>" };

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
			assertEquals("maven-metadata-local.xml", matcher.group(HttpRepositoryBrowserPlugin.DIRECTORY_PATTERN_GROUP_INDEX));
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
			assertEquals("antlr", matcher.group(HttpRepositoryBrowserPlugin.DIRECTORY_PATTERN_GROUP_INDEX));
		}
		for (String parentEntry : parentEntriesToTest) {
			// CHECK IT IS AN ENTRY
			assertTrue(entryPattern.matcher(parentEntry).matches());
			// CHECK IT IS NOT A PARENT ENTRY
			assertTrue(parentPattern.matcher(parentEntry).matches());
		}

	}
}