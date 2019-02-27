/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import gov.usda.fs.fia.fiaphotos.service.PlotPhotoService;
import gov.usda.fs.fia.fiaphotos.util.PlotSheet;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author sdelucero
 */
// @Scope (value = "session")
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component(value = "listPhotos")
@ELBeanName(value = "listPhotos")
@Join(path = "/viewPhotos", to = "/photos.jsf")
public class ViewController extends PhotosController {

    @Autowired
    @Qualifier("viewPhotoService")
    PlotPhotoService plotPhotoService;

    private boolean incOthers = false;
    private boolean landscape = false;

    public PlotPhotoService getPlotPhotoService() {
        return plotPhotoService;
    }

    @Override
    public void refresh() {
        try {
            getPlotPhotoService().load();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, e.getMessage()));
        }

    }

    public StreamedContent getPlotSheet() {
        DefaultStreamedContent content = null;
        try {
            PlotSheet plotSheet = new PlotSheet(getPhotos(), isIncOthers(), isLandscape());
            ByteArrayOutputStream baos = plotSheet.generate();
            InputStream in =new ByteArrayInputStream(((ByteArrayOutputStream)baos).toByteArray());
            BufferedInputStream input = new BufferedInputStream(in);
            content = new DefaultStreamedContent(input, "application/pdf","plotsheet.pdf");            
        }
        catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, e.getMessage()));
        }
        return content;
    }

    /**
     * @return the incOthers
     */
    public boolean isIncOthers() {
        return incOthers;
    }

    /**
     * @param incOthers the incOthers to set
     */
    public void setIncOthers(boolean incOthers) {
        this.incOthers = incOthers;
    }

    /**
     * @return the landscape
     */
    public boolean isLandscape() {
        return landscape;
    }

    /**
     * @param landscape the landscape to set
     */
    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

}
