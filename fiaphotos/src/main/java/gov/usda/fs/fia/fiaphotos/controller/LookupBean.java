/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.usda.fs.ead.oidc.exception.TokenException;
import gov.usda.fs.ead.oidc.exception.TokenNotFoundException;
import gov.usda.fs.ead.oidc.token.TokenUtils;
import gov.usda.fs.fia.fiaphotos.model.NimsCounty;
import gov.usda.fs.fia.fiaphotos.model.NimsPlotTbl;
import gov.usda.fs.fia.fiaphotos.model.State;
import gov.usda.fs.fia.fiaphotos.persistence.NimsCountyRepository;
import gov.usda.fs.fia.fiaphotos.persistence.PlotRepository;
import gov.usda.fs.fia.fiaphotos.persistence.StateRepository;
import gov.usda.fs.fia.fiaphotos.util.EAuth;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.ManagedBean;
import javax.el.ELException;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpSession;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Example;

/**
 *
 * @author sdelucero
 */
@ManagedBean(value = "lookupBean")
@ELBeanName(value = "lookupBean")
@SessionScoped
public class LookupBean {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LookupBean.class);

    @Autowired
    EntityManager entityManager;
    
    @Autowired
    private StateRepository stateRepository;

    @Autowired
    NimsCountyRepository countyRepository;

    @Autowired
    PlotRepository plotRepository;

    private List<State> states;
    private Map<Long, List<NimsCounty>> countyMap;
    private Map<String,List<Long>> plotMap; 

    public LookupBean() {
        countyMap = new HashMap<Long, List<NimsCounty>>();
        plotMap = new HashMap<String,List<Long>>();
    }

    public static LookupBean getInstance() {
        LookupBean me = null;
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            me = (LookupBean)context.getApplication().evaluateExpressionGet(context, "#{lookupBean}", Object.class);
        }
        catch (ELException ele) {
            
        }
        return me;
    }
    public List<State> getStates() {
        if (states == null) {
            states = stateRepository.findDistinctStates();
        }
        return states;
    }

    public Map<Long, String> getStateNameMap() {
        HashMap<Long, String> map = new HashMap<Long, String>();
        List<State> sts = getStates();
        for (State s : sts) {
            map.put(s.getStateCd(), s.getStateNm());
        }
        return map;
    }

    public String getStateName(Long stateCd) {

        return (stateCd == null ? null : getStateNameMap().get(stateCd));
    }

    public List<SelectItem> getStateItems() {
        ArrayList<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem(null, "-- select --"));
        for (State s : getStates()) {
            items.add(new SelectItem(s.getStateCd(), s.getStateNm()));
        }
        return items;
    }
    public List<NimsCounty> getCounties(Long stateCd) {
        List<NimsCounty> counties = null;
        if (stateCd != null) {
            counties = countyMap.get(stateCd);
            if (counties == null) {
                counties = countyRepository.findDistinctCounties(stateCd, stateCd);
                countyMap.put(stateCd, counties);
            }
        }
        return counties;
    }
    public List<SelectItem> getCountyItems(Long stateCd) {

        ArrayList<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem(null, "-- select --"));
        if (stateCd != null) {
            List<NimsCounty> counties = countyMap.get(stateCd);
            if (counties == null) {
                counties = countyRepository.findDistinctCounties(stateCd, stateCd);
                countyMap.put(stateCd, counties);
            }
            for (NimsCounty county : counties) {
                items.add(new SelectItem(county.getCountyCd(), county.getCountyNm()));
            }
        }
        return items;
    }

    public Map<Long, String> getCountyNameMap(Long stateCd) {
        List<NimsCounty> counties = countyMap.get(stateCd);
        if (counties == null) {
            counties = countyRepository.findDistinctCounties(stateCd, stateCd);
            countyMap.put(stateCd, counties);
        }
        Map<Long, String> cmap = new HashMap<Long, String>();
        for (NimsCounty c : counties) {
            cmap.put(c.getCountyCd(), c.getCountyNm());
        }
        return cmap;
    }

    public String getCountyName(Long stateCd, Long countyCd) {
        return ((stateCd == null || countyCd == null) ? null : getCountyNameMap(stateCd).get(countyCd));
    }
    public List<Long> getPlots(Long stateCd, Long countyCd) {
        List<Long> plots  = null;
        
        if (stateCd != null && countyCd != null) {
            plots  = plotMap.get(stateCd+"_"+countyCd+"_PLOT");
            if (plots == null) {
                plots = plotRepository.findPlots(stateCd, countyCd);
                plotMap.put(stateCd+"_"+countyCd+"_PLOT",plots);
            }
        }
        return plots;
    }
    public List<SelectItem> getPlotItems(Long stateCd, Long countyCd, String plotType) {
        ArrayList<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem(null, "-- select --"));
        if (stateCd != null && countyCd != null) {
            List<Long> plots = plotMap.get(stateCd+"_"+countyCd+"_"+plotType);
            if (plots == null) {
                if ("PLOT".equals(plotType)) {
                    plots = plotRepository.findPlots(stateCd, countyCd);
                }
                else if ("PLOTFIADB".equals(plotType)) {
                    plots = plotRepository.findPlotFiaDbs(stateCd, countyCd);
                }
                plotMap.put(stateCd+"_"+countyCd+"_"+plotType,plots);
            }
            for (Long plot : plots) {
                items.add(new SelectItem(plot, plot.toString()));
            }
        }
        return items;
    }
    public List<Long> getYears(Long stateCd, Long countyCd, Long plot) {
        List<Long> years = null;
        if (stateCd != null && countyCd != null && plot != null) {
            years = plotRepository.findMeasYears(stateCd, countyCd, plot);
        }
        return years;
    }
    public List<SelectItem> getYearItems(String yearType, Long stateCd, Long countyCd, Long plot) {
        ArrayList<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem(null, "-- select --"));
        if (stateCd != null && countyCd != null && plot != null) {
            List<Long> years = null;
            if ("MEASURE".equals(yearType)) {
                years = plotRepository.findMeasYears(stateCd, countyCd, plot);
            } 
            else if ("INVENTORY".equals(yearType)) {
                years = plotRepository.findInvYears(stateCd, countyCd, plot);
            } 
            else if ("FLDSEASON".equals(yearType)) {
                years = plotRepository.findFldSeasYears(stateCd, countyCd, plot);
            }
            for (Long year : years) {
                items.add(new SelectItem(year, year.toString()));
            }
        }
        return items;
    }

    public NimsPlotTbl findPlot(Long stateCd, Long countyCd, Long plot, Long year) {
        
        if (stateCd == null || countyCd == null || plot == null || year == null) {
            return null;
        }
        NimsPlotTbl qbePlot = new NimsPlotTbl();
        qbePlot.setStateCd(stateCd);
        qbePlot.setCountyCd(countyCd);
        qbePlot.setPlot(plot);
        qbePlot.setMeasYr(year);
        Example<NimsPlotTbl> example = Example.of(qbePlot);
        
        List<NimsPlotTbl> plots = plotRepository.findAll(example);
        
        return (plots == null ? null : (plots.size()>0 ? plots.get(0) : null));
    }
    public String translateDir(String s) {
        String dir = null;
        if (s != null) {
            s = s.toLowerCase();
            if (s.startsWith("ne")) {
                dir = "Northeast";
            } else if (s.startsWith("nw")) {
                dir = "Northwest";
            } else if (s.startsWith("se")) {
                dir = "Southeast";
            } else if (s.startsWith("sw")) {
                dir = "Southwest";
            } else if (s.startsWith("n")) {
                dir = "North";
            } else if (s.startsWith("s")) {
                dir = "South";
            } else if (s.startsWith("e")) {
                dir = "East";
            } else if (s.startsWith("w")) {
                dir = "West";
            } else {
                dir = s;
            }
        }
        return dir;
    }
    
    public String getLinkSchema() {
        /*
        this will need to be parameterized when other research stations wnat to use this app
        */
        return "FS_NIMS_RMRS";
    }
    public String getLinkTable() {
        /*
        this will need to be parameterized when other research stations wnat to use this app
        */
        return "NIMS_PLOT_TBL";
    }

}
