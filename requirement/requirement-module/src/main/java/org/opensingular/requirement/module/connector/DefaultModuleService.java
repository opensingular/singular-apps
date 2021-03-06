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

package org.opensingular.requirement.module.connector;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.request.Url;
import org.opensingular.flow.persistence.dao.ModuleDAO;
import org.opensingular.flow.persistence.entity.Actor;
import org.opensingular.flow.persistence.entity.ModuleEntity;
import org.opensingular.form.persistence.entity.FormTypeEntity;
import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.lib.support.spring.util.ApplicationContextProvider;
import org.opensingular.requirement.module.ActionProvider;
import org.opensingular.requirement.module.AuthorizationAwareActionProviderDecorator;
import org.opensingular.requirement.module.RequirementDefinition;
import org.opensingular.requirement.module.SingularModule;
import org.opensingular.requirement.module.box.BoxItemDataImpl;
import org.opensingular.requirement.module.box.BoxItemDataList;
import org.opensingular.requirement.module.box.BoxItemDataMap;
import org.opensingular.requirement.module.box.action.ActionRequest;
import org.opensingular.requirement.module.box.action.ActionResponse;
import org.opensingular.requirement.module.config.IServerContext;
import org.opensingular.requirement.module.exception.SingularServerException;
import org.opensingular.requirement.module.flow.controllers.IController;
import org.opensingular.requirement.module.persistence.dao.form.RequirementDefinitionDAO;
import org.opensingular.requirement.module.persistence.entity.form.RequirementDefinitionEntity;
import org.opensingular.requirement.module.persistence.filter.BoxFilter;
import org.opensingular.requirement.module.persistence.filter.BoxFilterFactory;
import org.opensingular.requirement.module.service.RequirementService;
import org.opensingular.requirement.module.service.dto.BoxItemAction;
import org.opensingular.requirement.module.service.dto.ItemActionConfirmation;
import org.opensingular.requirement.module.spring.security.AuthorizationService;
import org.opensingular.requirement.module.spring.security.PermissionResolverService;
import org.opensingular.requirement.module.workspace.BoxDefinition;
import org.springframework.beans.factory.ObjectFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
public class DefaultModuleService implements ModuleService, Loggable {

    @Inject
    private SingularModule singularModule;

    @Inject
    private RequirementService requirementService;

    @Inject
    private AuthorizationService authorizationService;

    @Inject
    private PermissionResolverService permissionResolverService;

    @Inject
    private RequirementDefinitionDAO<RequirementDefinitionEntity> requirementDefinitionDAO;

    @Inject
    private ModuleDAO moduleDAO;

    @Inject
    private BoxFilterFactory boxFilterFactory;

    @Inject
    private ObjectFactory<IServerContext> serverContextObjectFactory;

    @Override
    public String countAllCounters(BoxDefinition box) {
        return String.valueOf(countFiltered(box, boxFilterFactory.create(box).setBoxCountQuery(true)));
    }

    @Override
    public long countFiltered(BoxDefinition box, BoxFilter filter) {
        return count(box, filter);
    }

    @Override
    public List<BoxItemDataMap> searchFiltered(BoxDefinition box, BoxFilter filter) {
        return search(box, filter).getBoxItemDataList().stream().map(BoxItemDataMap::new).collect(Collectors.toList());
    }

    @Override
    public List<Actor> findEligibleUsers(BoxItemDataMap rowItemData, ItemActionConfirmation confirmAction) {
        return listAllowedUsers(rowItemData);

    }

    @Override
    public ActionResponse executeAction(BoxItemAction rowAction, Map<String, String> params, ActionRequest actionRequest) {
        Url.QueryParameter idQueryParam = Url.parse(rowAction.getEndpoint()).getQueryParameter("id");
        Long action = null;
        if (idQueryParam != null) {
            action = Long.valueOf(idQueryParam.getValue());
        }
        return executar(action, actionRequest);
    }

    @Override
    public String buildUrlToBeRedirected(BoxItemDataMap rowItemData, BoxItemAction rowAction, Map<String, String> params, String baseURI) {
        final BoxItemAction action = rowItemData.getActionByName(rowAction.getName());
        final String endpoint = StringUtils.trimToEmpty(action.getEndpoint());
        if (endpoint.startsWith("http")) {
            return endpoint;
        } else {
            return baseURI
                    + endpoint
                    + appendParameters(params);
        }
    }

    public Long count(BoxDefinition boxDefinition, BoxFilter filter) {
        return boxDefinition.getDataProvider().count(filter);
    }


    public BoxItemDataList search(BoxDefinition boxDefinition, BoxFilter filter) {
        return searchCheckingActionPermissions(boxDefinition, filter);
    }

    private String appendParameters(Map<String, String> additionalParams) {
        StringBuilder paramsValue = new StringBuilder();
        if (!additionalParams.isEmpty()) {
            for (Map.Entry<String, String> entry : additionalParams.entrySet()) {
                paramsValue.append(String.format("&%s=%s", entry.getKey(), entry.getValue()));
            }
        }
        return paramsValue.toString();
    }

    public ActionResponse executar(Long id, ActionRequest actionRequest) {
        try {
            IController controller = getActionController(actionRequest);
            return controller.run(requirementService.loadRequirementInstance(id), actionRequest);
        } catch (Exception e) {
            final String msg = String.format("Erro ao executar a ação %s para o id %d. ", StringEscapeUtils.escapeJava(actionRequest.getAction().getName()), id);
            getLogger().error(msg, e);//NOSONAR
            return new ActionResponse(msg, false);
        }
    }

    private IController getActionController(ActionRequest actionRequest) {
        try {
            return ApplicationContextProvider.get().getBean(actionRequest.getAction().getController());
        } catch (Exception e) {
            throw SingularServerException.rethrow(e.getMessage(), e);
        }
    }


    public boolean hasModuleAccess(String user) {
        return authorizationService.hasPermission(user, permissionResolverService.buildCategoryPermission(singularModule.abbreviation()).getSingularId());
    }

    public List<Actor> listAllowedUsers(Map<String, Object> selectedTask) {
        return requirementService.listAllowedUsers(selectedTask);
    }

    @Override
    public RequirementDefinitionEntity getOrCreateRequirementDefinition(RequirementDefinition<?> requirementDefinition, FormTypeEntity formType) {
        ModuleEntity module = getModule();
        RequirementDefinitionEntity requirementDefinitionEntity = requirementDefinitionDAO.findByKey(module.getCod(), requirementDefinition.getKey());

        if (requirementDefinitionEntity == null) {
            requirementDefinitionEntity = new RequirementDefinitionEntity();
            requirementDefinitionEntity.setFormType(formType);
            requirementDefinitionEntity.setKey(requirementDefinition.getKey());
            requirementDefinitionEntity.setModule(module);
            requirementDefinitionEntity.setName(requirementDefinition.getName());
        }

        return requirementDefinitionEntity;
    }

    /**
     * Retorna o módulo a que este código pertence.
     *
     * @return o módulo
     */
    @Override
    public ModuleEntity getModule() {
        return moduleDAO.findOrException(singularModule.abbreviation());
    }

    @Override
    public String getBaseUrl() {
        return getModuleContext() + serverContextObjectFactory.getObject().getSettings().getUrlPath();
    }

    /**
     * Evoluir para botão wicket
     */
    @Deprecated
    @Override
    public String getModuleContext() {
        final String groupConnectionURL = getModule().getConnectionURL();
        try {
            final String path = new URL(groupConnectionURL).getPath();
            if (path.endsWith("/")) {
                return path.substring(0, path.length() - 1);
            } else {
                return path;
            }
        } catch (Exception e) {
            throw SingularServerException.rethrow(String.format("Erro ao tentar fazer o parse da URL: %s", groupConnectionURL), e);
        }
    }

    @Override
    public String getBoxRowStyleClass(BoxDefinition boxDefinition, BoxItemDataMap boxItemDataMap) {
        return boxDefinition.getRowStyleClass(boxItemDataMap);
    }

    protected BoxItemDataList searchCheckingActionPermissions(BoxDefinition boxDefinition, BoxFilter filter) {
        List<Map<String, Serializable>> itens = boxDefinition.getDataProvider().search(filter);
        BoxItemDataList result = new BoxItemDataList();
        ActionProvider actionProvider = addBuiltInDecorators(boxDefinition.getDataProvider().getActionProvider());

        for (Map<String, Serializable> item : itens) {
            BoxItemDataImpl line = new BoxItemDataImpl();
            line.setRawMap(item);
            line.setBoxItemActions(actionProvider.getLineActions(line, filter));
            result.getBoxItemDataList().add(line);
        }
        return result;
    }

    protected ActionProvider addBuiltInDecorators(ActionProvider actionProvider) {
        return new AuthorizationAwareActionProviderDecorator(actionProvider);
    }
}