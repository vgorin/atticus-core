package one.atticus.core.config;

import one.atticus.core.security.CrossOriginResourceSharingFilter;
import one.atticus.core.security.JerseyAuthFilter;
import one.atticus.core.services.AccountService;
import one.atticus.core.services.ContractService;
import one.atticus.core.services.ContractTemplateService;
import one.atticus.core.services.DealService;
import one.atticus.core.util.JsonUtil;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

/**
 * @author vgorin
 * file created on 12/6/18 7:20 PM
 */


@Component
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        // https://dzone.com/articles/using-jax-rs-with-spring-boot-instead-of-mvc
        // https://dzone.com/articles/7-reasons-i-do-not-use-jax-rs-in-spring-boot-web-a

        // RESTful web service(s)
        register(AccountService.class);
        register(ContractService.class);
        register(ContractTemplateService.class);
        register(DealService.class);

        // request filter(s)
        register(CrossOriginResourceSharingFilter.class);
        register(JerseyAuthFilter.class);

        // exception mapper(s)
        register(DefaultExceptionMapper.class);

        // JAXB JSON - configure Jersey object mapper
        register(new JacksonJaxbJsonProvider(JsonUtil.OBJECT_MAPPER, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS));
   }
}
