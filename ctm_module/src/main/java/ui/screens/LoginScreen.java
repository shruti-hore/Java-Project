package ui.screens;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.http.AuthHttpClient;
import java.util.Map;

/**
 * Login screen logic and UI.
 */
public class LoginScreen {

    private final Stage stage;
    private final String identifier;
    private final AuthHttpClient httpClient = new AuthHttpClient();
    
    private String salt;
    private String vaultBlob;

    public LoginScreen(Stage stage, String identifier) {
        this.stage = stage;
        this.identifier = identifier;
    }

    public void show() {
        Label title = new Label("Unlock Vault");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label idLabel = new Label("Identifier: " + identifier);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Master Password");

        Button unlockBtn = new Button("Unlock");
        Button backBtn = new Button("Back");
        Label statusLabel = new Label("Fetching challenge...");

        // 1. Immediate Challenge on load
        new Thread(() -> {
            httpClient.challenge(identifier)
                .thenAccept(resp -> {
                    this.salt = resp.get("salt");
                    this.vaultBlob = resp.get("vault_blob");
                    Platform.runLater(() -> statusLabel.setText("Challenge received. Enter password."));
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("User not found or server error."));
                    return null;
                });
        }).start();

        unlockBtn.setOnAction(e -> {
            String password = passwordField.getText();
            statusLabel.setText("Verifying...");

            new Thread(() -> {
                try {
                    // 2. Re-derive auth proof (Simplified for Step 8)
                    // String authProof = cryptoAdapter.deriveAuthProof(password, salt);
                    String simulatedAuthProof = password; 

                    httpClient.verify(identifier, simulatedAuthProof)
                        .thenAccept(token -> {
                            Platform.runLater(() -> {
                                statusLabel.setText("Login successful!");
                                // Step 9: Store token in SessionState
                                // ui.SessionState.setToken(token);
                                // Navigate to Dashboard (placeholder)
                                System.out.println("JWT Token: " + token);
                            });
                        })
                        .exceptionally(ex -> {
                            Platform.runLater(() -> statusLabel.setText("Incorrect password or error."));
                            return null;
                        });
                } catch (Exception ex) {
                    Platform.runLater(() -> statusLabel.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });

        backBtn.setOnAction(e -> new EntryScreen(stage).show());

        VBox layout = new VBox(15, title, idLabel, passwordField, unlockBtn, backBtn, statusLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(400);
        layout.setStyle("-fx-padding: 40;");

        Scene scene = new Scene(layout, 1200, 800);
        stage.setScene(scene);
    }
}
