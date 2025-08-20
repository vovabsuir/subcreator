package org.example.util;

import org.example.config.ProgramConfig;
import org.example.exception.ExtractingAudioException;
import java.io.File;
import java.io.IOException;

public class VideoTool {

    private VideoTool() {
        throw new IllegalStateException("Utility class");
    }

    public static void extractAudio(File file, String fileName) {
        ProcessBuilder pb = new ProcessBuilder(
                ProgramConfig.FFMPEG_PATH + "\\ffmpeg.exe", "-i", "\"" + file.getAbsolutePath() + "\"", "-vn",
                "-acodec", "pcm_s16le", fileName);
        try {
            pb.start().waitFor();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExtractingAudioException(e.getMessage());
        }
    }
}
