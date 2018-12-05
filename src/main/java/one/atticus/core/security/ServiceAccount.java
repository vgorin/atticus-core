package one.atticus.core.security;

import one.atticus.core.util.PasswordUtil;
import org.springframework.boot.autoconfigure.security.SecurityProperties;

class ServiceAccount extends SecurityProperties.User {
    private final int accountId;
    private final String email;
    private final String username;
    private final byte[] passwordHash;
    private final String role;

    ServiceAccount(int accountId, String email, String username, byte[] passwordHash, String role) {
        this.accountId = accountId;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    int getAccountId() {
        return accountId;
    }

    String getEmail() {
        return email;
    }

    String getUsername() {
        return username;
    }

    String getRole() {
        return role;
    }

    boolean verify(String password) {
        return PasswordUtil.verifyPasswordHash(passwordHash, password);
    }

    String getAccount() {
        return String.valueOf(accountId);
    }
}
