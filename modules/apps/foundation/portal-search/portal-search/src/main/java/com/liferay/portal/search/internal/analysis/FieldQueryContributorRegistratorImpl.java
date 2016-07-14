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

package com.liferay.portal.search.internal.analysis;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.liferay.portal.search.analysis.FieldQueryContributorRegistrator;
import com.liferay.portal.search.analysis.QueryContributor;

/**
 * @author Rodrigo Paulino
 */
@Component(
	immediate = true,
	service = FieldQueryContributorRegistratorImpl.class
)
public class FieldQueryContributorRegistratorImpl
	implements FieldQueryContributorRegistrator{

	@Override
	public QueryContributor getQueryContributor(String fieldName) {
		switch (fieldName) {
		case "assetTagNames":
			return _assetTagNamesQueryContributor;
		case "title":
			return _titleQueryContributor;
		default:
			return null;
		}
	}

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void setFullTextSearchWithoutProximityContributor(
		TitleQueryContributor titleQueryContributor) {

		_titleQueryContributor = titleQueryContributor;
	}

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void setPhrasePrefixQuerySearchContributor(
		AssetTagNamesQueryContributor assetTagNamesQueryContributor) {

		_assetTagNamesQueryContributor = assetTagNamesQueryContributor;
	}

	protected void unsetFullTextSearchWithoutProximityContributor(
		TitleQueryContributor titleQueryContributor) {

		_titleQueryContributor = null;
	}

	protected void unsetPhrasePrefixQuerySearchContributor(
		AssetTagNamesQueryContributor assetTagNamesQueryContributor) {

		_assetTagNamesQueryContributor = null;
	}

	private TitleQueryContributor _titleQueryContributor;
	private AssetTagNamesQueryContributor _assetTagNamesQueryContributor;

}