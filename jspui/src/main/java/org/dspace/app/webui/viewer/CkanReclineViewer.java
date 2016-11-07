package org.dspace.app.webui.viewer;


import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;


import org.apache.log4j.Logger;
import org.dspace.app.ckan.CKANConstants;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
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
		String bitstream_id = Integer.toString(bitstream.getID());
		
		request.setAttribute("bitstream_id",bitstream_id);
		try {
			bitstream = Bitstream.find(context, Integer.parseInt(bitstream_id));
			if (bitstream == null)
			{
			    // No bitstream found or filename was wrong -- ID invalid
			    log.info(LogManager.getHeader(context, "invalid bitstream id", "ID="
			            + bitstream_id));
			    return;
			}		
			
			if(!AuthorizeManager.authorizeActionBoolean(context,bitstream,Constants.READ)){
				log.info("User can not read bitstream:"+bitstream_id+" handle:"+bitstream.getHandle());
				return;
			}
			
			String resource_id= bitstream.getMetadata(CKANConstants.CKAN_METADATA_STRING_RESOURCEID);
			request.setAttribute("resource_id", resource_id);
		} catch (NumberFormatException | SQLException e) {
		    log.error(e.getMessage(), e);
		}
	}
}
