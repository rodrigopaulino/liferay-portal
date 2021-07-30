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

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayForm, {ClayInput} from '@clayui/form';
import ClayModal, {useModal} from '@clayui/modal';
import PropTypes from 'prop-types';
import React, {useRef, useState} from 'react';

const ImportStructureModal = ({importDataDefinitionURL, portletNamespace}) => {
	const [visible, setVisible] = useState(false);
	const inputFileRef = useRef();
	const [structureName, setStructureName] = useState('');
	const importStructureModalComponentId = `${portletNamespace}importStructureModal`;
	const jsonFileInputId = `${portletNamespace}jsonFile`;
	const nameInputId = `${portletNamespace}name`;
	const [{fileName, inputFile, inputFileValue}, setFile] = useState({
		fileName: '',
		inputFile: null,
		inputFileValue: '',
	});
	const {observer, onClose} = useModal({
		onClose: () => {
			setVisible(false);
			setFile({
				fileName: '',
				inputFile: null,
				inputFileValue: '',
			});
			setStructureName('');
		},
	});

	if (!Liferay.component(importStructureModalComponentId)) {
		Liferay.component(
			importStructureModalComponentId,
			{
				open: () => {
					setVisible(true);
				},
			},
			{
				destroyOnNavigate: true,
			}
		);
	}

	return visible ? (
		<ClayModal observer={observer} size="md">
			<ClayModal.Header>
				{Liferay.Language.get('import-structure')}
			</ClayModal.Header>
			<form
				action={importDataDefinitionURL}
				encType="multipart/form-data"
				method="post"
			>
				<ClayModal.Body>
					<ClayAlert
						displayType="info"
						title={Liferay.Language.get('info')}
					>
						{Liferay.Language.get(
							'once-you-click-import-the-process-will-run-in-the-background-this-may-take-a-while'
						)}
					</ClayAlert>
					<ClayForm.Group>
						<label htmlFor={nameInputId}>
							{Liferay.Language.get('name')}
						</label>
						<ClayInput
							id={nameInputId}
							name={nameInputId}
							onChange={(event) =>
								setStructureName(event.target.value)
							}
							type="text"
							value={structureName}
						/>
						<label htmlFor={jsonFileInputId}>
							{Liferay.Language.get('json-file')}
						</label>
						<ClayInput.Group>
							<ClayInput.GroupItem prepend>
								<ClayInput
									disabled
									id={jsonFileInputId}
									type="text"
									value={fileName}
								/>
							</ClayInput.GroupItem>
							<ClayInput.GroupItem append shrink>
								<ClayButton
									displayType="secondary"
									onClick={() => inputFileRef.current.click()}
								>
									{Liferay.Language.get('select')}
								</ClayButton>
							</ClayInput.GroupItem>
							{inputFile && (
								<ClayInput.GroupItem shrink>
									<ClayButton
										displayType="secondary"
										onClick={() => {
											setFile({
												fileName: '',
												inputFile: null,
												inputFileValue: '',
											});
										}}
									>
										{Liferay.Language.get('clear')}
									</ClayButton>
								</ClayInput.GroupItem>
							)}
						</ClayInput.Group>
					</ClayForm.Group>
					<input
						className="d-none"
						name={jsonFileInputId}
						onChange={({target}) => {
							const [file] = target.files;
							setFile({
								fileName: file.name,
								inputFile: file,
								inputFileValue: target.value,
							});
						}}
						ref={inputFileRef}
						type="file"
						value={inputFileValue}
					/>
				</ClayModal.Body>
				<ClayModal.Footer
					last={
						<ClayButton.Group spaced>
							<ClayButton
								displayType="secondary"
								onClick={onClose}
							>
								{Liferay.Language.get('cancel')}
							</ClayButton>
							<ClayButton
								disabled={!inputFile || !structureName}
								type="submit"
							>
								{Liferay.Language.get('import')}
							</ClayButton>
						</ClayButton.Group>
					}
				/>
			</form>
		</ClayModal>
	) : null;
};

ImportStructureModal.propTypes = {
	importDataDefinitionURL: PropTypes.string,
	portletNamespace: PropTypes.string,
};

export default ImportStructureModal;
