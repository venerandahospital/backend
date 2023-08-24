package org.example.auth.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.auth.services.payloads.UserAuthRequest;
import org.example.auth.services.payloads.UserAuthResponse;
import org.example.configuration.security.JwtUtils;
import org.example.domains.repositories.UserRepository;
import org.example.statics.UserTypes;


import java.util.Optional;

@ApplicationScoped

public class UserAuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    JwtUtils jwtUtils;

    private static final String BEARER = "Bearer ";


    public UserAuthResponse login(UserAuthRequest request) {
        return Optional.ofNullable(userRepository.login(request.email, request.password))
                .map(user -> {
                    String jwtToken = jwtUtils.generateJwtToken(user);
                    return new UserAuthResponse(user, jwtToken, UserTypes.AGENT.label); // Pass user.email and user.username
                })
                .orElseThrow(() -> new WebApplicationException("user Not Found", 404));
    }


}
