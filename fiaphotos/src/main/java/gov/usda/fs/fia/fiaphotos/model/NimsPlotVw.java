/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

/**
 *
 * @author sdelucero
 */
/*
@Data
@Entity
@Table(name = "NIMS_PREFIELD_PLOT_VW", schema = "FS_NIMS_RMRS")
*/
public class NimsPlotVw implements Serializable {

    @Id
    @Column(name = "CN", updatable=false, insertable=false)
    private String cn;

    @Column(name = "STATECD", updatable=false, insertable=false)
    private Long stateCd;

    @Column(name = "COUNTYCD", updatable=false, insertable=false)
    private Long countyCd;

    @Column(name = "PLOT", updatable=false, insertable=false)
    private Long plot;

    @Column(name = "PLOT_FIADB", updatable=false, insertable=false)
    private Long plotFiaDb;

    @Column(name = "INVYR", updatable=false, insertable=false)
    private Long invYr;

    /*
    @Column(name = "MEASYEAR", updatable=false, insertable=false)
    private Long measYr;
    */
    @Column(name = "PREV_INV_YEAR", updatable=false, insertable=false)
    private Long measYr;
    
    @OneToMany(mappedBy = "nimsPlotTbl", cascade = { CascadeType.REMOVE })
    private List<LobDocPlotLink> lobDocPlotLinkList;

    @OneToOne(targetEntity = NimsCounty.class, fetch = FetchType.EAGER, cascade=CascadeType.REFRESH, optional=true)
    @JoinColumns({
        @JoinColumn(updatable=false,insertable=false,name = "statecd", referencedColumnName = "statecd")
        ,
        @JoinColumn(updatable=false,insertable=false,name = "countycd", referencedColumnName = "countycd")
    })
    private NimsCounty nimsCounty;
    
    @Column(name = "FIELD_SEASON_YR", updatable=false, insertable=false)
    private Long fieldSeasonYr;

    /*
    @OneToOne(targetEntity = NimsPrefieldPlot.class, fetch = FetchType.EAGER, cascade=CascadeType.REFRESH, optional=true)
    @JoinColumns({
        @JoinColumn(updatable=false,insertable=false,name = "cn", referencedColumnName = "cn")
    })
    private NimsPrefieldPlot prefieldPlot;

    public Long getFldSeasYr() {
        Long yr = null; 
        try {
            yr = (prefieldPlot == null ? null : prefieldPlot.getFieldSeasonYr());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return yr;
    }
*/
    public NimsPlotVw() {
        super();
    }
    public NimsPlotVw(boolean filter) {
        super();
        /*
        if (filter) {
            prefieldPlot = new NimsPrefieldPlot();
        }
        */
    }
}
