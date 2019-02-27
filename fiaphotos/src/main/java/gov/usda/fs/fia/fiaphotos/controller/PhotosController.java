/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import com.zaxxer.hikari.HikariDataSource;
import gov.usda.fs.fia.fiaphotos.service.PlotPhotoService;
import gov.usda.fs.fia.fiaphotos.model.LobDocPlotLink;
import gov.usda.fs.fia.fiaphotos.util.PlotSheet;
import gov.usda.fs.fia.fiaphotos.util.Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import oracle.jdbc.OracleConnection;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.LoggerFactory;
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
public abstract class PhotosController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PhotosController.class);

    @Autowired
    LookupBean lookupBean;

    //private MyPhotoModel model = null;
    private boolean selectAll = false;
    private int gridCols = 3;
    private Long filterState;
    private Long filterCounty;
    private Long filterPlot;
    private String filterPlotType = "PLOT";
    private Long filterYear;
    private String filterYearType = "MEASURE";

    @Autowired
    EntityManager entityManager;
    @Autowired
    private DataSource dataSource;

    public abstract PlotPhotoService getPlotPhotoService();

    public abstract void refresh();

    @Deferred
    @RequestAction
    @IgnorePostback
    public String loadData() throws Exception {
        
        try {
            getPlotPhotoService().load();
        }
        catch (Exception e) {
            e.printStackTrace();
            Throwable t = Utils.translateException(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, t.getMessage()));
        }
        return "viewPhotos";
    }

    public List<LobDocPlotLink> getPhotos() {
        List<LobDocPlotLink> list = null;
        try {
            list = getPlotPhotoService().getData();
        } catch (Exception e) {
            Throwable t = Utils.translateException(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, t.getMessage()));
        }
        return list;
    }

    public List<SelectItem> getStates() {
        return lookupBean.getStateItems();
    }

    public List<SelectItem> getCounties() {
        return lookupBean.getCountyItems(filterState);
    }

    public List<SelectItem> getPlots() {
        return lookupBean.getPlotItems(filterState, filterCounty, getFilterPlotType());
    }

    public List<SelectItem> getYears() {
        return lookupBean.getYearItems(filterYearType, filterState, filterCounty, filterPlot);
    }

    /**
     * @return the selectAll
     */
    // public void doSelectAll(ActionEvent ev) {
    public void doSelectAll() {
        boolean selected = isShowSelectAll();
        List<LobDocPlotLink> photos = getPhotos();
        if (photos != null) {
            for (LobDocPlotLink p : photos) {
                p.setSelected(selected);
            }
        }
    }

    public void pageFirst(ActionEvent ev) {
        try {
            getPlotPhotoService().pageFirst();
            scroll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, e.getMessage()));
        }

    }

    public void pagePrevious(ActionEvent ev) {
        try {
            getPlotPhotoService().pagePrevious();
            scroll();
        } catch (Exception e) {
            Throwable t = Utils.translateException(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, t.getMessage()));
        }

    }

    public void pageNext(ActionEvent ev) {
        try {
            getPlotPhotoService().pageNext();
            scroll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, e.getMessage()));
        }
    }

    public void pageLast(ActionEvent ev) {
        try {
            getPlotPhotoService().pageLast();
            scroll();
        } catch (Exception e) {
            Throwable t = Utils.translateException(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, t.getMessage()));
        }
    }

    public boolean isShowPageFirst() {
        return getPlotPhotoService().getCurrentPage() > 0;
    }

    public boolean isShowPagePrevious() {
        return getPlotPhotoService().getCurrentPage() > 0;
    }

    public boolean isShowPageNext() {
        return getPlotPhotoService().getCurrentPage() < getPlotPhotoService().getMaxPage();
    }

    public boolean isShowPageLast() {
        return getPlotPhotoService().getCurrentPage() < getPlotPhotoService().getMaxPage();
    }

    public boolean isShowSelectAll() {
        boolean show = false;
        List<LobDocPlotLink> photos = getPhotos();
        if (photos != null) {
            Iterator<LobDocPlotLink> iter = photos.iterator();
            while (!show && iter.hasNext()) {
                show = !iter.next().isSelected();
            }
        }
        return show;
    }

    public boolean isShowDownload() {
        boolean show = false;
        List<LobDocPlotLink> photos = getPhotos();
        if (photos != null) {
            Iterator<LobDocPlotLink> iter = photos.iterator();
            while (!show && iter.hasNext()) {
                show = iter.next().isSelected();
            }
        }
        return show;
    }

    public void download() {
    }

    public void applyFilter(ActionEvent ev) {

        FilterModel filter = new FilterModel();
        filter.setState(getFilterState());
        filter.setCounty(getFilterCounty());
        filter.setPlot(getFilterPlot());
        filter.setYear(getFilterYear());
        filter.setYearType(getFilterYearType());
        filter.setPlotType(getFilterPlotType());

        try {
            getPlotPhotoService().filter(filter);
            scroll();
        } catch (Exception e) {
            Throwable t = Utils.translateException(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, t.getMessage()));
        }

    }

    public void clearFilter(ActionEvent ev) {

        setFilterState(null);
        setFilterCounty(null);
        setFilterPlot(null);
        setFilterYear(null);
        FilterModel filter = new FilterModel();
        try {
            getPlotPhotoService().filter(filter);
            scroll();
        } catch (Exception e) {
            Throwable t = Utils.translateException(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, t.getMessage()));
        }
    }

    public void doDownload(ActionEvent ev) throws Exception {
        StreamedContent content = getDownload();

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        Integer len = content.getContentLength();

        ec.responseReset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
        ec.setResponseContentType(content.getContentType()); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
        // ec.setResponseContentLength(len.intValue()); // Set it with the file size. This header is optional. It will work if it's omitted, but the download progress will be unknown.
        ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + content.getName() + "\""); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.

        OutputStream output = ec.getResponseOutputStream();
        InputStream input = content.getStream();

        IOUtils.copy(input, output);
        /*
        byte data[] = new byte[2048];
        int count;

        while ((count = input.read(data, 0, 2048)) != -1) {
            output.write(data, 0, count);
        }
         */
        fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.        
    }
    public StreamedContent getDownload() {
        
        DefaultStreamedContent content = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-");
        File fZip = null;

        try {
            Calendar now = Calendar.getInstance();
            int milli = now.get(Calendar.MILLISECOND);
            String zipName = "photos_" + sdf.format(now.getTime()) + milli;
            fZip = File.createTempFile(zipName, ".zip");
            FileOutputStream fos = new FileOutputStream(fZip);
            ZipOutputStream zipos = new ZipOutputStream(new BufferedOutputStream(fos));

            Map<String, Integer> fnames = new HashMap<String, Integer>();
            List<LobDocPlotLink> photos = getPhotos();
            for (LobDocPlotLink dpl : photos) {
                if (dpl.isSelected()) {
                    byte data[] = new byte[2048];
                    int count;

                    String fname = genDownloadFilename(dpl, fnames);
                    ZipEntry zipEntry = new ZipEntry(fname);
                    zipos.putNextEntry(zipEntry);

                    Blob blob = dpl.getLobDocument().getContent();
                    InputStream origin = blob.getBinaryStream();

                    IOUtils.copy(origin, zipos);
                    origin.close();
                    zipos.closeEntry();
                }
            }
            zipos.close();

            FileInputStream in = new FileInputStream(fZip);
            BufferedInputStream input = new BufferedInputStream(in);
            content = new DefaultStreamedContent(input, "application/zip", zipName + ".zip");
        } catch (Exception e) {
            e.printStackTrace();
            fZip.delete();
        }
        return content;
    }

    public String genDownloadFilename(LobDocPlotLink dpLink, Map<String, Integer> fnames) {
        String fname = dpLink.getLobDocument().getFileName();
        if (fnames.containsKey(fname)) {
            Integer I = fnames.get(fname);
            int seq = I.intValue() + 1;
            fnames.put(fname, new Integer(seq));
            String base = FilenameUtils.getBaseName(fname);
            String ext = FilenameUtils.getExtension(fname);
            fname = base + "_" + seq + "." + ext;
        } else {
            fnames.put(fname, new Integer(0));
        }
        return fname;
        /*
        LobDocPlotLink ex = new LobDocPlotLink(true);
        ex.getNimsPlotTbl().setStateCd(dpLink.getNimsPlotTbl().getStateCd());
        ex.getNimsPlotTbl().setCountyCd(dpLink.getNimsPlotTbl().getCountyCd());
        ex.getNimsPlotTbl().setPlot(dpLink.getNimsPlotTbl().getPlot());
        String dir = PlotPhotoData.getDirFromAttrs(dpLink.getLobDocument().getLobDocumentAttrList());
        ex.getLobDocument().getLobDocumentAttrList().add(new LobDocumentAttr("DIRECTION", dir));
        Example<LobDocPlotLink> example = Example.of(ex);
        
        int seq = getPlotPhotoService().getPlotPhotoSeq(ex);
        
        String fname = PlotPhotoData.genFilename(dpLink, seq);
        return fname;
         */
    }

    public int getGridCols() {
        return gridCols;
    }

    public void setGridCols(int gridCols) {
        this.gridCols = gridCols;
    }

    public int getPageSize() {
        return getPlotPhotoService().getPageSize();
    }

    public void setPageSize(int pageSize) {
        getPlotPhotoService().setPageSize(pageSize);
    }

    public void resetPageSize() {
        try {
            getPlotPhotoService().resetPageSize();
            scroll();
        } catch (Exception e) {
            Throwable t = Utils.translateException(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, null, t.getMessage()));
        }
    }

    public void scroll() {
        PrimeFaces.current().scrollTo("f1:top");
        // PrimeFaces.current().executeScript("$('.ui-layout-center').animate({scrollTop: 0}, 'fast');");

    }

    /**
     * @return the filterCounty
     */
    public Long getFilterCounty() {
        return filterCounty;
    }

    /**
     * @param filterCounty the filterCounty to set
     */
    public void setFilterCounty(Long filterCounty) {
        this.filterCounty = filterCounty;
    }

    public Long getFilterPlot() {
        return filterPlot;
    }

    public void setFilterPlot(Long filterPlot) {
        this.filterPlot = filterPlot;
    }

    public Long getFilterYear() {
        return filterYear;
    }

    public void setFilterYear(Long filterYear) {
        this.filterYear = filterYear;
    }

    public String getFilterYearType() {
        return filterYearType;
    }

    public void setFilterYearType(String filterYearType) {
        this.filterYearType = filterYearType;
    }

    public String getPageText() {
        int cp = getPlotPhotoService().getCurrentPage() + 1;
        int mp = getPlotPhotoService().getMaxPage() + 1;
        String s = " Page " + cp + " of " + mp;

        return s;
    }

    public class FilterModel {

        private Long state;
        private Long county;
        private Long plot;
        private Long year;
        private String yearType;
        private String plotType;

        public FilterModel() {

        }

        /**
         * @return the state
         */
        public Long getState() {
            return state;
        }

        /**
         * @param state the state to set
         */
        public void setState(Long state) {
            this.state = state;
        }

        /**
         * @return the county
         */
        public Long getCounty() {
            return county;
        }

        /**
         * @param county the county to set
         */
        public void setCounty(Long county) {
            this.county = county;
        }

        /**
         * @return the plot
         */
        public Long getPlot() {
            return plot;
        }

        /**
         * @param plot the plot to set
         */
        public void setPlot(Long plot) {
            this.plot = plot;
        }

        /**
         * @return the year
         */
        public Long getYear() {
            return year;
        }

        /**
         * @param year the year to set
         */
        public void setYear(Long year) {
            this.year = year;
        }

        /**
         * @return the yearType
         */
        public String getYearType() {
            return yearType;
        }

        /**
         * @param yearType the yearType to set
         */
        public void setYearType(String yearType) {
            this.yearType = yearType;
        }

        /**
         * @return the plotType
         */
        public String getPlotType() {
            return plotType;
        }

        /**
         * @param plotType the plotType to set
         */
        public void setPlotType(String plotType) {
            this.plotType = plotType;
        }
    }

    /**
     * @return the selectAll
     */
    public boolean isSelectAll() {
        return selectAll;
    }

    /**
     * @param selectAll the selectAll to set
     */
    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    /**
     * @return the filterState
     */
    public Long getFilterState() {
        return filterState;
    }

    /**
     * @param filterState the filterState to set
     */
    public void setFilterState(Long filterState) {
        this.filterState = filterState;
    }

    /**
     * @return the filterPlotType
     */
    public String getFilterPlotType() {
        return filterPlotType;
    }

    /**
     * @param filterPlotType the filterPlotType to set
     */
    public void setFilterPlotType(String filterPlotType) {
        this.filterPlotType = filterPlotType;
    }

    public int getRowCount() {
        return getPlotPhotoService().getRowCount();
    }

}
