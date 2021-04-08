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
import ClayModal, {useModal} from '@clayui/modal';
import React, {useCallback, useContext, useEffect, useState} from 'react';

import App from '../../App.es';
import AppContext from '../../AppContext.es';
import {
	UPDATE_CONFIG,
	UPDATE_DATA_DEFINITION_AVAILABLE_LANGUAGE,
	UPDATE_EDITING_DATA_DEFINITION_ID,
	UPDATE_EDITING_LANGUAGE_ID,
} from '../../actions.es';
import {
	containsField,
	isDataLayoutEmpty,
} from '../../utils/dataLayoutVisitor.es';
import {errorToast} from '../../utils/toast.es';
import TranslationManager from '../translation-manager/TranslationManager.es';
import useCreateFieldSet from './actions/useCreateFieldSet.es';
import usePropagateFieldSet from './actions/usePropagateFieldSet.es';
import useSaveFieldSet from './actions/useSaveFieldSet.es';

const ModalContent = ({
	childrenAppProps,
	defaultLanguageId,
	editingDataDefinition,
	fieldSet,
	onClose,
}) => {
	const [{appProps, config: appConfig}] = useContext(AppContext);
	const [childrenContext, setChildrenContext] = useState({
		dataLayoutBuilder: null,
		dispatch: () => {},
		state: {},
	});
	const [dataLayoutIsEmpty, setDataLayoutIsEmpty] = useState(true);
	const [editingLanguageId, setEditingLanguageId] = useState(
		defaultLanguageId
	);
	const [name, setName] = useState({});

	const {contentType} = appProps;
	const {
		dataLayoutBuilder,
		dispatch,
		state: {dataDefinition, dataLayout},
	} = childrenContext;

	const onEditingLanguageIdChange = useCallback(
		(editingLanguageId) => {
			setEditingLanguageId(editingLanguageId);

			dispatch({
				payload: editingLanguageId,
				type: UPDATE_EDITING_LANGUAGE_ID,
			});
		},
		[dispatch]
	);

	useEffect(() => {
		dispatch({
			payload: editingLanguageId,
			type: UPDATE_DATA_DEFINITION_AVAILABLE_LANGUAGE,
		});
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [dispatch]);

	const actionProps = {
		availableLanguageIds: dataDefinition?.availableLanguageIds,
		childrenContext,
		defaultLanguageId,
		editingLanguageId,
		fieldSet,
		onEditingLanguageIdChange,
	};

	const createFieldSet = useCreateFieldSet(actionProps);
	const saveFieldSet = useSaveFieldSet(actionProps);
	const propagateFieldSet = usePropagateFieldSet();

	const onSave = () => {
		const hasRemovedField = () => {
			const fieldNames = fieldSet.dataDefinitionFields.map(
				({name}) => name
			);

			const [prevLayoutFields, currentLayoutFields] = [
				fieldSet.defaultDataLayout.dataLayoutPages,
				dataLayout.dataLayoutPages,
			].map((layout) =>
				fieldNames.filter((field) => containsField(layout, field))
			);

			return !!prevLayoutFields.filter(
				(field) => !currentLayoutFields.includes(field)
			).length;
		};

		if (fieldSet) {
			propagateFieldSet({
				fieldSet,
				modal: {
					actionMessage: Liferay.Language.get('propagate'),
					allowReferencedDataDefinitionDeletion: true,
					fieldSetMessage: Liferay.Language.get(
						'do-you-want-to-propagate-the-changes-to-other-objects-views-using-this-fieldset'
					),
					headerMessage: Liferay.Language.get('propagate-changes'),
					...(hasRemovedField() && {
						warningMessage: Liferay.Language.get(
							'the-changes-include-the-deletion-of-fields-and-may-erase-the-data-collected-permanently'
						),
					}),
				},
				onPropagate: () => saveFieldSet(name),
			})
				.then(onClose)
				.catch(({message}) => errorToast(message));
		}
		else {
			createFieldSet(name)
				.then(onClose)
				.catch(({message}) => errorToast(message));
		}
	};

	const changeZIndex = (zIndex) => {
		document
			.querySelectorAll('.ddm-field-actions-container')
			.forEach((container) => {
				container.style.zIndex = zIndex;
			});
	};

	useEffect(() => {
		onEditingLanguageIdChange(defaultLanguageId);
	}, [defaultLanguageId, onEditingLanguageIdChange]);

	useEffect(() => {
		if (dataLayoutBuilder) {
			dataLayoutBuilder.onEditingLanguageIdChange({
				defaultLanguageId,
				editingLanguageId,
			});
		}
	}, [dataLayoutBuilder, defaultLanguageId, editingLanguageId]);

	useEffect(() => {
		if (fieldSet) {
			setName(fieldSet.name);
		}
	}, [fieldSet]);

	useEffect(() => {
		dispatch({
			payload: {
				config: {
					...appConfig,
					allowFieldSets: false,
				},
			},
			type: UPDATE_CONFIG,
		});
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [contentType, dispatch]);

	useEffect(() => {
		if (dataLayout) {
			const {dataLayoutPages} = dataLayout;
			setDataLayoutIsEmpty(isDataLayoutEmpty(dataLayoutPages));
		}
	}, [dataLayout]);

	useEffect(() => {
		if (dataLayoutBuilder) {
			changeZIndex('1050');
		}

		return () => {
			changeZIndex(null);
		};
	}, [dataLayoutBuilder]);

	useEffect(() => {
		if (editingDataDefinition && editingDataDefinition.id) {
			dispatch({
				payload: {
					editingDataDefinitionId: editingDataDefinition.id,
				},
				type: UPDATE_EDITING_DATA_DEFINITION_ID,
			});
		}
	}, [dispatch, editingDataDefinition]);

	return (
		<>
			<ClayModal.Header>
				{fieldSet
					? Liferay.Language.get('edit-fieldset')
					: Liferay.Language.get('create-new-fieldset')}
			</ClayModal.Header>
			<ClayModal.Header withTitle={false}>
				<TranslationManager
					defaultLanguageId={defaultLanguageId}
					editingLanguageId={editingLanguageId}
					onEditingLanguageIdChange={onEditingLanguageIdChange}
					translatedLanguageIds={name}
				/>
				<ClayInput.Group className="pl-4 pr-4">
					<ClayInput.GroupItem>
						<ClayInput
							aria-label={Liferay.Language.get(
								'untitled-fieldset'
							)}
							autoFocus
							className="form-control-inline"
							dir={
								Liferay.Language.direction[editingLanguageId] ||
								'ltr'
							}
							onChange={({target: {value}}) =>
								setName({...name, [editingLanguageId]: value})
							}
							placeholder={Liferay.Language.get(
								'untitled-fieldset'
							)}
							type="text"
							value={
								name[editingLanguageId] ||
								name[defaultLanguageId] ||
								''
							}
						/>
					</ClayInput.GroupItem>
				</ClayInput.Group>
			</ClayModal.Header>
			<ClayModal.Body>
				<div className="pl-4 pr-4">
					<App
						{...appProps}
						dataLayoutBuilderId={`${appProps.dataLayoutBuilderId}_2`}
						setChildrenContext={setChildrenContext}
						{...childrenAppProps}
						defaultLanguageId={defaultLanguageId}
					/>
				</div>
			</ClayModal.Body>
			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton displayType="secondary" onClick={onClose}>
							{Liferay.Language.get('cancel')}
						</ClayButton>
						<ClayButton
							disabled={
								Object.keys(name).length == 0 ||
								dataLayoutIsEmpty
							}
							onClick={onSave}
						>
							{Liferay.Language.get('save')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</>
	);
};

export default ({isVisible, onClose: onCloseFn, ...props}) => {
	const {observer, onClose} = useModal({
		onClose: onCloseFn,
	});

	if (!isVisible) {
		return null;
	}

	return (
		<ClayModal
			className="data-layout-builder-editor-modal fieldset-modal"
			observer={observer}
			size="full-screen"
		>
			<ModalContent onClose={onClose} {...props} />
		</ClayModal>
	);
};
