package org.opensingular.singular.server.commons.form;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;

import org.opensingular.form.spring.SpringFormConfig;

public class SingularServerFormConfigFactory {

    @Inject
    private SingularServerDocumentFactory singularServerDocumentFactory;

    @Inject
    private SingularServerSpringTypeLoader serverSpringTypeLoader;

    @Bean
    public SpringFormConfig<String> formConfigWithDatabase() {
        SpringFormConfig<String> formConfigWithoutDatabase = new SpringFormConfig<>();
        formConfigWithoutDatabase.setTypeLoader(serverSpringTypeLoader);
        formConfigWithoutDatabase.setDocumentFactory(singularServerDocumentFactory);
        return formConfigWithoutDatabase;
    }
}
