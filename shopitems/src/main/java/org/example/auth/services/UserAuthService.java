package org.example.auth.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.auth.services.payloads.ResetPasswordRequest;
import org.example.auth.services.payloads.UserAuthRequest;
import org.example.auth.services.payloads.UserAuthResponse;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.configuration.security.JwtUtils;
import org.example.domains.repositories.UserRepository;
import org.example.messages.EmailService;
import org.example.statics.UserTypes;
import org.jboss.logging.Logger;


import java.util.Optional;

@ApplicationScoped

public class UserAuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    EmailService emailService;

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

    public Response sendResetPassword(ResetPasswordRequest request){
        return userRepository.findByEmailOptional(request.email)
                .map(user -> {
                            emailService.sendPasswordResetLink(user)
                                    .subscribe()
                                    .with(voidItem -> Logger.getLogger(UserAuthService.class).info("Email sent"));
                            return Response.ok(new ResponseMessage(ActionMessages.SEND.label)).build();
                        }
                )
                .orElseThrow(() -> new WebApplicationException("Not found",404));
    }


}
