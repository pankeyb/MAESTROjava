/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import gov.usda.fs.fia.fiaphotos.model.LobDocPlotLink;
import gov.usda.fs.fia.fiaphotos.model.LobDocument;
import gov.usda.fs.fia.fiaphotos.service.PlotPhotoService;
import gov.usda.fs.fia.fiaphotos.util.ContextProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.primefaces.PrimeFaces;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author sdelucero
 */
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component(value = "approvePhotos")
@ELBeanName(value = "approvePhotos")
@Join(path = "/approve", to = "/approve.jsf")
public class ApproveController extends PhotosController {
    @Autowired
    @Qualifier("approvePhotoService")
    private PlotPhotoService plotPhotoService;

    private boolean approving = false;
    public ApproveController() {
        super();
        setGridCols(4);
    }

    public PlotPhotoService getPlotPhotoService() {
        return plotPhotoService;
    }

    @Override
    public void refresh() {
        try {
            getPlotPhotoService().load();
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, e.getMessage()));
        }
    }

    public void approveSelected() throws Throwable {
        approving=true;
        EditPhotoController editor = (EditPhotoController) ContextProvider.getBean("editPhoto");
        List<LobDocPlotLink> photos = getPhotos();
        String fn=null;
        try {
            for (LobDocPlotLink dpl : photos) {
                if (dpl.isSelected()) {
                    fn = dpl.getLobDocument().getFileName();
                    editor.approveItem(dpl.getCn());
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, null, fn+" Approved"));
                }
            }
            refresh();
            approving=false;
        } catch (Throwable t) {
            approving=false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: "+fn, t.getMessage()));
            throw t;
        }
    }
    public void approveAll() {
        approving=true;
        String s = null;
        int n = 0;
        int x = 0;
        try {
            getPlotPhotoService().pageFirst();
            this.setSelectAll(true);
            this.doSelectAll();
            while (getPhotos().size() > 0) {
                x = getRowCount();
                n = getPhotos().size();
                s = this.getPageText();
                approveSelected();
                getPlotPhotoService().pageFirst();    
                x = getRowCount();
                n = getPhotos().size();
                s = this.getPageText();
                this.setSelectAll(true);
                this.doSelectAll();
            }
            refresh();
            approving=false;            
        }
        catch (Throwable t) {
            approving=false;
            t.printStackTrace();
        }
    }
    public boolean isDisableApproveSelected() {
        return ! this.isShowDownload();
    }
    public boolean isDisableApproveAll() {
        return (getPlotPhotoService().getRowCount() == 0);
    }
    public boolean isApproving() {
        return approving;
    }
}
