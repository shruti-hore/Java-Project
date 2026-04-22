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
import java.io.File;
import java.net.URL;

import javafx.scene.control.Label;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.*;
import model.Task;
import service.TaskService;

public class DashboardUI extends Application {

    private ObservableList<Task> taskList;
    private String currentFilter = "ALL";
    private Label totalLabel;
    private Label completedLabel;
    private Label dueSoonLabel;

    private String searchText = "";
    private String currentView = "KANBAN";

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

    private BorderPane mainRoot;
    private VBox kanbanView;
    private ScrollPane calendarView;
    private YearMonth currentYearMonth = YearMonth.now();

    private VBox statContainer; // Container for stat cards
    private Label completionVal;
    private Label workloadVal;
    private Label totalVal;

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
        searchField.setStyle("-fx-background-color: #21262d; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 8 15;");
        searchField.textProperty().addListener((obs, o, n) -> { searchText = n.toLowerCase(); refreshTasks(); });

        Button profileBtn = new Button("PROFILE");
        profileBtn.getStyleClass().add("view-btn");
        Button notifyBtn = new Button("NOTIFICATIONS");
        notifyBtn.getStyleClass().add("view-btn");

        HBox topBar = new HBox(20, searchField, new Region(), notifyBtn, profileBtn);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #161b22; -fx-padding: 15 30;");

        // --- HEADER ---
        Label welcome = new Label("WELCOME BACK, USER!");
        welcome.getStyleClass().add("column-title");
        welcome.setStyle("-fx-font-size: 24px;");

        totalLabel = new Label();
        completedLabel = new Label();
        dueSoonLabel = new Label();
        totalLabel.setStyle("-fx-text-fill: #8b949e;");
        completedLabel.setStyle("-fx-text-fill: #10b981;");
        dueSoonLabel.setStyle("-fx-text-fill: #e74c3c;");

        HBox headerStats = new HBox(30, totalLabel, completedLabel, dueSoonLabel);
        VBox header = new VBox(10, welcome, headerStats);
        header.setStyle("-fx-padding: 20 30;");

        // --- SIDEBAR ---
        Label dashboardNav = new Label("Dashboard");
        Label calendarNav = new Label("Calendar");
        Label myTasksNav = new Label("My Tasks");
        Label settingsNav = new Label("Settings");
        Label logoutNav = new Label("Logout");

        String navStyle = "-fx-text-fill: #8b949e; -fx-font-size: 15px; -fx-padding: 12 20; -fx-cursor: hand;";
        for (Label l : new Label[]{dashboardNav, calendarNav, myTasksNav, settingsNav, logoutNav}) {
            l.setStyle(navStyle);
            l.setMaxWidth(Double.MAX_VALUE);
        }
        dashboardNav.setStyle(navStyle + "-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-background-radius: 8;");

        dashboardNav.setOnMouseClicked(e -> {
            currentView = "KANBAN";
            mainRoot.setCenter(kanbanView);
            highlightSidebar(dashboardNav, calendarNav, myTasksNav, settingsNav, logoutNav);
        });

        calendarNav.setOnMouseClicked(e -> {
            currentView = "CALENDAR";
            showCalendarView();
            highlightSidebar(calendarNav, dashboardNav, myTasksNav, settingsNav, logoutNav);
        });

        VBox navBox = new VBox(5, dashboardNav, calendarNav, myTasksNav, new Region(), settingsNav, logoutNav);
        VBox.setVgrow(navBox.getChildren().get(3), Priority.ALWAYS);
        navBox.setStyle("-fx-background-color: #161b22; -fx-padding: 30 15;");
        navBox.setPrefWidth(240);

        // --- QUICK ADD ---
        TextField tIn = new TextField(); tIn.setPromptText("Title");
        TextField dIn = new TextField(); dIn.setPromptText("Description");
        DatePicker dateIn = new DatePicker(LocalDate.now());
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("Important", "High", "Low"));
        pIn.setValue("Low");
        pIn.setMaxWidth(Double.MAX_VALUE);
        Button addBtn = new Button("ADD NEW TASK");
        addBtn.getStyleClass().add("button-sleek");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        
        addBtn.setOnAction(e -> {
            if (tIn.getText().isEmpty()) return;
            Task task = new Task(null, tIn.getText(), dIn.getText(), dateIn.getValue().toString(), false, "DEADLINE", pIn.getValue());
            taskService.addTask(task);
            taskList.add(task);
            tIn.clear(); dIn.clear();
            refreshTasks();
        });

        VBox addForm = new VBox(10, new Label("QUICK ADD"), tIn, dIn, dateIn, pIn, addBtn);
        addForm.setStyle("-fx-padding: 20; -fx-background-color: #0d1117; -fx-background-radius: 10;");
        navBox.getChildren().add(5, addForm);

        // --- KANBAN COLUMNS ---
        deadlineColumn = new VBox(15);
        inProgressColumn = new VBox(15);
        doneColumn = new VBox(15);
        
        ScrollPane dScroll = createScroll(deadlineColumn);
        ScrollPane pScroll = createScroll(inProgressColumn);
        ScrollPane doneScroll = createScroll(doneColumn);

        VBox dCol = createColumn("TO DO", "todo-dot", deadlineColumn, dScroll, "DEADLINE");
        VBox pCol = createColumn("IN PROGRESS", "progress-dot", inProgressColumn, pScroll, "IN_PROGRESS");
        VBox cCol = createColumn("COMPLETED", "completed-dot", doneColumn, doneScroll, "DONE");
        
        HBox.setHgrow(dCol, Priority.ALWAYS); HBox.setHgrow(pCol, Priority.ALWAYS); HBox.setHgrow(cCol, Priority.ALWAYS);

        HBox kanban = new HBox(25, dCol, pCol, cCol);
        kanban.setStyle("-fx-padding: 0 30 30 30;");

        VBox centerContent = new VBox(header, kanban);
        VBox.setVgrow(kanban, Priority.ALWAYS);
        centerContent.setStyle("-fx-background-color: #0d1117;");
        kanbanView = centerContent;

        mainRoot = new BorderPane();
        mainRoot.setTop(topBar);
        mainRoot.setLeft(navBox);
        mainRoot.setCenter(kanbanView);

        Scene scene = new Scene(mainRoot, 1280, 800);
        try {
            File cssFile = new File("resources/style.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        stage.setScene(scene);
        stage.setTitle("Secure Task Manager");
        stage.setFullScreen(true);
        stage.show();
        
        setupColumnDragAndDrop(deadlineColumn, "DEADLINE");
        setupColumnDragAndDrop(inProgressColumn, "IN_PROGRESS");
        setupColumnDragAndDrop(doneColumn, "DONE");

        refreshTasks();
    }

    private ScrollPane createScroll(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        return sp;
    }

    private VBox createColumn(String title, String dotClass, VBox content, ScrollPane scroll, String status) {
        Region dot = new Region(); dot.getStyleClass().addAll("dot-indicator", dotClass);
        Label titleLbl = new Label(title); titleLbl.getStyleClass().add("column-title");
        Label countLbl = new Label("0 Total"); countLbl.getStyleClass().add("task-count");
        
        HBox titleRow = new HBox(10, dot, titleLbl, new Region(), countLbl);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleRow.getChildren().get(2), Priority.ALWAYS);
        
        Button addBtn = new Button("+ Add New Task");
        addBtn.getStyleClass().add("add-task-btn");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> showAddTaskDialog(status));

        VBox header = new VBox(10, titleRow, addBtn);
        header.getStyleClass().add("column-header");

        VBox col = new VBox(5, header, scroll);
        col.getStyleClass().add("column-container");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return col;
    }

    private void showAddTaskDialog(String status) {
        Stage dialog = new Stage();
        TextField tIn = new TextField(); tIn.setPromptText("Title");
        TextField dIn = new TextField(); dIn.setPromptText("Description");
        DatePicker dateIn = new DatePicker(LocalDate.now());
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("Important", "High", "Low"));
        pIn.setValue("Low");
        
        Button save = new Button("Add Task");
        save.getStyleClass().add("button-sleek");
        save.setOnAction(e -> {
            if (tIn.getText().isEmpty()) return;
            Task t = new Task(null, tIn.getText(), dIn.getText(), dateIn.getValue().toString(), false, status, pIn.getValue());
            taskService.addTask(t);
            taskList.add(t);
            refreshTasks();
            dialog.close();
        });

        VBox layout = new VBox(15, new Label("NEW TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle("-fx-padding: 30; -fx-background-color: #161b22;");
        dialog.setScene(new Scene(layout, 350, 400));
        dialog.getScene().getStylesheets().addAll(new File("resources/style.css").toURI().toString());
        dialog.show();
    }

    private void updateStats() {
        int total = taskList.size();
        int completed = 0;
        for (Task t : taskList) { if (t.isCompleted()) completed++; }
        
        int percent = total == 0 ? 0 : (int) ((double) completed / total * 100);
        String workload = "LOW";
        String workloadColor = "#2ecc71";
        if (total >= 8) { workload = "HIGH"; workloadColor = "#e74c3c"; }
        else if (total >= 4) { workload = "MEDIUM"; workloadColor = "#e67e22"; }

        totalLabel.setText("Total: " + total);
        completedLabel.setText("Completion: " + percent + "%");
        dueSoonLabel.setText("Workload: " + workload);
        dueSoonLabel.setStyle("-fx-text-fill: " + workloadColor + ";");
    }

    private void showCalendarView() {
        VBox container = new VBox(20);
        container.getStyleClass().add("calendar-grid");
        
        HBox nav = new HBox(20);
        nav.setAlignment(Pos.CENTER);
        Button prev = new Button("←");
        Button next = new Button("→");
        prev.getStyleClass().add("month-nav-btn");
        next.getStyleClass().add("month-nav-btn");
        
        Label monthLbl = new Label(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentYearMonth.getYear());
        monthLbl.getStyleClass().add("column-title");
        
        prev.setOnAction(e -> { currentYearMonth = currentYearMonth.minusMonths(1); showCalendarView(); });
        next.setOnAction(e -> { currentYearMonth = currentYearMonth.plusMonths(1); showCalendarView(); });
        
        nav.getChildren().addAll(prev, monthLbl, next);
        
        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar-grid");
        grid.setAlignment(Pos.CENTER);

        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int i=0; i<7; i++) {
            Label d = new Label(days[i]);
            d.setStyle("-fx-text-fill: #8b949e; -fx-font-weight: bold; -fx-padding: 10;");
            grid.add(d, i, 0);
        }

        LocalDate first = currentYearMonth.atDay(1);
        int dayOffset = first.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        Map<LocalDate, List<Task>> tasksByDate = new HashMap<>();
        for (Task t : taskList) {
            try {
                LocalDate d = LocalDate.parse(t.getDeadline());
                tasksByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(t);
            } catch (Exception e) {}
        }

        for (int day=1; day<=daysInMonth; day++) {
            int row = (day + dayOffset - 1) / 7 + 1;
            int col = (day + dayOffset - 1) % 7;
            
            VBox cell = new VBox(5);
            cell.getStyleClass().add("calendar-cell");
            LocalDate date = currentYearMonth.atDay(day);
            if (date.equals(LocalDate.now())) cell.getStyleClass().add("calendar-cell-today");
            
            Label dayNum = new Label(String.valueOf(day));
            dayNum.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            cell.getChildren().add(dayNum);
            
            if (tasksByDate.containsKey(date)) {
                for (Task t : tasksByDate.get(date)) {
                    Label tl = new Label(t.getTitle());
                    tl.getStyleClass().add("calendar-task-label");
                    String p = t.getPriority() != null ? t.getPriority().toLowerCase() : "low";
                    String color = "#2ecc71"; // Green
                    if (p.equals("important") || p.equals("high")) color = "#e74c3c";
                    else if (p.equals("medium")) color = "#e67e22";
                    tl.setStyle("-fx-background-color: " + color + ";");
                    tl.setOnMouseClicked(e -> showEditDialog(t));
                    cell.getChildren().add(tl);
                }
            }
            
            final LocalDate finalDate = date;
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    showAddTaskDialogForDate(finalDate);
                }
            });

            grid.add(cell, col, row);
        }

        container.getChildren().addAll(nav, grid);
        calendarView = new ScrollPane(container);
        calendarView.setFitToWidth(true);
        calendarView.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        mainRoot.setCenter(calendarView);
    }

    private void showAddTaskDialogForDate(LocalDate date) {
        showAddTaskDialog("DEADLINE"); // Reuse existing, but could be date-specific
    }

    private void setupColumnDragAndDrop(VBox column, String status) {
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String taskId = db.getString();
                for (Task t : taskList) {
                    if (t.getId().equals(taskId)) {
                        taskService.updateStatus(t, status);
                        refreshTasks();
                        success = true;
                        break;
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void refreshTasks() {
        updateStats();

        if (currentView.equals("CALENDAR")) {
            showCalendarView();
            return;
        }

        deadlineColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        int dCount = 0, pCount = 0, cCount = 0;

        for (Task t : taskList) {
            if (currentFilter.equals("COMPLETED") && !t.isCompleted()) continue;
            if (currentFilter.equals("PENDING") && t.isCompleted()) continue;
            if (!t.getTitle().toLowerCase().contains(searchText)) continue;

            VBox card = createTaskCard(t);
            String status = t.getStatus();
            if (status == null) status = "DEADLINE";

            switch (status) {
                case "DEADLINE": deadlineColumn.getChildren().add(card); dCount++; break;
                case "IN_PROGRESS": inProgressColumn.getChildren().add(card); pCount++; break;
                case "DONE": doneColumn.getChildren().add(card); cCount++; break;
            }
        }

        updateColumnCounts(dCount, pCount, cCount);
    }

    private void updateColumnCounts(int d, int p, int c) {
        // Find the count labels in the column containers
        updateColLabel(deadlineColumn, d);
        updateColLabel(inProgressColumn, p);
        updateColLabel(doneColumn, c);
    }

    private void updateColLabel(VBox col, int count) {
        try {
            VBox parent = (VBox) col.getParent();
            VBox header = (VBox) parent.getChildren().get(0);
            HBox titleRow = (HBox) header.getChildren().get(0);
            Label countLbl = (Label) titleRow.getChildren().get(3);
            countLbl.setText(count + " Total");
        } catch (Exception e) {}
    }

    private VBox createTaskCard(Task t) {
        String priority = t.getPriority();
        if (priority == null) priority = "Low";
        
        Label pTag = new Label(priority.toUpperCase());
        pTag.getStyleClass().addAll("priority-tag", "priority-" + priority.toLowerCase());
        
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("task-title-bold");
        
        Label desc = new Label(t.getDescription());
        desc.getStyleClass().add("metadata-text");
        
        Label deadline = new Label("📅 " + t.getDeadline());
        deadline.getStyleClass().add("metadata-text");

        // --- METADATA (Deadline only) ---
        HBox metaRow = new HBox(deadline);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        // --- ACTION BUTTONS (Horizontal Row) ---
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        styleButton(editBtn, "#34495e", "#2c3e50", "white");
        styleButton(deleteBtn, "#e74c3c", "#c0392b", "white");
        editBtn.setPrefWidth(70);
        deleteBtn.setPrefWidth(70);

        HBox actions = new HBox(10, editBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        String status = t.getStatus();
        if (status == null) status = "DEADLINE";

        if (status.equals("DEADLINE")) {
            Button startBtn = new Button("Start");
            styleButton(startBtn, "#3498db", "#2980b9", "white");
            startBtn.setPrefWidth(80);
            startBtn.setOnAction(e -> { taskService.updateStatus(t, "IN_PROGRESS"); refreshTasks(); });
            actions.getChildren().add(0, startBtn);
        } else if (status.equals("IN_PROGRESS")) {
            Button doneBtn = new Button("Done");
            styleButton(doneBtn, "#2ecc71", "#27ae60", "white");
            doneBtn.setPrefWidth(80);
            doneBtn.setOnAction(e -> { taskService.updateStatus(t, "DONE"); refreshTasks(); });
            actions.getChildren().add(0, doneBtn);
        }

        editBtn.setOnAction(e -> showEditDialog(t));
        deleteBtn.setOnAction(e -> {
            if (showConfirmation("Delete Task", "Are you sure?")) {
                taskService.deleteTask(t.getId());
                taskList.remove(t);
                refreshTasks();
            }
        });

        VBox card = new VBox(12, pTag, title, desc, metaRow, actions);
        card.getStyleClass().add("task-card");

        // --- STATUS BASED COLORING ---
        String statusColor = "#30363d"; // Default
        if (status.equals("DEADLINE")) statusColor = "#8b5cf6"; // To Do - Blue/Purple
        else if (status.equals("IN_PROGRESS")) statusColor = "#f59e0b"; // In Progress - Orange
        else if (status.equals("DONE")) statusColor = "#10b981"; // Done - Green

        card.setStyle("-fx-border-color: transparent transparent transparent " + statusColor + "; -fx-border-width: 0 0 0 4;");

        // --- DRAG AND DROP ---
        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(t.getId());
            db.setContent(content);
            event.consume();
        });

        return card;
    }

    private void showEditDialog(Task t) {
        Stage dialog = new Stage();
        TextField tIn = new TextField(t.getTitle());
        TextField dIn = new TextField(t.getDescription());
        DatePicker dateIn = new DatePicker(LocalDate.parse(t.getDeadline()));
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("Important", "High", "Low"));
        pIn.setValue(t.getPriority());

        Button save = new Button("Save Changes");
        save.getStyleClass().add("button-sleek");
        save.setOnAction(e -> {
            t.setTitle(tIn.getText());
            t.setDescription(dIn.getText());
            t.setDeadline(dateIn.getValue().toString());
            t.setPriority(pIn.getValue());
            taskService.updateTask(t);
            refreshTasks();
            dialog.close();
        });

        VBox layout = new VBox(15, new Label("EDIT TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle("-fx-padding: 30; -fx-background-color: #161b22;");
        dialog.setScene(new Scene(layout, 350, 400));
        dialog.getScene().getStylesheets().addAll(new File("resources/style.css").toURI().toString());
        dialog.show();
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
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