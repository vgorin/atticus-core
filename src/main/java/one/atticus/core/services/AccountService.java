package one.atticus.core.services;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.UserAccount;
import one.atticus.core.util.PasswordUtil;
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *         file created on 12/6/18 1:16 PM
 */


@Service
@Path("/account")
public class AccountService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbc;
    private final AppConfig queries;

    private final ContractTemplateService contractTemplateService;
    private final ContractService contractService;
    private final DealService dealService;

    @Autowired
    public AccountService(
            JdbcTemplate jdbc,
            AppConfig queries,
            ContractTemplateService contractTemplateService,
            ContractService contractService,
            DealService dealService
    ) {
        this.jdbc = jdbc;
        this.queries = queries;
        this.contractTemplateService = contractTemplateService;
        this.contractService = contractService;
        this.dealService = dealService;
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int create(@Context SecurityContext context, UserAccount account) {
        log.trace("creating account: {}", account);

        byte[] passwordHash = PasswordUtil.passwordHash(account.password);

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        log.trace("connecting to database...");
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("create_account"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setString(1, account.email);
                        ps.setString(2, account.username);
                        ps.setBytes(3, passwordHash);
                        ps.setString(4, account.legalName);
                        ps.setString(5, account.languageCode);
                        ps.setString(6, account.countryCode);
                        ps.setString(7, account.timezone);

                        log.trace("executing {}", queries.getQuery("create_account"));
                        return ps;
                    },
                    keyHolder
            );
            int accountId = Objects.requireNonNull(keyHolder.getKey()).intValue();
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
        UserAccount account = jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("get_account"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                this::getUserAccount
        );
        if(account == null) {
            throw new NotFoundException();
        }
        if(includeTemplates) {
            account.templates = contractTemplateService.listTemplates(context);
        }
        if(includeContracts) {
            account.contracts = contractService.listContracts(context, "all");
        }
        if(includeDeals) {
            account.deals = dealService.listDeals(context);
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
            int rowsUpdated = jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(queries.getQuery("update_account"));
                        ps.setString(1, account.email);
                        ps.setString(2, account.username);
                        ps.setBytes(3, PasswordUtil.passwordHash(account.password));
                        ps.setString(4, account.legalName);
                        ps.setString(5, account.languageCode);
                        ps.setString(6, account.countryCode);
                        ps.setString(7, account.timezone);
                        ps.setInt(8, accountId);
                        return ps;
                    }
            );
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
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("delete_account"));
                    ps.setInt(1, accountId);
                    return ps;
                }
        );
        if(rowsUpdated == 0) {
            throw new NotFoundException();
        }
    }

    @GET
    @Path("/auth")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserAccount auth(@Context SecurityContext context) {
        int accountId = authenticate(context);
        UserAccount account = jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("get_account"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                this::getUserAccount
        );
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

        if(!context.isUserInRole("admin")) {
            throw new ForbiddenException();
        }

        return jdbc.query(
                c -> c.prepareStatement(queries.getQuery("list_accounts")),
                this::getUserAccounts
        );
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<UserAccount> searchAccounts(@Context SecurityContext context, @QueryParam("q") String prefix) {
        authenticate(context);

        String query = String.format("%s%%", prefix);

        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("search_accounts"));
                    ps.setString(1, query);
                    ps.setString(2, query);
                    return ps;
                },
                this::getUserAccounts
        );
    }

    private List<UserAccount> getUserAccounts(ResultSet rs) throws SQLException {
        List<UserAccount> result = new LinkedList<>();
        UserAccount account;
        while((account = getUserAccount(rs)) != null) {
            result.add(account);
        }
        return result;
    }

    private UserAccount getUserAccount(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }

        Timestamp updated = rs.getTimestamp("rec_updated");

        UserAccount userAccount = new UserAccount();
        userAccount.accountId = rs.getInt("id");
        userAccount.email = rs.getString("email");
        userAccount.username = rs.getString("username");
        userAccount.legalName = rs.getString("legal_name");
        userAccount.languageCode = rs.getString("language_code");
        userAccount.countryCode = rs.getString("country_code");
        userAccount.timezone = rs.getString("timezone_tz");
        userAccount.created = rs.getTimestamp("rec_created").getTime();
        userAccount.updated = updated == null? null: updated.getTime();

        return userAccount;
    }

}
