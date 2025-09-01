/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2018
 */
package de.muenchen.oss.keycloak.custom;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * <p>A factory that creates {@link TOTPCustomResourceProvider} instances.
 */
public class TOTPCustomResourceProviderFactory implements RealmResourceProviderFactory {

    private static final String ID = "totp";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(final KeycloakSession session) {
        return new TOTPCustomResourceProvider(session);
    }

    @Override
    public void init(final Scope config) {
        // Needed to be overridden
    }

    @Override
    public void postInit(final KeycloakSessionFactory factory) {
        // Needed to be overridden
    }

    @Override
    public void close() {
        // Needed to be overridden
    }

}
