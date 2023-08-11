package org.example.services.payloads;

import io.smallrye.common.constraint.NotNull;

public class UpdateRequest {

    @NotNull
    public String email;

    @NotNull
    public String username;
}
