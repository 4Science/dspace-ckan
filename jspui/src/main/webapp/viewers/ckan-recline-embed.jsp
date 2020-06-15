<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%
	String bitstream_id = (String) request.getParameter("bitstream_id");
	String resource_id= (String) request.getParameter("resource_id");
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <title>Dataset explorer</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/recline/vendor/bootstrap/3.2.0/css/bootstrap.css">
    <!-- vendor css -->
    <link href="<%= request.getContextPath() %>/recline/vendor/leaflet/0.7.3/leaflet.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/recline/vendor/leaflet.markercluster/MarkerCluster.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/recline/vendor/leaflet.markercluster/MarkerCluster.Default.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/slick.grid.css">
    
    <!-- recline css -->
    <link href="<%= request.getContextPath() %>/recline/css/map.css" rel="stylesheet">

    <link href="<%= request.getContextPath() %>/recline/css/multiview.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/recline/css/slickgrid.css"rel="stylesheet">
    <link href="<%= request.getContextPath() %>/recline/css/flot.css" rel="stylesheet">
    
    <!-- Vendor JS - general dependencies -->
    <script src="<%= request.getContextPath() %>/recline/vendor/jquery/3.4.1/jquery.min.js" type="text/javascript"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/underscore/1.4.4/underscore.js" type="text/javascript"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/backbone/1.0.0/backbone.js" type="text/javascript"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/mustache/0.5.0-dev/mustache.js" type="text/javascript"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/bootstrap/3.2.0/js/bootstrap.js" type="text/javascript"></script>

    <!-- Vendor JS - view dependencies -->
    <script src="<%= request.getContextPath() %>/recline/vendor/leaflet/0.7.3/leaflet.js" type="text/javascript"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/leaflet.markercluster/leaflet.markercluster.js" type="text/javascript"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/recline/vendor/flot/jquery.flot.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/recline/vendor/flot/jquery.flot.time.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/recline/vendor/moment/2.0.0/moment.js"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/jquery-ui-1.8.16.custom.min.js"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/jquery.event.drag-2.2.js"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/jquery.event.drop-2.2.js"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/slick.core.js"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/slick.formatters.js"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/slick.editors.js"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/slick.grid.js"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/plugins/slick.rowselectionmodel.js" type="text/javascript"></script>
    <script src="<%= request.getContextPath() %>/recline/vendor/slickgrid/2.2/plugins/slick.rowmovemanager.js" type="text/javascript"></script>

    <!-- Recline JS (combined distribution, all views) -->
    <script src="<%= request.getContextPath() %>/recline/recline.js" type="text/javascript"></script>
	<script src="<%= request.getContextPath() %>/recline/ckan.js"></script>
  </head>
  <body>
    <div class="container">
      <style type="text/css">
        .recline-slickgrid {
          height: 500px;
        }
<%--
        .changelog {
          display: none;
          border-bottom: 1px solid #ccc;
          margin-bottom: 10px;
        } --%>
      </style>
<%--  
     <div class="changelog">
        <h3>Changes</h3>
      </div>
  --%>
      <div class="data-explorer-here"></div>
      <div style="clear: both;"></div>

      <script type="text/javascript">
      
      jQuery(function($) {
    	  window.multiView = null;
    	  window.explorerDiv = $('.data-explorer-here');
    	  var dataset = createDataset();
    	  window.multiview = createMultiView(dataset);
    	  dataset.fetch().done(function(dataset) {
			  if (console) {
			    console.log(dataset.records);
			  }
			});
    	  
    	  <%--  
    	  // last, we'll demonstrate binding to changes in the dataset
    	  // this will print out a summary of each change onto the page in the
    	  // changelog section
    	  dataset.records.bind('all', function(name, obj) {
    	    var $info = $('<div />');
    	    $info.html(name + ': ' + JSON.stringify(obj.toJSON()));
    	    $('.changelog').append($info);
    	    $('.changelog').show();
    	  }); 
    	  --%>
    	});

    	function createDataset() {
    		var dataset = new recline.Model.Dataset({
    			  endpoint: '<%= request.getContextPath() %>/ckan/<%= bitstream_id %>/',
    			  id: '<%= resource_id %>',
    			  backend: 'ckan'
    			});
    	  return dataset;
    	}

    	// make MultivView
    	//
    	// creation / initialization in a function so we can call it again and again
    	var createMultiView = function(dataset, state) {
    	  // remove existing multiview if present
    	  var reload = false;
    	  if (window.multiView) {
    	    window.multiView.remove();
    	    window.multiView = null;
    	    reload = true;
    	  }

    	  var $el = $('<div />');
    	  $el.appendTo(window.explorerDiv);

    	  // customize the subviews for the MultiView
    	  var views = [
    	    {
    	      id: 'grid',
    	      label: 'Grid',
    	      view: new recline.View.SlickGrid({
    	        model: dataset
    	      })
    	    },
    	    {
    	      id: 'graph',
    	      label: 'Graph',
    	      view: new recline.View.Graph({
    	        model: dataset

    	      })
    	    },
    	    {
    	      id: 'map',
    	      label: 'Map',
    	      view: new recline.View.Map({
    	        model: dataset
    	      })
    	    }
    	  ];

    	  var multiView = new recline.View.MultiView({
    	    model: dataset,
    	    el: $el,
    	    state: state,
    	    views: views
    	  });
    	  return multiView;
    	}
      
      
      </script>
    </div>
  </body>
</html>