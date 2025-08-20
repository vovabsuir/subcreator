package org.example.gui;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.example.config.ProgramConfig;
import org.example.util.ProgramTool;
import org.example.web.TranscriptApiClient;

import java.util.HashMap;
import java.util.Map;
import static org.example.config.ProgramConfig.ALGORITHM_ASCA;
import static org.example.config.ProgramConfig.ALGORITHM_ASSEMBLY;

@Getter
public class UiManager extends Application {
    private Stage primaryStage;
    private VBox root;
    @Setter
    private int currentStep = 0;
    private Label titleLabel;
    private StackPane contentPane;
    private Button backButton;
    private Button nextButton;
    private Button quitButton;
    private ComboBox<String> sourceLangCombo;
    private ComboBox<String> targetLangCombo;
    private CheckBox translateCheck;
    private ToggleGroup algorithmGroup;

    private final Map<String, Object> data = new HashMap<>();

    private UiSteps uiSteps;

    @Override
    public void start(Stage primaryStage) {
        if (TranscriptApiClient.API_KEY == null) {
            showWarning("Set <ASSEMBLY_AI_API_KEY> environment variable");
            return;
        }

        if (ProgramConfig.FFMPEG_PATH == null) {
            showWarning("Set <FFMPEG_PATH> environment variable for ffmpeg/bin directory");
            return;
        }

        if (!ProgramTool.checkFfmpeg()) {
            showWarning("ffmpeg not found. Please install ffmpeg and add it to your PATH");
            return;
        }

        this.uiSteps  = new UiSteps(this);

        this.primaryStage = primaryStage;
        primaryStage.setTitle("SubCreator");

        initUI();
        uiSteps.showWelcomeScreen();
        backButton.setVisible(false);
        nextButton.setVisible(false);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initUI() {
        ToggleButton themeToggle = new ToggleButton("Light Mode");
        themeToggle.selectedProperty().addListener((_, _, newVal) -> {
            if (Boolean.TRUE.equals(newVal)) {
                root.setStyle("-fx-base: #f5f7fa; -fx-background: #ffffff; -fx-text-fill: #333333;");
                themeToggle.setText("Dark Mode");
            } else {
                root.setStyle("-fx-base: #3f474f; -fx-background: #2d3447; -fx-text-fill: #e0e0e0;");
                themeToggle.setText("Light Mode");
            }
        });

        sourceLangCombo = new ComboBox<>();
        sourceLangCombo.getItems().addAll(ProgramConfig.LANG_CODES);
        sourceLangCombo.getSelectionModel().selectFirst();

        translateCheck = new CheckBox("Translate subtitles");

        targetLangCombo = new ComboBox<>();
        targetLangCombo.getItems().addAll(ProgramConfig.LANG_CODES);
        targetLangCombo.getSelectionModel().selectFirst();
        targetLangCombo.disableProperty().bind(translateCheck.selectedProperty().not());

        algorithmGroup = new ToggleGroup();

        RadioButton rbAssembly = new RadioButton(ALGORITHM_ASSEMBLY);
        rbAssembly.setToggleGroup(algorithmGroup);
        rbAssembly.setSelected(true);

        RadioButton rbASCA = new RadioButton(ALGORITHM_ASCA);
        rbASCA.setToggleGroup(algorithmGroup);

        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-base: #3f474f; -fx-background: #2d3447; -fx-text-fill: #e0e0e0;");

        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #458fd9;");

        contentPane = new StackPane();
        contentPane.setPrefHeight(400);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        backButton.setOnAction(_ -> previousStep());

        nextButton = new Button("Next");
        nextButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        nextButton.setOnAction(_ -> nextStep());

        quitButton = new Button("Quit");
        quitButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        quitButton.setOnAction(_ -> primaryStage.close());

        buttonBox.getChildren().addAll(backButton, nextButton, quitButton);

        root.getChildren().addAll(titleLabel, contentPane, buttonBox, themeToggle);
    }

    public void showStep(int step) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), contentPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(_ -> {
            titleLabel.setVisible(false);
            backButton.setVisible(false);
            nextButton.setVisible(false);
            contentPane.getChildren().clear();
            currentStep = step;

            switch (step) {
                case 0 -> uiSteps.showWelcomeScreen();
                case 1 -> uiSteps.stepSelectVideo();
                case 2 -> uiSteps.stepTranscriptionSettings();
                case 3 -> uiSteps.stepTranslationSettings();
                case 4 -> uiSteps.stepSubtitleSettings();
                case 5 -> uiSteps.stepOutputSettings();
                case 6 -> uiSteps.stepProcessing();
                case 7 -> uiSteps.stepCompletion();
                default -> throw new IllegalStateException("Unexpected value: " + step);
            }

            titleLabel.setVisible(true);
            backButton.setVisible(step > 0 && step < 6);
            nextButton.setVisible(step > 0 && step < 6);
            nextButton.setText(step == 5 ? "Start Processing" : "Next");
            quitButton.setVisible(step != 6);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentPane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    public void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Tooltip createTooltip(String message) {
        Tooltip tooltip = new Tooltip(message);
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setStyle("-fx-font-size: 14px; -fx-background-color: #333; -fx-text-fill: white;");
        return tooltip;
    }

    public void nextStep() {
        showStep(currentStep + 1);
    }
    public void previousStep() {
        showStep(currentStep - 1);
    }

    public void initialize(String[] args) {
        launch(args);
    }
}
