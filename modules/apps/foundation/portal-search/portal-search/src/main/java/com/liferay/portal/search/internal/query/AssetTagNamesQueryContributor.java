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

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.kernel.search.query.QueryContributor;
import com.liferay.portal.kernel.search.query.QueryContributorUtil;

/**
 * @author André de Oliveira
 */
@Component(
	immediate = true,
	service = AssetTagNamesQueryContributor.class
)
public class AssetTagNamesQueryContributor implements QueryContributor{

	@Override
	public Query contribute(String field, String value, boolean splitKeywords) {
		BooleanQueryImpl booleanQueryImpl = new BooleanQueryImpl();

		booleanQueryImpl.add(
			new MatchQuery(field, value), BooleanClauseOccur.SHOULD);

		booleanQueryImpl.add(
			QueryContributorUtil.createPrefixQuery(field, value),
			BooleanClauseOccur.SHOULD);

		return booleanQueryImpl;
	}

}