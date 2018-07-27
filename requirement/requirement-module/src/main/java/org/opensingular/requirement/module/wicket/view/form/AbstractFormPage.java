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

package org.opensingular.requirement.module.wicket.view.form;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.opensingular.flow.core.STask;
import org.opensingular.flow.core.STransition;
import org.opensingular.flow.core.TaskInstance;
import org.opensingular.flow.core.TransitionAccess;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.document.RefType;
import org.opensingular.form.persistence.FormKey;
import org.opensingular.form.wicket.component.SingularButton;
import org.opensingular.form.wicket.component.SingularSaveButton;
import org.opensingular.form.wicket.component.SingularValidationButton;
import org.opensingular.form.wicket.enums.AnnotationMode;
import org.opensingular.form.wicket.enums.ViewMode;
import org.opensingular.form.wicket.panel.ModalEventListenerBehavior;
import org.opensingular.form.wicket.panel.SingularFormPanel;
import org.opensingular.form.wicket.util.WicketFormProcessing;
import org.opensingular.internal.lib.support.spring.injection.SingularSpringInjector;
import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;
import org.opensingular.lib.wicket.util.modal.BSModalBorder;
import org.opensingular.lib.wicket.util.model.IReadOnlyModel;
import org.opensingular.lib.wicket.util.util.Shortcuts;
import org.opensingular.requirement.module.RequirementDefinition;
import org.opensingular.requirement.module.exception.SingularRequirementException;
import org.opensingular.requirement.module.exception.SingularServerException;
import org.opensingular.requirement.module.exception.SingularServerFormValidationError;
import org.opensingular.requirement.module.persistence.entity.form.FormRequirementEntity;
import org.opensingular.requirement.module.service.FormRequirementService;
import org.opensingular.requirement.module.service.RequirementInstance;
import org.opensingular.requirement.module.service.RequirementService;
import org.opensingular.requirement.module.service.RequirementUtil;
import org.opensingular.requirement.module.service.SingularRequirementService;
import org.opensingular.requirement.module.service.dto.RequirementSubmissionResponse;
import org.opensingular.requirement.module.wicket.SingularSession;
import org.opensingular.requirement.module.wicket.builder.HTMLParameters;
import org.opensingular.requirement.module.wicket.builder.MarkupCreator;
import org.opensingular.requirement.module.wicket.view.panel.FeedbackAposEnvioPanel;
import org.opensingular.requirement.module.wicket.view.panel.NotificationPanel;
import org.opensingular.requirement.module.wicket.view.template.ServerTemplate;
import org.opensingular.requirement.module.wicket.view.util.ActionContext;
import org.opensingular.requirement.module.wicket.view.util.ModuleButtonFactory;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.opensingular.lib.wicket.util.util.WicketUtils.*;
import static org.opensingular.requirement.module.wicket.builder.MarkupCreator.*;

public abstract class AbstractFormPage<RI extends RequirementInstance> extends ServerTemplate implements Loggable {

    protected final String                   typeName;
    protected final FormPageExecutionContext config;
    protected final SingularFormPanel        singularFormPanel;
    protected final IModel<Boolean>          inheritParentFormData;
    protected final IModel<FormKey>          parentRequirementFormKeyModel;
    protected final BSContainer<?> modalContainer = new BSContainer<>("modals");
    protected final BSModalBorder  closeModal     = construirCloseModal();

    private final Map<String, TransitionController<?>>       transitionControllerMap   = new HashMap<>();
    private       Map<String, STypeBasedFlowConfirmModal<?>> transitionConfirmModalMap = new HashMap<>();
    private BSModalBorder notificacoesModal;
    private FeedbackAposEnvioPanel feedbackAposEnvioPanel = null;
    private           IModel<RI>             requirementInstanceModel;
    private transient Optional<TaskInstance> currentTaskInstance;


    @Inject
    private RequirementService requirementService;

    @Inject
    private FormRequirementService      formRequirementService;
    private AbstractDefaultAjaxBehavior saveFormAjaxBehavior;

    public AbstractFormPage(@Nullable ActionContext context) {
        this(context, null);
    }

    public AbstractFormPage(@Nullable ActionContext context, @Nullable Class<? extends SType<?>> formType) {
        if (context == null) {
            String path = WebApplication.get().getServletContext().getContextPath();
            getLogger().info(" Redirecting to {}", path);
            throw new RedirectToUrlException(path);
        }

        this.config = new FormPageExecutionContext(Objects.requireNonNull(context), RequirementUtil.getTypeName(formType));
        this.parentRequirementFormKeyModel = $m.ofValue();
        this.inheritParentFormData = $m.ofValue();
        this.typeName = config.getFormName();
        this.singularFormPanel = new SingularFormPanel("singular-panel");
        onBuildSingularFormPanel(singularFormPanel);

        context.getInheritParentFormData().ifPresent(inheritParentFormData::setObject);

        if (this.config.getFormName() == null) {
            throw new SingularServerException("Tipo do formulário da página nao foi definido");
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        requirementInstanceModel = $m.loadable(this::loadRequirement);

        fillTransitionControllerMap(transitionControllerMap);
        SingularSpringInjector.get().injectAll(transitionControllerMap.values());
        SingularSpringInjector.get().injectAll(transitionConfirmModalMap.values());

        modalContainer.setOutputMarkupId(true);
        modalContainer.add(new ModalEventListenerBehavior(modalContainer));
        singularFormPanel.setViewMode(getViewMode(config));
        singularFormPanel.setAnnotationMode(getAnnotationMode(config));
        singularFormPanel.setInstanceCreator(() -> requirementInstanceModel.getObject().getDraft(config.getFormName()));
        singularFormPanel.setModalContainer(modalContainer);

        Form<?> form = new Form<>("save-form");
        form.setMultiPart(true);
        form.add(singularFormPanel);
        form.add(modalContainer);
        BSModalBorder enviarModal = buildConfirmationModal(modalContainer, getInstanceModel());
        form.add(buildSendButton(enviarModal));
        form.add(buildSaveButton("save-btn"));
        form.add(buildFlowButtons());
        form.add(buildValidateButton());
        form.add(buildExtensionButtons());
        form.add(buildCloseButton());
        form.add(closeModal);
        form.add(buildExtraContent("extra-content"));
        add(form);
        addSaveCallBackUrl();
    }


    protected void fillTransitionControllerMap(Map<String, TransitionController<?>> transitionControllerMap) {

    }

    protected Optional<TaskInstance> getCurrentTaskInstance() {
        if (currentTaskInstance == null) {//NOSONAR
            currentTaskInstance = getRequirement().getFlowInstance().getCurrentTask();
        }
        return currentTaskInstance;
    }


    protected Optional<RequirementDefinition> getSingularRequirement(@Nullable ActionContext context) {
        SingularRequirementService requirementService = SingularRequirementService.get();
        return Optional.ofNullable(requirementService.getSingularRequirement(context));
    }

    /**
     * Retorna o serviço de petição.
     */
    @Nonnull
    protected final RequirementService getRequirementService() {
        return Objects.requireNonNull(requirementService);
    }

    /**
     * Retorna o serviço de manipulação de formuluário de petição.
     */
    @Nonnull
    protected final FormRequirementService getFormRequirementService() {
        return Objects.requireNonNull(formRequirementService);
    }

    /**
     * Retorna model que contêm a petição. Se ainda não tiver inicilizado, dispara exception.
     */
    @Nonnull
    protected final IModel<RI> getRequirementModel() {
        if (requirementInstanceModel == null) {
            throw new SingularServerException("A página ainda não foi inicializada");
        }
        return requirementInstanceModel;
    }

    /**
     * Retorna a petição atual ou dispara exception se ainda não estiver configurada.
     */
    @Nonnull
    protected final RI getRequirement() {
        if (requirementInstanceModel != null && requirementInstanceModel.getObject() != null) {
            return requirementInstanceModel.getObject();
        }
        throw new SingularServerException("O requerimento (" + RequirementInstance.class.getName() + ") ainda está null");
    }

    /**
     * Retorna a petição atual.
     */
    @Nonnull
    protected final Optional<RI> getRequirementOptional() {
        if (requirementInstanceModel == null || requirementInstanceModel.getObject() == null) {
            return Optional.empty();
        }
        return Optional.of(requirementInstanceModel.getObject());
    }

    /**
     * Retorna a instância sendo editada no momento (ou dispara exception senão tiver sido inicializada).
     */
    @Nonnull
    protected final SIComposite getInstance() {
        return (SIComposite) getSingularFormPanel().getInstance();
    }


    /**
     * Retorna as configurações da página de edição de formulário.
     */
    @Nonnull
    protected FormPageExecutionContext getConfig() {
        return config;
    }


    @Override
    protected boolean isWithMenu() {
        return false;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        configurarExibicaoNotificacoes();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptReferenceHeaderItem.forReference(new PackageResourceReference(AbstractFormPage.class, "CollapseFlowButtons.js")));
        response.render(JavaScriptReferenceHeaderItem.forReference(new PackageResourceReference(AbstractFormPage.class, "AbstractFormPage.js")));
        response.render(OnDomReadyHeaderItem.forScript(generateInitJS()));
    }

    private String generateInitJS() {
        return "\n $(function () { "
                + "\n   window.AbstractFormPage.setup(" + new JSONObject()
                .put("callbackUrl", saveFormAjaxBehavior.getCallbackUrl().toString())
                .toString(2) + "); "
                + "\n });";
    }



    private Component buildExtensionButtons() {
        return new ExtensionButtonsPanel<>("extensions-buttons", requirementInstanceModel, singularFormPanel.getInstanceModel())
                .setRenderBodyOnly(true);
    }

    private RI loadRequirement() {
        RI             requirement;
        Optional<Long> requirementId = config.getRequirementId();
        Optional<String> requirementDefinitionKey = config.getRequirementDefinitionKey();
        if (requirementId.isPresent()) {
            if (requirementDefinitionKey.isPresent()) {
                requirement = (RI) getRequirementDefinition().loadRequirement(config.getRequirementId().get());
            }  else {
                requirement = (RI) requirementService.loadRequirementInstance(requirementId.get());
            }
        } else {
            RI             parentRequirement   = null;
            Optional<Long> parentRequirementId = config.getParentRequirementId();
            if (parentRequirementId.isPresent()) {
                parentRequirement = requirementService.loadRequirementInstance(parentRequirementId.get());
                requirement = (RI) getRequirementDefinition().newRequirement(parentRequirement);
            } else {
                requirement = (RI) getRequirementDefinition().newRequirement();
            }
        }
        return requirement;
    }

    protected RequirementDefinition getRequirementDefinition() {
        return requirementService.lookupRequirementDefinition(getConfig()
                .getRequirementDefinitionKey()
                .orElseThrow(() -> new SingularRequirementException("Could not identify the RequirementDefinition, requirement definition key or requirement id must be provided.")));
    }

    private Optional<FormRequirementEntity> getFormRequirementEntity(RI requirement) {
        if (isMainForm()) {
            return formRequirementService.findFormRequirementEntityByType(requirement, getFormType());
        } else {
            return formRequirementService.findFormRequirementEntityByTypeAndTask(requirement, getFormType(),
                    requirement.getCurrentTaskOrException());
        }
    }

    protected ServerSendButton makeServerSendButton(String id, IModel<? extends SInstance> formInstance, BSModalBorder enviarModal) {
        return new ServerSendButton(id, formInstance, enviarModal);
    }

    private Component buildExtraContent(String id) {
        final TemplatePanel extraPanel     = new TemplatePanel(id, MarkupCreator.div("extraContainer"));
        final BSContainer   extraContainer = new BSContainer("extraContainer");
        extraPanel.add(extraContainer);
        appendExtraContent(extraContainer);
        extraPanel.add($b.visibleIf(() -> extraContainer.visitChildren((object, visit) -> visit.stop("found!")) != null));
        return extraPanel;
    }

    private Component buildPreFormPanelContent(String id) {
        final TemplatePanel extraPanel     = new TemplatePanel(id, MarkupCreator.div("extraContainer"));
        final BSContainer   extraContainer = new BSContainer("extraContainer");
        extraPanel.add(extraContainer);
        appendBeforeFormContent(extraContainer);
        extraPanel.add($b.visibleIf(() -> extraContainer.visitChildren((object, visit) -> visit.stop("found!")) != null));
        return extraPanel;
    }

    protected void appendExtraContent(BSContainer extraContainer) {
        extraContainer.newComponent(this::buildNotificacoesModal);
        extraContainer.newComponent(this::createFeedbackAposEnvioPanel);
        getTransitionControllerMap().forEach((k, v) -> v.appendExtraContent(extraContainer));
    }

    private Component createFeedbackAposEnvioPanel(String id) {
        this.feedbackAposEnvioPanel = buildFeedbackAposEnvioPanel(id);
        if (feedbackAposEnvioPanel == null) {
            return new WebMarkupContainer(id);
        } else {
            return this.feedbackAposEnvioPanel;
        }
    }

    protected FeedbackAposEnvioPanel buildFeedbackAposEnvioPanel(String id) {
        return null;
    }

    private void onBuildSingularFormPanel(SingularFormPanel singularFormPanel) {
        singularFormPanel.setPreFormPanelFactory(this::buildPreFormPanelContent);
    }

    protected void appendBeforeFormContent(BSContainer container) {
    }

    @Nonnull
    protected abstract Optional<String> getIdentifier();

    /**
     * será removido na próxima versão do Singular
     *
     * @param requirement
     */
    @Deprecated
    protected void onNewRequirementCreation(RI requirement) {
    }

    protected void configureCustomButtons(BSContainer<?> buttonContainer, BSContainer<?> modalContainer, boolean transitionButtonsVisible, IModel<? extends SInstance> currentInstance) {
        Optional<Long> requirementId = config.getRequirementId();
        if (requirementId.isPresent()) {
            configureDiffButton(requirementId.get(), buttonContainer, currentInstance);
        }

        Optional<TaskInstance> currentTaskInstanceOpt = getCurrentTaskInstance();
        if (!currentTaskInstanceOpt.isPresent()) {
            buttonContainer.setVisible(false).setEnabled(false);
        } else {
            configureTransitionButtons(buttonContainer, modalContainer, transitionButtonsVisible, currentInstance, currentTaskInstanceOpt.get());
        }

        appendViewNotificationsButton(buttonContainer);
    }

    private void configureDiffButton(Long requirementId, BSContainer<?> buttonContainer, IModel<? extends SInstance> currentInstance) {
        if (hasMultipleVersionsAndIsMainForm(requirementId)) {
            appendButtonViewDiff(buttonContainer, requirementId, currentInstance);
        }
    }

    private void configureTransitionButtons(BSContainer<?> buttonContainer, BSContainer<?> modalContainer, boolean transitionButtonsVisibility, IModel<? extends SInstance> currentInstance, TaskInstance taskInstance) {
        int               buttonsCount = 0;
        List<STransition> transitions  = getCurrentTaskInstance().flatMap(TaskInstance::getFlowTask).map(STask::getTransitions).orElse(Collections.emptyList());
        if (transitionButtonsVisibility && CollectionUtils.isNotEmpty(transitions)) {
            int index = 0;
            for (STransition t : transitions) {
                TransitionAccess transitionAccess = getButtonAccess(t, taskInstance);
                if (transitionAccess.isVisible()) {
                    String btnId = "flow-btn" + index;
                    buttonsCount++;
                    buildFlowTransitionButton(
                            btnId,
                            buttonContainer,
                            modalContainer,
                            t.getName(),
                            currentInstance,
                            transitionAccess);
                }

            }
        }
        if (buttonsCount > 2) {
            this.add($b.onReadyScript(s ->
                    "$(window).resize(function(){FlowButtonsConfigurer.configure()});" +
                            "FlowButtonsConfigurer.configure();"
            ));
        }
    }

    /**
     * Verifica se existe mais de uma versão do formulário principal
     * lavando em consideração o rascunho.
     *
     * @param requirementId id da petição a ser verificada
     * @return true em caso afirmativo, false caso contrário
     */
    protected boolean hasMultipleVersionsAndIsMainForm(Long requirementId) {
        if (requirementId != null) {

            int totalVersoes = 0;

            // Verifica se existe rascunho
            RequirementInstance requirement = requirementService.loadRequirementInstance(requirementId);
            String              typeName    = RequirementUtil.getTypeName(requirement);
            if (requirement.getEntity().currentEntityDraftByType(typeName).isPresent()) {
                totalVersoes++;
            }

            // Busca o número de versões consolidadas
            Long versoesConsolidadas = formRequirementService.countVersions(requirement.getEntity().getMainForm().getCod());
            totalVersoes += versoesConsolidadas;

            String formType = getFormType();

            return totalVersoes > 1 && formType.equalsIgnoreCase(typeName);
        }
        return false;
    }

    /**
     * Adiciona o botão para visualizar o diff na barra de botões.
     *
     * @param buttonContainer
     * @param requirementId
     * @param currentInstance
     */
    protected void appendButtonViewDiff(BSContainer<?> buttonContainer, Long requirementId, IModel<? extends SInstance> currentInstance) {
        buttonContainer.appendComponent(id ->
                new ModuleButtonFactory(ActionContext.fromFormConfig(config), getAdditionalParams())
                        .getDiffButton(id)
        );
    }

    protected Map<String, String> getAdditionalParams() {
        return Collections.emptyMap();
    }


    protected final TransitionAccess getButtonAccess(STransition transition, TaskInstance t) {
        return transition.getAccessFor(t);
    }

    protected final RI getUpdatedRequirementFromInstance(IModel<? extends SInstance> currentInstance, boolean mainForm) {
        RI requirement = getRequirement();
        if (currentInstance.getObject() instanceof SIComposite && mainForm) {
            requirementService.updateRequirementDescription(currentInstance.getObject(), requirement);
        }
        return requirement;
    }


    protected void buildFlowTransitionButton(String buttonId, BSContainer<?> buttonContainer, BSContainer<?> modalContainer, String transitionName, IModel<? extends SInstance> instanceModel, TransitionAccess transitionButtonEnabled) {
        final BSModalBorder modal = buildFlowConfirmationModal(buttonId, modalContainer, transitionName, instanceModel);
        buildFlowButton(buttonId, buttonContainer, transitionName, modal, transitionButtonEnabled);
    }

    public void atualizarContentWorklist(AjaxRequestTarget target) {
        target.appendJavaScript("Singular.atualizarContentWorklist();");
    }

    protected BSModalBorder buildConfirmationModal(BSContainer<?> modalContainer, IModel<? extends SInstance> instanceModel) {
        TemplatePanel tpModal = modalContainer.newTemplateTag(tt ->
                "<div wicket:id='send-modal' class='portlet-body form'>\n"
                        + "<wicket:message key=\"label.confirm.message\"/>\n"
                        + "</div>\n");
        BSModalBorder enviarModal = new BSModalBorder("send-modal", getMessage("label.title.send"));
        enviarModal
                .addButton(BSModalBorder.ButtonStyle.CANCEL, "label.button.close", new AjaxButton("cancel-btn") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        enviarModal.hide(target);
                    }
                })
                .addButton(BSModalBorder.ButtonStyle.CONFIRM, "label.button.confirm", new SingularSaveButton("confirm-btn", instanceModel) {

                    @Override
                    protected void onValidationSuccess(AjaxRequestTarget target, Form<?> form, IModel<? extends SInstance> instanceModel) {
                        AbstractFormPage.this.send(instanceModel, target, enviarModal);
                    }

                    @Override
                    protected void onValidationError(AjaxRequestTarget target, Form<?> form, IModel<? extends SInstance> instanceModel) {
                        enviarModal.hide(target);
                        target.add(form);
                        addToastrErrorMessage("message.send.error");
                    }

                });
        tpModal.add(enviarModal);
        return enviarModal;
    }

    /**
     * Inicia o fluxo da petição, consolidando os formularios de rascunho e criando o historico
     *
     * @param mi    modal da instancia atual do formulario
     * @param ajxrt ajax request target do wicket
     * @param sm    modal que disparou a confirmação
     */
    protected void send(IModel<? extends SInstance> mi, AjaxRequestTarget ajxrt, BSModalBorder sm) {
        //executa em block try, caso exita rollback deve recarregar a peticao, para que a mesma não
        //tenha dados que sofreram rollback
        RI requirement = getRequirement();
        try {
            //usuario para persistencia
            String username = SingularSession.get().getUsername();

            //save do formulário em transação separada
            requirement.saveForm(mi.getObject());

            //executa o envio, iniciando o fluxo informado
            RequirementSubmissionResponse sendedFeedback = requirement.send(username);
            //janela de oportunidade para executar ações apos o envio, normalmente utilizado para mostrar mensagens
            onAfterSend(ajxrt, sm, sendedFeedback);
        } catch (Exception ex) {
            //recarrega a petição novamente
            getRequirementModel().setObject(requirementService.loadRequirementInstance(requirement.getCod()));
            //faz o rethrow da exeção, algumas são tratadas e exibidas na tela como mensagens informativas
            throw SingularServerException.rethrow(ex.getMessage(), ex);
        }

    }

    protected void onAfterSend(AjaxRequestTarget target, BSModalBorder enviarModal, RequirementSubmissionResponse sendedFeedback) {
        if (feedbackAposEnvioPanel != null) {
            enviarModal.hide(target);
            atualizarContentWorklist(target);
            feedbackAposEnvioPanel.show(target, sendedFeedback);
        } else {
            atualizarContentWorklist(target);
            addAfterSendSuccessMessage();
            target.appendJavaScript("; window.close();");
        }
    }

    protected void addAfterSendSuccessMessage() {
        Optional<String> identifier = getIdentifier();
        if (identifier.isPresent()) {
            addToastrSuccessMessageWorklist("message.send.success.identifier", identifier.get());
        } else {
            addToastrSuccessMessageWorklist("message.send.success");
        }
    }

    protected void onBeforeExecuteTransition(AjaxRequestTarget ajaxRequestTarget,
                                             Form<?> form,
                                             String transitionName,
                                             IModel<? extends SInstance> currentInstance)
            throws SingularServerFormValidationError {
        final STypeBasedFlowConfirmModal<?> flowConfirmModal = transitionConfirmModalMap.get(transitionName);
        if (flowConfirmModal == null) {
            getRequirement().saveForm(currentInstance.getObject());
        } else {
            boolean isFormOnModalValid = WicketFormProcessing.onFormSubmit(form, ajaxRequestTarget,
                    flowConfirmModal.getInstanceModel(), true, true);
            if (isFormOnModalValid) {
                getRequirement().saveForm(currentInstance.getObject());
            } else {
                throw new SingularServerFormValidationError();
            }
        }
    }

    /**
     * Executa a transição da tarefa para a transição informada
     *
     * @param ajxrt target do wicket
     * @param form  form atual
     * @param tn    transição a ser executada
     * @param mi    model contendo a instancia atual
     * @throws SingularServerFormValidationError caso exista erros de validação
     * @see AbstractFormPage#onBeforeExecuteTransition
     * @see AbstractFormPage#onTransitionExecuted(AjaxRequestTarget, String)
     */
    protected void executeTransition(AjaxRequestTarget ajxrt, Form<?> form, String tn, IModel<? extends SInstance> mi)
            throws SingularServerFormValidationError {

        //relizada a chamada, abrindo janela de oportunidade para salvar e alteradas dados da petição
        onBeforeExecuteTransition(ajxrt, form, tn, mi);

        //petição atual, qualuer alteracao deve ser feita em onBeforeExecuteTransition
        RI requirement = getRequirement();

        //busca os parametros do FLOW
        Map<String, String> flowParameters = getFlowParameters(tn);

        //busca os parametros da transicao atual
        Map<String, String> currentTransitionParameters = getCurrentTransitionParameters(tn);

        //Executa em bloco try, executa rollback da petição caso exista erro
        try {
            //executa a transicao informada
            requirementService.executeTransition(tn, requirement, this::onTransition, flowParameters, currentTransitionParameters);

            //executa chamada, abrindo janela de oportunidade de executar ações apos execução da transicao
            onTransitionExecuted(ajxrt, tn);
        } catch (Exception ex) {
            //recarrega a petição novamente
            getRequirementModel().setObject(requirementService.loadRequirementInstance(requirement.getCod()));
            //faz o rethrow da exeção, algumas são tratadas e exibidas na tela como mensagens informativas
            throw SingularServerException.rethrow(ex.getMessage(), ex);
        }
    }

    protected Map<String, String> getCurrentTransitionParameters(String currentTransition) {
        return new HashMap<>();
    }

    /**
     * Permite a configuração de parametros de instancia do flow durante a transição.
     *
     * @param transition a transicao sendo executada
     * @return Mapa de parametros
     */
    protected Map<String, String> getFlowParameters(String transition) {
        Map<String, String>           params           = new HashMap<>();
        TransitionController<?>       controller       = getTransitionControllerMap().get(transition);
        STypeBasedFlowConfirmModal<?> flowConfirmModal = transitionConfirmModalMap.get(transition);
        if (controller != null && flowConfirmModal != null) {
            Map<String, String> moreParams = controller.getFlowParameters(getInstance(), flowConfirmModal.getInstanceModel().getObject());
            if (moreParams != null) {
                params.putAll(moreParams);
            }
        }
        return params;
    }

    protected void onTransition(RequirementInstance pe, String transitionName) {
        TransitionController<?> controller = getTransitionControllerMap().get(transitionName);
        if (controller != null) {
            STypeBasedFlowConfirmModal<?> flowConfirmModal = transitionConfirmModalMap.get(transitionName);
            controller.onTransition(getInstance(), flowConfirmModal.getInstanceModel().getObject());
        }
    }

    protected void onTransitionExecuted(AjaxRequestTarget ajaxRequestTarget, String transitionName) {
        atualizarContentWorklist(ajaxRequestTarget);
        addToastrSuccessMessageWorklist("message.action.success", transitionName);
        closeBrowserWindow(ajaxRequestTarget);
    }

    protected void closeBrowserWindow(AjaxRequestTarget ajaxRequestTarget) {
        ajaxRequestTarget.appendJavaScript("window.close();");
    }

    protected final boolean isFlowInstanceCreated() {
        return getRequirement().isFlowInstanceCreated();
    }

    private void buildFlowButton(String buttonId,
                                 BSContainer<?> buttonContainer,
                                 String transitionName,
                                 BSModalBorder confirmActionFlowModal, TransitionAccess access) {
        final TemplatePanel tp = buttonContainer.newTemplateTag(tt ->
                "<button transition='" + transitionName + " ' type='submit' class='btn flow-btn' wicket:id='" + buttonId + "'>\n <span wicket:id='flowButtonLabel' /> \n</button>\n"
        );
        final SingularButton singularButton = new SingularButton(buttonId, getFormInstance()) {
            @Override
            protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                showConfirmModal(transitionName, confirmActionFlowModal, ajaxRequestTarget);
            }

            @Override
            protected void onInitialize() {
                super.onInitialize();
                this.setEnabled(access.isEnabled());
                Optional<String> messageOpt = access.getMessage();
                if (messageOpt.isPresent()) {
                    this.add($b.attr("data-toggle", "tooltip"));
                    this.add($b.attr("data-placement", "top"));
                    this.add($b.attr("title", messageOpt.get()));
                }
            }
        };
        singularButton.add(new Label("flowButtonLabel", transitionName).setRenderBodyOnly(true));
        tp.add(singularButton);
    }

    protected void showConfirmModal(String transitionName, BSModalBorder modal, AjaxRequestTarget ajaxRequestTarget) {
        TransitionController<?>       controller       = getTransitionControllerMap().get(transitionName);
        STypeBasedFlowConfirmModal<?> flowConfirmModal = transitionConfirmModalMap.get(transitionName);
        boolean                       show             = controller == null || controller.onShow(getInstance(), flowConfirmModal.getInstanceModel().getObject(), modal, ajaxRequestTarget);
        if (show) {
            modal.show(ajaxRequestTarget);
        }

    }

    /**
     * @param idSuffix  -> button id suffix
     * @param container -> modal container
     * @param tn        -> transition name
     * @param im        -> instance model
     * @return
     */
    private BSModalBorder buildFlowConfirmationModal(String idSuffix, BSContainer<?> container, String tn, IModel<? extends SInstance> im) {
        final FlowConfirmPanel flowConfirmPanel = resolveFlowConfirmModal("confirmPanel" + idSuffix, tn);
        container.appendTag("div", flowConfirmPanel);
        return flowConfirmPanel.getModalBorder();
    }

    /**
     * @param id             - the modal id
     * @param transitionName -> the transition name
     * @return the FlowConfirmPanel
     */
    protected FlowConfirmPanel resolveFlowConfirmModal(String id, String transitionName) {
        TransitionController<?> controller = getTransitionControllerMap().get(transitionName);
        if (controller == null || controller.getType() == null) {
            return new SimpleMessageFlowConfirmModal<>(id, transitionName, this);
        }
        RefType refType = getFormRequirementService().loadRefType(controller.getType());
        FormKey formKey = loadFormKeyFromTypeAndTask(controller.getType(), false).orElse(null);
        STypeBasedFlowConfirmModal<?> modal = new STypeBasedFlowConfirmModal<>(
                id,
                transitionName,
                this,
                refType,
                formKey,
                controller);
        transitionConfirmModalMap.put(transitionName, modal);
        return modal;
    }

    private boolean isMainForm() {
        return config.isMainForm();
    }

    @Nonnull
    private String getFormType() {
        return config.getFormName();
    }

    protected ViewMode getViewMode(FormPageExecutionContext formPageConfig) {
        return formPageConfig.getViewMode();
    }

    protected AnnotationMode getAnnotationMode(FormPageExecutionContext formPageConfig) {
        return formPageConfig.getAnnotationMode();
    }

    @Nonnull
    protected final Optional<FormKey> loadFormKeyFromTypeAndTask(@Nonnull Class<? extends SType<?>> typeClass, boolean mainForm) {
        return formRequirementService.findFormRequirementEntity(getRequirement(), typeClass, mainForm)
                .map(x -> x.getCurrentDraftEntity() == null ? x.getForm() : x.getCurrentDraftEntity().getForm())
                .map(formEntity -> formRequirementService.formKeyFromFormEntity(formEntity));
    }

    private IReadOnlyModel<SInstance> getInstanceModel() {
        return (IReadOnlyModel<SInstance>) singularFormPanel::getInstance;
    }

    private Component buildFlowButtons() {
        BSContainer<?> buttonContainer = new BSContainer<>("custom-buttons");
        buttonContainer.setVisible(true);

        configureCustomButtons(buttonContainer, modalContainer, (ViewMode.EDIT == getViewMode(config) || AnnotationMode.EDIT == getAnnotationMode(config)), getFormInstance());

        return buttonContainer;
    }


    private Component buildSendButton(final BSModalBorder enviarModal) {
        final Component button = makeServerSendButton("send-btn", getFormInstance(), enviarModal);
        return button.add(visibleOnlyIfDraftInEditionBehaviour());
    }


    protected Component buildSaveButton(String id) {
        final Component button = new SingularButton(id, getFormInstance()) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                try {
                    eventOnSaveAction(target);
                    addToastrSuccessMessage("message.success");
                } catch (HibernateOptimisticLockingFailureException e) {
                    getLogger().debug(e.getMessage(), e);
                    addToastrErrorMessage("message.save.concurrent_error");
                }
            }
        };
        return button.add(visibleOnlyInEditionBehaviour());
    }

    private void eventOnSaveAction(AjaxRequestTarget target) {
        getRequirement().saveForm(getFormInstance().getObject());
        atualizarContentWorklist(target);
    }

    private void addSaveCallBackUrl() {
        saveFormAjaxBehavior = new AbstractDefaultAjaxBehavior() {
            @Override
            protected void respond(AjaxRequestTarget target) {
                eventOnSaveAction(target);
            }
        };
        add(saveFormAjaxBehavior);
    }

    @SuppressWarnings("rawtypes")
    protected AjaxLink<?> buildCloseButton() {
        return new AjaxLink("close-btn") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (isReadOnly()) {
                    atualizarContentWorklist(target);
                    target.appendJavaScript("window.close()");
                } else {
                    closeModal.show(target);
                }
            }
        };
    }

    private boolean isReadOnly() {
        return getViewMode(config) == ViewMode.READ_ONLY && getAnnotationMode(config) != AnnotationMode.EDIT;
    }

    protected BSModalBorder construirCloseModal() {
        BSModalBorder closeModal = new BSModalBorder("close-modal", getMessage("label.title.close.draft"));
        closeModal.addButton(BSModalBorder.ButtonStyle.CANCEL, "label.button.cancel", new AjaxButton("cancel-close-btn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                closeModal.hide(target);
            }
        });
        closeModal.addButton(BSModalBorder.ButtonStyle.CONFIRM, "label.button.confirm", new AjaxButton("close-btn") {
            @Override
            protected String getOnClickScript() {
                return " Singular.atualizarContentWorklist();"
                        + "window.close();";
            }
        });

        return closeModal;
    }

    protected Component buildValidateButton() {
        final SingularValidationButton button = new SingularValidationButton("validate-btn", singularFormPanel.getInstanceModel()) {

            @Override
            protected void onValidationSuccess(AjaxRequestTarget target, Form<?> form, IModel<? extends SInstance> instanceModel) {
                addToastrSuccessMessage("message.validation.success");
            }

            @Override
            protected void onValidationError(AjaxRequestTarget target, Form<?> form, IModel<? extends SInstance> instanceModel) {
                super.onValidationError(target, form, instanceModel);
                addToastrErrorMessage("message.validation.error");
            }
        };

        return button.add(visibleOnlyInEditionBehaviour());
    }

    protected Behavior visibleOnlyInEditionBehaviour() {
        return $b.visibleIf(() -> getViewMode(config).isEdition() || getAnnotationMode(config).editable());
    }

    protected Behavior visibleOnlyIfDraftInEditionBehaviour() {
        return $b.visibleIf(() -> !isFlowInstanceCreated() && getViewMode(config).isEdition());
    }

    protected IModel<? extends SInstance> getFormInstance() {
        return singularFormPanel.getInstanceModel();
    }

    public final SingularFormPanel getSingularFormPanel() {
        return singularFormPanel;
    }

    @Override
    protected IModel<String> getContentSubtitle() {
        return new Model<>();
    }

    protected Map<String, TransitionController<?>> getTransitionControllerMap() {
        return transitionControllerMap;
    }

    protected void configurarExibicaoNotificacoes() {
        notificacoesModal.setVisible(!getNotificacoes().getObject().isEmpty());
    }

    protected IModel<ArrayList<Pair<String, String>>> getNotificacoes() {
        return new Model<>(new ArrayList<>());
    }

    private Component buildNotificacoesModal(String id) {

        final String        modalPanelMarkup = div("modal-panel", null, div("list-view", null, div("notificacao")));
        final TemplatePanel modalPanel       = new TemplatePanel(id, modalPanelMarkup);

        final ListView<Pair<String, String>> listView = new ListView<Pair<String, String>>("list-view", getNotificacoes()) {
            @Override
            protected void populateItem(ListItem<Pair<String, String>> item) {
                item.add(new NotificationPanel("notificacao", item.getModel(), getRequirementModel()));
            }
        };

        final AjaxButton closeButton = new AjaxButton("close-button") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                notificacoesModal.hide(target);
            }
        };

        notificacoesModal = new BSModalBorder("modal-panel");
        notificacoesModal.setTitleText(Model.of("Notificações"));
        notificacoesModal.addButton(BSModalBorder.ButtonStyle.DEFAULT, Shortcuts.$m.ofValue("Fechar"), closeButton);
        notificacoesModal.setSize(BSModalBorder.Size.NORMAL);

        return modalPanel.add(notificacoesModal.add(listView));
    }

    private void appendViewNotificationsButton(final BSContainer<?> container) {
        final String markup =
                button("notificacoes", new HTMLParameters().add("type", "button").add("class", "btn"),
                        span("notificacoesLabel"), span("notificacoesBadge", new HTMLParameters().add("class", "badge").add("style", "margin-left:5px;top:-2px;"))
                );

        final TemplatePanel buttonPanel = container.newTemplateTag(tt -> markup);
        final AjaxButton viewButton = new AjaxButton("notificacoes") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                notificacoesModal.show(target);
            }
        };
        viewButton.add(new Label("notificacoesLabel", "Visualizar Notificações"));
        viewButton.add($b.visibleIf(() -> !getNotificacoes().getObject().isEmpty()));
        viewButton.add(new Label("notificacoesBadge", $m.get(() -> getNotificacoes().getObject().size())));
        buttonPanel.add(viewButton);
    }

    protected String getUserDisplayName() {
        Session session = Session.get();
        if (session instanceof SingularSession) {
            return SingularSession.get().getUserDetails().getDisplayName();
        }
        return "";
    }

    public Map<String, STypeBasedFlowConfirmModal<?>> getTransitionConfirmModalMap() {
        return transitionConfirmModalMap;
    }

    public void onConfirmTransition(String transitionName, IModel<? extends SInstance> instanceModel) {

    }
}