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

package com.liferay.portal.search.internal.query;

import java.util.HashSet;
import java.util.Iterator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.query.FieldQueryFactory;
import com.liferay.portal.kernel.search.query.QueryPreProcessConfiguration;
import com.liferay.portal.search.analysis.FieldQueryContributorRegistrator;
import com.liferay.portal.search.analysis.QueryContributor;
import com.liferay.portal.search.internal.analysis.FieldQueryContributorRegistratorImpl;
import com.liferay.portal.search.internal.analysis.FullTextSearchQueryContributor;
import com.liferay.portal.search.internal.analysis.SubstringSearchQueryContributor;

/**
 * @author Michael C. Han
 */
@Component(
	immediate = true,
	service = FieldQueryFactory.class
)
public class FieldQueryFactoryImpl implements FieldQueryFactory {

	@Override
	public Query createQuery(
		String field, String value, boolean like, boolean splitKeywords) {

		if (_fieldQueryContributorRegistratorImpl != null) {
			QueryContributor queryContributor =
				_fieldQueryContributorRegistratorImpl.getQueryContributor(
				field);

			if (queryContributor != null) {
				return queryContributor.contribute(field, value, splitKeywords);
			}
		}

		Iterator<FieldQueryContributorRegistrator> iterator =
			_fieldQueryContributorRegistratorSet.iterator();

		FieldQueryContributorRegistrator registrator = null;

		while (iterator.hasNext()) {
			registrator = iterator.next();

			QueryContributor queryContributor =
				registrator.getQueryContributor(field);

			if (queryContributor != null) {
				return queryContributor.contribute(field, value, splitKeywords);
			}
		}

		if (_queryPreProcessConfiguration != null &&
			_queryPreProcessConfiguration.isSubstringSearchAlways(field)) {

			return _substringSearchQueryContributor.contribute(
				field, value, splitKeywords);
		}

		return _fullTextSearchQueryContributor.contribute(
			field, value, splitKeywords);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void addFieldQueryContributorRegistrator(
		FieldQueryContributorRegistrator fieldQueryContributorRegistrator) {
		_fieldQueryContributorRegistratorSet.add(
				fieldQueryContributorRegistrator);
	}

	protected void removeFieldQueryContributorRegistrator(
		FieldQueryContributorRegistrator fieldQueryContributorRegistrator) {
		_fieldQueryContributorRegistratorSet.remove(
				fieldQueryContributorRegistrator);
	}

	private HashSet<FieldQueryContributorRegistrator>
		_fieldQueryContributorRegistratorSet =
		new HashSet<FieldQueryContributorRegistrator>();

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private FieldQueryContributorRegistratorImpl
		_fieldQueryContributorRegistratorImpl;

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private FullTextSearchQueryContributor _fullTextSearchQueryContributor;

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private volatile QueryPreProcessConfiguration _queryPreProcessConfiguration;

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private SubstringSearchQueryContributor _substringSearchQueryContributor;

}