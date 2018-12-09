package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author vgorin
 *         file created on 12/6/18 1:41 PM
 */


@XmlRootElement
public class UserAccount extends AbstractJson {
    @XmlElement(name = "account_id")
    public Integer accountId;

    @XmlElement
    public String email;

    @XmlElement
    public String username;

    @XmlElement
    public String password;

    @XmlElement
    public byte[] passwordHash;

    @XmlElement(name = "legal_name")
    public String legalName;

    @XmlElement(name = "language_code")
    public String languageCode;

    @XmlElement(name = "country_code")
    public String countryCode;

    @XmlElement
    public String timezone;

    @XmlElement
    public Long created;

    @XmlElement
    public Long updated;

    @XmlElement
    public Long deleted;

    @XmlElement
    public List<ContractTemplate> templates;

    @XmlElement
    public List<Contract> contracts;

    @XmlElement
    public List<Deal> deals;
}
