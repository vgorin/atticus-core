package one.atticus.core.services;

import one.atticus.core.resources.ContractTemplate;
import org.codehaus.plexus.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Component
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
    public ContractTemplate create(ContractTemplate template) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO contract_template(account_id, title, version, body) VALUES(?, ? ,?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, template.accountId);
                        ps.setString(2, template.title);
                        ps.setString(3, template.version);
                        ps.setString(4, template.body);
                        return ps;
                    },
                    keyHolder
            );
            template.templateId = Objects.requireNonNull(keyHolder.getKey()).intValue();
            return template;
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }


    @GET
    @Path("/{templateId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ContractTemplate retrieve(@PathParam("templateId") int templateId) {
        ContractTemplate template = jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "SELECT * FROM contract_template WHERE id = ?"
                    );
                    ps.setInt(1, templateId);
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
    public void update(@PathParam("templateId") int templateId, ContractTemplate template) {
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE account_template SET id = ?, account_id = ?, title = ?, version = ?, body = ? WHERE id = ?",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, template.templateId);
                    ps.setInt(2, template.accountId);
                    ps.setString(2, template.title);
                    ps.setString(3, template.version);
                    ps.setString(4, template.body);
                    ps.setInt(5, templateId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new NotFoundException();
        }
    }

    private ContractTemplate getContractTemplate(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }
        ContractTemplate template = new ContractTemplate();
        template.templateId = rs.getInt("id");
        template.accountId = rs.getInt("account_id");
        template.title = rs.getString("title");
        template.version = rs.getString("version");
        template.body = rs.getString("body");
        return template;
    }

}
