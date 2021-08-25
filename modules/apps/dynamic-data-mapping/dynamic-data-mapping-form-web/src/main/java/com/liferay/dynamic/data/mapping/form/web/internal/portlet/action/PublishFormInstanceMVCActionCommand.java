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

package com.liferay.dynamic.data.mapping.form.web.internal.portlet.action;

import com.liferay.dynamic.data.mapping.constants.DDMActionKeys;
import com.liferay.dynamic.data.mapping.constants.DDMPortletKeys;
import com.liferay.dynamic.data.mapping.form.values.query.DDMFormValuesQuery;
import com.liferay.dynamic.data.mapping.form.values.query.DDMFormValuesQueryFactory;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bruno Basto
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + DDMPortletKeys.DYNAMIC_DATA_MAPPING_FORM_ADMIN,
		"mvc.command.name=/dynamic_data_mapping_form/publish_form_instance"
	},
	service = MVCActionCommand.class
)
public class PublishFormInstanceMVCActionCommand
	extends SaveFormInstanceMVCActionCommand {

	@Override
	protected void doService(
			ActionRequest actionRequest, ActionResponse actionResponse,
			LiferayPortletURL portletURL)
		throws Exception {

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			DDMFormInstance.class.getName(), actionRequest);

		serviceContext.setAttribute(
			"status", WorkflowConstants.STATUS_APPROVED);

		DDMFormValues settingsDDMFormValues =
			saveFormInstanceMVCCommandHelper.getSettingsDDMFormValues(
				actionRequest);

		boolean published = _togglePublishedSetting(settingsDDMFormValues);

		DDMFormInstance ddmFormInstance =
			saveFormInstanceMVCCommandHelper.saveFormInstance(
				actionRequest, actionResponse, settingsDDMFormValues, true);

		_updateFormInstancePermission(
			actionRequest, ddmFormInstance.getFormInstanceId(), published);

		portletURL.setParameter(
			"formInstanceId",
			String.valueOf(ddmFormInstance.getFormInstanceId()));

		portletURL.setParameter("showPublishAlert", Boolean.TRUE.toString());
	}

	private boolean _togglePublishedSetting(DDMFormValues ddmFormValues)
		throws Exception {

		DDMFormValuesQuery ddmFormValuesQuery =
			_ddmFormValuesQueryFactory.create(ddmFormValues, "/published");

		DDMFormFieldValue ddmFormFieldValue =
			ddmFormValuesQuery.selectSingleDDMFormFieldValue();

		Value value = ddmFormFieldValue.getValue();

		boolean published = !GetterUtil.getBoolean(
			value.getString(value.getDefaultLocale()));

		value.addString(
			ddmFormValues.getDefaultLocale(), Boolean.toString(published));

		return published;
	}

	private void _updateFormInstancePermission(
			ActionRequest actionRequest, long formInstanceId, boolean published)
		throws Exception {

		Role role = _roleLocalService.getRole(
			_portal.getCompanyId(actionRequest), RoleConstants.GUEST);

		ResourcePermission resourcePermission =
			_resourcePermissionLocalService.fetchResourcePermission(
				role.getCompanyId(), DDMFormInstance.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(formInstanceId), role.getRoleId());

		if (resourcePermission == null) {
			_resourcePermissionLocalService.setResourcePermissions(
				role.getCompanyId(), DDMFormInstance.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(formInstanceId), role.getRoleId(),
				new String[] {DDMActionKeys.ADD_FORM_INSTANCE_RECORD});

			resourcePermission =
				_resourcePermissionLocalService.fetchResourcePermission(
					role.getCompanyId(), DDMFormInstance.class.getName(),
					ResourceConstants.SCOPE_INDIVIDUAL,
					String.valueOf(formInstanceId), role.getRoleId());
		}

		if (published) {
			resourcePermission.addResourceAction(
				DDMActionKeys.ADD_FORM_INSTANCE_RECORD);
		}
		else {
			resourcePermission.removeResourceAction(
				DDMActionKeys.ADD_FORM_INSTANCE_RECORD);
		}

		_resourcePermissionLocalService.updateResourcePermission(
			resourcePermission);
	}

	@Reference
	private DDMFormValuesQueryFactory _ddmFormValuesQueryFactory;

	@Reference
	private Portal _portal;

	@Reference
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Reference
	private RoleLocalService _roleLocalService;

}