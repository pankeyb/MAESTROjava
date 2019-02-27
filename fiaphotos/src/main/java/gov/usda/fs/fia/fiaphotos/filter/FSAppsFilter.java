/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usda.fs.fia.fiaphotos.filter;

import gov.usda.fs.fia.fiaphotos.util.EAuth;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author sdelucero
 */
public class FSAppsFilter implements Filter {
    private String fsAppsUrl;
    @Override
    public void init(FilterConfig fc) throws ServletException {
        fsAppsUrl = fc.getInitParameter("FS_APPS_URL");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest)request;
        HttpServletResponse servletResponse = (HttpServletResponse)response;
        
        EAuth eauth = (EAuth)servletRequest.getSession(true).getAttribute(EAuth.EAUTH);
        Object inProgress = servletRequest.getSession(true).getAttribute(EAuth.IN_PROGRESS);
        if (eauth == null) {
            if (inProgress == null) {
                servletRequest.getSession(true).setAttribute(EAuth.IN_PROGRESS, "Y"); 
                String myUrl = getMyUrl(servletRequest);
                String redirect=fsAppsUrl+myUrl.toString();
                servletResponse.sendRedirect(redirect);
            }
            else {
                String reqURI = servletRequest.getRequestURI();
                if (reqURI.endsWith("login") || reqURI.contains("simUser")) {
                    chain.doFilter(request, response);
                }
                else {
                    servletRequest.getSession().setAttribute("javax.servlet.error.status_code", "403");
                    servletRequest.getSession().setAttribute("javax.servlet.error.message", "Unable to Authenticate User");
                    servletResponse.sendError(403, "Unable to authenticate user");
                }
            }
        }
        else {
            chain.doFilter(request, response);
        }
    }
    private String getMyUrl(HttpServletRequest req) {
        String url = null;
        String path = req.getContextPath();
        String xUrl = req.getRequestURL().toString();
        int i = xUrl.indexOf(path);
        path+="/auth/login";
        url = xUrl.substring(0, i)+path;
        return url;
    }

    @Override
    public void destroy() {
        
    }
    
}
