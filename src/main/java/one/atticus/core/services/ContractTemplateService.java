package one.atticus.core.services;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.ContractTemplate;
import org.codehaus.plexus.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static one.atticus.core.services.PackageUtils.authenticate;

/**
 * @author vgorin
 *         file created on 12/6/18 1:26 PM
 */


@Service
@Path("/template")
public class ContractTemplateService {
    private final JdbcTemplate jdbc;
    private final AppConfig queries;

    @Autowired
    public ContractTemplateService(JdbcTemplate jdbc, AppConfig queries) {
        this.jdbc = jdbc;
        this.queries = queries;
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
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("create_contract_template"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, accountId);
                        ps.setString(2, template.title);
                        ps.setString(3, template.version);
                        ps.setString(4, template.body);
                        ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                        return ps;
                    },
                    keyHolder
            );

            // TODO: replace with 201 Created
            return Objects.requireNonNull(keyHolder.getKey()).intValue();
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @GET
    @Path("/{templateId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ContractTemplate retrieve(@Context SecurityContext context, @PathParam("templateId") int templateId) {
        int accountId = authenticate(context);

        ContractTemplate template = jdbc.query(
                c -> preparedStatement(c, templateId, accountId, queries.getQuery("get_contract_template")),
                this::getContractTemplate
        );
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
            int rowsUpdated = jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(queries.getQuery("update_contract_template"));
                        ps.setString(1, template.title);
                        ps.setString(2, template.version);
                        ps.setString(3, template.body);
                        ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                        ps.setInt(5, templateId);
                        ps.setInt(6, accountId);
                        return ps;
                    }
            );
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
        int rowsUpdated = jdbc.update(c -> preparedStatement (c, templateId, accountId, queries.getQuery("delete_contract_template")));
        if(rowsUpdated == 0) {
            throw new NotFoundException("template doesn't exist or already deleted");
        }
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ContractTemplate> listTemplates(@Context SecurityContext context) {
        int accountId = authenticate(context);

        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("list_contract_templates"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                this::getContractTemplates
        );
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ContractTemplate> searchTemplates(@Context SecurityContext context, @QueryParam("q") String q) {
        int accountId = authenticate(context);

        String query = String.format("%%%s%%", q);
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("search_contract_templates"));
                    ps.setInt(1, accountId);
                    ps.setString(2, query);
                    return ps;
                },
                this::getContractTemplates
        );
    }

    @PUT
    @Path("/release/{templateId}")
    public void release(@Context SecurityContext context, @PathParam("templateId") int templateId) {
        int accountId = authenticate(context);

        int rowsUpdated = jdbc.update(c -> preparedStatement(c, templateId, accountId, queries.getQuery("release_contract_template")));
        if(rowsUpdated == 0) {
            throw new NotFoundException("template doesn't exist, doesn't have a version, deleted or already versioned");
        }
    }

    @PUT
    @Path("/publish/{templateId}")
    public void publish(@Context SecurityContext context, @PathParam("templateId") int templateId) {
        int accountId = authenticate(context);

        int rowsUpdated = jdbc.update(c -> preparedStatement(c, templateId, accountId, queries.getQuery("publish_contract_template")));
        if(rowsUpdated == 0) {
            throw new NotFoundException("template doesn't exist, not versioned, deleted or already published");
        }
    }

    private PreparedStatement preparedStatement(Connection c, int templateId, int accountId, String query) throws SQLException {
        PreparedStatement ps = c.prepareStatement(query);
        ps.setInt(1, templateId);
        ps.setInt(2, accountId);
        return ps;
    }

    private List<ContractTemplate> getContractTemplates(ResultSet rs) throws SQLException {
        List<ContractTemplate> result = new LinkedList<>();
        ContractTemplate template;
        while((template = getContractTemplate(rs)) != null) {
            result.add(template);
        }
        return result;
    }

    private ContractTemplate getContractTemplate(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }
        Timestamp versioned = rs.getTimestamp("versioned");
        Timestamp deleted = rs.getTimestamp("deleted");
        Timestamp published = rs.getTimestamp("published");
        Timestamp updated = rs.getTimestamp("rec_updated");

        ContractTemplate template = new ContractTemplate();
        template.templateId = rs.getInt("id");
        template.accountId = rs.getInt("account_id");
        template.title = rs.getString("title");
        template.version = rs.getString("version");
        template.body = rs.getString("body");
        template.versioned = versioned == null? null: versioned.getTime();
        template.deleted = deleted == null? null: deleted.getTime();
        template.published = published == null? null: published.getTime();
        template.created = rs.getTimestamp("rec_created").getTime();
        template.updated = updated == null? null: updated.getTime();

        return template;
    }

}
