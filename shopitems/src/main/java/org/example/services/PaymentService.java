package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.domains.*;
import org.example.domains.repositories.InvoiceRepository;
import org.example.domains.repositories.PatientVisitRepository;
import org.example.domains.repositories.PaymentsRepository;
import org.example.services.payloads.requests.PaymentRequest;
import org.example.services.payloads.responses.dtos.PatientVisitDTO;
import org.example.services.payloads.responses.dtos.PaymentDTO;
import org.example.services.payloads.responses.dtos.ProcedureRequestedDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentsRepository paymentRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InvoiceService invoiceService;

    @Inject
    PatientVisitRepository patientVisitRepository;

    @Transactional
    public PaymentDTO createNewPayment(PaymentRequest request) {
        // Validate the request
        if (request == null) {
            throw new IllegalArgumentException("Payment request cannot be null.");
        }
        if (request.visitId == null) {
            throw new IllegalArgumentException("Visit ID cannot be null.");
        }
        if (request.amountToPay == null || request.amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount to pay must be greater than zero.");
        }
        if (request.paymentForm == null || request.paymentForm.isEmpty()) {
            throw new IllegalArgumentException("Payment form cannot be null or empty.");
        }

        // Validate that the visit exists
        PatientVisit visit = patientVisitRepository.findById(request.visitId);
        if (visit == null) {
            throw new IllegalArgumentException("Visit not found for ID: " + request.visitId);
        }

        // Ensure visit.invoice is not null and contains at least one invoice
        if (visit.invoice == null || visit.invoice.isEmpty()) {
            throw new IllegalArgumentException("No invoice found for visit ID: " + request.visitId);
        }

        // Get the first invoice from the list (or handle multiple invoices as needed)
        Invoice invoice = visit.invoice.get(0); // Assuming visit.invoice is a List<Invoice>
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found for visit ID: " + request.visitId);
        }

        // Create a new Payment entity with PENDING status
        Payments payment = new Payments();
        payment.visit = visit;
        payment.invoice = invoice;
        payment.amountToPay = request.amountToPay;
        payment.paymentForm = request.paymentForm;
        payment.dateOfPayment = java.time.LocalDate.now();
        payment.timeOfPayment = java.time.LocalTime.now();
        payment.status = request.status;
        payment.notes = request.notes;

        // Persist the payment
        paymentRepository.persist(payment);

        // Update the invoice amount paid
        invoiceService.updateInvoiceAmountPaid(invoice);

        // Return the PaymentDTO
        return new PaymentDTO(payment);
    }

    @Transactional
    public BigDecimal getTotalPaymentOfInvoice(Long invoiceId) {
        // Validate the invoice ID
        if (invoiceId == null) {
            throw new IllegalArgumentException("Invoice ID cannot be null.");
        }

        // Fetch payments associated with the given invoice ID
        List<Payments> paymentsMade = Payments.find(
                "invoice.id = ?1 ORDER BY id DESC",
                invoiceId
        ).list();

        // Calculate the total amount paid or return BigDecimal.ZERO if no payments exist
        return paymentsMade.stream()
                .map(payment -> payment.amountToPay != null ? payment.amountToPay : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public List<PaymentDTO> getPaymentsByVisitId(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is "LabTest" and visit ID matches, ordered descending
        List<Payments> visitPayments = Payments.find(
                "visit.id = ?1", // Replace 'id' with your desired field for sorting
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return visitPayments.stream()
                .map(PaymentDTO::new)
                .toList();
    }















}