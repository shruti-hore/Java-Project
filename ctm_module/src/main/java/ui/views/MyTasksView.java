package ui.views;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import client.model.Task;
import service.TaskService;
import ui.components.TaskCard;
import java.util.function.Consumer;

public class MyTasksView extends VBox {
    private TaskService taskService;
    private ObservableList<Task> taskList;
    private String searchText = "";
    private VBox deadlineColumn, inProgressColumn, doneColumn;
    private HBox workloadSummary;
    private Consumer<Task> onEdit;
    private Consumer<Task> onDelete;

    public MyTasksView(TaskService service, ObservableList<Task> tasks, Consumer<Task> editAction, Consumer<Task> deleteAction) {
        this.taskService = service;
        this.taskList = tasks;
        this.onEdit = editAction;
        this.onDelete = deleteAction;

        setSpacing(30);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #f5f6fa;");

        // --- TOP BAR ---
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 0, 10, 0));

        Label title = new Label("TRACKING");
        title.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: white; -fx-text-fill: #1f2937; -fx-background-radius: 12; -fx-padding: 12 15; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");
        searchField.textProperty().addListener((obs, o, n) -> { searchText = n.toLowerCase(); refresh(); });

        Button addTaskBtn = new Button("+ Add Task");
        addTaskBtn.getStyleClass().add("button-primary");
        addTaskBtn.setPrefHeight(45);
        addTaskBtn.setOnAction(e -> onEdit.accept(null)); // null means new task

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(title, searchField, spacer, addTaskBtn);

        // --- WORKLOAD SUMMARY ---
        workloadSummary = new HBox(30);
        workloadSummary.setAlignment(Pos.CENTER_LEFT);
        workloadSummary.setPadding(new Insets(15, 25, 15, 25));
        workloadSummary.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        // --- KANBAN ---
        HBox kanban = new HBox(25);
        VBox.setVgrow(kanban, Priority.ALWAYS);

        deadlineColumn = new VBox(15);
        inProgressColumn = new VBox(15);
        doneColumn = new VBox(15);

        kanban.getChildren().addAll(
            createColumn("TO DO", "#8b5cf6", deadlineColumn, "DEADLINE"),
            createColumn("IN PROGRESS", "#f59e0b", inProgressColumn, "IN_PROGRESS"),
            createColumn("DONE", "#10b981", doneColumn, "DONE")
        );

        getChildren().addAll(topBar, workloadSummary, kanban);
        refresh();
    }

    private VBox createColumn(String title, String color, VBox content, String status) {
        VBox col = new VBox(15);
        HBox.setHgrow(col, Priority.ALWAYS);

        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        content.setStyle("-fx-padding: 15; -fx-background-color: #f0f3f6; -fx-background-radius: 20;");
        content.setMinWidth(300);

        // Drag over column
        scroll.setOnDragOver(event -> {
            if (event.getGestureSource() != content && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        scroll.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                String taskId = db.getString();
                Task found = taskList.stream().filter(t -> t.getId().equals(taskId)).findFirst().orElse(null);
                if (found != null && !found.getStatus().equals(status)) {
                    taskService.updateStatus(found, status);
                    refresh();
                }
                event.setDropCompleted(true);
            }
            event.consume();
        });

        col.getChildren().addAll(lbl, scroll);
        return col;
    }

    public void refresh() {
        deadlineColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        for (Task t : taskList) {
            if (!searchText.isEmpty() && !t.getTitle().toLowerCase().contains(searchText)) continue;

            TaskCard card = new TaskCard(t, taskService, this::refresh, onEdit, onDelete);
            
            if ("DEADLINE".equals(t.getStatus())) deadlineColumn.getChildren().add(card);
            else if ("IN_PROGRESS".equals(t.getStatus())) inProgressColumn.getChildren().add(card);
            else if ("DONE".equals(t.getStatus())) doneColumn.getChildren().add(card);
        }
        
        updateWorkloadSummary();
    }

    private void updateWorkloadSummary() {
        workloadSummary.getChildren().clear();
        
        long high = taskList.stream().filter(t -> "High".equalsIgnoreCase(t.getPriority())).count();
        long active = taskList.stream().filter(t -> !"DONE".equals(t.getStatus())).count();
        long overdue = taskList.stream().filter(t -> !"DONE".equals(t.getStatus()) && java.time.LocalDate.parse(t.getDeadline()).isBefore(java.time.LocalDate.now())).count();

        Label label = new Label("WORKLOAD ANALYSIS:");
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        
        HBox stats = new HBox(25);
        stats.getChildren().addAll(
            createStatItem("🔥 HIGH PRIORITY", String.valueOf(high), "#ef4444"),
            createStatItem("📝 ACTIVE TASKS", String.valueOf(active), "#4f46e5"),
            createStatItem("⚠️ OVERDUE", String.valueOf(overdue), "#f59e0b")
        );
        
        workloadSummary.getChildren().addAll(label, stats);
    }

    private HBox createStatItem(String label, String value, String color) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        Label vLbl = new Label(value);
        vLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + color + ";");
        Label lLbl = new Label(label);
        lLbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px; -fx-font-weight: bold;");
        item.getChildren().addAll(vLbl, lLbl);
        return item;
    }
}
