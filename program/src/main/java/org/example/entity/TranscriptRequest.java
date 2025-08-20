package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TranscriptRequest {
    @JsonProperty("audio_url")
    private String audioUrl;
    @JsonProperty("auto_chapters")
    private boolean autoChapters = false;
    @JsonProperty("auto_highlights")
    private boolean autoHighlights = false;
    @JsonProperty("content_safety")
    private boolean contentSafety = false;
    @JsonProperty("entity_detection")
    private boolean entityDetection = false;
    private boolean disfluencies = true;
    @JsonProperty("language_code")
    private String languageCode = "en_us";
    @JsonProperty("language_detection")
    private boolean languageDetection = false;
    @JsonProperty("speech_model")
    private String speechModel = "universal";
    private boolean summarization = false;
    private boolean punctuate = true;
    @JsonProperty("speaker_labels")
    private boolean speakerLabels = false;
}
