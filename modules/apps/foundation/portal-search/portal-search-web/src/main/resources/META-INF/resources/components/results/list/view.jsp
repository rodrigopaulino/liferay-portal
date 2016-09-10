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

<%@ page import="com.liferay.portal.search.web.internal.results.data.SearchResultsData" %>
<%@ page import="com.liferay.portal.search.web.internal.search.results.list.portlet.SearchResultsListDisplayContext" %>

<%
SearchResultsListDisplayContext dc = new SearchResultsListDisplayContext(request);

SearchResultsData searchResultsData = dc.getSearchResultsData();

List<Document> documents = searchResultsData.getDocuments();
%>

<style>
	.highlight {
		background: none;
	}

	.text-results-amount {
		margin-top: 30px;
	}
</style>

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

<p class="text-default text-results-amount">X results for <strong>Test</strong></p>

<ul class="tabular-list-group">
	<%
	for (Document document : documents) {
	%>
		<li class="list-group-item " data-qa-id="row">
			<div class=" list-group-item-field">
				<div class="user-icon user-icon-color-6 user-icon-default user-icon-lg">
					<span>TT</span>
				</div>
			</div>

			<div class=" list-group-item-content">
				<h4>
					<a href="/">
						<strong>
							<%= document.get(Field.TITLE) %>
						</strong>
					</a>
				</h4>

				<h6 class="text-default">
					<strong><%= document.get(Field.ENTRY_CLASS_NAME) %></strong>
				</h6>

				<p class="text-default">
					<%= document.get(Field.CONTENT) %>
				</p>

				<h6 class="text-default">
					<span class="taglib-asset-tags-summary">
						<span class="badge badge-default badge-sm"><%= document.get(Field.ASSET_TAG_NAMES) %></span>
					</span>
				</h6>
			</div>
		</li>
	<%
	}
	%>
</ul>