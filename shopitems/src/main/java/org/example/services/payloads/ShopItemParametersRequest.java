package org.example.services.payloads;

import jakarta.ws.rs.QueryParam;

public class ShopItemParametersRequest {


    @QueryParam("category")
    public String category;

    @QueryParam("title")
    public String title;
}
