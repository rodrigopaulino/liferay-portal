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

package com.liferay.journal.web.internal.portlet.action;

import com.liferay.data.engine.rest.dto.v2_0.DataDefinition;
import com.liferay.data.engine.rest.dto.v2_0.DataLayout;
import com.liferay.data.engine.rest.resource.v2_0.DataDefinitionResource;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvoker;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.File;

import java.util.concurrent.Callable;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Rodrigo Paulino
 */
@PrepareForTest(
	{
		DataDefinition.class, DataLayout.class, SessionErrors.class,
		SessionMessages.class, FileUtil.class
	}
)
@RunWith(PowerMockRunner.class)
public class ImportDataDefinitionMVCActionCommandTest extends PowerMockito {

	@Before
	public void setUp() throws Exception {
		_setUpDataDefinition();
		_setUpDataDefinitionResourceFactory();
		_setUpDataLayout();
		_setUpJSONFactory();
		_setUpPropsUtil();
		_setUpTransactionInvokerUtil();
	}

	@Test
	public void testProcessActionWithoutName() throws Exception {
		_mockFileUtil("valid-data-definition.json");
		_mockPortal();

		ActionRequest actionRequest = _mockActionRequest(null);

		_mockSessionErrors(actionRequest);
		_mockSessionMessages(actionRequest);

		_importDataDefinitionMVCActionCommand.processAction(
			actionRequest, null);

		verifyStatic();

		SessionMessages.add(
			actionRequest,
			"portletId" +
				SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);

		verifyStatic();

		SessionErrors.add(actionRequest, "importDataDefinitionErrorMessage");
	}

	@Test
	public void testProcessActionWithValidDataDefinitionAndName()
		throws Exception {

		_mockFileUtil("valid-data-definition.json");
		_mockPortal();

		ActionRequest actionRequest = _mockActionRequest("Imported Structure");

		_mockSessionMessages(actionRequest);

		_importDataDefinitionMVCActionCommand.processAction(
			actionRequest, null);

		verifyStatic();

		SessionMessages.add(
			actionRequest, "importDataDefinitionSuccessMessage");
	}

	private ActionRequest _mockActionRequest(String name) {
		ActionRequest actionRequest = mock(ActionRequest.class);

		when(
			actionRequest.getAttribute(Matchers.eq(WebKeys.THEME_DISPLAY))
		).thenReturn(
			new ThemeDisplay()
		);

		when(
			actionRequest.getParameter(Matchers.eq("name"))
		).thenReturn(
			name
		);

		return actionRequest;
	}

	private void _mockFileUtil(String fileName) throws Exception {
		mockStatic(FileUtil.class);

		Class<?> clazz = getClass();

		when(
			FileUtil.read(Matchers.any(File.class))
		).thenReturn(
			StringUtil.read(
				clazz.getClassLoader(),
				"com/liferay/journal/web/internal/portlet/action/dependencies" +
					"/" + fileName)
		);
	}

	private void _mockPortal() {
		Portal portal = mock(Portal.class);

		when(
			portal.getPortletId(Matchers.any(PortletRequest.class))
		).thenReturn(
			"portletId"
		);

		UploadPortletRequest uploadPortletRequest = _mockUploadPortletRequest();

		when(
			portal.getUploadPortletRequest(Matchers.any(PortletRequest.class))
		).thenReturn(
			uploadPortletRequest
		);

		ReflectionTestUtil.setFieldValue(
			_importDataDefinitionMVCActionCommand, "_portal", portal);
	}

	private void _mockSessionErrors(PortletRequest portletRequest)
		throws Exception {

		mockStatic(SessionErrors.class);

		doNothing().when(
			SessionErrors.class, "add", portletRequest,
			"importDataDefinitionErrorMessage");
	}

	private void _mockSessionMessages(PortletRequest portletRequest)
		throws Exception {

		mockStatic(SessionMessages.class);

		doNothing().when(
			SessionMessages.class, "add", portletRequest,
			"importDataDefinitionSuccessMessage");

		doNothing().when(
			SessionMessages.class, "add", portletRequest,
			"portletId" +
				SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
	}

	private UploadPortletRequest _mockUploadPortletRequest() {
		UploadPortletRequest uploadPortletRequest = mock(
			UploadPortletRequest.class);

		File file = mock(File.class);

		when(
			uploadPortletRequest.getFile(Matchers.eq("jsonFile"))
		).thenReturn(
			file
		);

		return uploadPortletRequest;
	}

	private void _setUpDataDefinition() {
		mockStatic(DataDefinition.class);

		DataDefinition dataDefinition = mock(DataDefinition.class);

		when(
			DataDefinition.toDTO(Matchers.anyString())
		).thenReturn(
			dataDefinition
		);
	}

	private void _setUpDataDefinitionResourceFactory() throws Exception {
		DataDefinitionResource.Factory dataDefinitionResourceFactory = mock(
			DataDefinitionResource.Factory.class);

		DataDefinitionResource.Builder
			dataDefinitionResourceBuilder = _mockDataDefinitionResourceBuilder();

		when(
			dataDefinitionResourceFactory.create()
		).thenReturn(
			dataDefinitionResourceBuilder
		);

		ReflectionTestUtil.setFieldValue(
			_importDataDefinitionMVCActionCommand,
			"_dataDefinitionResourceFactory", dataDefinitionResourceFactory);
	}

	private DataDefinitionResource.Builder _mockDataDefinitionResourceBuilder() throws Exception {
		DataDefinitionResource.Builder dataDefinitionResourceBuilder = mock(
			DataDefinitionResource.Builder.class);

		DataDefinitionResource
			dataDefinitionResource = _mockDataDefinitionResource();

		when(
			dataDefinitionResourceBuilder.build()
		).thenReturn(
			dataDefinitionResource
		);

		when(
			dataDefinitionResourceBuilder.user(Matchers.any())
		).thenReturn(
			dataDefinitionResourceBuilder
		);

		return dataDefinitionResourceBuilder;
	}

	private DataDefinitionResource _mockDataDefinitionResource()
		throws Exception {
		DataDefinitionResource dataDefinitionResource = mock(
			DataDefinitionResource.class);

		when(
			dataDefinitionResource.postSiteDataDefinitionByContentType(
				Matchers.anyLong(), Matchers.anyString(),
				Matchers.any(DataDefinition.class))
		).then(
			null
		);

		return dataDefinitionResource;
	}

	private void _setUpDataLayout() {
		mockStatic(DataLayout.class);

		when(
			DataLayout.toDTO(Matchers.anyString())
		).thenReturn(
			mock(DataLayout.class)
		);
	}

	private void _setUpJSONFactory() {
		ReflectionTestUtil.setFieldValue(
			_importDataDefinitionMVCActionCommand, "_jsonFactory",
			new JSONFactoryImpl());
	}

	private void _setUpPropsUtil() {
		PropsUtil.setProps(mock(Props.class));
	}

	private void _setUpTransactionInvokerUtil() {
		TransactionInvokerUtil transactionInvokerUtil =
			new TransactionInvokerUtil();

		transactionInvokerUtil.setTransactionInvoker(
			new TransactionInvoker() {

				@Override
				public <T> T invoke(
						TransactionConfig transactionConfig,
						Callable<T> callable)
					throws Throwable {

					return callable.call();
				}

			});
	}

	private final ImportDataDefinitionMVCActionCommand
		_importDataDefinitionMVCActionCommand =
			new ImportDataDefinitionMVCActionCommand();

}