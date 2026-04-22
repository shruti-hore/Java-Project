package ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import java.io.File;
import java.time.LocalDate;
import model.Task;
import model.User;
import service.TaskService;
import ui.views.DashboardView;
import ui.views.MyTasksView;
import ui.views.SidebarView;
import utils.UserSession;
import utils.ValidationUtils;

public class DashboardUI extends Application {

    private StackPane mainStack;
    private BorderPane mainRoot;
    private TaskService taskService = new TaskService();
    private ObservableList<Task> taskList;
    
    private DashboardView dashboardView;
    private MyTasksView myTasksView;
    private ui.views.CalendarView calendarView;

    @Override
    public void start(Stage stage) {
        mainStack = new StackPane();
        Scene scene = new Scene(mainStack, 1200, 800);
        try {
            File cssFile = new File("resources/style.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        stage.setScene(scene);
        stage.setTitle("Secure Task Manager");
        stage.centerOnScreen();
        stage.show();

        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox loginBox = new VBox(25);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setMaxSize(400, 450);
        loginBox.setStyle("-fx-background-color: #161b22; -fx-padding: 40; -fx-background-radius: 20; -fx-border-color: #30363d; -fx-border-width: 1;");

        Label title = new Label("SECURE TASKER");
        title.setStyle("-fx-text-fill: #8b5cf6; -fx-font-size: 28px; -fx-font-weight: bold;");

        VBox fields = new VBox(15);
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setStyle("-fx-background-color: #0d1117; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #30363d;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-background-color: #0d1117; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #30363d;");
        fields.getChildren().addAll(new Label("EMAIL"), emailField, new Label("PASSWORD"), passField);

        Button loginBtn = new Button("LOG IN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12; -fx-cursor: hand;");
        loginBtn.setPrefHeight(45);

        loginBtn.setOnAction(e -> {
            String email = emailField.getText();
            if (!ValidationUtils.isValidEmail(email)) {
                showError("Invalid Email! Must be a valid @gmail.com address.");
                return;
            }
            User user = new User();
            user.setEmail(email);
            UserSession.login(user);
            initializeDashboard();
        });

        loginBox.getChildren().addAll(title, new Label("Please sign in to continue"), fields, loginBtn);
        mainStack.getChildren().add(loginBox);
    }

    private void initializeDashboard() {
        mainStack.getChildren().clear();
        String userEmail = UserSession.getCurrentUserEmail();
        
        taskList = FXCollections.observableArrayList(taskService.getAllTasks(userEmail, null));
        
        dashboardView = new DashboardView(taskList);
        myTasksView = new MyTasksView(taskService, taskList, this::handleEditAction, t -> {
            if (showConfirmation("Delete Task", "Are you sure you want to delete this task?")) {
                taskService.deleteTask(t.getId());
                taskList.remove(t);
                myTasksView.refresh();
            }
        });
        calendarView = new ui.views.CalendarView(taskList);
        
        SidebarView sidebar = new SidebarView(viewKey -> {
            switch(viewKey) {
                case "DASHBOARD": mainRoot.setCenter(dashboardView); break;
                case "KANBAN": mainRoot.setCenter(myTasksView); myTasksView.refresh(); break;
                case "CALENDAR": mainRoot.setCenter(calendarView); calendarView.refresh(); break;
                case "LOGOUT": UserSession.logout(); showLoginScreen(); break;
                default: showError("Module coming soon!");
            }
        });

        mainRoot = new BorderPane();
        mainRoot.setLeft(sidebar);
        mainRoot.setCenter(dashboardView);

        mainStack.getChildren().add(mainRoot);
    }

    private void handleEditAction(Task t) {
        if (t == null) showAddTaskDialog();
        else showEditDialog(t);
    }

    private void showAddTaskDialog() {
        TextField tIn = new TextField(); tIn.setPromptText("Title");
        TextField dIn = new TextField(); dIn.setPromptText("Description");
        DatePicker dateIn = new DatePicker(LocalDate.now());
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("High", "Medium", "Low"));
        pIn.setValue("Low");
        pIn.setMaxWidth(Double.MAX_VALUE);
        
        Button save = new Button("Add Task");
        save.setStyle("-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        save.setMaxWidth(Double.MAX_VALUE);
        
        save.setOnAction(e -> {
            String title = tIn.getText();
            String date = dateIn.getValue().toString();
            if (!ValidationUtils.isValidTaskTitle(title)) { showError("Title cannot be empty!"); return; }
            if (!ValidationUtils.isFutureOrPresentDate(date)) { showError("Date cannot be in the past!"); return; }

            Task newTask = new Task(null, title, dIn.getText(), date, false, "DEADLINE", pIn.getValue(), UserSession.getCurrentUserEmail(), null);
            taskService.addTask(newTask);
            taskList.add(newTask);
            myTasksView.refresh();
            hideOverlay();
        });

        VBox layout = new VBox(15, new Label("NEW TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle("-fx-padding: 30; -fx-background-color: #161b22; -fx-background-radius: 12; -fx-border-color: #30363d; -fx-border-width: 1;");
        layout.setMaxSize(350, 400);
        showOverlay(layout);
    }

    private void showEditDialog(Task t) {
        TextField tIn = new TextField(t.getTitle());
        TextField dIn = new TextField(t.getDescription());
        DatePicker dateIn = new DatePicker(LocalDate.parse(t.getDeadline()));
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("High", "Medium", "Low"));
        pIn.setValue(t.getPriority());
        pIn.setMaxWidth(Double.MAX_VALUE);

        Button save = new Button("Save Changes");
        save.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        save.setMaxWidth(Double.MAX_VALUE);
        
        save.setOnAction(e -> {
            String title = tIn.getText();
            String date = dateIn.getValue().toString();
            if (!ValidationUtils.isValidTaskTitle(title)) { showError("Title cannot be empty!"); return; }
            if (!ValidationUtils.isFutureOrPresentDate(date)) { showError("Date cannot be in the past!"); return; }

            t.setTitle(title);
            t.setDescription(dIn.getText());
            t.setDeadline(date);
            t.setPriority(pIn.getValue());
            taskService.updateTask(t);
            myTasksView.refresh();
            hideOverlay();
        });

        VBox layout = new VBox(15, new Label("EDIT TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle("-fx-padding: 30; -fx-background-color: #161b22; -fx-background-radius: 12; -fx-border-color: #30363d; -fx-border-width: 1;");
        layout.setMaxSize(350, 400);
        showOverlay(layout);
    }

    private void showOverlay(Node content) {
        Region glassPane = new Region();
        glassPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");
        glassPane.setOnMouseClicked(e -> hideOverlay());

        VBox container = new VBox(content);
        container.setAlignment(Pos.CENTER);
        container.setPickOnBounds(false);
        content.setEffect(new DropShadow(30, Color.BLACK));

        mainStack.getChildren().addAll(glassPane, container);
    }

    private void hideOverlay() {
        if (mainStack.getChildren().size() > 1) {
            mainStack.getChildren().remove(mainStack.getChildren().size() - 1);
            mainStack.getChildren().remove(mainStack.getChildren().size() - 1);
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static void main(String[] args) { launch(); }
}