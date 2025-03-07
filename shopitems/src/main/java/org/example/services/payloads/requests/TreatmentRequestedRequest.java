package org.example.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class TreatmentRequestedRequest {

    @Schema(example = "1")
    public int quantity;

    @Schema(example = "1")
    public Long visitID;

    @Schema(example = "1")
    public Long itemId;

}
