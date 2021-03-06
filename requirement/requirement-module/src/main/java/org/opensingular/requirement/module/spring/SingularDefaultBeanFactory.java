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

package org.opensingular.requirement.module.spring;

import org.hibernate.SessionFactory;
import org.opensingular.app.commons.cache.SingularKeyGenerator;
import org.opensingular.app.commons.jobs.AttachmentGCJob;
import org.opensingular.app.commons.mail.persistence.dao.EmailAddresseeDao;
import org.opensingular.app.commons.mail.persistence.dao.EmailDao;
import org.opensingular.app.commons.mail.schedule.SingularSchedulerBean;
import org.opensingular.app.commons.mail.schedule.TransactionalQuartzScheduledService;
import org.opensingular.app.commons.mail.service.email.DefaultMailSenderREST;
import org.opensingular.app.commons.mail.service.email.EmailPersistenceService;
import org.opensingular.app.commons.mail.service.email.EmailSender;
import org.opensingular.app.commons.mail.service.email.EmailSenderScheduledJob;
import org.opensingular.app.commons.mail.service.email.IEmailService;
import org.opensingular.app.commons.mail.service.email.IMailSenderREST;
import org.opensingular.app.commons.spring.persistence.attachment.FormAttachmentService;
import org.opensingular.app.commons.spring.persistence.attachment.IFormAttachmentService;
import org.opensingular.app.commons.spring.persistence.attachment.ServerAttachmentPersistenceHelper;
import org.opensingular.app.commons.spring.persistence.attachment.ServerAttachmentPersistenceService;
import org.opensingular.app.commons.spring.persistence.attachment.ServerTemporaryAttachmentPersistenceService;
import org.opensingular.flow.core.FlowDefinitionCache;
import org.opensingular.flow.core.SingularFlowConfigurationBean;
import org.opensingular.flow.core.service.IUserService;
import org.opensingular.flow.persistence.dao.ModuleDAO;
import org.opensingular.form.SType;
import org.opensingular.form.document.SDocument;
import org.opensingular.form.persistence.dao.AttachmentContentDao;
import org.opensingular.form.persistence.dao.AttachmentDao;
import org.opensingular.form.persistence.dao.FormAnnotationDAO;
import org.opensingular.form.persistence.dao.FormAnnotationVersionDAO;
import org.opensingular.form.persistence.dao.FormAttachmentDAO;
import org.opensingular.form.persistence.dao.FormCacheFieldDAO;
import org.opensingular.form.persistence.dao.FormCacheValueDAO;
import org.opensingular.form.persistence.dao.FormDAO;
import org.opensingular.form.persistence.dao.FormTypeDAO;
import org.opensingular.form.persistence.dao.FormVersionDAO;
import org.opensingular.form.service.FormService;
import org.opensingular.form.service.FormTypeService;
import org.opensingular.form.service.IFormService;
import org.opensingular.form.spring.SpringFormConfig;
import org.opensingular.form.spring.UserDetailsProvider;
import org.opensingular.form.type.core.attachment.IAttachmentPersistenceHandler;
import org.opensingular.form.type.core.attachment.IAttachmentRef;
import org.opensingular.form.type.core.attachment.helper.IAttachmentPersistenceHelper;
import org.opensingular.lib.commons.base.SingularProperties;
import org.opensingular.lib.commons.context.spring.SpringServiceRegistry;
import org.opensingular.lib.commons.pdf.HtmlToPdfConverter;
import org.opensingular.lib.commons.scan.SingularClassPathScanner;
import org.opensingular.lib.support.persistence.SessionLocator;
import org.opensingular.lib.support.spring.security.DefaultRestUserDetailsService;
import org.opensingular.lib.support.spring.security.RestUserDetailsService;
import org.opensingular.requirement.module.config.IServerContext;
import org.opensingular.requirement.module.config.ServerStartExecutorBean;
import org.opensingular.requirement.module.connector.DefaultModuleService;
import org.opensingular.requirement.module.connector.ModuleService;
import org.opensingular.requirement.module.extrato.ExtratoGenerator;
import org.opensingular.requirement.module.extrato.ExtratoGeneratorImpl;
import org.opensingular.requirement.module.flow.SingularServerFlowConfigurationBean;
import org.opensingular.requirement.module.flow.builder.RequirementFlowDefinition;
import org.opensingular.requirement.module.form.DefinitionsPackageProvider;
import org.opensingular.requirement.module.form.FormTypesProvider;
import org.opensingular.requirement.module.form.SingularServerDocumentFactory;
import org.opensingular.requirement.module.form.SingularServerSpringTypeLoader;
import org.opensingular.requirement.module.persistence.dao.ParameterDAO;
import org.opensingular.requirement.module.persistence.dao.flow.ActorDAO;
import org.opensingular.requirement.module.persistence.dao.flow.TaskInstanceDAO;
import org.opensingular.requirement.module.persistence.dao.form.ApplicantDAO;
import org.opensingular.requirement.module.persistence.dao.form.DraftDAO;
import org.opensingular.requirement.module.persistence.dao.form.FormRequirementDAO;
import org.opensingular.requirement.module.persistence.dao.form.RequirementContentHistoryDAO;
import org.opensingular.requirement.module.persistence.dao.form.RequirementDAO;
import org.opensingular.requirement.module.persistence.dao.form.RequirementDefinitionDAO;
import org.opensingular.requirement.module.persistence.filter.BoxFilterFactory;
import org.opensingular.requirement.module.persistence.query.config.DefaultRequirementSearchQueryConfig;
import org.opensingular.requirement.module.persistence.query.config.RequirementSearchQueryConfig;
import org.opensingular.requirement.module.service.DefaultRequirementService;
import org.opensingular.requirement.module.service.FormRequirementService;
import org.opensingular.requirement.module.service.ParameterService;
import org.opensingular.requirement.module.service.RequirementDefinitionService;
import org.opensingular.requirement.module.service.RequirementService;
import org.opensingular.requirement.module.service.SingularDiffService;
import org.opensingular.requirement.module.spring.security.AuthorizationService;
import org.opensingular.requirement.module.spring.security.AuthorizationServiceImpl;
import org.opensingular.requirement.module.spring.security.DefaultUserDetailService;
import org.opensingular.requirement.module.spring.security.PermissionResolverService;
import org.opensingular.requirement.module.spring.security.SingularUserDetailsService;
import org.opensingular.requirement.module.workspace.WorkspaceRegistry;
import org.opensingular.schedule.IScheduleService;
import org.opensingular.schedule.ScheduleDataBuilder;
import org.opensingular.ws.wkhtmltopdf.client.MockHtmlToPdfConverter;
import org.opensingular.ws.wkhtmltopdf.client.RestfulHtmlToPdfConverter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import static org.opensingular.app.commons.cache.SingularKeyGenerator.SINGULAR_KEY_GENERATOR;


@SuppressWarnings("rawtypes")
@Lazy(false)
public class SingularDefaultBeanFactory {

    @Order(1)
    @Bean
    @Lazy(false)
    public SpringServiceRegistry getSpringServiceRegistry() {
        return new SpringServiceRegistry();
    }

    @Primary
    @Bean(name = "peticionamentoUserDetailService")
    public SingularUserDetailsService workListUserDetailServiceFactory() {
        return new DefaultUserDetailService();
    }

    @Bean
    public SingularDiffService singularDiffService() {
        return new SingularDiffService();
    }

    @Bean
    public RequirementDAO requirementDAO() {
        return new RequirementDAO();
    }

    @Bean
    public RequirementContentHistoryDAO requirementContentHistoryDAO() {
        return new RequirementContentHistoryDAO();
    }

    @Bean
    public FormRequirementDAO formRequirementDAO() {
        return new FormRequirementDAO();
    }

    @Bean
    public DraftDAO draftDAO() {
        return new DraftDAO();
    }

    @Bean
    public RequirementService requirementService() {
        return new DefaultRequirementService();
    }

    @Bean
    public TaskInstanceDAO taskInstanceDAO() {
        return new TaskInstanceDAO();
    }

    @Bean
    public ActorDAO actorDAO() {
        return new ActorDAO();
    }

    @Bean
    public ModuleDAO moduleDAO() {
        return new ModuleDAO();
    }

    @Bean(name = SDocument.FILE_PERSISTENCE_SERVICE)
    public IAttachmentPersistenceHandler<IAttachmentRef> attachmentPersistenceService() {
        return new ServerAttachmentPersistenceService();
    }

    @Bean(name = SDocument.FILE_TEMPORARY_SERVICE)
    public IAttachmentPersistenceHandler<IAttachmentRef> attachmentTemporaryService() {
        return new ServerTemporaryAttachmentPersistenceService();
    }

    @Bean
    public AttachmentDao attachmentDao() {
        return new AttachmentDao();
    }

    @Bean
    public AttachmentContentDao attachmentContentDao() {
        return new AttachmentContentDao();
    }

    @Bean
    public IFormService formService() {
        return new FormService();
    }

    @Bean
    public FormTypeService formTypeService() {
        return new FormTypeService();
    }

    @Bean
    public FormCacheFieldDAO formCacheFieldDAO() {
        return new FormCacheFieldDAO();
    }

    @Bean
    public FormCacheValueDAO formCacheValueDAO() {
        return new FormCacheValueDAO();
    }

    @Bean
    public ApplicantDAO applicantDAO() {
        return new ApplicantDAO();
    }

    @Bean
    public FormDAO formDAO() {
        return new FormDAO();
    }

    @Bean
    public FormVersionDAO formVersionDAO() {
        return new FormVersionDAO();
    }

    @Bean
    public FormAnnotationDAO formAnnotationDAO() {
        return new FormAnnotationDAO();
    }

    @Bean
    public FormAnnotationVersionDAO formAnnotationVersionDAO() {
        return new FormAnnotationVersionDAO();
    }

    @Bean
    public FormTypeDAO formTypeDAO() {
        return new FormTypeDAO();
    }

    @Bean
    public IUserService userService() {
        return new SingularDefaultUserService();
    }

    @Bean
    public PermissionResolverService getPermissionResolverService() {
        return new PermissionResolverService();
    }

    @Bean
    public AuthorizationService getAuthorizationService() {
        return new AuthorizationServiceImpl();
    }

    @Bean
    public EmailDao<?> emailDao() {
        return new EmailDao<>();
    }

    @Bean
    public EmailAddresseeDao<?> emailAddresseeDao() {
        return new EmailAddresseeDao<>();
    }

    @Bean
    public RequirementDefinitionDAO<?> requirementDefinitionDAO() {
        return new RequirementDefinitionDAO<>();
    }

    @Bean
    @DependsOn(SDocument.FILE_PERSISTENCE_SERVICE)
    public IEmailService<?> emailService() {
        return new EmailPersistenceService();
    }


    @Bean
    public ParameterDAO parameterDAO() {
        return new ParameterDAO();
    }

    @Bean
    public ParameterService parameterService() {
        return new ParameterService();
    }

    @Bean
    public FormRequirementService formRequirementService() {
        return new FormRequirementService();
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheCacheManager() {
        EhCacheManagerFactoryBean cacheManager = new EhCacheManagerFactoryBean();
        cacheManager.setConfigLocation(new ClassPathResource("default-singular-ehcache.xml"));
        cacheManager.setShared(true);
        return cacheManager;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(EhCacheManagerFactoryBean ehCacheManagerFactoryBean) {
        return new EhCacheCacheManager(ehCacheManagerFactoryBean.getObject());
    }

    @Bean(name = SINGULAR_KEY_GENERATOR)
    public KeyGenerator singularKeyGenerator() {
        return new SingularKeyGenerator();
    }

    @Bean
    public FormAttachmentDAO formAttachmentDAO() {
        return new FormAttachmentDAO();
    }

    @Bean
    public IFormAttachmentService formAttachmentService() {
        return new FormAttachmentService();
    }

    @Bean
    public IAttachmentPersistenceHelper serverAttachmentPersistenceHelper(IFormService formService, IFormAttachmentService attachmentService) {
        return new ServerAttachmentPersistenceHelper(formService, attachmentService);
    }

    @Bean
    public RestUserDetailsService restUserDetailsService() {
        return new DefaultRestUserDetailsService();
    }

    @Bean
    public ServerStartExecutorBean lifecycle() {
        return new ServerStartExecutorBean();
    }

    @Bean
    public ModuleService moduleDriver() {
        return new DefaultModuleService();
    }

    @Bean
    public UserDetailsProvider singularUserDetails() {
        return new UserDetailsProvider();
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowSemicolon(true);
        return firewall;
    }

    @Bean
    public HtmlToPdfConverter htmlToPdfConverter() {
        if (SingularProperties.get().isTrue(SingularProperties.SINGULAR_DEV_MODE)) {
            return new MockHtmlToPdfConverter();
        } else {
            return RestfulHtmlToPdfConverter.createUsingDefaultConfig();
        }
    }

    @Bean
    public ExtratoGenerator extratoGenerator() {
        return new ExtratoGeneratorImpl();
    }

    @Bean
    public SingularServerDocumentFactory documentFactory() {
        return new SingularServerDocumentFactory();
    }

    @Bean
    public SingularServerSpringTypeLoader typeLoader() {
        return new SingularServerSpringTypeLoader();
    }

    @Bean
    public SpringFormConfig<String> formConfigWithDatabase(SingularServerDocumentFactory singularServerDocumentFactory,
                                                           SingularServerSpringTypeLoader serverSpringTypeLoader) {
        SpringFormConfig<String> formConfigWithoutDatabase = new SpringFormConfig<>();
        formConfigWithoutDatabase.setTypeLoader(serverSpringTypeLoader);
        formConfigWithoutDatabase.setDocumentFactory(singularServerDocumentFactory);
        return formConfigWithoutDatabase;
    }

    @Bean
    public SingularFlowConfigurationBean singularFlowConfiguration() {
        FlowDefinitionCache.invalidateAll();
        return new SingularServerFlowConfigurationBean();
    }

    @Bean
    public DefinitionsPackageProvider definitionsPackageProvider() {
        String[] packages = lookupFlowDefinitionackages();
        return () -> packages;
    }

    /**
     * Procuta todas as definições de flow
     * TODO Vinicius ainda precisamos disso?
     */
    protected String[] lookupFlowDefinitionackages() {
        return SingularClassPathScanner.get()
                .findSubclassesOf(RequirementFlowDefinition.class)
                .stream()
                .map(c -> c.getPackage().getName())
                .distinct().toArray(String[]::new);
    }

    @Bean
    public FormTypesProvider formTypesProvider() {
        List<Class<? extends SType<?>>> formTypes = lookupFormTypes();
        return () -> formTypes;
    }

    /**
     * Procura todos os STypes do ClassPath
     */
    protected List<Class<? extends SType<?>>> lookupFormTypes() {
        return SingularClassPathScanner.get()
                .findSubclassesOf(SType.class)
                .stream()
                .filter(f -> !Modifier.isAbstract(f.getModifiers()))
                .map(i -> (Class<? extends SType<?>>) i)
                .collect(Collectors.toList());
    }

    @Bean
    public RequirementDefinitionService requirementDefinitionService() {
        return new RequirementDefinitionService();
    }

    @Bean
    public BoxFilterFactory boxFilterFactory() {
        return new BoxFilterFactory();
    }

    @Bean
    @DependsOn("schedulerFactoryBean")
    public IScheduleService scheduleService(SingularSchedulerBean schedulerFactoryBean) {
        return new TransactionalQuartzScheduledService(schedulerFactoryBean);
    }

    /**
     * Configure the SchedulerBean for Singular.
     * This bean have to implents InitializingBean to work properly.
     *
     * @return SingularSchedulerBean instance.
     */
    @Bean
    public SingularSchedulerBean schedulerFactoryBean(DataSource dataSource) {
        return new SingularSchedulerBean(dataSource);
    }

    @Bean
    public EmailSender emailSender() {
        return new EmailSender();
    }

    @Bean
    @DependsOn({"emailSender", "scheduleService", "emailService"})
    public EmailSenderScheduledJob scheduleEmailSenderJob(IScheduleService scheduleService) {
        EmailSenderScheduledJob emailSenderScheduledJob = new EmailSenderScheduledJob(ScheduleDataBuilder.buildMinutely(1));
        scheduleService.schedule(emailSenderScheduledJob);
        return emailSenderScheduledJob;
    }

    @Bean
    public IMailSenderREST mailSenderREST() {
        return new DefaultMailSenderREST();
    }

    @Bean
    public AttachmentGCJob scheduleAttachmentGCJob(IScheduleService scheduleService) {
        AttachmentGCJob attachmentGCJob = new AttachmentGCJob(ScheduleDataBuilder.buildDaily(1, 1));
        scheduleService.schedule(attachmentGCJob);
        return attachmentGCJob;
    }

    @Bean
    @Scope(ConfigurableWebApplicationContext.SCOPE_REQUEST)
    public IServerContext serverContext(HttpServletRequest httpServletRequest, WorkspaceRegistry workspaceRegistry) {
        if (httpServletRequest != null) {
            String contextPath = httpServletRequest.getContextPath();
            String context     = httpServletRequest.getPathInfo().replaceFirst(contextPath, "");
            for (IServerContext ctx : workspaceRegistry.getContexts()) {
                if (context.startsWith(ctx.getSettings().getUrlPath())) {
                    return ctx;
                }
            }
        }
        return null;
    }

    @Bean
    public SessionLocator sessionProvider(SessionFactory sessionFactory) {
        return () -> sessionFactory.getCurrentSession();
    }

    @Bean
    public RequirementSearchQueryConfig requirementSearchQueryConfig() {
        return new DefaultRequirementSearchQueryConfig();
    }
}