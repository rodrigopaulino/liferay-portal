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

package com.liferay.dynamic.data.mapping.data.provider.instance.internal;

import com.liferay.dynamic.data.mapping.data.provider.DDMDataProvider;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderInputParametersSettings;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderOutputParametersSettings;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderParameterSettings;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderRequest;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderResponse;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderTracker;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializer;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializerDeserializeRequest;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializerDeserializeResponse;
import com.liferay.dynamic.data.mapping.model.DDMDataProviderInstance;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.service.DDMDataProviderInstanceService;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.util.DDMFormFactory;
import com.liferay.dynamic.data.mapping.util.DDMFormInstanceFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ClassUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.KeyValuePair;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Rafael Praxedes
 */
@Component(
	immediate = true,
	property = "ddm.data.provider.instance.id=getDataProviderInstanceInputParameters",
	service = DDMDataProvider.class
)
public class DDMDataProviderInstanceInputParametersDataProvider
	implements DDMDataProvider {

	@Override
	public DDMDataProviderResponse getData(
		DDMDataProviderRequest ddmDataProviderRequest) {

		Optional<Long> dataProviderInstanceIdOptional =
			ddmDataProviderRequest.getParameterOptional(
				"dataProviderInstanceId", String.class);

		long dataProviderInstanceId = 0;

		if (dataProviderInstanceIdOptional.isPresent()) {
			dataProviderInstanceId = GetterUtil.getLong(
				dataProviderInstanceIdOptional.get());
		}

		DDMDataProviderResponse.Builder builder =
			DDMDataProviderResponse.Builder.newBuilder();

		List<KeyValuePair> keyValuePairs = new ArrayList<>();

		if (dataProviderInstanceId == 0) {
			return builder.withOutput(
				"inputParameterNames", keyValuePairs
			).build();
		}

		try {
			DDMDataProviderInputParametersSettings[]
				ddmDataProviderOutputParametersSettings =
					getDDMDataProviderOutputParametersSettings(
						dataProviderInstanceId);

			for (DDMDataProviderInputParametersSettings
					ddmDataProviderOutputParametersSetting :
						ddmDataProviderOutputParametersSettings) {

				keyValuePairs.add(
					new KeyValuePair(
						ddmDataProviderOutputParametersSetting.
							inputParameterName(),
						ddmDataProviderOutputParametersSetting.
							inputParameterLabel()));
			}
		}
		catch (Exception exception) {
			_log.error(
				String.format(
					"Unable to get the output parameters for data provider " +
						"instance with id '%d'",
					dataProviderInstanceId),
				exception);
		}

		return builder.withOutput(
			"inputParameterNames", keyValuePairs
		).build();
	}

	@Override
	public Class<?> getSettings() {
		throw new UnsupportedOperationException();
	}

	protected DDMFormValues deserialize(String content, DDMForm ddmForm) {
		DDMFormValuesDeserializerDeserializeRequest.Builder builder =
			DDMFormValuesDeserializerDeserializeRequest.Builder.newBuilder(
				content, ddmForm);

		DDMFormValuesDeserializerDeserializeResponse
			ddmFormValuesDeserializerDeserializeResponse =
				jsonDDMFormValuesDeserializer.deserialize(builder.build());

		return ddmFormValuesDeserializerDeserializeResponse.getDDMFormValues();
	}

	protected DDMFormValues getDataProviderInstanceFormValues(
		DDMDataProvider ddmDataProvider,
		DDMDataProviderInstance ddmDataProviderInstance) {

		DDMForm ddmForm = DDMFormFactory.create(ddmDataProvider.getSettings());

		return deserialize(ddmDataProviderInstance.getDefinition(), ddmForm);
	}

	protected DDMDataProviderInputParametersSettings[]
			getDDMDataProviderOutputParametersSettings(
				long dataProviderInstanceId)
		throws Exception {

		DDMDataProviderInstance ddmDataProviderInstance =
			ddmDataProviderInstanceService.getDataProviderInstance(
				dataProviderInstanceId);

		DDMDataProvider ddmDataProvider =
			ddmDataProviderTracker.getDDMDataProvider(
				ddmDataProviderInstance.getType());

		if (!ClassUtil.isSubclass(
				ddmDataProvider.getSettings(),
				DDMDataProviderParameterSettings.class)) {

			return new DDMDataProviderInputParametersSettings[0];
		}

		DDMFormValues dataProviderFormValues =
			getDataProviderInstanceFormValues(
				ddmDataProvider, ddmDataProviderInstance);

		DDMDataProviderParameterSettings ddmDataProviderParameterSetting =
			DDMFormInstanceFactory.create(
				DDMDataProviderParameterSettings.class, dataProviderFormValues);

		return ddmDataProviderParameterSetting.inputParameters();
	}

	@Reference
	protected DDMDataProviderInstanceService ddmDataProviderInstanceService;

	@Reference
	protected DDMDataProviderTracker ddmDataProviderTracker;

	@Reference(target = "(ddm.form.values.deserializer.type=json)")
	protected DDMFormValuesDeserializer jsonDDMFormValuesDeserializer;

	private static final Log _log = LogFactoryUtil.getLog(
		DDMDataProviderInstanceInputParametersDataProvider.class);

}