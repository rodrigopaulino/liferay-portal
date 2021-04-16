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

import {ClassicEditor} from 'frontend-editor-ckeditor-web';
import React, {useCallback, useEffect, useRef, useState} from 'react';

import {FieldBase} from '../FieldBase/ReactFieldBase.es';
import {useSyncValue} from '../hooks/useSyncValue.es';

const RichText = ({
	editingLanguageId,
	editorConfig,
	id,
	name,
	onChange,
	predefinedValue,
	readOnly,
	value,
	visible,
	...otherProps
}) => {
	const [currentValue, setCurrentValue] = useSyncValue(
		value ? value : predefinedValue
	);

	const [dirty, setDirty] = useState(false);

	const editorRef = useRef();

	const afterSetDataListener = useCallback(
		({data}) => {
			const {dataValue} = data;

			setCurrentValue(dataValue);

			onChange({}, dataValue);
		},
		// eslint-disable-next-line react-hooks/exhaustive-deps
		[editingLanguageId, editorRef]
	);

	useEffect(
		() => {
			const editor = editorRef.current?.editor;

			if (editor) {
				editor.config.contentsLangDirection =
					Liferay.Language.direction[editingLanguageId];

				editor.config.contentsLanguage = editingLanguageId;

				editor.setData(editor.getData());

				if (editor.mode === 'source') {
					editor.on('afterSetData', afterSetDataListener);
				}

				return () => {
					editor.removeListener('afterSetData', afterSetDataListener);
				};
			}
		},
		// eslint-disable-next-line react-hooks/exhaustive-deps
		[afterSetDataListener]
	);

	return (
		<FieldBase
			{...otherProps}
			id={id}
			name={name}
			readOnly={readOnly}
			style={readOnly ? {pointerEvents: 'none'} : null}
			visible={visible}
		>
			<ClassicEditor
				contents={currentValue}
				data={currentValue}
				editorConfig={editorConfig}
				name={name}
				onChange={(data) => {
					if (currentValue !== data) {
						setCurrentValue(data);
						setDirty(true);

						onChange({}, data);
					}
					else if (!dirty) {
						CKEDITOR.instances[name].resetUndo();
					}
				}}
				onMode={({editor}) => {
					if (editor.mode === 'source') {
						editor.on('afterSetData', afterSetDataListener);
					}
					else {
						editor.removeListener(
							'afterSetData',
							afterSetDataListener
						);
					}
				}}
				readOnly={readOnly}
				ref={editorRef}
			/>

			<input
				defaultValue={currentValue}
				id={id || name}
				name={name}
				type="hidden"
			/>
		</FieldBase>
	);
};

export default RichText;
