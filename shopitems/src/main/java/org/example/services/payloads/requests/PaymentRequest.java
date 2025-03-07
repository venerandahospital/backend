package org.example.services.payloads.requests;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class PaymentRequest {

    @Schema(example = "1")
    public Long visitId;

    @Schema(example = "202")
    public Long invoiceId;

    @Schema(example = "15000.00")
    public BigDecimal amountToPay;

    @Schema(example = "cash at hand")
    public String paymentForm; // e.g., "cash at hand or cash at bank"

    @Schema(example = "Payment Done")
    public String notes;

    @Schema(example = "Approved")
    public String status;
}
