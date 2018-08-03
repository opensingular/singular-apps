package org.opensingular.requirement.module.config.workspace;

import net.vidageek.mirror.dsl.Mirror;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.opensingular.internal.lib.support.spring.injection.SingularSpringInjector;
import org.opensingular.lib.commons.ui.Icon;
import org.opensingular.requirement.module.wicket.box.BoxContent;
import org.opensingular.requirement.module.workspace.BoxDefinition;

public class WorkspaceMenuBoxItem implements WorkspaceMenuItem {
    private final Class<? extends BoxDefinition> boxDefitionClass;
    private BoxDefinition boxDefinition;

    public WorkspaceMenuBoxItem(Class<? extends BoxDefinition> boxDefitionClass) {
        this.boxDefitionClass = boxDefitionClass;
    }

    @Override
    public Panel newContent(String id) {
        return new BoxContent(id, new Model<>(boxDefinition));
    }

    @Override
    public Icon getIcon() {
        return boxDefinition.getItemBox().getIcone();
    }

    @Override
    public String getName() {
        return boxDefinition.getItemBox().getName();
    }

    @Override
    public String getDescription() {
        return boxDefinition.getItemBox().getDescription();
    }

    @Override
    public String getHelpText() {
        return boxDefinition.getItemBox().getHelpText();
    }

    public final BoxDefinition getBoxDefinition() {
        if (boxDefinition == null) {
            boxDefinition = new Mirror().on(boxDefitionClass).invoke().constructor().withoutArgs();
            SingularSpringInjector.get().inject(boxDefinition);
        }
        return boxDefinition;
    }

    @Override
    public boolean isVisible() {
        return boxDefinition.isVisible();
    }
}