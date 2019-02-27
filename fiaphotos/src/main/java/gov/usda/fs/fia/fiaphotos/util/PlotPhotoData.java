/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.util;

import gov.usda.fs.fia.fiaphotos.controller.LookupBean;
import gov.usda.fs.fia.fiaphotos.controller.UploadController;
import gov.usda.fs.fia.fiaphotos.model.LobDocPlotLink;
import gov.usda.fs.fia.fiaphotos.model.LobDocument;
import gov.usda.fs.fia.fiaphotos.model.LobDocumentAttr;
import gov.usda.fs.fia.fiaphotos.model.NimsPlotTbl;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author sdelucero
 */
public class PlotPhotoData {

    private PlotPhotoData saveState;
    private UploadedFile uploadedFile;
    private String fileName;
    private long size;
    private String contentType;
    private String uuid;
    private byte content[];
    private String errorMsg;
    private NimsPlotTbl plotRecord;
    private LobDocPlotLink linkRecord;

    private Long stateCode;
    private String stateName;
    private Long countyCode;
    private String countyName;
    private Long plotCode;
    private Long year;

    private String title;
    private String description;
    private boolean duplicate = false;
    private String addOrReplace;
    
    private String fileExt;

    private List<PhotoAttribute> attributes;
    private int subplot;

    public boolean isReplace() {
        return "REPLACE".equals(addOrReplace);
    }
    public void setReplace(boolean replace) {
        addOrReplace = (replace ? "REPLACE" : null);
    }
    /**
     * @return the addOrReplace
     */
    public String getAddOrReplace() {
        return addOrReplace;
    }

    /**
     * @param addOrReplace the addOrReplace to set
     */
    public void setAddOrReplace(String addOrReplace) {
        this.addOrReplace = addOrReplace;
    }
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the uploadedFile
     */
    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    /**
     * @param uploadedFile the uploadedFile to set
     */
    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getStateName() {
        return LookupBean.getInstance().getStateName(stateCode);
    }

    /**
     * @return the stateCode
     */
    public Long getStateCode() {
        return stateCode;
    }

    /**
     * @param stateCode the stateCode to set
     */
    public void setStateCode(Long stateCode) {
        this.stateCode = stateCode;
        stateName = LookupBean.getInstance().getStateName(stateCode);
    }

    /**
     * @return the countyCode
     */
    public Long getCountyCode() {
        return countyCode;
    }

    /**
     * @param countyCode the countyCode to set
     */
    public void setCountyCode(Long countyCode) {
        this.countyCode = countyCode;
        countyName = LookupBean.getInstance().getCountyName(stateCode, countyCode);
    }

    /**
     * @return the countyName
     */
    public String getCountyName() {
        return countyName;
    }

    /**
     * @param countyName the countyName to set
     */
    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    /**
     * @return the plotCode
     */
    public Long getPlotCode() {
        return plotCode;
    }

    /**
     * @param plotCode the plotCode to set
     */
    public void setPlotCode(Long plotCode) {
        this.plotCode = plotCode;
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

    public PhotoAttribute getAttribute(String name) {
        PhotoAttribute attr = null;
        for (PhotoAttribute a : attributes) {
            if (a.getName().equals(name)) {
                attr = a;
            }
        }
        return attr;
    }

    /**
     * @return the direction
     */
    public String getDirection() {
        PhotoAttribute attr = getAttribute("DIRECTION");
        if (attr == null) {
            attr = new PhotoAttribute("DIRECTION", "North");
            attributes.add(attr);
        }
        return (attr != null ? attr.getValue() : null);
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(String direction) {
        PhotoAttribute attr = getAttribute("DIRECTION");
        if (attr == null) {
            attr = new PhotoAttribute("DIRECTION", direction);
            attributes.add(attr);
        } else {
            attr.setValue(direction);
        }
    }
    public PlotPhotoData() {
        super();
attributes = new ArrayList<>();        
    }
    public PlotPhotoData(PlotPhotoData saveState) {
        this.countyCode = saveState.getCountyCode();
        this.countyName = saveState.getCountyName();
        this.stateCode = saveState.getStateCode();
        this.stateName = saveState.getStateName();
        this.year = saveState.getYear();
        this.title = saveState.getTitle();
        this.description = saveState.getDescription();
        this.fileName = saveState.getFileName();
        this.fileExt = saveState.fileExt;
        attributes = new ArrayList<>();
        for (PhotoAttribute attr : saveState.attributes) {
            this.attributes.add(new PhotoAttribute(attr));
        }
        this.subplot = saveState.getSubplot();
        this.plotRecord = saveState.getPlotRecord();
        this.linkRecord = saveState.getLinkRecord();
        

    }
    public PlotPhotoData(LobDocPlotLink link) throws SQLException {
        attributes = new ArrayList<>();
        fileName = link.getLobDocument().getFileName();
        fileExt = FilenameUtils.getExtension(fileName);
        stateCode = link.getNimsPlotTbl().getStateCd();
        countyCode = link.getNimsPlotTbl().getCountyCd();
        plotCode = link.getNimsPlotTbl().getPlot();
        year = link.getNimsPlotTbl().getMeasYr();
        contentType = link.getLobDocument().getContentType();
        size = link.getLobDocument().getFileSize();
        uuid = link.getLobDocument().getCn();
        // content = link.getLobDocument().getContent().getBytes(1, (int)size);
        title = link.getLobDocument().getTitle();
        description = link.getLobDocument().getDescription();
        for (LobDocumentAttr attr : link.getLobDocument().getLobDocumentAttrList()) {
            attributes.add(new PhotoAttribute(attr.getCn(),attr.getAttrName(),attr.getAttrValue()));
        }
        stateName = LookupBean.getInstance().getStateName(stateCode);
        countyName = LookupBean.getInstance().getCountyName(stateCode, countyCode);
        setPlotRecord(link.getNimsPlotTbl());
        setLinkRecord(link);
        
        int dash = fileName.indexOf("_");
        String s1 = fileName.substring(dash + 1);
        if (s1.length() > 5) {
            String sp = s1.substring(5, 6);
            subplot = Integer.parseInt(sp);
        }

        saveState = new PlotPhotoData(this);
        
    }
    public PlotPhotoData(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
        attributes = new ArrayList<PhotoAttribute>();
        fileName = uploadedFile.getFileName();
        fileExt = FilenameUtils.getExtension(fileName);
        subplot = 1;
        try {
            int dash = fileName.indexOf("_");
            String s = fileName.substring(0, dash);
            String s1 = fileName.substring(dash + 1);
            int fnl = s.length();
            if (fnl > 1) {
                stateCode = new Long(s.substring(0, 2));
            }
            if (fnl > 4) {
                countyCode = new Long(s.substring(2, 5));
            }
            if (fnl > 5) {
                plotCode = new Long(s.substring(5));
            }

            if (s1.length() > 3) {
                year = new Long(s1.substring(0, 4));
            }
            if (s1.length() > 4) {
                String d = s1.substring(4, 5);
                attributes.add(new PhotoAttribute("DIRECTION", LookupBean.getInstance().translateDir(d)));
            }
            if (s1.length() > 5) {
                String sp = s1.substring(5, 6);
                subplot = Integer.parseInt(sp);
            }
        } catch (Exception e) {
        }
        contentType = uploadedFile.getContentType();
        size = uploadedFile.getSize();
        uuid = UUID.randomUUID().toString();
        content = uploadedFile.getContents();
        stateName = LookupBean.getInstance().getStateName(stateCode);
        countyName = LookupBean.getInstance().getCountyName(stateCode, countyCode);
        title = "Plot Photo";
        saveState = new PlotPhotoData(this);
    }

    public void revertState() {
        this.countyCode = saveState.getCountyCode();
        this.countyName = saveState.getCountyName();
        this.stateCode = saveState.getStateCode();
        this.stateName = saveState.getStateName();
        this.year = saveState.getYear();
        this.title = saveState.getTitle();
        this.description = saveState.getDescription();
        this.setFileName(saveState.getFileName());
        this.attributes = saveState.getAttributes();
    }

    public List<PhotoAttribute> getAttributes() {
        return attributes;
    }

    public List<PhotoAttribute> getTags() {
        List<PhotoAttribute> tags = new ArrayList<PhotoAttribute>();

        for (PhotoAttribute attr : attributes) {
            if (attr.getName().equals("TAG")) {
                tags.add(new PhotoAttribute(attr));
            }
        }
        while (tags.size() < 4) {
            tags.add(new PhotoAttribute("", ""));
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        Iterator<PhotoAttribute> iter = attributes.iterator();
        while (iter.hasNext()) {
            PhotoAttribute attr = iter.next();
            if (attr.getName().equals("TAG")) {
                attributes.remove(attr);
            }
        }
        for (String s : tags) {
            attributes.add(new PhotoAttribute("TAG", s));
        }

    }

    public void setNewTag(String newTag) {
        if (newTag != null && !newTag.isEmpty()) {
            attributes.add(new PhotoAttribute("TAG", newTag));
        }
    }

    public String getNewTag() {
        return null;
    }
    public void removeTag(String uuid) {
        Iterator<PhotoAttribute> iter = attributes.iterator();
        while (iter.hasNext()) {
            PhotoAttribute attr = iter.next();
            if (attr.getUuid().equals(uuid)) {
                iter.remove();
            }
        }
    }
    public void saveState() {
        saveState = new PlotPhotoData(this);
    }

    public String getUuid() {
        return uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public byte[] getContent() throws SQLException {
        if (content == null && getLinkRecord() != null) {
            Long l = getLinkRecord().getLobDocument().getContent().length();
            content = getLinkRecord().getLobDocument().getContent().getBytes(1, l.intValue());
        }
        return content;
    }

    /**
     * @return the errorMsg
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * @param errorMsg the errorMsg to set
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * @return the plotRecord
     */
    public NimsPlotTbl getPlotRecord() {
        return plotRecord;
    }

    /**
     * @param plotRecord the plotRecord to set
     */
    public void setPlotRecord(NimsPlotTbl plotRecord) {
        this.plotRecord = plotRecord;
        /*
        if (plotRecord != null) {
            setFileName(PlotPhotoData.genFilename(plotRecord, this.getDirection(), fileExt));
        }
        */
    }
    public String updateFilename() {
        String fname = genFilename(this.getPlotRecord(), getDirection(), getSubplot(), fileExt);
        this.setFileName(fname);
        return fname;
    }
    /*
    public static String genFilename(LobDocPlotLink dpLink, int seq) {
        String fname = "";
        DecimalFormat format2 = new DecimalFormat("00");
        DecimalFormat format3 = new DecimalFormat("000");
        DecimalFormat format5 = new DecimalFormat("00000");
        fname += format2.format(dpLink.getNimsPlotTbl().getStateCd());
        fname += format3.format(dpLink.getNimsPlotTbl().getCountyCd());
        fname += format5.format(dpLink.getNimsPlotTbl().getPlot());
        fname += "_";
        fname += dpLink.getNimsPlotTbl().getMeasYr().toString();
        List<LobDocumentAttr> attrs = dpLink.getLobDocument().getLobDocumentAttrList();
        fname += getDirAbbrFromAttrs(attrs);
        fname += seq;

        fname += "." + FilenameUtils.getExtension(dpLink.getLobDocument().getFileName());
        return fname;
    }
    */
    public String getCurrentFilename() {
        String fname = "";
        DecimalFormat format2 = new DecimalFormat("00");
        DecimalFormat format3 = new DecimalFormat("000");
        DecimalFormat format5 = new DecimalFormat("00000");
        fname += format2.format(getStateCode());
        fname += format3.format(getCountyCode());
        fname += format5.format(getPlotCode());
        fname += "_";
        fname += getYear().toString();
        String dir = getDirection();
        
        if (dir.length() > 1) {
            dir = dir.substring(0,1);
        }
        dir = dir.toLowerCase();
        
        fname += dir;
        fname += subplot;

        fname += ".jpg";
        return fname;
    }
    public static String genFilename(NimsPlotTbl plotRecord, String dir, int subplot, String fext) {
        String fname = "";
        DecimalFormat format2 = new DecimalFormat("00");
        DecimalFormat format3 = new DecimalFormat("000");
        DecimalFormat format5 = new DecimalFormat("00000");
        fname += format2.format(plotRecord.getStateCd());
        fname += format3.format(plotRecord.getCountyCd());
        fname += format5.format(plotRecord.getPlot());
        fname += "_";
        fname += plotRecord.getMeasYr().toString();
        
        if (dir.length() > 1) {
            dir = dir.substring(0,1);
        }
        dir = dir.toLowerCase();
        
        fname += dir;
        fname += subplot;

        fname += "." + fext;
        return fname;
    }
    /*
    public static String genFilename(NimsPlotTbl plotRecord, String dir, int subplot, String fext) {
        String fname = "";
        DecimalFormat format2 = new DecimalFormat("00");
        DecimalFormat format3 = new DecimalFormat("000");
        DecimalFormat format5 = new DecimalFormat("00000");
        fname += format2.format(plotRecord.getStateCd());
        fname += format3.format(plotRecord.getCountyCd());
        fname += format5.format(plotRecord.getPlot());
        fname += "_";
        fname += plotRecord.getMeasYr().toString();
        
        if (dir.length() > 1) {
            dir = dir.substring(0,1);
        }
        dir = dir.toLowerCase();
        
        fname += dir;
        fname += seq;


        fname += "." + fext;
        return fname;
    }
    */
    public static String getDirAbbrFromAttrs(List<LobDocumentAttr> attrs) {
        String d = null;
        for (LobDocumentAttr a : attrs) {
            if ("DIRECTION".equals(a.getAttrName())) {
                d = a.getAttrValue().substring(0, 1).toLowerCase();
            }
        }
        return d;
    }
    public static String getDirFromAttrs(List<LobDocumentAttr> attrs) {
        String d = null;
        for (LobDocumentAttr a : attrs) {
            if ("DIRECTION".equals(a.getAttrName())) {
                d = a.getAttrValue();
            }
        }
        return d;
    }

    /**
     * @return the linkRecord
     */
    public LobDocPlotLink getLinkRecord() {
        return linkRecord;
    }

    /**
     * @param linkRecord the linkRecord to set
     */
    public void setLinkRecord(LobDocPlotLink linkRecord) {
        this.linkRecord = linkRecord;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the duplicate
     */
    public boolean isDuplicate() {
        return duplicate;
    }

    /**
     * @param duplicate the duplicate to set
     */
    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public boolean isPlotOrDirChanged() {
        String cn = this.getPlotRecord().getCn();
        String oldcn = saveState.getPlotRecord().getCn();
        
        String dir = this.getDirection();
        String olddir = saveState.getDirection();
        
        int sp = this.getSubplot();
        int oldsp = saveState.getSubplot();
        
        boolean changed = !(cn.equals(oldcn) && dir.equals(olddir) && sp == oldsp);
        
        return changed;
    }
    public String revertFilename() {
        this.setFileName(saveState.getFileName());
        return this.getFileName();
    }

    /**
     * @return the subplot
     */
    public int getSubplot() {
        return subplot;
    }

    /**
     * @param subplot the subplot to set
     */
    public void setSubplot(int subplot) {
        this.subplot = subplot;
    }
}
