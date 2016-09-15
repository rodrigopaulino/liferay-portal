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

package com.liferay.portal.search.web.components.results.map.portlet;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.request.helper.OriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.PortalOriginalHttpServletRequestSupplier;
import com.liferay.portal.search.web.internal.request.helper.SearchHttpServletRequestHelper;
import com.liferay.portal.search.web.internal.request.params.SearchParameters;
import com.liferay.portal.search.web.internal.request.params.SearchParametersImpl;
import com.liferay.portal.search.web.internal.results.data.SearchResultsData;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

/**
 * @author André de Oliveira
 */
public class SearchResultsMapDisplayContext {

	public SearchResultsMapDisplayContext(HttpServletRequest request) {
		OriginalHttpServletRequestSupplier originalHttpServletRequestSupplier =
			new PortalOriginalHttpServletRequestSupplier(
				()->request, PortalUtil.getPortal());

		_parameters = new SearchParametersImpl(
			originalHttpServletRequestSupplier,
			new SearchResultsMapConfigurationImpl());

		_searchResultsData = SearchHttpServletRequestHelper.getResults(
			originalHttpServletRequestSupplier);

		_request = request;

		_mapMarkersJSON = buildMapMarkersJSON();

	}

	public String getMapMarkersJSON() {
		return _mapMarkersJSON;
	}

	public String getQ() {
		return _parameters.getQParameter();
	}

	protected String buildMapMarkersJSON() {
		JSONArray locations = JSONFactoryUtil.createJSONArray();

		List<Document> documents = _searchResultsData.getDocuments();

		documents.stream().flatMap(this::getMapMarkers).forEach(locations::put);

		return locations.toString();
	}

	protected JSONObject getMapMarker(
		GeoLocationPoint geoLocationPoint, String assetTypeName, String formattedDate, String summary, String title, String userName) {

		JSONObject jObj = JSONFactoryUtil.createJSONObject();

		jObj.put("assetTypeName", assetTypeName);
		jObj.put("date", formattedDate);
		jObj.put("lat", geoLocationPoint.getLatitude());
		jObj.put("lng", geoLocationPoint.getLongitude());
		jObj.put("summary", summary);
		jObj.put("title", title);
		jObj.put("userName", userName);

		return jObj;
	}

	protected Stream<JSONObject> getMapMarkers(Document document) {
		String className = document.get(Field.ENTRY_CLASS_NAME);
		String title = document.get(Field.TITLE);
		String summary = document.get(Field.CONTENT);
		String userName = document.get(Field.USER_NAME);

		ThemeDisplay themeDisplay = (ThemeDisplay) _request.getAttribute(WebKeys.THEME_DISPLAY);

		String assetTypeName = ResourceActionsUtil.getModelResource(themeDisplay.getLocale(), className);

		SimpleDateFormat simpleDateFormatInput = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat simpleDateFormatOutput = new SimpleDateFormat("MMM dd yyyy, h:mm a");

		Date formattedDate = null;

		try {
			String createDateString = document.get(Field.CREATE_DATE);

			formattedDate = simpleDateFormatInput.parse(createDateString);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		String formattedDateString = simpleDateFormatOutput.format(formattedDate);

		return document.getFields().values().stream()
			.map(Field::getGeoLocationPoint).filter(Objects::nonNull)
			.map(
				geoLocationPoint-> getMapMarker(
					geoLocationPoint, assetTypeName, formattedDateString, summary, title, userName));
	}

	private final String _mapMarkersJSON;
	private final SearchParameters _parameters;
	private final HttpServletRequest _request;
	private final SearchResultsData _searchResultsData;

}