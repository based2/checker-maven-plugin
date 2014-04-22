package org.github.based2.maven.plugin.checker.version;

import com.sun.tools.javac.resources.version;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Semantic version with date and items
 * 
 * http://semver.org/
 http://www.osgi.org/wiki/uploads/Links/SemanticVersioning.pdf
 http://en.wikipedia.org/wiki/Software_versioning
 major> [ ‘.’ <minor> [ ‘.’ <micro> [ ‘.’ <qualifier

 * add ref
 */
public class SemanticVersion
{
    private static Logger logger = LoggerFactory.getLogger(SemanticVersion.class);

    private int major = -1;
    private String majorString = null;
    private int minor = -1;
    private String minorString = null;
    private int micro = -1;
    private String microString = null;
    private int qualifier = -1;
    private String qualifierString = null;

    private int timestamp = -1;

    private String separator;

    private final static String[] SEPARATORS = {".","-","_","#","°","*"};
    private final static String[] FLAGS_ORDER ={"alpha","beta","m", "rc", "final", "ga", "sec"};

    private final static int FALSE = 0;
    private final static int TRUE = 1;
    private final static int EQUALS = 2;

    public void SemanticVersion(String signature) {
        if (signature==null) return;
        for (String separator : SEPARATORS) {
            if ((separate(StringUtils.strip(signature), separator))) return;
        }
        logger.error("Failed to load version:"+signature);
    }

    private boolean separate(String signature, String separator){
        try {
            String[] version = signature.split(separator);
            if (version.length>0) {
                inject(version[0], this.major, this.majorString);
                if (version.length>1) {
                    inject(version[1], this.minor, this.minorString);
                    if (version.length>2) {
                        inject(version[2], this.micro, this.microString);
                    }
                }
                return true;
            }
        } catch (Exception e){
        }
        return false;
    }

    private String subSeparator(){
        boolean isNext = false;
        for (String sep : SEPARATORS) {
            if (isNext) return separator;
            if (sep.equals(separator)) {
                 isNext=true;
            }
        }
        return separator;
    }

    private void inject(String source, int target, String strTarget) {
        source = StringUtils.strip(source);
        if (StringUtils.isBlank(source)) return;
        target = toInt(source);
        if (target==-1) {
            // Extract first numbers
            int pos = maxPositionToExtractConsecutiveDigit(source);
            if (pos!=-1) {
                target = toInt(source.substring(0, pos));
                if (target==-1) {
                    strTarget = source;
                } else {
                    strTarget = source.substring(pos, source.length()-1).toLowerCase();
                    if (!StringUtils.isAlpha(strTarget)) {
                        if (strTarget.startsWith(separator+"v")) { // date detected v20141231
                            this.timestamp=toInt(strTarget.substring(2));
                        } else if (strTarget.startsWith(subSeparator())) { // 5.4-beta-XXXX, 0.0.1-SNAPSHOT,
                            //qualifierString // consecutiveAlpha
                            //qualifier
                        } else if (strTarget.startsWith(separator)) { // 2.5.6.SEC03
                            //qualifierString // consecutiveAlpha
                            //qualifier
                        }
                    }
                }
            } else {
                strTarget = source;
            }
        }
    }

    private int toInt(String n) {
        int a = -1;
        try {
            return Integer.parseInt(n);
        } catch (Exception e){
            return a;
        }
    }

    // prepare substring
    // todo with regex
    private int maxPositionToExtractConsecutiveDigit(String n) {
        int a = -1;
        try {
            return Integer.parseInt(n);
        } catch (Exception e){
            return a;
        }
    }

    /**
     * Exclude the current separator from the separators array
     * @return
     */
   /* public String[] availableSeparators(){
        for (String separator : SEPARATORS) { int
            if
        }
    } */

    public int indexOf(String str, String[] flagOrder) {
        for(int i = 0; i < flagOrder.length ; i++){
           if (str.equals(flagOrder[i])) return i;
        }
        return -1;
    }

    public boolean isTrue(int tag){
        if (tag==TRUE) return true;
        return false;
    }

    public int isInferiorOrEqual(String str1, String str2, String[] flagOrder) {
        if (StringUtils.isBlank(str1)) {
            if (StringUtils.isBlank(str2)) return EQUALS;
            return FALSE;
        } else {
            if (StringUtils.isBlank(str2)) return TRUE;
        }
        if (str1.equals(str2)) return TRUE;
        int index1 = indexOf(str1, flagOrder);
        int index2 = indexOf(str2, flagOrder);

        if (index1 < index2) return TRUE;
        if (index1 == index2) return EQUALS; // todo for -1
        return FALSE;
    }

    public boolean isInferiorOrEqual(SemanticVersion version) {
        int qualifierLevel = EQUALS;
        if (this.qualifier<version.qualifier) qualifierLevel = TRUE;
        if (this.qualifier==version.qualifier) qualifierLevel = isInferiorOrEqual(this.qualifierString, version.qualifierString, FLAGS_ORDER);

        if ((this.timestamp<version.timestamp) && isTrue(qualifierLevel)) qualifierLevel = TRUE;

        if (this.major<version.major) return true;
        if (this.major==version.major) return isTrue(isInferiorOrEqual(this.majorString, version.majorString, FLAGS_ORDER));

        if (this.major<version.major) return true;
        if (this.major==version.major) return isTrue(isInferiorOrEqual(this.majorString, version.majorString, FLAGS_ORDER));

        if (this.major<version.major) return true;
        if (this.major==version.major) return isTrue(isInferiorOrEqual(this.majorString, version.majorString, FLAGS_ORDER));
    }

}
