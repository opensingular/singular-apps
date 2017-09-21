package org.opensingular.server.module.rest;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opensingular.flow.core.Flow;
import org.opensingular.flow.core.FlowDefinition;
import org.opensingular.flow.core.STask;
import org.opensingular.flow.persistence.entity.Actor;
import org.opensingular.form.SFormUtil;
import org.opensingular.form.SInfoType;
import org.opensingular.form.SType;
import org.opensingular.form.context.SFormConfig;
import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.lib.support.spring.util.ApplicationContextProvider;
import org.opensingular.server.commons.box.BoxItemDataList;
import org.opensingular.server.commons.box.action.ActionRequest;
import org.opensingular.server.commons.box.action.ActionResponse;
import org.opensingular.server.commons.config.IServerContext;
import org.opensingular.server.commons.config.SingularServerConfiguration;
import org.opensingular.server.commons.exception.SingularServerException;
import org.opensingular.server.commons.flow.controllers.IController;
import org.opensingular.server.commons.flow.metadata.PetitionHistoryTaskMetaDataValue;
import org.opensingular.server.commons.persistence.entity.form.PetitionEntity;
import org.opensingular.server.commons.persistence.filter.QuickFilter;
import org.opensingular.server.commons.service.PetitionInstance;
import org.opensingular.server.commons.service.PetitionService;
import org.opensingular.server.commons.service.dto.BoxConfigurationData;
import org.opensingular.server.commons.service.dto.FormDTO;
import org.opensingular.server.commons.service.dto.ProcessDTO;
import org.opensingular.server.commons.spring.security.AuthorizationService;
import org.opensingular.server.commons.spring.security.PermissionResolverService;
import org.opensingular.server.module.BoxController;
import org.opensingular.server.module.SingularModuleConfiguration;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ModuleBackstageService implements Loggable {

    @Inject
    private PetitionService<PetitionEntity, PetitionInstance> petitionService;

    @Inject
    private SingularServerConfiguration singularServerConfiguration;

    @Inject
    private SingularModuleConfiguration singularModuleConfiguration;

    @Inject
    private AuthorizationService authorizationService;

    @Inject
    private PermissionResolverService permissionResolverService;

    @Inject
    @Named("formConfigWithDatabase")
    private SFormConfig<String> singularFormConfig;


    public List<BoxConfigurationData> listMenu(String context, String user) {
        return listMenu(IServerContext.getContextFromName(context, singularServerConfiguration.getContexts()), user);
    }


    public ActionResponse executar(Long id, ActionRequest actionRequest) {
        try {
            IController controller = getActionController(actionRequest);
            return controller.run(petitionService.getPetition(id), actionRequest);
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


    private List<BoxConfigurationData> listMenu(IServerContext context, String user) {
        List<BoxConfigurationData> groups = listMenuGroups();
        filterAccessRight(groups, user);
        customizeMenu(groups, context, user);
        return groups;
    }


    private List<BoxConfigurationData> listMenuGroups() {
        final List<BoxConfigurationData> groups = new ArrayList<>();
        getDefinitionsMap().forEach((category, definitions) -> {
            BoxConfigurationData boxConfigurationMetadata = new BoxConfigurationData();
            boxConfigurationMetadata.setId(permissionResolverService.buildCategoryPermission(category).getSingularId());
            boxConfigurationMetadata.setLabel(category);
            boxConfigurationMetadata.setProcesses(new ArrayList<>());
            definitions.forEach(d -> {
                        List<STask<?>> tasks = d.getFlowMap().getTasksWithMetadata(PetitionHistoryTaskMetaDataValue.KEY);
                        List<String> allowedHistoryTasks = tasks.stream().map(STask::getAbbreviation).collect(Collectors.toList());
                        boxConfigurationMetadata
                                .getProcesses()
                                .add(new ProcessDTO(d.getKey(), d.getName(), null, allowedHistoryTasks));
                    }
            );
            addForms(boxConfigurationMetadata);
            groups.add(boxConfigurationMetadata);
        });
        return groups;
    }

    private Map<String, List<FlowDefinition>> getDefinitionsMap() {
        final Map<String, List<FlowDefinition>> definitionMap = new HashMap<>();
        Flow.getDefinitions().forEach(d -> {
            if (!definitionMap.containsKey(d.getCategory())) {
                definitionMap.put(d.getCategory(), new ArrayList<>());
            }
            definitionMap.get(d.getCategory()).add(d);
        });
        return definitionMap;
    }

    @SuppressWarnings("unchecked")
    private void addForms(BoxConfigurationData boxConfigurationMetadata) {
        for (Class<? extends SType<?>> formClass : singularServerConfiguration.getFormTypes()) {
            SInfoType annotation = formClass.getAnnotation(SInfoType.class);
            String name = SFormUtil.getTypeName(formClass);
            Optional<SType<?>> sTypeOptional = singularFormConfig.getTypeLoader().loadType(name);
            if (sTypeOptional.isPresent()) {
                SType<?> sType = sTypeOptional.get();
                String label = sType.asAtr().getLabel();
                boxConfigurationMetadata.getForms().add(new FormDTO(name, sType.getNameSimple(), label, annotation.newable()));
            }
        }
    }

    private void filterAccessRight(List<BoxConfigurationData> groupDTOs, String user) {
        authorizationService.filterBoxWithPermissions(groupDTOs, user);
    }

    private void customizeMenu(List<BoxConfigurationData> groupDTOs, IServerContext menuContext, String user) {
        for (BoxConfigurationData boxConfigurationMetadata : groupDTOs) {
            boxConfigurationMetadata.setBoxesDefinition(singularModuleConfiguration.buildItemBoxes(menuContext));
        }
    }

    public Long count(String boxId, QuickFilter filter) {
        Optional<BoxController> boxController = singularModuleConfiguration.getBoxControllerByBoxId(boxId);
        if (boxController.isPresent()) {
            return boxController.get().countItens(filter);
        }
        return 0l;
    }

    public BoxItemDataList search(String boxId, QuickFilter filter) {
        Optional<BoxController> boxController = singularModuleConfiguration.getBoxControllerByBoxId(boxId);
        if (boxController.isPresent()) {
            return boxController.get().searchItens(filter);
        }
        return new BoxItemDataList();
    }

    public List<Actor> listAllocableUsers(Map<String, Object> selectedTask) {
        return petitionService.listAllocableUsers(selectedTask);
    }
}