/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author sdelucero
 */
@Data
@Entity
@Table(name = "LOB_DOCUMENT_ATTR", schema = "FS_FIA_LOB")
public class LobDocumentAttr implements Serializable {
    private static final long serialVersionUID = -6057423983917400367L;    
    
    @Id
    @Column(nullable = false, updatable=false, length = 34)
    private String cn;
    public void setCn(String cn) {
        this.cn = cn;
    }
    public String getCn() {
        return cn;
    }
    
    @Column(name = "ATTR_NAME", nullable = false, length = 80)
    private String attrName;
    
    @Column(name = "ATTR_VALUE", length = 2000)
    private String attrValue;
    
    
    @ManyToOne
    @JoinColumn(name = "DOC_CN")
    private LobDocument lobDocument;    
    
    public LobDocumentAttr() {
        super();
    }
    public LobDocumentAttr(boolean filter) {
        super();
        if (filter) {
            lobDocument = new LobDocument(true);
        }
    }
    public LobDocumentAttr(String cn, String name, String value) {
        super();
        this.cn = cn;
        this.setAttrName(name);
        this.setAttrValue(value);
    }
    public LobDocumentAttr(LobDocumentAttr attr) {
        super();
        this.setAttrName(attr.getAttrName());
        this.setAttrValue(attr.getAttrValue());
    }    
}
