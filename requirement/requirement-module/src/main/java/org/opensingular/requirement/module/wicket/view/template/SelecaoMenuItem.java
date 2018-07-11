/*
 * Copyright (c) 2016, Singular and/or its affiliates. All rights reserved.
 * Singular PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.requirement.module.wicket.view.template;

import java.util.List;
import javax.inject.Inject;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.opensingular.flow.persistence.entity.ModuleEntity;
import org.opensingular.lib.wicket.util.behavior.BSSelectInitBehaviour;
import org.opensingular.lib.wicket.util.behavior.FormComponentAjaxUpdateBehavior;
import org.opensingular.lib.wicket.util.menu.AbstractMenuItem;
import org.opensingular.requirement.module.service.dto.BoxConfigurationData;
import org.opensingular.requirement.module.wicket.SingularSession;

import static org.opensingular.requirement.module.wicket.view.util.ActionContext.MENU_PARAM_NAME;
import static org.opensingular.requirement.module.wicket.view.util.ActionContext.MODULE_PARAM_NAME;

public class SelecaoMenuItem extends AbstractMenuItem {

    @Inject
    @SpringBean(required = false)
    private MenuService menuService;

    private List<ModuleEntity> categorias;

    public SelecaoMenuItem(List<ModuleEntity> categorias) {
        super("menu-item");
        this.categorias = categorias;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Form                form  = new Form<String>("form");
        Model<ModuleEntity> model = new Model<>(SingularSession.get().getCategoriaSelecionada());
        final DropDownChoice<ModuleEntity> select = new DropDownChoice<>("select", model, categorias,
                new ChoiceRenderer<>("name", "cod"));

        form.add(select);
        select.add(new BSSelectInitBehaviour());
        select.add(new FormComponentAjaxUpdateBehavior("change", (target, component) -> {
            final ModuleEntity categoriaSelecionada = (ModuleEntity) component.getDefaultModelObject();
            SingularSession.get().setCategoriaSelecionada(categoriaSelecionada);
            getPage().getPageParameters().set(MODULE_PARAM_NAME, categoriaSelecionada.getCod());
            final BoxConfigurationData boxConfigurationMetadataDTO = getDefaultMenuSelection(categoriaSelecionada);
            if (boxConfigurationMetadataDTO != null) {
                getPage().getPageParameters().set(MENU_PARAM_NAME, boxConfigurationMetadataDTO.getLabel());
            } else {
                getPage().getPageParameters().remove(MENU_PARAM_NAME);
            }
            setResponsePage(getPage().getClass(), getPage().getPageParameters());
        }));

        add(form);
    }

    private BoxConfigurationData getDefaultMenuSelection(ModuleEntity categoriaSelecionada) {
        return menuService.getDefaultSelectedMenu(categoriaSelecionada);
    }

    @Override
    protected boolean configureActiveItem() {
        return false;
    }

}