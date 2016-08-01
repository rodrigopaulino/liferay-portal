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

package com.liferay.portal.search.web.search.facet.portlet;

import java.util.List;

import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PredicateFilter;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.liferay.portal.search.web.search.params.SearchParameters;
import com.liferay.portal.search.web.search.params.SearchParametersImpl;

/**
 * @author Eudaldo Alonso
 */
public class SearchFacetDisplayContext {

	public SearchFacetDisplayContext(
		HttpServletRequest request, PortletPreferences portletPreferences)
		throws Exception {

		_portletPreferences = portletPreferences;

		SearchFacetConfigurationImpl searchFacetConfigurationImpl =
				new SearchFacetConfigurationImpl(_portletPreferences);

		_parameters = new SearchParametersImpl(
			request, searchFacetConfigurationImpl);
	}

	public String getSearchConfiguration() {
		if (_searchConfiguration != null) {
			return _searchConfiguration;
		}

		_searchConfiguration = _portletPreferences.getValue(
			"searchConfiguration", StringPool.BLANK);

		return _searchConfiguration;
	}

	public String getQ() {
		return _parameters.getQParameter();
	}

	public SearchFacet getSearchFacet() {
		List<SearchFacet> searchFacets = ListUtil.filter(
			SearchFacetTracker.getSearchFacets(),
			new PredicateFilter<SearchFacet>() {

				@Override
				public boolean filter(SearchFacet searchFacet) {
					return isDisplayFacet(searchFacet.getClassName());
				}

			});

		if (searchFacets.isEmpty())
			return null;

		_searchFacet = searchFacets.get(0);

		return _searchFacet;
	}

	public boolean isDisplayFacet(String className) {
		String facet =
			_portletPreferences.getValue(SearchFacetPortletKeys.FACET, null);

		if (facet == null) {
			return false;
		} else {
			return facet.equals(className);
		}
	}

	private SearchParameters _parameters;
	private String _searchConfiguration;
	private final PortletPreferences _portletPreferences;
	private SearchFacet _searchFacet;

}