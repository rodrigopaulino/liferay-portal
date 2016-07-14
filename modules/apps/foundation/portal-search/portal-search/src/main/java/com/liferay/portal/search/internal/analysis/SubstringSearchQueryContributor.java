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

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.search.analysis.KeywordTokenizer;
import com.liferay.portal.search.analysis.QueryContributor;
import com.liferay.portal.search.analysis.QueryContributorUtil;

/**
 * @author Rodrigo Paulino
 */
@Component(
	immediate = true,
	service = SubstringSearchQueryContributor.class
)
public class SubstringSearchQueryContributor implements QueryContributor{

	@Override
	public Query contribute(String field, String value, boolean splitKeywords) {
		if (!splitKeywords && (_keywordTokenizer != null)) {
			splitKeywords = _keywordTokenizer.requiresTokenization(value);
		}

		if (splitKeywords && (_keywordTokenizer != null)) {
			List<String> tokens = _keywordTokenizer.tokenize(value);

			if (tokens.size() == 1) {
				return contribute(field, tokens.get(0), false);
			}

			BooleanQueryImpl booleanQuery = new BooleanQueryImpl();

			for (String token : tokens) {
				Query tokenQuery = createTokenQuery(field, token);

				booleanQuery.add(tokenQuery, BooleanClauseOccur.SHOULD);
			}

			return booleanQuery;
		}

		return createTokenQuery(field, value);
	}

	protected Query createTokenQuery(String field, String value) {
		if (QueryContributorUtil.isPhrase(value)) {
			return new MatchQuery(field, value);
		}

		return QueryContributorUtil.createSubstringQuery(field, value);
	}

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected void setKeywordTokenizer(KeywordTokenizer keywordTokenizer) {
		_keywordTokenizer = keywordTokenizer;
	}

	protected void unsetKeywordTokenizer(KeywordTokenizer keywordTokenizer) {
		_keywordTokenizer = null;
	}

	private KeywordTokenizer _keywordTokenizer;

}