package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import ui.http.HttpAuthClient;
import ui.http.HttpAuthClient.InboxItem;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class InboxView extends VBox {
    private final HttpAuthClient httpClient;
    private final VBox listContainer = new VBox(20);
    private final Runnable onRefresh;

    public InboxView(HttpAuthClient httpClient) {
        this.httpClient = httpClient;
        this.onRefresh = this::loadInbox;
        
        setSpacing(30);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #f5f6fa;");

        Label title = new Label("INBOX");
        title.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Manage your team invitations and join requests.");
        subtitle.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");

        VBox header = new VBox(5, title, subtitle);
        
        listContainer.setPadding(new Insets(10, 0, 30, 0));
        
        ScrollPane scroll = new ScrollPane(listContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(header, scroll);
        
        loadInbox();
    }

    private void loadInbox() {
        listContainer.getChildren().clear();
        Label loading = new Label("Loading inbox...");
        loading.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
        listContainer.getChildren().add(loading);

        Task<List<InboxItem>> task = new Task<>() {
            @Override
            protected List<InboxItem> call() throws Exception {
                return httpClient.fetchInbox().get();
            }
        };

        task.setOnSucceeded(e -> {
            listContainer.getChildren().clear();
            List<InboxItem> items = task.getValue();
            
            if (items.isEmpty()) {
                VBox emptyState = new VBox(15);
                emptyState.setAlignment(Pos.CENTER);
                emptyState.setPadding(new Insets(100, 0, 0, 0));
                
                Label emptyLabel = new Label("No pending actions");
                emptyLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 18px; -fx-font-weight: bold;");
                
                Label emptySub = new Label("You're all caught up! New invites or requests will appear here.");
                emptySub.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");
                
                emptyState.getChildren().addAll(emptyLabel, emptySub);
                listContainer.getChildren().add(emptyState);
                return;
            }

            for (InboxItem item : items) {
                listContainer.getChildren().add(createInboxCard(item));
            }
        });

        task.setOnFailed(e -> {
            listContainer.getChildren().clear();
            Label error = new Label("Failed to load inbox items.");
            error.setStyle("-fx-text-fill: #ef4444;");
            listContainer.getChildren().add(error);
        });

        new Thread(task).start();
    }

    private VBox createInboxCard(InboxItem item) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 25; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5);");

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String typeColor = item.type().equals("INVITE") ? "#3b82f6" : "#f59e0b";
        Label typeTag = new Label(item.type().toUpperCase());
        typeTag.setStyle("-fx-background-color: " + typeColor + "15; -fx-text-fill: " + typeColor + "; " +
                        "-fx-padding: 5 12; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label time = new Label(formatTimestamp(item.timestamp()));
        time.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");

        topRow.getChildren().addAll(typeTag, spacer, time);

        VBox content = new VBox(5);
        Label mainText = new Label(item.senderName());
        mainText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        String actionText = item.type().equals("INVITE") ? "invited you to join " : "requested to join ";
        Label subText = new Label(actionText + item.teamName());
        subText.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
        
        content.getChildren().addAll(mainText, subText);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button acceptBtn = new Button("Accept");
        acceptBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-padding: 10 25; -fx-background-radius: 10; -fx-cursor: hand;");
        
        Button rejectBtn = new Button("Reject");
        rejectBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-padding: 10 25; -fx-background-radius: 10; -fx-cursor: hand;");

        acceptBtn.setOnAction(e -> handleAction(item, true, acceptBtn, rejectBtn));
        rejectBtn.setOnAction(e -> handleAction(item, false, acceptBtn, rejectBtn));

        actions.getChildren().addAll(rejectBtn, acceptBtn);

        card.getChildren().addAll(topRow, content, actions);
        return card;
    }

    private void handleAction(InboxItem item, boolean accept, Button b1, Button b2) {
        b1.setDisable(true);
        b2.setDisable(true);
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                httpClient.respondToInbox(item.id(), accept).get();
                return null;
            }
        };
        
        task.setOnSucceeded(e -> loadInbox());
        task.setOnFailed(e -> {
            b1.setDisable(false);
            b2.setDisable(false);
        });
        
        new Thread(task).start();
    }

    private String formatTimestamp(String ts) {
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(ts);
            return dt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        } catch (Exception e) {
            return ts;
        }
    }
}
