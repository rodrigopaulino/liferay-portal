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

package com.liferay.dynamic.data.mapping.form.field.type.internal.document.library;

import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.dynamic.data.mapping.constants.DDMPortletKeys;
import com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldTemplateContextContributor;
import com.liferay.dynamic.data.mapping.form.field.type.constants.DDMFormFieldTypeConstants;
import com.liferay.dynamic.data.mapping.form.item.selector.criterion.DDMUserPersonalFolderItemSelectorCriterion;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.render.DDMFormFieldRenderingContext;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.ItemSelectorCriterion;
import com.liferay.item.selector.criteria.FileEntryItemSelectorReturnType;
import com.liferay.item.selector.criteria.file.criterion.FileItemSelectorCriterion;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Html;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pedro Queiroz
 */
@Component(
	immediate = true,
	property = "ddm.form.field.type.name=" + DDMFormFieldTypeConstants.DOCUMENT_LIBRARY,
	service = {
		DDMFormFieldTemplateContextContributor.class,
		DocumentLibraryDDMFormFieldTemplateContextContributor.class
	}
)
public class DocumentLibraryDDMFormFieldTemplateContextContributor
	implements DDMFormFieldTemplateContextContributor {

	@Override
	public Map<String, Object> getParameters(
		DDMFormField ddmFormField,
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext) {

		return HashMapBuilder.<String, Object>put(
			"containsAddFolderPermission",
			GetterUtil.getBoolean(
				ddmFormField.getProperty("containsAddFolderPermission"), true)
		).put(
			"evaluable",
			_isEvaluable(ddmFormField, ddmFormFieldRenderingContext)
		).put(
			"groupId", ddmFormFieldRenderingContext.getProperty("groupId")
		).put(
			"guestUploadForbidden",
			GetterUtil.getBoolean(
				ddmFormField.getProperty("guestUploadForbidden"))
		).put(
			"guestUploadLimitReached",
			GetterUtil.getBoolean(
				ddmFormField.getProperty("guestUploadLimitReached"))
		).put(
			"guestUploadMaxFileSize",
			GetterUtil.getLong(
				ddmFormField.getProperty("guestUploadMaxFileSize"))
		).put(
			"maximumRepetitions",
			GetterUtil.getInteger(
				ddmFormField.getProperty("maximumRepetitions"))
		).put(
			"message",
			_getMessage(
				ddmFormFieldRenderingContext.getLocale(),
				ddmFormFieldRenderingContext.getValue())
		).put(
			"value",
			() -> {
				String value = ddmFormFieldRenderingContext.getValue();

				if (Validator.isNull(value)) {
					return "{}";
				}

				return value;
			}
		).putAll(
			_getFileEntryParameters(
				ddmFormFieldRenderingContext.getHttpServletRequest(),
				ddmFormFieldRenderingContext.getValue())
		).putAll(
			_getUploadParameters(ddmFormField, ddmFormFieldRenderingContext)
		).build();
	}

	protected ThemeDisplay getThemeDisplay(
		HttpServletRequest httpServletRequest) {

		return (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	private FileEntry _getFileEntry(JSONObject valueJSONObject) {
		try {
			return _dlAppService.getFileEntryByUuidAndGroupId(
				valueJSONObject.getString("uuid"),
				valueJSONObject.getLong("groupId"));
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to retrieve file entry ", portalException);
			}

			return null;
		}
	}

	private Map<String, Object> _getFileEntryParameters(
		HttpServletRequest httpServletRequest, String value) {

		Map<String, Object> fileEntryParametersMap =
			HashMapBuilder.<String, Object>put(
				"fileEntryTitle", StringPool.BLANK
			).put(
				"fileEntryURL", StringPool.BLANK
			).build();

		if (Validator.isNull(value)) {
			return fileEntryParametersMap;
		}

		JSONObject valueJSONObject = _getValueJSONObject(value);

		if ((valueJSONObject == null) || (valueJSONObject.length() <= 0)) {
			return fileEntryParametersMap;
		}

		FileEntry fileEntry = _getFileEntry(valueJSONObject);

		fileEntryParametersMap.put(
			"fileEntryTitle", _getFileEntryTitle(fileEntry, valueJSONObject));
		fileEntryParametersMap.put(
			"fileEntryURL", _getFileEntryURL(httpServletRequest, fileEntry));

		return fileEntryParametersMap;
	}

	private String _getFileEntryTitle(
		FileEntry fileEntry, JSONObject valueJSONObject) {

		if (fileEntry == null) {
			return StringPool.BLANK;
		}

		if (fileEntry.isInTrash()) {
			return valueJSONObject.getString("title");
		}

		return _html.escape(fileEntry.getTitle());
	}

	private String _getFileEntryURL(
		HttpServletRequest httpServletRequest, FileEntry fileEntry) {

		if (fileEntry == null) {
			return StringPool.BLANK;
		}

		ThemeDisplay themeDisplay = getThemeDisplay(httpServletRequest);

		if (themeDisplay == null) {
			return StringPool.BLANK;
		}

		return _html.escape(
			StringBundler.concat(
				themeDisplay.getPathContext(), "/documents/",
				fileEntry.getRepositoryId(), StringPool.SLASH,
				fileEntry.getFolderId(), StringPool.SLASH,
				URLCodec.encodeURL(_html.unescape(fileEntry.getTitle()), true),
				StringPool.SLASH, fileEntry.getUuid()));
	}

	private long _getFolderId(
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext) {

		Object folderId = ddmFormFieldRenderingContext.getProperty("folderId");

		if (folderId == null) {
			return DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
		}

		return GetterUtil.getLong(folderId);
	}

	private String _getGuestUploadURL(
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext,
		long folderId, HttpServletRequest httpServletRequest) {

		RequestBackedPortletURLFactory requestBackedPortletURLFactory =
			RequestBackedPortletURLFactoryUtil.create(httpServletRequest);

		return PortletURLBuilder.create(
			requestBackedPortletURLFactory.createActionURL(
				GetterUtil.getString(
					_portal.getPortletId(httpServletRequest),
					DDMPortletKeys.DYNAMIC_DATA_MAPPING_FORM))
		).setActionName(
			"/dynamic_data_mapping_form/upload_file_entry"
		).setParameter(
			"folderId", folderId
		).setParameter(
			"formInstanceId",
			ParamUtil.getString(
				httpServletRequest, "formInstanceId",
				String.valueOf(
					ddmFormFieldRenderingContext.getDDMFormInstanceId()))
		).setParameter(
			"groupId", ddmFormFieldRenderingContext.getProperty("groupId")
		).buildString();
	}

	private String _getItemSelectorURL(
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext,
		long folderId, long repositoryId, ThemeDisplay themeDisplay) {

		if (_itemSelector == null) {
			return StringPool.BLANK;
		}

		long groupId = GetterUtil.getLong(
			ddmFormFieldRenderingContext.getProperty("groupId"));

		Group group = _groupLocalService.fetchGroup(groupId);

		if (group == null) {
			group = themeDisplay.getScopeGroup();
		}

		List<ItemSelectorCriterion> itemSelectorCriteria = new ArrayList<>();

		DDMUserPersonalFolderItemSelectorCriterion
			ddmUserPersonalFolderItemSelectorCriterion =
				new DDMUserPersonalFolderItemSelectorCriterion(
					folderId, group.getGroupId());

		ddmUserPersonalFolderItemSelectorCriterion.
			setDesiredItemSelectorReturnTypes(
				new FileEntryItemSelectorReturnType());
		ddmUserPersonalFolderItemSelectorCriterion.setRepositoryId(
			repositoryId);

		itemSelectorCriteria.add(ddmUserPersonalFolderItemSelectorCriterion);

		String portletNamespace =
			ddmFormFieldRenderingContext.getPortletNamespace();

		if (!StringUtil.startsWith(
				portletNamespace,
				_portal.getPortletNamespace(
					DDMPortletKeys.DYNAMIC_DATA_MAPPING_FORM)) &&
			!StringUtil.startsWith(
				portletNamespace,
				_portal.getPortletNamespace(
					DDMPortletKeys.DYNAMIC_DATA_MAPPING_FORM_ADMIN))) {

			FileItemSelectorCriterion fileItemSelectorCriterion =
				new FileItemSelectorCriterion();

			fileItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
				new FileEntryItemSelectorReturnType());

			itemSelectorCriteria.add(fileItemSelectorCriterion);
		}

		PortletURL itemSelectorURL = _itemSelector.getItemSelectorURL(
			RequestBackedPortletURLFactoryUtil.create(
				ddmFormFieldRenderingContext.getHttpServletRequest()),
			group, group.getGroupId(),
			portletNamespace + "selectDocumentLibrary",
			itemSelectorCriteria.toArray(new ItemSelectorCriterion[0]));

		return itemSelectorURL.toString();
	}

	private String _getMessage(Locale defaultLocale, String value) {
		if (Validator.isNull(value)) {
			return StringPool.BLANK;
		}

		JSONObject valueJSONObject = _getValueJSONObject(value);

		if ((valueJSONObject == null) || (valueJSONObject.length() <= 0)) {
			return StringPool.BLANK;
		}

		FileEntry fileEntry = _getFileEntry(valueJSONObject);

		if (fileEntry == null) {
			return LanguageUtil.get(
				defaultLocale, "the-selected-document-was-deleted");
		}

		if (fileEntry.isInTrash()) {
			return LanguageUtil.get(
				defaultLocale,
				"the-selected-document-was-moved-to-the-recycle-bin");
		}

		return StringPool.BLANK;
	}

	private Map<String, Object> _getUploadParameters(
		DDMFormField ddmFormField,
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext) {

		if (ddmFormFieldRenderingContext.isReadOnly()) {
			return new HashMap<>();
		}

		HttpServletRequest httpServletRequest =
			ddmFormFieldRenderingContext.getHttpServletRequest();

		ThemeDisplay themeDisplay = getThemeDisplay(httpServletRequest);

		if ((themeDisplay == null) ||
			(!themeDisplay.isSignedIn() &&
			 !GetterUtil.getBoolean(
				 ddmFormField.getProperty("allowGuestUsers")))) {

			return new HashMap<>();
		}

		if (!themeDisplay.isSignedIn()) {
			return HashMapBuilder.<String, Object>put(
				"guestUploadURL",
				() -> {
					String guestUploadURL = GetterUtil.getString(
						ddmFormField.getProperty("guestUploadURL"));

					if (Validator.isNotNull(guestUploadURL)) {
						return guestUploadURL;
					}

					return _getGuestUploadURL(
						ddmFormFieldRenderingContext,
						_getFolderId(ddmFormFieldRenderingContext),
						httpServletRequest);
				}
			).build();
		}

		return HashMapBuilder.<String, Object>put(
			"itemSelectorURL",
			() -> {
				String itemSelectorURL = GetterUtil.getString(
					ddmFormField.getProperty("itemSelectorURL"));

				if (Validator.isNotNull(itemSelectorURL)) {
					return itemSelectorURL;
				}

				return _getItemSelectorURL(
					ddmFormFieldRenderingContext,
					_getFolderId(ddmFormFieldRenderingContext),
					GetterUtil.getLong(
						ddmFormFieldRenderingContext.getProperty(
							"repositoryId")),
					themeDisplay);
			}
		).build();
	}

	private JSONObject _getValueJSONObject(String value) {
		try {
			return _jsonFactory.createJSONObject(value);
		}
		catch (JSONException jsonException) {
			if (_log.isDebugEnabled()) {
				_log.debug(jsonException, jsonException);
			}

			return null;
		}
	}

	private boolean _isEvaluable(
		DDMFormField ddmFormField,
		DDMFormFieldRenderingContext ddmFormFieldRenderingContext) {

		if (Objects.equals(ddmFormField.getProperty("allowGuestUsers"), true) &&
			!ddmFormField.isRequired()) {

			return true;
		}

		return GetterUtil.getBoolean(
			ddmFormFieldRenderingContext.getProperty("evaluable"));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DocumentLibraryDDMFormFieldTemplateContextContributor.class);

	@Reference
	private DLAppService _dlAppService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Html _html;

	@Reference
	private ItemSelector _itemSelector;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

}