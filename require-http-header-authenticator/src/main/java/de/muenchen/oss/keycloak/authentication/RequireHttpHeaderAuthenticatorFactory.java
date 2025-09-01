package de.muenchen.oss.keycloak.authentication;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

public class RequireHttpHeaderAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "require-http-header";

    static final String HEADER_NAME = "HEADER_NAME";
    static final String HEADER_VALUE = "HEADER_VALUE";
    static final String NEGATE_LOGIC = "NEGATE_LOGIC";

    public static final RequireHttpHeaderAuthenticator REQUIRE_HTTP_HEADER_AUTHENTICATOR = new RequireHttpHeaderAuthenticator();

    @Override
    public String getDisplayType() {
        return "Require HTTP-Header";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            // AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED,
            // AuthenticationExecutionModel.Requirement.CONDITIONAL
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Require a HTTP-Header to have a specified value";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        final ProviderConfigProperty negate_logic = new ProviderConfigProperty();
        negate_logic.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        negate_logic.setName(NEGATE_LOGIC);
        negate_logic.setLabel("negate logic");
        negate_logic.setHelpText("If on/true, the HTTP-Request-Header *must not* be set or *must not* have the specified value for this check to pass.");

        final ProviderConfigProperty headerName = new ProviderConfigProperty();
        headerName.setType(ProviderConfigProperty.STRING_TYPE);
        headerName.setName(HEADER_NAME);
        headerName.setLabel("HTTP Header Name");
        headerName.setHelpText("HTTP-Request-Header name the client has to provide.");

        final ProviderConfigProperty headerValue = new ProviderConfigProperty();
        headerValue.setType(ProviderConfigProperty.STRING_TYPE);
        headerValue.setName(HEADER_VALUE);
        headerValue.setLabel("HTTP Header Value");
        headerValue.setHelpText("Value of the Header that has to be set for this check to pass.");

        return Arrays.asList(negate_logic, headerName, headerValue);
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(final KeycloakSession session) {
        return REQUIRE_HTTP_HEADER_AUTHENTICATOR;
    }

    @Override
    public void init(final Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(final KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
