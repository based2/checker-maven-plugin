package org.github.based2.maven.plugin.checker;

import com.fasterxml.jackson.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Json reader tool using JSON token and supporting array auto-loading.
 * <p/>
 * TODO tests cases
 * TODO Should be in com.fasterxml.jackson.core ;-)
 */
public class JsonReader
{
    private static Logger LOG = LoggerFactory.getLogger(JsonReader.class);

    private JsonParser jp;
    private JsonToken token;
    private String _contextMessage = "context name:";
    private boolean isDebug = true;
    private boolean isExitOnError = true;

    private String _contextValue = "";
    private Object _contextObject;

    public JsonReader(File file)
    {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        try {
            jp = f.createJsonParser(file);
        } catch (JsonParseException e) {
            LOG.error("", e);
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    public JsonReader(File file, boolean isDebug, boolean isExitOnError)
    {
        this(file);
        isDebug = isDebug;
        isExitOnError = isExitOnError;
    }

    public JsonToken next() throws JsonParseException, IOException
    {
        if (isDebug) {
            token = nextTokenDebug();
            return token;
        } else {
            return jp.nextToken();
        }
    }

    private void setContextMessage(String contextMessage)
    {
        _contextMessage = contextMessage;
    }

    void setContexValue(String contextValue)
    {
        _contextValue = contextValue;
    }

    private void setValue(Object objectLoaded, String attribute, Object value)
    {
        try {
            Field field = objectLoaded.getClass().getField(attribute);
            try {
                field.set(objectLoaded, value);
            } catch (IllegalArgumentException e) {
                LOG.error("", e);
                System.exit(0);
            } catch (IllegalAccessException e) {
                LOG.error("", e);
                System.exit(0);
            }
            // LOG.debug(attribute + ":" + getter);
        } catch (NoSuchFieldException e) {
            LOG.error("", e);
            LOG.error("List of methods for "
                    + objectLoaded.getClass().getCanonicalName() + ":");
            try {
                Method m[] = objectLoaded.getClass().getDeclaredMethods();
                // TODO remove getters

                for (int i = 0; i < m.length; i++)
                    LOG.error(m[i].toString());
            } catch (Throwable e2) {
                LOG.error("", e2);
                System.exit(0);
            }
        }
    }

    /**
     * set current token value to the attribute of the current ContextObject (setContextObject)
     *
     * @param attribute
     * @throws JsonParseException
     * @throws IOException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    void set(final String attribute) throws JsonParseException, IOException,
            SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException
    {

        jp.nextToken();

        if (!attribute.equals(jp.getCurrentName())) {
            LOG.error("Missing mandatory attribute '" + attribute + "', "
                    + _contextMessage + _contextValue);
            if (isExitOnError) {
                System.exit(0);
            }
        }

        setValue(this._contextObject, attribute, jp.getText());
        try {
            jp.nextToken();
        } catch (JsonParseException e) {
            LOG.error("Error on mandatory attribute '" + attribute + "', "
                    + _contextMessage + _contextValue, e);
        }
    }

    void setIfExists(final String attribute) throws JsonParseException,
            IOException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException
    {
        if (attribute.equals(jp.getCurrentName())) {
            setValue(this._contextObject, attribute, jp.getText());
            jp.nextToken();
        }
    }

    /**
     * set current token value: an array, to the attribute of the current ContextObject (setContextObject)
     *
     * @param attribute
     * @throws JsonParseException
     * @throws IOException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    void setArray(final String attribute) throws JsonParseException, IOException,
            SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException
    {
        List<String> list = new ArrayList<String>();

        JsonToken current = JsonToken.START_ARRAY;
        current = jp.nextToken();

        if (!attribute.equals(jp.getCurrentName())) {
            LOG.error("Missing mandatory array '" + attribute + "', "
                    + _contextMessage + _contextValue);
            System.exit(0);
        }
        current = jp.nextToken();
        if (current != JsonToken.START_ARRAY) {
            LOG.error("Bad Type mandatory array '" + attribute + "', "
                    + _contextMessage + _contextValue);
            System.exit(0);
        }
        current = jp.nextToken();
        // String fieldName = "";
        String value = "";
        while (current != JsonToken.END_ARRAY) {
            // fieldName = jp.getCurrentName();
            value = jp.getText();
            if (value == null)
                break;
            LOG.debug(/* "name:" + fieldName + */" value:" + value);
            list.add(value);
            current = jp.nextToken();
        }
        String[] array = new String[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }

        setValue(this._contextObject, attribute, array);
        jp.nextToken();
        // return array;
        // return (String[]) list.toArray();
    }

    // http://www.ngdata.com/site/blog/63-ng.html
    List setRecordsArray(Class clazz, final String attribute, String[] recordColumn) throws JsonParseException,
            JsonProcessingException, IOException
    {

        JsonToken current = JsonToken.START_ARRAY;
        String value = "";
        List list = new ArrayList();
        jp.nextToken();

        String fieldName = jp.getCurrentName();
        if (fieldName.equals(attribute)) {
            jp.nextToken();
            while (current != JsonToken.END_ARRAY) {
                jp.nextToken();
                if (current == JsonToken.END_ARRAY)
                    break;
                try {
                    Class<?> c = Class.forName(clazz.getCanonicalName());
                    Object o = c.newInstance(); // InstantiationException
                    for (int i = 0; i < recordColumn.length; i++) {
                        if (fieldName == null)
                            break;
                        fieldName = jp.getCurrentName();
                        value = jp.nextTextValue();
                        LOG.debug("RecordsArray attribute: " + attribute + "/"
                                + recordColumn[i] + " fieldName:" + fieldName + " value:"
                                + value + " i:" + i);
                        setValue(o, recordColumn[i], value);
                        current = jp.nextToken();
                        if (current == JsonToken.END_ARRAY)
                            break;
                    }
                    list.add(o);
                    current = jp.nextToken();
                    if (current == JsonToken.END_ARRAY)
                        break;
                    // Production code should handle these exceptions more gracefully
                } catch (ClassNotFoundException x) {
                    LOG.error("", x);
                } catch (InstantiationException x) {
                    LOG.error("", x);
                } catch (IllegalAccessException x) {
                    LOG.error("", x);
                }
            }
        } else {
            LOG.error("Unprocessed property: " + fieldName + " " + attribute + ", "
                    + _contextMessage + _contextValue);
        }
        return list;
    }

    private void toString(String message, JsonToken token)
    {
        try {
            try {
                LOG.info(message + token.asString());
            } catch (NullPointerException e) {
                LOG.info(message + token);
            }
        } catch (NullPointerException e) {
            LOG.info(message);
        }
    }

    private JsonToken nextTokenDebug() throws JsonParseException, IOException
    {
        if (jp == null) {
            LOG.error("NULL JP");
        }
        JsonToken token = jp.nextToken();
        if (token == null) {
            LOG.error("NULL TOKEN");
        }
        try {
            if (token == JsonToken.END_ARRAY) {
                LOG.info("END_ARRAY");
            } else if (token == JsonToken.END_OBJECT) {
                LOG.info("END_OBJECT");
            } else if (token == JsonToken.FIELD_NAME) {
                LOG.info("FIELD_NAME:" + jp.getCurrentName());
            } else if (token == JsonToken.NOT_AVAILABLE) {
                toString("NOT_AVAILABLE:", token);
            } else if (token == JsonToken.START_ARRAY) {
                toString("START_ARRAY:", token);
            } else if (token == JsonToken.START_OBJECT) {
                toString("START_OBJECT:", token);
            } else if (token == JsonToken.VALUE_EMBEDDED_OBJECT) {
                toString("VALUE_EMBEDDED_OBJECT:", token);
            } else if (token == JsonToken.VALUE_NULL) {
                toString("VALUE_NULL:", token);
            } else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
                LOG.info("VALUE_NUMBER_FLOAT:" + jp.getDoubleValue());
            } else if (token == JsonToken.VALUE_NUMBER_INT) {
                LOG.info("VALUE_NUMBER_INT:" + jp.getIntValue());
            } else if (token == JsonToken.VALUE_STRING) {
                LOG.info("VALUE_STRING:" + jp.getText());
            } else if (token == JsonToken.VALUE_TRUE) {
                LOG.info("VALUE_TRUE:" + jp.getBooleanValue());
            } else {
                LOG.info("UNKNOWN--");
                return token;
            }
        } catch (NullPointerException e) {
            LOG.debug("empty value", e);
        }
        return token;
    }

    public void setContextObject(Object object)
    {
        this._contextObject = object;
    }

    public void close()
    {
        try {
            // Ensure resources get cleaned up timely and properly
            jp.close();
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

}
