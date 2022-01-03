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

import {useIsMounted} from '@liferay/frontend-js-react-web';
import {
	FormSupport,
	useConfig,
	useFormState,
} from 'data-engine-js-components-web';
import objectHash from 'object-hash';
import React, {useCallback, useContext, useEffect, useRef} from 'react';

import {saveForm} from '../util/saveForm';
import {useStateSync} from './useStateSync.es';
import {useValidateFormWithObjects} from './useValidateFormWithObjects';

const AutoSaveContext = React.createContext({});

AutoSaveContext.displayName = 'AutoSaveContext';

const getStateHash = (state) =>
	objectHash(state, {
		algorithm: 'md5',
		unorderedObjects: true,
	});

const MILLISECONDS_TO_MINUTE = 60000;

/**
 * AutoSave performs a periodic routine in minutes to save the current form. Save will
 * only happen if the data on the form changes.
 *
 * Each time the rules are changed, the form is saved.
 */
export function AutoSaveProvider({children, interval, url}) {
	const {portletNamespace} = useConfig();
	const {
		availableLanguageIds,
		defaultLanguageId,
		localizedDescription,
		localizedName,
		pages,
		paginationMode,
		successPageSettings,
	} = useFormState();

	const doSyncInput = useStateSync();

	const isMounted = useIsMounted();

	const intervalIdRef = useRef(null);

	const pendingRequestRef = useRef(null);

	const lastKnownHashRef = useRef(null);

	const validateFormWithObjects = useValidateFormWithObjects();

	const getCurrentStateHash = useCallback(
		() =>
			getStateHash({
				availableLanguageIds,
				defaultLanguageId,
				description: localizedDescription,
				name: localizedName,
				pages,
				paginationMode,
				successPageSettings,
			}),
		[
			availableLanguageIds,
			defaultLanguageId,
			localizedDescription,
			localizedName,
			pages,
			paginationMode,
			successPageSettings,
		]
	);

	const doSave = useCallback(() => {
		const lastKnownHash = getCurrentStateHash();

		doSyncInput();

		if (validateFormWithObjects()) {
			pendingRequestRef.current = saveForm(
				{localizedName, portletNamespace, url},
				() => {
					lastKnownHashRef.current = lastKnownHash;
				}
			).finally(() => {
				pendingRequestRef.current = null;
			});
		}

		return pendingRequestRef.current;
	}, [
		doSyncInput,
		getCurrentStateHash,
		lastKnownHashRef,
		localizedName,
		pendingRequestRef,
		portletNamespace,
		url,
		validateFormWithObjects,
	]);

	const isSaved = useCallback(() => {
		return lastKnownHashRef.current === getCurrentStateHash();
	}, [lastKnownHashRef, getCurrentStateHash]);

	const performSave = useCallback(() => {
		if (isMounted) {
			if (pendingRequestRef.current) {
				pendingRequestRef.current
					.then(() => performSave())
					.catch((error) => console.error(error));
			}
			else if (!isSaved() && !FormSupport.isEmpty(pages)) {
				doSave();
			}
		}
	}, [doSave, isMounted, isSaved, pages, pendingRequestRef]);

	useEffect(() => {
		if (interval > 0) {
			intervalIdRef.current = setInterval(
				() => performSave(),
				interval * MILLISECONDS_TO_MINUTE
			);
		}

		return () => {
			if (intervalIdRef.current) {
				clearInterval(intervalIdRef.current);
			}
		};
	}, [intervalIdRef, interval, performSave]);

	useEffect(() => {
		lastKnownHashRef.current = getCurrentStateHash();

		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	return (
		<AutoSaveContext.Provider value={{doSave, doSyncInput, isSaved}}>
			{children}
		</AutoSaveContext.Provider>
	);
}

export function useAutoSave() {
	return useContext(AutoSaveContext);
}
