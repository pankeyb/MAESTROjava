/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.persistence;

import gov.usda.fs.fia.fiaphotos.model.LobDocumentAttr;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author sdelucero
 */
public interface LobDocumentAttrRepository extends JpaRepository<LobDocumentAttr, String> {
    
}
