package one.atticus.core.services;

import one.atticus.core.resources.Deal;
import one.atticus.core.resources.DealDialog;
import one.atticus.core.resources.Party;
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

@Component
@Path("/deal")
public class DealService {
    private final JdbcTemplate jdbc;

    @Autowired
    public DealService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Path("/")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Deal create(Deal deal) {
        if(deal.dialog == null || deal.dialog.size() == 0) {
           throw new BadRequestException("deal must begin with a dialog (deal.dialog cannot be empty)");
        }

        DealDialog dialog0 = deal.dialog.get(0);

        if(dialog0.contractId == null) {
            throw new BadRequestException("deal must begin with contract proposal (deal.dialog[0].contract_id must be specified");
        }

        if(deal.parties == null || deal.parties.size() < 2) {
            throw new BadRequestException("deal must contain at least two parties");
        }

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO deal(account_id, title) VALUES(?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, deal.accountId);
                        ps.setString(2, deal.title);
                        return ps;
                    },
                    keyHolder
            );
            deal.dealId = Objects.requireNonNull(keyHolder.getKey()).intValue();

            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO deal_dialog(deal_id, account_id, seq_num, message, attachment, contract_id) VALUES(?, ?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, deal.dealId);
                        ps.setInt(2, deal.accountId);
                        ps.setInt(3, 0);
                        ps.setString(4, dialog0.message);
                        ps.setBytes(5, dialog0.attachment);
                        ps.setInt(6, dialog0.contractId);
                        return ps;
                    },
                    keyHolder
            );
            dialog0.dialogId = Objects.requireNonNull(keyHolder.getKey()).intValue();
            dialog0.sequenceNum = 0;

            for(Party party: deal.parties) {
                jdbc.update(
                        c -> {
                            PreparedStatement ps = c.prepareStatement(
                                    "INSERT INTO contract_party(contract_id, account_id, party_label, valid_until) VALUES(?, ?, ?, ?)",
                                    Statement.RETURN_GENERATED_KEYS
                            );
                            ps.setInt(1, party.contractId);
                            ps.setInt(2, party.accountId);
                            ps.setString(3, "");
                            ps.setDate(4, new Date(1L));
                            return ps;
                        },
                        keyHolder
                );
                party.partyId = Objects.requireNonNull(keyHolder.getKey()).intValue();
            }

            return deal;
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

}
