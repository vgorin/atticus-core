package one.atticus.core.dao;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @author vgorin
 *         file created on 12/13/18 4:11 PM
 */


@Service
public class ContractDAO {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbc;
    private final AppConfig config;

    @Autowired
    public ContractDAO(JdbcTemplate jdbc, AppConfig config) {
        this.jdbc = jdbc;
        this.config = config;
    }

    public int create(Contract contract) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            config.getQuery("create_contract"),
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, contract.accountId);
                    ps.setString(2, contract.memo);
                    ps.setString(3, contract.body);
                    ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    return ps;
                },
                keyHolder
        );

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public int clone(int contractId, int accountId) {
        List<SqlParameter> parameters = new LinkedList<>();
        parameters.add(new SqlParameter(Types.INTEGER));
        parameters.add(new SqlParameter(Types.INTEGER));
        parameters.add(new SqlOutParameter("row_count", Types.INTEGER));
        parameters.add(new SqlOutParameter("insert_id", Types.INTEGER));

        Map<String, Object> result = jdbc.call(
                c -> {
                    CallableStatement cs = c.prepareCall(
                            config.getQuery("clone_contract")
                    );
                    cs.setInt(1, contractId);
                    cs.setInt(2, accountId);
                    cs.registerOutParameter(3, Types.INTEGER);
                    cs.registerOutParameter(4, Types.INTEGER);
                    return cs;
                },
                parameters
        );

        int rowCount = ((Long) result.get("row_count")).intValue();
        if(rowCount != 1) {
            throw new EmptyResultDataAccessException(1);
        }

        return ((Long) result.get("insert_id")).intValue();
    }

    public Contract retrieve(int contractId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("get_contract"));
                    ps.setInt(1, contractId);
                    return ps;
                },
                ContractDAO::getContract
        );
    }

    public Contract retrieve(int contractId, int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("get_contract_of"));
                    ps.setInt(1, contractId);
                    ps.setInt(2, accountId);
                    return ps;
                },
                ContractDAO::getContract
        );
    }

    public int update(Contract contract) {
        return jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("update_contract"));
                    ps.setString(1, contract.memo);
                    ps.setString(2, contract.body);
                    ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    ps.setInt(4, contract.contractId);
                    ps.setInt(5, contract.accountId);
                    return ps;
                }
        );
    }

    public int delete(int contractId, int accountId) {
        return jdbc.update(c -> {
            PreparedStatement ps = c.prepareStatement(config.getQuery("delete_contract"));
            ps.setInt(1, contractId);
            ps.setInt(2, accountId);
            return ps;
        });
    }

    public List<Contract> list(int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("list_contracts"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                ContractDAO::getContracts
        );
    }

    public List<Contract> listDrafts(int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("list_draft_contracts"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                ContractDAO::getContracts
        );
    }

    public List<Contract> listProposed(int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("list_proposed_contracts"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                ContractDAO::getContracts
        );
    }

    private static List<Contract> getContracts(ResultSet rs) throws SQLException {
        List<Contract> contracts = new LinkedList<>();
        Contract contract;
        while((contract = getContract(rs)) != null) {
            contracts.add(contract);
        }
        return contracts;
    }

    private static Contract getContract(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }

        Timestamp proposed = rs.getTimestamp("proposed");
        Timestamp deleted = rs.getTimestamp("deleted");
        Timestamp modified = rs.getTimestamp("modified");
        Timestamp updated = rs.getTimestamp("rec_updated");

        Contract contract = new Contract();
        contract.contractId = rs.getInt("id");
        contract.accountId = rs.getInt("account_id");
        contract.templateId = rs.getInt("template_id");
        contract.memo = rs.getString("memo");
        contract.body = rs.getString("body");
        contract.proposed = proposed == null? null: proposed.getTime();
        contract.deleted = deleted == null? null: deleted.getTime();
        contract.modified = modified == null? null: modified.getTime();
        contract.updated = updated == null? null: updated.getTime();
        contract.created = rs.getTimestamp("rec_created").getTime();

        return contract;
    }


}
