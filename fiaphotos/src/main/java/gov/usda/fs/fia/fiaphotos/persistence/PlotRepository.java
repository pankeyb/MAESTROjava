/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.persistence;

import gov.usda.fs.fia.fiaphotos.model.NimsPlotTbl;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author sdelucero
 */
public interface PlotRepository   extends JpaRepository<NimsPlotTbl, String>  {
    @Query("SELECT distinct p.plot FROM NimsPlotTbl p WHERE p.stateCd = ?1 and p.countyCd = ?2 order by p.plot")
    List<Long> findPlots(Long stateCd, Long countyCd);

    @Query("SELECT distinct p.plotFiaDb FROM NimsPlotTbl p WHERE p.stateCd = ?1 and p.countyCd = ?2 order by p.plotFiaDb")
    List<Long> findPlotFiaDbs(Long stateCd, Long countyCd);

    @Query("SELECT distinct p.invYr FROM NimsPlotTbl p WHERE p.stateCd = ?1 and p.countyCd = ?2 and p.plot = ?3 order by p.invYr")
    List<Long> findInvYears(Long stateCd, Long countyCd, Long plot);

    @Query("SELECT distinct p.measYr FROM NimsPlotTbl p WHERE p.stateCd = ?1 and p.countyCd = ?2 and p.plot = ?3 order by p.measYr")
    List<Long> findMeasYears(Long stateCd, Long countyCd, Long plot);

    @Query("SELECT distinct p.prefieldPlot.fieldSeasonYr FROM NimsPlotTbl p WHERE p.stateCd = ?1 and p.countyCd = ?2 and p.plot = ?3 order by p.prefieldPlot.fieldSeasonYr")
    // @Query("SELECT distinct p.fieldSeasonYr FROM NimsPlotTbl p WHERE p.stateCd = ?1 and p.countyCd = ?2 and p.plot = ?3 order by p.fieldSeasonYr")
    List<Long> findFldSeasYears(Long stateCd, Long countyCd, Long plot);
    
    /*
    @Query("select user from dual")
    String getUser();
    */
}
