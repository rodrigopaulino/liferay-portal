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

<aui:row cssClass="search-map-list-container">
	<aui:col cssClass="search-map-container" span="8">
		<div id="map-canvas" style="height:100%; width:100%;"></div>
	</aui:col>

	<aui:col cssClass="search-list-container" span="4">
		<%@ include file="/components/results/list/results_list.jspf" %>
	</aui:col>
</aui:row>

<script>
	var ____lat = 42.359849;
	var ____lng = -71.0586345;

	var map;
	var markers = [];
	var infoWindow;

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

		var mapOptions = {
			center: new google.maps.LatLng(____lat, ____lng),
			maxZoom: 18,
			streetViewControl: false,
			zoom: 10
		};

		var bounds = new google.maps.LatLngBounds();

		var searchLocations = <%= dc.getMapMarkersJSON() %>;

		map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

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

		var drawingManager = new google.maps.drawing.DrawingManager(
			{
				drawingControl: true,
				drawingControlOptions: {
					position: google.maps.ControlPosition.TOP_RIGHT,
					drawingModes: ['circle', 'polygon', 'rectangle']
				},
				circleOptions: {
					fillOpacity: 0.2
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
				var bounds = event.overlay.getBounds();

				for (var i = 0; i < markers.length; i++) {
					var isContained = bounds.contains(markers[i].getPosition());

					if (isContained) {
						markers[i].setMap(map);
					}
					else {
						markers[i].setMap(null);
					}
				}
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