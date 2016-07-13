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

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.search.analysis.QueryContributor;

/**
 * @author André de Oliveira
 */
@Component(
	immediate = true,
	service = PhrasePrefixSearchQueryContributor.class
)
public class PhrasePrefixSearchQueryContributor implements QueryContributor{

	@Override
	public Query contribute(String field, String value) {
		BooleanQueryImpl booleanQueryImpl = new BooleanQueryImpl();

		booleanQueryImpl.add(
			new MatchQuery(field, value), BooleanClauseOccur.SHOULD);

		booleanQueryImpl.add(
			QueryContributorUtil.createPrefixQuery(field, value),
			BooleanClauseOccur.SHOULD);

		return booleanQueryImpl;
	}

}