package org.example.gui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.awt.Desktop;
import org.example.exception.UnknownException;
import org.example.util.ProgramTool;
import org.example.util.SubTool;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import static org.example.config.ProgramConfig.ALGORITHM_ASCA;
import static org.example.config.ProgramConfig.PROPERTY_ALGORITHM;
import static org.example.config.ProgramConfig.PROPERTY_INPUT_FILE;
import static org.example.config.ProgramConfig.PROPERTY_OUTPUT_FILE_PATH;
import static org.example.config.ProgramConfig.PROPERTY_PUNCTUATE;
import static org.example.config.ProgramConfig.PROPERTY_SOURCE_LANG;
import static org.example.config.ProgramConfig.PROPERTY_TRANSLATE;

public class UiSteps {
    private final TextField outputPathField;
    private final TextArea logArea;

    private final Preferences prefs = Preferences.userNodeForPackage(UiManager.class);
    private final Map<String, Object> data;

    private final UiManager uiManager;

    public UiSteps(UiManager uiManager) {
        this.uiManager = uiManager;
        this.data = uiManager.getData();

        outputPathField = new TextField();
        logArea = new TextArea();
    }

    public void showWelcomeScreen() {
        uiManager.setCurrentStep(0);
        uiManager.getTitleLabel().setText("Welcome to SubCreator!");

        VBox welcomePane = new VBox(20);
        welcomePane.setAlignment(Pos.CENTER);
        welcomePane.setPadding(new Insets(40));

        Label welcomeLabel = new Label("Create professional subtitles for your videos");
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        welcomeLabel.setWrapText(true);
        welcomeLabel.setAlignment(Pos.CENTER);

        Label descriptionLabel = new Label("""
                You can:
                • Transcribe audio to text
                • Translate subtitles to different languages
                • Customize subtitle appearance
                • Burn subtitles directly into your video""");
        descriptionLabel.setStyle("-fx-font-size: 18px;");
        descriptionLabel.setWrapText(true);

        Button startButton = new Button("Start creating subtitles");
        startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;");
        startButton.setOnAction(e -> uiManager.showStep(1));

        welcomePane.getChildren().addAll(welcomeLabel, descriptionLabel, startButton);
        uiManager.getContentPane().getChildren().setAll(welcomePane);


    }

    public void stepSelectVideo() {
        uiManager.getTitleLabel().setText("Step 1/5: Select Video File");

        VBox stepContent = new VBox(20);
        stepContent.setPadding(new Insets(20));
        stepContent.setAlignment(Pos.CENTER);

        Label instruction = new Label("Select the video file you want to add subtitles to");
        instruction.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label selectedLabel = new Label();
        selectedLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        if (data.containsKey(PROPERTY_INPUT_FILE)) {
            File file = (File) data.get(PROPERTY_INPUT_FILE);
            selectedLabel.setText("Selected: " + file.getName());
        } else selectedLabel.setText("Selected: N/A");

        StackPane targetPane = new StackPane();
        targetPane.setPrefSize(300, 150);
        targetPane.setStyle("-fx-border-color: #ccc; -fx-border-width: 2px; -fx-background-color: #f9f9f9;");

        Label dropLabel = new Label("Drag & Drop video file here");
        dropLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
        dropLabel.setLayoutX(50);
        dropLabel.setLayoutY(65);
        targetPane.setAlignment(Pos.CENTER);
        targetPane.getChildren().add(dropLabel);

        targetPane.setOnMouseClicked(e -> {
            File file = new FileChooser().showOpenDialog(uiManager.getPrimaryStage());
            if (file != null) {
                data.put(PROPERTY_INPUT_FILE, file);
                selectedLabel.setText("Selected: " + file.getName());
            }
        });

        targetPane.setOnDragEntered(_ -> dropLabel.setText("Drop video file"));

        targetPane.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                targetPane.setStyle("-fx-border-color: #4caf50; -fx-border-width: 2px; -fx-background-color: #e8f5e9;");
            }
            event.consume();
        });

        targetPane.setOnDragExited(event -> {
            dropLabel.setText("Drag & Drop video file here");
            targetPane.setStyle("-fx-border-color: #ccc; -fx-border-width: 2px; -fx-background-color: #f9f9f9;");
            event.consume();
        });

        targetPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().getFirst();

                data.put(PROPERTY_INPUT_FILE, file);
                selectedLabel.setText("Selected: " + file.getName());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        uiManager.getNextButton().setOnAction(e -> {
            if (!data.containsKey(PROPERTY_INPUT_FILE)) {
                uiManager.showWarning("Please select a video file");
                return;
            }
            uiManager.nextStep();
        });

        stepContent.getChildren().addAll(instruction, targetPane, selectedLabel);
        uiManager.getContentPane().getChildren().add(stepContent);
    }

    public void stepTranscriptionSettings() {
        uiManager.getTitleLabel().setText("Step 2/5: Transcription Settings");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        Label sourceLangLabel = new Label("Source Language:");
        GridPane.setConstraints(sourceLangLabel, 0, 0);

        GridPane.setConstraints(uiManager.getSourceLangCombo(), 1, 0);
        uiManager.getSourceLangCombo().setPrefWidth(200);
        uiManager.getSourceLangCombo().setTooltip(new Tooltip("Original language of the video"));

        grid.getChildren().addAll(sourceLangLabel, uiManager.getSourceLangCombo());

        uiManager.getNextButton().setOnAction(e -> {
            data.put(PROPERTY_SOURCE_LANG, uiManager.getSourceLangCombo().getValue());
            data.put(PROPERTY_PUNCTUATE, true);

            uiManager.nextStep();
        });

        uiManager.getContentPane().getChildren().add(grid);
    }

    public void stepTranslationSettings() {
        uiManager.getTitleLabel().setText("Step 3/5: Translation Settings");

        VBox stepContent = new VBox(20);
        stepContent.setPadding(new Insets(20));

        Label question = new Label("Do you want to translate the subtitles?");
        question.setStyle("-fx-font-size: 18px;");

        HBox translateBox = new HBox(15);
        translateBox.setAlignment(Pos.CENTER_LEFT);
        translateBox.getChildren().addAll(uiManager.getTranslateCheck(), uiManager.getTargetLangCombo());

        uiManager.getNextButton().setOnAction(e -> {
            data.put(PROPERTY_TRANSLATE, uiManager.getTranslateCheck().isSelected());
            if (uiManager.getTranslateCheck().isSelected()) {
                data.put("targetLanguage", uiManager.getTargetLangCombo().getValue());
            }

            uiManager.showStep(uiManager.getCurrentStep() + 1 + (uiManager.getTranslateCheck().isSelected() ? 1 : 0));
        });

        stepContent.getChildren().addAll(question, translateBox);
        uiManager.getContentPane().getChildren().add(stepContent);
    }

    public void stepSubtitleSettings() {
        uiManager.getTitleLabel().setText("Step 4/5: Subtitle Generation");

        VBox stepContent = new VBox(20);
        stepContent.setPadding(new Insets(20));

        VBox ascaSettingsContainer = new VBox(10);
        ascaSettingsContainer.setPadding(new Insets(15, 0, 0, 15));
        ascaSettingsContainer.setVisible(false);

        TextField pauseThresholdField = new TextField(String.valueOf(SubTool.getPauseThreshold()));
        pauseThresholdField.setTooltip(uiManager
                .createTooltip("Time interval between two words that will be considered as pause"));

        Spinner<Integer> lineCountSpinner = new Spinner<>(1, 5, SubTool.getLineCount());
        lineCountSpinner.setTooltip(uiManager
                .createTooltip("Number of subtitles' lines that will be displayed in the video simultaneously"));

        Spinner<Integer> maxCharsSpinner = new Spinner<>(20, 80, SubTool.getMaxCharsPerLine());
        maxCharsSpinner.setTooltip(uiManager.createTooltip("Maximum number of characters per line"));

        CheckBox usePauseCheck = new CheckBox("Use pause detection");
        usePauseCheck.setTooltip(uiManager
                .createTooltip("Algorithm will identify pauses and set speech after it into a new block of subtitles"));
        usePauseCheck.setSelected(SubTool.isUsePauseIdentifier());
        pauseThresholdField.disableProperty().bind(usePauseCheck.selectedProperty().not());

        pauseThresholdField.setPrefWidth(80);
        lineCountSpinner.setPrefWidth(80);
        maxCharsSpinner.setPrefWidth(80);

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(10);
        settingsGrid.setVgap(10);

        settingsGrid.addRow(0, usePauseCheck);
        settingsGrid.addRow(1, new Label("Pause threshold (sec):"), pauseThresholdField);
        settingsGrid.addRow(2, new Label("Max lines on screen:"), lineCountSpinner);
        settingsGrid.addRow(3, new Label("Max characters per line:"), maxCharsSpinner);

        ascaSettingsContainer.getChildren().addAll(
                new Label("Advanced Settings:"),
                settingsGrid
        );

        if (!uiManager.getTranslateCheck().isSelected()) {
            Label algorithmLabel = new Label("Select subtitle generation algorithm:");
            algorithmLabel.setStyle("-fx-font-size: 16px;");

            VBox algoBox = new VBox(10);
            for (Toggle toggle : uiManager.getAlgorithmGroup().getToggles()) {
                RadioButton rb = (RadioButton) toggle;
                algoBox.getChildren().add(rb);

                rb.selectedProperty().addListener((obs, oldVal, newVal) ->
                        ascaSettingsContainer.setVisible(newVal && rb.getText().equals(ALGORITHM_ASCA))
                );
            }

            stepContent.getChildren().addAll(algorithmLabel, algoBox, ascaSettingsContainer);
        } else {
            Label infoLabel = new Label("Subtitles will be generated from the translated text.");
            infoLabel.setStyle("-fx-font-size: 16px;");
            stepContent.getChildren().add(infoLabel);
        }

        uiManager.getNextButton().setOnAction(_ -> {
            data.put(PROPERTY_ALGORITHM, ((RadioButton) uiManager.getAlgorithmGroup().getSelectedToggle()).getText());

            if (ascaSettingsContainer.isVisible()) {
                try {
                    float pauseThreshold = Float.parseFloat(pauseThresholdField.getText());
                    SubTool.setPauseThreshold(pauseThreshold);
                } catch (NumberFormatException _) {
                    uiManager.showWarning("Invalid pause threshold value. Using default.");
                }

                SubTool.setLineCount((byte) lineCountSpinner.getValue().intValue());
                SubTool.setMaxCharsPerLine((byte) maxCharsSpinner.getValue().intValue());
                SubTool.setUsePauseIdentifier(usePauseCheck.isSelected());
            }

            uiManager.nextStep();
        });

        uiManager.getContentPane().getChildren().add(stepContent);
    }

    public void stepOutputSettings() {
        uiManager.getTitleLabel().setText("Step 5/5: Output Settings");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        Label outputLabel = new Label("Output File Path:");
        GridPane.setConstraints(outputLabel, 0, 0);

        String lastPath = prefs.get("dir", System.getProperty("user.home")) + "\\video_subtitled.mp4";

        outputPathField.setPrefWidth(400);
        outputPathField.setText(lastPath);
        GridPane.setConstraints(outputPathField, 1, 0);

        Button browseButton = new Button("Browse");
        browseButton.setOnAction(e -> {
            FileChooser saveChooser = new FileChooser();
            saveChooser.setTitle("Save Output Video");
            saveChooser.setInitialFileName("video_subtitled.mp4");
            saveChooser.setInitialDirectory(new File(prefs.get("dir", System.getProperty("user.home"))));

            File file = saveChooser.showSaveDialog(uiManager.getPrimaryStage());
            if (file != null) {
                if (file.exists()) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "File already exists. Overwrite?", ButtonType.YES, ButtonType.NO);
                    if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
                    else {
                        if (!file.delete()) {
                            uiManager.showWarning("File could not be deleted.");
                            return;
                        }
                    }
                }
                prefs.put("dir", file.getParent());
                outputPathField.setText(file.getAbsolutePath());
            }
        });
        GridPane.setConstraints(browseButton, 2, 0);
        grid.getChildren().addAll(outputLabel, outputPathField, browseButton);

        TitledPane stylePane = new TitledPane();
        stylePane.setText("Subtitle Styling");
        stylePane.setExpanded(false);
        GridPane.setConstraints(stylePane, 0, 1, 3, 1);

        GridPane styleGrid = new GridPane();
        styleGrid.setHgap(10);
        styleGrid.setVgap(10);
        styleGrid.setPadding(new Insets(15));

        Label fontLabel = new Label("Font:");
        ComboBox<String> fontCombo = new ComboBox<>();
        fontCombo.getItems().addAll("Arial", "Verdana", "Helvetica", "Times New Roman", "Courier New", "Tahoma");
        fontCombo.setValue("Arial");

        Label sizeLabel = new Label("Font Size:");
        Spinner<Integer> sizeSpinner = new Spinner<>(10, 50, 14);
        sizeSpinner.setEditable(true);
        sizeSpinner.setPrefWidth(80);

        Label colorLabel = new Label("Text Color:");
        ColorPicker textColorPicker = new ColorPicker(Color.WHITE);

        Label borderLabel = new Label("Background:");
        CheckBox borderCheck = new CheckBox("Add background");
        borderCheck.setSelected(true);

        Label bgLabel = new Label("Background Color:");
        ColorPicker bgColorPicker = new ColorPicker(Color.BLACK);
        bgLabel.disableProperty().bind(borderCheck.selectedProperty().not());
        bgColorPicker.disableProperty().bind(borderCheck.selectedProperty().not());

        Label positionLabel = new Label("Position:");
        ComboBox<String> positionCombo = new ComboBox<>();
        positionCombo.getItems().addAll("Bottom", "Top", "Middle");
        positionCombo.setValue("Bottom");

        styleGrid.addRow(0, fontLabel, fontCombo);
        styleGrid.addRow(1, sizeLabel, sizeSpinner);
        styleGrid.addRow(2, colorLabel, textColorPicker);
        styleGrid.addRow(3, borderLabel, borderCheck);
        styleGrid.addRow(4, bgLabel, bgColorPicker);
        styleGrid.addRow(5, positionLabel, positionCombo);

        stylePane.setContent(styleGrid);
        grid.getChildren().add(stylePane);

        uiManager.getNextButton().setOnAction(_ -> {
            if (outputPathField.getText().isBlank()) {
                uiManager.showWarning("Please specify output path");
                return;
            }

            Path path = Path.of(outputPathField.getText());
            if (!Files.exists(path.getParent())) {
                uiManager.showWarning("Incorrect output path");
                return;
            }

            if (Files.exists(path)) {
                uiManager.showWarning("File with such name already exists");
                return;
            }

            data.put(PROPERTY_OUTPUT_FILE_PATH, outputPathField.getText());

            Map<String, Object> styles = new HashMap<>();
            styles.put("font", fontCombo.getValue());
            styles.put("fontSize", sizeSpinner.getValue());
            styles.put("textColor", ProgramTool.formatColor(textColorPicker.getValue()));
            styles.put("bgColor", ProgramTool.formatColor(bgColorPicker.getValue()));
            styles.put("position", positionCombo.getValue());
            styles.put("border", borderCheck.isSelected());

            data.put("subtitleStyles", styles);

            uiManager.nextStep();
        });

        uiManager.getContentPane().getChildren().add(grid);
    }

    public void stepProcessing() {
        uiManager.getTitleLabel().setText("Processing");

        VBox processingPane = new VBox(20);
        processingPane.setPadding(new Insets(20));
        processingPane.setAlignment(Pos.CENTER);

        Button cancelButton = new Button("Cancel");

        logArea.setPrefHeight(300);
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-size: 18px;");
        logArea.setText("Starting subtitle creation\n");

        processingPane.getChildren().addAll(
                new ScrollPane(logArea),
                cancelButton
        );

        uiManager.getContentPane().getChildren().add(processingPane);

        Task<Void> task = startBackgroundProcessing();

        cancelButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure?", ButtonType.YES, ButtonType.NO);
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

            task.cancel();
            uiManager.showStep(0);
        });
    }

    public void stepCompletion() {
        uiManager.getTitleLabel().setText("Process Completed!");

        VBox completionPane = new VBox(30);
        completionPane.setPadding(new Insets(40));
        completionPane.setAlignment(Pos.CENTER);

        Label successLabel = new Label("Subtitles successfully added to your video!");
        successLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        Button newButton = new Button("Create another video");
        newButton.setStyle("-fx-font-size: 16px; -fx-padding: 10 20;");
        newButton.setOnAction(_ -> showWelcomeScreen());

        Button openButton = new Button("Open created video");
        openButton.setStyle("-fx-font-size: 16px; -fx-padding: 10 20;");
        openButton.setOnAction(_ -> {
            try {
                File file = new File((String) data.get(PROPERTY_OUTPUT_FILE_PATH));
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (IOException ex) {
                throw new UnknownException(ex.getMessage());
            }
        });

        Label savedFile = new Label("Your video with subtitles has been saved to:\n" + data.get(PROPERTY_OUTPUT_FILE_PATH));
        savedFile.setStyle("-fx-font-size: 16px");

        completionPane.getChildren().addAll(
                successLabel,
                savedFile,
                openButton,
                newButton
        );

        uiManager.getContentPane().getChildren().add(completionPane);
    }

    private Task<Void> startBackgroundProcessing() {
        Task<Void> processingTask = new ProcessingTask(logArea, data);

        processingTask.setOnSucceeded(_ -> uiManager.showStep(7));

        processingTask.setOnFailed(_ -> {
            Throwable ex = processingTask.getException();
            logArea.appendText("\n\nERROR: " + ex.getMessage() + Arrays.toString(ex.getStackTrace()));
            new Alert(Alert.AlertType.ERROR, "Processing failed: " + ex.getMessage()).show();
        });

        new Thread(processingTask).start();

        return processingTask;
    }
}
