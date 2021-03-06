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

import org.opensingular.requirement.module.config.DefaultContexts;
import org.opensingular.requirement.module.workspace.WorkspaceRegistry;

/**
 * Main definition of a requirement Module.
 * Every singular server module must define exactly one {@link SingularModule}.
 */
public interface SingularModule {

    /**
     * @return Textual key representing this module.
     * Should be a valid java identifier
     */
    String abbreviation();

    /**
     * @return Textual descriptive name of this module.
     */
    String name();

    /**
     * @param requirementRegistry All requirements managed by this module and its corresponding
     *               configuration must be declared in the {@link RequirementRegistry} object
     */
    void requirements(RequirementRegistry requirementRegistry);

    /**
     * <p> Setups the workspace, adding contexts that will be exposed by the application. All contexts added will be used
     * to register a WicketFilter at {@link WorkspaceAppInitializerListener} </p>
     *
     * <p>A simple example of implementation is:</p>
     *
     * <pre>
     * workspaceRegistry
     *      .add(MyFirstContext.class)
     *      .add(MySecondContext.class)
     * </pre>
     *
     * <p>Where MyFirstContext and MySecondContext are implementations of {@link org.opensingular.requirement.module.config.IServerContext}</p>
     *
     * @param workspaceRegistry the registry where the contexts should be added
     */
    void workspace(WorkspaceRegistry workspaceRegistry);

    /**
     * Add singular default workspaces, ex AdministrationContext
     */
    default void defaultWorkspace(WorkspaceRegistry workspaceRegistry) {
        workspaceRegistry.add(DefaultContexts.AdministrationContext.class);
    }

}