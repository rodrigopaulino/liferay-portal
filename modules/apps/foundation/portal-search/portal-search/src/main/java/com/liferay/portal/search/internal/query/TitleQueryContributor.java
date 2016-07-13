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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.search.analysis.KeywordTokenizer;
import com.liferay.portal.search.analysis.QueryContributor;

/**
 * @author Rodrigo Paulino
 */
@Component(
	immediate = true,
	property = {"full.text.exact.match.boost=2.0"},
	service = FullTextSearchWithoutProximityContributor.class
)
public class FullTextSearchWithoutProximityContributor
	implements QueryContributor{

	@Override
	public Query contribute(String field, String value) {
		if (QueryContributorUtil.isPhrase(value)) {
			return QueryContributorUtil.createPhraseQuery(field, value);
		}

		return createQueryForFullTextScoring(field, value);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_fullTextExactMatchBoost = GetterUtil.getFloat(
			properties.get("full.text.exact.match.boost"),
			_fullTextExactMatchBoost);
	}

	protected Query createQueryForFullTextScoring(String field, String value) {
		BooleanQueryImpl booleanQueryImpl = new BooleanQueryImpl();

		booleanQueryImpl.add(
			new MatchQuery(field, value), BooleanClauseOccur.MUST);

		booleanQueryImpl.add(
			QueryContributorUtil.createPhraseExactMatchQuery(
			field, value, _fullTextExactMatchBoost), BooleanClauseOccur.SHOULD);

		booleanQueryImpl.add(
			QueryContributorUtil.createPrefixQuery(field, value),
			BooleanClauseOccur.SHOULD);

		List<String> phrases = getEmbeddedPhrases(value);

		for (String phrase : phrases) {
			Query query = QueryContributorUtil.createPhraseQuery(
				field, phrase);

			booleanQueryImpl.add(query, BooleanClauseOccur.MUST);
		}

		return booleanQueryImpl;
	}

	protected List<String> getEmbeddedPhrases(String value) {
		if (_keywordTokenizer == null) {
			return Collections.emptyList();
		}

		List<String> tokens = _keywordTokenizer.tokenize(value);

		List<String> phrases = new ArrayList<>(tokens.size());

		for (String token : tokens) {
			if (QueryContributorUtil.isPhrase(token)) {
				phrases.add(token);
			}
		}

		return phrases;
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

	private volatile float _fullTextExactMatchBoost = 2.0f;
	private KeywordTokenizer _keywordTokenizer;

}