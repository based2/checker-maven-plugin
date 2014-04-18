package org.github.based2.maven.plugin.checker.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// recheck with http://maven.40175.n5.nabble.com/how-exactly-does-maven-compare-versions-td45310.html
// http://mojo.codehaus.org/versions-maven-plugin/version-rules.html

public class CompareVersions {

    private static Logger logger = LoggerFactory.getLogger(CompareVersions.class);

    private int majorReference;
    private int minorReference;
    private int minusReference;
    private String dateReference;

    private int majorCandidate;
    private int minorCandidate;
    private int minusCandidate;
    private String dateCandidate;

    public CompareVersions() {
    }

    public static boolean releaseIsInferiorOrEqual(String reference, String value) {
        return releaseIsInferiorOrEqual(reference, value, false);
    }

    public static boolean releaseIsInferiorOrEqual(String reference, String value, boolean strictEquality)
    {
        boolean isStrictLevelCheck = strictEquality;
        if (reference.endsWith(".x")) {
            isStrictLevelCheck = true;
            // Remove .x at the end
            reference = reference.substring(0, reference.length()-2);
        }
        String[] referenceSplit = reference.split("\\.");
        int refLevel = referenceSplit.length - 1; // level1.level2.level3 ...

        String[] valueSplit = value.split("\\.");
        int valueLevel = valueSplit.length - 1;
        if (isStrictLevelCheck) {
            valueLevel--;
        }
        if ((valueSplit == null) || (valueSplit.length == 0))
            return true; // TOCHECKNo version ?!

        for (int i = 0; i < valueSplit.length; i++) {
            int ref = 0;
            try {
                ref = new Integer(referenceSplit[i]).intValue();
            } catch (Exception e) {
                try {
                    if ((referenceSplit[i] != null) && (valueSplit[i] != null)) {
                        return compareTextVersion(referenceSplit[i], valueSplit[i]);
                    }
                    return false;
                } catch (Exception e1) {
                    return false;
                }
            }

            int val = new Integer(valueSplit[i]).intValue();
            int r = inferiorOrEqual(ref, val, isStrictLevelCheck, valueLevel, i);
            if (r == FALSE) {
                return false;
            } else if (r == TRUE) {
                return true;
            }
        }
        return false;
    }

    private static int FALSE = 0;
    private static int TRUE = 1;
    private static int DO_NOTHING = 2;
    /**
     *
     * @param ref
     * @param val
     * @param isStrictLevelCheck
     * @param valueLevel
     * @param i
     * @return FALSE | TRUE | DO_NOTHING
     */
    private static int inferiorOrEqual(int ref, int val, boolean isStrictLevelCheck, int valueLevel, int i)
    {
        logger.debug("ref:"+ref + " val:"+ val + " valueLevel:"+ valueLevel + " i:" + i);
        if (isStrictLevelCheck) {
            if (ref < val) return TRUE;
        }
        // TODO check for non integer conversion 1a.2b .... and number >
        if (!isStrictLevelCheck) {
            if (ref > val) return FALSE;
        }
        if (valueLevel == i) {
            if (ref == val) return TRUE;
        }
        logger.error("Failed to compare: ref:"+ref + " val:"+ val + " valueLevel:"+ valueLevel + " i:" + i );
        return DO_NOTHING;
    }

    private static String[] R1 = { "alpha", "beta", "M", "RC", "Final", "SEC" };

    private static boolean contains(String[] a, String value) {
        for (int i = 0; i < a.length; i++) {
            if (value.equals(a[i]))
            return true;
        }
        return false;
    }

    // TODO in dev process
    private static boolean compareTextVersion(String reference, String value)
    {
        // (1) Model 1: M<numeric> > RC<numeric> > Final (ex: hibernate, jboss)
        // (1) Model 1: ALPHA|alpha<numeric > BETA|beta<numeric> > CR<numeric> >
        // GA (ex: jboss seam, jboss aop) > Final RELEASE
        // SP<numeric> SEC
        // find numeric part
        String refText = "";
        int refInt = 0;
        String valText = "";
        int valInt = 0;

        if ((contains(R1, refText)) && (contains(R1, valText))) {
            boolean ref_isAlpha = false;
            boolean ref_isBeta = false;
            boolean ref_isMilestone = false;
            boolean ref_isReleaseCandidate = false;
            boolean ref_isFinal = false;
            boolean ref_isSecured = false;

            boolean useNextModel = false;
            if (R1[0].equals(refText))
            {
                ref_isAlpha = true;
            } else if (R1[1].equals(refText)) {
                ref_isBeta = true;
            } else if (R1[2].equals(refText)) {
                ref_isMilestone = true;
            } else if (R1[3].equals(refText)) {
                ref_isReleaseCandidate = true;
            } else if (R1[4].equals(refText)) {
                ref_isFinal = true;
            } else if (R1[5].equals(refText)) {
                ref_isSecured = true;
            } else {
                // Not Model1 - impossible, already checked
                useNextModel = true;
            }

            boolean val_isM = false;
            boolean val_isRC = false;
            boolean val_isFinal = false;
            boolean val_isSEC = false;
            if (R1[0].equals(valText)) // M
            {
                val_isM = true;
            } else if (R1[1].equals(valText)) {// RC
                val_isRC = true;
            } else if (R1[2].equals(valText)) {// Final
                val_isFinal = true;
            } else {
            // Not Model1 - impossible, already checked
            useNextModel = true;
            }

            if (!useNextModel) {
            if (ref_isFinal && (val_isM || val_isRC)) {
                return refSupVal();
            } else if (ref_isReleaseCandidate && val_isM) {
                return refSupVal();
            } else if ((ref_isMilestone && val_isM) || (ref_isReleaseCandidate && val_isRC)
                || (ref_isFinal && val_isFinal)) {
                return equalz(refInt, valInt);
            } else {
                // impossible
            }
            }
        }
        // (2) Rule 2: version-date
        return false;
    }

    public static boolean refSupVal() {
	    return true;
	    // TODO compare.
    }

    public static boolean equalz(int r, int v) {
	    return true;
	    // TODO compare.
    }

    public static void main(String[] args) {
        System.out.println("12.x".substring(0, "12.x".length()-2));
    }
}