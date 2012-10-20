package org.github.based2.maven.plugin.checker;

// recheck with http://maven.40175.n5.nabble.com/how-exactly-does-maven-compare-versions-td45310.html
// http://mojo.codehaus.org/versions-maven-plugin/version-rules.html
// TODO unit test
public class CompareVersions {

    CompareVersions() {
    }

    public static boolean releaseIsInferiorOrEqual(String reference,
	    String value) {
	return releaseIsInferiorOrEqual(reference, value, false);
    }

    public static boolean releaseIsInferiorOrEqual(String reference,
	    String value, boolean strictEquality) {
	boolean isStrictLevelCheck = strictEquality;
	if (reference.endsWith(".x")) {
	    isStrictLevelCheck = true;
	    reference = reference.replaceAll(".x", "");
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
			return compareTextVersion(referenceSplit[i],
				valueSplit[i]);
		    }
		    return false;
		} catch (Exception e1) {
		    return false;
		}
	    }

	    int val = new Integer(valueSplit[i]).intValue();
	    int r = compare(ref, val, isStrictLevelCheck, valueLevel, i);
	    if (r == 0) {
		return false;
	    } else if (r == 1) {
		return true;
	    }
	}
	return false;
    }

    // int 0= false, 1 = True, 2= do nothing
    private static int compare(int ref, int val, boolean isStrictLevelCheck,
	    int valueLevel, int i) {
	if (isStrictLevelCheck) {
	    if (ref < val)
		return 0;
	}
	// TODO check for non integer conversion 1a.2b .... and number >
	if (!isStrictLevelCheck) {
	    if (ref > val)
		return 1;
	}
	if (valueLevel == i) {
	    if (ref == val)
		return 1;
	}
	return 2;
    }

    private static String[] R1 = { "M", "RC", "Final" };

    private static boolean contains(String[] a, String value) {
	for (int i = 0; i < a.length; i++) {
	    if (value.equals(a[i]))
		return true;
	}
	return false;
    }

    // TODO oufti - in dev process
    private static boolean compareTextVersion(String reference, String value) {
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
	    boolean ref_isM = false;
	    boolean ref_isRC = false;
	    boolean ref_isFinal = false;

	    boolean useNextModel = false;
	    if (R1[0].equals(refText)) // M
	    {
		ref_isM = true;
	    } else if (R1[1].equals(refText)) {// RC
		ref_isRC = true;
	    } else if (R1[2].equals(refText)) {// Final
		ref_isFinal = true;
	    } else {
		// Not Model1 - impossible, already checked
		useNextModel = true;
	    }

	    boolean val_isM = false;
	    boolean val_isRC = false;
	    boolean val_isFinal = false;
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
		} else if (ref_isRC && val_isM) {
		    return refSupVal();
		} else if ((ref_isM && val_isM) || (ref_isRC && val_isRC)
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

	// compare.
    }

    public static boolean equalz(int r, int v) {
	return true;
	// compare.
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	String a = "";
	if (CompareVersions.releaseIsInferiorOrEqual("1", "1"))
	    a += " 1"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("119456", "119456"))
	    a += " 119456"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6.x", "1.5.6"))
	    a += " 1.5.6.x"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6.x", "1.5.6.10"))
	    a += " 1.5.6.x v2"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6", "1.5.6"))
	    a += " 1.5.6"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6", "1.5.6.1"))
	    a += " >1.5.6.1"; // N
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6.1", "1.5.6"))
	    a += " 1.5.6"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6", "1.5.7"))
	    a += " >1.5.7"; // N
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6", "1.6"))
	    a += " >1.6"; // N
	if (CompareVersions.releaseIsInferiorOrEqual("1.6.x", "1.4.1"))
	    a += " !=1.4.1"; // N
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6", "1.5.6.5"))
	    a += " >1.5.6.5"; // N
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6", "1.4"))
	    a += " 1.4"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6", "1.4.4"))
	    a += " 1.4.4"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6", "1.4.4.4.4.4"))
	    a += " 1.4.4.4.4.4"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("1.5.6", "1.5.6.SEC01"))
	    a += " 1.5.6.SEC01"; // N
	if (CompareVersions.releaseIsInferiorOrEqual(
		"1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20",
		"1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19"))
	    a += " 1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual(
		"1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20",
		"1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20.21"))
	    a += " 1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20.21"; // N
	if (CompareVersions.releaseIsInferiorOrEqual("256.255.254",
		"10.555.1024"))
	    a += " 10.555.1024"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("3.6.10", "3.6.10.Final"))
	    a += " >3.6.10.Final"; // N
	if (CompareVersions.releaseIsInferiorOrEqual("3.6.10.Final", "3.6.10"))
	    a += " 3.6.10"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("3.6.10.Final",
		"3.6.10.RC1"))
	    a += " 3.6.10.RC1"; // Y
	if (CompareVersions
		.releaseIsInferiorOrEqual("3.6.10.RC2", "3.6.10.RC1"))
	    a += " 3.6.10.RC1"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("3.6.10.RC20",
		"3.6.10.RC11"))
	    a += " 3.6.10.RC11"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("3.6.10.M1000",
		"3.6.10.M98"))
	    a += " 3.6.10.M98"; // Y
	if (CompareVersions.releaseIsInferiorOrEqual("3.6.10.RC1000",
		"3.6.10.M200000"))
	    a += " 3.6.10.M200000"; // Y

	System.out.println(a);
	// TODO - Final - M1 -RC ; dates 1.2.3-20120323 v1
    }
}