package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
public class Party {
    @XmlElement(name = "party_id")
    public int partyId;

    @XmlElement
    public UserAccount party;

    @XmlElement(name = "contract_id")
    public int contractId;

    @XmlElement(name = "account_id")
    public int accountId;

    @XmlElement(name = "party_label")
    public String partyLabel;

    @XmlElement(name = "valid_until")
    public Long validUntil;

    @XmlElement
    public byte[] signature;

    @XmlElement(name = "signed_on")
    public Long signedOn;
}
