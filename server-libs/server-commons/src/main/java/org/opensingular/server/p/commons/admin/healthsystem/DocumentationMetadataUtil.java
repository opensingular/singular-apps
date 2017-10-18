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

package org.opensingular.server.p.commons.admin.healthsystem;

import org.opensingular.form.SType;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.form.type.core.SPackageDocumentation;

import java.util.Optional;

public class DocumentationMetadataUtil {
    /**
     * Fields that are hard-coded invisible, or do not exists or explicit marked as hidden for documentation should not appear in the documentation as it is not related to anything functional
     * for users.
     *
     * @param sType
     * @return
     */
    public static boolean isHiddenForDocumentation(SType<?> sType) {
        boolean visible = true;
        if (sType.hasAttributeDefinedInHierarchy(SPackageBasic.ATR_EXISTS)) {
            visible &= Optional.ofNullable(sType.getAttributeValue(SPackageBasic.ATR_EXISTS)).orElse(Boolean.TRUE);
        }
        if (sType.hasAttributeDefinedInHierarchy(SPackageBasic.ATR_VISIBLE)) {
            visible &= Optional.ofNullable(sType.getAttributeValue(SPackageBasic.ATR_VISIBLE)).orElse(Boolean.TRUE);
        }
        if (sType.hasAttributeDefinedInHierarchy(SPackageDocumentation.ATR_DOC_HIDDEN)) {
            visible &= !Optional.ofNullable(sType.getAttributeValue(SPackageDocumentation.ATR_DOC_HIDDEN)).orElse(Boolean.FALSE);
        }
        return !visible;
    }
}
