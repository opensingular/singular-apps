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

package org.opensingular.requirement.module.admin.healthsystem.stypes;

import org.opensingular.form.SIComposite;
import org.opensingular.form.SInfoType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.STypeList;
import org.opensingular.form.TypeBuilder;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.view.list.SViewListByTable;
import org.opensingular.requirement.module.admin.healthsystem.validation.webchecker.IProtocolChecker;
import org.opensingular.requirement.module.admin.healthsystem.validation.webchecker.ProtocolCheckerFactory;

import java.util.Arrays;

@SInfoType(spackage = SSystemHealthPackage.class,  name = SWebHealth.TYPE_NAME, label = "Utilitário de rede")
public class SWebHealth extends STypeComposite<SIComposite> {
	public static final String TYPE_NAME = "webhealth";
	public static final String TYPE_FULL_NAME = SSystemHealthPackage.PACKAGE_NAME+"."+TYPE_NAME;

	@Override
	protected void onLoadType(TypeBuilder tb) {

        STypeList<STypeComposite<SIComposite>, SIComposite> urlsList = this.addFieldListOfComposite("urls", "urlsList");
        urlsList.withView(()->new SViewListByTable());

        this
        	.asAtr()
        		.label("Url (Protocolos suportados: " +
                        Arrays.asList(ProtocolCheckerFactory.values()).toString().replace("[", "").replace("]", "")
                        + ")");

        STypeComposite<SIComposite> table = urlsList.getElementsType();

        STypeString urlField = table.addFieldString("url");
		urlField
	        .asAtr()
	        	.maxLength(100)
	        .asAtrBootstrap()
	        	.colPreference(3);

		urlField.addInstanceValidator(validatable->{
			try {
				IProtocolChecker protocolChecker = ProtocolCheckerFactory.getProtocolChecker(validatable.getInstance().getValue());
				protocolChecker.protocolCheck(validatable);
			} catch (Exception e) {
                getLogger().error(e.getMessage(), e);
				validatable.error(e.getMessage());
			}
		});
	}
}
