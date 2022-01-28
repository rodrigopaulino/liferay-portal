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

package com.liferay.dynamic.data.mapping.internal.util;

import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.dynamic.data.mapping.constants.DDMFormConstants;
import com.liferay.dynamic.data.mapping.util.DocumentLibraryDDMFormFieldHelper;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepository;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.Calendar;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Paulino
 */
@Component(immediate = true, service = DocumentLibraryDDMFormFieldHelper.class)
public class DocumentLibraryDDMFormFieldHelperImpl
	implements DocumentLibraryDDMFormFieldHelper {

	@Override
	public Folder getUploadFolder(
		long companyId, boolean folderMountPoint, String folderUuid,
		long groupId, boolean signedIn, long userId, Locale userLocale,
		String userScreenName) {

		ServiceContext serviceContext = _getServiceContext(
			folderMountPoint, folderUuid);

		Repository repository = _getRepository(groupId, serviceContext);

		if (repository == null) {
			return null;
		}

		if (!signedIn) {
			return _getPortletFolder(
				companyId, groupId, repository, serviceContext);
		}

		return _getPrivateUserFolder(
			companyId, groupId, userLocale, repository, userScreenName,
			serviceContext, userId);
	}

	private User _createDDMFormDefaultUser(long companyId) {
		try {
			long creatorUserId = 0;
			boolean autoPassword = true;
			String password1 = StringPool.BLANK;
			String password2 = StringPool.BLANK;
			boolean autoScreenName = false;
			String screenName =
				DDMFormConstants.DDM_FORM_DEFAULT_USER_SCREEN_NAME;
			String emailAddress = _getEmailAddress(companyId);
			Locale locale = LocaleUtil.getDefault();
			String firstName =
				DDMFormConstants.DDM_FORM_DEFAULT_USER_FIRST_NAME;
			String middleName = StringPool.BLANK;
			String lastName = DDMFormConstants.DDM_FORM_DEFAULT_USER_LAST_NAME;
			long prefixId = 0;
			long suffixId = 0;
			boolean male = true;
			int birthdayMonth = Calendar.JANUARY;
			int birthdayDay = 1;
			int birthdayYear = 1970;
			String jobTitle = StringPool.BLANK;
			long[] groupIds = null;
			long[] organizationIds = null;
			long[] roleIds = null;
			long[] userGroupIds = null;
			boolean sendEmail = false;
			ServiceContext serviceContext = null;

			User user = _userLocalService.addUser(
				creatorUserId, companyId, autoPassword, password1, password2,
				autoScreenName, screenName, emailAddress, locale, firstName,
				middleName, lastName, prefixId, suffixId, male, birthdayMonth,
				birthdayDay, birthdayYear, jobTitle, groupIds, organizationIds,
				roleIds, userGroupIds, sendEmail, serviceContext);

			_userLocalService.updateStatus(
				user.getUserId(), WorkflowConstants.STATUS_INACTIVE,
				new ServiceContext());

			return user;
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}

			return null;
		}
	}

	private Folder _createPortletFolder(
		long companyId, long groupId, ServiceContext serviceContext) {

		User user = _getDDMFormDefaultUser(companyId);

		Repository repository = _getRepository(groupId, serviceContext);

		try {
			return _portletFileRepository.addPortletFolder(
				user.getUserId(), repository.getRepositoryId(),
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
				DDMFormConstants.DDM_FORM_UPLOADED_FILES_FOLDER_NAME,
				serviceContext);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}

			return null;
		}
	}

	private Folder _createPrivateUserFolder(
		Locale locale, long parentFolderId, long repositoryId,
		String screenName, ServiceContext serviceContext, long userId) {

		try {
			return _dlAppService.addFolder(
				repositoryId, parentFolderId, screenName,
				LanguageUtil.get(
					locale,
					"this-folder-was-automatically-created-by-forms-to-store-" +
						"all-your-uploaded-files"),
				serviceContext);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Unable to retrieve private uploads folder of user " +
						userId,
					portalException);
			}

			return null;
		}
	}

	private User _getDDMFormDefaultUser(long companyId) {
		try {
			return _userLocalService.getUserByEmailAddress(
				companyId, _getEmailAddress(companyId));
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}

			return _createDDMFormDefaultUser(companyId);
		}
	}

	private String _getEmailAddress(long companyId) {
		try {
			Company company = _companyLocalService.getCompany(companyId);

			return StringBundler.concat(
				DDMFormConstants.DDM_FORM_DEFAULT_USER_SCREEN_NAME,
				StringPool.AT, company.getMx());
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}

			return null;
		}
	}

	private Folder _getPortletFolder(
		long companyId, long groupId, Repository repository,
		ServiceContext serviceContext) {

		try {
			return _portletFileRepository.getPortletFolder(
				repository.getRepositoryId(),
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
				DDMFormConstants.DDM_FORM_UPLOADED_FILES_FOLDER_NAME);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}

			return _createPortletFolder(companyId, groupId, serviceContext);
		}
	}

	private Folder _getPrivateUserFolder(
		long companyId, long groupId, Locale locale, Repository repository,
		String screenName, ServiceContext serviceContext, long userId) {

		Folder parentFolder = _getPortletFolder(
			companyId, groupId, repository, serviceContext);

		try {
			return _dlAppService.getFolder(
				repository.getRepositoryId(), parentFolder.getFolderId(),
				screenName);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"The user " + userId +
						" does not have a private uploads folder",
					portalException);
			}

			return _createPrivateUserFolder(
				locale, parentFolder.getFolderId(),
				repository.getRepositoryId(), screenName, serviceContext,
				userId);
		}
	}

	private Repository _getRepository(
		long groupId, ServiceContext serviceContext) {

		Repository repository = null;

		try {
			repository = _portletFileRepository.fetchPortletRepository(
				groupId, DDMFormConstants.SERVICE_NAME);

			if (repository == null) {
				return _portletFileRepository.addPortletRepository(
					groupId, DDMFormConstants.SERVICE_NAME, serviceContext);
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}
		}

		return repository;
	}

	private ServiceContext _getServiceContext(
		boolean folderMountPoint, String folderUuid) {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setAttribute("mountPoint", folderMountPoint);
		serviceContext.setUuid(folderUuid);

		return serviceContext;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DocumentLibraryDDMFormFieldHelperImpl.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private DLAppService _dlAppService;

	@Reference
	private PortletFileRepository _portletFileRepository;

	@Reference
	private UserLocalService _userLocalService;

}