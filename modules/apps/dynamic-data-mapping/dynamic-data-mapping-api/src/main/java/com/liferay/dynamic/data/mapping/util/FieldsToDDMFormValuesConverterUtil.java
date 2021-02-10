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

package com.liferay.dynamic.data.mapping.util;

import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.UnlocalizedValue;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.storage.Field;
import com.liferay.dynamic.data.mapping.storage.Fields;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Rodrigo Paulino
 */
public class FieldsToDDMFormValuesConverterUtil {

	public static DDMFormValues convert(
			DDMStructure ddmStructure, Fields fields,
			boolean nestParentStructureFields)
		throws PortalException {

		DDMForm ddmForm = ddmStructure.getDDMForm();

		if (!nestParentStructureFields) {
			ddmForm = ddmStructure.getFullHierarchyDDMForm();
		}

		Map<String, DDMFormField> ddmFormFieldsMap =
			ddmForm.getDDMFormFieldsMap(true);

		DDMFormValues ddmFormValues = _createDDMFormValues(
			ddmForm, fields.getAvailableLocales(), fields.getDefaultLocale());

		DDMFieldsCounter ddmFieldsCounter = new DDMFieldsCounter();

		for (DDMFormField ddmFormField : ddmForm.getDDMFormFields()) {
			int repetitions = 0;

			if (nestParentStructureFields &&
				_isUpgradedFieldset(ddmFormField)) {

				repetitions = 1;
			}
			else {
				repetitions = _countDDMFieldRepetitions(
					ddmFormFieldsMap, fields, ddmFormField.getName(), null, -1);
			}

			for (int i = 0; i < repetitions; i++) {
				DDMFormFieldValue ddmFormFieldValue = _createDDMFormFieldValue(
					ddmFormField.getName());

				ddmFormFieldValue.setFieldReference(
					ddmFormField.getFieldReference());

				_setDDMFormFieldValueProperties(
					ddmFormFieldValue, ddmFormFieldsMap, fields,
					ddmFieldsCounter, nestParentStructureFields);

				ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);
			}
		}

		return ddmFormValues;
	}

	private static int _countDDMFieldRepetitions(
			Map<String, DDMFormField> ddmFormFieldsMap, Fields ddmFields,
			String fieldName, String parentFieldName, int parentOffset)
		throws PortalException {

		Field ddmFieldsDisplayField = ddmFields.get(DDM.FIELDS_DISPLAY_NAME);

		if (ddmFieldsDisplayField == null) {
			if (ddmFields.contains(fieldName)) {
				return 1;
			}

			return 0;
		}

		String[] ddmFieldsDisplayValues = _getDDMFieldsDisplayValues(
			ddmFormFieldsMap, ddmFieldsDisplayField);

		int offset = -1;

		int repetitions = 0;

		for (String fieldDisplayName : ddmFieldsDisplayValues) {
			if (offset > parentOffset) {
				break;
			}

			if (fieldDisplayName.equals(parentFieldName)) {
				offset++;
			}

			if (fieldDisplayName.equals(fieldName) &&
				(offset == parentOffset)) {

				repetitions++;
			}
		}

		return repetitions;
	}

	private static DDMFormFieldValue _createDDMFormFieldValue(String name) {
		DDMFormFieldValue ddmFormFieldValue = new DDMFormFieldValue();

		ddmFormFieldValue.setName(name);

		return ddmFormFieldValue;
	}

	private static DDMFormValues _createDDMFormValues(
		DDMForm ddmForm, Set<Locale> availableLocales, Locale defaultLocale) {

		DDMFormValues ddmFormValues = new DDMFormValues(ddmForm);

		ddmFormValues.setAvailableLocales(availableLocales);
		ddmFormValues.setDefaultLocale(defaultLocale);

		return ddmFormValues;
	}

	private static String _getDDMFieldInstanceId(
		Fields ddmFields, String fieldName, int index) {

		Field ddmFieldsDisplayField = ddmFields.get(DDM.FIELDS_DISPLAY_NAME);

		if (ddmFieldsDisplayField == null) {
			return StringUtil.randomString();
		}

		String prefix = fieldName.concat(DDM.INSTANCE_SEPARATOR);

		String[] ddmFieldsDisplayValues = StringUtil.split(
			(String)ddmFieldsDisplayField.getValue());

		for (String ddmFieldsDisplayValue : ddmFieldsDisplayValues) {
			if (ddmFieldsDisplayValue.startsWith(prefix)) {
				index--;

				if (index < 0) {
					return StringUtil.extractLast(
						ddmFieldsDisplayValue, DDM.INSTANCE_SEPARATOR);
				}
			}
		}

		return null;
	}

	private static String[] _getDDMFieldsDisplayValues(
			Map<String, DDMFormField> ddmFormFieldsMap,
			Field ddmFieldsDisplayField)
		throws PortalException {

		try {
			List<String> fieldsDisplayValues = new ArrayList<>();

			String[] values = _splitFieldsDisplayValue(ddmFieldsDisplayField);

			for (String value : values) {
				String fieldName = StringUtil.extractFirst(
					value, DDM.INSTANCE_SEPARATOR);

				if (ddmFormFieldsMap.containsKey(fieldName)) {
					fieldsDisplayValues.add(fieldName);
				}
			}

			return fieldsDisplayValues.toArray(new String[0]);
		}
		catch (Exception exception) {
			throw new PortalException(exception);
		}
	}

	private static String _getDDMFieldValueString(
		Field ddmField, Locale locale, int index) {

		Serializable fieldValue = ddmField.getValue(locale, index);

		if (fieldValue instanceof Date) {
			Date valueDate = (Date)fieldValue;

			fieldValue = valueDate.getTime();
		}
		else if ((fieldValue instanceof Number) &&
				 !(fieldValue instanceof Integer)) {

			NumberFormat numberFormat = NumberFormat.getInstance(locale);

			Number number = (Number)fieldValue;

			if (number instanceof Double || number instanceof Float) {
				numberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
				numberFormat.setMinimumFractionDigits(1);
			}

			return numberFormat.format(number.doubleValue());
		}

		return String.valueOf(fieldValue);
	}

	private static List<String> _getDDMFormFieldNames(
		List<DDMFormField> ddmFormFields) {

		List<String> fieldNames = new ArrayList<>();

		for (DDMFormField ddmFormField : ddmFormFields) {
			fieldNames.add(ddmFormField.getName());
		}

		return fieldNames;
	}

	private static boolean _isUpgradedFieldset(DDMFormField ddmFormField) {
		if (StringUtil.equals(ddmFormField.getType(), "fieldset") &&
			GetterUtil.getBoolean(
				ddmFormField.getProperty("upgradedStructure"))) {

			return true;
		}

		return false;
	}

	private static void _setDDMFormFieldValueInstanceId(
		DDMFormFieldValue ddmFormFieldValue, Fields ddmFields,
		DDMFieldsCounter ddmFieldsCounter) {

		String name = ddmFormFieldValue.getName();

		String instanceId = _getDDMFieldInstanceId(
			ddmFields, name, ddmFieldsCounter.get(name));

		ddmFormFieldValue.setInstanceId(instanceId);
	}

	private static void _setDDMFormFieldValueLocalizedValue(
		DDMFormFieldValue ddmFormFieldValue, Field ddmField, int index) {

		Value value = new LocalizedValue(ddmField.getDefaultLocale());

		for (Locale availableLocale : ddmField.getAvailableLocales()) {
			String valueString = _getDDMFieldValueString(
				ddmField, availableLocale, index);

			value.addString(availableLocale, valueString);
		}

		ddmFormFieldValue.setValue(value);
	}

	private static void _setDDMFormFieldValueProperties(
			DDMFormFieldValue ddmFormFieldValue,
			Map<String, DDMFormField> ddmFormFieldsMap, Fields ddmFields,
			DDMFieldsCounter ddmFieldsCounter,
			boolean nestParentStructureFields)
		throws PortalException {

		_setDDMFormFieldValueInstanceId(
			ddmFormFieldValue, ddmFields, ddmFieldsCounter);

		_setNestedDDMFormFieldValues(
			ddmFormFieldValue, ddmFormFieldsMap, ddmFields, ddmFieldsCounter,
			nestParentStructureFields);

		_setDDMFormFieldValueValues(
			ddmFormFieldValue, ddmFormFieldsMap, ddmFields, ddmFieldsCounter);
	}

	private static void _setDDMFormFieldValueUnlocalizedValue(
		DDMFormFieldValue ddmFormFieldValue, Field ddmField, int index) {

		String valueString = _getDDMFieldValueString(
			ddmField, ddmField.getDefaultLocale(), index);

		Value value = new UnlocalizedValue(valueString);

		ddmFormFieldValue.setValue(value);
	}

	private static void _setDDMFormFieldValueValues(
			DDMFormFieldValue ddmFormFieldValue,
			Map<String, DDMFormField> ddmFormFieldMap, Fields ddmFields,
			DDMFieldsCounter ddmFieldsCounter)
		throws PortalException {

		String fieldName = ddmFormFieldValue.getName();

		DDMFormField ddmFormField = ddmFormFieldMap.get(fieldName);

		if (Validator.isNotNull(ddmFormField.getDataType())) {
			if (ddmFormField.isLocalizable()) {
				_setDDMFormFieldValueLocalizedValue(
					ddmFormFieldValue, ddmFields.get(fieldName),
					ddmFieldsCounter.get(fieldName));
			}
			else {
				_setDDMFormFieldValueUnlocalizedValue(
					ddmFormFieldValue, ddmFields.get(fieldName),
					ddmFieldsCounter.get(fieldName));
			}
		}

		ddmFieldsCounter.incrementKey(fieldName);
	}

	private static void _setNestedDDMFormFieldValues(
			DDMFormFieldValue ddmFormFieldValue,
			Map<String, DDMFormField> ddmFormFieldsMap, Fields ddmFields,
			DDMFieldsCounter ddmFieldsCounter,
			boolean nestParentStructureFields)
		throws PortalException {

		int parentOffset = 0;

		String fieldName = ddmFormFieldValue.getName();

		DDMFormField parentDDMFormField = ddmFormFieldsMap.get(fieldName);

		if (nestParentStructureFields &&
			_isUpgradedFieldset(parentDDMFormField)) {

			parentOffset = -1;
		}
		else {
			parentOffset = ddmFieldsCounter.get(fieldName);
		}

		List<String> nestedFieldNames = _getDDMFormFieldNames(
			parentDDMFormField.getNestedDDMFormFields());

		for (String nestedFieldName : nestedFieldNames) {
			int repetitions = _countDDMFieldRepetitions(
				ddmFormFieldsMap, ddmFields, nestedFieldName, fieldName,
				parentOffset);

			for (int i = 0; i < repetitions; i++) {
				DDMFormFieldValue nestedDDMFormFieldValue =
					_createDDMFormFieldValue(nestedFieldName);

				DDMFormField nestedDDMFormField = ddmFormFieldsMap.get(
					nestedFieldName);

				if (nestedDDMFormField != null) {
					nestedDDMFormFieldValue.setFieldReference(
						nestedDDMFormField.getFieldReference());
				}

				_setDDMFormFieldValueProperties(
					nestedDDMFormFieldValue, ddmFormFieldsMap, ddmFields,
					ddmFieldsCounter, nestParentStructureFields);

				ddmFormFieldValue.addNestedDDMFormFieldValue(
					nestedDDMFormFieldValue);
			}
		}
	}

	private static String[] _splitFieldsDisplayValue(Field fieldsDisplayField) {
		String value = (String)fieldsDisplayField.getValue();

		return StringUtil.split(value);
	}

}