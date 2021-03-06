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

package org.opensingular.requirement.module.persistence.dao;

import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.opensingular.lib.support.persistence.SimpleDAO;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Map;

@Named
public class HealthSystemDAO extends SimpleDAO {

    @Inject
    private SessionFactory sessionFactory;

	@Transactional
	public Map<String, ClassMetadata> getAllDbMetaData(){
		//TODO: evoluir para dar suporte ao Hibernate 5
        //https://vladmihalcea.com/how-to-get-access-to-database-table-metadata-with-hibernate-5/
        //return sessionFactory.getAllClassMetadata();
        return Collections.emptyMap();
	}
	
	@Transactional
	public String getHibernateDialect(){
		return ((SessionFactoryImpl)sessionFactory).getDialect().toString();
	}

}
