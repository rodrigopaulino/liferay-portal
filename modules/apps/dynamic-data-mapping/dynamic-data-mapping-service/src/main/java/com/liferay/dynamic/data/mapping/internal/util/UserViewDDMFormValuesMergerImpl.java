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

package com.liferay.dynamic.data.mapping.internal.util;

import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.util.DDMFormValuesMerger;
import org.osgi.service.component.annotations.Component;

import java.util.Locale;

/**
 * @author Rodrigo Paulino
 */
@Component(
	immediate = true,
	property = "dynamic.data.mapping.form.values.merger.type=userView",
	service = DDMFormValuesMerger.class
)
public class UserViewDDMFormValuesMergerImpl
	extends DDMFormValuesMergerBaseImpl {

	@Override
	protected void mergeValue(Value newValue, Value existingValue) {
		if (existingValue == null) {
			return;
		}

		for (Locale locale : existingValue.getAvailableLocales()) {
			String value = newValue.getString(locale);

			if (value == null) {
				newValue.addString(locale, existingValue.getString(locale));
			}
		}
	}

}