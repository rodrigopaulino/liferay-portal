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
import React, {useCallback} from 'react';

const UserUpload: React.FC<IProps> = ({
	editingLanguageId,
	handleChange,
	itemSelectorURL,
	name,
	onBlur,
	onFocus,
	portletNamespace,
	readOnly,
	title,
}) => {
	const onClickSelect = useCallback(
		(event) => {
			onFocus(event);

			Liferay.Util.openSelectionModal({
				onClose: () => onBlur(event),
				onSelect: (selectedItem: any) => {
					if (selectedItem?.value) {
						handleChange(selectedItem, selectedItem.value);
					}
				},
				selectEventName: `${portletNamespace}selectDocumentLibrary`,
				title: Liferay.Util.sub(
					Liferay.Language.get('select-x'),
					Liferay.Language.get('document')
				),
				url: itemSelectorURL,
			});
		},
		[handleChange, itemSelectorURL, onBlur, onFocus, portletNamespace]
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
							onClick={(event) => handleChange(event, undefined)}
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
	handleChange: (event: any, value?: string) => void;
	itemSelectorURL: string;
	name: string;
	onBlur: (event: any) => void;
	onFocus: (event: any) => void;
	portletNamespace: string;
	readOnly: boolean;
	title: string;
}
