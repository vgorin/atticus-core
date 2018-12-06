package one.atticus.core.config;

import java.util.function.Function;

/**
 * @author vgorin
 *         file created on 12/6/18 1:12 PM
 */


public class PropertyReader {
    private Function<String, String> strGetter;

    public PropertyReader(Function<String, String> strGetter) {
        this.strGetter = strGetter;
    }

    public String getString(String key) {
        return strGetter.apply(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    public String getString(String key, String defaultValue) {
        return getWithDefault(this::getString, key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return getWithDefault(this::getInt, key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getWithDefault(this::getBoolean, key, defaultValue);
    }

    public String loadProperty(String key) {
        String value = loadSystemProperty(key);
        if(value == null) {
            value = strGetter.apply(key);
        }
        return value;
    }

    private static <T> T getWithDefault(Function<String, T> getter, String key, T defaultValue) {
        try {
            return getter.apply(key);
        }
        catch(Exception e) {
            return defaultValue;
        }
    }

    public static String loadSystemProperty(String key) {
        String value = System.getProperty(key);
        if(value == null) {
            value = System.getenv(key);
        }
        return value;
    }

}
