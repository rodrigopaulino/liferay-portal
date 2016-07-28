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

<p class="text-default text-results-amount">3 results for <strong>Test</strong></p>

<ul class="tabular-list-group">
	<li class="list-group-item " data-qa-id="row">
		<div class=" list-group-item-field">
			<div class="user-icon user-icon-color-4 user-icon-default user-icon-lg">
				<span>TT</span>
			</div>
		</div>

		<div class=" list-group-item-content">
			<h4>
				<a href="/">
					<strong>Far Far Away
						<span class="highlight">Test</span>
					</strong>
				</a>
			</h4>

			<h6 class="text-default">
				<strong>Blogs Entry</strong>
			</h6>

			<p class="text-default">
				Separated
				<span class="highlight">test</span>
				they live in Bookmarksgrove right at the coast of the Semantics,
				<span class="highlight">test</span>
				a large
			</p>

			<h6 class="text-default">
				<span class="taglib-asset-tags-summary">
					<span class="badge badge-default badge-sm">tag1</span>
					<span class="badge badge-default badge-sm">tag2</span>
				</span>
			</h6>
		</div>
	</li>
	<li class="list-group-item " data-qa-id="row">
		<div class=" list-group-item-field">
			<div class="user-icon user-icon-color-4 user-icon-default user-icon-lg">
				<span>TT</span>
			</div>
		</div>

		<div class=" list-group-item-content">
			<h4>
				<a href="/">
					<strong>Far Far Away
						<span class="highlight">Test</span>
					</strong>
				</a>
			</h4>

			<h6 class="text-default">
				<strong>Blogs Entry</strong>
			</h6>

			<p class="text-default">
				Separated
				<span class="highlight">test</span>
				they live in Bookmarksgrove right at the coast of the Semantics,
				<span class="highlight">test</span>
				a large
			</p>

			<h6 class="text-default">
				<span class="taglib-asset-tags-summary">
					<span class="badge badge-default badge-sm">Another Test Tag</span>
					<span class="badge badge-default badge-sm">Tag Test</span>
					<span class="badge badge-default badge-sm">Tags</span>
				</span>
			</h6>
		</div>
	</li>
	<li class="list-group-item " data-qa-id="row">
		<div class=" list-group-item-field">
			<div class="user-icon user-icon-color-4 user-icon-default user-icon-lg">
				<span>TT</span>
			</div>
		</div>

		<div class=" list-group-item-content">
			<h4>
				<a href="/">
					<strong>Far Far Away
						<span class="highlight">Test</span>
					</strong>
				</a>
			</h4>

			<h6 class="text-default">
				<strong>Blogs Entry</strong>
			</h6>

			<p class="text-default">
				Separated
				<span class="highlight">test</span>
				they live in Bookmarksgrove right at the coast of the Semantics,
				<span class="highlight">test</span>
				a large
			</p>
		</div>
	</li>
</ul>