package ca.bc.gov.hlth.auth.provider.browser;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;

public class IdentityProviderStopForm extends AbstractUsernameFormAuthenticator {
    protected static ServicesLogger log = ServicesLogger.LOGGER;

    @Override
    public void action(AuthenticationFlowContext context) {
        context.attempted();
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        List<IdentityProviderModel> realmIdps = context.getRealm().getIdentityProvidersStream().toList();
        Map<String, ClientScopeModel> scopes =
                context.getAuthenticationSession().getClient().getClientScopes(true);
        String idpkeys = "";

        Map<String, Map<String, String>> idpContext = new HashMap<>();

        for (IdentityProviderModel ridp : realmIdps) {
            String oidcAlias = ridp.getAlias();
            String samlAlias = oidcAlias + "-saml";

            if (ridp.isEnabled() && (scopes.containsKey(oidcAlias) || scopes.containsKey(samlAlias))) {
                Map<String, String> data = new HashMap<>();
                data.put("enabled", "true");

                idpContext.put(oidcAlias, data);
            }
        }

        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(idpContext);
            log.tracef("idp context: %s", json);
            formData.add(AuthenticationManager.FORM_USERNAME, json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            formData.add(AuthenticationManager.FORM_USERNAME, "{}");
        }

        Response challengeResponse = challenge(context, formData);
        context.challenge(challengeResponse);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    protected Response challenge(
            AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();

        if (formData.size() > 0) forms.setFormData(formData);

        return forms.createLoginUsernamePassword();
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // never called
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // never called
    }

    @Override
    public void close() {}
}
