package org.opensingular.server.commons.form;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import org.opensingular.form.SDictionary;
import org.opensingular.form.SFormUtil;
import org.opensingular.form.SType;
import org.opensingular.form.spring.SpringTypeLoader;
import org.opensingular.server.commons.config.SingularServerConfiguration;

public class SingularServerSpringTypeLoader extends SpringTypeLoader<String> {

    private final Map<String, Supplier<SType<?>>> entries = new LinkedHashMap<>();

    @Inject
    private SingularServerConfiguration singularServerConfiguration;

    public SingularServerSpringTypeLoader() {
    }

    @PostConstruct
    private void init() {
        singularServerConfiguration.getFormTypes().forEach(this::add);
    }

    private void add(Class<? extends SType<?>> type) {
        String typeName   = SFormUtil.getTypeName(type);
        String simpleName = StringUtils.defaultIfBlank(StringUtils.substringAfterLast(typeName, "."), typeName);
        add(typeName, simpleName, () -> {
            SDictionary d = SDictionary.create();
            d.loadPackage(SFormUtil.getTypePackage(type));
            return d.getType(type);
        });
    }

    private void add(String typeName, String displayName, Supplier<SType<?>> typeSupplier) {
        entries.put(typeName, typeSupplier);
    }

    @Override
    protected Optional<SType<?>> loadTypeImpl(String typeId) {
        return Optional.ofNullable(entries.get(typeId)).map(Supplier::get);
    }
}