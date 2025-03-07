package org.example.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class InvoiceRequest {

    @Schema(example = "301")
    public Long visitId;
}
