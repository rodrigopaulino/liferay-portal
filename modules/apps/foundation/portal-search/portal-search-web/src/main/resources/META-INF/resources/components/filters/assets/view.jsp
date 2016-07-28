<%--
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
--%>

<%@ include file="/init.jsp" %>

<style>
	input[type="checkbox"].facet-value {
		margin-right: 5px;
	}

	label {
		font-size: 16px;
		font-weight: normal;
	}

	.form-group {
		margin: 0;
	}

	.form-group:not(:last-child) {
		margin-bottom: 10px;
	}

	.panel-group {
		margin-bottom: 0;
	}
</style>

<div aria-multiselectable="true" class="panel-group" id="accordion01" role="tablist">
	<div class="panel panel-default">
		<div class="panel-heading" role="tab">
			<div class="panel-title">
				<a aria-controls="collapseOne" aria-expanded="true" class="collapse-icon expanded" data-parent="#accordion00" data-toggle="collapse" href="#panelCollapse00" role="button">
					<liferay-ui:message key="asset-entries" />

					<liferay-ui:icon cssClass="collapse-icon-closed" icon="angle-right" markupView="lexicon" />
					<liferay-ui:icon cssClass="collapse-icon-open" icon="angle-down" markupView="lexicon" />
				</a>
			</div>
		</div>

		<div aria-labelledby="heading00" class="collapse in panel-collapse" id="panelCollapse00" role="tabpanel">
			<div class="panel-body">
				<div id="facetContainer">
					<aui:input cssClass="default facet-value" label="Any Asset" name="facetValue" title="search" type="checkbox" />

					<aui:input cssClass="facet-value text-default" label="Blogs Entry (2)" name="AssetTypeCheckbox" title="blogsEntry" type="checkbox" />

					<aui:input cssClass="facet-value text-default" label="Message Boards Message (1)" name="AssetTypeCheckbox" title="blogsEntry" type="checkbox" />

					<aui:input cssClass="facet-value text-default" label="Web Content Article (1)" name="AssetTypeCheckbox" title="blogsEntry" type="checkbox" />
				</div>
			</div>
		</div>
	</div>
</div>