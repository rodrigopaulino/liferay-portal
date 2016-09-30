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

import com.liferay.dynamic.data.lists.service.DDLRecordSetService;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderer;
import com.liferay.dynamic.data.mapping.form.values.factory.DDMFormValuesFactory;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Paulino
 */
@Component(immediate = true, service = AdvancedSearchDisplayContextFactory.class)
public class AdvancedSearchDisplayContextFactoryImpl
	implements AdvancedSearchDisplayContextFactory {

	@Override
	public AdvancedSearchDisplayContext create(
		RenderRequest renderRequest, RenderResponse renderResponse)
		throws Exception {

		return new AdvancedSearchDisplayContext(
			renderRequest, renderResponse, _ddlRecordSetService,
			_ddmFormRenderer, _ddmFormValuesFactory);
	}

	@Reference
	protected DDLRecordSetService _ddlRecordSetService;

	@Reference
	private DDMFormRenderer _ddmFormRenderer;

	@Reference
	private DDMFormValuesFactory _ddmFormValuesFactory;

}