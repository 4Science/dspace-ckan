/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.ckan.CKANConstants;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.content.Bitstream;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.json.JSONObject;

public class CKANDatastoreProxyServiceSecurityCheck implements IProxyServiceSecurityCheck {

Logger log = Logger.getLogger(CKANDatastoreProxyServiceSecurityCheck.class);    
    @Override
    public void extraSecurityCheck(Context context, Bitstream bit, HttpServletRequest req)
            throws AuthorizeException, SQLException, IOException {
        if (bit == null)
        {
            // No bitstream found or filename was wrong -- ID invalid
            log.info("Viewer CKAN tryng to access an invalid bitstream");
            return;
        }       
        
        if(!AuthorizeServiceFactory.getInstance().getAuthorizeService().authorizeActionBoolean(context, bit, Constants.READ)){
            throw new AuthorizeException();
        }

        BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
        ServletInputStream inputStream = req.getInputStream();        
        String value = IOUtils.toString(inputStream);        
        JSONObject jOb = new JSONObject(URLDecoder.decode(value));
        String resource_id= bitstreamService.getMetadata(bit, CKANConstants.CKAN_METADATA_STRING_RESOURCEID);
        if(!StringUtils.equals(resource_id, jOb.getString("resource_id"))){
            throw new AuthorizeException("tryng to access resource_id"+ jOb.getString("resource_id")+"not related to bitstream:"+ bit.getID());
        }
    
    }
    
}