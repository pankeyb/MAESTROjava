/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.filter;

import gov.usda.fs.ead.oidc.OIDCTokens;
import gov.usda.fs.ead.oidc.exception.TokenException;
import gov.usda.fs.ead.oidc.filter.AuthorizationFilter;
import gov.usda.fs.ead.oidc.token.IdToken;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 *
 * @author sdelucero
 */
public class AuthFilter extends AuthorizationFilter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
       super.init(filterConfig);
    }

    @Override
    public void onSuccess(OIDCTokens tokens) {
        try {
            IdToken idToken = tokens.getIdToken();
            String eAuthId = idToken.getSubject();
            String commonName = (String)tokens.getIdToken().getClaim("usdacn");

            Logger.getLogger(AuthFilter.class.getName()).log(Level.INFO, "Auth Success: eAuthID=" + eAuthId);
        } catch (TokenException ex) {
            Logger.getLogger(AuthFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
