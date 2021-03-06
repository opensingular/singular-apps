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

package org.opensingular.app.commons.spring.persistence.database;

import java.util.List;

/**
 * If this interface will be overriding for specific Dialect,the method  <code> supportsIdentityColumns()</code> should be used to declare if will use or not identity.
 * If the method supportsIdentityColumns() return false, the Singular will use the sequence <code>DBSINGULAR.SQ_CO_ATOR</code> to increment the table TB_ACTOR
 **/
public interface SingularDataBaseSuport {

    /**
     * Some scripts files to execute after Singular DataBase be created.
     *
     * @return A list contain all Script files.
     */
    List<String> getScripts();

    /**
     * Method that return the script file of Actor.
     *
     * @return The script file of Actor.
     */
    String getDefaultActorScript();


    /**
     * Method that return the script file of Quartz.
     * <p>
     * If the project don't use Quartz database, this script could be empty.
     * Property:<code>singular.quartz.jobstore.enabled</code>
     * <p>
     * We do not recommend using Quartz in memory for Cluster applications.
     *
     * For more information see the Quartz source code: <code>https://github.com/quartz-scheduler/quartz/releases/tag/quartz-2.2.3</code>
     * @return The script file of Quartz.
     */
    String getQuartzScript();
}
