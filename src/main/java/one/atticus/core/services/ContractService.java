package one.atticus.core.services;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.Contract;
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

/**
 * @author vgorin
 * file created on 12/6/18 2:08 PM
 */


@Service
@Path("/contract")
public class ContractService {
    private final JdbcTemplate jdbc;
    private final AppConfig queries;

    @Autowired
    public ContractService(JdbcTemplate jdbc, AppConfig queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int create(@Context SecurityContext context, Contract contract) {
        int accountId = authenticate(context);

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("create_contract"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, accountId);
                        ps.setString(2, contract.memo);
                        ps.setString(3, contract.body);
                        ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
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
    @Path("/{contractId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Contract retrieve(@Context SecurityContext context, @PathParam("contractId") int contractId) {
        int accountId = authenticate(context);

        Contract contract = jdbc.query(
                c -> preparedStatement (c, contractId, accountId, queries.getQuery("get_contract")),
                this::getContract
        );
        if(contract == null) {
            throw new NotFoundException("contract doesn't exist / deleted");
        }
        return contract;
    }

    @PUT
    @Path("/{contractId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(@Context SecurityContext context, @PathParam("contractId") int contractId, Contract contract) {
        int accountId = authenticate(context);

        try {
            int rowsUpdated = jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(queries.getQuery("update_contract"));
                        ps.setString(1, contract.memo);
                        ps.setString(2, contract.body);
                        ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                        ps.setInt(4, contractId);
                        ps.setInt(5, accountId);
                        return ps;
                    }
            );
            if(rowsUpdated == 0) {
                throw new NotFoundException("contract doesn't exist, deleted or is not editable (proposed)");
            }
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @DELETE
    @Path("/{contractId}")
    public void delete(@Context SecurityContext context, @PathParam("contractId") int contractId, Contract contract) {
        int accountId = authenticate(context);
        int rowsUpdated = jdbc.update(c -> preparedStatement (c, contractId, accountId, queries.getQuery("delete_contract")));
        if(rowsUpdated == 0) {
            throw new NotFoundException("contract doesn't exist or already deleted");
        }
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Contract> listContracts(@Context SecurityContext context, @QueryParam("type") String type) {
        int accountId = authenticate(context);

        switch(type) {
            case "draft": {
                return jdbc.query(
                        c -> preparedStatement(c, accountId, queries.getQuery("list_draft_contracts")),
                        this::getContracts
                );
            }
            case "proposed": {
                return jdbc.query(
                        c -> preparedStatement(c, accountId, queries.getQuery("list_proposed_contracts")),
                        this::getContracts
                );
            }
            default: {
                return jdbc.query(
                        c -> preparedStatement(c, accountId, queries.getQuery("list_contracts")),
                        this::getContracts
                );
            }
        }
    }

    @PUT
    @Path("/propose/{contractId}")
    public void propose(@Context SecurityContext context, @PathParam("contractId") int contractId) {
        int accountId = authenticate(context);

        int rowsUpdated = jdbc.update(c -> preparedStatement(c, contractId, accountId, queries.getQuery("propose_contract")));
        if(rowsUpdated == 0) {
            throw new NotFoundException("contract doesn't exist, deleted or already proposed");
        }
    }

    private PreparedStatement preparedStatement(Connection c, int accountId, String query) throws SQLException {
        PreparedStatement ps = c.prepareStatement(query);
        ps.setInt(1, accountId);
        return ps;
    }

    private PreparedStatement preparedStatement(Connection c, int contractId, int accountId, String query) throws SQLException {
        PreparedStatement ps = c.prepareStatement(query);
        ps.setInt(1, contractId);
        ps.setInt(2, accountId);
        return ps;
    }

    private List<Contract> getContracts(ResultSet rs) throws SQLException {
        List<Contract> contracts = new LinkedList<>();
        Contract contract;
        while((contract = getContract(rs)) != null) {
            contracts.add(contract);
        }
        return contracts;
    }

    private Contract getContract(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }

        Timestamp proposed = rs.getTimestamp("proposed");
        Timestamp deleted = rs.getTimestamp("deleted");
        Timestamp modified = rs.getTimestamp("modified");
        Timestamp updated = rs.getTimestamp("rec_updated");

        Contract template = new Contract();
        template.contractId = rs.getInt("id");
        template.accountId = rs.getInt("account_id");
        template.memo = rs.getString("memo");
        template.body = rs.getString("body");
        template.proposed = proposed == null? null: proposed.getTime();
        template.deleted = deleted == null? null: deleted.getTime();
        template.modified = modified == null? null: modified.getTime();
        template.updated = updated == null? null: updated.getTime();
        template.created = rs.getTimestamp("rec_created").getTime();

        return template;
    }

}
