package one.atticus.core.services;

import one.atticus.core.resources.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

@Component
@Path("/account")
public class AccountService {
    private final JdbcTemplate jdbc;

    @Autowired
    public AccountService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int create(UserAccount account) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz) VALUES(?, ? ,?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setString(1, account.email);
                    ps.setString(2, account.username);
                    ps.setBytes(3, new byte[0]);
                    ps.setString(4, account.legalName);
                    ps.setString(5, account.languageCode);
                    ps.setString(6, account.countryCode);
                    ps.setString(7, account.timezone);
                    return ps;
                },
                keyHolder
        );
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    @GET
    @Path("/{accountId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserAccount retrieve(@PathParam("accountId") int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "SELECT * FROM user_acc WHERE account_id = ?"
                    );
                    ps.setInt(1, accountId);
                    return ps;
                },
                rs -> {
                    if(!rs.next()) {
                        return null;
                    }
                    UserAccount userAccount = new UserAccount();
                    userAccount.accountId = rs.getInt("account_id");
                    userAccount.email = rs.getString("email");
                    userAccount.username = rs.getString("username");
                    userAccount.legalName = rs.getString("legal_name");
                    userAccount.languageCode = rs.getString("language_code");
                    userAccount.countryCode = rs.getString("country_code");
                    userAccount.timezone = rs.getString("timezone_tz");
                    return userAccount;
                }

        );
    }

    @PUT
    @Path("/{accountId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(@PathParam("accountId") int accountId, UserAccount account) {
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE user_acc SET account_id = ?, email = ?, username = ?, password = ?, legal_name = ?, language_code = ?, country_code = ?, timezone_tz = ? WHERE account_id = ?",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, account.accountId);
                    ps.setString(2, account.email);
                    ps.setString(3, account.username);
                    ps.setBytes(4, new byte[0]);
                    ps.setString(5, account.legalName);
                    ps.setString(6, account.languageCode);
                    ps.setString(7, account.countryCode);
                    ps.setString(8, account.timezone);
                    ps.setInt(9, accountId);
                    return ps;
                }
        );
    }

    @DELETE
    @Path("/{accountId}")
    public void delete(@PathParam("accountId") int accountId) {
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            "DELETE FROM user_acc WHERE account_id = ?"
                    );
                    ps.setInt(1, accountId);
                    return ps;
                }
        );
    }

}
