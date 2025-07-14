package org.example.treatment.services.payloads.requests;

import jakarta.persistence.Column;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class TreatmentRequestedRequest {

    @Schema(example = "1")
    public BigDecimal quantity;

    @Schema(example = "1")
    public Long itemId;

    @Schema(example = "1")
    public Long visitId;

    @Schema(example = "1")
    public BigDecimal durationValue;

    @Schema(example = "duration in days, weeks, or months")
    public String durationUnit;

    @Schema(example = "2")
    public BigDecimal amountPerFrequencyValue;

    @Schema(example = "mg")
    public String amountPerFrequencyUnit;

    @Schema(example = "3")
    public BigDecimal frequencyValue;

    @Schema(example = "per day, week, or month")
    public String frequencyUnit;


    @Schema(example = "after meals")
    public String instructions;

    @Schema(example = "Oral")
    public String route;

}
