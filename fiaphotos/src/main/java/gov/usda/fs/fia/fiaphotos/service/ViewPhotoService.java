/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.service;

import gov.usda.fs.fia.fiaphotos.model.LobDocPlotLink;
import org.springframework.stereotype.Service;

/**
 *
 * @author sdelucero
 */
@Service("viewPhotoService")
public class ViewPhotoService extends PlotPhotoService {

    public LobDocPlotLink getPlotLinkQBE() {
        if (qbeDocPlotLink == null) {
            qbeDocPlotLink = new LobDocPlotLink(true);
            qbeDocPlotLink.setSchemaName(getLookupBean().getLinkSchema());
            qbeDocPlotLink.setTableName(getLookupBean().getLinkTable());
            qbeDocPlotLink.getLobDocument().setArchived("N");
            qbeDocPlotLink.getLobDocument().setStatus("APPROVED");
        }
        return qbeDocPlotLink;
    }
}
