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
package com.liferay.portal.search.web.search.results.list.portlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.web.search.params.SearchParameters;
import com.liferay.portal.search.web.search.params.SearchParametersImpl;
import com.liferay.portal.search.web.util.SearchUtil;

/**
 * @author Eudaldo Alonso
 */
public class SearchResultsListDisplayContext {

	public SearchResultsListDisplayContext(HttpServletRequest request)
		throws Exception {

		_parameters = new SearchParametersImpl(
				request, new SearchResultsListConfigurationImpl());
		_documentList = SearchUtil.getDocumentList();
	}

	public List<Document> getDocumentList() {
		return _documentList;
	}

	public String getQ() {
		return _parameters.getQParameter();
	}

	private SearchParameters _parameters;
	private List<Document> _documentList;

}