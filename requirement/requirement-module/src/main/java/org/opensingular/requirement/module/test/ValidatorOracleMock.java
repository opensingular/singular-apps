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

package org.opensingular.requirement.module.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opensingular.requirement.module.admin.healthsystem.validation.database.ValidatorOracle;
import org.opensingular.requirement.module.persistence.dto.healthsystem.ColumnInfoDTO;
import org.opensingular.requirement.module.persistence.dto.healthsystem.SequenceInfoDTO;

public class ValidatorOracleMock extends ValidatorOracle {

    private boolean mockEncontradoBanco = true;

    @Override
    protected List<ColumnInfoDTO> getColumnsInfoFromTable(String table) {
        ColumnInfoDTO column = new ColumnInfoDTO();

        column.setSchema("");
        column.setColumnName("");
        column.setDataType("");
        column.setTableName("");
        column.setNullable(false);
        column.setFoundDataBase(true);

        List<ColumnInfoDTO> columns = new ArrayList<>();
        columns.add(column);

        return columns;
    }

    @Override
    protected List<String> getPermissionSpecificTable(String table) {
        if(mockEncontradoBanco){
            mockEncontradoBanco = !mockEncontradoBanco;
            return Arrays.asList("SELECT", "UPDATE", "INSERT", "DELETE");
        }else{
            return Arrays.asList("SELECT", "UPDATE", "DELETE");
        }
    }

    @Override
    protected SequenceInfoDTO getSequenceInfoDTO(String sequenceName) {
        SequenceInfoDTO info = new SequenceInfoDTO();

        info.setSequenceName(sequenceName);
        info.setFound(true);

        return info;
    }

}
