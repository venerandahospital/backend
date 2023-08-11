package org.example.statics;

import jakarta.ws.rs.WebApplicationException;

import java.util.Arrays;

public enum AppTypes {

    BANKING("https://banking.arxcess.com/arxcess-banking-api"),
    ERP("http://67.205.175.206:8081/arxcess-erp-api"),
    HR("https://hr.arxcess.com/arxcess-hr-api");

    public final String label;

    AppTypes(String label){
        this.label = label;
    }

    public static AppTypes getEnum(String value){
        return Arrays.stream(AppTypes.values())
                .filter(enumValue -> enumValue.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("Application type not found"));
    }
}
