package br.net.mirante.singular.pet.module.wicket.view.template;


import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import javax.inject.Inject;

import static br.net.mirante.singular.util.wicket.util.WicketUtils.$b;


public abstract class Content extends Panel {

    private boolean withBreadcrumb;
    private boolean withInfoLink;

    public Content(String id) {
        this(id, false, false);
    }

    public Content(String id, boolean withInfoLink, boolean withBreadcrumb) {
        super(id);
        this.withBreadcrumb = withBreadcrumb;
        this.withInfoLink = withInfoLink;
    }

    public Content addSideBar() {
        return this;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("contentTitle", getContentTitlelModel()));
        add(new Label("contentSubtitle", getContentSubtitlelModel()));
        WebMarkupContainer breadcrumb = new WebMarkupContainer("breadcrumb");
        add(breadcrumb);
        breadcrumb.add(new WebMarkupContainer("breadcrumbDashboard").add(
                $b.attr("href", "null null")));
        breadcrumb.add(getBreadcrumbLinks("_BreadcrumbLinks"));
        if (!withBreadcrumb) {
            breadcrumb.add(new AttributeAppender("class", "hide", " "));
        }
        WebMarkupContainer infoLink = new WebMarkupContainer("_Info");
        add(infoLink.setVisible(withInfoLink));
        if (withInfoLink) {
            infoLink.add(getInfoLink("_InfoLink"));
        }
    }

    protected WebMarkupContainer getBreadcrumbLinks(String id) {
        return new WebMarkupContainer(id);
    }

    protected WebMarkupContainer getInfoLink(String id) {
        return new WebMarkupContainer(id);
    }

    protected abstract IModel<?> getContentTitlelModel();

    protected abstract IModel<?> getContentSubtitlelModel();
}