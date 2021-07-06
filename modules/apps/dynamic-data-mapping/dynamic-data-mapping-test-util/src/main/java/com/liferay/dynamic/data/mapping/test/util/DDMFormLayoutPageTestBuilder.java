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

package com.liferay.dynamic.data.mapping.test.util;

import com.liferay.dynamic.data.mapping.model.DDMFormLayoutPage;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutRow;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;

/**
 * @author Rodrigo Paulino
 */
public class DDMFormLayoutPageTestBuilder {

	public static DDMFormLayoutPageTestBuilder newBuilder() {
		return new DDMFormLayoutPageTestBuilder();
	}

	public DDMFormLayoutPage build() {
		DDMFormLayoutPage ddmFormLayoutPage = new DDMFormLayoutPage();

		if (_ddmFormLayoutRows != null) {
			ddmFormLayoutPage.setDDMFormLayoutRows(
				ListUtil.fromArray(_ddmFormLayoutRows));
		}

		if (_description != null) {
			ddmFormLayoutPage.setDescription(
				_createLocalizedValue(_description));
		}

		if (_title != null) {
			ddmFormLayoutPage.setTitle(_createLocalizedValue(_title));
		}

		return ddmFormLayoutPage;
	}

	public DDMFormLayoutPageTestBuilder withDDMFormLayoutRows(
		DDMFormLayoutRow... ddmFormLayoutRows) {

		_ddmFormLayoutRows = ddmFormLayoutRows;

		return this;
	}

	public DDMFormLayoutPageTestBuilder withDescription(String description) {
		_description = description;

		return this;
	}

	public DDMFormLayoutPageTestBuilder withTitle(String title) {
		_title = title;

		return this;
	}

	private DDMFormLayoutPageTestBuilder() {
	}

	private LocalizedValue _createLocalizedValue(String value) {
		LocalizedValue localizedValue = new LocalizedValue();

		localizedValue.addString(LocaleUtil.US, value);

		return localizedValue;
	}

	private DDMFormLayoutRow[] _ddmFormLayoutRows;
	private String _description;
	private String _title;

}