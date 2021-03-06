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

package org.opensingular.requirement.module;

import org.apache.wicket.protocol.http.WicketFilter;
import org.opensingular.lib.commons.scan.SingularClassPathScanner;
import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.requirement.module.config.IServerContext;
import org.opensingular.requirement.module.config.SingularWebAppInitializerListener;
import org.opensingular.requirement.module.exception.SingularServerException;
import org.opensingular.requirement.module.spring.security.config.SingularLogoutFilter;
import org.opensingular.requirement.module.workspace.WorkspaceRegistry;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Inicializa o workspace, fazendo a configuração de:
 * <p>
 * - Contextos {@link IServerContext}
 * - Boxs {@link org.opensingular.requirement.module.workspace.BoxDefinition}
 * - Requerimentos {@link SingularRequirement}
 */
public class WorkspaceAppInitializerListener implements SingularWebAppInitializerListener, Loggable {
    public static final String SERVLET_ATTRIBUTE_SGL_MODULE = "Singular-SingularModule";

    public static final String SERVLET_ATTRIBUTE_SGL_WORKSPACE_REGISTRY = "Singular-SingularWorkspaceRegistry";

    /**
     * Inicializa os contextos, registrando um WicketApplication para cada contexto
     */
    @Override
    public void onInitialize(ServletContext servletContext, AnnotationConfigWebApplicationContext applicationContext) {
        try {
            SingularModule module = lookupModule();
            WorkspaceRegistry workspaceRegistry = new WorkspaceRegistry();
            if (module != null) {
                module.requirements(new RequirementRegistry(applicationContext));
                module.workspace(workspaceRegistry);
                module.defaultWorkspace(workspaceRegistry);
            }

            for (IServerContext context : workspaceRegistry.getContexts()) {
                addWicketFilter(servletContext, context);
                Class<?> config = getSpringSecurityConfigClassByContext(context);
                if (config != null) {
                    applicationContext.register(config);
                    addLogoutFilter(servletContext, context);
                }
            }
            servletContext.setAttribute(SERVLET_ATTRIBUTE_SGL_MODULE, module);
            servletContext.setAttribute(SERVLET_ATTRIBUTE_SGL_WORKSPACE_REGISTRY, workspaceRegistry);

        } catch (IllegalAccessException | InstantiationException ex) {
            getLogger().error(ex.getMessage(), ex);
        }
    }

    /**
     * Adiciona o Filtro Wicket para o contexto informado
     */
    protected void addWicketFilter(ServletContext ctx, IServerContext context) {
        FilterRegistration.Dynamic wicketFilter = ctx.addFilter(context.getName() + System.identityHashCode(context), WicketFilter.class);
        wicketFilter.setInitParameter("applicationClassName", context.getSettings().getWicketApplicationClass().getName());
        wicketFilter.setInitParameter(WicketFilter.FILTER_MAPPING_PARAM, context.getSettings().getContextPath());
        wicketFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, context.getSettings().getContextPath());
    }

    /**
     * Adiciona o Logout filter
     */
    protected void addLogoutFilter(ServletContext ctx, IServerContext context) {
        ctx
                .addFilter("singularLogoutFilter" + System.identityHashCode(context), SingularLogoutFilter.class)
                .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, context.getSettings().getUrlPath() + "/logout");
    }

    /**
     * Recupera a configuração de segurança por contexto, por padrão delega para o contexto atual
     */
    protected Class<? extends WebSecurityConfigurerAdapter> getSpringSecurityConfigClassByContext(IServerContext context) {
        return context.getSettings().getSpringSecurityConfigClass();
    }

    /**
     * Busca o modulo no classpath
     *
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private SingularModule lookupModule() throws IllegalAccessException, InstantiationException {
        Set<Class<? extends SingularModule>> modules = SingularClassPathScanner.get()
                .findSubclassesOf(SingularModule.class)
                .stream()
                .filter(f -> !Modifier.isAbstract(f.getModifiers()) || !Modifier.isInterface(f.getModifiers()))
                .collect(Collectors.toSet());

        if ((long) modules.size() != 1) {
            throw new SingularServerException(String.format("Apenas uma e somente uma implementação de %s " +
                    "é permitida por módulo. Encontradas: %s", SingularModule.class.getName(), String.valueOf(modules
                    .stream().map(Class::getName).collect(Collectors.toList()))));
        }
        Optional<Class<? extends SingularModule>> firstModule = modules.stream().findFirst();
        if (firstModule.isPresent()) {
            return firstModule.get().newInstance();
        }
        return null;
    }
}