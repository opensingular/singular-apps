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

package org.opensingular.requirement.module.wicket;

import javax.inject.Inject;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Before;
import org.junit.Test;
import org.opensingular.form.wicket.helpers.SingularWicketTester;
import org.opensingular.requirement.commons.CommonsApplicationMock;
import org.opensingular.requirement.commons.SingularCommonsBaseTest;
import org.opensingular.requirement.module.form.FormAction;
import org.opensingular.requirement.module.spring.security.AuthorizationService;
import org.opensingular.requirement.module.test.SingularServletContextTestExecutionListener;
import org.opensingular.requirement.module.wicket.error.Page403;
import org.opensingular.requirement.module.wicket.view.form.FormPage;
import org.opensingular.requirement.module.wicket.view.util.dispatcher.DispatcherPage;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.opensingular.requirement.module.wicket.view.util.ActionContext.*;

@TestExecutionListeners(listeners = {SingularServletContextTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class DispatcherPageTest extends SingularCommonsBaseTest {


    @Inject
    CommonsApplicationMock singularApplication;

    private SingularWicketTester tester;

    @Inject
    private AuthorizationService authorizationService;

    @Before
    public void setUp() {
        super.setUp();
        reset(authorizationService);
    }

    @WithUserDetails("vinicius.nunes")
    @Test
    public void accessDenied() {
        tester = new SingularWicketTester(singularApplication);
        PageParameters pageParameters = new PageParameters();
        pageParameters.add(FORM_ACTION, FormAction.FORM_ANALYSIS.getId());
        pageParameters.add(FORM_NAME, "foooooo.STypeFoo");
        pageParameters.add(REQUIREMENT_DEFINITION_KEY, getRequirementDefinitionKey());
        tester.startPage(DispatcherPage.class, pageParameters);
        tester.assertRenderedPage(Page403.class);
    }

    @WithUserDetails("vinicius.nunes")
    @Test
    public void accessGranted() {
        when(authorizationService.hasPermission(any(), any(), any(), any(), any(), any(), anyBoolean())).thenReturn(true);
        tester = new SingularWicketTester(singularApplication);
        PageParameters pageParameters = new PageParameters();
        pageParameters.add(FORM_ACTION, FormAction.FORM_ANALYSIS.getId());
        pageParameters.add(FORM_NAME, "foooooo.STypeFoo");
        pageParameters.add(REQUIREMENT_DEFINITION_KEY, getRequirementDefinitionKey());
        tester.startPage(DispatcherPage.class, pageParameters);
        tester.assertRenderedPage(FormPage.class);
    }
}
