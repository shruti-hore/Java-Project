package ui;

// javac -cp ".;lib/*" --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -d . ui/*.java model/*.java service/*.java
// java -cp ".;lib/*" --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml ui.DashboardUI

import javafx.application.Application;
import javafx.collections.FXCollections; // updated
import javafx.collections.ObservableList; // updated
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

// import java.lang.classfile.Label;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.time.LocalDate; // updated

import model.Task;
import service.MongoService;

public class DashboardUI extends Application {

    private VBox taskContainer;
    private ObservableList<Task> taskList; // updated
    private String currentFilter = "ALL";
    private String searchText = "";

    private Label totalLabel;
    private Label completedLabel;
    private Label pendingLabel;

    @Override
    public void start(Stage stage) {

        // LOAD FROM MONGO
        taskList = FXCollections.observableArrayList(MongoService.getTasks()); // updated
        taskList.sort((a, b) -> a.getDeadline().compareTo(b.getDeadline())); // updated

        Label title = new Label("Secure Task Manager Dashboard");

        TextField titleInput = new TextField();
        titleInput.setPromptText("Task Title");

        TextField descriptionInput = new TextField();
        descriptionInput.setPromptText("Task Description");

        DatePicker deadlineInput = new DatePicker();

        // DISABLE PAST DATES
        deadlineInput.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        Button addBtn = new Button("Add Task");

        // ===== ADD TASK =====
        addBtn.setOnAction(e -> {

            String t = titleInput.getText();
            String desc = descriptionInput.getText();
            LocalDate d = deadlineInput.getValue();

            if (t.isEmpty() || desc.isEmpty() || d == null) {
                showError("Enter all fields");
                return;
            }

            Task task = new Task(null, t, desc, d, false);

            MongoService.addTask(task);
            taskList.add(task);

            taskList.sort((a, b) -> a.getDeadline().compareTo(b.getDeadline()));

            titleInput.clear();
            descriptionInput.clear();
            deadlineInput.setValue(null);

            refreshTasks();
        });

        HBox inputBox = new HBox(10, titleInput, descriptionInput, deadlineInput, addBtn);
        inputBox.setAlignment(Pos.CENTER);

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

        HBox statsBox = new HBox(20, totalLabel, completedLabel, pendingLabel);
        statsBox.setAlignment(Pos.CENTER);

        VBox topBox = new VBox(15, title, inputBox, searchField, statsBox);
        topBox.setAlignment(Pos.CENTER);

        // SIDEBAR
        Label allTasks = new Label("All Tasks");
        Label completed = new Label("Completed");
        Label pending = new Label("Pending");

        allTasks.setOnMouseClicked(e -> { currentFilter = "ALL"; refreshTasks(); });
        completed.setOnMouseClicked(e -> { currentFilter = "COMPLETED"; refreshTasks(); });
        pending.setOnMouseClicked(e -> { currentFilter = "PENDING"; refreshTasks(); });

        VBox sidebar = new VBox(15, allTasks, completed, pending);

        // TASK AREA
        taskContainer = new VBox(15);
        refreshTasks();

        ScrollPane scrollPane = new ScrollPane(taskContainer);
        scrollPane.setFitToWidth(true);

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setLeft(sidebar);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 900, 600);
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
        taskContainer.getChildren().clear();

        for (Task t : taskList) {

            if (currentFilter.equals("COMPLETED") && !t.isCompleted()) continue;
            if (currentFilter.equals("PENDING") && t.isCompleted()) continue;

            if (!t.getTitle().toLowerCase().contains(searchText) &&
                !t.getDescription().toLowerCase().contains(searchText)) continue;

            Label title = new Label(t.getTitle());
            Label desc = new Label(t.getDescription());
            Label deadline = new Label("Due: " + t.getDeadline());

            Button completeBtn = new Button("Complete");
            Button editBtn = new Button("Edit");
            Button deleteBtn = new Button("Delete");

            // COMPLETE
            completeBtn.setOnAction(e -> {
                t.setCompleted(true); // updated
                refreshTasks();
            });

            // DELETE (FIXED)
            deleteBtn.setOnAction(e -> {
                MongoService.deleteTask(t); // updated
                taskList.remove(t);
                refreshTasks();
            });

            // EDIT (FIXED WITH MONGO)
            editBtn.setOnAction(e -> {

                Stage editStage = new Stage();

                TextField titleField = new TextField(t.getTitle());
                TextField descField = new TextField(t.getDescription());
                DatePicker datePicker = new DatePicker(t.getDeadline()); // updated

                Button saveBtn = new Button("Save");

                saveBtn.setOnAction(ev -> {

                    String newTitle = titleField.getText();
                    String newDesc = descField.getText();
                    LocalDate newDate = datePicker.getValue();

                    if (newTitle.isEmpty() || newDate == null) {
                        showError("Invalid input");
                        return;
                    }

                    Task newTask = new Task(t.getId(), newTitle, newDesc, newDate, t.isCompleted()); // updated

                    MongoService.updateTask(t, newTask); // updated

                    taskList.remove(t);
                    taskList.add(newTask);

                    taskList.sort((a, b) -> a.getDeadline().compareTo(b.getDeadline())); // updated

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

            HBox btnRow = new HBox(10, completeBtn, editBtn, deleteBtn);

            VBox card = new VBox(5, title, desc, deadline, btnRow);
            card.setStyle("-fx-background-color: white; -fx-padding: 10;");

            taskContainer.getChildren().add(card);
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