package org.github.based2.maven.plugin.checker;

import junit.framework.TestCase;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.github.based2.maven.plugin.checker.data.FixInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Date: 02/11/12 15:10
 */
public class CVEListLoaderTest extends TestCase {
    private static Logger LOG = LoggerFactory.getLogger(CVEListLoaderTest.class);

    public void testCVEFilePresence() throws Exception {
        String[] cveFile = { "..", "..", "..", "..", "..", "..", "..", "..", "..",
                "data", "java_cve.json" };

        ConsoleLogger logger = new ConsoleLogger();
        CVEListLoader cll = new CVEListLoader(null);
        // Map<String, FixInfo> m = cll.run(new File("." +
        // FileUtils.makePath(cveFile)));
        File dir1 = new File(".");
        File dir2 = new File("..");
        try {
            LOG.info("Current dir : " + dir1.getCanonicalPath());
            LOG.info("Parent  dir : " + dir2.getCanonicalPath());
        } catch (Exception e1) {
            LOG.error("", e1);
        }

        try {
            // Map<String, FixInfo> m = cll.run(new File("src/main/resources/data/java_cve.json"));
            // Map<String, FixInfo> m = cll.run(new File(
            // TODO use classloader to load the file
           File f = new File("../../../../../../../resources/java_cve.json");
            if (!f.exists()) {
                // f.canRead()
                LOG.error("File not found:" + f.getPath());
            } else  {
                Map<String, FixInfo> m = cll.run(f);
                for (String impact : m.keySet()) {
                    LOG.debug(impact + " " + m.get(impact).getFix());
                }
            }

          //  assertEquals(f.exists(), true);
        } catch (FileNotFoundException e) {
            dir1 = new File(".");
            dir2 = new File("..");
            try {
                LOG.info("Current dir : " + dir1.getCanonicalPath());
                LOG.info("Parent  dir : " + dir2.getCanonicalPath());
            } catch (Exception e2) {
                LOG.error("", e2);
            }
        }
    }
}
