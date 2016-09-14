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

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>

<%@ page import="com.liferay.portal.search.web.internal.search.facet.portlet.SearchFacetDisplayContext" %>

<portlet:defineObjects />

<%
SearchFacetDisplayContext searchFacetDisplayContext = new SearchFacetDisplayContext(request, portletPreferences);

if (!searchFacetDisplayContext.isSearchFacetConfigured()) {
%>

	<p>Please select a Facet to display from Portlet Configuration.</p>

<%
}
else {
	searchFacetDisplayContext.includeView(pageContext);
}
%>

<aui:script sandbox="<%= true %>">
	$('.facet-value a').on(
		'click',
		function(event) {
			var term = $(event.currentTarget);
			insertParam("city", term.data('value'));
		}
	);

	function insertParam(key, value) {
	key = encodeURI(key); value = encodeURI(value);

	var kvp = document.location.search.substr(1).split('&');

	var i=kvp.length; var x; while(i--)
	{
	x = kvp[i].split('=');

	if (x[0]==key)
	{
	x[1] = value;
	kvp[i] = x.join('=');
	break;
	}
	}

	if (i<0) {kvp[kvp.length] = [key,value].join('=');}

	    //this will reload the page, it's likely better to store this until finished
	document.location.search = kvp.join('&');
	}
</aui:script>

<p style="font-size:0.5em">
	Search Facets (q=<%= searchFacetDisplayContext.getQ() %>)
</p>