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

package com.liferay.dynamic.data.mapping.form.field.type.internal.search.location;

import com.liferay.dynamic.data.mapping.form.field.type.internal.searchLocation.SearchLocationDDMFormFieldTemplateContextContributor;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.render.DDMFormFieldRenderingContext;
import com.liferay.dynamic.data.mapping.test.util.DDMFormTestUtil;
import com.liferay.google.places.util.GooglePlacesUtil;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Rodrigo Paulino
 */
@PrepareForTest(
	{GooglePlacesUtil.class, LanguageUtil.class, ResourceBundleUtil.class}
)
@RunWith(PowerMockRunner.class)
public class SearchLocationDDMFormFieldTemplateContextContributorTest {

	@Before
	public void setUp() throws Exception {
		_setUpGooglePlacesUtil();
		_setUpJSONFactory();
		_setUpLanguageUtil();
		_setUpResourceBundleUtil();
	}

	@Test
	public void testGetParameters() throws Exception {
		Map<String, Object> parameters =
			_searchLocationDDMFormFieldTemplateContextContributor.getParameters(
				DDMFormTestUtil.createSearchLocationDDMFormField(
					_createLocalizedValue("[\"two-columns\"]"),
					StringUtil.randomString(),
					_createLocalizedValue("[\"city\",\"country\"]")),
				_createDDMFormFieldRenderingContext());

		Assert.assertEquals(
			"googlePlacesAPIKey", parameters.get("googlePlacesAPIKey"));
		Assert.assertTrue(
			JSONUtil.equals(
				_jsonFactory.createJSONObject(
					"{\"city\":\"City\",\"country\":\"Country\"}"),
				(JSONObject)parameters.get("labels")));
		Assert.assertEquals("[\"two-columns\"]", parameters.get("layout"));
		Assert.assertEquals(
			"[\"city\",\"country\"]", parameters.get("visibleFields"));
	}

	private DDMFormFieldRenderingContext _createDDMFormFieldRenderingContext() {
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext =
			new DDMFormFieldRenderingContext();

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, PowerMockito.mock(ThemeDisplay.class));

		ddmFormFieldRenderingContext.setHttpServletRequest(
			mockHttpServletRequest);

		ddmFormFieldRenderingContext.setLocale(LocaleUtil.US);

		return ddmFormFieldRenderingContext;
	}

	private LocalizedValue _createLocalizedValue(String value) {
		LocalizedValue localizedValue = new LocalizedValue();

		localizedValue.addString(LocaleUtil.US, value);

		return localizedValue;
	}

	private void _mockGet(String key, String message) {
		PowerMockito.when(
			LanguageUtil.get(
				Matchers.any(ResourceBundle.class), Matchers.eq(key))
		).thenReturn(
			message
		);
	}

	private void _setUpGooglePlacesUtil() {
		PowerMockito.mockStatic(GooglePlacesUtil.class);

		PowerMockito.when(
			GooglePlacesUtil.getGooglePlacesAPIKey(
				Matchers.anyLong(), Matchers.anyLong(),
				Matchers.any(GroupLocalService.class))
		).thenReturn(
			"googlePlacesAPIKey"
		);
	}

	private void _setUpJSONFactory() throws Exception {
		MemberMatcher.field(
			SearchLocationDDMFormFieldTemplateContextContributor.class,
			"_jsonFactory"
		).set(
			_searchLocationDDMFormFieldTemplateContextContributor, _jsonFactory
		);
	}

	private void _setUpLanguageUtil() {
		PowerMockito.mockStatic(LanguageUtil.class);

		_mockGet("address", "Address");
		_mockGet("city", "City");
		_mockGet("country", "Country");
		_mockGet("postal-code", "Postal Code");
		_mockGet("state", "State");
	}

	private void _setUpResourceBundleUtil() {
		PowerMockito.mockStatic(ResourceBundleUtil.class);

		PowerMockito.when(
			ResourceBundleUtil.getModuleAndPortalResourceBundle(
				Matchers.any(Locale.class), Matchers.any())
		).thenReturn(
			PowerMockito.mock(ResourceBundle.class)
		);
	}

	private final JSONFactory _jsonFactory = new JSONFactoryImpl();
	private final SearchLocationDDMFormFieldTemplateContextContributor
		_searchLocationDDMFormFieldTemplateContextContributor =
			new SearchLocationDDMFormFieldTemplateContextContributor();

}