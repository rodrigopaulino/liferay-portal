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
import axios from 'axios';

// @ts-ignore

import {convertToFormData, useConfig} from 'data-engine-js-components-web';
import React, {useCallback, useState} from 'react';

const GuestUpload: React.FC<IProps> = ({
	maxFileSize,
	name,
	onBlur,
	onChange,
	onFocus,
	readOnly,
	title,
	url,
}) => {
	const {portletNamespace} = useConfig();

	const [progress, setProgress] = useState(0);

	const disableSubmitButton = useCallback((disable = true) => {
		const submitButton = document.getElementById('ddm-form-submit');

		if (disable) {
			submitButton?.setAttribute('disabled', disable);
		}
		else {
			submitButton?.removeAttribute('disabled');
		}
	}, []);

	const onClickClear = useCallback(
		(event) => {
			onFocus(event);

			onChange(event, ['', '{}'], ['guestUploadErrorMessage', 'value']);

			const guestUploadInput = document.getElementById(
				`${name}inputFileGuestUpload`
			);

			if (guestUploadInput) {
				guestUploadInput.setAttribute('value', '');
			}

			onBlur(event);
		},
		[name, onBlur, onChange, onFocus]
	);

	const onClickSelect = useCallback(
		(event) => {
			onFocus(event);

			if (!event.target.files) {
				return;
			}

			const file = event.target.files[0];

			if (file?.size > maxFileSize) {
				onChange(
					event,
					['true', '', '{}'],
					[
						'guestUploadFileSizeExceeded',
						'guestUploadErrorMessage',
						'value',
					]
				);

				onBlur(event);

				return;
			}

			const data = {
				[`${portletNamespace}file`]: file,
			};

			axios
				.post(url, convertToFormData(data), {
					onUploadProgress: (event) => {
						const progress = Math.round(
							(event.loaded * 100) / event.total
						);

						setProgress(progress);

						disableSubmitButton();
					},
				})
				.then((response) => {
					const {error, file} = response.data;

					disableSubmitButton(false);

					if (error) {
						onChange(
							event,
							['false', error.message, '{}'],
							[
								'guestUploadFileSizeExceeded',
								'guestUploadErrorMessage',
								'value',
							]
						);
					}
					else {
						onChange(
							event,
							['false', '', JSON.stringify(file)],
							[
								'guestUploadFileSizeExceeded',
								'guestUploadErrorMessage',
								'value',
							]
						);
					}

					setProgress(0);
				})
				.catch(() => {
					disableSubmitButton(false);

					setProgress(0);
				})
				.finally(() => {
					onBlur(event);
				});
		},
		[
			disableSubmitButton,
			maxFileSize,
			url,
			portletNamespace,
			onBlur,
			onChange,
			onFocus,
		]
	);

	return (
		<>
			<ClayInput.Group>
				<ClayInput.GroupItem prepend>
					<ClayInput
						className="bg-light"
						disabled={readOnly}
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
						onChange={(event) => onClickSelect(event)}
						type="file"
					/>
				</ClayInput.GroupItem>

				{title && (
					<ClayInput.GroupItem shrink>
						<ClayButton
							aria-label={Liferay.Language.get('unselect-file')}
							displayType="secondary"
							onClick={(event) => onClickClear(event)}
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
	maxFileSize: number;
	name: string;
	onBlur: (event: any) => void;
	onChange: (event: any, values: string[], keys: string[]) => void;
	onFocus: (event: any) => void;
	readOnly: boolean;
	title: string;
	url: string;
}
