package org.org.repository.crawler.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.org.repository.crawler.maven2.model.protocolplugins.HttpRepositoryBrowserPlugin;

/**
 * @author zepag
 */
public class S3BrowsePatternsTest extends TestCase {
	private Pattern directoryEntryPattern;

	private Pattern fileEntryPattern;

	private Pattern parentPattern;

	private Pattern entryPattern;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		directoryEntryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.S3BROWSE_PATTERNSET.getDirectoryEntryPattern(), Pattern.CASE_INSENSITIVE);
		fileEntryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.S3BROWSE_PATTERNSET.getFileEntryPattern(), Pattern.CASE_INSENSITIVE);
		parentPattern = Pattern.compile(HttpRepositoryBrowserPlugin.S3BROWSE_PATTERNSET.getParentDirectoryPattern(), Pattern.CASE_INSENSITIVE);
		entryPattern = Pattern.compile(HttpRepositoryBrowserPlugin.S3BROWSE_PATTERNSET.getEntryPattern(), Pattern.CASE_INSENSITIVE);
	}

	public void testPatterns() {
		final String[] parentEntriesToTest = new String[] { "<p><a href=\"/explore/repository.springsource.com\">ROOT</a>/<a href=\"/explore/repository.springsource.com/maven\">maven</a>/<a href=\"/explore/repository.springsource.com/maven/bundles\">bundles</a>/external&nbsp;</p>" };
		final String[] fileEntriesToTest = new String[] { "	<td><a href=\"/getObject/repository.springsource.com/maven/bundles/external/edu/oswego/cs/concurrent/com.springsource.edu.oswego.cs.dl.util.concurrent/1.3.4/com.springsource.edu.oswego.cs.dl.util.concurrent-1.3.4-license.txt\">com.springsource.edu.oswego.cs.dl.util.concurrent-1.3.4-license.txt</a></td>" };
		final String[] directoryEntriesToTest = new String[] { "	<td><a href=\"/explore/repository.springsource.com/maven/bundles/external/edu/oswego/cs/concurrent/com.springsource.edu.oswego.cs.dl.util.concurrent/1.3.4/\">1.3.4</a></td>" };
		final String invalidEntryToTest = "            <li><a href=\"/\">Home</a></li>";
		assertFalse(entryPattern.matcher(invalidEntryToTest).matches());
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
			assertEquals("com.springsource.edu.oswego.cs.dl.util.concurrent-1.3.4-license.txt", matcher.group(HttpRepositoryBrowserPlugin.DIRECTORY_PATTERN_GROUP_INDEX));
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
			assertEquals("1.3.4", matcher.group(HttpRepositoryBrowserPlugin.DIRECTORY_PATTERN_GROUP_INDEX));
		}
		for (String parentEntry : parentEntriesToTest) {
			// CHECK IT IS AN ENTRY
			assertTrue(entryPattern.matcher(parentEntry).matches());
			// CHECK IT IS A PARENT ENTRY
			assertTrue(parentPattern.matcher(parentEntry).matches());
		}

	}
}