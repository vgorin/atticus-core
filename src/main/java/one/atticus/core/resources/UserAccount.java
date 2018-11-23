package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserAccount {
    @XmlElement(name = "account_id")
    public int accountId;

    @XmlElement
    public String email;

    @XmlElement
    public String username;

    @XmlElement
    public String password;

    @XmlElement(name = "legal_name")
    public String legalName;

    @XmlElement(name = "langage_code")
    public String languageCode;

    @XmlElement(name = "country_code")
    public String countryCode;

    @XmlElement
    public String timezone;
}
