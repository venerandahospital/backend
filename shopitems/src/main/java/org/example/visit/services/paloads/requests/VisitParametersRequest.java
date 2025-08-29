package org.example.visit.services.paloads.requests;

import jakarta.ws.rs.QueryParam;

import java.time.LocalDate;

public class VisitParametersRequest {



    @QueryParam("visitGroup")
    public String visitGroup;

    @QueryParam("datefrom")
    public LocalDate datefrom;

    @QueryParam("dateto")
    public LocalDate dateto;
}
