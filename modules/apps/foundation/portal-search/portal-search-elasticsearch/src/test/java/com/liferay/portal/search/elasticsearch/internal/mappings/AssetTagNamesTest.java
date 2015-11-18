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

package com.liferay.portal.search.elasticsearch.internal.mappings;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.elasticsearch.internal.ElasticsearchIndexingFixture;
import com.liferay.portal.search.elasticsearch.internal.connection.LiferayIndexCreationHelperFactory;
import com.liferay.portal.search.unit.test.BaseIndexingTestCase;
import com.liferay.portal.search.unit.test.IndexingFixture;
import com.liferay.portal.search.unit.test.mappings.BaseAssetTagNamesTestCase;

import org.junit.Test;

/**
 * @author André de Oliveira
 */
public class AssetTagNamesTest extends BaseAssetTagNamesTestCase {

	@Override
	@Test
	public void testBasicWordMatches() throws Exception {
		super.testBasicWordMatches();
	}

	@Override
	@Test
	public void testNumbers() throws Exception {
		super.testNumbers();
	}

	@Override
	@Test
	public void testPhrasePrefixesWithMultipleTerms() throws Exception {
		super.testPhrasePrefixesWithMultipleTerms();
	}

	@Override
	@Test
	public void testPhrases() throws Exception {
		addDocument("Names of Tags");
		addDocument("More names of tags here");
		addDocument(RandomTestUtil.randomString());

		assertSearch("\"Tags here\"", 1);

		super.testPhrases();
	}

	@Override
	@Test
	public void testPrefixes() throws Exception {
		super.testPrefixes();
	}

	@Override
	@Test
	public void testPrefixesWithMultipleTerms() throws Exception {
		super.testPrefixesWithMultipleTerms();
	}

	@Override
	@Test
	public void testStopwords() throws Exception {
		super.testStopwords();
	}

	@Override
	protected IndexingFixture createIndexingFixture() throws Exception {
		return new ElasticsearchIndexingFixture(
			AssetTagNamesTest.class.getSimpleName(),
			BaseIndexingTestCase.COMPANY_ID,
			new LiferayIndexCreationHelperFactory());
	}

}