package ui.views;

import auth.service.AuthService;
import auth.service.CryptoAdapter;
import auth.session.SessionState;
import ui.http.HttpAuthClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.function.Consumer;

public class LoginView extends VBox {

    private final AuthService authService;
    private final CryptoAdapter cryptoAdapter;
    private final HttpAuthClient authClient;
    private final Consumer<SessionState> onLoginSuccess;
    
    private final TextField emailField;
    private final PasswordField passwordField;
    private final Button loginBtn;
    private final Label statusLabel;

    public LoginView(AuthService authService, CryptoAdapter cryptoAdapter, HttpAuthClient authClient, Consumer<SessionState> onLoginSuccess) {
        this.authService = authService;
        this.cryptoAdapter = cryptoAdapter;
        this.authClient = authClient;
        this.onLoginSuccess = onLoginSuccess;

        setAlignment(Pos.CENTER);
        setSpacing(25);
        setMaxSize(400, 480);
        setStyle("-fx-background-color: white; -fx-padding: 50; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");

        Label title = new Label("SECURE TASKER");
        title.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 32px; -fx-font-weight: bold;");

        VBox fields = new VBox(10);
        Label eLbl = new Label("EMAIL ADDRESS");
        eLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        Label pLbl = new Label("PASSWORD");
        pLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");
        
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");

        fields.getChildren().addAll(eLbl, emailField, new Region(), pLbl, passwordField, statusLabel);

        loginBtn = new Button("SIGN IN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.getStyleClass().add("button-primary");
        loginBtn.setPrefHeight(50);

        loginBtn.setOnAction(e -> handleLogin());

        getChildren().addAll(title, new Label("Manage your tasks efficiently"), fields, loginBtn);
    }

    private void handleLogin() {
        String email = emailField.getText();
        char[] password = passwordField.getText().toCharArray();
        
        if (email.isEmpty() || password.length == 0) {
            statusLabel.setText("Email and password are required.");
            return;
        }

        setLoading(true);

        Task<SessionState> loginTask = new Task<>() {
            @Override
            protected SessionState call() throws Exception {
                byte[] masterKey = null;
                byte[] authKey = null;
                byte[] vaultKey = null;
                char[] passwordCopy = java.util.Arrays.copyOf(password, password.length);

                try {
                    // Phase 1: Challenge
                    Map<String, String> challengeResult = authClient.challenge(email).get();
                    byte[] salt = java.util.Base64.getDecoder().decode(challengeResult.get("saltBase64"));
                    byte[] vaultBlob = java.util.Base64.getDecoder().decode(challengeResult.get("vaultBlobBase64"));

                    // Phase 2: Verify
                    // 1. Derive Master Key
                    masterKey = cryptoAdapter.deriveMasterKey(passwordCopy, salt);

                    // 2. Derive sub-keys
                    authKey = cryptoAdapter.deriveAuthKey(masterKey);
                    vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

                    // 3. Compute proof (same derivation as registration)
                    String authProof = cryptoAdapter.computeMasterKeyProof(authKey);

                    // 4. Verify with server
                    Map<String, String> verifyResult = authClient.verify(email, authProof).get();
                    String jwt = verifyResult.get("token");
                    byte[] publicKeyBytes = java.util.Base64.getDecoder().decode(verifyResult.get("publicKeyBase64"));

                    // 5. Unseal vault locally
                    byte[] privateKeyBytes = cryptoAdapter.decryptVault(vaultBlob, vaultKey);
                    java.security.PrivateKey privateKey = cryptoAdapter.loadPrivateKey(privateKeyBytes);
                    java.util.Arrays.fill(privateKeyBytes, (byte) 0);

                    // 6. Complete local login
                    return new SessionState(email, jwt, authKey, privateKey, publicKeyBytes);
                } finally {
                    if (masterKey != null) java.util.Arrays.fill(masterKey, (byte) 0);
                    if (vaultKey != null) java.util.Arrays.fill(vaultKey, (byte) 0);
                    java.util.Arrays.fill(password, '\0');
                    java.util.Arrays.fill(passwordCopy, '\0');
                }
            }
        };

        loginTask.setOnSucceeded(e -> {
            setLoading(false);
            onLoginSuccess.accept(loginTask.getValue());
        });

        loginTask.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = loginTask.getException();
            if (ex.getCause() instanceof javax.crypto.AEADBadTagException || ex.getMessage().contains("401")) {
                statusLabel.setText("Incorrect password.");
            } else {
                statusLabel.setText("Login failed: " + ex.getMessage());
            }
        });

        new Thread(loginTask).start();
    }

    private void setLoading(boolean loading) {
        loginBtn.setDisable(loading);
        emailField.setEditable(!loading);
        passwordField.setEditable(!loading);
        if (loading) statusLabel.setText("");
    }
}
