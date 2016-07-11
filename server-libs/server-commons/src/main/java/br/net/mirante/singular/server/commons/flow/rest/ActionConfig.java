/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.server.commons.flow.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.net.mirante.singular.flow.core.property.MetaDataRef;

/**
 * Classe responsável por guardar a configuração de ações
 * disponível para o módulo
 */
public class ActionConfig {

    public static final MetaDataRef<ActionConfig> KEY = new MetaDataRef<>(ActionConfig.class.getName(), ActionConfig.class);

    private List<String> defaultActions;

    private Map<String, IController> customActions;

    public ActionConfig() {
        defaultActions = new ArrayList<>();
        customActions = new HashMap<>();
    }

    public Map<String, IController> getCustomActions() {
        return Collections.unmodifiableMap(customActions);
    }

    public ActionConfig addAction(String name, IController controller) {
        customActions.put(name, controller);
        return this;
    }

    public List<String> getDefaultActions() {
        return Collections.unmodifiableList(defaultActions);
    }

    public ActionConfig addDefaultAction(String action) {
        defaultActions.add(action);
        return this;
    }

    public IController getCustomAction(String name) {
        return customActions.get(name);
    }

    public boolean containsAction(String name) {
        return customActions.containsKey(name)
                || defaultActions.contains(name);
    }
}