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

// Utils

export {default as compose} from './util/compose.es';
export {normalizeFieldName} from './util/fields.es';
export {
	getRepeatedIndex,
	generateName,
	generateInstanceId,
	parseName,
} from './util/repeatable.es';
export {PagesVisitor, RulesVisitor} from './util/visitors.es';
export * as FormSupport from './util/FormSupport.es';
export {getConnectedReactComponentAdapter} from './util/ReactComponentAdapter.es';
export {getUid} from './util/formId.es';
export {parseProps} from './util/parseProps.es';
export {makeFetch, convertToFormData} from './util/fetch.es';
export {default as setDataRecord} from './util/setDataRecord.es';

// Form/Data Engine Core

export {default as Pages} from './core/components/Pages.es';
export {Field} from './core/components/Field/Field.es';
export {FieldStateless} from './core/components/Field/FieldStateless.es';
export {EVENT_TYPES, MAPPED_EVENT_TYPES} from './core/actions/eventTypes.es';
export {PageProvider, usePage} from './core/hooks/usePage.es';
export {useFieldTypesResource} from './core/hooks/useResource.es';
export {FormProvider, useForm, useFormState} from './core/hooks/useForm.es';
export {ConfigProvider, useConfig} from './core/hooks/useConfig.es';
export {Layout} from './core/components/PageRenderer/Layout.es';
export * as DefaultVariant from './core/components/PageRenderer/DefaultVariant.es';

// Custom Form

export {EVENT_TYPES as FORM_EVENT_TYPES} from './custom/form/eventTypes.es';

// Containers

export {FormView} from './custom/form/FormView.es';
export {default as LegacyFormBuilder} from './custom/form/LegacyFormBuilder.es';
export {
	FormFieldSettings,
	FormFieldSettingsAdapter,
} from './custom/form/FormFieldSettings.es';
