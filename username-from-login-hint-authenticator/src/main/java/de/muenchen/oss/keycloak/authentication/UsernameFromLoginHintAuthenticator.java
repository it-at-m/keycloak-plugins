package de.muenchen.oss.keycloak.authentication;

import jakarta.ws.rs.core.MultivaluedHashMap;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Objects;

/**
 * Nachbau der UsernameForm (<a href="https://github.com/keycloak/keycloak/blob/26.3.2/services/src/main/java/org/keycloak/authentication/authenticators/browser/UsernameForm.java">...</a>)
 * Diese ist als final deklariert und kann somit nicht erweitert werden.
 * Lediglich die Authenticate-Methode wurde angepasst. Alle anderen Methoden wurden 1-zu-1 uebernommen.
 */
public class UsernameFromLoginHintAuthenticator extends UsernamePasswordForm {

    @Override
    public void authenticate(final AuthenticationFlowContext context) {
        final String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        if (loginHint != null) {
            // We can skip the form, wenn LoginHint is present
            final MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.add(AuthenticationManager.FORM_USERNAME, loginHint);

            // validateUser sets user in Authentication context if successfully validated (not disabled, locked, etc.)
            if (this.validateUser(context, formData) && context.getUser() != null) {
                // We can skip the form when user is re-authenticating. Unless current user has some IDP set, so he can re-authenticate with that IDP
                if (!this.hasLinkedBrokers(context)) {
                    context.success();
                    return;
                }
            }
        }
        super.authenticate(context);
    }

    // Kopie aus der urspruenglichen UsernameForm
    @Override
    protected boolean validateForm(final AuthenticationFlowContext context, final MultivaluedMap<String, String> formData) {
        return this.validateUser(context, formData);
    }

    @Override
    protected Response challenge(final AuthenticationFlowContext context, final MultivaluedMap<String, String> formData) {
        final LoginFormsProvider forms = context.form();

        if (!formData.isEmpty()) forms.setFormData(formData);

        return forms.createLoginUsername();
    }

    @Override
    protected Response createLoginForm(final LoginFormsProvider form) {
        return form.createLoginUsername();
    }

    @Override
    protected String getDefaultChallengeMessage(final AuthenticationFlowContext context) {
        if (context.getRealm().isLoginWithEmailAllowed()) return Messages.INVALID_USERNAME_OR_EMAIL;
        return Messages.INVALID_USERNAME;
    }

    /**
     * Checks if the context user, if it has been set, is currently linked to any IDPs they could use to authenticate.
     * If the auth session has an existing IDP in the brokered context, it is filtered out.
     *
     * @param context a reference to the {@link AuthenticationFlowContext}
     * @return {@code true} if the context user has federated IDPs that can be used for authentication; {@code false} otherwise.
     */
    private boolean hasLinkedBrokers(AuthenticationFlowContext context) {
        KeycloakSession session = context.getSession();
        UserModel user = context.getUser();
        if (user == null) {
            return false;
        }
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
        final IdentityProviderModel existingIdp = (serializedCtx == null) ? null : serializedCtx.deserialize(session, authSession).getIdpConfig();

        return session.users().getFederatedIdentitiesStream(session.getContext().getRealm(), user)
                .map(fedIdentity -> session.identityProviders().getByAlias(fedIdentity.getIdentityProvider()))
                .filter(Objects::nonNull)
                .anyMatch(idpModel -> existingIdp == null || !Objects.equals(existingIdp.getAlias(), idpModel.getAlias()));

    }

}