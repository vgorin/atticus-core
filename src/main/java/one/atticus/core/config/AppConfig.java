package one.atticus.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class AppConfig implements PropertyReader {
    @Value("classpath:db-queries.xml")
    private Resource queryStorage;
    private PropertyReader queries;

    @Value("classpath:app-config.properties")
    private Resource appConfig;
    private PropertyReader config;

    @PostConstruct
    public void init() {
        try {
            loadQueryStorage();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadQueryStorage() throws IOException {
        Properties p = new Properties();
        p.loadFromXML(queryStorage.getInputStream());
        queries = new StringPropertyReader(p::getProperty);
    }

    private void loadAppConfig() throws IOException {
        Properties p = new Properties();
        p.load(appConfig.getInputStream());
        config = new StringPropertyReader(p::getProperty);
    }

    public String getQuery(String key) {
        return queries.getString(key);
    }

    @Override
    public String getString(String key) {
        return config.getString(key);
    }

    @Override
    public int getInt(String key) {
        return config.getInt(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }
}
