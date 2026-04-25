package ui.views;

import auth.model.User;
import auth.service.AuthService;
import auth.service.CryptoAdapter;
import ui.http.HttpAuthClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Arrays;
import java.util.Base64;

public class RegisterView extends VBox {

    private final AuthService authService;
    private final CryptoAdapter cryptoAdapter;
    private final HttpAuthClient authClient;
    private final Runnable onBackToLogin;
    
    private final TextField emailField;
    private final PasswordField passwordField;
    private final Button registerBtn;
    private final Label statusLabel;

    public RegisterView(AuthService authService, CryptoAdapter cryptoAdapter, HttpAuthClient authClient, Runnable onBackToLogin) {
        this.authService = authService;
        this.cryptoAdapter = cryptoAdapter;
        this.authClient = authClient;
        this.onBackToLogin = onBackToLogin;

        setAlignment(Pos.CENTER);
        setSpacing(25);
        setMaxSize(400, 550);
        setStyle("-fx-background-color: white; -fx-padding: 50; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");

        Label title = new Label("CREATE ACCOUNT");
        title.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 28px; -fx-font-weight: bold;");

        VBox fields = new VBox(10);
        
        Label eLbl = new Label("EMAIL ADDRESS");
        eLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        Label pLbl = new Label("CHOOSE PASSWORD");
        pLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Min 8 characters");
        passwordField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");
        
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");

        fields.getChildren().addAll(eLbl, emailField, new Region(), pLbl, passwordField, statusLabel);

        registerBtn = new Button("CREATE ACCOUNT");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.getStyleClass().add("button-primary");
        registerBtn.setPrefHeight(50);

        Button backBtn = new Button("Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-cursor: hand;");
        backBtn.setOnAction(e -> onBackToLogin.run());

        registerBtn.setOnAction(e -> handleRegister());

        getChildren().addAll(title, new Label("Join the secure task manager"), fields, registerBtn, backBtn);
    }

    private void handleRegister() {
        String email = emailField.getText();
        char[] password = passwordField.getText().toCharArray();
        
        if (email.isEmpty() || password.length < 8) {
            statusLabel.setText("Valid email and 8+ char password required.");
            return;
        }

        setLoading(true);

        Task<Void> regTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                byte[] masterKey = null;
                byte[] authKey = null;
                char[] passwordCopy = Arrays.copyOf(password, password.length);

                try {
                    // 1. Generate salt and derive master key
                    byte[] salt = new byte[16];
                    new java.security.SecureRandom().nextBytes(salt);
                    
                    masterKey = cryptoAdapter.deriveMasterKey(passwordCopy, salt);

                    // 2. Derive sub-keys (from masterKey before it's cleared)
                    authKey = cryptoAdapter.deriveAuthKey(masterKey);
                    byte[] vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

                    // 3. Compute Proof (from authKey branch, NOT masterKey directly)
                    String authProof = cryptoAdapter.computeMasterKeyProof(authKey);

                    // 4. Generate Key Pair and Seal Vault
                    crypto.api.X25519KeyPair keyPair = cryptoAdapter.generateKeyPair();
                    byte[] publicKeyBytes = cryptoAdapter.extractPublicKeyBytes(keyPair.publicKey());
                    byte[] privateKeyBytes = keyPair.privateKeyBytes();
                    byte[] vaultBlob = cryptoAdapter.encryptVault(privateKeyBytes, vaultKey);
                    
                    // Cleanup private components
                    java.util.Arrays.fill(privateKeyBytes, (byte) 0);
                    java.util.Arrays.fill(vaultKey, (byte) 0);

                    // 5. POST to Server
                    authClient.register(
                        email,
                        authProof,
                        java.util.Base64.getEncoder().encodeToString(publicKeyBytes),
                        java.util.Base64.getEncoder().encodeToString(vaultBlob),
                        java.util.Base64.getEncoder().encodeToString(salt)
                    ).get();

                    return null;
                } finally {
                    if (masterKey != null) java.util.Arrays.fill(masterKey, (byte) 0);
                    if (authKey != null) java.util.Arrays.fill(authKey, (byte) 0);
                    java.util.Arrays.fill(password, '\0');
                    java.util.Arrays.fill(passwordCopy, '\0');
                }
            }
        };

        regTask.setOnSucceeded(e -> {
            setLoading(false);
            onBackToLogin.run();
        });

        regTask.setOnFailed(e -> {
            setLoading(false);
            statusLabel.setText("Registration failed: " + regTask.getException().getMessage());
        });

        new Thread(regTask).start();
    }

    private void setLoading(boolean loading) {
        registerBtn.setDisable(loading);
        emailField.setEditable(!loading);
        passwordField.setEditable(!loading);
        if (loading) statusLabel.setText("");
    }
}
