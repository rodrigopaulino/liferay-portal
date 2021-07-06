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
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutRow;
import com.liferay.portal.kernel.util.ListUtil;

/**
 * @author Rodrigo Paulino
 */
public class DDMFormLayoutRowTestBuilder {

	public static DDMFormLayoutRowTestBuilder newBuilder() {
		return new DDMFormLayoutRowTestBuilder();
	}

	public DDMFormLayoutRow build() {
		DDMFormLayoutRow ddmFormLayoutRow = new DDMFormLayoutRow();

		if (_ddmFormLayoutColumns != null) {
			ddmFormLayoutRow.setDDMFormLayoutColumns(
				ListUtil.fromArray(_ddmFormLayoutColumns));
		}

		return ddmFormLayoutRow;
	}

	public DDMFormLayoutRowTestBuilder withDDMFormLayoutColumns(
		DDMFormLayoutColumn... ddmFormLayoutColumns) {

		_ddmFormLayoutColumns = ddmFormLayoutColumns;

		return this;
	}

	private DDMFormLayoutRowTestBuilder() {
	}

	private DDMFormLayoutColumn[] _ddmFormLayoutColumns;

}