package org.example.services;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.User;
import org.example.domains.repositories.UserRepository;
import org.example.services.payloads.*;
import org.example.statics.RoleEnums;
import org.example.statics.UserTypes;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    public static final String NOT_FOUND = "Not found!";

    public User createNewUser(UserRequest request){
        User user = new User();
        user.username = request.username;
        user.email = request.email;
        user.role = RoleEnums.CUSTOMER.label;
        user.password = BcryptUtil.bcryptHash(request.password);

        userRepository.persist(user);

        return user;

    }

    public User updateUserById(UpdateRequest request, Long id){
        User user = userRepository.findById(id);
        user.username = request.username;
        user.email = request.email;

        userRepository.persist(user);

        return user;

    }

    public List<User> getAllUsers(){
        return userRepository.listAll();
    }


    public Response deleteUserById(Long id) {
        User user = userRepository.findById(id);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        userRepository.delete(user);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }












































    /*public User createNewUser(UserRequest request){
        User user = new User();
        user.username = request.username;
        user.email = request.email;
        user.password = BcryptUtil.bcryptHash(request.password);
        user.role = RoleEnums.valueOf(request.role).label;
        userRepository.persist(user);

        return user;
    }

    public User getById(Long id){
        return userRepository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public User update(Long id, UpdateRequest request){
        return userRepository.findByIdOptional(id)
                .map(user -> {
                    user.email = request.email;
                    user.username = request.username;

                    user.persist();

                    return user;
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public User updatePassword(Long id, UpdatePasswordRequest request){
        return userRepository.findByIdOptional(id)
                .map(user -> Optional.ofNullable(userRepository.login(user.username, request.oldPassword))
                        .map(confirmedUser -> validateUser(user,request))
                        .orElseThrow(() -> new WebApplicationException("Invalid credentials",409))
                )
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public User validateUser(User user, UpdatePasswordRequest request){
        if (Boolean.FALSE.equals(request.oldPassword.equals(request.newPassword))){
            user.password = BcryptUtil.bcryptHash(request.newPassword);
            user.persist();

            return user;
        }
        throw new WebApplicationException("Your new password must be unique",409);
    }

    public User updateRole(Long id , RoleRequest request){
        return userRepository.findByIdOptional(id)
                .map(user -> {
                    user.role = RoleEnums.valueOf(request.role).name();
                    user.persist();

                    return user;
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public RoleResponse roles(){
        return new RoleResponse(Arrays.stream(RoleEnums.values())
                .map(Enum::name)
                .collect(Collectors.toSet()));
    }
    public List<User> getAllAgents() {
        return userRepository.listAll();
    }*/
}




