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

package com.liferay.dynamic.data.mapping.service.persistence.impl;

import com.liferay.dynamic.data.mapping.model.DDMFormInstanceVersion;
import com.liferay.dynamic.data.mapping.model.impl.DDMFormInstanceVersionImpl;
import com.liferay.dynamic.data.mapping.service.persistence.DDMFormInstanceVersionFinder;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.dao.orm.custom.sql.CustomSQL;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.SQLQuery;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.Type;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.security.permission.InlineSQLHelperUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Iterator;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodrigo Paulino
 */
@Component(service = DDMFormInstanceVersionFinder.class)
public class DDMFormInstanceVersionFinderImpl
	extends DDMFormInstanceVersionFinderBaseImpl
	implements DDMFormInstanceVersionFinder {

	public static final String COUNT_BY_C_G_N_D =
		DDMFormInstanceVersionFinder.class.getName() + ".countByC_G_N_D";

	public static final String FIND_BY_C_G_N_D =
		DDMFormInstanceVersionFinder.class.getName() + ".findByC_G_N_D";

	@Override
	public int countByKeywords(long companyId, long groupId, String keywords) {
		return doCountByKeywords(companyId, groupId, keywords, false);
	}

	@Override
	public int countByC_G_N_D(
		long companyId, long groupId, String[] names, String[] descriptions,
		boolean andOperator) {

		return doCountByC_G_N_D(
			companyId, groupId, names, descriptions, andOperator, false);
	}

	@Override
	public int filterCountByKeywords(
		long companyId, long groupId, String keywords) {

		return doCountByKeywords(companyId, groupId, keywords, true);
	}

	@Override
	public int filterCountByC_G_N_D(
		long companyId, long groupId, String[] names, String[] descriptions,
		boolean andOperator) {

		return doCountByC_G_N_D(
			companyId, groupId, names, descriptions, andOperator, true);
	}

	@Override
	public List<DDMFormInstanceVersion> filterFindByKeywords(
		long companyId, long groupId, String keywords, int start, int end,
		OrderByComparator<DDMFormInstanceVersion> orderByComparator) {

		return doFindByKeywords(
			companyId, groupId, keywords, start, end, orderByComparator, true);
	}

	@Override
	public List<DDMFormInstanceVersion> filterFindByC_G_N_D(
		long companyId, long groupId, String[] names, String[] descriptions,
		boolean andOperator, int start, int end,
		OrderByComparator<DDMFormInstanceVersion> orderByComparator) {

		return doFindByC_G_N_D(
			companyId, groupId, names, descriptions, andOperator, start, end,
			orderByComparator, true);
	}

	@Override
	public List<DDMFormInstanceVersion> findByKeywords(
		long companyId, long groupId, String keywords, int start, int end,
		OrderByComparator<DDMFormInstanceVersion> orderByComparator) {

		return doFindByKeywords(
			companyId, groupId, keywords, start, end, orderByComparator, false);
	}

	@Override
	public List<DDMFormInstanceVersion> findByC_G_N_D(
		long companyId, long groupId, String[] names, String[] descriptions,
		boolean andOperator, int start, int end,
		OrderByComparator<DDMFormInstanceVersion> orderByComparator) {

		return doFindByC_G_N_D(
			companyId, groupId, names, descriptions, andOperator, start, end,
			orderByComparator, false);
	}

	protected int doCountByKeywords(
		long companyId, long groupId, String keywords,
		boolean inlineSQLHelper) {

		String[] names = null;
		String[] descriptions = null;
		boolean andOperator = false;

		if (Validator.isNotNull(keywords)) {
			names = _customSQL.keywords(keywords);
			descriptions = _customSQL.keywords(keywords, false);
		}
		else {
			andOperator = true;
		}

		return doCountByC_G_N_D(
			companyId, groupId, names, descriptions, andOperator,
			inlineSQLHelper);
	}

	protected int doCountByC_G_N_D(
		long companyId, long groupId, String[] names, String[] descriptions,
		boolean andOperator, boolean inlineSQLHelper) {

		names = _customSQL.keywords(names);
		descriptions = _customSQL.keywords(descriptions, false);

		Session session = null;

		try {
			session = openSession();

			String sql = _customSQL.get(getClass(), COUNT_BY_C_G_N_D);

			if (inlineSQLHelper) {
				sql = InlineSQLHelperUtil.replacePermissionCheck(
					sql, DDMFormInstanceVersion.class.getName(),
					"DDMFormInstanceVersion.formInstanceVersionId", groupId);
			}

			if (groupId <= 0) {
				sql = StringUtil.removeSubstring(
					sql, "(DDMFormInstanceVersion.groupId = ?) AND");
			}

			sql = _customSQL.replaceKeywords(
				sql, "LOWER(DDMFormInstanceVersion.name)", StringPool.LIKE,
				false, names);
			sql = _customSQL.replaceKeywords(
				sql, "DDMFormInstanceVersion.description", StringPool.LIKE,
				true, descriptions);
			sql = _customSQL.replaceAndOperator(sql, andOperator);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addScalar(COUNT_COLUMN_NAME, Type.LONG);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			if (groupId > 0) {
				queryPos.add(groupId);
			}

			queryPos.add(companyId);
			queryPos.add(names, 2);
			queryPos.add(descriptions, 2);

			Iterator<Long> iterator = sqlQuery.iterate();

			if (iterator.hasNext()) {
				Long count = iterator.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected List<DDMFormInstanceVersion> doFindByKeywords(
		long companyId, long groupId, String keywords, int start, int end,
		OrderByComparator<DDMFormInstanceVersion> orderByComparator,
		boolean inlineSQLHelper) {

		String[] names = null;
		String[] descriptions = null;
		boolean andOperator = false;

		if (Validator.isNotNull(keywords)) {
			names = _customSQL.keywords(keywords);
			descriptions = _customSQL.keywords(keywords, false);
		}
		else {
			andOperator = true;
		}

		return doFindByC_G_N_D(
			companyId, groupId, names, descriptions, andOperator, start, end,
			orderByComparator, inlineSQLHelper);
	}

	protected List<DDMFormInstanceVersion> doFindByC_G_N_D(
		long companyId, long groupId, String[] names, String[] descriptions,
		boolean andOperator, int start, int end,
		OrderByComparator<DDMFormInstanceVersion> orderByComparator,
		boolean inlineSQLHelper) {

		names = _customSQL.keywords(names);
		descriptions = _customSQL.keywords(descriptions, false);

		Session session = null;

		try {
			session = openSession();

			String sql = _customSQL.get(getClass(), FIND_BY_C_G_N_D);

			if (inlineSQLHelper) {
				sql = InlineSQLHelperUtil.replacePermissionCheck(
					sql, DDMFormInstanceVersion.class.getName(),
					"DDMFormInstanceVersion.formInstanceVersionId", groupId);
			}

			if (groupId <= 0) {
				sql = StringUtil.removeSubstring(
					sql, "(DDMFormInstanceVersion.groupId = ?) AND");
			}

			sql = _customSQL.replaceKeywords(
				sql, "LOWER(DDMFormInstanceVersion.name)", StringPool.LIKE,
				false, names);
			sql = _customSQL.replaceKeywords(
				sql, "DDMFormInstanceVersion.description", StringPool.LIKE,
				true, descriptions);
			sql = _customSQL.replaceAndOperator(sql, andOperator);
			sql = _customSQL.replaceOrderBy(sql, orderByComparator);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addEntity(
				"DDMFormInstanceVersion", DDMFormInstanceVersionImpl.class);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			if (groupId > 0) {
				queryPos.add(groupId);
			}

			queryPos.add(companyId);
			queryPos.add(names, 2);
			queryPos.add(descriptions, 2);

			return (List<DDMFormInstanceVersion>)QueryUtil.list(
				sqlQuery, getDialect(), start, end);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Reference
	private CustomSQL _customSQL;

}