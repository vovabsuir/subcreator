package org.example.util;

import lombok.Getter;
import lombok.Setter;
import org.example.config.ProgramConfig;
import org.example.entity.SubtitleBlock;
import org.example.entity.TranscriptResponse.Word;
import org.example.exception.WritingFileException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubTool {
    @Getter
    @Setter
    private static float pauseThreshold = 1f;
    @Getter
    @Setter
    private static byte lineCount = 2;
    @Getter
    @Setter
    private static byte maxCharsPerLine = 45;
    @Getter
    @Setter
    private static boolean usePauseIdentifier = false;

    private SubTool() {
        throw new IllegalStateException("Utility class");
    }

    public static List<SubtitleBlock> buildSubtitlesV1(List<Word> words) {
        List<SubtitleBlock> subtitles = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        int start = words.getFirst().getStart();
        int end = words.getFirst().getEnd();

        int currentLine = 0;
        int lineLength = 0;
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);

            boolean isLimitReached = lineLength + word.getText().length() > maxCharsPerLine;
            boolean isLongPause = (i > 0) && (word.getStart() - words.get(i - 1).getEnd() > pauseThreshold);

            if (isLimitReached || (usePauseIdentifier && isLongPause)) {
                if (++currentLine == lineCount) {
                    subtitles.add(new SubtitleBlock(text.toString().trim(), start, end));

                    text = new StringBuilder(word.getText() + " ");
                    start = word.getStart();
                    currentLine = 0;
                } else {
                    text.append('\n');
                    text.append(word.getText()).append(" ");
                }
                lineLength = word.getText().length();
            } else {
                text.append(word.getText()).append(" ");
                lineLength += word.getText().length();
            }
            end = word.getEnd();
        }

        if (!text.isEmpty()) {
            subtitles.add(new SubtitleBlock(text.toString().trim(), start, end));
        }

        return subtitles;
    }

    public static void insertSubtitles(String videoFilePath, String subtitlesFilePath,
                                       String outputFilePath, Map<String, Object> styles) {
        String font = styles.getOrDefault("font", "Arial").toString();
        int fontSize = (int) styles.getOrDefault("fontSize", 14);
        String textColor = (String) styles.getOrDefault("textColor", "&HFFFFFF&");
        String bgColor = (String) styles.getOrDefault("bgColor", "&H000000&");
        String position = styles.getOrDefault("position", "Bottom").toString();
        boolean border = (boolean) styles.getOrDefault("border", true);

        StringBuilder filter = new StringBuilder("subtitles=");
        filter.append('"').append(subtitlesFilePath).append('"')
                .append(":force_style='")
                .append("Fontname=").append(font).append(",")
                .append("Fontsize=").append(fontSize).append(",")
                .append("PrimaryColour=").append(textColor).append(",");

        if (border) {
            filter.append("OutlineColour=").append(bgColor).append(",")
                    .append("BorderStyle=3,Outline=1,Shadow=0,");
        }

        switch (position.toLowerCase()) {
            case "top" -> filter.append("Alignment=6,");
            case "middle" -> filter.append("Alignment=5,");
            default -> filter.append("Alignment=2,");
        }

        filter.deleteCharAt(filter.length() - 1);
        filter.append("'");

        ProcessBuilder pb = new ProcessBuilder(
                ProgramConfig.FFMPEG_PATH + "\\ffmpeg.exe", "-i", videoFilePath,
                "-vf", filter.toString(), outputFilePath);

        File tempOutput = null;
        File tempError = null;
        try {
            tempOutput = File.createTempFile("ffmpeg_output", ".log");
            tempError = File.createTempFile("ffmpeg_error", ".log");

            pb.redirectOutput(tempOutput);
            pb.redirectError(tempError);

            pb.start().waitFor();

        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new WritingFileException("Exception while creating final video: " + ex.getMessage());
        } finally {
            if (tempOutput != null) tempOutput.delete();
            if (tempError != null) tempError.delete();
        }
    }

    public static void createSubFile(List<SubtitleBlock> subtitles, String filename) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            for (int i = 0; i < subtitles.size(); i++) {
                SubtitleBlock block = subtitles.get(i);
                writer.write(String.format("%d%n%s --> %s%n%s%n%n",
                        i + 1,
                        formatTime(block.getStart()),
                        formatTime(block.getEnd()),
                        block.getText()
                ));
            }
        } catch (IOException e) {
            throw new WritingFileException("Exception while writing to .srt file. " + e.getMessage());
        }
    }

    private static String formatTime(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;
        long milliseconds = millis % 1000;
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
    }

    public static int timeStringToMillis(String timeString) {
        String[] parts = timeString.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Incorrect time format: " + timeString);
        }

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        String[] secMillis = parts[2].split(",");
        if (secMillis.length != 2) {
            throw new IllegalArgumentException("Incorrect seconds and milliseconds format: " + parts[2]);
        }

        int seconds = Integer.parseInt(secMillis[0]);
        int millis = Integer.parseInt(secMillis[1]);

        return hours * 3600_000 + minutes * 60_000 + seconds * 1000 + millis;
    }
}
