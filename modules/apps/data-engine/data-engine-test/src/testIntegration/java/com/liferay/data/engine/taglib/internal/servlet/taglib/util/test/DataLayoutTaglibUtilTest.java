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

package com.liferay.data.engine.taglib.internal.servlet.taglib.util.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.data.engine.rest.dto.v2_0.DataDefinition;
import com.liferay.data.engine.rest.test.util.DataDefinitionTestUtil;
import com.liferay.data.engine.taglib.internal.servlet.taglib.util.DataLayoutTaglibUtil;
import com.liferay.data.engine.test.util.servlet.taglib.definition.TestDataLayoutBuilderDefinition;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.InputStream;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Marcela Cunha
 * @author Rodrigo Paulino
 */
@RunWith(Arquillian.class)
public class DataLayoutTaglibUtilTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetDataLayoutJSONObjectWithDisabledProperties()
		throws Exception {

		DataDefinition dataDefinition =
			DataDefinitionTestUtil.addDataDefinition(
				"test", TestPropsValues.getGroupId(),
				_read("test-data-definition.json"), TestPropsValues.getUser());

		JSONObject jsonObject = _dataLayoutTaglibUtil.getDataLayoutJSONObject(
			Collections.singleton(LocaleUtil.US), "test",
			dataDefinition.getId(), null, _createMockHttpServletRequest(),
			new MockHttpServletResponse());

		JSONObject testFieldJSONObject = _getFieldJSONObject(
			jsonObject, "TestField");

		JSONObject disabledPropertyJSONObject = _getFieldJSONObject(
			testFieldJSONObject.getJSONObject("settingsContext"),
			"disabledProperty");

		Assert.assertNull(disabledPropertyJSONObject);
	}

	@Test
	public void testGetDataLayoutJSONObjectWithoutDisabledProperties()
		throws Exception {

		String[] disabledProperties =
			_testDataLayoutBuilderDefinition.getDisabledProperties();

		_testDataLayoutBuilderDefinition.setDisabledProperties();

		DataDefinition dataDefinition =
			DataDefinitionTestUtil.addDataDefinition(
				"test", TestPropsValues.getGroupId(),
				_read("test-data-definition.json"), TestPropsValues.getUser());

		JSONObject jsonObject = _dataLayoutTaglibUtil.getDataLayoutJSONObject(
			Collections.singleton(LocaleUtil.US), "test",
			dataDefinition.getId(), null, _createMockHttpServletRequest(),
			new MockHttpServletResponse());

		_testDataLayoutBuilderDefinition.setDisabledProperties(
			disabledProperties);

		JSONObject testFieldJSONObject = _getFieldJSONObject(
			jsonObject, "TestField");

		JSONObject disabledPropertyJSONObject = _getFieldJSONObject(
			testFieldJSONObject.getJSONObject("settingsContext"),
			"disabledProperty");

		Assert.assertNotNull(disabledPropertyJSONObject);
	}

	@Test
	public void testGetFieldTypesJSONArrayWithSearchableFieldsDisabled()
		throws Exception {

		JSONArray jsonArray = _dataLayoutTaglibUtil.getFieldTypesJSONArray(
			_createMockHttpServletRequest(), Collections.singleton("test"),
			true);

		JSONObject fieldTypeJSONObject = jsonArray.getJSONObject(0);

		JSONObject indexTypeJSONObject = _getFieldJSONObject(
			fieldTypeJSONObject.getJSONObject("settingsContext"), "indexType");

		Assert.assertEquals("none", indexTypeJSONObject.getString("value"));
	}

	@Test
	public void testGetFieldTypesJSONArrayWithSearchableFieldsEnabled()
		throws Exception {

		JSONArray jsonArray = _dataLayoutTaglibUtil.getFieldTypesJSONArray(
			_createMockHttpServletRequest(), Collections.singleton("test"),
			false);

		JSONObject fieldTypeJSONObject = jsonArray.getJSONObject(0);

		JSONObject indexTypeJSONObject = _getFieldJSONObject(
			fieldTypeJSONObject.getJSONObject("settingsContext"), "indexType");

		Assert.assertEquals("keyword", indexTypeJSONObject.getString("value"));
	}

	private MockHttpServletRequest _createMockHttpServletRequest()
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.USER, TestPropsValues.getUser());

		return mockHttpServletRequest;
	}

	private JSONObject _getFieldJSONObject(
		JSONObject jsonObject, String fieldName) {

		Optional<JSONObject> jsonObjectOptional = _stream(
			"rows",
			rowJSONObject -> _stream(
				"fields", null, _stream(rowJSONObject.getJSONArray("columns"))),
			_stream(jsonObject.getJSONArray("pages"))
		).filter(
			fieldJSONObject -> Objects.equals(
				fieldJSONObject.getString("fieldName"), fieldName)
		).findFirst();

		return jsonObjectOptional.orElse(null);
	}

	private String _read(String fileName) throws Exception {
		Class<?> clazz = getClass();

		InputStream inputStream = clazz.getResourceAsStream(
			"dependencies/" + fileName);

		return StringUtil.read(inputStream);
	}

	private Stream<JSONObject> _stream(JSONArray jsonArray) {
		return StreamSupport.stream(jsonArray.spliterator(), true);
	}

	private Stream<JSONObject> _stream(
		String key, Function<JSONObject, Stream<JSONObject>> function,
		Stream<JSONObject> stream) {

		return stream.flatMap(
			jsonObject -> {
				Stream<JSONObject> nestedStream = _stream(
					jsonObject.getJSONArray(key));

				if (function == null) {
					return nestedStream;
				}

				return nestedStream.flatMap(function);
			});
	}

	@Inject
	private static TestDataLayoutBuilderDefinition
		_testDataLayoutBuilderDefinition;

	private final DataLayoutTaglibUtil _dataLayoutTaglibUtil =
		new DataLayoutTaglibUtil();

}