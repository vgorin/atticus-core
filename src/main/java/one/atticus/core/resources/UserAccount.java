package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserAccount extends AbstractJson {
    @XmlElement(name = "account_id")
    public int accountId;

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
    public long created;

    @XmlElement
    public Long updated;
}
