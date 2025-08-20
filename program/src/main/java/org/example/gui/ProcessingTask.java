package org.example.gui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.example.entity.SentencesResponse;
import org.example.entity.SubtitleBlock;
import org.example.entity.TranscriptRequest;
import org.example.entity.TranscriptResponse;
import org.example.entity.UploadResponse;
import org.example.exception.TranscriptingAudioException;
import org.example.util.ProgramTool;
import org.example.util.SubTool;
import org.example.util.VideoTool;
import org.example.web.TranscriptApiClient;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import static org.example.config.ProgramConfig.ALGORITHM_ASCA;
import static org.example.config.ProgramConfig.ALGORITHM_ASSEMBLY;
import static org.example.config.ProgramConfig.PROPERTY_ALGORITHM;
import static org.example.config.ProgramConfig.PROPERTY_INPUT_FILE;
import static org.example.config.ProgramConfig.PROPERTY_OUTPUT_FILE_PATH;
import static org.example.config.ProgramConfig.PROPERTY_PUNCTUATE;
import static org.example.config.ProgramConfig.PROPERTY_SOURCE_LANG;
import static org.example.config.ProgramConfig.PROPERTY_TRANSLATE;

public class ProcessingTask extends Task<Void> {
    private final TextArea logArea;
    private final Map<String, Object> data;

    public ProcessingTask(TextArea logArea, Map<String, Object> data) {
        this.data = data;
        this.logArea = logArea;
    }

    private String extractAudio(File videoFile) {
        updateLog("Extracting audio from video");

        String audioFileName = "audio_" + System.currentTimeMillis() + ".wav";
        VideoTool.extractAudio(videoFile, audioFileName);

        return audioFileName;
    }

    private TranscriptResponse startTranscription(UploadResponse uploadResponse) {
        TranscriptRequest transcriptRequest = new TranscriptRequest();
        transcriptRequest.setAudioUrl(uploadResponse.getUploadUrl());
        transcriptRequest.setLanguageCode((String) data.get(PROPERTY_SOURCE_LANG));
        transcriptRequest.setPunctuate((Boolean) data.get(PROPERTY_PUNCTUATE));
        transcriptRequest.setSpeakerLabels(false);

        updateLog("Starting transcription");

        return TranscriptApiClient.transcriptAudio(transcriptRequest);
    }

    private boolean translateAndSaveSubtitles(TranscriptResponse transcriptResponse, String srtFileName) {
        updateLog("Starting translation");
        String targetLanguage = (String) data.get("targetLanguage");
        SentencesResponse sentencesResponse = ProgramTool.createSentencesFromWords(transcriptResponse);
        if (isCancelled()) return false;

        updateLog("Translating text");
        java.util.List<SubtitleBlock> subtitleBlocks = ProgramTool.translateTranscript(
                sentencesResponse,
                6,
                (String) data.get(PROPERTY_SOURCE_LANG),
                targetLanguage
        );

        if (isCancelled()) return false;
        updateLog("Creating subtitle file");
        SubTool.createSubFile(subtitleBlocks, srtFileName);

        return true;
    }

    private boolean createAndSaveSubtitles(TranscriptResponse transcriptResponse, String srtFileName) {
        String algorithm = (String) data.get(PROPERTY_ALGORITHM);

        if (ALGORITHM_ASSEMBLY.equals(algorithm)) {
            updateLog("Generating subtitles with Assembly AI");
            String subtitles = TranscriptApiClient.getSubtitlesForTranscript(transcriptResponse.getId());

            if (isCancelled()) return false;
            updateLog("Correcting subtitles");
            ProgramTool.correctAndSaveSubtitles(subtitles, srtFileName);
        } else if (ALGORITHM_ASCA.equals(algorithm)) {
            updateLog("Generating subtitles with ASCA");
            List<SubtitleBlock> subtitles = SubTool.buildSubtitlesV1(transcriptResponse.getWords());

            if (isCancelled()) return false;
            updateLog("Creating subtitle file");
            SubTool.createSubFile(subtitles, srtFileName);
        }

        return true;
    }

    private void insertSubtitles(File videoFile, String srtFileName) {
        updateLog("Adding subtitles to video");
        String outputPath = (String) data.get(PROPERTY_OUTPUT_FILE_PATH);
        SubTool.insertSubtitles(videoFile.getAbsolutePath(), srtFileName, outputPath,
                (Map<String, Object>) data.get("subtitleStyles"));
    }

    private void clearData(String audioFileName, String srtFileName) throws IOException {
        updateLog("Clearing files");
        Files.deleteIfExists(Path.of(audioFileName));
        Files.deleteIfExists(Path.of(srtFileName));
    }

    @Override
    protected Void call() throws Exception {
        File videoFile = (File) data.get(PROPERTY_INPUT_FILE);
        String audioFileName = extractAudio(videoFile);
        if (isCancelled()) return null;

        updateLog("Uploading audio for transcription");
        UploadResponse uploadResponse = TranscriptApiClient.uploadAudio(audioFileName);
        if (isCancelled()) return null;

        TranscriptResponse transcriptResponse = startTranscription(uploadResponse);
        if (isCancelled()) return null;

        while (transcriptResponse.getStatus().equals("processing") ||
                transcriptResponse.getStatus().equals("queued")) {
            updateLog("Transcription in progress (" + transcriptResponse.getStatus() + ")");
            Thread.sleep(10000);

            if (isCancelled()) return null;

            transcriptResponse = TranscriptApiClient.getTranscript(transcriptResponse.getId());
        }

        if (transcriptResponse.getStatus().equals("error")) {
            throw new TranscriptingAudioException("Transcription failed: " + transcriptResponse.getError());
        }
        updateLog("Transcription completed successfully");

        String srtFileName = "subs_" + System.currentTimeMillis() + ".srt";

        if ((boolean) data.get(PROPERTY_TRANSLATE)) {
            if (!translateAndSaveSubtitles(transcriptResponse, srtFileName)) return null;
        } else {
            if (!createAndSaveSubtitles(transcriptResponse, srtFileName)) return null;
        }

        if (isCancelled()) return null;
        insertSubtitles(videoFile, srtFileName);

        clearData(audioFileName, srtFileName);

        updateLog("Process completed successfully!");
        succeeded();

        return null;
    }

    private void updateLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText("• " + message + "\n");
            logArea.selectEnd();
        });
    }
}
