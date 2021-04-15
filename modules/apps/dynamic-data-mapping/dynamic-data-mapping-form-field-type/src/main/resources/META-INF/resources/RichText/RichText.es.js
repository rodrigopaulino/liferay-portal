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
	const usePrevious = (value, initialValue) => {
		const ref = useRef(initialValue);
		useEffect(() => {
			ref.current = value;
		});
		return ref.current;
	};

	const useEffectDebugger = (effectHook, dependencies, dependencyNames = []) => {
		const previousDeps = usePrevious(dependencies, []);

		const changedDeps = dependencies.reduce((accum, dependency, index) => {
			if (dependency !== previousDeps[index]) {
				const keyName = dependencyNames[index] || index;
				return {
					...accum,
					[keyName]: {
						before: previousDeps[index],
						after: dependency
					}
				};
			}

			return accum;
		}, {});

		if (Object.keys(changedDeps).length) {
			// console.log('[use-effect-debugger] ', changedDeps);
		}

		useEffect(effectHook, dependencies);
	};

	const useCallbackDebugger = (effectHook, dependencies, dependencyNames = []) => {
		const previousDeps = usePrevious(dependencies, []);

		const changedDeps = dependencies.reduce((accum, dependency, index) => {
			if (dependency !== previousDeps[index]) {
				const keyName = dependencyNames[index] || index;
				return {
					...accum,
					[keyName]: {
						before: previousDeps[index],
						after: dependency
					}
				};
			}

			return accum;
		}, {});

		if (Object.keys(changedDeps).length) {
			// console.log('[use-callback-debugger] ', changedDeps);
		}

		return useCallback(effectHook, dependencies);
	};

	const [
		currentAfterSetDataListener,
		setCurrentAfterSetDataListener,
	] = useState(() => () => {});

	const [
		currentChangeListener,
		setCurrentChangeListener,
	] = useState(() => () => {});

	const [currentValue, setCurrentValue] = useSyncValue(
		value ? value : predefinedValue
	);

	const [dirty, setDirty] = useState(false);

	const afterSetDataListener = useCallbackDebugger(
		({data}) => {
			const {dataValue} = data;

			setCurrentValue(dataValue);

			onChange({}, dataValue);
		},
		[onChange, setCurrentValue]
	);

	const changeListener = useCallbackDebugger(
		(data) => {
			if (currentValue !== data) {
				setCurrentValue(data);
				setDirty(true);

				onChange({}, data);
			}
			else if (!dirty) {
				CKEDITOR.instances[name].resetUndo();
			}
		},
		[currentValue, dirty, name, onChange, setCurrentValue, setDirty]
	);

	const editorRef = useRef();

	// useEffect(() => {
	// 	const editor = editorRef.current?.editor;
	//
	// 	if (editor) {
	// 		if (editor.mode === 'source') {
	// 			editor.removeListener('afterSetData', currentAfterSetDataListener);
	// 		}
	// 		else {
	// 			editor.removeListener('change', currentChangeListener);
	// 		}
	//
	// 		editor.config.contentsLangDirection =
	// 			Liferay.Language.direction[editingLanguageId];
	//
	// 		editor.config.contentsLanguage = editingLanguageId;
	//
	// 		editor.setData(editor.getData());
	//
	// 		if (editor.mode === 'source') {
	// 			setCurrentAfterSetDataListener(() => afterSetDataListener);
	//
	// 			editor.on('afterSetData', afterSetDataListener);
	// 		}
	// 		else {
	// 			setCurrentChangeListener(() => changeListener);
	//
	// 			editor.on('change', changeListener);
	// 		}
	// 	}
	// }, [
	// 	afterSetDataListener,
	// 	changeListener,
	// 	currentAfterSetDataListener,
	// 	currentChangeListener,
	// 	editingLanguageId,
	// 	setCurrentAfterSetDataListener,
	// 	setCurrentChangeListener,
	// ]);

	useEffectDebugger(() => {
		console.log("useEffect");
	}, [
		afterSetDataListener,
		changeListener,
		currentAfterSetDataListener,
		currentChangeListener,
		editingLanguageId,
		setCurrentAfterSetDataListener,
		setCurrentChangeListener,
	]);

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
				onChange={(event) => {
					console.log("custom change");
				}}
				onInstanceReady={(event) => {
					console.log("custom instanceReady");
				}}
				onMode={({editor}) => {
					console.log("custom mode");
					// editor.removeListener('afterSetData', currentAfterSetDataListener);
					//
					// editor.removeListener('change', currentChangeListener);
					//
					// if (editor.mode === 'source') {
					// 	setCurrentAfterSetDataListener(() => afterSetDataListener);
					//
					// 	editor.on('afterSetData', afterSetDataListener);
					// }
					// else {
					// 	setCurrentChangeListener(() => changeListener);
					//
					// 	editor.on('change', changeListener);
					// }
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
