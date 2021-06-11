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

import {ClayInput} from '@clayui/form';
import {useFormState} from 'data-engine-js-components-web';
import {SettingsContext} from 'dynamic-data-mapping-form-builder';
import React, {useEffect, useState} from 'react';

import {FieldBase} from '../FieldBase/ReactFieldBase.es';

const parse = (value, defaultValue) => {
	try {
		return JSON.parse(value);
	} catch (e) {
		return defaultValue !== undefined ? defaultValue : {};
	}
};

const getClassNameBasedOnLayout = (layout, visibleField) => {
	return layout?.includes('two-columns') && visibleField !== 'address'
		? 'col-md-6'
		: 'col-md-12';
};

const isEmpty = (object) => {
	return object && Object.keys(object).length === 0;
};

const Field = ({
	disabled,
	editingLanguageId,
	label,
	name,
	onBlur,
	onChange,
	onFocus,
	parsedValue,
	placeholder,
	readOnly,
	visibleField,
	...otherProps
}) => {
	return (
		<FieldBase
			{...otherProps}
			label={label[editingLanguageId] ?? label}
			name={name}
			readOnly={readOnly}
		>
			<ClayInput
				className="ddm-field-text"
				dir={Liferay.Language.direction[editingLanguageId]}
				disabled={disabled}
				name={name}
				onBlur={onBlur}
				onChange={(event) => {
					const value = !isEmpty(parsedValue)
						? {
								...parsedValue,
								[visibleField]: event.target.value,
						  }
						: {[visibleField]: event.target.value};
					onChange({
						target: {
							value: JSON.stringify({
								...value,
							}),
						},
					});
				}}
				onFocus={onFocus}
				placeholder={placeholder}
				type="text"
				value={!isEmpty(parsedValue) ? parsedValue[visibleField] : ''}
			/>
		</FieldBase>
	);
};

const Main = ({
	label,
	labels,
	layout,
	name,
	onBlur,
	onChange,
	onFocus,
	placeholder,
	readOnly,
	settingsContext,
	value,
	visibleFields,
	...otherProps
}) => {
	const [availableLabels, setAvailableLabels] = useState();
	const [availableVisibleFields, setAvailableVisibleFields] = useState([]);
	const currentVisibleFields = Array.isArray(visibleFields)
		? visibleFields
		: parse(visibleFields, []);
	const currentLayout = Array.isArray(layout) ? layout : parse(layout, []);

	const {editingLanguageId} = useFormState();
	const parsedValue = parse(value, {});

	useEffect(() => {
		if (settingsContext) {
			const options = SettingsContext.getSettingsContextProperty(
				settingsContext,
				'visibleFields',
				'options'
			);

			setAvailableLabels(
				options.reduce((accumulator, currentOption) => {
					return {
						...accumulator,
						[currentOption.value]: currentOption.label,
					};
				}, {})
			);

			setAvailableVisibleFields(options.map((option) => option.value));
		} else {
			setAvailableLabels(labels ?? {});
			setAvailableVisibleFields(currentVisibleFields);
		}
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	return (
		<div>
			<Field
				{...otherProps}
				className="col-md-12"
				disabled={readOnly}
				editingLanguageId={editingLanguageId}
				label={label}
				name={name}
				onBlur={onBlur}
				onChange={onChange}
				onFocus={onFocus}
				parsedValue={parsedValue}
				placeholder={placeholder}
				readOnly={readOnly}
				visibleField="place"
			/>
			<div className="row">
				{availableVisibleFields.length > 0 &&
					availableVisibleFields.map((visibleField) => {
						if (currentVisibleFields.includes(visibleField)) {
							const visibleFieldName = name + '#' + visibleField;
							const className = getClassNameBasedOnLayout(
								currentLayout,
								visibleField
							);
							return (
								<div className={className}>
									<Field
										{...otherProps}
										disabled={readOnly}
										editingLanguageId={editingLanguageId}
										key={visibleFieldName}
										label={availableLabels[visibleField]}
										name={visibleFieldName}
										onBlur={onBlur}
										onChange={onChange}
										onFocus={onFocus}
										parsedValue={parsedValue}
										placeholder={placeholder}
										readOnly={readOnly}
										visibleField={visibleField}
									/>
								</div>
							);
						}
					})}
			</div>
			<ClayInput name={name} type="hidden" value={value} />
		</div>
	);
};

Main.displayName = 'SearchLocation';

export default Main;
