package org.example.config;

import java.util.Set;

public class ProgramConfig {
    public static final Set<String> LANG_CODES = Set.of("en", "fr", "de", "es", "ru");
    public static final int SENTENCE_MAX_LENGTH = 45;

    public static final String PROPERTY_OUTPUT_FILE_PATH = "outputFilePath";
    public static final String PROPERTY_ALGORITHM = "algorithm";
    public static final String PROPERTY_TRANSLATE = "translate";
    public static final String PROPERTY_PUNCTUATE = "punctuate";
    public static final String PROPERTY_INPUT_FILE = "videoFile";
    public static final String PROPERTY_SOURCE_LANG = "sourceLanguage";
    public static final String ALGORITHM_ASSEMBLY = "AssemblyAI";
    public static final String ALGORITHM_ASCA = "ASCA";

    public static final String FFMPEG_PATH = System.getenv("FFMPEG_PATH");

    private ProgramConfig() {
        throw new IllegalStateException("Config class");
    }
}
