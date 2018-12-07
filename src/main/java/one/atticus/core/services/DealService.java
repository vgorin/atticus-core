package one.atticus.core.services;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.Contract;
import one.atticus.core.resources.Deal;
import one.atticus.core.resources.DealDialog;
import one.atticus.core.resources.Party;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static one.atticus.core.services.PackageUtils.authenticate;

/**
 * @author vgorin
 *         file created on 12/6/18 8:16 PM
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
    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    public int initDeal(
            @Context SecurityContext context,
            @QueryParam("contract_id") int contractId,
            @QueryParam("to_account_id") int toAccountId,
            @QueryParam("deal_title") String dealTitle
    ) {
        int accountId = authenticate(context);

        // 1. update contract – set proposed date
        saveContract(contractId, accountId);

        // 2. insert a deal
        int dealId = createNewDeal(accountId, dealTitle);

        // 3. insert first deal dialog (message)
        createDealDialog(dealId, accountId, contractId);

        // contract is valid until 7 days from now
        long validUntil = System.currentTimeMillis() + 604800000; // 7 * 24 * 3600 * 1000

        // 4. self sign the contract
        selfSignContract(contractId, accountId, validUntil);

        // 5. invite another party to the contract
        inviteContractParty(contractId, toAccountId, validUntil);

        return dealId;
    }

    @GET
    @Path("/submitted-proposals")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Deal> listSubmittedProposals() {
        return null;
    }

    @GET
    @Path("/received-proposals")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Deal> listReceivedProposals(@Context SecurityContext context) {
        int accountId = authenticate(context);
        List<Deal> receivedProposals = jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("list_received_proposals"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                rs -> {
                    List<Deal> result = new LinkedList<>();

                    while(rs.next()) {
                        Deal deal = new Deal();
                        result.add(deal);
                        deal.dealId = rs.getInt("id");
                        deal.contractId = rs.getInt("contract_id");
                        deal.title = rs.getString("title");
                        deal.created = rs.getTimestamp("rec_created").getTime();
                    }
                    return result;
                }
        );

        for(Deal deal: Objects.requireNonNull(receivedProposals)) {
            deal.dialog = jdbc.query(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(queries.getQuery("retrieve_deal_dialog"));
                        ps.setInt(1, deal.dealId);
                        return ps;
                    },
                    rs -> {
                        List<DealDialog> dialogs = new LinkedList<>();
                        while(rs.next()) {
                            DealDialog dialog = new DealDialog();
                            dialogs.add(dialog);
                            dialog.dialogId = rs.getInt("id");
                            dialog.message = rs.getString("message");
                            dialog.attachment = rs.getBytes("attachment");
                            dialog.contractId = rs.getInt("contract_id");
                        }
                        return dialogs;
                    }
            );
            deal.parties = jdbc.query(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(queries.getQuery("list_deal_parties"));
                        ps.setInt(1, deal.contractId);
                        return ps;
                    },
                    rs -> {
                        List<Party> parties = new LinkedList<>();
                        while(rs.next()) {
                            Party party = new Party();
                            parties.add(party);
                            party.partyId = rs.getInt("id");
                            party.contractId = rs.getInt("contract_id");
                            party.accountId = rs.getInt("account_id");
                            party.partyLabel = rs.getString("party_label");
                            party.validUntil = rs.getTimestamp("valid_until").getTime();
                        }
                        return parties;
                    }
            );
        }

        return receivedProposals;
    }

    @GET
    @Path("/active-deals")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Deal> listActiveDeals() {
        return null;
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Deal> listDeals() {
        return null;
    }


    private void saveContract(int contractId, int accountId) {
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("init_deal__save_contract"));
                    ps.setInt(1, contractId);
                    ps.setInt(2, accountId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new NotFoundException("contract doesn't exist, deleted or is already proposed");
        }
    }

    private int createNewDeal(int accountId, String dealTitle) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("init_deal__create_deal"),
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
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    private int createDealDialog(int dealId, int accountId, int contractId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("init_deal__create_dialog"),
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
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    private int selfSignContract(int contractId, int accountId, long validUntil) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("init_deal__self_sign_contract"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, contractId);
                        ps.setInt(2, accountId);
                        ps.setTimestamp(3, new Timestamp(validUntil));
                        ps.setBytes(4, "Atticus".getBytes()); // TODO: calculate real signature
                        return ps;
                    },
                    keyHolder
            );
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    private int inviteContractParty(int contractId, int accountId, long validUntil) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("init_deal__invite_contract_party"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, contractId);
                        ps.setInt(2, accountId);
                        ps.setTimestamp(3, new Timestamp(validUntil));
                        return ps;
                    },
                    keyHolder
            );
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

}
