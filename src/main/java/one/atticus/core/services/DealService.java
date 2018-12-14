package one.atticus.core.services;

import one.atticus.core.config.AppConfig;
import one.atticus.core.dao.DealDAO;
import one.atticus.core.resources.Deal;
import org.codehaus.plexus.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static one.atticus.core.services.PackageUtils.authenticate;

/**
 * @author vgorin
 *         file created on 12/6/18 8:16 PM
 */


@Service
@Path("/deal")
public class DealService {
    private final DealDAO dealDAO;
    private final AppConfig config;

    @Autowired
    public DealService(DealDAO dealDAO, AppConfig config) {
        this.dealDAO = dealDAO;
        this.config = config;
    }

    @POST
    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int proposeDeal(
            @Context SecurityContext context,
            @QueryParam("contract_id") int contractId,
            @QueryParam("to_account_id") int toAccountId,
            @QueryParam("deal_title") String dealTitle
    ) {
        int accountId = authenticate(context);

        try {
            return dealDAO.submitNewDeal(
                    contractId,
                    accountId,
                    toAccountId,
                    dealTitle,
                    config.getInt("proposal.default_validity_period")
            );
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @POST
    @Path("/{deal_id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int counterPropose(
            @Context SecurityContext context,
            @PathParam("deal_id") int dealId,
            @QueryParam("contract_id") int contractId
    ) {
        int accountId = authenticate(context);

        try {
            return dealDAO.postCounterProposal(
                    dealId,
                    accountId,
                    contractId,
                    config.getInt("proposal.default_validity_period")
            );
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @GET
    @Path("/submitted-proposals")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Deal> listSubmittedProposals(@Context SecurityContext context) {
        int accountId = authenticate(context);
        return dealDAO.listSubmittedProposals(accountId);
    }

    @GET
    @Path("/received-proposals")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Deal> listReceivedProposals(@Context SecurityContext context) {
        int accountId = authenticate(context);
        return dealDAO.listReceivedProposals(accountId);
    }

    @GET
    @Path("/active-deals")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Deal> listActiveDeals(@Context SecurityContext context) {
        int accountId = authenticate(context);
        return dealDAO.listActiveDeals(accountId);
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Deal> listDeals(@Context SecurityContext context) {
        int accountId = authenticate(context);
        return dealDAO.listDeals(accountId);
    }

    @PUT
    @Path("/accept/{contractId}")
    public void acceptDealAndSignProposal(
            @Context SecurityContext context,
            @PathParam("contractId") int contractId
    ) {
        int accountId = authenticate(context);
        int rowsUpdated = dealDAO.signProposal(contractId, accountId);

        if(rowsUpdated == 0) {
            throw new NotFoundException("contract doesn't exist, deleted or is already signed");
        }
    }


}
