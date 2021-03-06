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

package org.opensingular.requirement.commons.admin.healthsystem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.opensingular.requirement.commons.SingularCommonsBaseTest;
import org.opensingular.requirement.module.admin.healthsystem.validation.database.IValidatorDatabase;
import org.opensingular.requirement.module.persistence.dto.healthsystem.ColumnInfoDTO;
import org.opensingular.requirement.module.persistence.dto.healthsystem.TableInfoDTO;
import org.opensingular.requirement.module.test.SingularServletContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;

@TestExecutionListeners(listeners = {SingularServletContextTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ValidatorTest extends SingularCommonsBaseTest {
    
    @Inject
    private IValidatorDatabase validatorDatabase;

    @Before
    public void setUp() {
        reset(validatorDatabase);
    }

    @Test
    public void getTablesPermission() {
        TableInfoDTO       tableInfoDTO = new TableInfoDTO();
        tableInfoDTO.setSchema("DBSINGULAR");
        tableInfoDTO.setTableName("TB_REQUISICAO");
        List<TableInfoDTO> permissions = validatorDatabase.getTablesPermission(Collections.singletonList(tableInfoDTO));

        assertEquals(1L, permissions.size());
    }

    @Test
    public void checkColumnPermissions() {
        TableInfoDTO       tableInfoDTO = new TableInfoDTO();
        tableInfoDTO.setSchema("DBSINGULAR");
        tableInfoDTO.setTableName("TB_REQUISICAO");

        ColumnInfoDTO cod = new ColumnInfoDTO("CO_REQUISICAO", false);
        ColumnInfoDTO instance = new ColumnInfoDTO("CO_INSTANCIA_PROCESSO", false);
        tableInfoDTO.setColumnsInfo(Arrays.asList(cod, instance));

        validatorDatabase.checkColumnPermissions(tableInfoDTO);
    }

    @Test
    public void checkSequences() {
        validatorDatabase.checkSequences(Collections.singletonList(""));
    }

    @Test
    public void getAllInfoTable() {
        validatorDatabase.getAllInfoTable(Collections.singletonList(""));
    }

    @Test
    public void checkAllInfoTable() {
        TableInfoDTO       tableInfoDTO = new TableInfoDTO();
        tableInfoDTO.setSchema("DBSINGULAR");
        tableInfoDTO.setTableName("TB_REQUISICAO");

        validatorDatabase.checkAllInfoTable(Collections.singletonList(tableInfoDTO));
    }
    
}
