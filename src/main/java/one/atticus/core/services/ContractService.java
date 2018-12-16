package one.atticus.core.services;

import one.atticus.core.dao.ContractDAO;
import one.atticus.core.resources.Contract;
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
 * file created on 12/6/18 2:08 PM
 */


@Service
@Path("/contract")
public class ContractService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ContractDAO contractDAO;

    @Autowired
    public ContractService(ContractDAO contractDAO) {
        this.contractDAO = contractDAO;
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int create(@Context SecurityContext context, Contract contract) {
        int accountId = authenticate(context);

        try {
            contract.accountId = accountId;
            int contractId = contractDAO.create(contract);
            contract.contractId = contractId;

            log.debug("contract successfully created: {}", contract);
            // TODO: replace with 201 Created
            return contractId;
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @GET
    @Path("/{contractId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Contract retrieve(@Context SecurityContext context, @PathParam("contractId") int contractId) {
        authenticate(context);

        // TODO: verify this account has access to the contract (is part of the same deal)

        Contract contract = contractDAO.retrieve(contractId);
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
            contract.contractId = contractId;
            contract.accountId = accountId;

            int rowsUpdated = contractDAO.update(contract);

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
        int rowsUpdated = contractDAO.delete(contractId, accountId);
        if(rowsUpdated == 0) {
            throw new NotFoundException("contract doesn't exist or already deleted");
        }
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Contract> listContracts(@Context SecurityContext context, @QueryParam("type") String type) {
        int accountId = authenticate(context);

        if("draft".equals(type)) {
            return contractDAO.listDrafts(accountId);
        }
        if("proposed".equals(type)) {
            return contractDAO.listProposed(accountId);
        }
        return contractDAO.list(accountId);
    }

}
