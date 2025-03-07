package org.example.services.payloads.requests;

import jakarta.ws.rs.QueryParam;

import java.time.LocalDate;

public class ShopItemParametersRequest {

    @QueryParam("invoiceId")
    public String category;

    @QueryParam("title")
    public String title;

    @QueryParam("datefrom")
    public LocalDate datefrom;

    @QueryParam("dateto")
    public LocalDate dateto;

}
