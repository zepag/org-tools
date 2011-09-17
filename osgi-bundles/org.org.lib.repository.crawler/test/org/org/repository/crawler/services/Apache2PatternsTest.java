package org.org.repository.crawler.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.org.repository.crawler.maven2.model.protocolplugins.HttpRepositoryBrowserPlugin;

/**
 * @author zepag
 */
public class Apache2PatternsTest extends TestCase {
	private Pattern directoryEntryPattern;

	private Pattern fileEntryPattern;

	private Pattern parentPattern;

	private Pattern entryPattern;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		directoryEntryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.APACHE2_PATTERNSET.getDirectoryEntryPattern(), Pattern.CASE_INSENSITIVE);
		fileEntryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.APACHE2_PATTERNSET.getFileEntryPattern(), Pattern.CASE_INSENSITIVE);
		parentPattern = Pattern.compile(HttpRepositoryBrowserPlugin.APACHE2_PATTERNSET.getParentDirectoryPattern(), Pattern.CASE_INSENSITIVE);
		entryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.APACHE2_PATTERNSET.getEntryPattern(), Pattern.CASE_INSENSITIVE);
	}

	public void testPatterns() {
		final String[] parentEntriesToTest = new String[] { "<pre><img src=\"/icons/blank.gif\" alt=\"Icon \"> <a href=\"?C=N;O=D\">Name</a>                         <a href=\"?C=M;O=A\">Last modified</a>      <a href=\"?C=S;O=A\">Size</a>  <a href=\"?C=D;O=A\">Description</a><hr><img src=\"/icons/back.gif\" alt=\"[DIR]\"> <a href=\"/maven2/org/springframework/\">Parent Directory</a>                                  -   " };
		final String[] fileEntriesToTest = new String[] { "<img src=\"/icons/text.gif\" alt=\"[TXT]\"> <a href=\"maven-metadata.xml\">maven-metadata.xml</a>           01-Oct-2007 20:55  543   " };
		final String[] directoryEntriesToTest = new String[] { "<img src=\"/icons/folder.gif\" alt=\"[DIR]\"> <a href=\"1.0-m4/\">1.0-m4/</a>                      04-Jan-2007 13:23    -   " };

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
			assertEquals("1.0-m4", matcher.group(HttpRepositoryBrowserPlugin.DIRECTORY_PATTERN_GROUP_INDEX));
		}
		for (String parentEntry : parentEntriesToTest) {
			// CHECK IT IS AN ENTRY
			assertTrue(entryPattern.matcher(parentEntry).matches());
			// CHECK IT IS NOT A PARENT ENTRY
			assertTrue(parentPattern.matcher(parentEntry).matches());
		}

	}
}