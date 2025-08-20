package org.example.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.SentencesResponse;
import org.example.entity.TranscriptRequest;
import org.example.entity.TranscriptResponse;
import org.example.entity.UploadResponse;
import org.example.exception.IncorrectJsonMappingException;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.SendingRequestException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;

public class TranscriptApiClient {
    private static final String BASE_URL = "https://api.assemblyai.com";
    private static final String TRANSCRIPT_URL = "/v2/transcript/";
    public static final String API_KEY = System.getenv("ASSEMBLY_AI_API_KEY");
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final int TIMEOUT = 10;

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT))
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();

    private TranscriptApiClient() {
        throw new IllegalStateException("Utility class");
    }

    public static UploadResponse uploadAudio(String filePath) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofFile(Path.of(filePath)))
                    .uri(URI.create(BASE_URL + "/v2/upload"))
                    .header("Content-Type", "application/octet-stream")
                    .header(AUTHORIZATION_HEADER, API_KEY)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseHandler.handleTranscriptApiResponse(response, UploadResponse.class);
        } catch (FileNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SendingRequestException(ex.getMessage());
        } catch (IOException ex) {
            throw new SendingRequestException(ex.getMessage());
        }
    }

    public static TranscriptResponse transcriptAudio(TranscriptRequest transcriptRequest) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(transcriptRequest)))
                    .uri(URI.create(BASE_URL + "/v2/transcript"))
                    .header(AUTHORIZATION_HEADER, API_KEY)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseHandler.handleTranscriptApiResponse(response, TranscriptResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IncorrectJsonMappingException(ex.getMessage());
        } catch (IOException ex) {
            throw new SendingRequestException(ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SendingRequestException(ex.getMessage());
        }
    }

    public static TranscriptResponse getTranscript(String transcriptId) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + TRANSCRIPT_URL + transcriptId))
                .header(AUTHORIZATION_HEADER, API_KEY)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseHandler.handleTranscriptApiResponse(response, TranscriptResponse.class);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SendingRequestException(ex.getMessage());
        } catch (IOException ex) {
            throw new SendingRequestException(ex.getMessage());
        }
    }

    public static String getSubtitlesForTranscript(String id) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + TRANSCRIPT_URL + id + "/srt"))
                .header(AUTHORIZATION_HEADER, API_KEY)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException ex) {
            throw new SendingRequestException(ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SendingRequestException(ex.getMessage());
        }
    }

    public static SentencesResponse getTranscriptSentences(String id) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + TRANSCRIPT_URL + id + "/sentences"))
                .header(AUTHORIZATION_HEADER, API_KEY)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseHandler.handleTranscriptApiResponse(response, SentencesResponse.class);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SendingRequestException(ex.getMessage());
        } catch (IOException ex) {
            throw new SendingRequestException(ex.getMessage());
        }
    }
}
