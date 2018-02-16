/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.opensingular.server.commons.test;

import org.opensingular.form.SType;
import org.opensingular.server.commons.flow.FlowResolver;
import org.opensingular.server.commons.requirement.SingularRequirement;
import org.opensingular.server.commons.service.SingularRequirementService;
import org.opensingular.server.commons.wicket.view.util.ActionContext;

import javax.inject.Named;
import java.util.Optional;

@Named
public class SingularRequirementServiceMock implements SingularRequirementService {

    @Override
    public SingularRequirement getSingularRequirement(ActionContext context) {
        return new SingularRequirement() {
            @Override
            public String getName() {
                return "FooRequirement";
            }

            @Override
            public Class<? extends SType> getMainForm() {
                return null;
            }

            @Override
            public FlowResolver getFlowResolver() {
                return (FlowResolver) (cfg, iRoot) -> Optional.of(FOOFlow.class);
            }
        };
    }
}