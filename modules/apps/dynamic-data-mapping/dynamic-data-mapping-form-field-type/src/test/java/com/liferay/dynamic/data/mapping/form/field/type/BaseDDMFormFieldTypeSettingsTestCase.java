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

package com.liferay.dynamic.data.mapping.form.field.type;

import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutColumn;
import com.liferay.dynamic.data.mapping.test.util.DDMFormLayoutColumnTestBuilder;
import com.liferay.dynamic.data.mapping.test.util.DDMFormLayoutPageTestBuilder;
import com.liferay.dynamic.data.mapping.test.util.DDMFormLayoutRowTestBuilder;
import com.liferay.dynamic.data.mapping.test.util.DDMFormLayoutTestBuilder;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.SetUtil;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

import org.mockito.Mock;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Leonardo Barros
 */
@PrepareForTest({PortalClassLoaderUtil.class, ResourceBundleUtil.class})
@RunWith(PowerMockRunner.class)
public abstract class BaseDDMFormFieldTypeSettingsTestCase
	extends PowerMockito {

	@Before
	public void setUp() throws Exception {
		setUpLanguageUtil();
		setUpPortalClassLoaderUtil();
		setUpResourceBundleUtil();
	}

	protected void assertDDMFormLayout(
		DDMFormLayout actualDDMFormLayout,
		DDMFormLayout expectedDDMFormLayout) {

		_assertObject(
			actualDDMFormLayout, expectedDDMFormLayout,
			expectedObjects1 -> actualObject1 -> _assertObject(
				actualObject1, expectedObjects1.remove(0),
				expectedObjects2 -> actualObject2 -> _assertObject(
					actualObject2, expectedObjects2.remove(0),
					expectedObjects3 ->
						actualObject3 -> _assertDDMFormLayoutColumn(
							(DDMFormLayoutColumn)actualObject3,
							(DDMFormLayoutColumn)expectedObjects3.remove(0)),
					"getDDMFormLayoutColumns", null),
				"getDDMFormLayoutRows", null),
			"getDDMFormLayoutPages",
			() -> Assert.assertEquals(
				expectedDDMFormLayout.getPaginationMode(),
				actualDDMFormLayout.getPaginationMode()));
	}

	protected void setUpLanguageUtil() {
		Set<Locale> availableLocales = SetUtil.fromArray(
			new Locale[] {LocaleUtil.US});

		when(
			language.getAvailableLocales()
		).thenReturn(
			availableLocales
		);

		LanguageUtil languageUtil = new LanguageUtil();

		languageUtil.setLanguage(language);
	}

	protected void setUpPortalClassLoaderUtil() {
		mockStatic(PortalClassLoaderUtil.class);

		when(
			PortalClassLoaderUtil.getClassLoader()
		).thenReturn(
			_classLoader
		);
	}

	protected void setUpResourceBundleUtil() {
		mockStatic(ResourceBundleUtil.class);

		when(
			ResourceBundleUtil.getBundle(
				"content.Language", LocaleUtil.BRAZIL, _classLoader)
		).thenReturn(
			_resourceBundle
		);

		when(
			ResourceBundleUtil.getBundle(
				"content.Language", LocaleUtil.US, _classLoader)
		).thenReturn(
			_resourceBundle
		);
	}

	protected final DDMFormLayoutColumnTestBuilder
		ddmFormLayoutColumnTestBuilder =
			DDMFormLayoutColumnTestBuilder.newBuilder();
	protected final DDMFormLayoutPageTestBuilder ddmFormLayoutPageTestBuilder =
		DDMFormLayoutPageTestBuilder.newBuilder();
	protected final DDMFormLayoutRowTestBuilder ddmFormLayoutRowTestBuilder =
		DDMFormLayoutRowTestBuilder.newBuilder();
	protected final DDMFormLayoutTestBuilder ddmFormLayoutTestBuilder =
		DDMFormLayoutTestBuilder.newBuilder();

	@Mock
	protected Language language;

	private void _assertDDMFormLayoutColumn(
		DDMFormLayoutColumn actualDDMFormLayoutColumn,
		DDMFormLayoutColumn expectedDDMFormLayoutColumn) {

		Assert.assertEquals(
			expectedDDMFormLayoutColumn.getDDMFormFieldNames(),
			actualDDMFormLayoutColumn.getDDMFormFieldNames());
		Assert.assertEquals(
			expectedDDMFormLayoutColumn.getSize(),
			actualDDMFormLayoutColumn.getSize());
	}

	private void _assertObject(
		Object actualObject, Object expectedObject,
		Function<List<Object>, Consumer<Object>> function, String methodName,
		Runnable runnable) {

		List<Object> expectedObjects = ReflectionTestUtil.invoke(
			expectedObject, methodName, null);

		List<Object> actualObjects = ReflectionTestUtil.invoke(
			actualObject, methodName, null);

		Stream<Object> stream = actualObjects.stream();

		stream.forEachOrdered(function.apply(expectedObjects));

		Assert.assertEquals(
			expectedObjects.toString(), 0, expectedObjects.size());

		Optional.ofNullable(
			runnable
		).ifPresent(
			Runnable::run
		);
	}

	@Mock
	private ClassLoader _classLoader;

	@Mock
	private ResourceBundle _resourceBundle;

}