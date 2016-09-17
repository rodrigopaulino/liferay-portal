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

package com.liferay.portal.search.web.internal.display.context;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.ScopeFacet;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcher;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Html;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.web.constants.SearchPortletParameterNames;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eudaldo Alonso
 */
public class SearchDisplayContext {

	public SearchDisplayContext(
			RenderRequest renderRequest, RenderResponse renderResponse,
			PortletPreferences portletPreferences, Portal portal, Html html,
			Language language, FacetedSearcherManager facetedSearcherManager,
			KeywordsSupplier keywordsSupplier,
			SearchScopeGroupIdSupplier searchScopeGroupIdSupplier,
			SearchScopeSupplier searchScopeSupplier,
			SearchScopePreferenceSupplier searchScopePreferenceSupplier,
			SearchScopePreferenceStringSupplier searchScopePreferenceStringSupplier,
			SearchFacetsSupplier searchFacetsSupplier,
			FacetsConfigurationSupplier facetsConfigurationSupplier,
			FacetsDisplayPreferences facetsDisplayPreferences,
			QueryConfigSupplier queryConfigSupplier,
			QueryConfigPreferences queryConfigPreferences,
			ThemeDisplaySupplier themeDisplaySupplier,
			PortletURLFactory portletURLFactory,
			SearchDisplayContextHttpServletRequestSupplier requestSupplier)
		throws Exception {

		_portletPreferences = portletPreferences;
		_keywordsSupplier = keywordsSupplier;
		_searchScopeSupplier = searchScopeSupplier;
		_searchScopeGroupIdSupplier = searchScopeGroupIdSupplier;
		_searchScopePreferenceSupplier = searchScopePreferenceSupplier;
		_searchScopePreferenceStringSupplier =
			searchScopePreferenceStringSupplier;
		_searchFacetsSupplier = searchFacetsSupplier;
		_facetsConfigurationSupplier = facetsConfigurationSupplier;
		_facetsDisplayPreferences = facetsDisplayPreferences;
		_queryConfigSupplier = queryConfigSupplier;
		_queryConfigPreferences = queryConfigPreferences;
		_themeDisplaySupplier = themeDisplaySupplier;
		_portletURLFactory = portletURLFactory;

		String keywords = StringUtil.trim(keywordsSupplier.getKeywords());

		if (keywords == null) {
			_hits = null;
			_searchContext = null;
			_searchContainer = null;

			return;
		}

		HttpServletRequest request = requestSupplier.get();

		ThemeDisplay themeDisplay = themeDisplaySupplier.getThemeDisplay();

		String emptyResultMessage = language.format(
			request, "no-results-were-found-that-matched-the-keywords-x",
			"<strong>" + html.escape(keywords) + "</strong>", false);

		SearchContainer<Document> searchContainer = new SearchContainer<>(
			renderRequest, portletURLFactory.getPortletURL(), null,
			emptyResultMessage);

		FacetedSearcher facetedSearcher =
			facetedSearcherManager.createFacetedSearcher();

		SearchContext searchContext = SearchContextFactory.getInstance(request);

		searchContext.setAttribute("paginationType", "more");
		searchContext.setEnd(searchContainer.getEnd());
		searchContext.setKeywords(keywords);
		searchContext.setQueryConfig(getQueryConfig());
		searchContext.setStart(searchContainer.getStart());

		searchContext.setAttribute(
			SearchPortletParameterNames.GROUP_ID,
			String.valueOf(getSearchScopeGroupId()));

		addAssetEntriesFacet(searchContext);

		addScopeFacet(searchContext);

		addEnabledSearchFacets(
			searchFacetsSupplier, themeDisplay.getCompanyId(), searchContext);

		Hits hits;

		if (keywords.isEmpty()) {
			hits = new HitsImpl();
		}
		else {
			hits = facetedSearcher.search(searchContext);

			searchContainer.setTotal(hits.getLength());
			searchContainer.setResults(hits.toList());
			searchContainer.setSearch(true);
		}

		_hits = hits;
		_searchContext = searchContext;
		_searchContainer = searchContainer;
	}

	public String checkViewURL(String viewURL, String currentURL) {
		ThemeDisplay themeDisplay = getThemeDisplay();

		if (Validator.isNotNull(viewURL) &&
			viewURL.startsWith(themeDisplay.getURLPortal())) {

			viewURL = HttpUtil.setParameter(
				viewURL, "inheritRedirect", isViewInContext());

			if (!isViewInContext()) {
				viewURL = HttpUtil.setParameter(
					viewURL, "redirect", currentURL);
			}
		}

		return viewURL;
	}

	public int getCollatedSpellCheckResultDisplayThreshold() {
		return _queryConfigPreferences.getCollatedSpellCheckResultDisplayThreshold();
	}

	public List<SearchFacet> getEnabledSearchFacets() {
		return new ArrayList<>(_searchFacetsSupplier.getSearchFacets());
	}

	public Hits getHits() {
		return _hits;
	}

	public String getKeywords() {
		return _keywordsSupplier.getKeywords();
	}

	public PortletURL getPortletURL() throws PortletException {
		return _portletURLFactory.getPortletURL();
	}

	public QueryConfig getQueryConfig() {
		return _queryConfigSupplier.getQueryConfig();
	}

	public int getQueryIndexingThreshold() {
		return _queryConfigPreferences.getQueryIndexingThreshold();
	}

	public int getQuerySuggestionsDisplayThreshold() {
		return _queryConfigPreferences.getQuerySuggestionsDisplayThreshold();
	}

	public int getQuerySuggestionsMax() {
		return _queryConfigPreferences.getQuerySuggestionsMax();
	}

	public String[] getQueryTerms() {
		Hits hits = getHits();

		return hits.getQueryTerms();
	}

	public String getSearchConfiguration() {
		return _facetsConfigurationSupplier.getFacetsConfiguration();
	}

	public SearchContainer<Document> getSearchContainer() {
		return _searchContainer;
	}

	public SearchContext getSearchContext() {
		return _searchContext;
	}

	public long getSearchScopeGroupId() {
		return _searchScopeGroupIdSupplier.getSearchScopeGroupId();
	}

	public String getSearchScopeParameterString() {
		SearchScope searchScope = getSearchScope();

		return searchScope.getParameterString();
	}

	public String getSearchScopePreferenceString() {
		return _searchScopePreferenceStringSupplier.getSearchScopePreferenceString();
	}

	public boolean isCollatedSpellCheckResultEnabled() {
		return _queryConfigPreferences.isCollatedSpellCheckResultEnabled();
	}

	public boolean isDisplayFacet(SearchFacet searchFacet) {
		return _facetsDisplayPreferences.isDisplay(searchFacet);
	}

	public boolean isDisplayMainQuery() {
		if (_displayMainQuery != null) {
			return _displayMainQuery;
		}

		_displayMainQuery = GetterUtil.getBoolean(
			_portletPreferences.getValue("displayMainQuery", null));

		return _displayMainQuery;
	}

	public boolean isDisplayOpenSearchResults() {
		if (_displayOpenSearchResults != null) {
			return _displayOpenSearchResults;
		}

		_displayOpenSearchResults = GetterUtil.getBoolean(
			_portletPreferences.getValue("displayOpenSearchResults", null));

		return _displayOpenSearchResults;
	}

	public boolean isDisplayResultsInDocumentForm() {
		if (_displayResultsInDocumentForm != null) {
			return _displayResultsInDocumentForm;
		}

		_displayResultsInDocumentForm = GetterUtil.getBoolean(
			_portletPreferences.getValue("displayResultsInDocumentForm", null));

		ThemeDisplay themeDisplay = getThemeDisplay();

		PermissionChecker permissionChecker =
			themeDisplay.getPermissionChecker();

		if (!permissionChecker.isCompanyAdmin()) {
			_displayResultsInDocumentForm = false;
		}

		return _displayResultsInDocumentForm;
	}

	public boolean isDLLinkToViewURL() {
		if (_dlLinkToViewURL != null) {
			return _dlLinkToViewURL;
		}

		_dlLinkToViewURL = false;

		return _dlLinkToViewURL;
	}

	public boolean isHighlightEnabled() {
		QueryConfig queryConfig = getQueryConfig();

		return queryConfig.isHighlightEnabled();
	}

	public boolean isIncludeSystemPortlets() {
		if (_includeSystemPortlets != null) {
			return _includeSystemPortlets;
		}

		_includeSystemPortlets = false;

		return _includeSystemPortlets;
	}

	public boolean isQueryIndexingEnabled() {
		return _queryConfigPreferences.isQueryIndexingEnabled();
	}

	public boolean isQuerySuggestionsEnabled() {
		return _queryConfigPreferences.isQuerySuggestionsEnabled();
	}

	public boolean isSearchScopePreferenceEverythingAvailable() {
		ThemeDisplay themeDisplay = getThemeDisplay();

		Group group = themeDisplay.getScopeGroup();

		if (group.isStagingGroup()) {
			return false;
		}

		return true;
	}

	public boolean isSearchScopePreferenceLetTheUserChoose() {
		SearchScopePreference searchScopePreference =
			getSearchScopePreference();

		if (searchScopePreference ==
				SearchScopePreference.LET_THE_USER_CHOOSE) {

			return true;
		}

		return false;
	}

	public boolean isShowMenu() {
		for (SearchFacet searchFacet : SearchFacetTracker.getSearchFacets()) {
			if (_facetsDisplayPreferences.isDisplay(searchFacet)) {
				return true;
			}
		}

		return false;
	}

	public boolean isViewInContext() {
		if (_viewInContext != null) {
			return _viewInContext;
		}

		_viewInContext = GetterUtil.getBoolean(
			_portletPreferences.getValue("viewInContext", null), true);

		return _viewInContext;
	}

	protected void addAssetEntriesFacet(SearchContext searchContext) {
		Facet assetEntriesFacet = new AssetEntriesFacet(searchContext);

		assetEntriesFacet.setStatic(true);

		searchContext.addFacet(assetEntriesFacet);
	}

	protected void addEnabledSearchFacets(
			SearchFacetsSupplier searchFacetsSupplier, long companyId,
			SearchContext searchContext)
		throws Exception {

		for (SearchFacet searchFacet :
				_searchFacetsSupplier.getSearchFacets()) {

			searchFacet.init(
				companyId,
				_facetsConfigurationSupplier.getFacetsConfiguration(),
				searchContext);

			Facet facet = searchFacet.getFacet();

			if (facet == null) {
				continue;
			}

			searchContext.addFacet(facet);
		}
	}

	protected void addScopeFacet(SearchContext searchContext) {
		Facet scopeFacet = new ScopeFacet(searchContext);

		scopeFacet.setStatic(true);

		searchContext.addFacet(scopeFacet);
	}

	protected SearchScope getSearchScope() {
		return _searchScopeSupplier.getSearchScope();
	}

	protected SearchScopePreference getSearchScopePreference() {
		return _searchScopePreferenceSupplier.getSearchScopePreference();
	}

	protected ThemeDisplay getThemeDisplay() {
		return _themeDisplaySupplier.getThemeDisplay();
	}

	private Boolean _displayMainQuery;
	private Boolean _displayOpenSearchResults;
	private Boolean _displayResultsInDocumentForm;
	private Boolean _dlLinkToViewURL;
	private final FacetsConfigurationSupplier _facetsConfigurationSupplier;
	private final FacetsDisplayPreferences _facetsDisplayPreferences;
	private final Hits _hits;
	private Boolean _includeSystemPortlets;
	private final KeywordsSupplier _keywordsSupplier;
	private final PortletPreferences _portletPreferences;
	private final PortletURLFactory _portletURLFactory;
	private final QueryConfigPreferences _queryConfigPreferences;
	private final QueryConfigSupplier _queryConfigSupplier;
	private final SearchContainer<Document> _searchContainer;
	private final SearchContext _searchContext;
	private final SearchFacetsSupplier _searchFacetsSupplier;
	private final SearchScopeGroupIdSupplier _searchScopeGroupIdSupplier;
	private final SearchScopePreferenceStringSupplier
		_searchScopePreferenceStringSupplier;
	private final SearchScopePreferenceSupplier _searchScopePreferenceSupplier;
	private final SearchScopeSupplier _searchScopeSupplier;
	private final ThemeDisplaySupplier _themeDisplaySupplier;
	private Boolean _viewInContext;

}