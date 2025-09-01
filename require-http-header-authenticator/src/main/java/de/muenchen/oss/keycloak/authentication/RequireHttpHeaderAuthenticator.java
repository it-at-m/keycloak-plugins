package de.muenchen.oss.keycloak.authentication;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

/**
 * Simple {@link Authenticator} that checks of the user has a given {@link RoleModel Role}.
 */
public class RequireHttpHeaderAuthenticator implements Authenticator {
    protected static final Logger LOG = Logger.getLogger(RequireHttpHeaderAuthenticator.class);

    @Override
    public void authenticate(final AuthenticationFlowContext context) {
        final String NEGATE_LOGIC = context.getAuthenticatorConfig().getConfig().get(RequireHttpHeaderAuthenticatorFactory.NEGATE_LOGIC);
        final String HEADER_NAME = context.getAuthenticatorConfig().getConfig().get(RequireHttpHeaderAuthenticatorFactory.HEADER_NAME);
        final String HEADER_VALUE = context.getAuthenticatorConfig().getConfig().get(RequireHttpHeaderAuthenticatorFactory.HEADER_VALUE);

        final String headerString = context.getHttpRequest().getHttpHeaders().getHeaderString(HEADER_NAME);

        // Debugging - Start
        LOG.debugf("Header-Check for %s started.", HEADER_NAME);
        LOG.debugf("NEGATE_LOGIC: |%s|", NEGATE_LOGIC);
        LOG.debugf("Header-Name: |%s|", HEADER_NAME);
        LOG.debugf("Header-Value: |%s|", HEADER_VALUE);
        LOG.debugf("Header for key |%s|: |%s|", HEADER_NAME, headerString);
        // Debugging - End

        if (Boolean.parseBoolean(NEGATE_LOGIC)) {
            if (this.isNotEmpty(headerString) && headerString.equals(HEADER_VALUE)) {
                LOG.debugf("requireHttpHeader not statisfied: |%s| is set to |%s|, expecting |%s| != |%s|. Aborting with INVALID_USER error. ", HEADER_NAME, headerString, HEADER_NAME, HEADER_VALUE);
                context.failure(AuthenticationFlowError.INVALID_USER);
            } else {
                LOG.debugf("requireHttpHeader statisfied: |%s| is not set to |%s|, actual value is |%s|. Succeeding execution", HEADER_NAME, HEADER_VALUE, headerString);
                context.success();
            }
        } else {
            if (this.isNotEmpty(headerString) && headerString.equals(HEADER_VALUE)) {
                LOG.debugf("requireHttpHeader statisfied: |%s| is set to |%s|. Succeeding execution", HEADER_NAME, headerString);
                context.success();
            } else {
                LOG.debugf("requireHttpHeader not statisfied: |%s| is set to |%s|, expecting: |%s| == |%s|. Aborting with INVALID_USER error.", HEADER_NAME, headerString, HEADER_NAME, HEADER_VALUE);
                context.failure(AuthenticationFlowError.INVALID_USER);
            }
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(final KeycloakSession session, final RealmModel realm, final UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(final KeycloakSession session, final RealmModel realm, final UserModel user) {
        // NOOP
    }

    @Override
    public void action(final AuthenticationFlowContext context) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }

    private boolean isNotEmpty(final String toCheck) {
        return toCheck != null && toCheck.length() > 0;
    }
}
