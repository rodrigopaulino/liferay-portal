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
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutColumn;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutPage;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutRow;

import java.util.stream.Stream;

/**
 * @author Rodrigo Paulino
 */
public class DDMFormLayoutTestUtil {

	public static DDMFormLayout createDDMFormLayout(
		DDMFormLayoutPage... ddmFormLayoutPages) {

		DDMFormLayoutTestBuilder ddmFormLayoutTestBuilder =
			DDMFormLayoutTestBuilder.newBuilder();

		return ddmFormLayoutTestBuilder.withDDMFormLayoutPages(
			ddmFormLayoutPages
		).build();
	}

	public static DDMFormLayout createDDMFormLayout(
		String paginationMode, DDMFormLayoutPage... ddmFormLayoutPages) {

		DDMFormLayoutTestBuilder ddmFormLayoutTestBuilder =
			DDMFormLayoutTestBuilder.newBuilder();

		return ddmFormLayoutTestBuilder.withDDMFormLayoutPages(
			ddmFormLayoutPages
		).withPaginationMode(
			paginationMode
		).build();
	}

	public static DDMFormLayout createDDMFormLayout(
		String pageDescription, String pageTitle, String[] ddmFormFieldNames) {

		DDMFormLayoutTestBuilder ddmFormLayoutTestBuilder =
			DDMFormLayoutTestBuilder.newBuilder();

		return ddmFormLayoutTestBuilder.withDDMFormLayoutPages(
			createDDMFormLayoutPage(
				pageDescription, pageTitle, ddmFormFieldNames)
		).build();
	}

	public static DDMFormLayoutColumn createDDMFormLayoutColumn(
		String... ddmFormFieldNames) {

		DDMFormLayoutColumnTestBuilder ddmFormLayoutColumnTestBuilder =
			DDMFormLayoutColumnTestBuilder.newBuilder();

		return ddmFormLayoutColumnTestBuilder.withDDMFormFieldNames(
			ddmFormFieldNames
		).withSize(
			DDMFormLayoutColumn.FULL
		).build();
	}

	public static DDMFormLayoutColumn[] createDDMFormLayoutColumns(
		String... ddmFormFieldNames) {

		DDMFormLayoutColumnTestBuilder ddmFormLayoutColumnTestBuilder =
			DDMFormLayoutColumnTestBuilder.newBuilder();

		return Stream.of(
			ddmFormFieldNames
		).map(
			ddmFormFieldName ->
				ddmFormLayoutColumnTestBuilder.withDDMFormFieldNames(
					ddmFormFieldName
				).withSize(
					DDMFormLayoutColumn.FULL / ddmFormFieldNames.length
				).build()
		).toArray(
			DDMFormLayoutColumn[]::new
		);
	}

	public static DDMFormLayoutPage createDDMFormLayoutPage(
		String... ddmFormFieldNames) {

		DDMFormLayoutPageTestBuilder ddmFormLayoutPageTestBuilder =
			DDMFormLayoutPageTestBuilder.newBuilder();

		return ddmFormLayoutPageTestBuilder.withDDMFormLayoutRows(
			createDDMFormLayoutRow(ddmFormFieldNames)
		).build();
	}

	public static DDMFormLayoutPage createDDMFormLayoutPage(
		String description, String title,
		DDMFormLayoutColumn... ddmFormLayoutColumns) {

		DDMFormLayoutPageTestBuilder ddmFormLayoutPageTestBuilder =
			DDMFormLayoutPageTestBuilder.newBuilder();

		return ddmFormLayoutPageTestBuilder.withDDMFormLayoutRows(
			createDDMFormLayoutRow(ddmFormLayoutColumns)
		).withDescription(
			description
		).withTitle(
			title
		).build();
	}

	public static DDMFormLayoutPage createDDMFormLayoutPage(
		String description, String title,
		DDMFormLayoutRow... ddmFormLayoutRows) {

		DDMFormLayoutPageTestBuilder ddmFormLayoutPageTestBuilder =
			DDMFormLayoutPageTestBuilder.newBuilder();

		return ddmFormLayoutPageTestBuilder.withDDMFormLayoutRows(
			ddmFormLayoutRows
		).withDescription(
			description
		).withTitle(
			title
		).build();
	}

	public static DDMFormLayoutPage createDDMFormLayoutPage(
		String description, String title, String[] ddmFormFieldNames) {

		return createDDMFormLayoutPage(
			description, title, createDDMFormLayoutColumn(ddmFormFieldNames));
	}

	public static DDMFormLayoutRow createDDMFormLayoutRow(
		DDMFormLayoutColumn... ddmFormLayoutColumns) {

		DDMFormLayoutRowTestBuilder ddmFormLayoutRowTestBuilder =
			DDMFormLayoutRowTestBuilder.newBuilder();

		return ddmFormLayoutRowTestBuilder.withDDMFormLayoutColumns(
			ddmFormLayoutColumns
		).build();
	}

	public static DDMFormLayoutRow createDDMFormLayoutRow(
		String... ddmFormFieldNames) {

		return createDDMFormLayoutRow(
			createDDMFormLayoutColumn(ddmFormFieldNames));
	}

}