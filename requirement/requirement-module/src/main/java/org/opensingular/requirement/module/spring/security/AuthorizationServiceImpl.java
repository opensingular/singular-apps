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

package org.opensingular.requirement.module.spring.security;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensingular.flow.persistence.entity.AbstractTaskInstanceEntity;
import org.opensingular.form.context.SFormConfig;
import org.opensingular.lib.commons.base.SingularProperties;
import org.opensingular.requirement.module.box.action.BoxItemActionList;
import org.opensingular.requirement.module.config.IServerContext;
import org.opensingular.requirement.module.service.RequirementInstance;
import org.opensingular.requirement.module.service.RequirementService;
import org.opensingular.requirement.module.service.dto.BoxItemAction;
import org.opensingular.requirement.module.wicket.SingularSession;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Classe responsável por resolver as permissões do usuário em permissões do singular
 *
 * @author Vinicius Nunes
 */
public class AuthorizationServiceImpl implements AuthorizationService {

    @Inject
    protected PermissionResolverService permissionResolverService;

    @Inject
    protected RequirementService requirementService;

    @Inject
    @Named("peticionamentoUserDetailService")
    private SingularUserDetailsService peticionamentoUserDetailService;

    @Inject
    @Named("formConfigWithDatabase")
    private Optional<SFormConfig<String>> singularFormConfig;

    @Override
    public boolean hasPermission(String idUsuario, String permissionKey) {
        List<SingularPermission> permissions = searchPermissions(idUsuario);
        return hasPermission(idUsuario, new SingularPermission(permissionKey, null), permissions);
    }

    @Override
    public void filterActions(String formType, Long requirementId, BoxItemActionList actions, String idUsuario) {
        List<SingularPermission> permissions = searchPermissions(idUsuario);
        filterActions(formType, requirementId, actions, idUsuario, permissions);
    }

    @SuppressWarnings("unchecked")
    private void filterActions(String formType, Long requirementId, BoxItemActionList actions, String idUsuario, List<SingularPermission> permissions) {
        RequirementAuthMetadataDTO requirementAuthMetadataDTO = null;
        if (requirementId != null) {
            requirementAuthMetadataDTO = requirementService.findRequirementAuthMetadata(requirementId);
        }
        for (Iterator<BoxItemAction> it = actions.iterator(); it.hasNext(); ) {
            BoxItemAction      action           = it.next();
            SingularPermission permissionsNeeded;
            String             typeAbbreviation = getFormSimpleName(formType);
            permissionsNeeded = buildPermissionKey(requirementAuthMetadataDTO, typeAbbreviation, action.getName());
            if (!hasPermission(idUsuario, permissionsNeeded, permissions)) {
                it.remove();
            }
        }

    }

    @Override
    public List<SingularPermission> filterListTaskPermissions(List<SingularPermission> permissions) {
        return permissions.stream().filter(p -> p != null && p.getSingularId() != null && p.getSingularId().startsWith(LIST_TASKS_PERMISSION_PREFIX)).collect(Collectors.toList());
    }


    private List<SingularPermission> searchPermissions(String userPermissionKey) {
        if (SingularSession.exists()) {
            SingularRequirementUserDetails userDetails = SingularSession.get().getUserDetails();
            if (userDetails != null && userPermissionKey != null && userPermissionKey.equals(userDetails.getApplicantId())) {
                if (CollectionUtils.isEmpty(userDetails.getPermissions())) {
                    userDetails.addPermissions(peticionamentoUserDetailService.searchPermissions((String) userDetails.getApplicantId()));
                }
                return userDetails.getPermissions();
            }
        }
        return permissionResolverService.searchPermissions(userPermissionKey);
    }

    /**
     * Monta a chave de permissão do singular, não deve ser utilizado diretamente.
     *
     * @param requirementAuthMetadataDTO
     * @param formSimpleName
     * @param action
     * @return
     */
    private SingularPermission buildPermissionKey(RequirementAuthMetadataDTO requirementAuthMetadataDTO, String formSimpleName, String action) {
        SingularPermission permission = new SingularPermission(action, formSimpleName, getDefinitionKey(requirementAuthMetadataDTO), getCurrentTaskAbbreviation(requirementAuthMetadataDTO));
        if (getLogger().isTraceEnabled()) {
            getLogger().debug(String.format("Nome de permissão computada %s", permissionResolverService.toString()));
        }
        return permission;
    }

    private String getDefinitionKey(RequirementAuthMetadataDTO requirementAuthMetadataDTO) {
        if (requirementAuthMetadataDTO != null) {
            return requirementAuthMetadataDTO.getDefinitionKey();
        }
        return null;
    }

    private String getCurrentTaskAbbreviation(RequirementAuthMetadataDTO requirementAuthMetadataDTO) {
        if (requirementAuthMetadataDTO != null) {
            return requirementAuthMetadataDTO.getCurrentTaskAbbreviation();
        }
        return null;
    }

    private String upperCaseOrNull(String string) {
        if (string != null) {
            return string.toUpperCase();
        }
        return null;
    }


    @Override
    public boolean hasPermission(Long requirementId, String formType, String idUsuario, String action) {
        RequirementAuthMetadataDTO requirementAuthMetadataDTO = null;
        if (requirementId != null) {
            requirementAuthMetadataDTO = requirementService.findRequirementAuthMetadata(requirementId);
        }
        return hasPermission(requirementAuthMetadataDTO, formType, idUsuario, action);
    }

    private boolean hasPermission(RequirementAuthMetadataDTO requirementAuthMetadataDTO, String formType, String idUsuario, String action) {
        String formSimpleName = getFormSimpleName(formType);
        if (requirementAuthMetadataDTO != null) {
            formSimpleName = getFormSimpleName(requirementAuthMetadataDTO.getFormTypeAbbreviation());
        }
        return hasPermission(idUsuario, buildPermissionKey(requirementAuthMetadataDTO, formSimpleName, action).getSingularId());
    }


    private boolean hasPermission(String idUsuario, SingularPermission permissionNeeded, List<SingularPermission> permissions) {
        if (SingularProperties.get().isTrue(SingularProperties.DISABLE_AUTHORIZATION)) {
            return true;
        }

        if (permissions.stream().anyMatch(ps -> ps.matchesPermission(permissionNeeded))) {
            return true;
        }
        getLogger().info(" Usuário logado {} não possui a permissão {} ", idUsuario, permissionNeeded);
        return false;
    }

    private String getFormSimpleName(String formTypeName) {
        if (StringUtils.isBlank(formTypeName)) {
            return null;
        }
        return singularFormConfig
                .flatMap(formConfig -> formConfig.getTypeLoader().loadType(formTypeName))
                .map(type -> type.getNameSimple())
                .map(String::toUpperCase)
                .orElse(null);
    }

    @Transactional
    @Override
    public boolean hasPermission(Long requirementId, String formType, String userId, String applicantId, String action, IServerContext context, boolean readonly) {
        boolean hasPermission = hasPermission(
                requirementId,
                formType,
                userId,
                action
        );

        boolean isOwner       = false;
        boolean isAllowedUser = requirementId == null;

        if (requirementId != null) {
            if (context.getSettings().isCheckOwner()) {
                isOwner = isOwner(requirementId, userId, applicantId);

            } else {
                // Qualquer modo de edição o usuário deve ter permissão e estar alocado na tarefa,
                // para os modos de visualização basta a permissão.
                isAllowedUser = readonly || isUserAllowedToEdit(requirementId, userId, action);
            }
        }

        return hasPermission && (isOwner || isAllowedUser);
    }

    /**
     * Utility method used by {@link #hasPermission(Long, String, String, String, String, IServerContext, boolean)}
     */
    protected boolean isOwner(Long requirementId, String userId, String applicantId) {
        RequirementInstance requirement = requirementService.loadRequirementInstance(requirementId);
        boolean             truth       = Objects.equals(requirement.getApplicant().getIdPessoa(), applicantId);
        if (!truth) {
            getLogger()
                    .info("User {} (SingularRequirementUserDetails::getApplicantId={}) is not owner of Requirement with id={}. Expected owner id={} ",
                            userId, applicantId, requirementId, requirement.getApplicant().getIdPessoa());
        }
        return truth;
    }

    /**
     * Utility method used by {@link #hasPermission(Long, String, String, String, String, IServerContext, boolean)}
     */
    protected boolean isUserAllowedToEdit(Long requirementId, String idUsuario, String action) {
        return !isTaskAssignedToAnotherUser(requirementId, idUsuario);
    }

    /**
     * Utility method used by {@link #hasPermission(Long, String, String, String, String, IServerContext, boolean)}
     *
     * @param requirementId
     * @param idUsuario
     * @return
     */
    protected boolean isTaskAssignedToAnotherUser(Long requirementId, String idUsuario) {
        if (requirementId != null && idUsuario != null) {
            return requirementService.findCurrentTaskEntityByRequirementId(requirementId)
                    .map(AbstractTaskInstanceEntity::getTaskHistory)
                    .filter(histories -> !histories.isEmpty())
                    .map(histories -> histories.get(histories.size() - 1))
                    .map(history -> history.getAllocatedUser() != null
                            && history.getAllocationEndDate() == null
                            && !idUsuario.equalsIgnoreCase(history.getAllocatedUser().getCodUsuario()))
                    .orElse(Boolean.FALSE);
        }
        return false;
    }

}