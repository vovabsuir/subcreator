package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TranscriptResponse {
    private String id;
    private String status;
    private String text;
    private List<Word> words;
    private String error;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Word {
        private String text;
        private int start;
        private int end;
        private String speaker;
    }
}
