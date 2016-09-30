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

package com.liferay.portal.search.web.components.advanced.search.portlet;

import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.model.DDLRecordSetConstants;
import com.liferay.dynamic.data.lists.service.DDLRecordSetService;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderer;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderingContext;
import com.liferay.dynamic.data.mapping.form.values.factory.DDMFormValuesFactory;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.web.internal.display.context.ThemeDisplaySupplier;
import com.liferay.portal.search.web.internal.portlet.PortletRequestThemeDisplaySupplier;

import java.util.List;
import java.util.Optional;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author Rodrigo Paulino
 */
public class AdvancedSearchDisplayContext {

	public AdvancedSearchDisplayContext(
		RenderRequest renderRequest, RenderResponse renderResponse,
		DDLRecordSetService ddlRecordSetService,
		DDMFormRenderer ddmFormRenderer,
		DDMFormValuesFactory ddmFormValuesFactory) {

		_ddlRecordSetService = ddlRecordSetService;
		_searchContainer = new SearchContainer<DDLRecordSet>();
		_ddmFormRenderer = ddmFormRenderer;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
		_ddmFormValuesFactory = ddmFormValuesFactory;

		setRecordSetSearchTotal(_searchContainer);
		setRecordSetSearchResults(_searchContainer);
	}

	public SearchContainer<DDLRecordSet> getSearchContainer() {
		return _searchContainer;
	}

	public long getRecordSetId() {
		if (_recordSetId != 0) {
			return _recordSetId;
		}

		AdvancedSearchConfiguration advancedSearchConfiguration =
			getAdvancedSearchConfiguration();

		Optional<String> optionalRecordSetId =
			advancedSearchConfiguration.getRecordSet();

		optionalRecordSetId.ifPresent(
			recordSetId -> _recordSetId = Long.parseLong(recordSetId));

		return _recordSetId;
	}

	public DDLRecordSet getRecordSet() {
		if (_recordSet != null) {
			return _recordSet;
		}

		try {
			_recordSet = _ddlRecordSetService.fetchRecordSet(getRecordSetId());
		}
		catch (PortalException pe) {
			return null;
		}

		return _recordSet;
	}

	protected void setRecordSetSearchResults(
		SearchContainer<DDLRecordSet> searchContainer) {

		ThemeDisplay themeDisplay = getThemeDisplay(_renderRequest);

		List<DDLRecordSet> results = _ddlRecordSetService.search(
			themeDisplay.getCompanyId(), themeDisplay.getScopeGroupId(), "",
			DDLRecordSetConstants.SCOPE_FORMS, searchContainer.getStart(),
			searchContainer.getEnd(), searchContainer.getOrderByComparator());

		searchContainer.setResults(results);
	}

	protected ThemeDisplay getThemeDisplay(RenderRequest renderRequest) {
		ThemeDisplaySupplier themeDisplaySupplier =
			new PortletRequestThemeDisplaySupplier(renderRequest);

		return themeDisplaySupplier.getThemeDisplay();
	}

	public String getDDMFormHTML() throws PortalException {
		DDLRecordSet recordSet = getRecordSet();

		if (recordSet == null) {
			return StringPool.BLANK;
		}

		DDMStructure ddmStructure = recordSet.getDDMStructure();

		DDMForm ddmForm = ddmStructure.getDDMForm();

		DDMFormLayout ddmFormLayout = ddmStructure.getDDMFormLayout();

		DDMFormRenderingContext ddmFormRenderingContext =
			createDDMFormRenderingContext(ddmForm);

		return _ddmFormRenderer.render(
			ddmForm, ddmFormLayout, ddmFormRenderingContext);
	}

	protected DDMFormRenderingContext createDDMFormRenderingContext(
		DDMForm ddmForm) {

		String languageId = ParamUtil.getString(_renderRequest, "languageId");

		DDMFormRenderingContext ddmFormRenderingContext =
			new DDMFormRenderingContext();

		ddmFormRenderingContext.setDDMFormValues(
			_ddmFormValuesFactory.create(_renderRequest, ddmForm));
		ddmFormRenderingContext.setHttpServletRequest(
			PortalUtil.getHttpServletRequest(_renderRequest));
		ddmFormRenderingContext.setHttpServletResponse(
			PortalUtil.getHttpServletResponse(_renderResponse));
		ddmFormRenderingContext.setLocale(
			LocaleUtil.fromLanguageId(languageId));
		ddmFormRenderingContext.setPortletNamespace(
			_renderResponse.getNamespace());

		return ddmFormRenderingContext;
	}

	protected AdvancedSearchConfiguration getAdvancedSearchConfiguration() {
		PortletPreferences preferences = _renderRequest.getPreferences();

		AdvancedSearchConfigurationImpl advancedSearchConfigurationImpl =
			new AdvancedSearchConfigurationImpl(preferences);

		return advancedSearchConfigurationImpl;
	}

	protected void setRecordSetSearchTotal(
		SearchContainer<DDLRecordSet> searchContainer) {

		ThemeDisplay themeDisplay = getThemeDisplay(_renderRequest);

		int total = _ddlRecordSetService.searchCount(
			themeDisplay.getCompanyId(), themeDisplay.getScopeGroupId(), "",
			DDLRecordSetConstants.SCOPE_FORMS);

		searchContainer.setTotal(total);
	}

	private final DDLRecordSetService _ddlRecordSetService;
	private final DDMFormRenderer _ddmFormRenderer;
	private final DDMFormValuesFactory _ddmFormValuesFactory;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final SearchContainer<DDLRecordSet> _searchContainer;
	private long _recordSetId;
	private DDLRecordSet _recordSet;

}