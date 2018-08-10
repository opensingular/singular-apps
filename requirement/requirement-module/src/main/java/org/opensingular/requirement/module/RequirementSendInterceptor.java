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

import org.opensingular.requirement.module.service.RequirementInstance;
import org.opensingular.requirement.module.service.dto.RequirementSubmissionResponse;

public interface RequirementSendInterceptor<RI extends RequirementInstance, RSR extends RequirementSubmissionResponse> {

    RSR newInstanceSubmissionResponse();

    void onBeforeSend(RI requirement, String codSubmitterActor, RSR response);

    void onAfterStartFlow(RI requirement, String codSubmitterActor, RSR response);


    void onBeforeStartFlow(RI requirement, String codSubmitterActor, RSR response);

    void onAfterSend(RI requirement, String codSubmitterActor, RSR response);

}
