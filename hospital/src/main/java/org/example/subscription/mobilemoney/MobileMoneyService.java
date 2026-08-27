package org.example.subscription.mobilemoney;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.subscription.mobilemoney.payloads.MobileMoneyPayRequest;
import org.example.subscription.mobilemoney.payloads.MobileMoneyPaymentStatusDTO;

@ApplicationScoped
public class MobileMoneyService {

    private final Map<String, MobileMoneyPaymentRecord> payments = new ConcurrentHashMap<>();

    @Inject
    MtnMomoClient mtnMomoClient;

    @Inject
    AirtelMoneyClient airtelMoneyClient;

    @ConfigProperty(name = "mobile-money.mock-when-unconfigured", defaultValue = "true")
    boolean mockWhenUnconfigured;

    @ConfigProperty(name = "mobile-money.default-currency", defaultValue = "UGX")
    String defaultCurrency;

    @ConfigProperty(name = "mobile-money.mock-auto-success-seconds", defaultValue = "12")
    int mockAutoSuccessSeconds;

    public MobileMoneyPaymentStatusDTO initiate(MobileMoneyPayRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request is required");
        }
        String provider = normalizeProvider(request.provider);
        String phone = normalizeMsisdn(request.phoneNumber);
        double amount = request.amount == null ? 0 : request.amount;
        if (amount <= 0) {
            throw new IllegalArgumentException("Enter a valid amount greater than zero");
        }
        if (phone.length() < 9) {
            throw new IllegalArgumentException("Enter a valid mobile money phone number");
        }
        String currency = (request.currency != null && !request.currency.isBlank())
                ? request.currency.trim().toUpperCase(Locale.ROOT)
                : defaultCurrency;

        String referenceId = UUID.randomUUID().toString();
        boolean useMock = shouldMock(provider);

        if (useMock) {
            MobileMoneyPaymentRecord record = new MobileMoneyPaymentRecord(
                    referenceId,
                    provider,
                    phone,
                    amount,
                    currency,
                    true,
                    "PENDING",
                    "Demo mode: pretend an approval request was sent to " + phone
                            + ". Enter your PIN on the phone (simulated). Status will complete shortly.");
            payments.put(referenceId, record);
            return toDto(record);
        }

        try {
            if ("mtn".equals(provider)) {
                mtnMomoClient.requestToPay(referenceId, phone, amount, currency, request.note);
                MobileMoneyPaymentRecord record = new MobileMoneyPaymentRecord(
                        referenceId,
                        provider,
                        phone,
                        amount,
                        currency,
                        false,
                        "PENDING",
                        "Approval request sent to " + phone + ". Enter your MTN MoMo PIN on the phone to complete payment.");
                payments.put(referenceId, record);
                return toDto(record);
            }

            String externalId = airtelMoneyClient.initiatePayment(referenceId, phone, amount, currency, request.note);
            MobileMoneyPaymentRecord record = new MobileMoneyPaymentRecord(
                    referenceId,
                    provider,
                    phone,
                    amount,
                    currency,
                    false,
                    "PENDING",
                    "Approval request sent to " + phone + ". Enter your Airtel Money PIN on the phone to complete payment.");
            record.externalTransactionId = externalId;
            payments.put(referenceId, record);
            return toDto(record);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage() != null ? e.getMessage() : "Could not start mobile money payment", e);
        }
    }

    public MobileMoneyPaymentStatusDTO getStatus(String referenceId) {
        MobileMoneyPaymentRecord record = payments.get(referenceId);
        if (record == null) {
            MobileMoneyPaymentStatusDTO missing = new MobileMoneyPaymentStatusDTO();
            missing.referenceId = referenceId;
            missing.status = "UNKNOWN";
            missing.message = "Payment reference not found";
            return missing;
        }

        if ("SUCCESSFUL".equals(record.status) || "FAILED".equals(record.status) || "TIMEOUT".equals(record.status)) {
            return toDto(record);
        }

        if (record.mock) {
            long elapsed = Duration.between(record.createdAt, Instant.now()).getSeconds();
            if (elapsed >= mockAutoSuccessSeconds) {
                record.status = "SUCCESSFUL";
                record.message = "Demo payment completed successfully.";
            }
            return toDto(record);
        }

        try {
            String remoteStatus;
            if ("mtn".equals(record.provider)) {
                remoteStatus = normalizeRemoteStatus(mtnMomoClient.getRequestToPayStatus(referenceId));
            } else {
                String id = record.externalTransactionId != null ? record.externalTransactionId : referenceId;
                remoteStatus = normalizeRemoteStatus(airtelMoneyClient.enquireStatus(id));
            }
            record.status = remoteStatus;
            if ("SUCCESSFUL".equals(remoteStatus)) {
                record.message = "Payment completed. Thank you.";
            } else if ("FAILED".equals(remoteStatus)) {
                record.message = "Payment failed or was cancelled on the phone.";
            } else if ("TIMEOUT".equals(remoteStatus)) {
                record.message = "Payment timed out waiting for PIN approval.";
            } else {
                record.message = "Waiting for PIN approval on " + record.phoneNumber + "…";
            }
        } catch (Exception e) {
            record.message = "Could not refresh status: " + e.getMessage();
        }
        return toDto(record);
    }

    public Map<String, Object> providersInfo() {
        return Map.of(
                "mtnConfigured", mtnMomoClient.isConfigured(),
                "airtelConfigured", airtelMoneyClient.isConfigured(),
                "mockWhenUnconfigured", mockWhenUnconfigured,
                "defaultCurrency", defaultCurrency,
                "hint", "Register on momodeveloper.mtn.com (Collections) and/or Airtel Open API, then set keys in application.properties.");
    }

    private boolean shouldMock(String provider) {
        if ("mtn".equals(provider)) {
            return !mtnMomoClient.isConfigured() && mockWhenUnconfigured;
        }
        return !airtelMoneyClient.isConfigured() && mockWhenUnconfigured;
    }

    private static String normalizeProvider(String provider) {
        String p = provider == null ? "" : provider.trim().toLowerCase(Locale.ROOT);
        if ("mtn".equals(p) || "momo".equals(p) || "mtn-momo".equals(p)) {
            return "mtn";
        }
        if ("airtel".equals(p) || "airtel-money".equals(p)) {
            return "airtel";
        }
        throw new IllegalArgumentException("Choose MTN or Airtel Money as the payment provider");
    }

    /** Normalize to international MSISDN without + (e.g. 256770123456). */
    static String normalizeMsisdn(String raw) {
        if (raw == null) {
            return "";
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("0") && digits.length() == 10) {
            return "256" + digits.substring(1);
        }
        if (digits.startsWith("256")) {
            return digits;
        }
        if (digits.length() == 9) {
            return "256" + digits;
        }
        return digits;
    }

    private static String normalizeRemoteStatus(String status) {
        if (status == null) {
            return "UNKNOWN";
        }
        String s = status.trim().toUpperCase(Locale.ROOT);
        if (s.contains("SUCCESS")) {
            return "SUCCESSFUL";
        }
        if (s.contains("FAIL") || s.contains("REJECT") || s.contains("ERROR")) {
            return "FAILED";
        }
        if (s.contains("TIMEOUT") || s.contains("EXPIRED")) {
            return "TIMEOUT";
        }
        if (s.contains("PEND")) {
            return "PENDING";
        }
        return s;
    }

    private static MobileMoneyPaymentStatusDTO toDto(MobileMoneyPaymentRecord record) {
        MobileMoneyPaymentStatusDTO dto = new MobileMoneyPaymentStatusDTO();
        dto.referenceId = record.referenceId;
        dto.provider = record.provider;
        dto.phoneNumber = record.phoneNumber;
        dto.amount = record.amount;
        dto.currency = record.currency;
        dto.status = record.status;
        dto.message = record.message;
        dto.mock = record.mock;
        dto.externalTransactionId = record.externalTransactionId;
        return dto;
    }
}
