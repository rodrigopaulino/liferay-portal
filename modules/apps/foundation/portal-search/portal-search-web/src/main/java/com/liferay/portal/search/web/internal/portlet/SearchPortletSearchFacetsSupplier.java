package com.liferay.portal.search.web.internal.portlet;

import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PredicateFilter;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.facet.util.SearchFacetTracker;
import com.liferay.portal.search.web.internal.display.context.FacetsDisplayPreferences;
import com.liferay.portal.search.web.internal.display.context.SearchFacetsSupplier;

import java.util.Collection;
import java.util.List;
public class SearchPortletSearchFacetsSupplier implements SearchFacetsSupplier {

	public SearchPortletSearchFacetsSupplier(
		FacetsDisplayPreferences facetsDisplayPreferences) {

		_facetsDisplayPreferences = facetsDisplayPreferences;
	}

	@Override
	public Collection<SearchFacet> getSearchFacets() {
		if (_enabledSearchFacets != null) {
			return _enabledSearchFacets;
		}

		_enabledSearchFacets = ListUtil.filter(
			SearchFacetTracker.getSearchFacets(),
			new PredicateFilter<SearchFacet>() {

				@Override
				public boolean filter(SearchFacet searchFacet) {
					return _facetsDisplayPreferences.isDisplay(searchFacet);
				}

			});

		return _enabledSearchFacets;
	}

	private List<SearchFacet> _enabledSearchFacets;
	private final FacetsDisplayPreferences _facetsDisplayPreferences;

}