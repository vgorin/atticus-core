package one.atticus.core.services;

import one.atticus.core.dao.ContractTemplateDAO;
import one.atticus.core.resources.ContractTemplate;
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static one.atticus.core.services.PackageUtils.authenticate;

/**
 * @author vgorin
 *         file created on 12/6/18 1:26 PM
 */


@Service
@Path("/template")
public class ContractTemplateService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ContractTemplateDAO templateDAO;

    @Autowired
    public ContractTemplateService(ContractTemplateDAO templateDAO) {
        this.templateDAO = templateDAO;
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int create(
            @Context SecurityContext context,
            ContractTemplate template
    ) {
        int accountId = authenticate(context);

        try {
            template.accountId = accountId;
            int templateId = templateDAO.create(template);
            template.templateId = templateId;

            log.debug("template successfully created: {}", template);
            // TODO: replace with 201 Created
            return templateId;
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @POST
    @Path("/{contractId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int clone(@Context SecurityContext context, @PathParam("contractId") int templateId) {
        int accountId = authenticate(context);

        return templateDAO.clone(templateId, accountId);
    }

    @GET
    @Path("/{templateId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ContractTemplate retrieve(@Context SecurityContext context, @PathParam("templateId") int templateId) {
        int accountId = authenticate(context);

        ContractTemplate template = templateDAO.retrieve(templateId, accountId);
        if(template == null) {
            throw new NotFoundException("template doesn't exist / deleted");
        }
        return template;
    }

    @PUT
    @Path("/{templateId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(@Context SecurityContext context, @PathParam("templateId") int templateId, ContractTemplate template) {
        int accountId = authenticate(context);

        try {
            template.templateId = templateId;
            template.accountId = accountId;

            int rowsUpdated = templateDAO.update(template);

            if(rowsUpdated == 0) {
                throw new NotFoundException("template doesn't exist, deleted or is not editable (versioned / published)");
            }
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @DELETE
    @Path("/{templateId}")
    public void delete(@Context SecurityContext context, @PathParam("templateId") int templateId, ContractTemplate template) {
        int accountId = authenticate(context);
        int rowsUpdated = templateDAO.delete(templateId, accountId);
        if(rowsUpdated == 0) {
            throw new NotFoundException("template doesn't exist or already deleted");
        }
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ContractTemplate> listTemplates(@Context SecurityContext context) {
        int accountId = authenticate(context);

        return templateDAO.list(accountId);
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ContractTemplate> searchTemplates(@Context SecurityContext context, @QueryParam("q") String q) {
        int accountId = authenticate(context);

        return templateDAO.search(accountId, q);
    }

    @PUT
    @Path("/release/{templateId}")
    public void release(@Context SecurityContext context, @PathParam("templateId") int templateId) {
        int accountId = authenticate(context);

        int rowsUpdated = templateDAO.release(templateId, accountId);
        if(rowsUpdated == 0) {
            throw new NotFoundException("template doesn't exist, doesn't have a version, deleted or already versioned");
        }
    }

    @PUT
    @Path("/publish/{templateId}")
    public void publish(@Context SecurityContext context, @PathParam("templateId") int templateId) {
        int accountId = authenticate(context);

        int rowsUpdated = templateDAO.publish(templateId, accountId);
        if(rowsUpdated == 0) {
            throw new NotFoundException("template doesn't exist, not versioned, deleted or already published");
        }
    }

}
