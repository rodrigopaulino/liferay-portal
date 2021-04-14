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

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.dynamic.data.mapping.io.DDMFormDeserializer;
import com.liferay.dynamic.data.mapping.io.DDMFormDeserializerDeserializeRequest;
import com.liferay.dynamic.data.mapping.io.DDMFormDeserializerDeserializeResponse;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializer;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializerDeserializeRequest;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializerDeserializeResponse;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesSerializer;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesSerializerSerializeRequest;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesSerializerSerializeResponse;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.util.DDMFormFieldValueTransformer;
import com.liferay.dynamic.data.mapping.util.DDMFormValuesTransformer;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Validator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO
 * "ddm-documentlibrary" types can have titles if they were created after 6.2.x
 * therefore it's necessary to check only those without title already.
 * "document_library" types are new types after the migration and are already fixed.
 */

/**
 * @author István András Dézsi
 */
public class UpgradeDDMFormDocumentLibraryFields extends UpgradeProcess {

	public UpgradeDDMFormDocumentLibraryFields(
		DDMFormDeserializer ddmFormDeserializer,
		DDMFormValuesDeserializer ddmFormValuesDeserializer,
		DDMFormValuesSerializer ddmFormValuesSerializer,
		DLFileEntryLocalService dlFileEntryLocalService,
		JSONFactory jsonFactory) {

		_ddmFormDeserializer = ddmFormDeserializer;
		_ddmFormValuesDeserializer = ddmFormValuesDeserializer;
		_ddmFormValuesSerializer = ddmFormValuesSerializer;
		_dlFileEntryLocalService = dlFileEntryLocalService;
		_jsonFactory = jsonFactory;
	}

	protected DDMForm deserialize(String content) {
		DDMFormDeserializerDeserializeRequest.Builder builder =
			DDMFormDeserializerDeserializeRequest.Builder.newBuilder(content);

		DDMFormDeserializerDeserializeResponse
			ddmFormDeserializerDeserializeResponse =
				_ddmFormDeserializer.deserialize(builder.build());

		return ddmFormDeserializerDeserializeResponse.getDDMForm();
	}

	protected DDMFormValues deserialize(String content, DDMForm ddmForm) {
		DDMFormValuesDeserializerDeserializeRequest.Builder builder =
			DDMFormValuesDeserializerDeserializeRequest.Builder.newBuilder(
				content, ddmForm);

		DDMFormValuesDeserializerDeserializeResponse
			ddmFormValuesDeserializerDeserializeResponse =
				_ddmFormValuesDeserializer.deserialize(builder.build());

		return ddmFormValuesDeserializerDeserializeResponse.getDDMFormValues();
	}

	@Override
	protected void doUpgrade() throws Exception {
		StringBundler sb = new StringBundler(9);

		sb.append("select DDMContent.contentId, DDMContent.data_, ");
		sb.append("DDMStructureVersion.structureVersionId, ");
		sb.append("DDMStructureVersion.definition from DDMContent inner join ");
		sb.append("DDMStorageLink on DDMContent.contentId = ");
		sb.append("DDMStorageLink.classPK inner join DDMStructureVersion on ");
		sb.append("DDMStorageLink.structureVersionId = ");
		sb.append("DDMStructureVersion.structureVersionId where ");
		sb.append("DDMStructureVersion.definition like ");
		sb.append("'%ddm-documentlibrary%'");

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				sb.toString());
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update DDMContent set data_ = ? where contentId = ?")) {

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					DDMForm ddmForm = deserialize(
						resultSet.getString("definition"));

					List<DDMFormField> ddmFormFields =
						ddmForm.getDDMFormFields();

					Stream<DDMFormField> stream = ddmFormFields.stream();

					List<DDMFormField> documentLibraryDDMFormFields =
						stream.filter(
							ddmFormField -> ddmFormField.getType(
							).equals(
								"ddm-documentlibrary"
							)
						).collect(
							Collectors.toList()
						);

					if (documentLibraryDDMFormFields.isEmpty()) {
						continue;
					}

					String data = resultSet.getString("data_");

					DDMFormValues ddmFormValues = deserialize(data, ddmForm);

					transformDocumentLibraryDDMFormFieldValues(ddmFormValues);

					preparedStatement2.setString(1, serialize(ddmFormValues));

					preparedStatement2.setLong(
						2, resultSet.getLong("contentId"));

					preparedStatement2.addBatch();
				}

				preparedStatement2.executeBatch();
			}
		}
	}

	protected String serialize(DDMFormValues ddmFormValues) {
		DDMFormValuesSerializerSerializeRequest.Builder builder =
			DDMFormValuesSerializerSerializeRequest.Builder.newBuilder(
				ddmFormValues);

		DDMFormValuesSerializerSerializeResponse
			ddmFormValuesSerializerSerializeResponse =
				_ddmFormValuesSerializer.serialize(builder.build());

		return ddmFormValuesSerializerSerializeResponse.getContent();
	}

	protected void transformDocumentLibraryDDMFormFieldValues(
			DDMFormValues ddmFormValues)
		throws Exception {

		DDMFormValuesTransformer ddmFormValuesTransformer =
			new DDMFormValuesTransformer(ddmFormValues);

		ddmFormValuesTransformer.addTransformer(
			new DocumentLibraryDDMFormFieldValueTransformer());

		ddmFormValuesTransformer.transform();
	}

	private final DDMFormDeserializer _ddmFormDeserializer;
	private final DDMFormValuesDeserializer _ddmFormValuesDeserializer;
	private final DDMFormValuesSerializer _ddmFormValuesSerializer;
	private final DLFileEntryLocalService _dlFileEntryLocalService;
	private final JSONFactory _jsonFactory;

	private class DocumentLibraryDDMFormFieldValueTransformer
		implements DDMFormFieldValueTransformer {

		@Override
		public String getFieldType() {
			return "ddm-documentlibrary";
		}

		@Override
		public void transform(DDMFormFieldValue ddmFormFieldValue)
			throws PortalException {

			Value value = ddmFormFieldValue.getValue();

			for (Locale locale : value.getAvailableLocales()) {
				String valueString = value.getString(locale);

				if (Validator.isNull(valueString)) {
					continue;
				}

				JSONObject jsonObject = _jsonFactory.createJSONObject(
					valueString);

				long groupId = jsonObject.getLong("groupId");
				String uuid = jsonObject.getString("uuid");

				DLFileEntry dlFileEntry =
					_dlFileEntryLocalService.fetchDLFileEntryByUuidAndGroupId(
						uuid, groupId);

				if (dlFileEntry == null) {
					continue;
				}

				jsonObject.put("title", dlFileEntry.getTitle());

				value.addString(locale, jsonObject.toString());
			}
		}

	}

}