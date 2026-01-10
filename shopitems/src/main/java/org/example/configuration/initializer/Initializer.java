package org.example.configuration.initializer;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;
import org.example.statics.RoleEnums;

public class Initializer {

    @Inject
    UserRepository userRepository;

    @Transactional
    public void initUser(@Observes StartupEvent ev){

        // Check if admin user already exists by username OR email to avoid duplicate entry errors
        boolean adminExists = userRepository.usernameExists("admin") || 
                             userRepository.findByEmailOptional("admin@vmd.com").isPresent();
        
        if (!adminExists){
            User adminUser = new User();
            adminUser.username = "admin";
            adminUser.profilePic = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2Fpic.PNG?alt=media&token=56906e62-65aa-4f2b-bc1a-60d5df63839c";
            adminUser.role = RoleEnums.admin.label;
            adminUser.email = "admin@vmd.com";
            adminUser.password = BcryptUtil.bcryptHash("Hope@91991245");

            adminUser.persist();
        }
    }
}

