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

package com.liferay.dynamic.data.mapping.form.evaluator.internal.function;

import com.liferay.dynamic.data.mapping.expression.DDMExpressionFunction;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionParameterAccessor;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionParameterAccessorAware;
import com.liferay.dynamic.data.mapping.form.evaluator.internal.function.util.DateFunctionsUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Carolina Barbosa
 */
public class PastDatesFunction
	implements DDMExpressionFunction.Function2<Object, Object, Boolean>,
			   DDMExpressionParameterAccessorAware {

	public static final String NAME = "pastDates";

	@Override
	public Boolean apply(Object object1, Object object2) {
		if ((_ddmExpressionParameterAccessor == null) ||
			(_ddmExpressionParameterAccessor.getLocale() == null) ||
			Validator.isNull(object1) || Validator.isNull(object2)) {

			return false;
		}

		return DateFunctionsUtil.isPastDate(
			object1.toString(), object2.toString(),
			_ddmExpressionParameterAccessor.getLocale());
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void setDDMExpressionParameterAccessor(
		DDMExpressionParameterAccessor ddmExpressionParameterAccessor) {

		_ddmExpressionParameterAccessor = ddmExpressionParameterAccessor;
	}

	private DDMExpressionParameterAccessor _ddmExpressionParameterAccessor;

}