/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2018
 */
package de.muenchen.oss.keycloak.custom;

import java.util.List;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.forms.login.freemarker.model.TotpBean;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminRoot;
//up to 26.2
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.keycloak.credential.CredentialModel;
import org.keycloak.services.ErrorResponseException;
//from 26.3
//import org.keycloak.services.resources.admin.fgap.AdminPermissions;
import org.keycloak.utils.CredentialHelper;

/**
 * Bietet eine Restschnittstelle an, welche für einen User den QR-Code
 * zur 2-Faktor-Authentifizierung zurückliefert.
 */
public class TOTPCustomResourceProvider extends AdminRoot implements RealmResourceProvider {

    private static final Logger LOG = Logger.getLogger(TOTPCustomResourceProvider.class);

    private static final String CLIENT_ID = "realm-management";
    private static final String REQUIRED_ROLE = "custom-add-otp";
    private static final String REALM_SUFFIX = "-realm";

    /**
     * Ctor, der die aktuelle KeycloakSession erhält.
     *
     * @param session KeycloakSession
     */
    public TOTPCustomResourceProvider(final KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }


    /**
     * Bietet eine Restschnittstelle an, welche für einen User den QR-Code
     * zur 2-Faktor-Authentifizierung zurückliefert.
     *
     * @param headers     HttpHeaders des Requests
     * @param totpRequest der TOTPRequest an sich
     * @return TOTPResponse mit QR-Code
     */
    @POST
    @Produces("application/json")
    public TOTPResponse post(@Context final HttpHeaders headers, final TOTPRequest totpRequest) {
        final KeycloakContext context = session.getContext();
        final RealmModel targetRealm = context.getRealm(); //schon hier abholen; context.getRealm ändert sich nach Aufruf von authenticateRealmAdminRequest!

        //check if the admin has the permissions to execute this
        checkAdminPermissions(headers, targetRealm);

        //check inputs
        final String username = totpRequest.getUsername();
        UserModel user = findUser(username, targetRealm);

        //check if user does not yet have TOTP
        LOG.info("Checking for existing TOTP for user " + user.getUsername());
        //Typ bisher targetRealm.getOTPPolicy().getType() - jetzt "otp"
        Stream<CredentialModel> creds = user.credentialManager().getStoredCredentialsByTypeStream("otp");

        if (creds.findAny().isPresent()) {
            throw new ErrorResponseException("USER-HAS-OTP", "Current user already has an TOTP. Remove first before assigning a new one.", Response.Status.BAD_REQUEST);
        }

        //generate secret for target user in target realm
        final TotpBean totpBean = new TotpBean(session, targetRealm, user, context.getUri().getRequestUriBuilder());
        final String totpSecret = totpBean.getTotpSecret();
        final String totpSecretQrCode = totpBean.getTotpSecretQrCode();

        //store otp secret as new credentials for target user
        OTPCredentialModel ocm = OTPCredentialModel.createFromPolicy(targetRealm, totpSecret);
        CredentialHelper.createOTPCredential(session, targetRealm, user, "not-used", ocm);
        LOG.info("Generated new TOTP for user " + user.getUsername());

        //ALT
        /*
        final UserCredentialModel credentials = new UserCredentialModel();
        //credentials.setType(targetRealm.getOTPPolicy().getType());
        credentials.setType("otp");
        credentials.setValue(totpSecret);
        boolean result = session.userCredentialManager().updateCredential(targetRealm, user, credentials);
        */

        //If CONFIGURE_OTP Required Action exists for target user, remove it (gets added if user tries to log in without otp)
        if (user.getRequiredActionsStream().anyMatch((String action) -> action.equals(UserModel.RequiredAction.CONFIGURE_TOTP.name()))) {
            LOG.info("Remove required action CONFIGURE_TOTP of user " + user.getUsername());
            user.removeRequiredAction(UserModel.RequiredAction.CONFIGURE_TOTP.name());
        }

        //generate response
        final TOTPResponse totpResponse = new TOTPResponse();
        totpResponse.setSecret(totpSecret);
        totpResponse.setSecretQrcode(totpSecretQrCode);
        return totpResponse;
    }

    @DELETE
    @Path("{username}")
    public Response delete(@Context final HttpHeaders headers, @PathParam("username") String username) {
        final KeycloakContext context = session.getContext();
        final RealmModel targetRealm = context.getRealm(); //schon hier abholen; context.getRealm ändert sich nach Aufruf von authenticateRealmAdminRequest!

        //check if the admin has the permissions to execute this
        checkAdminPermissions(headers, targetRealm);

        //check inputs
        UserModel user = findUser(username, targetRealm);

        //find credential-ID of OTP
        List<CredentialModel> creds = user.credentialManager().getStoredCredentialsByTypeStream("otp").toList();
        if (creds.isEmpty()) {
            throw new ErrorResponseException("USER-HAS-NO-OTP", "Current user does not have a TOTP. Cannot delete!", Response.Status.BAD_REQUEST);
        } else if (creds.size() > 1) {
            throw new ErrorResponseException("MORE-THAN-ONE-OTP", "Current user has more than one TOTP. Don't know which one to delete!", Response.Status.BAD_REQUEST);
        }
        String credentialId = creds.get(0).getId();

        deleteOTPCredential(session, targetRealm, user, credentialId);
        LOG.info("Removed TOTP from user " + user.getUsername());
        return Response
                .status(Response.Status.NO_CONTENT)
                .build();

    }

    //taken from CredentialHelper before 26.2 (was removed then)
    public void deleteOTPCredential(KeycloakSession session, RealmModel realm, UserModel user, String credentialId) {
        CredentialProvider otpCredentialProvider = session.getProvider(CredentialProvider.class, "keycloak-otp");
        boolean removed = otpCredentialProvider.deleteCredential(realm, user, credentialId);

        // This can usually happened when credential is stored in the userStorage. Propagate to "disable" credential in the userStorage
        if (!removed) {
            logger.debug("Removing OTP credential from userStorage");
            user.credentialManager().disableCredentialType(OTPCredentialModel.TYPE);
        }
    }

    @Override
    public void close() {
        // Needed to be overridden
    }

    private void checkAdminPermissions(final HttpHeaders headers, RealmModel targetRealm) {
        final KeycloakContext context = session.getContext();

        //check if current user is authenticated and authorized (checks bearer token) as (local or global) admin
        final AdminAuth auth = authenticateRealmAdminRequest(headers);
        if (!AdminPermissions.realms(session, auth).isAdmin(targetRealm)) {
            LOG.error("User with given Access Token is not admin for realm " + targetRealm.getName());
            throw new ErrorResponseException("NOT-ADMIN-USER", "User with given Access Token is not admin for realm " + targetRealm.getName(),
                    Response.Status.FORBIDDEN);
        }

        //check if current user has specific admin-role "manage-users" (directly or indirectly via composite roles)
        //the user can have this role through the client "realm-management" (if he is a local admin in the otp realm)
        ClientModel realmManagementClient = targetRealm.getClientByClientId(CLIENT_ID);
        //or the user can have this role through the client "otp-realm" (if he is a global admin in the master realm with restricted rights)
        ClientModel otpRealmClient = context.getRealm().getClientByClientId(targetRealm.getName() + REALM_SUFFIX);

        RoleModel roleFromRealmManagement = realmManagementClient.getRole(REQUIRED_ROLE);
        RoleModel roleFromOtpRealm = otpRealmClient.getRole(REQUIRED_ROLE);


        if (roleFromRealmManagement == null && roleFromOtpRealm == null) {
            LOG.error("Role custom-add-otp does neither exist on " + CLIENT_ID + " nor on " + targetRealm.getName() + REALM_SUFFIX);
            throw new ErrorResponseException("CUSTOM-ROLE-NOT-EXISTING", "Role custom-add-otp does neither exist on " +
                    CLIENT_ID + " nor on " + targetRealm.getName() + REALM_SUFFIX,
                    Response.Status.FORBIDDEN);
        }

        boolean userInRole = false;

        if (roleFromOtpRealm != null && auth.getUser().hasRole(roleFromOtpRealm)) {
            userInRole = true;
        }

        if (roleFromRealmManagement != null && auth.getUser().hasRole(roleFromRealmManagement)) {
            userInRole = true;
        }

        if (!userInRole) {
            LOG.error("Current user does not have role " + REQUIRED_ROLE);
            throw new ErrorResponseException("ADMINUSER-NOT-IN-ROLE", "Current user does not have role " + REQUIRED_ROLE,
                    Response.Status.FORBIDDEN);
        }
    }



    private UserModel findUser(String username, RealmModel targetRealm) {
        if (username == null) {
            LOG.error("Username missing!");
            throw new ErrorResponseException("USERNAME-MISSING", "Current request does not contain a username", Response.Status.BAD_REQUEST);
        }

        //search user in DB in target realm
        LOG.info("Searching user in Realm " + targetRealm.getName() + " with username " + username);
        final UserModel user = KeycloakModelUtils.findUserByNameOrEmail(session, targetRealm, username);
        if (user == null) {
            LOG.error("User not found!");
            throw new ErrorResponseException("USER-NOT-FOUND", "User with username " + username +
                    " not found in realm " + targetRealm.getName(), Response.Status.BAD_REQUEST);
        }

        return user;
    }

}
