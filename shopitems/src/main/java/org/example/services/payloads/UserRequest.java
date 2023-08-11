package org.example.services.payloads;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class UserRequest {

    @Schema(example = "Dr.Cryton")
    public String username;

    @Schema(example = "herdsontisk@gmail.com")
    public String email;

    @Schema(example = "Hope@9199")
    public  String password;

    @Schema(example = "ADMIN")
    public String role;

}
