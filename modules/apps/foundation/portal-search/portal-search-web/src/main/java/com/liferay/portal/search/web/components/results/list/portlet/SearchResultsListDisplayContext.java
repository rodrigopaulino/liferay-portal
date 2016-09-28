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

package com.liferay.portal.search.web.components.results.list.portlet;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.search.web.internal.document.display.context.DocumentDisplayContext;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortalOriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.SearchHttpServletRequestHelper;
import com.liferay.portal.search.web.internal.request.params.SearchParameters;
import com.liferay.portal.search.web.internal.request.params.SearchParametersImpl;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

import java.util.Locale;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author André de Oliveira
 */
public class SearchResultsListDisplayContext {

	public SearchResultsListDisplayContext(
		HttpServletRequest request, RenderRequest renderRequest,
		RenderResponse renderResponse, Locale locale) {

		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			new PortalOriginalHttpServletRequestSupplier(
				()-> request, PortalUtil.getPortal());

		_locale = locale;

		_parameters = new SearchParametersImpl(
			originalHttpServletRequestSupplier,
			new SearchResultsListConfigurationImpl());

		_renderResponse = renderResponse;
		_renderRequest = renderRequest;

		_searchResultsData = SearchHttpServletRequestHelper.getResults(
			originalHttpServletRequestSupplier);
	}

	public DocumentDisplayContext getDocumentDisplayContext(Document document) {
		return new DocumentDisplayContext(
			document, _renderRequest, _renderResponse, _locale);
	}

	public String[] getQueryTerms() {
		return _searchResultsData.getQueryTerms();
	}

	public String getQ() {
		return _parameters.getQParameter();
	}

	public SearchResultsData getSearchResultsData() {
		return _searchResultsData;
	}

	public SearchContainer<Document> getSearchResultsContainer() {
		return _searchResultsData.getDocuments();
	}

	private final Locale _locale;
	private final SearchParameters _parameters;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final SearchResultsData _searchResultsData;

}