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

package com.liferay.dynamic.data.mapping.service.impl;

import com.liferay.dynamic.data.mapping.data.provider.DDMDataProvider;
import com.liferay.dynamic.data.mapping.data.provider.DDMDataProviderTracker;
import com.liferay.dynamic.data.mapping.exception.DataProviderInstanceNameException;
import com.liferay.dynamic.data.mapping.exception.NoSuchDataProviderInstanceException;
import com.liferay.dynamic.data.mapping.exception.RequiredDataProviderInstanceException;
import com.liferay.dynamic.data.mapping.form.builder.context.DDMFormContextDeserializer;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializer;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializerDeserializeRequest;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializerDeserializeResponse;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesDeserializerTracker;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesSerializer;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesSerializerSerializeRequest;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesSerializerSerializeResponse;
import com.liferay.dynamic.data.mapping.io.DDMFormValuesSerializerTracker;
import com.liferay.dynamic.data.mapping.model.DDMDataProviderInstance;
import com.liferay.dynamic.data.mapping.model.DDMDataProviderInstanceLink;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.base.DDMDataProviderInstanceLocalServiceBaseImpl;
import com.liferay.dynamic.data.mapping.service.persistence.DDMDataProviderInstanceLinkUtil;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.util.DDMFormFactory;
import com.liferay.dynamic.data.mapping.validator.DDMFormValuesValidator;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.permission.ModelPermissions;
import com.liferay.portal.kernel.util.GroupThreadLocal;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.spring.extender.service.ServiceReference;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Leonardo Barros
 */
public class DDMDataProviderInstanceLocalServiceImpl
	extends DDMDataProviderInstanceLocalServiceBaseImpl {

	@Override
	public DDMDataProviderInstance addDataProviderInstance(
			long userId, long groupId, Map<Locale, String> nameMap,
			Map<Locale, String> descriptionMap, DDMFormValues ddmFormValues,
			String type, ServiceContext serviceContext)
		throws PortalException {

		// Data provider instance

		User user = userLocalService.getUser(userId);

		validate(nameMap, ddmFormValues);

		long dataProviderInstanceId = counterLocalService.increment();

		DDMDataProviderInstance dataProviderInstance =
			ddmDataProviderInstancePersistence.create(dataProviderInstanceId);

		dataProviderInstance.setUuid(serviceContext.getUuid());
		dataProviderInstance.setGroupId(groupId);
		dataProviderInstance.setCompanyId(user.getCompanyId());
		dataProviderInstance.setUserId(user.getUserId());
		dataProviderInstance.setUserName(user.getFullName());
		dataProviderInstance.setNameMap(nameMap);
		dataProviderInstance.setDescriptionMap(descriptionMap);
		dataProviderInstance.setDefinition(serialize(ddmFormValues));
		dataProviderInstance.setType(type);

		ddmDataProviderInstancePersistence.update(dataProviderInstance);

		// Resources

		if (serviceContext.isAddGroupPermissions() ||
			serviceContext.isAddGuestPermissions()) {

			addDataProviderInstanceResources(
				dataProviderInstance, serviceContext.isAddGroupPermissions(),
				serviceContext.isAddGuestPermissions());
		}
		else {
			addDataProviderInstanceResources(
				dataProviderInstance, serviceContext.getModelPermissions());
		}

		return dataProviderInstance;
	}

	@Override
	public void deleteDataProviderInstance(
			DDMDataProviderInstance dataProviderInstance)
		throws PortalException {

		if (!GroupThreadLocal.isDeleteInProcess()) {
			int count =
				ddmDataProviderInstanceLinkPersistence.
					countByDataProviderInstanceId(
						dataProviderInstance.getDataProviderInstanceId());

			if (count > 0) {
				throw new RequiredDataProviderInstanceException.
					MustNotDeleteDataProviderInstanceReferencedByDataProviderInstanceLinks(
						dataProviderInstance.getDataProviderInstanceId());
			}
		}

		// Data provider instance

		ddmDataProviderInstancePersistence.remove(dataProviderInstance);

		// Resources

		resourceLocalService.deleteResource(
			dataProviderInstance.getCompanyId(),
			DDMDataProviderInstance.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			dataProviderInstance.getDataProviderInstanceId());
	}

	@Override
	public void deleteDataProviderInstance(long dataProviderInstanceId)
		throws PortalException {

		DDMDataProviderInstance dataProviderInstance =
			ddmDataProviderInstancePersistence.findByPrimaryKey(
				dataProviderInstanceId);

		ddmDataProviderInstanceLocalService.deleteDataProviderInstance(
			dataProviderInstance);
	}

	@Override
	public void deleteDataProviderInstances(long companyId, final long groupId)
		throws PortalException {

		ActionableDynamicQuery actionableDynamicQuery =
			ddmDataProviderInstanceLocalService.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property groupIdProperty = PropertyFactoryUtil.forName(
					"groupId");

				dynamicQuery.add(groupIdProperty.eq(groupId));
			});
		actionableDynamicQuery.setPerformActionMethod(
			(DDMDataProviderInstance ddmDataProviderInstance) ->
				deleteDataProviderInstance(ddmDataProviderInstance));

		actionableDynamicQuery.setCompanyId(companyId);

		actionableDynamicQuery.performActions();
	}

	@Override
	public DDMDataProviderInstance fetchDataProviderInstance(
		long dataProviderInstanceId) {

		return ddmDataProviderInstancePersistence.fetchByPrimaryKey(
			dataProviderInstanceId);
	}

	@Override
	public DDMDataProviderInstance fetchDataProviderInstanceByUuid(
		String uuid) {

		List<DDMDataProviderInstance> ddmDataProviderInstances =
			ddmDataProviderInstancePersistence.findByUuid(uuid);

		if (ddmDataProviderInstances.isEmpty()) {
			return null;
		}

		return ddmDataProviderInstances.get(0);
	}

	@Override
	public DDMDataProviderInstance getDataProviderInstance(
			long dataProviderInstanceId)
		throws PortalException {

		return ddmDataProviderInstancePersistence.findByPrimaryKey(
			dataProviderInstanceId);
	}

	@Override
	public DDMDataProviderInstance getDataProviderInstanceByUuid(String uuid)
		throws PortalException {

		List<DDMDataProviderInstance> ddmDataProviderInstances =
			ddmDataProviderInstancePersistence.findByUuid(uuid);

		if (ddmDataProviderInstances.isEmpty()) {
			throw new NoSuchDataProviderInstanceException(
				"No DataProviderInstance found with uuid: " + uuid);
		}

		return ddmDataProviderInstances.get(0);
	}

	@Override
	public List<DDMDataProviderInstance> getDataProviderInstances(
		long[] groupIds) {

		return ddmDataProviderInstancePersistence.findByGroupId(groupIds);
	}

	@Override
	public List<DDMDataProviderInstance> getDataProviderInstances(
		long[] groupIds, int start, int end) {

		return ddmDataProviderInstancePersistence.findByGroupId(
			groupIds, start, end);
	}

	@Override
	public List<DDMDataProviderInstance> getDataProviderInstances(
		long[] groupIds, int start, int end,
		OrderByComparator<DDMDataProviderInstance> orderByComparator) {

		return ddmDataProviderInstancePersistence.findByGroupId(
			groupIds, start, end, orderByComparator);
	}

	@Override
	public List<DDMDataProviderInstance> search(
		long companyId, long[] groupIds, String keywords, int start, int end,
		OrderByComparator<DDMDataProviderInstance> orderByComparator) {

		return ddmDataProviderInstanceFinder.findByKeywords(
			companyId, groupIds, keywords, start, end, orderByComparator);
	}

	@Override
	public List<DDMDataProviderInstance> search(
		long companyId, long[] groupIds, String name, String description,
		boolean andOperator, int start, int end,
		OrderByComparator<DDMDataProviderInstance> orderByComparator) {

		return ddmDataProviderInstanceFinder.findByC_G_N_D(
			companyId, groupIds, name, description, andOperator, start, end,
			orderByComparator);
	}

	@Override
	public int searchCount(long companyId, long[] groupIds, String keywords) {
		return ddmDataProviderInstanceFinder.countByKeywords(
			companyId, groupIds, keywords);
	}

	@Override
	public int searchCount(
		long companyId, long[] groupIds, String name, String description,
		boolean andOperator) {

		return ddmDataProviderInstanceFinder.countByC_G_N_D(
			companyId, groupIds, name, description, andOperator);
	}

	@Override
	public DDMDataProviderInstance updateDataProviderInstance(
			long userId, long dataProviderInstanceId,
			Map<Locale, String> nameMap, Map<Locale, String> descriptionMap,
			DDMFormValues ddmFormValues, ServiceContext serviceContext)
		throws PortalException {

		User user = userLocalService.getUser(userId);

		validate(nameMap, ddmFormValues);

		DDMDataProviderInstance dataProviderInstance =
			ddmDataProviderInstancePersistence.findByPrimaryKey(
				dataProviderInstanceId);

		DDMDataProvider ddmDataProvider =
			ddmDataProviderTracker.getDDMDataProvider(
				dataProviderInstance.getType());

		Class<?> clazz = ddmDataProvider.getSettings();

		DDMForm ddmFormDataProvider = DDMFormFactory.create(clazz);

		DDMFormValues originalDDMFormValues = deserialize(
			dataProviderInstance.getDefinition(), ddmFormDataProvider);

		Map<String, List<DDMFormFieldValue>> originalDDMFormFieldValuesMap =
			originalDDMFormValues.getDDMFormFieldValuesMap();

		List<DDMFormFieldValue> originalDDMFormFieldValues =
			originalDDMFormFieldValuesMap.get("outputParameters");

		Stream<DDMFormFieldValue> originalDDMFormFieldValuesStream =
			originalDDMFormFieldValues.stream();

		HashMap<String, String> changedOutputParameters =
			originalDDMFormFieldValuesStream.collect(
				HashMap::new,
				(map, originalDDMFormFieldValue) -> {
					String originalLabel = getOutputParameterLabel(
						originalDDMFormFieldValue);

					Map<String, List<DDMFormFieldValue>> ddmFormFieldValuesMap =
						ddmFormValues.getDDMFormFieldValuesMap();

					List<DDMFormFieldValue> ddmFormFieldValues =
						ddmFormFieldValuesMap.get("outputParameters");

					Stream<DDMFormFieldValue> ddmFormFieldValuesStream =
						ddmFormFieldValues.stream();

					ddmFormFieldValuesStream.forEach(
						newDDMFormFieldValue -> {
							if (newDDMFormFieldValue.getInstanceId().equals(
									originalDDMFormFieldValue.
										getInstanceId())) {

								String newLabel = getOutputParameterLabel(
									newDDMFormFieldValue);

								if (!newLabel.equals(originalLabel)) {
									map.put(originalLabel, newLabel);
								}
							}
						});
				},
				HashMap::putAll);

		dataProviderInstance.setUserId(user.getUserId());
		dataProviderInstance.setUserName(user.getFullName());
		dataProviderInstance.setNameMap(nameMap);
		dataProviderInstance.setDescriptionMap(descriptionMap);
		dataProviderInstance.setDefinition(serialize(ddmFormValues));

		ddmDataProviderInstancePersistence.update(dataProviderInstance);

		List<DDMDataProviderInstanceLink> dataProviderInstanceLinks =
			DDMDataProviderInstanceLinkUtil.findByDataProviderInstanceId(
				dataProviderInstanceId);

		for (DDMDataProviderInstanceLink ddmDataProviderInstanceLink :
				dataProviderInstanceLinks) {

			DynamicQuery dynamicQuery =
				DDMFormInstanceLocalServiceUtil.dynamicQuery();

			dynamicQuery.add(
				RestrictionsFactoryUtil.eq(
					"structureId",
					ddmDataProviderInstanceLink.getStructureId()));

			List<DDMFormInstance> ddmFormInstances =
				DDMFormInstanceLocalServiceUtil.dynamicQuery(dynamicQuery);

			for (DDMFormInstance ddmFormInstance : ddmFormInstances) {
				DDMForm ddmForm = ddmFormInstance.getDDMForm();

				List<DDMFormField> listDDMFormField =
					ddmForm.getDDMFormFields();

				listDDMFormField.forEach(
					ddmFormField -> {
						String type = ddmFormField.getType();

						Map<String, Object> properties =
							ddmFormField.getProperties();

						String dataSourceType = (String)properties.get(
							"dataSourceType");

						if (type.equals("select") &&
							dataSourceType.equals("data-provider")) {

							String ddmDataProviderInstanceId =
								(String)properties.get(
									"ddmDataProviderInstanceId");

							ddmDataProviderInstanceId =
								ddmDataProviderInstanceId.substring(
									2, ddmDataProviderInstanceId.length() - 2);

							if (Long.valueOf(ddmDataProviderInstanceId) ==
									dataProviderInstanceId) {

								String ddmDataProviderInstanceOutput =
									(String)properties.get(
										"ddmDataProviderInstanceOutput");

								ddmDataProviderInstanceOutput =
									ddmDataProviderInstanceOutput.substring(
										2,
										ddmDataProviderInstanceOutput.length() -
											2);

								if (changedOutputParameters.containsKey(
										ddmDataProviderInstanceOutput)) {

									String newDDMDataProviderInstanceOutput =
										changedOutputParameters.get(
											ddmDataProviderInstanceOutput);

									properties.put(
										"ddmDataProviderInstanceOutput",
										"[\"" +
											newDDMDataProviderInstanceOutput +
												"\"]");
								}
							}
						}
					});

				DDMStructure ddmStructure = ddmFormInstance.getStructure();

				DDMFormInstanceLocalServiceUtil.updateFormInstance(
					userId, ddmFormInstance.getFormInstanceId(),
					ddmFormInstance.getNameMap(),
					ddmFormInstance.getDescriptionMap(), ddmForm,
					ddmStructure.getDDMFormLayout(),
					ddmFormInstance.getSettingsDDMFormValues(), serviceContext);
			}
		}

		return dataProviderInstance;
	}

	protected void addDataProviderInstanceResources(
			DDMDataProviderInstance dataProviderInstance,
			boolean addGroupPermissions, boolean addGuestPermissions)
		throws PortalException {

		resourceLocalService.addResources(
			dataProviderInstance.getCompanyId(),
			dataProviderInstance.getGroupId(), dataProviderInstance.getUserId(),
			DDMDataProviderInstance.class.getName(),
			dataProviderInstance.getDataProviderInstanceId(), false,
			addGroupPermissions, addGuestPermissions);
	}

	protected void addDataProviderInstanceResources(
			DDMDataProviderInstance dataProviderInstance,
			ModelPermissions modelPermissions)
		throws PortalException {

		resourceLocalService.addModelResources(
			dataProviderInstance.getCompanyId(),
			dataProviderInstance.getGroupId(), dataProviderInstance.getUserId(),
			DDMDataProviderInstance.class.getName(),
			dataProviderInstance.getDataProviderInstanceId(), modelPermissions);
	}

	protected DDMFormValues deserialize(String content, DDMForm ddmForm) {
		DDMFormValuesDeserializer ddmFormValuesDeserializer =
			ddmFormValuesDeserializerTracker.getDDMFormValuesDeserializer(
				"json");

		DDMFormValuesDeserializerDeserializeRequest.Builder builder =
			DDMFormValuesDeserializerDeserializeRequest.Builder.newBuilder(
				content, ddmForm);

		DDMFormValuesDeserializerDeserializeResponse
			ddmFormValuesDeserializerDeserializeResponse =
				ddmFormValuesDeserializer.deserialize(builder.build());

		return ddmFormValuesDeserializerDeserializeResponse.getDDMFormValues();
	}

	protected String getOutputParameterLabel(
		DDMFormFieldValue ddmFormFieldValue) {

		List<DDMFormFieldValue> list =
			ddmFormFieldValue.getNestedDDMFormFieldValues();

		DDMFormFieldValue outputParameterName = list.get(0);

		Value value = outputParameterName.getValue();

		Locale locale = value.getDefaultLocale();

		return value.getString(locale);
	}

	protected String serialize(DDMFormValues ddmFormValues) {
		DDMFormValuesSerializer ddmFormValuesSerializer =
			ddmFormValuesSerializerTracker.getDDMFormValuesSerializer("json");

		DDMFormValuesSerializerSerializeRequest.Builder builder =
			DDMFormValuesSerializerSerializeRequest.Builder.newBuilder(
				ddmFormValues);

		DDMFormValuesSerializerSerializeResponse
			ddmFormValuesSerializerSerializeResponse =
				ddmFormValuesSerializer.serialize(builder.build());

		return ddmFormValuesSerializerSerializeResponse.getContent();
	}

	protected void validate(
			Map<Locale, String> nameMap, DDMFormValues ddmFormValues)
		throws PortalException {

		Locale locale = LocaleUtil.getSiteDefault();

		String name = nameMap.get(locale);

		if (Validator.isNull(name)) {
			throw new DataProviderInstanceNameException(
				"Name is null for locale " + locale.getDisplayName());
		}

		ddmFormValuesValidator.validate(ddmFormValues);
	}

	@ServiceReference(type = DDMDataProviderTracker.class)
	protected DDMDataProviderTracker ddmDataProviderTracker;

	@ServiceReference(
		filterString = "(dynamic.data.mapping.form.builder.context.deserializer.type=form)",
		type = DDMFormContextDeserializer.class
	)
	protected DDMFormContextDeserializer<DDMForm> ddmFormContextDeserializer;

	@ServiceReference(type = DDMFormValuesDeserializerTracker.class)
	protected DDMFormValuesDeserializerTracker ddmFormValuesDeserializerTracker;

	@ServiceReference(type = DDMFormValuesSerializerTracker.class)
	protected DDMFormValuesSerializerTracker ddmFormValuesSerializerTracker;

	@ServiceReference(type = DDMFormValuesValidator.class)
	protected DDMFormValuesValidator ddmFormValuesValidator;

	@ServiceReference(type = JSONFactory.class)
	protected JSONFactory jsonFactory;

}