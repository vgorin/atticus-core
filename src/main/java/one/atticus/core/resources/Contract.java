package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Contract {
    @XmlElement(name = "contract_id")
    public int contractId;

    @XmlElement(name = "account_id")
    public int accountId;

    @XmlElement
    public String memo;

    @XmlElement
    public String body;
}
