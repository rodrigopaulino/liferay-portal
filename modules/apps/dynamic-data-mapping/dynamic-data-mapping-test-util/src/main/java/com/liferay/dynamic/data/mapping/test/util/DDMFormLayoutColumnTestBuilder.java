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

import com.liferay.dynamic.data.mapping.model.DDMFormLayoutColumn;
import com.liferay.portal.kernel.util.ListUtil;

/**
 * @author Rodrigo Paulino
 */
public class DDMFormLayoutColumnTestBuilder {

	public static DDMFormLayoutColumnTestBuilder newBuilder() {
		return new DDMFormLayoutColumnTestBuilder();
	}

	public DDMFormLayoutColumn build() {
		DDMFormLayoutColumn ddmFormLayoutColumn = new DDMFormLayoutColumn();

		if (_ddmFormFieldNames != null) {
			ddmFormLayoutColumn.setDDMFormFieldNames(
				ListUtil.fromArray(_ddmFormFieldNames));
		}

		if (_size != null) {
			ddmFormLayoutColumn.setSize(_size);
		}

		return ddmFormLayoutColumn;
	}

	public DDMFormLayoutColumnTestBuilder withDDMFormFieldNames(
		String... ddmFormFieldNames) {

		_ddmFormFieldNames = ddmFormFieldNames;

		return this;
	}

	public DDMFormLayoutColumnTestBuilder withSize(Integer size) {
		_size = size;

		return this;
	}

	private DDMFormLayoutColumnTestBuilder() {
	}

	private String[] _ddmFormFieldNames;
	private Integer _size;

}