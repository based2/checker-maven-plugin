package org.github.based2.maven.plugin.checker.version;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Semantic version with date and items
 * http://semver.org/
 * http://www.osgi.org/wiki/uploads/Links/SemanticVersioning.pdf
 * http://en.wikipedia.org/wiki/Software_versioning
 * http://apr.apache.org/versioning.html
 * https://github.com/netty/netty/issues/1269
 * http://www.javacodegeeks.com/2014/02/version-numbering-scheme-yet-another-approach.html?ModPagespeed=noscript
 * major> [ ‘.’ <minor> [ ‘.’ <micro> [ ‘.’ <qualifier
 *
 * add ref
 */
public class SemanticVersion
{
    private final static Logger LOG = LoggerFactory.getLogger(SemanticVersion.class);

    private final static int MAJOR_1 = 1;
    private final static int MINOR_2 = 2;
    private final static int MICRO_3 = 3; // PATCH
    private final static int QUALIFIER_4 = 4;

    protected int major = -1;
    protected String majorString = null;
    protected int minor = -1;
    protected String minorString = null;
    protected int micro = -1;
    protected String microString = null;

    protected int qualifier = -1;
    protected String qualifierString = null;

    protected int timestamp = -1;

    protected String separator;

    private final static String[] SEPARATORS = { ".", "-", "_", "#", ";", ",", "\t", "|", "*", "°",
            "¥" };
    private final static String[] FLAGS_ORDER = { "alpha", "beta", "m", "rc", "final", "ga",
            "sec" };

    private final static int FALSE = 0;
    private final static int TRUE = 1;
    private final static int EQUALS = 2;

    public SemanticVersion(String signature)
    {
        if (signature == null)
            return;
        for (String separator : SEPARATORS)
        {
            if ((separate(StringUtils.strip(signature), separator)))
                return;
        }
        LOG.error("Failed to load version:" + signature);
    }

    private boolean separate(String signature, String separator)
    {
        try
        {
            String[] version = signature.split(separator);
            if (version.length > 0)
            {
                inject(version[0], MAJOR_1);
                if (version.length > 1)
                {
                    inject(version[1], MINOR_2);
                    if (version.length > 2)
                    {
                        inject(version[2], MICRO_3);
                    }
                }
                return true;
            }
            else
            {
                inject(signature, MAJOR_1);
            }
        }
        catch (Exception e)
        {
        }
        return false;
    }

    private String subSeparator()
    {
        boolean isNext = false;
        for (String sep : SEPARATORS)
        {
            if (isNext)
                return separator;
            if (sep.equals(separator))
                isNext = true;
        }
        return separator;
    }

    private void inject(String source, final int LEVEL)
    {
        source = StringUtils.strip(source);
        if (StringUtils.isBlank(source))
            return;
        int v = toInt(source);
        if (v != -1)
        {
            setInt(v, LEVEL);
            // Extract String part
            int pos = intLength(v);
            if (pos != -1)
            {
                int pos2 = nextIntPos(source.substring(pos));
                if (pos2 != -1)
                {
                    setString(source.substring(pos, pos2), LEVEL);
                } else
                {
                    LOG.error("Int inside int");
                }
                v = toInt(source.substring(0, pos));
                if (v == -1)
                {

                    //setInt(v, LEVEL);
                }
                else
                {
                    String strTarget = source.substring(pos, source.length() - 1).toLowerCase();
                    if (!StringUtils.isAlpha(strTarget))
                    {
                        if (strTarget.startsWith(separator + "v"))
                        { // date detected v20141231
                            this.timestamp = toInt(strTarget.substring(2));
                        }
                        else if (strTarget.startsWith(subSeparator()))
                        { // 5.4-beta-XXXX, 0.0.1-SNAPSHOT,
                            //qualifierString // consecutiveAlpha
                            //qualifier
                        }
                        else if (strTarget.startsWith(separator))
                        { // 2.5.6.SEC03
                            //qualifierString // consecutiveAlpha
                            //qualifier
                        }
                    }
                    else
                    {
                        setString(source, LEVEL);
                    }
                }
            }
            else
            {
                setString(source, LEVEL);
            }
        }
        else
        {
            setString(source, LEVEL);
        }
    }



    private void setInt(int value, final int LEVEL)
    {
        switch(LEVEL)
        {
            case MAJOR_1: this.major = value; break;
            case MINOR_2: this.minor = value; break;
            case MICRO_3: this.micro = value; break;
            case QUALIFIER_4: this.qualifier = value; break;
            default: LOG.error("Bad level:" + LEVEL + " for value:" + value);
        }
    }

    private int toInt(String n)
    {
        int a = -1;
        try
        {
            return Integer.parseInt(n);
        }
        catch (Exception e)
        {
            return a;
        }
    }

    // http://stackoverflow.com/questions/11214245/java-find-index-of-first-regex
    /** @return index of pattern in s or -1, if not found */
    public static int indexOf(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s);
        return matcher.find() ? matcher.start() : -1;
    }

    call:

    int index = indexOf(Pattern.compile("(?<!a)bc"), "abc xbc");

    // http://stackoverflow.com/questions/8938498/get-the-index-of-a-pattern-in-a-string-using-regex
    public static void printMatches(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        // Check all occurrences
        while (matcher.find()) {
            System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end());
            System.out.println(" Found: " + matcher.group());
        }
    }

    // CharSequence cs = "string";
    private int nextIntPos(String str)
    {
        CharSequence cs = new CharSequence(str)
        {
            @Override
            public int length()
            {
                return 0;
            }

            @Override
            public char charAt(int i)
            {
                return 0;
            }

            @Override
            public CharSequence subSequence(int i, int i2)
            {
                return null;
            }

            @Override
            public String toString()
            {
                return null;
            }
        };

    }

    private int intLength(int i)
    {
        return new String(""+i).length();
    }

    // prepare substring
    private int maxPositionToExtractConsecutiveDigit(String n)
    {
        int a = -1;
        try
        {
            return Integer.parseInt(n);
        }
        catch (Exception e)
        {
            return a;
        }
    }

    /**
     * Exclude the current separator from the separators array
     *
     * @return
     */
   /* public String[] availableSeparators(){
        for (String separator : SEPARATORS) { int
            if
        }
    } */
    public boolean isTrue(int tag)
    {
        if (tag == TRUE)
            return true;
        return false;
    }

    public int isInferiorOrEqual(String str1, String str2, String[] flagOrder)
    {
        if (StringUtils.isBlank(str1))
        {
            if (StringUtils.isBlank(str2))
                return EQUALS;
            return FALSE;
        }
        else
        {
            if (StringUtils.isBlank(str2))
                return TRUE;
        }
        if (str1.equals(str2))
            return TRUE;
        int index1 = indexOf(str1, flagOrder);
        int index2 = indexOf(str2, flagOrder);

        if (index1 < index2)
            return TRUE;
        if (index1 == index2)
            return EQUALS; // todo for -1
        return FALSE;
    }

    public void setString(String value, final int LEVEL)
    {
        switch (LEVEL)
        {
            case MAJOR_1:
                this.majorString = value;
                break;
            case MINOR_2:
                this.minorString = value;
                break;
            case MICRO_3:
                this.microString = value;
                break;
            case QUALIFIER_4:
                this.qualifierString = value;
                break;
            default:
                LOG.error("Bad level:" + LEVEL + " for value:" + value);
        }
    }

    public boolean isInferiorOrEqual(SemanticVersion version)
    {
        return Comparison.isInferiorOrEqual(compare(version));
    }

    public int indexOf(String str, String[] flagOrder)
    {
        for (int i = 0; i < flagOrder.length; i++)
        {
            if (str.equals(flagOrder[i]))
                return i;
        }
        return -1;
    }

    public Comparison compare(String str1, String str2, String[] flagOrder)
    {
        if (StringUtils.isBlank(str1))
            return Comparison.NOT_COMPARABLE;
        if (StringUtils.isBlank(str2))
            return Comparison.NOT_COMPARABLE;
        if (str1.equals(str2))
            return Comparison.EQUALS;
        int index1 = indexOf(str1, flagOrder);
        int index2 = indexOf(str2, flagOrder);

        if (index1 == index2)
            return Comparison.EQUALS; // todo for -1
        if (index1 < index2)
            return Comparison.INFERIOR;
        return Comparison.SUPERIOR;
    }

    public Comparison compare(int v, String vs, int vv, String vvs)
    {
        if (v > vv)
            return Comparison.SUPERIOR;
        if (v < vv)
            return Comparison.INFERIOR;
        // v == vv
        Comparison comparison = compare(vs, vvs, FLAGS_ORDER);
        if (comparison == Comparison.NOT_COMPARABLE)
            return Comparison.EQUALS;
        return comparison;
    }

    public boolean isTimestamp(int timestamp)
    {
        if (timestamp == -1)
            return false;
        // todo better check
        return true;
    }

    public Comparison compareTimestamp(int timestamp)
    {
        if (!isTimestamp(this.timestamp))
            return Comparison.NOT_COMPARABLE;
        if (!isTimestamp(timestamp))
            return Comparison.NOT_COMPARABLE;
        if (this.timestamp > timestamp)
            return Comparison.SUPERIOR;
        if (this.timestamp < timestamp)
            return Comparison.INFERIOR;
        return Comparison.EQUALS;
    }

    /**
     * Check current version against the version in parameter
     *
     * @param version
     * @return
     */
    public Comparison compare(SemanticVersion version)
    {
        LOG.debug("timestamp:" + timestamp + "/" + version.timestamp);
        Comparison comparison = compareTimestamp(version.timestamp);
        if ((comparison != Comparison.NOT_COMPARABLE) && (Comparison.EQUALS != comparison))
            return comparison;

        LOG.debug("major:" + major + "/" + version.major + " " + majorString + "/"
                + version.majorString);
        comparison = compare(this.major, this.majorString, version.major, version.majorString);
        if (Comparison.EQUALS != comparison)
            return comparison;

        LOG.debug("minor:" + minor + "/" + version.minor + " " + minorString + "/"
                + version.minorString);
        comparison = compare(this.minor, this.minorString, version.minor, version.minorString);
        if (Comparison.EQUALS != comparison)
            return comparison;

        LOG.debug("micro:" + micro + "/" + version.micro + " " + microString + "/"
                + version.microString);
        comparison = compare(this.micro, this.microString, version.micro, version.microString);
        if (Comparison.EQUALS != comparison)
            return comparison;

        LOG.debug("q:" + qualifier + "/" + version.qualifier + " " + qualifierString + "/"
                + version.qualifierString);
        return compare(this.qualifier, this.qualifierString, version.qualifier,
                version.qualifierString);

    }

}
