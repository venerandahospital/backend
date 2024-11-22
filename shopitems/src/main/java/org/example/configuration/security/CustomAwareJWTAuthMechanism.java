package org.example.configuration.security;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Alternative
@Priority(1)
@ApplicationScoped
public class CustomAwareJWTAuthMechanism implements HttpAuthenticationMechanism {

    protected static final String AUTHORIZATION_HEADER = "Authorization";

    protected static final String BEARER = "Bearer";

    @Inject
    JwtUtils jwtUtils;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        HttpServerRequest request = context.request();
        String jwt = parseJwt(request);

        if (request.path().startsWith("/auth/user-login") ||
                request.path().contains("/shop-item/get-all-Items") ||
                request.path().contains("/user-management/signup") ||
                request.path().contains("/course/get-all-courses") ||
                request.path().contains("/course/create-new-course") ||
                request.path().contains("/course/course/update-course") ||
                request.path().contains("/course/shop-cart/add-to-cart") ||
                request.path().contains("/course/shop-item/add-new-Items") ||
                request.path().contains("/course/shop-cart/get-cart-items/") ||
                request.path().contains("/course/Patient-management/get-all-patients") ||
                request.path().contains("/course/Patient-management/get-patient") ||
                request.path().contains("/course/Patient-management/update-patient/") ||
                request.path().contains("/course/Patient-management/create-new-patient-visit") ||
                request.path().contains("/course/Patient-management/create-new-InitialTriageVitals") ||
                request.path().contains("/course/Patient-management/get-all-InitialTriageVitals") ||
                request.path().contains("/course/Patient-management/create-new-Consultation") ||
                request.path().contains("/course/Patient-management/create-new-labTest") ||
                request.path().contains("/course/items-used/add-to-item-used") ||
                request.path().contains("/course/Patient-management/get-All-used-items") ||
                request.path().contains("/course/Patient-management/get-used-items/") ||
                request.path().contains("/course/Patient-management/add-to-item-used") ||
                request.path().contains("/course/Patient-management/get-InitialTriageVitals-visit") ||
                request.path().contains("/course/Patient-management/get-all-labTest") ||
                request.path().contains("/course/Patient-management/create-new-InPatientTreatmentGiven") ||
                request.path().contains("/course/Patient-management/create-new-procedure") ||
                request.path().contains("/course/Patient-management/get-all-procedures") ||
                request.path().contains("/course/Patient-management/create-new-treatmentRequest") ||
                request.path().contains("/course/Patient-management/create-new-Recommendation") ||
                request.path().contains("/course/Patient-management/create-new-VitalsMonitoring") ||





                request.path().contains("/course/Patient-management/create-new-patient") ||
                request.path().contains("/course/Patient-management/get-patient-with-max-number") ||
                request.path().contains("/course/Patient-management/delete-patient-by-id/") ||

                request.path().contains("/auth/reset-link") ||
                request.path().contains("/course/user-management/get-user") ||
                request.path().contains("/course/user-management/update-user") ||
                request.path().contains("/course/user-management/get-all-users") ||
                request.path().contains("/course/shop-item/get-Items-advanced-search") ||
                request.path().contains("/course/shop-item/add-new-Items") ||
                request.path().contains("/course/shop-item/search") ||
                request.path().contains("/shop-item/generate-pdf") ||




                request.path().contains("/user-login")) {

            return Uni.createFrom()
                    .optional(Optional.empty());

        } else {

            if (jwt != null) {
                if (jwtUtils.validateJwtToken(jwt)) {

                    String username = jwtUtils.getUserNameFromJwtToken(jwt);

                    Set<String> userRole = jwtUtils.getRoles();
                    QuarkusSecurityIdentity identity = QuarkusSecurityIdentity.builder()
                            .setPrincipal(new QuarkusPrincipal(username))
                            .addRoles(userRole)
                            .build();

                    return Uni.createFrom()
                            .item(identity);

                }

                return Uni.createFrom()
                        .failure(new AuthenticationFailedException());

            }

            return Uni.createFrom()
                    .failure(new AuthenticationFailedException());

        }
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        ChallengeData result = new ChallengeData(HttpResponseStatus.UNAUTHORIZED.code(),
                HttpHeaderNames.WWW_AUTHENTICATE, "Bearer {token}");
        return Uni.createFrom()
                .item(result);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Collections.singleton(TokenAuthenticationRequest.class);
    }

    private String parseJwt(HttpServerRequest request) {

        String headerAuth = request.getHeader(AUTHORIZATION_HEADER);

        if (headerAuth != null && headerAuth.startsWith(BEARER)) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
