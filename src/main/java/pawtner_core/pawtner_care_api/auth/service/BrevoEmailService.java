package pawtner_core.pawtner_care_api.auth.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BrevoEmailService {

    private static final URI BREVO_SEND_EMAIL_URI = URI.create("https://api.brevo.com/v3/smtp/email");

    private final HttpClient httpClient;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final boolean sandboxMode;

    public BrevoEmailService(
        @Value("${BREVO_API_KEY}") String apiKey,
        @Value("${BREVO_SENDER_EMAIL}") String senderEmail,
        @Value("${BREVO_SENDER_NAME:Pawtner Care}") String senderName,
        @Value("${BREVO_SANDBOX_MODE:false}") boolean sandboxMode
    ) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.sandboxMode = sandboxMode;
    }

    public void sendOtpEmail(String recipientEmail, String purpose, String otpCode, long expiryMinutes) {
        String payload = """
            {
              "sender": {
                "name": "%s",
                "email": "%s"
              },
              "to": [
                {
                  "email": "%s"
                }
              ],
              "subject": "Your Pawtner Care OTP Code",
              "htmlContent": "<html><body><h2>Your OTP Code</h2><p>Your %s OTP is <strong>%s</strong>.</p><p>This code expires in %d minutes.</p><p>If you did not request this, you can ignore this email.</p></body></html>",
              "textContent": "Your %s OTP is %s. This code expires in %d minutes.",
              "headers": {
                "idempotencyKey": "%s"%s
              }
            }
            """.formatted(
            escapeJson(senderName),
            escapeJson(senderEmail),
            escapeJson(recipientEmail),
            escapeJson(purpose),
            otpCode,
            expiryMinutes,
            escapeJson(purpose),
            otpCode,
            expiryMinutes,
            UUID.randomUUID(),
            sandboxMode ? ",\n                \"X-Sib-Sandbox\": \"drop\"" : ""
        );

        HttpRequest request = HttpRequest.newBuilder(BREVO_SEND_EMAIL_URI)
            .timeout(Duration.ofSeconds(20))
            .header("accept", "application/json")
            .header("content-type", "application/json")
            .header("api-key", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Brevo email send failed with status " + response.statusCode() + ": " + response.body());
            }
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to send OTP email via Brevo", exception);
        }
    }

    private String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }
}

