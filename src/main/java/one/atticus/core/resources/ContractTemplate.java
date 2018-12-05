package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ContractTemplate extends AbstractJson {
    @XmlElement(name = "template_id")
    public int templateId;

    @XmlElement(name = "account_id")
    public int accountId;

    @XmlElement
    public String title;

    @XmlElement
    public String version;

    @XmlElement
    public String body;

    @XmlElement
    public Long versioned;

    @XmlElement
    public Long deleted;

    @XmlElement
    public Long published;
}
