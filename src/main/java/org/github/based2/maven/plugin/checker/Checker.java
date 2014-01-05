package org.github.based2.maven.plugin.checker;

// http://www.devdaily.com/java/edu/pj/pj010011

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.github.based2.maven.plugin.checker.data.FixInfo;
import org.github.based2.maven.plugin.checker.data.Info;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import org.apache.maven.artifact.Artifact;
//import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
//import org.apache.maven.artifact.resolver.ArtifactCollector;
//import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
//import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
//import org.apache.maven.artifact.versioning.ArtifactVersion;
//import org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource;
//import org.twdata.maven.mojoexecutor.MojoExecutor;

// https://github.com/TimMoore/mojo-executor/blob/master/mojo-executor/src/test/java/org/twdata/maven/mojoexecutor/MojoExecutorTest.java

/**
 * Analyze Maven project artifact dependencies of the project and look for CVE
 * (Common Vulnerabilities and Exposures)
 * <p/>
 * [groupId]:[artifactId]:[type]:[version]
 * <p/>
 * Major version upgrade indicates incompatible changes.
 * <p/>
 * You MUST recompile (and re-test) your application or service when upgrading
 * to a new major version Major version upgrades occur rarely
 * <p/>
 * Minor version upgrade indicates added functionality, but without removing
 * existing functionality
 * <p/>
 * It is possible that observed behavior changes, but this should usually not
 * cause compatibility problems Changes are binary compatible so it is possible
 * -- although not recommended -- to just upgrade jar. Instead, you should
 * recompile application, and re-test code that uses Jackson
 * <p/>
 * Patch version upgrades are strictly for bug fixes, and no new functionality
 * is added
 * <p/>
 * Only changes to observed behavior is to fix broken behavior Upgrade by
 * swapping jars should work without problems
 * <p/>
 * testing: http://maven.apache.org/plugin-testing/maven-plugin-testing-harness/
 * examples/complex-mojo-parameters.html
 * http://maven.apache.org/plugins/maven-dependency-plugin/xref/index.html
 * http://stackoverflow.com/questions/4275466/how-do-you-deal-with-maven-3-
 * timestamped-snapshots-efficiently
 * http://stackoverflow.com/questions/4243686/how
 * -to-programatically-invoke-a-maven-dependency-plugin/5761554#5761554 >
 * https://github.com/TimMoore/mojo-executor
 * http://search.maven.org/#browse|2068961873
 *
 * @phase test
 * @goal security
 */
public class Checker extends AbstractMojo
{

    /**
     * The Maven Project Object
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    /* TODO final transient > cstr */
    protected MavenProject project;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of plugins
     * and their dependencies.
     *
     * @parameter default-value="${project.remotePluginRepositories}"
     * @readonly
     */
    private List<RemoteRepository> remoteRepos;

    /**
     * JSON file of Vulnerabities/EOL reference to check
     * <p/>
     * checker-maven-plugin - CVE file list: default:
     * https://github.com/
     * based2/checker-maven-plugin/data/java_cve_list.json Not mandatory
     * <p/>
     * <pre>
     *
     * Usage
     *  <javaCVEList>c:\data\java_cve_list.json</javaCVEList>
     *
     * JSON Item format by example:
     *
     *  {"name":    "CVE-2011-2526",
     *   "level":   "MEDIUM",
     *   "description":"Apache Tomcat 5.5.x before 5.5.34, 6.x before 6.0.33, and 7.x before 7.0.19, when sendfile is enabled for the HTTP APR or HTTP NIO connector, does not validate certain request attributes, which allows local users to bypass intended file access restrictions or cause a denial of service (infinite loop or JVM crash) by leveraging an untrusted web application.",
     *   "ifs": [{"impact": "org.apache.tomcat:5.5", "fix": "5.5.34"},
     *           {"impact": "org.apache.tomcat:6", "fix": "6.0.33"},
     *       {"impact": "org.apache.tomcat:7", "fix": "7.0.19"}],
     *   "docs": ["http://web.nvd.nist.gov/view/vuln/detail?vulnId=CVE-2011-2526"]}
     *
     * </pre>
     *
     * @TODO Check OVAL - http://oval.mitre.org/about/faqs.html#a1 format
     * @parameter
     */
    private File javaCVEList;

    private final static String JAVA_CVE_URL = "https://github.com/based2/checker-maven-plugin/src/main/resources/java_cve.json";
    private final static String JAVA_CVE_FILE = "java_cve.json";
    // TODO put it on the maven repository M2_HOME

    // private final String osFilter = null; // android
    private Map<String, FixInfo> impacts;
    /* artifact position in dependency tree */
    private List<String> dependenciesPosition = new ArrayList<String>();

    private List<FixInfo> impacted = new ArrayList<FixInfo>();
    private List<String> artifacted = new ArrayList<String>();

    private static Log LOG = null;

    public void setProject(MavenProject project)
    {
        this.project = project;
    }

    public void setRepoSession(RepositorySystemSession repoSession)
    {
        this.repoSession = repoSession;
    }

    /**
     * http://www.maestrodev.com/better-builds-with-maven/developing-custom-
     * maven-plugins/advanced-mojo-development/
     *
     * @requiresDependencyResolution test
     */
    @Override
    public void execute() throws MojoExecutionException
    {
        LOG = getLog();
        javaCVEList = checkFileCVEList();
        // CVEListLoader.run(javaCVEList);
        loadMavenDependencyTree();
        analyze();
        print();
    }

    public File checkFileCVEList()
    {
        LOG.info("Loading:" + javaCVEList);
        if (javaCVEList == null) {
            // Download file
            try {
                getAndStoreURL(JAVA_CVE_URL,
                        this.getPluginContext().get("basedir") + "/"
                                + JAVA_CVE_FILE);
            } catch (IOException e) {
                LOG.error("", e);
            }
        }
        return javaCVEList;
    }

    private List<Dependency> getArtifactsDependencies(Artifact a)
    {
        List<Dependency> ret = new ArrayList<Dependency>();

        // Note: I get the POM artifact, not the WAR or whatever.
        DefaultArtifact pomArtifact = new DefaultArtifact(a.getGroupId(),
                a.getArtifactId(), "pom", a.getVersion());
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(pomArtifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(remoteRepos);

        try {
            DependencyNode node = repoSystem.collectDependencies(repoSession,
                    collectRequest).getRoot();
            DependencyRequest projectDependencyRequest = new DependencyRequest(
                    node, null);

            repoSystem.resolveDependencies(repoSession, projectDependencyRequest);

            PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
            node.accept(nlg);

            ret.addAll(nlg.getDependencies(true));
        } catch (Exception e) {
            LOG.error("", e);
        }

        return ret;
    }

    /**
     * // https://github.com/TimMoore/mojo-executor/blob/master/mojo-executor/src/main/java/org/twdata/maven/mojoexecutor/MojoExecutor.java
     * public void loadMavenDependencyTree() {
     * try {
     * MojoExecutor.executeMojo(MojoExecutor.plugin(
     * MojoExecutor.groupId("org.apache.maven.plugins"),
     * MojoExecutor.artifactId("maven-dependency-plugin"),
     * MojoExecutor.version("2.4")), MojoExecutor.goal("tree"), MojoExecutor
     * .configuration(MojoExecutor.element(
     * MojoExecutor.name("outputDirectory"),
     * "${project.build.directory}/target/dependency_tree.txt")),
     * MojoExecutor.executionEnvironment(project, session, pluginManager));
     * } catch (MojoExecutionException e) {
     * LOG.error("", e);
     * }
     * <p/>
     * Map<String, Integer> dependencies = new TreeMap<String, Integer>();
     * <p/>
     * // http://maven.apache.org/plugins/maven-dependency-plugin/xref/index.html
     * // DependencyNode rootNode
     * }
     */

    private Artifact getProjectArtifact()
    {
        // @TODO There must be a better way!
        return new DefaultArtifact(project.getArtifact().toString());
    }

	/*
	 * List of transitive deps of the artifact.
	 * 
	 * @param root The artifact to work with
	 * 
	 * @return The list of dependencies
	 * 
	 * @TODO #134 This "IF NOT NULL" validation is a workaround, since I don't
	 * know what the actual problem is. Looks like sometimes (for some unknown
	 * reason) #classpathFilter() returns NULL. When exactly this may happen I
	 * have no idea. That's why this workaround. Sometime later we should do a
	 * proper testing and reproduce this defect in a test. src:
	 * http://www.rexsl.
	 * com/rexsl-maven-plugin/cobertura/com.rexsl.maven.aether.DepsResolver.html
	 * >
	 * http://www.rexsl.com/rexsl-maven-plugin/cobertura/com.rexsl.maven.aether.
	 * RepositorySystemBuilder.html
	 */
	/*public List<Artifact> deps(final Artifact root) {
		final CollectRequest crq = new CollectRequest();
		crq.setRoot(new Dependency(root, JavaScopes.PROVIDED));
		// , JavaScopes.PROVIDED, JavaScopes.RUNTIME, JavaScopes.SYSTEM

		for (RemoteRepository repo : this.project
				.getRemoteProjectRepositories()) {
			crq.addRepository(repo);
		}

		final RepositorySystem system = new RepositorySystemBuilder().build();
		final DependencyFilter filter = DependencyFilterUtils.classpathFilter(
				JavaScopes.PROVIDED, JavaScopes.RUNTIME, JavaScopes.SYSTEM);

		MavenRepositorySystemSession session = new MavenRepositorySystemSession();
		LocalRepository localRepo = session.getLocalRepository();
		LOG.info("repo:" + localRepo.getBasedir().getAbsolutePath());
		// session.setLocalRepositoryManager();
		final List<Artifact> deps = new LinkedList<Artifact>();
		if (filter != null && system != null) {
			Collection<ArtifactResult> results;
			try {
				results = system.resolveDependencies(session,
						new DependencyRequest(crq, filter))
						.getArtifactResults();
			} catch (DependencyResolutionException ex) {
				throw new IllegalStateException(ex);
			}
			for (ArtifactResult res : results) {
				deps.add(res.getArtifact());
			}
		}
		return deps;
	}*/

    // http://stackoverflow.com/questions/1492000/how-to-get-access-to-mavens-dependency-hierarchy-within-a-plugin
    // http://jira.codehaus.org/browse/MSHARED-80
    // http://maven.apache.org/plugins/maven-dependency-plugin/xref/org/apache/maven/plugin/dependency/TreeMojo.html
    // http://www.opensourcejavaphp.net/java/karaf/org/apache/karaf/tooling/features/GenerateFeaturesXmlMojo.java.html
    // http://sonatype.github.com/sonatype-aether/apidocs/org/sonatype/aether/repository/class-use/LocalRepository.html
    // https://github.com/eclipse/aether-demo/tree/master/aether-demo-snippets/src/main/java/org/eclipse/aether/examples
    // http://dev.eclipse.org/mhonarc/lists/aether-users/msg00099.html
    // http://www.rexsl.com/rexsl-maven-plugin/cobertura/com.rexsl.maven.aether.DepsResolver.html
    // http://maven.apache.org/ref/3.0.4/maven-core/apidocs/src-html/org/apache/maven/project/DefaultProjectDependenciesResolver.html
    // http://stackoverflow.com/questions/9364270/get-dependencies-of-war-artifact-using-maven-2-api
    public void loadMavenDependencyTree()
    {
        Artifact projectArtifact = getProjectArtifact();
        List<Dependency> projectDependencies = getArtifactsDependencies(projectArtifact);

        for (Dependency d : projectDependencies) {
            if (d.getArtifact() != null) {
                LOG.info(d.getArtifact().toString());
            } else {
                LOG.info("EMPTY");
            }
        }
    }

	/*
	 * MavenRepositorySystemSession session = new
	 * MavenRepositorySystemSession(); LocalRepository localRepo =
	 * session.getLocalRepository();
	 * LOG.info("repo:"+localRepo.getBasedir().getAbsolutePath());
	 * 
	 * //org.apache.maven.project.MavenProject ->
	 * //org.sonatype.aether.util.DefaultRepositorySystemSession ->
	 * //org.sonatype.aether.graph.Dependency
	 * 
	 * 
	 * mavenProject Artifact artifact = new DefaultArtifact(
	 * "org.sonatype.aether:aether-impl:1.13" );
	 * 
	 * new LocalRepository( applicationConfiguration.getWorkingDirectory(
	 * "aether-local-repository" ) ); session.setLocalRepositoryManager(
	 * repositorySystem.newLocalRepositoryManager( localRepo ) );
	 * 
	 * // RemoteRepository repo = Booter.newCentralRepository();
	 * DependencyFilter classpathFilter =
	 * DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE,
	 * JavaScopes.PROVIDED, JavaScopes.RUNTIME, JavaScopes.SYSTEM);
	 * 
	 * CollectRequest collectRequest = new CollectRequest();
	 * collectRequest.setRoot( new Dependency( artifact, JavaScopes.COMPILE ) );
	 * collectRequest.addRepository( repo );
	 * 
	 * DependencyRequest dependencyRequest = new DependencyRequest(
	 * collectRequest, classpathFilter );
	 * 
	 * List<ArtifactResult> artifactResults = system.resolveDependencies(
	 * session, dependencyRequest ).getArtifactResults();
	 * 
	 * for ( ArtifactResult artifactResult : artifactResults ) { LOG.info(
	 * artifactResult.getArtifact() + " resolved to " +
	 * artifactResult.getArtifact().getFile() ); }
	 */
	/*
	 * try { ArtifactFilter artifactFilter = new ScopeArtifactFilter(
	 * Artifact.SCOPE_COMPILE); // buildDependencyTree(List<String>
	 * repositoryIds, String groupId, String artifactId, String version,
	 * org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor
	 * nodeVisitor)
	 * 
	 * DependencyNode rootNode =
	 * DefaultDependencyTreeBuilder.buildDependencyTree(project,
	 * localRepository, artifactFactory, artifactMetadataSource, artifactFilter,
	 * artifactCollector); //org.apache.maven.project
	 * 
	 * org.apache.maven.project.MavenProject project,
	 * org.apache.maven.artifact.repository.ArtifactRepository repository,
	 * org.apache.maven.artifact.factory.ArtifactFactory factory,
	 * 
	 * // org.apache.maven.repository.legacy.metadata
	 * org.apache.maven.artifact.metadata.ArtifactMetadataSource metadataSource,
	 * org.apache.maven.artifact.resolver.filter.ArtifactFilter filter,
	 * org.apache.maven.artifact.resolver.ArtifactCollector collector)
	 * 
	 * 
	 * 
	 * 
	 * final Collection<Artifact> kernelArtifacts; if (kernelVersion == null) {
	 * getLog().info("Step 1: Building list of provided bundle exports");
	 * kernelArtifacts = new HashSet<Artifact>(); DependencyNode tree =
	 * dependencyTreeBuilder.buildDependencyTree(project, localRepo, factory,
	 * artifactMetadataSource, new ArtifactFilter() {
	 * 
	 * public boolean include(Artifact artifact) { return true; }
	 * 
	 * }, new DefaultArtifactCollector()); tree.accept(new
	 * DependencyNodeVisitor() { public boolean endVisit(DependencyNode node) {
	 * // we want the next sibling too return true; } public boolean
	 * visit(DependencyNode node) { if (node.getState() !=
	 * DependencyNode.OMITTED_FOR_CONFLICT) { Artifact artifact =
	 * node.getArtifact(); if
	 * (Artifact.SCOPE_PROVIDED.equals(artifact.getScope()) &&
	 * !artifact.getType().equals("pom")) { kernelArtifacts.add(artifact); } }
	 * // we want the children too return true; } }); } else {
	 * getLog().info("Step 1 : Building list of kernel exports"); getLog().warn(
	 * "Use of 'kernelVersion' is deprecated -- use a dependency with scope 'provided' instead"
	 * ); Artifact kernel = factory.createArtifact("org.apache.karaf",
	 * "apache-karaf", kernelVersion, Artifact.SCOPE_PROVIDED, "pom");
	 * resolver.resolve(kernel, remoteRepos, localRepo); kernelArtifacts =
	 * getDependencies(kernel); } for (Artifact artifact : kernelArtifacts) {
	 * registerKernelBundle(artifact); }
	 * 
	 * 
	 * //////////////////////////////////////////////////////////////////////////
	 * ///////////////////////////////////
	 * 
	 * 
	 * CollectingDependencyNodeVisitor visitor = new
	 * CollectingDependencyNodeVisitor();
	 * 
	 * rootNode.accept(visitor);
	 * 
	 * List<DependencyNode> nodes = visitor.getNodes(); for (DependencyNode
	 * dependencyNode : nodes) { int state = dependencyNode.getState(); Artifact
	 * artifact = dependencyNode.getArtifact(); if (state ==
	 * DependencyNode.INCLUDED) { // ... } } } catch
	 * (DependencyTreeBuilderException e) { LOG.error(e); } }
	 * 
	 * //
	 * https://github.com/TimMoore/mojo-executor/blob/master/mojo-executor/src
	 * /main/java/org/twdata/maven/mojoexecutor/MojoExecutor.java public void
	 * loadMavenDependencyTree2() { Map<String, Artifact> artifactsById =
	 * project.getArtifactMap();
	 * 
	 * for (Artifact artifact : dependencies) { LOG.info(artifact.toString());
	 * for (ArtifactVersion av : artifact.getAvailableVersions()) {
	 * LOG.info((CharSequence) av); }
	 * 
	 * artifact.getGroupId(); rd.setArtifactId( artifact.getArtifactId() );
	 * rd.setResolvedVersion( artifact.getVersion() ); rd.setOptional(
	 * artifact.isOptional() ); rd.setScope( artifact.getScope() ); rd.setType(
	 * artifact.getType() ); if ( artifact.getClassifier() != null ) {
	 * rd.setClassifier( artifact.getClassifier() ); }
	 * //buildInfo.addResolvedDependency( rd );
	 * 
	 * dependenciesPosition.add(artifact.toString()); } }
	 */

    public void analyze()
    {
        getLog().info("Analyzing...");
        impacted = new ArrayList<FixInfo>();
        artifacted = new ArrayList<String>();
        String[] artifactSplit = null;
        String[] impactSplit = null;
        int count = 0;
        for (String artifact : dependenciesPosition) {
            count++;
            artifactSplit = artifact.split(":");
            if (artifactSplit != null && artifactSplit.length > 0) {
                for (String impact : impacts.keySet()) {
                    impactSplit = impact.split(":");
                    if (impactSplit != null && impactSplit.length > 0) {
                        // Check group name
                        if (impact.startsWith(artifactSplit[0])) {
                            String[] arts = artifact.split(":");
                            String[] imps = impact.split(":");
                            // Check artifact name
                            if (!"?".equals(imps[1])) {
                                if (!arts[1].equals(imps[1])) {
                                    continue;
                                }
                            }
                            // Check version
                            if (impactSplit[2].startsWith("[")) {
                                // Must match exactly +
                                impactSplit[2] = impactSplit[2].substring(1,
                                        impactSplit[2].length() - 2);
                                if (CompareVersions.releaseIsInferiorOrEqual(
                                        arts[2], impactSplit[2], true)
                                        && (!isFixSubVersion(impact, arts[2],
                                        artifact,
                                        artifactSplit.length - 1))) {
                                    // Found CVE impact
                                    artifacted.add(artifact);
                                    impacted.add(impacts.get(impact));
                                }
                            } else {
                                if ((CompareVersions.releaseIsInferiorOrEqual(
                                        arts[2], impactSplit[2], false))
                                        && (!isFixSubVersion(impact, arts[2],
                                        artifact,
                                        artifactSplit.length - 1))) {
                                    // Found CVE impact
                                    artifacted.add(artifact);
                                    impacted.add(impacts.get(impact));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isFixSubVersion(String impact, String artifact,
                                   String artifactVersion, int artifactVersionLevel)
    {
        FixInfo fixInfo = this.impacts.get(impact);
        String fix = fixInfo.getFix();
        String[] fixVersion = fix.split(".");
        if (artifactVersionLevel == fixVersion.length - 1) {
            if (artifact.startsWith(impact)) {
                if (fixVersion.equals(artifactVersion))
                    return true;
                return false;
            }
        }
        return false;
    }

    private void print()
    {
        if (impacted.size() == 0) {
            LOG.info("No CVE security impact found.");
        } else {
            LOG.info("CVE security impact found:");
            for (int i = 0; i < artifacted.size(); i++) {
                FixInfo info = impacted.get(i);
                Info inf = info.getInfo();
                LOG.info("");
                LOG.info("(" + (i + 1) + ")" + artifacted.get(i) + "=<"
                        + info.getFix());
                LOG.info("  " + inf.getName() + " " + inf.getLevel());
                LOG.info("  " + inf.getDescription());
                LOG.info("  fix:" + info.getFix());

                // Get tree dependency context
                // printdependency(artifacted.get(i));
                LOG.info("");
            }
        }
    }

    public File getAndStoreURL(String URL, String CVEFile) throws IOException
    {
        URL u;
        InputStream is = null;
        DataInputStream dis;
        String s;
        this.javaCVEList = new File(CVEFile);
        FileWriter fw = new FileWriter(javaCVEList);

        try {
            u = new URL(URL);
            is = u.openStream();
            BufferedReader dr = new BufferedReader(new InputStreamReader(is));
            while ((s = dr.readLine()) != null) {
                fw.write(s);
            }
        } catch (MalformedURLException mue) {
            getLog().error("", mue);
            System.exit(1);
        } catch (IOException ioe) {
            getLog().error("", ioe);
            System.exit(1);
        } finally {
            try {
                fw.close();
                is.close();
            } catch (IOException ioe) {
                getLog().error("", ioe);
                System.exit(1);
            }
        }
        return javaCVEList;
    }

    public static void main(String[] args)
    {
        Checker msc = new Checker();
        msc.loadMavenDependencyTree();
    }

}