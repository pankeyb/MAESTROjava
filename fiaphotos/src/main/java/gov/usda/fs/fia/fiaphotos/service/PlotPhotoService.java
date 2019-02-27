/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.service;

import gov.usda.fs.fia.fiaphotos.controller.LookupBean;
import gov.usda.fs.fia.fiaphotos.controller.PhotosController;
import gov.usda.fs.fia.fiaphotos.model.LobDocPlotLink;
import gov.usda.fs.fia.fiaphotos.model.LobDocument;
import gov.usda.fs.fia.fiaphotos.model.LobDocumentAttr;
import gov.usda.fs.fia.fiaphotos.model.NimsPlotTbl;
import gov.usda.fs.fia.fiaphotos.util.PlotPhotoData;
import gov.usda.fs.fia.fiaphotos.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import gov.usda.fs.fia.fiaphotos.persistence.LobDocumentRepository;
import gov.usda.fs.fia.fiaphotos.persistence.LobDocPlotLinkRepository;
import gov.usda.fs.fia.fiaphotos.persistence.LobDocumentAttrRepository;
import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;


/**
 *
 * @author sdelucero
 */
public abstract class PlotPhotoService {

    Pageable pageControl;
    List<LobDocPlotLink> data;
    int rowCount = 0;
    private int pageSize = 10;
    int maxPage = 0;
    LobDocPlotLink qbeDocPlotLink;

    @Autowired
    EntityManager entityManager;
    @Autowired
    private LobDocPlotLinkRepository docPlotLinkRepos;
    @Autowired
    private LobDocumentRepository lobDocRepos;
    @Autowired
    private LookupBean lookupBean;

    public PlotPhotoService() {
        List<String> sortFld = new ArrayList<String>();
        sortFld.add("LobDocument.FileName");
        pageControl = PageRequest.of(0, pageSize, new Sort(Sort.Direction.ASC, sortFld));
    }

    public abstract LobDocPlotLink getPlotLinkQBE();

    public int getMaxPage() {
        return maxPage;
    }

    public int getCurrentPage() {
        return pageControl.getPageNumber();
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public List<LobDocPlotLink> getData() throws Exception {
        if (data == null) {
            load();
        }
        return data;
    }

    public void load() throws Exception {

        Example<LobDocPlotLink> example = Example.of(getPlotLinkQBE());

        Page<LobDocPlotLink> page = findAllPlotPhotos(example, pageControl);
        Long l = new Long(page.getTotalElements());
        this.setRowCount(l.intValue());
        maxPage = getRowCount() / getPageSize();
        data = page.getContent();

    }

    public void filter(PhotosController.FilterModel filter) throws Exception {
        LobDocPlotLink qbeDocPlotLink = getPlotLinkQBE();
        qbeDocPlotLink.getLobDocument().setArchived("N");
        // qbeDocPlotLink.setNimsPlotTbl(new NimsPlotTbl());
        qbeDocPlotLink.getNimsPlotTbl().setStateCd(filter.getState());
        qbeDocPlotLink.getNimsPlotTbl().setCountyCd(filter.getCounty());
        if ("PLOT".equals(filter.getPlotType())) {
            qbeDocPlotLink.getNimsPlotTbl().setPlot(filter.getPlot());
        } else if ("PLOTFIADB".equals(filter.getPlotType())) {
            qbeDocPlotLink.getNimsPlotTbl().setPlotFiaDb(filter.getPlot());
        }

        if ("MEASURE".equals(filter.getYearType())) {
            qbeDocPlotLink.getNimsPlotTbl().setMeasYr(filter.getYear());
        } else if ("INVENTORY".equals(filter.getYearType())) {
            qbeDocPlotLink.getNimsPlotTbl().setInvYr(filter.getYear());
        } else if ("FLDSEASON".equals(filter.getYearType())) {
            qbeDocPlotLink.getNimsPlotTbl().getPrefieldPlot().setFieldSeasonYr(filter.getYear());
            // qbeDocPlotLink.getNimsPlotTbl().setFieldSeasonYr(filter.getYear());
        }
        load();
        pageControl = pageControl.first();
    }

    public void pageFirst() throws Exception {
        pageControl = pageControl.first();
        load();
    }

    public void pagePrevious() throws Exception {
        pageControl = pageControl.previousOrFirst();
        load();
    }

    public void pageNext() throws Exception {
        pageControl = pageControl.next();
        load();
    }

    public void pageLast() throws Exception {
        Sort sort = pageControl.getSort();
        int p = getRowCount() / getPageSize();
        pageControl = PageRequest.of(p, getPageSize(), sort);
        load();
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void resetPageSize() throws Exception {
        Sort sort = pageControl.getSort();
        pageControl = PageRequest.of(0, getPageSize(), sort);
        load();
    }

    public Page<LobDocPlotLink> findAllPlotPhotos(Example<LobDocPlotLink> example, Pageable pageControl) {
        example.getProbe().setLinkType("PLOT_PHOTO");
        return docPlotLinkRepos.findAll(example, pageControl);
    }

    public long countAllPlotPhotos(Example<LobDocPlotLink> example) {
        return docPlotLinkRepos.count(example);
    }

    public LobDocument getPrimaryPhoto(PlotPhotoData ppd) {
        LobDocument qbe = new LobDocument();
        qbe.setFileName(ppd.getFileName());
        Example<LobDocument> example = Example.of(qbe);

        List<LobDocument> list = lobDocRepos.findAll(example);
        return (list == null ? null : list.get(0));
    }

    public boolean isDupPrimaryPhoto(PlotPhotoData ppd) {
        boolean dup = false;
        if (!"Other".equals(ppd.getDirection())) {
            LobDocument qbe = new LobDocument();
            qbe.setFileName(ppd.getFileName());
            Example<LobDocument> example = Example.of(qbe);

            List<LobDocument> list = lobDocRepos.findAll(example);
            dup = list.size() > 0;
        }
        return dup;

        /*
        LobDocPlotLink ex = new LobDocPlotLink(true);
        ex.getLobDocument().setArchived("N");
        ex.getNimsPlotTbl().setStateCd(ppd.getStateCode());
        ex.getNimsPlotTbl().setCountyCd(ppd.getCountyCode());
        ex.getNimsPlotTbl().setPlot(ppd.getPlotCode());
        ex.getNimsPlotTbl().setMeasYr(ppd.getYear());
        ex.getLobDocument().setArchived("N");
        String dir = ppd.getDirection();
        ex.getLobDocument().getLobDocumentAttrList().add(new LobDocumentAttr("DIRECTION", dir));
        Example<LobDocPlotLink> example = Example.of(ex);
        
        List<LobDocPlotLink> list = docPlotLinkRepos.findAll(example);
        return list.size()+1;
         */
    }

    /*
    public Page<LobDocPlotLink> findAllPlotPhotos(Pageable pageControl) {
        return docPlotLinkRepos.findAll(pageControl);
    }
     */
    @Transactional
    public Optional<LobDocPlotLink> findById(String cn) {
        Optional<LobDocPlotLink> link = docPlotLinkRepos.findById(cn);
        if (link.isPresent()) {
            link.get().getLobDocument().getContent();
            link.get().getLobDocument().getLobDocumentAttrList().size();
        }
        return link;
    }

    @Transactional
    public LobDocPlotLink save(LobDocPlotLink docPlotLink) throws Throwable {
        try {
            docPlotLink.setLinkType("PLOT_PHOTO");
            return docPlotLinkRepos.saveAndFlush(docPlotLink);
        } catch (Exception e) {
            e.printStackTrace();
            throw Utils.translateException(e);
        }
    }

    @Transactional
    public LobDocPlotLink insert(LobDocPlotLink docPlotLink) throws Throwable {
        try {
            docPlotLink.setLinkType("PLOT_PHOTO");
            return docPlotLinkRepos.saveAndFlush(docPlotLink);
        } catch (Exception e) {
            e.printStackTrace();
            throw Utils.translateException(e);
        }
    }

    public LobDocument save(LobDocument lobDocument) throws Throwable {
        try {
            return lobDocRepos.saveAndFlush(lobDocument);
        } catch (Exception e) {
            throw Utils.translateException(e);
        }
    }

    @Transactional
    public LobDocPlotLink replace(LobDocPlotLink docPlotLink, LobDocument lobDocument) throws Throwable {
        LobDocPlotLink link = null;
        try {
            docPlotLink.setLinkType("PLOT_PHOTO");
            link = docPlotLinkRepos.save(docPlotLink);

            lobDocRepos.save(lobDocument);
        } catch (Exception e) {
            throw Utils.translateException(e);
        }
        return link;
    }

    @Transactional
    public LobDocument replace(LobDocument lobDocument1, LobDocument lobDocument2) throws Throwable {
        LobDocument doc1 = null;
        try {
            lobDocument1.setRscd(22);
            doc1 = lobDocRepos.save(lobDocument1);
            lobDocument2.setRscd(22);
            lobDocRepos.save(lobDocument2);
        } catch (Exception e) {
            throw Utils.translateException(e);
        }
        return doc1;
    }

    @Transactional
    public void remove(String linkCn) throws Throwable {

        Optional<LobDocPlotLink> optLink = docPlotLinkRepos.findById(linkCn);
        if (!optLink.isPresent()) {
            throw new Throwable("Plot Photo Link Not Found");
        }
        LobDocPlotLink link = optLink.get();
        try {
            link.getLobDocument().setArchived("Y");
            lobDocRepos.save(link.getLobDocument());
        } catch (Throwable e) {
            e.printStackTrace();
            throw Utils.translateException(e);
        }
        return;
    }

    public LookupBean getLookupBean() {
        return lookupBean;
    }

    @Transactional
    public String getNextSeq() {
        Query query = entityManager.createNativeQuery("select db_instance.generate_global_id('NIMS_SEQ') from dual");
        String seq = (String)query.getSingleResult();
        return seq;
        /*
        StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery("db_instance.generate_global_id")
                .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(2, String.class, ParameterMode.OUT)
                .setParameter(1, "NIMS_SEQ");
        query.execute();
        String seq = (String) query.getOutputParameterValue(2);
        return seq;
        */
    }

}
