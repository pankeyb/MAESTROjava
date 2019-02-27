/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author sdelucero
 */
@Data
@Entity
@NamedNativeQuery(name = "State.findDistinctStates", resultClass = State.class, query = "select to_number(code) STATECD, meaning STATENM, abbr STATEABBR from FS_NIMS_RMRS.NIMS_REF_CATEGORY_CODE t where t.category like 'STATE'  and to_number(code) in (select distinct statecd from FS_NIMS_RMRS.nims_plot_tbl) order by to_number(code)")
/*
@NamedNativeQuery(name = "State.findDistinctStates", resultClass = State.class, query = "select S.STATECD, S.STATENM from (select 1 STATECD, 'Alabama' STATENM from DUAL union\n"
        + "select 2 STATECD, 'Alaska' STATENM from DUAL union\n"
        + "select 4 STATECD, 'Arizona' STATENM from DUAL union\n"
        + "select 5 STATECD, 'Arkansas' STATENM from DUAL union\n"
        + "select 6 STATECD, 'California' STATENM from DUAL union\n"
        + "select 8 STATECD, 'Colorado' STATENM from DUAL union\n"
        + "select 9 STATECD, 'Connecticut' STATENM from DUAL union\n"
        + "select 10 STATECD, 'Delaware' STATENM from DUAL union\n"
        + "select 11 STATECD, 'District of Columbia' STATENM from DUAL union\n"
        + "select 12 STATECD, 'Florida' STATENM from DUAL union\n"
        + "select 13 STATECD, 'Geogia' STATENM from DUAL union\n"
        + "select 15 STATECD, 'Hawaii' STATENM from DUAL union\n"
        + "select 16 STATECD, 'Idaho' STATENM from DUAL union\n"
        + "select 17 STATECD, 'Illinois' STATENM from DUAL union\n"
        + "select 18 STATECD, 'Indiana' STATENM from DUAL union\n"
        + "select 19 STATECD, 'Iowa' STATENM from DUAL union\n"
        + "select 20 STATECD, 'Kansas' STATENM from DUAL union\n"
        + "select 21 STATECD, 'Kentucky' STATENM from DUAL union\n"
        + "select 22 STATECD, 'Louisiana' STATENM from DUAL union\n"
        + "select 23 STATECD, 'Maine' STATENM from DUAL union\n"
        + "select 24 STATECD, 'Maryland' STATENM from DUAL union\n"
        + "select 25 STATECD, 'Massachusetts' STATENM from DUAL union\n"
        + "select 26 STATECD, 'Michigan' STATENM from DUAL union\n"
        + "select 27 STATECD, 'Minnesota' STATENM from DUAL union\n"
        + "select 28 STATECD, 'Mississippi' STATENM from DUAL union\n"
        + "select 29 STATECD, 'Missouri' STATENM from DUAL union\n"
        + "select 30 STATECD, 'Montana' STATENM from DUAL union\n"
        + "select 31 STATECD, 'Nebraska' STATENM from DUAL union\n"
        + "select 32 STATECD, 'Nevada' STATENM from DUAL union\n"
        + "select 33 STATECD, 'New Hampshire' STATENM from DUAL union\n"
        + "select 34 STATECD, 'New Jersey' STATENM from DUAL union\n"
        + "select 35 STATECD, 'New Mexico' STATENM from DUAL union\n"
        + "select 36 STATECD, 'New York' STATENM from DUAL union\n"
        + "select 37 STATECD, 'North Carolina' STATENM from DUAL union\n"
        + "select 38 STATECD, 'North Dakota' STATENM from DUAL union\n"
        + "select 39 STATECD, 'Ohio' STATENM from DUAL union\n"
        + "select 40 STATECD, 'Oklahoma' STATENM from DUAL union\n"
        + "select 41 STATECD, 'Oregon' STATENM from DUAL union\n"
        + "select 42 STATECD, 'Pennsylvania' STATENM from DUAL union\n"
        + "select 44 STATECD, 'Rhode Island' STATENM from DUAL union\n"
        + "select 45 STATECD, 'South Carolina' STATENM from DUAL union\n"
        + "select 46 STATECD, 'South Dakota' STATENM from DUAL union\n"
        + "select 47 STATECD, 'Tennessee' STATENM from DUAL union\n"
        + "select 48 STATECD, 'Texas' STATENM from DUAL union\n"
        + "select 49 STATECD, 'Utah' STATENM from DUAL union\n"
        + "select 50 STATECD, 'Vermont' STATENM from DUAL union\n"
        + "select 51 STATECD, 'Virginia' STATENM from DUAL union\n"
        + "select 53 STATECD, 'Washington' STATENM from DUAL union\n"
        + "select 54 STATECD, 'West Virginia' STATENM from DUAL union\n"
        + "select 55 STATECD, 'Wisconsin' STATENM from DUAL union\n"
        + "select 56 STATECD, 'Wyoming' STATENM from DUAL) S where S.STATECD in (select distinct statecd from nims_plot_tbl)")
*/
@Table(name = "NIMS_REF_CATEGORY_CODE", schema = "FS_NIMS_RMRS")
public class State implements Serializable {

    @Id
    @Column(name = "STATECD")
    private Long stateCd;

    @Column(name = "STATENM")
    private String stateNm;

    @Column(name = "STATEABBR")
    private String stateAbbr;
    /*
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(updatable = false, insertable = false, name = "statecd", referencedColumnName = "statecd")
    private Set<NimsCounty> counties = new HashSet<>();
*/
}
