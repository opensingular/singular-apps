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

package org.opensingular.requirement.module;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensingular.form.SingularFormException;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.requirement.module.builder.SingularRequirementBuilder;
import org.opensingular.requirement.module.exception.SingularServerException;

/**
 * Configuration object for module {@link SingularRequirement} registration.
 */
public class RequirementConfiguration {

    private Set<SingularRequirementRef> requirements = new LinkedHashSet<>();

    /**
     * Register a  {@link SingularRequirement}
     *
     * @param requirement the {@link SingularRequirement} instance.
     * @return
     */
    public RequirementConfiguration addRequirement(SingularRequirement requirement) {
        requirements.add(new SingularRequirementRef(requirement));
        return this;
    }

    /**
     * Register a  {@link SingularRequirement}
     *
     * @param requirementProvider a {@link IFunction<SingularRequirementBuilder, SingularRequirement> } lambda to build the requirement definition
     *                            through an fluent interface builder.
     * @return
     */
    public RequirementConfiguration addRequirement(IFunction<SingularRequirementBuilder, SingularRequirement> requirementProvider) {
        if (!requirements.add(new SingularRequirementRef(requirementProvider))) {
            throw new SingularServerException("O mesmo " + SingularRequirement.class.getName() + " não pode ser configurado duas vezes no módulo");
        }
        return this;
    }


    public SingularRequirementRef getRequirementRef(SingularRequirement requirement) {
        return requirements
                .stream()
                .filter(ref -> ref.equals(new SingularRequirementRef(requirement)))
                .findFirst()
                .orElseThrow(() -> new SingularFormException("Não foi possível encontrar referência registrada para o requerimento informado"));
    }

    public SingularRequirementRef getRequirementRef(IFunction<SingularRequirementBuilder, SingularRequirement> requirementProvider) {
        return requirements
                .stream()
                .filter(ref -> ref.equals(new SingularRequirementRef(requirementProvider)))
                .findFirst()
                .orElseThrow(() -> new SingularFormException("Não foi possível encontrar referência registrada para o requerimento informado"));
    }

    List<SingularRequirementRef> getRequirements() {
        return requirements.stream().collect(Collectors.toList());
    }
}
