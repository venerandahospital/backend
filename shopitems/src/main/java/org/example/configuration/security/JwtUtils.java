package org.example.configuration.security;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.auth.services.payloads.UserAuthResponse;
import org.example.domains.User;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.example.statics.RoleEnums.AGENT;
import static org.example.statics.RoleEnums.CUSTOMER;

@ApplicationScoped
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final String jwtSecret = "z$C&F)J@NcRfUjXn2r5u8x/A?D*G-KaPdSgVkYp3s6v9y$B&E)"
            + "H+MbQeThWmZq4t7w!z%C*F-JaNcRfUjXn2r5u8x/A?D(G+KbPeSgVkYp3s6v9y$B&E)H@McQfTjWnZ";

    private final String resetSecret = "cvb%@wDfcRfUjXn2r5u8x/A?D*G-KaPdSgVkYp3s6v9y$B&E)"
            + "H+MbQeThWmZq4t7w!z%C*F-JaNcRfUjXn2r5u8x/A?D(G+KbPeSgVkYp3s6v9y$B&E)qWerTGGd7";


    @Inject
    JWTParser parser;

    @Inject
    JsonWebToken jwt;

    public String generateJwtToken(User user) {

        return Jwt.subject(user.email).issuedAt(Instant.now())
                .groups(Set.of(user.role))
                .expiresIn(Duration.ofSeconds(100000))
                .upn(user.username)
                .issuer("shop server")
                .signWithSecret(jwtSecret);

    }


    public Set<String> getRoles(){
        return jwt.getGroups();
    }

    public String generateResetToken(String email){
        return Jwt.subject(email)
                .expiresIn(Duration.ofMinutes(5))
                .signWithSecret(resetSecret);
    }

    public Boolean validateResetToken(String resetToken){
        try {
            jwt = parser.verify(resetToken, resetSecret);

            return true;

        } catch (JwtSignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            return false;

        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            return false;

        } catch (ParseException e) {
            logger.error("Parse Exception: {}", e.getMessage());
            return false;

        } catch (Exception e) {
            logger.error("Null Exception: {}", e.getMessage());
            return false;
        }

    }

    public JsonWebToken getJwt(){
        return jwt;
    }

    public String getUserNameFromJwtToken(String token) {

        try {
            jwt = parser.verify(token, jwtSecret);

            return jwt.getSubject();

        } catch (ParseException e) {
            logger.error("Parse Token Exception: {}", e.getMessage());
            throw new WebApplicationException("Access Denied.", 401);

        } catch (Exception e) {
            logger.error("Null Token Exception: {}", e.getMessage());
            throw new WebApplicationException("Access Denied.", 401);
        }

    }

    public boolean validateJwtToken(String authToken) {

        try {
            jwt = parser.verify(authToken, jwtSecret);

            return true;

        } catch (JwtSignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            return false;

        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            return false;

        } catch (ParseException e) {
            logger.error("Parse Exception: {}", e.getMessage());
            return false;

        } catch (Exception e) {
            logger.error("Null Exception: {}", e.getMessage());
            return false;
        }

    }
}
