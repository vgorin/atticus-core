package one.atticus.core.dao;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.ContractTemplate;
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
 *         file created on 12/13/18 5:57 PM
 */


@Service
public class ContractTemplateDAO {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbc;
    private final AppConfig queries;

    @Autowired
    public ContractTemplateDAO(JdbcTemplate jdbc, AppConfig queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    public int create(ContractTemplate template) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                c -> {
                    PreparedStatement ps = c.prepareStatement(
                            queries.getQuery("create_contract_template"),
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

    public ContractTemplate retrieve(int templateId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("get_contract_template"));
                    ps.setInt(1, templateId);
                    return ps;
                },
                ContractTemplateDAO::getContractTemplate
        );
    }

    public ContractTemplate retrieve(int templateId, int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("get_contract_template_of"));
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
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("update_contract_template"));
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
            PreparedStatement ps = c.prepareStatement(queries.getQuery("delete_contract_template"));
            ps.setInt(1, templateId);
            ps.setInt(2, accountId);
            return ps;
        });
    }

    public List<ContractTemplate> list(int accountId) {
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("list_contract_templates"));
                    ps.setInt(1, accountId);
                    return ps;
                },
                ContractTemplateDAO::getContractTemplates
        );
    }

    public int release(int templateId, int accountId) {
        return jdbc.update(c -> {
            PreparedStatement ps = c.prepareStatement(queries.getQuery("release_contract_template"));
            ps.setInt(1, templateId);
            ps.setInt(2, accountId);
            return ps;
        });
    }

    public int publish(int templateId, int accountId) {
        return jdbc.update(c -> {
            PreparedStatement ps = c.prepareStatement(queries.getQuery("publish_contract_template"));
            ps.setInt(1, templateId);
            ps.setInt(2, accountId);
            return ps;
        });
    }

    public List<ContractTemplate> search(int accountId, String prefix) {
        String query = String.format("%%%s%%", prefix);
        return jdbc.query(
                c -> {
                    PreparedStatement ps = c.prepareStatement(queries.getQuery("search_contract_templates"));
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
