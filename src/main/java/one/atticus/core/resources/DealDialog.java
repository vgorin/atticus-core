package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DealDialog {
    @XmlElement(name = "dialog_id")
    public Integer dialogId;

    @XmlElement(name = "deal_id")
    public Integer dealId;

    @XmlElement(name = "account_id")
    public Integer accountId;

    @XmlElement(name = "seq_num")
    public Integer sequenceNum;

    @XmlElement
    public String message;

    @XmlElement
    public byte[] attachment;

    @XmlElement(name = "contract_id")
    public Integer contractId;
}
