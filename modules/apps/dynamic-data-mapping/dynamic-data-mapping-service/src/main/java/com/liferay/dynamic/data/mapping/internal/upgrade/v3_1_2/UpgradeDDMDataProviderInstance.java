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
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Rodrigo Paulino
 */
public class UpgradeDDMDataProviderInstance extends UpgradeProcess {

	public UpgradeDDMDataProviderInstance(JSONFactory jsonFactory) {
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
			}

			ps2.executeBatch();
		}
	}

	protected String updateOutputParametersInstanceId(String definition)
		throws JSONException {

		JSONObject jsonObject1 = _jsonFactory.createJSONObject(definition);

		JSONArray jsonArray1 = jsonObject1.getJSONArray("fieldValues");

		for (int i = 0; i < jsonArray1.length(); i++) {
			JSONObject jsonObject2 = jsonArray1.getJSONObject(i);

			String name = jsonObject2.getString("name");

			if (name.equals("outputParameters")) {
				jsonObject2.put("instanceId", StringUtil.randomString());

				JSONArray jsonArray2 = jsonObject2.getJSONArray(
					"nestedFieldValues");

				for (int j = 0; j < jsonArray2.length(); j++) {
					JSONObject jsonObject3 = jsonArray2.getJSONObject(j);

					jsonObject3.put("instanceId", StringUtil.randomString());
				}
			}
		}

		return jsonObject1.toString();
	}

	private final JSONFactory _jsonFactory;

}