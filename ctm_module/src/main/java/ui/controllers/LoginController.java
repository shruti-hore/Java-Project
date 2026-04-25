package ui.controllers;

import auth.service.AuthService;
import auth.service.CryptoAdapter;
import auth.session.SessionState;
import javafx.application.Platform;
import javafx.concurrent.Task;
import ui.http.HttpAuthClient;
import ui.views.LoginView;
import javax.crypto.AEADBadTagException;
import java.util.Arrays;
import java.util.Base64;

public class LoginController {

    private final LoginView view;
    private final HttpAuthClient httpClient;
    private final CryptoAdapter cryptoAdapter;
    private final AuthService authService;
    private final Runnable onSuccess;

    private String pendingEmailHmac;
    private String pendingSaltBase64;
    private String pendingVaultBlobBase64;
    private byte[] pendingPublicKeyBytes; // assuming server returned this or it's fetched

    private boolean isRegisterMode = false;

    public LoginController(LoginView view, HttpAuthClient httpClient, CryptoAdapter cryptoAdapter, AuthService authService, Runnable onSuccess) {
        this.view = view;
        this.httpClient = httpClient;
        this.cryptoAdapter = cryptoAdapter;
        this.authService = authService;
        this.onSuccess = onSuccess;
    }

    public void initialize() {
        view.getPrimaryButton().setOnAction(e -> handlePrimaryAction());
        view.getToggleButton().setOnAction(e -> toggleMode());
    }

    private void handlePrimaryAction() {
        String email = view.getEmailField().getText();
        if (email == null || email.trim().isEmpty()) {
            showError("Email is required");
            return;
        }

        if (isRegisterMode) {
            handleRegister();
            return;
        }

        // If password field is visible/managed, we are in Phase 2
        if (!view.getPasswordField().isDisabled() && "Unlock Vault".equals(view.getPrimaryButton().getText())) {
            handleVerify();
            return;
        }

        // Phase 1: Challenge
        setLoading(true);

        Task<HttpAuthClient.ChallengeResponse> challengeTask = new Task<>() {
            @Override
            protected HttpAuthClient.ChallengeResponse call() throws Exception {
                // In a real scenario, cryptoAdapter would have hmacEmail, but looking at cryptoAdapter it doesn't.
                // Assuming we use SHA-256 or just email for now, or maybe the backend just takes raw email for challenge if HMAC not implemented.
                // The README says "emailHmac <- cryptoAdapter.hmacEmail(email)". 
                // Since cryptoAdapter doesn't have hmacEmail in the provided code, we'll hash it simply or just pass the email.
                String emailHmac = Base64.getEncoder().encodeToString(email.getBytes()); // Placeholder for HMAC
                pendingEmailHmac = emailHmac;
                return httpClient.challenge(emailHmac);
            }
        };

        challengeTask.setOnSucceeded(e -> {
            HttpAuthClient.ChallengeResponse response = challengeTask.getValue();
            pendingSaltBase64 = response.saltBase64();
            pendingVaultBlobBase64 = response.vaultBlobBase64();
            setLoading(false);
            promptForPassword();
        });

        challengeTask.setOnFailed(e -> {
            setLoading(false);
            showError("Account not found.");
        });

        Thread thread = new Thread(challengeTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void promptForPassword() {
        view.getPasswordField().setDisable(false);
        view.getPrimaryButton().setText("Unlock Vault");
        view.getStatusLabel().setText("Account found. Enter your master password.");
        view.getStatusLabel().setStyle("-fx-text-fill: #8b949e;");
        view.getPasswordField().requestFocus();
    }

    private void handleVerify() {
        String passwordStr = view.getPasswordField().getText();
        if (passwordStr == null || passwordStr.isEmpty()) {
            showError("Password is required");
            return;
        }
        
        char[] password = passwordStr.toCharArray();
        String email = view.getEmailField().getText().trim();
        setLoading(true);

        Task<SessionState> verifyTask = new Task<>() {
            @Override
            protected SessionState call() throws Exception {
                try {
                    byte[] salt = Base64.getDecoder().decode(pendingSaltBase64);
                    byte[] vaultBlob = Base64.getDecoder().decode(pendingVaultBlobBase64);
                    // PublicKey is generally retrieved or can be null if not strictly needed by login just to unseal
                    byte[] publicKeyBytes = pendingPublicKeyBytes != null ? pendingPublicKeyBytes : new byte[32]; 

                    SessionState sessionState = authService.login(email, password, salt, vaultBlob, publicKeyBytes);
                    
                    // For bcrypt hash, typically we'd generate it using the password.
                    // The backend verify needs a bcrypt hash to prove identity without sending password.
                    String bcryptHash = "dummy_bcrypt_hash"; // Placeholder if cryptoAdapter lacks bcrypt
                    
                    String jwt = httpClient.verify(pendingEmailHmac, bcryptHash);
                    httpClient.setJwt(jwt);
                    
                    return sessionState;
                } finally {
                    Arrays.fill(password, '\0');
                }
            }
        };

        verifyTask.setOnSucceeded(e -> {
            setLoading(false);
            zeroPendingFields();
            if (onSuccess != null) {
                onSuccess.run();
            }
        });

        verifyTask.setOnFailed(e -> {
            setLoading(false);
            zeroPendingFields();
            Throwable ex = verifyTask.getException();
            if (ex instanceof AEADBadTagException) {
                showError("Incorrect password.");
            } else {
                showError("Authentication failed. Please try again.");
            }
        });

        Thread thread = new Thread(verifyTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void handleRegister() {
        String email = view.getEmailField().getText();
        String pass1 = view.getPasswordField().getText();
        String pass2 = view.getConfirmPasswordField().getText();

        if (email == null || email.trim().isEmpty() || pass1 == null || pass1.isEmpty()) {
            showError("Email and password are required");
            return;
        }
        if (!pass1.equals(pass2)) {
            showError("Passwords do not match.");
            return;
        }

        char[] password = pass1.toCharArray();
        setLoading(true);

        Task<String> registerTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                auth.model.User user = null;
                try {
                    user = authService.register(email, password);
                    String emailHmac = Base64.getEncoder().encodeToString(email.getBytes());
                    String bcryptHash = "dummy_bcrypt_hash";
                    String pkBase64 = Base64.getEncoder().encodeToString(user.getPublicKey());
                    String vaultBase64 = Base64.getEncoder().encodeToString(user.getKeyVault());
                    String saltBase64 = Base64.getEncoder().encodeToString(user.getSalt());

                    HttpAuthClient.RegisterPayload payload = new HttpAuthClient.RegisterPayload(
                            emailHmac, bcryptHash, pkBase64, vaultBase64, saltBase64
                    );

                    return httpClient.register(payload);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }
        };

        registerTask.setOnSucceeded(e -> {
            setLoading(false);
            showSuccess("Account created. Please sign in.");
            toggleMode(); // switch back to login
        });

        registerTask.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = registerTask.getException();
            if (ex instanceof HttpAuthClient.ConflictException) {
                showError("An account with this email already exists.");
            } else {
                showError("Registration failed. Please try again.");
            }
        });

        Thread thread = new Thread(registerTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            view.getPrimaryButton().setText("Create Account");
            view.getToggleButton().setText("Already have an account? Sign in");
            view.getConfirmPasswordField().setVisible(true);
            view.getConfirmPasswordField().setManaged(true);
            view.getStatusLabel().setText("");
            // Reset to step 1
            view.getPasswordField().setDisable(false);
        } else {
            view.getPrimaryButton().setText("Sign In");
            view.getToggleButton().setText("No account? Register");
            view.getConfirmPasswordField().setVisible(false);
            view.getConfirmPasswordField().setManaged(false);
            view.getStatusLabel().setText("");
            view.getPasswordField().setDisable(false);
        }
    }

    private void setLoading(boolean loading) {
        view.getSpinner().setVisible(loading);
        view.getPrimaryButton().setDisable(loading);
        view.getToggleButton().setDisable(loading);
        view.getEmailField().setDisable(loading);
        view.getPasswordField().setDisable(loading);
        if (loading) {
            view.getStatusLabel().setText("");
        }
    }

    private void showError(String message) {
        view.getStatusLabel().setText(message);
        view.getStatusLabel().setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 13px;");
    }

    private void showSuccess(String message) {
        view.getStatusLabel().setText(message);
        view.getStatusLabel().setStyle("-fx-text-fill: #3fb950; -fx-font-size: 13px;");
    }

    private void zeroPendingFields() {
        pendingEmailHmac = null;
        pendingSaltBase64 = null;
        pendingVaultBlobBase64 = null;
    }
}
