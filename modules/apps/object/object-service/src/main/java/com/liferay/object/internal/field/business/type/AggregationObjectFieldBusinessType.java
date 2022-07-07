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

package com.liferay.object.internal.field.business.type;

import com.liferay.dynamic.data.mapping.form.field.type.constants.DDMFormFieldTypeConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.exception.ObjectFieldSettingNameException;
import com.liferay.object.exception.ObjectFieldSettingValueException;
import com.liferay.object.field.business.type.ObjectFieldBusinessType;
import com.liferay.object.field.render.ObjectFieldRenderingContext;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFieldSetting;
import com.liferay.object.model.ObjectFilter;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFieldSettingLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcela Cunha
 */
@Component(
	immediate = true,
	property = "object.field.business.type.key=" + ObjectFieldConstants.BUSINESS_TYPE_AGGREGATION,
	service = {
		AggregationObjectFieldBusinessType.class, ObjectFieldBusinessType.class
	}
)
public class AggregationObjectFieldBusinessType
	implements ObjectFieldBusinessType {

	@Override
	public Set<String> getAllowedObjectFieldSettingsNames() {
		return SetUtil.fromArray(
			"filters", "function", "relationship", "summarizeField");
	}

	@Override
	public String getDBType() {
		return ObjectFieldConstants.DB_TYPE_STRING;
	}

	@Override
	public String getDDMFormFieldTypeName() {
		return DDMFormFieldTypeConstants.TEXT;
	}

	@Override
	public String getDescription(Locale locale) {
		return LanguageUtil.get(
			ResourceBundleUtil.getModuleAndPortalResourceBundle(
				locale, getClass()),
			"summarize-data-values");
	}

	@Override
	public String getLabel(Locale locale) {
		return LanguageUtil.get(
			ResourceBundleUtil.getModuleAndPortalResourceBundle(
				locale, getClass()),
			"aggregation");
	}

	@Override
	public String getName() {
		return ObjectFieldConstants.BUSINESS_TYPE_AGGREGATION;
	}

	@Override
	public Map<String, Object> getProperties(
		ObjectField objectField,
		ObjectFieldRenderingContext objectFieldRenderingContext) {

		Map<String, Object> properties = HashMapBuilder.<String, Object>put(
			"readOnly", true
		).build();

		ListUtil.isNotEmptyForEach(
			_objectFieldSettingLocalService.
				getObjectFieldSettingsByObjectFieldId(
					objectField.getObjectFieldId()),
			objectFieldSetting -> properties.put(
				objectFieldSetting.getName(), objectFieldSetting.getValue()));

		return properties;
	}

	@Override
	public Set<String> getRequiredObjectFieldSettingsNames() {
		return SetUtil.fromArray("function", "relationship", "summarizeField");
	}

	@Override
	public void validateObjectFieldSettings(
			long companyId, String objectFieldName,
			List<ObjectFieldSetting> objectFieldSettings)
		throws PortalException {

		Set<String> missingRequiredObjectFieldSettingsNames = new HashSet<>();

		Stream<ObjectFieldSetting> stream = objectFieldSettings.stream();

		Map<String, Object> objectFieldSettingsValuesMap = stream.collect(
			Collectors.toMap(
				ObjectFieldSetting::getName,
				this::_getObjectFieldSettingValue));

		Set<String> requiredObjectFieldSettingsNames =
			getRequiredObjectFieldSettingsNames();

		if (Objects.equals(
				GetterUtil.getString(
					objectFieldSettingsValuesMap.get("function")),
				"COUNT")) {

			requiredObjectFieldSettingsNames.remove("summarizeField");
		}

		for (String requiredObjectFieldSettingName :
				requiredObjectFieldSettingsNames) {

			if (Validator.isNull(
					objectFieldSettingsValuesMap.get(
						requiredObjectFieldSettingName))) {

				missingRequiredObjectFieldSettingsNames.add(
					requiredObjectFieldSettingName);
			}
		}

		if (!missingRequiredObjectFieldSettingsNames.isEmpty()) {
			throw new ObjectFieldSettingValueException.MissingRequiredValues(
				objectFieldName, missingRequiredObjectFieldSettingsNames);
		}

		Set<String> notAllowedObjectFieldSettingsNames = new HashSet<>(
			objectFieldSettingsValuesMap.keySet());

		notAllowedObjectFieldSettingsNames.removeAll(
			getAllowedObjectFieldSettingsNames());
		notAllowedObjectFieldSettingsNames.removeAll(
			requiredObjectFieldSettingsNames);

		if (!notAllowedObjectFieldSettingsNames.isEmpty()) {
			throw new ObjectFieldSettingNameException.NotAllowedNames(
				objectFieldName, notAllowedObjectFieldSettingsNames);
		}

		if (!Objects.equals(
				objectFieldSettingsValuesMap.get("function"), "AVERAGE") &&
			!Objects.equals(
				objectFieldSettingsValuesMap.get("function"), "COUNT") &&
			!Objects.equals(
				objectFieldSettingsValuesMap.get("function"), "MAX") &&
			!Objects.equals(
				objectFieldSettingsValuesMap.get("function"), "MIN") &&
			!Objects.equals(
				objectFieldSettingsValuesMap.get("function"), "SUM")) {

			// throw

		}

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinition(
				companyId,
				GetterUtil.getString(
					objectFieldSettingsValuesMap.get("relationshipt")));

		_validateObjectFilters(
			objectDefinition, objectFieldName,
			(List<ObjectFilter>)objectFieldSettingsValuesMap.get("filters"));
	}

	private Object _getObjectFieldSettingValue(
		ObjectFieldSetting objectFieldSetting) {

		if (Objects.equals(objectFieldSetting.getName(), "filters")) {
			return objectFieldSetting.getObjectFilters();
		}

		return objectFieldSetting.getValue();
	}

	private void _validateObjectFilters(
			ObjectDefinition objectDefinition, String objectFieldName,
			List<ObjectFilter> objectFilters)
		throws PortalException {

		if (ListUtil.isEmpty(objectFilters)) {
			return;
		}

		for (ObjectFilter objectFilter : objectFilters) {
			Set<String> missingObjectFilterValues = new HashSet<>();

			if (Validator.isNull(objectFilter.getFilterBy())) {
				missingObjectFilterValues.add("filterBy");
			}

			if (Validator.isNull(objectFilter.getFilterType())) {
				missingObjectFilterValues.add("filterType");
			}

			if (Validator.isNull(objectFilter.getJson())) {
				missingObjectFilterValues.add("json");
			}

			if (!missingObjectFilterValues.isEmpty()) {
				throw new ObjectFieldSettingValueException.
					MissingRequiredValues(
						objectFieldName, missingObjectFilterValues);
			}

			ObjectField objectField = _objectFieldLocalService.fetchObjectField(
				objectDefinition.getObjectDefinitionId(),
				objectFilter.getFilterBy());

			if (objectField == null) {

				// throw

			}

			if (ArrayUtil.contains(
					_NOT_ALLOWED_BUSINESS_TYPES,
					objectField.getBusinessType())) {

				// throw

			}

			if (Objects.equals(
					objectField.getBusinessType(),
					ObjectFieldConstants.BUSINESS_TYPE_DATE)) {

				if (!ArrayUtil.contains(
						_DATE_FILTER_TYPES, objectFilter.getFilterType())) {

					// throw not allowed

				}

				if (Objects.equals("range", objectFilter.getFilterType())) {
					JSONObject jsonObject = _jsonFactory.createJSONObject(
						objectFilter.getJson());

					Set<String> keys = jsonObject.keySet();

					if ((keys.size() != 1) &&
						(jsonObject.get("range") == null)) {

						// throw

					}

					jsonObject = jsonObject.getJSONObject("range");

					if (jsonObject == null) {

						// throw

					}

					keys = jsonObject.keySet();

					if ((keys.size() != 2) &&
						((jsonObject.get("gt") == null) ||
						 (jsonObject.get("lt") == null))) {

						// throw

					}

					String gt = jsonObject.getString("gt");

					if (!gt.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {

						// throw

					}

					String lt = jsonObject.getString("lt");

					if (!lt.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {

						// throw

					}
				}
			}

			if (ArrayUtil.contains(
					_NUMERIC_BUSINESS_TYPES, objectField.getBusinessType())) {

				if (!ArrayUtil.contains(
						_NUMERIC_FILTER_TYPES, objectFilter.getFilterType())) {

					// throw not allowed

				}

				if (Objects.equals("eq", objectFilter.getFilterType())) {
					JSONObject jsonObject = _jsonFactory.createJSONObject(
						objectFilter.getJson());

					Set<String> keys = jsonObject.keySet();

					if ((keys.size() != 1) && (jsonObject.get("eq") == null)) {

						// throw

					}

					if (!(jsonObject.get("eq") instanceof Number)) {

						// throw

					}
				}

				if (Objects.equals("ne", objectFilter.getFilterType())) {
					JSONObject jsonObject = _jsonFactory.createJSONObject(
						objectFilter.getJson());

					Set<String> keys = jsonObject.keySet();

					if ((keys.size() != 1) && (jsonObject.get("ne") == null)) {

						// throw

					}

					if (!(jsonObject.get("ne") instanceof Number)) {

						// throw

					}
				}
			}

			if (Objects.equals(
					objectField.getBusinessType(),
					ObjectFieldConstants.BUSINESS_TYPE_PICKLIST)) {

				if (!ArrayUtil.contains(
						_PICKLIST_FILTER_TYPES, objectFilter.getFilterType())) {

					// throw not allowed

				}

				if (Objects.equals("includes", objectFilter.getFilterType())) {
					JSONObject jsonObject = _jsonFactory.createJSONObject(
						objectFilter.getJson());

					Set<String> keys = jsonObject.keySet();

					if ((keys.size() != 1) &&
						(jsonObject.get("includes") == null)) {

						// throw

					}

					JSONArray jsonArray = jsonObject.getJSONArray("includes");

					if (jsonArray == null) {

						// throw

					}

					for (int i = 0; i < jsonArray.length(); i++) {
						if (!(jsonArray.get(i) instanceof String)) {

							// throw

						}
					}
				}
			}

			if (ArrayUtil.contains(
					_TEXT_BUSINESS_TYPES, objectField.getBusinessType())) {

				if (!ArrayUtil.contains(
						_TEXT_FILTER_TYPES, objectFilter.getFilterType())) {

					// throw not allowed

				}
			}
		}
	}

	private static final String[] _DATE_FILTER_TYPES = {"range"};

	private static final String[] _NOT_ALLOWED_BUSINESS_TYPES = {
		ObjectFieldConstants.BUSINESS_TYPE_AGGREGATION,
		ObjectFieldConstants.BUSINESS_TYPE_ATTACHMENT,
		ObjectFieldConstants.BUSINESS_TYPE_BOOLEAN,
		ObjectFieldConstants.BUSINESS_TYPE_LARGE_FILE,
		ObjectFieldConstants.BUSINESS_TYPE_RELATIONSHIP,
		ObjectFieldConstants.BUSINESS_TYPE_RICH_TEXT
	};

	private static final String[] _NUMERIC_BUSINESS_TYPES = {
		ObjectFieldConstants.BUSINESS_TYPE_DECIMAL,
		ObjectFieldConstants.BUSINESS_TYPE_INTEGER,
		ObjectFieldConstants.BUSINESS_TYPE_LONG_INTEGER,
		ObjectFieldConstants.BUSINESS_TYPE_PRECISION_DECIMAL
	};

	private static final String[] _NUMERIC_FILTER_TYPES = {"eq", "ne"};

	private static final String[] _PICKLIST_FILTER_TYPES = {
		"excludes", "includes"
	};

	private static final String[] _TEXT_BUSINESS_TYPES = {
		ObjectFieldConstants.BUSINESS_TYPE_LONG_TEXT,
		ObjectFieldConstants.BUSINESS_TYPE_TEXT
	};

	private static final String[] _TEXT_FILTER_TYPES = {""};

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private ObjectFieldSettingLocalService _objectFieldSettingLocalService;

}