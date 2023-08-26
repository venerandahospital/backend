package org.example;

import io.smallrye.common.constraint.NotNull;

public class ResetPasswordRequest {

    @NotNull
    public String email;
}