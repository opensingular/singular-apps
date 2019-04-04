package org.opensingular.studio.core.panel.button;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;

public abstract class HeaderRightButtonAjax implements IHeaderRightButton {

    public abstract void onAction(AjaxRequestTarget target);

    @Override
    public AbstractLink createButton(String id) {
        return new AjaxLink<Void>(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                onAction(target);
            }
        };
    }
}
