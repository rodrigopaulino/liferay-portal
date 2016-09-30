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

<%@page import="com.liferay.dynamic.data.lists.model.DDLRecordSet"%>
<%@page import="com.liferay.portal.kernel.dao.search.SearchContainer"%>
<%@page import="com.liferay.portal.search.web.components.advanced.search.portlet.AdvancedSearchPortletKeys"%>
<%@page import="com.liferay.portal.search.web.components.advanced.search.portlet.AdvancedSearchDisplayContextFactoryUtil"%>
<%@page import="com.liferay.portal.search.web.components.advanced.search.portlet.AdvancedSearchDisplayContext"%>
<%@ page import="com.liferay.portal.kernel.util.Constants" %>
<%@ page import="com.liferay.portal.search.web.internal.search.facet.portlet.SearchFacetPortletKeys" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.search.web.internal.search.facet.portlet.SearchFacetDisplayContext" %>
<%@ page import="com.liferay.taglib.servlet.PipingServletResponse" %>
<%@ page import="com.liferay.taglib.aui.AUIUtil" %>
<%@ page import="com.liferay.portal.kernel.util.ListUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="com.liferay.portal.search.web.facet.SearchFacet" %>
<%@ page import="com.liferay.portal.kernel.util.StringPool" %>
<%@ page import="com.liferay.portal.search.web.facet.util.SearchFacetTracker" %>
<%@ page import="com.liferay.portal.search.web.facet.util.comparator.SearchFacetComparator" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />

<liferay-portlet:actionURL portletConfiguration="<%= true %>" var="configurationActionURL" />

<%
AdvancedSearchDisplayContext dc = AdvancedSearchDisplayContextFactoryUtil.create(renderRequest, renderResponse);

String recordSet = portletPreferences.getValue(AdvancedSearchPortletKeys.RECORD_SET, StringPool.BLANK);

SearchContainer<DDLRecordSet> searchContainer = dc.getSearchContainer();
%>
<aui:form action="<%= configurationActionURL %>" method="post" name="fm" onSubmit='<%= "event.preventDefault(); " + renderResponse.getNamespace() + "saveConfiguration();" %>'>
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />

	<div class="portlet-configuration-body-content">
		<div class="container-fluid-1280">
			<aui:fieldset>
				<aui:select label="recordSet" name="preferences--record_set--" value="<%= recordSet %>">
				<%
				for (DDLRecordSet ddlRecordSet : searchContainer.getResults()) {
				%>
					<aui:option label="<%= ddlRecordSet.getName() %>" value="<%= ddlRecordSet.getRecordSetId() %>" />
				<%
				}
				%>
				</aui:select>
			</aui:fieldset>
		</div>
	</div>

	<aui:button-row>
		<aui:button type="submit" />
	</aui:button-row>
</aui:form>

<aui:script>
	function <portlet:namespace />saveConfiguration() {
		var form = AUI.$(document.<portlet:namespace />fm);

		submitForm(form);
	}
</aui:script>