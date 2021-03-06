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

package org.opensingular.requirement.commons.util.url;

import org.apache.wicket.request.Url;
import org.junit.Test;
import org.opensingular.requirement.module.util.url.UrlToolkit;
import org.opensingular.requirement.module.util.url.UrlToolkitBuilder;

import static org.junit.Assert.assertEquals;

public class UrlToolkitTest {

    @Test
    public void buildUrlWithoutSlash() {
        UrlToolkit urlBuilder = new UrlToolkitBuilder().build(Url.parse("http://localhost:8080"));
        String     url        = urlBuilder.concatServerAdressWithContext("/singular");

        assertEquals("http://localhost:8080/singular", url);
    }

    @Test
    public void buildUrlBothWithSlash() {
        UrlToolkit urlBuilder = new UrlToolkitBuilder().build(Url.parse("http://localhost:8080/"));
        String     url   = urlBuilder.concatServerAdressWithContext("/singular");

        assertEquals("http://localhost:8080/singular", url);
    }

    @Test
    public void buildUrlContextWithoutSlash() {
        UrlToolkit urlBuilder = new UrlToolkitBuilder().build(Url.parse("http://localhost:8080/"));
        String     url   = urlBuilder.concatServerAdressWithContext("singular");

        assertEquals("http://localhost:8080/singular", url);
    }
}