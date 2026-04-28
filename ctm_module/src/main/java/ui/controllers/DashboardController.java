package ui.controllers;

import auth.service.CryptoAdapter;
import auth.session.SessionState;
import client.service.EncryptedTaskService;
import client.crypto.EncryptedDocumentPayload;
import exceptions.SessionExpiredException;
import client.sync.ConflictResolver;
import client.sync.LocalCache;
import client.sync.SyncManager;
import client.sync.ConflictPair; // Assumption
import ui.http.HttpAuthClient;
import ui.views.DashboardView;
import ui.views.LoginView;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javax.crypto.AEADBadTagException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class DashboardController {

    private final DashboardView view;
    private final SessionState session;
    private final HttpAuthClient httpClient;
    private final EncryptedTaskService encryptedTaskService;
    private final LocalCache localCache;
    private final ConflictResolver conflictResolver;
    private final CryptoAdapter cryptoAdapter;

    private SyncManager syncManager;
    private Task<Void> currentPhase3Task;

    public DashboardController(DashboardView view, SessionState session,
                               HttpAuthClient httpClient, EncryptedTaskService encryptedTaskService,
                               LocalCache localCache, ConflictResolver conflictResolver,
                               CryptoAdapter cryptoAdapter) {
        this.view = view;
        this.session = session;
        this.httpClient = httpClient;
        this.encryptedTaskService = encryptedTaskService;
        this.localCache = localCache;
        this.conflictResolver = conflictResolver;
        this.cryptoAdapter = cryptoAdapter;

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            if (e instanceof SessionExpiredException) redirectToLogin();
        });
    }

    public void initialize() {
        view.getCreateWorkspaceButton().setOnAction(e -> handleCreateWorkspace());
        view.getJoinWorkspaceButton().setOnAction(e -> handleJoinWorkspace());
        
        loadWorkspaceList();

        syncManager = new SyncManager(
            localCache,
            conflictResolver,
            encryptedTaskService,
            session,
            httpClient.getClient(),
            httpClient.getBaseUrl(),
            conflictPair -> Platform.runLater(() -> handleConflict(conflictPair)),
            () -> Platform.runLater(() -> setSyncing(true)),
            () -> Platform.runLater(() -> setSyncing(false))
        );
        syncManager.start();
    }

    private void handleCreateWorkspace() {}
    private void handleJoinWorkspace() {}

    private void setSyncing(boolean syncing) {
        view.getSyncSpinner().setVisible(syncing);
        view.getSyncStatusLabel().setText(syncing ? "Syncing..." : "");
        view.getSyncStatusLabel().setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
    }

    private void loadWorkspaceList() {
        view.getWorkspaceListContainer().getChildren().clear();
        view.getWorkspaceListContainer().getChildren().add(new javafx.scene.control.ProgressIndicator());

        Task<List<HttpAuthClient.WorkspaceSummary>> task = new Task<>() {
            @Override
            protected List<HttpAuthClient.WorkspaceSummary> call() throws Exception {
                return httpClient.fetchWorkspaces();
            }
        };

        task.setOnSucceeded(e -> {
            List<HttpAuthClient.WorkspaceSummary> workspaces = task.getValue();
            view.getWorkspaceListContainer().getChildren().clear();
            for (HttpAuthClient.WorkspaceSummary ws : workspaces) {
                javafx.scene.layout.VBox card = view.createWorkspaceCard(ws.name(), ws.ownerUserId(), ws.lastSyncedAt());
                card.setOnMouseClicked(event -> handleWorkspaceSelected(ws.teamId()));
                view.getWorkspaceListContainer().getChildren().add(card);
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex instanceof SessionExpiredException) {
                redirectToLogin();
            } else {
                view.getWorkspaceListContainer().getChildren().clear();
                Label err = new Label("Could not load workspaces. Check your connection.");
                err.setStyle("-fx-text-fill: red;");
                view.getWorkspaceListContainer().getChildren().add(err);
            }
        });

        new Thread(task).start();
    }

    private void handleWorkspaceSelected(String teamId) {
        Task<Void> phase0 = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (!session.hasTeamKey(teamId)) {
                    String envelopeBase64 = httpClient.fetchTeamKeyEnvelope(teamId);
                    byte[] envelopeBytes = Base64.getDecoder().decode(envelopeBase64.trim().replace("\"", ""));
                    byte[] ownerPublicKeyBytes = httpClient.fetchOwnerPublicKey(teamId);
                    PublicKey ownerPublicKey = cryptoAdapter.loadPublicKey(ownerPublicKeyBytes);
                    byte[] teamKeyBytes = cryptoAdapter.unwrapTeamKey(envelopeBytes, ownerPublicKey, session.getX25519PrivateKey());
                    
                    Platform.runLater(() -> {
                        session.addTeamKey(teamId, teamKeyBytes);
                        Arrays.fill(teamKeyBytes, (byte) 0);
                        startPhase1(teamId);
                    });
                } else {
                    Platform.runLater(() -> startPhase1(teamId));
                }
                return null;
            }
        };
        phase0.setOnFailed(e -> {
            Throwable ex = phase0.getException();
            if (ex instanceof SessionExpiredException) {
                redirectToLogin();
            } else if (ex instanceof AEADBadTagException) {
                Platform.runLater(() -> {
                    Label err = new Label("Could not unlock workspace.");
                    err.setStyle("-fx-text-fill: red;");
                    view.getWorkspaceListContainer().getChildren().add(err);
                });
            } else {
                ex.printStackTrace();
            }
        });
        new Thread(phase0).start();
    }

    private void startPhase1(String teamId) {
        Task<List<HttpAuthClient.DocumentMeta>> phase1 = new Task<>() {
            @Override
            protected List<HttpAuthClient.DocumentMeta> call() throws Exception {
                return httpClient.fetchWorkspaceMetadata(teamId);
            }
        };
        
        phase1.setOnSucceeded(e -> {
            List<HttpAuthClient.DocumentMeta> metadata = phase1.getValue();
            // Render document name stubs into the Kanban board
            startPhase2(teamId, metadata);
        });
        
        phase1.setOnFailed(e -> {
            if (phase1.getException() instanceof SessionExpiredException) redirectToLogin();
        });
        
        new Thread(phase1).start();
    }

    private void startPhase2(String teamId, List<HttpAuthClient.DocumentMeta> metadata) {
        Task<Void> phase2 = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (HttpAuthClient.DocumentMeta meta : metadata) {
                    EncryptedDocumentPayload payload = httpClient.fetchDocument(meta.documentUuid());
                    encryptedTaskService.loadTask(meta.documentUuid(), teamId, payload);
                    Platform.runLater(() -> {
                        // Replace placeholder stub with real task card content
                    });
                }
                return null;
            }
        };
        phase2.setOnSucceeded(e -> startPhase3(teamId, metadata));
        phase2.setOnFailed(e -> {
            if (phase2.getException() instanceof SessionExpiredException) redirectToLogin();
        });
        
        new Thread(phase2).start();
    }

    private void startPhase3(String teamId, List<HttpAuthClient.DocumentMeta> metadata) {
        if (currentPhase3Task != null && currentPhase3Task.isRunning()) {
            currentPhase3Task.cancel();
        }

        currentPhase3Task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (HttpAuthClient.DocumentMeta meta : metadata) {
                    if (isCancelled()) break;
                    httpClient.fetchDocument(meta.documentUuid());
                    // Update local cache only
                    // localCache.cacheDocument(meta.documentUuid(), teamId, meta.versionSeq(), payload);
                }
                return null;
            }
        };
        currentPhase3Task.setOnFailed(e -> {
            if (currentPhase3Task.getException() instanceof SessionExpiredException) redirectToLogin();
        });

        Thread t = new Thread(currentPhase3Task);
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    private void handleConflict(ConflictPair pair) {
        view.getSyncStatusLabel().setText("Conflict detected — resolution required.");
    }

    public void onExit() {
        if (syncManager != null) {
            syncManager.stop();
        }
        session.zero();
        httpClient.setJwt(null);
    }

    private void redirectToLogin() {
        Platform.runLater(() -> {
            onExit();
            Stage stage = (Stage) view.getScene().getWindow();
            if (stage != null) {
                LoginView loginView = new LoginView();
                // Assume LoginController exists and takes view, httpClient, cryptoAdapter
                // LoginController loginController = new LoginController(loginView, httpClient, cryptoAdapter);
                // loginController.initialize();
                Scene scene = new Scene(loginView);
                java.net.URL resource = getClass().getResource("/style.css");
                if (resource != null) {
                    scene.getStylesheets().add(resource.toExternalForm());
                }
                stage.setScene(scene);
            }
        });
    }
}
