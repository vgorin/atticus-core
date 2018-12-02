package one.atticus.core;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import one.atticus.core.services.AccountService;
import one.atticus.core.services.ContractService;
import one.atticus.core.services.ContractTemplateService;
import one.atticus.core.services.DealService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.internal.WadlResource;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        // https://dzone.com/articles/using-jax-rs-with-spring-boot-instead-of-mvc
        // https://dzone.com/articles/7-reasons-i-do-not-use-jax-rs-in-spring-boot-web-a
        register(AccountService.class);
        register(ContractService.class);
        register(ContractTemplateService.class);
        register(DealService.class);
        register(CrossOriginResourceSharingFilter.class);
        register(AtticusExceptionMapper.class);
        configureSwagger();
        registerEndpoints();
    }


    private void configureSwagger() {
        register(ApiListingResource.class);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/");
        beanConfig.setResourcePackage("one.atticus.core.services");
        beanConfig.setPrettyPrint(true);
        beanConfig.setScan(true);
    }
    private void registerEndpoints() {
        register(WadlResource.class);
    }
}
