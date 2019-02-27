/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import gov.usda.fs.fia.fiaphotos.model.LobDocPlotLink;
import gov.usda.fs.fia.fiaphotos.model.LobDocument;
import gov.usda.fs.fia.fiaphotos.model.LobDocumentAttr;
import gov.usda.fs.fia.fiaphotos.model.NimsPlotTbl;
import gov.usda.fs.fia.fiaphotos.service.PlotPhotoService;
import gov.usda.fs.fia.fiaphotos.util.ContextProvider;
import gov.usda.fs.fia.fiaphotos.util.PhotoAttribute;
import gov.usda.fs.fia.fiaphotos.util.PlotPhotoData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author sdelucero
 */
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component(value = "editPhoto")
@ELBeanName(value = "editPhoto")
@Join(path = "/editPhoto", to = "/editPhoto.jsf")
public class EditPhotoController extends UploadController {

    private String returnAction = "backToView";
    private String callingBeanName;
    @Autowired
    @Qualifier("viewPhotoService")
    private PlotPhotoService plotPhotoService;

    @Autowired
    private LookupBean lookupBean;

    public String getBeanName() {
        return "editPhoto";
    }

    public void editItem(ActionEvent ae) {
        FacesContext context = FacesContext.getCurrentInstance();
        returnAction = context.getExternalContext().getRequestParameterMap().get("returnAction");
        callingBeanName = context.getExternalContext().getRequestParameterMap().get("callingBeanName");
        String cn = context.getExternalContext().getRequestParameterMap().get("uuid");
        Optional<LobDocPlotLink> plotDoc = plotPhotoService.findById(cn);
        if (!plotDoc.isPresent()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Plot Photo Link Not Found"));
            return;
        }

        try {
            setEdit(new PlotPhotoData(plotDoc.get()));
        } catch (SQLException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", ex.getMessage()));
        }
    }

    public void removePhoto(ActionEvent ae) {
        FacesContext context = FacesContext.getCurrentInstance();
        callingBeanName = context.getExternalContext().getRequestParameterMap().get("callingBeanName");

        String cn = context.getExternalContext().getRequestParameterMap().get("uuid");
        try {
            plotPhotoService.remove(cn);
            refreshCallingBean();
        } catch (Throwable ex) {
            ex.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", ex.getMessage()));
        }

    }

    @Override
    public byte[] getEditImageContent(String uuid) throws SQLException {
        return getEdit().getContent();
    }

    @Override
    public String editCancel() {
        getEdit().revertState();
        // PrimeFaces.current().dialog().closeDynamic(null);
        return returnAction;
    }

    @Override
    public String editSave() {
        String outcome = null;
        PlotPhotoData ud = getEdit();
        ud.saveState();
        NimsPlotTbl newPlot = LookupBean.getInstance().findPlot(ud.getStateCode(), ud.getCountyCode(), ud.getPlotCode(), ud.getYear());

        if (newPlot == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Plot Record Not Found"));
            ud.setErrorMsg("Plot Record Not Found");
            return outcome;
        }
        boolean saveLink = false;
        LobDocPlotLink link = ud.getLinkRecord();
        if (!newPlot.getCn().equals(ud.getPlotRecord().getCn())) {
            link.setNimsPlotTbl(newPlot);
            saveLink = true;
        }
        LobDocument lobDoc = link.getLobDocument();
        lobDoc.setFileName(ud.getFileName());
        lobDoc.setTitle(ud.getTitle());
        lobDoc.setDescription(ud.getDescription());

        updateDocAttrs(lobDoc, ud.getAttributes());
        try {
            if (ud.isDuplicate()) {
                if ("REPLACE".equals(ud.getAddOrReplace())) {
                    LobDocument oldPrimary = plotPhotoService.getPrimaryPhoto(ud);
                    oldPrimary.setArchived("Y");

                    lobDoc.setFileName(ud.getFileName());
                    if (saveLink) {
                        plotPhotoService.replace(link, oldPrimary);
                    } else {
                        plotPhotoService.replace(lobDoc, oldPrimary);
                    }
                } else {
                    throw new Exception("Duplicate Primary Photo - Select Replace Duplicate or Change Direction to Other");
                }

            } else {
                if (saveLink) {
                    link = plotPhotoService.save(link);
                } else {
                    plotPhotoService.save(lobDoc);
                }
            }
            refreshCallingBean();
            outcome = returnAction;
        } catch (Throwable e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage()));
        }
        return outcome;
    }

    public void updateDocAttrs(LobDocument lobDoc, List<PhotoAttribute> newList) {
        
        List<LobDocumentAttr> oldList = lobDoc.getLobDocumentAttrList();
        // update existing
        for (LobDocumentAttr da : oldList) {
            for (PhotoAttribute pa : newList) {
                if (da.getCn() != null && da.getCn().equals(pa.getUuid())) {
                    da.setAttrValue(pa.getValue());
                }
            }
        }
        // remove deleted
        Iterator<LobDocumentAttr> iter = oldList.iterator();
        while (iter.hasNext()) {
            LobDocumentAttr da=iter.next();
            boolean remove = true;
            for (PhotoAttribute pa : newList) {
                if (da.getCn() != null && da.getCn().equals(pa.getUuid())) {
                    remove=false;
                }
            }
            if (remove) {
                iter.remove();
            }
        }
        // add new
        for (PhotoAttribute pa : newList) {
            boolean add = true;
            for (LobDocumentAttr da : oldList) {
                if (pa.getUuid().equals(da.getCn())) {
                    add = false;
                }
            }
            if (add) {
                LobDocumentAttr docAttr = new LobDocumentAttr(plotPhotoService.getNextSeq(),pa.getName(), pa.getValue());
                docAttr.setLobDocument(lobDoc);
                oldList.add(docAttr);
            }
        }
        return;
    }

    public void editChanged() {
        PlotPhotoData ud = getEdit();
        ud.setErrorMsg(null);
        ud.setDuplicate(false);
        ud.setAddOrReplace(null);
        NimsPlotTbl nimsPlot = lookupBean.findPlot(ud.getStateCode(), ud.getCountyCode(), ud.getPlotCode(), ud.getYear());
        ud.setPlotRecord(nimsPlot);
        if (nimsPlot == null) {
            ud.setErrorMsg("Plot Record Not Found");
            ud.setFileName("");
        } else {
            if (ud.isPlotOrDirChanged()) {
                ud.updateFilename();
                if (plotPhotoService.isDupPrimaryPhoto(ud)) {
                    ud.setErrorMsg("Duplicate Primary Photo");
                    ud.setDuplicate(true);
                }
            } else {
                ud.revertFilename();
            }
        }
    }

    public void approveOne() {
        FacesContext context = FacesContext.getCurrentInstance();
        returnAction = context.getExternalContext().getRequestParameterMap().get("returnAction");
        callingBeanName = context.getExternalContext().getRequestParameterMap().get("callingBeanName");

        String fn = null;
        String cn = context.getExternalContext().getRequestParameterMap().get("uuid");
        try {
            fn = approveItem(cn);
            refreshCallingBean();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, null, fn + " Approved"));
        } catch (Throwable e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: " + fn, e.getMessage()));
        }
    }

    public String approveItem(String cn) throws Throwable {

        Optional<LobDocPlotLink> plotDoc = plotPhotoService.findById(cn);
        if (!plotDoc.isPresent()) {
            throw new Exception("Plot Photo Link Not Found");
        }
        LobDocument lobDoc = plotDoc.get().getLobDocument();
        lobDoc.setStatus("APPROVED");
        plotPhotoService.save(lobDoc);
        return lobDoc.getFileName();
    }

    public void refreshCallingBean() {
        PhotosController controller = (PhotosController) ContextProvider.getBean(callingBeanName);
        controller.refresh();
    }

}
