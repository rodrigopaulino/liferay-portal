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

boolean scopeEverything = (groupId == 0);

String format = ParamUtil.getString(request, SearchPortletParameterNames.FORMAT);

com.liferay.portal.search.web.internal.search.bar.classic.portlet.SearchBarClassicDisplayContext context =
	new com.liferay.portal.search.web.internal.search.bar.classic.portlet.SearchBarClassicDisplayContext(request, portletPreferences);
%>

<portlet:actionURL name="redirectSearchBar" var="portletURL">
	<portlet:param name="mvcActionCommandName" value="redirectSearchBar" />
</portlet:actionURL>

<aui:form action="<%= portletURL %>" method="post" name="fm">
	<aui:input name="<%= SearchContainer.DEFAULT_CUR_PARAM %>" type="hidden" value="<%= ParamUtil.getInteger(request, SearchContainer.DEFAULT_CUR_PARAM, SearchContainer.DEFAULT_CUR) %>" />
	<aui:input name="format" type="hidden" value="<%= format %>" />

	<aui:fieldset id="searchContainer">
		<div class="input-group search-bar">
			<aui:field-wrapper cssClass="search-field" inlineField="<%= true %>">
				<aui:input
					autoFocus="<%= windowState.equals(WindowState.MAXIMIZED) %>"
					cssClass="search-input"
					label=""
					name="<%= context.getQParameterName() %>"
					placeholder="search-..."
					title="search"
					type="text"
					value="<%= context.getQ() %>"
				/>
			</aui:field-wrapper>

			<c:choose>
				<c:when test="<%= searchDisplayContext.isSearchScopePreferenceLetTheUserChoose() %>">
					<aui:field-wrapper cssClass="search-field" inlineField="<%= true %>">
						<aui:select cssClass="search-select" label="" name="scope" title="scope">
							<c:if test="<%= searchDisplayContext.isSearchScopePreferenceEverythingAvailable() %>">
								<aui:option label="everything" selected="<%= scopeEverything %>" value="everything" />
							</c:if>

							<aui:option label="this-site" selected="<%= !scopeEverything %>" value="this-site" />
						</aui:select>
					</aui:field-wrapper>
				</c:when>
				<c:otherwise>
					<aui:input name="scope" type="hidden" value="<%= searchDisplayContext.getSearchScopeParameterString() %>" />
				</c:otherwise>
			</c:choose>

			<aui:field-wrapper cssClass="input-group-btn search-field" inlineField="<%= true %>">
				<aui:button icon="icon-search" primary="<%= false %>" type="submit" value="" />
			</aui:field-wrapper>
		</div>
	</aui:fieldset>
</aui:form>

<aui:script sandbox="<%= true %>">
	$('#<portlet:namespace /><%= context.getQParameterName() %>').on(
		'keydown',
		function(event) {
			if (event.keyCode === 13) {
				<portlet:namespace />search();
			}
		}
	);
</aui:script>