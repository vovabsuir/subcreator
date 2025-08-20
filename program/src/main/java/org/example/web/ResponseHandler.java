package org.example.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.BadRequestException;
import org.example.exception.ErrorResponse;
import org.example.exception.IncorrectJsonMappingException;
import org.example.exception.InternalServerException;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.TooManyReequestsException;
import org.example.exception.UnauthorizedException;
import org.example.exception.UnknownException;
import java.net.http.HttpResponse;

public class ResponseHandler {
    private static final ObjectMapper mapper = new ObjectMapper();

    private ResponseHandler() {
        throw new IllegalStateException("Utility class");
    }

    public static<T> T handleTranscriptApiResponse(HttpResponse<String> response, Class<T> responseClass) {
        try {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return mapper.readValue(response.body(), responseClass);
            } else {
                ErrorResponse error = mapper.readValue(response.body(), ErrorResponse.class);
                switch (response.statusCode()) {
                    case 400 -> throw new BadRequestException(error.getError());
                    case 401 -> throw new UnauthorizedException(error.getError());
                    case 404 -> throw new ResourceNotFoundException(error.getError());
                    case 429 -> throw new TooManyReequestsException(error.getError());
                    case 500 -> throw new InternalServerException(error.getError());
                    default -> throw new UnknownException(error.getError());
                }
            }
        } catch (JsonProcessingException ex) {
            throw new IncorrectJsonMappingException(ex.getMessage());
        }
    }

    public static<T> T handleTranslationApiResponse(HttpResponse<String> response, Class<T> responseClass) {
        try {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return mapper.readValue(response.body(), responseClass);
            } else {
                throw new UnknownException("Unknown translation API response");
            }
        } catch (JsonProcessingException ex) {
            throw new IncorrectJsonMappingException(ex.getMessage());
        }
    }
}
