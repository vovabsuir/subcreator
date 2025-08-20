package org.example.util;

import javafx.scene.paint.Color;
import org.example.config.ProgramConfig;
import org.example.entity.Sentence;
import org.example.entity.SentencesResponse;
import org.example.entity.SubtitleBlock;
import org.example.entity.TranscriptResponse;
import org.example.entity.TranslationResponse;
import org.example.exception.WritingFileException;
import org.example.web.TranslationApiClient;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class ProgramTool {

    private ProgramTool() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Corrects subtitles given by AssemblyAI, since it returns incorrect time intervals
     * by adding +500 ms (sometimes can be different) to each new interval (regardless of previous interval end).
     * Problem is faced only with sentences
     *
     * @param subtitles raw string containing subtitles in .srt format
     * @param subtitlesFileName file name where subtitles will be saved
     */
    public static void correctAndSaveSubtitles(String subtitles, String subtitlesFileName) {
        List<String> subs = new ArrayList<>(List.of(subtitles.split("\n")));
        String buf = subs.get(1).substring(17);

        for (int i = 5; i < subs.size(); i += 4) {
            int start = SubTool.timeStringToMillis(subs.get(i).substring(0, 11));
            int end = SubTool.timeStringToMillis(buf);
            if (start - end < 1500) {
                subs.set(i, buf + subs.get(i).substring(12));
            }

            buf = subs.get(i).substring(17);
        }

        File file = new File(subtitlesFileName);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(String.join("\n", subs));
        } catch (IOException ex) {
            throw new WritingFileException(ex.getMessage());
        }
    }

    public static SentencesResponse createSentencesFromWords(TranscriptResponse transcriptResponse) {
        StringBuilder sentence = new StringBuilder();
        List<Sentence> sentences = new ArrayList<>();
        int start = 0;
        int flag = 1;

        for (TranscriptResponse.Word word : transcriptResponse.getWords()) {
            if (flag == 1) {
                start = word.getStart();
                flag = 0;
            }

            if (".?!".indexOf(word.getText().charAt(word.getText().length() - 1)) >= 0) {
                sentence.append(word.getText());
                sentences.add(new Sentence(sentence.toString(), start, word.getEnd(), null));

                sentence = new StringBuilder();
                flag = 1;
            } else sentence.append(word.getText()).append(" ");
        }

        return new SentencesResponse(sentences);
    }

    public static List<SubtitleBlock> translateTranscript(SentencesResponse sentencesResponse,
                                                          int sentencesPerTranslation,
                                                          String sourceLanguage, String targetLanguage) {
        List<Sentence> sentences = sentencesResponse.getSentences();
        List<SubtitleBlock> subtitles = new ArrayList<>();
        int i = 1;
        int start = 0;
        String text = "";
        while (i <= sentences.size()) {
            text = String.join(" ", text, sentences.get(i - 1).getText());
            if (i % sentencesPerTranslation == 0) {
                translateSentences(text, sourceLanguage, targetLanguage, start, subtitles, sentences);
                text = "";
                start = i;
            }
            i++;
        }

        if (!text.isEmpty()) {
            translateSentences(text, sourceLanguage, targetLanguage, start, subtitles, sentences);
        }

        return subtitles;
    }

    private static void translateSentences(String text, String sourceLanguage, String targetLanguage,
                                           int start, List<SubtitleBlock> subtitles,
                                           List<Sentence> sentences) {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        TranslationResponse translationResponse = TranslationApiClient.translateText(
                encodedText, sourceLanguage, targetLanguage);
        List<String> translatedSentences = List.of(translationResponse.getText().split("(?<=[.!?])\\s+"));

        int j = start;
        while (j < start + translatedSentences.size()) {
            if (translatedSentences.get(j - start).length() <= ProgramConfig.SENTENCE_MAX_LENGTH
                    && j != start + translatedSentences.size() - 1 &&
                    translatedSentences.get(j + 1 - start).length() <= ProgramConfig.SENTENCE_MAX_LENGTH) {
                subtitles.add(new SubtitleBlock(translatedSentences.get(j - start)
                        + translatedSentences.get(j + 1 - start),
                        sentences.get(j).getStart(), sentences.get(j + 1).getEnd()));
                j++;
            } else {
                subtitles.add(new SubtitleBlock(translatedSentences.get(j - start),
                        sentences.get(j).getStart(), sentences.get(j).getEnd()));
            }
            j++;
        }
    }

    public static boolean checkFfmpeg() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"ffmpeg", "-version"});
            return process.waitFor() == 0;
        } catch (Exception _) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static String formatColor(Color color) {
        int r = (int) (color.getRed()   * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue()  * 255);

        return String.format("&H%02X%02X%02X&", b, g, r);
    }
}
