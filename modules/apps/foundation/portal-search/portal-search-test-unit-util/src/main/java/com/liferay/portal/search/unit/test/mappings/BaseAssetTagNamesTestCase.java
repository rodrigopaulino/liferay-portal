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

import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.query.AssetTagNamesQueryContributor;
import com.liferay.portal.kernel.test.IdempotentRetryAssert;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.unit.test.BaseIndexingTestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Assume;

/**
 * @author André de Oliveira
 */
public abstract class BaseAssetTagNamesTestCase extends BaseIndexingTestCase {

	protected void addDocument(final String... assetTagNames) throws Exception {
		addDocument(
			new DocumentCreationHelper() {

				@Override
				public void populate(Document document) {
					document.addText(Field.ASSET_TAG_NAMES, assetTagNames);
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
		BooleanQuery booleanQuery = new BooleanQueryImpl();

		AssetTagNamesQueryContributor assetTagNamesQueryContributor =
			new AssetTagNamesQueryContributor();

		assetTagNamesQueryContributor.contribute(keywords, booleanQuery);

		return booleanQuery;
	}

	protected boolean isPhrasePrefixesWithMultipleTermsSupported() {
		return true;
	}

	protected void testBasicWordMatches() throws Exception {
		addDocument("Nametag");
		addDocument("NA-META-G");
		addDocument("Tag Name");
		addDocument("TAG1");
		addDocument(RandomTestUtil.randomString());

		assertSearch("g", 1);
		assertSearch("meta", 1);
		assertSearch("\"meta\"", 1);
		assertSearch("name", 2);
		assertSearch("NaMe*", 2);
		assertSearch("nameTAG", 1);
		assertSearch("tag1", 1);
		assertSearch("\"tag1\"", 1);

		assertSearch("ame", 0);
		assertSearch("METAG", 0);
		assertSearch("METAG*", 0);

		assertSearch("*METAG", 0);
		assertSearch("tag2", 0);
		assertSearch("1", 0);

		assertSearch("META G", 1);
		assertSearch("META-G", 1);
		assertSearch("name tag", 1);
		assertSearch("name-tag", 1);
		assertSearch("NA-META-G", 1);
		assertSearch("nA mEtA g", 1);
		assertSearch("\"na, meta, g\"", 1);
		assertSearch("tag name", 1);
		assertSearch("\"Tag (Name)\"", 1);
		assertSearch("tag 1", 1);
		assertSearch("tag(142857)", 1);

		assertSearch("\"NA G\"", 0);
		assertSearch("\"Name Tag\"", 0);
		assertSearch("\"tag 1\"", 0);
	}

	protected void testNumbers() throws Exception {
		addDocument("Nametag5");
		addDocument("2Tagname");
		addDocument("LETTERS ONLY");

		assertSearch("2", 1);
		assertSearch("2Tag", 1);
		assertSearch("2Tagname", 1);
		assertSearch("Name", 1);
		assertSearch("Nametag", 1);
		assertSearch("Nametag5", 1);

		assertSearch("5", 0);
		assertSearch("5Nametag", 0);
		assertSearch("5Tagname", 0);
		assertSearch("Nametag2", 0);
		assertSearch("Nametag9", 0);
		assertSearch("Tagname", 0);
		assertSearch("Tagname5", 0);
		assertSearch("Tagname2", 0);
		assertSearch("Tagname9", 0);
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

	protected void testPhrases() throws Exception {
		addDocument("Names of Tags");
		addDocument("More names of tags here");
		addDocument(RandomTestUtil.randomString());

		assertSearch("\"names of tags\"", 2);
		assertSearch("\"TAGS\"", 2);
		assertSearch("\"more\"", 1);
		assertSearch("\"More\"", 1);
		assertSearch("\"Tags here\"", 1);
		assertSearch("\"HERE\"", 1);
	}

	protected void testPrefixes() throws Exception {
		addDocument("Nametag");
		addDocument("NA-META-G");
		addDocument("Tag Name");
		addDocument("TAG1");
		addDocument(RandomTestUtil.randomString());

		assertSearch("me", 1);
		assertSearch("me*", 1);
		assertSearch("*me", 1);
		assertSearch("*me*", 1);
		assertSearch("met", 1);
		assertSearch("*met*", 1);
		assertSearch("Na", 3);
		assertSearch("NA*", 3);
		assertSearch("*NA", 3);
		assertSearch("*NA*", 3);
		assertSearch("Namet", 1);
		assertSearch("namet*", 1);
		assertSearch("*namet", 1);
		assertSearch("*namet*", 1);
		assertSearch("Ta", 2);
		assertSearch("Ta*", 2);
		assertSearch("*Ta", 2);
		assertSearch("*Ta*", 2);
		assertSearch("tag", 2);
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