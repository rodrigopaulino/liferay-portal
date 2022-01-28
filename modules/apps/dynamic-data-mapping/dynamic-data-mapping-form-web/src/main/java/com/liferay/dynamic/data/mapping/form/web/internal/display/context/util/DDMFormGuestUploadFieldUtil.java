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

package com.liferay.dynamic.data.mapping.form.web.internal.display.context.util;

import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carolina Barbosa
 */
@Component(immediate = true, service = {})
public class DDMFormGuestUploadFieldUtil {

	public static boolean guestUploadLimitReached(
		DDMFormInstance ddmFormInstance, HttpServletRequest httpServletRequest,
		int guestUploadMaximumSubmissions) {

		List<DDMFormInstanceRecord> ddmFormInstanceRecords =
			_ddmFormInstanceRecordLocalService.getFormInstanceRecords(
				ddmFormInstance.getFormInstanceId(),
				WorkflowConstants.STATUS_ANY, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null);

		for (int i = 0; i < ddmFormInstanceRecords.size(); i++) {
			DDMFormInstanceRecord ddmFormInstanceRecord =
				ddmFormInstanceRecords.get(i);

			if (Objects.equals(
					ddmFormInstanceRecord.getIpAddress(),
					httpServletRequest.getRemoteAddr()) &&
				((i + 1) == guestUploadMaximumSubmissions)) {

				return true;
			}
		}

		return false;
	}

	@Reference(unbind = "-")
	private void _setDDMFormInstanceRecordLocalService(
		DDMFormInstanceRecordLocalService ddmFormInstanceRecordLocalService) {

		_ddmFormInstanceRecordLocalService = ddmFormInstanceRecordLocalService;
	}

	private static DDMFormInstanceRecordLocalService
		_ddmFormInstanceRecordLocalService;

}