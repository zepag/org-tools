package org.org.maven.plugin.dws;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.testing.stubs.StubArtifactRepository;
import org.apache.maven.project.MavenProject;

/**
 * Tests <code>TreeMojo</code>.
 * 
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: TestDWSDescriptorMojo.java 728546 2008-12-21 22:56:51Z bentmann $
 * @since 2.0
 */
public class TestDWSDescriptorMojo extends AbstractDependencyMojoTestCase {
	// TestCase methods -------------------------------------------------------

	/*
	 * @see org.apache.maven.plugin.testing.AbstractMojoTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// required for mojo lookups to work
		super.setUp("descriptor", false);
	}

	// tests ------------------------------------------------------------------

	/**
	 * Tests the proper discovery and configuration of the mojo.
	 * 
	 * @throws Exception
	 */
	public void testTreeTestEnvironment() throws Exception {
		File testPom = new File(getBasedir(), "target/test-classes/unit/dwsdescriptor-test/plugin-config.xml");
		DWSDescriptorMojo mojo = (DWSDescriptorMojo) lookupMojo("descriptor", testPom);
		String testDirAbsolutePath = testDir.getAbsolutePath();
		setVariableValueToObject(mojo, "localRepository", new StubArtifactRepository(testDirAbsolutePath));
		setVariableValueToObject(mojo, "scope", "compile");

		assertNotNull(mojo);
		assertNotNull(mojo.getProject());
		MavenProject project = mojo.getProject();
		project.setArtifact(this.stubFactory.createArtifact("testGroupId", "project", "1.0"));
		project.setArtifact(this.stubFactory.createArtifact("g", "test", "1.0"));

		Set artifacts = this.stubFactory.getScopedArtifacts();
		Set directArtifacts = this.stubFactory.getReleaseAndSnapshotArtifacts();
		artifacts.addAll(directArtifacts);

		project.setArtifacts(artifacts);
		project.setDependencyArtifacts(directArtifacts);

		mojo.execute();

	}
}
