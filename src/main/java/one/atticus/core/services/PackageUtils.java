package one.atticus.core.services;

import one.atticus.core.resources.Contract;
import one.atticus.core.resources.Deal;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author vgorin
 * file created on 12/6/18 1:52 PM
 */


class PackageUtils {
    static int authenticate(SecurityContext context) {
        if(context == null || context.getAuthenticationScheme() == null || context.getUserPrincipal() == null) {
            throw new NotAuthorizedException("authentication required");
        }
        if(!context.isSecure()) {
            // throw new ForbiddenException("secure protocol (https) required");
        }

        final Principal principal = context.getUserPrincipal();

        return Integer.valueOf(principal.getName());
    }

    static PreparedStatement prepareStatement(Connection c, String query, int ...params) throws SQLException {
        PreparedStatement ps = c.prepareStatement(query);
        for(int i = 0; i < params.length; i++) {
            ps.setInt(i + 1, params[i]);
        }
        return ps;
    }

    static List<Contract> getContracts(ResultSet rs) throws SQLException {
        List<Contract> contracts = new LinkedList<>();
        Contract contract;
        while((contract = getContract(rs)) != null) {
            contracts.add(contract);
        }
        return contracts;
    }

    static Contract getContract(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }

        Timestamp proposed = rs.getTimestamp("proposed");
        Timestamp deleted = rs.getTimestamp("deleted");
        Timestamp modified = rs.getTimestamp("modified");
        Timestamp updated = rs.getTimestamp("rec_updated");

        Contract template = new Contract();
        template.contractId = rs.getInt("id");
        template.accountId = rs.getInt("account_id");
        template.memo = rs.getString("memo");
        template.body = rs.getString("body");
        template.proposed = proposed == null? null: proposed.getTime();
        template.deleted = deleted == null? null: deleted.getTime();
        template.modified = modified == null? null: modified.getTime();
        template.updated = updated == null? null: updated.getTime();
        template.created = rs.getTimestamp("rec_created").getTime();

        return template;
    }

    static List<Deal> getDeals(ResultSet rs) throws SQLException {
        List<Deal> result = new LinkedList<>();
        Deal deal;
        while((deal = getDeal(rs)) != null) {
            result.add(deal);
        }
        return result;
    }

    static Deal getDeal(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }
        Deal deal = new Deal();
        deal.dealId = rs.getInt("id");
        deal.contractId = rs.getInt("contract_id");
        deal.title = rs.getString("title");
        deal.created = rs.getTimestamp("rec_created").getTime();
        return deal;
    }


}
