package org.example.hospitalCafeteria.finance.invoice.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class CanteenInvoiceRequest {

    @Schema(example = "301")
    public Long visitId;
}
