/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.server.common.dao.impl.sql;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_ALIAS;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.APPLICATION_REFERENCE;
import static org.kaaproject.kaa.server.common.dao.impl.sql.HibernateDaoConstants.SEQUENCE_NUMBER_PROPERTY;

import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.kaaproject.kaa.server.common.dao.impl.HistoryDao;
import org.kaaproject.kaa.server.common.dao.model.sql.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateHistoryDao extends HibernateAbstractDao<History> implements HistoryDao<History> {

    private static final Logger LOG = LoggerFactory.getLogger(HibernateHistoryDao.class);

    @Override
    public List<History> findByAppId(String appId) {
        List<History> histories = Collections.emptyList();
        LOG.debug("Find history by application id {}", appId);
        if (isNotBlank(appId)) {
            histories = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)));
        }
        return histories;
    }

    @Override
    public History findBySeqNumber(String appId, int seqNum) {
        History history = null;
        LOG.debug("Find history by application id {} and sequence number {}", appId, seqNum);
        if (isNotBlank(appId)) {
            history = findOneByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.eq(SEQUENCE_NUMBER_PROPERTY, seqNum)));
        }
        return history;
    }

    @Override
    public List<History> findBySeqNumberStart(String appId, int startSeqNum) {
        List<History> histories = Collections.emptyList();
        LOG.debug("Find history by application id {} start sequence number {}", appId, startSeqNum);
        if (isNotBlank(appId)) {
            histories = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.gt(SEQUENCE_NUMBER_PROPERTY, startSeqNum)));
        }
        return histories;
    }

    @Override
    public List<History> findBySeqNumberRange(String appId, int startSeqNum, int endSeqNum) {
        List<History> histories = Collections.emptyList();
        LOG.debug("Find history by application id {} start sequence number {} and end {}", appId, startSeqNum, endSeqNum);
        if (isNotBlank(appId)) {
            histories = findListByCriterionWithAlias(APPLICATION_PROPERTY, APPLICATION_ALIAS,
                    Restrictions.and(
                            Restrictions.eq(APPLICATION_REFERENCE, Long.valueOf(appId)),
                            Restrictions.gt(SEQUENCE_NUMBER_PROPERTY, startSeqNum),
                            Restrictions.le(SEQUENCE_NUMBER_PROPERTY, endSeqNum)));
        }
        return histories;
    }

    @Override
    protected Class<History> getEntityClass() {
        return History.class;
    }
}
