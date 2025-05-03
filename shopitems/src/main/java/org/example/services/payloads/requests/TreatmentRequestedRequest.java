package org.example.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class TreatmentRequestedRequest {

    @Schema(example = "1")
    public BigDecimal quantity;

    @Schema(example = "1")
    public Long itemId;

    @Schema(example = "1")
    public BigDecimal unitSellingPrice;

}
