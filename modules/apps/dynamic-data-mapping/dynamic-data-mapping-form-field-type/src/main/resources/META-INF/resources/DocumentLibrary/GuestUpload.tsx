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
import ClayProgressBar from '@clayui/progress-bar';
import React from 'react';

const GuestUpload: React.FC<IProps> = ({
	handleClear,
	handleSelect,
	name,
	progress,
	readOnly,
	title,
}) => {
	return (
		<>
			<ClayInput.Group>
				<ClayInput.GroupItem prepend>
					<ClayInput
						className="bg-light"
						disabled={readOnly}
						onClick={(event) => handleSelect(event)}
						type="text"
						value={title || ''}
					/>
				</ClayInput.GroupItem>

				<ClayInput.GroupItem append shrink>
					<label
						className={
							'btn btn-secondary select-button' +
							(title ? ' clear-button-upload-on' : '') +
							(readOnly ? ' disabled' : '')
						}
						htmlFor={`${name}inputFileGuestUpload`}
					>
						{Liferay.Language.get('select')}
					</label>

					<input
						className="input-file"
						disabled={readOnly}
						id={`${name}inputFileGuestUpload`}
						onChange={(event) => handleSelect(event)}
						type="file"
					/>
				</ClayInput.GroupItem>

				{title && (
					<ClayInput.GroupItem shrink>
						<ClayButton
							aria-label={Liferay.Language.get('unselect-file')}
							displayType="secondary"
							onClick={(event) => handleClear(event)}
							type="button"
						>
							{Liferay.Language.get('clear')}
						</ClayButton>
					</ClayInput.GroupItem>
				)}
			</ClayInput.Group>

			{progress !== 0 && <ClayProgressBar value={progress} />}
		</>
	);
};

export default GuestUpload;

interface IProps {
	handleClear: (event: any) => void;
	handleSelect: (event: any) => void;
	name: string;
	progress: number;
	readOnly: boolean;
	title: string;
}
