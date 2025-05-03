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
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jakarta.ws.rs.core.MultivaluedMap;

@Alternative
@Priority(1)
@ApplicationScoped
@Provider
public class CustomAwareJWTAuthMechanism implements HttpAuthenticationMechanism, ContainerResponseFilter {

    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String BEARER = "Bearer";

    @Inject
    JwtUtils jwtUtils;

    private static final List<String> allowedOrigins = List.of(
            "http://162.212.157.6",
            "http://localhost:4200",
            "http://veneranda-hospital.s3-website.eu-north-1.amazonaws.com"

    );

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        HttpServerRequest request = context.request();
        String jwt = parseJwt(request);

        if ("OPTIONS".equals(request.method().name())) {
            return Uni.createFrom().optional(Optional.empty());
        }

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
                request.path().contains("/course/Patient-management/get-Initial-TriageVitals-visit-by-id") ||
                request.path().contains("/course/Patient-management/get-patient-Visit-List-by-id") ||
                request.path().contains("/course/Patient-management/get-procedure-Requested-with-type-LabTest-by-visit-id") ||
                request.path().contains("/course/Patient-management/create-new-payment") ||
                request.path().contains("/course/Patient-management/get-ultrasound-scan-by-visit-id") ||
                request.path().contains("/course/Patient-management/get-total-cost-of-all-procedures-by-visit-id/") ||
                request.path().contains("/course/Patient-management/get-total-cost-of-all-procedures-v2-by-visit-id") ||
                request.path().contains("/course/Patient-management/get-other-procedures-by-visit-id") ||
                request.path().contains("/course/Patient-management/create-new-Invoice") ||
                request.path().contains("/course/Patient-management/get-total-cost-of-every-service-by-visit-id") ||
                request.path().contains("/course/Patient-management/get-all-invoices") ||
                request.path().contains("/course/shop-item/") ||
                request.path().contains("/course/shop-item/delete-item") ||
                request.path().contains("/course/shop-item/delete-stock-received/") ||
                request.path().contains("/course/shop-item/add-new-bulk-items") ||
                request.path().contains(" /course/shop-item/update-item") ||
                request.path().contains("/course/shop-item/update-bulk-items-after-service-order") ||
                request.path().contains("/course/hospital-management/delete") ||
                request.path().contains("/course/Patient-management/create-new-store") ||
                request.path().contains("/course/Patient-management/get-all-stores") ||

                request.path().contains("/course/Patient-management/get-all-procedures-requested-by-visit-id") ||

                request.path().contains("/course/Patient-management/delete-item-used-id") ||





                request.path().contains("/course/Patient-management/get-payment-List-by-visit-id/") ||

                request.path().contains("/course/Patient-management/get-invoice-by-visit-id/") ||


                request.path().contains("/course/Patient-management/get-all-patient-groups") ||
                request.path().contains("/course/Patient-management/update-patient-group/") ||
                request.path().contains("/course/Patient-management/create-new-patient-group") ||
                request.path().contains("/course/Patient-management/update-vital/") ||
                request.path().contains("/course/Patient-management/delete-vital-by-id/") ||
                request.path().contains("/course/Patient-management/create-multiple-patients") ||
                request.path().contains("/course/Patient-management/delete-service") ||
                request.path().contains("/course/Patient-management/update-stock") ||
                request.path().contains("/course/Patient-management/get-latest-patient-visit-by-patient-id") ||


                request.path().contains("/course/Patient-management/update-service") ||
                request.path().contains("/course/Patient-management/create-new-patient") ||
                request.path().contains("/course/Patient-management/get-patient-group/") ||
                request.path().contains("/course/Patient-management/get-patient-group-by-id/") ||
                request.path().contains("/course/Patient-management/update-invoice") ||
                request.path().contains("/course/Patient-management/invoice/generate-pdf") ||
                request.path().contains("/course/Patient-management/get-treatment-requested-by-visit-id") ||
                request.path().contains("/course/shop-item/receive-new-stock") ||
                request.path().contains("/get-all-labTest-procedures") ||
                request.path().contains("/delete-requested-procedure-by-id/{id}") ||
                request.path().contains("create-new-procedure-requested/{id}") ||
                request.path().contains("/course/Patient-management/get-total-cost-of-every-service-by-patient-id/") ||
                request.path().contains("/course/Patient-management/delete-payment-id/") ||
                request.path().contains("/course/Patient-management/delete-invoice-id/") ||
                request.path().contains("/course/Patient-management/get-procedure-requested-by-id/") ||
                request.path().contains("/course/Patient-management/create-bulk-procedures") ||
                request.path().contains("/course/Patient-management/items-used/") ||
                request.path().contains("/course/Patient-management/add-itemUsed") ||
                request.path().contains("get-all-scan-procedures") ||
                request.path().contains("get-Other-Procedures") ||
                request.path().contains("/course/Patient-management/get-total-cost-of-all-lab-tests") ||
                request.path().contains("/course/Patient-management/create-new-patient-with-group-id") ||
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
            return Uni.createFrom().optional(Optional.empty());
        } else {
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                Set<String> userRole = jwtUtils.getRoles();
                QuarkusSecurityIdentity identity = QuarkusSecurityIdentity.builder()
                        .setPrincipal(new QuarkusPrincipal(username))
                        .addRoles(userRole)
                        .build();
                return Uni.createFrom().item(identity);
            }
            return Uni.createFrom().failure(new AuthenticationFailedException());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        addCORSHeaders(requestContext, responseContext);
    }

    private void addCORSHeaders(ContainerRequestContext requestContext,
                                ContainerResponseContext responseContext) {
        String origin = requestContext.getHeaderString("Origin");

        if (origin != null && allowedOrigins.contains(origin)) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
        }

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-requested-with");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        headers.add("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(Response.Status.NO_CONTENT.getStatusCode());
        }
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        ChallengeData result = new ChallengeData(
                HttpResponseStatus.UNAUTHORIZED.code(),
                HttpHeaderNames.WWW_AUTHENTICATE,
                "Bearer {token}"
        );
        return Uni.createFrom().item(result);
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
