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

package org.opensingular.requirement.module.admin.healthsystem.docs.presentation;

import java.util.Collection;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.requirement.module.admin.healthsystem.docs.DocFieldMetadata;

public class DefaultValueConverter implements FormFieldValueConverter, Loggable {

    public DefaultValueConverter() {
    }

    @Override
    public String format(DocFieldMetadata.DocFieldValue<?> fieldValue, Object value) {
        return defaultToString(value);

    }

    @SuppressWarnings("unchecked")
    protected String defaultToString(Object value) {
        if (value == null || (value instanceof Collection && ((Collection) value).isEmpty())) {
            return null;
        } else if (value instanceof Collection) {
            return Joiner.on(", ").join(((Collection<Object>) value).stream().map(this::defaultToString).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        } else {
            return String.valueOf(value);
        }

    }
}
