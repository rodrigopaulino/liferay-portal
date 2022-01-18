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

import ClayCard from '@clayui/card';
import ClayIcon from '@clayui/icon';
import React from 'react';

const DownloadCard: React.FC<IProps> = ({title, url}) => {
	return (
		<ClayCard horizontal>
			<ClayCard.Body>
				<div className="card-col-content card-col-gutters">
					<h4 className="text-truncate" title={title}>
						{title}
					</h4>
				</div>

				<div className="card-col-field">
					<a download={title} href={url}>
						<ClayIcon symbol="download" />
					</a>
				</div>
			</ClayCard.Body>
		</ClayCard>
	);
};

export default DownloadCard;

interface IProps {
	title: string;
	url: string;
}
