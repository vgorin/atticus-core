package one.atticus.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class AppConfig {
    @Value("classpath:db-queries.xml")
    private Resource queryStorage;
    private PropertyReader queries;

    @PostConstruct
    public void init() {
        try {
            Properties p = new Properties();
            p.loadFromXML(queryStorage.getInputStream());
            queries = new PropertyReader(p::getProperty);
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getQuery(String key) {
        return queries.getString(key);
    }

}
