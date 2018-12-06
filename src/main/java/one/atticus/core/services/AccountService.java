package one.atticus.core.services;

import one.atticus.core.resources.UserAccount;
import one.atticus.core.util.PasswordUtil;
import org.codehaus.plexus.util.ExceptionUtils;
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

@Service
@Path("/account")
public class AccountService {
    private final JdbcTemplate jdbc;
    private final Queries queries;

    @Autowired
    public AccountService(JdbcTemplate jdbc, Queries queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserAccount create(@Context SecurityContext context, UserAccount account) {
        try {
            byte[] passwordHash = PasswordUtil.passwordHash(account.password);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.get("create_account"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setString(1, account.email);
                        ps.setString(2, account.username);
                        ps.setBytes(3, passwordHash);
                        ps.setString(4, account.legalName);
                        ps.setString(5, account.languageCode);
                        ps.setString(6, account.countryCode);
                        ps.setString(7, account.timezone);
                        return ps;
                    },
                    keyHolder
            );
            account.accountId = Objects.requireNonNull(keyHolder.getKey()).intValue();
            account.passwordHash = passwordHash;
            return account;
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @GET
    @Path("/{accountId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserAccount retrieve(@Context SecurityContext context, @PathParam("accountId") int accountId) {
        int authAccountId = authenticate(context);
        if(accountId != authAccountId) {
            throw new ForbiddenException();
        }
        UserAccount account = jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.get("get_account"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                this::getUserAccount
        );
        if(account == null) {
            throw new NotFoundException();
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
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.get("update_account"));
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

    @DELETE
    @Path("/{accountId}")
    public void delete(@Context SecurityContext context, @PathParam("accountId") int accountId) {
        int authAccountId = authenticate(context);
        if(accountId != authAccountId) {
            throw new ForbiddenException();
        }
        int rowsUpdated = jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.get("delete_account"));
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
                    PreparedStatement ps = c.prepareStatement(queries.get("get_account"));
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
                c -> c.prepareStatement(queries.get("list_accounts")),
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

        Date updated = rs.getDate("rec_updated");

        UserAccount userAccount = new UserAccount();
        userAccount.accountId = rs.getInt("id");
        userAccount.email = rs.getString("email");
        userAccount.username = rs.getString("username");
        userAccount.legalName = rs.getString("legal_name");
        userAccount.languageCode = rs.getString("language_code");
        userAccount.countryCode = rs.getString("country_code");
        userAccount.timezone = rs.getString("timezone_tz");
        userAccount.created = rs.getDate("rec_created").getTime();
        userAccount.updated = updated == null? null: updated.getTime();

        return userAccount;
    }

}
