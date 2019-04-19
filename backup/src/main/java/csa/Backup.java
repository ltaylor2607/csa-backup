package csa;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class Backup  extends Application
{
    FileChooser chooseBackupFiles = new FileChooser();
    DirectoryChooser chooseBackupDirectory = new DirectoryChooser();
    TextArea selectedOutput;
    // Label statusText;
    Label selectedLocation;
    Label locationText;
    TextField selectedSuffix;
    List<File> selectedFiles;
    String location;
    String suffix;

    public static void main( String[] args )
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("File Backup");

        // UI elements for selecting files to backup
        Button selectFiles = new Button();
        selectFiles.setText("Select files for backup...");
        selectFiles.setOnAction(e -> setSelectedFiles(primaryStage));
        selectedOutput = new TextArea();
        selectedOutput.setText("");

        // UI elements for selecting backup location
        locationText = new Label();
        locationText.setText("Select backup location");
        Button selectLocation = new Button();
        selectLocation.setText("...");
        selectLocation.setOnAction(e -> setBackupLocation(primaryStage));
        selectedLocation = new Label();

        // UI elements for setting the suffix to add to the backed up files
        // The default is the current date in the format yyyyMMdd
        Label suffixLabel = new Label();
        suffixLabel.setText("Suffix: ");
        selectedSuffix = new TextField();
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        selectedSuffix.setText(dateFormat.format(date));
        Label suffixHint = new Label();
        suffixHint.setText("*Please select suffix to append to backed up files");

        // UI elements for backing up the selected files
        Button backup = new Button();
        backup.setText("Execute backup");
        backup.setOnAction(e -> backupFiles());
        // statusText = new Label();
        // statusText.setWrapText(true);

        // Setting up the UI
        BorderPane outerLayout = new BorderPane();

        // Setting top section with file selection elements
        VBox fileSelection = new VBox();
        fileSelection.setPadding(new Insets(15, 12, 15, 12));
        fileSelection.setSpacing(10);
        outerLayout.setTop(fileSelection);
        fileSelection.getChildren().addAll(selectFiles, selectedOutput);

        // Center section of the UI
        BorderPane centerLayout = new BorderPane();
        outerLayout.setCenter(centerLayout);
        // Setting top section of the center for location selection
        HBox locationSelection = new HBox();
        locationSelection.setPadding(new Insets(15, 12, 15, 12));
        locationSelection.setSpacing(10);
        centerLayout.setTop(locationSelection);
        locationSelection.getChildren().addAll(locationText, selectLocation);
        // Setting middle of the center section for location output
        HBox locationSelectionOutput = new HBox();
        locationSelectionOutput.setPadding(new Insets(15, 12, 15, 12));
        locationSelectionOutput.setSpacing(10);
        centerLayout.setCenter(locationSelectionOutput);
        locationSelectionOutput.getChildren().addAll(selectedLocation);
        // Setting bottom of the center section for suffix selection information
        BorderPane centerBottomLayout = new BorderPane();
        centerLayout.setBottom(centerBottomLayout);
        HBox suffixSelection = new HBox();
        suffixSelection.setPadding(new Insets(15, 12, 15, 12));
        suffixSelection.setSpacing(10);
        centerBottomLayout.setTop(suffixSelection);
        suffixSelection.getChildren().addAll(suffixLabel, selectedSuffix);
        VBox suffixHintBox = new VBox();
        suffixHintBox.setPadding(new Insets(15, 12, 15, 12));
        suffixHintBox.setSpacing(10);
        centerBottomLayout.setBottom(suffixHintBox);
        suffixHintBox.getChildren().add(suffixHint);

        // Setting bottom section with backup layout
        VBox backupSection = new VBox();
        backupSection.setPadding(new Insets(15, 12, 15, 12));
        backupSection.setSpacing(10);
        outerLayout.setBottom(backupSection);
        backupSection.getChildren().addAll(backup);
        
        Scene scene = new Scene(outerLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method for selecting files to backup
    private void setSelectedFiles(Stage primaryStage) {
        chooseBackupFiles.setTitle("Select files to Backup");

        selectedFiles = chooseBackupFiles.showOpenMultipleDialog(primaryStage);
        
        if (selectedFiles != null) {
            selectedOutput.setText("Files selected for backup [" + selectedFiles.size() + "]:\n" + String.join("\n", selectedFiles.stream().map(f -> f.getName()).collect(Collectors.toList())));
        }
    }

    // Method for selecting the backup location
    private void setBackupLocation(Stage primaryStage) {
        chooseBackupDirectory.setTitle("Select backup location");

        File directory = chooseBackupDirectory.showDialog(primaryStage);

        if (directory != null) {
            location = directory.getAbsolutePath();
            selectedLocation.setText("Backup location: " + location);
        }
    }

    // Method for backing up the selected files to the selected location
    private void backupFiles() {
        suffix = selectedSuffix.getText();
        selectedFiles.forEach(file -> {
            String fullName = file.getName();
            String name = fullName.substring(0, fullName.lastIndexOf("."));
            String extension = fullName.substring(fullName.lastIndexOf("."));
            File backupFile = new File(location + "\\" + name + "_" + suffix + extension);

            try {
                Files.copy(file.toPath(), backupFile.toPath());
                // Reset labels if backup is successful
                selectedOutput.setText("");
                locationText.setText("Please select backup location");
                Alert success = new Alert(AlertType.INFORMATION);
                success.setHeaderText("Success");
                success.setContentText("Successfully backed up selected files");
                success.showAndWait();
            } 
            catch (FileAlreadyExistsException fe) {
                Alert warning = new Alert(AlertType.WARNING);
                warning.setHeaderText("File already exists");
                warning.setContentText("One or more of the files you are trying to back up already exist with the above suffix. Please update the suffix, change the backup location or select different files to back up.");
                warning.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                warning.showAndWait();
            }
            catch (Exception ex) {
                Alert error = new Alert(AlertType.ERROR);
                error.setHeaderText("Error");
                error.setContentText("Failed to back up the selected files");
                error.showAndWait();
            }
        });
    }

}
