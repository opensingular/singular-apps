/*
 * Copyright (c) 2016, Singular and/or its affiliates. All rights reserved.
 * Singular PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.server.commons.persistence.dao.flow;

import org.opensingular.flow.persistence.entity.ProcessGroupEntity;
import org.opensingular.lib.support.persistence.BaseDAO;
import org.hibernate.criterion.Restrictions;

import java.util.List;


public class GrupoProcessoDAO extends BaseDAO<ProcessGroupEntity, String> {

    public GrupoProcessoDAO() {
        super(ProcessGroupEntity.class);
    }

    public List<ProcessGroupEntity> listarTodosGruposProcesso() {
        return getSession().createCriteria(ProcessGroupEntity.class).list();
    }

    public ProcessGroupEntity findByName(String name) {
        return (ProcessGroupEntity) getSession()
                .createCriteria(ProcessGroupEntity.class)
                .add(Restrictions.ilike("name", name))
                .uniqueResult();
    }

}