package org.github.based2.maven.plugin.checker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.FastMap;
import org.github.based2.maven.plugin.securityChecker.data.FixInfo;
import org.github.based2.maven.plugin.securityChecker.data.Info;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class CVEListLoader {

  private static Log LOG = null;

  private final String latestName = null;

  private final JsonParser jp = null;

  public CVEListLoader(Log log) {
    LOG = log;
    if (log == null) {
      LOG = new MockLog();
    }
  }

  // TODO LOW better latestName auto management: one this.setContexValue(
  public Map<String, FixInfo> run(File javaCVEList)
      throws FileNotFoundException {

    @SuppressWarnings("unchecked")
    Map<String, FixInfo> impacts = new FastMap();
    String name = null;
    String latestName = null;
    JsonReader jr = null;
    try {
      jr = new JsonReader(javaCVEList);
      JsonToken token = jr.next();
      token = jr.next();
      while (token != JsonToken.END_ARRAY) {
        token = jr.next();
        while (token != JsonToken.END_OBJECT) {
          Info info = new Info();
          jr.setContextObject(info);
          jr.setContexValue(latestName + "(latest record)");
          LOG.error(latestName + "(latest record)");
          jr.set(Info.NAME);
          LOG.debug(Info.NAME + ":" + info.getName());
          jr.setContexValue(info.getName());

          jr.set(Info.LEVEL);
          LOG.debug(Info.LEVEL + ":" + info.getLevel());
          jr.set(Info.DESCRIPTION);
          LOG.debug(Info.DESCRIPTION + ":" + info.getDescription());

          // TOCHECK os must be in FixInfo
          jr.setIfExists(Info.OS);
          // LOG.debug(Info.OS + ":" + info.getOs());

          List<FixInfo> fixs = jr.setRecordsArray(FixInfo.class, "ifs",
              new String[] { FixInfo.IMPACT, FixInfo.FIX });

          jr.setArray(Info.DOCS);
          LOG.debug(Info.DOCS + ":" + this.print(info.getDocs()));
          LOG.error("dd");
          for (FixInfo fi : fixs) {
            LOG.error("fi");
            fi.setInfo(info);
            LOG.error("info set");
            if (fi.getImpact() == null)
              break;
            impacts.put(fi.getImpact(), fi);
            LOG.error("impact");
          }
          latestName = info.getName();
          LOG.debug("name");
          jr.next();
          jr.next();
        }
        LOG.error("end docs");
      }
      LOG.error("end nfo");
    } catch (JsonParseException e) {
      LOG.error("", e);
    } catch (IOException e) {
      LOG.error("", e);
    } catch (SecurityException e) {
      LOG.error("", e);
    } catch (IllegalArgumentException e) {
      LOG.error("", e);
    } catch (NoSuchFieldException e) {
      LOG.error("", e);
    } catch (IllegalAccessException e) {
      LOG.error("", e);
    } finally {
      jr.close();
    }
    return impacts;
  }

  public String print(String[] array) {
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    for (String s : array) {
      sb.append(s);
      sb.append(", ");
    }
    sb = sb.replace(sb.length() - 2, sb.length(), "");
    sb.append("]");
    return sb.toString();
  }

  public static void main(String[] args) {

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
      // Map<String, FixInfo> m = cll.run(new
      // File("src/main/resources/data/java_cve.json"));
      // Map<String, FixInfo> m = cll
      // .run(new File(
      // "W:\\git\\checker-maven-plugin\\src\\main\\resources\\java_cve.json"));

      Map<String, FixInfo> m = cll
          .run(new File(
              "/Users/based/Documents/workspace/checker-maven-plugin/src/main/java/org/github/based2/maven/plugin/checker/java_cve.json"));

      for (String impact : m.keySet()) {
        LOG.debug(impact + " " + m.get(impact).getFix());
      }
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
