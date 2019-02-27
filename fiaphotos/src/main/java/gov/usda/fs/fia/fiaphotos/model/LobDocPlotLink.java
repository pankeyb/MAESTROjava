/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.model;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author sdelucero
 */
@Data
@Entity
@Table(name = "LOB_DOCUMENT_LINK", schema = "FS_FIA_LOB")
public class LobDocPlotLink implements Serializable {
    @Id
    @Column(nullable = false, length = 34)
    private String cn;
    
    @Column(name = "LINK_TYPE")
    private String linkType = "PLOT_PHOTO";

    @Column(name = "SCHEMA_NAME", nullable = false, length = 30)
    private String schemaName;

    @Column(name = "TABLE_NAME", nullable = false, length = 30)
    private String tableName;

    @ManyToOne(cascade = { CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "DOC_CN")  
    private LobDocument lobDocument;
    
    @ManyToOne
    @JoinColumn(name = "LINK_CN")
    private NimsPlotTbl nimsPlotTbl;
   
    @Transient
    private boolean selected = false;
    
    public LobDocPlotLink() {
        super();
    }
    public  LobDocPlotLink(boolean filter) {
        super();
        if (filter) {
            nimsPlotTbl = new NimsPlotTbl(filter);
            lobDocument = new LobDocument();
        }
    }

}
