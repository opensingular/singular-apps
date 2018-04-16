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

package org.opensingular.server.commons.wicket.buttons;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.opensingular.lib.commons.lambda.ISupplier;
import org.opensingular.lib.wicket.util.metronic.menu.DropdownMenu;
import org.opensingular.server.commons.form.FormAction;
import org.opensingular.server.commons.service.dto.RequirementData;
import org.opensingular.server.commons.wicket.view.util.DispatcherPageUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.opensingular.lib.wicket.util.util.WicketUtils.$b;
import static org.opensingular.lib.wicket.util.util.WicketUtils.$m;
import static org.opensingular.server.commons.wicket.view.util.ActionContext.REQUIREMENT_DEFINITION_ID;

public class NewRequirementLink extends Panel {

    private final String                        url;
    private final Map<String, String>           params;
    private       IModel<List<RequirementData>> requirements;
    private IModel<String> labelModel = new StringResourceModel("label.button.insert", this, null);

    public NewRequirementLink(String id, String url, Map<String, String> params, IModel<List<RequirementData>> requirements) {
        this(id, null, url, params, requirements);
    }

    public NewRequirementLink(String id, IModel<String> labelModel, String url, Map<String, String> params, IModel<List<RequirementData>> requirements) {
        super(id);
        this.url = url;
        this.params = params;
        this.labelModel = labelModel == null ? this.labelModel : labelModel;
        this.requirements = requirements;
        buildButtons();
    }

    protected void buildButtons() {
        addSingleButton(() -> Optional.ofNullable(requirements.getObject()).map(List::size).orElse(0) == 1);
        addDropdownButton(() -> Optional.ofNullable(requirements.getObject()).map(List::size).orElse(0) > 1);
    }

    protected void addSingleButton(ISupplier<Boolean> visibleSupplier) {
        Optional<RequirementData> findFirst = requirements.getObject().stream().findFirst();
        if(findFirst.isPresent()){
            Link<String> newButton = buildLink("_botao", labelModel, findFirst.get());
            newButton.add($b.visibleIf(visibleSupplier));
            this.add(newButton);
        }
    }

    private Link<String> buildLink(String id, IModel<String> labelModel, RequirementData requirement) {
        Link botao = new Link(id) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                this.add($b.attr("href", NewRequirementLink.this.buildURL(requirement)));
                this.add($b.attr("target", "_blank"));
                this.setBody(labelModel);
            }

            @Override
            public void onClick() {
            }
        };
        return botao;
    }

    protected void addDropdownButton(ISupplier<Boolean> visibleSupplier) {
        DropdownMenu dropdownMenu = new DropdownMenu("_novos");
        dropdownMenu.add($b.visibleIf(visibleSupplier));
        dropdownMenu.add($b.onConfigure(c -> {
            if (visibleSupplier.get()) {
                for (RequirementData r : requirements.getObject()) {
                    dropdownMenu.adicionarMenu(id -> buildLink(id, $m.ofValue(r.getLabel()), r));
                }
            }
        }));
        this.add(dropdownMenu);
    }

    protected String buildURL(RequirementData requirement) {
        String result = DispatcherPageUtil
                .baseURL(url)
                .formAction(FormAction.FORM_FILL.getId())
                .requirementId(null)
                .param(REQUIREMENT_DEFINITION_ID, requirement.getId())
                .params(params)
                .build();
        return result;

    }

    @Override
    protected boolean getStatelessHint() {
        return true;
    }


}