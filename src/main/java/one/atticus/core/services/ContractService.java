package one.atticus.core.services;

import one.atticus.core.resources.Contract;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component
@Path("/contract")
public class ContractService {
    private final JdbcTemplate jdbc;

    @Autowired
    public ContractService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Path("/")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Contract create(Contract contract) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO contract(account_id, memo, body) VALUES(?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, contract.accountId);
                        ps.setString(2, contract.memo);
                        ps.setString(3, contract.body);
                        return ps;
                    },
                    keyHolder
            );
            contract.contractId = Objects.requireNonNull(keyHolder.getKey()).intValue();
            return contract;
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @GET
    @Path("/{contractId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Contract retrieve(@PathParam("contractId") int contractId) {
        Contract contract = jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "SELECT * FROM contract WHERE id = ?"
                    );
                    ps.setInt(1, contractId);
                    return ps;
                },
                this::getContract
        );
        if(contract == null) {
            throw new NotFoundException();
        }
        return contract;
    }

    @PUT
    @Path("/{contractId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(@PathParam("contractId") int contractId, Contract contract) {
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE contract SET id = ?, account_id = ?, memo = ?, body = ? WHERE id = ?",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, contract.contractId);
                    ps.setInt(2, contract.accountId);
                    ps.setString(3, contract.memo);
                    ps.setString(4, contract.body);
                    ps.setInt(5, contractId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new NotFoundException();
        }
    }

    @GET
    @Path("/list/{accountId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Contract> listContracts(@PathParam("accountId") int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "SELECT * FROM contract WHERE account_id = ?"
                    );
                    ps.setInt(1, accountId);
                    return ps;
                },
                this::getContracts
        );
    }

    private List<Contract> getContracts(ResultSet rs) throws SQLException {
        List<Contract> contracts = new LinkedList<>();
        Contract contract = null;
        while((contract = getContract(rs)) != null) {
            contracts.add(contract);
        }
        return contracts;
    }

    private Contract getContract(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }
        Contract template = new Contract();
        template.contractId = rs.getInt("id");
        template.accountId = rs.getInt("account_id");
        template.memo = rs.getString("memo");
        template.body = rs.getString("body");
        return template;
    }

}
