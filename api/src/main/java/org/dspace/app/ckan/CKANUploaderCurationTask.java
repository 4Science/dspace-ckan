/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ckan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.dspace.app.util.IViewer;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.curate.Distributive;
import org.dspace.storage.bitstore.BitstreamStorageManager;
import org.json.JSONObject;

@Distributive
public class CKANUploaderCurationTask extends AbstractCurationTask {

	protected Logger log = Logger.getLogger(CKANUploaderCurationTask.class);

	private Set<String> validFormats = new HashSet<String>();
	
	private String serviceURL;
	private String serviceApiKey;
	private String ownerOrg;
	
	@Override
	public void init(Curator curator, String taskId) throws IOException {
		serviceURL = ConfigurationManager.getProperty("ckan","datastore.service.url");
		if (!serviceURL.endsWith("/")) {
			serviceURL += "/";
		}
		serviceApiKey = ConfigurationManager.getProperty("ckan","datastore.service.apiKey");
		ownerOrg = ConfigurationManager.getProperty("ckan","datastore.service.orgName");
		
		String[] formats = ConfigurationManager.getProperty("ckan", "valid-formats").split(",");
		for (String f : formats) {
			validFormats.add(f.trim());
		}
		super.init(curator, taskId);
	}
	
	@Override
	public int perform(DSpaceObject dso) throws IOException {
		distribute(dso);
        return Curator.CURATE_SUCCESS;
	}
	
	
	@Override
    protected void performItem(Item item) throws SQLException, IOException
    {
		Context c = Curator.curationContext();
		
		boolean datasetExist = false;
		String packageName= StringUtils.replace(item.getHandle(),"/","-");
		packageName = URLEncoder.encode(packageName);
		
		for (Bundle bundle : item.getBundles())
        {
            for (Bitstream b : bundle.getBitstreams())
            {
            	String sourceMeta = b.getMetadata(CKANConstants.CKAN_METADATA_STRING_RESOURCEID);
				if(StringUtils.isNotBlank(sourceMeta)){
					// skip already processed bitstreams
					continue;
				}
				
				if (b.getFormat() == null || !validFormats.contains(b.getFormat().getShortDescription())) {
            		// skip unmanaged and unknown formats
            		continue;
            	}
				
				if (!datasetExist) {
					DefaultHttpClient httpclient = new DefaultHttpClient();
					HttpGet getDataset = new HttpGet(serviceURL+"api/action/package_show?id="+packageName);
		 			HttpResponse re = httpclient.execute(getDataset);

		 			if(re.getStatusLine().getStatusCode()==200){
		 				datasetExist = true;
		 			}
		 			else{
		 				DefaultHttpClient createClient = new DefaultHttpClient();
		 				HttpPost createDataset = new HttpPost(serviceURL+"api/action/package_create");
		 	            createDataset.addHeader("Authorization", serviceApiKey);
			            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			            builder.addTextBody("owner_org", ownerOrg, ContentType.DEFAULT_BINARY);
			            builder.addTextBody("name", packageName, ContentType.DEFAULT_BINARY);
			            // 
			            HttpEntity entity = builder.build();
			            createDataset.setEntity(entity);
			            
		 	            HttpResponse res = createClient.execute(createDataset);
		 	 			if(res.getStatusLine().getStatusCode()==200){
		 	 			
		 	 				datasetExist= true;
		 	 			}
		 	 			createClient.close();
		 			}
		
		 			httpclient.close();
				}		 			

				String path = BitstreamStorageManager.absolutePath(c, b.getID());
				String fileName = b.getName();
				fileName = URLEncoder.encode(fileName);
				File file = new File(path);
				DefaultHttpClient resClient = new DefaultHttpClient();
				HttpPost post = new HttpPost(serviceURL+"api/action/resource_create");
	            post.addHeader("Authorization", serviceApiKey);
	            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	            builder.addBinaryBody("upload", file, ContentType.DEFAULT_BINARY, b.getName());
	            builder.addTextBody("package_id", packageName, ContentType.DEFAULT_TEXT);
	            builder.addTextBody("name", fileName, ContentType.DEFAULT_TEXT);
	            // 
	            HttpEntity entity = builder.build();
	            post.setEntity(entity);
	        
	            HttpResponse response = resClient.execute(post);
	            
				InputStream is = response.getEntity().getContent();
				String myString = IOUtils.toString(is, "UTF-8");
				JSONObject jObj = new JSONObject(myString);
				Boolean success = jObj.getBoolean("success");
				
				if(success){
					String resourceID = jObj.getJSONObject("result").getString("id");
//					InputStream in = new URL( serviceURL+"api/action/resource_view_list?id="+resourceID ).openStream();
//					String value = IOUtils.toString(in, "UTF-8");
					b.addMetadata(IViewer.BITSTREAM_SCHEMA, IViewer.VIEWER_ELEMENT, IViewer.PROVIDER_QUALIFIER, Item.ANY, "ckan-recline");
//					JSONObject jOb= new JSONObject(in);
//					JSONArray jResult = jObj.getJSONArray("result");
//					String resource_id= jResult.getJSONObject(0).getString("resource_id");
//					String package_id= jResult.getJSONObject(0).getString("package_id");
//					String view_id = jResult.getJSONObject(0).getString("id");
//					b.addMetadata("ckan", "packageid", null, Item.ANY, package_id );
					b.addMetadata(CKANConstants.CKAN_METADATA_RESOURCEID[0],
							CKANConstants.CKAN_METADATA_RESOURCEID[1], 
							CKANConstants.CKAN_METADATA_RESOURCEID[2], 
							Item.ANY, resourceID );
//					b.addMetadata("ckan", "viewid", null, Item.ANY, view_id );
					try {
						b.update();
					} catch (AuthorizeException e) {
						throw new RuntimeException(e.getMessage(), e); 
					}
//					in.close();
				}
				resClient.close();
				is.close();
 			}
        }
		c.commit();
	}
}
