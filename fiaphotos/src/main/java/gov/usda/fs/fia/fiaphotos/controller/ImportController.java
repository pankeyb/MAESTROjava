/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import gov.usda.fs.fia.fiaphotos.model.LobDocument;
import gov.usda.fs.fia.fiaphotos.model.Photo;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.sql.rowset.serial.SerialBlob;
import org.apache.commons.io.IOUtils;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.primefaces.PrimeFaces;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author sdelucero
 */
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component(value = "impBean")
@ELBeanName(value = "impBean")
@Join(path = "/import", to = "/import.jsf")
public class ImportController {

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }

    /**
     * @return the countProcessed
     */
    public long getCountProcessed() {
        return countProcessed;
    }

    public boolean isImporting() {
        return importing;
    }

    public String getStatusMsg() {
        String s = null;
        if (isImporting()) {
            s = "Processing " + countProcessed + " of " + total;
        }
        return s;
    }

    /**
     * @return the year
     */
    public long getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(long year) {
        this.year = year;
    }

    /**
     * @return the rscd
     */
    public long getRscd() {
        return rscd;
    }

    /**
     * @param rscd the rscd to set
     */
    public void setRscd(long rscd) {
        this.rscd = rscd;
    }

    /**
     * @return the statecd
     */
    public long getStatecd() {
        return statecd;
    }

    /**
     * @param statecd the statecd to set
     */
    public void setStatecd(long statecd) {
        this.statecd = statecd;
    }

    /**
     * @return the countycd
     */
    public long getCountycd() {
        return countycd;
    }

    /**
     * @param countycd the countycd to set
     */
    public void setCountycd(long countycd) {
        this.countycd = countycd;
    }

    FileModel model = null;
    private DefaultMenuModel breadCrumbs = null;
    private DefaultMenuModel rootMenu = null;
    private File rootFiles[];

    private long rscd = 23;
    private long statecd = 5;
    private long countycd = 100;
    private long year = 2018;
    private long total = 0;
    private long countProcessed = 0;
    private boolean importing = false;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ImportController.class);
    
    public FileModel getFiles() {
        return model;
    }

    public void setFiles(FileModel model) {
        this.model = model;
    }

    @Deferred
    @RequestAction
    @IgnorePostback
    public void loadData() {
        if (model == null) {
            model = new FileModel();
        }
        if (rootMenu == null) {
            rootMenu = new DefaultMenuModel();
            rootFiles = File.listRoots();
            int i = 0;
            for (File f : rootFiles) {
                DefaultMenuItem mi = new DefaultMenuItem(f.getPath());
                mi.setAjax(true);
                mi.setCommand("#{impBean.selectRoot}");
                mi.setParam("selectedRoot", new Integer(i++));
                mi.setUpdate("f1");
                rootMenu.addElement(mi);
            }
            model.refresh(new MyFile(rootFiles[0], 0, "Parent"));
        }

    }

    public void selectRoot(ActionEvent ae) {
        FacesContext context = FacesContext.getCurrentInstance();
        String root = context.getExternalContext().getRequestParameterMap().get("selectedRoot");
        File f = rootFiles[Integer.parseInt(root)];
        MyFile mf = new MyFile(f, 0, "Parent");
        model.refresh(mf);
        refreshBreadCrumbs(mf);

    }

    public void selectPath(ActionEvent ae) {

        FacesContext context = FacesContext.getCurrentInstance();
        String path = context.getExternalContext().getRequestParameterMap().get("selectedPath");
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException ex) {

        }
        if (path != null) {
            File f = new File(path);
            MyFile myFile = new MyFile(f, 0, f.isDirectory() ? "File Folder" : "File");
            refreshBreadCrumbs(myFile);
            model.refresh(myFile);

            PrimeFaces.current().scrollTo("f1:tabs");
        }
    }

    public void refreshBreadCrumbs(MyFile myFile) {
        breadCrumbs = new DefaultMenuModel();
        Stack<File> lifo = new Stack<File>();
        File mf = myFile.getTheFile();
        lifo.push(mf);
        while (mf.getParentFile() != null) {
            mf = mf.getParentFile();
            lifo.push(mf);
        }
        while (!lifo.empty()) {
            File f = lifo.pop();
            String value = f.getName();
            if (value == null || value.length() == 0) {
                value = f.getPath();
            }
            DefaultMenuItem mi = new DefaultMenuItem(value);
            String p = f.getPath();
            try {
                p = URLEncoder.encode(p, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
            }
            mi.setParam("selectedPath", p);
            mi.setAjax(true);
            mi.setUpdate("f1");
            mi.setCommand("#{impBean.selectPath}");
            breadCrumbs.addElement(mi);
        }
    }

    @Transactional
    public void save(List<LobDocument> docs) throws Exception {
        // photoRepository.saveAndFlush(doc);
        //photoRepository.saveAll(docs);
    }

    public void doImport(ActionEvent ae) {
        importing = true;
        long plotcd = 1000;
        ArrayList<LobDocument> docs = new ArrayList<LobDocument>();
        List<MyFile> myFiles = model.getWrappedData();
        total = myFiles.size();
        countProcessed = 0;
        try {
            for (MyFile mf : myFiles) {
                File f = mf.getTheFile();
                LobDocument doc = new LobDocument();
                String contentType = URLConnection.getFileNameMap().getContentTypeFor(f.getPath());
                doc.setContentType(contentType);
                doc.setFileName(f.getName());
                FileInputStream fis = new FileInputStream(f);
                Blob content = new SerialBlob(StreamUtils.copyToByteArray(fis));
                doc.setContent(content);

                docs.add(doc);
                countProcessed++;
                if (docs.size() > 20) {
                    save(docs);
                    docs.clear();
                }
            }
            if (docs.size() > 0) {
                save(docs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
        importing = false;
    }

    public class MyFile {

        private File theFile;
        private int index;
        private String type;

        public MyFile(File theFile, int index, String type) {
            this.theFile = theFile;
            this.index = index;
            this.type = type;
        }

        public String getName() {
            String n = theFile.getName();
            if ("Parent".equals(getType()) || n == null || n.length() == 0) {
                n = theFile.getPath();
            }

            return n;
        }

        public String getPath() {
            String p = theFile.getPath();

            return p;
        }

        public String getPathEnc() {
            String p = theFile.getPath();

            try {
                p = URLEncoder.encode(p, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
            }

            return p;
        }

        /**
         * @return the theFile
         */
        public File getTheFile() {
            return theFile;
        }

        /**
         * @param theFile the theFile to set
         */
        public void setTheFile(File theFile) {
            this.theFile = theFile;
        }

        /**
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /**
         * @param index the index to set
         */
        public void setIndex(int index) {
            this.index = index;
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
        }
    }

    public class FileModel extends LazyDataModel<MyFile> {

        List<MyFile> myFiles = null;

        public FileModel() {
            super();
        }

        public void buildMyFiles(File parent, File files[]) {
            int ndx = 0;
            myFiles = new ArrayList<MyFile>();
            /*
            if (parent != null) {
                myFiles.add(new MyFile(parent, ndx++, "Parent"));
            }
             */
            if (files != null) {
                for (File f : files) {
                    myFiles.add(new MyFile(f, ndx++, f.isDirectory() ? "File Folder" : "File"));
                }
            }
        }

        @Override
        public List<MyFile> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
            List<MyFile> newList = new ArrayList<MyFile>();
            try {
                this.setRowCount(myFiles.size());
                if (first < myFiles.size()) {
                    MyFile[] xFiles = new MyFile[myFiles.size()];
                    int i = first;
                    while (i < myFiles.size()) {
                        newList.add(myFiles.get(i++));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return newList;
        }

        public void refresh(MyFile myFile) {
            if (myFile != null) {
                File dir = new File(myFile.getTheFile().getPath());
                File parent = (dir.getParentFile() == null ? dir : dir.getParentFile());

                File files[] = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || isImage(file);
                    }

                    private boolean isImage(File file) {
                        String contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getPath());

                        return (contentType != null && contentType.startsWith("image"));
                    }
                });
                buildMyFiles(parent, files);
            }
        }

        public MyFile getMyFile(int index) {
            return myFiles.get(index);
        }
    }

    /**
     * @return the breadCrumbs
     */
    public DefaultMenuModel getBreadCrumbs() {
        return breadCrumbs;
    }

    /**
     * @param breadCrumbs the breadCrumbs to set
     */
    public void setBreadCrumbs(DefaultMenuModel breadCrumbs) {
        this.breadCrumbs = breadCrumbs;
    }

    /**
     * @return the rootMenu
     */
    public DefaultMenuModel getRootMenu() {
        return rootMenu;
    }

    /**
     * @param rootMenu the rootMenu to set
     */
    public void setRootMenu(DefaultMenuModel rootMenu) {
        this.rootMenu = rootMenu;
    }

    
}
