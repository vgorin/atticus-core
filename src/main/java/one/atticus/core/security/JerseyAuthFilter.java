package one.atticus.core.security;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

@Service
@Provider
@PreMatching
public class JerseyAuthFilter implements ContainerRequestFilter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbc;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");

    @Autowired
    public JerseyAuthFilter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        log.trace("{} {} {}{}", request.getProtocol(), request.getMethod(), request.getPathInfo(), request.getQueryString());

        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            ServiceAccount userAccount = loadUserAccount(authHeader);

            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return userAccount::getAccount;
                }

                @Override
                public boolean isUserInRole(String role) {
                    return role != null && role.equals(userAccount.getRole());
                }

                @Override
                public boolean isSecure() {
                    return "https".equalsIgnoreCase(requestContext.getUriInfo().getRequestUri().getScheme());
                }

                @Override
                public String getAuthenticationScheme() {
                    return SecurityContext.BASIC_AUTH;
                }
            });
        }
        catch(AuthenticationException e) {
            log.debug("request authentication failed", e);
        }

        log.trace("{} {} {}{} - auth filter complete", request.getProtocol(), request.getMethod(), request.getPathInfo(), request.getQueryString());
    }

    private ServiceAccount loadUserAccount(String authHeader) throws AuthenticationException {
        if(authHeader != null) {
            StringTokenizer tokenizer = new StringTokenizer(authHeader);
            if(tokenizer.hasMoreTokens()) {
                String authMethod = tokenizer.nextToken();
                if("Basic".equals(authMethod)) {
                    if(tokenizer.hasMoreTokens()) {
                        try {
                            String credentials = new String(Base64.getDecoder().decode(tokenizer.nextToken()));
                            int colonIndex = credentials.indexOf(':');
                            if(colonIndex != -1) {
                                String username = StringUtils.substring(credentials, 0, colonIndex);
                                String password = StringUtils.substring(credentials, colonIndex + 1);

                                ServiceAccount account;

                                if(EMAIL_PATTERN.matcher(username).matches()) {
                                    account = jdbc.query(
                                            c -> {
                                                PreparedStatement ps = c.prepareStatement(
                                                        "SELECT * FROM user_account WHERE email = ?"
                                                );
                                                ps.setString(1, username);
                                                return ps;
                                            },
                                            this::getUserAccount
                                    );
                                }
                                else {
                                    account = jdbc.query(
                                            c -> {
                                                PreparedStatement ps = c.prepareStatement(
                                                        "SELECT * FROM user_account WHERE username = ?"
                                                );
                                                ps.setString(1, username);
                                                return ps;
                                            },
                                            this::getUserAccount
                                    );
                                }

                                // TODO: possible timing attack
                                if(account != null && account.verify(password)) {
                                    return account;
                                }

                                throw new AuthenticationException("invalid username/password");
                            }
                            throw new AuthenticationException("malformed auth header: empty password");
                        }
                        catch(IllegalArgumentException e) {
                            log.debug("base64 decoding failed", e);
                            throw new AuthenticationException("malformed auth header: base64 decoding failed");
                        }
                    }
                    log.debug("unsupported auth method: {}", authMethod);
                    throw new AuthenticationException("malformed auth header: empty credentials");
                }
                throw new AuthenticationException("auth method not supported");
            }
            throw new AuthenticationException("empty auth header");
        }
        throw new AuthenticationException("no auth header provided");
    }


    private ServiceAccount getUserAccount(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }
        return new ServiceAccount(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("username"),
                rs.getBytes("password"),
                rs.getString("role")
        );
    }
}
