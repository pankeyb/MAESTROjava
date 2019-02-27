/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author sdelucero
 */
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component(value = "poolStats")
@ELBeanName(value = "poolStats")
@Join(path = "/poolStats", to = "/poolStats.jsf")
public class PoolStats {
    @Autowired
    MetricRegistry metricRegistry;
    
    public List<Stat> getStats() {
        List<Stat> stats = new ArrayList<Stat>();
        SortedMap<String, Gauge>map = metricRegistry.getGauges();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            Gauge gauge = map.get(key);
            stats.add(new Stat(key, gauge.getValue()));
        }
        SortedMap<String, Histogram> histMap = metricRegistry.getHistograms();
        keys = histMap.keySet();
        for (String key : keys) {
            Histogram hist = histMap.get(key);
            
            
            stats.add(new Stat(key+".Min", hist.getSnapshot().getMin()));
            stats.add(new Stat(key+".Max", hist.getSnapshot().getMax()));
            stats.add(new Stat(key+".Avg", hist.getSnapshot().getMean()));
        }
                
        return stats;
    }
    
    public class Stat {

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the value
         */
        public Object getValue() {
            return value;
        }

        /**
         * @param value the value to set
         */
        public void setValue(Object value) {
            this.value = value;
        }
        private String name;
        private Object value;
        public Stat(String n, Object v) {
            this.name = n;
            this.value = v;
        }
    }
}
