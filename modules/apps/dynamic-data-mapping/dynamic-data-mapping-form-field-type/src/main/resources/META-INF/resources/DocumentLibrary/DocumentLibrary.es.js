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

import axios from 'axios';
import {
	PagesVisitor,
	convertToFormData,
	useConfig,
	useFormState,
} from 'data-engine-js-components-web';
import React, {useCallback, useEffect, useMemo, useState} from 'react';

import {FieldBase} from '../FieldBase/ReactFieldBase.es';
import DownloadCard from './DownloadCard';
import GuestUpload from './GuestUpload';
import UserUpload from './UserUpload';

const Main = ({
	_onBlur,
	_onFocus,
	allowGuestUsers,
	displayErrors: initialDisplayErrors,
	editingLanguageId,
	errorMessage: initialErrorMessage,
	fieldName,
	fileEntryTitle,
	fileEntryURL,
	guestUploadURL,
	id,
	itemSelectorURL,
	maximumRepetitions,
	maximumSubmissionLimitReached,
	message,
	name,
	onBlur,
	onChange,
	onFocus,
	placeholder,
	readOnly,
	showUploadPermissionMessage,
	valid: initialValid,
	value = '{}',
	...otherProps
}) => {
	const {portletNamespace} = useConfig();
	const {pages} = useFormState();

	const isSignedIn = Liferay.ThemeDisplay.isSignedIn();

	const getErrorMessages = useCallback(
		(errorMessage, isSignedIn) => {
			const errorMessages = [errorMessage];

			if (!allowGuestUsers && !isSignedIn) {
				errorMessages.push(
					Liferay.Language.get(
						'you-need-to-be-signed-in-to-edit-this-field'
					)
				);
			}
			else if (maximumSubmissionLimitReached) {
				errorMessages.push(
					Liferay.Language.get(
						'the-maximum-number-of-submissions-allowed-for-this-form-has-been-reached'
					)
				);
			}
			else if (showUploadPermissionMessage) {
				errorMessages.push(
					Liferay.Language.get(
						'you-need-to-be-assigned-to-the-same-site-where-the-form-was-created-to-use-this-field'
					)
				);
			}

			return errorMessages.join(' ');
		},
		[
			allowGuestUsers,
			maximumSubmissionLimitReached,
			showUploadPermissionMessage,
		]
	);

	const [errorMessage, setErrorMessage] = useState(
		getErrorMessages(initialErrorMessage, isSignedIn)
	);
	const [displayErrors, setDisplayErrors] = useState(initialDisplayErrors);
	const [progress, setProgress] = useState(0);
	const [valid, setValid] = useState(initialValid);

	const checkMaximumRepetitions = useCallback(() => {
		const visitor = new PagesVisitor(pages);

		let repetitionsCounter = 0;

		visitor.mapFields(
			(field) => {
				if (fieldName === field.fieldName) {
					repetitionsCounter++;
				}
			},
			true,
			true
		);

		return repetitionsCounter === maximumRepetitions;
	}, [fieldName, maximumRepetitions, pages]);

	const configureErrorMessage = useCallback((message) => {
		setErrorMessage(message);
		setDisplayErrors(!!message);
		setValid(!message);
	}, []);

	const disableSubmitButton = useCallback((disable = true) => {
		document.getElementById('ddm-form-submit').disabled = disable;
	}, []);

	const handleGuestUploadFileChanged = useCallback(
		(errorMessage, event, value) => {
			configureErrorMessage(errorMessage);

			onChange(event, value ? value : '{}');
		},
		[configureErrorMessage, onChange]
	);

	const isExceededUploadRequestSizeLimit = useCallback(
		(fileSize) => {
			const uploadRequestSizeLimit =
				Liferay.PropsValues.UPLOAD_SERVLET_REQUEST_IMPL_MAX_SIZE;

			if (fileSize <= uploadRequestSizeLimit) {
				return false;
			}

			const errorMessage = Liferay.Util.sub(
				Liferay.Language.get(
					'please-enter-a-file-with-a-valid-file-size-no-larger-than-x'
				),
				[Liferay.Util.formatStorage(uploadRequestSizeLimit)]
			);

			handleGuestUploadFileChanged(errorMessage, {}, null);

			return true;
		},
		[handleGuestUploadFileChanged]
	);

	const [transformedFileEntryTitle, transformedFileEntryURL] = useMemo(() => {
		let title = fileEntryTitle;
		let url = fileEntryURL;

		if (value && typeof value === 'string') {
			try {
				const fileEntry = JSON.parse(value);

				title = fileEntry.title;

				if (fileEntry.url) {
					url = fileEntry.url;
				}
			}
			catch (error) {
				console.warn('Unable to parse JSON', value);
			}
		}

		return value ? [title, url] : [];
	}, [fileEntryTitle, fileEntryURL, value]);

	const handleChangeUserUpload = useCallback(
		(event, value) => {
			onChange(event, value ?? '{}');
		},
		[onChange]
	);

	const handleClearGuestUpload = useCallback(
		(event) => {
			onFocus(event);

			onChange(event, '{}');

			const guestUploadInput = document.getElementById(
				`${name}inputFileGuestUpload`
			);

			if (guestUploadInput) {
				guestUploadInput.value = '';
			}

			onBlur(event);
		},
		[name, onBlur, onChange, onFocus]
	);

	const handleSelectGuestUpload = useCallback(
		(event) => {
			onFocus(event);

			const file = event.target.files[0];

			if (isExceededUploadRequestSizeLimit(file.size)) {
				onBlur(event);

				return;
			}

			const data = {
				[`${portletNamespace}file`]: file,
			};

			axios
				.post(guestUploadURL, convertToFormData(data), {
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
						handleGuestUploadFileChanged(
							error.message,
							event,
							null
						);
					}
					else {
						handleGuestUploadFileChanged(
							'',
							event,
							JSON.stringify(file)
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
			guestUploadURL,
			handleGuestUploadFileChanged,
			isExceededUploadRequestSizeLimit,
			portletNamespace,
			onBlur,
			onFocus,
		]
	);

	useEffect(() => {
		if ((!allowGuestUsers && !isSignedIn) || showUploadPermissionMessage) {
			const ddmFormUploadPermissionMessage = document.querySelector(
				`.ddm-form-upload-permission-message`
			);

			if (ddmFormUploadPermissionMessage) {
				ddmFormUploadPermissionMessage.classList.remove('hide');
			}
		}
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	const hasCustomError =
		(!isSignedIn && !allowGuestUsers) ||
		maximumSubmissionLimitReached ||
		showUploadPermissionMessage;

	return (
		<FieldBase
			{...otherProps}
			displayErrors={hasCustomError ? true : displayErrors}
			errorMessage={errorMessage}
			id={id}
			name={name}
			overMaximumRepetitionsLimit={
				maximumRepetitions > 0 ? checkMaximumRepetitions() : false
			}
			readOnly={hasCustomError ? true : readOnly}
			valid={hasCustomError ? false : valid}
		>
			<div className="liferay-ddm-form-field-document-library">
				{allowGuestUsers && !isSignedIn ? (
					<GuestUpload
						handleClear={handleClearGuestUpload}
						handleSelect={handleSelectGuestUpload}
						name={name}
						progress={progress}
						readOnly={hasCustomError ? true : readOnly}
						title={transformedFileEntryTitle}
					/>
				) : (
					<>
						{transformedFileEntryURL && readOnly ? (
							<DownloadCard
								title={transformedFileEntryTitle}
								url={transformedFileEntryURL}
							/>
						) : (
							<UserUpload
								editingLanguageId={editingLanguageId}
								handleChange={handleChangeUserUpload}
								itemSelectorURL={itemSelectorURL}
								name={name}
								onBlur={onBlur}
								onFocus={onFocus}
								portletNamespace={portletNamespace}
								readOnly={hasCustomError ? true : readOnly}
								title={transformedFileEntryTitle}
							/>
						)}
					</>
				)}

				<input
					id={id}
					name={name}
					placeholder={placeholder}
					type="hidden"
					value={value}
				/>

				{message && <div className="form-feedback-item">{message}</div>}
			</div>
		</FieldBase>
	);
};

Main.displayName = 'DocumentLibrary';

export default Main;
