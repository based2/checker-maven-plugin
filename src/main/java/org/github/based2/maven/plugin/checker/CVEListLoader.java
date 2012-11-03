package org.github.based2.maven.plugin.checker;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FastMap;
import org.github.based2.maven.plugin.checker.data.FixInfo;
import org.github.based2.maven.plugin.checker.data.Info;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Load JAVA_CVE_EOL.json file and create Map: impactArtifact, FixInfo
 * <pre>
 *      JAVA_CVE_EOL.json example:
 *
 *    [{"name":     "CVE-2010-1622",
 *    "level":     "MEDIUM",
 *    "description": "SpringSource Spring Framework 2.5.x before 2.5.6.SEC02, 2.5.7 before 2.5.7.SR01, and 3.0.x before 3.0.3 allows remote attackers to execute arbitrary code via an HTTP request containing class.classLoader.URLs[0]=jar: followed by a URL of a crafted .jar file.",
 *    "ifs": [{"impact":   "org.springframework:spring|spring-beans:2.5.x", "fix": "2.5.6.SEC03"},
 *    {"impact":   "org.springframework:spring|spring-beans:2.5.7", "fix":     "2.5.7.SR01"},
 *    {"impact":   "org.springframework:spring|spring-beans:3.0.x", "fix":     "3.0.6.RELEASE"}],
 *    "docs": ["http://www.springsource.com/security/cve-2010-1622", "http://forum.springsource.org/showthread.php?91522-CVE-2010-1622-Spring-Framework-execution-of-arbitrary-code"]
 *    },
 *    {"name":     "CVE-2010-1632",
 *    "level":     "HIGH",
 *    "description": "Apache Axis2 before 1.5.2, as used in IBM WebSphere Application Server (WAS) 7.0 through 7.0.0.12, IBM Feature Pack for Web Services 6.1.0.9 through 6.1.0.32, IBM Feature Pack for Web 2.0 1.0.1.0, Apache Synapse, Apache ODE, Apache Tuscany, Apache Geronimo, and other products, does not properly reject DTDs in SOAP messages, which allows remote attackers to read arbitrary files, send HTTP requests to intranet servers, or cause a denial of service (CPU and memory consumption) via a crafted DTD, as demonstrated by an entity declaration in a request to the Synapse SimpleStockQuoteService.",
 *    "ifs": [{"impact": "org.apache.axis2", "fix": "1.5.2" },
 *    {"impact": "org.apache.geronimo.*:?:2.x", "fix": "2.2.1" }],
 *    "docs": ["http://web.nvd.nist.gov/view/vuln/detail?vulnId=CVE-2010-1632", "https://bugzilla.redhat.com/show_bug.cgi?format=multiple&id=606706", "http://axis.apache.org/axis2/java/core/", "http://geronimo.apache.org/geronimo-21x-cve-2010-1632-patch-instructions.html", "http://geronimo.apache.org/2010/12/11/apache-geronimo-v221-released.html", "http://mail-archives.apache.org/mod_mbox/servicemix-users/201201.mbox/%3CCAJUL34NnCnQ4LSDN-9NWfia+2C0pSXaMajY51-=YgES46dsoiw@mail.gmail.com%3E"]
 *    }]
 *
 * </pre>
 */
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

    /**
     * Read java_cve.json file and store it in a Map: impactArtifact, FixInfo
     * @param javaCVEList
     * @return Map: impactArtifact, FixInfo
     * @throws FileNotFoundException
     */
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
          LOG.debug(latestName + "(latest record)");
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
      LOG.error("end info");
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
}
