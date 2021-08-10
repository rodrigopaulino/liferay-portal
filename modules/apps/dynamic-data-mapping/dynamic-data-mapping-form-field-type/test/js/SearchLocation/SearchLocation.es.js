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

import '@testing-library/jest-dom/extend-expect';
import {cleanup, render} from '@testing-library/react';
import {PageProvider} from 'data-engine-js-components-web';
import React from 'react';

import SearchLocation from '../../../src/main/resources/META-INF/resources/SearchLocation/SearchLocation.es';

const globalLanguageDirection = Liferay.Language.direction;

const SearchLocationWithProvider = (props) => (
	<PageProvider value={{editingLanguageId: 'en_US'}}>
		<SearchLocation {...props} />
	</PageProvider>
);

const defaultConfig = {
	googlePlacesAPIKey: '',
	label: {
		en_US: 'Search Location',
	},
	labels: {
		address: 'Address',
		city: 'City',
		country: 'Country',
		['postal-code']: 'Postal Code',
		state: 'State',
	},
	layout: ['one-column'],
	name: 'test_search_location_field',
	onChange: jest.fn(),
	readOnly: false,
	viewMode: true,
	visibleFields: ['address', 'city', 'country', 'postal-code', 'state'],
};

const hasAllFields = (getByLabelText, labels) => {
	Object.values(labels).forEach((label) => {
		if (!getByLabelText(label)) {
			return false;
		}
	});

	return true;
};

const hasAllFieldsInCorrectOrder = (
	correctFieldsOrderByName,
	elementFields
) => {
	let isInOrder = true;
	const elementFieldNames = [];

	for (const element of elementFields) {
		const elementName = element.getAttribute('name');
		if (!elementName.includes('_edited') && elementName.includes('#')) {
			elementFieldNames.push(elementName);
		}
	}

	correctFieldsOrderByName.forEach((name, index) => {
		const elementName = elementFieldNames[index];
		if (elementName !== name) {
			isInOrder = false;
		}
	});

	return isInOrder;
};

describe('Field Search Location', () => {
	beforeAll(() => {
		Liferay.Language.direction = {
			en_US: 'rtl',
		};

		window.google = {
			maps: {
				event: {
					removeListener: jest.fn(),
				},
				places: {
					Autocomplete: class {},
				},
			},
		};
	});

	afterAll(() => {
		Liferay.Language.direction = globalLanguageDirection;
	});

	afterEach(cleanup);

	it('must to be show search location fields', () => {
		const {getByLabelText} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		expect(hasAllFields(getByLabelText, defaultConfig.labels)).toBe(true);
	});

	it('must to be show search location fields in correct order', () => {
		const {container} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		const correctFieldsOrderByName = [
			'test_search_location_field#place',
			'test_search_location_field#address',
			'test_search_location_field#city',
			'test_search_location_field#country',
			'test_search_location_field#postal-code',
			'test_search_location_field#state',
		];

		const renderedFields = container.getElementsByTagName('input');

		expect(
			hasAllFieldsInCorrectOrder(correctFieldsOrderByName, renderedFields)
		).toBe(true);
	});

	it('must to be reflect the visible fields settings - remove field', () => {
		delete defaultConfig.labels.city;
		defaultConfig.visibleFields.splice(1, 1);

		const {getByLabelText} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		expect(hasAllFields(getByLabelText, defaultConfig.labels)).toBe(true);
	});

	it('must to be show search location fields in correct order - remove field', () => {
		const {container} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		const correctFieldsOrderByName = [
			'test_search_location_field#place',
			'test_search_location_field#address',
			'test_search_location_field#country',
			'test_search_location_field#postal-code',
			'test_search_location_field#state',
		];

		const renderedFields = container.getElementsByTagName('input');

		expect(
			hasAllFieldsInCorrectOrder(correctFieldsOrderByName, renderedFields)
		).toBe(true);
	});

	it('must to be reflect the visible fields settings - add field', () => {
		defaultConfig.labels.city = 'City';
		defaultConfig.visibleFields.splice(1, 0, 'city');

		const {getByLabelText} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		expect(hasAllFields(getByLabelText, defaultConfig.labels)).toBe(true);
	});

	it('must to be show search location fields in correct order - add field', () => {
		const {container} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		const correctFieldsOrderByName = [
			'test_search_location_field#place',
			'test_search_location_field#address',
			'test_search_location_field#city',
			'test_search_location_field#country',
			'test_search_location_field#postal-code',
			'test_search_location_field#state',
		];

		const renderedFields = container.getElementsByTagName('input');

		expect(
			hasAllFieldsInCorrectOrder(correctFieldsOrderByName, renderedFields)
		).toBe(true);
	});

	it('must to be reflect the one column layout settings', () => {
		const {container} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		const fieldsWithLayoutBehavior = container.getElementsByClassName(
			'col-md-12'
		);

		expect(fieldsWithLayoutBehavior.length).toBe(
			defaultConfig.visibleFields.length
		);
	});

	it('must to be reflect the two columns layout settings', () => {
		defaultConfig.layout = ['two-columns'];
		const {container} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		const fieldsWithLayoutBehavior = container.getElementsByClassName(
			'col-md-6'
		);

		expect(fieldsWithLayoutBehavior.length).toBe(
			defaultConfig.visibleFields.length - 1
		);
	});

	it('must to be reflect the visible fields settings - remove field - layout changed', () => {
		delete defaultConfig.labels.city;
		defaultConfig.visibleFields.splice(1, 1);

		const {getByLabelText} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		expect(hasAllFields(getByLabelText, defaultConfig.labels)).toBe(true);
	});

	it('must to be show search location fields in correct order - remove field - layout changed', () => {
		const {container} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		const correctFieldsOrderByName = [
			'test_search_location_field#place',
			'test_search_location_field#address',
			'test_search_location_field#country',
			'test_search_location_field#postal-code',
			'test_search_location_field#state',
		];

		const renderedFields = container.getElementsByTagName('input');

		expect(
			hasAllFieldsInCorrectOrder(correctFieldsOrderByName, renderedFields)
		).toBe(true);
	});

	it('must to be reflect the visible fields settings - add field - layout changed', () => {
		defaultConfig.labels.city = 'City';
		defaultConfig.visibleFields.splice(1, 0, 'city');

		const {getByLabelText} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		expect(hasAllFields(getByLabelText, defaultConfig.labels)).toBe(true);
	});

	it('must to be show search location fields in correct order - add field - layout changed', () => {
		const {container} = render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		const correctFieldsOrderByName = [
			'test_search_location_field#place',
			'test_search_location_field#address',
			'test_search_location_field#city',
			'test_search_location_field#country',
			'test_search_location_field#postal-code',
			'test_search_location_field#state',
		];

		const renderedFields = container.getElementsByTagName('input');

		expect(
			hasAllFieldsInCorrectOrder(correctFieldsOrderByName, renderedFields)
		).toBe(true);
	});

	it('must to be append google dropdown places script', async () => {
		const {getByLabelText} = await render(
			<SearchLocationWithProvider {...defaultConfig} />
		);

		const searchLocationField = getByLabelText('Search Location');
		const googlePlacesScriptElement = searchLocationField
			.getElementsByTagName('script')
			.item(0);

		expect(!!googlePlacesScriptElement).toBe(true);
	});
});
