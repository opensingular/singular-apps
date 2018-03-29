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

package org.opensingular.server.commons.admin.healthsystem;

import org.junit.Assert;
import org.junit.Test;
import org.opensingular.requirement.commons.admin.healthsystem.validation.database.ValidatorFactory;
import org.opensingular.requirement.commons.exception.SingularServerException;
import org.opensingular.server.commons.test.SingularCommonsBaseTest;


public class ValidatorFactoryTest extends SingularCommonsBaseTest {

    @Test(expected = SingularServerException.class)
    public void getValidatorOracleTest() {
        Assert.assertNotNull(ValidatorFactory.getValidator("Oracle"));

        ValidatorFactory.getValidator("NOTEXIST");
    }
}
