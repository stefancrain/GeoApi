//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.07.29 at 04:21:09 PM EDT 
//


package generated.bing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.microsoft.com/search/local/ws/rest/v1}EstimatedTotal"/>
 *         &lt;element ref="{http://schemas.microsoft.com/search/local/ws/rest/v1}Resources"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "estimatedTotal",
    "resources"
})
@XmlRootElement(name = "ResourceSet")
public class ResourceSet {

    @XmlElement(name = "EstimatedTotal", required = true)
    protected int estimatedTotal;
    @XmlElement(name = "Resources", required = true)
    protected Resources resources;

    /**
     * Gets the value of the estimatedTotal property.
     * 
     * @return
     *     possible object is
     *     {@link int }
     *     
     */
    public int getEstimatedTotal() {
        return estimatedTotal;
    }

    /**
     * Sets the value of the estimatedTotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link int }
     *     
     */
    public void setEstimatedTotal(int value) {
        this.estimatedTotal = value;
    }

    /**
     * Gets the value of the resources property.
     * 
     * @return
     *     possible object is
     *     {@link Resources }
     *     
     */
    public Resources getResources() {
        return resources;
    }

    /**
     * Sets the value of the resources property.
     * 
     * @param value
     *     allowed object is
     *     {@link Resources }
     *     
     */
    public void setResources(Resources value) {
        this.resources = value;
    }

}