/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.viewer;


import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dspace.app.ckan.CKANConstants;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.content.Bitstream;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

public class CkanReclineViewer implements JSPViewer {

	Logger log = Logger.getLogger(CkanReclineViewer.class);

	@Override
	public boolean isEmbedded() {
		return true;
	}

	@Override
	public String getViewJSP() {
		return "ckan-recline";
	}

	@Override
	public void prepareViewAttribute(Context context, HttpServletRequest request, Bitstream bitstream) {
		
		BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
		
		String bitstream_id = bitstream.getID().toString();
		
		request.setAttribute("bitstream_id",bitstream_id);
		try {
			bitstream = bitstreamService.find(context, bitstream.getID());
			if (bitstream == null)
			{
			    // No bitstream found or filename was wrong -- ID invalid
			    log.info(LogManager.getHeader(context, "invalid bitstream id", "ID="
			            + bitstream_id));
			    return;
			}		
			
			if(!AuthorizeServiceFactory.getInstance().getAuthorizeService().authorizeActionBoolean(context,bitstream,Constants.READ)){
				log.info("User can not read bitstream:"+bitstream_id+" handle:"+bitstream.getHandle());
				return;
			}
			
			String resource_id= bitstreamService.getMetadata(bitstream, CKANConstants.CKAN_METADATA_STRING_RESOURCEID);
			request.setAttribute("resource_id", resource_id);
		} catch (NumberFormatException | SQLException e) {
		    log.error(e.getMessage(), e);
		}
	}
}
