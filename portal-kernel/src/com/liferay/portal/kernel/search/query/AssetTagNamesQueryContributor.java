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

package com.liferay.portal.kernel.search.query;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.generic.MatchQuery;

/**
 * @author André de Oliveira
 */
public class AssetTagNamesQueryContributor {

	public void contribute(String keywords, BooleanQuery booleanQuery) {
		_add(createMatchQuery(keywords), booleanQuery);
		_add(createPrefixQuery(keywords), booleanQuery);
	}

	protected MatchQuery createMatchQuery(String keywords) {
		return new MatchQuery(Field.ASSET_TAG_NAMES, keywords);
	}

	protected MatchQuery createPrefixQuery(String keywords) {
		MatchQuery matchQuery = createMatchQuery(keywords);

		matchQuery.setType(MatchQuery.Type.PHRASE_PREFIX);

		return matchQuery;
	}

	private void _add(Query query, BooleanQuery booleanQuery) {
		booleanQuery.add(query, BooleanClauseOccur.SHOULD);
	}

}