package com.quickbite.authservice.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.authservice.dto.GoogleProfile;
import com.quickbite.authservice.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${quickbite.google.client-id:}")
    private String googleClientId;

    public GoogleProfile verify(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new InvalidCredentialsException("Google credential is required");
        }

        try {
            String token = URLEncoder.encode(credential, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest
                    .newBuilder(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + token))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new InvalidCredentialsException("Unable to verify Google sign-in");
            }

            JsonNode payload = objectMapper.readTree(response.body());
            String audience = text(payload, "aud");
            if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(audience)) {
                throw new InvalidCredentialsException("Google token audience mismatch");
            }

            boolean emailVerified = payload.path("email_verified").asBoolean(false)
                    || "true".equalsIgnoreCase(payload.path("email_verified").asText());
            if (!emailVerified) {
                throw new InvalidCredentialsException("Google email is not verified");
            }

            return new GoogleProfile(
                    text(payload, "sub"),
                    text(payload, "email"),
                    text(payload, "given_name"),
                    text(payload, "family_name"),
                    text(payload, "picture"),
                    true);
        } catch (IOException e) {
            throw new InvalidCredentialsException("Unable to read Google sign-in payload");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidCredentialsException("Google sign-in was interrupted");
        }
    }

    private String text(JsonNode payload, String field) {
        String value = payload.path(field).asText();
        return value == null || value.isBlank() ? null : value;
    }
}
