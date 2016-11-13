/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.security.authorize.AuthorizationException;
import org.apache.log4j.Logger;
import org.dspace.app.ckan.CKANConstants;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.json.JSONObject;

public class CKANDatastoreProxyServiceSecurityCheck implements IProxyServiceSecurityCheck {

Logger log = Logger.getLogger(CKANDatastoreProxyServiceSecurityCheck.class);	
	@Override
	public void extraSecurityCheck(Context context, Bitstream bit, HttpServletRequest req)
			throws AuthorizationException, SQLException, IOException {
		if (bit == null)
        {
            // No bitstream found or filename was wrong -- ID invalid
            log.info("Viewer CKAN tryng to access an invalid bitstream");
            return;
        }		
        
		if(!AuthorizeManager.authorizeActionBoolean(context, bit, Constants.READ)){
			throw new AuthorizationException();
		}

		ServletInputStream inputStream = req.getInputStream();
		String value = IOUtils.toString(inputStream);
		JSONObject jOb = new JSONObject(value);
		String resource_id= bit.getMetadata(CKANConstants.CKAN_METADATA_STRING_RESOURCEID);
		if(!StringUtils.equals(resource_id, jOb.getString("resource_id"))){
			throw new AuthorizationException("tryng to access resource_id"+ jOb.getString("resource_id")+"not related to bitstream:"+ bit.getID());
		}
	
	}
	
}
