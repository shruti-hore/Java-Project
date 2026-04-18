// compile : javac --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -d . ui/*.java model/*.java service/*.java
// run : java --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml ui.DashboardUI

package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
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

    @Override
    public void start(Stage stage) {

        // ================= DATA =================
        taskList = new ArrayList<>(MockDataService.getSampleTasks());

        // ================= TITLE =================
        Label title = new Label("Secure Task Manager Dashboard");

        // ================= INPUT FIELDS =================
        TextField titleInput = new TextField();
        titleInput.setPromptText("Task Title");

        TextField deadlineInput = new TextField();
        deadlineInput.setPromptText("Deadline");

        Button addBtn = new Button("➕ Add Task");

        addBtn.setOnAction(e -> {
            String t = titleInput.getText();
            String d = deadlineInput.getText();

            if (!t.isEmpty() && !d.isEmpty()) {
                taskList.add(new Task(t, d, false));
                titleInput.clear();
                deadlineInput.clear();
                refreshTasks();
            }
        });

        HBox inputBox = new HBox(10, titleInput, deadlineInput, addBtn);
        inputBox.setAlignment(Pos.CENTER);

        VBox topBox = new VBox(10, title, inputBox);
        topBox.setAlignment(Pos.CENTER);

        // ================= SIDEBAR =================
        VBox sidebar = new VBox(10);
        sidebar.getChildren().addAll(
            new Label("All Tasks"),
            new Label("Completed"),
            new Label("Pending")
        );

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

        // Title
        Label title = new Label(t.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Deadline
        Label deadline = new Label("Due: " + t.getDeadline());

        if (t.isCompleted()) {
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");
            deadline.setStyle("-fx-text-fill: green;");
        }

        // Buttons
        Button completeBtn = new Button("✔ Complete");
        Button deleteBtn = new Button("❌ Delete");

        completeBtn.setOnAction(e -> {
            t.markComplete();
            refreshTasks();
        });

        deleteBtn.setOnAction(e -> {
            taskList.remove(t);
            refreshTasks();
        });

        // Button row
        HBox buttonRow = new HBox(10, completeBtn, deleteBtn);

        // Card layout
        VBox card = new VBox(5, title, deadline, buttonRow);

        // Card Styling
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

    public static void main(String[] args) {
        launch();
    }
}