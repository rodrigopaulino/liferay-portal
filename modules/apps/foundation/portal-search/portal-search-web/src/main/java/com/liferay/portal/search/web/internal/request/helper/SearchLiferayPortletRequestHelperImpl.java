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
import com.liferay.portal.kernel.search.facet.faceted.searcher.FacetedSearcherManager;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.internal.demo.DemoData;
import com.liferay.portal.search.web.internal.display.context.KeywordsSupplier;
import com.liferay.portal.search.web.internal.display.context.PortletURLFactoryImpl;
import com.liferay.portal.search.web.internal.display.context.SearchDisplayContextHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.results.data.SearchResultsDataSupplier;
import com.liferay.portal.search.web.internal.results.search.SearchResultsDataSupplierImpl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
	protected void addSearchFacet(SearchFacet searchFacet) {
		_searchFacets.add(searchFacet);
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
			requestSupplier, renderRequest, () -> _searchFacets,
			facetedSearcherManager, language);
	}

	protected void removeSearchFacet(SearchFacet searchFacet) {
		_searchFacets.remove(searchFacet);
	}

	@Reference
	protected FacetedSearcherManager facetedSearcherManager;

	@Reference
	protected Language language;

	@Reference
	protected Portal portal;

	private final List<SearchFacet> _searchFacets =
		new CopyOnWriteArrayList<>();

}