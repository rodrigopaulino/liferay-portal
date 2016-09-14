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

package com.liferay.portal.search.web.internal.request.helper;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.constants.SearchAwareFacetPortlet;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.internal.demo.DemoData;
import com.liferay.portal.search.web.internal.display.context.KeywordsSupplier;
import com.liferay.portal.search.web.internal.display.context.PortletURLFactoryImpl;
import com.liferay.portal.search.web.internal.display.context.SearchDisplayContextHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.results.data.SearchResultsDataSupplier;
import com.liferay.portal.search.web.internal.results.search.SearchResultsDataSupplierImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author André de Oliveira
 */
@Component(immediate = true, service = SearchLiferayPortletRequestHelper.class)
public class SearchLiferayPortletRequestHelperImpl
	implements SearchLiferayPortletRequestHelper {

	@Override
	public void search(
		KeywordsSupplier keywordsSupplier, RenderRequest renderRequest,
		RenderResponse renderResponse) {

		HttpServletRequestSupplier httpServletRequestSupplier =
			new LiferayPortletHttpServletRequestSupplier(renderRequest);

		SearchHttpServletRequestHelper searchHttpServletRequestHelper =
			createSearchHttpServletRequestHelper(httpServletRequestSupplier);

		SearchResultsDataSupplier searchResultsDataSupplier =
			createSearchResultsDataSupplier(
				keywordsSupplier, httpServletRequestSupplier::get,
				renderRequest, renderResponse);

		searchHttpServletRequestHelper.setResults(searchResultsDataSupplier);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void addSearchAwarePortlet(
		SearchAwareFacetPortlet searchAwarePortlet) {

		_searchAwareFacetPortlets.put(
			searchAwarePortlet.getClass().getName(), searchAwarePortlet);
	}

	protected SearchHttpServletRequestHelper
		createSearchHttpServletRequestHelper(
			HttpServletRequestSupplier httpServletRequestSupplier) {

		return new SearchHttpServletRequestHelper(
			new PortalOriginalHttpServletRequestSupplier(
				httpServletRequestSupplier, portal));
	}

	protected SearchResultsDataSupplier createSearchResultsDataSupplier(
		KeywordsSupplier keywordsSupplier,
		SearchDisplayContextHttpServletRequestSupplier requestSupplier,
		RenderRequest renderRequest, RenderResponse renderResponse) {

		if (false) return DemoData::new;

		return new SearchResultsDataSupplierImpl(
			keywordsSupplier,
			new PortletURLFactoryImpl(renderRequest, renderResponse),
			requestSupplier, renderRequest,
			() -> getSearchFacets(renderRequest), facetedSearcherManager,
			language);
	}

	protected PortletPreferences getPortletPreferences(
		ThemeDisplay themeDisplay, Portlet portlet) {

		PortletPreferences portletPreferences =
		PortletPreferencesLocalServiceUtil.fetchPreferences(
			themeDisplay.getCompanyId(), PortletKeys.PREFS_OWNER_ID_DEFAULT,
			PortletKeys.PREFS_OWNER_TYPE_LAYOUT, themeDisplay.getPlid(),
			portlet.getPortletId());

		return portletPreferences;
	}

	protected List<SearchFacet> getSearchFacets(RenderRequest renderRequest) {
		List<SearchFacet> searchFacets = new CopyOnWriteArrayList<>();

		ThemeDisplay themeDisplay = getThemeDisplay(renderRequest);

		Layout layout = themeDisplay.getLayout();

		LayoutTypePortlet layoutTypePortlet =
			(LayoutTypePortlet)layout.getLayoutType();

		List<Portlet> portletList =
			layoutTypePortlet.getExplicitlyAddedPortlets();

		for (Portlet portlet : portletList) {
			if (_searchAwareFacetPortlets.containsKey(
					portlet.getPortletClass())) {

				SearchAwareFacetPortlet searchAwareFacetPortlet =
					_searchAwareFacetPortlets.get(portlet.getPortletClass());

				PortletPreferences portletPreferences = getPortletPreferences(
					themeDisplay, portlet);

				SearchFacet searchFacet =
					searchAwareFacetPortlet.getSearchFacet(portletPreferences);

				if (searchFacet != null &&
					!searchFacets.contains(searchFacet)) {
					searchFacets.add(searchFacet);
				}
			}
		}

		return searchFacets;
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		return (ThemeDisplay)renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
	}

	protected void removeSearchAwarePortlet(
		SearchAwareFacetPortlet searchAwarePortlet) {

		_searchAwareFacetPortlets.remove(
			searchAwarePortlet.getClass().getName());
	}

	@Reference
	protected FacetedSearcherManager facetedSearcherManager;

	@Reference
	protected Language language;

	@Reference
	protected Portal portal;

	private final Map<String, SearchAwareFacetPortlet>
		_searchAwareFacetPortlets = new HashMap<>();

}