package org.example.subscription.mobilemoney;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * MTN MoMo Collections — RequestToPay (USSD/PIN prompt on customer's phone).
 * Docs: https://momodeveloper.mtn.com / https://www.mtn.co.ug/helppersonal/mtn-open-api/
 */
@ApplicationScoped
public class MtnMomoClient {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @ConfigProperty(name = "mobile-money.mtn.base-url", defaultValue = "https://sandbox.momodeveloper.mtn.com")
    String baseUrl;

    @ConfigProperty(name = "mobile-money.mtn.subscription-key")
    Optional<String> subscriptionKey;

    @ConfigProperty(name = "mobile-money.mtn.api-user")
    Optional<String> apiUser;

    @ConfigProperty(name = "mobile-money.mtn.api-key")
    Optional<String> apiKey;

    @ConfigProperty(name = "mobile-money.mtn.target-environment", defaultValue = "sandbox")
    String targetEnvironment;

    public boolean isConfigured() {
        return notBlank(subscriptionKey) && notBlank(apiUser) && notBlank(apiKey);
    }

    public void requestToPay(String referenceId, String msisdn, double amount, String currency, String note)
            throws Exception {
        String token = fetchAccessToken();
        JsonObject body = Json.createObjectBuilder()
                .add("amount", formatAmount(amount))
                .add("currency", currency)
                .add("externalId", referenceId.replace("-", "").substring(0, Math.min(12, referenceId.replace("-", "").length())))
                .add("payer", Json.createObjectBuilder()
                        .add("partyIdType", "MSISDN")
                        .add("partyId", msisdn)
                        .build())
                .add("payerMessage", truncate(note != null && !note.isBlank() ? note : "Subscription payment", 160))
                .add("payeeNote", "Facility subscription")
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(baseUrl) + "/collection/v1_0/requesttopay"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("X-Reference-Id", referenceId)
                .header("X-Target-Environment", targetEnvironment)
                .header("Ocp-Apim-Subscription-Key", value(subscriptionKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();
        if (code != 202 && code != 200) {
            throw new IllegalStateException("MTN RequestToPay failed (" + code + "): " + response.body());
        }
    }

    public String getRequestToPayStatus(String referenceId) throws Exception {
        String token = fetchAccessToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(baseUrl) + "/collection/v1_0/requesttopay/" + referenceId))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("X-Target-Environment", targetEnvironment)
                .header("Ocp-Apim-Subscription-Key", value(subscriptionKey))
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("MTN status check failed (" + response.statusCode() + "): " + response.body());
        }
        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            JsonObject json = reader.readObject();
            return json.getString("status", "UNKNOWN");
        }
    }

    private String fetchAccessToken() throws Exception {
        String basic = Base64.getEncoder()
                .encodeToString((value(apiUser) + ":" + value(apiKey)).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(baseUrl) + "/collection/token/"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Basic " + basic)
                .header("Ocp-Apim-Subscription-Key", value(subscriptionKey))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("MTN token failed (" + response.statusCode() + "): " + response.body());
        }
        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            return reader.readObject().getString("access_token");
        }
    }

    private static String formatAmount(double amount) {
        if (Math.abs(amount - Math.rint(amount)) < 0.0001) {
            return String.valueOf((long) Math.rint(amount));
        }
        return String.format(java.util.Locale.US, "%.2f", amount);
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static String trimSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String value(Optional<String> opt) {
        return opt.orElse("").trim();
    }

    private static boolean notBlank(Optional<String> opt) {
        return opt.isPresent() && !opt.get().isBlank();
    }
}
