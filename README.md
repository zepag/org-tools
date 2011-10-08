ORG Tools
========
Prerequisites
---------------------------
1. a git client
2. JDK 1.6+
3. Maven 3.0+
4. Ant 1.8+

Howto build
----------------------------
1. git clone repository locally (obviously ;) )
2. cd to builds-and-tests/org.org.eclipse.global-build/
3. launch:
<pre>
ant -f build-target-data.xml -propertyfile build-target-data.properties -Dbasedir=.
</pre>
This prepares the target platform. Not absolutely necessary for the build, though it should prove useful if you plan on testing features from Eclipse in any PDE version.
4. then launch:
<pre>
mvn clean install
</pre>
and wait for the internet to download ;);)

When build is done, builds-and-tests/org.org.eclipse.p2-repository/target should contain a useable p2 repository.
Point an Eclipse install to it to use its features.
