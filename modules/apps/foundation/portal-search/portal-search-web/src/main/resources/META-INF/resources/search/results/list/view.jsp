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


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@page import="com.liferay.portal.kernel.search.Field"%>
<%@page import="com.liferay.portal.kernel.search.Document"%>
<%@ page import="com.liferay.portal.search.web.search.results.list.portlet.SearchResultsListDisplayContext" %>

<portlet:defineObjects />

<%
SearchResultsListDisplayContext dc = new SearchResultsListDisplayContext(request);

for (Document document : dc.getDocumentList()) {
%>
  <option><%= document.get(Field.TITLE) %></option>
<%
}
%>

<p style="font-size:0.5em">
Search Results List (q=<%= dc.getQ() %>)
</p>