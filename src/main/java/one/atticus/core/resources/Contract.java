package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Contract extends AbstractJson {
    @XmlElement
    public int contractId;

    @XmlElement
    public int accountId;

    @XmlElement
    public int parties;

    @XmlElement
    public String header;

    @XmlElement
    public String body;

    @XmlElement
    public boolean draft;
}
