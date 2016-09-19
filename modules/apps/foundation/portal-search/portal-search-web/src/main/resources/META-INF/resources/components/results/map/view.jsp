<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/init.jsp" %>

<%@ page import="com.liferay.portal.search.web.components.results.map.portlet.SearchResultsMapDisplayContext" %>

<%@ page import="com.liferay.blogs.kernel.model.BlogsEntry" %>
<%@ page import="com.liferay.blogs.kernel.service.BlogsEntryLocalServiceUtil" %>
<%@ page import="com.liferay.journal.model.JournalArticle" %>
<%@ page import="com.liferay.message.boards.kernel.model.MBMessage" %>

<%@ page import="java.text.SimpleDateFormat" %>

<%
SearchResultsMapDisplayContext dc = new SearchResultsMapDisplayContext(request);

SearchContainer<Document> newSearchContainer = dc.getSearchResultsContainer();
%>

<style>
	.gm-style {
		font-family: inherit;
		font-size: inherit;
		font-weight: inherit;
	}

	.map-drawing-toolbar {
		background-color: #29343D;
		display: none;
		margin-bottom: 0;
		padding: 8px 10px;
	}

	.map-drawing-toolbar .map-clear-button {
		display: none;
	}

	.map-drawing-toolbar .toolbar-message {
		color: #FFF;
		padding-left: 10px;
	}

	.search-results-map-window-content {
		box-shadow: none;
		font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
		margin-bottom: 0;
		max-width: 400px;
	}

	.search-results-map-window-content.tabular-list-group .list-group-item-field, .search-results-map-window-content.tabular-list-group .list-group-item-content {
		border-width: 0;
		padding-bottom: 0;
		padding-top: 5px;
	}

	.search-results-map-window-content.tabular-list-group .list-group-item-field {
		padding-left: 0;
	}

	#content .row.search-map-list-container {
		height: 800px;
		margin: 0;
		overflow: hidden;
		position: relative;
	}

	.search-list-container, .search-map-container {
		height: 100%;
		padding: 0;
	}

	.search-list-container .main-content-body {
		margin-top: 0;
	}

	.search-list-container {
		overflow: scroll;
	}

	.search-list-sidebar {
		background-color: #FFF;
		height: 100%;
		position: absolute;
		right: -370px;
		top: 0;
		width: 370px;
	}

	.search-list-sidebar-toggle {
		background-color: #FFF;
		border: 1px solid rgba(133, 156, 173, 0.2);
		border-radius: 4px 0 0 4px;
		height: 42px;
		margin-left: -26px;
		margin-top: -13px;
		padding: 7px 0;
		position: absolute;
		text-align: center;
		top: 50%;
		width: 26px;
		z-index: 1;
	}

	.search-list-sidebar-toggle:hover {
		color: #65B6F0;
	}

	#content .row.search-map-list-container.sidebar-open .search-map-container {
		padding-right: 370px;
	}

	#content .row.search-map-list-container.sidebar-open .search-list-sidebar {
		right: 0;
	}
</style>

<%
List<Document> searchResults = newSearchContainer.getResults();

int searchResultsAmount = searchResults.size();

String searchQuery = dc.getQ();
%>

<%
PortletURL portletURL = renderResponse.createRenderURL();
%>

<liferay-frontend:management-bar
	searchContainerId="resultsContainer"
>
	<liferay-frontend:management-bar-buttons>
		<liferay-frontend:management-bar-display-buttons
			displayViews='<%= new String[] {"icon", "descriptive"} %>'
			portletURL="<%= portletURL %>"
			selectedDisplayStyle="descriptive"
		/>
	</liferay-frontend:management-bar-buttons>

	<liferay-frontend:management-bar-filters>
		<liferay-frontend:management-bar-navigation
			navigationKeys='<%= new String[] {"category", "asset-type"} %>'
			navigationParam=""
			portletURL="<%= portletURL %>"
		/>

		<liferay-frontend:management-bar-sort
			orderByCol=""
			orderByType=""
			orderColumns='<%= new String[] {"title", "display-date"} %>'
			portletURL="<%= portletURL %>"
		/>
	</liferay-frontend:management-bar-filters>
</liferay-frontend:management-bar>

<p class="search-total-label text-default">
	About <%= searchResultsAmount %> results for <strong><%= searchQuery %></strong>
</p>

<aui:row cssClass="search-map-list-container sidebar-open" id="searchMapListContainer">
	<div class="search-map-container">
		<div class="map-drawing-toolbar toolbar" id="mapDrawingToolbar">
			<div class="toolbar-group">
        		<div class="toolbar-group-content">
					<span class="toolbar-message"><strong>Draw an area</strong> where you would like to search.</span>
				</div>
			</div>

			<div class="toolbar-group-right">
				<div class="toolbar-group-content">
					<button class="btn btn-default" onclick="cancelOverlayMode();">Cancel</button>

					<button class="btn btn-default map-clear-button" onclick="clearOverlay();">Clear Area</button>

					<button class="btn btn-primary map-search-button" onclick="searchOverlay();">Search</button>
				</div>
			</div>
		</div>

		<div id="mapCanvas" style="height: 100%; width: 100%;"></div>
	</div>

	<div class="search-list-sidebar">
		<aui:a href="javascript:;"><div class="search-list-sidebar-toggle text-default" id="<portlet:namespace />searchSidebarToggle">&#8811;</div></aui:a>

		<div class="search-list-container">
			<%@ include file="/components/results/list/results_list.jspf" %>
		</div>
	</div>
</aui:row>

<script>
	var circleSVG = '<svg id="SvgjsSvg1022" xmlns="http://www.w3.org/2000/svg" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" width="20" height="20"><defs id="SvgjsDefs1023"></defs><path id="SvgjsPath1024" d="M1111.35 770.888C1106.11 770.888 1101.84 775.1550000000001 1101.84 780.399C1101.84 785.644 1106.11 789.9110000000001 1111.35 789.9110000000001C1116.6 789.9110000000001 1120.86 785.644 1120.86 780.3990000000001C1120.86 775.1550000000001 1116.6 770.8880000000001 1111.35 770.8880000000001ZM1111.35 788.182C1107.06 788.182 1103.57 784.691 1103.57 780.399C1103.57 776.108 1107.06 772.617 1111.35 772.617C1115.6399999999999 772.617 1119.1299999999999 776.108 1119.1299999999999 780.399C1119.1299999999999 784.691 1115.6399999999999 788.182 1111.35 788.182Z " fill="#859cad" transform="matrix(1,0,0,1,-1101,-770)"></path></svg>';
	var polygonSVG = '<svg id="SvgjsSvg1016" xmlns="http://www.w3.org/2000/svg" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" width="22" height="16"><defs id="SvgjsDefs1017"></defs><path id="SvgjsPath1018" d="M1108.31 787.986C1108.19 787.986 1108.07 787.965 1107.96 787.923C1107.7 787.829 1107.49 787.635 1107.38 787.386L1100.71 772.483C1100.53 772.0859999999999 1100.63 771.621 1100.96 771.3309999999999C1101.29 771.0409999999999 1101.77 770.9939999999999 1102.15 771.2079999999999L1111.8200000000002 776.6809999999998L1120.0700000000002 773.5869999999998C1120.4 773.4649999999998 1120.7700000000002 773.5159999999997 1121.0500000000002 773.7239999999997C1121.3300000000002 773.9319999999997 1121.4900000000002 774.2659999999997 1121.4600000000003 774.6109999999996L1120.8500000000004 781.6389999999997C1120.8200000000004 781.9929999999997 1120.6000000000004 782.3059999999997 1120.2700000000004 782.4589999999997L1108.7500000000005 787.8869999999997C1108.6100000000004 787.9519999999998 1108.4600000000005 787.9859999999998 1108.3100000000004 787.9859999999998ZM1103.83 774.477L1108.82 785.6279999999999L1118.86 780.8969999999999L1119.28 776.0329999999999L1112.09 778.7269999999999C1111.8 778.8339999999998 1111.48 778.8079999999999 1111.22 778.6589999999999Z " fill="#859cad" transform="matrix(1,0,0,1,-1100,-771)"></path></svg>';
	var rectangleSVG = '<svg id="SvgjsSvg1019" xmlns="http://www.w3.org/2000/svg" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" width="17" height="17"><defs id="SvgjsDefs1020"></defs><path id="SvgjsPath1021" d="M1117 771.882L1104.58 771.882C1103.6 771.882 1102.8 772.678 1102.8 773.656L1102.8 786.079C1102.8 787.058 1103.6 787.853 1104.58 787.853L1117 787.853C1117.98 787.853 1118.77 787.058 1118.77 786.079L1118.77 773.656C1118.77 772.678 1117.98 771.882 1117 771.882ZM1104.58 786.079L1104.58 773.656L1117 773.656L1117 786.079Z " fill="#859cad" transform="matrix(1,0,0,1,-1102,-771)"></path></svg>';

	var ____lat = 42.359849;
	var ____lng = -71.0586345;

	var drawingManager;
	var infoWindow;
	var map;
	var markers = [];
	var overlay;

	function DrawControl(controlContainer, map) {
		controlContainer.style.padding = '8px 10px';

		var controlWrapper = document.createElement('div');

		controlWrapper.classList.add('btn-group');
		controlWrapper.classList.add('dropdown');

		controlWrapper.innerHTML = '<button class="btn btn-default" id="mapDrawingButton" onclick="toggleDrawingMode(\'polygon\')" onmouseover="Liferay.Portal.ToolTip.show(this, \'Draw an area to search\')" type="button">' +
		polygonSVG +
		'</button>' +
		'<button class="btn btn-default dropdown-toggle" data-toggle="dropdown" type="button">' +
			'<span class="caret"></span>' +
			'<span class="sr-only">Toggle Dropdown</span>' +
		'</button>' +
		'<ul class="dropdown-menu dropdown-menu-right" role="menu">' +
			'<li><a href="javascript:;" onclick="event.preventDefault(); toggleDrawingMode(\'polygon\');">Shape</a></li>' +
			'<li><a href="javascript:;" onclick="event.preventDefault(); toggleDrawingMode(\'circle\');">Circle</a></li>' +
			'<li><a href="javascript:;" onclick="event.preventDefault(); toggleDrawingMode(\'rectangle\');">Rectangle</a></li>' +
		'</ul>';

		controlContainer.appendChild(controlWrapper);
	}

	function toggleDrawingMode(mode) {
		var drawingButtonElement = document.getElementById('mapDrawingButton');
		var drawingToolbarElement = $('#mapDrawingToolbar');

		var drawingMode = drawingManager.getDrawingMode();

		var mapCanvasElement = $('#mapCanvas');

		var mapCanvasHeight = mapCanvasElement.outerHeight();
		var toolbarHeight = drawingToolbarElement.outerHeight();

		if (drawingMode === mode) {
			drawingManager.setDrawingMode(null);

			drawingButtonElement.style.backgroundColor = '#FFF';

			drawingToolbarElement.hide();

			mapCanvasElement.height(mapCanvasHeight + toolbarHeight);
		}
		else {
			drawingManager.setDrawingMode(mode);

			drawingButtonElement.onclick = function() {
				toggleDrawingMode(mode);
			};

			drawingButtonElement.style.backgroundColor = '#EDF0F3';

			if (mode === 'circle') {
				drawingButtonElement.innerHTML = circleSVG;
			}
			else if (mode === 'polygon') {
				drawingButtonElement.innerHTML = polygonSVG;
			}
			else if (mode === 'rectangle') {
				drawingButtonElement.innerHTML = rectangleSVG;
			}

			if (!drawingToolbarElement.is(':visible')) {
				drawingToolbarElement.show();

				mapCanvasElement.height(mapCanvasHeight - toolbarHeight);
			}
		}
	}

	function cancelOverlayMode() {
		clearOverlay();

		drawingManager.setDrawingMode(null);

		var drawingToolbarElement = $('#mapDrawingToolbar');

		drawingToolbarElement.hide();

		var mapCanvasElement = $('#mapCanvas');

		var mapCanvasHeight = mapCanvasElement.outerHeight();
		var toolbarHeight = drawingToolbarElement.outerHeight();

		mapCanvasElement.height(mapCanvasHeight + toolbarHeight);

		$('#mapDrawingButton').css('background-color', '#FFF');

		toggleSearchClearButtons(true);
	}

	function clearOverlay() {
		if (overlay) {
			overlay.setMap(null);
		}

		toggleSearchClearButtons(true);
	}

	function searchOverlay() {
		if (overlay) {
			var bounds = overlay.getBounds();

			for (var i = 0; i < markers.length; i++) {
				var contained = bounds.contains(markers[i].getPosition());

				if (contained) {
					markers[i].setMap(map);
				}
				else {
					markers[i].setMap(null);
				}
			}

			toggleSearchClearButtons(false);
		}
	}

	function toggleSearchClearButtons(search) {
		var clearButton = $('#mapDrawingToolbar .map-clear-button');
		var searchButton = $('#mapDrawingToolbar .map-search-button');

		if (search && !searchButton.is(':visible')) {
			clearButton.hide();

			searchButton.show();
		}
		else if (!search && !clearButton.is(':visible')) {
			clearButton.show();

			searchButton.hide();
		}
	}

	function ZoomControl(controlContainer, map) {
		controlContainer.style.padding = '8px 10px';

		var controlWrapper = document.createElement('div');

		controlWrapper.classList.add('btn-group-vertical');

		controlContainer.appendChild(controlWrapper);

		var zoomInButton = document.createElement('button');

		zoomInButton.classList.add('btn');
		zoomInButton.classList.add('btn-default');
		zoomInButton.innerHTML = '<svg class="lexicon-icon">' +
		'<use xlink:href="<%= themeDisplay.getPathThemeImages() %>/lexicon/icons.svg#plus" /></svg>';

		controlWrapper.appendChild(zoomInButton);

		var zoomOutButton = document.createElement('button');

		zoomOutButton.classList.add('btn');
		zoomOutButton.classList.add('btn-default');
		zoomOutButton.innerHTML = '<svg class="lexicon-icon">' +
		'<use xlink:href="<%= themeDisplay.getPathThemeImages() %>/lexicon/icons.svg#hr" /></svg>';

		controlWrapper.appendChild(zoomOutButton);

		google.maps.event.addDomListener(
			zoomInButton,
			'click',
			function() {
				map.setZoom(map.getZoom() + 1);
			}
		);

		google.maps.event.addDomListener(
			zoomOutButton,
			'click',
			function() {
				map.setZoom(map.getZoom() - 1);
			}
		);
	}

	function initMap() {
		if (!google.maps.Polygon.prototype.getBounds) {
			google.maps.Polygon.prototype.getBounds = function() {
				var bounds = new google.maps.LatLngBounds();

				this.getPath().forEach(
					function(element,index) {
						bounds.extend(element);
					}
				);

				return bounds;
			}
		}

		infoWindow = new google.maps.InfoWindow({
			content: 'Boston City Hall'
		});

		// Map Options
		var mapOptions = {
			center: new google.maps.LatLng(____lat, ____lng),
			maxZoom: 18,
			scrollwheel: false,
			streetViewControl: false,
			zoom: 10,
			zoomControl: false,
		};

		// Markers
		var bounds = new google.maps.LatLngBounds();

		var searchLocations = <%= dc.getMapMarkersJSON() %>;

		map = new google.maps.Map(document.getElementById('mapCanvas'), mapOptions);

		for (var i = 0; i < searchLocations.length; i++) {
			var p = searchLocations[i];

			var latlng = new google.maps.LatLng(p.lat, p.lng);

			var marker = addMarker(
				{
					assetTypeName: p.assetTypeName,
					date: p.date,
					position: latlng,
					summary: p.summary,
					title: p.title,
					userName: p.userName
				}
			);

			bounds.extend(marker.position);
		}

		if (searchLocations.length > 0) {
			map.fitBounds(bounds);
			map.panToBounds(bounds);
		}

		// Custom Drawing Controls
		var drawControlContainer = document.createElement('div');

		new DrawControl(drawControlContainer, map);

		drawControlContainer.index = 1;
		map.controls[google.maps.ControlPosition.RIGHT_TOP].push(drawControlContainer);

		// Custom Zoom Controls
		var zoomControlContainer = document.createElement('div');

		new ZoomControl(zoomControlContainer, map);

		zoomControlContainer.index = 1;
		map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(zoomControlContainer);

		// Drawing Manager
		drawingManager = new google.maps.drawing.DrawingManager(
			{
				circleOptions: {
					fillOpacity: 0.2
				},
				drawingControl: false,
				drawingControlOptions: {
					drawingModes: ['circle', 'polygon', 'rectangle']
				},
				polygonOptions: {
					fillOpacity: 0.2
				},
				rectangleOptions: {
					fillOpacity: 0.2
				}
			}
		);

		drawingManager.setMap(map);

		google.maps.event.addListener(
			drawingManager,
			'overlaycomplete',
			function(event) {
				drawingManager.setOptions(
					{
						drawingControl: false
					}
				);

				drawingManager.setDrawingMode(null);

				overlay = event.overlay;

				var searchButton = $('#mapDrawingToolbar .map-search-button');
			}
		);
	}

	function addMarker(data) {
		var marker = new google.maps.Marker(
			{
				icon: {
					url: 'data:image/svg+xml;utf-8, \ <svg id="SvgjsSvg1000" xmlns="http://www.w3.org/2000/svg" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" width="46" height="46"><defs id="SvgjsDefs1001"><filter id="SvgjsFilter1008" width="200%" height="200%" x="-50%" y="-50%"><feGaussianBlur id="SvgjsFeGaussianBlur1009" stdDeviation="1.5 " result="SvgjsFeGaussianBlur1009Out" in="SourceGraphic"></feGaussianBlur></filter><clipPath id="SvgjsClipPath1014"><path id="SvgjsPath1013" d="M724 1048C735.046 1048 744 1056.95 744 1068C744 1079.05 735.046 1088 724 1088C712.954 1088 704 1079.05 704 1068C704 1056.95 712.954 1048 724 1048Z " fill="#ffffff"></path></clipPath></defs><path id="SvgjsPath1007" d="M724 1050C735.046 1050 744 1058.95 744 1070C744 1081.05 735.046 1090 724 1090C712.954 1090 704 1081.05 704 1070C704 1058.95 712.954 1050 724 1050Z " fill="#28353d" fill-opacity="0.2" filter="url(#SvgjsFilter1008)" transform="matrix(1,0,0,1,-701,-1047)"></path><path id="SvgjsPath1010" d="M724 1048C735.046 1048 744 1056.95 744 1068C744 1079.05 735.046 1088 724 1088C712.954 1088 704 1079.05 704 1068C704 1056.95 712.954 1048 724 1048Z " fill="#65b6f0" transform="matrix(1,0,0,1,-701,-1047)"></path><path id="SvgjsPath1011" d="M724 1048C735.046 1048 744 1056.95 744 1068C744 1079.05 735.046 1088 724 1088C712.954 1088 704 1079.05 704 1068C704 1056.95 712.954 1048 724 1048Z " fill="#65b6f0" transform="matrix(1,0,0,1,-701,-1047)"></path><path id="SvgjsPath1012" d="M724 1048C735.046 1048 744 1056.95 744 1068C744 1079.05 735.046 1088 724 1088C712.954 1088 704 1079.05 704 1068C704 1056.95 712.954 1048 724 1048Z " fill-opacity="0" fill="#ffffff" stroke-dasharray="0" stroke-linejoin="round" stroke-linecap="round" stroke-opacity="1" stroke="#ffffff" stroke-miterlimit="50" stroke-width="6" clip-path="url(&quot;#SvgjsClipPath1014&quot;)" transform="matrix(1,0,0,1,-701,-1047)"></path><path id="SvgjsPath1015" d="M730.003 1060L717.9970000000001 1060C716.893 1060 715.9960000000001 1060.89 715.9960000000001 1062L715.9960000000001 1074C715.9960000000001 1075.11 716.8930000000001 1076 717.9970000000001 1076L730.003 1076C731.1070000000001 1076 732.004 1075.11 732.004 1074L732.004 1062C732.004 1060.89 731.107 1060 730.003 1060ZM717.997 1074L717.997 1062L730.0029999999999 1062L730.0029999999999 1074ZM728.002 1068L719.9979999999999 1068C719.4449999999999 1068 718.997 1068.45 718.997 1069C718.997 1069.55 719.4449999999999 1070 719.9979999999999 1070L728.002 1070C728.5559999999999 1070 729.0029999999999 1069.55 729.0029999999999 1069C729.0029999999999 1068.45 728.5559999999999 1068 728.002 1068ZM725.001 1071L719.9979999999999 1071C719.4449999999999 1071 718.997 1071.45 718.997 1072C718.997 1072.56 719.4449999999999 1073 719.9979999999999 1073L725.001 1073C725.554 1073 726.001 1072.56 726.001 1072C726.001 1071.45 725.554 1071 725.001 1071ZM722.999 1064C722.999 1064.55 723.447 1065 724 1065C724.553 1065 725.001 1064.55 725.001 1064C725.001 1063.45 724.553 1063 724 1063C723.447 1063 722.999 1063.45 722.999 1064Z " fill="#ffffff" transform="matrix(1,0,0,1,-701,-1047)"></path></svg>'
				},
				map: map,
				position: data.position,
				title: data.title
			}
		);

		markers.push(marker);

		createInfoWindow(marker, data);

		return marker;
	}

	function createInfoWindow(marker, data) {
		var summary = data.summary

		if (summary.length > 100) {
			summary = summary.substring(0, 100) + "...";
		}

		var contentStr = '<div class="search-results-map-window-content tabular-list-group">' +
			'<div class="list-group-item-field">' +
				'<span class="search-asset-type-sticker sticker sticker-default sticker-lg sticker-rounded sticker-static">' +
					'<svg class="lexicon-icon">' +
						'<use xlink:href="<%= themeDisplay.getPathThemeImages() %>/lexicon/icons.svg#blogs" />' +
					'</svg>' +
				'</span>' +
			'</div>' +
			'<div class="list-group-item-content">' +
				'<h4><strong>' + data.title + '</strong></h4>' +
				'<h6 class="text-default">' +
					'<strong>' + data.assetTypeName + '</strong> &#183;' +
					'<liferay-ui:message key="written-by" /> <strong>' + data.userName + '</strong> ' +
					'<liferay-ui:message key="on-date" /> ' + data.date + '</h6>' +
				'<h6 class="search-document-content text-default">' + summary + '</h6>' +
			'</div>' +
			'</div>';

		google.maps.event.addListener(
			marker,
			'click',
			function() {
				infoWindow.setContent(contentStr);
				infoWindow.open(map, marker);
			}
		);
	}
</script>

<script
	src="https://maps.googleapis.com/maps/api/js?v=3&key=AIzaSyABOXmu2BMXwxNHbhHrTcMRLnOJQYpHbWQ&libraries=drawing&callback=initMap"
	type="text/javascript"></script>

<aui:script use="aui-base">
	$('#<portlet:namespace />searchSidebarToggle').on(
		'click',
		function() {
			var mapListContainerElement = $('#<portlet:namespace />searchMapListContainer');

			mapListContainerElement.toggleClass('sidebar-open');

			if (mapListContainerElement.hasClass('sidebar-open')) {
				$('#<portlet:namespace />searchSidebarToggle').html('&#8811;');
			}
			else {
				$('#<portlet:namespace />searchSidebarToggle').html('&#8810;');
			}

			google.maps.event.trigger(map, "resize");
		}
	);

	$('#<portlet:namespace />searchSidebarToggle').on(
		'mouseover',
		function() {
			if ($('#<portlet:namespace />searchMapListContainer').hasClass('sidebar-open')) {
				Liferay.Portal.ToolTip.show(this, 'Collapse Side Panel');
			}
			else {
				Liferay.Portal.ToolTip.show(this, 'Expand Side Panel');
			}
		}
	);
</aui:script>