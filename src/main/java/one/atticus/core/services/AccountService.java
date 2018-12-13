package one.atticus.core.services;

import one.atticus.core.dao.AccountDAO;
import one.atticus.core.dao.ContractDAO;
import one.atticus.core.dao.ContractTemplateDAO;
import one.atticus.core.dao.DealDAO;
import one.atticus.core.resources.UserAccount;
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *         file created on 12/6/18 1:16 PM
 */


@Service
@Path("/account")
public class AccountService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AccountDAO accountDAO;
    private final ContractDAO contractDAO;
    private final ContractTemplateDAO templateDAO;
    private final DealDAO dealDAO;

    @Autowired
    public AccountService(
            AccountDAO accountDAO,
            ContractDAO contractDAO,
            ContractTemplateDAO templateDAO,
            DealDAO dealDAO
    ) {
        this.accountDAO = accountDAO;
        this.contractDAO = contractDAO;
        this.templateDAO = templateDAO;
        this.dealDAO = dealDAO;
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int create(@Context SecurityContext context, UserAccount account) {
        log.trace("creating account: {}", account);

        try {
            int accountId = accountDAO.create(account);
            account.accountId = accountId;

            log.debug("account successfully created: {}", account);

            // TODO: replace with 201 Created
            return accountId;
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @GET
    @Path("/{accountId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserAccount retrieve(
            @Context SecurityContext context,
            @PathParam("accountId") int accountId,
            @QueryParam("includeTemplates") boolean includeTemplates,
            @QueryParam("includeContracts") boolean includeContracts,
            @QueryParam("includeDeals") boolean includeDeals
    ) {
        authenticate(context);
        UserAccount account = accountDAO.retrieve(accountId);
        if(account == null) {
            throw new NotFoundException();
        }
        if(includeTemplates) {
            account.templates = templateDAO.list(accountId);
        }
        if(includeContracts) {
            account.contracts = contractDAO.list(accountId);
        }
        if(includeDeals) {
            account.deals = dealDAO.listDeals(accountId);
        }
        return account;
    }

    @PUT
    @Path("/{accountId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(@Context SecurityContext context, @PathParam("accountId") int accountId, UserAccount account) {
        int authAccountId = authenticate(context);

        if(accountId != authAccountId) {
            throw new ForbiddenException();
        }

        try {
            account.accountId = accountId;
            int rowsUpdated = accountDAO.update(account);
            if(rowsUpdated == 0) {
                throw new NotFoundException();
            }
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @DELETE
    @Path("/{accountId}")
    public void delete(@Context SecurityContext context, @PathParam("accountId") int accountId) {
        int authAccountId = authenticate(context);
        if(accountId != authAccountId) {
            throw new ForbiddenException();
        }
        int rowsUpdated = accountDAO.delete(accountId);
        if(rowsUpdated == 0) {
            throw new NotFoundException();
        }
    }

    @GET
    @Path("/auth")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserAccount auth(@Context SecurityContext context) {
        int accountId = authenticate(context);
        UserAccount account = accountDAO.retrieve(accountId);
        if(account == null) {
            throw new NotAuthorizedException("please specify username:password in request auth header");
        }
        return account;
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<UserAccount> listAccounts(@Context SecurityContext context) {
        authenticate(context);

        return accountDAO.list();
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<UserAccount> searchAccounts(@Context SecurityContext context, @QueryParam("q") String prefix) {
        authenticate(context);

        return accountDAO.search(prefix);
    }

}
