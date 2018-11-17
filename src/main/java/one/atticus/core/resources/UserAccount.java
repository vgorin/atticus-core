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

    @XmlElement(name = "legal_name")
    public String legalName;

    @XmlElement
    public String language;

    @XmlElement
    public String country;

    @XmlElement
    public String timezone;
}
