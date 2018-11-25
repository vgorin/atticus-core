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

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Contract create(Contract contract) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO contract(account_id, parties, header, body, draft) VALUES(?, ? ,?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, contract.accountId);
                        ps.setInt(2, contract.parties);
                        ps.setString(3, contract.header);
                        ps.setString(4, contract.body);
                        ps.setBoolean(5, contract.draft);
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

    @GET
    @Path("/by/{accountId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Contract[] byUsername(@PathParam("accountId") int accountId) {
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

    private Contract getContract(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }
        Contract contract = new Contract();
        contract.contractId = rs.getInt("id");
        contract.accountId = rs.getInt("account_id");
        contract.parties = rs.getInt("parties");
        contract.header = rs.getString("header");
        contract.body = rs.getString("body");
        contract.draft = rs.getBoolean("draft");
        return contract;
    }

    private Contract[] getContracts(ResultSet rs) throws SQLException {
        List<Contract> contracts = new LinkedList<>();
        Contract c;
        while((c = getContract(rs)) != null) {
            contracts.add(c);
        }
        return contracts.toArray(new Contract[0]);
    }

}
