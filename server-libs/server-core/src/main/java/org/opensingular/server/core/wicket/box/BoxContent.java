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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.opensingular.flow.persistence.entity.Actor;
import org.opensingular.lib.commons.lambda.IBiFunction;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.lib.wicket.util.datatable.BSDataTableBuilder;
import org.opensingular.lib.wicket.util.datatable.IBSAction;
import org.opensingular.lib.wicket.util.datatable.column.BSActionColumn;
import org.opensingular.lib.wicket.util.modal.BSModalBorder;
import org.opensingular.lib.wicket.util.resource.Icone;
import org.opensingular.server.commons.box.ItemBoxDataList;
import org.opensingular.server.commons.flow.actions.ActionAtribuirRequest;
import org.opensingular.server.commons.flow.actions.ActionRequest;
import org.opensingular.server.commons.flow.actions.ActionResponse;
import org.opensingular.server.commons.form.FormAction;
import org.opensingular.server.commons.persistence.filter.QuickFilter;
import org.opensingular.server.commons.service.dto.BoxItemAction;
import org.opensingular.server.commons.service.dto.DatatableField;
import org.opensingular.server.commons.service.dto.FormDTO;
import org.opensingular.server.commons.service.dto.ItemAction;
import org.opensingular.server.commons.service.dto.ItemActionConfirmation;
import org.opensingular.server.commons.service.dto.ItemActionType;
import org.opensingular.server.commons.service.dto.ItemBox;
import org.opensingular.server.commons.service.dto.ItemBoxMetadata;
import org.opensingular.server.commons.service.dto.ProcessDTO;
import org.opensingular.server.commons.wicket.buttons.NewRequirementLink;
import org.opensingular.server.commons.wicket.view.util.DispatcherPageParameters;
import org.opensingular.server.commons.wicket.view.util.DispatcherPageUtil;
import org.opensingular.server.core.wicket.history.HistoryPage;
import org.opensingular.server.core.wicket.model.BoxItemModel;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.opensingular.lib.wicket.util.util.WicketUtils.$b;
import static org.opensingular.lib.wicket.util.util.WicketUtils.$m;
import static org.opensingular.server.commons.RESTPaths.PATH_BOX_SEARCH;
import static org.opensingular.server.commons.wicket.view.util.DispatcherPageParameters.FORM_NAME;

public class BoxContent extends AbstractBoxContent<BoxItemModel> implements Loggable {

    protected IModel<BoxItemModel>    currentModel;
    private   Pair<String, SortOrder> sortProperty;
    private   IModel<ItemBoxMetadata> itemBoxModel;

    public BoxContent(String id, String processGroupCod, String menu, ItemBoxMetadata itemBox) {
        super(id, processGroupCod, menu);
        this.itemBoxModel = new Model<>(itemBox);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        configureQuickFilter();
    }

    private void configureQuickFilter() {
        getFiltroRapido().setVisible(isShowQuickFilter());
        getPesquisarButton().setVisible(isShowQuickFilter());
    }

    @Override
    public Component buildNewPetitionButton(String id) {
        if (isShowNew() && getMenu() != null) {
            return new NewRequirementLink(id, getBaseUrl(), getLinkParams(), new PropertyModel<>(itemBoxModel, "requirements"));
        } else {
            return super.buildNewPetitionButton(id);
        }
    }

    @Override
    protected void appendPropertyColumns(BSDataTableBuilder<BoxItemModel, String, IColumn<BoxItemModel, String>> builder) {
        for (DatatableField entry : getFieldsDatatable()) {
            builder.appendPropertyColumn($m.ofValue(entry.getKey()), entry.getLabel(), entry.getLabel());
        }
    }

    @Override
    protected void appendActionColumns(BSDataTableBuilder<BoxItemModel, String, IColumn<BoxItemModel, String>> builder) {
        BSActionColumn<BoxItemModel, String> actionColumn = new BSActionColumn<>(getMessage("label.table.column.actions"));

        ItemBox itemBox = getItemBoxModelObject();

        if (getLogger().isDebugEnabled()) {
            getLogger().debug(String.format("A caixa %s permite a apresentação apenas das ações %s", itemBox.getName(), Arrays.toString(itemBox.getActions().keySet().toArray())));
        }
        for (ItemAction itemAction : itemBox.getActions().values()) {

            if (itemAction.getType() == ItemActionType.POPUP) {
                actionColumn.appendStaticAction(
                        $m.ofValue(itemAction.getLabel()),
                        itemAction.getIcon(),
                        linkFunction(itemAction, getBaseUrl(), getLinkParams()),
                        visibleFunction(itemAction),
                        c -> c.styleClasses($m.ofValue("worklist-action-btn")));
            } else if (itemAction.getType() == ItemActionType.ENDPOINT) {
                actionColumn.appendAction(
                        $m.ofValue(itemAction.getLabel()),
                        itemAction.getIcon(),
                        dynamicLinkFunction(itemAction, getProcessGroup().getConnectionURL(), getLinkParams()),
                        visibleFunction(itemAction),
                        c -> c.styleClasses($m.ofValue("worklist-action-btn")));
            }
        }

        actionColumn
                .appendStaticAction(
                        getMessage("label.table.column.history"),
                        Icone.HISTORY,
                        this::criarLinkHistorico,
                        (x) -> Boolean.TRUE,
                        c -> c.styleClasses($m.ofValue("worklist-action-btn")));

        builder.appendColumn(actionColumn);
    }

    private MarkupContainer criarLinkHistorico(String id, IModel<BoxItemModel> boxItemModel) {
        BoxItemModel   boxItem        = boxItemModelObject(boxItemModel);
        PageParameters pageParameters = new PageParameters();
        if (boxItem.getProcessInstanceId() != null) {
            pageParameters.add(DispatcherPageParameters.PETITION_ID, boxItem.getCod());
            pageParameters.add(DispatcherPageParameters.INSTANCE_ID, boxItem.getProcessInstanceId());
            pageParameters.add(DispatcherPageParameters.PROCESS_GROUP_PARAM_NAME, getProcessGroup().getCod());
            pageParameters.add(DispatcherPageParameters.MENU_PARAM_NAME, getMenu());
        }
        BookmarkablePageLink<?> historiLink = new BookmarkablePageLink<>(id, getHistoricoPage(), pageParameters);
        historiLink.setVisible(boxItem.getProcessBeginDate() != null);
        return historiLink;
    }

    protected Class<? extends HistoryPage> getHistoricoPage() {
        return HistoryPage.class;
    }

    @Override
    protected WebMarkupContainer criarLinkEdicao(String id, IModel<BoxItemModel> peticao) {
        if (boxItemModelObject(peticao).getProcessBeginDate() == null) {
            return criarLink(id, peticao, FormAction.FORM_FILL);
        } else {
            return criarLink(id, peticao, FormAction.FORM_FILL_WITH_ANALYSIS);
        }
    }

    public IBiFunction<String, IModel<BoxItemModel>, MarkupContainer> linkFunction(ItemAction itemAction, String baseUrl, Map<String, String> additionalParams) {
        return (id, boxItemModel) -> {
            String url = mountStaticUrl(itemAction, baseUrl, additionalParams, boxItemModel);

            WebMarkupContainer link = new WebMarkupContainer(id);
            link.add($b.attr("target", String.format("_%s_%s", itemAction.getName(), boxItemModelObject(boxItemModel).getCod())));
            link.add($b.attr("href", url));
            return link;
        };
    }

    private BoxItemModel boxItemModelObject(IModel<BoxItemModel> boxItemModel) {
        return boxItemModel.getObject();
    }

    private String mountStaticUrl(ItemAction itemAction, String baseUrl, Map<String, String> additionalParams, IModel<BoxItemModel> boxItemModel) {
        final BoxItemAction action = boxItemModelObject(boxItemModel).getActionByName(itemAction.getName());
        if (action.getEndpoint().startsWith("http")) {
            return action.getEndpoint();
        } else {
            return baseUrl
                    + action.getEndpoint()
                    + appendParameters(additionalParams);
        }
    }

    private IBSAction<BoxItemModel> dynamicLinkFunction(ItemAction itemAction, String baseUrl, Map<String, String> additionalParams) {
        if (itemAction.getConfirmation() != null) {
            return (target, model) -> {
                currentModel = model;
                final BSModalBorder confirmationModal = construirModalConfirmationBorder(itemAction, baseUrl, additionalParams);
                confirmationForm.addOrReplace(confirmationModal);
                confirmationModal.show(target);
            };
        } else {
            return (target, model) -> executeDynamicAction(itemAction, baseUrl, additionalParams, boxItemModelObject(model), target);
        }
    }

    protected void executeDynamicAction(ItemAction itemAction, String baseUrl, Map<String, String> additionalParams, BoxItemModel boxItem, AjaxRequestTarget target) {
        final BoxItemAction boxAction = boxItem.getActionByName(itemAction.getName());

        String url = baseUrl
                + boxAction.getEndpoint()
                + appendParameters(additionalParams);

        try {
            callModule(url, buildCallObject(boxAction, boxItem));
        } catch (Exception e) {
            getLogger().error("Erro ao acessar serviço: " + url, e);
            addToastrErrorMessage("Não foi possível executar esta ação.");
        } finally {
            target.add(tabela);
        }
    }

    protected void relocate(ItemAction itemAction, String baseUrl, Map<String, String> additionalParams, BoxItemModel boxItem, AjaxRequestTarget target, Actor actor) {
        final BoxItemAction boxAction = boxItem.getActionByName(itemAction.getName());

        String url = baseUrl
                + boxAction.getEndpoint()
                + appendParameters(additionalParams);

        try {
            callModule(url, buildCallAtribuirObject(boxAction, boxItem, actor));
        } catch (Exception e) {
            getLogger().error("Erro ao acessar serviço: " + url, e);
            addToastrErrorMessage("Não foi possível executar esta ação.");
        } finally {
            target.add(tabela);
        }
    }

    protected Object buildCallAtribuirObject(BoxItemAction boxAction, BoxItemModel boxItem, Actor actor) {
        ActionAtribuirRequest actionRequest = new ActionAtribuirRequest();
        actionRequest.setIdUsuario(getBoxPage().getIdUsuario());
        if (actor == null) {
            actionRequest.setEndLastAllocation(true);
        } else {
            actionRequest.setIdUsuarioDestino(actor.getCodUsuario());
        }
        if (boxAction.isUseExecute()) {
            actionRequest.setName(boxAction.getName());
            actionRequest.setLastVersion(boxItem.getVersionStamp());
        }

        return actionRequest;
    }

    private void callModule(String url, Object arg) {
        ActionResponse response = new RestTemplate().postForObject(url, arg, ActionResponse.class);
        if (response.isSuccessful()) {
            addToastrSuccessMessage(response.getResultMessage());
        } else {
            addToastrErrorMessage(response.getResultMessage());
        }
    }

    private Object buildCallObject(BoxItemAction boxAction, BoxItemModel boxItem) {
        ActionRequest actionRequest = new ActionRequest();
        actionRequest.setIdUsuario(getBoxPage().getIdUsuario());
        if (boxAction.isUseExecute()) {
            actionRequest.setName(boxAction.getName());
            actionRequest.setLastVersion(boxItem.getVersionStamp());
        }

        return actionRequest;
    }

    protected BSModalBorder construirModalConfirmationBorder(ItemAction itemAction, String baseUrl, Map<String, String> additionalParams) {
        final ItemActionConfirmation confirmation      = itemAction.getConfirmation();
        BSModalBorder                confirmationModal = new BSModalBorder("confirmationModal", $m.ofValue(confirmation.getTitle()));
        confirmationModal.addOrReplace(new Label("message", $m.ofValue(confirmation.getConfirmationMessage())));

        Model<Actor>        actorModel     = new Model<>();
        IModel<List<Actor>> actorsModel    = $m.get(() -> buscarUsuarios(currentModel, confirmation));
        DropDownChoice      dropDownChoice = criarDropDown(actorsModel, actorModel);
        dropDownChoice.setVisible(StringUtils.isNotBlank(confirmation.getSelectEndpoint()));
        confirmationModal.addOrReplace(dropDownChoice);

        confirmationModal.addButton(BSModalBorder.ButtonStyle.CANCEL, $m.ofValue(confirmation.getCancelButtonLabel()), new AjaxButton("cancel-delete-btn", confirmationForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                currentModel = null;
                confirmationModal.hide(target);
            }
        }.setDefaultFormProcessing(false));

        appendExtraButtons(confirmationModal, actorModel, itemAction, baseUrl, additionalParams);

        if (StringUtils.isNotBlank(confirmation.getSelectEndpoint())) {
            confirmationModal.addButton(BSModalBorder.ButtonStyle.CONFIRM, $m.ofValue(confirmation.getConfirmationButtonLabel()), new AjaxButton("delete-btn", confirmationForm) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    relocate(itemAction, baseUrl, additionalParams, boxItemModelObject(currentModel), target, actorModel.getObject());
                    target.add(tabela);
                    atualizarContadores(target);
                    confirmationModal.hide(target);
                }
            });
        } else {
            confirmationModal.addButton(BSModalBorder.ButtonStyle.CONFIRM, $m.ofValue(confirmation.getConfirmationButtonLabel()), new AjaxButton("delete-btn", confirmationForm) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    executeDynamicAction(itemAction, baseUrl, additionalParams, boxItemModelObject(currentModel), target);
                    target.add(tabela);
                    atualizarContadores(target);
                    confirmationModal.hide(target);
                }
            });
        }

        return confirmationModal;
    }

    protected void appendExtraButtons(BSModalBorder confirmationModal, Model<Actor> actorModel, ItemAction itemAction, String baseUrl, Map<String, String> additionalParams) {

    }

    protected void atualizarContadores(AjaxRequestTarget target) {
        target.appendJavaScript("(function(){window.Singular.atualizarContadores();}())");
    }

    private DropDownChoice criarDropDown(IModel<List<Actor>> actorsModel, Model<Actor> model) {
        DropDownChoice<Actor> dropDownChoice = new DropDownChoice<>("selecao",
                model,
                actorsModel,
                new ChoiceRenderer<>("nome", "codUsuario"));
        dropDownChoice.setRequired(true);
        return dropDownChoice;
    }

    @SuppressWarnings("unchecked")
    private List<Actor> buscarUsuarios(IModel<BoxItemModel> currentModel, ItemActionConfirmation confirmation) {
        final String connectionURL = getProcessGroup().getConnectionURL();
        final String url           = connectionURL + PATH_BOX_SEARCH + confirmation.getSelectEndpoint();

        try {
            return Arrays.asList(new RestTemplate().postForObject(url, boxItemModelObject(currentModel), Actor[].class));
        } catch (Exception e) {
            getLogger().error("Erro ao acessar serviço: " + url, e);
            return Collections.emptyList();
        }
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

    private IFunction<IModel<BoxItemModel>, Boolean> visibleFunction(ItemAction itemAction) {
        return (model) -> {
            BoxItemModel boxItemModel = boxItemModelObject(model);
            boolean visible = boxItemModel.hasAction(itemAction);
            if (!visible) {
                getLogger().debug("Action {} não está disponível para o item ({}: código da petição) da listagem ", itemAction.getName(), boxItemModel.getCod());
            }

            return visible;
        };
    }

    @Override
    protected Pair<String, SortOrder> getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(Pair<String, SortOrder> sortProperty) {
        this.sortProperty = sortProperty;
    }

    @Override
    protected void onDelete(BoxItemModel peticao) {

    }

    @Override
    protected QuickFilter montarFiltroBasico() {
        BoxPage boxPage = getBoxPage();
        return boxPage.createFilter()
                .withFilter(getFiltroRapidoModelObject())
                .withProcessesAbbreviation(getProcessesNames())
                .withTypesNames(getFormNames())
                .withRascunho(isWithRascunho())
                .withEndedTasks(getItemBoxModelObject().getEndedTasks());
    }

    private BoxPage getBoxPage() {
        return (BoxPage) getPage();
    }

    private List<String> getProcessesNames() {
        if (getProcesses() == null) {
            return Collections.emptyList();
        } else {
            return getProcesses()
                    .stream()
                    .map(ProcessDTO::getAbbreviation)
                    .collect(Collectors.toList());
        }
    }

    private List<String> getFormNames() {
        if (getForms() == null) {
            return Collections.emptyList();
        } else {
            return getForms()
                    .stream()
                    .map(FormDTO::getName)
                    .collect(Collectors.toList());
        }
    }

    @Override
    protected List<BoxItemModel> quickSearch(QuickFilter filter, List<String> siglasProcesso, List<String> formNames) {
        final String connectionURL = getProcessGroup().getConnectionURL();
        final String url           = connectionURL + getSearchEndpoint();
        try {
            return new RestTemplate().postForObject(url, filter, ItemBoxDataList.class)
                    .getItemBoxDataList()
                    .stream()
                    .map(BoxItemModel::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            getLogger().error("Erro ao acessar serviço: " + url, e);
            return Collections.emptyList();
        }
    }

    @Override
    protected WebMarkupContainer criarLink(String id, IModel<BoxItemModel> itemModel, FormAction formAction) {
        BoxItemModel item = boxItemModelObject(itemModel);
        String href = DispatcherPageUtil
                .baseURL(getBaseUrl())
                .formAction(formAction.getId())
                .petitionId(item.getCod())
                .param(FORM_NAME, item.get("type"))
                .params(getCriarLinkParameters(item))
                .build();

        WebMarkupContainer link = new WebMarkupContainer(id);
        link.add($b.attr("target", String.format("_%s_%s", formAction.getId(), item.getCod())));
        link.add($b.attr("href", href));
        return link;
    }

    @Override
    protected Map<String, String> getCriarLinkParameters(BoxItemModel item) {
        final Map<String, String> linkParameters = new HashMap<>();
        linkParameters.putAll(getLinkParams());
        return linkParameters;
    }

    private Map<String, String> getLinkParams() {
        final BoxPage page = getBoxPage();
        return page.createLinkParams();
    }

    @Override
    protected long countQuickSearch(QuickFilter filter, List<String> processesNames, List<String> formNames) {
        final String connectionURL = getProcessGroup().getConnectionURL();
        final String url           = connectionURL + getCountEndpoint();
        try {
            return new RestTemplate().postForObject(url, filter, Long.class);
        } catch (Exception e) {
            getLogger().error("Erro ao acessar serviço: " + url, e);
            return 0;
        }
    }

    @Override
    public IModel<?> getContentTitleModel() {
        return $m.ofValue(getItemBoxModelObject().getName());
    }

    @Override
    public IModel<?> getContentSubtitleModel() {
        return $m.ofValue(getItemBoxModelObject().getDescription());
    }

    public boolean isShowNew() {
        return getItemBoxModelObject().isShowNewButton();
    }

    public boolean isShowQuickFilter() {
        return getItemBoxModelObject().isQuickFilter();
    }

    public List<DatatableField> getFieldsDatatable() {
        return getItemBoxModelObject().getFieldsDatatable();
    }

    public String getSearchEndpoint() {
        return getItemBoxModelObject().getSearchEndpoint();
    }

    public String getCountEndpoint() {
        return getItemBoxModelObject().getCountEndpoint();
    }

    public boolean isWithRascunho() {
        return getItemBoxModelObject().isShowDraft();
    }

    private ItemBox getItemBoxModelObject() {
        return itemBoxModel.getObject().getItemBox();
    }
}
