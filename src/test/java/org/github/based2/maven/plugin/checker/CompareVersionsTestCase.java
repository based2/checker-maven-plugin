package org.github.based2.maven.plugin.checker;

import org.github.based2.maven.plugin.checker.version.VersionComparator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

// Semantic versioning
// versionImpacted / others , true if inferior or equal version
// http://softwarecave.org/2014/04/11/parameterized-unit-tests-in-junit/
@RunWith(Parameterized.class)
public class CompareVersionsTestCase {
    //private static Logger logger = LoggerFactory.getLogger(CompareVersionsTest.class);

    @Parameterized.Parameters(name = "{index}: test({0} ? =< {1}) expected={2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { "1", "1", true },
                { "256", "10", false },
                { "10", "256", true },
                { "119456", "119456", true },
                { "119456", "119457", false },
                { "119457", "119456", true },
                { "1.5.6.x", "1.5.6", true },
                { "1.5.6", "1.5.6.x", false },
                { "1.5.6.x", "1.5.6.10", true },
                { "1.5.6", "1.5.6.1", false },
                { "1.5.6.1", "1.5.6", true },
                { "1.5.6", "1.5.7", true },
                { "1.5.7", "1.5.6", false },
                { "1.5.6", "1.6", false },
                { "1.6", "1.5.6", true },
                { "1.6.x", "1.4.1", false },
                { "1.5.6", "1.5.6.5", false },
                { "1.5.6", "1.4", false },
                { "1.4", "1.5.6", true },
                { "1.5.6", "1.4.4", false },
                { "1.5.6", "1.4.4.4.4.4", false },
                { "1.5.6", "1.5.6.SEC01", false },
                { "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20",
                        "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19", true },
                { "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20",
                        "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20.21", false },
                { "256.255.254", "10.555.1024", false },
                { "3.6.10", "3.6.10.Final", false },
                { "3.6.10.Final", "3.6.10", true },
                { "3.6.10", "3.6.10.Final", false },
                { "3.6.10.RC2", "3.6.10.RC1", true },
                { "3.6.10.RC20", "3.6.10.RC11", true },
                { "3.6.10.M1000", "3.6.10.M98", true },
                { "3.6.10.RC1000", "3.6.10.M200000", false },
                { "3.6.10.SEC01", "3.6.10.Final", false },
                { "3.6.10.Final", "3.6.10.SEC01", true },
                { "3.6.10.SEC01", "3.6.10.SEC02", true },
                { "3.6.10-alpha-1", "3.6.10-alpha-2", true },
                { "3.6.10-alpha-12", "3.6.10-alpha-20", true },
                { "3.6.10-alpha-12", "3.6.10-beta-1", true },
                { "3.6.10-beta-1", "3.6.10-beta-2", true },
                { "3.6.10-beta-2", "3.6.10-beta-3", true },
                { "3.6.10-beta-3", "3.6.10-beta-11", true },
                { "3.6.10-beta-3", "3.6.10", true },
                { "0.0.1-SNAPSHOT", "0.0.1", true},
        });
    }

    private String v1;
    private String v2;
    private boolean expected;

    private static VersionComparator versionComparator = new VersionComparator();

    public CompareVersionsTestCase(String v1, String v2, boolean expected)
    {
        this.v1 = v1;
        this.v2 = v2;
        this.expected = expected;
    }

    @Test
    public void testReleaseIsInferiorOrEqual() {

        assertEquals(expected, versionComparator.releaseIsInferiorOrEqual(v1, v2));
    }

}
