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

package com.liferay.journal.util.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.mapping.constants.DDMStructureConstants;
import com.liferay.dynamic.data.mapping.io.DDMFormDeserializer;
import com.liferay.dynamic.data.mapping.io.DDMFormDeserializerDeserializeRequest;
import com.liferay.dynamic.data.mapping.io.DDMFormDeserializerDeserializeResponse;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMStructureLayout;
import com.liferay.dynamic.data.mapping.model.DDMStructureVersion;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.service.DDMStructureLayoutLocalServiceUtil;
import com.liferay.dynamic.data.mapping.storage.Field;
import com.liferay.dynamic.data.mapping.storage.Fields;
import com.liferay.dynamic.data.mapping.storage.StorageType;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestHelper;
import com.liferay.dynamic.data.mapping.util.DDM;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.util.JournalConverter;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.UnsecureSAXReaderUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.InputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Bruno Basto
 * @author Marcellus Tavares
 */
@RunWith(Arquillian.class)
public class JournalConverterUtilTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_enLocale = LocaleUtil.fromLanguageId("en_US");
		_ptLocale = LocaleUtil.fromLanguageId("pt_BR");

		_group = GroupTestUtil.addGroup();

		_ddmStructureTestHelper = new DDMStructureTestHelper(
			PortalUtil.getClassNameId(JournalArticle.class), _group);

		String definition = read("test-ddm-structure-all-fields.xml");

		DDMForm ddmForm = deserialize(definition);

		_ddmStructure = _ddmStructureTestHelper.addStructure(
			PortalUtil.getClassNameId(JournalArticle.class), null,
			"Test Structure", ddmForm, StorageType.DEFAULT.getValue(),
			DDMStructureConstants.TYPE_DEFAULT);
	}

	@Test
	public void testGetContentFromListField() throws Exception {
		Fields fields = new Fields();

		fields.put(getListField(_ddmStructure.getStructureId()));

		Field fieldsDisplayField = getFieldsDisplayField(
			_ddmStructure.getStructureId(), "list_INSTANCE_pcm9WPVX");

		fields.put(fieldsDisplayField);

		String expectedContent = read("test-journal-content-list-field.xml");

		String actualContent = _journalConverter.getContent(
			_ddmStructure, fields, _ddmStructure.getGroupId());

		assertEquals(expectedContent, actualContent);
	}

	@Test
	public void testGetContentFromMultiListField() throws Exception {
		Fields fields = new Fields();

		Field multiListField = getMultiListField(
			_ddmStructure.getStructureId());

		fields.put(multiListField);

		Field fieldsDisplayField = getFieldsDisplayField(
			_ddmStructure.getStructureId(), "multi_list_INSTANCE_9X5wVsSv");

		fields.put(fieldsDisplayField);

		String expectedContent = read(
			"test-journal-content-multi-list-field.xml");

		String actualContent = _journalConverter.getContent(
			_ddmStructure, fields, _ddmStructure.getGroupId());

		assertEquals(expectedContent, actualContent);
	}

	@Test
	public void testGetContentFromNestedFields() throws Exception {
		Fields fields = getNestedFields(_ddmStructure.getStructureId());

		String expectedContent = read("test-journal-content-nested-fields.xml");

		String actualContent = _journalConverter.getContent(
			_ddmStructure, fields, _ddmStructure.getGroupId());

		assertEquals(expectedContent, actualContent);
	}

	@Test
	public void testGetContentFromTextAreaField() throws Exception {
		Fields fields = new Fields();

		fields.put(getTextAreaField(_ddmStructure.getStructureId()));

		Field fieldsDisplayField = getFieldsDisplayField(
			_ddmStructure.getStructureId(), "text_area_INSTANCE_RFnJ1nCn");

		fields.put(fieldsDisplayField);

		String expectedContent = read(
			"test-journal-content-text-area-field.xml");

		String actualContent = _journalConverter.getContent(
			_ddmStructure, fields, _ddmStructure.getGroupId());

		assertEquals(expectedContent, actualContent);
	}

	@Test
	public void testGetContentFromTextBoxField() throws Exception {
		Fields fields = new Fields();

		fields.put(getTextBoxField(_ddmStructure.getStructureId()));

		Field fieldsDisplayField = getFieldsDisplayField(
			_ddmStructure.getStructureId(),
			"text_box_INSTANCE_ND057krU,text_box_INSTANCE_HvemvQgl," +
				"text_box_INSTANCE_enAnbvq6");

		fields.put(fieldsDisplayField);

		String expectedContent = read(
			"test-journal-content-text-box-repeatable-field.xml");

		String actualContent = _journalConverter.getContent(
			_ddmStructure, fields, _ddmStructure.getGroupId());

		assertEquals(expectedContent, actualContent);
	}

	@Test
	public void testGetContentFromTextField() throws Exception {
		Fields fields = new Fields();

		fields.put(getTextField(_ddmStructure.getStructureId()));

		Field fieldsDisplayField = getFieldsDisplayField(
			_ddmStructure.getStructureId(), "text_INSTANCE_bf4sdx6Q");

		fields.put(fieldsDisplayField);

		String expectedContent = read("test-journal-content-text-field.xml");

		String actualContent = _journalConverter.getContent(
			_ddmStructure, fields, _ddmStructure.getGroupId());

		assertEquals(expectedContent, actualContent);
	}

	@Test
	public void testGetFieldsFromContentWithListElement() throws Exception {
		Fields expectedFields = new Fields();

		expectedFields.put(getListField(_ddmStructure.getStructureId()));

		Field fieldsDisplayField = getFieldsDisplayField(
			_ddmStructure.getStructureId(),
			StringBundler.concat(
				"boolean_INSTANCE_", _ddmStructure.getStructureId(),
				",document_library_INSTANCE_", _ddmStructure.getStructureId(),
				",link_to_layout_INSTANCE_", _ddmStructure.getStructureId(),
				",text_area_INSTANCE_", _ddmStructure.getStructureId(),
				",multi_list_INSTANCE_", _ddmStructure.getStructureId(),
				",list_INSTANCE_pcm9WPVX,contactFieldSet_INSTANCE_",
				_ddmStructure.getStructureId(), ",contact_INSTANCE_",
				_ddmStructure.getStructureId(), ",phoneFieldSet_INSTANCE_",
				_ddmStructure.getStructureId(), ",phone_INSTANCE_",
				_ddmStructure.getStructureId(), ",ext_INSTANCE_",
				_ddmStructure.getStructureId(), ",text_INSTANCE_",
				_ddmStructure.getStructureId(), ",text_box_INSTANCE_",
				_ddmStructure.getStructureId(), ",image_1_INSTANCE_",
				_ddmStructure.getStructureId(), ",image_2_INSTANCE_",
				_ddmStructure.getStructureId(), ",image_3_INSTANCE_",
				_ddmStructure.getStructureId()));

		expectedFields.put(fieldsDisplayField);

		String content = read("test-journal-content-list-field.xml");

		Fields actualFields = _journalConverter.getDDMFields(
			_ddmStructure, content);

		Assert.assertEquals(expectedFields, actualFields);
	}

	@Test
	public void testGetFieldsFromContentWithMultiListElement()
		throws Exception {

		Fields expectedFields = new Fields();

		Field multiListField = getMultiListField(
			_ddmStructure.getStructureId());

		expectedFields.put(multiListField);

		Field fieldsDisplayField = getFieldsDisplayField(
			_ddmStructure.getStructureId(),
			StringBundler.concat(
				"boolean_INSTANCE_", _ddmStructure.getStructureId(),
				",document_library_INSTANCE_", _ddmStructure.getStructureId(),
				",link_to_layout_INSTANCE_", _ddmStructure.getStructureId(),
				",text_area_INSTANCE_", _ddmStructure.getStructureId(),
				",multi_list_INSTANCE_9X5wVsSv,list_INSTANCE_",
				_ddmStructure.getStructureId(), ",contactFieldSet_INSTANCE_",
				_ddmStructure.getStructureId(), ",contact_INSTANCE_",
				_ddmStructure.getStructureId(), ",phoneFieldSet_INSTANCE_",
				_ddmStructure.getStructureId(), ",phone_INSTANCE_",
				_ddmStructure.getStructureId(), ",ext_INSTANCE_",
				_ddmStructure.getStructureId(), ",text_INSTANCE_",
				_ddmStructure.getStructureId(), ",text_box_INSTANCE_",
				_ddmStructure.getStructureId(), ",image_1_INSTANCE_",
				_ddmStructure.getStructureId(), ",image_2_INSTANCE_",
				_ddmStructure.getStructureId(), ",image_3_INSTANCE_",
				_ddmStructure.getStructureId()));

		expectedFields.put(fieldsDisplayField);

		String content = read("test-journal-content-multi-list-field.xml");

		Fields actualFields = _journalConverter.getDDMFields(
			_ddmStructure, content);

		Assert.assertEquals(expectedFields, actualFields);
	}

	@Test
	public void testGetFieldsFromContentWithNestedElements() throws Exception {
		Fields expectedFields = getNestedFields(_ddmStructure.getStructureId());

		String content = read("test-journal-content-nested-fields.xml");

		Fields actualFields = _journalConverter.getDDMFields(
			_ddmStructure, content);

		Assert.assertEquals(expectedFields, actualFields);
	}

	@Test
	public void testGetFieldsFromContentWithParentStructuresElementsBackwardsCompatibility()
		throws Exception {

		String parentStructureDefinition = read(
			"test-ddm-structure-parent-structure.json");

		DDMStructure parentDDMStructure = _ddmStructureTestHelper.addStructure(
			PortalUtil.getClassNameId(JournalArticle.class), null,
			"Test Structure", jsonDeserialize(parentStructureDefinition),
			StorageType.DEFAULT.getValue(), DDMStructureConstants.TYPE_DEFAULT);

		DDMStructureVersion parentDDMStructureVersion =
			parentDDMStructure.getStructureVersion();

		DDMStructureLayout parentDDMStructureLayout =
			DDMStructureLayoutLocalServiceUtil.
				getStructureLayoutByStructureVersionId(
					parentDDMStructureVersion.getStructureVersionId());

		String childStructureDefinition = StringUtil.replace(
			read("test-ddm-structure-child-structure.json"),
			new String[] {"$DDM_STRUCTURE_ID", "$DDM_STRUCTURE_LAYOUT_ID"},
			new String[] {
				String.valueOf(parentDDMStructure.getStructureId()),
				String.valueOf(parentDDMStructureLayout.getStructureLayoutId())
			});

		DDMStructure childDDMStructure = _ddmStructureTestHelper.addStructure(
			PortalUtil.getClassNameId(JournalArticle.class), null,
			"Test Structure", jsonDeserialize(childStructureDefinition),
			StorageType.DEFAULT.getValue(), DDMStructureConstants.TYPE_DEFAULT);

		Fields expectedFields = new Fields();

		Field textField1 = new Field();

		textField1.setDDMStructureId(childDDMStructure.getStructureId());
		textField1.setDefaultLocale(_enLocale);
		textField1.setName("Text23i4");

		textField1.addValue(_enLocale, "Text 1");

		expectedFields.put(textField1);

		Field textField2 = new Field();

		textField2.setDDMStructureId(childDDMStructure.getStructureId());
		textField2.setDefaultLocale(_enLocale);
		textField2.setName("Textlmzq");

		textField2.addValue(_enLocale, "Text 2");

		expectedFields.put(textField2);

		Field fieldsDisplayField = getFieldsDisplayField(
			childDDMStructure.getStructureId(),
			"parentStructureFieldSet37599_INSTANCE_" +
				childDDMStructure.getStructureId() +
					",Text23i4_INSTANCE_ngkuwrmn,Textlmzq_INSTANCE_yxxxshhf");

		expectedFields.put(fieldsDisplayField);

		String content = read(
			"test-journal-content-parent-structure-fields.xml");

		Fields actualFields = _journalConverter.getDDMFields(
			childDDMStructure, content);

		Assert.assertEquals(expectedFields, actualFields);
	}

	@Test
	public void testGetFieldsFromContentWithUnlocalizedElement()
		throws Exception {

		Fields expectedFields = new Fields();

		Field textField = getTextField(_ddmStructure.getStructureId());

		textField.setValue(_ptLocale, textField.getValue(_enLocale));

		expectedFields.put(textField);

		Field fieldsDisplayField = getFieldsDisplayField(
			_ddmStructure.getStructureId(),
			StringBundler.concat(
				"boolean_INSTANCE_", _ddmStructure.getStructureId(),
				",document_library_INSTANCE_", _ddmStructure.getStructureId(),
				",link_to_layout_INSTANCE_", _ddmStructure.getStructureId(),
				",text_area_INSTANCE_", _ddmStructure.getStructureId(),
				",multi_list_INSTANCE_", _ddmStructure.getStructureId(),
				",list_INSTANCE_", _ddmStructure.getStructureId(),
				",contactFieldSet_INSTANCE_", _ddmStructure.getStructureId(),
				",contact_INSTANCE_", _ddmStructure.getStructureId(),
				",phoneFieldSet_INSTANCE_", _ddmStructure.getStructureId(),
				",phone_INSTANCE_", _ddmStructure.getStructureId(),
				",ext_INSTANCE_", _ddmStructure.getStructureId(),
				",text_INSTANCE_Okhyj7Ni,text_box_INSTANCE_",
				_ddmStructure.getStructureId(), ",image_1_INSTANCE_",
				_ddmStructure.getStructureId(), ",image_2_INSTANCE_",
				_ddmStructure.getStructureId(), ",image_3_INSTANCE_",
				_ddmStructure.getStructureId()));

		expectedFields.put(fieldsDisplayField);

		String content = read(
			"test-journal-content-text-unlocalized-field.xml");

		Fields actualFields = _journalConverter.getDDMFields(
			_ddmStructure, content);

		Assert.assertEquals(expectedFields, actualFields);
	}

	protected void assertEquals(
		DDMForm expectedDDMForm, DDMForm actualDDMForm) {

		Map<String, DDMFormField> expectedDDMFormFieldsMap =
			expectedDDMForm.getDDMFormFieldsMap(true);

		Map<String, DDMFormField> actualDDMFormFieldsMap =
			actualDDMForm.getDDMFormFieldsMap(true);

		for (Map.Entry<String, DDMFormField> expectedEntry :
				expectedDDMFormFieldsMap.entrySet()) {

			DDMFormField actualDDMFormField = actualDDMFormFieldsMap.get(
				expectedEntry.getKey());

			assertEquals(expectedEntry.getValue(), actualDDMFormField);
		}
	}

	protected void assertEquals(
		DDMFormField expectedDDMFormField, DDMFormField actualDDMFormField) {

		Assert.assertEquals(
			expectedDDMFormField.getDataType(),
			actualDDMFormField.getDataType());
		assertEquals(
			expectedDDMFormField.getDDMFormFieldOptions(),
			actualDDMFormField.getDDMFormFieldOptions());
		Assert.assertEquals(
			expectedDDMFormField.getIndexType(),
			actualDDMFormField.getIndexType());
		assertEquals(
			expectedDDMFormField.getLabel(), actualDDMFormField.getLabel());
		Assert.assertEquals(
			expectedDDMFormField.getName(), actualDDMFormField.getName());
		assertEquals(
			expectedDDMFormField.getStyle(), actualDDMFormField.getStyle());
		assertEquals(
			expectedDDMFormField.getTip(), actualDDMFormField.getTip());
		Assert.assertEquals(
			expectedDDMFormField.getType(), actualDDMFormField.getType());
		Assert.assertEquals(
			expectedDDMFormField.isMultiple(), actualDDMFormField.isMultiple());
		Assert.assertEquals(
			expectedDDMFormField.isRepeatable(),
			actualDDMFormField.isRepeatable());
		Assert.assertEquals(
			expectedDDMFormField.isRequired(), actualDDMFormField.isRequired());
	}

	protected void assertEquals(
		DDMFormFieldOptions expectedDDMFormFieldOptions,
		DDMFormFieldOptions actualDDMFormFieldOptions) {

		Set<String> expectedOptionValues =
			expectedDDMFormFieldOptions.getOptionsValues();

		for (String expectedOptionValue : expectedOptionValues) {
			LocalizedValue expectedOptionLabels =
				expectedDDMFormFieldOptions.getOptionLabels(
					expectedOptionValue);

			LocalizedValue actualOptionLabels =
				actualDDMFormFieldOptions.getOptionLabels(expectedOptionValue);

			assertEquals(expectedOptionLabels, actualOptionLabels);
		}
	}

	protected void assertEquals(
		LocalizedValue expectedLocalizedValue,
		LocalizedValue actualLocalizedValue) {

		Set<Locale> expectedAvailableLocales =
			expectedLocalizedValue.getAvailableLocales();

		for (Locale expectedLocale : expectedAvailableLocales) {
			String expectedValue = expectedLocalizedValue.getString(
				expectedLocale);

			String actualValue = actualLocalizedValue.getString(expectedLocale);

			Assert.assertEquals(expectedValue, actualValue);
		}
	}

	protected void assertEquals(String expectedContent, String actualContent)
		throws Exception {

		Map<String, Map<Locale, List<String>>> expectedFieldsMap = getFieldsMap(
			expectedContent);

		Map<String, Map<Locale, List<String>>> actualFieldsMap = getFieldsMap(
			actualContent);

		Assert.assertEquals(expectedFieldsMap, actualFieldsMap);
	}

	protected DDMForm deserialize(String content) {
		DDMFormDeserializerDeserializeRequest.Builder builder =
			DDMFormDeserializerDeserializeRequest.Builder.newBuilder(content);

		DDMFormDeserializerDeserializeResponse
			ddmFormDeserializerDeserializeResponse =
				_xsdDDMFormDeserializer.deserialize(builder.build());

		return ddmFormDeserializerDeserializeResponse.getDDMForm();
	}

	protected Field getFieldsDisplayField(long ddmStructureId, String value) {
		Field fieldsDisplayField = new Field();

		fieldsDisplayField.setDDMStructureId(ddmStructureId);
		fieldsDisplayField.setName(DDM.FIELDS_DISPLAY_NAME);
		fieldsDisplayField.setValue(value);

		return fieldsDisplayField;
	}

	protected Map<String, Map<Locale, List<String>>> getFieldsMap(
			String content)
		throws Exception {

		Map<String, Map<Locale, List<String>>> fieldsMap = new HashMap<>();

		Document document = UnsecureSAXReaderUtil.read(content);

		Element rootElement = document.getRootElement();

		List<Element> dynamicElementElements = rootElement.elements(
			"dynamic-element");

		for (Element dynamicElementElement : dynamicElementElements) {
			udpateFieldsMap(dynamicElementElement, fieldsMap);
		}

		return fieldsMap;
	}

	protected Field getListField(long ddmStructureId) {
		Field field = new Field();

		field.setDDMStructureId(ddmStructureId);
		field.setName("list");

		field.addValue(_enLocale, "[\"value 01\"]");

		return field;
	}

	protected Field getMultiListField(long ddmStructureId) {
		Field field = new Field();

		field.setDDMStructureId(ddmStructureId);
		field.setName("multi_list");

		field.addValue(_enLocale, "[\"value 01\",\"value 02\"]");

		return field;
	}

	protected Fields getNestedFields(long ddmStructureId) {
		Fields fields = new Fields();

		_addField(
			ddmStructureId, fields, "boolean",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("")
			).put(
				_ptLocale, Collections.singletonList("")
			).build());

		_addField(
			ddmStructureId, fields, "document_library",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("")
			).put(
				_ptLocale, Collections.singletonList("")
			).build());

		_addField(
			ddmStructureId, fields, "link_to_layout",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("")
			).put(
				_ptLocale, Collections.singletonList("")
			).build());

		_addField(
			ddmStructureId, fields, "text_area",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("")
			).put(
				_ptLocale, Collections.singletonList("")
			).build());

		_addField(
			ddmStructureId, fields, "multi_list",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("[\"\"]")
			).put(
				_ptLocale, Collections.singletonList("[\"\"]")
			).build());

		_addField(
			ddmStructureId, fields, "list",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("[\"\"]")
			).put(
				_ptLocale, Collections.singletonList("[\"\"]")
			).build());

		_addField(
			ddmStructureId, fields, "contact",
			HashMapBuilder.put(
				_enLocale, Arrays.asList("joe", "richard")
			).put(
				_ptLocale, Arrays.asList("joao", "ricardo")
			).build());

		_addField(
			ddmStructureId, fields, "phone",
			HashMapBuilder.put(
				_enLocale, Arrays.asList("123", "456")
			).put(
				_ptLocale, Arrays.asList("123", "456")
			).build());

		_addField(
			ddmStructureId, fields, "ext",
			HashMapBuilder.put(
				_enLocale, Arrays.asList("1", "2", "3", "4", "5")
			).put(
				_ptLocale, Arrays.asList("1", "2", "3", "4", "5")
			).build());

		_addField(
			ddmStructureId, fields, "text",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("")
			).put(
				_ptLocale, Collections.singletonList("")
			).build());

		_addField(
			ddmStructureId, fields, "text_box",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("")
			).put(
				_ptLocale, Collections.singletonList("")
			).build());

		_addField(
			ddmStructureId, fields, "image_1",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("")
			).put(
				_ptLocale, Collections.singletonList("")
			).build());

		_addField(
			ddmStructureId, fields, "image_2",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("")
			).put(
				_ptLocale, Collections.singletonList("")
			).build());

		_addField(
			ddmStructureId, fields, "image_3",
			HashMapBuilder.put(
				_enLocale, Collections.singletonList("")
			).put(
				_ptLocale, Collections.singletonList("")
			).build());

		Field fieldsDisplayField = new Field(
			ddmStructureId, DDM.FIELDS_DISPLAY_NAME,
			StringBundler.concat(
				"boolean_INSTANCE_YELSrniM,document_library_INSTANCE_HzKJrSts,",
				"link_to_layout_INSTANCE_eHQALxHa,text_area_INSTANCE_ucizquBv,",
				"multi_list_INSTANCE_oOUZHUcy,list_INSTANCE_RMYIbORN,",
				"contactFieldSet_INSTANCE_RJSkjdfi,",
				"contact_INSTANCE_RF3do1m5,phoneFieldSet_INSTANCE_zhglwgmk,",
				"phone_INSTANCE_QK6B0wK9,ext_INSTANCE_L67MPqQf,",
				"ext_INSTANCE_8uxzZl41,ext_INSTANCE_S58K861T,",
				"contactFieldSet_INSTANCE_3hACzXcE,contact_INSTANCE_CUeFxcrA,",
				"phoneFieldSet_INSTANCE_UgCyiQd3,phone_INSTANCE_lVTcTviF,",
				"ext_INSTANCE_cZalDSll,ext_INSTANCE_HDrK2Um5,",
				"text_INSTANCE_zWlZDoLc,text_box_INSTANCE_fmyemVKH,",
				"image_1_INSTANCE_xhdykzYh,image_2_INSTANCE_FViKyTea,",
				"image_3_INSTANCE_JVBHdcGZ"));

		fields.put(fieldsDisplayField);

		return fields;
	}

	protected Field getTextAreaField(long ddmStructureId) {
		Field field = new Field();

		field.setDDMStructureId(ddmStructureId);
		field.setName("text_area");

		field.addValue(_enLocale, "<p>Hello World!</p>");

		return field;
	}

	protected Field getTextBoxField(long ddmStructureId) {
		Field field = new Field();

		field.setDDMStructureId(ddmStructureId);
		field.setName("text_box");

		List<Serializable> enValues = new ArrayList<>();

		enValues.add("one");
		enValues.add("two");
		enValues.add("three");

		field.addValues(_enLocale, enValues);

		List<Serializable> ptValues = new ArrayList<>();

		ptValues.add("um");
		ptValues.add("dois");
		ptValues.add("tres");

		field.addValues(_ptLocale, ptValues);

		return field;
	}

	protected Field getTextField(long ddmStructureId) {
		Field field = new Field();

		field.setDDMStructureId(ddmStructureId);
		field.setName("text");

		field.addValue(_enLocale, "one");
		field.addValue(_ptLocale, "um");

		return field;
	}

	protected List<String> getValues(
		Map<Locale, List<String>> valuesMap, Locale locale) {

		return valuesMap.computeIfAbsent(locale, key -> new ArrayList<>());
	}

	protected DDMForm jsonDeserialize(String content) {
		DDMFormDeserializerDeserializeRequest.Builder builder =
			DDMFormDeserializerDeserializeRequest.Builder.newBuilder(content);

		DDMFormDeserializerDeserializeResponse
			ddmFormDeserializerDeserializeResponse =
				_jsonDDMFormDeserializer.deserialize(builder.build());

		return ddmFormDeserializerDeserializeResponse.getDDMForm();
	}

	protected String read(String fileName) throws Exception {
		Class<?> clazz = getClass();

		ClassLoader classLoader = clazz.getClassLoader();

		InputStream inputStream = classLoader.getResourceAsStream(
			"com/liferay/journal/dependencies/" + fileName);

		return StringUtil.read(inputStream);
	}

	protected void udpateFieldsMap(
		Element dynamicElementElement,
		Map<String, Map<Locale, List<String>>> fieldsMap) {

		List<Element> childrenDynamicElementElements =
			dynamicElementElement.elements("dynamic-element");

		for (Element childrenDynamicElementElement :
				childrenDynamicElementElements) {

			udpateFieldsMap(childrenDynamicElementElement, fieldsMap);
		}

		String name = dynamicElementElement.attributeValue("name");

		Map<Locale, List<String>> valuesMap = fieldsMap.computeIfAbsent(
			name, key -> new HashMap<>());

		List<Element> dynamicContentElements = dynamicElementElement.elements(
			"dynamic-content");

		for (Element dynamicContentElement : dynamicContentElements) {
			Locale locale = LocaleUtil.fromLanguageId(
				dynamicContentElement.attributeValue("language-id"));

			List<String> values = getValues(valuesMap, locale);

			List<Element> optionElements = dynamicContentElement.elements(
				"option");

			if (!optionElements.isEmpty()) {
				for (Element optionElement : optionElements) {
					values.add(optionElement.getText());
				}
			}
			else {
				values.add(dynamicContentElement.getText());
			}
		}
	}

	private void _addField(
		long ddmStructureId, Fields fields, String name,
		HashMap<Locale, List<String>> values) {

		Field field = new Field();

		field.setDDMStructureId(ddmStructureId);
		field.setName(name);

		for (Map.Entry<Locale, List<String>> entry : values.entrySet()) {
			for (String value : entry.getValue()) {
				field.addValue(entry.getKey(), value);
			}
		}

		fields.put(field);
	}

	@Inject(filter = "ddm.form.deserializer.type=json")
	private static DDMFormDeserializer _jsonDDMFormDeserializer;

	@Inject(filter = "ddm.form.deserializer.type=xsd")
	private static DDMFormDeserializer _xsdDDMFormDeserializer;

	private DDMStructure _ddmStructure;
	private DDMStructureTestHelper _ddmStructureTestHelper;
	private Locale _enLocale;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private JournalConverter _journalConverter;

	private Locale _ptLocale;

}