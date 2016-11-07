<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>


<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core"
    prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"
    prefix="fmt" %>
	
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%
	String id = (String) request.getAttribute("bitstream_id");
	String resource_id= (String) request.getAttribute("resource_id");
%>
    
    <iframe src="<%= request.getContextPath() %>/viewers/ckan-recline-embed.jsp?resource_id=<%= resource_id %>&bitstream_id=<%= id %>" 
    	frameborder="0" width="100%" height="600px" data-module="data-viewer">
          <p>Your browser does not support iframes.</p>
    </iframe>