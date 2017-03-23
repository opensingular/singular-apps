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

package org.opensingular.server.core.wicket.box;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.opensingular.flow.persistence.entity.ProcessGroupEntity;
import org.opensingular.server.commons.persistence.filter.QuickFilter;
import org.opensingular.server.commons.service.dto.BoxConfigurationMetadata;
import org.opensingular.server.commons.service.dto.ItemBoxMetadata;
import org.opensingular.server.commons.spring.security.SingularUserDetails;
import org.opensingular.server.commons.wicket.SingularSession;
import org.opensingular.server.commons.wicket.error.AccessDeniedContent;
import org.opensingular.server.commons.wicket.view.template.Content;
import org.opensingular.server.commons.wicket.view.template.MenuService;
import org.opensingular.server.core.wicket.template.ServerTemplate;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.opensingular.server.commons.wicket.view.util.DispatcherPageParameters.ITEM_PARAM_NAME;
import static org.opensingular.server.commons.wicket.view.util.DispatcherPageParameters.MENU_PARAM_NAME;
import static org.opensingular.server.commons.wicket.view.util.DispatcherPageParameters.PROCESS_GROUP_PARAM_NAME;

public class BoxPage extends ServerTemplate {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BoxPage.class);

    @Inject
    private transient Optional<MenuService> menuService;

    @Override
    protected Content getContent(String id) {

        String processGroupCod = getPageParameters().get(PROCESS_GROUP_PARAM_NAME).toOptionalString();
        String menu            = getPageParameters().get(MENU_PARAM_NAME).toOptionalString();
        String item            = getPageParameters().get(ITEM_PARAM_NAME).toOptionalString();


        if (processGroupCod == null
                && menu == null
                && item == null
                && menuService.isPresent()) {

            for (Iterator<Map.Entry<ProcessGroupEntity, List<BoxConfigurationMetadata>>> it = menuService.get().getMap().entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<ProcessGroupEntity, List<BoxConfigurationMetadata>> entry = it.next();
                if (!entry.getValue().isEmpty()) {
                    processGroupCod = entry.getKey().getCod();
                    BoxConfigurationMetadata mg = entry.getValue().get(0);
                    menu = mg.getLabel();
                    item = mg.getItemBoxes().get(0).getName();
                    PageParameters pageParameters = new PageParameters();
                    pageParameters.add(PROCESS_GROUP_PARAM_NAME, processGroupCod);
                    pageParameters.add(MENU_PARAM_NAME, menu);
                    pageParameters.add(ITEM_PARAM_NAME, item);
                    throw new RestartResponseException(getPage().getClass(), pageParameters);
                }
            }

        }

        BoxConfigurationMetadata boxConfigurationMetadata = null;
        if (menuService.isPresent()) {
            boxConfigurationMetadata = menuService.get().getMenuByLabel(menu);
        }

        if (boxConfigurationMetadata != null) {
            final ItemBoxMetadata itemBoxMetadata = boxConfigurationMetadata.getItemPorLabel(item);
            /**
             * itemBoxDTO pode ser nulo quando nenhum item está selecionado.
             */
            if (itemBoxMetadata != null) {
                return newBoxContent(id, processGroupCod, boxConfigurationMetadata, itemBoxMetadata);
            }
        }

        /**
         * Fallback
         */
        LOGGER.warn("Não existe correspondencia para o label {}", String.valueOf(item));
        return new AccessDeniedContent(id);
    }

    protected BoxContent newBoxContent(String id, String processGroupCod, BoxConfigurationMetadata boxConfigurationMetadata, ItemBoxMetadata itemBoxMetadata) {
        return new BoxContent(id, processGroupCod, boxConfigurationMetadata.getLabel(), itemBoxMetadata);
    }

    protected Map<String, String> createLinkParams() {
        return new HashMap<>();
    }

    protected QuickFilter createFilter() {
        return new QuickFilter()
                .withIdUsuarioLogado(getIdUsuario());
    }

    protected String getIdUsuario() {
        SingularUserDetails userDetails = SingularSession.get().getUserDetails();
        return Optional.ofNullable(userDetails)
                .map(SingularUserDetails::getUsername)
                .orElse(null);
    }

}