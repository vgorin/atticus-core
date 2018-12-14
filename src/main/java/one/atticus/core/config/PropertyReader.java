package one.atticus.core.config;

/**
 * @author vgorin
 *         file created on 12/14/18 8:56 PM
 */


public interface PropertyReader {
    String getString(String key);

    int getInt(String key);

    boolean getBoolean(String key);

    String getString(String key, String defaultValue);

    int getInt(String key, int defaultValue);

    boolean getBoolean(String key, boolean defaultValue);

}
