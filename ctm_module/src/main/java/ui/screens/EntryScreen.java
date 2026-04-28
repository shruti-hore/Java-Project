package ui.screens;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The first screen the user sees.
 * Handles the initial "identifier" (email or username) entry.
 */
public class EntryScreen {

    private final Stage stage;

    public EntryScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // 1. Create UI components
        Label title = new Label("Secure Task Manager");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label label = new Label("Email or Username:");
        TextField identifierField = new TextField();
        identifierField.setPromptText("e.g. john@example.com or jdoe");
        identifierField.setMaxWidth(300);

        Button signInBtn = new Button("Sign In");
        Button registerBtn = new Button("Register");

        // 2. Set button actions (Navigation)
        signInBtn.setOnAction(e -> {
            String identifier = identifierField.getText();
            if (!identifier.isEmpty()) {
                // Navigate to Login Screen
                new LoginScreen(stage, identifier).show();
            }
        });

        registerBtn.setOnAction(e -> {
            // Navigate to Registration Screen
            new RegistrationScreen(stage).show();
        });

        // 3. Arrange in layout
        HBox buttons = new HBox(10, signInBtn, registerBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, title, label, identifierField, buttons);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");

        // 4. Set scene and show
        Scene scene = new Scene(layout, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }
}
