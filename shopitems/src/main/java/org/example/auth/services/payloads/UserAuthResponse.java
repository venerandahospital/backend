package org.example.auth.services.payloads;

import org.example.domains.User;
import org.hibernate.usertype.UserType;

public class UserAuthResponse {
    public User user;

    public String jwt;

    public String userType;

    public UserAuthResponse() {
    }

    public UserAuthResponse(User user, String jwt, String userType) {
        this.user = user;
        this.jwt = jwt;
        this.userType = userType;
    }
}

