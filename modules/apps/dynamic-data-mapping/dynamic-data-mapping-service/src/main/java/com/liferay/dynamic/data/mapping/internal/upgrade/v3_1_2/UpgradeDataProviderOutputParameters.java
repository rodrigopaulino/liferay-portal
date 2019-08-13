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

package com.liferay.dynamic.data.mapping.internal.upgrade.v3_1_2;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rodrigo Paulino
 */
public class UpgradeDataProviderOutputParameters extends UpgradeProcess {

	public UpgradeDataProviderOutputParameters(JSONFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	@Override
	protected void doUpgrade() throws Exception {
		StringBundler sb1 = new StringBundler(1);

		sb1.append("select * from DDMDataProviderInstance");

		StringBundler sb2 = new StringBundler(2);

		sb2.append("update DDMDataProviderInstance set definition = ? where ");
		sb2.append("dataProviderInstanceId = ?");

		try (PreparedStatement ps1 = connection.prepareStatement(
				sb1.toString());
			PreparedStatement ps2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection, sb2.toString());
			ResultSet rs = ps1.executeQuery()) {

			while (rs.next()) {
				String definition = rs.getString("definition");

				definition = updateOutputParametersInstanceId(definition);

				ps2.setString(1, definition);

				long dataProviderInstanceId = rs.getLong(
					"dataProviderInstanceId");

				ps2.setLong(2, dataProviderInstanceId);

				ps2.addBatch();

				updateStructures(dataProviderInstanceId);
			}

			ps2.executeBatch();
		}
	}

	protected String updateOutputParametersInstanceId(String definition)
		throws JSONException {

		JSONObject jsonObject1 = _jsonFactory.createJSONObject(definition);

		JSONArray jsonArray1 = jsonObject1.getJSONArray("fieldValues");

		String instanceId = StringUtil.randomString();

		for (int i = 0; i < jsonArray1.length(); i++) {
			JSONObject jsonObject2 = jsonArray1.getJSONObject(i);

			String name1 = jsonObject2.getString("name");

			if (name1.equals("outputParameters")) {
				String outputParameterId = StringUtil.randomString();

				JSONObject jsonObject3 = JSONUtil.put(
					"instanceId", instanceId
				).put(
					"name", "outputParameterId"
				).put(
					"value", outputParameterId
				);

				JSONArray jsonArray2 = jsonObject2.getJSONArray(
					"nestedFieldValues");

				_updateOutputParameterMap(outputParameterId, jsonArray2);

				jsonArray2.put(jsonObject3);
			}
		}

		return jsonObject1.toString();
	}

	protected void updateStructures(long dataProviderInstanceId)
		throws Exception {

		StringBundler sb5 = new StringBundler(2);

		sb5.append("update DDMStructure set definition = ? where structureId ");
		sb5.append("= ?");

		PreparedStatement ps5 =
			AutoBatchPreparedStatementUtil.concurrentAutoBatch(
				connection, sb5.toString());

		StringBundler sb4 = new StringBundler(2);

		sb4.append("update DDMStructureVersion set definition = ? where ");
		sb4.append("structureVersionId = ?");

		PreparedStatement ps4 =
			AutoBatchPreparedStatementUtil.concurrentAutoBatch(
				connection, sb4.toString());

		StringBundler sb1 = new StringBundler(11);

		sb1.append("select structureVersionId, DDMStructureVersion.");
		sb1.append("structureId, DDMStructureVersion.definition, case when ");
		sb1.append("DDMStructure.structureId is not null then true else ");
		sb1.append("false end as updateStructure from ");
		sb1.append("DDMDataProviderInstanceLink join DDMStructureVersion on ");
		sb1.append("DDMStructureVersion.structureId ");
		sb1.append("DDMDataProviderInstanceLink.structureId left join ");
		sb1.append("DDMStructure on DDMStructure.structureId = ");
		sb1.append("DDMDataProviderInstanceLink.structureId and ");
		sb1.append("DDMStructure.version = DDMStructureVersion.version where ");
		sb1.append("dataProviderInstanceId = ?");

		PreparedStatement ps1 = connection.prepareStatement(sb1.toString());

		ps1.setLong(1, dataProviderInstanceId);

		ResultSet rs1 = ps1.executeQuery();

		while (rs1.next()) {
			long structureVersionId = rs1.getLong("structureVersionId");
			long structureId = rs1.getLong("structureId");
			String definition = rs1.getString("definition");
			boolean updateStructure = rs1.getBoolean("updateStructure");

			JSONObject jsonObject1 = _jsonFactory.createJSONObject(definition);

			JSONArray jsonArray1 = jsonObject1.getJSONArray("fields");

			boolean updateStructureVersion = false;

			for (int i = 0; i < jsonArray1.length(); i++) {
				JSONObject jsonObject2 = jsonArray1.getJSONObject(i);

				String dataSourceType = jsonObject2.getString("dataSourceType");

				if (dataSourceType.equals("data-provider")) {
					String ddmDataProviderInstanceId = jsonObject2.getString(
						"ddmDataProviderInstanceId");

					JSONArray jsonArray2 = _jsonFactory.createJSONArray(
						ddmDataProviderInstanceId);

					if (dataProviderInstanceId == jsonArray2.getLong(0)) {
						String ddmDataProviderInstanceOutput =
							jsonObject2.getString(
								"ddmDataProviderInstanceOutput");

						jsonArray2 = _jsonFactory.createJSONArray(
							ddmDataProviderInstanceOutput);

						ddmDataProviderInstanceOutput = jsonArray2.getString(0);

						String outputParameterId = _outputParametersMap.get(
							ddmDataProviderInstanceOutput);

						jsonObject2.put(
							"ddmDataProviderInstanceOutput",
							"[\"" + outputParameterId + "\"]");

						updateStructureVersion = true;
					}
				}
			}

			if (updateStructureVersion) {
				definition = jsonObject1.toString();

				ps4.setString(1, definition);

				ps4.setLong(2, structureVersionId);

				ps4.addBatch();

				if (updateStructure) {
					ps5.setString(1, definition);
					ps5.setLong(2, structureId);

					ps5.addBatch();
				}
			}
		}

		ps4.executeBatch();
		ps5.executeBatch();
	}

	private void _updateOutputParameterMap(
		String outputParameterId, JSONArray jsonArray) {

		Map<String, JSONObject> outputParameterMap = JSONUtil.toJSONObjectMap(
			jsonArray, "name");

		JSONObject jsonObject4 = outputParameterMap.get("outputParameterName");

		String value = jsonObject4.getString("value");

		_outputParametersMap.put(value, outputParameterId);
	}

	private final JSONFactory _jsonFactory;
	private Map<String, String> _outputParametersMap = new HashMap<>();

}