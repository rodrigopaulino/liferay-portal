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

import React, {useMemo} from 'react';

import {FieldBase} from '../FieldBase/ReactFieldBase.es';
import DownloadCard from './DownloadCard';
import GuestUpload from './GuestUpload';
import UserUpload from './UserUpload';

const Main = ({
	_onBlur,
	_onFocus,
	containsAddFolderPermission,
	displayErrors,
	editingLanguageId,
	errorMessage,
	fileEntryTitle,
	fileEntryURL,
	guestUploadForbidden,
	guestUploadLimitReached,
	guestUploadMaxFileSize,
	guestUploadURL,
	id,
	itemSelectorURL,
	maximumRepetitions,
	message,
	name,
	onBlur,
	onChange,
	onFocus,
	placeholder,
	readOnly,
	valid,
	value = '{}',
	...otherProps
}) => {
	const hasCustomError = useMemo(
		() =>
			!containsAddFolderPermission ||
			guestUploadForbidden ||
			guestUploadLimitReached,
		[
			containsAddFolderPermission,
			guestUploadForbidden,
			guestUploadLimitReached,
		]
	);

	return (
		<FieldBase
			{...otherProps}
			displayErrors={hasCustomError ? true : displayErrors}
			errorMessage={errorMessage}
			id={id}
			maximumRepetitions={maximumRepetitions}
			name={name}
			readOnly={hasCustomError ? true : readOnly}
			valid={hasCustomError ? false : valid}
		>
			<div className="liferay-ddm-form-field-document-library">
				{guestUploadURL ? (
					<GuestUpload
						maxFileSize={guestUploadMaxFileSize}
						name={name}
						onBlur={onBlur}
						onChange={onChange}
						onFocus={onFocus}
						readOnly={hasCustomError ? true : readOnly}
						title={fileEntryTitle}
						url={guestUploadURL}
					/>
				) : (
					<>
						{fileEntryURL && readOnly ? (
							<DownloadCard
								title={fileEntryTitle}
								url={fileEntryURL}
							/>
						) : (
							<UserUpload
								editingLanguageId={editingLanguageId}
								name={name}
								onBlur={onBlur}
								onChange={onChange}
								onFocus={onFocus}
								readOnly={hasCustomError ? true : readOnly}
								title={fileEntryTitle}
								url={itemSelectorURL}
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
