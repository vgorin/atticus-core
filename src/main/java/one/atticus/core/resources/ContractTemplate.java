package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author vgorin
 *         file created on 12/6/18 2:18 PM
 */


@XmlRootElement
public class ContractTemplate extends AbstractJson {
    @XmlElement(name = "template_id")
    public Integer templateId;

    @XmlElement(name = "account_id")
    public Integer accountId;

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

    @XmlElement
    public Long modified;

    @XmlElement
    public Long created;

    @XmlElement
    public Long updated;
}
