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

import ClayButton from '@clayui/button';
import {ClayInput} from '@clayui/form';

// @ts-ignore

import {useConfig} from 'data-engine-js-components-web';

// @ts-ignore

import {openSelectionModal} from 'frontend-js-web';
import React, {useCallback} from 'react';

import {Locale} from '../types';

const UserUpload: React.FC<IProps> = ({
	editingLanguageId,
	name,
	onBlur,
	onChange,
	onFocus,
	readOnly,
	title,
	url,
}) => {
	const {portletNamespace} = useConfig();

	const onClickSelect = useCallback(
		(event) => {
			onFocus(event);

			openSelectionModal({
				onClose: () => onBlur(event),
				onSelect: (selectedItem: any) => {
					if (selectedItem?.value) {
						try {
							const fileEntry = JSON.parse(selectedItem.value);

							onChange(
								event,
								[
									fileEntry.title,
									fileEntry.url,
									selectedItem.value,
								],
								['fileEntryTitle', 'fileEntryURL', 'value']
							);
						}
						catch (error) {
							console.warn(
								'Unable to parse JSON',
								selectedItem.value
							);
						}
					}
				},
				selectEventName: `${portletNamespace}selectDocumentLibrary`,
				title: Liferay.Util.sub(
					Liferay.Language.get('select-x'),
					Liferay.Language.get('document')
				),
				url,
			});
		},
		[onBlur, onChange, onFocus, portletNamespace, url]
	);

	return (
		<>
			<ClayInput.Group>
				<ClayInput.GroupItem prepend>
					<ClayInput
						aria-label={Liferay.Language.get('file')}
						className="bg-light field"
						dir={Liferay.Language.direction[editingLanguageId]}
						disabled={readOnly}
						id={`${name}inputFile`}
						lang={editingLanguageId}
						onClick={(event) => onClickSelect(event)}
						value={title || ''}
					/>
				</ClayInput.GroupItem>

				<ClayInput.GroupItem append shrink>
					<ClayButton
						className="select-button"
						disabled={readOnly}
						displayType="secondary"
						onClick={(event) => onClickSelect(event)}
					>
						<span className="lfr-btn-label">
							{Liferay.Language.get('select')}
						</span>
					</ClayButton>
				</ClayInput.GroupItem>

				{title && (
					<ClayInput.GroupItem shrink>
						<ClayButton
							aria-label={Liferay.Language.get('unselect-file')}
							displayType="secondary"
							onClick={(event) =>
								onChange(
									event,
									['', '', ''],
									['fileEntryTitle', 'fileEntryURL', 'value']
								)
							}
							type="button"
						>
							{Liferay.Language.get('clear')}
						</ClayButton>
					</ClayInput.GroupItem>
				)}
			</ClayInput.Group>
		</>
	);
};

export default UserUpload;

interface IProps {
	editingLanguageId: Locale;
	name: string;
	onBlur: (event: any) => void;
	onChange: (event: any, values: string[], keys: string[]) => void;
	onFocus: (event: any) => void;
	readOnly: boolean;
	title: string;
	url: string;
}
