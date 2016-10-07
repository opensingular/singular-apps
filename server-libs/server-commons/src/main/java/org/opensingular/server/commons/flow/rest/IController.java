/*
 * Copyright (c) 2016, Singular and/or its affiliates. All rights reserved.
 * Singular PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.server.commons.flow.rest;

import javax.inject.Inject;

import org.opensingular.server.commons.persistence.entity.form.PetitionEntity;
import org.opensingular.server.commons.spring.security.AuthorizationService;

public abstract class IController {

    @Inject
    private AuthorizationService authorizationService;

    public ActionResponse run(PetitionEntity petition, ActionRequest actionRequest) {
        if (hasPermission(petition, actionRequest)) {
            return execute(petition, actionRequest);
        } else {
            return new ActionResponse("Você não tem permissão para executar esta ação.", false);
        }
    }

    private boolean hasPermission(PetitionEntity petition, ActionRequest actionRequest) {
        if (getType() == Type.PROCESS) {
            return authorizationService.hasPermission(petition, null, actionRequest.getIdUsuario(), getActionName());
        } else {
            return authorizationService.hasPermission(petition, null, actionRequest.getIdUsuario(), getActionName());
        }
    }

    public abstract String getActionName();

    protected abstract ActionResponse execute(PetitionEntity petition, ActionRequest actionRequest);

    protected Type getType() {
        return Type.PROCESS;
    }

    protected enum Type {
        PROCESS, FORM
    }
}