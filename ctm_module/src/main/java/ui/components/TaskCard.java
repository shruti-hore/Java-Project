package ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.*;
import client.model.Task;
import service.TaskService;
import java.util.function.Consumer;

public class TaskCard extends VBox {
    private Task task;
    private TaskService taskService;
    private Runnable onAction;
    private Consumer<Task> onEdit;
    private Consumer<Task> onDelete;

    public TaskCard(Task t, TaskService service, Runnable refresh, Consumer<Task> editAction, Consumer<Task> deleteAction) {
        this.task = t;
        this.taskService = service;
        this.onAction = refresh;
        this.onEdit = editAction;
        this.onDelete = deleteAction;

        setSpacing(12);
        setPadding(new javafx.geometry.Insets(20));
        getStyleClass().add("task-card");

        String priority = t.getPriority();
        if (priority == null) priority = "Low";
        
        Label pTag = new Label(priority.toUpperCase());
        pTag.getStyleClass().addAll("priority-tag", "priority-" + priority.toLowerCase());
        
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("task-title-bold");
        
        Label desc = new Label(t.getDescription());
        desc.getStyleClass().add("metadata-text");
        desc.setWrapText(true);
        
        Label deadline = new Label("📅 " + t.getDeadline());
        deadline.getStyleClass().add("metadata-text");

        HBox metaRow = new HBox(deadline);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        styleBtn(editBtn, "#34495e", "#2c3e50");
        styleBtn(deleteBtn, "#e74c3c", "#c0392b");
        editBtn.setPrefWidth(70);
        deleteBtn.setPrefWidth(70);

        HBox actions = new HBox(10, editBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        String status = t.getStatus();
        if (status == null) status = "DEADLINE";

        if (status.equals("DEADLINE")) {
            Button startBtn = new Button("Start");
            styleBtn(startBtn, "#3498db", "#2980b9");
            startBtn.setPrefWidth(80);
            startBtn.setOnAction(e -> { taskService.updateStatus(t, "IN_PROGRESS"); onAction.run(); });
            actions.getChildren().add(0, startBtn);
        } else if (status.equals("IN_PROGRESS")) {
            Button doneBtn = new Button("Done");
            styleBtn(doneBtn, "#2ecc71", "#27ae60");
            doneBtn.setPrefWidth(80);
            doneBtn.setOnAction(e -> { taskService.updateStatus(t, "DONE"); onAction.run(); });
            actions.getChildren().add(0, doneBtn);
        }

        editBtn.setOnAction(e -> onEdit.accept(t));
        deleteBtn.setOnAction(e -> {
            onDelete.accept(t);
        });

        getChildren().addAll(pTag, title, desc, metaRow, actions);

        // Status coloring
        String statusColor = "#e5e7eb";
        if (status.equals("DEADLINE")) statusColor = "#8b5cf6";
        else if (status.equals("IN_PROGRESS")) statusColor = "#f59e0b";
        else if (status.equals("DONE")) statusColor = "#10b981";
        
        setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                 "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4); " +
                 "-fx-border-color: transparent transparent transparent " + statusColor + "; -fx-border-width: 0 0 0 4;");

        // Drag support
        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(t.getId());
            db.setContent(content);
            event.consume();
        });

        // Single click to edit
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                onEdit.accept(t);
                e.consume();
            }
        });
    }

    private void styleBtn(Button btn, String color, String hoverColor) {
        String base = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 15; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base + "-fx-background-color: " + hoverColor + ";"));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }
}
