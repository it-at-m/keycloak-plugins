package de.muenchen.oss.keycloak.mapper;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.authorization.AuthorizationTokenService;
import org.keycloak.authorization.util.Tokens;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.Permission;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.cors.Cors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Mapper für den Userinfo-Endpunkt, um auch die Permissions/Authorities im Json-Objekt zu haben.
 * Unter Zuhilfenahme der Klasse AuthorizationTokenService.
 * Analog zum Ablauf in der Klasse TokenEndpoint.
 */
@Slf4j
public class AuthorityMapper extends AbstractOIDCProtocolMapper implements UserInfoTokenMapper {
    private static final String AUTHORITIES = "authorities";

    private static final String PARAM_NAME = "audience";

    /*
     * A config which keycloak uses to display a generic dialog to configure the token.
     */
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /*
     * The ID of the token mapper. Is public, because we need this id in our data-setup project to
     * configure the protocol mapper in keycloak.
     */
    private static final String PROVIDER_ID = "oidc-authorities-mapper";

    static {
        // The builtin protocol mapper let the user define for which tokens the protocol mapper
        // is executed (access token, id token, user info). To add the config options for the different types
        // to the dialog execute the following method. Note that the following method uses the interfaces
        // this token mapper implements to decide which options to add to the config. So if this token
        // mapper should never be available for some sort of options, e.g. like the id token, just don't
        // implement the corresponding interface.
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, AuthorityMapper.class);
        configProperties.add(createConfigProperty());
    }

    protected static ProviderConfigProperty createConfigProperty() {
        final ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(AuthorityMapper.AUTHORITIES);
        property.setLabel("Token Claim Name");
        property.setHelpText("The claim for Spring-Security compliant authorities.");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue(AuthorityMapper.AUTHORITIES);
        return property;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "User's authorities";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Maps the OpenId Connect authorities to the user's authorities.";
    }

    /**
     * Fügt die gewünschten Daten dem Token hinzu.
     *
     * @param token            siehe AbstractOIDCProtocolMapper.
     * @param mappingModel     siehe AbstractOIDCProtocolMapper.
     * @param userSession      siehe AbstractOIDCProtocolMapper.
     * @param keycloakSession  siehe AbstractOIDCProtocolMapper.
     * @param clientSessionCtx siehe AbstractOIDCProtocolMapper.
     */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {
        final String token_claim_name = mappingModel.getConfig().get(AUTHORITIES);
        mappingModel.getConfig().put(ProtocolMapperUtils.MULTIVALUED, "true");
        try (final Response response = AuthorizationTokenService.instance().authorize(getKeycloakAuthorizationRequest(keycloakSession))) {
            final AuthorizationResponse entity = (AuthorizationResponse) response.getEntity();
            final AccessToken accessToken = Tokens.getAccessToken(entity.getToken(), keycloakSession);
            if (accessToken != null) {
                final Set<String> permissions = accessToken.getAuthorization().getPermissions().stream()
                        .map(Permission::getResourceName)
                        .collect(Collectors.toSet());
                token.getOtherClaims().put(token_claim_name, permissions);
            } else {
                log.warn("Der AccessToken konnte nicht extrahiert werden.");
            }
        } catch (CorsErrorResponseException ex) {
            log.warn("Der böse Fehler ist aufgetreten.", ex);
            token.getOtherClaims().put(token_claim_name, Collections.emptySet());
        } catch (AuthorityMapperException ex) {
            log.warn(ex.getMessage());
            token.getOtherClaims().put(token_claim_name, Collections.emptySet());
        } catch (Exception ex) {
            log.warn("Die Authorities konnten nicht extrahiert werden.", ex);
            token.getOtherClaims().put(token_claim_name, Collections.emptySet());
        }
    }

    /**
     * Legt ein AuthorizationTokenService.KeycloakAuthorizationRequest-Objekt an.
     *
     * @param keycloakSession enthält die benötigten Daten.
     * @return AuthorizationTokenService.KeycloakAuthorizationRequest-Objekt.
     */
    private AuthorizationTokenService.KeycloakAuthorizationRequest getKeycloakAuthorizationRequest(final KeycloakSession keycloakSession) {
        final HttpRequest httpRequest = keycloakSession.getContext().getHttpRequest();
        final RealmModel realm = keycloakSession.getContext().getRealm();
        final ClientConnection clientConnection = keycloakSession.getContext().getConnection();
        final EventBuilder eventBuilder = new EventBuilder(realm, keycloakSession, clientConnection);
        // Zur Vermeidung folgender Exception: https://github.com/keycloak/keycloak/blob/release/24.0/server-spi-private/src/main/java/org/keycloak/events/EventBuilder.java#L226
        eventBuilder.event(EventType.PERMISSION_TOKEN);
        final TokenManager tokenmanager = new TokenManager();
        final Cors cors = Cors.builder().auth().allowedMethods("GET", "POST").exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS).allowAllOrigins();
        final AuthorizationProvider provider = keycloakSession.getProvider(AuthorizationProvider.class);

        final AuthorizationTokenService.KeycloakAuthorizationRequest authorizationRequest = new AuthorizationTokenService.KeycloakAuthorizationRequest(provider, tokenmanager, eventBuilder, httpRequest, cors, clientConnection);
        // Hier wird der Client ausgewählt
        authorizationRequest.setAudience(getAudienceString(keycloakSession));
        authorizationRequest.setSubjectToken(getTokenString(keycloakSession));
        return authorizationRequest;
    }

    /**
     * Holt den Token aus dem HttpRequestHeader und schneidet das Prefix ab.
     *
     * @param keycloakSession enthält die HttpRequestHeader.
     * @return Token als String ohne Prefix.
     */
    private String getTokenString(final KeycloakSession keycloakSession) {
        final HttpHeaders headers = keycloakSession.getContext().getRequestHeaders();
        if (headers == null) {
            throw new AuthorityMapperException("Keine Header im Context der KeyCloak-Session vorhanden.");
        }
        final String authorizationHeader = headers.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        final String accessTokenString = extractTokenStringFromHeader(authorizationHeader);
        if (accessTokenString == null) {
            throw new AuthorityMapperException("Kein Token im HttpRequestHeader vorhanden.");
        }
        return accessTokenString;
    }

    /**
     * Extrahiert aus dem AuthorizationHeader den AccessToken
     *
     * @param authHeader Authorization Header
     * @return accessToken As String
     */
    private String extractTokenStringFromHeader(final String authHeader) {
        if (authHeader == null) {
            throw new AuthorityMapperException("Kein authorizationHeader vorhanden.");
        }

        final String[] split = Pattern.compile("\\s+").split(authHeader.trim());
        if (split.length != 2) {
            throw new AuthorityMapperException("Ungueltige Tokenlaenge.");
        }

        final String bearerPart = split[0];
        if (!bearerPart.equalsIgnoreCase("Bearer")) {
            throw new AuthorityMapperException("Kein Token vom Typ 'bearer' vorhanden.");
        }

        final String tokenString = split[1];
        if (ObjectUtil.isBlank(tokenString)) {
            return null;
        }

        return tokenString;
    }

    /**
     * Holt den Header 'audience' aus den QueryParameters, wenn vorhanden.
     *
     * @param keycloakSession enthält die keycloakSession.
     * @return Audience als String oder clientId aus Token
     */
    private String getAudienceString(final KeycloakSession keycloakSession) {
        final MultivaluedMap<String, String> queryParameters = keycloakSession.getContext().getUri().getQueryParameters();
        String audience = keycloakSession.getContext().getClient().getClientId();
        if (queryParameters.containsKey(PARAM_NAME)) {
            final List<String> audienceList = queryParameters.get(PARAM_NAME);
            if (audienceList != null && audienceList.size() == 1) {
                audience = audienceList.getFirst();
            }
        }
        return audience;
    }

}
