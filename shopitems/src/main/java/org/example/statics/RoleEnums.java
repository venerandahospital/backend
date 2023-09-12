package org.example.statics;

public enum RoleEnums {

    ADMIN("Admin".toUpperCase()),
    CUSTOMER("Customer".toUpperCase()),
    AGENT("Agent".toUpperCase());

    public final String label;

    RoleEnums(String label){
        this.label = label;
    }
}
