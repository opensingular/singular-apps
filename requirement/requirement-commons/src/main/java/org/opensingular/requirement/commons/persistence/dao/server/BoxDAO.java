/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.requirement.commons.persistence.dao.server;

import org.opensingular.flow.persistence.entity.ModuleEntity;
import org.opensingular.lib.support.persistence.BaseDAO;
import org.opensingular.requirement.commons.persistence.entity.form.BoxEntity;

public class BoxDAO extends BaseDAO<BoxEntity, Long> {

    public BoxDAO() {
        super(BoxEntity.class);
    }

    public BoxEntity findByModuleAndName(ModuleEntity moduleEntity, String name) {
        StringBuilder hql = new StringBuilder();

        hql.append(" FROM ");
        hql.append(BoxEntity.class.getName());
        hql.append(" as box ");
        hql.append(" WHERE box.module = :module ");
        hql.append("    AND box.name = :name ");

        return (BoxEntity) getSession().createQuery(hql.toString())
                .setParameter("module", moduleEntity)
                .setParameter("name", name)
                .uniqueResult();
    }

}
