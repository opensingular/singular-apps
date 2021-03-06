/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.requirement.module.persistence.dao.form;

import org.opensingular.lib.support.persistence.BaseDAO;
import org.opensingular.requirement.module.persistence.entity.form.RequirementDefinitionEntity;
import org.opensingular.requirement.module.persistence.entity.form.RequirementEntity;

@SuppressWarnings("unchecked")
public class RequirementDefinitionDAO<T extends RequirementDefinitionEntity> extends BaseDAO<T, Long> {

    public RequirementDefinitionDAO() {
        super((Class<T>) RequirementDefinitionEntity.class);
    }

    public RequirementDefinitionDAO(Class<T> entityClass) {
        super(entityClass);
    }

    public T findByKey(String codModulo, String key) {
        StringBuilder hql = new StringBuilder();

        hql.append(" FROM ");
        hql.append(RequirementDefinitionEntity.class.getName());
        hql.append(" as req ");
        hql.append(" WHERE req.module.cod = :codModulo ");
        hql.append("    AND req.key = :key ");

        return (T) getSession().createQuery(hql.toString())
                .setParameter("codModulo", codModulo)
                .setParameter("key", key)
                .uniqueResult();
    }

    public T findRequirementDefinitionByRequrimentId(Long requirementId) {
        StringBuilder hql = new StringBuilder();

        hql.append(" select reqdef ");
        hql.append(" FROM ");
        hql.append(RequirementEntity.class.getName());
        hql.append(" as req ");
        hql.append(" inner join req.requirementDefinitionEntity as reqdef ");
        hql.append("    where req.cod = :cod ");

        return (T) getSession().createQuery(hql.toString())
                .setParameter("cod", requirementId)
                .uniqueResult();
    }
}