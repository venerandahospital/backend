package org.example.services.payloads;

import jakarta.ws.rs.QueryParam;

import java.time.LocalDate;

public class ShopItemParametersRequest {

    @QueryParam("category")
    public String category;

    @QueryParam("title")
    public String title;

    @QueryParam("datefrom")
    public LocalDate datefrom;

    @QueryParam("dateto")
    public LocalDate dateto;

}
