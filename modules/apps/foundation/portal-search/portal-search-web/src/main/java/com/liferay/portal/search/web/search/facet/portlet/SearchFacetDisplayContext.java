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
import java.util.concurrent.CopyOnWriteArrayList;

import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.kernel.search.facet.collector.DefaultTermCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.search.web.search.params.SearchParameters;
import com.liferay.portal.search.web.search.params.SearchParametersConfiguration;
import com.liferay.portal.search.web.search.params.SearchParametersImpl;

/**
 * @author Eudaldo Alonso
 */
public class SearchFacetDisplayContext {

	public SearchFacetDisplayContext(
		HttpServletRequest request, PortletPreferences portletPreferences)
		throws Exception {

		SearchFacetConfigurationImpl searchFacetConfigurationImpl =
				new SearchFacetConfigurationImpl(portletPreferences);
		_parameters = new SearchParametersImpl(
			request, searchFacetConfigurationImpl);
		_parametersConfiguration = searchFacetConfigurationImpl;
		populateTermCollectors();
	}

	public String getQ() {
		return _parameters.getQParameter();
	}

	public String getQParameterName() {
		return _parametersConfiguration.getQParameterName();
	}

	protected void populateTermCollectors() {
		TermCollector termCollector = new DefaultTermCollector("Boston", 4);

		_termCollectors.add(termCollector);
	}

	public List<TermCollector> getTermCollectors() {
		return _termCollectors;
	}

	public void setTermCollectors(List<TermCollector> _termCollectors) {
		this._termCollectors = _termCollectors;
	}

	private SearchParameters _parameters;
	private List<TermCollector> _termCollectors = new CopyOnWriteArrayList<>();
	private final SearchParametersConfiguration _parametersConfiguration;

}