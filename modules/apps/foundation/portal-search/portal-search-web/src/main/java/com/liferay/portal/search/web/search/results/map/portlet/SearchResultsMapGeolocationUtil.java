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

package com.liferay.portal.search.web.search.results.map.portlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.search.geolocation.GeoLocationPoint;

/**
 * @author André de Oliveira
 */
public class SearchResultsMapGeolocationUtil {

	public static void addLocation(
		HttpServletRequest request, Document document, Summary summary) {

		String geoLocationKey = findGeolocationKey(document);

		if (geoLocationKey == null) {
			return;
		}

		Field field = document.getField(geoLocationKey);

		GeoLocationPoint geoLocation = field.getGeoLocationPoint();

		JSONArray locations = getLocations(request);

		locations.put(buildLocationJSON(summary, geoLocation));

		setLocations(request, locations);
	}

	public static String getLocationsAsJSON(HttpServletRequest request) {
		JSONArray locations = getLocations(request);

		return locations.toString();
	}

	public static String getSampleLocationsAsJSON(List<Document> documents) {
		JSONArray locations = JSONFactoryUtil.createJSONArray();

		for (Document document : documents) {
			locations.put(buildLocationJSON(document));
		}

		return locations.toString();
	}

	protected static JSONObject buildLocationJSON(
		Summary summary, GeoLocationPoint geoLocation) {

		double lat = geoLocation.getLatitude();
		double lng = geoLocation.getLongitude();

		JSONObject jObj = JSONFactoryUtil.createJSONObject();

		jObj.put("lat", lat);
		jObj.put("lng", lng);

		jObj.put("title", summary.getHighlightedTitle());
		jObj.put("summary", summary.getHighlightedContent());

		return jObj;
	}

	public static List<Document> getDocumentList() {
		List<Document> list = new ArrayList<>();

		Field title = new Field(Field.TITLE);
		Field content = new Field(Field.CONTENT);

		title.setValue("Pothole Repair (Internal)");
		content.setValue(
			"Pothole Repair (Internal) " +
			"{ latitude: 42.2844, longitude: -71.0663 }" +
			" 65 Bailey St Dorchester MA 02124 | " +
			"Case Closed Internal Case Performed by Highway Maintenance crew.");

		Document document = new DocumentImpl();

		document.addGeoLocation(42.2844, -71.0663);
		document.add(title);
		document.add(content);

		list.add(document);

		content.setValue(
			"Pothole Repair (Internal) " +
			"{ latitude: 42.2753, longitude: -71.0631 }" +
			" 116 Richmond St  Dorchester  MA  02124 | " +
			"Case Closed Internal Case Performed by Highway Maintenance crew.");

		document = new DocumentImpl();

		document.addGeoLocation(42.2753, -71.0631);
		document.add(title);
		document.add(content);

		list.add(document);

		content.setValue(
			"Pothole Repair (Internal) " +
			"{ latitude: 42.2872, longitude: -71.0634 }" +
			" 12 Dracut St  Dorchester  MA  02124 | " +
			"Case Closed Internal Case Performed by Highway Maintenance crew.");

		document = new DocumentImpl();

		document.addGeoLocation(42.2872, -71.0634);
		document.add(title);
		document.add(content);

		list.add(document);

		content.setValue(
			"Pothole Repair (Internal) " +
			"{ latitude: 42.2869, longitude: -71.0632 }" +
			" 13 Dracut St  Dorchester  MA  02124 | " +
			"Case Closed Internal Case Performed by Highway Maintenance crew.");

		document = new DocumentImpl();

		document.addGeoLocation(42.2869, -71.0632);
		document.add(title);
		document.add(content);

		list.add(document);

		return list;
	}

	protected static JSONObject buildLocationJSON(Document document) {
		Field field = document.getField(Field.GEO_LOCATION);

		GeoLocationPoint geoLocationPoint = field.getGeoLocationPoint();

		JSONObject jObj = JSONFactoryUtil.createJSONObject();

		jObj.put("lat", geoLocationPoint.getLatitude());
		jObj.put("lng", geoLocationPoint.getLongitude());
		jObj.put("title", document.get(Field.TITLE));
		jObj.put("summary", document.get(Field.CONTENT));

		return jObj;
	}

	protected static String findGeolocationKey(Document document) {
		Set<String> keys = document.getFields().keySet();

		for (String key : keys) {
			if (key.contains("__geolocation_en_US")) {
				return key;
			}
		}

		return null;
	}

	protected static JSONArray getLocations(HttpServletRequest request) {
		JSONArray locations = (JSONArray)request.getAttribute(
			"search.jsp-geolocation");

		if (locations != null) {
			return locations;
		}

		return JSONFactoryUtil.createJSONArray();
	}

	protected static void setLocations(
		HttpServletRequest request, JSONArray locations) {

		request.setAttribute("search.jsp-geolocation", locations);
	}

}