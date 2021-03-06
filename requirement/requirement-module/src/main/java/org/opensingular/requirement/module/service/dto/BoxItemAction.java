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

package org.opensingular.requirement.module.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.opensingular.lib.commons.ui.Icon;
import org.opensingular.lib.commons.util.ObjectUtils;
import org.opensingular.requirement.module.flow.controllers.IController;
import org.opensingular.requirement.module.form.FormAction;
import org.opensingular.requirement.module.jackson.IconJsonDeserializer;
import org.opensingular.requirement.module.jackson.IconJsonSerializer;

import java.io.Serializable;

public class BoxItemAction implements Serializable {

    private String                 endpoint;
    private FormAction             formAction;
    private String                 requirementId;
    private String                 name;
    private boolean                defaultAction;
    private ItemActionConfirmation confirmation;
    private String                 label;
    private Icon                   icon;
    private ItemActionType         type;
    private String                 controllerClassName;


    public BoxItemAction() {
    }

    public BoxItemAction(String name, String label, Icon icon, ItemActionType type, String endpoint, Class<? extends IController> controller, ItemActionConfirmation confirmation) {
        this.name = name;
        this.endpoint = endpoint;
        this.label = label;
        this.icon = icon;
        this.type = type;
        this.controllerClassName = controller != null ? controller.getName() : null;
        this.confirmation = confirmation;
    }

    public BoxItemAction(String name, String label, Icon icon, ItemActionType type, FormAction fomAction, String endpoint) {
        this.name = name;
        this.label = label;
        this.icon = icon;
        this.type = type;
        this.formAction = fomAction;
        this.endpoint = endpoint;
        defaultAction = false;
    }


    @Deprecated
    public String getEndpoint() {
        return endpoint;
    }

    @Deprecated
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @JsonIgnore
    public boolean isUseExecute() {
        return ItemActionType.EXECUTE == type;
    }


    public FormAction getFormAction() {
        return formAction;
    }

    public void setFormAction(FormAction formAction) {
        this.formAction = formAction;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(String requirementId) {
        this.requirementId = requirementId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(boolean defaultAction) {
        this.defaultAction = defaultAction;
    }

    public ItemActionType getType() {
        return type;
    }

    public void setType(ItemActionType type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonSerialize(using = IconJsonSerializer.class)
    public Icon getIcon() {
        return icon;
    }

    @JsonDeserialize(using = IconJsonDeserializer.class)
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public ItemActionConfirmation getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(ItemActionConfirmation confirmation) {
        this.confirmation = confirmation;
    }


    @SuppressWarnings("unchecked")
    @JsonIgnore
    public Class<? extends IController> getController() {
        return ObjectUtils.loadClass(controllerClassName, IController.class);
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    public void setControllerClassName(String controllerClassName) {
        this.controllerClassName = controllerClassName;
    }
}
