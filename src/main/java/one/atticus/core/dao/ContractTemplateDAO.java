package one.atticus.core.dao;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.ContractTemplate;
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
 *         file created on 12/13/18 5:57 PM
 */


@Service
public class ContractTemplateDAO {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbc;
    private final AppConfig config;

    @Autowired
    public ContractTemplateDAO(JdbcTemplate jdbc, AppConfig config) {
        this.jdbc = jdbc;
        this.config = config;
    }

    public int create(ContractTemplate template) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            config.getQuery("create_contract_template"),
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, template.accountId);
                    ps.setString(2, template.title);
                    ps.setString(3, template.version);
                    ps.setString(4, template.body);
                    ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                    return ps;
                },
                keyHolder
        );
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public int clone(int templateId, int accountId) {
        List<SqlParameter> parameters = new LinkedList<>();
        parameters.add(new SqlParameter(Types.INTEGER));
        parameters.add(new SqlParameter(Types.INTEGER));
        parameters.add(new SqlOutParameter("row_count", Types.INTEGER));
        parameters.add(new SqlOutParameter("insert_id", Types.INTEGER));

        Map<String, Object> result = jdbc.call(
                c -> {
                    CallableStatement cs = c.prepareCall(
                            config.getQuery("clone_template")
                    );
                    cs.setInt(1, templateId);
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

    public ContractTemplate retrieve(int templateId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("get_contract_template"));
                    ps.setInt(1, templateId);
                    return ps;
                },
                ContractTemplateDAO::getContractTemplate
        );
    }

    public ContractTemplate retrieve(int templateId, int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("get_contract_template_of"));
                    ps.setInt(1, templateId);
                    ps.setInt(2, accountId);
                    return ps;
                },
                ContractTemplateDAO::getContractTemplate
        );
    }

    public int update(ContractTemplate template) {
        return jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("update_contract_template"));
                    ps.setString(1, template.title);
                    ps.setString(2, template.version);
                    ps.setString(3, template.body);
                    ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    ps.setInt(5, template.templateId);
                    ps.setInt(6, template.accountId);
                    return ps;
                }
        );
    }

    public int delete(int templateId, int accountId) {
        return jdbc.update(c -> {
            PreparedStatement ps = c.prepareStatement(config.getQuery("delete_contract_template"));
            ps.setInt(1, templateId);
            ps.setInt(2, accountId);
            return ps;
        });
    }

    public List<ContractTemplate> list(int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("list_contract_templates"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                ContractTemplateDAO::getContractTemplates
        );
    }

    public int release(int templateId, int accountId) {
        return jdbc.update(c -> {
            PreparedStatement ps = c.prepareStatement(config.getQuery("release_contract_template"));
            ps.setInt(1, templateId);
            ps.setInt(2, accountId);
            return ps;
        });
    }

    public int publish(int templateId, int accountId) {
        return jdbc.update(c -> {
            PreparedStatement ps = c.prepareStatement(config.getQuery("publish_contract_template"));
            ps.setInt(1, templateId);
            ps.setInt(2, accountId);
            return ps;
        });
    }

    public List<ContractTemplate> search(int accountId, String prefix) {
        String query = String.format("%%%s%%", prefix);
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(config.getQuery("search_contract_templates"));
                    ps.setInt(1, accountId);
                    ps.setString(2, query);
                    return ps;
                },
                ContractTemplateDAO::getContractTemplates
        );

    }

    private static List<ContractTemplate> getContractTemplates(ResultSet rs) throws SQLException {
        List<ContractTemplate> result = new LinkedList<>();
        ContractTemplate template;
        while((template = getContractTemplate(rs)) != null) {
            result.add(template);
        }
        return result;
    }

    private static ContractTemplate getContractTemplate(ResultSet rs) throws SQLException {
        if(!rs.next()) {
            return null;
        }
        Timestamp versioned = rs.getTimestamp("versioned");
        Timestamp deleted = rs.getTimestamp("deleted");
        Timestamp published = rs.getTimestamp("published");
        Timestamp updated = rs.getTimestamp("rec_updated");

        ContractTemplate template = new ContractTemplate();
        template.templateId = rs.getInt("id");
        template.accountId = rs.getInt("account_id");
        template.title = rs.getString("title");
        template.version = rs.getString("version");
        template.body = rs.getString("body");
        template.versioned = versioned == null? null: versioned.getTime();
        template.deleted = deleted == null? null: deleted.getTime();
        template.published = published == null? null: published.getTime();
        template.created = rs.getTimestamp("rec_created").getTime();
        template.updated = updated == null? null: updated.getTime();

        return template;
    }


}
