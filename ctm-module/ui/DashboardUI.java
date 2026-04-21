package ui;

// javac -cp ".;lib/*" --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -d . ui/*.java model/*.java service/*.java
// java -cp ".;lib/*" --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml ui.DashboardUI

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.scene.control.Label; // updated
import java.time.LocalDate;

import model.Task;
import service.MongoService;

public class DashboardUI extends Application {

    private ObservableList<Task> taskList;
    private String currentFilter = "ALL";
    private String searchText = "";

    private Label totalLabel;
    private Label completedLabel;
    private Label pendingLabel;

    // kanban
    private VBox deadlineColumn;
    private VBox inProgressColumn;
    private VBox doneColumn;

    private MongoService mongoService = new MongoService(); // updated

    private void highlightSidebar(Label selected, Label... others) {
        selected.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10 15;" +
            "-fx-background-radius: 10;" +
            "-fx-background-color: #1abc9c;"
        );

        for (Label l : others) {
            l.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 10 15;" +
                "-fx-background-radius: 10;"
            );
        }
    }

    private void addHoverEffect(Label label) {
        label.setOnMouseEntered(e -> {
            label.setStyle(label.getStyle() + "-fx-background-color: #16a085;");
        });

        label.setOnMouseExited(e -> {
            if (!label.getStyle().contains("#1abc9c")) {
                label.setStyle(
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 10 15;" +
                    "-fx-background-radius: 10;"
                );
            }
        });
    }

    @Override
    public void start(Stage stage) {

        // LOAD DATA
        taskList = FXCollections.observableArrayList(mongoService.getTasks()); // updated
        taskList.sort((a, b) -> a.getDeadline().toString().compareTo(b.getDeadline().toString())); // updated

        Label heading = new Label("Secure Task Manager Dashboard");
        heading.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );

        TextField titleInput = new TextField();
        titleInput.setPromptText("Task Title");

        TextField descriptionInput = new TextField();
        descriptionInput.setPromptText("Task Description");

        DatePicker deadlineInput = new DatePicker();

        // Disable past dates
        deadlineInput.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        Button addBtn = new Button("Add Task");

        // ADD TASK
        addBtn.setOnAction(e -> {

            String t = titleInput.getText();
            String desc = descriptionInput.getText();
            LocalDate d = deadlineInput.getValue();

            if (t.isEmpty() || desc.isEmpty() || d == null) {
                showError("Enter all fields");
                return;
            }

            Task task = new Task(null, t, desc, d.toString(), false, "DEADLINE"); // updated

            mongoService.addTask(task); // updated
            taskList.add(task);

            taskList.sort((a, b) -> a.getDeadline().compareTo(b.getDeadline()));

            titleInput.clear();
            descriptionInput.clear();
            deadlineInput.setValue(null);

            refreshTasks();
        });

        HBox inputBox = new HBox(10, titleInput, descriptionInput, deadlineInput, addBtn);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setStyle("-fx-padding: 10;");

        // SEARCH
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        searchField.textProperty().addListener((obs, o, n) -> {
            searchText = n.toLowerCase();
            refreshTasks();
        });

        // STATS
        totalLabel = new Label();
        completedLabel = new Label();
        pendingLabel = new Label();

        // updated
        totalLabel.setStyle("-fx-font-weight: bold;");
        completedLabel.setStyle("-fx-text-fill: green;");
        pendingLabel.setStyle("-fx-text-fill: red;");

        HBox statsBox = new HBox(30, totalLabel, completedLabel, pendingLabel);

        statsBox.setAlignment(Pos.CENTER);

        VBox topBox = new VBox(15, heading, inputBox, searchField, statsBox);
        topBox.setAlignment(Pos.CENTER);

        // SIDEBAR
        Label allTasks = new Label("All Tasks");
        Label completed = new Label("Completed");
        Label pending = new Label("Pending");

        // Base style
        String baseStyle =
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10 15;" +
            "-fx-background-radius: 10;";

        // Apply default styles
        allTasks.setStyle(baseStyle + "-fx-background-color: #1abc9c;");
        completed.setStyle(baseStyle);
        pending.setStyle(baseStyle);

        // Click actions
        allTasks.setOnMouseClicked(e -> {
            currentFilter = "ALL";
            highlightSidebar(allTasks, completed, pending);
            refreshTasks();
        });

        completed.setOnMouseClicked(e -> {
            currentFilter = "COMPLETED";
            highlightSidebar(completed, allTasks, pending);
            refreshTasks();
        });

        pending.setOnMouseClicked(e -> {
            currentFilter = "PENDING";
            highlightSidebar(pending, allTasks, completed);
            refreshTasks();
        });

        // Hover effects
        addHoverEffect(allTasks);
        addHoverEffect(completed);
        addHoverEffect(pending);

        // Sidebar container
        VBox sidebar = new VBox(20, allTasks, completed, pending);

        sidebar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);" +
            "-fx-padding: 20;"
        );

        sidebar.setPrefWidth(180);

        BorderPane root = new BorderPane();

        root.setStyle("-fx-background-color: #f5f6fa;"); // updated
        deadlineColumn = new VBox(10);
        inProgressColumn = new VBox(10);
        doneColumn = new VBox(10);

        deadlineColumn.setStyle("-fx-padding: 10;");
        inProgressColumn.setStyle("-fx-padding: 10;");
        doneColumn.setStyle("-fx-padding: 10;");

        deadlineColumn.setPrefWidth(250);
        inProgressColumn.setPrefWidth(250);
        doneColumn.setPrefWidth(250);

        Label dLabel = new Label("Deadline");
        dLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label pLabel = new Label("In Progress");
        pLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label doneLabel = new Label("Done");
        doneLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox deadlineBox = new VBox(10, dLabel, deadlineColumn);
        VBox progressBox = new VBox(10, pLabel, inProgressColumn);
        VBox doneBox = new VBox(10, doneLabel, doneColumn);

        HBox kanbanBoard = new HBox(20, deadlineBox, progressBox, doneBox);
        kanbanBoard.setStyle("-fx-padding: 20;");
        kanbanBoard.setAlignment(Pos.CENTER);

        root.setTop(topBox);
        root.setLeft(sidebar);
        root.setCenter(kanbanBoard);

        refreshTasks();

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Dashboard");
        stage.show();
    }

    private void updateStats() {
        int total = taskList.size();
        int completed = 0;

        for (Task t : taskList) {
            if (t.isCompleted()) completed++;
        }

        totalLabel.setText("Total: " + total);
        completedLabel.setText("Completed: " + completed);
        pendingLabel.setText("Pending: " + (total - completed));
    }

    private void refreshTasks() {

        updateStats();

        deadlineColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        for (Task t : taskList) {

            if (currentFilter.equals("COMPLETED") && !t.isCompleted()) continue;
            if (currentFilter.equals("PENDING") && t.isCompleted()) continue;

            if (!t.getTitle().toLowerCase().contains(searchText) &&
                !t.getDescription().toLowerCase().contains(searchText)) continue;

            Label title = new Label(t.getTitle());
            Label desc = new Label(t.getDescription());
            Label deadline = new Label("Due: " + t.getDeadline());

            // updated - text styling
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            desc.setStyle("-fx-text-fill: #555;");
            deadline.setStyle("-fx-text-fill: #888;");

            Button completeBtn = new Button("Complete");
            Button editBtn = new Button("Edit");
            Button deleteBtn = new Button("Delete");

            Button startBtn = new Button("Start");
            Button doneBtn = new Button("Done");

            // updated - button colors
           startBtn.setStyle(
                "-fx-background-color: #3498db;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 12;"
            );

            doneBtn.setStyle(
                "-fx-background-color: #2ecc71;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 12;"
            );

            editBtn.setStyle(
                "-fx-background-color: #f1c40f;" +
                "-fx-text-fill: black;" +   // yellow → black text
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 12;"
            );

            deleteBtn.setStyle(
                "-fx-background-color: #e74c3c;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 6 12;"
            );
            // COMPLETE
            completeBtn.setOnAction(e -> {
                t.setCompleted(true);
                mongoService.updateCompletion(t.getId(), true); // updated
                refreshTasks();
            });

            // START
            startBtn.setOnAction(e -> {
                t.setStatus("IN_PROGRESS");
                mongoService.updateStatus(t.getId(), "IN_PROGRESS"); // updated
                refreshTasks();
            });

            // DONE
            doneBtn.setOnAction(e -> {
                t.setStatus("DONE");
                t.setCompleted(true);
                mongoService.updateStatus(t.getId(), "DONE"); // updated
                mongoService.updateCompletion(t.getId(), true); // updated
                refreshTasks();
            });

            // DELETE
            deleteBtn.setOnAction(e -> {
                mongoService.deleteTask(t.getId()); // updated
                taskList.remove(t);
                refreshTasks();
            });

            // EDIT
            editBtn.setOnAction(e -> {

                Stage editStage = new Stage();

                TextField titleField = new TextField(t.getTitle());
                TextField descField = new TextField(t.getDescription());
                DatePicker datePicker = new DatePicker(LocalDate.parse(t.getDeadline())); // updated

                Button saveBtn = new Button("Save");

                saveBtn.setOnAction(ev -> {

                    String newTitle = titleField.getText();
                    String newDesc = descField.getText();
                    LocalDate newDate = datePicker.getValue();

                    if (newTitle.isEmpty() || newDesc.isEmpty() || newDate == null) {
                        showError("Invalid input");
                        return;
                    }

                    // update object
                    t.setTitle(newTitle);
                    t.setDescription(newDesc);
                    t.setDeadline(newDate.toString());

                    // (Temporary until API layer)
                    // mongoService.deleteTask(t.getId());
                    // mongoService.addTask(t);
                    mongoService.updateTask(t);

                    refreshTasks();
                    editStage.close();
                });

                VBox layout = new VBox(10,
                        new Label("Title"), titleField,
                        new Label("Description"), descField,
                        new Label("Deadline"), datePicker,
                        saveBtn
                );

                layout.setAlignment(Pos.CENTER);
                layout.setStyle("-fx-padding: 20;");

                editStage.setScene(new Scene(layout, 300, 350));
                editStage.show();
            });

            HBox btnRow = new HBox(10, startBtn, doneBtn, editBtn, deleteBtn); // updated


            String status = t.getStatus();
            if (status == null) {
                status = "DEADLINE";
                t.setStatus(status);
            }

            // updated - status color strip (MOVE HERE)
            String borderColor = "#95a5a6";

            if (status.equals("DEADLINE")) borderColor = "#e67e22";
            if (status.equals("IN_PROGRESS")) borderColor = "#3498db";
            if (status.equals("DONE")) borderColor = "#2ecc71";

            // NOW create card
            VBox card = new VBox(5, title, desc, deadline, btnRow);

            card.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-padding: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-background-radius: 15;" +
                "-fx-border-width: 0 0 0 5;" +
                "-fx-border-color: transparent transparent transparent " + borderColor + ";" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08),8,0,0,2);"
            );

            switch (status) {
                case "DEADLINE":
                    deadlineColumn.getChildren().add(card);
                    break;

                case "IN_PROGRESS":
                    inProgressColumn.getChildren().add(card);
                    break;

                case "DONE":
                    doneColumn.getChildren().add(card);
                    break;
            }
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }

    public static void main(String[] args) {
        launch();
    }
}