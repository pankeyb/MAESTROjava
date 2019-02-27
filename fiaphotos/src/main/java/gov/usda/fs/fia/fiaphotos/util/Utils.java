/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.util;

import gov.usda.fs.fia.fiaphotos.controller.LookupBean;
import javax.el.ELException;
import javax.faces.context.FacesContext;
import gov.usda.fs.fia.fiaphotos.controller.EditImageContent;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;

/**
 *
 * @author sdelucero
 */
public class Utils {

    public static EditImageContent getImageContentBean(String beanName) {
        EditImageContent bean = null;
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            bean = (EditImageContent) context.getApplication().evaluateExpressionGet(context, "#{"+beanName+"}", Object.class);
        } catch (ELException ele) {

        }
        return bean;
    }
    public static Object getBean(String beanName) {
        Object obj = null;
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            obj = context.getApplication().evaluateExpressionGet(context, "#{"+beanName+"}", Object.class);
        }
        return obj;
    }
    public static Throwable translateException(Throwable e) {
        Throwable t = e;
        if (e instanceof DataAccessException) {
            t = ((DataAccessException)e).getMostSpecificCause();
        }
        if (e instanceof TransactionException) {
            t = ((TransactionException)e).getMostSpecificCause();
        }
        
        return t;
    }

}
