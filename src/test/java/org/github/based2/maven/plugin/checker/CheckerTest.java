package org.github.based2.maven.plugin.checker;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.LocalRepository;

import java.io.File;

// http://trac.fazend.com/rexsl/browser/rexsl/branches/ticket3/rexsl/rexsl-maven-plugin/src/test/java/com/rexsl/maven/CheckMojoTest.java?rev=34

public final class CheckerTest extends AbstractMojoTestCase {
	    @Test
	    public void testMojoGoal() throws Exception {
	        final Checker mojo = this.mojo();
	        mojo.execute();
	    }

	    /**
	     * Create MOJO for tests.
	     * @return The MOJO just created
	     * @throws Exception If something goes wrong inside
	     */
	    private Checker mojo() throws Exception {
	    	File pom = getTestFile( "src/test/resources/pom.xml" );
	        assertNotNull( pom );
	        assertTrue(pom.exists());
	        
	        final Checker mojo = new Checker();
	        final MavenProject project = Mockito.mock(MavenProject.class);
	        Mockito.doReturn(new File(".")).when(project).getBasedir();
	        //Mockito.doReturn("war").when(project).getPackaging();
	        mojo.setProject(project);
	        //mojo.setWebappDirectory(".");
	        final RepositorySystemSession repoSession =
	            Mockito.mock(RepositorySystemSession.class);
	        final LocalRepository repo = new LocalRepository(new File("."));
	        Mockito.doReturn(repo).when(repoSession).getLocalRepository();
	        mojo.setRepoSession(repoSession);
	        return mojo;
	    }
}
