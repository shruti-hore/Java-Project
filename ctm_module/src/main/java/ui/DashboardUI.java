// to run : mvn clean javafx:run

package ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import java.io.File;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.AEADBadTagException;
import java.nio.file.Paths;

import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import exceptions.*;
import service.TaskService;
import service.AuthService;
import service.TeamService;
import ui.views.DashboardView;
import ui.views.MyTasksView;
import ui.views.SidebarView;
import utils.ValidationUtils;
import utils.UserSession;

import client.crypto.DocumentCryptoService;
import client.crypto.NonceCounterStore;
import client.sync.LocalCache;
import client.sync.ConflictResolver;
import client.sync.SyncManager;

public class DashboardUI extends Application {

    private StackPane mainStack;
    private BorderPane mainRoot;
    
    // Secure dependencies — injected at start()
    private ui.http.HttpAuthClient httpClient;
    private auth.service.CryptoAdapter cryptoAdapter;
    private auth.service.AuthService secureAuthService;
    private auth.session.SessionState sessionState;        // null until login succeeds
    private client.service.EncryptedTaskService encryptedTaskService;
    private client.sync.SyncManager syncManager;

    // Keep these field declarations — their internals change in later steps
    private service.TaskService taskService;
    private service.TeamService teamService;

    private ObservableList<client.model.Task> taskList;

    private DashboardView dashboardView;
    private MyTasksView myTasksView;
    private ui.views.CalendarView calendarView;
    private ui.views.WorkspaceView workspaceView;
    private String selectedTeamId;
    
    private boolean isLoginMode = true;
    private enum LoginPhase { EMAIL_ENTRY, PASSWORD_ENTRY }
    private LoginPhase loginPhase = LoginPhase.EMAIL_ENTRY;
    private String pendingEmailHmac;
    private String pendingSaltBase64;
    private String pendingVaultBlobBase64;
    private String pendingPublicKeyBase64;

    private Label loginErrorLabel = new Label();
    private PasswordField confirmPassField = new PasswordField();
    private Label confirmPassLbl = new Label("CONFIRM PASSWORD");

    @Override
    public void start(Stage stage) {
        this.httpClient = new ui.http.HttpAuthClient(java.net.http.HttpClient.newHttpClient(), "http://localhost:8080");
        this.cryptoAdapter = new auth.service.CryptoAdapter(new crypto.internal.CryptoServiceImpl());
        this.secureAuthService = new auth.service.AuthService(cryptoAdapter);
        this.teamService = new service.TeamService(httpClient);
        
        mainStack = new StackPane();
        Scene scene = new Scene(mainStack, 1200, 800);
        try {
            java.net.URL cssUrl = getClass().getResource("/light_style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                File cssFile = new File("src/main/resources/light_style.css");
                if (cssFile.exists()) {
                    scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        stage.setScene(scene);
        stage.setTitle("Secure Task Manager");
        stage.centerOnScreen();
        stage.show();

        showLoginScreen();
    }

    private void showLoginScreen() {
        mainStack.getChildren().clear();
        mainStack.setStyle("-fx-background-color: #f5f6fa;");
        
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setMaxSize(400, 580);
        loginBox.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");

        Label title = new Label(isLoginMode ? "SECURE SIGN IN" : "CREATE ACCOUNT");
        title.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 28px; -fx-font-weight: bold;");

        loginErrorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");
        loginErrorLabel.setWrapText(true);
        loginErrorLabel.setText("");

        VBox fields = new VBox(10);
        Label eLbl = new Label("EMAIL ADDRESS");
        eLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        Label pLbl = new Label("PASSWORD");
        pLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        confirmPassLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        confirmPassField.setPromptText("Confirm your password");
        confirmPassField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        fields.getChildren().addAll(eLbl, emailField, pLbl, passField);
        if (!isLoginMode) {
            fields.getChildren().addAll(confirmPassLbl, confirmPassField);
        }

        Button actionBtn = new Button(isLoginMode ? "SIGN IN" : "REGISTER");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.getStyleClass().add("button-primary");
        actionBtn.setPrefHeight(50);

        actionBtn.setOnAction(e -> {
            String email = emailField.getText();
            char[] password = passField.getText().toCharArray();
            loginErrorLabel.setText("");

            if (!isLoginMode) {
                char[] confirmPassword = confirmPassField.getText().toCharArray();
                handleRegistration(email, password, confirmPassword, actionBtn);
                return;
            }

            // Safety: If email changed, always go back to Phase 1
            if (pendingEmailHmac != null && !pendingEmailHmac.equals(email)) {
                loginPhase = LoginPhase.EMAIL_ENTRY;
                actionBtn.setText("Sign In");
            }

            if (loginPhase == LoginPhase.EMAIL_ENTRY) {
                handleLoginPhase1(email, actionBtn);
            } else {
                handleLoginPhase2(email, password, actionBtn);
            }
        });

        Button toggleBtn = new Button(isLoginMode ? "Don't have an account? Register" : "Already have an account? Sign In");
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-cursor: hand;");
        toggleBtn.setOnAction(e -> {
            isLoginMode = !isLoginMode;
            showLoginScreen();
        });

        loginBox.getChildren().addAll(title, loginErrorLabel, fields, actionBtn, toggleBtn);
        mainStack.getChildren().add(loginBox);
    }

    private void handleLoginPhase1(String email, Button actionBtn) {
        if (email == null || email.trim().isEmpty()) {
            loginErrorLabel.setText("Email cannot be empty");
            return;
        }

        setLoginLoading(true, actionBtn);
        Task<java.util.Map<String, String>> task = new Task<>() {
            @Override
            protected java.util.Map<String, String> call() throws Exception {
                // Step 1: In a real system, we'd HMAC the email. For now, we use the plaintext email
                // to match the server implementation we worked on.
                // String emailHmac = cryptoAdapter.computeStableEmailHash(email); 
                // Actually, the server expects the plaintext email for the challenge in Part 6
                return httpClient.challenge(email).get();
            }
        };

        task.setOnSucceeded(e -> {
            java.util.Map<String, String> response = task.getValue();
            pendingEmailHmac = email; 
            pendingSaltBase64 = response.get("saltBase64");
            pendingVaultBlobBase64 = response.get("vaultBlobBase64");
            pendingPublicKeyBase64 = response.get("publicKeyBase64");
            
            loginPhase = LoginPhase.PASSWORD_ENTRY;
            setLoginLoading(false, actionBtn);
            loginErrorLabel.setText("Account found. Enter your master password.");
            loginErrorLabel.setStyle("-fx-text-fill: #4f46e5;");
            actionBtn.setText("Unlock");
        });

        task.setOnFailed(e -> {
            setLoginLoading(false, actionBtn);
            loginErrorLabel.setText("No account found or server error.");
            loginErrorLabel.setStyle("-fx-text-fill: #ef4444;");
        });

        new Thread(task).start();
    }

    private void handleLoginPhase2(String email, char[] password, Button actionBtn) {
        if (password == null || password.length == 0) {
            loginErrorLabel.setText("Password cannot be empty");
            return;
        }

        setLoginLoading(true, actionBtn);
        Task<auth.session.SessionState> task = new Task<>() {
            @Override
            protected auth.session.SessionState call() throws Exception {
                char[] passwordClone = null;
                byte[] masterKey = null;
                try {
                    byte[] salt = Base64.getDecoder().decode(pendingSaltBase64);
                    byte[] vaultBlob = Base64.getDecoder().decode(pendingVaultBlobBase64);
                    
                    passwordClone = password.clone();
                    
                    // Derivations to get Auth Key for verify
                    masterKey = cryptoAdapter.deriveMasterKey(passwordClone, salt);
                    String authProof = cryptoAdapter.computeMasterKeyProof(masterKey);
                    
                    System.out.println("[DEBUG] Login Phase 2: computed authProof: " + authProof);
                    System.out.println("[DEBUG] Login Phase 2: using saltBase64: " + pendingSaltBase64);

                    // Verify with server to get JWT
                    java.util.Map<String, String> verifyResp = httpClient.verify(email, authProof).get();
                    String jwt = verifyResp.get("jwt");
                    httpClient.setJwt(jwt);
                    
                    // Fetch public key if we didn't store it from challenge (but we should have)
                    // Let's use a dummy or retrieve it. 
                    // Since I updated challenge, I need to store it in Phase 1.
                    
                    // Actually, let's just use the one from the JWT or verify resp?
                    // I'll update Phase 1 to store it.
                    
                    byte[] publicKeyBytes = Base64.getDecoder().decode(pendingPublicKeyBase64);
                    
                    auth.session.SessionState session = secureAuthService.login(email, password, salt, vaultBlob, publicKeyBytes, jwt);
                    
                    return session;
                } finally {
                    java.util.Arrays.fill(password, '\0');
                    if (passwordClone != null) java.util.Arrays.fill(passwordClone, '\0');
                    if (masterKey != null) java.util.Arrays.fill(masterKey, (byte) 0);
                }
            }
        };

        task.setOnSucceeded(e -> {
            sessionState = task.getValue();
            loginPhase = LoginPhase.EMAIL_ENTRY;
            pendingEmailHmac = pendingSaltBase64 = pendingVaultBlobBase64 = null;
            setLoginLoading(false, actionBtn);
            
            // Step 3: Team Selection
            showTeamSelectionScreen();
        });

        task.setOnFailed(e -> {
            setLoginLoading(false, actionBtn);
            Throwable ex = task.getException();
            
            // Reset phase on fatal errors so user can retry or change email
            loginPhase = LoginPhase.EMAIL_ENTRY;
            actionBtn.setText("Sign In");
            
            if (ex.getCause() instanceof AEADBadTagException || ex instanceof AEADBadTagException) {
                loginErrorLabel.setText("Incorrect password.");
            } else {
                loginErrorLabel.setText("Login failed: " + ex.getMessage());
            }
            loginErrorLabel.setStyle("-fx-text-fill: #ef4444;");
        });

        new Thread(task).start();
    }

    private void handleRegistration(String email, char[] password, char[] confirmPassword, Button actionBtn) {
        try {
            validateInputs(email, new String(password));
            if (!java.util.Arrays.equals(password, confirmPassword)) {
                loginErrorLabel.setText("Passwords do not match!");
                return;
            }
        } catch (Exception ex) {
            loginErrorLabel.setText(ex.getMessage());
            return;
        } finally {
            java.util.Arrays.fill(confirmPassword, '\0');
        }

        setLoginLoading(true, actionBtn);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                char[] passwordClone = null;
                byte[] masterKey = null;
                try {
                    passwordClone = password.clone();
                    auth.model.User user = secureAuthService.register(email, password);
                    
                    masterKey = cryptoAdapter.deriveMasterKey(passwordClone, user.getSalt());
                    String authProof = cryptoAdapter.computeMasterKeyProof(masterKey);
                    
                    System.out.println("[DEBUG] Registration: computed authProof: " + authProof);
                    System.out.println("[DEBUG] Registration: using saltBase64: " + Base64.getEncoder().encodeToString(user.getSalt()));

                    httpClient.register(
                        user.getEmail(),
                        authProof,
                        Base64.getEncoder().encodeToString(user.getPublicKey()),
                        Base64.getEncoder().encodeToString(user.getKeyVault()),
                        Base64.getEncoder().encodeToString(user.getSalt())
                    ).get();
                    return null;
                } finally {
                    java.util.Arrays.fill(password, '\0');
                    if (passwordClone != null) java.util.Arrays.fill(passwordClone, '\0');
                    if (masterKey != null) java.util.Arrays.fill(masterKey, (byte) 0);
                }
            }
        };

        task.setOnSucceeded(e -> {
            setLoginLoading(false, actionBtn);
            isLoginMode = true;
            showLoginScreen();
            loginErrorLabel.setText("Registration successful! Please sign in.");
            loginErrorLabel.setStyle("-fx-text-fill: #10b981;");
        });

        task.setOnFailed(e -> {
            setLoginLoading(false, actionBtn);
            loginErrorLabel.setText("Registration failed: " + task.getException().getMessage());
            loginErrorLabel.setStyle("-fx-text-fill: #ef4444;");
        });

        new Thread(task).start();
    }

    private void setLoginLoading(boolean loading, Button actionBtn) {
        actionBtn.setDisable(loading);
        if (loading) {
            loginErrorLabel.setText("Processing...");
            loginErrorLabel.setStyle("-fx-text-fill: #6b7280;");
        }
    }

    private void showTeamSelectionScreen() {
        showWorkspaceSelection();
    }

    private void validateInputs(String email, String password)
            throws EmptyFieldException, InvalidEmailException, WeakPasswordException {
        if (email == null || email.trim().isEmpty()) {
            throw new EmptyFieldException("Email cannot be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new EmptyFieldException("Password cannot be empty");
        }

        String trimmedEmail = email.trim();
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$";
        if (!trimmedEmail.matches(emailRegex)) {
            throw new InvalidEmailException("Enter a valid email address ending in .com");
        }

        if (password.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters");
        }

        String passRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s]).{8,}$";
        if (!password.matches(passRegex)) {
            throw new WeakPasswordException("Include uppercase, lowercase, number and special character");
        }
    }

    private void initializeDashboard() {
        initializeMainApp(null);
    }

    private void showWorkspaceSelection() {
        if (sessionState == null) return;
        String userEmail = sessionState.getUserId();
        
        Task<List<model.Team>> task = new Task<>() {
            @Override
            protected List<model.Team> call() throws Exception {
                return teamService.getTeamsForUser(userEmail);
            }
        };

        task.setOnSucceeded(e -> {
            List<model.Team> teams = task.getValue();
            workspaceView = new ui.views.WorkspaceView(
                teams,
                this::handleTeamSelected,
                this::handleCreateTeam,
                this::handleJoinTeam
            );

            if (mainRoot == null) {
                initializeMainApp(null);
            }
            mainRoot.setCenter(workspaceView);
        });

        task.setOnFailed(e -> showError("Failed to load workspaces: " + task.getException().getMessage()));

        new Thread(task).start();
    }

    private void handleTeamSelected(model.Team team) {
        this.selectedTeamId = team.getId();
        
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                String envelopeBase64 = httpClient.fetchTeamKeyEnvelope(team.getId());
                byte[] ownerPublicKeyBytes = httpClient.fetchOwnerPublicKey(team.getId());
                
                java.security.PublicKey ownerPublicKey = cryptoAdapter.loadPublicKey(ownerPublicKeyBytes);
                
                byte[] teamKeyBytes = cryptoAdapter.unwrapTeamKey(
                    Base64.getDecoder().decode(envelopeBase64),
                    ownerPublicKey,
                    sessionState.getX25519PrivateKey()
                );
                
                sessionState.addTeamKey(team.getId(), teamKeyBytes);
                java.util.Arrays.fill(teamKeyBytes, (byte) 0);

                // Step 6 & 7: Initialize secure client engines
                NonceCounterStore counterStore = new NonceCounterStore(Paths.get("nonce_counters.txt"));
                counterStore.load();
                DocumentCryptoService docCrypto = new DocumentCryptoService(cryptoAdapter, counterStore);
                LocalCache localCache = new LocalCache("local_cache.db");
                
                encryptedTaskService = new client.service.EncryptedTaskService(
                    docCrypto, sessionState, httpClient.getClient(), httpClient.getBaseUrl()
                );
                taskService = new service.TaskService(encryptedTaskService, sessionState, selectedTeamId);
                
                syncManager = new SyncManager(
                    localCache,
                    new ConflictResolver(),
                    encryptedTaskService,
                    sessionState,
                    httpClient.getClient(),
                    httpClient.getBaseUrl(),
                    pair -> Platform.runLater(() -> showConflictDialog(pair)),
                    () -> Platform.runLater(() -> setSyncIndicator(true)),
                    () -> Platform.runLater(() -> setSyncIndicator(false))
                );
                syncManager.start();

                return null;
            }
        };

        task.setOnSucceeded(e -> initializeMainApp(team));
        task.setOnFailed(e -> showError("Failed to unlock workspace: " + task.getException().getMessage()));

        new Thread(task).start();
    }

    private void setSyncIndicator(boolean syncing) {
        // Update UI to show sync status
        System.out.println("Syncing: " + syncing);
    }

    private void showConflictDialog(client.sync.ConflictPair pair) {
        // Show conflict resolution UI
        System.out.println("Conflict detected for doc: " + pair.docUuid());
    }

    private void handleCreateTeam() {
        VBox form = new VBox(20);
        form.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24;");
        form.setMaxSize(400, 450);
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Create Workspace");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        TextField nameField = new TextField();
        nameField.setPromptText("Team Name");
        nameField.setStyle("-fx-background-color: #f9fafb; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button submit = new Button("CREATE TEAM");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14; -fx-background-radius: 12; -fx-cursor: hand;");
        
        submit.setOnAction(e -> {
            String name = nameField.getText();
            if (name == null || name.trim().isEmpty()) {
                statusLabel.setText("Name cannot be empty");
                return;
            }
            
            submit.setDisable(true);
            statusLabel.setText("Creating workspace...");
            
            javafx.concurrent.Task<ui.http.HttpAuthClient.CreateWorkspaceResponse> task = new javafx.concurrent.Task<>() {
                @Override
                protected ui.http.HttpAuthClient.CreateWorkspaceResponse call() throws Exception {
                    byte[] teamKey = new byte[32];
                    java.security.SecureRandom.getInstanceStrong().nextBytes(teamKey);
                    
                    try {
                        String pubKeyBase64 = Base64.getEncoder().encodeToString(sessionState.getX25519PublicKeyBytes());
                        ui.http.HttpAuthClient.CreateWorkspaceResponse response = httpClient.createWorkspace(name, pubKeyBase64).get();
                        
                        byte[] envelope = cryptoAdapter.wrapTeamKey(
                            teamKey,
                            sessionState.getX25519PublicKey(),
                            sessionState.getX25519PrivateKey()
                        );
                        
                        httpClient.postKeyEnvelope(response.teamId(), sessionState.getUserId(), Base64.getEncoder().encodeToString(envelope)).get();
                        
                        sessionState.addTeamKey(response.teamId(), teamKey);
                        return response;
                    } finally {
                        java.util.Arrays.fill(teamKey, (byte) 0);
                    }
                }
            };

            task.setOnSucceeded(ev -> {
                ui.http.HttpAuthClient.CreateWorkspaceResponse res = task.getValue();
                form.getChildren().clear();
                
                Label successTitle = new Label("Workspace Created!");
                successTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
                
                Label codeLabel = new Label(res.workspaceCode());
                codeLabel.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 24px; -fx-text-fill: #4f46e5; -fx-font-weight: bold;");
                
                Label hint = new Label("Share this code with your team.");
                hint.setStyle("-fx-text-fill: #6b7280;");
                
                Button done = new Button("DONE");
                done.setMaxWidth(Double.MAX_VALUE);
                done.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14; -fx-background-radius: 12;");
                done.setOnAction(a -> {
                    hideOverlay();
                    showWorkspaceSelection();
                });
                
                form.getChildren().addAll(successTitle, new Label("Invite Code:"), codeLabel, hint, done);
            });

            task.setOnFailed(ev -> {
                submit.setDisable(false);
                statusLabel.setText("Failed: " + task.getException().getMessage());
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
            });

            new Thread(task).start();
        });

        form.getChildren().addAll(title, nameField, statusLabel, submit);
        showOverlay(form);
    }

    private void handleJoinTeam() {
        VBox form = new VBox(20);
        form.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24;");
        form.setMaxSize(400, 300);
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Join Workspace");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        TextField idField = new TextField();
        idField.setPromptText("Enter Team Code");
        idField.setStyle("-fx-background-color: #f9fafb; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button submit = new Button("JOIN TEAM");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14; -fx-background-radius: 12; -fx-cursor: hand;");
        
        submit.setOnAction(e -> {
            String code = idField.getText();
            if (code == null || code.trim().isEmpty()) {
                statusLabel.setText("Code cannot be empty");
                return;
            }
            
            submit.setDisable(true);
            statusLabel.setText("Joining workspace...");
            
            javafx.concurrent.Task<ui.http.HttpAuthClient.JoinWorkspaceResponse> task = new javafx.concurrent.Task<>() {
                @Override
                protected ui.http.HttpAuthClient.JoinWorkspaceResponse call() throws Exception {
                    return httpClient.joinWorkspace(code.trim()).get();
                }
            };

            task.setOnSucceeded(ev -> {
                ui.http.HttpAuthClient.JoinWorkspaceResponse res = task.getValue();
                if ("PENDING".equals(res.status())) {
                    statusLabel.setText("Request sent. Waiting for owner approval.");
                    statusLabel.setStyle("-fx-text-fill: #6b7280;");
                    submit.setText("CLOSE");
                    submit.setDisable(false);
                    submit.setOnAction(a -> {
                        hideOverlay();
                        showWorkspaceSelection();
                    });
                } else {
                    hideOverlay();
                    showWorkspaceSelection();
                }
            });

            task.setOnFailed(ev -> {
                submit.setDisable(false);
                statusLabel.setText("Failed: " + task.getException().getMessage());
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
            });

            new Thread(task).start();
        });

        form.getChildren().addAll(title, idField, statusLabel, submit);
        showOverlay(form);
    }

    private void initializeMainApp(model.Team selectedTeam) {
        mainStack.getChildren().clear();
        String userEmail = sessionState.getUserId();
        
        mainRoot = new BorderPane();
        
        SidebarView sidebar = new SidebarView(viewKey -> {
            switch(viewKey) {
                case "TEAMS": showWorkspaceSelection(); break;
                case "DASHBOARD": 
                    if (selectedTeam == null) showError("Please select a workspace first!");
                    else mainRoot.setCenter(dashboardView); 
                    break;
                case "KANBAN": 
                    if (selectedTeam == null) showError("Please select a workspace first!");
                    else { mainRoot.setCenter(myTasksView); myTasksView.refresh(); }
                    break;
                case "CALENDAR": 
                    if (selectedTeam == null) {
                        ObservableList<client.model.Task> allTasks = FXCollections.observableArrayList(taskService.getAllTasks(userEmail, null));
                        ui.views.CalendarView globalCal = new ui.views.CalendarView(allTasks);
                        mainRoot.setCenter(globalCal);
                    } else { 
                        mainRoot.setCenter(calendarView); 
                        calendarView.refresh(); 
                    }
                    break;
                case "LOGOUT": handleLogout(); break;
                default: showError("Module coming soon!");
            }
        }, selectedTeam != null);

        sidebar.getAddTaskBtn().setDisable(selectedTeam == null);
        sidebar.getAddTaskBtn().setOnAction(e -> handleEditAction(null));

        if (selectedTeam != null) {
            this.selectedTeamId = selectedTeam.getId();
            taskList = FXCollections.observableArrayList(taskService.getAllTasks(userEmail, selectedTeamId));
            dashboardView = new DashboardView(taskList, selectedTeam);
            myTasksView = new MyTasksView(taskService, taskList, this::handleEditAction, (client.model.Task t) -> {
                showConfirmation("Delete Task", "Are you sure you want to delete this task?", () -> {
                    taskService.deleteTask(t.getId());
                    taskList.remove(t);
                    myTasksView.refresh();
                });
            });
            calendarView = new ui.views.CalendarView(taskList);

            dashboardView.getAddTaskButton().setOnAction(e -> handleEditAction(null));
            dashboardView.getViewTasksButton().setOnAction(e -> {
                mainRoot.setCenter(myTasksView);
                myTasksView.refresh();
            });

            mainRoot.setCenter(dashboardView);

            // Fetch workspace code in background (Step 8)
            javafx.concurrent.Task<String> codeTask = new javafx.concurrent.Task<>() {
                @Override
                protected String call() throws Exception {
                    return httpClient.fetchWorkspaceCode(selectedTeamId);
                }
            };
            codeTask.setOnSucceeded(ev -> {
                String code = codeTask.getValue();
                dashboardView.setTeamCode(code);
            });
            new Thread(codeTask).start();
            
        } else {
            showWorkspaceSelection();
        }

        mainRoot.setLeft(sidebar);
        mainStack.getChildren().add(mainRoot);
    }

    private void handleEditAction(client.model.Task t) {
        if (t == null)
            showAddTaskDialog();
        else
            showEditDialog(t);
    }

    private void showAddTaskDialog() {
        TextField tIn = new TextField();
        tIn.setPromptText("Title");
        TextField dIn = new TextField();
        dIn.setPromptText("Description");
        DatePicker dateIn = new DatePicker(LocalDate.now());
        dateIn.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("High", "Medium", "Low"));
        pIn.setValue("Low");
        pIn.setMaxWidth(Double.MAX_VALUE);

        Button save = new Button("Add Task");
        save.setStyle(
                "-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        save.setMaxWidth(Double.MAX_VALUE);

        save.setOnAction(e -> {
            String title = tIn.getText();
            String date = dateIn.getValue().toString();
            if (!ValidationUtils.isValidTaskTitle(title)) {
                showError("Title cannot be empty!");
                return;
            }
            if (!ValidationUtils.isFutureOrPresentDate(date)) {
                showError("Date cannot be in the past!");
                return;
            }

            client.model.Task newTask = new client.model.Task(null, title, dIn.getText(), date, false, "DEADLINE", pIn.getValue(),
                    sessionState.getUserId(), selectedTeamId);
            taskService.addTask(newTask);
            taskList.add(newTask);
            myTasksView.refresh();
            hideOverlay();
        });

        VBox layout = new VBox(15, new Label("NEW TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle(
                "-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");
        layout.setMaxSize(400, 450);
        showOverlay(layout);
    }

    private void showEditDialog(client.model.Task t) {
        TextField tIn = new TextField(t.getTitle());
        TextField dIn = new TextField(t.getDescription());
        DatePicker dateIn = new DatePicker(LocalDate.parse(t.getDeadline()));
        dateIn.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("High", "Medium", "Low"));
        pIn.setValue(t.getPriority());
        pIn.setMaxWidth(Double.MAX_VALUE);

        Button save = new Button("Save Changes");
        save.setStyle(
                "-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        save.setMaxWidth(Double.MAX_VALUE);

        save.setOnAction(e -> {
            String title = tIn.getText();
            String date = dateIn.getValue().toString();
            if (!ValidationUtils.isValidTaskTitle(title)) {
                showError("Title cannot be empty!");
                return;
            }
            if (!ValidationUtils.isFutureOrPresentDate(date)) {
                showError("Date cannot be in the past!");
                return;
            }

            t.setTitle(title);
            t.setDescription(dIn.getText());
            t.setDeadline(date);
            t.setPriority(pIn.getValue());
            taskService.updateTask(t);
            myTasksView.refresh();
            hideOverlay();
        });

        VBox layout = new VBox(15, new Label("EDIT TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle(
                "-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");
        layout.setMaxSize(400, 450);
        showOverlay(layout);
    }

    private void showOverlay(Node content) {
        Region glassPane = new Region();
        glassPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");
        glassPane.setOnMouseClicked(e -> hideOverlay());

        VBox container = new VBox(content);
        container.setAlignment(Pos.CENTER);
        container.setPickOnBounds(false);
        content.setEffect(new DropShadow(30, Color.BLACK));

        mainStack.getChildren().addAll(glassPane, container);
    }

    private void hideOverlay() {
        if (mainStack.getChildren().size() > 1) {
            mainStack.getChildren().remove(mainStack.getChildren().size() - 1);
            mainStack.getChildren().remove(mainStack.getChildren().size() - 1);
        }
    }

    private void showError(String msg) {
        VBox box = new VBox(20);
        box.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24;");
        box.setMaxSize(400, 200);
        box.setAlignment(Pos.CENTER);

        Label title = new Label("Error");
        title.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label content = new Label(msg);
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #374151;");

        Button ok = new Button("OK");
        ok.setPrefWidth(100);
        ok.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        ok.setOnAction(e -> hideOverlay());

        box.getChildren().addAll(title, content, ok);
        showOverlay(box);
    }

    private void showConfirmation(String titleStr, String contentStr, Runnable onConfirm) {
        VBox box = new VBox(20);
        box.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24;");
        box.setMaxSize(400, 200);
        box.setAlignment(Pos.CENTER);

        Label title = new Label(titleStr);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        Label content = new Label(contentStr);
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #374151;");

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        
        Button cancel = new Button("CANCEL");
        cancel.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        cancel.setOnAction(e -> hideOverlay());

        Button confirm = new Button("CONFIRM");
        confirm.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        confirm.setOnAction(e -> {
            hideOverlay();
            onConfirm.run();
        });

        buttons.getChildren().addAll(cancel, confirm);
        box.getChildren().addAll(title, content, buttons);
        showOverlay(box);
    }

    private void handleLogout() {
        if (syncManager != null) {
            syncManager.stop();
            syncManager = null;
        }
        if (sessionState != null) {
            sessionState.zero();
            sessionState = null;
        }
        if (httpClient != null) {
            httpClient.setJwt(null);
        }
        showLoginScreen();
    }

    @Override
    public void stop() {
        if (syncManager != null) {
            syncManager.stop();
        }
        if (sessionState != null) {
            sessionState.zero();
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting DashboardUI...");
        launch(args);
    }
}
