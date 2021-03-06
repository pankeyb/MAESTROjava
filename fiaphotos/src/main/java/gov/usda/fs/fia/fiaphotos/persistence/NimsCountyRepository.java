/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.persistence;

import gov.usda.fs.fia.fiaphotos.model.NimsCounty;
import gov.usda.fs.fia.fiaphotos.model.State;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author sdelucero
 */
public interface NimsCountyRepository  extends JpaRepository<NimsCounty, String> {
    
    @Query(nativeQuery = true)
    public List<NimsCounty> findDistinctCounties(Long stateCd1, Long stateCd2);
        
}
