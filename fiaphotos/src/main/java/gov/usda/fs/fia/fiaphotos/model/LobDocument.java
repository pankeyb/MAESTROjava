/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.model;

import java.io.Serializable;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author sdelucero
 */
@Data
@Entity
@Table(name = "LOB_DOCUMENT", schema = "FS_FIA_LOB")
public class LobDocument implements Serializable {

    @Id
    @Column(nullable = false, updatable=false, length = 34)
    private String cn;

    @Column(name="RSCD", nullable = false)
    private Integer rscd;
    
    @Lob
    @Column(name = "CONTENT", columnDefinition = "BLOB", updatable = false)
    @Basic(fetch = FetchType.LAZY)
    // private byte[] content;
    private Blob content;

    @Column(name = "CONTENT_TYPE", nullable = false, length = 200)
    private String contentType;

    @Column(name = "CREATED_BY", length = 80)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(length = 1024)
    private String description;
    
    @Column(name = "FILE_NAME", nullable = false, length = 1024)
    private String fileName;

    @Column(name = "FILE_SIZE", insertable=false, updatable = false)
    private Long fileSize;

    @Column(name = "MODIFIED_BY", length = 80)
    private String modifiedBy;

    @Column(name = "MODIFIED_DATE")
    private Date modifiedDate;

    @Column(length = 200)
    private String title;

    @Column(name = "ARCHIVED", nullable = false, length = 1)
    private String archived = "N";

    @Column(name = "STATUS", nullable = false, length = 80)
    private String status;

    @OneToMany(mappedBy = "lobDocument", cascade = { CascadeType.REMOVE }, orphanRemoval = true)
    private List<LobDocPlotLink> lobDocPlotLinkList;

    @OneToMany(mappedBy = "lobDocument", fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<LobDocumentAttr> lobDocumentAttrList;
    
    public LobDocument() {
        super();
    }
    public LobDocument(boolean filter) {
        super();
        if (filter) {
            lobDocPlotLinkList = new ArrayList<LobDocPlotLink>();
        }
    }
    public List<LobDocumentAttr> getLobDocumentAttrList() {
        if (lobDocumentAttrList == null) {
            lobDocumentAttrList = new ArrayList<LobDocumentAttr>();
        }
        return lobDocumentAttrList;
    }
    public String toString() {
        String s = super.toString();
        return s;
    }
    public String getCn() {
        return cn;
    }
}
