
package edu.harvard.iq.dvn.api.resources;

import edu.harvard.iq.dvn.api.entities.DownloadInfo;
import edu.harvard.iq.dvn.api.exceptions.AuthorizationRequiredException;


import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author leonidandreev
 */
@Stateless

public class DownloadInfoResourceBean {
    @Context HttpHeaders headers;


    @EJB FileAccessSingletonBean singleton;
    
    public DownloadInfoResourceBean() {};

    
    // Lookup by local (database) study ID:
     
    @Path("{stdyFileId}")
    @GET
    @Produces({ "application/xml" })

    public DownloadInfo getDownloadInfo(@PathParam("stdyFileId") Long studyFileId) throws WebApplicationException, AuthorizationRequiredException {
        
        String authCredentials = null; 
        
        for (String header : headers.getRequestHeaders().keySet()) {
            if (header.equalsIgnoreCase("Authorization")) {
                String headerValue = headers.getRequestHeader(header).get(0);
                if (headerValue != null && headerValue.startsWith("Basic ")) {
                    authCredentials = headerValue.substring(6);
                }
            }
        }

        if (authCredentials == null) {
            //throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            throw new AuthorizationRequiredException(); 
        }
        
         
        //DownloadInfo mf = singleton.getMetadataFormatsAvailable(studyId);
        DownloadInfo di = singleton.getDownloadInfo(studyFileId, authCredentials);
        
        if (di == null) {
            // Study not found;
            // returning 404
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        di.setAuthUserName(authCredentials);

        return di;
    }
}




