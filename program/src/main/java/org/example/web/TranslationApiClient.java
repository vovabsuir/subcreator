package org.example.web;

import org.example.entity.TranslationResponse;
import org.example.exception.SendingRequestException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TranslationApiClient {
    private static final String BASE_URL = "https://ftapi.pythonanywhere.com";
    private static final String TRANSLATION_URL = "/translate";
    private static final int TIMEOUT = 10;

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT))
            .build();

    private TranslationApiClient() {
        throw new IllegalStateException("Utility class");
    }

    public static TranslationResponse translateText(String encodedText, String sourceLanguage,
                                                    String targetLanguage) {
        String uri = String.format("%s%s?sl=%s&dl=%s&text=%s", BASE_URL,
                TRANSLATION_URL, sourceLanguage, targetLanguage, encodedText);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseHandler.handleTranslationApiResponse(response, TranslationResponse.class);
        } catch (IOException ex) {
            throw new SendingRequestException(ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SendingRequestException(ex.getMessage());
        }
    }
}
