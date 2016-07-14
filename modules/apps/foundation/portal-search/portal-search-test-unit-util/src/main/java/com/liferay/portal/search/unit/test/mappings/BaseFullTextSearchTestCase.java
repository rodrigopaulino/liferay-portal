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

package com.liferay.portal.search.unit.test.mappings;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Assume;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.test.IdempotentRetryAssert;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.internal.analysis.FullTextSearchQueryContributor;
import com.liferay.portal.search.internal.analysis.SimpleKeywordTokenizer;
import com.liferay.portal.search.unit.test.BaseIndexingTestCase;

/**
 * @author André de Oliveira
 */
public abstract class BaseFullTextSearchTestCase extends BaseIndexingTestCase {

	protected void addDocument(final String... assetTagNames) throws Exception {
		addDocument(
			new DocumentCreationHelper() {

				@Override
				public void populate(Document document) {
					document.addText(Field.DESCRIPTION, assetTagNames);
				}

			});
	}

	protected void assertSearch(final String keywords, final int count)
		throws Exception {

		final SearchContext searchContext = createSearchContext();

		final Query query = createQuery(keywords);

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					assertSearch(keywords, count, searchContext, query);

					return null;
				}

			});
	}

	protected void assertSearch(
			String keywords, int count, SearchContext searchContext,
			Query query)
		throws Exception {

		Hits hits = search(searchContext, query);

		Assert.assertEquals(keywords, count, hits.getLength());
	}

	protected Query createQuery(String keywords) {
		FullTextSearchQueryContributor fullTextSearchQueryContributor =
			new FullTextSearchQueryContributor() {
				{
					keywordTokenizer = new SimpleKeywordTokenizer();
				}
			};

		return fullTextSearchQueryContributor.contribute(
			Field.DESCRIPTION, keywords, false);
	}

	protected boolean isPhrasePrefixesWithMultipleTermsSupported() {
		return true;
	}

	protected void testBasicWordMatches() throws Exception {
		addDocument("LOOKing for DOCUments");
		addDocument("this is a test for description");
		addDocument("Description Test");
		addDocument("TESTING THE DESCRIPTION");
		addDocument(RandomTestUtil.randomString());

		assertSearch("looking", 1);
		assertSearch("for", 2);
		assertSearch("look", 0);
		assertSearch("description", 3);
		assertSearch("\"TEST\"", 2);
		assertSearch("\"look\"", 0);
		assertSearch("\"LOOKING\"", 1);
		assertSearch("\"description\"", 3);
		assertSearch("\"testing\"", 1);
		assertSearch("\"a\"", 1);
	}

	protected void testNumbers() throws Exception {
		addDocument("Description with 1 number");
		addDocument("Description with NO numbers");
		addDocument("4ever");
		addDocument(RandomTestUtil.randomString());

		assertSearch("1", 1);
		assertSearch("4", 0);
		assertSearch("4ever", 1);
		assertSearch("4EVER", 1);
		assertSearch("\"1\"", 1);
		assertSearch("\"4\"", 0);
		assertSearch("\"4ever\"", 1);
		assertSearch("\"4EVER\"", 1);
		assertSearch("FOREVER", 0);
	}

	protected void testPhrases() throws Exception {
		addDocument("Names of Tags");
		addDocument("More names of tags here");
		addDocument(RandomTestUtil.randomString());

		assertSearch("\"Tags here\"", 1);
		assertSearch("\"Tags\" here", 2);
		assertSearch("\"NAmes\" \"TAGS\"", 2);
		assertSearch("\"names\" of \"tAgs\"", 2);
		assertSearch("\"names\" MORE \"tags\"", 2);
		assertSearch("\"name\" of \"tags\"", 0);
		assertSearch("\"more\" other \"here\"", 1);
		assertSearch("\"   more   \"     other    \"   here   \"", 0);
		assertSearch("\"more\"     other    \"here\"", 1);
		assertSearch("\"names of tags\"", 2);
		assertSearch("\"TAGS\"", 2);
		assertSearch("\"more\"", 1);
		assertSearch("\"More\"", 1);
		assertSearch("\"Tags here\"", 1);
		assertSearch("\"HERE\"", 1);
	}

	protected void testPhrasePrefixesWithMultipleTerms() throws Exception {
		Assume.assumeTrue(isPhrasePrefixesWithMultipleTermsSupported());

		addDocument("Name Tags");
		addDocument("Names Tab");
		addDocument("Tag Names");
		addDocument("Tabs Names Tags");
		addDocument(RandomTestUtil.randomString());

		assertSearch("\"name ta*\"", 1);
		assertSearch("\"name tab*\"", 0);
		assertSearch("\"name tabs*\"", 0);
		assertSearch("\"name tag*\"", 1);
		assertSearch("\"name tags*\"", 1);

		assertSearch("\"names ta*\"", 2);
		assertSearch("\"names tab*\"", 1);
		assertSearch("\"names tabs*\"", 0);
		assertSearch("\"names tag*\"", 1);
		assertSearch("\"names tags*\"", 1);

		assertSearch("\"tab na*\"", 0);
		assertSearch("\"tab names*\"", 0);

		assertSearch("\"tabs na ta*\"", 0);
		assertSearch("\"tabs name*\"", 1);
		assertSearch("\"tabs name ta*\"", 0);
		assertSearch("\"tabs names*\"", 1);
		assertSearch("\"tabs names ta*\"", 1);
		assertSearch("\"tabs names tag*\"", 1);
		assertSearch("\"tabs names tags*\"", 1);

		assertSearch("\"tag na*\"", 1);
		assertSearch("\"tag name*\"", 1);
		assertSearch("\"tag names*\"", 1);

		assertSearch("\"tags na ta*\"", 0);
		assertSearch("\"tags names*\"", 0);
		assertSearch("\"tags names tabs*\"", 0);

		assertSearch("\"zz na*\"", 0);
		assertSearch("\"zz name*\"", 0);
		assertSearch("\"zz names*\"", 0);
		assertSearch("\"zz ta*\"", 0);
		assertSearch("\"zz tab*\"", 0);
		assertSearch("\"zz tabs*\"", 0);
		assertSearch("\"zz tag*\"", 0);
		assertSearch("\"zz tags*\"", 0);
	}

	protected void testPhrasePrefixes() throws Exception {
		addDocument("Nametag");
		addDocument("NA-META-G");
		addDocument("Tag Name");
		addDocument("TAG1");
		addDocument(RandomTestUtil.randomString());

		assertSearch("\"meta\"", 0);
		assertSearch("\"me*\"", 0);
		assertSearch("\"*me\"", 0);
		assertSearch("\"*me*\"", 0);
		assertSearch("\"met\"", 0);
		assertSearch("\"*met*\"", 0);
		assertSearch("\"Na\"", 0);
		assertSearch("\"NA*\"", 3);
		assertSearch("\"*NA\"", 0);
		assertSearch("\"*NA*\"", 0);
		assertSearch("\"Namet\"", 0);
		assertSearch("\"namet*\"", 1);
		assertSearch("\"*namet\"", 0);
		assertSearch("\"*namet*\"", 0);
		assertSearch("\"Ta\"", 0);
		assertSearch("\"Ta*\"", 2);
		assertSearch("\"*Ta\"", 0);
		assertSearch("\"*Ta*\"", 0);
		assertSearch("\"tag\"", 1);
	}

	protected void testPrefixes() throws Exception {
		addDocument("Nametag");
		addDocument("NA-META-G");
		addDocument("Tag Name");
		addDocument("TAG1");
		addDocument(RandomTestUtil.randomString());

		assertSearch("me", 0);
		assertSearch("me*", 0);
		assertSearch("*me", 0);
		assertSearch("*me*", 0);
		assertSearch("met", 0);
		assertSearch("*met*", 0);
		assertSearch("Na", 0);
		assertSearch("NA*", 0);
		assertSearch("*NA", 0);
		assertSearch("*NA*", 0);
		assertSearch("Namet", 0);
		assertSearch("namet*", 0);
		assertSearch("*namet", 0);
		assertSearch("*namet*", 0);
		assertSearch("Ta", 0);
		assertSearch("Ta*", 0);
		assertSearch("*Ta", 0);
		assertSearch("*Ta*", 0);
		assertSearch("tag", 1);
	}

	protected void testPrefixesWithMultipleTerms() throws Exception {
		addDocument("Name Tags");
		addDocument("Names Tab");
		addDocument("Tag Names");
		addDocument("Tabs Names Tags");
		addDocument(RandomTestUtil.randomString());

		assertSearch("name ta", 1);
		assertSearch("name tab", 2);
		assertSearch("name tabs", 2);
		assertSearch("name tag", 2);
		assertSearch("name tags", 2);

		assertSearch("names ta", 3);
		assertSearch("names tab", 3);
		assertSearch("names tabs", 3);
		assertSearch("names tag", 3);
		assertSearch("names tags", 4);

		assertSearch("tab na", 1);
		assertSearch("tab names", 3);

		assertSearch("tabs na ta", 1);
		assertSearch("tabs names", 3);
		assertSearch("tabs names tags", 4);

		assertSearch("tag na", 1);
		assertSearch("tag name", 2);
		assertSearch("tag names", 3);

		assertSearch("tags na ta", 2);
		assertSearch("tags names", 4);
		assertSearch("tags names tabs", 4);

		assertSearch("zz na", 0);
		assertSearch("zz name", 1);
		assertSearch("zz names", 3);
		assertSearch("zz ta", 0);
		assertSearch("zz tab", 1);
		assertSearch("zz tabs", 1);
		assertSearch("zz tag", 1);
		assertSearch("zz tags", 2);
	}

	protected void testStopwords() throws Exception {
		addDocument("Names of Tags");
		addDocument("More names of tags");
		addDocument(RandomTestUtil.randomString());

		assertSearch("of", 2);
		assertSearch("Names of tags", 2);
		assertSearch("tags names", 2);
	}

}