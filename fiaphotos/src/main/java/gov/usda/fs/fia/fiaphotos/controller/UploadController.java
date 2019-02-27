/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import gov.usda.fs.fia.fiaphotos.model.NimsCounty;
import gov.usda.fs.fia.fiaphotos.service.PlotPhotoService;
import gov.usda.fs.fia.fiaphotos.model.LobDocPlotLink;
import gov.usda.fs.fia.fiaphotos.model.LobDocument;
import gov.usda.fs.fia.fiaphotos.model.LobDocumentAttr;
import gov.usda.fs.fia.fiaphotos.model.NimsPlotTbl;
import gov.usda.fs.fia.fiaphotos.model.State;
import gov.usda.fs.fia.fiaphotos.util.PhotoAttribute;
import gov.usda.fs.fia.fiaphotos.util.PlotPhotoData;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;
import javax.sql.rowset.serial.SerialBlob;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author sdelucero
 */
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component(value = "uplBean")
@ELBeanName(value = "uplBean")
@Join(path = "/uploadPhotos", to = "/uploadPhotos.jsf")
public class UploadController implements EditImageContent {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UploadController.class);
    private List<PlotPhotoData> uploadedData;
    private PlotPhotoData editUpload = null;

    @Autowired
    private LookupBean lookupBean;

    @Autowired
    @Qualifier("viewPhotoService")
    private PlotPhotoService plotPhotoService;

    @Deferred
    @RequestAction
    @IgnorePostback
    public void loadData() {
        refreshMsgs();
    }

    public String getBeanName() {
        return "uplBean";
    }

    /**
     * @return the uploadedFile
     */
    public void handleFileUpload(FileUploadEvent event) {
        log.info(event.getFile().getFileName() + " Uploaded");

        if (uploadedData == null) {
            uploadedData = new ArrayList<PlotPhotoData>();
        }
        PlotPhotoData ud = new PlotPhotoData(event.getFile());

        NimsPlotTbl nimsPlot = lookupBean.findPlot(ud.getStateCode(), ud.getCountyCode(), ud.getPlotCode(), ud.getYear());
        ud.setPlotRecord(nimsPlot);
        if (nimsPlot == null) {
            ud.setErrorMsg("Plot Record Not Found");
        } else {
            if (plotPhotoService.isDupPrimaryPhoto(ud)) {
                ud.setErrorMsg("Duplicate Primary Photo");
                ud.setDuplicate(true);
            }
        }
        uploadedData.add(ud);
        refreshMsgs();

    }

    public void removeTag(ActionEvent ae) {
        FacesContext context = FacesContext.getCurrentInstance();
        String uuid = context.getExternalContext().getRequestParameterMap().get("uuid");
        getEdit().removeTag(uuid);
    }

    public void editItem(ActionEvent ae) {
        FacesContext context = FacesContext.getCurrentInstance();
        String uuid = context.getExternalContext().getRequestParameterMap().get("uuid");
        for (PlotPhotoData ud : uploadedData) {
            if (ud.getUuid().equals(uuid)) {
                editUpload = ud;
                break;
            }
        }
        /*
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", false);
        options.put("resizable", false);
        options.put("closable", false);
        options.put("contentHeight", 320);
        PrimeFaces.current().dialog().openDynamic("editUpload",options, null);
         */

    }

    public void editReturn(SelectEvent event) {
        String id = event.getComponent().getClientId();
        PrimeFaces.current().ajax().update(id);
    }

    public PlotPhotoData getEdit() {
        return editUpload;
    }

    public void setEdit(PlotPhotoData ud) {
        editUpload = ud;
    }

    public String editCancel() {
        getEdit().revertState();
        // PrimeFaces.current().dialog().closeDynamic(null);
        return "back";
    }

    public String editSave() {
        String outcome = null;
        PlotPhotoData ud = null;
        boolean replaced = false;
        try {
            ud = getEdit();
            ud.saveState();
            NimsPlotTbl nimsPlot = lookupBean.findPlot(ud.getStateCode(), ud.getCountyCode(), ud.getPlotCode(), ud.getYear());
            ud.setPlotRecord(nimsPlot);
            if (nimsPlot == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Plot Record Not Found"));
                throw new Exception("Plot Record Not Found");
            }
            if (ud.isDuplicate()) {
                if ("REPLACE".equals(ud.getAddOrReplace())) {
                    LobDocument oldPrimary = plotPhotoService.getPrimaryPhoto(ud);
                    oldPrimary.setArchived("Y");

                    LobDocPlotLink newPrimary = createDocPlotLink(ud);

                    plotPhotoService.replace(newPrimary, oldPrimary);
                    replaced = true;

                } else {
                    throw new Exception("Duplicate Primary Photo - Select Replace Duplicate or Change Direction to Other");
                }
            }
            /*
            int seq = plotPhotoService.getPlotPhotoSeq(ud);
            ud.updateFilename(seq);
            if (seq > 1) {
                ud.setDuplicate(true);
                throw new Exception("Possible Duplicate Photo");
            }
             */
            ud.setErrorMsg(null);
            if (!replaced) {
                savePhoto(ud);
            }
            Iterator<PlotPhotoData> iter = uploadedData.iterator();
            while (iter.hasNext()) {
                if (iter.next().getUuid().equals(ud.getUuid())) {
                    iter.remove();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, null, "1 Photo Saved"));
            outcome = "back";
        } catch (Throwable e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage()));
            ud.setErrorMsg(e.getMessage());

        }
        return outcome;
    }

    public void removeItem(ActionEvent ae) {
        FacesContext context = FacesContext.getCurrentInstance();
        String uuid = context.getExternalContext().getRequestParameterMap().get("uuid");
        for (PlotPhotoData ud : uploadedData) {
            if (ud.getUuid().equals(uuid)) {
                uploadedData.remove(ud);
                break;
            }
        }
        refreshMsgs();

    }

    public List<SelectItem> getStateItems() {
        return lookupBean.getStateItems();
    }

    public List<SelectItem> getCountyItems() {
        PlotPhotoData ud = getEdit();
        return lookupBean.getCountyItems(ud.getStateCode());
    }

    public List<SelectItem> getPlotItems() {
        PlotPhotoData ud = getEdit();
        return lookupBean.getPlotItems(ud.getStateCode(), ud.getCountyCode(), "PLOT");
    }

    public List<SelectItem> getYearItems() {
        PlotPhotoData ud = getEdit();
        return lookupBean.getYearItems("MEASURE", ud.getStateCode(), ud.getCountyCode(), ud.getPlotCode());
    }

    public int getMaxFiles() {
        int max = (uploadedData == null ? 120 : 120 - uploadedData.size());
        return max;
    }

    public List<PlotPhotoData> getUploadedData() {
        return uploadedData;
    }

    public boolean isDisableSave() {
        return (uploadedData == null ? true : uploadedData.size() == 0);
    }

    public void cancelUpload(ActionEvent ae) {
        try {
            if (uploadedData != null) {
                uploadedData.clear();
                uploadedData = null;
                plotPhotoService.load();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, e.getMessage()));
        }
    }

    public void bulkUpload(ActionEvent ae) {
        try {
            int i = 0;
            int x = 0;
            PlotPhotoData ppd = uploadedData.get(0);
            List<State> states = lookupBean.getStates();

            for (State state : states) {
                x = 0;
                List<NimsCounty> counties = lookupBean.getCounties(state.getStateCd());
                // for (NimsCounty county : counties) {
                int maxc = counties.size();
                for (int c = 0; c < Integer.min(maxc, 5); c++) {
                    NimsCounty county = counties.get(c);
                    List<Long> plots = lookupBean.getPlots(state.getStateCd(), county.getCountyCd());

                    int maxp = plots.size();
                    // for (Long plot : plots) {
                    for (int p = 0; p < Integer.min(maxp, 5); p++) {
                        Long plot = plots.get(p);
                        List<Long> years = lookupBean.getYears(state.getStateCd(), county.getCountyCd(), plot);
                        for (Long year : years) {
                            x++;
                            ppd.setStateCode(state.getStateCd());
                            ppd.setCountyCode(county.getCountyCd());
                            ppd.setPlotCode(plot);
                            ppd.setYear(year);
                            ppd.setTitle("Plot Photo");
                            ppd.setDirection("North");
                            String filename = ppd.getCurrentFilename();
                            ByteArrayInputStream in = new ByteArrayInputStream(ppd.getContent());
                            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("C:\\Users\\sdelucero\\Pictures\\FIA Photos\\Other\\" + filename));
                            IOUtils.copy(in, out);
                            out.close();

                            ppd.setDirection("South");
                            filename = ppd.getCurrentFilename();
                            in = new ByteArrayInputStream(ppd.getContent());
                            out = new BufferedOutputStream(new FileOutputStream("C:\\Users\\sdelucero\\Pictures\\FIA Photos\\Other\\" + filename));
                            IOUtils.copy(in, out);
                            out.close();

                            ppd.setDirection("East");
                            filename = ppd.getCurrentFilename();
                            in = new ByteArrayInputStream(ppd.getContent());
                            out = new BufferedOutputStream(new FileOutputStream("C:\\Users\\sdelucero\\Pictures\\FIA Photos\\Other\\" + filename));
                            IOUtils.copy(in, out);
                            out.close();

                            ppd.setDirection("West");
                            filename = ppd.getCurrentFilename();
                            in = new ByteArrayInputStream(ppd.getContent());
                            out = new BufferedOutputStream(new FileOutputStream("C:\\Users\\sdelucero\\Pictures\\FIA Photos\\Other\\" + filename));
                            IOUtils.copy(in, out);
                            out.close();
                            i += 4;
                            log.debug("Bulk Save: " + i);
                        }

                    }
                }
            }
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", t.getMessage()));
        }
    }

    public void saveUpload(ActionEvent ae) {
        int x = 0;
        Iterator<PlotPhotoData> iter = uploadedData.iterator();
        while (iter.hasNext()) {
            // for (PlotPhotoData ud : uploadedData) {
            PlotPhotoData ud = iter.next();
            try {
                savePhoto(ud);

                iter.remove();

                x++;
            } catch (Throwable e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage()));
                ud.setErrorMsg(e.getMessage());
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, null, x + " Photos Saved"));

    }

    public LobDocPlotLink createDocPlotLink(PlotPhotoData ud) throws Exception {
        LobDocument lobDoc = new LobDocument();
        lobDoc.setCn(plotPhotoService.getNextSeq());
        lobDoc.setRscd(UserSession.getInstance().getUserStation());
        lobDoc.setContentType(ud.getContentType());
        lobDoc.setFileName(ud.getFileName());
        lobDoc.setTitle(ud.getTitle());
        lobDoc.setDescription(ud.getDescription());
        // lobDoc.setFileSize(new Long(ud.getContent().length));
        lobDoc.setStatus("PENDING");
        Blob content = new SerialBlob(ud.getContent());
        //lobDoc.setContent(ud.getContent());
        lobDoc.setContent(content);

        List<PhotoAttribute> attrs = ud.getAttributes();
        for (PhotoAttribute attr : attrs) {
            LobDocumentAttr docAttr = new LobDocumentAttr(plotPhotoService.getNextSeq(),attr.getName(), attr.getValue());
            docAttr.setLobDocument(lobDoc);
            lobDoc.getLobDocumentAttrList().add(docAttr);
        }

        LobDocPlotLink docPlotLink = new LobDocPlotLink();
        docPlotLink.setCn(plotPhotoService.getNextSeq());
        docPlotLink.setLobDocument(lobDoc);
        docPlotLink.setNimsPlotTbl(ud.getPlotRecord());
        docPlotLink.setSchemaName(lookupBean.getLinkSchema());
        docPlotLink.setTableName(lookupBean.getLinkTable());

        return docPlotLink;
    }

    public void savePhoto(PlotPhotoData ud) throws Throwable {

        if (ud.getPlotRecord() == null) {
            throw new Exception("Plot Record Not Found");
        }
        if (ud.getErrorMsg() != null) {
            throw new Exception(ud.getErrorMsg());
        }
        LobDocPlotLink docPlotLink = createDocPlotLink(ud);
        plotPhotoService.insert(docPlotLink);

    }

    public void refreshMsgs() {
        int x = (uploadedData == null ? 0 : uploadedData.size());
        String msg = x + " of 120 Photos Staged";
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, null, msg));
    }

    @Override
    public byte[] getEditImageContent(String uuid) throws SQLException {
        byte content[] = null;
        List<PlotPhotoData> data = getUploadedData();
        for (PlotPhotoData ud : data) {
            if (ud.getUuid().equals(uuid)) {
                content = ud.getContent();
                break;
            }
        }
        return content;
    }

    @Override
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
            ud.updateFilename();
            if (plotPhotoService.isDupPrimaryPhoto(ud)) {
                ud.setErrorMsg("Duplicate Primary Photo");
                ud.setDuplicate(true);
            }
        }
    }
}
