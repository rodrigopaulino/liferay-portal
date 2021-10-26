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

package com.liferay.dynamic.data.mapping.form.validation.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.TimeZoneUtil;
import com.liferay.portal.kernel.util.Validator;

import java.text.ParseException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Carolina Barbosa
 */
public class DateParameterUtil {

	public static LocalDate getLocalDate(String dateString, Locale locale) {
		try {
			Date date = DateUtil.parseDate("yyyy-MM-dd", dateString, locale);

			ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
				date.toInstant(), ZoneOffset.UTC);

			return zonedDateTime.toLocalDate();
		}
		catch (ParseException parseException) {
			if (_log.isDebugEnabled()) {
				_log.debug(parseException, parseException);
			}
		}

		return LocalDate.parse(
			dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

	public static String getParameter(
		String key, String parameter, String timeZoneId) {

		JSONObject jsonObject;

		try {
			jsonObject = JSONFactoryUtil.createJSONObject(parameter);
		}
		catch (JSONException jsonException) {
			return StringPool.BLANK;
		}

		LocalDate localDate = _getComparisonLocalDate(
			_getCurrentLocalDate(timeZoneId), jsonObject.getJSONObject(key));

		if (localDate == null) {
			return StringPool.BLANK;
		}

		return localDate.toString();
	}

	private static LocalDate _getComparisonLocalDate(
		LocalDate currentLocalDate, JSONObject jsonObject) {

		if (jsonObject == null) {
			return null;
		}

		String type = jsonObject.getString("type");

		if (StringUtil.equals(type, "customDate") &&
			StringUtil.equals(jsonObject.getString("date"), "responseDate")) {

			return _getCustomLocalDate(
				currentLocalDate, jsonObject.getInt("quantity"),
				jsonObject.getString("unit"));
		}
		else if (StringUtil.equals(type, "responseDate")) {
			return currentLocalDate;
		}

		return null;
	}

	private static LocalDate _getCurrentLocalDate(String timeZoneId) {
		if (Validator.isNull(timeZoneId)) {
			TimeZone timeZone = TimeZoneUtil.getDefault();

			timeZoneId = timeZone.getID();
		}

		return LocalDate.now(ZoneId.of(timeZoneId));
	}

	private static LocalDate _getCustomLocalDate(
		LocalDate localDate, int quantity, String unit) {

		if (StringUtil.equals(unit, "days")) {
			return localDate.plusDays(quantity);
		}
		else if (StringUtil.equals(unit, "months")) {
			return localDate.plusMonths(quantity);
		}
		else if (StringUtil.equals(unit, "years")) {
			return localDate.plusYears(quantity);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DateParameterUtil.class);

}