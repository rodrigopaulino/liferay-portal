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
<%@ page import="com.liferay.portal.search.web.components.results.list.portlet.SearchResultsListDisplayContext" %>

<%@ page import="com.liferay.asset.kernel.model.AssetEntry" %>
<%@ page import="com.liferay.blogs.kernel.model.BlogsEntry" %>
<%@ page import="com.liferay.message.boards.kernel.model.MBMessage" %>

<%@ page import="java.text.SimpleDateFormat" %>

<%@ page import="com.liferay.blogs.kernel.service.BlogsEntryLocalServiceUtil" %>

<%
SearchResultsListDisplayContext dc = new SearchResultsListDisplayContext(request);

SearchResultsData searchResultsData = dc.getSearchResultsData();

List<Document> documents = searchResultsData.getDocuments();

SearchContainer<Document> newSearchContainer = new SearchContainer<Document>();

newSearchContainer.setResults(documents);
%>

<style>
	.taglib-asset-tags-summary a.badge, .taglib-asset-tags-summary a.badge:hover {
		color: #65B6F0;
	}

	.search-total-label {
		margin-top: 35px;
	}

	.search-asset-type-sticker {
		color: #869CAD;
	}

	.search-document-content {
		font-weight: 400;
	}

	.search-result-thumbnail-img {
		height: 44px;
		width: 44px;
	}

	.tabular-list-group .list-group-item-content h6.search-document-tags {
		margin-top: 13px;
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

<%
List<Document> searchResults = newSearchContainer.getResults();

int searchResultsAmount = searchResults.size();

String searchQuery = dc.getQ();
%>

<p class="search-total-label text-default">
	About <%= searchResultsAmount %> results for <strong><%= searchQuery %></strong>
</p>

<liferay-ui:search-container
	delta="<%= 10 %>"
	id="search"
	searchContainer="<%= newSearchContainer %>"
>
	<liferay-ui:search-container-row
		className="com.liferay.portal.kernel.search.Document"
		escapedModel="<%= false %>"
		keyProperty="UID"
		modelVar="document"
		stringKey="<%= true %>"
	>
		<%
			String className = document.get(Field.ENTRY_CLASS_NAME);
			long classPK = GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK));

			AssetRendererFactory<?> assetRendererFactory = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(className);

			AssetRenderer<?> assetRenderer = null;

			if (assetRendererFactory != null) {
				long resourcePrimKey = GetterUtil.getLong(document.get(Field.ROOT_ENTRY_CLASS_PK));

				if (resourcePrimKey > 0) {
					classPK = resourcePrimKey;
				}

				assetRenderer = assetRendererFactory.getAssetRenderer(classPK);
			}

			String viewURL = com.liferay.portal.search.web.internal.util.SearchUtil.getSearchResultViewURL(renderRequest, renderResponse, className, classPK, searchDisplayContext.isViewInContext(), currentURL);

			Indexer indexer = IndexerRegistryUtil.getIndexer(className);

			Summary summary = null;

			if (indexer != null) {
				String snippet = document.get(Field.SNIPPET);

				summary = indexer.getSummary(document, snippet, renderRequest, renderResponse);
			}
			else if (assetRenderer != null) {
				summary = new Summary(locale, assetRenderer.getTitle(locale), assetRenderer.getSearchSummary(locale));
			}

			viewURL = searchDisplayContext.checkViewURL(viewURL, currentURL);

			summary.setHighlight(searchDisplayContext.isHighlightEnabled());
			// summary.setQueryTerms(searchDisplayContext.getQueryTerms());

			AssetEntry assetEntry = AssetEntryLocalServiceUtil.fetchEntry(className, classPK);
		%>

		<liferay-ui:search-container-column-text>
			<%
			String assetIcon = "blogs";
			boolean hasCoverImage = false;

			if (className.equals(BlogsEntry.class.getName())) {
				assetIcon = "blogs";

				String entryClassPK = document.get(Field.ENTRY_CLASS_PK);

				Long entryClassPKLong = Long.parseLong(entryClassPK, 10);

				BlogsEntry searchResultBlogEntry = BlogsEntryLocalServiceUtil.getEntry(entryClassPKLong);

				String coverImageURL = searchResultBlogEntry.getCoverImageURL();

				if ((!coverImageURL.isEmpty()) && (coverImageURL != null)) {
					hasCoverImage = true;
				}
			%>

				<c:if test="<%= hasCoverImage %>">
					<img alt="blog cover image" class="img-rounded search-result-thumbnail-img" src="<%= coverImageURL %>" />
				</c:if>

			<%
			}
			else if (className.equals(AssetEntry.class.getName())) {
				assetIcon = "web-content";
			}
			else if (className.equals(MBMessage.class.getName())) {
				assetIcon = "message-boards";
			}
			%>

			<c:if test="<%= !hasCoverImage %>">
				<span class="search-asset-type-sticker sticker sticker-default sticker-lg sticker-rounded sticker-static">
					<svg class="lexicon-icon">
						<use xlink:href="<%= themeDisplay.getPathThemeImages() %>/lexicon/icons.svg#<%= assetIcon %>" />
					</svg>
				</span>
			</c:if>
		</liferay-ui:search-container-column-text>

		<liferay-ui:search-container-column-text
			colspan="<%= 2 %>"
		>
			<h4>
				<a href="<%= viewURL %>">
					<strong><%= summary.getHighlightedTitle() %></strong>
				</a>
			</h4>

			<h6 class="text-default">
				<strong><%= ResourceActionsUtil.getModelResource(themeDisplay.getLocale(), className) %></strong> &#183;

				<c:if test="<%= locale != summary.getLocale() %>">
					<%
					Locale summaryLocale = summary.getLocale();
					%>

					<liferay-ui:icon image='<%= "../language/" + LocaleUtil.toLanguageId(summaryLocale) %>' message='<%= LanguageUtil.format(request, "this-result-comes-from-the-x-version-of-this-content", summaryLocale.getDisplayLanguage(locale), false) %>' />
				</c:if>

				<c:if test="<%= (document.get(Field.USER_NAME) != null) %>">
					<liferay-ui:message key="written-by" /> <strong><%= document.get(Field.USER_NAME) %></strong>
				</c:if>

				<c:if test="<%= (document.get(Field.CREATE_DATE) != null) %>">
					<%
						SimpleDateFormat simpleDateFormatInput = new SimpleDateFormat("yyyyMMddHHmmss");
						SimpleDateFormat simpleDateFormatOutput = new SimpleDateFormat("MMM dd yyyy, h:mm a");

						String createDateString = document.get(Field.CREATE_DATE);

						Date formattedDate = simpleDateFormatInput.parse(createDateString);

						String formattedDateString = simpleDateFormatOutput.format(formattedDate);
					%>

					<liferay-ui:message key="on-date" /> <%= formattedDateString %>
				</c:if>
			</h6>

			<c:if test="<%= Validator.isNotNull(summary.getContent()) %>">
				<h6 class="search-document-content text-default">
					<%= summary.getHighlightedContent() %>
				</h6>
			</c:if>

			<c:if test="<%= (assetEntry != null) && (ArrayUtil.isNotEmpty(assetEntry.getCategoryIds()) || ArrayUtil.isNotEmpty(assetEntry.getTagNames())) %>">
				<h6 class="search-document-tags text-default">
					<liferay-ui:asset-tags-summary
						className="<%= className %>"
						classPK="<%= classPK %>"
						paramName="<%= Field.ASSET_TAG_NAMES %>"
						portletURL="<%= searchDisplayContext.getPortletURL() %>"
					/>

					<liferay-ui:asset-categories-summary
						className="<%= className %>"
						classPK="<%= classPK %>"
						paramName="<%= Field.ASSET_CATEGORY_IDS %>"
						portletURL="<%= searchDisplayContext.getPortletURL() %>"
					/>
				</h6>
			</c:if>
		</liferay-ui:search-container-column-text>
	</liferay-ui:search-container-row>

	<liferay-ui:search-iterator displayStyle="descriptive" markupView="lexicon" type="more" />
</liferay-ui:search-container>