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
<%@page import="com.liferay.portal.kernel.util.StringPool"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@page import="com.liferay.portal.search.web.search.facet.portlet.SearchFacetDisplayContext"%>
<%@ page import="com.liferay.portal.kernel.search.facet.collector.TermCollector" %>
<%@ page import="java.util.List" %>

<portlet:defineObjects />

<%
String titlePref = "title";
String title = portletPreferences.getValue(titlePref, StringPool.BLANK);
%>

<div class="panel panel-default">
	<div class="panel-heading">
		<div class="panel-title">
			<liferay-ui:message key="<%= title %>" />
		</div>
	</div>
	<div class="panel-body">
		<div>
			<ul class="list-unstyled users">
				<li class="default facet-value">
					<a class="text-default" data-value="" href="javascript:;"><liferay-ui:message key="All Cities" /></a>
				</li>

				<%
				SearchFacetDisplayContext dc = new SearchFacetDisplayContext(request, portletPreferences);
				
				List<TermCollector> termCollectors = dc.getTermCollectors();
				
				for (int i = 0; i < termCollectors.size(); i++) {
					TermCollector termCollector = termCollectors.get(i);
				%>
					<li class="facet-value">
						<a class="text-primary" data-value="<%= termCollector.getTerm() %>" href="javascript:;">
							<%= termCollector.getTerm() %>
								<span class="frequency">(<%= termCollector.getFrequency() %>)</span>
						</a>
					</li>
				<%
				}
				%>

			</ul>
		</div>
	</div>
</div>
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
	
	    if(i<0) {kvp[kvp.length] = [key,value].join('=');}
	
	    //this will reload the page, it's likely better to store this until finished
	    document.location.search = kvp.join('&'); 
	}
</aui:script>
<p style="font-size:0.5em">
Search Facets (q=<%= dc.getQ() %>)
</p>