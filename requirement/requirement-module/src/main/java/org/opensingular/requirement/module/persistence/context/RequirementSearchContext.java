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

package org.opensingular.requirement.module.persistence.context;


import java.util.ArrayList;
import java.util.List;

import org.opensingular.requirement.module.persistence.filter.BoxFilter;
import org.opensingular.requirement.module.persistence.query.RequirementSearchAliases;
import org.opensingular.requirement.module.persistence.query.RequirementSearchExtender;
import org.opensingular.requirement.module.persistence.query.RequirementSearchQuery;
import org.opensingular.requirement.module.spring.security.SingularPermission;

public class RequirementSearchContext {

    private List<SingularPermission> permissions = new ArrayList<>();
    private Boolean evaluatePermissions = Boolean.FALSE;
    private Boolean count = Boolean.FALSE;
    private RequirementSearchAliases aliases = new RequirementSearchAliases();
    private RequirementSearchQuery query;

    private BoxFilter boxFilter;
    private List<RequirementSearchExtender> extenders;

    public RequirementSearchContext(BoxFilter boxFilter) {
        this.boxFilter = boxFilter;
    }

    public BoxFilter getBoxFilter() {
        return boxFilter;
    }

    public List<SingularPermission> getPermissions() {
        return permissions;
    }

    public Boolean getEvaluatePermissions() {
        return evaluatePermissions;
    }

    public RequirementSearchContext setEvaluatePermissions(Boolean evaluatePermissions) {
        this.evaluatePermissions = evaluatePermissions;
        return this;
    }

    public Boolean getCount() {
        return count;
    }

    public RequirementSearchContext setCount(Boolean count) {
        this.count = count;
        return this;
    }

    public RequirementSearchContext addPermissions(List<SingularPermission> permissions) {
        this.permissions.addAll(permissions);
        return this;
    }

    public List<RequirementSearchExtender> getExtenders() {
        return extenders;
    }

    public RequirementSearchContext setExtenders(List<RequirementSearchExtender> extenders) {
        this.extenders = extenders;
        return this;
    }

    public RequirementSearchAliases getAliases() {
        return aliases;
    }

    public RequirementSearchContext setQuery(RequirementSearchQuery query) {
        this.query = query;
        return this;
    }

    public RequirementSearchQuery getQuery() {
        return query;
    }
}