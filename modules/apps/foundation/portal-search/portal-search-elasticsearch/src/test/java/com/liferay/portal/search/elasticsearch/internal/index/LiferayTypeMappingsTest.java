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

package com.liferay.portal.search.elasticsearch.internal.index;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture;
import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture.Index;
import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture.IndexName;
import com.liferay.portal.search.elasticsearch.internal.connection.LiferayIndexCreationHelper;

import java.util.Date;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author André de Oliveira
 */
public class LiferayTypeMappingsTest {

	@Before
	public void setUp() throws Exception {
		_elasticsearchFixture = new ElasticsearchFixture(
			LiferayTypeMappingsTest.class.getSimpleName());

		_elasticsearchFixture.setUp();

		_index = createIndex();
	}

	@After
	public void tearDown() throws Exception {
		_elasticsearchFixture.tearDown();
	}

	@Test
	public void testMatchMappingTypeDynamicTemplates() throws Exception {
		IndexRequestBuilder indexRequestBuilder = getIndexRequestBuilder();

		String fieldDDM1 = "ddm__keyword__" + RandomTestUtil.randomString();
		String fieldDDM2 = "ddm__keyword__" + RandomTestUtil.randomString();
		String fieldDDM3 = "ddm__keyword__" + RandomTestUtil.randomString();
		String fieldDDM4 = "ddm__keyword__" + RandomTestUtil.randomString();
		String fieldDDM5 = "ddm__keyword__" + RandomTestUtil.randomString();
		String fieldDDM6 = "ddm__keyword__" + RandomTestUtil.randomString();

		indexRequestBuilder.setSource(
			fieldDDM1, "", fieldDDM2, RandomTestUtil.randomString(), fieldDDM3,
			"2011-07-01T01:32:33", fieldDDM4, Long.valueOf("321231312321"),
			fieldDDM5, true, fieldDDM6, new Date());

		indexRequestBuilder.get();

		assertFieldType(fieldDDM1, "string");
		assertFieldType(fieldDDM2, "string");
		assertFieldType(fieldDDM3, "string");
		assertFieldType(fieldDDM4, "string");
		assertFieldType(fieldDDM5, "string");
		assertFieldType(fieldDDM6, "string");
	}

	@Test
	public void testPortugueseDynamicTemplatesMatchAnalyzers()
		throws Exception {

		IndexRequestBuilder indexRequestBuilder = getIndexRequestBuilder();

		String field_pt = RandomTestUtil.randomString() + "_pt";
		String field_pt_BR = RandomTestUtil.randomString() + "_pt_BR";
		String field_pt_PT = RandomTestUtil.randomString() + "_pt_PT";

		indexRequestBuilder.setSource(
			field_pt, RandomTestUtil.randomString(), field_pt_BR,
			RandomTestUtil.randomString(), field_pt_PT,
			RandomTestUtil.randomString());

		indexRequestBuilder.get();

		assertAnalyzer(field_pt, "portuguese");
		assertAnalyzer(field_pt_BR, "brazilian");
		assertAnalyzer(field_pt_PT, "portuguese");
	}

	@Rule
	public TestName testName = new TestName();

	protected void assertAnalyzer(String field, String analyzer)
		throws Exception {

		FieldMappingAssert.assertAnalyzer(
			analyzer, field, LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE,
			_index.getName(), _elasticsearchFixture.getIndicesAdminClient());
	}

	protected void assertFieldType(String field, String type) throws Exception {
		FieldMappingAssert.assertFieldType(
			type, field, LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE,
			_index.getName(), _elasticsearchFixture.getIndicesAdminClient());
	}

	protected Index createIndex() {
		return _elasticsearchFixture.createIndex(
			new IndexName(testName.getMethodName()),
			new LiferayIndexCreationHelper(
				_elasticsearchFixture.getIndicesAdminClient()));
	}

	protected IndexRequestBuilder getIndexRequestBuilder() {
		Client client = _elasticsearchFixture.getClient();

		IndexRequestBuilder indexRequestBuilder = client.prepareIndex(
			_index.getName(),
			LiferayTypeMappingsConstants.LIFERAY_DOCUMENT_TYPE);
		return indexRequestBuilder;
	}

	private ElasticsearchFixture _elasticsearchFixture;
	private Index _index;

}