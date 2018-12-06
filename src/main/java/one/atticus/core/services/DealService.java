package one.atticus.core.services;

import one.atticus.core.config.AppConfig;
import org.codehaus.plexus.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Objects;

import static one.atticus.core.services.PackageUtils.authenticate;

/**
 * @author vgorin
 * file created on 12/6/18 8:16 PM
 */


@Service
@Path("/deal")
public class DealService {
    private final JdbcTemplate jdbc;
    private final AppConfig queries;

    @Autowired
    public DealService(JdbcTemplate jdbc, AppConfig queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    @POST
    @Path("/send")
    @Transactional
    public int sendContractProposal(
            @Context SecurityContext context,
            @QueryParam("contract_id") int contractId,
            @QueryParam("to_account_id") int toAccountId,
            @QueryParam("deal_title") String dealTitle
    ) {
        int accountId = authenticate(context);

        // 1. update contract – set proposed date
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("send_contract_proposal_update_contract"));
                    ps.setInt(1, contractId);
                    ps.setInt(2, accountId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new NotFoundException("contract doesn't exist, deleted or is already proposed");
        }

        // 2. insert a deal
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("send_contract_proposal_create_deal"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, accountId);
                        ps.setString(2, dealTitle);
                        return ps;
                    },
                    keyHolder
            );
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
        int dealId = Objects.requireNonNull(keyHolder.getKey()).intValue();

        // 3. insert first deal dialog (message)
        try {
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("send_contract_proposal_create_deal_dialog"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, dealId);
                        ps.setInt(2, accountId);
                        ps.setInt(3, contractId);
                        return ps;
                    },
                    keyHolder
            );
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
        int dealDialogId = Objects.requireNonNull(keyHolder.getKey()).intValue();

        // 4. insert contract party 1
        insertContractParty(contractId, accountId, keyHolder);
        int party1Id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        // 5. insert contract party 2
        insertContractParty(contractId, toAccountId, keyHolder);
        int party2Id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        // 6. sign the contract – party 1
        rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("send_contract_proposal_sign_contract"));
                    ps.setInt(1, contractId);
                    ps.setInt(2, accountId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new NotFoundException("contract party doesn't exist, deleted or is already signed");
        }

        return dealId;
    }

    private void insertContractParty(@QueryParam("contract_id") int contractId, int accountId, KeyHolder keyHolder) {
        try {
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("send_contract_proposal_create_contract_party"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, contractId);
                        ps.setInt(2, accountId);
                        ps.setTimestamp(3, new Timestamp(System.currentTimeMillis() + 604800000)); // plus 7 days
                        return ps;
                    },
                    keyHolder
            );
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

}
