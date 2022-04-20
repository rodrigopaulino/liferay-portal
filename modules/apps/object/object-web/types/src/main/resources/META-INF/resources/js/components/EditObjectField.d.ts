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

/// <reference types="react" />

import {IFeatureFlag} from 'data-engine-js-components-web';
import './EditObjectField.scss';
export declare function EditObjectField({
	isApproved,
	objectField: initialValues,
	objectFieldTypes,
	readOnly,
}: IEditObjectFieldProps): JSX.Element;
export default function main({
	featureFlags,
	...otherProps
}: IProps): JSX.Element;
interface IEditObjectFieldProps {
	isApproved: boolean;
	objectField: ObjectField;
	objectFieldTypes: ObjectFieldType[];
	readOnly: boolean;
}
interface IProps extends IEditObjectFieldProps, IFeatureFlag {}
export {};
