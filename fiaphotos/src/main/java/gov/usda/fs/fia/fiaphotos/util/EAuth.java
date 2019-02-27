/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.util;

/**
 *
 * @author sdelucero
 */
public class EAuth {
    public final static String EAUTH = "EAUTH";
    public final static String IN_PROGRESS = "EAUTH_IN_PROGRESS";
    private String authid;
    private String email;
    private String firstname;
    private String lastname;

    public EAuth(String authid, String email, String firstname, String lastname) {
        this.authid = authid;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
    }
    /**
     * @return the authid
     */
    public String getAuthid() {
        return authid;
    }

    /**
     * @param authid the authid to set
     */
    public void setAuthid(String authid) {
        this.authid = authid;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the firstname
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * @param firstname the firstname to set
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * @return the lastname
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * @param lastname the lastname to set
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
}
