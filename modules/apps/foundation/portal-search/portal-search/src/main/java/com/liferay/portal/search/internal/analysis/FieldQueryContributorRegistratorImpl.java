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
			return _phrasePrefixSearchQueryContributor;
		case "title":
			return _fullTextSearchWithoutProximityContributor;
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
		FullTextSearchWithoutProximityContributor
		fullTextSearchWithoutProximityContributor) {
		_fullTextSearchWithoutProximityContributor =
			fullTextSearchWithoutProximityContributor;
	}

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void setPhrasePrefixQuerySearchContributor(
		PhrasePrefixSearchQueryContributor phrasePrefixQuerySearchContributor) {
		_phrasePrefixSearchQueryContributor =
			phrasePrefixQuerySearchContributor;
	}

	protected void unsetFullTextSearchWithoutProximityContributor(
		FullTextSearchWithoutProximityContributor
		fullTextSearchWithoutProximityContributor) {
		_fullTextSearchWithoutProximityContributor = null;
	}

	protected void unsetPhrasePrefixQuerySearchContributor(
		PhrasePrefixSearchQueryContributor phrasePrefixQuerySearchContributor) {
		_phrasePrefixSearchQueryContributor = null;
	}

	private FullTextSearchWithoutProximityContributor
		_fullTextSearchWithoutProximityContributor;
	private PhrasePrefixSearchQueryContributor
		_phrasePrefixSearchQueryContributor;

}