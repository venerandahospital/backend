package org.example.pharmacy.otc.services.payloads.requests;

import java.math.BigDecimal;
import java.util.List;

public class OtcSaleCompleteRequest {
    public BigDecimal amountReceived;
    /** Optional discount subtracted from line total before payment check. */
    public BigDecimal discount;
    public String paymentForm;
    public String receivedBy;
    public String notes;
    /** Visit id when billing doctor prescriptions at the counter. */
    public Long visitId;
    /** Patient/client to tag the OTC sale / unpaid balance. */
    public Long patientId;
    public String patientName;
    public List<OtcSaleLineRequest> lines;
}
