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
import java.util.ArrayList;

import model.Task;
import service.MockDataService;

public class DashboardUI extends Application {

    private VBox taskContainer;
    private List<Task> taskList;
    private String currentFilter = "ALL";

    @Override
    public void start(Stage stage) {

        // ================= DATA =================
        taskList = new ArrayList<>(MockDataService.getSampleTasks());

        // ================= TITLE =================
        Label title = new Label("Secure Task Manager Dashboard");

        // ================= INPUT FIELDS =================
        TextField titleInput = new TextField();
        titleInput.setPromptText("Task Title");

        // Updated: Using DatePicker instead of TextField to avoid invalid date input
        DatePicker deadlineInput = new DatePicker();

        // Updated: Disable past dates in calendar UI
        deadlineInput.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(java.time.LocalDate.now()));
            }
        });

        Button addBtn = new Button("Add Task");

        // Updated: Validation + popup alerts
        addBtn.setOnAction(e -> {
            String t = titleInput.getText();

            // Check empty title or no date selected
            if (t.isEmpty() || deadlineInput.getValue() == null) {
                showError("Please enter task title and select a valid date.");
                return;
            }

            // Check if selected date is in the past
            if (deadlineInput.getValue().isBefore(java.time.LocalDate.now())) {
                showError("Deadline cannot be in the past.");
                return;
            }

            String d = deadlineInput.getValue().toString();

            taskList.add(new Task(t, d, false));
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

            // Filter logic
            if (currentFilter.equals("COMPLETED") && !t.isCompleted()) continue;
            if (currentFilter.equals("PENDING") && t.isCompleted()) continue;

            Label title = new Label(t.getTitle());
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label deadline = new Label("Due: " + t.getDeadline());

            // Change color if completed
            if (t.isCompleted()) {
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");
                deadline.setStyle("-fx-text-fill: green;");
            }

            Button completeBtn = new Button("Complete");
            Button deleteBtn = new Button("Delete");

            completeBtn.setOnAction(e -> {
                t.markComplete();
                taskList.remove(t);
                taskList.add(t);   // move completed task to bottom
                refreshTasks();
            });

            deleteBtn.setOnAction(e -> {
                taskList.remove(t);
                refreshTasks();
            });

            HBox buttonRow = new HBox(10, completeBtn, deleteBtn);
            VBox card = new VBox(5, title, deadline, buttonRow);

            // Card styling
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

    // Updated: Method to show popup error messages
    private void showError(String message) {
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