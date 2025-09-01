/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2018
 */
package de.muenchen.oss.keycloak.custom;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author roland
 */
@Getter
@Setter
public class TOTPRequest {
    private String username;
}
