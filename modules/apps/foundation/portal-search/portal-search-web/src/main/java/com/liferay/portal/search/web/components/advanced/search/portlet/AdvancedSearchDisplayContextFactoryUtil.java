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

package com.liferay.portal.search.web.components.advanced.search.portlet;

import com.liferay.osgi.util.ServiceTrackerFactory;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Rodrigo Paulino
 */
public class AdvancedSearchDisplayContextFactoryUtil {

	public static AdvancedSearchDisplayContext create(
		RenderRequest renderRequest, RenderResponse renderResponse)
		throws Exception {

		AdvancedSearchDisplayContextFactory
			advancedSearchDisplayContextFactory = _serviceTracker.getService();

		return advancedSearchDisplayContextFactory.create(
			renderRequest, renderResponse);
	}

	private static final ServiceTracker
		<AdvancedSearchDisplayContextFactory, AdvancedSearchDisplayContextFactory>
			_serviceTracker = ServiceTrackerFactory.open(
				AdvancedSearchDisplayContextFactory.class);

}