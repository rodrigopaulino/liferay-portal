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

package com.liferay.object.web.internal.object.entries.frontend.data.set.view.table.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.frontend.data.set.view.FDSView;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.deployer.ObjectDefinitionDeployer;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.util.LocalizedMapUtil;
import com.liferay.object.util.ObjectFieldUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermissionFactory;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermissionLogic;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @author Rodrigo Paulino
 */
@RunWith(Arquillian.class)
public class ObjectEntriesTableFDSViewTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_objectDefinition =
			_objectDefinitionLocalService.addCustomObjectDefinition(
				TestPropsValues.getUserId(),
				LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
				"A" + RandomTestUtil.randomString(), null, null,
				LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
				ObjectDefinitionConstants.SCOPE_COMPANY,
				Collections.singletonList(
					ObjectFieldUtil.createObjectField(
						"Text", "String", true, true, null,
						RandomTestUtil.randomString(), StringUtil.randomId(),
						false)));

		Bundle bundle = FrameworkUtil.getBundle(
			ObjectEntriesTableFDSViewTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		bundleContext.registerService(
			PortletResourcePermission.class,
			PortletResourcePermissionFactory.create(
				_objectDefinition.getResourceName(),
				new PortletResourcePermissionLogic() {

					@Override
					public Boolean contains(
						PermissionChecker permissionChecker, String s,
						Group group, String s1) {

						return true;
					}

				}),
			HashMapDictionaryBuilder.<String, Object>put(
				"com.liferay.object", "true"
			).put(
				"resource.name", _objectDefinition.getResourceName()
			).build());

		_objectDefinitionDeployer.deploy(_objectDefinition);

		_fdsView = _getFDSViewService(bundleContext);
	}

	@AfterClass
	public static void tearDownClass() {
		_objectDefinitionDeployer.undeploy(_objectDefinition);
	}

	@Test
	public void testGetFDSTableSchema() {
		Assert.assertNotNull(_fdsView.getFDSTableSchema(LocaleUtil.US));
	}

	private static FDSView _getFDSViewService(BundleContext bundleContext)
		throws Exception {

		ServiceReference<FDSView>[] serviceReferences =
			(ServiceReference<FDSView>[])bundleContext.getAllServiceReferences(
				FDSView.class.getName(),
				"(frontend.data.set.name=" + _objectDefinition.getPortletId() +
					StringPool.CLOSE_PARENTHESIS);

		return bundleContext.getService(serviceReferences[0]);
	}

	private static FDSView _fdsView;
	private static ObjectDefinition _objectDefinition;

	@Inject(
		filter = "component.name=*web.internal.deployer.ObjectDefinitionDeployerImpl"
	)
	private static ObjectDefinitionDeployer _objectDefinitionDeployer;

	@Inject
	private static ObjectDefinitionLocalService _objectDefinitionLocalService;

}