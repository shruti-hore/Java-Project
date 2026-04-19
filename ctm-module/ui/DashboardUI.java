// compile : javac --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -d . ui/*.java model/*.java service/*.java
// run : java --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml ui.DashboardUI

package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.util.List;
import javafx.scene.control.Label; // updated
import java.util.ArrayList;

import model.Task;
import service.FileService;
import service.MockDataService;

public class DashboardUI extends Application {

    private VBox taskContainer;
    private List<Task> taskList;
    private String currentFilter = "ALL";

    @Override
    public void start(Stage stage) {

        // ================= DATA =================
        taskList = FileService.loadTasks();

        // ================= TITLE =================
        Label title = new Label("Secure Task Manager Dashboard");

        // ================= INPUT FIELDS =================
        TextField titleInput = new TextField();
        titleInput.setPromptText("Task Title");

        DatePicker deadlineInput = new DatePicker();

        // updated: disable past dates
        deadlineInput.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(java.time.LocalDate.now())); // updated
            }
        });

        Button addBtn = new Button("Add Task");

        // updated: validation + popup
        addBtn.setOnAction(e -> {
            String t = titleInput.getText();

            if (t.isEmpty() || deadlineInput.getValue() == null) {
                showError("Please enter task title and select a valid date."); // updated
                return;
            }

            if (deadlineInput.getValue().isBefore(java.time.LocalDate.now())) {
                showError("Deadline cannot be in the past."); // updated
                return;
            }

            String d = deadlineInput.getValue().toString();

            taskList.add(new Task(t, d, false));
            FileService.saveTasks(taskList); // updated
            titleInput.clear();
            deadlineInput.setValue(null);
            refreshTasks();
        });

        HBox inputBox = new HBox(10, titleInput, deadlineInput, addBtn);
        inputBox.setAlignment(Pos.CENTER);

        VBox topBox = new VBox(10, title, inputBox);
        topBox.setAlignment(Pos.CENTER);

        // ================= SIDEBAR =================
        Label allTasks = new Label("All Tasks");
        Label completed = new Label("Completed");
        Label pending = new Label("Pending");

        allTasks.setStyle("-fx-cursor: hand;");
        completed.setStyle("-fx-cursor: hand;");
        pending.setStyle("-fx-cursor: hand;");

        allTasks.setOnMouseClicked(e -> {
            currentFilter = "ALL";
            refreshTasks();
        });

        completed.setOnMouseClicked(e -> {
            currentFilter = "COMPLETED";
            refreshTasks();
        });

        pending.setOnMouseClicked(e -> {
            currentFilter = "PENDING";
            refreshTasks();
        });

        VBox sidebar = new VBox(10, allTasks, completed, pending);
        sidebar.setPrefWidth(150);
        sidebar.setStyle("-fx-padding: 10; -fx-background-color: #eeeeee;");

        // ================= TASK AREA =================
        taskContainer = new VBox(10);
        refreshTasks();

        ScrollPane scrollPane = new ScrollPane(taskContainer);
        scrollPane.setFitToWidth(true);

        // ================= ROOT =================
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setLeft(sidebar);
        root.setCenter(scrollPane);
        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 800, 600);
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI';");

        stage.setTitle("CTM Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    // ================= REFRESH =================
    private void refreshTasks() {

        taskContainer.getChildren().clear();

        for (Task t : taskList) {

            if (currentFilter.equals("COMPLETED") && !t.isCompleted()) continue;
            if (currentFilter.equals("PENDING") && t.isCompleted()) continue;

            Label title = new Label(t.getTitle());
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label deadline = new Label("Due: " + t.getDeadline());

            // updated: convert deadline to LocalDate
            java.time.LocalDate taskDate = java.time.LocalDate.parse(t.getDeadline());
            java.time.LocalDate today = java.time.LocalDate.now();

            // updated: overdue condition
            boolean isOverdue = taskDate.isBefore(today) && !t.isCompleted();

            if (t.isCompleted()) {
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");
                deadline.setStyle("-fx-text-fill: green;");
            }
            // updated: overdue styling
            else if (isOverdue) {
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");
                deadline.setStyle("-fx-text-fill: red;");
            }

            Button completeBtn = new Button("Complete");
            Button deleteBtn = new Button("Delete");
            Button editBtn = new Button("Edit"); // updated

            completeBtn.setOnAction(e -> {
                t.markComplete();

                taskList.remove(t);
                taskList.add(t);

                FileService.saveTasks(taskList); // updated

                refreshTasks();
            });

            deleteBtn.setOnAction(e -> {
                taskList.remove(t);

                FileService.saveTasks(taskList); // updated

                refreshTasks();
            });

            editBtn.setOnAction(e -> {

                // updated: dialog for title
                TextInputDialog titleDialog = new TextInputDialog(t.getTitle());
                titleDialog.setTitle("Edit Task");
                titleDialog.setHeaderText("Edit Task Title");
                titleDialog.setContentText("Title:");

                String newTitle = titleDialog.showAndWait().orElse(null);

                if (newTitle == null || newTitle.isEmpty()) return;

                // updated: dialog for date
                Dialog<java.time.LocalDate> dateDialog = new Dialog<>();
                dateDialog.setTitle("Edit Deadline");

                DatePicker datePicker = new DatePicker(java.time.LocalDate.parse(t.getDeadline()));

                // updated: disable past dates
                datePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(java.time.LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(empty || date.isBefore(java.time.LocalDate.now()));
                    }
                });

                dateDialog.getDialogPane().setContent(datePicker);
                dateDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                dateDialog.setResultConverter(btn -> {
                    if (btn == ButtonType.OK) {
                        return datePicker.getValue();
                    }
                    return null;
                });

                java.time.LocalDate newDate = dateDialog.showAndWait().orElse(null);

                if (newDate == null) return;

                // updated: apply changes
                t.setTitle(newTitle); // make sure setter exists
                t.setDeadline(newDate.toString()); // make sure setter exists

                FileService.saveTasks(taskList); // updated

                refreshTasks();
            });

            HBox buttonRow = new HBox(10, completeBtn, editBtn, deleteBtn); // updated

            // updated: overdue label
            Label overdueLabel = new Label("OVERDUE");
            overdueLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

            // updated: conditional card UI
            VBox card;
            if (isOverdue) {
                card = new VBox(5, title, deadline, overdueLabel, buttonRow); // updated
            } else {
                card = new VBox(5, title, deadline, buttonRow);
            }

            card.setStyle("""
                -fx-background-color: #f5f5f5;
                -fx-padding: 10;
                -fx-border-color: #ddd;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
            """);

            taskContainer.getChildren().add(card);
        }
    }

    // updated: error popup
    private void showError(String message) { // updated
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}