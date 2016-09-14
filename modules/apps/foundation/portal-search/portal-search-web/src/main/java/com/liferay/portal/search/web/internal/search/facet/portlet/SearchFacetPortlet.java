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

package com.liferay.portal.search.web.internal.search.facet.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.search.web.constants.SearchAwareFacetPortlet;
import com.liferay.portal.search.web.facet.SearchFacet;
import com.liferay.portal.search.web.internal.request.helper.LiferayPortletHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortalOriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.SearchLiferayPortletRequestHelper;
import com.liferay.portal.search.web.internal.request.params.SearchParameters;
import com.liferay.portal.search.web.internal.request.params.SearchParametersConfiguration;
import com.liferay.portal.search.web.internal.request.params.SearchParametersImpl;

import java.io.IOException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
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
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.css-class-wrapper=" +
			SearchFacetPortletKeys.CSS_CLASS_WRAPPER,
		"com.liferay.portlet.display-category=category.search",
		"com.liferay.portlet.icon=/icons/search.png",
		"com.liferay.portlet.instanceable=true",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.restore-current-view=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=" +
			SearchFacetPortletKeys.DISPLAY_NAME,
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=" +
			SearchFacetPortletKeys.VIEW_TEMPLATE,
		"javax.portlet.name=" + SearchFacetPortletKeys.PORTLET_NAME,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = {Portlet.class, SearchAwareFacetPortlet.class}
)
public class SearchFacetPortlet
	extends MVCPortlet implements SearchAwareFacetPortlet {

	// "javax.portlet.supported-public-render-parameter=q",

	@Override
	public SearchFacet getSearchFacet(PortletPreferences portletPreferences) {
		SearchFacetConfigurationImpl searchFacetConfigurationImpl =
			new SearchFacetConfigurationImpl(portletPreferences);

		Stream<SearchFacet> searchFacetsStream = _searchFacets.stream();

		searchFacetsStream = searchFacetsStream.filter(
			searchFacet -> Objects.equals(
				searchFacet.getClassName(),
				searchFacetConfigurationImpl.getSearchFacetClassName().get()));

		Optional<SearchFacet> searchFacetOptional =
			searchFacetsStream.findAny();

		return searchFacetOptional.orElse(null);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletPreferences preferences = renderRequest.getPreferences();

		SearchFacetConfigurationImpl searchFacetConfigurationImpl =
			new SearchFacetConfigurationImpl(preferences);

		SearchParameters searchParameters = createSearchParameters(
			renderRequest, searchFacetConfigurationImpl);

		searchLiferayPortletRequestHelper.search(
			searchParameters::getQParameter, renderRequest, renderResponse);

		Optional<String> searchFacetClassNameOptional =
			searchFacetConfigurationImpl.getSearchFacetClassName();

		Optional<SearchFacet> searchFacetOptional =
			searchFacetClassNameOptional.map(this::getSearchFacet);

		searchFacetOptional.ifPresent(
			searchFacet -> this.setAttributes(renderRequest, searchFacet));

		super.render(renderRequest, renderResponse);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void addSearchFacet(SearchFacet searchFacet) {
		_searchFacets.add(searchFacet);
	}

	protected SearchParametersImpl createSearchParameters(
		RenderRequest renderRequest,
		SearchParametersConfiguration searchParametersConfiguration) {

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			new PortalOriginalHttpServletRequestSupplier(
				new LiferayPortletHttpServletRequestSupplier(renderRequest),
				portal);

		return new SearchParametersImpl(
			originalHttpServletRequestSupplier, searchParametersConfiguration);
	}

	protected SearchFacet getSearchFacet(String searchFacetClassName) {
		Stream<SearchFacet> searchFacetsStream = _searchFacets.stream();

		searchFacetsStream = searchFacetsStream.filter(
			searchFacet -> Objects.equals(
				searchFacet.getClassName(), searchFacetClassName));

		Optional<SearchFacet> searchFacetOptional =
			searchFacetsStream.findAny();

		return searchFacetOptional.orElseThrow(
			() -> new IllegalArgumentException(searchFacetClassName));
	}

	protected void removeSearchFacet(SearchFacet searchFacet) {
		_searchFacets.remove(searchFacet);
	}

	protected void setAttributes(
		RenderRequest renderRequest, SearchFacet searchFacet) {

		renderRequest.setAttribute("search.jsp-facet", searchFacet.getFacet());
		renderRequest.setAttribute("search.jsp-search-facet", searchFacet);
	}

	@Reference
	protected Portal portal;

	@Reference
	protected SearchLiferayPortletRequestHelper
		searchLiferayPortletRequestHelper;

	private final List<SearchFacet> _searchFacets =
		new CopyOnWriteArrayList<>();

}