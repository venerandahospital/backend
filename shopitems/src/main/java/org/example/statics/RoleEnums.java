package org.example.statics;

public enum RoleEnums {

    ADMIN("Admin".toUpperCase()),
    CUSTOMER("Customer".toUpperCase()),

    USER("User".toUpperCase());

    public final String label;

    RoleEnums(String label){
        this.label = label;
    }
}
