package one.atticus.core.dao;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.UserAccount;
import one.atticus.core.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author vgorin
 *         file created on 12/13/18 1:40 PM
 */


@Service
public class AccountDAO {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbc;
    private final AppConfig queries;

    @Autowired
    public AccountDAO(JdbcTemplate jdbc, AppConfig queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    public int create(UserAccount account) {
        byte[] passwordHash = PasswordUtil.passwordHash(account.password);

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
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public UserAccount retrieve(int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("get_account"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                AccountDAO::getUserAccount
        );
    }

    public int update(UserAccount account) {
        return jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("update_account"));
                    ps.setString(1, account.email);
                    ps.setString(2, account.username);
                    ps.setBytes(3, PasswordUtil.passwordHash(account.password));
                    ps.setString(4, account.legalName);
                    ps.setString(5, account.languageCode);
                    ps.setString(6, account.countryCode);
                    ps.setString(7, account.timezone);
                    ps.setInt(8, account.accountId);
                    return ps;
                }
        );
    }

    public int delete(int accountId) {
        return jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("delete_account"));
                    ps.setInt(1, accountId);
                    return ps;
                }
        );
    }

    public List<UserAccount> list() {
        return jdbc.query(
                c -> c.prepareStatement(queries.getQuery("list_accounts")),
                AccountDAO::getUserAccounts
        );
    }

    public List<UserAccount> search(String prefix) {
        String query = String.format("%s%%", prefix);

        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("search_accounts"));
                    ps.setString(1, query);
                    ps.setString(2, query);
                    return ps;
                },
                AccountDAO::getUserAccounts
        );
    }

    private static List<UserAccount> getUserAccounts(ResultSet rs) throws SQLException {
        List<UserAccount> result = new LinkedList<>();
        UserAccount account;
        while((account = getUserAccount(rs)) != null) {
            result.add(account);
        }
        return result;
    }

    private static UserAccount getUserAccount(ResultSet rs) throws SQLException {
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
