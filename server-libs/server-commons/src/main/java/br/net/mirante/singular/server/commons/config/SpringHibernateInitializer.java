package br.net.mirante.singular.server.commons.config;

import br.net.mirante.singular.server.commons.spring.SingularServerSpringAppConfig;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

public abstract class SpringHibernateInitializer {

    protected static final String SPRING_MVC_DISPATCHER_SERVLET = "Spring MVC Dispatcher Servlet";

    public SpringHibernateInitializer() {

    }

    public AnnotationConfigWebApplicationContext init(ServletContext ctx) {
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(getSpringConfigurationClass());

        addSpringContextListener(ctx, applicationContext);
        addSpringRequestContextListener(ctx, applicationContext);
        addSpringMVCServlet(ctx, applicationContext);
        return applicationContext;
    }

    protected void addSpringContextListener(ServletContext ctx, AnnotationConfigWebApplicationContext applicationContext) {
        ctx.addListener(new ContextLoaderListener(applicationContext));
    }

    protected void addSpringRequestContextListener(ServletContext ctx, AnnotationConfigWebApplicationContext applicationContext) {
        ctx.addListener(RequestContextListener.class);
    }

    protected void addSpringMVCServlet(ServletContext ctx, AnnotationConfigWebApplicationContext applicationContext) {
        //Servlet
        ServletRegistration.Dynamic dispatcher = ctx.addServlet(SPRING_MVC_DISPATCHER_SERVLET, new DispatcherServlet(applicationContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping(getSpringMVCServletMapping());
    }

    /**
     * Fornece a classe que será utilizada como configuração java do Spring.
     * A classe fornecida deve herdar de {@link SingularServerSpringAppConfig} e deve
     * ser anotada com {@link org.springframework.context.annotation.Configuration}.
     * As principais configurações do pet server são feitas pela superclasse bastando declarar
     * na classe informada apenas as configurações e beans do spring específicos da aplicação
     *
     * @return Uma classe concreta que herda de {@link SingularServerSpringAppConfig} e anotada com {@link org.springframework.context.annotation.Configuration}
     */
    protected abstract Class<? extends SingularServerSpringAppConfig> getSpringConfigurationClass();

    protected String getSpringMVCServletMapping() {
        return "/*";
    }

}
