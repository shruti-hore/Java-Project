package ui;

// javac -cp ".;lib/*" --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -d . ui/*.java model/*.java service/*.java
// java -cp ".;lib/*" --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml ui.DashboardUI

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
// updated
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.scene.control.Label;
import java.time.LocalDate;

import model.Task;
import service.TaskService;
import service.MongoService;

public class DashboardUI extends Application {

    private ObservableList<Task> taskList;
    private String currentFilter = "ALL";
    private String searchText = "";

    private Label totalLabel;
    private Label completedLabel;
    private Label dueSoonLabel;

    // Theme Colors
    private static final String BG_COLOR = "#1a1a2e";
    private static final String SIDEBAR_COLOR = "#16213e";
    private static final String CARD_BG = "#0f3460";
    private static final String ACCENT_COLOR = "#1abc9c";
    private static final String TEXT_COLOR = "#ffffff";
    private static final String MUTED_TEXT = "#95a5a6";

    // kanban
    private VBox deadlineColumn;
    private VBox inProgressColumn;
    private VBox doneColumn;

    private TaskService taskService = new TaskService(); // updated

    private void highlightSidebar(Label selected, Label... others) {
        selected.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 12 20; -fx-background-color: " + ACCENT_COLOR + "; -fx-background-radius: 8; -fx-cursor: hand;");

        for (Label l : others) {
            if (l != null) {
                l.setStyle("-fx-text-fill: " + MUTED_TEXT + "; -fx-font-size: 15px; -fx-padding: 12 20; -fx-cursor: hand;");
            }
        }
    }

    private void addHoverEffect(Label label) {
        label.setOnMouseEntered(e -> {
            if (!label.getStyle().contains(ACCENT_COLOR)) {
                label.setStyle(label.getStyle() + "-fx-background-color: #24345d; -fx-background-radius: 8;");
            }
        });

        label.setOnMouseExited(e -> {
            if (!label.getStyle().contains(ACCENT_COLOR)) {
                label.setStyle("-fx-text-fill: " + MUTED_TEXT + "; -fx-font-size: 15px; -fx-padding: 12 20; -fx-cursor: hand;");
            }
        });
    }

    private void styleButton(Button btn, String color, String hoverColor, String textColor) {
        String base = "-fx-background-color: " + color + ";" +
                      "-fx-text-fill: " + textColor + ";" +
                      "-fx-font-weight: bold;" +
                      "-fx-background-radius: 8;" +
                      "-fx-padding: 6 12;" +
                      "-fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base + "-fx-background-color: " + hoverColor + ";"));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    @Override
    public void start(Stage stage) {

        // LOAD DATA
        taskList = FXCollections.observableArrayList(taskService.getAllTasks());
        taskList.sort((a, b) -> a.getDeadline().compareTo(b.getDeadline()));

        // --- TOP BAR ---
        TextField searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 8 15;");
        searchField.textProperty().addListener((obs, o, n) -> {
            searchText = n.toLowerCase();
            refreshTasks();
        });

        Button profileBtn = new Button("PROFILE");
        styleButton(profileBtn, "#16213e", "#0f3460", "white");
        Button notifyBtn = new Button("NOTIFICATIONS");
        styleButton(notifyBtn, "#16213e", "#0f3460", "white");

        HBox topBar = new HBox(20, searchField, new Region(), notifyBtn, profileBtn);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: " + SIDEBAR_COLOR + "; -fx-padding: 15 30;");

        // --- HEADER ---
        Label welcome = new Label("WELCOME BACK, USER!");
        welcome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        totalLabel = new Label();
        completedLabel = new Label();
        dueSoonLabel = new Label();
        
        totalLabel.setStyle("-fx-text-fill: " + MUTED_TEXT + ";");
        completedLabel.setStyle("-fx-text-fill: #2ecc71;");
        dueSoonLabel.setStyle("-fx-text-fill: #e74c3c;");

        HBox headerStats = new HBox(30, totalLabel, completedLabel, dueSoonLabel);
        VBox header = new VBox(10, welcome, headerStats);
        header.setStyle("-fx-padding: 20 30;");

        // --- SIDEBAR ---
        Label dashboardNav = new Label("Dashboard");
        Label myTasksNav = new Label("My Tasks");
        Label categoriesNav = new Label("Categories");
        Label calendarNav = new Label("Calendar");
        Label settingsNav = new Label("Settings");
        Label logoutNav = new Label("Logout");

        String navStyle = "-fx-text-fill: " + MUTED_TEXT + "; -fx-font-size: 15px; -fx-padding: 12 20; -fx-cursor: hand;";
        for (Label l : new Label[]{dashboardNav, myTasksNav, categoriesNav, calendarNav, settingsNav, logoutNav}) {
            l.setStyle(navStyle);
            l.setMaxWidth(Double.MAX_VALUE);
            addHoverEffect(l);
        }
        
        dashboardNav.setStyle(navStyle + "-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 8;");
        
        myTasksNav.setOnMouseClicked(e -> {
            currentFilter = "ALL";
            highlightSidebar(myTasksNav, dashboardNav, categoriesNav, calendarNav, settingsNav, logoutNav);
            refreshTasks();
        });

        VBox navBox = new VBox(5, dashboardNav, myTasksNav, categoriesNav, calendarNav, new Region(), settingsNav, logoutNav);
        VBox.setVgrow(navBox.getChildren().get(4), Priority.ALWAYS);
        navBox.setStyle("-fx-background-color: " + SIDEBAR_COLOR + "; -fx-padding: 30 15;");
        navBox.setPrefWidth(220);

        // --- TASK CREATION (Moved to Sidebar bottom or separate panel) ---
        TextField titleInput = new TextField(); titleInput.setPromptText("Title");
        TextField descInput = new TextField(); descInput.setPromptText("Description");
        DatePicker dateInput = new DatePicker();
        Button addBtn = new Button("ADD NEW TASK");
        styleButton(addBtn, ACCENT_COLOR, "#16a085", "white");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        
        addBtn.setOnAction(e -> {
            String t = titleInput.getText();
            String desc = descInput.getText();
            LocalDate d = dateInput.getValue();
            if (t.isEmpty() || desc.isEmpty() || d == null) { showError("Missing fields"); return; }
            Task task = new Task(null, t, desc, d.toString(), false, "DEADLINE");
            taskService.addTask(task);
            taskList.add(task);
            taskList.sort((a, b) -> a.getDeadline().compareTo(b.getDeadline()));
            titleInput.clear(); descInput.clear(); dateInput.setValue(null);
            refreshTasks();
        });

        VBox addForm = new VBox(10, new Label("QUICK ADD"), titleInput, descInput, dateInput, addBtn);
        addForm.setStyle("-fx-padding: 20; -fx-background-color: #1a1a2e; -fx-background-radius: 10;");
        navBox.getChildren().add(5, addForm);

        // --- KANBAN BOARD ---
        deadlineColumn = new VBox(15);
        inProgressColumn = new VBox(15);
        doneColumn = new VBox(15);
        
        ScrollPane dScroll = createScroll(deadlineColumn);
        ScrollPane pScroll = createScroll(inProgressColumn);
        ScrollPane doneScroll = createScroll(doneColumn);

        Label dHead = createColHeader("DEADLINE");
        Label pHead = createColHeader("IN PROGRESS");
        Label cHead = createColHeader("DONE");

        VBox dCol = new VBox(10, dHead, dScroll);
        VBox pCol = new VBox(10, pHead, pScroll);
        VBox cCol = new VBox(10, cHead, doneScroll);
        HBox.setHgrow(dCol, Priority.ALWAYS); HBox.setHgrow(pCol, Priority.ALWAYS); HBox.setHgrow(cCol, Priority.ALWAYS);

        HBox kanban = new HBox(25, dCol, pCol, cCol);
        kanban.setStyle("-fx-padding: 0 30 30 30;");

        // --- ANALYTICS PLACEHOLDER ---
        Label completionLabel = new Label("Completion: 65%");
        Label workloadLabel = new Label("Workload: Balanced");
        completionLabel.setStyle("-fx-text-fill: white;"); workloadLabel.setStyle("-fx-text-fill: white;");
        HBox analytics = new HBox(40, completionLabel, workloadLabel);
        analytics.setStyle("-fx-background-color: " + SIDEBAR_COLOR + "; -fx-padding: 15 30;");

        VBox centerContent = new VBox(header, kanban, analytics);
        VBox.setVgrow(kanban, Priority.ALWAYS);
        centerContent.setStyle("-fx-background-color: " + BG_COLOR + ";");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(navBox);
        root.setCenter(centerContent);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Secure Task Manager");
        stage.setFullScreen(true);
        stage.show();
        
        refreshTasks();
    }

    private ScrollPane createScroll(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        return sp;
    }

    private Label createColHeader(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + MUTED_TEXT + "; -fx-padding: 5 0;");
        return l;
    }

    private void updateStats() {
        int total = taskList.size();
        int completed = 0;
        int dueSoon = 0;

        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(2);

        for (Task t : taskList) {
            if (t.isCompleted()) {
                completed++;
            } else {
                try {
                    LocalDate d = LocalDate.parse(t.getDeadline());
                    if (!d.isBefore(today) && !d.isAfter(soon)) {
                        dueSoon++;
                    }
                } catch (Exception e) {}
            }
        }

        totalLabel.setText("Total: " + total);
        completedLabel.setText("Done: " + completed);
        dueSoonLabel.setText("Due Soon: " + dueSoon);
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
            title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
            desc.setStyle("-fx-text-fill: " + MUTED_TEXT + "; -fx-font-size: 13px;");
            deadline.setStyle("-fx-text-fill: " + ACCENT_COLOR + "; -fx-font-size: 12px; -fx-font-weight: bold;");

            Button editBtn = new Button("Edit");
            Button deleteBtn = new Button("Delete");

            Button startBtn = new Button("Start");
            Button doneBtn = new Button("Done");

            // Apply styles
            styleButton(startBtn, "#3498db", "#2980b9", "white");
            styleButton(doneBtn, "#2ecc71", "#27ae60", "white");
            styleButton(editBtn, "#f1c40f", "#f39c12", "black");
            styleButton(deleteBtn, "#e74c3c", "#c0392b", "white");

            // START
            startBtn.setOnAction(e -> {
                // t.setStatus("IN_PROGRESS");
                taskService.markInProgress(t); // updated
                refreshTasks();
            });

            // DONE
            doneBtn.setOnAction(e -> {
                taskService.markDone(t); // updated
                refreshTasks();
            });

            // DELETE
            deleteBtn.setOnAction(e -> {
                taskService.deleteTask(t.getId()); // updated
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
                styleButton(saveBtn, "#34495e", "#2c3e50", "white");

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
                    taskService.updateTask(t);

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

            HBox btnRow = new HBox(10, editBtn, deleteBtn);
            btnRow.setAlignment(Pos.CENTER_LEFT);

            String status = t.getStatus();
            if (status == null) {
                status = "DEADLINE";
                t.setStatus(status);
            }

            // Conditional visibility based on status
            if (status.equals("DEADLINE")) {
                btnRow.getChildren().add(0, startBtn);
            } else if (status.equals("IN_PROGRESS")) {
                btnRow.getChildren().add(0, doneBtn);
            }

            // updated - status color strip (MOVE HERE)
            String borderColor = "#95a5a6";

            if (status.equals("DEADLINE")) borderColor = "#e67e22";
            if (status.equals("IN_PROGRESS")) borderColor = "#3498db";
            if (status.equals("DONE")) borderColor = "#2ecc71";

            // NOW create card
            VBox card = new VBox(8, title, desc, deadline, btnRow);

            card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                "-fx-padding: 20;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-border-width: 0 0 0 4;" +
                "-fx-border-color: transparent transparent transparent " + borderColor + ";" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);"
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