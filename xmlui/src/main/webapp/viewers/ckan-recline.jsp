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
	
<%
	String id = (String) request.getAttribute("bitstream_id");
	String resource_id= (String) request.getAttribute("resource_id");
	String handle= (String) request.getAttribute("handle");
	String itemTitle= (String) request.getAttribute("itemTitle");
	String titleMessage= (String) request.getAttribute("titleMessage");
	String backMessage= (String) request.getAttribute("backMessage");
%>
<html>  
  <head>
    <meta charset="utf-8" />
    <title>${titleMessage}</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/recline/vendor/bootstrap/3.2.0/css/bootstrap.css">
    <link href="<%= request.getContextPath() %>/static/css/addon-common.css" rel="stylesheet">
  </head>
  <body>
	<header>
		<div class="navbar">
			<div class="container">
				<a href="<%= request.getContextPath()%>">
					<span class="addon-logo-left">&nbsp;</span>
					<span class="addon-logo-right">&nbsp;</span>
				</a>
			</div>
		</div>
	</header>  
    <div class="container fullheight">
	  <br>
	  <br>    
	  <h1 class="addon-title"><%= itemTitle%></h1>
		<div class="row backButton">
			<div class="col-md-12">
				<a href="<%= request.getContextPath()%>/handle/<%=handle%>">
					<%=backMessage%>
				</a>
			</div>
		</div>
    </div>

    <iframe src="<%= request.getContextPath() %>/viewers/ckan-recline-embed.jsp?resource_id=<%= resource_id %>&bitstream_id=<%= id %>" 
    	frameborder="0" width="100%" height="600px" data-module="data-viewer">
          <p>Your browser does not support iframes.</p>
    </iframe>
</body>
</html>