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

package org.opensingular.requirement.module.wicket.view;

import de.alpharogroup.wicket.js.addon.core.StringTextType;
import de.alpharogroup.wicket.js.addon.core.StringTextValue;
import de.alpharogroup.wicket.js.addon.core.ValueEnum;
import de.alpharogroup.wicket.js.addon.toastr.ToastrSettings;
import de.alpharogroup.wicket.js.addon.toastr.ToastrType;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.opensingular.lib.wicket.util.toastr.ToastrHelper;

public class SingularToastrHelper extends ToastrHelper {

    public SingularToastrHelper(Component component) {
        super(component);
    }

    public void addToastrMessageWorklist(ToastrType toastrType, String messageKey, String... args) {
        ToastrSettings settings = getDefaultSettings();
        settings.getToastrType().setValue(toastrType);

        String options = toStringJson(settings);
        String mensagem = getString(messageKey, args);
        AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
        String js = "Singular.exibirMensagemWorklist('%s', %s); ";
        target.appendJavaScript(String.format(js, mensagem, options));
    }

    private String toStringJson(ToastrSettings settings) {
        JSONWriter jsonWriter = new JSONStringer().object();

        for (StringTextValue<?> textValue : settings.asSet()) {
            String name = textValue.getName();
            name = name.substring(name.lastIndexOf('.') + 1);
            Object value;
            if (textValue.getType() == StringTextType.ENUM) {
                value = ((StringTextValue<? extends ValueEnum>) textValue).getValue().getValue();
            } else {
                value = textValue.getValue();
            }

            jsonWriter.key(name).value(value);
        }

        return jsonWriter.endObject().toString();
    }
}
