/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.requirement.module.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections.CollectionUtils;
import org.opensingular.flow.core.Flow;
import org.opensingular.flow.core.FlowDefinition;
import org.opensingular.flow.core.FlowInstance;
import org.opensingular.flow.core.STask;
import org.opensingular.flow.core.STransition;
import org.opensingular.flow.core.TaskInstance;
import org.opensingular.flow.core.TransitionCall;
import org.opensingular.flow.persistence.entity.Actor;
import org.opensingular.flow.persistence.entity.FlowInstanceEntity;
import org.opensingular.flow.persistence.entity.ModuleEntity;
import org.opensingular.flow.persistence.entity.TaskInstanceEntity;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.persistence.FormKey;
import org.opensingular.form.persistence.entity.FormAnnotationEntity;
import org.opensingular.form.persistence.entity.FormEntity;
import org.opensingular.form.persistence.entity.FormTypeEntity;
import org.opensingular.form.persistence.entity.FormVersionEntity;
import org.opensingular.form.service.FormTypeService;
import org.opensingular.lib.commons.base.SingularException;
import org.opensingular.lib.commons.util.FormatUtil;
import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.lib.support.spring.util.ApplicationContextProvider;
import org.opensingular.requirement.module.RequirementDefinition;
import org.opensingular.requirement.module.connector.ModuleService;
import org.opensingular.requirement.module.exception.SingularRequirementException;
import org.opensingular.requirement.module.exception.SingularServerException;
import org.opensingular.requirement.module.flow.builder.RequirementFlowDefinition;
import org.opensingular.requirement.module.form.SingularServerSpringTypeLoader;
import org.opensingular.requirement.module.persistence.dao.flow.ActorDAO;
import org.opensingular.requirement.module.persistence.dao.flow.TaskInstanceDAO;
import org.opensingular.requirement.module.persistence.dao.form.ApplicantDAO;
import org.opensingular.requirement.module.persistence.dao.form.RequirementContentHistoryDAO;
import org.opensingular.requirement.module.persistence.dao.form.RequirementDAO;
import org.opensingular.requirement.module.persistence.dao.form.RequirementDefinitionDAO;
import org.opensingular.requirement.module.persistence.dto.RequirementHistoryDTO;
import org.opensingular.requirement.module.persistence.entity.enums.PersonType;
import org.opensingular.requirement.module.persistence.entity.form.ApplicantEntity;
import org.opensingular.requirement.module.persistence.entity.form.FormRequirementEntity;
import org.opensingular.requirement.module.persistence.entity.form.FormVersionHistoryEntity;
import org.opensingular.requirement.module.persistence.entity.form.RequirementContentHistoryEntity;
import org.opensingular.requirement.module.persistence.entity.form.RequirementDefinitionEntity;
import org.opensingular.requirement.module.persistence.entity.form.RequirementEntity;
import org.opensingular.requirement.module.persistence.filter.QuickFilter;
import org.opensingular.requirement.module.persistence.query.RequirementSearchExtender;
import org.opensingular.requirement.module.spring.security.AuthorizationService;
import org.opensingular.requirement.module.spring.security.RequirementAuthMetadataDTO;
import org.opensingular.requirement.module.spring.security.SingularPermission;
import org.opensingular.requirement.module.spring.security.SingularRequirementUserDetails;
import org.springframework.core.ResolvableType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import static org.opensingular.flow.core.TaskInstance.TASK_VISUALIZATION;

@Transactional
public abstract class RequirementService implements Loggable {

    @Inject
    protected RequirementDAO requirementDAO;

    @Inject
    protected TaskInstanceDAO taskInstanceDAO;

    @Inject
    protected ApplicantDAO applicantDAO;

    @Inject
    protected AuthorizationService authorizationService;

    @Inject
    protected ActorDAO actorDAO;

    @Inject
    private RequirementContentHistoryDAO requirementContentHistoryDAO;

    @Inject
    private FormRequirementService formRequirementService;

    @Inject
    private RequirementDefinitionDAO<RequirementDefinitionEntity> requirementDefinitionDAO;

    @Inject
    private Provider<SingularRequirementUserDetails> singularUserDetails;

    @Inject
    private FormTypeService formTypeService;

    @Inject
    private SingularServerSpringTypeLoader singularServerSpringTypeLoader;

    @Inject
    private ModuleService moduleService;

    /**
     * FOR INTERNAL USE ONLY,
     * MUST NOT BE EXPOSED BY SUBCLASSES
     *
     * @return
     */
    protected SingularRequirementUserDetails getSingularUserDetails() {
        return singularUserDetails.get();
    }

//    /**
//     * Recupera a petição associada ao fluxo informado ou dispara exception senão encontrar.
//     */
//    @Nonnull
//    private  <RI extends RequirementInstance> RI getRequirementInstance(@Nonnull RE requirementEntity) {
//        Objects.requireNonNull(requirementEntity);
//        return newRequirementInstance(requirementEntity);
//    }

//    /**
//     * Recupera a petição associada ao fluxo informado ou dispara exception senão encontrar.
//     */
//    @Nonnull
//    public <RI extends RequirementInstance> RI getRequirementInstance(@Nonnull FlowInstance flowInstance) {
//        Objects.requireNonNull(flowInstance);
//        RI instance = getRequirementInstance(getRequirementByFlowCod(flowInstance.getEntityCod()));
//        instance.setFlowInstance(flowInstance);
//        return instance;
//    }


//    /**
//     * Recupera a petição associada a task informada ou dispara exception senão encontrar.
//     */
//    @Nonnull
//    public  <RI extends RequirementInstance> RI getRequirementInstance(@Nonnull TaskInstance taskInstance) {
//        Objects.requireNonNull(taskInstance);
//        return getRequirementInstance(taskInstance.getFlowInstance());
//    }

    public <RD extends RequirementDefinition<?>> RD lookupRequirementDefinitionForRequirementId(Long requirementId) {
        String   key         = getRequirementDefinitionByRequirementId(requirementId).getKey();
        return lookupRequirementDefinition(key);
    }

    public <RD extends RequirementDefinition<?>> RD lookupRequirementDefinition(String  requirementDefinitionKey) {
        String[] definitions = ApplicationContextProvider.get().getBeanNamesForType(ResolvableType.forClass(RequirementDefinition.class));
        for (String definitionName : definitions) {
            RequirementDefinition<?> definition = (RequirementDefinition<?>) ApplicationContextProvider.get().getBean(definitionName);
            if (definition.getKey().equals(requirementDefinitionKey)) {
                return (RD) definition;
            }
        }
        throw new SingularRequirementException(String.format("Could not corresponding definition for definition key %s", requirementDefinitionKey));
    }

    public <RI extends RequirementInstance> RI loadRequirementInstance(Long requirementId){
        return (RI)lookupRequirementDefinitionForRequirementId(requirementId).loadRequirement(requirementId);
    }

    /**
     * Retorna o serviço de formulários da petição.
     */
    @Nonnull
    protected FormRequirementService getFormRequirementService() {
        return Objects.requireNonNull(formRequirementService);
    }

    /**
     * Configures the applicant in an requirement.
     *
     * @param requirement
     */
    protected <RI extends RequirementInstance> void configureApplicant(RI requirement) {
        UserDetails userDetails = singularUserDetails.get();
        if (userDetails instanceof SingularRequirementUserDetails) {
            ApplicantEntity p;
            p = applicantDAO.findApplicantByExternalId(userDetails.getUsername());
            if (p == null) {
                p = new ApplicantEntity();
                p.setIdPessoa(userDetails.getUsername());
                p.setName(((SingularRequirementUserDetails) userDetails).getDisplayName());
                p.setPersonType(PersonType.FISICA);
            }
            requirement.getEntity().setApplicant(p);
        } else {
            getLogger().error(" The applicant (current logged user, {}) for requirement: \"{}\" could not be identified ", SingularRequirementUserDetails.class.getSimpleName(), requirement.getRequirementDefinitionName());
        }
    }

    /**
     * Procura a petição com o código informado.
     */
    @Nonnull
    private Optional<RequirementEntity> findRequirementByCod(@Nonnull Long cod) {
        Objects.requireNonNull(cod);
        return requirementDAO.find(cod);
    }

    /**
     * Procura a petição com o código informado.
     */
    @Nonnull
    public Optional<RequirementEntity> findRequirementEntity(@Nonnull Long cod) {
        Objects.requireNonNull(cod);
        return requirementDAO.find(cod);
    }

    /**
     * Recupera a petição com o código informado ou dispara Exception senão encontrar.
     */
    @Nonnull
    @Deprecated
    public RequirementEntity getRequirementByCod(@Nonnull Long cod) {
        return findRequirementByCod(cod).orElseThrow(
                () -> SingularServerException.rethrow("Não foi encontrada a petição de cod=" + cod));
    }

    /**
     * Recupera a petição com o código informado ou dispara Exception senão encontrar.
     */
    @Nonnull
    public RequirementEntity getRequirementEntity(@Nonnull Long cod) {
        return findRequirementEntity(cod).orElseThrow(
                () -> SingularServerException.rethrow("Não foi encontrada a petição de cod=" + cod));
    }

    public RequirementDefinitionEntity getOrCreateRequirementDefinition(RequirementDefinition requirementDefintion, FormTypeEntity formType, ModuleEntity module) {
        RequirementDefinitionEntity requirementDefinitionEntity = requirementDefinitionDAO.findByModuleAndName(module, formType);

        if (requirementDefinitionEntity == null) {
            requirementDefinitionEntity = new RequirementDefinitionEntity();
            requirementDefinitionEntity.setKey(requirementDefintion.getKey());
            requirementDefinitionEntity.setFormType(formType);
            requirementDefinitionEntity.setModule(module);
            requirementDefinitionEntity.setName(requirementDefintion.getName());
        }

        return requirementDefinitionEntity;
    }

    /**
     * Recupera a petição associado a código de fluxo informado ou dispara exception senão encontrar.
     */
    @Nonnull
    @Deprecated
    public RequirementEntity getRequirementByFlowCod(@Nonnull Integer cod) {
        Objects.requireNonNull(cod);
        return requirementDAO.findByFlowCodOrException(cod);
    }

//    /**
//     * Recupera a petição associado ao fluxo informado.
//     */
//    @Nonnull
//    public <RI extends RequirementInstance> RI getRequirementEntity(@Nonnull FlowInstance flowInstance) {
//        Objects.requireNonNull(flowInstance);
//        RE requirement = getRequirementByFlowCod(flowInstance.getEntityCod());
//        return newRequirementInstance(requirement);
//    }

//    /**
//     * Recupera a petição associada a tarefa informada.
//     */
//    @Nonnull
//    public <RI extends RequirementInstance> RI getRequirementEntity(@Nonnull TaskInstance taskInstance) {
//        Objects.requireNonNull(taskInstance);
//        return getRequirementEntity(taskInstance.getFlowInstance());
//    }

    public void deleteRequirement(@Nonnull Long idRequirement) {
        requirementDAO.find(idRequirement).ifPresent(re -> requirementDAO.delete(re));
    }

    public Long countQuickSearch(QuickFilter filter) {
        return countQuickSearch(filter, Collections.emptyList());
    }

    public Long countQuickSearch(QuickFilter filter, List<RequirementSearchExtender> extenders) {
        return requirementDAO.countQuickSearch(filter, extenders);
    }

    public List<Map<String, Serializable>> quickSearchMap(QuickFilter filter) {
        return quickSearchMap(filter, Collections.emptyList());
    }

    public List<Map<String, Serializable>> quickSearchMap(QuickFilter filter, List<RequirementSearchExtender> extenders) {
        return requirementDAO.quickSearchMap(filter, extenders);
    }


    @Nonnull
    public <RI extends RequirementInstance> FormKey saveOrUpdate(@Nonnull RI requirement, @Nonnull SInstance instance, boolean mainForm) {
        Objects.requireNonNull(requirement);
        Objects.requireNonNull(instance);

        requirementDAO.saveOrUpdate(requirement.getEntity());

        if (requirement.getApplicant() != null) {
            applicantDAO.saveOrUpdate(requirement.getApplicant());
        }
        return formRequirementService.saveFormRequirement(requirement, instance, mainForm);
    }


    public <RI extends RequirementInstance> void saveRequirementHistory(RequirementInstance requirement, List<FormEntity> newEntities) {

        Optional<TaskInstanceEntity> taskInstance = findCurrentTaskEntityByRequirementId(requirement.getCod());
        FormEntity                   formEntity   = requirement.getEntity().getMainForm();

        getLogger().info("Atualizando histórico da petição.");

        final RequirementContentHistoryEntity contentHistoryEntity = new RequirementContentHistoryEntity();

        contentHistoryEntity.setRequirementEntity(requirement.getEntity());

        if (taskInstance.isPresent()) {
            contentHistoryEntity.setActor(taskInstance.get().getAllocatedUser());
            contentHistoryEntity.setTaskInstanceEntity(taskInstance.get());
        }

        if (CollectionUtils.isNotEmpty(formEntity.getCurrentFormVersionEntity().getFormAnnotations())) {
            contentHistoryEntity.setFormAnnotationsVersions(formEntity.getCurrentFormVersionEntity().getFormAnnotations().stream().map(FormAnnotationEntity::getAnnotationCurrentVersion).collect(Collectors.toList()));
        }

        contentHistoryEntity.setApplicantEntity(requirement.getApplicant());
        contentHistoryEntity.setHistoryDate(new Date());

        requirementContentHistoryDAO.saveOrUpdate(contentHistoryEntity);

        contentHistoryEntity.setFormVersionHistoryEntities(
                requirement.getEntity()
                        .getFormRequirementEntities()
                        .stream()
                        .filter(fpe -> newEntities.contains(fpe.getForm()))
                        .map(f -> formRequirementService.createFormVersionHistory(contentHistoryEntity, f))
                        .collect(Collectors.toList())
        );
    }


    /**
     * Executa a transição informada, consolidando todos os rascunhos, este metodo não salva a petição
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <RI extends RequirementInstance> void executeTransition(String transitionName,
                                                                   RI requirement,
                                                                   BiConsumer<RI, String> transitionListener,
                                                                   Map<String, String> processParameters,
                                                                   Map<String, String> transitionParameters) {
        try {
            if (transitionListener != null) {
                transitionListener.accept(requirement, transitionName);
            }

            saveRequirementHistory(requirement, formRequirementService.consolidateDrafts(requirement));
            FlowInstance flowInstance = requirement.getFlowInstance();

            if (processParameters != null && !processParameters.isEmpty()) {
                for (Map.Entry<String, String> entry : processParameters.entrySet()) {
                    flowInstance.getVariables().addValueString(entry.getKey(), entry.getValue());
                }
            }

            TransitionCall transitionCall = flowInstance.prepareTransition(transitionName);
            if (transitionParameters != null && !transitionParameters.isEmpty()) {
                for (Map.Entry<String, String> transitionParameter : transitionParameters.entrySet()) {
                    transitionCall.addValueString(transitionParameter.getKey(), transitionParameter.getValue());
                }
            }
            transitionCall.go();
        } catch (SingularException e) {
            throw e;
        } catch (Exception e) {
            throw SingularServerException.rethrow(e.getMessage(), e);
        }
    }

    public List<Map<String, Serializable>> listTasks(QuickFilter filter, List<SingularPermission> permissions) {
        return listTasks(filter, authorizationService.filterListTaskPermissions(permissions), Collections.emptyList());
    }

    public Long countTasks(QuickFilter filter, List<SingularPermission> permissions) {
        return countTasks(filter, authorizationService.filterListTaskPermissions(permissions), Collections.emptyList());
    }

    public List<Map<String, Serializable>> listTasks(QuickFilter filter, List<SingularPermission> permissions, List<RequirementSearchExtender> extenders) {
        return requirementDAO.quickSearchMap(filter, authorizationService.filterListTaskPermissions(permissions), extenders);
    }

    public Long countTasks(QuickFilter filter, List<SingularPermission> permissions, List<RequirementSearchExtender> extenders) {
        return requirementDAO.countQuickSearch(filter, authorizationService.filterListTaskPermissions(permissions), extenders);
    }

    public Optional<TaskInstance> findCurrentTaskInstanceByRequirementId(Long requirementId) {
        return findCurrentTaskEntityByRequirementId(requirementId)
                .map(Flow::getTaskInstance);
    }

    @Nonnull
    public Optional<TaskInstanceEntity> findCurrentTaskEntityByRequirementId(@Nonnull Long requirementId) {
        //TODO (Daniel) Por que usar essa entidade em vez de TaskIntnstace ?
        Objects.requireNonNull(requirementId);
        List<TaskInstanceEntity> taskInstances = taskInstanceDAO.findCurrentTasksByRequirementId(requirementId);
        if (taskInstances.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(taskInstances.get(0));
    }


    @Nonnull
    public void configureParentRequirement(@Nonnull RequirementInstance<?, ?> requirementInstance, @Nonnull RequirementInstance<?, ?> parentRequirement) {
        RequirementEntity requirementEntity = requirementInstance.getEntity();
        if (parentRequirement != null) {
            RequirementEntity parentRequirementEntity = parentRequirement.getEntity();
            requirementEntity.setParentRequirement(parentRequirementEntity);
            if (parentRequirementEntity.getRootRequirement() != null) {
                requirementEntity.setRootRequirement(parentRequirementEntity.getRootRequirement());
            } else {
                requirementEntity.setRootRequirement(parentRequirementEntity);
            }
        }
    }

    /**
     * List history entries.
     * Hidden entries could be removed from the result wheter {@param showHidden} is true or not.
     * An entry is considered hidden if the metadata {@link RequirementFlowDefinition#HIDE_FROM_HISTORY}
     * is set on the corresponding flow task.
     *
     * @param
     * @return
     */
    public List<RequirementHistoryDTO> listRequirementContentHistoryByCodRequirement(RequirementInstance<?, ?> requirementInstance, boolean showHidden) {
        List<RequirementHistoryDTO> entries = requirementContentHistoryDAO.listRequirementContentHistoryByCodRequirement(requirementInstance.getCod());
        if (!showHidden) {
            Set<String> hiddenEntriesAbbreviation = requirementInstance
                    .getFlowDefinition()
                    .getFlowMap()
                    .getAllTasks()
                    .stream()
                    .filter(task -> task.getMetaDataValue(RequirementFlowDefinition.HIDE_FROM_HISTORY))
                    .map(STask::getAbbreviation)
                    .collect(HashSet::new, HashSet::add, HashSet::addAll);

            return entries
                    .stream()
                    .filter(e -> !hiddenEntriesAbbreviation.contains(e.getTask().getTaskVersion().getAbbreviation()))
                    .collect(Collectors.toList());
        }
        return entries;
    }


    public List<Actor> listAllowedUsers(Map<String, Object> selectedTask) {
        Integer taskInstanceId = Integer.valueOf(String.valueOf(selectedTask.get("taskInstanceId")));
        return actorDAO.listAllowedUsers(taskInstanceId);
    }

    public ApplicantEntity findApplicantByExternalId(String externalId) {
        return applicantDAO.findApplicantByExternalId(externalId);
    }

    @Nonnull
    public boolean isPreviousTransition(@Nonnull TaskInstance taskInstance, @Nonnull String trasitionName) {
        Optional<STransition> executedTransition = taskInstance.getFlowInstance().getLastFinishedTask().map(TaskInstance::getExecutedTransition).orElse(Optional.<STransition>empty());
        if (executedTransition.isPresent()) {
            STransition transition = executedTransition.get();
            return trasitionName.equals(transition.getName());

        }
        return false;
    }

    public RequirementAuthMetadataDTO findRequirementAuthMetadata(Long requirementId) {
        return requirementDAO.findRequirementAuthMetadata(requirementId);
    }

    public List<FormVersionEntity> buscarDuasUltimasVersoesForm(@Nonnull Long codRequirement) {
        RequirementEntity requirementEntity = requirementDAO.findOrException(codRequirement);
        FormEntity        mainForm          = requirementEntity.getMainForm();
        return formRequirementService.findTwoLastFormVersions(mainForm.getCod());
    }

    /**
     * Procura a instância de fluxo associado ao formulário se o mesmo existir.
     */
    public Optional<FlowInstanceEntity> getFormFlowInstanceEntity(@Nonnull SInstance instance) {
        return getFormRequirementService().findFormEntity(instance)
                .map(formEntity -> requirementDAO.findByFormEntity(formEntity))
                .map(RequirementEntity::getFlowInstanceEntity);
    }

    /**
     * Verifica se o formulário já foi persistido e possui um fluxo instanciado e associado.
     */
    public boolean formHasFlowInstance(SInstance instance) {
        return getFormFlowInstanceEntity(instance).isPresent();
    }

    /**
     * Recupera o formulário {@link SInstance} de abertura do requerimento.
     */
    @Nonnull
    public SIComposite getMainFormAsInstance(@Nonnull RequirementEntity requirement) {
        Objects.requireNonNull(requirement);
        return (SIComposite) getFormRequirementService().getSInstance(requirement.getMainForm());
    }

    /**
     * Recupera o formulário {@link SInstance} de abertura do requerimento e garante que é do tipo inforado.
     */
    @Nonnull
    public <I extends SInstance, K extends SType<? extends I>> I getMainFormAsInstance(@Nonnull RequirementEntity requirement,
                                                                                       @Nonnull Class<K> expectedType) {
        Objects.requireNonNull(requirement);
        return getFormRequirementService().getSInstance(requirement.getMainForm(), expectedType);
    }

    /**
     * Procura na petição a versão mais recente do formulário do tipo informado.
     */
    @Nonnull
    public Optional<FormRequirementEntity> findLastFormRequirementEntityByType(@Nonnull RequirementInstance requirement,
                                                                               @Nonnull Class<? extends SType<?>> typeClass) {
        return getFormRequirementService().findLastFormRequirementEntityByType(requirement, typeClass);
    }

    /**
     * Procura na petição a versão mais recente do formulário do tipo informado.
     */
    @Nonnull
    public Optional<SInstance> findLastFormRequirementInstanceByType(@Nonnull RequirementInstance requirement,
                                                                     @Nonnull Class<? extends SType<?>> typeClass) {
        return getFormRequirementService().findLastFormRequirementInstanceByType(requirement, typeClass);
    }

    @Nonnull
    public Optional<SIComposite> findLastFormInstanceByTypeAndTask(@Nonnull RequirementInstance requirement, @Nonnull Class<? extends SType<?>> typeClass, TaskInstance taskInstance) {
        return findLastFormEntityByTypeAndTask(requirement, typeClass, taskInstance)
                .map(version -> (SIComposite) getFormRequirementService().getSInstance(version));
    }

    @Nonnull
    public Optional<FormVersionEntity> findLastFormEntityByTypeAndTask(@Nonnull RequirementInstance requirement, @Nonnull Class<? extends SType<?>> typeClass, TaskInstance taskInstance) {
        Objects.requireNonNull(requirement);
        return requirementContentHistoryDAO.findLastByCodRequirementCodTaskInstanceAndType(typeClass, requirement.getCod(), (Integer) taskInstance.getId())
                .map(FormVersionHistoryEntity::getFormVersion);
    }

    /**
     * Procura na petição a versão mais recente do formulário do tipo informado.
     */
    @Nonnull
    public Optional<SIComposite> findLastFormInstanceByType(@Nonnull RequirementInstance requirement,
                                                            @Nonnull Class<? extends SType<?>> typeClass) {
        //TODO Verificar se esse método não está redundante com FormRequirementService.findLastFormRequirementEntityByType
        Objects.requireNonNull(requirement);
        return requirementContentHistoryDAO.findLastByCodRequirementAndType(typeClass, requirement.getCod())
                .map(FormVersionHistoryEntity::getFormVersion)
                .map(version -> (SIComposite) getFormRequirementService().getSInstance(version));
    }

    /**
     * Procura na petição a versão mais recente do formulário do tipo informado.
     */
    @Nonnull
    public Optional<FormVersionEntity> findLastFormEntityByType(@Nonnull RequirementInstance requirement,
                                                                @Nonnull Class<? extends SType<?>> typeClass) {
        Objects.requireNonNull(requirement);
        return requirementContentHistoryDAO.findLastByCodRequirementAndType(typeClass, requirement.getCod())
                .map(FormVersionHistoryEntity::getFormVersion);
    }

    /**
     * Procura na petição o formulário mais recente dentre os tipos informados.
     */
    @Nonnull
    protected Optional<SIComposite> findLastFormInstanceByType(@Nonnull RequirementInstance requirement,
                                                               @Nonnull Collection<Class<? extends SType<?>>> typesClass) {
        Objects.requireNonNull(requirement);
        FormVersionHistoryEntity max = null;
        for (Class<? extends SType<?>> type : typesClass) {
            //TODO (Daniel) Deveria fazer uma única consulta para otimziar o resultado
            Optional<FormVersionHistoryEntity> result = requirementContentHistoryDAO.findLastByCodRequirementAndType(type,
                    requirement.getCod());
            if (result.isPresent() && (max == null || max.getRequirementContentHistory().getHistoryDate().before(
                    result.get().getRequirementContentHistory().getHistoryDate()))) {
                max = result.get();
            }
        }
        return Optional.ofNullable(max).map(
                version -> (SIComposite) getFormRequirementService().getSInstance(version.getFormVersion()));
    }

    @Nonnull
    public void startNewFlow(@Nonnull RequirementInstance requirement, @Nonnull FlowDefinition flowDefinition, @Nullable String codSubmitterActor) {
        FlowInstance newFlowInstance = flowDefinition.newPreStartInstance();
        newFlowInstance.setDescription(requirement.getDescription());

        FlowInstanceEntity flowEntity = newFlowInstance.saveEntity();

        if (codSubmitterActor != null) {
            RequirementUtil.findUser(codSubmitterActor).filter(u -> u instanceof Actor).ifPresent(user -> {
                flowEntity.setUserCreator((Actor) user);
            });
        }

        RequirementEntity requirementEntity = requirement.getEntity();
        requirementEntity.setFlowInstanceEntity(flowEntity);
        requirementEntity.setFlowDefinitionEntity(flowEntity.getFlowVersion().getFlowDefinition());
        requirementDAO.saveOrUpdate(requirementEntity);

        newFlowInstance.start();

        requirement.setFlowInstance(newFlowInstance);
    }

    //TODO vinicius.nunes LENTO
    @Deprecated
    public boolean containChildren(Long codRequirement) {
        return requirementDAO.containChildren(codRequirement);
    }

    public <RI extends RequirementInstance> void updateRequirementDescription(SInstance currentInstance, RI requirement) {
        String description = currentInstance.toStringDisplay();
        if (description != null && description.length() > 200) {
            getLogger().error("Descrição do formulário muito extensa. A descrição foi cortada.");
            description = description.substring(0, 197) + "...";
        }
        requirement.setDescription(description);
    }

    public RequirementDefinitionEntity getRequirementDefinitionByRequirementId(Long requirementId) {
        return requirementDefinitionDAO.findRequirementDefinitionByRequrimentId(requirementId);
    }

    public RequirementDefinitionEntity getRequirementDefinition(String requirementDefinitionKey) {
        return requirementDefinitionDAO.findByKey(moduleService.getModule().getCod(), requirementDefinitionKey);
    }

    public <RI extends RequirementInstance> void logTaskVisualization(RI requirement) {
        TaskInstance taskInstance = requirement.getFlowInstance().getCurrentTaskOrException();
        taskInstance.log(TASK_VISUALIZATION, FormatUtil.dateToDefaultTimestampString(new Date()));
    }



    /**
     * Persiste se necessário o RequirementDefinitionEntity
     * e atualiza no ref o valor que está em banco.
     *
     * @param requirementDefinition - o requerimento do qual o {@link RequirementDefinitionEntity} será criado
     */
    public void saveOrUpdateRequirementDefinition(RequirementDefinition requirementDefinition) {
        Class<? extends SType> mainForm = requirementDefinition.getMainForm();
        SType<?> type = singularServerSpringTypeLoader.loadTypeOrException(mainForm);
        FormTypeEntity formType = formTypeService.findFormTypeEntity(type);

        RequirementDefinitionEntity requirementDefinitionEntity = getOrCreateRequirementDefinition(requirementDefinition, formType, moduleService.getModule());
        requirementDefinitionDAO.save(requirementDefinitionEntity);
    }
}