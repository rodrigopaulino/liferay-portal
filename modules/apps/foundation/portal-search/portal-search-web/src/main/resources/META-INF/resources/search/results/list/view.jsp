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

<%@page import="java.util.List"%>
<%@page import="com.liferay.portal.kernel.search.Field"%>
<%@page import="com.liferay.portal.kernel.search.Document"%>
<%@ page import="com.liferay.portal.search.web.search.results.list.portlet.SearchResultsListDisplayContext" %>

<portlet:defineObjects />

<%
SearchResultsListDisplayContext dc = new SearchResultsListDisplayContext(request);

List<Document> documentList = (List) request.getAttribute("DOCUMENT_LIST");
%>

<ul class="tabular-list-group"> 
<%
for (Document document : documentList) {
%>
	<li class="list-group-item"> 
		<div class="list-group-item-field"> 
			<div class="user-icon-color-4 user-icon-lg user-icon user-icon-default"> 
				<span>TT</span> 
			</div> 
		</div> 
		<div class="list-group-item-content"> 
			<h5> 
				<a href=""> 
					<%= document.get(Field.TITLE) %>
				</a> 
			</h5> 
			<h6 class="text-default"> Journal Article </h6> 
		</div> 
	</li> 
<%
}
%>
</ul> 

<p style="font-size:0.5em">
Search Results List (q=<%= dc.getQ() %>)
</p>