package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author vgorin
 *         file created on 12/6/18 2:18 PM
 */


@XmlRootElement
public class Contract extends AbstractJson {
    @XmlElement(name = "contract_id")
    public Integer contractId;

    @XmlElement(name = "account_id")
    public Integer accountId;

    @XmlElement(name = "template_id")
    public Integer templateId;

    @XmlElement
    public String memo;

    @XmlElement
    public String body;

    @XmlElement
    public Long proposed;

    @XmlElement
    public Long deleted;

    @XmlElement
    public Long modified;

    @XmlElement
    public Long created;

    @XmlElement
    public Long updated;
}
