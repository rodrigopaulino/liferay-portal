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

<%
String redirect = ParamUtil.getString(request, "redirect");

if (Validator.isNotNull(redirect)) {
	portletDisplay.setURLBack(redirect);
}

long groupId = ParamUtil.getLong(request, SearchPortletParameterNames.GROUP_ID);

String format = ParamUtil.getString(request, SearchPortletParameterNames.FORMAT);
%>

<liferay-portlet:renderURL varImpl="searchURL">
	<portlet:param name="mvcPath" value="/components/bar/view.jsp" />
</liferay-portlet:renderURL>

<aui:form action="<%= searchURL %>" method="get" name="fm" onSubmit="event.preventDefault();">
	<liferay-portlet:renderURLParams varImpl="searchURL" />
	<aui:input name="<%= SearchContainer.DEFAULT_CUR_PARAM %>" type="hidden" value="<%= ParamUtil.getInteger(request, SearchContainer.DEFAULT_CUR_PARAM, SearchContainer.DEFAULT_CUR) %>" />
	<aui:input name="format" type="hidden" value="<%= format %>" />

	<aui:fieldset id="searchContainer">
		<div class="input-group">
			<aui:input autoFocus="<%= windowState.equals(WindowState.MAXIMIZED) %>" label="" name="keywords" placeholder="Search..." title="search" value="<%= HtmlUtil.escape(searchDisplayContext.getKeywords()) %>" />

			<span class="input-group-btn">
				<aui:button onClick='<%= renderResponse.getNamespace() + "search();" %>' value="search" />
			</span>
		</div>
	</aui:fieldset>
</aui:form>

<aui:script sandbox="<%= true %>">
	$('#<portlet:namespace />keywords').on(
		'keydown',
		function(event) {
			if (event.keyCode === 13) {
				<portlet:namespace />search();
			}
		}
	);
</aui:script>