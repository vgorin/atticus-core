package one.atticus.core.dao;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.Deal;
import one.atticus.core.resources.DealDialog;
import one.atticus.core.resources.Party;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.NotFoundException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author vgorin
 * file created on 12/13/18 6:21 PM
 */


@Service
public class DealDAO {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbc;
    private final AppConfig config;

    @Autowired
    public DealDAO(JdbcTemplate jdbc, AppConfig config) {
        this.jdbc = jdbc;
        this.config = config;
    }

    @Transactional
    public int submitNewDeal(int contractId, int accountId, int toAccountId, String dealTitle, long validityPeriod) {
        // 1. update contract – set proposed date
        saveContract(contractId, accountId);

        // 2. insert a deal
        int dealId = createNewDeal(accountId, dealTitle);

        // 3. insert first deal dialog (message)
        postFirstDialogMessage(dealId, accountId, contractId);

        // contract is valid until 7 days from now
        long validUntil = System.currentTimeMillis() + validityPeriod;

        // 4. self sign the contract
        selfSignContract(contractId, accountId, validUntil);

        // 5. invite another party to the contract
        inviteContractParty(contractId, toAccountId, validUntil);

        return dealId;
    }

    @Transactional
    public int postCounterProposal(int dealId, int accountId, int contractId, long validityPeriod) {
        // TODO: verify account is part of the deal and can post
        // 1. post message into dialog
        int dialogId = postNthDialogMessage(dealId, accountId, contractId);

        // contract is valid until 7 days from now
        long validUntil = System.currentTimeMillis() + validityPeriod;

        // 2. self sign the contract
        selfSignContract(contractId, accountId, validUntil);

        // 3. determine parties of the deal
        List<Integer> parties = listDealParties(dealId);

        // 4. invite another parties to the contract
        for(int party: parties) {
            if(party != accountId) {
                inviteContractParty(contractId, party, validUntil);
            }
        }

        return dialogId;
    }

    public List<Deal> listSubmittedProposals(int accountId) {
        return fetchDeals(accountId, config.getQuery("list_submitted_proposals"));
    }

    public List<Deal> listReceivedProposals(int accountId) {
        return fetchDeals(accountId, config.getQuery("list_received_proposals"));
    }

    public List<Deal> listActiveDeals(int accountId) {
        return fetchDeals(accountId, config.getQuery("list_active_deals"));
    }

    public List<Deal> listDeals(int accountId) {
        return fetchDeals(accountId, config.getQuery("list_deals"));
    }

    private void saveContract(int contractId, int accountId) {
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("init_deal__save_contract"));
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
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            config.getQuery("init_deal__create_deal"),
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, accountId);
                    ps.setString(2, dealTitle);
                    return ps;
                },
                keyHolder
        );
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    private int postFirstDialogMessage(int dealId, int accountId, int contractId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            config.getQuery("post_first_dialog_message"),
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, dealId);
                    ps.setInt(2, accountId);
                    ps.setInt(3, contractId);
                    return ps;
                },
                keyHolder
        );
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    private int postNthDialogMessage(int dealId, int accountId, int contractId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            config.getQuery("post_nth_dialog_message"),
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, dealId);
                    ps.setInt(2, accountId);
                    ps.setInt(3, contractId);
                    return ps;
                },
                keyHolder
        );
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    private int selfSignContract(int contractId, int accountId, long validUntil) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            config.getQuery("self_sign_contract"),
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, contractId);
                    ps.setInt(2, accountId);
                    ps.setTimestamp(3, new Timestamp(validUntil));
                    ps.setBytes(4, "Atticus-1".getBytes()); // TODO: calculate real signature
                    return ps;
                },
                keyHolder
        );
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    private int inviteContractParty(int contractId, int accountId, long validUntil) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            config.getQuery("invite_contract_party"),
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, contractId);
                    ps.setInt(2, accountId);
                    ps.setTimestamp(3, new Timestamp(validUntil));
                    return ps;
                },
                keyHolder
        );

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }


    private static List<Deal> getDeals(ResultSet rs) throws SQLException {
        List<Deal> result = new LinkedList<>();
        Deal deal;
        while((deal = getDeal(rs)) != null) {
            result.add(deal);
        }
        return result;
    }

    private static Deal getDeal(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }
        Deal deal = new Deal();
        deal.dealId = rs.getInt("id");
        deal.contractId = rs.getInt("contract_id");
        deal.title = rs.getString("title");
        deal.created = rs.getTimestamp("rec_created").getTime();
        return deal;
    }

    private List<Deal> fetchDeals(int accountId, String query) {
        List<Deal> deals = jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(query);
                    ps.setInt(1, accountId);
                    return ps;
                },
                DealDAO::getDeals
        );

        for(Deal deal: Objects.requireNonNull(deals)) {
            deal.dialog = retrieveDialog(deal.dealId);
            deal.parties = listParties(deal.contractId);
        }
        return deals;
    }

    private List<Integer> listDealParties(int dealId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("list_deal_parties"));
                    ps.setInt(1, dealId);
                    return ps;
                },
                rs -> {
                    List<Integer> parties = new LinkedList<>();
                    while(rs.next()) {
                        parties.add(rs.getInt("account_id"));
                    }
                    return parties;
                }
        );
    }

    private List<DealDialog> retrieveDialog(int dealId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("retrieve_deal_dialog"));
                    ps.setInt(1, dealId);
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
    }

    private List<Party> listParties(int contractId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("list_deal_parties_by_contract_id"));
                    ps.setInt(1, contractId);
                    return ps;
                },
                rs -> {
                    List<Party> parties = new LinkedList<>();
                    while(rs.next()) {
                        Party party = new Party();
                        parties.add(party);

                        Timestamp signedOn = rs.getTimestamp("signed_on");

                        party.partyId = rs.getInt("id");
                        party.contractId = rs.getInt("contract_id");
                        party.accountId = rs.getInt("account_id");
                        party.partyLabel = rs.getString("party_label");
                        party.validUntil = rs.getTimestamp("valid_until").getTime();
                        party.signature = rs.getBytes("signature");
                        party.signedOn = signedOn == null? null: signedOn.getTime();
                    }
                    return parties;
                }
        );
    }

    private void acceptDeal(int contractId, int accountId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            config.getQuery("accept_deal"),
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, contractId);
                    ps.setInt(2, accountId);
                    ps.setString(3, String.format("contract #%d accepted", contractId));
                    return ps;
                },
                keyHolder
        );
        if(rowsUpdated == 0) {
            throw new NotFoundException("contract doesn't exist, deleted or is already signed");
        }
    }

    public int signProposal(int contractId, int accountId) {
        return jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("sign_proposal"));
                    ps.setBytes(1, "Atticus-2".getBytes()); // TODO: calculate real signature
                    ps.setInt(2, contractId);
                    ps.setInt(3, accountId);
                    return ps;
                }
        );
    }

}
