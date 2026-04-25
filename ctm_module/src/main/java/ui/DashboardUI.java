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
import service.TaskService;
import ui.http.HttpAuthClient;
import auth.service.AuthService;
import auth.service.CryptoAdapter;
import auth.session.SessionState;
import service.EncryptedTaskService;
import crypto.DocumentCryptoService;
import crypto.internal.CryptoServiceImpl;
import crypto.NonceCounterStore;
import java.net.http.HttpClient;
import java.nio.file.Paths;
import java.util.List;
import ui.views.LoginView;
import ui.views.RegisterView;
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
    
    // Secure Services
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final CryptoServiceImpl cryptoService = new CryptoServiceImpl();
    private final CryptoAdapter cryptoAdapter = new CryptoAdapter(cryptoService);
    private final AuthService authService = new AuthService(cryptoAdapter);
    private final NonceCounterStore counterStore = new NonceCounterStore(Paths.get("nonce_store.txt"));
    private final DocumentCryptoService documentCryptoService = new DocumentCryptoService(cryptoAdapter, counterStore);
    private EncryptedTaskService encryptedTaskService;
    private TaskService taskService;
    private SessionState sessionState;
    private HttpAuthClient httpAuthClient;

    private ObservableList<Task> taskList;
    
    private DashboardView dashboardView;
    private MyTasksView myTasksView;
    private ui.views.CalendarView calendarView;

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
        } catch (Exception ex) { ex.printStackTrace(); }

        stage.setScene(scene);
        stage.setTitle("Secure Task Manager");
        stage.centerOnScreen();
        stage.show();

        showLoginScreen();
    }

    private void showLoginScreen() {
        mainStack.getChildren().clear();
        mainStack.setStyle("-fx-background-color: #f5f6fa;");
        
        httpAuthClient = new HttpAuthClient(httpClient, "http://localhost:8080");
        
        LoginView loginView = new LoginView(authService, cryptoAdapter, httpAuthClient, session -> {
            UserSession.login(session);
            this.sessionState = session;
            this.encryptedTaskService = new EncryptedTaskService(
                documentCryptoService, 
                sessionState, 
                httpClient, 
                "http://localhost:8080"
            );
            this.taskService = new TaskService(encryptedTaskService);
            initializeDashboard();
        });

        // Add a "Register" button to the login view logic or a separate toggle
        Button goToRegister = new Button("Need an account? Register here");
        goToRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: #4f46e5; -fx-cursor: hand; -fx-padding: 10;");
        goToRegister.setOnAction(e -> showRegistrationScreen());
        
        VBox container = new VBox(10, loginView, goToRegister);
        container.setAlignment(Pos.CENTER);

        mainStack.getChildren().add(container);
    }

    private void showRegistrationScreen() {
        mainStack.getChildren().clear();
        RegisterView registerView = new RegisterView(authService, cryptoAdapter, httpAuthClient, this::showLoginScreen);
        mainStack.getChildren().add(registerView);
    }

    private void validateInputs(String email, String password) throws EmptyFieldException, InvalidEmailException, WeakPasswordException {
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
        mainStack.getChildren().clear();
        
        try {
            // For now, using a mock teamId "1" to fetch tasks
            java.util.List<Task> remoteTasks = encryptedTaskService.fetchTasksByTeam("1");
            taskList = FXCollections.observableArrayList(remoteTasks);
        } catch (Exception e) {
            taskList = FXCollections.observableArrayList();
            showError("Failed to fetch tasks: " + e.getMessage());
        }
        
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
        layout.setStyle("-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");
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
        layout.setStyle("-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");
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