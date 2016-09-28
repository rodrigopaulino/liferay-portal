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

package com.liferay.portal.search.web.internal.document.display.context;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * @author Rodrigo Paulino
 */
public class DocumentDisplayContext {

	public DocumentDisplayContext(
		Document document, PortletRequest request, PortletResponse response,
		Locale locale) {

		_document = document;
		_locale = locale;
		_response = response;
		_request = request;
	}

	public Summary getSummary() throws PortalException {
		String className = _document.get(Field.ENTRY_CLASS_NAME);
		long classPK = GetterUtil.getLong(_document.get(Field.ENTRY_CLASS_PK));

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				className);

		AssetRenderer<?> assetRenderer = null;

		if (assetRendererFactory != null) {
			long resourcePrimKey = GetterUtil.getLong(
				_document.get(Field.ROOT_ENTRY_CLASS_PK));

			if (resourcePrimKey > 0) {
				classPK = resourcePrimKey;
			}

			assetRenderer = assetRendererFactory.getAssetRenderer(classPK);
		}

		Indexer<?> indexer = IndexerRegistryUtil.getIndexer(className);

		Summary summary = null;

		if (indexer != null) {
			String snippet = _document.get(Field.SNIPPET);

			summary = indexer.getSummary(
				_document, snippet, _request, _response);
		}
		else if (assetRenderer != null) {
			summary = new Summary(
				_locale, assetRenderer.getTitle(_locale),
				assetRenderer.getSearchSummary(_locale));
		}

		return summary;
	}

	private final Document _document;
	private final Locale _locale;
	private final PortletRequest _request;
	private final PortletResponse _response;

}