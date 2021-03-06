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

package org.opensingular.requirement.module.flow.controllers;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.opensingular.requirement.module.box.action.ActionRequest;
import org.opensingular.requirement.module.box.action.ActionResponse;
import org.opensingular.requirement.module.service.RequirementInstance;
import org.opensingular.requirement.module.spring.security.AuthorizationService;

public abstract class IController {

    protected enum Type {
        PROCESS, FORM
    }
    @Inject
    private AuthorizationService authorizationService;

    public ActionResponse run(RequirementInstance requirement, ActionRequest actionRequest) {
        if (hasPermission(requirement, actionRequest)) {
            return execute(requirement, actionRequest);
        } else {
            return new ActionResponse("Você não tem permissão para executar esta ação.", false);
        }
    }

    private boolean hasPermission(RequirementInstance requirement, ActionRequest actionRequest) {
        return authorizationService.hasPermission(requirement.getCod(), null, actionRequest.getIdUsuario(), actionRequest.getAction().getName());
    }

    protected abstract ActionResponse execute(@Nonnull RequirementInstance requirement, ActionRequest actionRequest);

    protected Type getType() {
        return Type.PROCESS;
    }
}
