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

package com.liferay.portal.search.analysis;

import java.util.ArrayList;
import java.util.List;

import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.generic.MatchQuery;
import com.liferay.portal.kernel.search.generic.QueryTermImpl;
import com.liferay.portal.kernel.search.generic.WildcardQueryImpl;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Rodrigo Paulino
 */
public class QueryContributorUtil {

	public static Query createFullTextProximityQuery(
		String field, String value, Integer slop) {

		MatchQuery matchQuery = new MatchQuery(field, value);

		matchQuery.setType(MatchQuery.Type.PHRASE);

		if (slop != null) {
			matchQuery.setSlop(slop);
		}

		return matchQuery;
	}

	public static Query createPhraseExactMatchQuery(
		String field, String value, Float boost) {

		MatchQuery matchQuery = new MatchQuery(field, value);

		matchQuery.setType(MatchQuery.Type.PHRASE);

		if (boost != null) {
			matchQuery.setBoost(boost);
		}

		return matchQuery;
	}

	public static MatchQuery createPrefixQuery(String field, String value) {
		MatchQuery matchQuery = new MatchQuery(field, value);

		matchQuery.setType(MatchQuery.Type.PHRASE_PREFIX);

		return matchQuery;
	}

	public static Query createSubstringQuery(String field, String value) {
		value = StringUtil.replace(value, CharPool.PERCENT, StringPool.BLANK);

		if (value.length() == 0) {
			value = StringPool.STAR;
		}
		else {
			value = StringUtil.toLowerCase(value);

			value = StringPool.STAR + value + StringPool.STAR;
		}

		return new WildcardQueryImpl(new QueryTermImpl(field, value));
	}

	public static List<String> getEmbeddedPhrases(List<String> tokens) {
		List<String> phrases = new ArrayList<>(tokens.size());

		for (String token : tokens) {
			if (isPhrase(token)) {
				phrases.add(token);
			}
		}

		return phrases;
	}

	public static boolean isPhrase(String value) {
		if (value.startsWith(StringPool.QUOTE) &&
			value.endsWith(StringPool.QUOTE)) {

			return true;
		}

		return false;
	}

}