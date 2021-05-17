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

package com.liferay.dynamic.data.mapping.test.util;

import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderingContext;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormTemplateContextFactory;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.portal.kernel.exception.PortalException;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rodrigo Paulino
 */
public class DDMFormTemplateContext {

	public static class Builder {

		public static Builder newBuilder(
			DDMFormTemplateContextFactory ddmFormTemplateContextFactory) {

			return new Builder(ddmFormTemplateContextFactory);
		}

		public Map<String, Object> build() throws PortalException {
			DDMFormRenderingContext ddmFormRenderingContext =
				new DDMFormRenderingContext();

			if (_containerId != null) {
				ddmFormRenderingContext.setContainerId(_containerId);
			}

			if (_httpServletRequest != null) {
				ddmFormRenderingContext.setHttpServletRequest(
					_httpServletRequest);
			}

			if (_locale != null) {
				ddmFormRenderingContext.setLocale(_locale);
			}

			if (_portletNamespace != null) {
				ddmFormRenderingContext.setPortletNamespace(_portletNamespace);
			}

			if (_readOnly != null) {
				ddmFormRenderingContext.setReadOnly(_readOnly);
			}

			if (_showRequiredFieldsWarning != null) {
				ddmFormRenderingContext.setShowRequiredFieldsWarning(
					_showRequiredFieldsWarning);
			}

			if (_showSubmitButton != null) {
				ddmFormRenderingContext.setShowSubmitButton(_showSubmitButton);
			}

			if (_submitLabel != null) {
				ddmFormRenderingContext.setSubmitLabel(_submitLabel);
			}

			if (_viewMode != null) {
				ddmFormRenderingContext.setViewMode(_viewMode);
			}

			if (_paginationMode != null) {
				DDMFormLayout ddmFormLayout = new DDMFormLayout();

				ddmFormLayout.setPaginationMode(_paginationMode);

				return _ddmFormTemplateContextFactory.create(
					DDMFormTestUtil.createDDMForm(), ddmFormLayout,
					ddmFormRenderingContext);
			}

			return _ddmFormTemplateContextFactory.create(
				DDMFormTestUtil.createDDMForm(), ddmFormRenderingContext);
		}

		public Builder withContainerId(String containerId) {
			_containerId = containerId;

			return this;
		}

		public Builder withHttpServletRequest(
			HttpServletRequest httpServletRequest) {

			_httpServletRequest = httpServletRequest;

			return this;
		}

		public Builder withLocale(Locale locale) {
			_locale = locale;

			return this;
		}

		public Builder withPaginationMode(String paginationMode) {
			_paginationMode = paginationMode;

			return this;
		}

		public Builder withPortletNamespace(String portletNamespace) {
			_portletNamespace = portletNamespace;

			return this;
		}

		public Builder withReadOnly(boolean readOnly) {
			_readOnly = readOnly;

			return this;
		}

		public Builder withShowRequiredFieldsWarning(
			boolean showRequiredFieldsWarning) {

			_showRequiredFieldsWarning = showRequiredFieldsWarning;

			return this;
		}

		public Builder withShowSubmitButton(boolean showSubmitButton) {
			_showSubmitButton = showSubmitButton;

			return this;
		}

		public Builder withSubmitLabel(String submitLabel) {
			_submitLabel = submitLabel;

			return this;
		}

		public Builder withViewMode(boolean viewMode) {
			_viewMode = viewMode;

			return this;
		}

		private Builder(
			DDMFormTemplateContextFactory ddmFormTemplateContextFactory) {

			_ddmFormTemplateContextFactory = ddmFormTemplateContextFactory;
		}

		private String _containerId;
		private final DDMFormTemplateContextFactory
			_ddmFormTemplateContextFactory;
		private HttpServletRequest _httpServletRequest;
		private Locale _locale;
		private String _paginationMode;
		private String _portletNamespace;
		private Boolean _readOnly;
		private Boolean _showRequiredFieldsWarning;
		private Boolean _showSubmitButton;
		private String _submitLabel;
		private Boolean _viewMode;

	}

}