/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.controller;

import gov.usda.fs.fia.fiaphotos.jdbc.MyProxyDataSource;
import gov.usda.fs.fia.fiaphotos.util.EAuth;
import gov.usda.fs.fia.fiaphotos.util.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.el.ELException;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author sdelucero
 */
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component(value = "userSession")
@ELBeanName(value = "userSession")
public class UserSession {

    /**
     * @return the simViewRole
     */
    public boolean isSimViewRole() {
        return simViewRole;
    }

    /**
     * @param simViewRole the simViewRole to set
     */
    public void setSimViewRole(boolean simViewRole) {
        this.simViewRole = simViewRole;
    }

    /**
     * @return the simUploadRole
     */
    public boolean isSimUploadRole() {
        return simUploadRole;
    }

    /**
     * @param simUploadRole the simUploadRole to set
     */
    public void setSimUploadRole(boolean simUploadRole) {
        this.simUploadRole = simUploadRole;
    }

    /**
     * @return the simApproveRole
     */
    public boolean isSimApproveRole() {
        return simApproveRole;
    }

    /**
     * @param simApproveRole the simApproveRole to set
     */
    public void setSimApproveRole(boolean simApproveRole) {
        this.simApproveRole = simApproveRole;
    }

    public static final int STATUS_NOT_VALIDATED = -1;
    public static final int STATUS_NOT_VALID = 0;
    public static final int STATUS_VALID = 1;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    private int sessionStatus = STATUS_NOT_VALIDATED;

    private String simEmail;
    private boolean simViewRole;
    private boolean simUploadRole;
    private boolean simApproveRole;

    private Integer userRS;

    private ArrayList<String> roles;

    private String editSource;

    public static UserSession getInstance() {
        UserSession me = null;
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            try {
                me = (UserSession) context.getApplication().evaluateExpressionGet(context, "#{userSession}", Object.class);
            } catch (ELException ele) {
                ele.printStackTrace();
            }
        }
        return me;
    }

    public boolean isValid() {
        return sessionStatus == STATUS_VALID;
    }

    public boolean isNotValidated() {
        return sessionStatus == STATUS_NOT_VALIDATED;
    }

    public String getDBUsername() {
        Query q = entityManager.createNativeQuery("select user from dual");
        String user = (String) q.getSingleResult();
        return user;
    }

    public void checkStatus() throws IOException {

        if (isNotValidated()) {
            validate();
        }
        /*
        if (!isValid()) {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/error.xhtml");
        }
        */
    }

    public void setInvalid(String code, String message) {
        sessionStatus = STATUS_NOT_VALID;
        Map<String, Object> rs = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        rs.put("javax.servlet.error.status_code", code);
        rs.put("javax.servlet.error.message", message);
    }

    public void setValid() {
        sessionStatus = STATUS_VALID;
        Map<String, Object> rs = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        rs.remove("javax.servlet.error.status_code");
        rs.remove("javax.servlet.error.message");
    }

    public boolean validate() {
        setInvalid("500", "No Authenticated User Found");
        if (getUsername() == null) {
            return false;
        }
        try {
            if (dataSource instanceof MyProxyDataSource) {
                ((MyProxyDataSource) dataSource).validateProxyUser(getUsername());
                getUserRoles();
                if (isViewRole()) {
                    setValid();
                } else {
                    throw new Exception("User does not have the proper role(s) to access this application");
                }
            }
        } catch (Exception e) {
            setInvalid("403", Utils.translateException(e).getMessage());
        }
        return sessionStatus == STATUS_VALID;
    }

    public void simulate() {
        EAuth eauth = new EAuth("0000000000000", getSimEmail(), "Simulated", "User");
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        session.setAttribute(EAuth.EAUTH, eauth);
        validate();
    }

    public void clearSimulate() {
        simEmail = null;
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        session.setAttribute(EAuth.EAUTH, null);
        
        validate();
    }

    public Integer getUserStation() {
        /*
        this will need to be parameterized when other research stations wnat to use this app
         */
        if (userRS == null) {
            Query q = entityManager.createNativeQuery("select code from FS_NIMS_RMRS.NIMS_REF_CATEGORY_CODE where category = 'REGION_STATION' and abbr = 'RMRS'");
            String rs = (String) q.getSingleResult();
            userRS = Integer.valueOf(rs);
        }
        return userRS;
    }

    public String getFullName() {
        String fullName = null;
        if (simEmail != null) {
            fullName = simEmail;
        } else {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            EAuth eauth = (EAuth) session.getAttribute(EAuth.EAUTH);
            if (eauth != null) {
                fullName = eauth.getFirstname() + " " + eauth.getLastname();
            }
        }

        return fullName;

    }

    public String getEmail() {
        String email = null;
        if (simEmail != null) {
            email = simEmail;
        } else {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            EAuth eauth = (EAuth) session.getAttribute(EAuth.EAUTH);
            if (eauth != null) {
                email = eauth.getEmail();
            }
        }
        return email;

    }

    public String getEauthId() {
        String eauthId = null;
        if (simEmail != null) {
            eauthId = "000000000000000000";
        } else {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            EAuth eauth = (EAuth) session.getAttribute(EAuth.EAUTH);
            if (eauth != null) {
                eauthId = eauth.getAuthid();
            }
        }
        return eauthId;
    }

    public String getUsername() {
        String un = null;
        String em = getEmail();
        if (em != null) {
            int x = em.indexOf("@");
            if (x > 0) {
                un = em.substring(0, x);
            }
        }
        return un;
    }

    /**
     * @return the simEmail
     */
    public String getSimEmail() {
        return simEmail;
    }

    /**
     * @param simEmail the simEmail to set
     */
    public void setSimEmail(String simEmail) {
        this.simEmail = simEmail;
    }

    public List<String> getUserRoles() {
        roles = new ArrayList<String>();
        if (simEmail == null) {
            Query q = entityManager.createNativeQuery("select granted_role from user_role_privs where granted_role like 'FIA_LOB%'");
            List l = q.getResultList();
            for (Object o : l) {
                roles.add(o.toString());
            }
        }
        else {
            if (simViewRole) {
                roles.add("FIA_LOB_VIEW");
            }
            if (simUploadRole) {
                roles.add("FIA_LOB_UPLOAD");
            }
            if (simApproveRole) {
                roles.add("FIA_LOB_APPROVE");
            }
            
        }
        return roles;
    }

    public boolean isViewable() {
        return isViewRole() || isUploadRole() || isApproveRole();
    }

    public boolean isUploadable() {
        return isUploadRole() || isApproveRole();
    }

    public boolean isApprovable() {
        return isApproveRole();
    }

    public boolean isEditable() {
        boolean b = false;
        if ("view".equals(editSource)) {
            b = isApproveRole();
        }
        if ("approve".equals(editSource)) {
            b = isApproveRole();
        }
        if ("upload".equals(editSource)) {
            b = isApproveRole() || isUploadRole();
        }

        return b;
    }

    public boolean isViewRole() {
        return hasRole("FIA_LOB_VIEW") || isUploadRole() || isApproveRole();
    }

    public boolean isUploadRole() {
        return hasRole("FIA_LOB_UPLOAD") || isApproveRole();
    }

    public boolean isApproveRole() {
        return hasRole("FIA_LOB_APPROVE");
    }

    public boolean hasRole(String role) {
        boolean hasRole = false;
        if (roles != null) {
            for (String r : roles) {
                if (r.equals(role)) {
                    hasRole = true;
                }
            }
        }
        return hasRole;
    }

    public boolean renderContent(Object o) {
        return false;
    }

    public boolean isRenderContent() {
        UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
        String vid = view.getViewId();
        String id = view.getId();
        String cid = view.getClientId();

        return true;
    }

    /**
     * @return the editSource
     */
    public String getEditSource() {
        return editSource;
    }

    /**
     * @param editSource the editSource to set
     */
    public void setEditSource(String editSource) {
        this.editSource = editSource;
    }

}
