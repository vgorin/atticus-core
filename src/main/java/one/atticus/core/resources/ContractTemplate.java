package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
public class ContractTemplate {
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

}
