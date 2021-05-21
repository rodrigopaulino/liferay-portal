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

package com.liferay.dynamic.data.mapping.internal.upgrade.v4_3_4;

import com.liferay.dynamic.data.mapping.internal.upgrade.util.BaseDDMStructureDefinitionUpgradeProcess;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rodrigo Paulino
 */
public class DDMFormFieldValidationUpgradeProcess
	extends BaseDDMStructureDefinitionUpgradeProcess {

	public DDMFormFieldValidationUpgradeProcess(JSONFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	@Override
	protected String upgradeDefinition(String definition)
		throws PortalException {

		JSONObject jsonObject = _jsonFactory.createJSONObject(definition);

		_upgradeFields(jsonObject.getJSONArray("fields"));

		return jsonObject.toJSONString();
	}

	private String _extractFieldName(String regex, String value) {
		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(value);

		matcher.find();

		return matcher.group(1);
	}

	private void _upgradeExpression(
		JSONObject expressionJSONObject, String name, String regex,
		String valueTemplate) {

		expressionJSONObject.put("name", name);

		String value = expressionJSONObject.getString("value");

		if (value.matches(regex)) {
			expressionJSONObject.put(
				"value",
				StringUtil.replace(
					valueTemplate, "fieldName",
					_extractFieldName(regex, value)));
		}
	}

	private void _upgradeFields(JSONArray fieldsJSONArray) {
		for (int i = 0; i < fieldsJSONArray.length(); i++) {
			JSONObject jsonObject = fieldsJSONArray.getJSONObject(i);

			JSONObject validationJSONObject = jsonObject.getJSONObject(
				"validation");

			if (validationJSONObject != null) {
				_upgradeValidation(
					validationJSONObject.getJSONObject("expression"));
			}

			JSONArray nestedFieldsJSONArray = jsonObject.getJSONArray(
				"nestedFields");

			if (nestedFieldsJSONArray != null) {
				_upgradeFields(nestedFieldsJSONArray);
			}
		}
	}

	private void _upgradeValidation(JSONObject expressionJSONObject) {
		String name = expressionJSONObject.getString("name");

		if (StringUtil.equals(name, "contains")) {
			_upgradeExpression(
				expressionJSONObject, "doesNotContain",
				"contains\\((.+), \"\\{parameter}\"\\)",
				"NOT(contains(fieldName, \"{parameter}\"))");
		}
		else if (StringUtil.equals(name, "email")) {
			_upgradeExpression(
				expressionJSONObject, "isNotEmail", "isEmailAddress\\((.+)\\)",
				"NOT(isEmailAddress(fieldName))");
		}
		else if (StringUtil.equals(name, "eq")) {
			_upgradeExpression(
				expressionJSONObject, "neq", "(.+)==\\{parameter}",
				"fieldName != {parameter}");
		}
		else if (StringUtil.equals(name, "gt")) {
			_upgradeExpression(
				expressionJSONObject, "lteq", "(.+)>\\{parameter}",
				"fieldName <= {parameter}");
		}
		else if (StringUtil.equals(name, "gteq")) {
			_upgradeExpression(
				expressionJSONObject, "lt", "(.+)>=\\{parameter}",
				"fieldName < {parameter}");
		}
		else if (StringUtil.equals(name, "lt")) {
			_upgradeExpression(
				expressionJSONObject, "gteq", "(.+)<\\{parameter}",
				"fieldName >= {parameter}");
		}
		else if (StringUtil.equals(name, "lteq")) {
			_upgradeExpression(
				expressionJSONObject, "gt", "(.+)<=\\{parameter}",
				"fieldName > {parameter}");
		}
		else if (StringUtil.equals(name, "neq")) {
			_upgradeExpression(
				expressionJSONObject, "eq", "(.+)!=\\{parameter}",
				"fieldName == {parameter}");
		}
		else if (StringUtil.equals(name, "notContains")) {
			_upgradeExpression(
				expressionJSONObject, "contains",
				"NOT\\(contains\\((.+), \"\\{parameter}\"\\)\\)",
				"contains(fieldName, \"{parameter}\")");
		}
		else if (StringUtil.equals(name, "regularExpression")) {
			_upgradeExpression(
				expressionJSONObject, "doesNotMatch",
				"match\\((.+), \"\\{parameter}\"\\)",
				"NOT(match(fieldName, \"{parameter}\"))");
		}
		else if (StringUtil.equals(name, "url")) {
			_upgradeExpression(
				expressionJSONObject, "isNotURL", "isURL\\((.+)\\)",
				"NOT(isURL(fieldName))");
		}
	}

	private final JSONFactory _jsonFactory;

}