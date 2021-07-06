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

import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutPage;
import com.liferay.portal.kernel.util.ListUtil;

/**
 * @author Rodrigo Paulino
 */
public class DDMFormLayoutTestBuilder {

	public static DDMFormLayoutTestBuilder newBuilder() {
		return new DDMFormLayoutTestBuilder();
	}

	public DDMFormLayout build() {
		DDMFormLayout ddmFormLayout = new DDMFormLayout();

		if (_ddmFormLayoutPages != null) {
			ddmFormLayout.setDDMFormLayoutPages(
				ListUtil.fromArray(_ddmFormLayoutPages));
		}

		if (_paginationMode != null) {
			ddmFormLayout.setPaginationMode(_paginationMode);
		}

		return ddmFormLayout;
	}

	public DDMFormLayoutTestBuilder withDDMFormLayoutPages(
		DDMFormLayoutPage... ddmFormLayoutPages) {

		_ddmFormLayoutPages = ddmFormLayoutPages;

		return this;
	}

	public DDMFormLayoutTestBuilder withPaginationMode(String paginationMode) {
		_paginationMode = paginationMode;

		return this;
	}

	private DDMFormLayoutTestBuilder() {
	}

	private DDMFormLayoutPage[] _ddmFormLayoutPages;
	private String _paginationMode;

}