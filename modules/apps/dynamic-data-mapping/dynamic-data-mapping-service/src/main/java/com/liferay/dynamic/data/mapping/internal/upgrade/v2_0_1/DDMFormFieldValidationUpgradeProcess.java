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

package com.liferay.dynamic.data.mapping.internal.upgrade.v2_0_1;

import com.liferay.dynamic.data.mapping.internal.upgrade.util.BaseDDMStructureDefinitionUpgradeProcess;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Lino Alves
 */
public class DDMFormFieldValidationUpgradeProcess
	extends BaseDDMStructureDefinitionUpgradeProcess {

	public DDMFormFieldValidationUpgradeProcess(JSONFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	@Override
	protected String upgradeDefinition(String definition)
		throws PortalException {

		JSONObject definitionJSONObject = _jsonFactory.createJSONObject(
			definition);

		JSONArray fieldsJSONArray = definitionJSONObject.getJSONArray("fields");

		for (int i = 0; i < fieldsJSONArray.length(); i++) {
			JSONObject fieldJSONObject = fieldsJSONArray.getJSONObject(i);

			if (!fieldJSONObject.has("validation")) {
				continue;
			}

			JSONObject validationJSONObject = fieldJSONObject.getJSONObject(
				"validation");

			if (validationJSONObject == null) {
				fieldJSONObject.remove("validation");

				continue;
			}

			JSONObject expressionJSONObject =
				validationJSONObject.getJSONObject("expression");

			String expressionValue = expressionJSONObject.getString("value");

			if (Validator.isNull(expressionValue)) {
				fieldJSONObject.remove("validation");

				continue;
			}

			if (Validator.isNotNull(expressionJSONObject.getString("name"))) {
				continue;
			}

			expressionJSONObject.put(
				"name", _getExpressionName(expressionValue));

			String parameterValue = _getParameterValueFromExpression(
				expressionValue);

			_addParameterValue(
				parameterValue, validationJSONObject,
				definitionJSONObject.getString("defaultLanguageId"));

			if (Validator.isNotNull(parameterValue)) {
				expressionJSONObject.put(
					"value",
					StringUtil.replace(
						expressionValue, parameterValue, "{parameter}"));
			}
		}

		return definitionJSONObject.toJSONString();
	}

	private void _addParameterValue(
		String value, JSONObject validationJSONObject,
		String defaultLanguageId) {

		JSONObject parameterJSONObject = validationJSONObject.getJSONObject(
			"parameter");

		if (!parameterJSONObject.has(defaultLanguageId)) {
			parameterJSONObject.put(defaultLanguageId, value);
		}
	}

	private String _getExpressionName(String expressionValue) {
		String name = "";

		if (expressionValue.startsWith("contains")) {
			name = "contains";
		}
		else if (expressionValue.startsWith("NOT(contains")) {
			name = "notContains";
		}
		else if (expressionValue.startsWith("isEmailAddress")) {
			name = "email";
		}
		else if (expressionValue.startsWith("match")) {
			name = "regularExpression";
		}
		else if (expressionValue.startsWith("isURL")) {
			name = "url";
		}

		return name;
	}

	private String _getParameterValueFromExpression(String expressionValue) {
		String[] parts = expressionValue.split("\"");

		if (parts.length > 1) {
			return parts[1];
		}

		return "";
	}

	private final JSONFactory _jsonFactory;

}