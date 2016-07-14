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
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.analysis.KeywordTokenizer;
import com.liferay.portal.search.analysis.QueryContributor;
import com.liferay.portal.search.analysis.QueryContributorUtil;

/**
 * @author Rodrigo Paulino
 */
@Component(
	immediate = true,
	property = {
		"full.text.exact.match.boost=2.0", "full.text.proximity.slop=50"
	},
	service = FullTextSearchQueryContributor.class
)
public class FullTextSearchQueryContributor implements QueryContributor{

	@Override
	public Query contribute(String field, String value, boolean splitKeywords) {
		BooleanQueryImpl booleanQueryImpl = new BooleanQueryImpl();

		List<String> tokens = null;

		if (keywordTokenizer != null) {
			tokens = keywordTokenizer.tokenize(value);

			List<String> phrases = QueryContributorUtil.getEmbeddedPhrases(
					tokens);

			if (phrases.isEmpty()) {
				createTokenQuery(field, value, booleanQueryImpl);
			}
			else {
				createPhraseQuery(field, booleanQueryImpl, tokens, phrases);
			}
		}


		return booleanQueryImpl;
	}

	protected void createPhraseQuery(String field,
			BooleanQueryImpl booleanQueryImpl, List<String> tokens,
			List<String> phrases) {
		String value;
		for (String phrase : phrases) {
			booleanQueryImpl.add(
				new MatchQuery(field, phrase), BooleanClauseOccur.MUST);
		}

		tokens.removeAll(phrases);

		if (!tokens.isEmpty()) {
			value = StringUtil.merge(tokens, " ");

			booleanQueryImpl.add(
				new MatchQuery(field, value),
				BooleanClauseOccur.SHOULD);
		}
	}

	protected void createTokenQuery(String field, String value,
			BooleanQueryImpl booleanQueryImpl) {
		booleanQueryImpl.add(
			new MatchQuery(field, value), BooleanClauseOccur.MUST);

		booleanQueryImpl.add(
			QueryContributorUtil.createPhraseExactMatchQuery(
			field, value, _fullTextExactMatchBoost), BooleanClauseOccur.SHOULD);

		booleanQueryImpl.add(
			QueryContributorUtil.createFullTextProximityQuery(
			field, value, _fullTextProximitySlop), BooleanClauseOccur.SHOULD);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_fullTextExactMatchBoost = GetterUtil.getFloat(
			properties.get("full.text.exact.match.boost"),
			_fullTextExactMatchBoost);

		_fullTextProximitySlop = GetterUtil.getInteger(
			properties.get("full.text.proximity.slop"), _fullTextProximitySlop);
	}

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	protected KeywordTokenizer keywordTokenizer;

	private volatile float _fullTextExactMatchBoost = 2.0f;
	private volatile int _fullTextProximitySlop = 50;

}