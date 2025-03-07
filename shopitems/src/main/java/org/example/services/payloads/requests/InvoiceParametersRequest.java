package org.example.services.payloads.requests;

import jakarta.ws.rs.QueryParam;

import java.time.LocalDate;

public class InvoiceParametersRequest {
    @QueryParam("InvoiceId")
    public Long invoiceId;

}
