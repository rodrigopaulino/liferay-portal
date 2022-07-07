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

package com.liferay.object.service.impl;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectFilterConstants;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFieldSetting;
import com.liferay.object.model.ObjectFilter;
import com.liferay.object.model.impl.ObjectFieldSettingImpl;
import com.liferay.object.service.ObjectFilterLocalService;
import com.liferay.object.service.base.ObjectFieldSettingLocalServiceBaseImpl;
import com.liferay.object.service.persistence.ObjectFieldPersistence;
import com.liferay.object.service.persistence.ObjectFilterPersistence;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carolina Barbosa
 */
@Component(
	property = "model.class.name=com.liferay.object.model.ObjectFieldSetting",
	service = AopService.class
)
public class ObjectFieldSettingLocalServiceImpl
	extends ObjectFieldSettingLocalServiceBaseImpl {

	@Override
	public void addObjectFieldSetting(
			long userId, long objectFieldId, String name,
			List<ObjectFilter> objectFilters, String value)
		throws PortalException {

		ObjectField objectField = _objectFieldPersistence.findByPrimaryKey(
			objectFieldId);

		if (GetterUtil.getBoolean(PropsUtil.get("feature.flag.LPS-156704")) &&
			Objects.equals(
				objectField.getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_AGGREGATION) &&
			Objects.equals(name, "filters")) {

			_addObjectFilter(userId, objectFieldId, objectFilters);
		}
		else {
			_addObjectFieldSetting(userId, objectFieldId, name, value);
		}
	}

	@Override
	public ObjectFieldSetting deleteObjectFieldSetting(
			long objectFieldSettingId)
		throws PortalException {

		ObjectFieldSetting objectFieldSetting =
			objectFieldSettingPersistence.findByPrimaryKey(
				objectFieldSettingId);

		return objectFieldSettingPersistence.remove(objectFieldSetting);
	}

	public void deleteObjectFieldSettingByObjectFieldId(long objectFieldId)
		throws PortalException {

		objectFieldSettingPersistence.removeByObjectFieldId(objectFieldId);

		ObjectField objectField = _objectFieldPersistence.findByPrimaryKey(
			objectFieldId);

		if (Objects.equals(
				objectField.getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_AGGREGATION)) {

			_objectFilterPersistence.removeByObjectFieldId(
				objectField.getObjectFieldId());
		}
	}

	@Override
	public ObjectFieldSetting fetchObjectFieldSetting(
		long objectFieldId, String name) {

		return objectFieldSettingPersistence.fetchByOFI_N(objectFieldId, name);
	}

	@Override
	public List<ObjectFieldSetting> getObjectFieldSettings(long objectFieldId) {
		return objectFieldSettingPersistence.findByObjectFieldId(objectFieldId);
	}

	@Override
	public ObjectFieldSetting updateObjectFieldSetting(
			long objectFieldSettingId, String value)
		throws PortalException {

		ObjectFieldSetting objectFieldSetting =
			objectFieldSettingPersistence.fetchByPrimaryKey(
				objectFieldSettingId);

		objectFieldSetting.setValue(value);

		return objectFieldSettingPersistence.update(objectFieldSetting);
	}

	private void _addObjectFieldSetting(
			long userId, long objectFieldId, String name, String value)
		throws PortalException {

		ObjectFieldSetting objectFieldSetting =
			objectFieldSettingPersistence.create(
				counterLocalService.increment());

		User user = _userLocalService.getUser(userId);

		objectFieldSetting.setCompanyId(user.getCompanyId());
		objectFieldSetting.setUserId(user.getUserId());
		objectFieldSetting.setUserName(user.getFullName());

		objectFieldSetting.setObjectFieldId(objectFieldId);
		objectFieldSetting.setName(name);
		objectFieldSetting.setValue(value);

		objectFieldSettingPersistence.update(objectFieldSetting);
	}

	private void _addObjectFilter(
			long userId, long objectFieldId, List<ObjectFilter> objectFilters)
		throws PortalException {

		_objectFilterPersistence.removeByObjectFieldId(objectFieldId);

		for (ObjectFilter objectFilter : objectFilters) {
			_objectFilterLocalService.addObjectFilter(
				userId, objectFieldId, objectFilter.getFilterBy(),
				objectFilter.getFilterType(), objectFilter.getJson());
		}
	}

	@Reference
	private ObjectFieldPersistence _objectFieldPersistence;

	@Reference
	private ObjectFilterLocalService _objectFilterLocalService;

	@Reference
	private ObjectFilterPersistence _objectFilterPersistence;

	@Reference
	private UserLocalService _userLocalService;

}