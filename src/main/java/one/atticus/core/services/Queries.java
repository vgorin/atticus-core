package one.atticus.core.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;

@Configuration
@PropertySource("classpath:db-queries.xml")
public class Queries {
    private Properties queryStorage = new Properties();

    @Value("${create_contract_template}")
    private void create_contract_template(String value) {
        queryStorage.put("create_contract_template", value);
    }

    @Value("${get_contract_template}")
    private void get_contract_template(String value) {
        queryStorage.put("get_contract_template", value);
    }

    @Value("${update_contract_template}")
    private void update_contract_template(String value) {
        queryStorage.put("update_contract_template", value);
    }

    @Value("${delete_contract_template}")
    private void delete_contract_template(String value) {
        queryStorage.put("delete_contract_template", value);
    }

    @Value("${list_contract_templates}")
    private void list_contract_templates(String value) {
        queryStorage.put("list_contract_templates", value);
    }

    @Value("${release_contract_template}")
    private void release_contract_template(String value) {
        queryStorage.put("release_contract_template", value);
    }

    @Value("${publish_contract_template}")
    private void publish_contract_template(String value) {
        queryStorage.put("publish_contract_template", value);
    }

    String get(String key) {
        return queryStorage.getProperty(key);
    }

}
