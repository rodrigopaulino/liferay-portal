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

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<%
com.liferay.portal.search.web.internal.search.results.map.portlet.SearchResultsMapDisplayContext dc =
	new com.liferay.portal.search.web.internal.search.results.map.portlet.SearchResultsMapDisplayContext(request);
%>

<%-- --%>

<div id="map-canvas" style="height:500px; width:800px"></div>

<%-- --%>

<script
	src="https://maps.googleapis.com/maps/api/js?v=3&key=AIzaSyABOXmu2BMXwxNHbhHrTcMRLnOJQYpHbWQ"
	type="text/javascript"> </script>

<script>
	var ____lat = 42.359849;
	var ____lng = -71.0586345;

	var map;
	var panorama;

	var infoWindow = new google.maps.InfoWindow({
		content: 'Boston City Hall'
	});

	var labels = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
	var labelIndex = 0;

	function initialize() {
		var mapOptions = {
		zoom: 10,
		maxZoom: 18,
		center: new google.maps.LatLng(____lat, ____lng)
		};

		var bounds = new google.maps.LatLngBounds();

		var searchLocations = <%= dc.getMapMarkersJSON() %>;

		map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

	for (var i = 0; i < searchLocations.length; i++) {
		var p = searchLocations[i];
			var latlng = new google.maps.LatLng(p.lat, p.lng);
			var marker = addMarker(latlng, p.title, p.summary);

			bounds.extend(marker.position);
		}

		if (searchLocations.length > 0) {
		map.fitBounds(bounds);
		map.panToBounds(bounds);
		}
	}

	function addMarker(pos, title, summary) {
		var marker = new google.maps.Marker({
			position: pos,
			map: map,
			icon:  {
		    	url: 'data:image/svg+xml;utf-8, \
	      			<svg id="SvgjsSvg1000" xmlns="http://www.w3.org/2000/svg" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" width="46" height="46"><defs id="SvgjsDefs1001"><filter id="SvgjsFilter1008" width="200%" height="200%" x="-50%" y="-50%"><feGaussianBlur id="SvgjsFeGaussianBlur1009" stdDeviation="1.5 " result="SvgjsFeGaussianBlur1009Out" in="SourceGraphic"></feGaussianBlur></filter><clipPath id="SvgjsClipPath1014"><path id="SvgjsPath1013" d="M724 1048C735.046 1048 744 1056.95 744 1068C744 1079.05 735.046 1088 724 1088C712.954 1088 704 1079.05 704 1068C704 1056.95 712.954 1048 724 1048Z " fill="#ffffff"></path></clipPath></defs><path id="SvgjsPath1007" d="M724 1050C735.046 1050 744 1058.95 744 1070C744 1081.05 735.046 1090 724 1090C712.954 1090 704 1081.05 704 1070C704 1058.95 712.954 1050 724 1050Z " fill="#28353d" fill-opacity="0.2" filter="url(#SvgjsFilter1008)" transform="matrix(1,0,0,1,-701,-1047)"></path><path id="SvgjsPath1010" d="M724 1048C735.046 1048 744 1056.95 744 1068C744 1079.05 735.046 1088 724 1088C712.954 1088 704 1079.05 704 1068C704 1056.95 712.954 1048 724 1048Z " fill="#65b6f0" transform="matrix(1,0,0,1,-701,-1047)"></path><path id="SvgjsPath1011" d="M724 1048C735.046 1048 744 1056.95 744 1068C744 1079.05 735.046 1088 724 1088C712.954 1088 704 1079.05 704 1068C704 1056.95 712.954 1048 724 1048Z " fill="#65b6f0" transform="matrix(1,0,0,1,-701,-1047)"></path><path id="SvgjsPath1012" d="M724 1048C735.046 1048 744 1056.95 744 1068C744 1079.05 735.046 1088 724 1088C712.954 1088 704 1079.05 704 1068C704 1056.95 712.954 1048 724 1048Z " fill-opacity="0" fill="#ffffff" stroke-dasharray="0" stroke-linejoin="round" stroke-linecap="round" stroke-opacity="1" stroke="#ffffff" stroke-miterlimit="50" stroke-width="6" clip-path="url(&quot;#SvgjsClipPath1014&quot;)" transform="matrix(1,0,0,1,-701,-1047)"></path><path id="SvgjsPath1015" d="M730.003 1060L717.9970000000001 1060C716.893 1060 715.9960000000001 1060.89 715.9960000000001 1062L715.9960000000001 1074C715.9960000000001 1075.11 716.8930000000001 1076 717.9970000000001 1076L730.003 1076C731.1070000000001 1076 732.004 1075.11 732.004 1074L732.004 1062C732.004 1060.89 731.107 1060 730.003 1060ZM717.997 1074L717.997 1062L730.0029999999999 1062L730.0029999999999 1074ZM728.002 1068L719.9979999999999 1068C719.4449999999999 1068 718.997 1068.45 718.997 1069C718.997 1069.55 719.4449999999999 1070 719.9979999999999 1070L728.002 1070C728.5559999999999 1070 729.0029999999999 1069.55 729.0029999999999 1069C729.0029999999999 1068.45 728.5559999999999 1068 728.002 1068ZM725.001 1071L719.9979999999999 1071C719.4449999999999 1071 718.997 1071.45 718.997 1072C718.997 1072.56 719.4449999999999 1073 719.9979999999999 1073L725.001 1073C725.554 1073 726.001 1072.56 726.001 1072C726.001 1071.45 725.554 1071 725.001 1071ZM722.999 1064C722.999 1064.55 723.447 1065 724 1065C724.553 1065 725.001 1064.55 725.001 1064C725.001 1063.45 724.553 1063 724 1063C723.447 1063 722.999 1063.45 722.999 1064Z " fill="#ffffff" transform="matrix(1,0,0,1,-701,-1047)"></path></svg>'
		    },
			title: title
		});

		createInfoWindow(marker, title, summary);

		return marker;
	}

	function createInfoWindow(marker, title, summary) {
		var contentStr = '<div id="allInfo" style="width:250px;">' +
				'<div> <h2>' + title + '</h2></div><p>' + summary +
				'<p><input type="button" value="Go to Street View" onClick="streetView(\'' + marker.position + '\')"></input>' +
				'</div>';

		google.maps.event.addListener(marker, 'click', function() {
			infoWindow.setContent(contentStr);
			infoWindow.open(map,marker);
		});

	}

	function streetView(pos) {
		var posLatLng = pos.split(",");
		var plat = posLatLng[0].substring(1);
		var plng = posLatLng[1].substring(1,posLatLng[1].length-1);

		panorama = map.getStreetView();
		panorama.setPosition(new google.maps.LatLng(plat, plng));
		panorama.setPov(/** @type {google.maps.StreetViewPov} */({
			heading: 265,
			pitch: 0
		}));

		panorama.setVisible(true);
	}

	initialize();

</script>