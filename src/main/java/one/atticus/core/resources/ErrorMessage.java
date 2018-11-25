package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorMessage {
    @XmlElement
    public int status;

    @XmlElement
    public Integer code;

    @XmlElement
    public String message;

    @XmlElement(name = "detailed_message")
    public String detailedMessage;

    @XmlElement
    public String link;
}
