package org.github.based2.maven.plugin.checker;

import junit.framework.TestCase;

public class CompareVersionsTest extends TestCase {

    // versionImpacted / others , true if inferior or equal version

    Object[][] comparisons = {
            { "1", "1", true },
            { "256", "10", false },
            { "119456", "119456", true },
            { "119456", "119457", false },
            { "119456", "119455", true },
            { "1.5.6.x", "1.5.6", true },
            { "1.5.6.x", "1.5.6.10", true },
            { "1.5.6", "1.5.6.1", false },
            { "1.5.6.1", "1.5.6", true },
            { "1.5.6", "1.5.7", false },
            { "1.5.6", "1.6", false },
            { "1.6.x", "1.4.1", false },
            { "1.5.6", "1.5.6.5", false },
            { "1.5.6", "1.4", true },
            { "1.5.6", "1.4.4", true },
            { "1.5.6", "1.4.4.4.4.4", true },
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
            { "3.6.10.SEC01", "3.6.10.SEC02", true }
            };


    public void testReleaseIsInferiorOrEqual() {
        CompareVersions cv = new CompareVersions();
        for (Object[] comp : comparisons) {
            System.out.println(""+comp[0]+ " ? =< "+comp[1]);
            assertEquals(comp[2], cv.releaseIsInferiorOrEqual((String) comp[0],(String) comp[1]));
        }
    }

}
