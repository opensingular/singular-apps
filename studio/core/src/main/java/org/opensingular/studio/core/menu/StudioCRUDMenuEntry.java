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

package org.opensingular.studio.core.menu;

import org.opensingular.lib.commons.lambda.IPredicate;
import org.opensingular.lib.commons.ui.Icon;
import org.opensingular.studio.core.definition.StudioDefinition;
import org.opensingular.studio.core.view.StudioCRUDContent;
import org.opensingular.studio.core.view.StudioContent;

public class StudioCRUDMenuEntry extends ItemMenuEntry {

    private final StudioDefinition studioDefinition;

    public StudioCRUDMenuEntry(Icon icon, String name, StudioDefinition studioDefinition) {
        super(icon, name, null);
        this.studioDefinition = studioDefinition;
    }

    public StudioCRUDMenuEntry(Icon icon, String name, StudioDefinition studioDefinition, IPredicate<MenuEntry> visibilityFunction) {
        super(icon, name, visibilityFunction);
        this.studioDefinition = studioDefinition;
    }

    public StudioDefinition getStudioDefinition() {
        return studioDefinition;
    }

    @Override
    public StudioContent makeContent(String id) {
        return new StudioCRUDContent(id, this);
    }
}
