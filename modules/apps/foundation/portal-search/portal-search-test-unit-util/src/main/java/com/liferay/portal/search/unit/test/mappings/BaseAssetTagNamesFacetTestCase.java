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

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.facet.AssetTagNamesFacetFactory;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.search.generic.MatchAllQuery;
import com.liferay.portal.kernel.test.IdempotentRetryAssert;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.unit.test.BaseIndexingTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

/**
 * @author André de Oliveira
 */
public abstract class BaseAssetTagNamesFacetTestCase
	extends BaseIndexingTestCase {

	protected void addDocument(final String... assetTagNames) throws Exception {
		addDocument(
			new DocumentCreationHelper() {

				@Override
				public void populate(Document document) {
					document.addText(Field.ASSET_TAG_NAMES, assetTagNames);
				}

			});
	}

	protected void assertFacet(
			SearchContext searchContext, Query query, String facetName,
			String expected)
		throws Exception {

		search(searchContext, query);

		Map<String, Facet> facets = searchContext.getFacets();

		Facet facet = facets.get(facetName);

		FacetCollector facetCollector = facet.getFacetCollector();

		Assert.assertNotNull(facetCollector);

		List<String> buckets = new ArrayList<>();

		for (TermCollector termCollector : facetCollector.getTermCollectors()) {
			buckets.add(
				termCollector.getTerm() + "->" + termCollector.getFrequency());
		}

		Collections.sort(buckets);

		Assert.assertEquals(expected, StringUtil.merge(buckets));
	}

	protected void assertFacet(final String expected) throws Exception {
		final SearchContext searchContext = createSearchContext();

		AssetTagNamesFacetFactory assetTagNamesFacetFactory =
			new AssetTagNamesFacetFactory();

		Facet facet = assetTagNamesFacetFactory.newInstance(searchContext);

		final String facetName = facet.getFieldName();

		searchContext.addFacet(facet);

		final Query query = createQuery();

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					assertFacet(searchContext, query, facetName, expected);

					return null;
				}

			});
	}

	protected Query createQuery() {
		return new MatchAllQuery();
	}

	protected void testFacets() throws Exception {
		addDocument("green-blue tag");
		addDocument("green-blue tag", "red tag");
		addDocument("tag");

		assertFacet("green-blue tag->2,red tag->1,tag->1");
	}

}