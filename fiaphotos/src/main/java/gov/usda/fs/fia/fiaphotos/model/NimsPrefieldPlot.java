/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author sdelucero
 */
@Data
@Entity
@Table(name = "NIMS_PREFIELD_PLOT_VW", schema = "FS_NIMS_RMRS")

public class NimsPrefieldPlot implements Serializable {

    @Id
    @Column(name = "CN", updatable=false, insertable=false)
    private String cn;

    @Column(name = "FIELD_SEASON_YR", updatable=false, insertable=false)
    private Long fieldSeasonYr;
    
}
