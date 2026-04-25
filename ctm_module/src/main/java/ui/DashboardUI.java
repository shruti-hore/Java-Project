// to run : mvn clean javafx:run

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
import client.model.Task;
import model.User;
import java.util.List;
import service.TaskService;
import service.AuthService;
import service.TeamService;
import ui.views.DashboardView;
import ui.views.MyTasksView;
import ui.views.SidebarView;
import utils.UserSession;
import utils.ValidationUtils;
import exceptions.EmptyFieldException;
import exceptions.InvalidEmailException;
import exceptions.WeakPasswordException;

public class DashboardUI extends Application {

    private StackPane mainStack;
    private BorderPane mainRoot;
    private TaskService taskService = new TaskService();
    private ObservableList<Task> taskList;

    private DashboardView dashboardView;
    private MyTasksView myTasksView;
    private ui.views.CalendarView calendarView;
    private ui.views.WorkspaceView workspaceView;
    private AuthService authService = new AuthService();
    private TeamService teamService = new TeamService();
    
    private boolean isLoginMode = true;
    private Label loginErrorLabel = new Label();
    private PasswordField confirmPassField = new PasswordField();
    private Label confirmPassLbl = new Label("CONFIRM PASSWORD");

    @Override
    public void start(Stage stage) {
        mainStack = new StackPane();
        Scene scene = new Scene(mainStack, 1200, 800);
        try {
            java.net.URL cssUrl = getClass().getResource("/light_style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                File cssFile = new File("src/main/resources/light_style.css");
                if (cssFile.exists()) {
                    scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        stage.setScene(scene);
        stage.setTitle("Secure Task Manager");
        stage.centerOnScreen();
        stage.show();

        showLoginScreen();
    }

    private void showLoginScreen() {
        mainStack.getChildren().clear();
        mainStack.setStyle("-fx-background-color: #f5f6fa;");
        
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setMaxSize(400, 580);
        loginBox.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");

        Label title = new Label(isLoginMode ? "SECURE SIGN IN" : "CREATE ACCOUNT");
        title.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 28px; -fx-font-weight: bold;");

        loginErrorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");
        loginErrorLabel.setWrapText(true);
        loginErrorLabel.setText("");

        VBox fields = new VBox(10);
        Label eLbl = new Label("EMAIL ADDRESS");
        eLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        Label pLbl = new Label("PASSWORD");
        pLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        confirmPassLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        confirmPassField.setPromptText("Confirm your password");
        confirmPassField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        fields.getChildren().addAll(eLbl, emailField, pLbl, passField);
        if (!isLoginMode) {
            fields.getChildren().addAll(confirmPassLbl, confirmPassField);
        }

        Button actionBtn = new Button(isLoginMode ? "SIGN IN" : "REGISTER");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.getStyleClass().add("button-primary");
        actionBtn.setPrefHeight(50);

        actionBtn.setOnAction(e -> {
            String email = emailField.getText();
            String password = passField.getText();
            loginErrorLabel.setText("");

            try {
                validateInputs(email, password);
                if (!isLoginMode) {
                    if (!password.equals(confirmPassField.getText())) {
                        loginErrorLabel.setText("Passwords do not match!");
                        return;
                    }
                    if (authService.register(email, password)) {
                        isLoginMode = true;
                        showLoginScreen();
                        loginErrorLabel.setText("Registration successful! Please sign in.");
                        loginErrorLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: bold;");
                    } else {
                        loginErrorLabel.setText("User already exists!");
                    }
                } else {
                    if (authService.login(email, password)) {
                        User user = new User();
                        user.setEmail(email.trim());
                        UserSession.login(user);
                        initializeDashboard();
                    } else {
                        loginErrorLabel.setText("Invalid email or password.");
                    }
                }
            } catch (EmptyFieldException | InvalidEmailException | WeakPasswordException ex) {
                loginErrorLabel.setText(ex.getMessage());
            }
        });

        Button toggleBtn = new Button(isLoginMode ? "Don't have an account? Register" : "Already have an account? Sign In");
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-cursor: hand;");
        toggleBtn.setOnAction(e -> {
            isLoginMode = !isLoginMode;
            showLoginScreen();
        });

        loginBox.getChildren().addAll(title, loginErrorLabel, fields, actionBtn, toggleBtn);
        mainStack.getChildren().add(loginBox);
    }

    private void validateInputs(String email, String password)
            throws EmptyFieldException, InvalidEmailException, WeakPasswordException {
        if (email == null || email.trim().isEmpty()) {
            throw new EmptyFieldException("Email cannot be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new EmptyFieldException("Password cannot be empty");
        }

        String trimmedEmail = email.trim();
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$";
        if (!trimmedEmail.matches(emailRegex)) {
            throw new InvalidEmailException("Enter a valid email address ending in .com");
        }

        if (password.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters");
        }

        String passRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        if (!password.matches(passRegex)) {
            throw new WeakPasswordException("Include uppercase, lowercase, number and special character");
        }
    }

    private void initializeDashboard() {
        showWorkspaceSelection();
    }

    private void showWorkspaceSelection() {
        mainStack.getChildren().clear();
        String userEmail = UserSession.getCurrentUserEmail();
        List<model.Team> teams = teamService.getTeamsForUser(userEmail);

        workspaceView = new ui.views.WorkspaceView(
            teams,
            this::initializeMainApp,
            this::handleCreateTeam,
            this::handleJoinTeam
        );

        mainStack.getChildren().add(workspaceView);
    }

    private void handleCreateTeam() {
        VBox form = new VBox(20);
        form.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24;");
        form.setMaxSize(400, 300);
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Create Workspace");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        TextField nameField = new TextField();
        nameField.setPromptText("Team Name");
        nameField.setStyle("-fx-background-color: #f9fafb; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        Button submit = new Button("CREATE TEAM");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14; -fx-background-radius: 12; -fx-cursor: hand;");
        
        submit.setOnAction(e -> {
            String name = nameField.getText();
            if (name == null || name.trim().isEmpty()) {
                // Show local error or label
                return;
            }
            teamService.createTeam(name, UserSession.getCurrentUserEmail());
            hideOverlay();
            showWorkspaceSelection();
        });

        form.getChildren().addAll(title, nameField, submit);
        showOverlay(form);
    }

    private void handleJoinTeam() {
        VBox form = new VBox(20);
        form.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24;");
        form.setMaxSize(400, 300);
        form.setAlignment(Pos.CENTER);

        Label title = new Label("Join Workspace");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        TextField idField = new TextField();
        idField.setPromptText("Enter Team ID");
        idField.setStyle("-fx-background-color: #f9fafb; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        Button submit = new Button("JOIN TEAM");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14; -fx-background-radius: 12; -fx-cursor: hand;");
        
        submit.setOnAction(e -> {
            String id = idField.getText();
            if (id == null || id.trim().isEmpty()) {
                return;
            }
            teamService.joinTeam(id, UserSession.getCurrentUserEmail());
            hideOverlay();
            showWorkspaceSelection();
        });

        form.getChildren().addAll(title, idField, submit);
        showOverlay(form);
    }

    private void initializeMainApp(model.Team selectedTeam) {
        mainStack.getChildren().clear();
        String userEmail = UserSession.getCurrentUserEmail();
        
        taskList = FXCollections.observableArrayList(taskService.getAllTasks(userEmail, selectedTeam.getId()));
        
        dashboardView = new DashboardView(taskList);
        myTasksView = new MyTasksView(taskService, taskList, this::handleEditAction, t -> {
            showConfirmation("Delete Task", "Are you sure you want to delete this task?", () -> {
                taskService.deleteTask(t.getId());
                taskList.remove(t);
                myTasksView.refresh();
            });
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

        sidebar.getAddTaskBtn().setOnAction(e -> handleEditAction(null));
        dashboardView.getAddTaskButton().setOnAction(e -> handleEditAction(null));
        dashboardView.getViewTasksButton().setOnAction(e -> {
            mainRoot.setCenter(myTasksView);
            myTasksView.refresh();
        });
        
        mainRoot = new BorderPane();
        mainRoot.setLeft(sidebar);
        mainRoot.setCenter(dashboardView);

        mainStack.getChildren().add(mainRoot);
    }

    private void handleEditAction(Task t) {
        if (t == null)
            showAddTaskDialog();
        else
            showEditDialog(t);
    }

    private void showAddTaskDialog() {
        TextField tIn = new TextField();
        tIn.setPromptText("Title");
        TextField dIn = new TextField();
        dIn.setPromptText("Description");
        DatePicker dateIn = new DatePicker(LocalDate.now());
        dateIn.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("High", "Medium", "Low"));
        pIn.setValue("Low");
        pIn.setMaxWidth(Double.MAX_VALUE);

        Button save = new Button("Add Task");
        save.setStyle(
                "-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        save.setMaxWidth(Double.MAX_VALUE);

        save.setOnAction(e -> {
            String title = tIn.getText();
            String date = dateIn.getValue().toString();
            if (!ValidationUtils.isValidTaskTitle(title)) {
                showError("Title cannot be empty!");
                return;
            }
            if (!ValidationUtils.isFutureOrPresentDate(date)) {
                showError("Date cannot be in the past!");
                return;
            }

            Task newTask = new Task(null, title, dIn.getText(), date, false, "DEADLINE", pIn.getValue(),
                    UserSession.getCurrentUserEmail(), null);
            taskService.addTask(newTask);
            taskList.add(newTask);
            myTasksView.refresh();
            hideOverlay();
        });

        VBox layout = new VBox(15, new Label("NEW TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle(
                "-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");
        layout.setMaxSize(400, 450);
        showOverlay(layout);
    }

    private void showEditDialog(Task t) {
        TextField tIn = new TextField(t.getTitle());
        TextField dIn = new TextField(t.getDescription());
        DatePicker dateIn = new DatePicker(LocalDate.parse(t.getDeadline()));
        dateIn.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("High", "Medium", "Low"));
        pIn.setValue(t.getPriority());
        pIn.setMaxWidth(Double.MAX_VALUE);

        Button save = new Button("Save Changes");
        save.setStyle(
                "-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        save.setMaxWidth(Double.MAX_VALUE);

        save.setOnAction(e -> {
            String title = tIn.getText();
            String date = dateIn.getValue().toString();
            if (!ValidationUtils.isValidTaskTitle(title)) {
                showError("Title cannot be empty!");
                return;
            }
            if (!ValidationUtils.isFutureOrPresentDate(date)) {
                showError("Date cannot be in the past!");
                return;
            }

            t.setTitle(title);
            t.setDescription(dIn.getText());
            t.setDeadline(date);
            t.setPriority(pIn.getValue());
            taskService.updateTask(t);
            myTasksView.refresh();
            hideOverlay();
        });

        VBox layout = new VBox(15, new Label("EDIT TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle(
                "-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");
        layout.setMaxSize(400, 450);
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
        VBox box = new VBox(20);
        box.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24;");
        box.setMaxSize(400, 200);
        box.setAlignment(Pos.CENTER);

        Label title = new Label("Error");
        title.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label content = new Label(msg);
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #374151;");

        Button ok = new Button("OK");
        ok.setPrefWidth(100);
        ok.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        ok.setOnAction(e -> hideOverlay());

        box.getChildren().addAll(title, content, ok);
        showOverlay(box);
    }

    private void showConfirmation(String titleStr, String contentStr, Runnable onConfirm) {
        VBox box = new VBox(20);
        box.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24;");
        box.setMaxSize(400, 200);
        box.setAlignment(Pos.CENTER);

        Label title = new Label(titleStr);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        Label content = new Label(contentStr);
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #374151;");

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        
        Button cancel = new Button("CANCEL");
        cancel.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        cancel.setOnAction(e -> hideOverlay());

        Button confirm = new Button("CONFIRM");
        confirm.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        confirm.setOnAction(e -> {
            hideOverlay();
            onConfirm.run();
        });

        buttons.getChildren().addAll(cancel, confirm);
        box.getChildren().addAll(title, content, buttons);
        showOverlay(box);
    }

    public static void main(String[] args) {
        launch();
    }
}
