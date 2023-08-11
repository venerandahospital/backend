package org.example.configuration.initializer;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.domains.User;
import org.example.domains.repositories.UserRepository;

import static org.example.statics.RoleEnums.ADMIN;

public class Initializer {

    @Inject
    UserRepository userRepository;

    @Transactional
    public void initUser(@Observes StartupEvent ev){

        if (Boolean.FALSE.equals(userRepository.usernameExists("admin"))){
            User adminUser = new User();
            adminUser.username = "admin";
            adminUser.role = ADMIN.label;
            adminUser.email = "admin@support.com";
            adminUser.password = BcryptUtil.bcryptHash("123");

            adminUser.persist();
        }
    }
}
