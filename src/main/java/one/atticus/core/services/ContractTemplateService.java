package one.atticus.core.services;

import one.atticus.core.resources.ContractTemplate;
import org.codehaus.plexus.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static one.atticus.core.services.PackageUtils.authenticate;

@Service
@Path("/template")
public class ContractTemplateService {
    private final JdbcTemplate jdbc;

    @Autowired
    public ContractTemplateService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Path("/")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ContractTemplate create(@Context SecurityContext context, ContractTemplate template) {
        int accountId = authenticate(context);
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO contract_template(account_id, title, version, body) VALUES(?, ? ,?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, accountId);
                        ps.setString(2, template.title);
                        ps.setString(3, template.version);
                        ps.setString(4, template.body);
                        return ps;
                    },
                    keyHolder
            );
            template.templateId = Objects.requireNonNull(keyHolder.getKey()).intValue();
            template.accountId = accountId;
            return template;
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
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "SELECT * FROM contract_template WHERE id = ? AND account_id = ?"
                    );
                    ps.setInt(1, templateId);
                    ps.setInt(2, accountId);
                    return ps;
                },
                this::getContractTemplate
        );
        if(template == null) {
            throw new NotFoundException();
        }
        return template;
    }

    @PUT
    @Path("/{templateId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(@Context SecurityContext context, @PathParam("templateId") int templateId, ContractTemplate template) {
        int accountId = authenticate(context);
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE contract_template SET title = ?, version = ?, body = ? WHERE id = ? AND account_id = ? AND versioned IS NULL AND deleted IS NOT NULL"
                    );
                    ps.setString(1, template.title);
                    ps.setString(2, template.version);
                    ps.setString(3, template.body);
                    ps.setInt(4, templateId);
                    ps.setInt(5, accountId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new NotFoundException();
        }
    }

    @DELETE
    @Path("/{templateId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void delete(@Context SecurityContext context, @PathParam("templateId") int templateId, ContractTemplate template) {
        int accountId = authenticate(context);
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE contract_template SET deleted = now() WHERE id = ? and account_id = ? AND deleted IS NULL"
                    );
                    ps.setInt(1, templateId);
                    ps.setInt(2, accountId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new BadRequestException("template doesn't exist or already deleted");
        }
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ContractTemplate> listTemplates(@Context SecurityContext context) {
        int accountId = authenticate(context);
        List<ContractTemplate> templates = jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "SELECT * FROM contract_template WHERE account_id = ?"
                    );
                    ps.setInt(1, accountId);
                    return ps;
                },
                this::getContractTemplates
        );
        if(templates == null || templates.isEmpty()) {
            throw new NotFoundException();
        }
        return templates;
    }

    @PUT
    @Path("/version/{templateId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void version(@Context SecurityContext context, @PathParam("templateId") int templateId) {
        int accountId = authenticate(context);

        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE contract_template SET versioned = NOW() WHERE id = ? AND account_id = ? AND versioned IS NULL AND deleted IS NULL AND version IS NOT NULL"
                    );
                    ps.setInt(1, templateId);
                    ps.setInt(2, accountId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new BadRequestException("template doesn't exist, doesn't have a version, deleted or already versioned");
        }
    }

    @PUT
    @Path("/publish/{templateId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void publish(@Context SecurityContext context, @PathParam("templateId") int templateId) {
        int accountId = authenticate(context);

        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE contract_template SET published = NOW() WHERE id = ? AND account_id = ? AND versioned IS NOT NULL AND deleted IS NULL AND published IS NULL"
                    );
                    ps.setInt(1, templateId);
                    ps.setInt(2, accountId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new BadRequestException("template doesn't exist, not versioned, deleted or already published");
        }
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
        Date versioned = rs.getDate("versioned");
        Date deleted = rs.getDate("deleted");
        Date published = rs.getDate("published");

        ContractTemplate template = new ContractTemplate();
        template.templateId = rs.getInt("id");
        template.accountId = rs.getInt("account_id");
        template.title = rs.getString("title");
        template.version = rs.getString("version");
        template.body = rs.getString("body");
        template.versioned = versioned == null? null: versioned.getTime();
        template.deleted = deleted == null? null: deleted.getTime();
        template.published = published == null? null: published.getTime();

        return template;
    }

}
