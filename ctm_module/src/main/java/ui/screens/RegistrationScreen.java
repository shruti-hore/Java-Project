package ui.screens;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.http.AuthHttpClient;
import auth.service.CryptoAdapter;
import java.util.Base64;

/**
 * Registration screen logic and UI.
 */
public class RegistrationScreen {

    private final Stage stage;
    private final AuthHttpClient httpClient = new AuthHttpClient();
    private CryptoAdapter cryptoAdapter; // Will be injected

    public RegistrationScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        Label usernameError = new Label();
        usernameError.setStyle("-fx-text-fill: red;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");

        Label statusLabel = new Label();

        Button registerBtn = new Button("Register");
        Button backBtn = new Button("Back");

        registerBtn.setOnAction(e -> {
            String email = emailField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            // 1. Validate matching passwords
            if (!password.equals(confirm)) {
                statusLabel.setText("Passwords do not match!");
                return;
            }

            // 2. Validate username
            if (username.matches(".*[@#.].*")) {
                usernameError.setText("Special characters @ # . not allowed");
                return;
            }

            statusLabel.setText("Processing...");
            
            // 3. Crypto & Network call on background thread
            new Thread(() -> {
                try {
                    // Note: In Step 10 we wire up the real CryptoAdapter.
                    // For now, this is a simplified flow.
                    
                    // byte[] salt = cryptoAdapter.generateSalt();
                    // byte[] authProof = cryptoAdapter.deriveAuthProof(password, salt);
                    // String vaultBlob = cryptoAdapter.encryptVault(masterKey, "{}");
                    
                    // Simplified simulation for Step 7:
                    String simulatedSalt = "SIMULATED_SALT";
                    String simulatedVault = "SIMULATED_VAULT";
                    String simulatedAuthProof = password; // Should be hashed!

                    httpClient.register(email, username, simulatedAuthProof, simulatedSalt, simulatedVault)
                        .thenAccept(result -> {
                            Platform.runLater(() -> {
                                if ("SUCCESS".equals(result)) {
                                    new LoginScreen(stage, email).show();
                                } else if ("CONFLICT".equals(result)) {
                                    statusLabel.setText("Email or Username already exists.");
                                } else {
                                    statusLabel.setText("Error: " + result);
                                }
                            });
                        });
                } catch (Exception ex) {
                    Platform.runLater(() -> statusLabel.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });

        backBtn.setOnAction(e -> new EntryScreen(stage).show());

        VBox layout = new VBox(15, title, emailField, usernameField, usernameError, passwordField, confirmField, registerBtn, backBtn, statusLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(400);
        layout.setStyle("-fx-padding: 40;");

        Scene scene = new Scene(layout, 1200, 800);
        stage.setScene(scene);
    }
}
