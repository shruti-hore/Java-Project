package ui.views;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import model.Task;
import service.TaskService;
import ui.components.TaskCard;
import java.util.function.Consumer;

public class MyTasksView extends VBox {
    private TaskService taskService;
    private ObservableList<Task> taskList;
    private String searchText = "";
    private VBox deadlineColumn, inProgressColumn, doneColumn;
    private Consumer<Task> onEdit;
    private Consumer<Task> onDelete;

    public MyTasksView(TaskService service, ObservableList<Task> tasks, Consumer<Task> editAction, Consumer<Task> deleteAction) {
        this.taskService = service;
        this.taskList = tasks;
        this.onEdit = editAction;
        this.onDelete = deleteAction;

        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #0d1117;");

        // --- TOP BAR ---
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("MY TASKS");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: #161b22; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10 15; -fx-border-color: #30363d; -fx-border-radius: 10;");
        searchField.textProperty().addListener((obs, o, n) -> { searchText = n.toLowerCase(); refresh(); });

        Button addTaskBtn = new Button("+ Add Task");
        addTaskBtn.getStyleClass().add("button-primary");
        addTaskBtn.setStyle("-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        addTaskBtn.setOnAction(e -> onEdit.accept(null)); // null means new task

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(title, searchField, spacer, addTaskBtn);

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

        getChildren().addAll(topBar, kanban);
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

        content.setStyle("-fx-padding: 5;");
        content.setMinWidth(250);

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
    }
}
