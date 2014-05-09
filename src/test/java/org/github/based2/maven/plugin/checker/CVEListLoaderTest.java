package org.github.based2.maven.plugin.checker;

import junit.framework.TestCase;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.github.based2.maven.plugin.checker.data.FixInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Date: 02/11/12 15:10
 */
public class CVEListLoaderTest extends TestCase {
    //private static Logger logger = LoggerFactory.getLogger(CVEListLoaderTest.class);

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
            logger.info("Current dir : " + dir1.getCanonicalPath());
            logger.info("Parent  dir : " + dir2.getCanonicalPath());
        } catch (Exception e1) {
            logger.error("", e1);
            assertFalse(true);
        }

        try {
            File f = new File(dir1.getCanonicalPath() + "/src/main/resources/java_cve.json");
            if (!f.exists()) {
                // f.canRead()
                logger.error("File not found:" + f.getPath());
                assertFalse(true);
            } else  {
                Map<String, FixInfo> m = cll.run(f);
                for (String impact : m.keySet()) {
                    logger.debug(impact + " " + m.get(impact).getFix());
                }
            }

          //  assertEquals(f.exists(), true);
        } catch (FileNotFoundException e) {
            dir1 = new File(".");
            dir2 = new File("..");
            try {
                logger.info("Current dir : " + dir1.getCanonicalPath());
                logger.info("Parent  dir : " + dir2.getCanonicalPath());
            } catch (Exception e2) {
                logger.error("", e2);
                assertFalse(true);
            }
        }
    }
}
