package org.opensingular.server.p.core.wicket.view;

import org.opensingular.lib.wicket.util.menu.MetronicMenu;
import org.opensingular.server.commons.config.IServerContext;
import org.opensingular.server.commons.persistence.entity.form.PetitionEntity;
import org.opensingular.server.commons.service.PetitionService;
import org.opensingular.server.commons.wicket.view.template.Menu;
import org.opensingular.server.p.commons.config.PServerContext;

import javax.inject.Inject;

public class MenuPeticionamento extends Menu {

    private static final long serialVersionUID = 7622791136418841943L;

    @Inject
    private PetitionService<PetitionEntity> peticaoService;

    private MetronicMenu menu;

    public MenuPeticionamento(String id) {
        super(id);
    }

    @Override
    protected void onBeforeRender() {
        replace(buildMenu());
        super.onBeforeRender();
    }

    @Override
    protected MetronicMenu buildMenu() {
        loadMenuGroups();

        menu = new MetronicMenu("menu");

        buildMenuSelecao();

        getCategorias().forEach((processGroup) -> buildMenuGroup(menu, processGroup));

        return menu;
    }

    private void buildMenuSelecao() {
        SelecaoMenuItem selecaoMenuItem = new SelecaoMenuItem(categorias);
        menu.addItem(selecaoMenuItem);
    }

    @Override
    public IServerContext getMenuContext() {
        return PServerContext.PETITION;
    }
}
