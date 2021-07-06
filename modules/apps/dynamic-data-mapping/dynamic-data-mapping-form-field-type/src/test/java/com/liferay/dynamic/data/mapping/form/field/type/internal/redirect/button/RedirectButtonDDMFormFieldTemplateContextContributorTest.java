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

package com.liferay.dynamic.data.mapping.form.field.type.internal.redirect.button;

import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.render.DDMFormFieldRenderingContext;
import com.liferay.dynamic.data.mapping.test.util.DDMFormTestUtil;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletURL;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Map;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Rodrigo Paulino
 */
@PrepareForTest(RequestBackedPortletURLFactoryUtil.class)
@RunWith(PowerMockRunner.class)
public class RedirectButtonDDMFormFieldTemplateContextContributorTest {

	@Before
	public void setUp() throws Exception {
		_setUpRequestBackedPortletURLFactoryUtil();
	}

	@Test
	public void testGetParameters() {
		Map<String, Object> parameters =
			_redirectButtonDDMFormFieldTemplateContextContributor.getParameters(
				DDMFormTestUtil.createRedirectButtonDDMFormField(
					_createLocalizedValue("buttonLabel"),
					_createLocalizedValue("message"), "mvcRenderCommandName",
					StringUtil.randomString(), "[parameterName=parameterValue]",
					_PORTLET_ID, _createLocalizedValue("title")),
				_mockDDMFormFieldRenderingContext());

		Assert.assertEquals("buttonLabel", parameters.get("buttonLabel"));
		Assert.assertEquals("message", parameters.get("message"));
		Assert.assertEquals(
			"http//localhost/test?portletId_mvcRenderCommandName=" +
				"mvcRenderCommandName;portletId_parameterName=parameterValue",
			parameters.get("redirectURL"));
		Assert.assertEquals("title", parameters.get("title"));
	}

	private LocalizedValue _createLocalizedValue(String value) {
		LocalizedValue localizedValue = new LocalizedValue();

		localizedValue.addString(LocaleUtil.US, value);

		return localizedValue;
	}

	private DDMFormFieldRenderingContext _mockDDMFormFieldRenderingContext() {
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext =
			PowerMockito.mock(DDMFormFieldRenderingContext.class);

		PowerMockito.when(
			ddmFormFieldRenderingContext.getLocale()
		).thenReturn(
			LocaleUtil.US
		);

		return ddmFormFieldRenderingContext;
	}

	private PortletURL _mockPortletURL() {
		MockLiferayPortletURL mockLiferayPortletURL =
			new MockLiferayPortletURL();

		mockLiferayPortletURL.setPortletId(_PORTLET_ID);

		return mockLiferayPortletURL;
	}

	private RequestBackedPortletURLFactory
		_mockRequestBackedPortletURLFactory() {

		RequestBackedPortletURLFactory requestBackedPortletURLFactory =
			PowerMockito.mock(RequestBackedPortletURLFactory.class);

		PowerMockito.when(
			requestBackedPortletURLFactory.createActionURL(Matchers.anyString())
		).thenReturn(
			_mockPortletURL()
		);

		return requestBackedPortletURLFactory;
	}

	private void _setUpRequestBackedPortletURLFactoryUtil() {
		PowerMockito.mockStatic(RequestBackedPortletURLFactoryUtil.class);

		RequestBackedPortletURLFactory requestBackedPortletURLFactory =
			_mockRequestBackedPortletURLFactory();

		PowerMockito.when(
			RequestBackedPortletURLFactoryUtil.create(
				Matchers.any(HttpServletRequest.class))
		).thenReturn(
			requestBackedPortletURLFactory
		);
	}

	private static final String _PORTLET_ID = "portletId";

	private final RedirectButtonDDMFormFieldTemplateContextContributor
		_redirectButtonDDMFormFieldTemplateContextContributor =
			new RedirectButtonDDMFormFieldTemplateContextContributor();

}