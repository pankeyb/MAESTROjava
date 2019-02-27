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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author sdelucero
 */
@Data
@Entity
@NamedNativeQuery(name = "NimsCounty.findDistinctCounties", resultClass = NimsCounty.class, query = "SELECT c.* FROM FS_NIMS_RMRS.NIMS_COUNTY c WHERE c.stateCd = ?1 and c.countyCd in (select distinct countyCd from FS_NIMS_RMRS.NIMS_PLOT_TBL where stateCd = ?2) order by c.countyNm")
@Table(name = "NIMS_COUNTY", schema = "FS_NIMS_RMRS")
public class NimsCounty implements Serializable {

    /*
CN	VARCHAR2(34 BYTE)
STATECD	NUMBER(4,0)
COUNTYCD	NUMBER(3,0)
COUNTYNM	VARCHAR2(50 BYTE)    
     */
    @Id
    @Column(name = "CN")
    private String countyCn;

    @Column(name = "STATECD")
    private Long stateCd;

    @Column(name = "COUNTYCD")
    private Long countyCd;

    @Column(name = "COUNTYNM")
    private String countyNm;
    
}
