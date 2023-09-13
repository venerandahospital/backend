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
                request.path().contains("/auth/reset-link") ||
                request.path().contains("/shop/user-management/get-user") ||
                request.path().contains("/shop/user-management/update-user") ||
                request.path().contains("/shop/user-management/get-all-users") ||
                request.path().contains("/shop-item/get-Items-advanced-search") ||
                request.path().contains("/shop/shop-item/add-new-Items") ||
                request.path().contains("/shop-item/search") ||







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
