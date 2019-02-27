/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.persistence;

import gov.usda.fs.fia.fiaphotos.model.LobDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author sdelucero
 */
public interface LobDocumentRepository extends JpaRepository<LobDocument, String> {
    /*
    @Query(value="select d.cn from FS_FIA_LOB.nims_document d " +
                 "inner join FS_FIA_LOB.nims_doc_plot_link l on d.cn = l.doc_cn " +
                 "inner join FS_FIA_LOB.nims_document_attr a on d.cn = a.doc_cn " +
                 "inner join fs_nims_rmrs.nims_plot_tbl p on l.link_cn = p.cn " +
                 "where d.archived = 'N' and p.statecd = ?1 and p.countycd = ?2 and p.plot = ?3 and p.measyear=?4 and a.attr_name = 'DIRECTION' and a.attr_value = ?5 "+
                 "order by d.file_name", nativeQuery=true)
    List<String> findDupPhotos(Long stateCd, Long countyCd, Long plot, Long year, String direction);    
    */
}
