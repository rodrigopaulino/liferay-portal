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

export const VALIDATIONS = {
	numeric: [
		{
			label: Liferay.Language.get('is-equal-to'),
			name: 'eq',
			parameterMessage: Liferay.Language.get('number-placeholder'),
			regex: /^(.+)==(\d+\.?\d*)?$/,
			template: '{name} == {parameter}',
		},
		{
			label: Liferay.Language.get('is-greater-than-or-equal-to'),
			name: 'gteq',
			parameterMessage: Liferay.Language.get('number-placeholder'),
			regex: /^(.+)>=(\d+\.?\d*)?$/,
			template: '{name} >= {parameter}',
		},
		{
			label: Liferay.Language.get('is-greater-than'),
			name: 'gt',
			parameterMessage: Liferay.Language.get('number-placeholder'),
			regex: /^(.+)>(\d+\.?\d*)?$/,
			template: '{name} > {parameter}',
		},
		{
			label: Liferay.Language.get('is-not-equal-to'),
			name: 'neq',
			parameterMessage: Liferay.Language.get('number-placeholder'),
			regex: /^(.+)!=(\d+\.?\d*)?$/,
			template: '{name} != {parameter}',
		},
		{
			label: Liferay.Language.get('is-less-than-or-equal-to'),
			name: 'lteq',
			parameterMessage: Liferay.Language.get('number-placeholder'),
			regex: /^(.+)<=(\d+\.?\d*)?$/,
			template: '{name} <= {parameter}',
		},
		{
			label: Liferay.Language.get('is-less-than'),
			name: 'lt',
			parameterMessage: Liferay.Language.get('number-placeholder'),
			regex: /^(.+)<(\d+\.?\d*)?$/,
			template: '{name} < {parameter}',
		},
	],
	string: [
		{
			label: Liferay.Language.get('contains'),
			name: 'contains',
			parameterMessage: Liferay.Language.get('text'),
			regex: /^contains\((.+), "(.*)"\)$/,
			template: 'contains({name}, "{parameter}")',
		},
		{
			label: Liferay.Language.get('does-not-contain'),
			name: 'doesNotContain',
			parameterMessage: Liferay.Language.get('text'),
			regex: /^NOT\(contains\((.+), "(.*)"\)\)$/,
			template: 'NOT(contains({name}, "{parameter}"))',
		},
		{
			label: Liferay.Language.get('is-not-url'),
			name: 'isNotURL',
			parameterMessage: '',
			regex: /^NOT\(isURL\((.+)\)\)$/,
			template: 'NOT(isURL({name}))',
		},
		{
			label: Liferay.Language.get('is-not-email'),
			name: 'isNotEmail',
			parameterMessage: '',
			regex: /^NOT\(isEmailAddress\((.+)\)\)$/,
			template: 'NOT(isEmailAddress({name}))',
		},
		{
			label: Liferay.Language.get('does-not-match'),
			name: 'doesNotMatch',
			parameterMessage: Liferay.Language.get('regular-expression'),
			regex: /^NOT\(match\((.+), "(.*)"\)\)$/,
			template: 'NOT(match({name}, "{parameter}"))',
		},
	],
};

export default VALIDATIONS;
