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

import {
	StringUtils,
	convertToFormData,
	makeFetch,
} from 'data-engine-js-components-web';

const defineIds = (portletNamespace, response) => {
	const formInstanceIdNode = document.querySelector(
		`#${portletNamespace}formInstanceId`
	);

	if (formInstanceIdNode && formInstanceIdNode.value === '0') {
		formInstanceIdNode.value = response.formInstanceId;
	}

	const ddmStructureIdNode = document.querySelector(
		`#${portletNamespace}ddmStructureId`
	);

	if (ddmStructureIdNode && ddmStructureIdNode.value === '0') {
		ddmStructureIdNode.value = response.ddmStructureId;
	}
};

const getFormData = ({name, portletNamespace}) => {
	const form = document.querySelector(`#${portletNamespace}editForm`);

	const formData = new FormData(form);

	formData.append(`${portletNamespace}name`, JSON.stringify(name));
	formData.append(`${portletNamespace}saveAsDraft`, 'true');

	return convertToFormData(formData);
};

const updateAutoSaveMessage = ({modifiedDate, portletNamespace}) => {
	const autoSaveMessageNode = document.querySelector(
		`#${portletNamespace}autosaveMessage`
	);

	autoSaveMessageNode.innerHTML = StringUtils.sub(
		Liferay.Language.get('draft-x'),
		[modifiedDate]
	);
};

export function saveForm({localizedName, portletNamespace, url}, resolve) {
	return makeFetch({
		body: getFormData({
			name: localizedName,
			portletNamespace,
		}),
		url,
	})
		.then((response) => {
			defineIds(portletNamespace, response);

			updateAutoSaveMessage({
				modifiedDate: response.modifiedDate,
				portletNamespace,
			});

			resolve();

			return response;
		})
		.catch((error) => {
			console.error(error);
		});
}
